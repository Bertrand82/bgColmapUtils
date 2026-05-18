package bg.util;

import java.io.File;

import bg.display.divide1.ProcessSubsets_1;

public class ProcessSubsetsTestWindows1 {

	public static void main(String[] args) {
		System.err.println("ProcessSubsetTest ");
		File dir = new File("C:\\Users\\bertr\\data\\BG");
		int paquetSize = 20;
		double tauxRecouvrement = 0.2;
		ProcessSubsets_1 processSubset = new ProcessSubsets_1(dir, paquetSize, tauxRecouvrement);
		System.out.println(processSubset.traceSubset());
	}

}
