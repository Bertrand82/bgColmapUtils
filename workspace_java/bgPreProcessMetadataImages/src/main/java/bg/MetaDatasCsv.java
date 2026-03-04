package bg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaDatasCsv {
	
	List<MetaData> list =  new ArrayList<MetaData>();

	public MetaDatasCsv(File f) throws Exception {
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while((line= br.readLine() ) != null) {
			MetaData iv = new MetaData(line);
			list.add(iv);
		}
	}

	public MetaData getImageDroneView(String name) {
		for(MetaData idv :list) {
			if(idv.fileName.equals(name)) {
				return idv;
			}
		};
		return null;
	}
	
	public static void main(String[] a) throws Exception{
		File fileMetadata = new File("metadata.csv");
		new MetaDatasCsv(fileMetadata);
	}

	public List<MetaData> getList() {
		return list;
	}

	public MetaData getImageDronViewByImageName(String imageName) {
		for (MetaData idv :list) {
			if (idv.fileName.equals(imageName)) {
				return idv;
			}
		}
		return null;
	}

}
