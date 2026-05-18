package bg.util;

import java.io.File;

import bg.display.divide1.ProcessSubsets_1;
import bg.display.divide2.ProcessSubsets2;

public class ProcessSubsetsLinux2 {

	public static void main(String[] args) throws Exception {
		System.err.println("ProcessSubsetTest ");
		File dir = new File ("/data/BG");
		
		System.err.println("ProcessSubsetTest ");
	
		int paquetSize = 20;
		double tauxRecouvrement = 0.2;
		ProcessSubsets2 processSubset2 = new ProcessSubsets2(dir, paquetSize, tauxRecouvrement);
		System.out.println(processSubset2.traceSubset());
	}

}
