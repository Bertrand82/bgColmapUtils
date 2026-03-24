package bg.images.matcher.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import bg.metadata.MetaData;
import bg.metadata.MetaDatasCsv;
import bg.util.GpsPositionFactory;

public class PreMatcher {

	MetaDatasCsv metaDatasCsv;
	File dirImages;
	List<PaireMetadataClose> listPAires = new ArrayList<PaireMetadataClose>();
	Set<PaireMetadataClose> setPairesUniques;

	public PreMatcher(File fileMetadata, File dirImages) throws Exception {
		this(new MetaDatasCsv(fileMetadata,dirImages), dirImages);

	}

	public PreMatcher(MetaDatasCsv metaDatasCsv, File dirImages) throws Exception {
		this.metaDatasCsv = metaDatasCsv;
		this.dirImages = dirImages;
		loadGpsFromDirImages();
		processGpsFromView();
		processLoopListClosers();
		consolidationPaire();
		exportListPaires();
	}

	private void loadGpsFromDirImages() throws Exception {

		int i = 0;
		int iDoentexist=0;
		for (MetaData metaData : metaDatasCsv.getList()) {
			File fileImage = new File(dirImages, metaData.fileName);
			if (fileImage.exists()) {
				i += metaData.updateGpsPosition(fileImage);
			}else {
				iDoentexist++;
				System.err.println("File doesn't exists "+metaData.fileName+" in  dirImage exist: "+dirImages.exists()+"  "+dirImages.getAbsolutePath());
			}
		}
		System.out.println("Gps position from jpg updated " + i + " / " + metaDatasCsv.getList().size());
		System.out.println("File doen't exists  " + iDoentexist + " / " + metaDatasCsv.getList().size());

	}

	private void processGpsFromView() throws Exception {

		for (MetaData metaData : metaDatasCsv.getList()) {
			File fileImage = new File(dirImages, metaData.fileName);
			metaData.correctGpsPosition();
		}
		System.out.println("Gps position corrected updated  " + metaDatasCsv.getList().size());

	}

	private void processLoopListClosers() throws Exception {
		int i = 0;
		for (MetaData metaData : metaDatasCsv.getList()) {
			
			metaData.searchCloseView(i, metaDatasCsv.getList());
			i++;
		}
		System.out.println("Gps position corrected updated  " + metaDatasCsv.getList().size());

	}

	private void consolidationPaire() throws Exception {
		for (MetaData metaData : metaDatasCsv.getList()) {
			List<PaireMetadataClose> list = createPaires(metaData);
			this.listPAires.addAll(list);
		}
		System.out.println("List Paires size " + listPAires.size() + "  ");
		
		this.setPairesUniques = new HashSet<>(listPAires);
		System.out.println("Set Paires size " + setPairesUniques.size() + "  ");
		System.out.println("Moyenne Paires / image: " + setPairesUniques.size() / this.metaDatasCsv.getList().size() + "  ");
		
	}

	private List<PaireMetadataClose> createPaires(MetaData metaData) throws Exception {
		List<PaireMetadataClose> list = new ArrayList<PaireMetadataClose>();
		for (MetaData mClose : metaData.getListClose()) {
			list.add(new PaireMetadataClose(metaData, mClose));
		}
		return list;
	}

	private void exportListPaires() throws Exception {
		File fileDirOut = this.dirImages.getCanonicalFile().getParentFile();
		File fileOut = new File(fileDirOut, "match.txt");
		TreeSet<PaireMetadataClose> treeset = new TreeSet<PaireMetadataClose>(this.setPairesUniques);
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut)));

		for (PaireMetadataClose p : treeset) {
			String line = p.fileName1 + " " + p.fileName2 + "\n";
			w.write(line);
		}
		w.close();
		System.out.println("nb images "+metaDatasCsv.getList().size());
		System.out.println("Fichier ecrit dans "+fileOut.getPath());
	}
}
