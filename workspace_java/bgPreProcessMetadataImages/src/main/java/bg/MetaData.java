package bg;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bg.util.GpsPositionFactory;
import bg.util.GpsPosition2;


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
	GpsPosition2 gpsPosition;

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
				"| numeroSequence=" + String.format( "%04d",numeroSequence) + 
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




	public int updateGpsPosition(File fileImage) throws Exception {
		this.gpsPosition = GpsPositionFactory.extractPosition(fileImage);
		if (this.gpsPosition==null) {
			return 0;
		}else {
			return 1;
		}
		
	}



    double xCorrected;
    double yCorrected;
    double rView;
	public void correctGpsPosition() {
		double altitudeSol=0 ; // Prendre l'altitude du lieu voir: class ElevationClient
		double angleOuvertureCamera=60; 
		double zz;
		double xx;
		double yy;
		if (gpsPosition ==null) {
			zz=this.z;
			xx= this.x;
			yy=this.y;
		}else {
			zz = gpsPosition.getAltitudeMeters();
			xx=gpsPosition.getX();
			yy=gpsPosition.getY();
		}
		
		double hauteur = zz-altitudeSol;
		double delta =hauteur *Math.cos(Math.toRadians(pitch)   );
		 xCorrected = xx+delta*Math.cos(Math.toRadians(yaw));
		 yCorrected = yy+delta*Math.sin(Math.toRadians(yaw));
		 double rTAvhe =hauteur*Math.sin(Math.toRadians(angleOuvertureCamera));
		 this.rView=Math.abs(rTAvhe);
		 
	//	System.out.println(" yaw "+yaw+"  z:"+z+ "   pitch: "+this.pitch+"  delta :"+String.format("%7.2f",delta));
	}
	
	List<MetaData> listClose = new ArrayList<MetaData>();
	public void searchCloseView(int i, List<MetaData> list2) {
		int j=0;
		while (j < list2.size()) {
			
			MetaData m2 = list2.get(j);
			if (m2.equals(this)) {
				
			}else if (isClose(m2)) {
				listClose.add(m2);
			}
			j++;
		}
		System.out.println("listClose : "+listClose.size()+" rView "+rView);
		
	}




	public List<MetaData> getListClose() {
		return listClose;
	}




	private boolean isClose(MetaData m2) {
		double dx = Math.abs(m2.xCorrected - xCorrected);
		double dy = Math.abs(m2.yCorrected - yCorrected);
		return (dx < rView)  && (dy <rView);
	}


}
