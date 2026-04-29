package bg.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class UtilSwingChooseDoosier {

	
	public static File chooseDossierSparse(File dirSparse, String key, Component component) {
		System.out.println("choose Dossier sources");
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choisir le dossier source");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);

		// Optionnel : partir du dernier dossier choisi
		if (UtilFile.existsDir(dirSparse)) {
			chooser.setCurrentDirectory(dirSparse);
		} else {
			String dirPath = ""
					+ PropertiesGlobal.getProperties().getProperty(key+"", System.getProperty("user.home"));
			File file = new File(dirPath);

			chooser.setCurrentDirectory(file);
		}

		int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(component)); // ou this
		if (result == JFileChooser.APPROVE_OPTION) {
			dirSparse = chooser.getSelectedFile();
			PropertiesGlobal.saveProperty("DirSparse", dirSparse.getAbsolutePath());
			// Exemple : feedback utilisateur
			return dirSparse;
			
		}else {
			return null;
		}
	}


}
