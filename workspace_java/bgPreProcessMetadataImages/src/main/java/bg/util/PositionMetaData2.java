package bg.util;

import java.time.LocalDateTime;

public class PositionMetaData2 {

	private  Double altitudeMeters; // null si absente
	private  String imageName;
	private Double xx;
	private Double yy;
	private Double zz;
	private Double altitudeSol = 0.0d; // Prendre l'altitude du lieu voir: class ElevationClient
	private Double angleOuvertureCamera = 60.0d;
	private Double xCorrected;
	private Double yCorrected;
	private double rView;
	private double pitch;
	private double yaw;
	private double roll;
	
	
	public double getrView() {
		return rView;
	}
	public void setrView(double rView) {
		this.rView = rView;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	private LocalDateTime date; // null si absente
	
	public Double getAltitudeMeters() {
		return altitudeMeters;
	}
	public void setAltitudeMeters(Double altitudeMeters) {
		this.altitudeMeters = altitudeMeters;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public Double getXx() {
		return xx;
	}
	public void setXx(Double xx) {
		this.xx = xx;
	}
	public Double getYy() {
		return yy;
	}
	public void setYy(Double yy) {
		this.yy = yy;
	}
	public Double getZz() {
		return zz;
	}
	public void setZz(Double zz) {
		this.zz = zz;
	}
	public Double getAltitudeSol() {
		return altitudeSol;
	}
	public void setAltitudeSol(Double altitudeSol) {
		this.altitudeSol = altitudeSol;
	}
	public Double getAngleOuvertureCamera() {
		return angleOuvertureCamera;
	}
	public void setAngleOuvertureCamera(Double angleOuvertureCamera) {
		this.angleOuvertureCamera = angleOuvertureCamera;
	}
	public Double getxCorrected() {
		return xCorrected;
	}
	public void setxCorrected(Double xCorrected) {
		this.xCorrected = xCorrected;
	}
	public Double getyCorrected() {
		return yCorrected;
	}
	public void setyCorrected(Double yCorrected) {
		this.yCorrected = yCorrected;
	}
	public double getPitch() {
		return pitch;
	}
	public void setPitch(double pitch) {
		this.pitch = pitch;
	}
	public double getYaw() {
		return yaw;
	}
	public void setYaw(double yaw) {
		this.yaw = yaw;
	}
	public double getRoll() {
		return roll;
	}
	public void setRoll(double roll) {
		this.roll = roll;
	}
	@Override
	public String toString() {
		return toString2()+"  "+toString3();
	}
	public String toString2() {
	    return String.format(
	            java.util.Locale.US,
	            "imageName=%s, x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f, roll=%.2f",
	            imageName,
	            (xx != null ? xx : Double.NaN),
	            (yy != null ? yy : Double.NaN),
	            (zz != null ? zz : Double.NaN),
	            yaw,
	            pitch,
	            roll
	    );
	}
	public String toString2_csv() {
	    return String.format(
	            java.util.Locale.US,
	            "%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
	            imageName,
	            (xx != null ? xx : Double.NaN),
	            (yy != null ? yy : Double.NaN),
	            (zz != null ? zz : Double.NaN),
	            yaw,
	            pitch,
	            roll
	    );
	}
	public String toString3() {
	    return String.format(
	            java.util.Locale.US,
	            "altitudeMeters=%s, altitudeSol=%.2f, angleOuvertureCamera=%.2f, xCorrected=%s, yCorrected=%s, rView=%.2f, date=%s",
	            (altitudeMeters != null ? String.format(java.util.Locale.US, "%.2f", altitudeMeters) : "null"),
	            (altitudeSol != null ? altitudeSol : Double.NaN),
	            (angleOuvertureCamera != null ? angleOuvertureCamera : Double.NaN),
	            (xCorrected != null ? String.format(java.util.Locale.US, "%.2f", xCorrected) : "null"),
	            (yCorrected != null ? String.format(java.util.Locale.US, "%.2f", yCorrected) : "null"),
	            rView,
	            (date != null ? date.toString() : "null")
	    );
	}

}
