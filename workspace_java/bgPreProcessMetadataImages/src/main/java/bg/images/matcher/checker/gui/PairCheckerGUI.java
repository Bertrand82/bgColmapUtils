package bg.images.matcher.checker.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.*;

import javax.swing.JFrame;
import javax.swing.JTextField;

import bg.gui.DataGui;
import bg.images.matcher.checker.PairChecker;
import bg.images.matcher.checker.PaireSimple;

public class PairCheckerGUI {

	PairChecker pairChecker;
	JFrame frame;
	private final PanelImage canvas1;
	private final PanelImage canvas2;
	private final JTextField infoField;
	PaireSimple pairCurrent;
	int iCurrentPair;

	public PairCheckerGUI(PairChecker pairChecker, File dirImages) {
		this.pairChecker = pairChecker;
		iCurrentPair = 0;
		this.pairCurrent = this.pairChecker.list.get(iCurrentPair);
		frame = new JFrame("bg");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		infoField = new JTextField();
		infoField.setEditable(false);
		JButton buttonPrevious = new JButton("previous");
		JButton buttonNext = new JButton(" next   ");
		buttonNext.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				nextImage(1);

			}
		});
		buttonPrevious.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				nextImage(-1);

			}
		});
		JPanel panelNorth = new JPanel(new BorderLayout());
		panelNorth.add(infoField, BorderLayout.CENTER);
		panelNorth.add(buttonNext, BorderLayout.EAST);
		panelNorth.add(buttonPrevious, BorderLayout.WEST);
		PaireSimple pair = pairChecker.list.get(0);
		canvas1 = new PanelImage(pair.imag1,pair.nbRelations1, dirImages ,pairChecker);
		canvas2 = new PanelImage(pair.imag2,pair.nbRelations2, dirImages,pairChecker);

		GridLayout gridLayOut = new GridLayout(1, 2);
		JPanel panel0 = new JPanel(gridLayOut);
		panel0.add(canvas1);
		panel0.add(canvas2);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panelNorth, BorderLayout.NORTH);
		frame.getContentPane().add(panel0, BorderLayout.CENTER);

		frame.setSize(900, 700);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		panel0.updateUI();

	}

	private void nextImage(int i) {
		iCurrentPair+=i;
		if (iCurrentPair <0) {
			iCurrentPair=0;
		}
		if (iCurrentPair >= pairChecker.list.size()) {
			iCurrentPair=pairChecker.list.size()-1;
		}
		this.pairCurrent = pairChecker.list.get(iCurrentPair);
		this.canvas1.updateImage(pairCurrent.imag1,pairCurrent.nbRelations1);
		this.canvas2.updateImage(pairCurrent.imag2,pairCurrent.nbRelations2);
	}

}
