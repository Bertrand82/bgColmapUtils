package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bg.util.GpsPosition2;

public class PreviewImage extends JPanel{

	private static final long serialVersionUID = 1L;
	private CanvasMiniature canvasMiniature = new CanvasMiniature();
	private CanvasMiniature canvasMiniaturePrevious1= new CanvasMiniature();
	private CanvasMiniature canvasMiniaturePrevious2= new CanvasMiniature();
	
	private JLabel label = new JLabel("");
	public PreviewImage() {
		
		JPanel panelCAnvas= new JPanel(new GridLayout(0,1));
		panelCAnvas.add(canvasMiniature);
		panelCAnvas.add(canvasMiniaturePrevious1);
		panelCAnvas.add(canvasMiniaturePrevious2);
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.NORTH);
		this.add(panelCAnvas,BorderLayout.CENTER);
	}
	
	

	public void displayImage(File fileImage, GpsPosition2 gps) {
		try {
			this.label.setText(gps.getImageName());
			this.canvasMiniaturePrevious2.currentImage=this.canvasMiniaturePrevious1.currentImage;
			this.canvasMiniaturePrevious1.currentImage=this.canvasMiniature.currentImage;
			this.canvasMiniature.currentImage = ImageIO.read(fileImage);
			this.canvasMiniature.setPreferredSize(new Dimension(200,200));
			this.canvasMiniature.repaint();
			this.canvasMiniaturePrevious1.repaint();
			this.canvasMiniaturePrevious2.repaint();
			this.repaint();
			this.updateUI();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class CanvasMiniature extends Canvas{
		private static final long serialVersionUID = 1L;
		public Image currentImage;
		
		public void paint(Graphics g) {
			this.paintImageMiniature(g);
		}

		@Override
		public void update(Graphics g) {
			paint(g);
		}
		
		protected void paintImageMiniature(Graphics g) {
			if (currentImage == null) return;

	        int w = canvasMiniature.getWidth();
	        int h = canvasMiniature.getHeight();

	        int imgW = currentImage.getWidth(null);
	        int imgH = currentImage.getHeight(null);
	        if (imgW <= 0 || imgH <= 0) return;

	        double s = Math.min((double) w / imgW, (double) h / imgH);
	        int dw = (int) (imgW * s);
	        int dh = (int) (imgH * s);
	        int x = (w - dw) / 2;
	        int y = (h - dh) / 2;

			g.drawImage(currentImage, x, y, dw, dh, null);
			
		}
	}
}
