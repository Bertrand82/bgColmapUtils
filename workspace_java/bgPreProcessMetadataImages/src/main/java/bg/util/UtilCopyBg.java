package bg.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public final class UtilCopyBg {

  private UtilCopyBg() {}

  /**
   * Copie une ressource du classpath vers un fichier sur disque.
   *
   * @param resourcePath chemin dans resources, ex: "templates/report.html" (sans '/' initial)
   * @param targetDir répertoire de destination
   * @return le chemin du fichier copié
   */
  public static Path copyResourceToDir(String resourcePath, Path targetDir, boolean isExecutable) throws IOException {
    Files.createDirectories(targetDir);

    String fileName = Paths.get(resourcePath).getFileName().toString();
    Path targetFile = targetDir.resolve(fileName);

    try (InputStream in = UtilCopyBg.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IllegalArgumentException("Ressource introuvable dans le classpath: " + resourcePath);
      }
      Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    if (isExecutable) {
      setExecutableIfPossible(targetFile);
    }

    return targetFile;
  }
  
  private static void setExecutableIfPossible(Path file) throws IOException {
	    // Si on est sur un FS POSIX (Linux typiquement), on peut gérer finement les droits.
	    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
	      Set<PosixFilePermission> perms;
	      try {
	        perms = Files.getPosixFilePermissions(file);
	      } catch (UnsupportedOperationException e) {
	        perms = EnumSet.noneOf(PosixFilePermission.class);
	      }

	      // Ajoute +x pour user/group/others (équivalent chmod a+x).
	      perms = EnumSet.copyOf(perms);
	      perms.add(PosixFilePermission.OWNER_EXECUTE);
	      perms.add(PosixFilePermission.GROUP_EXECUTE);
	      perms.add(PosixFilePermission.OTHERS_EXECUTE);

	      Files.setPosixFilePermissions(file, perms);
	    } else {
	      // Fallback (Windows / FS non POSIX). Sur Windows, ça n'a souvent pas d'effet.
	      boolean ok = file.toFile().setExecutable(true, false); // false => pour tout le monde
	      if (!ok) {
	        // Pas forcément une erreur fatale, mais tu peux throw si tu veux.
	        // throw new IOException("Impossible de rendre le fichier exécutable: " + file);
	      }
	    }
	  }


  public static void main(String[] args) throws Exception {
    Path out = copyResourceToDir("myfile.txt", Paths.get(System.getProperty("java.io.tmpdir")),true);
    System.out.println("Copied to: " + out);
  }
}