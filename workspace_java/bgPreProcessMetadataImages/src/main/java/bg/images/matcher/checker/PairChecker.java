package bg.images.matcher.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PairChecker {

	public List<PaireSimple> list = new ArrayList<PaireSimple>();
	private Map<String,CloudImage> listCloud = new HashMap<String,CloudImage>();
	Map<Integer,Compteur> hMapResultByNumVoisin = new HashMap<Integer, Compteur>();
	
	public PairChecker(File filePairs) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(filePairs));
		String line = null;
		while((line=br.readLine()) !=null) {
			PaireSimple pair = new PaireSimple(line);
			list.add(pair);
		}
		for(PaireSimple pair :list){
			String image1 = pair.imag1;
			String image2 =pair.imag2;
			process(image1,image2)	;		
			process(image2,image1)	;		
		}
		System.out.println(" nb PAires :"+list.size());
		
		
		int i=0;

		for(CloudImage cloud : this.listCloud.values()) {
			Integer nbVoisin = cloud.listContact.size();
			Compteur compteur = getCompteur(nbVoisin);
			compteur.i=compteur.i+1;
			System.out.println(cloud.toString());
			i++;
		}
		System.out.println("Nb images "+i);
		for (Integer ii:hMapResultByNumVoisin.keySet()) {
			System.out.println(" "+String.format("%2d", ii)+"   -----nb voisins --> "+hMapResultByNumVoisin.get(ii).i);
		}
		
	}
	private Compteur getCompteur(Integer nbVoisin) {
		Compteur compteur = this.hMapResultByNumVoisin.get(nbVoisin);
		if(compteur == null) {
			compteur= new Compteur();
			this.hMapResultByNumVoisin.put(nbVoisin, compteur);
		}
		return compteur;
	}
	static class Compteur{
		int i=0;
	}
	private void process(String image1, String image2) {
		
		CloudImage cloud1 = getCloudImage(image1)	;	
		cloud1.add(image2);
		
	}
	private CloudImage getCloudImage(String image1) {
		CloudImage cloud = this.listCloud.get(image1);
		if (cloud == null) {
			cloud = new CloudImage(image1) ;
			this.listCloud.put(image1, cloud);
		}
		return cloud;
	}
	

}
