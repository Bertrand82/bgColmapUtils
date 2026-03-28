package bg.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class PropertiesGlobal {

	private static final File CACHE_DIR_0 = new File(System.getProperty("user.home"), ".cache");
	private static final File CACHE_DIR = new File(CACHE_DIR_0, "bg");
	private static final File CACHE_FILE = new File(CACHE_DIR, "propertiesBG.txt");
	public static final String KEY_DIRECTORY_GENERATED_IMAGES = "DIRECTORY_GENERATED_IMAGES";
	private static Properties PROPERTIES;
	
	
	public static Properties getProperties() throws Exception{
		if (PROPERTIES == null) {
			PROPERTIES = new Properties();
			if (CACHE_FILE.exists()) {				
				PROPERTIES.load(new FileReader(CACHE_FILE));				
			}
		}
		return PROPERTIES;
	}


	public static void saveProperties(String comment) throws Exception{
		CACHE_DIR.mkdirs();
		FileWriter fw = new FileWriter(CACHE_FILE);
		PROPERTIES.store(fw, comment);		
	}
}
