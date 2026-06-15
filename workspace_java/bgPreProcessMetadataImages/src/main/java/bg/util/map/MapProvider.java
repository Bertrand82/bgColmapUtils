package bg.util.map;

import java.awt.image.BufferedImage;

public class MapProvider implements Runnable {

	final double longMax;
	final double longMin;
	final double latMax;
	final double latMin;
	int zoom = 16;
	MapProviderListener listener;

	public MapProvider(double longMax, double longMin, double latMax, double latMin, MapProviderListener listener) {
		this(longMax, longMin, latMax, latMin,listener, getZoom(longMax,longMin,latMax,latMin));// 16 valeur pour miami // 19 pour rue Cariben // 20 : erreur 400
	}

	

	public MapProvider(double longMax, double longMin, double latMax, double latMin, MapProviderListener listener, int zoom) {
		super();
		this.longMax = longMax;
		this.longMin = longMin;
		this.latMax = latMax;
		this.latMin = latMin;
		this.zoom = zoom;
		this.listener=listener;
		System.out.println("MapProvider Init "+this);
		Thread thr = new Thread(this);
		thr.start();
	}
	
	private static int getZoom(double longitudeMax2, double longitudeMin2, double latitudeMax2, double latitudeMin2) {
		double dx = delta_metre( latitudeMax2, latitudeMin2);
		double dy = delta_metre( longitudeMax2,longitudeMin2);
		double dMin = Math.min(dx, dy);
		int zoom;
		if (dMin < 50.0) {
			zoom = 19;
		}else if (dMin <100.0) {
			zoom =18;
		}else if (dMin <500.0) {
			zoom =17;
		}else {
			zoom =16;
		}
		return zoom;
	}

	private static double delta_metre(double latitudeMax2, double latitudeMin2) {
	    final double EARTH_RADIUS = 6_371_000.0; // mètres
	    double deltaRadians = Math.toRadians(latitudeMax2 - latitudeMin2);
	    return Math.abs(deltaRadians * EARTH_RADIUS);
	}


	@Override
	public void run() {
		fetchImages();
	}

	public void fetchImages() {
		try {
			BufferedImage img = UtilMap.fetchBbox(latMin, longMin, latMax, longMax, zoom);
			if (this.listener==null) {
				System.err.println("MapProvider Warning no listener for image ");
			}else {
				this.listener.updateMapImage(img);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {
		return "MapProvider [longMax=" + longMax + ", longMin=" + longMin + ", latMax=" + latMax + ", latMin=" + latMin
				+ ", zoom=" + zoom + ", listener=" + listener + "]";
	}

	
}
