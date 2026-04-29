package bg.util;

import java.io.File;

import bg.display.divide.ProcessSubsets;

public class ProcessSubsetsTestWindows {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest ");
		File dir = new File ("C:\\Users\\bertr\\data\\BG");
		ProcessSubsets processSubset = new ProcessSubsets(dir, 20);
	}

}
