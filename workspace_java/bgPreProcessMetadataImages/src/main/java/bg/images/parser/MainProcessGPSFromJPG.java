package bg.images.parser;

import java.io.File;

public class MainProcessGPSFromJPG {

	public static void main(String[] args) throws Exception {
		String directoryPath = "D:\\aws_drones_images\\location1";
		directoryPath="D:\\aws_drones_images\\generated_pitch\\vol_pitch_60\\images";
		//directoryPath="D:\\aws_drones_images - Copie\\generated\\vol_7";
		File dir = new File(directoryPath);
		
		new ProcessGPSFromJPG(dir);
		
		
	}

}
