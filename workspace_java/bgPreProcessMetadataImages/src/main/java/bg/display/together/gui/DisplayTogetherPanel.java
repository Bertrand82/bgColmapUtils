package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import bg.metadata.MetaDatasCsv;
import bg.util.PaireMetadata2;
import bg.util.PositionBean2;
import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;
import bg.util.PositionMetaData2;
import bg.util.PositionMetaData2Factory;
import bg.util.PositionMetaData2UtilCloser;
import bg.util.UtilCreateDirPopups;
import bg.util.map.MapProvider;
import bg.util.map.MapProviderListener;

public class DisplayTogetherPanel extends JPanel implements Runnable, MapProviderListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Canvas canvas = new Canvas() {
		@Override
		public void paint(Graphics g) {
			DisplayTogetherPanel.this.paintImage(g);
		}

		@Override
		public void update(Graphics g) {
			paint(g);
		}
	};
	BufferedImage imageMap;
	private PreviewImage previewImage = new PreviewImage();

	private List<PositionGps2> listPositions;
	final List<PositionBean2> listBeans = new ArrayList<PositionBean2>();
	List<PositionBean2> listBeansSelected = new ArrayList<PositionBean2>();
	int w = 500;
	int h = 500;
	double xMin = 0;
	double xMax = 0;
	double yMin = 0;
	double yMax = 0;
	int xxMaxPixel_ = 0;
	int yyMaxPixel_ = 0;
	double longMax, longMin, latMax, latMin;
	File dirImages;
	double scale = 0.1d;
	private static int Point_R = 8;
	List<Point> listPointsInterret = new ArrayList<Point>();
	PositionBean2 beanSelected = null;
	JLabel labelNbDePoints = new JLabel("Nb of points:0");
	JLabel labelLog = new JLabel("");
	JTextField textFieldNbImages = new JTextField(" 1000 ");
	JButton buttonVisualiserImages = new JButton("Images Selected");
	JButton buttonExtractData = new JButton("extract Data");
	JButton buttonDossierSources = new JButton("sources");
	JCheckBox checkBoxImagesCorrected = new JCheckBox("corrected");
	MetaDatasCsv metaDataCsv;
	File dirSources ;

	public DisplayTogetherPanel(File dir) throws Exception {
		initData(dir);
		initSwing();
	}
	
	private void initData(File dir) throws Exception{
		dirSources = dir;
		dirImages = new File(dir, "images");

		File fileMetadata = new File(dir, "metadata.csv");
		metaDataCsv = new MetaDatasCsv(fileMetadata, dirImages);
		Thread threadInit = new Thread(this);
		threadInit.start();
	}

	private void initSwing() {
		this.setLayout(new BorderLayout());
		buttonDossierSources.addActionListener(e_-> chooseDossierSource());
		buttonVisualiserImages.addActionListener(e -> visualiserImages());
		buttonExtractData.addActionListener(e -> extractData());
		checkBoxImagesCorrected.addActionListener(e -> canvas.repaint());
		JPanel panelControl = new JPanel();
		panelControl.add(buttonDossierSources);
		panelControl.add(buttonVisualiserImages);
		panelControl.add(buttonExtractData);
		
		panelControl.add(textFieldNbImages);
		panelControl.add(labelNbDePoints);
		panelControl.add(checkBoxImagesCorrected);
		this.add(canvas, BorderLayout.CENTER);
		this.add(previewImage, BorderLayout.WEST);
		this.add(panelControl, BorderLayout.NORTH);
		this.add(labelLog, BorderLayout.SOUTH);
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

					Point ppp = getPointByProximity(x, y);
					if (ppp == null) {
						Point point = new Point(x, y);
						listPointsInterret.add(point);
						displayImage(x, y);

					} else {
						listPointsInterret.remove(ppp);

					}
					beanSelected = null;
					canvas.repaint();

				} else if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
					displayImage(x, y);
				}

			}

			private Point getPointByProximity(int x, int y) {
				for (Point pp : listPointsInterret) {
					int dx = Math.abs(x - pp.x);
					int dy = Math.abs(y - pp.y);
					if ((dx < Point_R) && (dy < Point_R)) {
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
		longMax = longMin = first.getLongitude();
		latMax = latMin = first.getLatitude();
		first.getLongitude();
		for (PositionGps2 gps : listPositions) {
			double x = gps.getX();
			double y = gps.getY();
			double longitude = gps.getLongitude();
			double latitude = gps.getLatitude();
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
			if (longitude > longMax) {
				longMax = longitude;
			}
			if (longitude < longMin) {
				longMin = longitude;
			}
			if (latitude > latMax) {
				latMax = latitude;
			}
			if (latitude < latMin) {
				latMin = latitude;
			}
		}
		System.out.println("longMax " + longMax);
		System.out.println("longMin " + longMin);
		System.out.println("latMax" + latMax);
		System.out.println("latMin" + latMin);
		scale = Math.min(w, h) / Math.max((yMax - yMin), (xMax - xMin));
		xxMaxPixel_ = (int) (scale * (xMax - xMin));
		yyMaxPixel_ = (int) (scale * (yMax - yMin));
		for (PositionGps2 gps : listPositions) {

			PositionMetaData2 pMetaData = PositionMetaData2Factory.extractPosition(gps,
					metaDataCsv.getListMetaDataAll());

			PositionBean2 bean = new PositionBean2(gps, pMetaData);
			bean.updatePosition(duree_ms, xMin, yMin);
			this.listBeans.add(bean);
		}
		MapProvider mapProvider = new MapProvider(longMax, longMin, latMax, latMin, this);
		this.labelNbDePoints.setText("" + this.listBeans.size());
		this.canvas.repaint();
	}

	private void resizeInit() {
		java.awt.Dimension dim = getSize();

		double scaleOld = scale;
		scale = Math.min(w, h) / Math.max((yMax - yMin), (xMax - xMin));
		int xxMaxPixel2 = (int) (scale * (xMax - xMin));
		int yyMaxPixel2 = (int) (scale * (yMax - yMin));
		xxMaxPixel_ = (int) (scale * (xMax - xMin));
		yyMaxPixel_ = (int) (scale * (yMax - yMin));

		this.w = dim.width;
		this.h = dim.height;
		for (PositionBean2 bean : listBeans) {
			bean.updatePosition(scale,xMin,yMin);
		}
		for (Point point : listPointsInterret) {
			point.x = (int) ((scale / scaleOld) * (point.x));
			point.y = (int) ((scale / scaleOld) * (point.y));
		}
		repaint();
	}

	

	private void paintImage(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.RED);
		if (this.imageMap != null) {
			g.drawImage(imageMap, 0, 0, xxMaxPixel_, yyMaxPixel_, null);
		}
		for (PositionBean2 bean : this.listBeans) {
			// g.fillOval(1, 1, bean.px, bean.py);
			g.fillRect(bean.px, bean.py, 3, 3);
		}
		if (checkBoxImagesCorrected.isSelected()) {
			g.setColor(Color.BLUE);
			for (PositionBean2 bean : this.listBeans) {
				if (bean.positionMetaData == null) {
					g.fillRect(bean.pxCorrected, bean.pyCorrected, 1, 1);
				} else {
					g.fillRect(bean.pxCorrected, bean.pyCorrected, 3, 3);
				}
			}
			g.setColor(Color.YELLOW);
			for (PositionBean2 bean : this.listBeans) {

				g.drawLine(bean.px, bean.py, bean.pxCorrected, bean.pyCorrected);

			}
		}
		g.setColor(Color.MAGENTA);
		for (PositionBean2 bean : this.listBeansSelected) {

			g.fillRect(bean.px, bean.py, 3, 3);
		}
		g.setColor(Color.GREEN);
		for (Point pp : listPointsInterret) {
			g.fillOval(pp.x - Point_R, pp.y - Point_R, 2 * Point_R, 2 * Point_R);
		}

		g.setColor(Color.ORANGE);
		if (beanSelected != null) {
			g.fillOval(beanSelected.px, beanSelected.py, 2 * Point_R, 2 * Point_R);
		}
	}

	private void displayImage(int x, int y) {

		int distanceMin = 1000020;

		for (PositionBean2 b : listBeans) {
			int distance = Math.abs(b.px - x) + Math.abs(b.py - y);

			if (distance < distanceMin) {
				distanceMin = distance;
				beanSelected = b;
			}
		}
		System.out.println("Bean selected   " + beanSelected + "  x: " + x + "   y: " + y);
		if (beanSelected == null) {
			System.err.println("Perplexité!!! beanSelected is null!! Should never happen");
			System.err.println("Perplexité!!! beanSelected is null!! Should never happen listBeans size "+listBeans.size());
			return;
		}

		File fileImage = new File(dirImages, beanSelected.gps.getImageName());
		this.previewImage.displayImage(fileImage, beanSelected.gps);
		this.canvas.repaint();

	}
	private void chooseDossierSource() {
		System.out.println("choose Dossier sources");
		JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir le dossier source");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);

        // Optionnel : partir du dernier dossier choisi
        if (dirSources != null) {
            chooser.setCurrentDirectory(dirSources);
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }

        int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // ou this
        if (result == JFileChooser.APPROVE_OPTION) {
        	dirSources = chooser.getSelectedFile();
            // Exemple : feedback utilisateur
            this.labelLog.setText(dirSources.getAbsolutePath());
            try {
				this.initData(dirSources);
				this.repaint();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.repaint();
			}
        }
	}

	private void visualiserImages() {
		int nbImages = Integer.parseInt(textFieldNbImages.getText().trim());
		labelLog.setText("Visualiser Images " + nbImages);
		this.listBeans.sort(Comparator.comparingInt(p -> p.distance(this.listPointsInterret)));
		this.listBeansSelected = new ArrayList<>(listBeans.subList(0, Math.min(nbImages, listBeans.size())));
		canvas.repaint();
	}

	@Override
	public void updateMapImage(BufferedImage imageMap_) {
		this.imageMap = imageMap_;
		this.canvas.repaint();
		System.out.println("updateMapImage " + imageMap_);

	}

	private void extractData() {
		;
		System.out.println("extract data");
		this.log("Selected Points :" + this.listBeansSelected.size());
		// Créer un directory
		File dirTarget = UtilCreateDirPopups.createDirectoryPopup(this);
		dirTarget.mkdirs();
		File dirTargetImages = new File(dirTarget, "images");
		dirTargetImages.mkdirs();
		// Copier les images
		int i = 0;
		for (PositionBean2 bean : this.listBeansSelected) {
			String imageName = bean.gps.getImageName();
			File imageFile = new File(this.dirImages, imageName);
			Path src = imageFile.toPath();
			File destFile = new File(dirTargetImages, imageName);
			Path destPath = destFile.toPath();
			
			try {
				long timeStart = System.currentTimeMillis();
				i++;
				System.out.print(i+"/"+listBeansSelected.size()+" --> ");
				if (destFile.exists()) {
					System.out.println("file already exists " + imageName);
				} else {
					Files.copy(src, destPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

					System.out.println("Copié: " + src + " -> " + i + "/" + listBeansSelected.size() + "   durée : "
							+ (System.currentTimeMillis() - timeStart));
				}
				log("copied " + i + " / " + this.listBeansSelected.size());
			} catch (IOException e) {
				System.err.println("Erreur copie " + src + " -> " + destPath + " : " + e.getMessage());

				log("copy pb : " + imageName + " Exception !!! " + e.getMessage());
			}
		}
		// Copier / générer les metaDatas
		String metadataCsv="";
		for (PositionBean2 bean : this.listBeansSelected) {
			System.out.println("g metaData :" + bean.positionMetaData);
			metadataCsv+= bean.positionMetaData.toString2_csv()+"\n";
		}
		File metadataCsvFile = new File(dirTarget,"metadataCSV.txt");
		System.out.println("file "+metadataCsvFile.getName()+"  exists "+metadataCsvFile.exists());
		if (metadataCsvFile.exists()) {
			boolean deleted  = metadataCsvFile.delete();
			System.out.println("file "+metadataCsvFile.getName()+"  deleted "+deleted);
		}
		try {
			Files.writeString(metadataCsvFile.toPath(), metadataCsv, StandardCharsets.UTF_8,
			        StandardOpenOption.CREATE_NEW);
			System.out.println("file "+metadataCsvFile.getName()+" generated ");
		} catch (Exception e) {
			log("Exception "+e.getMessage());
			e.printStackTrace();
		}
		// Générer les Matching
		List<PositionMetaData2> listPositionMetaDAta= getListPositionMetaData2();
		System.out.println("listPositionMetaDAta size "+listPositionMetaDAta.size());
		Hashtable<PositionMetaData2, Set<PositionMetaData2>> hashTableClosest = new Hashtable<PositionMetaData2, Set<PositionMetaData2>>();
		 for(PositionBean2 beanPosition : this.listBeansSelected) {
			 PositionMetaData2 position0 = beanPosition.positionMetaData;
			 Set<PositionMetaData2> setPosition = PositionMetaData2UtilCloser.searchClosest(position0, listPositionMetaDAta, 4, 4);
			 hashTableClosest.put(position0, setPosition);
		 }
		 System.out.println("HashTable closest "+hashTableClosest.size());
		 HashSet<PaireMetadata2> paires = consolidationPaire(hashTableClosest);
		 System.out.println("Nb de paires :"+paires.size());
		 try {
			exportListPaires(dirTarget, paires);
		} catch (Exception e) {
			log("Exception "+e.getMessage());
			e.printStackTrace();
		}

	}
	
	private void exportListPaires(File fileDirOut, HashSet<PaireMetadata2> setPairesUniques) throws Exception {
		
		File fileOut = new File(fileDirOut, "match.txt");
		TreeSet<PaireMetadata2> treeset = new TreeSet<PaireMetadata2>(setPairesUniques);
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut)));

		for (PaireMetadata2 p : treeset) {
			String line = p.getFileName1() + " " + p.getFileName2() + "\n";
			w.write(line);
		}
		w.close();
		System.out.println("nb paires "+setPairesUniques.size());
		System.out.println("Fichier ecrit dans "+fileOut.getPath());
	}

	private List<PositionMetaData2> getListPositionMetaData2() {
		 List<PositionMetaData2> list = new ArrayList<PositionMetaData2>();
		 for(PositionBean2 beanPosition : this.listBeansSelected) {
			 if (beanPosition.positionMetaData==null) {
					System.err.println("Warning positionMetaData is null "+beanPosition);
				}
			list.add(beanPosition.positionMetaData) ;
			
		 }
		return list;
	}

	private void log(String s) {
		this.labelLog.setText(s);
	}
	
	private HashSet<PaireMetadata2>  consolidationPaire(Hashtable<PositionMetaData2, Set<PositionMetaData2>> hashTableClosest ) {
		HashSet<PaireMetadata2> setPAires = new HashSet<PaireMetadata2>();
		for (Map.Entry<PositionMetaData2, Set<PositionMetaData2>> entry : hashTableClosest.entrySet()) {
			PositionMetaData2 metaData = entry.getKey();
			Set<PositionMetaData2> list =entry.getValue();
			for(PositionMetaData2 pm2 : list) {
				PaireMetadata2 paire = new PaireMetadata2(metaData, pm2);
				setPAires.add(paire);
			}
		}
		System.out.println("List Paires size " + setPAires.size() + "  ");
		return setPAires;
	}
	

}
