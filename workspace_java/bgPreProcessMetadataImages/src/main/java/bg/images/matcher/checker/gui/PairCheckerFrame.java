package bg.images.matcher.checker.gui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

import bg.images.matcher.checker.PairChecker;

public class PairCheckerFrame {

	final private JFrame frame = new JFrame("Pair Checker");
	public PairCheckerFrame(PairChecker pairChecker, File dirimages, File filedatabase) throws Exception {
		PairCheckerPanel pairCheckerGUI = new PairCheckerPanel(pairChecker, dirimages, filedatabase);
	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(pairCheckerGUI,BorderLayout.CENTER);
		frame.setSize(900, 700);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
