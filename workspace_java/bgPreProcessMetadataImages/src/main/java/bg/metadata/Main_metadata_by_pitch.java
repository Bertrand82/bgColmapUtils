package bg.metadata;

import java.io.File;

public class Main_metadata_by_pitch {

	public static void main(String[] args) throws Exception {
		String directoryPath = "D:\\aws_drones_images\\location1";
		directoryPath="D:\\aws_drones_images\\TEMP\\location1_TEMP";
		File dir = new File(directoryPath);
		ProcessDirImagesByCameraAngle processDirImages = new ProcessDirImagesByCameraAngle(dir);
		
	}

}
