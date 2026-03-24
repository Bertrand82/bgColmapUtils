package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import bg.display.images.gui.DisplayImagesPanel;
import bg.images.matcher.factory.PreMatcher;
import bg.util.GpsPosition2;
import bg.util.GpsPositionFactory;

public class DisplayTogetherPanel extends JPanel {

	Canvas canvas;
	List<GpsPosition2> listPositions;
	final List<Bean> listBeans = new ArrayList<DisplayTogetherPanel.Bean>();
	int w =500;
	int h =500;
	double xMin = 0;
	double xMax = 0;
	double yMin = 0;
	double yMax = 0;
	File dirImages;

	public DisplayTogetherPanel(File dir) throws Exception {
		dirImages = new File(dir, "images");

		File fileMetadata = new File(dir, "metadata.csv");
		this.initListPositions();

		canvas = new Canvas() {
			@Override
			public void paint(Graphics g) {
				DisplayTogetherPanel.this.paintImage(g);
			}

			@Override
			public void update(Graphics g) {
				paint(g);
			}
		};
		this.setLayout(new BorderLayout());
		this.add(canvas);
		Dimension dim = new Dimension(w,h);
		canvas.setPreferredSize(dim);

	}

	private void initListPositions() {
		long timeStart = System.currentTimeMillis();
		this.listPositions = GpsPositionFactory.getListGpsPositionFromDirImages(dirImages);
		
		long duree_ms = System.currentTimeMillis() - timeStart;
		System.out.println("List gps size  " + listPositions.size() + "    duree (secondes) : " + (duree_ms / 1000));
		GpsPosition2 first = listPositions.get(0);
		xMin = xMax = first.getX();
		yMin = yMax = first.getY();
		for (GpsPosition2 gps : listPositions) {
			double x = gps.getX();
			double y = gps.getY();

			if (x < xMin) {
				xMin = x;
			}
			if (x > xMax) {
				xMax = x;
			}
			if (y < yMin) {
				yMin = y;
			}
			if (y > yMax) {
				yMax = y;
			}
		}
		double scale =  Math. min (w,h) / Math.max((yMax-yMin), (xMax-xMin));
		for (GpsPosition2 gps : listPositions) { 
			int px =(int) (scale* (gps.getX()- xMin));
			int py =(int) (scale* (gps.getY()- yMin));
			Bean bean = new Bean(gps, px, py);
			this.listBeans.add(bean);
		}
	}
	static class Bean {
		GpsPosition2 gps;
		int px;
		int py;
		public Bean(GpsPosition2 gps2, int px2, int py2) {
			this.gps=gps2;
			this.px =px2;
			this.py=py2;
		}
		
	}

	private void paintImage(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.RED);
		for(Bean bean : this.listBeans) {
			//g.fillOval(1, 1, bean.px, bean.py);
			g.fillRect( bean.px,bean.py,3,3);
		}
	}

}
