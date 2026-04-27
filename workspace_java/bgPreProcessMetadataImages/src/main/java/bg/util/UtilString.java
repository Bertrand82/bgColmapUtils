package bg.util;

public class UtilString {

	public static String toString(String name, int i) {
		String s = ""+name;
		while(s.length() < i) {
			s+=" ";
		}
		return s;
	}

}
