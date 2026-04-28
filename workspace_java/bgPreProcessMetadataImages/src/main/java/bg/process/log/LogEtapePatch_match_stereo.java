package bg.process.log;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEtapePatch_match_stereo extends LogEtape{
	private static final Pattern P = Pattern.compile(
		      "Processing\\s+view\\s+(\\d+)\\s*/\\s*(\\d+)\\s+for\\s+([^=\\s]+)"
		  );
    static class ProcessingView {
    	int numero;
    	int nombreTotal;
    	String imageName;
        //I20260424 22:27:54.235857  4135 patch_match.cc:416] === Processing view 7 / 60 for DJI_20260206155143_0125_D.JPG ===
		public ProcessingView(String line) {
			 Matcher m = P.matcher(line);
			    if (m.find()) {
			      numero = Integer.parseInt(m.group(1));
			      nombreTotal  = Integer.parseInt(m.group(2));
			      imageName = m.group(3);
			    }
		}
    	
    }
	LogEtapePatch_match_stereo(String name_,OffsetDateTime date_){
		super(name_,date_);
	}
	List<ProcessingView> listProcessingView = new ArrayList<LogEtapePatch_match_stereo.ProcessingView>();
	ProcessingView processingViewCurrent;

	public void processLinePatchMatchStereo(String line) {
		if (line == null) {
			return;
		}
		
		if (line.indexOf("Processing view") >0) {
			ProcessingView processingView = new ProcessingView(line);
			listProcessingView.add(processingView);
			processingViewCurrent = processingView;
		}
	}

	@Override
	public String toString() {
		String s= super.toString();
		s+=" listProcessingView.size : "+listProcessingView.size();
		return s;
	}
	
	
}
