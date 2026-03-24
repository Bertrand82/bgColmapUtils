package bg.metadata;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessDirImagesByVols {
	private DateTimeFormatter TS14_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final Pattern TS14 = Pattern.compile("(\\d{14})");
	private static DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmss");

	
	MetaDatasCsv processCsv;
	List<Vol> listVols = new ArrayList<Vol>();
	File dirGenerated;
	File dirIn;
	File dirImages ;
	public ProcessDirImagesByVols(File dir, File dirImages) throws Exception {
		dirIn = dir.getCanonicalFile();
		this.dirImages=dirImages;
		init();
		processVols(this.listVols);
	}
	
	private void init() throws Exception{
		
		dirGenerated = new File(dirIn.getParentFile(),"generated");
		dirGenerated.mkdirs();
		
		File[] fileImages = dirImages.listFiles();
		// Tri par date
		Arrays.sort(fileImages, Comparator.comparing(f -> f.getName().toLowerCase()));
		System.out.println("Fichiers dans : " + dirIn.getAbsolutePath());
		int i = 0;
		LocalDateTime Z_1_date = null;
		for (File ff : dirIn.listFiles()) {
			if (ff.getName().endsWith(".csv")) {
				this.processCsv= new MetaDatasCsv(ff,dirImages);
			}
		}
		int numeroVol =0;
		Vol vol  = new Vol(numeroVol++);
		this.listVols.add(vol);
		for (File f : fileImages) {
			
			if (f.isFile() && (f.getName().startsWith("DJI"))) {
				MetaData metaData = this.processCsv.getMetaData(f.getName());
				if (metaData== null) {
					System.out.println("No metadata for "+f.getName());
				}else {
				long delta = diffSeconds(Z_1_date, metaData.date);
				i++;
				System.out.println(String.format("%03d", i++) 
						 + " |  delta:  " + delta+"  "+metaData);
				Z_1_date = metaData.date;
				if (delta > 30) {

					System.out.println(i
							+ "  New Vol                                        --------------------------------");
					i = 0;
					vol  = new Vol(numeroVol++);
					this.listVols.add(vol);
				}
				vol.list.add(metaData);	
				}
			}
		}
	
		System.out.println(this.toString());
	}

	

	private void processVols(List<Vol> listVols2) {
		for(Vol v : listVols2){
			if (v.list.size()>3) {
				processVol(v);
			}
		}
		
	}



	private void processVol(Vol v) {
		File dirOut = new File(dirGenerated,"vol_"+v.numeroVol);
		dirOut.mkdirs();
		v.generateExtraction(dirImages,dirOut,30);
	}



	public static long diffSeconds(LocalDateTime t1, LocalDateTime t2) {
		if (t2 == null) {
			return 0;
		}
		if (t1 == null) {
			return 0;
		}
		return Duration.between(t1, t2).getSeconds(); // t2 - t1
	}
	
	public String toString() {
		String s  = " nb de vols "+this.listVols.size()+"\n";
		int i=0;
		for(Vol v : listVols) {
			s +=i+++"   "+ v.toString()+"\n";
		}
		return s;
	}


}