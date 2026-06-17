package bg.metadata;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;

public class MetaData {

	private static final Pattern SEQ = Pattern.compile("^DJI_\\d{14}_(\\d+)_.*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern TS14 = Pattern.compile("(\\d{14})");
	private static final DateTimeFormatter TS14_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
	public PositionGps2 gpsPosition;
	public double xCorrected;
	public double yCorrected;
	public double rView;

	List<MetaData> listClose = new ArrayList<MetaData>();

	// DJI_20260207175436_0671_D.JPG,-207.0166253643368,-404.054606722876,-1.690999999999999,-71.8,-60.0,0.0
	public MetaData(String line) {
		this.line = line;
		parseLine(line);
		this.numeroSequence = extractSequenceNumberFromFileName(fileName);
		this.date = extractDateTimeFromFilename(fileName);
	}

	public MetaData(PositionGps2 pGps2) {
		x =pGps2.getX();
		y =pGps2.getY();
		fileName=pGps2.getImageName();
		z = pGps2.getAltitudeMeters();
		yaw = pGps2.getYaw();
		pitch = pGps2.getPitch();
		roll =pGps2.getRoll();
	}

	private void parseLine(String line) {
		String[] tokens = line.split(",");
		int i = 0;
		fileName = tokens[i++];
		x = Double.parseDouble(tokens[i++]);
		y = Double.parseDouble(tokens[i++]);
		z = Double.parseDouble(tokens[i++]);
		yaw = Double.parseDouble(tokens[i++]);
		pitch = Double.parseDouble(tokens[i++]);
		roll = Double.parseDouble(tokens[i++]);
	}

	@Override
	public String toString() {
		return " | fileName=" + fileName
				+ "| x=" + String.format("%06.1f", x)
				+ "| y=" + String.format("%06.1f", y)
				+ "| z=" + String.format("%06.1f", z)
				+ "| yaw=" + String.format("%06.1f", yaw)
				+ "| pitch=" + String.format("%06.1f", pitch)
				+ "| roll=" + String.format("%06.1f", roll)
				+ "| numeroSequence=" + String.format("%04d", numeroSequence)
				+ "| date=" + date + "]";
	}

	private static int extractSequenceNumberFromFileName(String filename) {
		Matcher m = SEQ.matcher(filename);
		if (!m.matches()) {
			return -1;
		}
		return Integer.parseInt(m.group(1));
	}

	private static LocalDateTime extractDateTimeFromFilename(String filename) {
		Matcher m = TS14.matcher(filename);
		if (!m.find()) {
			return null;
		}
		String ts = m.group(1);
		return LocalDateTime.parse(ts, TS14_FORMAT);
	}

	public File getFileImageIn(File dir) {
		return new File(dir, this.fileName);
	}

	public int updateGpsPosition(File fileImage) throws Exception {
		this.gpsPosition = PositionGps2Factory.extractPosition(fileImage);

		if (this.gpsPosition == null) {
			System.out.println("updateGpsPosition " + fileImage.getName() + "  gps From Image ::  " + this.gpsPosition);
			return 0;
		}
		return 1;
	}

	public void correctGpsPosition2() {
		applyCorrectedPosition(this.gpsPosition);
	}

	public void applyCorrectedPosition(PositionGps2 positionGps) {
		MetaDataPositionCorrector.CorrectionResult result =
				MetaDataPositionCorrector.compute(
						this.fileName,
						this.x,
						this.y,
						this.z,
						this.yaw,
						this.pitch,
						positionGps);

		this.xCorrected = result.getxCorrected();
		this.yCorrected = result.getyCorrected();
		this.rView = result.getrView();
	}

	/**
	 * Cherche les images proches selon la zone vue et la proximité temporelle.
	 * On cherche en pratique entre environ 4 et 8 voisines pour limiter les coûts
	 * de traitement tout en conservant une qualité suffisante.
	 */
	public void searchCloseView(int i, List<MetaData> list2) {
		MetaData metaData0 = list2.get(i);
		HashSet<MetaData> listClose1 = searchCloseView2(i, list2, this.rView);
		HashSet<MetaData> listCloseByGps = new HashSet<MetaData>();
		int n = 0;
		double rView2 = this.rView;

		while (((listCloseByGps.size() >= 8) || (listCloseByGps.size() <= 2)) && (n < 4)) {
			if (listClose1.size() >= 8) {
				rView2 = rView2 * 0.8;
				listCloseByGps = searchCloseView2(i, list2, rView2);
			} else if (listClose1.size() <= 2) {
				rView2 = rView2 * 1.2;
				listCloseByGps = searchCloseView2(i, list2, rView2);
			} else {
				listCloseByGps = listClose1;
			}
			n++;
		}

		HashSet<MetaData> listCloseByTime = new HashSet<MetaData>();
		for (MetaData mt : list2) {
			long delta = Math.abs(Duration.between(mt.date, metaData0.date).toMillis());
			if (delta < 3000L && !mt.equals(metaData0)) {
				listCloseByTime.add(mt);
			}
		}

		HashSet<MetaData> listCloser = new HashSet<MetaData>();
		listCloser.addAll(listCloseByGps);
		listCloser.addAll(listCloseByTime);
		this.listClose.addAll(listCloser);

		System.out.println(
				"listClose :  " + String.format("%2d", listClose.size())
				+ " | listCloseByGps:" + String.format("%2d", listCloseByGps.size())
				+ "| rView2 :" + String.format("%05.1f", rView2)
				+ "   n: " + n
				+ "   listCloseByTime :" + listCloseByTime.size()
				+ "  | listCloser:  " + listCloser.size());
	}

	public HashSet<MetaData> searchCloseView2(int i, List<MetaData> list2, double rView2) {
		HashSet<MetaData> listClose2 = new HashSet<MetaData>();
		int j = 0;
		while (j < list2.size()) {
			MetaData m2 = list2.get(j);
			if (!m2.equals(this) && isCloseFrom(m2, rView2)) {
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