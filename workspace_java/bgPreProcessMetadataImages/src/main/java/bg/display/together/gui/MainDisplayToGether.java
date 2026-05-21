package bg.display.together.gui;

import java.io.File;

public class MainDisplayToGether {

	public static void main(String[] args) throws Exception{
		String directoryPath = "D:\\aws_drones_images\\location1";
		directoryPath = "/data/BG";
		File dir = new File(directoryPath);
		System.out.println("dir exists : "+dir.exists()+" | path :"+dir.getAbsolutePath());
		new DisplayTogetherFrame(dir);

	}

}
