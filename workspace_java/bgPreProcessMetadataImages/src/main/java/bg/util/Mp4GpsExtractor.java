package bg.util;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

/**
 * Extracts GPS position from MP4/MOV video files using a multi-strategy fallback:
 * <ol>
 *   <li>QuickTime metadata (ISO6709 location atom, from QuickTime Keys or UserData box)</li>
 *   <li>XMP embedded GPS tags</li>
 *   <li>EXIF GPS directory (sometimes present in MP4)</li>
 *   <li>exiftool subprocess fallback (requires exiftool installed on PATH)</li>
 * </ol>
 */
public final class Mp4GpsExtractor {

    /** ISO6709 decimal pattern: e.g. {@code +44.2374+001.4766+100.000/} */
    private static final Pattern ISO6709_DECIMAL =
            Pattern.compile("([+-]\\d+\\.\\d+)([+-]\\d+\\.\\d+)(?:([+-]\\d+(?:\\.\\d+)?))?/?");

    /**
     * DMS pattern used by exiftool text output:
     * e.g. {@code 44 deg 14' 14.64" N}
     */
    private static final Pattern EXIFTOOL_DMS =
            Pattern.compile("(\\d+)\\s*deg\\s+(\\d+)'\\s*([\\d.]+)\"\\s*([NSEW])");

    /**
     * XMP GPS rational pattern: e.g. {@code 44,14.244N} or {@code 44/1 14/1 1464/100 N}
     */
    private static final Pattern XMP_DM =
            Pattern.compile("(\\d+),(\\d+\\.\\d+)([NSEW])");

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Attempts to extract a GPS position from a video file (.mp4, .mov, .MP4, .MOV).
     *
     * @param videoFile the video file to inspect
     * @return a {@link PositionGps2} when coordinates are found, {@code null} otherwise
     */
    public static PositionGps2 extractFromVideoFile(File videoFile) {
        // Strategy 1-3 via metadata-extractor
        PositionGps2 pos = extractWithMetadataExtractor(videoFile);
        if (pos != null) {
            System.out.println("[Mp4GpsExtractor] GPS via metadata-extractor (" + pos.getLatitude()
                    + ", " + pos.getLongitude() + ") for " + videoFile.getName());
            return pos;
        }

        // Strategy 4: exiftool fallback
        pos = extractWithExiftool(videoFile);
        if (pos != null) {
            System.out.println("[Mp4GpsExtractor] GPS via exiftool (" + pos.getLatitude()
                    + ", " + pos.getLongitude() + ") for " + videoFile.getName());
        } else {
            System.err.println("[Mp4GpsExtractor] No GPS found for " + videoFile.getName()
                    + " (tried metadata-extractor + exiftool)");
        }
        return pos;
    }

    // -------------------------------------------------------------------------
    // Strategy 1-3: metadata-extractor
    // -------------------------------------------------------------------------

