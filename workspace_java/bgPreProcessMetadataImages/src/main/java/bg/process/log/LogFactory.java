package bg.process.log;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LogFactory {

	public static String  process(File dirRoot) {
		File dirLog = new File(dirRoot,"logs");
		String s= "";
		s+="dirRoot : exists: "+dirRoot.exists()+"\n";
		s+="dirLog : exists: "+dirLog.exists()+"\n";
		List<File> listFiles =Arrays.asList(dirLog.listFiles());
		listFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
		for (File fileLog : listFiles) {
			s+="-------------------------------------"+fileLog.getName()+"\n";
			LogProcess logProcess = new LogProcess(fileLog);
			s+=""+logProcess.toString()+"\n";
			s+="-------------------------------------"+"\n";
		}
		System.out.println(s);
		return s;
	}

}
