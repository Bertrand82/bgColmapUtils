package bg.util;

import java.awt.Color;
import java.time.LocalDateTime;

/** Résultat (compatible Java 8, pas de record). */
public class PositionGps2 {
	enum ORIENTATIONS {
		Normal(1,0), ROTATE_180(2,180), ROTATE_90_CW(6,90), ROTATE_270_CW(8,270);

		String i = "";
		int angle_degre ;
		ORIENTATIONS(int ii, int angle) {
			this.i =""+ ii;
			angle_degre=angle;
		};
		public static ORIENTATIONS getORIENTATION(Object ii){
			for( ORIENTATIONS p: ORIENTATIONS.values()) {
				if (p.i.equals(""+ii)) {
					return p;
				}
			}
			return Normal;
		}
	}

	private final double latitude;
	private final double longitude;
	private final Double altitudeMeters; // null si absente
	public void setYaw(Double yaw) {
		this.yaw = yaw;
	}

	public void setPitch(Double pitch) {
		this.pitch = pitch;
	}

	public void setRoll(Double roll) {
		this.roll = roll;
	}


	private final String imageName;
	// Rayon moyen de la Terre (m)
	private static final double EARTH_RADIUS_METERS = 6371008.8;
	private final double x;
	private final double y;
	private final LocalDateTime date; // null si absente
	private int numeroPaquet = -1;
	private Color color = Color.BLACK;
	String orientation = null;
	String gpsImgDirection = null;
	String gpsImgDirectionRef = null;
	Double yaw = null;
	Double pitch = null;
	Double roll = null;

	public LocalDateTime getDate() {
		return date;
	}

	public PositionGps2(double latitude, double longitude, Double altitudeMeters, LocalDateTime takenAt,
			String imageName, String orientation, String gpsImgDirection, String gpsImgDirectionRef, Double yaw,
			Double pitch, Double roll) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitudeMeters = altitudeMeters;
		this.date = takenAt;
		this.imageName = imageName;
		this.x = getX_process();
		this.y = getY_process();
		this.orientation = orientation;
		this.gpsImgDirection = gpsImgDirection;
		this.gpsImgDirectionRef = gpsImgDirectionRef;
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public Double getAltitudeMeters() {
		return altitudeMeters;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public String getImageName() {
		return this.imageName;
	}

	@Override
	public String toString() {
		String s = "";

		s += "lat= " + String.format("%07.4f", latitude);
		s += " | lon= " + String.format("%07.4f", longitude);
		s += " | z = " + String.format("%7.4f ", altitudeMeters);
		s += " | date: " + date;
		s += " | Positions : " + toDegreMinuteSeconde(latitude, true);
		s += "   " + toDegreMinuteSeconde(longitude, false);
		s += " |googleMap : " + toGoogleMapFormat(latitude, longitude);
		s += " | orientaion : " + orientation+" "+ORIENTATIONS.getORIENTATION(orientation);
		s += " | gpsImgDirection " + this.gpsImgDirection;
		s += " | gpsImgDirectionRef " + this.gpsImgDirectionRef;
		s += " | yaw " + this.yaw;
		s += " pitch " + this.pitch;
		s += " roll " + this.roll;
		return s;
	}

	/**
	 * Return coordonnées x en metre
	 */
	public Double getX_process() {
		return getX_process(this.longitude);
	}

	/**
	 * Return y en metre
	 * 
	 * @return
	 */
	public Double getY_process() {
		return getY_process(this.latitude);
	}

	public static Double getX_process(double longitude) {
		// Mercator: x = R * lon(rad)
		double lonRad = Math.toRadians(longitude);
		return EARTH_RADIUS_METERS * lonRad;
	}

	/**
	 * Return coordonnées Y en mètres dans un repère "Web Mercator" (EPSG:3857).
	 * Attention: Mercator diverge vers +/- infini aux pôles. On clamp la latitude.
	 */
	public static Double getY_process(double latitude) {
		double latRad = -1 * Math.toRadians(latitude);

		return EARTH_RADIUS_METERS * latRad;
	}

	public int getNumeroPaquet() {
		return numeroPaquet;
	}

	public void setNumeroPaquet(int numeroPaquet) {
		this.numeroPaquet = numeroPaquet;
		this.color = UtilColor.colorFor(numeroPaquet);
	}
	
	

	public String getGpsImgDirection() {
		return gpsImgDirection;
	}

	public String getGpsImgDirectionRef() {
		return gpsImgDirectionRef;
	}

	public Double getYaw() {
		return yaw;
	}

	public Double getPitch() {
		return pitch;
	}

	public Double getRoll() {
		return roll;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public Color getColor() {

		return color;
	}

	public static String toDegreMinuteSeconde(double value, boolean isLatitude) {
		double absValue = Math.abs(value);

		int degrees = (int) absValue;
		double minutesFull = (absValue - degrees) * 60;
		int minutes = (int) minutesFull;
		double seconds = (minutesFull - minutes) * 60;

		String direction;
		if (isLatitude) {
			direction = value >= 0 ? "N" : "S";
		} else {
			direction = value >= 0 ? "E" : "W";
		}

		return String.format("%d°%d'%2.2f\" %s", degrees, minutes, seconds, direction);
	}

	public static String toGoogleMapFormat(double latitude, double longitude) {
		return String.format(java.util.Locale.US, "%.6f,%.6f", latitude, longitude);
	}

	
		public String toString2_csv() {
			double x_ =this.getX();
			double y_ = this.getY();
			double z_ =this.getAltitudeMeters();
		    return String.format(
		    		
		            java.util.Locale.US,
		            "%s,%.2f,%.2f,%.2f,%.4f,%.4f,%.4f",
		            imageName,
		            x_,
		            y_,
		            z_,
		            yaw,
		            pitch,
		            roll
		    );
		}
	}


