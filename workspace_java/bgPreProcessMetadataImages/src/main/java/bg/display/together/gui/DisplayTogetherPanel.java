package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bg.metadata.MetaDatasCsv;
import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;
import bg.util.PositionMetaData2;
import bg.util.PositionMetaData2Factory;

public class DisplayTogetherPanel extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Canvas canvas= new Canvas() {
		@Override
		public void paint(Graphics g) {
			DisplayTogetherPanel.this.paintImage(g);
		}

		@Override
		public void update(Graphics g) {
			paint(g);
		}
	};
	private PreviewImage previewImage = new PreviewImage();

	private List<PositionGps2> listPositions;
	final List<Bean> listBeans = new ArrayList<DisplayTogetherPanel.Bean>();
	 List<Bean> listBeansSelected = new ArrayList<DisplayTogetherPanel.Bean>();
	int w = 500;
	int h = 500;
	double xMin = 0;
	double xMax = 0;
	double yMin = 0;
	double yMax = 0;
	File dirImages;
	double scale = 0.1d;
	private static int Point_R=8;
	List<Point> listPointsInterret = new ArrayList<Point>();
	Bean beanSelected = null;
	JLabel labelNbDePoints= new JLabel("Nb of points:0");
	JLabel labelLog = new JLabel("");
	JTextField textFieldNbImages = new JTextField(" 1000 ");
	JButton buttonVisualiserImages = new JButton("Images Selected");
	JCheckBox checkBoxImagesCorrected = new JCheckBox("corrected");
	MetaDatasCsv metaDataCsv;
	public DisplayTogetherPanel(File dir) throws Exception {
		dirImages = new File(dir, "images");

		File fileMetadata = new File(dir, "metadata.csv");
		metaDataCsv = new MetaDatasCsv(fileMetadata, dirImages);
		Thread threadInit = new Thread(this);		
		threadInit.start();

		
		this.setLayout(new BorderLayout());
		
		buttonVisualiserImages.addActionListener(e->visualiserImages());
		checkBoxImagesCorrected.addActionListener(e->canvas.repaint());
		JPanel panelControl = new JPanel();
		panelControl.add(buttonVisualiserImages);
		panelControl.add(textFieldNbImages);
		panelControl.add(labelNbDePoints);
		panelControl.add(checkBoxImagesCorrected);
		this.add(canvas, BorderLayout.CENTER);
		this.add(previewImage, BorderLayout.WEST);
		this.add(panelControl,BorderLayout.NORTH);
		this.add(labelLog,BorderLayout.SOUTH);
		Dimension dim = new Dimension(w, h);
		canvas.setPreferredSize(dim);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				
				resizeInit();
				
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
						
						Point ppp = getPointByProximity(x,y);
						if (ppp == null) {
							Point point = new Point(x, y);
							listPointsInterret.add(point);
							displayImage(x, y);
							
						} else {	
							listPointsInterret.remove(ppp);
							
							
						}
						beanSelected=null;
						canvas.repaint();
					
					
				} else if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
					displayImage(x, y);
				}

			}

			private Point getPointByProximity(int x, int y) {
				for(Point pp:listPointsInterret) {
					int dx = Math.abs(x-pp.x);
					int dy = Math.abs(y-pp.y);
					if ((dx < Point_R) && (dy <Point_R)) {
						return pp;
					}
				}
				return null;
			}

			

		});

	}
	
	public void run() {
		this.initListPositionsThread();
	}

	private void initListPositionsThread() {
		long timeStart = System.currentTimeMillis();
		this.listPositions = PositionGps2Factory.getListGpsPositionFromDirImages(dirImages);

		long duree_ms = System.currentTimeMillis() - timeStart;
		System.out.println("List gps size  " + listPositions.size() + "    duree (secondes) : " + (duree_ms / 1000));
		PositionGps2 first = listPositions.get(0);
		xMin = xMax = first.getX();
		yMin = yMax = first.getY();
		for (PositionGps2 gps : listPositions) {
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
		scale = Math.min(w, h) / Math.max((yMax - yMin), (xMax - xMin));
		for (PositionGps2 gps : listPositions) {
			
			PositionMetaData2 pMetaData = PositionMetaData2Factory.extractPosition(gps, metaDataCsv.getListMetaDataAll());
			
			Bean bean = new Bean(gps,pMetaData);
			
			this.listBeans.add(bean);
		}
		this.labelNbDePoints.setText(""+this.listBeans.size());
		this.canvas.repaint();
	}

	private void resizeInit() {
		java.awt.Dimension dim = getSize();
		this.w = dim.width;
		this.h = dim.height;
		scale = Math.min(w, h) / Math.max((yMax - yMin), (xMax - xMin));
		for (Bean bean : listBeans) {
			bean.updatePosition();
		}
		repaint();
	}

	class Bean {
		PositionGps2 gps;
		PositionMetaData2 positionMetaData ;
		int px;
		int py;
		int pxCorrected;
		int pyCorrected;


		public Bean(PositionGps2 gps2,PositionMetaData2 positionMetaData) {
			this.gps = gps2;
			this.positionMetaData=positionMetaData;
			updatePosition();
		}

		public void updatePosition() {

			this.px = (int) (scale * (gps.getX() - xMin));
			this.py = (int) (scale * (gps.getY() - yMin));
			if (this.positionMetaData==null) {
				this.pxCorrected = this.px;
				this.pyCorrected=this.py;
			}else {
				this.pxCorrected = (int) (scale * (positionMetaData.getxCorrected() - xMin));
				this.pyCorrected=(int) (scale * (positionMetaData.getyCorrected() - yMin));
			}
			
		}

		@Override
		public String toString() {
			return "Bean [gps=" + gps + ", px=" + px + ", py=" + py + "]";
		}

		public int distance(List<Point> listPointsInterret) {
			if (listPointsInterret.size()==0) {
				return 0;
			}
			int distance = distance(listPointsInterret.getFirst());
			for (Point ppp : listPointsInterret) {
				int d1=  distance(ppp);
				if ( d1 < distance) {
					distance=d1;
				}
			}
			return distance;
			
		}

		private int distance(Point pp) {
			return Math.abs(px-pp.x)+Math.abs(py-pp.y);
			
		}

	}

	private void paintImage(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.RED);
		for (Bean bean : this.listBeans) {
			// g.fillOval(1, 1, bean.px, bean.py);
			g.fillRect(bean.px, bean.py, 3, 3);
		}
		if (checkBoxImagesCorrected.isSelected()) {
			g.setColor(Color.BLUE);
			for (Bean bean : this.listBeans) {	
				if (bean.positionMetaData==null) {
					g.fillRect(bean.pxCorrected, bean.pyCorrected, 1, 1);
				}else {
				g.fillRect(bean.pxCorrected, bean.pyCorrected, 3, 3);
				}
			}
			g.setColor(Color.YELLOW);
			for (Bean bean : this.listBeans) {	
				
				g.drawLine(bean.px, bean.py, bean.pxCorrected, bean.pyCorrected);
				
			}
		}
		g.setColor(Color.MAGENTA);
		for (Bean bean : this.listBeansSelected) {
			 
			g.fillRect(bean.px, bean.py, 3, 3);
		}
		g.setColor(Color.GREEN);
		for (Point pp : listPointsInterret) {
			g.fillOval(pp.x-Point_R, pp.y-Point_R,2* Point_R, 2*Point_R);
		}
		
		g.setColor(Color.ORANGE);
		if (beanSelected!=null) {
			g.fillOval(beanSelected.px, beanSelected.py,2*Point_R,2*Point_R);
		}
	}

	private void displayImage(int x, int y) {
		
		int distanceMin = 1000020;

		for (Bean b : listBeans) {
			int distance = Math.abs(b.px - x) + Math.abs(b.py - y);

			if (distance < distanceMin) {
				distanceMin = distance;
				beanSelected = b;
			}
		}
		System.out.println("Bean selected   " + beanSelected + "  x: " + x + "   y: " + y);
		
		File fileImage = new File(dirImages, beanSelected.gps.getImageName());
		this.previewImage.displayImage(fileImage, beanSelected.gps);
		this.canvas.repaint();

	}
	
	private void visualiserImages(){
		int nbImages =Integer.parseInt(textFieldNbImages.getText().trim());
		labelLog.setText("Visualiser Images "+nbImages);
		this.listBeans.sort(Comparator.comparingInt(p -> p.distance(this.listPointsInterret)));
		this.listBeansSelected = new ArrayList<>(listBeans.subList(0, Math.min(nbImages, listBeans.size())));
		canvas.repaint();
	}
}
