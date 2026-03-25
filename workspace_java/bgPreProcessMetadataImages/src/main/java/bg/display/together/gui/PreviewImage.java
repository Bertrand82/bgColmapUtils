package bg.display.together.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bg.util.GpsPosition2;

public class PreviewImage extends JPanel{

	private Canvas canvasMiniature;
	private Image currentImage;
	private JLabel label = new JLabel("");
	public PreviewImage() {
		canvasMiniature = new Canvas() {
			@Override
			public void paint(Graphics g) {
				PreviewImage.this.paintImageMiniature(g);
			}

			@Override
			public void update(Graphics g) {
				paint(g);
			}
		};
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.NORTH);
		this.add(canvasMiniature,BorderLayout.CENTER);
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

	public void displayImage(File fileImage, GpsPosition2 gps) {
		try {
			this.label.setText(gps.getImageName());
			this.currentImage = ImageIO.read(fileImage);
			this.canvasMiniature.setPreferredSize(new Dimension(200,200));
			this.canvasMiniature.repaint();
			this.repaint();
			this.updateUI();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
