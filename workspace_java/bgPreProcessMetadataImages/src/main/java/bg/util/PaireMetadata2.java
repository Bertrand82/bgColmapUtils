package bg.util;

import java.util.Objects;

public class PaireMetadata2 implements Comparable<PaireMetadata2>{

	private final  PositionMetaData2 metaData1;
	private final PositionMetaData2 metaData2;
	
	private final String fileName1;
	private final String fileName2;
	
	public PaireMetadata2(PositionMetaData2 metaDataA, PositionMetaData2 metaDataB) {
		int r1 = metaDataA.getImageName().compareTo(metaDataB.getImageName());  
		if (r1 > 0) {
			metaData1 = metaDataA;
			metaData2 = metaDataB;
		}else if (r1<0){
			metaData1 = metaDataB;
			metaData2 = metaDataA;
		}else {
			throw new RuntimeException("Meme paire interdite");
		}
		fileName1=metaData1.getImageName();
		fileName2=metaData2.getImageName();
	
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileName1, fileName2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaireMetadata2 other = (PaireMetadata2) obj;
		return Objects.equals(fileName1, other.fileName1) && Objects.equals(fileName2, other.fileName2);
	}

	@Override
	public int compareTo(PaireMetadata2 o) {
		if (this.fileName1.equals(o.fileName1)) {
			return this.fileName2.compareTo(o.fileName2);
		}
		return this.fileName1.compareTo(o.fileName1);
	}

	public String getFileName1() {
		return fileName1;
	}

	public String getFileName2() {
		return fileName2;
	}

	
	
	

}
