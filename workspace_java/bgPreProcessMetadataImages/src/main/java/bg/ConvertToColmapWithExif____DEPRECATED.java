package bg;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversion CSV (poses drone) -> COLMAP sparse/prior: cameras.txt, images.txt, points3D.txt
 *
 * CSV attendu (sans header), séparé par virgules:
 * filename,X,Y,Z,Yaw,Pitch,Roll
 *
 * Exemple ligne:
 * DJI_20260207175436_0671_D.JPG,-207.0166,-404.0546,-1.6910,-71.8,-60.0,0.0
 */
@Deprecated
public class ConvertToColmapWithExif____DEPRECATED {

    // =========================================================================
    // CONFIG (instance)
    // =========================================================================
    private  Path csvFile;
    private  Path imageDir;
    private  Path outputDir;

    private  String cameraModel; // SIMPLE_PINHOLE, PINHOLE, RADIAL
    private  int cameraId;
    private  int sampleStep;

    // Fallback si EXIF focale absent
    private  double focalEstimateFactor;

   

    public ConvertToColmapWithExif____DEPRECATED(Path csvFile,
                                   Path imageDir,
                                   Path outputDir,
                                   String cameraModel,
                                   int cameraId,
                                   int sampleStep,
                                   double focalEstimateFactor) {
        this.csvFile = Objects.requireNonNull(csvFile, "csvFile");
        this.imageDir = Objects.requireNonNull(imageDir, "imageDir");
        this.outputDir = Objects.requireNonNull(outputDir, "outputDir");
        this.cameraModel = Objects.requireNonNull(cameraModel, "cameraModel");
        this.cameraId = cameraId;
        this.sampleStep = sampleStep <= 0 ? 1 : sampleStep;
        this.focalEstimateFactor = focalEstimateFactor;
    }

    // =========================================================================
    // API
    // =========================================================================
    public void process() throws Exception {
        System.out.println("================================================================================");
        System.out.println("🚁 Conversion CSV (poses drone) → COLMAP (images.txt)");
        System.out.println("================================================================================");

        if (!Files.exists(csvFile)) {
            throw new IllegalArgumentException("CSV introuvable: " + csvFile.toAbsolutePath());
        }
        if (!Files.isDirectory(imageDir)) {
            throw new IllegalArgumentException("Dossier imageDir introuvable: " + imageDir.toAbsolutePath());
        }

        System.out.println("\n📂 Chargement du CSV: " + csvFile.toAbsolutePath());
        List<PoseRow> rows = loadCsv(csvFile);
        System.out.println("✅ " + rows.size() + " images chargées");

        rows = sampleEvery(rows, sampleStep);
        if (sampleStep > 1) {
            System.out.println("📉 Échantillonnage (1/" + sampleStep + "): " + rows.size() + " images conservées");
        }

        printStatistics(rows);

        System.out.println("\n📷 Extraction des paramètres de la caméra (EXIF + dimensions)...");
        Path firstImage = imageDir.resolve(rows.get(0).filename);
        CameraParams cam = extractCameraParams(firstImage);

        System.out.println("   Caméra EXIF Model: " + cam.modelName);
        System.out.println("   Dimensions: " + cam.width + " x " + cam.height);
        if (cam.focalLengthMm != null) {
            System.out.printf(Locale.US, "   Focale (mm): %.2f mm%n", cam.focalLengthMm);
        } else {
            System.out.println("   Focale (mm): (absente dans EXIF)");
        }
        System.out.printf(Locale.US, "   Focale (px): %.2f px (estimée)%n", cam.focalPx);

        Files.createDirectories(outputDir);
        System.out.println("\n📁 Dossier de sortie: " + outputDir.toAbsolutePath());

    //    System.out.println("\n📝 Création des fichiers COLMAP...cameras.txt ,images.txt,points3D.txt");
        createCamerasTxt(outputDir.resolve("cameras.txt"), cam, cameraId, cameraModel);
        createImagesTxt(outputDir.resolve("images.txt"), rows, cameraId);
        createPoints3DTxt(outputDir.resolve("points3D.txt"));
    }

    // =========================================================================
    // DATA
    // =========================================================================
    private static class PoseRow {
        final String filename;
        final double x, y, z;
        final double yawDeg, pitchDeg, rollDeg;

        PoseRow(String filename, double x, double y, double z, double yawDeg, double pitchDeg, double rollDeg) {
            this.filename = filename;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yawDeg = yawDeg;
            this.pitchDeg = pitchDeg;
            this.rollDeg = rollDeg;
        }
    }

    private static class CameraParams {
        final int width;
        final int height;
        final String modelName;      // ex: DJI...
        final Double focalLengthMm;  // peut être null
        final double focalPx;        // estimée en px

        CameraParams(int width, int height, String modelName, Double focalLengthMm, double focalPx) {
            this.width = width;
            this.height = height;
            this.modelName = modelName;
            this.focalLengthMm = focalLengthMm;
            this.focalPx = focalPx;
        }
    }

