package bg.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class UtilFile {

	public static boolean existsDir(File dirSources) {
		if (dirSources == null) {
			return false;
		}
		return dirSources.exists();
	}

	public static File mostRecentFile(File dir) {
		if (dir == null || !dir.isDirectory())
			return null;

		File[] files = dir.listFiles(File::isFile);
		if (files == null || files.length == 0)
			return null;

		return Arrays.stream(files).max(Comparator.comparingLong(File::lastModified)).orElse(null);
	}

	public static String toString(File file) {
		if (file == null) {
			return null;
		}
		String s =" exists : "+file.exists()+"  name :"+file.getName();
		return s;
		
	}
	public static void deleteDirRecursive(File dir) throws Exception {
		  if (dir == null) return;

		  java.nio.file.Path p = dir.toPath();
		  if (!java.nio.file.Files.exists(p)) return;

		  java.nio.file.Files.walkFileTree(p, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
		    @Override
		    public java.nio.file.FileVisitResult visitFile(java.nio.file.Path file,
		                                                  java.nio.file.attribute.BasicFileAttributes attrs)
		        throws java.io.IOException {
		      java.nio.file.Files.deleteIfExists(file);
		      return java.nio.file.FileVisitResult.CONTINUE;
		    }

		    @Override
		    public java.nio.file.FileVisitResult postVisitDirectory(java.nio.file.Path d, java.io.IOException exc)
		        throws java.io.IOException {
		      if (exc != null) throw exc;
		      java.nio.file.Files.deleteIfExists(d);
		      return java.nio.file.FileVisitResult.CONTINUE;
		    }
		  });
		}
}
