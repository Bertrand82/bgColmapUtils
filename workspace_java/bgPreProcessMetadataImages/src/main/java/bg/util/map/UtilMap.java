package bg.util.map;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;

public class UtilMap {

    

	    private static final int TILE_SIZE = 256;

	    // Configure ici ton identification (obligatoire en pratique)
	    private static final String BASE_URL = "https://tile.openstreetmap.org";
	    private static final String USER_AGENT = "MyStaticMapApp/1.0 (contact: bertrand.guiral@gmail.com)";
	    private static final String FROM_EMAIL = "bertrand.guiral@gmail.com";

	    // Cache disque + rate limit
	    private static final Path CACHE_DIR =
	            Path.of(System.getProperty("user.home"), ".cache", "myapp", "osm-tiles");
	    private static final long MIN_DELAY_MS = 1000; // 1 tuile / 200ms ~ 5 tuiles/s

	    private static final HttpClient HTTP = HttpClient.newBuilder()
	            .followRedirects(HttpClient.Redirect.NORMAL)
	            .connectTimeout(Duration.ofSeconds(10))
	            .build();

	    private static final Object RATE_LOCK = new Object();
	    private static long nextAllowedAtMs8 = 0;

	    /**
	     * Récupère une image statique (plan OSM) correspondant à la bbox (lat/lon) au zoom donné.
	     *
	     * @param latMin sud
	     * @param lonMin ouest
	     * @param latMax nord
	     * @param lonMax est
	     * @param zoom  0..19
	     */
	    public static BufferedImage fetchBbox(double latMin, double lonMin, double latMax, double lonMax) throws Exception{
	    	return fetchBbox(latMin, lonMin, latMax, lonMax,16);
	    }
	    public static BufferedImage fetchBbox(double latMin, double lonMin, double latMax, double lonMax, int zoom)
		            throws IOException, InterruptedException {
            System.err.println("latMin "+latMin+" latMAx"+latMax);
	        // Web Mercator clamp
	        latMin = clamp(latMin, -85.0511, 85.0511);
	        latMax = clamp(latMax, -85.0511, 85.0511);
	        System.err.println("latMin "+latMin+" latMAx"+latMax);
		       
	        // coins -> tuiles
	        int xMin = lonToTileX(lonMin, zoom)-1;
	        int xMax = lonToTileX(lonMax, zoom)+1;
	        int yMin = latToTileY(latMax, zoom)-1; // north
	        int yMax = latToTileY(latMin, zoom)+1; // south

	        int tilesW = xMax - xMin + 1;
	        int tilesH = yMax - yMin + 1;

	        if (tilesW <= 0 || tilesH <= 0) {
	            throw new IllegalArgumentException("Invalid bbox or zoom: tilesW=" + tilesW + " tilesH=" + tilesH);
	        }
	        // garde-fou anti “scraping involontaire”
	        if ((long) tilesW * (long) tilesH > 400) {
	            throw new IllegalArgumentException("Too many tiles (" + (tilesW * tilesH) +
	                    "). Reduce bbox or zoom (guard limit=400).");
	        }

	        BufferedImage stitched = new BufferedImage(tilesW * TILE_SIZE, tilesH * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g = stitched.createGraphics();
	        try {
	            g.setColor(Color.WHITE);
	            g.fillRect(0, 0, stitched.getWidth(), stitched.getHeight());
                System.err.println("xMin :"+xMin+"  xMax: "+xMax);
                System.err.println("yMin :"+yMin+"  yMax: "+yMax);
	             for (int x = xMin; x <= xMax; x++) {
	                for (int y = yMin; y <= yMax; y++) {
	                    BufferedImage tile = getTileCached(zoom, x, y);
	                    int px = (x - xMin) * TILE_SIZE;
	                    int py = (y - yMin) * TILE_SIZE;
	                    g.drawImage(tile, px, py, null);
	                }
	            }
	        } finally {
	            g.dispose();
	        }

	        // crop exact bbox (en pixels monde au zoom)
	        double pxMin = lonToPixelX(lonMin, zoom) - (double) xMin * TILE_SIZE;
	        double pxMax = lonToPixelX(lonMax, zoom) - (double) xMin * TILE_SIZE;
	        double pyMin = latToPixelY(latMax, zoom) - (double) yMin * TILE_SIZE;
	        double pyMax = latToPixelY(latMin, zoom) - (double) yMin * TILE_SIZE;

	        int cropX = (int) Math.floor(pxMin);
	        int cropY = (int) Math.floor(pyMin);
	        int cropW = (int) Math.ceil(pxMax - pxMin);
	        int cropH = (int) Math.ceil(pyMax - pyMin);

	        cropX = clampInt(cropX, 0, stitched.getWidth() - 1);
	        cropY = clampInt(cropY, 0, stitched.getHeight() - 1);
	        cropW = clampInt(cropW, 1, stitched.getWidth() - cropX);
	        cropH = clampInt(cropH, 1, stitched.getHeight() - cropY);

	        return stitched.getSubimage(cropX, cropY, cropW, cropH);
	    }

	    private static BufferedImage getTileCached(int z, int x, int y) throws IOException, InterruptedException {
	        Path file = CACHE_DIR.resolve(Paths.get(Integer.toString(z), Integer.toString(x), y + ".png"));
	       System.err.println("getTileCached : "+file);
	        if (Files.exists(file)) {
	            BufferedImage img = ImageIO.read(file.toFile());
	            if (img != null) return img;
	            Files.deleteIfExists(file); // corrompu
	        }

	        rateLimit();

	        String url = BASE_URL + "/" + z + "/" + x + "/" + y + ".png";
	        HttpRequest.Builder b = HttpRequest.newBuilder()
	                .uri(URI.create(url))
	                .timeout(Duration.ofSeconds(20))
	                .GET()
	                .header("User-Agent", USER_AGENT);

	        if (FROM_EMAIL != null && !FROM_EMAIL.isBlank()) {
	            b.header("From", FROM_EMAIL);
	        }

	        HttpResponse<InputStream> resp = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofInputStream());
	        if (resp.statusCode() != 200) {
	            throw new IOException("Tile HTTP " + resp.statusCode() + " for " + url);
	        }

	        Files.createDirectories(file.getParent());
	       
	        try (InputStream is = resp.body()) {
	            BufferedImage img = ImageIO.read(is);
	            if (img == null) throw new IOException("Unreadable tile: " + url);
	            System.err.println("File "+ file.toFile().getAbsolutePath());
	            // écrit sur disque (cache)
	            ImageIO.write(img, "png", file.toFile());
	            return img;
	        }
	    }

