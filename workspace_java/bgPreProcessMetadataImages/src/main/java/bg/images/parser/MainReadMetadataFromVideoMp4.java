package bg.images.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bg.util.Mp4GpsExtractor;
import bg.util.PositionGps2;

/**
 * Point d'entrée principal pour lire les métadonnées GPS depuis des fichiers vidéo MP4/MOV.
 *
 * <p>Utilise {@link Mp4GpsExtractor} avec la stratégie de fallback suivante :
 * <ol>
 *   <li>QuickTime location atom (ISO6709) — source native QuickTime/MP4</li>
 *   <li>XMP GPS embarqué dans le conteneur</li>
 *   <li>EXIF GPS directory (parfois présent dans les MP4 de drones)</li>
 *   <li>Fallback exiftool (nécessite exiftool installé sur le PATH)</li>
 * </ol>
 *
 * <p>Usage :
 * <pre>
 *   java bg.images.parser.MainReadMetadataFromVideoMp4 /chemin/vers/dossier_ou_fichier.mp4
 * </pre>
 */
public class MainReadMetadataFromVideoMp4 {

    public static void main(String[] args) throws Exception {
        String path = args.length > 0
                ? args[0]
                : "D:\\aws_drones_images\\video_test"; // chemin par défaut pour tests locaux

        File input = new File(path);
        if (!input.exists()) {
            System.err.println("Le chemin n'existe pas : " + path);
            System.exit(1);
        }

        List<File> videoFiles = collectVideoFiles(input);
        if (videoFiles.isEmpty()) {
            System.out.println("Aucun fichier MP4/MOV trouvé dans : " + path);
            return;
        }

        System.out.println("=== Lecture GPS depuis " + videoFiles.size() + " fichier(s) vidéo ===\n");
        int found = 0;
        int notFound = 0;
        for (File vf : videoFiles) {
            System.out.println("--- " + vf.getName() + " ---");
            PositionGps2 pos = Mp4GpsExtractor.extractFromVideoFile(vf);
            if (pos != null) {
                System.out.println("  GPS : " + pos);
                found++;
            } else {
                System.out.println("  GPS : non trouvé");
                notFound++;
            }
            System.out.println();
        }
        System.out.println("=== Résumé : " + found + " GPS trouvés / " + notFound + " non trouvés ===");
    }

    /**
     * Collecte les fichiers vidéo (MP4/MOV) depuis un fichier unique ou un répertoire.
     */
    public static List<File> collectVideoFiles(File input) {
        List<File> result = new ArrayList<>();
        if (input.isFile()) {
            if (isVideoFile(input)) result.add(input);
        } else if (input.isDirectory()) {
            File[] children = input.listFiles();
            if (children != null) {
                for (File f : children) {
                    if (f.isFile() && isVideoFile(f)) result.add(f);
                }
            }
        }
        return result;
    }

    private static boolean isVideoFile(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".mov");
    }
}
