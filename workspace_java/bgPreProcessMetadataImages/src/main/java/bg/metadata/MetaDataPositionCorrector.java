package bg.metadata;



import bg.util.PositionGps2;

/**
 * Service utilitaire chargé de calculer la position corrigée
 * et le rayon de vue à partir des métadonnées de prise de vue.
 */
public final class MetaDataPositionCorrector {

	private static final double DEFAULT_ALTITUDE_SOL = 0.0;
	private static final double DEFAULT_CAMERA_FOV_DEGREES = 60.0;

	private MetaDataPositionCorrector() {
		// Utility class
	}

	public static CorrectionResult compute(
			double x,
			double y,
			double z,
			double yaw,
			double pitch,
			PositionGps2 positionGps) {

		return compute(
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
			double x,
			double y,
			double z,
			double yaw,
			double pitch,
			PositionGps2 positionGps,
			double altitudeSol,
			double angleOuvertureCamera) {

		double zz;
		double xx;
		double yy;

		if (positionGps == null) {
			zz = z + 50;
			xx = x;
			yy = y;
		} else {
			zz = positionGps.getAltitudeMeters();
			xx = positionGps.getX_process();
			yy = positionGps.getY();
		}

		double hauteur = zz - altitudeSol;
		double delta = hauteur * Math.cos(Math.toRadians(pitch));
		double xCorrected = xx + delta * Math.cos(Math.toRadians(yaw));
		double yCorrected = yy + delta * Math.sin(Math.toRadians(yaw));
		double rView = Math.abs(hauteur * Math.sin(Math.toRadians(angleOuvertureCamera)));

		return new CorrectionResult(xCorrected, yCorrected, rView);
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