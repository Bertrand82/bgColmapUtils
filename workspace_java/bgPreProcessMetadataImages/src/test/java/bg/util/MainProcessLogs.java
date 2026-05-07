package bg.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import bg.process.log.LogFactory;
import bg.process.log.LogProcess;

public class MainProcessLogs {

	public static void main(String[] args) {
		File dirRoot = new File("/data/BG");
		LogFactory.process(dirRoot);
	}

}
