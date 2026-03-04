package bg.util;

import java.lang.Math;

/**
 * Conversions WGS84 (lat,lon,alt) <-> ECEF and ECEF -> ENU.
 * Compatible Java 1.8 (no records, no var).
 */
public final class Wgs84EcefEnu {

    // WGS84 ellipsoid constants
    private static final double A = 6378137.0;                // semi-major axis (m)
    private static final double F = 1.0 / 298.257223563;      // flattening
    private static final double E2 = F * (2.0 - F);           // first eccentricity squared

    private Wgs84EcefEnu() {}

    public static final class Vec3 {
        public final double x;
        public final double y;
        public final double z;

        public Vec3(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }

        @Override public String toString() {
            return "Vec3{x=" + x + ", y=" + y + ", z=" + z + "}";
        }
    }

    public static final class Enu {
        public final double east;
        public final double north;
        public final double up;

        public Enu(double east, double north, double up) {
            this.east = east; this.north = north; this.up = up;
        }

        @Override public String toString() {
            return "Enu{east=" + east + ", north=" + north + ", up=" + up + "}";
        }
    }

    /** Convertit WGS84 géodésique (degrés, degrés, mètres) -> ECEF (mètres). */
    public static Vec3 wgs84ToEcef(double latDeg, double lonDeg, double altMeters) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);

        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double sinLon = Math.sin(lon);
        double cosLon = Math.cos(lon);

        // Rayon de courbure dans le plan vertical (prime vertical)
        double N = A / Math.sqrt(1.0 - E2 * sinLat * sinLat);

        double x = (N + altMeters) * cosLat * cosLon;
        double y = (N + altMeters) * cosLat * sinLon;
        double z = (N * (1.0 - E2) + altMeters) * sinLat;

        return new Vec3(x, y, z);
    }

    /**
     * Convertit un point ECEF -> ENU local (Est, Nord, Haut) par rapport à une origine.
     *
     * @param pEcef          point cible en ECEF (m)
     * @param originEcef     origine en ECEF (m)
     * @param originLatDeg   latitude origine (deg)
     * @param originLonDeg   longitude origine (deg)
     */
    public static Enu ecefToEnu(Vec3 pEcef, Vec3 originEcef, double originLatDeg, double originLonDeg) {
        double lat0 = Math.toRadians(originLatDeg);
        double lon0 = Math.toRadians(originLonDeg);

        double sinLat0 = Math.sin(lat0);
        double cosLat0 = Math.cos(lat0);
        double sinLon0 = Math.sin(lon0);
        double cosLon0 = Math.cos(lon0);

        double dx = pEcef.x - originEcef.x;
        double dy = pEcef.y - originEcef.y;
        double dz = pEcef.z - originEcef.z;

        // Rotation ECEF -> ENU
        double east  = -sinLon0 * dx + cosLon0 * dy;
        double north = -sinLat0 * cosLon0 * dx - sinLat0 * sinLon0 * dy + cosLat0 * dz;
        double up    =  cosLat0 * cosLon0 * dx + cosLat0 * sinLon0 * dy + sinLat0 * dz;

        return new Enu(east, north, up);
    }

    /** Raccourci : WGS84 -> ENU autour d'une origine WGS84. */
    public static Enu wgs84ToEnu(double latDeg, double lonDeg, double altMeters,
                                 double originLatDeg, double originLonDeg, double originAltMeters) {
        Vec3 p = wgs84ToEcef(latDeg, lonDeg, altMeters);
        Vec3 o = wgs84ToEcef(originLatDeg, originLonDeg, originAltMeters);
        return ecefToEnu(p, o, originLatDeg, originLonDeg);
    }
}