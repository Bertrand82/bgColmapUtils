package bg.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
public final class UtilCopyBg {

  private UtilCopyBg() {}

  /**
   * Copie une ressource du classpath vers un fichier sur disque.
   *
   * @param resourcePath chemin dans resources, ex: "templates/report.html" (sans '/' initial)
   * @param targetDir répertoire de destination
   * @return le chemin du fichier copié
   */
  public static File copyResourceToDir(String resourcePath, Path targetDir, boolean isExecutable) throws IOException {
    Files.createDirectories(targetDir);

    String fileName = Paths.get(resourcePath).getFileName().toString();
    File targetFile = new File(targetDir.toFile(),fileName);

    try (InputStream in = UtilCopyBg.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IllegalArgumentException("Ressource introuvable dans le classpath: " + resourcePath);
      }
      Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    if (isExecutable) {
      setExecutableIfPossible(targetFile.toPath());
    }

    return targetFile;
  }
  
  public static boolean copyResourcesToDir(String classpathDir,Path targetDir) {
	  try {
		List<String> listResources = listRepertoireInResources(classpathDir);
		  for (String resPath : listResources) {
			  boolean isExecutable = resPath.endsWith(".sh");
			  File file =UtilCopyBg.copyResourceToDir(resPath, targetDir, isExecutable);
			  System.out.println("file exists : "+file.exists()+" : "+file.getAbsolutePath()+"  ");
			 
		  }
		  return true;
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	  }
	 

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
  
  




  /**
   * Liste les ressources directement sous classpathDir (non récursif).
   * classpathDir: ex "templates" ou "templates/" (sans slash initial).
   */
  public static List<String> listRepertoireInResources(String classpathDir) throws IOException {
    String dir = normalizeDir(classpathDir);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> urls = cl.getResources(dir);

    List<String> result = new ArrayList<>();
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      String protocol = url.getProtocol();

      if ("file".equals(protocol)) {
        // Exécution depuis un dossier (target/classes, build/classes, etc.)
        try {
          Path root = Paths.get(url.toURI());
          try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path p : stream) {
              // nom relatif au dir classpath
              result.add(dir + p.getFileName().toString());
            }
          }
        } catch (Exception e) {
          throw new IOException("Failed to list file resources for " + url, e);
        }

      } else if ("jar".equals(protocol)) {
        // Exécution depuis un JAR
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        try (JarFile jar = conn.getJarFile()) {
          Enumeration<JarEntry> entries = jar.entries();
          while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (entry.isDirectory()) continue;

            // enfant direct de dir (non récursif)
            if (name.startsWith(dir)) {
              String rest = name.substring(dir.length());
              if (!rest.isEmpty() && !rest.contains("/")) {
                result.add(name);
              }
            }
          }
        }

      } else {
        // ex: "jrt" (modules), "bundleresource" (OSGi), etc.
        // À traiter au cas par cas selon l'environnement.
        throw new IOException("Unsupported protocol: " + protocol + " for URL " + url);
      }
    }

    // dédoublonne + tri
    return result.stream().distinct().sorted().toList();
  }

  private static String normalizeDir(String classpathDir) {
    String d = classpathDir.startsWith("/") ? classpathDir.substring(1) : classpathDir;
    if (!d.isEmpty() && !d.endsWith("/")) d = d + "/";
    return d;
  }

  public static void main(String[] args) throws Exception {
    System.out.println(listRepertoireInResources("sh"));
  }
}