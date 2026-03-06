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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bg.util.ImageRotateUtil;
import bg.util.UtilImage;

public class PanelImage extends JPanel {

	BufferedImage image;
	String imageName;
	File dirImages;
	int w;
	int h ;
	private CanvasImage canvas;
	JLabel labelName =new JLabel("no name");

	public PanelImage(String imageName, File dirImages) {
		super(new BorderLayout());
		this.dirImages = dirImages;
		
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
		this.image = UtilImage.getBufferedImageFromDir(imageName, dirImages);
		w = this.image.getWidth();
		h = this.image.getHeight();
		this.canvas = new CanvasImage(w,h);
		Dimension dimension = new Dimension(w, h);
		this.canvas.setSize(dimension);
		this.canvas.setPreferredSize(dimension);
		this.canvas.setMaximumSize(dimension);
		JPanel panelN = new JPanel();
		JButton buttonRollD = new JButton("O");
		buttonRollD.addActionListener( new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				roll(1);
				
			}
		});
		panelN.add(buttonRollD);
		panelN.add(labelName);
		this.add(canvas,BorderLayout.CENTER);
		this.add(panelN,BorderLayout.NORTH);
		this.updateImage(imageName);
		
	}
	
	private void roll(int sens) {
		this.image = ImageRotateUtil.rotate90CCW(image);
		this.canvas.setImage(image);
		revalidate() ; 
		this.canvas.repaint();
	}
	
	public void updateImage(String imageName){
		this.imageName = imageName;
		this.image = UtilImage.getBufferedImageFromDir(imageName, dirImages);
		this.labelName.setText(imageName);
		this.canvas.setImage(image);
		revalidate() ; 
		this.canvas.repaint();
	}

	

}