    static PositionGps2 extractWithMetadataExtractor(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            logDirectories(metadata, file.getName());

            // 1. QuickTime directories: look for ISO6709 location strings
            PositionGps2 pos = extractFromQuickTime(metadata, file.getName());
            if (pos != null) return pos;

            // 2. XMP directory
            pos = extractFromXmp(metadata, file.getName());
            if (pos != null) return pos;

            // 3. EXIF GPS directory (sometimes present in MP4)
            pos = extractFromExifGps(metadata, file.getName());
            if (pos != null) return pos;

        } catch (Exception e) {
            System.err.println("[Mp4GpsExtractor] metadata-extractor error for "
                    + file.getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Iterates all directories whose name contains "QuickTime" and searches for
     * an ISO6709-formatted location string.
     */
    private static PositionGps2 extractFromQuickTime(Metadata metadata, String fileName) {
        for (Directory dir : metadata.getDirectories()) {
            String dirName = dir.getName();
            if (dirName == null) continue;
            if (!dirName.toLowerCase().contains("quicktime")
                    && !dirName.toLowerCase().contains("mp4")) {
                continue;
            }
            for (Tag tag : dir.getTags()) {
                String desc = tag.getDescription();
                if (desc == null) continue;
                // ISO6709 decimal: starts with + or - followed by digits
                if ((desc.startsWith("+") || desc.startsWith("-"))
                        && (desc.contains("+") || desc.lastIndexOf('-') > 0)) {
                    PositionGps2 pos = parseISO6709(desc, fileName);
                    if (pos != null) {
                        System.out.println("[Mp4GpsExtractor]   source: " + dirName
                                + " / " + tag.getTagName() + " = " + desc);
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Looks for GPS coordinates in XMP directories.
     * XMP GPS latitude/longitude are stored as "DDeg MM.mmm Direction" strings.
     */
    private static PositionGps2 extractFromXmp(Metadata metadata, String fileName) {
        for (Directory dir : metadata.getDirectories()) {
            String dirName = dir.getName();
            if (dirName == null || !dirName.toLowerCase().contains("xmp")) continue;

            String latStr = null;
            String lonStr = null;
            for (Tag tag : dir.getTags()) {
                String name = tag.getTagName();
                String desc = tag.getDescription();
                if (desc == null || desc.isEmpty()) continue;
                if (name != null && name.toLowerCase().contains("latitude")
                        && !name.toLowerCase().contains("longitude")) {
                    latStr = desc;
                }
                if (name != null && name.toLowerCase().contains("longitude")) {
                    lonStr = desc;
                }
            }
            if (latStr != null && lonStr != null) {
                Double lat = parseXmpDm(latStr);
                Double lon = parseXmpDm(lonStr);
                if (lat != null && lon != null && isValidGps(lat, lon)) {
                    System.out.println("[Mp4GpsExtractor]   source: XMP lat=" + latStr + " lon=" + lonStr);
                    return new PositionGps2(lat, lon, null, null, fileName);
                }
            }
        }
        return null;
    }

    /**
     * Extracts GPS from a standard EXIF {@link GpsDirectory} (sometimes present in MP4).
     */
    private static PositionGps2 extractFromExifGps(Metadata metadata, String fileName) {
        GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gps == null) return null;

        GeoLocation loc = gps.getGeoLocation();
        if (loc == null) return null;

        double lat = loc.getLatitude();
        double lon = loc.getLongitude();
        if (Double.isNaN(lat) || Double.isNaN(lon) || !isValidGps(lat, lon)) return null;

        Double altitude = extractAltitude(gps);
        LocalDateTime takenAt = extractDateFromMetadata(metadata);
        System.out.println("[Mp4GpsExtractor]   source: EXIF GpsDirectory");
        return new PositionGps2(lat, lon, altitude, takenAt, fileName);
    }

    // -------------------------------------------------------------------------
    // Strategy 4: exiftool
    // -------------------------------------------------------------------------

    /**
     * Runs {@code exiftool -j -n <file>} and parses {@code GPSLatitude} /
     * {@code GPSLongitude} / {@code GPSAltitude} from the JSON output.
     *
     * <p>Requires {@code exiftool} to be installed and on the system PATH.
     */
    static PositionGps2 extractWithExiftool(File file) {
        String json;
        try {
            json = CommandRunner.runCommand("exiftool", "-j", "-n", file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[Mp4GpsExtractor] exiftool not available or failed: " + e.getMessage());
            return null;
        }
        if (json == null || json.isEmpty()) return null;

        Double lat = parseJsonNumber(json, "GPSLatitude");
        Double lon = parseJsonNumber(json, "GPSLongitude");
        if (lat == null || lon == null || !isValidGps(lat, lon)) return null;

        Double alt = parseJsonNumber(json, "GPSAltitude");
        return new PositionGps2(lat, lon, alt, null, file.getName());
    }

    // -------------------------------------------------------------------------
    // Parsing helpers
    // -------------------------------------------------------------------------

    /**
     * Parses an ISO6709 string such as {@code +44.2374+001.4766+100.000/}
     * or {@code +44.2374+001.4766/}.
     *
     * @return a {@link PositionGps2} or {@code null} if the string is not valid
     */
    static PositionGps2 parseISO6709(String iso6709, String fileName) {
        if (iso6709 == null) return null;
        Matcher m = ISO6709_DECIMAL.matcher(iso6709.trim());
        if (!m.find()) return null;
        try {
            double lat = Double.parseDouble(m.group(1));
            double lon = Double.parseDouble(m.group(2));
            if (!isValidGps(lat, lon)) return null;
            Double alt = (m.group(3) != null) ? Double.parseDouble(m.group(3)) : null;
            return new PositionGps2(lat, lon, alt, null, fileName);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an exiftool DMS string such as {@code 44 deg 14' 14.64" N} to a
     * signed decimal degree.
     *
     * @return the decimal degree, or {@code null} if the string is not parseable
     */
    static Double parseExiftoolDms(String dms) {
        if (dms == null) return null;
        Matcher m = EXIFTOOL_DMS.matcher(dms.trim());
        if (!m.find()) return null;
        try {
            double deg = Double.parseDouble(m.group(1));
            double min = Double.parseDouble(m.group(2));
            double sec = Double.parseDouble(m.group(3));
            double decimal = deg + min / 60.0 + sec / 3600.0;
            String dir = m.group(4).toUpperCase();
            if ("S".equals(dir) || "W".equals(dir)) decimal = -decimal;
            return decimal;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an XMP GPS string in decimal-minutes format such as {@code 44,14.244N}.
     */
    static Double parseXmpDm(String xmpGps) {
        if (xmpGps == null) return null;
        // Try XMP DM format: "44,14.244N"
        Matcher m = XMP_DM.matcher(xmpGps.trim());
        if (m.find()) {
            try {
                double deg = Double.parseDouble(m.group(1));
                double min = Double.parseDouble(m.group(2));
                double decimal = deg + min / 60.0;
                String dir = m.group(3).toUpperCase();
                if ("S".equals(dir) || "W".equals(dir)) decimal = -decimal;
                return decimal;
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        // Try plain decimal
        try {
            return Double.parseDouble(xmpGps.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extracts a numeric JSON field value from a single-record JSON array produced
     * by {@code exiftool -j -n}. The field is matched by exact name (case-sensitive).
     *
     * @param json      the raw JSON string
     * @param fieldName the field name to look up (e.g. {@code "GPSLatitude"})
     * @return the numeric value, or {@code null} if not found
     */
    static Double parseJsonNumber(String json, String fieldName) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*([+-]?[\\d.]+(?:[eE][+-]?\\d+)?)");
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        try {
            return Double.parseDouble(m.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static Double extractAltitude(GpsDirectory gps) {
        Rational altRat = gps.getRational(GpsDirectory.TAG_ALTITUDE);
        if (altRat == null) return null;
        double alt = altRat.doubleValue();
        Integer ref = gps.getInteger(GpsDirectory.TAG_ALTITUDE_REF);
        if (ref != null && ref == 1) alt = -alt;
        return alt;
    }

    private static LocalDateTime extractDateFromMetadata(Metadata metadata) {
        ExifSubIFDDirectory subIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        Date d = null;
        if (subIfd != null) {
            d = subIfd.getDateOriginal();
            if (d == null) d = subIfd.getDateDigitized();
        }
        if (d == null) {
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) d = ifd0.getDate(ExifIFD0Directory.TAG_DATETIME);
        }
        return (d != null) ? LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()) : null;
    }

    /**
     * Validates GPS coordinates are within meaningful geographic bounds.
     */
    static boolean isValidGps(double lat, double lon) {
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0
                && !(lat == 0.0 && lon == 0.0);
    }

    private static void logDirectories(Metadata metadata, String fileName) {
        System.out.println("[Mp4GpsExtractor] Directories found in " + fileName + ":");
        for (Directory dir : metadata.getDirectories()) {
            System.out.println("  - " + dir.getName() + " (" + dir.getTagCount() + " tags)");
        }
    }
}
