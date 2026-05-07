package bg.util;

import java.io.File;

public class MainTransfertShFile {

	public static void main(String[] args) {
		File fileDest = new File("/data/BG");
		UtilCopyBg.copyResourcesToDir("sh", fileDest.toPath());
	}

}
