package bg.images.matcher.checker.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class CanvasImage extends Canvas{

	BufferedImage image;
	int w;
	int h;
	CanvasImage(int w, int h) {
		this.w =w;
		this.h =h;
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public void paint(Graphics g) {
        int cw = getWidth();
        int ch = getHeight();

        // fond (optionnel)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, cw, ch);

        if (image == null) return;
        int iw0 = Math.max(image.getWidth(),image.getHeight());
        int ih0 = Math.min(image.getWidth(),image.getHeight());
        int iw = image.getWidth();
        int ih = image.getHeight();

        double scale = Math.min((double) cw / iw0, (double) ch / ih0);

        int dw = (int) Math.round(iw * scale);
        int dh = (int) Math.round(ih * scale);

        int dx = (cw - dw) / 2;
        int dy = (ch - dh) / 2;

        g.drawImage(image, dx, dy, dw, dh, null);
	}

	public BufferedImage getImage() {
		return image;
	}
	
}
