package bg.images.matcher.checker;

public class PaireSimple {
	public String imag1;
	public String imag2;
	public int nbRelations1;
	public int nbRelations2;
	public PaireSimple(String line) {
		String[]  sArray = line.split(" ");
		imag1=sArray[0];
		imag2=sArray[1];
	}
	

}
