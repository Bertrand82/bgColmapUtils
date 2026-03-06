package bg.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class UtilImage {
	
	public static BufferedImage getBufferedImageFromDir(String imageName, File dir) {
		try {
			File file = new File(dir, imageName);
			BufferedImage img = ImageIO.read(file);
			return img;
		} catch (IOException e) {
			throw new RuntimeException("Pb loading image",e);
		}
	}

}
