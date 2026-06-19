package bg.metadata;



import bg.util.PositionGps2;

/**
 * Service utilitaire chargé de calculer la position corrigée
 * et le rayon de vue à partir des métadonnées de prise de vue.
 */
public final class MetaDataPositionCorrector {

	private static final double DEFAULT_ALTITUDE_SOL = -1.0;
	private static final double DEFAULT_CAMERA_FOV_DEGREES = 60.0;

	private MetaDataPositionCorrector() {
		// Utility class
	}

	public static CorrectionResult compute(
			String fileName,
			double x,
			double y,
			double z,
			Double yaw,
			Double pitch,
			PositionGps2 positionGps) {

		return compute(
				 fileName,
				x,
				y,
				z,
				yaw,
				pitch,
				positionGps,
				DEFAULT_ALTITUDE_SOL,
				DEFAULT_CAMERA_FOV_DEGREES);
	}

	public static CorrectionResult compute(
			String fileName,
			double x,
			double y,
			double z,
			Double yaw,
			Double pitch,
			PositionGps2 positionGps,
			Double altitudeSol,
			Double angleOuvertureCamera_degre) {

		double zz;
		double xx;
		double yy;

		try {
			if (positionGps == null) {
				zz = z + 50;
				xx = x;
				yy = y;
			} else {
				zz = positionGps.getAltitudeMeters();
				xx = positionGps.getX_process();
				yy = positionGps.getY_process();
			}
			double yaw_degre = yaw*180/Math.PI;
			// hauteur de 5 metres par default
			double hauteur =(altitudeSol==DEFAULT_ALTITUDE_SOL)? 5.0d : (zz - altitudeSol);
			double delta = hauteur * Math.cos(Math.toRadians(pitch));
			// x est associé à la longitude 
			double xCorrected = xx  + delta * Math.sin(yaw);
			// y est associé à la latitude (0 sur l'equateur)
			double yCorrected = yy- delta * Math.cos(yaw);
			double rView = Math.abs(hauteur * Math.sin(Math.toRadians(angleOuvertureCamera_degre)));

			return new CorrectionResult(xCorrected, yCorrected, rView);
		} catch (Exception e) {
			System.err.println("metadata compute exception : "+e.getMessage());
			return new CorrectionResult(x, y, 20);
		}
	}

	public static final class CorrectionResult {
		private final double xCorrected;
		private final double yCorrected;
		private final double rView;

		public CorrectionResult(double xCorrected, double yCorrected, double rView) {
			this.xCorrected = xCorrected;
			this.yCorrected = yCorrected;
			this.rView = rView;
		}

		public double getxCorrected() {
			return xCorrected;
		}

		public double getyCorrected() {
			return yCorrected;
		}

		public double getrView() {
			return rView;
		}
	}
}