package bg.display.together.gui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

public class DisplayTogetherFrame {

	final JFrame frame = new JFrame("DisplayImages");

	public DisplayTogetherFrame(File dir) throws Exception{
		DisplayTogetherPanel displayImagesGUI = new DisplayTogetherPanel(dir,frame);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(displayImagesGUI,BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);

	}

}
