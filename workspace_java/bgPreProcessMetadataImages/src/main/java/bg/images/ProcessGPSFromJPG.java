package bg.images;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;

import bg.MetaData;
import bg.MetaDatasCsv;
import bg.util.GpsExifExtractor;
import bg.util.GpsExifExtractor.GpsPosition;

public class ProcessGPSFromJPG {
	
	File metadataCsvFile;
	MetaDatasCsv processCsv;
	HashMap<String, GpsPosition> hGpsLocation = new HashMap<String, GpsExifExtractor.GpsPosition>();
	
	public ProcessGPSFromJPG(File dir) throws Exception {
		metadataCsvFile= new File(dir,"metadata.csv");
		this.processCsv = new MetaDatasCsv(metadataCsvFile);
		System.out.println("metadata "+metadataCsvFile.getName()+" exists :"+metadataCsvFile.exists());
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.getName().toLowerCase().endsWith(".jpg")) {
				
				processImageFile(f);
			}
		}
		System.out.println("nb images   : "+hGpsLocation.size());
		System.out.println("nb metadate : "+processCsv.getList().size());
		checkCoherence();
	}

	

	private void checkCoherence() {
		for( String imageName:this.hGpsLocation.keySet()) {
			GpsPosition gpsPosition = hGpsLocation.get(imageName);
			MetaData idv =  this.processCsv.getImageDronViewByImageName(imageName);
			System.out.println(gpsPosition+"  "+idv);
		}
		
	}



	private void processImageFile(File f) {
		try {
			
			GpsPosition gpsPosition = GpsExifExtractor.extractPosition(f);
			 //System.out.println(f.getName()+"    "+gpsPosition);
			 hGpsLocation.put(f.getName(), gpsPosition);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
