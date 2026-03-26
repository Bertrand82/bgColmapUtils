package bg.images.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;

import bg.util.PositionGps2Factory;
import bg.metadata.MetaData;
import bg.metadata.MetaDatasCsv;
import bg.util.PositionGps2;


public class ProcessGPSFromJPG {
	
	File metadataCsvFile;
	MetaDatasCsv processCsv;
	HashMap<String, PositionGps2> hGpsLocation = new HashMap<String, PositionGps2>();
	
	public ProcessGPSFromJPG(File dir) throws Exception {
		metadataCsvFile= new File(dir,"metadata.csv");
		File dirImages = new File(dir,"images");
		this.processCsv = new MetaDatasCsv(metadataCsvFile, dirImages);
		System.out.println("metadata "+metadataCsvFile.getName()+" exists :"+metadataCsvFile.exists()+" | dirImages exists : "+dirImages.exists());
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
			PositionGps2 gpsPosition = hGpsLocation.get(imageName);
			MetaData metaData =  this.processCsv.getImageDronViewByImageName(imageName);
			String logDz =" dz = "+String.format("%7.3f",  gpsPosition.getAltitudeMeters()- metaData.z);
			System.out.println(logDz+" "+gpsPosition+" || metaData: "+metaData);
		}
		
	}



	private void processImageFile(File f) {
		try {
			
			PositionGps2 gpsPosition = PositionGps2Factory.extractPosition(f);
			 //System.out.println(f.getName()+"    "+gpsPosition);
			 hGpsLocation.put(f.getName(), gpsPosition);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
