package bg;

import java.io.File;

public class ProcessDirImagesByCameraAngleMain {

	public static void main(String[] args) throws Exception {
		String directoryPath = "D:\\aws_drones_images\\location1";
		File dir = new File(directoryPath);
		ProcessDirImagesByCameraAngle processDirImages = new ProcessDirImagesByCameraAngle(dir);
		
	}

}
