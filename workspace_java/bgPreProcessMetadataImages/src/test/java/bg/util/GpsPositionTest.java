package bg.util;

import java.io.File;

import org.junit.Test;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;

public class GpsPositionTest {

	
	@Test
	public void testGpsFactory() throws Exception{
		File dir =new File("src/test/resources");
		File fileImage = new File(dir,"DJI_20260206153808_0798_D.JPG");
		System.out.println("testGpsFactory  dir exists :"+dir.exists());
		
		PositionGps2 gps  = PositionGps2Factory.extractPosition(fileImage);
		System.out.println("Gps : "+gps);
		Metadata metadata = ImageMetadataReader.readMetadata(fileImage);
		 for(Directory dd : metadata.getDirectories()) {
         	System.out.println("directory :"+dd.getName());
         }
	}
}
