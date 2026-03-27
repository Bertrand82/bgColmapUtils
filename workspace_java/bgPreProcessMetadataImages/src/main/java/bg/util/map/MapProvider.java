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
		this(longMax, longMin, latMax, latMin,listener, 16);
	}

	public MapProvider(double longMax, double longMin, double latMax, double latMin, MapProviderListener listener, int zoom) {
		super();
		this.longMax = longMax;
		this.longMin = longMin;
		this.latMax = latMax;
		this.latMin = latMin;
		this.zoom = zoom;
		this.listener=listener;
		Thread thr = new Thread(this);
		thr.start();
	}

	@Override
	public void run() {
		fetchImages();
	}

	public void fetchImages() {
		try {
			BufferedImage img = UtilMap.fetchBbox(latMin, longMin, latMax, longMax, zoom);
			if (this.listener==null) {
				System.err.println("Warning no listener for image ");
			}else {
			this.listener.updateMapImage(img);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
