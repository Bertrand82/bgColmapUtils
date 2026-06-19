package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import bg.display.divide1.ProcessSubsets_1;
import bg.display.divide2.ProcessSubsets2;
import bg.metadata.MetaData;
import bg.metadata.MetaDatasCsv;
import bg.process.log.LogFactory;
import bg.process.log.LogProcess;
import bg.util.PaireMetadata2;
import bg.util.PositionBean2;
import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;
import bg.util.PositionMetaData2;
import bg.util.PositionMetaData2Factory;
import bg.util.PositionMetaData2UtilCloser;
import bg.util.PropertiesGlobal;
import bg.util.SablierSwing;
import bg.util.UtilCopyBg;
import bg.util.UtilCreateDirPopups;
import bg.util.UtilFile;
import bg.util.UtilSwingChooseDoosier;
import bg.util.UtilVerticaliseImage;
import bg.util.map.MapProvider;
import bg.util.map.MapProviderListener;

public class DisplayTogetherPanel extends JPanel implements MapProviderListener {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ParamsConfiguration paramsConfiguration = new ParamsConfiguration();

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
	private DisplayTogetherPanelPreviewImage previewImage = new DisplayTogetherPanelPreviewImage(this);

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
	double longitudeMax, longitudeMin, latitudeMax, latitudeMin;
	File dirImages;
	double scale = 0.1d;
	private static int Point_R = 8;
	List<Point> listPointsInterret = new ArrayList<Point>();
	PositionBean2 beanSelected = null;
	JLabel labelNbDePoints = new JLabel("Nb of points:0");
	JLabel labelLog = new JLabel("");
	JButton buttonSelectImages = new JButton("Select Images "+this.paramsConfiguration.nbPointsExtraitsMax);
	JButton buttonExtractData = new JButton("Init Sparse Dir");
	JMenuItem buttonDossierSparsesCreatePaquets_1 = new JMenuItem("Create Sparse paquets 1");
	JMenuItem buttonDossierSparsesCreatePaquets_2 = new JMenuItem("Create Sparse paquets 2");
	JMenuItem buttonDossierCleanSparsePaquets = new JMenuItem("Clean Sparse paquets");

	JMenuItem buttonDossierSourcesSparse = new JMenuItem("Source Images for sparse");
	JMenuItem buttonDossierSourcesDense = new JMenuItem("Source Images for dense");
	JMenuItem buttonDebug = new JMenuItem("debug");
	JMenuItem buttonAnalyseLog = new JMenuItem("Analyse Log");
	JMenuItem buttonLoadSelected = new JMenuItem("Open metadataCsv.txt");
	JCheckBox checkBoxImagesCorrected = new JCheckBox("corrected");
	JCheckBox checkBoxShowPaquets = new JCheckBox("paquets");
	MetaDatasCsv metaDataCsv;
	File dirSourcesSparse_;
	File dirSourcesDense;
	File dirSparse_;
	final SablierSwing sablierSwing;
	final JFrame frame;

	public DisplayTogetherPanel(File dir, JFrame frame) throws Exception {
		this.frame = frame;
		this.sablierSwing = new SablierSwing(frame, "Patience ..", "Sablier");
		initData(dir);
		initSwing();
	}

	private void initData(File dir) throws Exception {
		dirSourcesSparse_ = dir;
		dirSourcesDense = PropertiesGlobal.getFile("dirSourcesDense");
		dirImages = new File(dir, "images");
		String trace = "dirImages exists : " + dirImages.exists() + "  " + dirImages.getAbsolutePath();
		System.out.println(trace);
		this.labelLog.setText(trace);
		File fileMetadata = new File(dir, "metadata.csv");
		metaDataCsv = new MetaDatasCsv(fileMetadata, dirImages);
		startInitPositionsImages();

	}

	void startInitPositionsImages() {

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				sablierSwing.start("initialisation images", "Init");
				initListPositionsThread();
				return null;
			}

