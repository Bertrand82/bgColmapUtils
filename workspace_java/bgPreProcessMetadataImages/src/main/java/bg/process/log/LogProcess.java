package bg.process.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import bg.util.UtilFile;

public class LogProcess {
	
	private File dir;
	private File dirSparse ;
	private File fileSparseLog ;
	private File dirImages;
	private int nbImages =0;
	private String grep="";

	public LogProcess(File dirSources_) {
		this.dir = dirSources_;
		dirImages= new File(this.dir, "images");
		nbImages=(dirImages.listFiles()==null)?-1: dirImages.listFiles().length;
		init();
	}


	private void init() {
		fileSparseLog  = getFileLogSparse();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileSparseLog));
			String line;
			while((line=br.readLine())!= null) {
				if (line.startsWith("bg")) {
					this.grep+=line+"\n";
				}else if (line.indexOf("error") >=0) {
					this.grep+=line +"\n";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private File getFileLogSparse() {
		dirSparse = new File(dir, "sparse");
		File dirSparseLogs = new File(dirSparse,"logs");
		return UtilFile.mostRecentFile(dirSparseLogs);
	}
	
	
	public String toString() {
		String s = "Rapport "+dir.getAbsolutePath()+"\n";
		s+=" Nb Images "+nbImages+"\n";
		s+=" fileSparseLog "+UtilFile.toString(fileSparseLog)+"\n";
		s+=grep;
		return s;
	}

}
