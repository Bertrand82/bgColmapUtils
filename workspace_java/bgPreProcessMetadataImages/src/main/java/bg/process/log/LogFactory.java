package bg.process.log;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LogFactory {

	public static void process(File dirRoot) {
		File dirLog = new File(dirRoot,"logs");
		System.out.println("dirRoot : exists: "+dirRoot.exists());
		System.out.println("dirLog : exists: "+dirLog.exists());
		List<File> listFiles =Arrays.asList(dirLog.listFiles());
		listFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
		for (File fileLog : listFiles) {
			System.out.println("-------------------------------------"+fileLog.getName());
			LogProcess logProcess = new LogProcess(fileLog);
			System.out.println(""+logProcess.toString());
			System.out.println("-------------------------------------");
		}
	}

}
