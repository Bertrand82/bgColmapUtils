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
	double xMin, xMax;
	double yMin, yMax;
	double dx,dy;
	int numero;

	public Paquet(double xxMin, double xxMax, double yyMin, double yyMax,int numero) {
		xMin = xxMin;
		xMax = xxMax;
		yMin = yyMin;
		yMax = yyMax;
		dx = xMax-xMin;
		dy = yMax-yMin;
		this.numero=numero;
	}

	List<PositionGps2> listPositions = new ArrayList<PositionGps2>();
	
	public boolean containsPosition(PositionGps2 position) {
		boolean xOK = (position.getX() >= xMin) && (position.getX() <= xMax);
		boolean yOK = (position.getY() >= yMin) && (position.getY() <= yMax);
		return xOK && yOK;
	}
	public String toString() {
		return "dx "+dx+" dy "+dy+"   size "+listPositions.size();
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
