package bg.util;

import java.io.File;

public class ProcessSubsetsLinuxClean {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest ");
		File dir = new File ("/data/BG");
		for (File d : dir.listFiles()) {
			if (d.isDirectory() && d.getName().startsWith("paquet_")){
				try {
					UtilFile.deleteDirRecursive(d);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
