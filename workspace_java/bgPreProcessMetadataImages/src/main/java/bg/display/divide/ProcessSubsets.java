package bg.display.divide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;
import bg.util.UtilCopyBg;
import bg.util.UtilPositionGps2;
import bg.util.UtilPositionGps2.MinMaxBounds;

public class ProcessSubsets {

	
	File dirRoot;
	File dirImages;
	int nbTotalImages = 0;
	private List<PositionGps2> listPositions;
	List<Paquet> listPaquets = new ArrayList<Paquet>();
	public ProcessSubsets(File dirRoot, int paquetSize) {
		this(dirRoot,paquetSize,PositionGps2Factory.getListGpsPositionFromDirImages(new File(dirRoot, "images")));
	}

	public ProcessSubsets(File dirRoot, int paquetSize, List<PositionGps2> listPositions_) {
		this.dirRoot = dirRoot;
		this.dirImages = new File(dirRoot, "images");
		this.nbTotalImages = dirImages.listFiles().length;
		this.listPositions =listPositions_;
		
		List<List<PositionGps2>> listList =UtilPositionGps2.extractPaquets(listPositions,paquetSize);
		this.listPaquets=toListPaquet(listList);
	
		System.out.println(" Total position  size "+listPositions.size());
		int i =1;
		for (Paquet paquet : listPaquets) {
			System.out.println(i+++" paquet ---> "+paquet);
		}
		createDirectories();
		try {
			UtilCopyBg.copyResourceToDir("sh/processChapeau.sh", this.dirRoot.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/processMergePLY.sh", this.dirRoot.toPath(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private List<Paquet> toListPaquet(List<List<PositionGps2>> listList) {
		List<Paquet> listP = new ArrayList<Paquet>();
		int numero=0;
		for(List<PositionGps2> list: listList) {
			Paquet paquet = new Paquet(list,this.dirImages,numero++);
			listP.add(paquet);
			paquet.updateReferenceNumeroPaquet();
		}
		return listP;
	}
	

	private void createDirectories() {
		try {
			System.out.println("create directories");
			File dirSparse = new File(dirRoot,"sparse");
			File dirSparse0 = new File(dirSparse,"0");
			ColmapSubsetBuilder colmapSubsetBuilder= new ColmapSubsetBuilder(dirSparse0.toPath());
			for(Paquet paquet : listPaquets) {
				paquet.createDirectorie_(this.dirRoot,colmapSubsetBuilder);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	

}
