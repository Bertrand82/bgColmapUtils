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

public class ProcessDirImagesByCameraAngle {
	private DateTimeFormatter TS14_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final Pattern TS14 = Pattern.compile("(\\d{14})");
	private static DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmss");

	
	MetaDatasCsv processCsv;
	List<Vol> listVols = new ArrayList<Vol>();
	File dirGenerated;
	File dirIn;
	File dirImages;
	public ProcessDirImagesByCameraAngle(File dir) throws Exception {
		dirIn = dir.getCanonicalFile();
		dirImages = new File(dirIn,"images");
		init(dirIn, dirImages);
		processVols(this.listVols);
	}
	
	private void init(File dirIn, File dirImages) throws Exception{
		
		dirGenerated = new File(dirIn.getParentFile(),"generated_pitch");
		dirGenerated.mkdirs();
		
		File[]  filesImages = dirIn.listFiles();
		// Tri par date
		Arrays.sort(filesImages, Comparator.comparing(f -> f.getName().toLowerCase()));
		System.out.println("Fichiers dans : " + dirIn.getAbsolutePath());
		int i = 0;
		
		File[] files2 = dirImages.listFiles();
		for (File f : files2) {
			if (f.getName().endsWith(".csv")) {
				this.processCsv= new MetaDatasCsv(f,dirImages);
			}
		}
		int numeroVol =0;
		
		for (File f : filesImages) {
			
			if (f.isFile() && (f.getName().startsWith("DJI"))) {
				MetaData droneView = this.processCsv.getMetaData(f.getName());
				Double pitch = droneView.pitch;
				
				Vol vol = getVolFromPitch(pitch);
				if (vol == null) {
					vol = new Vol(( pitch.intValue()));
					vol.pitchInt=pitch.intValue();
					listVols.add(vol);
				}
				vol.list.add(droneView);				
			}
		}
	
		System.out.println(this.toString());
	}

	
    
	private Vol getVolFromPitch(Double pitch) {
		for (Vol vv : listVols) {
			if (vv.pitchInt == pitch.intValue()) {
				return vv;
			}
		}
		return null;
	}

	private void processVols(List<Vol> listVols2) {
		for(Vol v : listVols2){
			if (v.list.size()>4) {
				processVol(v);
			}
		}
		
	}



	private void processVol(Vol vol) {
		File dirOut = new File(dirGenerated,("vol_pitch_"+vol.pitchInt).replace("-", ""));
		dirOut.mkdirs();
		vol.generateExtraction(dirImages,dirOut,2000);
		System.out.println("result : pitch : "+vol.pitchInt+"  "+vol.list.size());
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