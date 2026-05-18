package bg.display.divide2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bg.display.divide.common.Paquet;
import bg.display.divide1.ColmapSubsetBuilder;
import bg.util.PositionGps2;
import bg.util.PositionGps2Factory;
import bg.util.UtilPositionGps2;

public class ProcessSubsets2 {

	final File dirRoot;
	final File dirImages;
	final int nbTotalImages;
	final int paquetSize;
	final double tauxRecouvrement;
	final File dir_sparse_0;
	final File dir_sparse;
	final File fileImages;
	final File filePoints3D;
	final ColmapImagesReader colmapImagesReader;
	final ColmapPoints3DReader colmapPoints3DReader;
	final ImagePacketGrouper imagePacketGrouper;
	final List<List<ImageId>> listListImages;
	final List<Paquet> listPaquets;
	private List<PositionGps2> listPositions;

	public ProcessSubsets2(File dir, int paquetSize, double tauxRecouvrement) throws Exception {
		this(dir,paquetSize,tauxRecouvrement,PositionGps2Factory.getListGpsPositionFromDirImages(new File(dir, "images")));
	}
	public ProcessSubsets2(File dir, int paquetSize, double tauxRecouvrement,List<PositionGps2> listPositions) throws Exception {
		this.listPositions=listPositions;
		this.dirRoot = dir;
		this.dirImages = new File(dirRoot, "images");
		this.dir_sparse = new File(dirRoot, "sparse");
		this.dir_sparse_0 = new File(dir_sparse, "0");
		this.fileImages = new File(dir_sparse_0, "images.txt");
		this.filePoints3D = new File(dir_sparse_0, "points3D.txt");
		this.nbTotalImages = (dirImages.exists() ? dirImages.list().length : 0);
		this.paquetSize = paquetSize;
		this.tauxRecouvrement = tauxRecouvrement;
		this.colmapImagesReader = new ColmapImagesReader(this.fileImages);
		this.colmapPoints3DReader = new ColmapPoints3DReader(filePoints3D);
		int[][] coVisibility = colmapPoints3DReader.getCoVisibility();
		this.imagePacketGrouper = new ImagePacketGrouper(coVisibility);
		List<List<Integer>> packets = imagePacketGrouper.buildPackets(paquetSize);
		listListImages = getListListImages(packets);
		listPaquets = getListPaquets(listListImages);
		processPaquets();
	}

	private void processPaquets() throws Exception{
		ColmapSubsetBuilder colmapSubsetBuilder= new ColmapSubsetBuilder(this.dir_sparse_0.toPath());
		for(Paquet paquet : this.listPaquets) {
			paquet.createDirectorie_(this.dirRoot,colmapSubsetBuilder);
		}
		
	}

	private List<Paquet> getListPaquets(List<List<ImageId>> listListImages2) {
		List<Paquet> listPa = new ArrayList<Paquet>();
		int i = 0;
		for (List<ImageId> listI : listListImages2) {
			List<PositionGps2> list = getListPositions(listI);
			Paquet paquet = new Paquet(list, dirImages, i++);
			listPa.add(paquet);
			paquet.updateReferenceNumeroPaquet();
		}
		return listPa;
	}

	private List<PositionGps2> getListPositions(List<ImageId> listI) {
		List<PositionGps2> lPositions = new ArrayList<PositionGps2>();
		for (ImageId imageId : listI) {
			System.err.println("imageId "+imageId);
			PositionGps2 position = getPositionFromList(imageId);
			if (position!= null) {
				
				lPositions.add(position);
			}
		}

		return lPositions;
	}

	private PositionGps2 getPositionFromList(ImageId imageId) {
		for (PositionGps2 pGps2 : this.listPositions) {
			if (pGps2.getImageName().equals(imageId.name)) {
				return pGps2;
			}
		}
		return null;
	}
	private List<List<ImageId>> getListListImages(List<List<Integer>> packets) {
		List<List<ImageId>> listes = new ArrayList<List<ImageId>>();
		for (List<Integer> listInteger : packets) {
			List<ImageId> listImages = new ArrayList<ImageId>();
			for (Integer id : listInteger) {
				String imageName = this.colmapImagesReader.getImageName(id);
				ImageId imageId = new ImageId(id, imageName);
				listImages.add(imageId);
			}
			listes.add(listImages);
		}
		return listes;
	}

	public String traceSubset() {
		String trace = "";
		trace += this.toString() + "\n";
		trace += "Nb de paquets ::" + listListImages.size()+"\n";
		int i = 0;
		for (List<ImageId> li : listListImages) {
			trace += i++ + " size: " + li.size() + " " + li + "\n";
		}
		return trace;
	}

	@Override
	public String toString() {
		return "ProcessSubsets2 [dirRoot=" + dirRoot + ", dirImages=" + dirImages + ", nbTotalImages=" + nbTotalImages
				+ ", paquetSize=" + paquetSize + ", tauxRecouvrement=" + tauxRecouvrement + ", dir_0=" + dir_sparse_0
				+ ", dir_sparse=" + dir_sparse + ", fileImages=" + fileImages + ", filePoints3D=" + filePoints3D
				+ ", colmapImagesReader=" + colmapImagesReader + ", colmapPoints3DReader=" + colmapPoints3DReader + "]";
	}

	static class ImageId {
		Integer id;
		String name;

		public ImageId(Integer id, String name) {
			super();
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return " [id=" + id + ", name=" + name + "]";
		}

	}

}
