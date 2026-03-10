package bg.images.matcher.checker;

import java.io.File;
import java.util.List;

import bg.images.matcher.factory.PreMatcher;

public class MainPairChecker {
	
	static final public String directoryPath="D:\\aws_drones_images\\generated_pitch\\vol_pitch_60";
	static final public File dir = new File(directoryPath);
	static final public File dirImages = new File(dir,"images");
	static final public File fileDataBase = new File(dir,"database.db");
	static final public File fileMetadata = new File(dir,"metadata.csv");
	static final public File filePairs = new File(dir, "match.txt");
	
	public static void main(String[] args) throws Exception {
		
        PairChecker pairChecker = new PairChecker(filePairs);
        System.out.println("PreMatcher done "+filePairs.getCanonicalPath());
	}

}
