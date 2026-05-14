package bg.util;

import java.io.File;

import bg.display.divide.ProcessSubsets;

public class ProcessSubsetsLinux {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest ");
		File dir = new File ("/data/BG");
		ProcessSubsets processSubset = new ProcessSubsets(dir, 20,0.2);
	}

}
