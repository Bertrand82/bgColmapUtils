package bg.display.divide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bg.util.PositionGps2;
import bg.util.UtilCopyBg;

public class Paquet {

	File dirRoot;
	File dirImages;

	int numero;



	public Paquet(List<PositionGps2> list, File dirRoot, File dirImages, int numero) {
		this.listPositions=list;
		this.numero=numero;
		this.dirRoot=dirRoot;
		this.dirImages= dirImages;
		
	}

	private List<PositionGps2> listPositions = new ArrayList<PositionGps2>();
	
	
	public String toString() {
		return " Paquet  size "+listPositions.size();
	}
	public void createDirectorie(File dir) {
		try {
			File dirRoot = new File(dir,"paquet_"+numero);
			File dirSparse = new File(dir,"sparse");
			File dirSparse0 = new File(dirSparse,"0");
			Set<String> listImages = getSetImages();
			ColmapSubsetBuilder.buildSubsetTxt(dirSparse0.toPath(), dirRoot.toPath(), listImages);
			UtilCopyBg.copyResourceToDir("sh/processDensePaquet.sh", dirRoot.toPath(),true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private Set<String> getSetImages() {
		Set<String> setImages = new HashSet<String>();
		for (PositionGps2 poGps : listPositions) {
			setImages.add(poGps.getImageName());
		}
		return setImages;
	}
}
