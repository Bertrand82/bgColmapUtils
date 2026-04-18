package bg.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertiesGlobal {

	private static final File CACHE_DIR_0 = new File(System.getProperty("user.home"), ".cache");
	private static final File CACHE_DIR = new File(CACHE_DIR_0, "bg");
	private static final File CACHE_FILE = new File(CACHE_DIR, "propertiesBG.txt");
	public static final String KEY_DIRECTORY_GENERATED_IMAGES = "DIRECTORY_GENERATED_IMAGES";
	private static Properties PROPERTIES;
	
	
	public static Properties getProperties(){
		if (PROPERTIES == null) {
			try {
				PROPERTIES = new Properties();
				if (CACHE_FILE.exists()) {				
					PROPERTIES.load(new FileReader(CACHE_FILE));				
				}
			} catch (Exception e) {
				// Pas de trace
			} 
		}
		return PROPERTIES;
	}


	public static void saveProperties(String comment) {
		try {
			CACHE_DIR.mkdirs();
			FileWriter fw = new FileWriter(CACHE_FILE);
			PROPERTIES.store(fw, comment);
		} catch (IOException e) {
			
			e.printStackTrace();
		}		
	}


	public static void saveProperty(String key, String value) {
		Properties prop = getProperties();
		prop.setProperty(key, value);
		saveProperties("maj "+key+" : "+value);
		
	}
}
