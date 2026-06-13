package bg.util;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

public final class PositionGps2Factory {

    /** File extensions handled as video files. */
    private static final java.util.Set<String> VIDEO_EXTENSIONS =
            new java.util.HashSet<>(java.util.Arrays.asList("mp4", "mov", "MP4", "MOV"));

    /**
     * Extrait latitude/longitude et altitude (si présente).
     * Pour les fichiers vidéo (.mp4, .mov) délègue à {@link Mp4GpsExtractor}
     * qui tente plusieurs sources (QuickTime, XMP, EXIF, exiftool).
     *
     * @return null si aucune info GPS exploitable.
     */
    public static PositionGps2 extractPosition(File imageFile)  {
        if (imageFile == null || !imageFile.exists()) return null;

        String name = imageFile.getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String ext = name.substring(dot + 1);
            if (VIDEO_EXTENSIONS.contains(ext)) {
                return Mp4GpsExtractor.extractFromVideoFile(imageFile);
            }
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
           
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps == null) return null;

            GeoLocation loc = gps.getGeoLocation();
            if (loc == null) return null;

            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            if (Double.isNaN(lat) || Double.isNaN(lon)) return null;

            // Altitude optionnelle
            Double altitude = null;

            // TAG_ALTITUDE: Rational (souvent en mètres)
            Rational altRat = gps.getRational(GpsDirectory.TAG_ALTITUDE);
            if (altRat != null) {
                altitude = altRat.doubleValue();

                // TAG_ALTITUDE_REF: 0 = au-dessus du niveau mer, 1 = en dessous
                Integer ref = gps.getInteger(GpsDirectory.TAG_ALTITUDE_REF);
                if (ref != null && ref.intValue() == 1) {
                    altitude = -altitude;
                }
            }
            LocalDateTime takenAt = null;

            ExifSubIFDDirectory subIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date d = null;
            if (subIfd != null) {
                d = subIfd.getDateOriginal(); // DateTimeOriginal
                if (d == null) d = subIfd.getDateDigitized(); // DateTimeDigitized
            }
            if (d == null) {
                ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (ifd0 != null) d = ifd0.getDate(ExifIFD0Directory.TAG_DATETIME); // DateTime (fallback)
            }

            if (d != null) {
                // Attention: EXIF n’a souvent pas de timezone -> on convertit avec la timezone locale.
                takenAt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            }


            return new PositionGps2(lat, lon, altitude,takenAt,imageFile.getName());
        } catch (Exception e) {
            // format non supporté / EXIF illisible
            return null;
        }
        
        
      
    }
   
    public static List<PositionGps2> getListGpsPositionFromDirImages(File dir) {
    	List<PositionGps2> listA = new ArrayList<PositionGps2>();
    	if (dir == null) {
    		return listA;
    	}
    	if (!dir.exists()) {
    		return listA;
    	}
    	for(File fImage : dir.listFiles()) {
    		PositionGps2 gps = extractPosition(fImage);
    		if (gps == null) {
    			System.err.println("gps is null "+fImage.getName());
    		}else {
    			listA.add(gps);
    		}
    	}
    	return listA;
    }
  
}