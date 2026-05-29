package bg.process.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import bg.util.UtilFile;
import bg.util.UtilString;

public class LogProcess {

	int nbImages;

	private File dirLog;

	private File fileLog;

	private String grep = "";

	LogTache tacheCurrent = null;
	List<LogTache> listTaches = new ArrayList<LogTache>();
	List<LogData> listData = new ArrayList<LogData>();
	public static File dirStatsRuntime = new File("stats-runtime");

	public LogProcess(File fileLog) {
		if (fileLog.isDirectory()) {
			this.dirLog = fileLog;
			this.fileLog = getFileLogFromDirectory(this.dirLog);
		} else {
			this.dirLog = fileLog.getParentFile();
			this.fileLog = fileLog;
		}
		init();
		this.storeResult(dirStatsRuntime);
	}


	private File getFileLogFromDirectory(File dirLog2) {
		File[] files = dirLog2.listFiles();
	    if (files == null) {
	        return null;
	    }

	    File oldestFile = null;

	    for (File file : files) {
	        if (file.isFile() && file.getName().endsWith(".log")) {
	            if (oldestFile == null || file.lastModified() < oldestFile.lastModified()) {
	                oldestFile = file;
	            }
	        }
	    }

	    return oldestFile;
	}

	String lastLine;
	String lastLine_Z_1;
	LogTache tacheStereoFusion;
	LogTache tachePatchMatchStereo;

	private void init() {

		try {
			System.out.println("file Log " + fileLog.getName());
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
			this.nbImages = getNbImages();
			this.tachePatchMatchStereo = getTacheByName("patch_match_stereo");
			this.tacheStereoFusion = getTacheByName("stereo_fusion");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private LogTache getTacheByName(String name) {
		for (LogTache logT : listTaches) {
			if (logT.name.equals(name)) {
				return logT;
			}
		}
		System.err.println("------------- No tache : "+name +"  in : "+listTaches.size()+" taches ");
		return null;
	}

	private int getNbImages() {
		LogData dataNbImages = getDataByName("nb_images");
		if (dataNbImages==null) {
			System.err.println("no dataImages!!!!!!!!! ");
			return -1;
		}else {
				return dataNbImages.valueAsInt();
		}
		
	}
	
	private LogData getDataByName(String name) {
		for (LogData data : this.listData) {
			if (data.name.equalsIgnoreCase("nb_images")) {
				return data;
			}
		}
		return null;
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
	OffsetDateTime dateFirst = null;
	OffsetDateTime dateLast = null;

	private void initLineProcessBg(String line) {
		String tache = null;
		OffsetDateTime date = null;
		String step = "";

		String[] items = line.split(" ");
		for (String tok : items) {
			int i = tok.indexOf("=");
			if ((tok.startsWith("tache=")) || (tok.startsWith("task="))) {

				tache = tok.substring(i + 1, tok.length());
			} else if (tok.startsWith("date=")) {
				String sDate = tok.substring("date=".length());
				date = OffsetDateTime.parse(sDate);
				if (dateFirst == null) {
					dateFirst=date;
				}
				dateLast=date;
			} else if (tok.startsWith("step=")) {
				step = tok.substring("step=".length());

			} else if (tok.startsWith("bg=")) {
			} else {

				if (i > 0) {
					String variableName = tok.substring(0, i);
					String variableValue = tok.substring(i+1, tok.length());
					LogData logData = new LogData(variableName, variableValue, tok);
					if (logData.isPertinent()) {
						this.listData.add(logData);
					}
				}
			}
		}
		if (tache != null && (!step.equals("done"))) {
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
		s += toStringLogData();
		s += toStringListEtapes();
		return s;
	}

	public String toStringVerbose() {
		String s = "Rapport " + fileLog.getAbsolutePath() + "\n";
		s+= "dateFirst "+dateFirst+"\n";
		s+= "dateLast "+dateLast+"\n";
		s += " fileSparseLog " + UtilFile.toString(fileLog) + "\n";
		s += grep + " \n";
		s += "Nb etapes : " + listTaches.size() + "\n";
		s += "Nb data   : " + listData.size() + "\n";
		s += toStringListEtapes();
		s += " Nombre Images :" + this.nbImages +"\n";
		if (nbImages != 0) {
			if (this.tachePatchMatchStereo != null) {
			s += " Duree patch_match_stereo par image :"
					+ (this.tachePatchMatchStereo.duree_en_seconde() / this.nbImages) +"\n";
			}
			if (this.tacheStereoFusion != null) {
			s += " Duree stereo_fusion   par image :" + (this.tacheStereoFusion.duree_en_seconde() / this.nbImages)+"\n";
			}
		}
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
	

	private void storeResult(File dirStatsRuntime2) {
		try {
			String sTexte = this.toStringVerbose();
			String fileName = "stat_"+this.dateFirst+"__"+this.dateLast+".text";
			
			File stat = new File(dirStatsRuntime2,fileName);
			Files.writeString(stat.toPath(), sTexte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
