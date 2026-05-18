package bg.util;

import java.io.File;

import bg.display.divide1.ProcessSubsets;

public class ProcessSubsetsTestWindows {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest ");
		File dir = new File("C:\\Users\\bertr\\data\\BG");
		int paquetSize = 20;
		double tauxRecouvrement = 0.2;
		ProcessSubsets processSubset = new ProcessSubsets(dir, paquetSize, tauxRecouvrement);
		System.out.println(processSubset.traceSubset());
	}

}