    // =========================================================================
    // MATH: Euler -> Quaternion
    // =========================================================================
    // Retourne {qw, qx, qy, qz}
    private double[] eulerToQuaternion(double yawDeg, double pitchDeg, double rollDeg) {
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);
        double roll = Math.toRadians(rollDeg);

        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);

        double qw = cr * cp * cy + sr * sp * sy;
        double qx = sr * cp * cy - cr * sp * sy;
        double qy = cr * sp * cy + sr * cp * sy;
        double qz = cr * cp * sy - sr * sp * cy;

        double norm = Math.sqrt(qw * qw + qx * qx + qy * qy + qz * qz);
        return new double[]{qw / norm, qx / norm, qy / norm, qz / norm};
    }

    // =========================================================================
    // CSV
    // =========================================================================
    private List<PoseRow> loadCsv(Path csvFile) throws IOException {
        List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
        List<PoseRow> out = new ArrayList<>(lines.size());

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] p = line.split("\\s*,\\s*");
            if (p.length != 7) {
                throw new IllegalArgumentException("Ligne CSV invalide (attendu 7 champs): " + line);
            }

            out.add(new PoseRow(
                    p[0],
                    Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]),
                    Double.parseDouble(p[3]),
                    Double.parseDouble(p[4]),
                    Double.parseDouble(p[5]),
                    Double.parseDouble(p[6])
            ));
        }
        return out;
    }

    private List<PoseRow> sampleEvery(List<PoseRow> rows, int step) {
        if (step <= 1) return rows;
        List<PoseRow> out = new ArrayList<>((rows.size() + step - 1) / step);
        for (int i = 0; i < rows.size(); i += step) out.add(rows.get(i));
        return out;
    }

    // =========================================================================
    // EXIF + dimensions
    // =========================================================================
    private CameraParams extractCameraParams(Path imagePath) throws Exception {
        BufferedImage img = ImageIO.read(imagePath.toFile());
        if (img == null) throw new IOException("Impossible de lire l'image: " + imagePath);
        int w = img.getWidth();
        int h = img.getHeight();

        String modelName = "Unknown";
        Double focalMm = null;

        Metadata metadata = ImageMetadataReader.readMetadata(imagePath.toFile());

        ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0 != null) {
            String m = ifd0.getString(ExifIFD0Directory.TAG_MODEL);
            if (m != null && !m.isBlank()) modelName = m;
        }

        ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (sub != null) {
            Rational r = sub.getRational(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
            if (r != null) focalMm = r.doubleValue();
        }

        double focalPx = Math.max(w, h) * focalEstimateFactor;

        return new CameraParams(w, h, modelName, focalMm, focalPx);
    }

    // =========================================================================
    // COLMAP files
    // =========================================================================
    private void createCamerasTxt(Path outputFile, CameraParams cam, int cameraId, String model) throws IOException {
        Files.createDirectories(outputFile.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            w.write("# Camera list with one line of data per camera:\n");
            w.write("#   CAMERA_ID, MODEL, WIDTH, HEIGHT, PARAMS[]\n");
            w.write("# Number of cameras: 1\n");

            double width = cam.width;
            double height = cam.height;
            double focal = cam.focalPx;

            if ("SIMPLE_PINHOLE".equals(model)) {
                double cx = width / 2.0;
                double cy = height / 2.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f%n",
                        cameraId, model, cam.width, cam.height, focal, cx, cy));

            } else if ("SIMPLE_RADIAL".equals(model)) {
                double cx = width / 2.0;
                double cy = height / 2.0;
                double k1 = 0.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f %.6f%n",
                        cameraId, model, cam.width, cam.height, focal, cx, cy, k1));

            } else if ("PINHOLE".equals(model)) {
                double fx = focal;
                double fy = focal;
                double cx = width / 2.0;
                double cy = height / 2.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f %.2f%n",
                        cameraId, model, cam.width, cam.height, fx, fy, cx, cy));

            } else if ("RADIAL".equals(model)) {
                double cx = width / 2.0;
                double cy = height / 2.0;
                double k1 = 0.0, k2 = 0.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f %.1f %.1f%n",
                        cameraId, model, cam.width, cam.height, focal, cx, cy, k1, k2));

            } else {
                throw new IllegalArgumentException("Modèle caméra non géré: " + model);
            }
        }

        System.out.println("✅ Créé: " + outputFile);
    }
    private void createCamerasTxt_old(Path outputFile, CameraParams cam, int cameraId, String model) throws IOException {
        Files.createDirectories(outputFile.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            w.write("# Camera list with one line of data per camera:\n");
            w.write("#   CAMERA_ID, MODEL, WIDTH, HEIGHT, PARAMS[]\n");
            w.write("# Number of cameras: 1\n");

            double width = cam.width;
            double height = cam.height;
            double focal = cam.focalPx;

            if ("SIMPLE_PINHOLE".equals(model)) {
                double cx = width / 2.0;
                double cy = height / 2.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f%n",
                        cameraId, model, cam.width, cam.height, focal, cx, cy));
            } else if ("PINHOLE".equals(model)) {
                double fx = focal;
                double fy = focal;
                double cx = width / 2.0;
                double cy = height / 2.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f %.2f%n",
                        cameraId, model, cam.width, cam.height, fx, fy, cx, cy));
            } else if ("RADIAL".equals(model)) {
                double cx = width / 2.0;
                double cy = height / 2.0;
                double k1 = 0.0, k2 = 0.0;
                w.write(String.format(Locale.US, "%d %s %d %d %.2f %.2f %.2f %.1f %.1f%n",
                        cameraId, model, cam.width, cam.height, focal, cx, cy, k1, k2));
            } else {
                throw new IllegalArgumentException("Modèle caméra non géré: " + model);
            }
        }

        System.out.println("✅ Créé Camera: " + outputFile);
        System.out.println("   Modèle COLMAP: " + model);
        System.out.println("   Caméra EXIF Model: " + cam.modelName);
        System.out.println("   Dimensions: " + cam.width + " x " + cam.height);
        if (cam.focalLengthMm != null) {
            System.out.printf(Locale.US, "   Focale (mm): %.2f mm%n", cam.focalLengthMm);
        }
        System.out.printf(Locale.US, "   Focale (px): %.2f px (estimée)%n", cam.focalPx);
    }

    private void createImagesTxt(Path outputFile, List<PoseRow> rows, int cameraId) throws IOException {
        Files.createDirectories(outputFile.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            w.write("# Image list with two lines of data per image:\n");
            w.write("#   IMAGE_ID, QW, QX, QY, QZ, TX, TY, TZ, CAMERA_ID, NAME\n");
            w.write("#   POINTS2D[] as (X, Y, POINT3D_ID)\n");

            int imageId = 1;
            for (PoseRow r : rows) {
                double[] q = eulerToQuaternion(r.yawDeg, r.pitchDeg, r.rollDeg);

                w.write(String.format(Locale.US,
                        "%d %.10f %.10f %.10f %.10f %.10f %.10f %.10f %d %s%n",
                        imageId,
                        q[0], q[1], q[2], q[3],
                        r.x, r.y, r.z,
                        cameraId,
                        r.filename
                ));

                w.write("\n"); // points2D vide
                imageId++;
            }
        }

        System.out.println("✅ ImagesTxt Créé: " + outputFile);
        System.out.println("   Nombre d'images: " + rows.size());
    }

    private void createPoints3DTxt(Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            w.write("# 3D point list with one line of data per point:\n");
            w.write("#   POINT3D_ID, X, Y, Z, R, G, B, ERROR, TRACK[] as (IMAGE_ID, POINT2D_IDX)\n");
            w.write("# Number of points: 0\n");
        }
        System.out.println("✅ Créé Points3DTxt: " + outputFile + " (vide)");
    }

    // =========================================================================
    // Stats
    // =========================================================================
    private void printStatistics(List<PoseRow> rows) {
        DoubleSummaryStatistics xs = rows.stream().collect(Collectors.summarizingDouble(r -> r.x));
        DoubleSummaryStatistics ys = rows.stream().collect(Collectors.summarizingDouble(r -> r.y));
        DoubleSummaryStatistics zs = rows.stream().collect(Collectors.summarizingDouble(r -> r.z));

        DoubleSummaryStatistics yaws = rows.stream().collect(Collectors.summarizingDouble(r -> r.yawDeg));
        DoubleSummaryStatistics pitches = rows.stream().collect(Collectors.summarizingDouble(r -> r.pitchDeg));
        DoubleSummaryStatistics rolls = rows.stream().collect(Collectors.summarizingDouble(r -> r.rollDeg));

        System.out.println("\n📊 Statistiques des poses");
        System.out.println("================================================================================");
        System.out.println("Nombre d'images: " + rows.size());

        System.out.printf(Locale.US, "%nPosition (XYZ):%n");
        System.out.printf(Locale.US, "  X: [%8.2f, %8.2f] m  (plage: %.2f m)%n", xs.getMin(), xs.getMax(), (xs.getMax() - xs.getMin()));
        System.out.printf(Locale.US, "  Y: [%8.2f, %8.2f] m  (plage: %.2f m)%n", ys.getMin(), ys.getMax(), (ys.getMax() - ys.getMin()));
        System.out.printf(Locale.US, "  Z: [%8.2f, %8.2f] m  (plage: %.2f m)%n", zs.getMin(), zs.getMax(), (zs.getMax() - zs.getMin()));

        System.out.printf(Locale.US, "%nOrientation (Yaw, Pitch, Roll):%n");
        System.out.printf(Locale.US, "  Yaw:   [%7.2f°, %7.2f°]%n", yaws.getMin(), yaws.getMax());
        System.out.printf(Locale.US, "  Pitch: [%7.2f°, %7.2f°]%n", pitches.getMin(), pitches.getMax());
        System.out.printf(Locale.US, "  Roll:  [%7.2f°, %7.2f°]%n", rolls.getMin(), rolls.getMax());

        System.out.println("================================================================================");
    }

   
}