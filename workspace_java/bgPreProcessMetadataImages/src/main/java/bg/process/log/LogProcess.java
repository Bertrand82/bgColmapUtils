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

	private File dir;
	private File dirSparse;
	private File fileSparseLog;
	private File dirImages;
	private int nbImages = 0;
	private String grep = "";
	
	static class Etape {
		

		String name ;
		OffsetDateTime date = null;
		Duration duration;
		
		public Etape(String name_,OffsetDateTime date_) {
			this.name = name_;
			this.date = date_;
		}

		public void setDuration(Etape etape) {
			if (etape== null) {
				return;
			}
			if (date== null) {
				return;
			}
		    if (etape.date == null) {
		    	return;
		    }
		    duration = Duration.between(date.toInstant(), etape.date.toInstant());
			
		}
		
		public String toString() {
			String duration_str = (duration==null)? " - ":""+duration.toSeconds();
			String s = "etape="+UtilString.toString(name,30);
			s += " | duree =";
			s +=  UtilString.toString(duration_str,10);
			s+=" secondes";
			return s;
		}
	}
	
	Etape etapeCurrent = null;
	List<Etape> listEtape = new ArrayList<LogProcess.Etape>();

	public LogProcess(File dirSources_) {
		this.dir = dirSources_;
		dirImages = new File(this.dir, "images");
		nbImages = (dirImages.listFiles() == null) ? -1 : dirImages.listFiles().length;
		init();
	}

	private void init() {
		fileSparseLog = getFileLogSparse();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileSparseLog));
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
			}
			initListEtapes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initListEtapes() {
		Etape etape_Z_1=null;
		for(Etape etape: listEtape) {
			if (etape_Z_1!=null) {
			etape_Z_1.setDuration(etape);
			}
			etape_Z_1=etape;
		}
		
	}

	private void initLineProcessBg(String line) {
		String etape = null;
		OffsetDateTime date = null;

		String[] items = line.split(" ");
		for (String tok : items) {
			if (tok.startsWith("etape=")) {
				etape = tok.substring("etape=".length());
			} else if (tok.startsWith("date=")) {
				String sDate = tok.substring("date=".length());
				date = OffsetDateTime.parse(sDate);
			}
		}
		if (etape != null) {
			this.etapeCurrent= new Etape(etape,date );
			this.listEtape.add(etapeCurrent);
		}
		

	}

	private File getFileLogSparse() {
		dirSparse = new File(dir, "sparse");
		File dirSparseLogs = new File(dirSparse, "logs");
		return UtilFile.mostRecentFile(dirSparseLogs);
	}

	public String toString() {
		String s = "Rapport " + dir.getAbsolutePath() + "\n";
		s += " Nb Images " + nbImages + "\n";
		s += " fileSparseLog " + UtilFile.toString(fileSparseLog) + "\n";
		s += grep+" \n";
		s += toStringListEtapes();
		return s;
	}

	private String toStringListEtapes() {
		String s="";
		for(Etape etape : listEtape) {
			s += ""+etape+"\n";
		}
		return s;
	}

}
