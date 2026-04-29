package bg.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public final class UtilCopyBg {

  private UtilCopyBg() {}

  /**
   * Copie une ressource du classpath vers un fichier sur disque.
   *
   * @param resourcePath chemin dans resources, ex: "templates/report.html" (sans '/' initial)
   * @param targetDir répertoire de destination
   * @return le chemin du fichier copié
   */
  public static Path copyResourceToDir(String resourcePath, Path targetDir) throws IOException {
    Files.createDirectories(targetDir);

    String fileName = Paths.get(resourcePath).getFileName().toString();
    Path targetFile = targetDir.resolve(fileName);

    try (InputStream in = UtilCopyBg.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IllegalArgumentException("Ressource introuvable dans le classpath: " + resourcePath);
      }
      Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    return targetFile;
  }

  public static void main(String[] args) throws Exception {
    Path out = copyResourceToDir("myfile.txt", Paths.get(System.getProperty("java.io.tmpdir"), "out"));
    System.out.println("Copied to: " + out);
  }
}