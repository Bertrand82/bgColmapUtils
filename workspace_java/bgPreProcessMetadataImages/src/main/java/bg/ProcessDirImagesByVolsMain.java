package bg;

import java.io.File;

public class ProcessDirImagesByVolsMain {

	public static void main(String[] args) throws Exception {
		String directoryPath = "D:\\aws_drones_images\\location1";
		File dir = new File(directoryPath);
		ProcessDirImagesByVols processDirImages = new ProcessDirImagesByVols(dir);
		
	}

}
