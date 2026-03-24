package bg.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class Vol {
	
	public int pitchInt ;

	List<MetaData> list = new ArrayList<MetaData>();
	int numeroVol;

	public Vol(int numero) {
		super();
		this.numeroVol = numero;
	}

	public String toString() {
		String s = "Vol " + numeroVol + " nb images : " + list.size();
		return s;
	}

	public List<MetaData> extractViewFromVol30() {
		return extractViewFromVol(30);
	}

	private List<MetaData> extractViewFromVol(int i) {
		return extractViewFromVolFirstImages(i);
	}

	public List<MetaData> extractViewFromVolFirstImages(int nbMaxImages) {
		List<MetaData> listExtracted = new ArrayList<MetaData>();
		int nbTotal = this.list.size();
		if (nbTotal < nbMaxImages) {
			return list;
		}
		
		// i va de 0..sampleCount-1, index arrondi uniformément sur 0..n-1
		for (int i = 0; i < nbMaxImages; i++) {
			listExtracted.add(list.get(i));
		}
		return listExtracted;
	}
	public List<MetaData> extractViewFromVolEchantillonage(int nbMaxImages) {
		List<MetaData> listExtracted = new ArrayList<MetaData>();
		int nbTotal = this.list.size();
		if (nbTotal < nbMaxImages) {
			return list;
		}
		double k = nbTotal / nbMaxImages;
		// i va de 0..sampleCount-1, index arrondi uniformément sur 0..n-1
		for (int i = 0; i < nbMaxImages; i++) {
			int idx = (int) Math.round(i * (nbTotal - 1) / (double) (nbMaxImages - 1));
			listExtracted.add(list.get(idx));
		}
		return listExtracted;
	}
	 File fileMetadataGenerated2_;

	public void generateExtraction(File dirImadesIn, File dirOut, int max) {
		try {
			List<MetaData> l = this.extractViewFromVol(max);
			String metadata ="";
			for(MetaData i :l) {
				metadata+=i.line+"\n";
			}
			
			File dirImagesOut = new File(dirOut,"images");
			dirImagesOut.mkdirs();
			File fileMetadataGenerated = new File(dirOut,"metadata.csv");
			Files.writeString( fileMetadataGenerated.toPath(), metadata, StandardCharsets.UTF_8);
		
			for(MetaData idvMetaData :l) {
				File fileImageIn = idvMetaData.getFileImageIn(dirImadesIn);
				
				File fileImageOut = new File(dirImagesOut,idvMetaData.fileName);
				copy(fileImageIn,fileImageOut);
				//reduceAndSave2(fileImageIn, fileImageOut,1);
			}
			System.out.println("dirImageOut : "+dirImagesOut.getName()+"|  nb de fichiers "+dirImagesOut.listFiles().length+" | list image  "+list.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
    private void copy(File fileImageIn, File fileImageOut) throws Exception{
    	  Files.copy(fileImageIn.toPath(), fileImageOut.toPath(),
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.COPY_ATTRIBUTES);
		
	}

	
    public void processColmap__(File fileMetadataGenerated, File imageDirGenerated) {
    	
    	try {
			//ConvertToColmapWithExif convertToColMap = new ConvertToColmapWithExif(fileMetadataGenerated.toPath(), imageDirGenerated.toPath(),dirOut.toPath());
			//convertToColMap.process();
			//pastRunColmapBat(imageDirGenerated.getParentFile().getParentFile());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
   

}
