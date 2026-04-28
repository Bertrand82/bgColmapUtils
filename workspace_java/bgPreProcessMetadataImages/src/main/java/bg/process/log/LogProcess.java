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
	

	
	LogEtape etapeCurrent = null;
	List<LogEtape> listEtape = new ArrayList<LogEtape>();

	public LogProcess(File dirSources_) {
		this.dir = dirSources_;
		dirImages = new File(this.dir, "images");
		nbImages = (dirImages.listFiles() == null) ? -1 : dirImages.listFiles().length;
		init();
	}
	String lastLine ;
	String lastLine_Z_1 ;
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
				if (isEtape("patch_match_stereo")){
					LogEtapePatch_match_stereo etpms = (LogEtapePatch_match_stereo)etapeCurrent;
					etpms.processLinePatchMatchStereo(line);
				}
				lastLine_Z_1=lastLine;
				lastLine=line;
			}
			
			
			initListEtapes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initListEtapes() {
		LogEtape etape_Z_1=null;
		for(LogEtape etape: listEtape) {
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
			this.etapeCurrent= createEtape(etape,date );
			this.listEtape.add(etapeCurrent);
		}
		
		

	}

	private LogEtape createEtape(String etape, OffsetDateTime date) {
		if (etape.equals("patch_match_stereo")) {
			return new LogEtapePatch_match_stereo(etape,date );
		}else {
			return new LogEtape(etape,date );
		}
		
	}


	private boolean isEtape(String label) {
		if (etapeCurrent==null) {
			return false;
		}
		return etapeCurrent.name.equals(label);
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
		String s="Last line Z_1:"+this.lastLine_Z_1+"\n";
		 s+="Last line    :"+this.lastLine+"\n";
		for(LogEtape etape : listEtape) {
			s += ""+etape+"\n";
		}
		return s;
	}

}
