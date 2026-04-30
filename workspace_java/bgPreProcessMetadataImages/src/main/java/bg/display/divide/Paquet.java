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

	
	File dirImages;
	String paquetName;
	int numero;



	public Paquet(List<PositionGps2> list, File dirImages, int numero) {
		this.listPositions=list;
		this.numero=numero;
		
		this.dirImages= dirImages;
		this.paquetName= "paquet_"+String.format("%03d", numero);
		
	}

	private List<PositionGps2> listPositions = new ArrayList<PositionGps2>();
	
	
	public String toString() {
		return " Paquet name :"+paquetName+" | size :"+listPositions.size()+" images";
	}
	public void createDirectorie_(File dir,ColmapSubsetBuilder colmapSubsetBuilder) {
		try {
			
			
			
				System.out.print("createDirectorie "+paquetName+" start ");
				File dirRootPaquet = new File(dir,paquetName);
				//dirSparse0.mkdirs();
				Set<String> listImages = getSetImages();
				System.out.print(" listImages "+listImages.size());
				colmapSubsetBuilder.processPaquet( dirRootPaquet.toPath(), listImages);
				File fileImages = new File(dirRootPaquet,"images.txt");
				System.out.print("  fileImages exists :"+fileImages.exists()+" size "+fileImages.length()+" copy sh to dir ");
				UtilCopyBg.copyResourceToDir("sh/processDensePaquet.sh", dirRootPaquet.toPath(),true);
				System.out.println("createDirectorie "+paquetName+" done");
			
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
