package bg.util;

import java.time.LocalDateTime;

/** Résultat (compatible Java 8, pas de record). */
public class GpsPosition2 {
    private final double latitude;
    private final double longitude;
    private final Double altitudeMeters; // null si absente
 // Rayon moyen de la Terre (m)
    private static final double EARTH_RADIUS_METERS = 6371008.8;
   
    public LocalDateTime getDate() {
		return date;
	}

	private final LocalDateTime date; // null si absente

    public GpsPosition2(double latitude, double longitude, Double altitudeMeters,LocalDateTime takenAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMeters = altitudeMeters;
        this.date=takenAt;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Double getAltitudeMeters() { return altitudeMeters; }
    
    @Override
    public String toString() {
    	String s ="";
    	
        s+= "lat= "+String.format("%07.4f", latitude);
        s+=  " | lon= "+String.format("%07.4f", longitude);
        s+=  " | z = "+String.format("%7.4f ", altitudeMeters);
        s += " date: "+date;
        return s;
    }
    /**
     * Return coordonnées x en metre 
     */
    public Double getX(){
    	return getX(this.longitude);
    }
    /**
     * Return y en metre
     * @return
     */
    public Double getY(){
    	return getY(this.latitude);
    }
    
    public static Double getX( double longitude) {
        // Mercator: x = R * lon(rad)
        double lonRad = Math.toRadians(longitude);
        return EARTH_RADIUS_METERS * lonRad;
    }

    /**
     * Return coordonnées Y en mètres dans un repère "Web Mercator" (EPSG:3857).
     * Attention: Mercator diverge vers +/- infini aux pôles. On clamp la latitude.
     */
    public static Double getY(double latitude) {
         double latRad = Math.toRadians(latitude);

        return EARTH_RADIUS_METERS * latRad;
    }
}
