package bg.util;

import java.io.File;

public class UtilFile {

	public static boolean existsDir(File dirSources) {
		if (dirSources == null) {
			return false;
		}
		return dirSources.exists();
	}

}
