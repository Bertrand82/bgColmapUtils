package bg.util;

import java.time.LocalDateTime;

/** Résultat (compatible Java 8, pas de record). */
public class GpsPosition2 {
    private final double latitude;
    private final double longitude;
    private final Double altitudeMeters; // null si absente
    private final String imageName;
 // Rayon moyen de la Terre (m)
    private static final double EARTH_RADIUS_METERS = 6371008.8;
    private final double x;
    private final double y;
   
    public LocalDateTime getDate() {
		return date;
	}

	private final LocalDateTime date; // null si absente

    public GpsPosition2(double latitude, double longitude, Double altitudeMeters,LocalDateTime takenAt, String imageName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMeters = altitudeMeters;
        this.date=takenAt;
        this.imageName=imageName;
        this.x = getX_process();
        this.y = getY_process();
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Double getAltitudeMeters() { return altitudeMeters; }
    public double getX() {return this.x;}
    public double getY() {return this.y;}
    
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
    public Double getX_process(){
    	return getX_process(this.longitude);
    }
    /**
     * Return y en metre
     * @return
     */
    public Double getY_process(){
    	return getY_process(this.latitude);
    }
    
    public static Double getX_process( double longitude) {
        // Mercator: x = R * lon(rad)
        double lonRad = Math.toRadians(longitude);
        return EARTH_RADIUS_METERS * lonRad;
    }

    /**
     * Return coordonnées Y en mètres dans un repère "Web Mercator" (EPSG:3857).
     * Attention: Mercator diverge vers +/- infini aux pôles. On clamp la latitude.
     */
    public static Double getY_process(double latitude) {
         double latRad = Math.toRadians(latitude);

        return EARTH_RADIUS_METERS * latRad;
    }
}
