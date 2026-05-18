package bg.util;

import java.io.File;

import bg.display.divide1.ProcessSubsets_1;

public class ProcessSubsetsLinux1 {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest ");
		File dir = new File ("/data/BG");
		ProcessSubsets_1 processSubset = new ProcessSubsets_1(dir, 20,0.2);
	}

}
