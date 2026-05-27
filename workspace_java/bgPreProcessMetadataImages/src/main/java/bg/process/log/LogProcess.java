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

	LogTache tacheCurrent = null;
	List<LogTache> listTaches = new ArrayList<LogTache>();
	List<LogData> listData = new ArrayList<LogData>();

	public LogProcess(File fileLog) {
		if (fileLog.isDirectory()) {
			this.dirLog=fileLog;
			this.fileLog = getFileLogFromDirectory(this.dirLog);
		}else {
			this.dirLog = fileLog.getParentFile();
			this.fileLog = fileLog;
		}
		init();
	}

	private File getFileLogFromDirectory(File dirLog2) {
		for(File file :dirLog2.listFiles()) {
			if (file.getName().endsWith(".log")) {
				return file;
			}
		}
		return null;
	}

	String lastLine;
	String lastLine_Z_1;

	private void init() {

		try {
			System.out.println("file Log "+fileLog.getName());
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
					LogTachePatch_match_stereo etpms = (LogTachePatch_match_stereo) tacheCurrent;
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
		LogTache etape_Z_1 = null;
		for (LogTache etape : listTaches) {
			if (etape_Z_1 != null) {
				etape_Z_1.setDuration(etape);
			}
			etape_Z_1 = etape;
		}

	}

	private void initLineProcessBg(String line) {
		String tache = null;
		OffsetDateTime date = null;

		String[] items = line.split(" ");
		for (String tok : items) {
			int i = tok.indexOf("=");
			if ((tok.startsWith("tache=")) || (tok.startsWith("task="))){
				
				tache = tok.substring(i+1,tok.length());
			} else if (tok.startsWith("date=")) {
				String sDate = tok.substring("date=".length());
				date = OffsetDateTime.parse(sDate);
			} else if (tok.startsWith("bg=")){
			} else {
				
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
		if (tache != null) {
			this.tacheCurrent = createTache(tache, date);
			this.listTaches.add(tacheCurrent);
		}

	}

	private LogTache createTache(String etape, OffsetDateTime date) {
		if (etape.equals("patch_match_stereo")) {
			return new LogTachePatch_match_stereo(etape, date);
		} else {
			return new LogTache(etape, date);
		}

	}

	private boolean isEtape(String label) {
		if (tacheCurrent == null) {
			return false;
		}
		return tacheCurrent.name.equals(label);
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
		s += "Nb etapes : "+listTaches.size()+"\n";
		s += "Nb data   : " +listData.size()+"\n";
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
		for (LogTache etape : listTaches) {
			s += " -- " + etape + "\n";
		}
		return s;
	}

}
