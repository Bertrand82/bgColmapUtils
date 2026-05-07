package bg.util;

import java.io.File;

import bg.process.log.LogProcess;

public class MainProcessLogs {

	public static void main(String[] args) {
		File dirRoot = new File("/data/BG");
		File dirLog = new File(dirRoot,"logs");
		System.out.println("dirRoot : exists: "+dirRoot.exists());
		System.out.println("dirLog : exists: "+dirLog.exists());
		for (File fileLog : dirLog.listFiles()) {
			System.out.println("-------------------------------------"+fileLog.getName());
			LogProcess logProcess = new LogProcess(fileLog);
			System.out.println(""+logProcess.toString());
			System.out.println("-------------------------------------");
		}
	}

}
