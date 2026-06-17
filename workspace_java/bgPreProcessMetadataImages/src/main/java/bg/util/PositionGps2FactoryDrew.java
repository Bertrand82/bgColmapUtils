package bg.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

@Deprecated
public final class PositionGps2FactoryDrew {

    private PositionGps2FactoryDrew() {
    }

    /**
     * Extrait latitude/longitude, altitude, date, orientation, GPS direction,
     * et yaw/pitch/roll depuis les métadonnées EXIF.
     *
     * @return null si aucune info GPS exploitable.
     */
    public static PositionGps2 extractPosition(File imageFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory == null) {
                return null;
            }

            GeoLocation loc = gpsDirectory.getGeoLocation();
            if (loc == null) {
                return null;
            }

            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                return null;
            }

            Double altitude = null;
            Rational altRat = gpsDirectory.getRational(GpsDirectory.TAG_ALTITUDE);
            if (altRat != null) {
                altitude = altRat.doubleValue();
                Integer ref = gpsDirectory.getInteger(GpsDirectory.TAG_ALTITUDE_REF);
                if (ref != null && ref.intValue() == 1) {
                    altitude = -altitude;
                }
            }

            LocalDateTime takenAt = null;
            Date d = null;

            String orientation = null;
            String gpsImgDirection = null;
            String gpsImgDirectionRef = null;
            Double yaw = null;
            Double pitch = null;
            Double roll = null;

            ExifSubIFDDirectory subIfdDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (subIfdDirectory != null) {
                d = subIfdDirectory.getDateOriginal();
                if (d == null) {
                    d = subIfdDirectory.getDateDigitized();
                }
            }

            if (ifd0Directory != null) {
                if (d == null) {
                    d = ifd0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
                }
                orientation = ifd0Directory.getDescription(ExifIFD0Directory.TAG_ORIENTATION);
            }

            if (d != null) {
                takenAt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            }

            Rational gpsDirRat = gpsDirectory.getRational(GpsDirectory.TAG_IMG_DIRECTION);
            if (gpsDirRat != null) {
                gpsImgDirection = String.valueOf(gpsDirRat.doubleValue());
            }

            gpsImgDirectionRef = gpsDirectory.getString(GpsDirectory.TAG_IMG_DIRECTION_REF);
            if (gpsImgDirectionRef == null) {
                gpsImgDirectionRef = gpsDirectory.getDescription(GpsDirectory.TAG_IMG_DIRECTION_REF);
            }

            // Lecture brute du UserComment via Apache Commons Imaging
            String userComment = readUserCommentWithCommonsImaging(imageFile);

            if (userComment != null) {
                yaw = getAngleFromUserComment(userComment, "Yaw");
                pitch = getAngleFromUserComment(userComment, "Pitch");
                roll = getAngleFromUserComment(userComment, "Roll");
            }

            return new PositionGps2(
                    lat,
                    lon,
                    altitude,
                    takenAt,
                    imageFile.getName(),
                    orientation,
                    gpsImgDirection,
                    gpsImgDirectionRef,
                    yaw,
                    pitch,
                    roll
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readUserCommentWithCommonsImaging(File imageFile) {
        try {
            ImageMetadata metadata = Imaging.getMetadata(imageFile);
            if (!(metadata instanceof JpegImageMetadata jpegMetadata)) {
                return null;
            }

            TiffField userCommentField =
                    jpegMetadata.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_USER_COMMENT);

            if (userCommentField == null) {
                return null;
            }

            byte[] bytes = getByteArray(userCommentField);
            return decodeExifUserComment(bytes);

        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] getByteArray(TiffField field) {
        if (field == null) {
            return null;
        }

        try {
            return field.getByteArrayValue();
        } catch (Exception e) {
            // ignore
        }

        try {
            Object value = field.getValue();
            if (value instanceof byte[] bytes) {
                return bytes;
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    private static String decodeExifUserComment(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (bytes.length >= 8) {
            String prefix = new String(bytes, 0, 8, StandardCharsets.US_ASCII);
            byte[] content = Arrays.copyOfRange(bytes, 8, bytes.length);

            if (prefix.startsWith("ASCII")) {
                return new String(content, StandardCharsets.UTF_8)
                        .replace("\0", "")
                        .trim();
            }
        }

        return new String(bytes, StandardCharsets.UTF_8)
                .replace("\0", "")
                .trim();
    }

    private static Double getAngleFromUserComment(String userComment, String key) {
        if (userComment == null || key == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(key + "\\s*:\\s*([-+]?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(userComment);
        String s = matcher.find() ? matcher.group(1) : null;
        Double v_radient = null;
        try {
			double v_degre = Double.parseDouble(s);
			v_radient= Math.toRadians(v_degre);
		} catch (NumberFormatException e) {
			
		}
        return v_radient;
    }
}