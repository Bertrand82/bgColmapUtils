package bg.images;

import java.io.File;

public class MainProcessGPSFromJPG {

	public static void main(String[] args) throws Exception {
		String directoryPath = "D:\\aws_drones_images\\location1";
		File dir = new File(directoryPath);
		
		new ProcessGPSFromJPG(dir);
		
		
	}

}
