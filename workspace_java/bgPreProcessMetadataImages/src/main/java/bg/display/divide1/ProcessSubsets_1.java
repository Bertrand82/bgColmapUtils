package bg.display.divide1;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import bg.display.divide.common.Paquet;
import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;
import bg.util.UtilCopyBg;
import bg.util.UtilPositionGps2;
import bg.util.UtilPositionGps2.MinMaxBounds;

public class ProcessSubsets_1 {

	
	File dirRoot;
	File dirImages;
	int nbTotalImages = 0;
	int  paquetSize;
	double tauxRecouvrement;
	private List<PositionGps2> listPositions;
	List<Paquet> listPaquets = new ArrayList<Paquet>();
	public ProcessSubsets_1(File dirRoot, int paquetSize,double tauxRecouvrement_) {
		this(dirRoot,paquetSize,tauxRecouvrement_,PositionGps2Factory.getListGpsPositionFromDirImages(new File(dirRoot, "images")));
	}
	
	

	public ProcessSubsets_1(File dirRoot, int paquetSize_,double tauxRecouvrement_, List<PositionGps2> listPositions_) {
		this.dirRoot = dirRoot;
		this.dirImages = new File(dirRoot, "images");
		this.nbTotalImages = dirImages.listFiles().length;
		this.listPositions =listPositions_;
		this.paquetSize=paquetSize_;
		this.tauxRecouvrement=tauxRecouvrement_;
		List<List<PositionGps2>> listList =UtilPositionGps2.extractPaquets(listPositions,paquetSize,tauxRecouvrement);
		this.listPaquets=toListPaquet(listList);
	
		System.out.println(" Total position  size "+listPositions.size());
		int i =1;
		for (Paquet paquet : listPaquets) {
			System.out.println(i+++" paquet ---> "+paquet);
		}
		createDirectories();
		System.out.println("Copy sh to directory");
		try {
			UtilCopyBg.copyResourceToDir("sh/processChapeau.sh", this.dirRoot.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/processMergePLY.sh", this.dirRoot.toPath(), true);
			//UtilCopyBg.copyResourceToDir("sh/processMergePoissonPLY_DEPRECATED.sh", this.dirRoot.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/processPlyToLaz.sh", this.dirRoot.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/processMergePLYtoLaz.sh", this.dirRoot.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/processLasToPotree.sh", this.dirRoot.toPath(), true);
			UtilCopyBg.copyResourceToDir("sh/README.md", this.dirRoot.toPath(), false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Copy done");
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
				//this.createDirectorie(paquet,this.dirRoot,colmapSubsetBuilder);
				paquet.createDirectorie_(this.dirRoot,colmapSubsetBuilder);
			}
		} catch (NoSuchFileException e) {
			System.err.println("Message :"+e.getMessage());
			System.err.println("Le traitement sparse doit être excuté ");
			e.printStackTrace();
		}catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	@Deprecated
	public void createDirectorie(Paquet paquet,File dir,ColmapSubsetBuilder colmapSubsetBuilder) {
		try {
				System.out.print("createDirectorie "+paquet.paquetName+" start ");
				File dirRootPaquet = new File(dir,paquet.paquetName);
				//dirSparse0.mkdirs();
				Set<String> listImages = paquet.getSetImages();
				System.out.print(" listImages "+listImages.size());
				colmapSubsetBuilder.processPaquet( dirRootPaquet.toPath(), listImages);
				File fileImages = new File(dirRootPaquet,"images.txt");
				System.out.print("  fileImages exists :"+fileImages.exists()+" size "+fileImages.length()+" copy sh to dir ");
				UtilCopyBg.copyResourcesToDir("paquet", dirRootPaquet.toPath());
				System.out.println("createDirectorie "+paquet.paquetName+" done");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String traceSubset() {
		String s=" ProcessSubset trace \n ";
		s+=" nbTotalImages: "+this.nbTotalImages+"\n";
		s+= " listPositions size :"+this.listPositions.size()+"\n";
		s+= " listPAquets size :"+this.listPaquets.size()+"\n";
		s +=" paquetSize :"+this.paquetSize+"\n";
		s+= " nb Toatal Images In Paquets "+getNbImagesInPaquets()+"\n";
		return s;
		
	}

	private int getNbImagesInPaquets() {
		int nb =0;
		for (Paquet paq : listPaquets) {
			nb += paq.listPositions.size();
		}
		return nb;
	}

	

	

}
