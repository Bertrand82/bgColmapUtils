package bg.display.divide2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lit le fichier COLMAP images.txt et construit un mapping IMAGE_ID -> NAME.
 *
 * Format COLMAP:
 *   IMAGE_ID QW QX QY QZ TX TY TZ CAMERA_ID NAME
 *   POINTS2D[] as (X, Y, POINT3D_ID)
 *
 * Chaque image occupe 2 lignes :
 *   - ligne 1 : métadonnées image
 *   - ligne 2 : points 2D
 *
 * Les lignes commençant par '#' sont des commentaires.
 */
public class ColmapImagesReader {

    private final Map<Integer, String> imageIdToName = new LinkedHashMap<>();

    public ColmapImagesReader(File fileImages) throws IOException {
		this.readImages(fileImages.toPath());
	}

	private void readImages(Path imagesFile) throws IOException {
        imageIdToName.clear();

        try (BufferedReader reader = Files.newBufferedReader(imagesFile)) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Ligne 1 d'une image : IMAGE_ID QW QX QY QZ TX TY TZ CAMERA_ID NAME
                String[] tokens = line.split("\\s+");

                // Minimum: IMAGE_ID + 7 poses + CAMERA_ID + NAME = 10 tokens
                if (tokens.length < 10) {
                    throw new IOException("Ligne image invalide dans " + imagesFile + " : " + line);
                }

                int imageId;
                try {
                    imageId = Integer.parseInt(tokens[0]);
                } catch (NumberFormatException e) {
                    throw new IOException("IMAGE_ID invalide dans " + imagesFile + " : " + line, e);
                }

                // Le NAME peut contenir des espaces dans certains cas rares,
                // donc on reconstruit depuis le token 9 jusqu'à la fin.
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 9; i < tokens.length; i++) {
                    if (i > 9) {
                        nameBuilder.append(' ');
                    }
                    nameBuilder.append(tokens[i]);
                }
                String imageName = nameBuilder.toString();

                imageIdToName.put(imageId, imageName);

                // Lire et ignorer la 2e ligne (POINTS2D[])
                reader.readLine();
            }
        }
    }

    public String getImageName(int imageId) {
        return imageIdToName.get(imageId);
    }

    public Map<Integer, String> getImageIdToName() {
        return Collections.unmodifiableMap(imageIdToName);
    }

    public boolean containsImageId(int imageId) {
        return imageIdToName.containsKey(imageId);
    }

    public int size() {
        return imageIdToName.size();
    }

    public void printSummary() {
        System.out.println("Nombre d'images lues: " + imageIdToName.size());
        imageIdToName.entrySet().stream()
                .limit(10)
                .forEach(e -> System.out.println("IMAGE_ID " + e.getKey() + " -> " + e.getValue()));
    }

    public static void main(String[] args) throws IOException {
    	File dir_0 = UtilDivide2.dir_0_test;
        System.out.println("dir_0 exists "+dir_0.exists());
        File fileImages = new File(dir_0, "images.txt");

        ColmapImagesReader reader = new ColmapImagesReader(fileImages);
        
        reader.printSummary();

        // Exemple
        int testImageId = 1;
        String name = reader.getImageName(testImageId);
        System.out.println("Nom pour IMAGE_ID " + testImageId + " : " + name);
    }
}

