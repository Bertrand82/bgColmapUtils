package bg.util;



import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class ImageRotateUtil {
    private ImageRotateUtil() {}

    /** Rotation 90° sens horaire. */
    public static BufferedImage rotate90CW(BufferedImage src) {
        if (src == null) return null;

        int w = src.getWidth();
        int h = src.getHeight();

        // après rotation 90°, la nouvelle image fait h x w
        BufferedImage dst = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());

        Graphics2D g2 = dst.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // mapping pixels:
            // (x,y) dans src => (h-1-y, x) dans dst
            g2.translate(h, 0);
            g2.rotate(Math.toRadians(90));
            g2.drawImage(src, 0, 0, null);
        } finally {
            g2.dispose();
        }

        return dst;
    }

    /** Rotation 90° sens anti-horaire. */
    public static BufferedImage rotate90CCW(BufferedImage src) {
        if (src == null) return null;

        int w = src.getWidth();
        int h = src.getHeight();

        BufferedImage dst = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());

        Graphics2D g2 = dst.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.translate(0, w);
            g2.rotate(Math.toRadians(-90));
            g2.drawImage(src, 0, 0, null);
        } finally {
            g2.dispose();
        }

        return dst;
    }
}