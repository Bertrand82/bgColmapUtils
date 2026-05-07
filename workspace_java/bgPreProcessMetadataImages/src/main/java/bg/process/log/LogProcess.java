package bg.process.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import bg.util.UtilFile;
import bg.util.UtilString;

public class LogProcess {

	private File dirLog;

	private File fileLog;

	private String grep = "";

	LogEtape etapeCurrent = null;
	List<LogEtape> listEtape = new ArrayList<LogEtape>();
	List<LogData> listData = new ArrayList<LogData>();

	public LogProcess(File fileLog) {
		this.dirLog = fileLog.getParentFile();
		this.fileLog = fileLog;
		init();
	}

	String lastLine;
	String lastLine_Z_1;

	private void init() {

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileLog));

			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("bg")) {
					this.grep += line + "\n";
					if (line.startsWith("bg=")) {
						initLineProcessBg(line);
					}
				} else if (line.indexOf("error") >= 0) {
					this.grep += line + "\n";
				}
				if (isEtape("patch_match_stereo")) {
					LogEtapePatch_match_stereo etpms = (LogEtapePatch_match_stereo) etapeCurrent;
					etpms.processLinePatchMatchStereo(line);
				}
				lastLine_Z_1 = lastLine;
				lastLine = line;
			}

			initListEtapes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initListEtapes() {
		LogEtape etape_Z_1 = null;
		for (LogEtape etape : listEtape) {
			if (etape_Z_1 != null) {
				etape_Z_1.setDuration(etape);
			}
			etape_Z_1 = etape;
		}

	}

	private void initLineProcessBg(String line) {
		String etape = null;
		OffsetDateTime date = null;

		String[] items = line.split(" ");
		for (String tok : items) {
			if ((tok.startsWith("etape=")) || (tok.startsWith("step="))){
				etape = tok.substring("etape=".length());
			} else if (tok.startsWith("date=")) {
				String sDate = tok.substring("date=".length());
				date = OffsetDateTime.parse(sDate);
			} else if (tok.startsWith("bg=")){
			} else {
				int i = tok.indexOf("=");
				if (i > 0) {
					String variableName = tok.substring(0, i);
					String variableValue = tok.substring(i , tok.length());
					LogData logData = new LogData(variableName, variableValue,tok);
					if (logData.isPertinent()) {
					this.listData.add(logData);
					}
				}
			}
		}
		if (etape != null) {
			this.etapeCurrent = createEtape(etape, date);
			this.listEtape.add(etapeCurrent);
		}

	}

	private LogEtape createEtape(String etape, OffsetDateTime date) {
		if (etape.equals("patch_match_stereo")) {
			return new LogEtapePatch_match_stereo(etape, date);
		} else {
			return new LogEtape(etape, date);
		}

	}

	private boolean isEtape(String label) {
		if (etapeCurrent == null) {
			return false;
		}
		return etapeCurrent.name.equals(label);
	}

	public String toString() {
		String s = "Rapport " + fileLog.getAbsolutePath() + "\n";
		s += " fileSparseLog " + UtilFile.toString(fileLog) + "\n";
		s+= toStringLogData();
		s += toStringListEtapes();
		return s;
	}

	public String toStringVerbose() {
		String s = "Rapport " + fileLog.getAbsolutePath() + "\n";
		s += " fileSparseLog " + UtilFile.toString(fileLog) + "\n";
		s += grep + " \n";
		s += toStringListEtapes();
		s += "Last line Z_1:" + this.lastLine_Z_1 + "\n";
		s += "Last line    :" + this.lastLine + "\n";
		return s;
	}
	
	private String toStringLogData() {
		String s = "";
		for (LogData datas : listData) {
			s += "" + datas + "\n";
		}
		return s;
	}

	private String toStringListEtapes() {
		String s = "";
		for (LogEtape etape : listEtape) {
			s += "" + etape + "\n";
		}
		return s;
	}

}
