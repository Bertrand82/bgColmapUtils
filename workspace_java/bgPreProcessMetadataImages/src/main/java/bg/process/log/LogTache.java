package bg.process.log;

import java.time.Duration;
import java.time.OffsetDateTime;


import bg.util.UtilString;

public class LogTache {

	
	public String name ;
	OffsetDateTime date = null;
	Duration duration;
	
	
	public  LogTache(String name_,OffsetDateTime date_) {
		this.name = name_;
		this.date = date_;
	}

	public void setDuration(LogTache etape) {
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
		String duration_str = (duration==null)? " - ":""+String.format("%02d:%02d:%02d",
        duration.toHours(),
        duration.toMinutesPart(),
        duration.toSecondsPart());
		String s = "tache="+UtilString.toString(name,30);
		s += " | duree =";
		s +=  UtilString.toString(duration_str,10);
		s+=" ";
		return s;
	}
	
	public double duree_en_seconde() {
		return  (double) duration.getSeconds();
	}
}
