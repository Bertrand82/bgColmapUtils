package bg.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaDatasCsv {
	
	List<MetaData> list =  new ArrayList<MetaData>();

	public MetaDatasCsv(File fMetadataCsv, File dirImages) throws Exception {
		FileReader fr = new FileReader(fMetadataCsv);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while((line= br.readLine() ) != null) {
			MetaData iv = new MetaData(line);
			File fileImage = new File( dirImages,iv.fileName);
			if (fileImage.exists()) {
				list.add(iv);
			}
		}
	}

	public MetaData getMetaData(String name) {
		for(MetaData idv :list) {
			if(idv.fileName.equals(name)) {
				return idv;
			}
		};
		return null;
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
