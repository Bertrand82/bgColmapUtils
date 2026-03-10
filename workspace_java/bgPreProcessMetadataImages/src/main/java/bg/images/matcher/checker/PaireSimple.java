package bg.images.matcher.checker;

import java.util.Objects;

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
	public boolean searchMatch(String search) {
		if (search == null) {
			return false;
		}
		if (this.imag1.contains(search.trim())) {
			return true;
		}
		if (this.imag2.contains(search.trim())) {
			return true;
		}
		return false;
	}
	
	
	
	@Override
	public int hashCode() {
		return Objects.hash(imag1, imag2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaireSimple other = (PaireSimple) obj;
		return Objects.equals(imag1, other.imag1) && Objects.equals(imag2, other.imag2);
	}
	
	@Override
	public String toString() {
		return "PaireSimple [imag1=" + imag1 + ", imag2=" + imag2 + "]";
	}
	

}
