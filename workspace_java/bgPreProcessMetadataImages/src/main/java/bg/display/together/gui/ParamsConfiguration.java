package bg.display.together.gui;

import bg.util.PropertiesGlobal;

public  class ParamsConfiguration {
	
	
	public ParamsConfiguration() {
		nbPointsExtraitsMax = PropertiesGlobal.getPropertyAsInt("nbPointsExtraitsMax",100);
		taillePaquet=PropertiesGlobal.getPropertyAsInt("taillePaquet",30);
	}
	public int nbPointsExtraitsMax = 100;
	public int nbSeq = 7;
	public int nbProx = 8;
	public int taillePaquet = 30;
	public Double recouvrementPaquets = 0.2;
	
	public void save() {
		PropertiesGlobal.saveProperty("nbPointsExtraitsMax",""+nbPointsExtraitsMax);
		PropertiesGlobal.saveProperty("taillePaquet",""+taillePaquet);
	}
}
