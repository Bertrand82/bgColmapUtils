package bg.display.divide2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Lit un fichier COLMAP points3D.txt et construit une matrice de co-visibilité entre images.
 *
 * Format attendu par ligne:
 * POINT3D_ID X Y Z R G B ERROR TRACK[]
 *
 * TRACK[] = suite de couples:
 * IMAGE_ID POINT2D_IDX IMAGE_ID POINT2D_IDX ...
 */
public class ColmapPoints3DReader {

    private final Map<Integer, Integer> imageIdToIndex = new LinkedHashMap<>();
    private final List<Integer> indexToImageId = new ArrayList<>();
    private int[][] coVisibility;

    public ColmapPoints3DReader(File file3d) throws IOException {
    	 this.readPoints3D(file3d.toPath());
	}

	public void readPoints3D(Path points3DFile) throws IOException {
        List<int[]> tracks = new ArrayList<>();
        Set<Integer> imageIds = new TreeSet<>();

        try (BufferedReader reader = Files.newBufferedReader(points3DFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] tokens = line.split("\\s+");

                // Il faut au moins 8 colonnes fixes avant TRACK[]
                if (tokens.length < 8) {
                    continue;
                }

                // À partir de l'index 8, on a TRACK[] = (IMAGE_ID, POINT2D_IDX)...
                int trackStart = 8;
                int remaining = tokens.length - trackStart;

                // TRACK[] doit contenir un nombre pair de valeurs
                if (remaining < 2 || remaining % 2 != 0) {
                    continue;
                }

                List<Integer> trackImageIds = new ArrayList<>();

                for (int i = trackStart; i < tokens.length; i += 2) {
                    int imageId = Integer.parseInt(tokens[i]);
                    trackImageIds.add(imageId);
                    imageIds.add(imageId);
                }

                // Dédupliquer au cas où une image apparaîtrait plusieurs fois dans une track
                int[] uniqueTrack = trackImageIds.stream().distinct().mapToInt(Integer::intValue).toArray();

                if (uniqueTrack.length >= 2) {
                    tracks.add(uniqueTrack);
                }
            }
        }

        buildImageIndex(imageIds);
        buildCoVisibilityMatrix(tracks);
    }

    private void buildImageIndex(Set<Integer> imageIds) {
        imageIdToIndex.clear();
        indexToImageId.clear();

        int index = 0;
        for (int imageId : imageIds) {
            imageIdToIndex.put(imageId, index);
            indexToImageId.add(imageId);
            index++;
        }
    }

    private void buildCoVisibilityMatrix(List<int[]> tracks) {
        int n = imageIdToIndex.size();
        coVisibility = new int[n][n];

        for (int[] track : tracks) {
            for (int i = 0; i < track.length; i++) {
                for (int j = i + 1; j < track.length; j++) {
                    int imageIdA = track[i];
                    int imageIdB = track[j];

                    int a = imageIdToIndex.get(imageIdA);
                    int b = imageIdToIndex.get(imageIdB);

                    coVisibility[a][b] += 1;
                    coVisibility[b][a] += 1;
                }
            }
        }
    }

    public int[][] getCoVisibility() {
        return coVisibility;
    }

    public Map<Integer, Integer> getImageIdToIndex() {
        return Collections.unmodifiableMap(imageIdToIndex);
    }

    public List<Integer> getIndexToImageId() {
        return Collections.unmodifiableList(indexToImageId);
    }

    public int getCoVisibilityByImageId(int imageIdA, int imageIdB) {
        Integer a = imageIdToIndex.get(imageIdA);
        Integer b = imageIdToIndex.get(imageIdB);

        if (a == null || b == null) {
            throw new IllegalArgumentException("Unknown image ID(s): " + imageIdA + ", " + imageIdB);
        }

        return coVisibility[a][b];
    }

    public void printSummary() {
        System.out.println("Nombre d'images dans la matrice: " + indexToImageId.size());

        int nonZeroPairs = 0;
        int max = 0;
        int maxA = -1;
        int maxB = -1;

        for (int i = 0; i < coVisibility.length; i++) {
            for (int j = i + 1; j < coVisibility.length; j++) {
                if (coVisibility[i][j] > 0) {
                    nonZeroPairs++;
                    if (coVisibility[i][j] > max) {
                        max = coVisibility[i][j];
                        maxA = indexToImageId.get(i);
                        maxB = indexToImageId.get(j);
                    }
                }
            }
        }

        System.out.println("Nombre de paires avec co-visibilité > 0: " + nonZeroPairs);
        if (maxA != -1) {
            System.out.println("Paire la plus co-visible: image " + maxA + " <-> image " + maxB + " = " + max);
        }
    }

    public static void main(String[] args) throws IOException {
        File dir_0 = UtilDivide2.dir_0_test;
        System.out.println("dir_0 exists "+dir_0.exists());
        File file3D = new File(dir_0, "points3D.txt");
        System.out.println("file3D exists "+file3D.exists());
        ColmapPoints3DReader reader = new ColmapPoints3DReader(file3D);
     
        reader.printSummary();

        // Exemple d'affichage des premières images
        System.out.println("--- Mapping IMAGE_ID -> matrix index ---");
        reader.getImageIdToIndex().entrySet().stream()
                .limit(10)
                .forEach(e -> System.out.println("IMAGE_ID " + e.getKey() + " -> index " + e.getValue()));
    }
}