			// LIGNE 2 (remplace ss.stop() en fin): fermer quand c'est fini
			@Override
			protected void done() {
				File fileImages = new File(dirTargetOut, "images");
				if (fileImages.exists()) {
					System.out.println("Nb Images " + fileImages.listFiles().length);
				}
				System.err.println("sssssssssstop dialog ");
				sablierSwing.stop();
			}
		};
		sw.execute();
	}

	private void initSwing() {

		this.setLayout(new BorderLayout());
		buttonDebug.addActionListener(e -> debug());
		buttonAnalyseLog.addActionListener(e -> analyseLog());
		buttonLoadSelected.addActionListener(e -> actionLoadSelected());
		buttonDossierSourcesSparse.addActionListener(e_ -> chooseDossierSourceSparse());
		buttonDossierSourcesDense.addActionListener(e_ -> chooseDossierSourceDense());
		buttonDossierSparsesCreatePaquets_1.addActionListener(e -> processInitDossierDense_1());
		buttonDossierSparsesCreatePaquets_2.addActionListener(e -> processInitDossierDense_2());
		buttonDossierCleanSparsePaquets.addActionListener(e -> processDossierCleanPaquets());
		buttonSelectImages.addActionListener(e -> selectionnerImagesFirsts());
		buttonExtractData.addActionListener(e -> extractData());
		checkBoxImagesCorrected.addActionListener(e -> canvas.repaint());
		checkBoxShowPaquets.addActionListener(e -> canvas.repaint());
		JMenuItem menuItemConfigExtraction = new JMenuItem("config");
		JMenuItem menuItemProcessRapportFromLog = new JMenuItem("process Log");
		menuItemConfigExtraction.addActionListener(e ->config2());
		menuItemProcessRapportFromLog.addActionListener(econfig2 -> processLog());
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenu menuEdit = new JMenu("Edit");
		menuEdit.add(menuItemConfigExtraction);
		menuEdit.add(menuItemProcessRapportFromLog);
		menuFile.add(buttonDebug);
		menuFile.add(buttonLoadSelected);
		menuFile.add(buttonDossierSourcesSparse);
		menuFile.add(buttonDossierSourcesDense);
		menuFile.add(buttonDossierSparsesCreatePaquets_1);
		menuFile.add(buttonDossierSparsesCreatePaquets_2);
		menuFile.add(buttonDossierCleanSparsePaquets);
		menuFile.add(buttonAnalyseLog);

		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		menuBar.add(buttonSelectImages);
		menuBar.add(buttonExtractData);

		menuBar.add(checkBoxImagesCorrected);
		menuBar.add(checkBoxShowPaquets);

		this.add(canvas, BorderLayout.CENTER);
		this.add(previewImage, BorderLayout.WEST);
		this.add(menuBar, BorderLayout.NORTH);
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

	private void config2() {
		 DisplayTogetherPanelPopup.showPopup(this, paramsConfiguration);
		this.buttonSelectImages.setText("Selects Max :"+paramsConfiguration.nbPointsExtraitsMax);
		
	}

	private void initListPositionsThread() {
		long timeStart = System.currentTimeMillis();
		if (!dirImages.exists()) {
			System.err.println("dir : " + dirImages.getAbsolutePath() + "  doen't exists ");
			return;
		}

		this.listPositions = PositionGps2Factory.getListGpsPositionFromDirImages(dirImages);

		long duree_ms = System.currentTimeMillis() - timeStart;
		System.out.println("List gps size  " + listPositions.size() + "    duree (secondes) : " + (duree_ms / 1000));
		if (listPositions.size() == 0) {
			return;
		}
		PositionGps2 first = listPositions.get(0);
		xMin = xMax = first.getX();
		yMin = yMax = first.getY();
		longitudeMax = longitudeMin = first.getLongitude();
		latitudeMax = latitudeMin = first.getLatitude();
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
			if (longitude > longitudeMax) {
				longitudeMax = longitude;
			}
			if (longitude < longitudeMin) {
				longitudeMin = longitude;
			}
			if (latitude > latitudeMax) {
				latitudeMax = latitude;
			}
			if (latitude < latitudeMin) {
				latitudeMin = latitude;
			}
		}
		System.out.println("longitudeMax " + longitudeMax);
		System.out.println("longitudeMin " + longitudeMin);
		System.out.println("latitudeMax" + latitudeMax);
		System.out.println("latitudeMin" + latitudeMin);
		scale = Math.min(w, h) / Math.max((yMax - yMin), (xMax - xMin));
		xxMaxPixel_ = (int) (scale * (xMax - xMin));
		yyMaxPixel_ = (int) (scale * (yMax - yMin));
		for (PositionGps2 gps : listPositions) {
			try {
				List<MetaData> listMetaData = metaDataCsv.getListMetaDataAll();
				PositionMetaData2 pMetaData = PositionMetaData2Factory.extractPosition(gps, listMetaData);

				PositionBean2 bean = new PositionBean2(gps, pMetaData);
				bean.updatePosition(duree_ms, xMin, yMin);
				this.listBeans.add(bean);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		MapProvider mapProvider = new MapProvider(longitudeMax, longitudeMin, latitudeMax, latitudeMin, this);
		this.labelNbDePoints.setText("" + this.listBeans.size());
		this.resizeInit();
		SwingUtilities.invokeLater(() -> canvas.repaint());

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
			bean.updatePosition(scale, xMin, yMin);
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
			// bg ERROR . Il faut afficher uniquement la map entre longMAx et Min et latMax
			// et min
			g.drawImage(imageMap, 0, 0, xxMaxPixel_, yyMaxPixel_, null);
		}
		boolean showPaquets = checkBoxShowPaquets.isSelected();
		for (PositionBean2 bean : this.listBeans) {
			// g.fillOval(1, 1, bean.px, bean.py);
			if (showPaquets) {
				g.setColor(bean.gps.getColor());
				g.fillRect(bean.px, bean.py, 10, 10);
			}
			g.fillRect(bean.px, bean.py, 3, 3);
		}
		if (checkBoxImagesCorrected.isSelected()) {
			g.setColor(Color.BLUE);
			for (PositionBean2 bean : this.listBeans) {
				if (bean.getPositionMetaData() == null) {
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
			System.err.println(
					"Perplexité!!! beanSelected is null!! Should never happen listBeans size " + listBeans.size());
			return;
		}

		File fileImage = new File(dirImages, beanSelected.gps.getImageName());
		this.previewImage.displayImage(fileImage, beanSelected.gps);
		this.canvas.repaint();

	}

	private void processInitDossierDense_1() {
		System.out.println("processDossierSparse 1");
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				sablierSwing.start("initialisation paquets", "Init");
				processSparseBackGround_1(dirSourcesDense);
				return null;
			}

			// LIGNE 2 (remplace ss.stop() en fin): fermer quand c'est fini
			@Override
			protected void done() {
				File fileImages = new File(dirTargetOut, "images");
				if (fileImages.exists()) {
					System.out.println("Nb Images " + fileImages.listFiles().length);
				}
				System.err.println("sssssssssstop dialog ");
				sablierSwing.stop();
			}
		};
		sw.execute();

	}

	private void processInitDossierDense_2() {
		System.out.println("processDossierSparse 2");
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				sablierSwing.start("initialisation paquets", "Init");
				processSparseBackGround_2(dirSourcesDense);
				return null;
			}

			// LIGNE 2 (remplace ss.stop() en fin): fermer quand c'est fini
			@Override
			protected void done() {
				File fileImages = new File(dirTargetOut, "images");
				if (fileImages.exists()) {
					System.out.println("Nb Images " + fileImages.listFiles().length);
				}
				System.err.println("sssssssssstop dialog ");
				sablierSwing.stop();
			}
		};
		sw.execute();

	}

	private void processDossierCleanPaquets() {
		System.out.println("Clean paquets ");

		for (File f : this.dirSourcesDense.listFiles()) {
			System.out.println("-------" + f.getName());
			if (f.isDirectory() && f.getName().startsWith("paquet_")) {
				try {
					UtilFile.deleteDirRecursive(f);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		File dirLogs = new File(dirSourcesDense, "logs");
		if (dirLogs.exists()) {
			File dirLogArchive = new File(dirSourcesDense, "logs_archive_" + new Date());
			try {
				Files.move(dirLogs.toPath(), dirLogArchive.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void processSparseBackGround_1(File dirSparse) {
		System.out.println("processSparse 1 " + dirSparse.getAbsolutePath());
		int paquetSize = this.paramsConfiguration.taillePaquet;
		double tauxRecouvrementPaquets = this.paramsConfiguration.recouvrementPaquets;
		ProcessSubsets_1 processSubsets = new ProcessSubsets_1(dirSparse, paquetSize, tauxRecouvrementPaquets,
				this.listPositions);
		// Lots de 100 images
	}

	private void processSparseBackGround_2(File dirSparse) {
		try {
			System.out.println("processSparse 2 " + dirSparse.getAbsolutePath());
			int paquetSize = this.paramsConfiguration.taillePaquet;
			double tauxRecouvrementPaquets = this.paramsConfiguration.recouvrementPaquets;
			ProcessSubsets2 processSubsets = new ProcessSubsets2(dirSparse, paquetSize, tauxRecouvrementPaquets,
					this.listPositions);
			// Lots de 100 images
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.log(e.getMessage());
			e.printStackTrace();
		}
	}

	private void chooseDossierSourceSparse() {
		System.out.println("choose Dossier sources");
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choisir le dossier source");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);

		// Optionnel : partir du dernier dossier choisi
		if (UtilFile.existsDir(dirSourcesSparse_)) {
			chooser.setCurrentDirectory(dirSourcesSparse_);
		} else {
			String dirPath = ""
					+ PropertiesGlobal.getProperties().getProperty("DirSource", System.getProperty("user.home"));
			File file = new File(dirPath);

			chooser.setCurrentDirectory(file);
		}

		int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // ou this
		if (result == JFileChooser.APPROVE_OPTION) {
			dirSourcesSparse_ = chooser.getSelectedFile();
			PropertiesGlobal.saveProperty("DirSource", dirSourcesSparse_.getAbsolutePath());
			// Exemple : feedback utilisateur
			this.labelLog.setText(dirSourcesSparse_.getAbsolutePath());
			try {
				this.initData(dirSourcesSparse_);
				this.repaint();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.repaint();
			}
		}
	}

	private void chooseDossierSourceDense() {
		this.dirSourcesDense = UtilSwingChooseDoosier.chooseDossierSparse(dirSourcesDense, "dirSourcesDense",
				buttonDossierSourcesDense);
		PropertiesGlobal.saveProperty("dirSourcesDense", this.dirSourcesDense.getAbsolutePath());
		System.out.println("DirSourceDense " + this.dirSourcesDense.getAbsolutePath());
	}

	private void selectionnerImagesFirsts() {
		int nbImages = this.paramsConfiguration.nbPointsExtraitsMax;
		labelLog.setText("Visualiser Images " + nbImages);
		this.listBeans.sort(Comparator.comparingInt(p -> p.distance(this.listPointsInterret)));
		this.listBeansSelected = new ArrayList<>(listBeans.subList(0, Math.min(nbImages, listBeans.size() - 1)));
		canvas.repaint();
	}

	private void selectionnerImages(List<String> listImagesSelected) {
		labelLog.setText("Visualiser Images from  list " + listImagesSelected.size());
		this.listBeans.sort(Comparator.comparingInt(p -> p.distance(this.listPointsInterret)));
		this.listBeansSelected = new ArrayList<>();
		for (PositionBean2 pb : listBeans) {
			boolean isSelected = false;
			if (pb.getPositionMetaData() == null) {
			} else {
				isSelected = listImagesSelected.contains(pb.getPositionMetaData().getImageName());
			}
			if (isSelected) {
				this.listBeansSelected.add(pb);
			}
		}
		System.out.println("images selected  :" + this.listBeansSelected + " / " + listImagesSelected.size());
		canvas.repaint();
	}

	@Override
	public void updateMapImage(BufferedImage imageMap_) {
		this.imageMap = imageMap_;
		this.canvas.repaint();
		System.out.println("updateMapImage " + imageMap_);

	}

	private void extractData() {

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				sablierSwing.start("Ecriture Data", "Ecriture");
				extractDataProcessInBackGround();
				return null;
			}

			// LIGNE 2 (remplace ss.stop() en fin): fermer quand c'est fini
			@Override
			protected void done() {
				System.err.println("sssssssssstop dialog");
				sablierSwing.stop();
			}
		};
		sw.execute();
	}

	File dirTargetOut;

	/**
	 * Exporte les données correspondant aux images actuellement sélectionnées.
	 *
	 * <p>
	 * Cette méthode réalise le traitement complet d'extraction vers un dossier
	 * cible : elle crée l'arborescence de sortie, copie et réoriente les images
	 * sélectionnées, génère un fichier de métadonnées CSV, calcule les paires
	 * d'images proches pour le matching, puis écrit les fichiers de sortie
	 * nécessaires au traitement externe (notamment match.txt et les scripts shell
	 * associés).
	 * </p>
	 *
	 * <p>
	 * Le traitement s'appuie sur {@code listBeansSelected}, qui doit avoir été
	 * initialisée au préalable. Les paramètres de proximité et de séquencement
	 * utilisés pour générer les paires proviennent de {@code paramsConfiguration}.
	 * </p>
	 */
	private void extractDataProcessInBackGround() {
		System.out.println("extract data start | listBeansSelected.size :" + this.listBeansSelected.size());
		this.log("Selected Points :" + this.listBeansSelected.size());
		if (this.listBeansSelected.size()==0) {
			System.out.println("ListBeanSelected init");
			this.selectionnerImagesFirsts();
		}
		// Créer un directory
		dirTargetOut = UtilCreateDirPopups.createDirectoryPopup(this);
		dirTargetOut.mkdirs();
		File dirTargetImages = new File(dirTargetOut, "images");
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
				System.out.print(i + "/" + listBeansSelected.size() + " --> ");
				if (destFile.exists()) {
					System.out.println("file already exists " + imageName);
				} else {
					// Files.copy(src, destPath, StandardCopyOption.REPLACE_EXISTING,
					// StandardCopyOption.COPY_ATTRIBUTES);
					UtilVerticaliseImage.copyAndVerticalise(imageFile, dirTargetImages);
					System.out.println("Copié: " + src + " -> " + i + "/" + listBeansSelected.size() + "   durée : "
							+ (System.currentTimeMillis() - timeStart));
				}
				log("copied " + i + " / " + this.listBeansSelected.size());
			} catch (Exception e) {
				System.err.println("Erreur copie " + src + " -> " + destPath + " : " + e.getMessage());

				log("copy pb : " + imageName + " Exception !!! " + e.getMessage());
			}
		}
		// Copier / générer les metaDatas
		String metadataCsvStr = "";
		for (PositionBean2 bean : this.listBeansSelected) {
			System.out.println("g metaData : positionMetaData" + bean.getPositionMetaData() + "  gps: " + bean.gps);
			if (bean.getPositionMetaData() == null) {
				metadataCsvStr += bean.gps.toString2_csv() + " \n";
			} else {
				metadataCsvStr += bean.getPositionMetaData().toString2_csv() + "\n";
			}
		}
		File metadataCsvFile = new File(dirTargetOut, "metadataCSV.txt");
		System.out.println("file " + metadataCsvFile.getName() + "  exists " + metadataCsvFile.exists());
		if (metadataCsvFile.exists()) {
			boolean deleted = metadataCsvFile.delete();
			System.out.println("file " + metadataCsvFile.getName() + "  deleted " + deleted);
		}
		try {
			Files.writeString(metadataCsvFile.toPath(), metadataCsvStr, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE_NEW);
			System.out.println("file " + metadataCsvFile.getName() + " generated ");
		} catch (Exception e) {
			log("Exception " + e.getMessage());
			e.printStackTrace();
		}
		// Générer les Matching
		List<PositionMetaData2> listPositionMetaDAta = getListPositionMetaData2();
		System.out.println("listPositionMetaDAta size " + listPositionMetaDAta.size());
		Hashtable<PositionMetaData2, Set<PositionMetaData2>> hashTableClosest = new Hashtable<PositionMetaData2, Set<PositionMetaData2>>();
		for (PositionBean2 beanPosition : this.listBeansSelected) {
			PositionMetaData2 position0 = beanPosition.getPositionMetaData();
			if (position0 == null) {

			} else {
				int nDate = this.paramsConfiguration.nbSeq;
				int nDistance = this.paramsConfiguration.nbProx;
				try {
					Set<PositionMetaData2> setPosition = PositionMetaData2UtilCloser.searchClosest(position0,
							listPositionMetaDAta, nDate, nDistance);
					hashTableClosest.put(position0, setPosition);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("HashTable closest " + hashTableClosest.size());
		HashSet<PaireMetadata2> paires = consolidationPaire(hashTableClosest);
		System.out.println("Nb de paires :" + paires.size());
		try {
			exportListPaires(dirTargetOut, paires);
		} catch (Exception e) {
			log("Exception " + e.getMessage());
			e.printStackTrace();
		}
		exportFileSh(dirTargetOut);

	}

	private void exportFileSh(File dirTargetOut2) {
		try {
			UtilCopyBg.copyResourceToDir("sh/processColmapSparseLocal.sh", dirTargetOut2.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/processColmapDenseLocal.sh", dirTargetOut2.toPath(), true);

		} catch (Exception e) {
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
		System.out.println("nb paires " + setPairesUniques.size());
		System.out.println("Fichier ecrit dans " + fileOut.getPath());

	}

	private List<PositionMetaData2> getListPositionMetaData2() {
		List<PositionMetaData2> list = new ArrayList<PositionMetaData2>();
		for (PositionBean2 beanPosition : this.listBeansSelected) {
			if (beanPosition.getPositionMetaData() == null) {
				System.err.println("Warning2 positionMetaData is null . Should never happen" + beanPosition);
			} else {
				list.add(beanPosition.getPositionMetaData());
			}

		}
		return list;
	}

	private void log(String s) {
		this.labelLog.setText(s);
	}

	private HashSet<PaireMetadata2> consolidationPaire(
			Hashtable<PositionMetaData2, Set<PositionMetaData2>> hashTableClosest) {
		HashSet<PaireMetadata2> setPAires = new HashSet<PaireMetadata2>();
		for (Map.Entry<PositionMetaData2, Set<PositionMetaData2>> entry : hashTableClosest.entrySet()) {
			PositionMetaData2 metaData = entry.getKey();
			Set<PositionMetaData2> list = entry.getValue();
			for (PositionMetaData2 pm2 : list) {
				PaireMetadata2 paire = new PaireMetadata2(metaData, pm2);
				setPAires.add(paire);
			}
		}
		System.out.println("List Paires size " + setPAires.size() + "  ");
		return setPAires;
	}

	private void debug() {
		this.labelLog
				.setText("debug listBeansSelected.size :" + this.listBeansSelected.size() + " / " + listBeans.size());
		System.out.println("debug listBeansSelected.size :" + this.listBeansSelected.size());
		System.out.println("debug listBeans.size :" + this.listBeans.size());
		int i = 0;
		for (PositionBean2 pb2 : this.listBeansSelected) {
			String s = String.format("%2d  - ", i++);
			System.out.println(s + pb2.getPositionMetaData().toString());
		}
	}

	private void actionLoadSelected() {
		this.labelLog.setText("actionLoadSelected");
		// 1) Ouvrir un sélecteur de dossier
		String dirRootChooserPath = PropertiesGlobal.getProperties().getProperty("openMetaDataCsvDir",
				System.getProperty("user.home"));
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choisir un fichier metadata.csv ou metadataCsv.txt");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setAcceptAllFileFilterUsed(false);

		// Optionnel: dossier par défaut (à adapter)
		File dirRootChooser = new File(dirRootChooserPath);
		chooser.setCurrentDirectory(dirRootChooser);

		int result = chooser.showOpenDialog(this);

		if (result != JFileChooser.APPROVE_OPTION) {
			return; // annulé
		}

		File metadataFile = chooser.getSelectedFile();
		PropertiesGlobal.saveProperty("openMetaDataCsvDir", metadataFile.getParentFile().getAbsolutePath());

		// 3) Charger le fichier
		try {
			BufferedReader br = new BufferedReader(new FileReader(metadataFile));
			String line = null;
			List<String> listImagesSelected = new ArrayList();
			while ((line = br.readLine()) != null) {
				String[] ws = line.split(",");

				listImagesSelected.add(ws[0].trim());
			}
			br.close();
			System.out.println("line image : " + listImagesSelected.size());
			selectionnerImages(listImagesSelected);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Impossible de lire : " + metadataFile + "\n" + ex.getMessage(),
					"Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void displayNextPreview(int i, PositionGps2 gpsCurrent) {
		PositionBean2 pb = getBeanNext(i, gpsCurrent);
		File fileImage = new File(dirImages, pb.gps.getImageName());
		this.previewImage.displayImage(fileImage, pb.gps);
	}

	private PositionBean2 getBeanNext(int i, PositionGps2 gpsCurrent) {
		PositionBean2 beanZ_1 = null;
		boolean bingo = false;
		for (PositionBean2 bean : this.listBeans) {
			if ((bingo) && (i == 1)) {
				return bean;
			}
			if (bean.gps.getImageName().equals(gpsCurrent.getImageName())) {
				bingo = true;
			}
			if ((bingo) && (i == -1)) {
				return beanZ_1;
			}
			beanZ_1 = bean;
		}
		return null;
	}

	private void processLog() {
		System.out.println("ProcessLog");
		this.labelLog.setText("actionLoadSelected");
		String dirRootChooserPath = PropertiesGlobal.getProperties().getProperty("DirSourceLog",
				System.getProperty("user.home"));
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choisir le dossier source");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		File dirSources_ = new File(dirRootChooserPath);
		// Optionnel : partir du dernier dossier choisi
		if (UtilFile.existsDir(dirSources_)) {
			chooser.setCurrentDirectory(dirSources_);
		} else {
			String dirPath = ""
					+ PropertiesGlobal.getProperties().getProperty("DirSourceLog", System.getProperty("user.home"));
			File file = new File(dirPath);

			chooser.setCurrentDirectory(file);
		}

		int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // ou this
		if (result == JFileChooser.APPROVE_OPTION) {
			dirSources_ = chooser.getSelectedFile();
			PropertiesGlobal.saveProperty("DirSourceLog", dirSources_.getAbsolutePath());

			this.labelLog.setText(dirSources_.getAbsolutePath());
			try {
				System.out.println("ProcessLog dirSources :" + dirSources_.getAbsolutePath());
				LogProcess logProcess = new LogProcess(dirSources_);
				System.out.println("logProcess toString ::: " + logProcess);
				System.out.println("logProcess toString ::: " + logProcess.toStringVerbose());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}

	private void analyseLog() {
		System.out.println("AnalyseLog ");
		String analyse = LogFactory.process(this.dirSourcesSparse_);
		JTextArea textArea = new JTextArea(analyse);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setCaretPosition(0); // démarre en haut

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(900, 600)); // ajuste taille popup

		JOptionPane.showMessageDialog(this.frame, scrollPane, "Analyse Log", JOptionPane.INFORMATION_MESSAGE);
	}
}
