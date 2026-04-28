package bg.display.together.gui;

import java.io.File;
import java.util.List;

import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;

public class ProcessDivideSparse {
	
	File dirRoot;
	File dirImages ;
	int nbTotalImages =0;
	private List<PositionGps2> listPositions;
	double xMin, xMax;
	double yMin, yMax;

	public ProcessDivideSparse(File dirRoot, int paquetSize) {
		this.dirRoot=dirRoot;
		this.dirImages= new File(dirRoot,"images");
		this.nbTotalImages=dirImages.listFiles().length;
		this.listPositions = PositionGps2Factory.getListGpsPositionFromDirImages(dirImages);
		initMinMAx();
		int nbPaquet = nbTotalImages/paquetSize+1;
		double dY = yMax-yMin;
		double dX =xMax-xMin;
		int nX;
		int nY;
		nX = (int)(1+Math.sqrt( (dX/dY)*nbPaquet));
		nY = (int)(1+Math.sqrt((dY/dX)/nbPaquet));
		System.out.println("dx "+dX);
		System.out.println("dY "+dY);
		System.out.println("Nb de paquets :"+nbPaquet);
		System.out.println("nx "+nX);
		System.out.println("nY "+nY);
	}
	

	private void initMinMAx() {
		xMin=listPositions.getFirst().getX();
		xMax = xMin;
		yMax = listPositions.getFirst().getY();
		yMin = yMax;
		for (PositionGps2 pos : listPositions) {
			if(pos.getX()>xMax) xMax=pos.getX();
			if(pos.getX()<xMin) xMin=pos.getX();
			if(pos.getY()<yMin) yMin=pos.getY();
			if(pos.getY()>yMax) yMax=pos.getY();
		}
		
	}

}
