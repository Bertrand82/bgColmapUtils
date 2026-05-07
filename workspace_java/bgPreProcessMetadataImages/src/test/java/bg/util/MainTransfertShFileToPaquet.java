package bg.util;

import java.io.File;

public class MainTransfertShFileToPaquet {

	public static void main(String[] args) {
		File fileDest = new File("/data/BG");
	    for( File file : fileDest.listFiles()) {
	    	if (file.isDirectory() && file.getName().startsWith("paquet")) {
	    		UtilCopyBg.copyResourcesToDir("paquet", file.toPath());
	    	}
	    }
	}

}
