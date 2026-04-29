package bg.util;

import java.io.File;

public class ProcessSubsetsLinuxClean {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest Clean ");
		File dir = new File ("/data/BG");
		System.err.println("ProcessSubsetTest Clean "+dir.getAbsolutePath());
		for (File d : dir.listFiles()) {
			if (d.isDirectory() && d.getName().startsWith("paquet_")){
				try {
					boolean deleted =UtilFile.deleteDirRecursive(d);
					System.out.println("  deleted "+deleted+"  "+d.getAbsolutePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	

	

}
