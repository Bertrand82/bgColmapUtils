package bg.metadata;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.metadata.Metadata;

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

	public int numeroSequence;
	public LocalDateTime date;
	public GpsPosition2 gpsPosition;

	// DJI_20260207175436_0671_D.JPG,-207.0166253643368,-404.054606722876,-1.690999999999999,-71.8,-60.0,0.0
	public MetaData(String line) {
		this.line = line;
		String[] tokens = line.split(",");
		int i = 0;
		fileName = tokens[i++];
		x = Double.parseDouble(tokens[i++]);
		y = Double.parseDouble(tokens[i++]);
		z = Double.parseDouble(tokens[i++]);
		yaw = Double.parseDouble(tokens[i++]);
		pitch = Double.parseDouble(tokens[i++]);
		roll = Double.parseDouble(tokens[i++]);
		numeroSequence = extractSequenceNumberFromFileName(fileName);
		date = extractDateTimeFromFilename(fileName);
	}

	@Override
	public String toString() {
		return " | fileName=" + fileName + "| x=" + String.format("%06.1f", x) + "| y=" + String.format("%06.1f", y)
				+ "| z=" + String.format("%06.1f", z) + "| yaw=" + String.format("%06.1f", yaw) + "| pitch="
				+ String.format("%06.1f", pitch) + "| roll=" + String.format("%06.1f", roll) + "| numeroSequence="
				+ String.format("%04d", numeroSequence) + "| date=" + date + "]";
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
		File fileImage = new File(dir, this.fileName);
		return fileImage;
	}

	public int updateGpsPosition(File fileImage) throws Exception {
		this.gpsPosition = GpsPositionFactory.extractPosition(fileImage);
		
		if (this.gpsPosition == null) {
			System.out.println("updateGpsPosition "+fileImage.getName()+ "  gps From Image ::  "+this.gpsPosition);
			
			return 0;
		} else {
			return 1;
		}

	}

	double xCorrected;
	double yCorrected;
	double rView;

	public void correctGpsPosition() {
		double altitudeSol = 0; // Prendre l'altitude du lieu voir: class ElevationClient
		double angleOuvertureCamera = 60;
		double zz;
		double xx;
		double yy;
		if (this.gpsPosition == null) {
			zz = this.z+50;
			xx = this.x;
			yy = this.y;
		} else {
			zz = gpsPosition.getAltitudeMeters();
			xx = gpsPosition.getX_process();
			yy = gpsPosition.getY();
		}

		double hauteur = zz - altitudeSol;
		double delta = hauteur * Math.cos(Math.toRadians(pitch));
		xCorrected = xx + delta * Math.cos(Math.toRadians(yaw));
		yCorrected = yy + delta * Math.sin(Math.toRadians(yaw));
		double rTAvhe = hauteur * Math.sin(Math.toRadians(angleOuvertureCamera));
		this.rView = Math.abs(rTAvhe);

		// System.out.println(" yaw "+yaw+" z:"+z+ " pitch: "+this.pitch+" delta
		// :"+String.format("%7.2f",delta));
	}

	List<MetaData> listClose = new ArrayList<MetaData>();
	/**
	 * Cherche les n images les plus proches, n étant compris entre 4 et 8
	 * Si il y en a trop, le traitement est trop long (matcher); si il y en a moins,on presume que la qualité est dégradée. 
	 * @param i
	 * @param list2
	 */
	public void searchCloseView(int i, List<MetaData> list2) {
		MetaData metaData0 =  list2.get(i);
		HashSet<MetaData> listClose1 = searchCloseView2(i, list2, this.rView);// Pour traces uniquement
		HashSet<MetaData> listCloseByGps = new HashSet<MetaData>();
		int n = 0;
		double rView2 = this.rView;
		while (((listCloseByGps.size() >= 8) || (listCloseByGps.size() <= 2)) && (n < 4)) {
			if (listClose1.size() >= 8) {
				rView2=rView2*0.8;
				listCloseByGps = searchCloseView2(i, list2, rView2 );
			} else if (listClose1.size() <= 2) {
				rView2=rView2*1.2;
				listCloseByGps = searchCloseView2(i, list2, rView2);
			} else {
				listCloseByGps = listClose1;
			}
			n++;
		}
		HashSet<MetaData> listCloseByTime = new HashSet<MetaData>();
		for (MetaData mt :list2) {
			long delta = Math.abs(Duration.between(mt.date, metaData0.date).toMillis());
			if (delta <3000l) {
				if (!mt.equals(metaData0)) {
					listCloseByTime.add(mt);
				}
			}
		}
		HashSet<MetaData> listCloser = new HashSet<MetaData>();
		listCloser.addAll(listCloseByGps);
		listCloser.addAll(listCloseByTime);
		this.listClose.addAll(listCloser);
		System.out.println("listClose :  " + String.format("%2d",listClose.size()) + " | listCloseByGps:" +String.format("%2d", listCloseByGps.size() )+ "| rView2 :" +String.format("%05.1f", rView2)+"   n: "+n+"   listCloseByTime :"+listCloseByTime.size()+"  | listCloser:  "+listCloser.size());
	}

	public HashSet<MetaData> searchCloseView2(int i, List<MetaData> list2, double rView2) {
		HashSet<MetaData> listClose2 = new HashSet<MetaData>();
		int j = 0;
		while (j < list2.size()) {

			MetaData m2 = list2.get(j);
			if (m2.equals(this)) {

			} else if (isCloseFrom(m2, rView2)) {
				listClose2.add(m2);
			}
			j++;
		}

		return listClose2;
	}

	public List<MetaData> getListClose() {
		return listClose;
	}

	private boolean isCloseFrom(MetaData m2, double rView2) {
		double dx = Math.abs(m2.xCorrected - xCorrected);
		double dy = Math.abs(m2.yCorrected - yCorrected);
		return (dx < rView2) && (dy < rView2);
	}

}
