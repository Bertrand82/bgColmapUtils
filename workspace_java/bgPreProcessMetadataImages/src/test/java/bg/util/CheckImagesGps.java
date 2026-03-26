package bg.util;

import java.io.File;

import org.junit.Test;

public class CheckImagesGps {

	@Test
	public void testDir() throws Exception{
		File dir = new File("D:\\aws_drones_images\\TEMP\\generated\\vol_2\\images");
		File[] imagesFile = dir.listFiles();
		for (File ff : imagesFile) {
			PositionGps2 gps  = PositionGps2Factory.extractPosition(ff);
			System.out.println(" ff "+ff.getName()+"  "+gps);
		}
	}
}
