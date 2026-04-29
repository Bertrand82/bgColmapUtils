package bg.display.divide;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ColmapSubsetBuilder {

  // ---- Public API ---------------------------------------------------------

  /**
   * Crée un sous-modèle COLMAP au format TXT.
   *
   * @param sparseTxtDir   dossier contenant cameras.txt, images.txt, points3D.txt
   * @param outTxtDir      dossier de sortie
   * @param imageNames     noms EXACTS des images à conserver (comme dans images.txt)
   */
  public static void buildSubsetTxt(Path sparseTxtDir, Path outTxtDir, Set<String> imageNames) throws IOException {
    Objects.requireNonNull(sparseTxtDir);
    Objects.requireNonNull(outTxtDir);
    Objects.requireNonNull(imageNames);

    ColmapTxtModel model = readModelTxt(sparseTxtDir);

    // 1) garder uniquement les images demandées (par NAME)
    Map<Integer, ImageEntry> keptImages = model.images.values().stream()
        .filter(img -> imageNames.contains(img.name))
        .collect(Collectors.toMap(img -> img.imageId, img -> img, (a,b) -> a, LinkedHashMap::new));

    if (keptImages.isEmpty()) {
      throw new IllegalArgumentException("Aucune image demandée n'a été trouvée dans images.txt");
    }

    // 2) caméras utilisées par les images gardées
    Set<Integer> keptCameraIds = keptImages.values().stream()
        .map(img -> img.cameraId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    Map<Integer, String> keptCamerasTxt = model.camerasTxtLines.entrySet().stream()
        .filter(e -> keptCameraIds.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));

    // 3) déterminer quels points 3D restent (points observés par au moins une image gardée)
    // On part des observations présentes dans les images (point3DId != -1)
    Set<Long> candidatePointIds = new LinkedHashSet<>();
    for (ImageEntry img : keptImages.values()) {
      for (Point2D p2 : img.points2D) {
        if (p2.point3DId >= 0) candidatePointIds.add(p2.point3DId);
      }
    }

    // 4) filtrer points3D.txt + filtrer les tracks pour ne garder que les observations sur images gardées
    Map<Long, Point3DEntry> keptPoints = new LinkedHashMap<>();
    for (long pid : candidatePointIds) {
      Point3DEntry p3 = model.points3D.get(pid);
      if (p3 == null) continue; // peut arriver si incohérence / fichier partiel

      List<TrackObs> newTrack = new ArrayList<>();
      for (TrackObs obs : p3.track) {
        if (keptImages.containsKey(obs.imageId)) newTrack.add(obs);
      }
      if (!newTrack.isEmpty()) {
        keptPoints.put(pid, p3.withTrack(newTrack));
      }
    }

    // 5) filtrer les points2D de chaque image: si point3DId n'existe plus -> le mettre à -1
    for (ImageEntry img : keptImages.values()) {
      for (int i = 0; i < img.points2D.size(); i++) {
        Point2D p2 = img.points2D.get(i);
        if (p2.point3DId >= 0 && !keptPoints.containsKey(p2.point3DId)) {
          img.points2D.set(i, new Point2D(p2.x, p2.y, -1));
        }
      }
    }

    // 6) écrire le subset
    Files.createDirectories(outTxtDir);
    writeCamerasTxt(outTxtDir.resolve("cameras.txt"), model.camerasHeaderLines, keptCamerasTxt);
    writeImagesTxt(outTxtDir.resolve("images.txt"), model.imagesHeaderLines, keptImages);
    writePoints3DTxt(outTxtDir.resolve("points3D.txt"), model.pointsHeaderLines, keptPoints);
  }

  // ---- Model structures ---------------------------------------------------

  private static final class ColmapTxtModel {
    // headers (comment lines starting with '#') preserved
    final List<String> camerasHeaderLines = new ArrayList<>();
    final List<String> imagesHeaderLines = new ArrayList<>();
    final List<String> pointsHeaderLines = new ArrayList<>();

    // cameras: we keep the raw line to avoid reformatting PARAMS
    final Map<Integer, String> camerasTxtLines = new LinkedHashMap<>();

    final Map<Integer, ImageEntry> images = new LinkedHashMap<>();
    final Map<Long, Point3DEntry> points3D = new LinkedHashMap<>();
  }

  private static final class ImageEntry {
    final int imageId;
    final double qw, qx, qy, qz, tx, ty, tz;
    final int cameraId;
    final String name;
    final List<Point2D> points2D;

    ImageEntry(int imageId, double qw, double qx, double qy, double qz,
               double tx, double ty, double tz, int cameraId, String name, List<Point2D> points2D) {
      this.imageId = imageId;
      this.qw = qw; this.qx = qx; this.qy = qy; this.qz = qz;
      this.tx = tx; this.ty = ty; this.tz = tz;
      this.cameraId = cameraId;
      this.name = name;
      this.points2D = points2D;
    }
  }

  private record Point2D(double x, double y, long point3DId) {}

  private record TrackObs(int imageId, int point2DIdx) {}

  private static final class Point3DEntry {
    final long pointId;
    final double x, y, z;
    final int r, g, b;
    final double error;
    final List<TrackObs> track;
    final String prefixLine; // everything before TRACK, to preserve formatting if you want

    Point3DEntry(long pointId, double x, double y, double z, int r, int g, int b, double error,
                 List<TrackObs> track, String prefixLine) {
      this.pointId = pointId;
      this.x = x; this.y = y; this.z = z;
      this.r = r; this.g = g; this.b = b;
      this.error = error;
      this.track = track;
      this.prefixLine = prefixLine;
    }

    Point3DEntry withTrack(List<TrackObs> newTrack) {
      return new Point3DEntry(pointId, x, y, z, r, g, b, error, newTrack, prefixLine);
    }
  }

  // ---- Reading ------------------------------------------------------------

  private static ColmapTxtModel readModelTxt(Path dir) throws IOException {
    ColmapTxtModel m = new ColmapTxtModel();
    readCamerasTxt(dir.resolve("cameras.txt"), m);
    readImagesTxt(dir.resolve("images.txt"), m);
    readPoints3DTxt(dir.resolve("points3D.txt"), m);
    return m;
  }

  private static void readCamerasTxt(Path path, ColmapTxtModel m) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) continue;
        if (line.startsWith("#")) { m.camerasHeaderLines.add(line); continue; }
        String[] tok = splitWs(line);
        int cameraId = Integer.parseInt(tok[0]);
        m.camerasTxtLines.put(cameraId, line);
      }
    }
  }

  private static void readImagesTxt(Path path, ColmapTxtModel m) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) continue;
        if (line.startsWith("#")) { m.imagesHeaderLines.add(line); continue; }

        // line 1
        String[] t = splitWs(line);
        int imageId = Integer.parseInt(t[0]);
        double qw = Double.parseDouble(t[1]);
        double qx = Double.parseDouble(t[2]);
        double qy = Double.parseDouble(t[3]);
        double qz = Double.parseDouble(t[4]);
        double tx = Double.parseDouble(t[5]);
        double ty = Double.parseDouble(t[6]);
        double tz = Double.parseDouble(t[7]);
        int cameraId = Integer.parseInt(t[8]);
        // NAME may contain no spaces in COLMAP; we assume no spaces.
        String name = t[9];

        // line 2: points2D
        String line2 = br.readLine();
        if (line2 == null) throw new IOException("images.txt: ligne points2D manquante pour imageId=" + imageId);
        List<Point2D> pts2d = parsePoints2D(line2);

        m.images.put(imageId, new ImageEntry(imageId, qw, qx, qy, qz, tx, ty, tz, cameraId, name, pts2d));
      }
    }
  }

  private static List<Point2D> parsePoints2D(String line) {
    String trimmed = line.trim();
    if (trimmed.isEmpty()) return new ArrayList<>();
    String[] tok = splitWs(trimmed);
    if (tok.length % 3 != 0) {
      // parfois il y a des doubles espaces / etc; splitWs gère, mais si le fichier est corrompu -> on prévient
      throw new IllegalArgumentException("Ligne points2D invalide (attendu multiple de 3): " + line);
    }
    List<Point2D> out = new ArrayList<>(tok.length / 3);
    for (int i = 0; i < tok.length; i += 3) {
      double x = Double.parseDouble(tok[i]);
      double y = Double.parseDouble(tok[i + 1]);
      long pid = Long.parseLong(tok[i + 2]);
      out.add(new Point2D(x, y, pid));
    }
    return out;
  }

  private static void readPoints3DTxt(Path path, ColmapTxtModel m) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) continue;
        if (line.startsWith("#")) { m.pointsHeaderLines.add(line); continue; }

        String[] tok = splitWs(line);
        long pid = Long.parseLong(tok[0]);
        double x = Double.parseDouble(tok[1]);
        double y = Double.parseDouble(tok[2]);
        double z = Double.parseDouble(tok[3]);
        int r = Integer.parseInt(tok[4]);
        int g = Integer.parseInt(tok[5]);
        int b = Integer.parseInt(tok[6]);
        double error = Double.parseDouble(tok[7]);

        // track starts at index 8: pairs (IMAGE_ID, POINT2D_IDX)
        List<TrackObs> track = new ArrayList<>();
        for (int i = 8; i + 1 < tok.length; i += 2) {
          int imageId = Integer.parseInt(tok[i]);
          int point2DIdx = Integer.parseInt(tok[i + 1]);
          track.add(new TrackObs(imageId, point2DIdx));
        }

        // prefixLine for re-writing (up to ERROR)
        String prefix = String.format(Locale.ROOT, "%d %.17g %.17g %.17g %d %d %d %.17g",
            pid, x, y, z, r, g, b, error);

        m.points3D.put(pid, new Point3DEntry(pid, x, y, z, r, g, b, error, track, prefix));
      }
    }
  }

  // ---- Writing ------------------------------------------------------------

  private static void writeCamerasTxt(Path path, List<String> header, Map<Integer, String> cameraLines) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (String h : header) bw.write(h + "\n");
      for (String line : cameraLines.values()) bw.write(line + "\n");
    }
  }

  private static void writeImagesTxt(Path path, List<String> header, Map<Integer, ImageEntry> images) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (String h : header) bw.write(h + "\n");

      for (ImageEntry img : images.values()) {
        bw.write(String.format(Locale.ROOT,
            "%d %.17g %.17g %.17g %.17g %.17g %.17g %.17g %d %s\n",
            img.imageId, img.qw, img.qx, img.qy, img.qz, img.tx, img.ty, img.tz, img.cameraId, img.name));

        // points2D line
        if (img.points2D.isEmpty()) {
          bw.write("\n");
        } else {
          StringBuilder sb = new StringBuilder(img.points2D.size() * 20);
          for (Point2D p : img.points2D) {
            sb.append(String.format(Locale.ROOT, "%.6f %.6f %d ", p.x, p.y, p.point3DId));
          }
          // trim last space
          if (sb.length() > 0) sb.setLength(sb.length() - 1);
          bw.write(sb.toString());
          bw.write("\n");
        }
      }
    }
  }

  private static void writePoints3DTxt(Path path, List<String> header, Map<Long, Point3DEntry> points) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (String h : header) bw.write(h + "\n");

      for (Point3DEntry p : points.values()) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.prefixLine);
        for (TrackObs obs : p.track) {
          sb.append(' ').append(obs.imageId).append(' ').append(obs.point2DIdx);
        }
        bw.write(sb.toString());
        bw.write("\n");
      }
    }
  }

  // ---- Helpers ------------------------------------------------------------

  private static String[] splitWs(String s) {
    return s.trim().split("\\s+");
  }
}