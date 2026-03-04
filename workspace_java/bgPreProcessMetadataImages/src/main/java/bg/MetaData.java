package bg;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaData {
	private static final Pattern SEQ = Pattern.compile("^DJI_\\d{14}_(\\d+)_.*$", Pattern.CASE_INSENSITIVE);

	public String line;
	public String fileName;
	public double x;
	public double y;
	public double z;
	public double yaw;
	public double pitch;
	public double roll;
	
	int numeroSequence ;
	LocalDateTime date;

	// DJI_20260207175436_0671_D.JPG,-207.0166253643368,-404.054606722876,-1.690999999999999,-71.8,-60.0,0.0
	public MetaData(String line) {
		this.line =line;
		String[] tokens = line.split(",");
		int i=0;
		fileName=tokens[i++];
		x =Double.parseDouble(tokens[i++]);
		y =Double.parseDouble(tokens[i++]);
		z =Double.parseDouble(tokens[i++]);
		yaw =Double.parseDouble(tokens[i++]);
		pitch =Double.parseDouble(tokens[i++]);
		roll =Double.parseDouble(tokens[i++]);
		numeroSequence = extractSequenceNumberFromFileName(fileName);
		date = extractDateTimeFromFilename(fileName);
	}
	
	
	

	@Override
	public String toString() {
		return " | fileName=" + fileName + 
				"| x=" +String.format("%06.1f", x)  + 
				"| y=" + String.format("%06.1f", y) +
				"| z=" + String.format("%06.1f", z)  +
				"| yaw=" + String.format("%06.1f",yaw) +
				"| pitch=" + String.format("%06.1f", pitch)  + 
				"| roll=" +  String.format("%06.1f", roll)  + 
				"| numeroSequence=" + numeroSequence + 
				"| date=" + date + "]";
	}




	private static int extractSequenceNumberFromFileName(String filename) {
		Matcher m = SEQ.matcher(filename);
		if (!m.matches()) {
			return -1;
		}
		return Integer.parseInt(m.group(1)); // "0044" -> 44
	}
	
	private static LocalDateTime extractDateTimeFromFilename(String filename) {
		
		Pattern TS14 = Pattern.compile("(\\d{14})");
		DateTimeFormatter TS14_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		
		Matcher m = TS14.matcher(filename);
		if (!m.find()) {
			return null;
		}
		String ts = m.group(1);
		return LocalDateTime.parse(ts, TS14_FORMAT);
	}




	public File getFileImageIn(File dir) {
		File fileImage =new File(dir,this.fileName);
		return fileImage;
	}

}
