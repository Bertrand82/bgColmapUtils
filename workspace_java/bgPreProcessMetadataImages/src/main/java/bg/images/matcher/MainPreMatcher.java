package bg.images.matcher;

import java.io.File;

public class MainPreMatcher {

	public static void main(String[] args) throws Exception{
		String directoryPath="D:\\aws_drones_images\\generated_pitch\\vol_pitch_60";
		//directoryPath="D:\\aws_drones_images - Copie\\generated\\vol_7";
		File dir = new File(directoryPath);
		File dirImages = new File(dir,"images");
		File fileMetadata = new File(dir,"metadata.csv");
		System.out.println("PreMatcher start");
		System.out.println("dirImages "+dirImages.getName()+"  exists "+dirImages.exists());
		System.out.println("fileMetadata "+fileMetadata.getName()+" exists "+fileMetadata.exists());
        PreMatcher preMatcher = new PreMatcher(fileMetadata,dirImages);
        System.out.println("PreMatcher done");
	}

}
