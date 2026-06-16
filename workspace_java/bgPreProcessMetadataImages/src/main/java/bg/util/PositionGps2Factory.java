package bg.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;



public final class PositionGps2Factory {

	/**
	 * Extrait latitude/longitude et altitude (si présente) depuis l'EXIF GPS.
	 * 
	 * @return null si aucune info GPS exploitable.
	 */
	public static PositionGps2 extractPosition(File imageFile) {
		PositionGps2 gps2 = PositionGps2FactoryApache.extractPosition(imageFile);
		System.out.println("->>>>--- "+gps2);
		return gps2;
	}
	
	
	
	
	

	public static List<PositionGps2> getListGpsPositionFromDirImages(File dir) {
		List<PositionGps2> listA = new ArrayList<PositionGps2>();
		if (dir == null) {
			return listA;
		}
		if (!dir.exists()) {
			return listA;
		}
		for (File fImage : dir.listFiles()) {
			PositionGps2 gps = extractPosition(fImage);
			if (gps == null) {
				System.err.println("gps is null " + fImage.getName());
			} else {
				System.out.println("gps ::: " + fImage.getName() + "  " + gps);
				listA.add(gps);
			}
		}
		return listA;
	}

}