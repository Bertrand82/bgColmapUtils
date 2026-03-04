package bg.util;
import java.io.File;
import java.io.IOException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

public final class GpsExifExtractor {

    private GpsExifExtractor() {}

    /** Résultat (compatible Java 8, pas de record). */
    public static final class GpsPosition {
        private final double latitude;
        private final double longitude;
        private final Double altitudeMeters; // null si absente

        public GpsPosition(double latitude, double longitude, Double altitudeMeters) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitudeMeters = altitudeMeters;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public Double getAltitudeMeters() { return altitudeMeters; }

        @Override
        public String toString() {
            return "GpsPosition{lat=" + latitude + ", lon=" + longitude + ", alt=" + altitudeMeters + "}";
        }
    }

    /**
     * Extrait latitude/longitude et altitude (si présente) depuis l'EXIF GPS.
     * @return null si aucune info GPS exploitable.
     */
    public static GpsPosition extractPosition(File imageFile) throws IOException {
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

            return new GpsPosition(lat, lon, altitude);
        } catch (ImageProcessingException e) {
            // format non supporté / EXIF illisible
            return null;
        }
    }
}