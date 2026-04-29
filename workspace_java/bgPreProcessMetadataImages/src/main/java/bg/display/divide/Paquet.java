package bg.display.divide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bg.util.PositionGps2;

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
		System.out.println(" xOk "+xOK+"  yOk "+yOK);
		return xOK && yOK;
	}
	public String toString() {
		return "dx "+dx+" dy "+dy+"   size "+listPositions.size();
	}
	public void createDirectorie(File dir) {
		File dirRoot = new File(dir,"paquet_"+numero);
		dirRoot.mkdirs();
		
	}
}
