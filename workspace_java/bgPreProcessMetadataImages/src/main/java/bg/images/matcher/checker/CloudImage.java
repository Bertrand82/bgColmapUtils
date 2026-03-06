package bg.images.matcher.checker;

import java.util.ArrayList;
import java.util.List;

public class CloudImage {

	String image0;
	List<String> listContact = new ArrayList<String>();
	public CloudImage(String image) {
		this.image0 =image;
	}
	public void add(String imag2) {
		this.listContact.add(imag2);
		
	}
	
	  public String toString() { 
		  String s ="";
		  s += ""+image0+"  "+listContact.size();
		  return s;
	  }

}
