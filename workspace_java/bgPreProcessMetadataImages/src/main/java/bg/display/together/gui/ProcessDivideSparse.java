package bg.display.together.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;

public class ProcessDivideSparse {

	static class Paquet {

		public Paquet(double xxMin, double xxMax, double yyMin, double yyMax) {
			xMin = xxMin;
			xMax = xxMax;
			yMin = yyMin;
			yMax = yyMax;
			dx = xMax-xMin;
			dy = yMax-yMin;
		}

		List<PositionGps2> listPositions = new ArrayList<PositionGps2>();
		
		double xMin, xMax;
		double yMin, yMax;
		double dx,dy;
		public boolean containsPosition(PositionGps2 position) {
			boolean xOK = (position.getX() >= xMin) && (position.getX() <= xMax);
			boolean yOK = (position.getY() >= yMin) && (position.getY() <= yMax);
			System.out.println(" xOk "+xOK+"  yOk "+yOK);
			return xOK && yOK;
		}
		public String toString() {
			return "dx "+dx+" dy "+dy+"   size "+listPositions.size();
		}
	}

	File dirRoot;
	File dirImages;
	int nbTotalImages = 0;
	private List<PositionGps2> listPositions;
	List<Paquet> listPaquets = new ArrayList<ProcessDivideSparse.Paquet>();
	double xMin, xMax;
	double yMin, yMax;

	public ProcessDivideSparse(File dirRoot, int paquetSize) {
		this.dirRoot = dirRoot;
		this.dirImages = new File(dirRoot, "images");
		this.nbTotalImages = dirImages.listFiles().length;
		this.listPositions = PositionGps2Factory.getListGpsPositionFromDirImages(dirImages);
		initMinMAx();
		int nbPaquet_0 = nbTotalImages / paquetSize + 1;
		double dY = yMax - yMin;
		double dX = xMax - xMin;
		int nX;
		int nY;
		nX = (int) (1 + Math.sqrt((dX / dY) * nbPaquet_0));
		nY = (int) (1 + Math.sqrt((dY / dX) / nbPaquet_0));
		System.out.println("dx " + dX);
		System.out.println("dY " + dY);
		System.out.println("Nb de paquets initial:" + nbPaquet_0);
		System.out.println("nx " + nX);
		System.out.println("nY " + nY);
		int nbPaquets = nX * nY;
		System.out.println("Nb de paquets :" + nbPaquets);
		for (int iX = 0; iX < nX; iX++) {
			for (int iY = 0; iY < nY; iY++) {
				double xxMin = xMin + iX * (dX / nX);
				double xxMax = xMin + (iX + 1) * (dX / nX);
				double yyMin = yMin + iY * (dY / nY);
				double yyMax = yMin + (iY + 1) * (dY / nY);
				Paquet paquet = new Paquet(xxMin, xxMax, yyMin, yyMax);
				this.listPaquets.add(paquet);
			}
		}
		System.out.println("List Paquets size :"+listPaquets.size());
		initListPaquets();
		System.out.println(" Total position  size "+listPositions.size());
		int i =1;
		for (Paquet paquet : listPaquets) {
			System.out.println(i+++" paquet size "+paquet);
		}
	}

	private void initListPaquets() {
		
		for(PositionGps2 position : listPositions) {
			for (Paquet paquet : listPaquets) {
				if (paquet.containsPosition(position)) {
					paquet.listPositions.add(position);
				}
			}
		}
	}

	private void initMinMAx() {
		xMin = listPositions.getFirst().getX();
		xMax = xMin;
		yMax = listPositions.getFirst().getY();
		yMin = yMax;
		for (PositionGps2 pos : listPositions) {
			if (pos.getX() > xMax)
				xMax = pos.getX();
			if (pos.getX() < xMin)
				xMin = pos.getX();
			if (pos.getY() < yMin)
				yMin = pos.getY();
			if (pos.getY() > yMax)
				yMax = pos.getY();
		}

	}

}
