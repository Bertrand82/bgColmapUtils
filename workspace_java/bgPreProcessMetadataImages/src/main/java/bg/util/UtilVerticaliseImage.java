package bg.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

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
					System.out.println(" gps Old:"+fImage.getName()+"    "+gps);
					File fIo = verticaliseImage(fImage, dirOut);
					PositionGps2 gpsNew= PositionGps2FactoryApache.extractPosition(fImage);
					System.out.println(" gps New:"+fImage.getName()+"    "+gpsNew);
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

	private static File verticaliseImage(File fImage, File dirOut) throws Exception{
		ImageMetadata metadata = Imaging.getMetadata(fImage);
		if (!(metadata instanceof JpegImageMetadata jpegMetadata)) {
            throw new Exception("Pb with JpegImageMetadata !!!! ");
        }
		String orientation = null;
		TiffField orientationField =
                jpegMetadata.findExifValueWithExactMatch(TiffTagConstants.TIFF_TAG_ORIENTATION);
        if (orientationField != null) {
            orientation = PositionGps2FactoryApache.safeStringValue(orientationField);
        }
        if (orientation == null) {
            orientation = orientationField.getValueDescription();
        }
        PositionGps2.ORIENTATIONS orientaionEnum = PositionGps2.ORIENTATIONS.getORIENTATION(orientation);
        System.out.println("  ->>>----- "+fImage.getName()+"   orientation :"+orientation+"  "+PositionGps2.ORIENTATIONS.getORIENTATION(orientation));
		File f =copyImageWithOrientation(fImage, dirOut, orientaionEnum, jpegMetadata);
		return f;
	}
	
    private static File copyImageWithOrientation(File fImage, File dirOut, PositionGps2.ORIENTATIONS orientation, ImageMetadata metadata) throws Exception {

        BufferedImage src = ImageIO.read(fImage);

        int angle_degre = orientation.angle_degre;
        if (angle_degre == 0) {
            File f = copyToDirectory(fImage, dirOut);
            return f;
        }

        BufferedImage dst = rotate(src,angle_degre);

        String format = getFormatName(fImage.getName());
        File outFile = new File(dirOut, fImage.getName());

        // Write rotated pixels into a byte array first
        byte[] pixelBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            boolean ok = ImageIO.write(dst, format, baos);
            if (!ok) {
                throw new IOException("Aucun writer ImageIO pour le format : " + format);
            }
            pixelBytes = baos.toByteArray();
        }

        // Embed original EXIF metadata into the output file
        if (metadata instanceof JpegImageMetadata jpegMetadata && jpegMetadata.getExif() != null) {
            TiffOutputSet outputSet = jpegMetadata.getExif().getOutputSet();
            if (outputSet != null) {
                // Update orientation to Normal (1) since the image has been physically rotated
                TiffOutputDirectory rootDirectory = outputSet.getOrCreateRootDirectory();
                rootDirectory.removeField(TiffTagConstants.TIFF_TAG_ORIENTATION);
                rootDirectory.add(TiffTagConstants.TIFF_TAG_ORIENTATION, (short) TiffTagConstants.ORIENTATION_VALUE_HORIZONTAL_NORMAL);

                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
                    new ExifRewriter().updateExifMetadataLossless(
                            new ByteArrayInputStream(pixelBytes), out, outputSet);
                }
                return outFile;
            }
        }

        // No EXIF available or outputSet is null – write the rotated pixels directly
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
            out.write(pixelBytes);
        }

        return outFile;
    }
	
	
	private static BufferedImage rotate(BufferedImage src,int angle_degre) {
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
            transform.rotate(Math.toRadians(angle_degre));

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
