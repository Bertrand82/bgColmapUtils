package bg.util;

import java.io.File;
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

}
