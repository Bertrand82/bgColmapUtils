package bg.metadata;

import java.io.File;

public class Main_metadata_by_vols {

	public static void main(String[] args) throws Exception {
		String directoryPath = "D:\\aws_drones_images\\location1";
		directoryPath="D:\\aws_drones_images\\TEMP\\location1_TEMP";
		File dir = new File(directoryPath);
		File dirImages = new File(dir,"images");
		ProcessDirImagesByVols processDirImages = new ProcessDirImagesByVols(dir,dirImages);
		
	}

}
