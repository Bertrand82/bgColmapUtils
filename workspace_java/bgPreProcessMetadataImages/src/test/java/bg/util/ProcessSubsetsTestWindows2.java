package bg.util;

import java.io.File;

import bg.display.divide2.ProcessSubsets2;

public class ProcessSubsetsTestWindows2 {

	public static void main(String[] args) throws Exception {
		System.err.println("ProcessSubsetTest ");
		File dir = new File("C:\\Users\\bertr\\data\\BG");
		int paquetSize = 20;
		double tauxRecouvrement = 0.2;
		ProcessSubsets2 processSubset = new ProcessSubsets2(dir, paquetSize, tauxRecouvrement);
		System.out.println(processSubset.traceSubset());
	}

}
