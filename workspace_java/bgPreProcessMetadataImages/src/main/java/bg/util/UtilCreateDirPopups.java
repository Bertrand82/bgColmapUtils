package bg.util;

import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;
import java.lang.*;

public class UtilCreateDirPopups {

	public static File createDirectoryPopup(Component componentParent) {
		File dirParent = getDirParent();
		JFileChooser fc = new JFileChooser(dirParent);
		fc.setDialogTitle("Choose directory");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);

		int r = fc.showOpenDialog(componentParent);
		if (r != JFileChooser.APPROVE_OPTION)
			return null;

		File dir = fc.getSelectedFile();
		setDirParent(dir.getParentFile());
		System.err.println("ParentDir " + dir.getAbsolutePath());
		dir.mkdirs();
		return dir;

	}

	private static void setDirParent(File parentFile) {
		try {
			Properties properties = PropertiesGlobal.getProperties();
			properties.setProperty(PropertiesGlobal.KEY_DIRECTORY_GENERATED_IMAGES, parentFile.getAbsolutePath());
			PropertiesGlobal.saveProperties("bg13");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static File getDirParent() {
		
		try {
			Properties properties = PropertiesGlobal.getProperties();
			String fileName = properties.getProperty(PropertiesGlobal.KEY_DIRECTORY_GENERATED_IMAGES);
			if (fileName != null) {
				File f = new File(fileName);
				return f.isDirectory() ? f : null; // on ne renvoie que si ça existe encore
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}