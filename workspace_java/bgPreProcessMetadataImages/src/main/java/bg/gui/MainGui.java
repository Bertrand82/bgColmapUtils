package bg.gui;

import java.io.File;

public class MainGui {

	public static void main(String[] args) throws Exception{
		String directoryPath = "D:\\aws_drones_images\\location1";
		File dir = new File(directoryPath);
		System.out.println("dir exists : "+dir.exists()+" | path :"+dir.getAbsolutePath());
		new DataGui(dir);
	}

}