	    private static void rateLimit() throws InterruptedException {
	          
	           Thread.sleep(MIN_DELAY_MS);
	           
	       
	    }

	    // --- Slippy map formulas (Web Mercator) ---
	    private static int lonToTileX(double lon, int zoom) {
	        int n = 1 << zoom;
	        int x = (int) Math.floor((lon + 180.0) / 360.0 * n);
	        return clampInt(x, 0, n - 1);
	    }

	    private static int latToTileY(double lat, int zoom) {
	        double latRad = Math.toRadians(lat);
	        int n = 1 << zoom;// 1 << zoom = (2^{zoom}).
	        System.err.println("latToTileY :"+n);
	        double y = (1.0 - asinh(Math.tan(latRad)) / Math.PI) / 2.0 * n;
	        int yi = (int) Math.floor(y);
	        return clampInt(yi, 0, n - 1);
	    }

	    private static double lonToPixelX(double lon, int zoom) {
	        double n = (double) (1 << zoom) * TILE_SIZE;
	        return (lon + 180.0) / 360.0 * n;
	    }

	    private static double latToPixelY(double lat, int zoom) {
	        double latRad = Math.toRadians(lat);
	        double n = (double) (1 << zoom) * TILE_SIZE;
	        double y = (1.0 - asinh(Math.tan(latRad)) / Math.PI) / 2.0;
	        return y * n;
	    }

	    private static double asinh(double x) {
	        return Math.log(x + Math.sqrt(x * x + 1.0));
	    }

	    private static double clamp(double v, double min, double max) {
	        return Math.max(min, Math.min(max, v));
	    }

	    private static int clampInt(int v, int min, int max) {
	        return Math.max(min, Math.min(max, v));
	    }
	}