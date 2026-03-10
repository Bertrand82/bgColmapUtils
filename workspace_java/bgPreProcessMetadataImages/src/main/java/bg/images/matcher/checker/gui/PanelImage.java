package bg.images.matcher.checker.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bg.database.DatabaseColmap;
import bg.database.Features;
import bg.database.KeyPoints;
import bg.database.Point;
import bg.images.matcher.checker.PairChecker;
import bg.util.ImageRotateUtil;
import bg.util.UtilImage;

public class PanelImage extends JPanel {

	PairChecker pairChecker;
	BufferedImage image;
	String imageName;
	File dirImages;
	int w;
	int h;
	private CanvasImage canvas;
	JLabel labelName = new JLabel("no name");
	JLabel labelNbPairs = new JLabel("no ");
	int nbImageInRelation = 0;
	DatabaseColmap databaseColmap;
	private List<Point> listPoints = null;
	boolean isKeyPointsDisplayed=false;

	public PanelImage(String imageName, int nbImageInRelation, File dirImages, PairChecker pairChecker,
			DatabaseColmap databaseColmap) {
		super(new BorderLayout());
		this.databaseColmap = databaseColmap;
		this.dirImages = dirImages;
		this.pairChecker = pairChecker;

		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		this.image = UtilImage.getBufferedImageFromDir(imageName, dirImages);
		w = this.image.getWidth();
		h = this.image.getHeight();
		this.canvas = new CanvasImage(w, h,this);
		Dimension dimension = new Dimension(w, h);
		this.canvas.setSize(dimension);
		this.canvas.setPreferredSize(dimension);
		this.canvas.setMaximumSize(dimension);
		JPanel panelN = new JPanel();
		JButton buttonFeatures = new JButton("keyPoints");
		buttonFeatures.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayKeyPoints();
			}
		});
		JButton buttonRollD = new JButton("O");
		buttonRollD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				roll(1);
			}
		});
		panelN.add(buttonRollD);
		panelN.add(buttonFeatures);
		panelN.add(labelName);
		panelN.add(labelNbPairs);
		this.add(canvas, BorderLayout.CENTER);
		this.add(panelN, BorderLayout.NORTH);
		this.updateImage(imageName, nbImageInRelation);
	}

	private void roll(int sens) {
		this.image = ImageRotateUtil.rotate90CCW(image);
		this.canvas.setImage(image);
		revalidate();
		this.canvas.repaint();
	}

	public void displayKeyPoints() {
		
		isKeyPointsDisplayed = !isKeyPointsDisplayed;
		if (this.listPoints == null) {
			try {

				long idImage = databaseColmap.getImageIdFromName(imageName);

				KeyPoints keyPoints = databaseColmap.readKeyPoints(idImage);
				this.listPoints = keyPoints.getListPoints();
				System.out.println("feature listPoints : " + listPoints.size());
				repaint();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		System.out.println("displayKeyPoints "+isKeyPointsDisplayed+"   listPoints "+((listPoints==null)?"null":""+listPoints.size()));
		for (Point p : listPoints) {
			System.out.println("--x-->"+p);
		}
		System.out.println("displayKeyPoints "+isKeyPointsDisplayed+"   listPoints "+((listPoints==null)?"null":""+listPoints.size()));
		this.canvas.repaint();
	}

	public void updateImage(String imageName, int nbImageInRelation) {
		this.imageName = imageName;
		this.image = UtilImage.getBufferedImageFromDir(imageName, dirImages);
		this.labelName.setText(imageName);
		this.canvas.setImage(image);
		revalidate();
		this.nbImageInRelation = nbImageInRelation;
		this.labelNbPairs.setText("nb :" + nbImageInRelation);
		isKeyPointsDisplayed=false;
		this.listPoints=null;
		this.canvas.repaint();
	}

	public List<Point> getListPoints() {
		return listPoints;
	}

	public boolean isKeyPointsDisplayed() {
		return isKeyPointsDisplayed;
	}
	
	

}
