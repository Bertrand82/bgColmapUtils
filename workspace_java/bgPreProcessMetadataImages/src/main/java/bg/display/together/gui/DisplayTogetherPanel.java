package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import bg.util.GpsPosition2;
import bg.util.GpsPositionFactory;

public class DisplayTogetherPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Canvas canvas;
	PreviewImage previewImage = new PreviewImage();
	
	List<GpsPosition2> listPositions;
	final List<Bean> listBeans = new ArrayList<DisplayTogetherPanel.Bean>();
	int w =500;
	int h =500;
	double xMin = 0;
	double xMax = 0;
	double yMin = 0;
	double yMax = 0;
	File dirImages;
	double scale=0.1d;
	int selectX1=0;
	int selectY1=0;
	int selectX2 =0;
	int selectY2=0;
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
		this.add(canvas,BorderLayout.CENTER);
		this.add(previewImage,BorderLayout.WEST);
		Dimension dim = new Dimension(w,h);
		canvas.setPreferredSize(dim);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
		    @Override
		    public void componentResized(java.awt.event.ComponentEvent e) {
		        java.awt.Dimension dim = getSize();
		        resizeInit(dim);
		        repaint();
		    }
		});
		
		

		canvas.addMouseListener(new java.awt.event.MouseAdapter() {
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent e) {
		        // Coordonnées du clic dans le panel
		        int x = e.getX();
		        int y = e.getY();

		        // Bouton cliqué
		        if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
		            System.out.println("Left click at " + x + "," + y);
		        } else if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
		            displayImage(x,y);
		        }

		        
		    }

		

			@Override
		    public void mousePressed(java.awt.event.MouseEvent e) {
				 selectX1 = e.getX();
			     selectY1 = e.getY();
		    }

		    @Override
		    public void mouseReleased(java.awt.event.MouseEvent e) {
		    	 selectX2 = e.getX();
			     selectY2 = e.getY();	
			     canvas.repaint();}
		});

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
		scale =  Math. min (w,h) / Math.max((yMax-yMin), (xMax-xMin));
		for (GpsPosition2 gps : listPositions) { 
			Bean bean = new Bean(gps);
			
			this.listBeans.add(bean);
		}
	}
	
	private void resizeInit(Dimension dim) {
		this.w = dim.width;
		this.h = dim.height;
		scale =  Math. min (w,h) / Math.max((yMax-yMin), (xMax-xMin));
		for (Bean bean : listBeans) { 
			bean.updatePosition();
		}
	}
	
	
	 class Bean {
		GpsPosition2 gps;
		int px;
		int py;
		public Bean(GpsPosition2 gps2) {
			this.gps=gps2;
			updatePosition();
		}
		
		public void updatePosition() {
		
			this.px =(int) (scale* (gps.getX()- xMin));
			this.py =(int) (scale* (gps.getY()- yMin));
		}

		@Override
		public String toString() {
			return "Bean [gps=" + gps + ", px=" + px + ", py=" + py + "]";
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
		g.setColor(Color.GREEN);
		g.drawRect(selectX1, selectY1, selectX2-selectX1, selectY2-selectY1);
	}

    private void displayImage(int x, int y) {
    	Bean beanSelected = null;
    	int distanceMin =1000020;
    	
    	for( Bean b: listBeans) {
    		int distance = Math.abs(b.px-x)+Math.abs(b.py-y);
    		
    		if (distance<distanceMin) {
    			distanceMin =distance;
    			beanSelected=b;
    		}
    	}
    	System.out.println("Bean selected   "+beanSelected+"  x: "+x+"   y: "+y);
    	File fileImage = new File(dirImages,beanSelected.gps.getImageName());
    	this.previewImage.displayImage(fileImage,beanSelected.gps);
    	
	}
}
