package bg.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

public class UtilVerticaliseImage {

	
	public static void main(String[] a) {
		
		File dirIn = new File("C:\\Users\\bertr\\workspace_java2\\bgLocalizeJava\\data");
		File target = new File("target");
		File dirOut = new File(target,"images2");
		dirOut.mkdirs();
		System.out.println("UtilVerticaliseImage  dirIn exists :"+dirIn.exists()+"  "+dirIn.getPath());
		verticaliseDir(dirIn, dirOut);
	}
	
	static public void verticaliseDir( File dirIn, File dirOut) {
		File[] fImages = dirIn.listFiles();
		for (File fImage : fImages) {
			if (isImageJPG(fImage)) {
				
				try {
					PositionGps2 gps= PositionGps2FactoryApache.extractPosition(fImage);
					System.out.println(" gps :"+fImage.getName()+"    "+gps);
					verticaliseImage(fImage, dirOut);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static boolean isImageJPG(File fImage) {
		if (fImage.exists()) {
			return fImage.getName().toLowerCase().endsWith(".jpg");
		}
		return false;
	}

	private static void verticaliseImage(File fImage, File dirOut) throws Exception{
		ImageMetadata metadata = Imaging.getMetadata(fImage);
		if (!(metadata instanceof JpegImageMetadata jpegMetadata)) {
            return ;
        }
		String orientation = null;
		TiffField orientationField =
                jpegMetadata.findExifValueWithExactMatch(TiffTagConstants.TIFF_TAG_ORIENTATION);
        if (orientationField != null) {
            orientation = PositionGps2FactoryApache.safeStringValue(orientationField);
            if (orientation == null) {
                orientation = orientationField.getValueDescription();
            }
        }
        PositionGps2.ORIENTATIONS orientaionEnum = PositionGps2.ORIENTATIONS.getORIENTATION(orientation);
        System.out.println("  ->>>----- "+fImage.getName()+"   orientation :"+orientation+"  "+PositionGps2.ORIENTATIONS.getORIENTATION(orientation));
		copyImageWithOrientation(fImage, dirOut, null, jpegMetadata)
	}
	
    private static File copyImageWithOrientation(File fImage, File dirOut, PositionGps2.ORIENTATIONS orientation,ImageMetadata metadata ) throws Exception {
        if (fImage == null || !fImage.exists()) {
            throw new IllegalArgumentException("fImage est null ou n'existe pas");
        }
        if (dirOut == null) {
            throw new IllegalArgumentException("dirOut est null");
        }
        if (!dirOut.exists() && !dirOut.mkdirs()) {
            throw new IOException("Impossible de créer le répertoire de sortie : " + dirOut);
        }

        BufferedImage src = ImageIO.read(fImage);
        if (src == null) {
            throw new IOException("Impossible de lire l'image : " + fImage);
        }

        BufferedImage dst =null;
        int angle_degre = orientation.angle_degre;
        if (angle_degre == 0) {
        	copyToDirectory(fImage,dirOut);
        } else {
        	 dst = rotate90Clockwise(src);
        }

        String format = getFormatName(fImage.getName());
        File outFile = new File(dirOut, fImage.getName());

        boolean ok = ImageIO.write(dst, format, outFile);
        if (!ok) {
            throw new IOException("Aucun writer ImageIO pour le format : " + format);
        }

        return outFile;
    }
	
	
	private static BufferedImage rotate90Clockwise(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dst = new BufferedImage(height, width, getBufferedImageType(src));

        Graphics2D g2d = dst.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AffineTransform transform = new AffineTransform();
            transform.translate(height, 0);
            transform.rotate(Math.toRadians(90));

            g2d.drawImage(src, transform, null);
        } finally {
            g2d.dispose();
        }

        return dst;
    }
	
	 private static int getBufferedImageType(BufferedImage img) {
	        return img.getType() != BufferedImage.TYPE_CUSTOM
	                ? img.getType()
	                : BufferedImage.TYPE_INT_RGB;
	    }
	 
	 private static String getFormatName(String fileName) {
	        int idx = fileName.lastIndexOf('.');
	        if (idx < 0 || idx == fileName.length() - 1) {
	            return "jpg";
	        }
	        return fileName.substring(idx + 1).toLowerCase();
	    }

	  public static File copyToDirectory(File sourceFile, File targetDir) throws IOException {
	       

	        Path targetPath = targetDir.toPath().resolve(sourceFile.getName());

	        Files.copy(
	                sourceFile.toPath(),
	                targetPath,
	                StandardCopyOption.REPLACE_EXISTING
	        );

	        return targetPath.toFile();
	    }
	
}
