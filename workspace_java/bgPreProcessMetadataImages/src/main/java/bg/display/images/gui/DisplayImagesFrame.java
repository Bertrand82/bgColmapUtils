package bg.display.images.gui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

public class DisplayImagesFrame {

	final JFrame frame = new JFrame("DisplayImages");

	public DisplayImagesFrame(File dir) throws Exception{
		DisplayImagesPanel displayImagesGUI = new DisplayImagesPanel(dir);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(displayImagesGUI,BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);

	}

}
