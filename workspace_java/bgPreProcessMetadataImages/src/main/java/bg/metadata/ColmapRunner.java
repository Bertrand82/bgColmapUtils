package bg.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ColmapRunner {

    private final Path colmapExe;   // ...\bin\colmap.exe
    private final Path workingDir;  // dossier de projet (optionnel)

    public ColmapRunner(Path colmapExe, Path workingDir) {
        this.colmapExe = colmapExe;
        this.workingDir = workingDir;
    }

    public int run(String subCommand, List<String> args) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(colmapExe.toString());
        cmd.add(subCommand);
        cmd.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (workingDir != null) pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);

        Process p = pb.start();

        // Sous Windows en français, la console peut être CP-850.
        // Si tu vois des caractères bizarres, remplace par Charset.forName("Cp850").
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }

        return p.waitFor();
    }

    // Exemple d'utilisation
    public static void main(String[] args) throws Exception {
        Path colmap = Path.of("C:/Program Files (x86)/colmap/colmap-x64-windows-cuda/bin/colmap.exe");

        Path projectDir = Path.of("D:/aws_drones_images/location1/colmap_project");
        Path imagesDir = projectDir.resolve("images");
        Path dbPath = projectDir.resolve("database.db");
        Path priorDir = Path.of("D:/aws_drones_images/location1/colmap_project/sparse/prior"); // tes fichiers cameras/images/points

        ColmapRunner runner = new ColmapRunner(colmap, projectDir);

        // 1) Test
        int code = runner.run("--help", List.of()); // équivalent: colmap.exe --help
        System.out.println("Exit=" + code);

        // 2) Feature extraction
        // code = runner.run("feature_extractor", List.of(
        //         "--database_path", dbPath.toString(),
        //         "--image_path", imagesDir.toString()
        // ));
        // System.out.println("feature_extractor Exit=" + code);

        // 3) Sequential matcher
        // code = runner.run("sequential_matcher", List.of(
        //         "--database_path", dbPath.toString(),
        //         "--SequentialMatching.overlap", "50"
        // ));
        // System.out.println("sequential_matcher Exit=" + code);

        // 4) Mapper (avec priors)
        // code = runner.run("mapper", List.of(
        //         "--database_path", dbPath.toString(),
        //         "--image_path", imagesDir.toString(),
        //         "--input_path", priorDir.toString(),
        //         "--output_path", projectDir.resolve("sparse").toString()
        // ));
        // System.out.println("mapper Exit=" + code);
    }
}