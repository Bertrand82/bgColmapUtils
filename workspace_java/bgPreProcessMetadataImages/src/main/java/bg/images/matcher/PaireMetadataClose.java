package bg.images.matcher;

import java.util.Objects;

import bg.MetaData;

public class PaireMetadataClose implements Comparable<PaireMetadataClose>{

	MetaData metaData1;
	MetaData metaData2;
	
	String fileName1;
	String fileName2;
	
	public PaireMetadataClose(MetaData metaDataA, MetaData metaDataB) throws Exception {
		int r1 = metaDataA.fileName.compareTo(metaDataB.fileName);  
		if (r1 > 0) {
			metaData1 = metaDataA;
			metaData2 = metaDataB;
		}else if (r1<0){
			metaData1 = metaDataB;
			metaData2 = metaDataA;
		}else {
			throw new Exception("Meme paire interdite");
		}
		fileName1=metaData1.fileName;
		fileName2=metaData2.fileName;
	
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
		PaireMetadataClose other = (PaireMetadataClose) obj;
		return Objects.equals(fileName1, other.fileName1) && Objects.equals(fileName2, other.fileName2);
	}

	@Override
	public int compareTo(PaireMetadataClose o) {
		if (this.fileName1.equals(o.fileName1)) {
			return this.fileName2.compareTo(o.fileName2);
		}
		return this.fileName1.compareTo(o.fileName1);
	}

	
	
	

}
