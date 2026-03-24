package bg.images.matcher.checker.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.swing.*;

import javax.swing.JFrame;
import javax.swing.JTextField;

import bg.database.DatabaseColmap;
import bg.database.Match;
import bg.database.UtilDataBase;
import bg.gui.DisplayImagesGui;
import bg.images.matcher.checker.PairChecker;
import bg.images.matcher.checker.PaireSimple;

public class PairCheckerGUI {

	PairChecker pairChecker;
	JFrame frame;
	private final PanelImage pImage1;
	private final PanelImage pImage2;
	private final JTextField infoField;
	PaireSimple pairCurrent;
	int iCurrentPair;
	boolean isMatchesDisplay = false;
	JTextField textFieldSearch = new JTextField(30) ;
	DatabaseColmap databaseColmap;

	public PairCheckerGUI(PairChecker pairChecker, File dirImages, File databaseColmapFile) throws Exception{
		this.pairChecker = pairChecker;
		this.databaseColmap= new DatabaseColmap(databaseColmapFile);
		iCurrentPair = 0;
		this.pairCurrent = this.pairChecker.list.get(iCurrentPair);
		frame = new JFrame("bg");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		infoField = new JTextField();
		infoField.setEditable(false);
		JButton buttonPrevious = new JButton("previous");
		JButton buttonNext = new JButton(" next   ");		
		JButton buttonMatches = new JButton(" match   ");
		
		buttonMatches.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayMatches();

			}

		
		});
		
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
		JButton buttonRechercherByName = new JButton("search by name");
		buttonRechercherByName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rechercherImagesByName();

			}			
		});
		JButton buttonRechercherByPairId = new JButton("search by pairId");
		buttonRechercherByPairId.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rechercherImagesByPairId();

			}			
		});
		JPanel panelNorth = new JPanel(new BorderLayout());
		panelNorth.add(infoField, BorderLayout.CENTER);
		JPanel panelSearch = new JPanel();
		panelSearch.add(this.textFieldSearch);
		panelSearch.add(buttonRechercherByName);
		panelSearch.add(buttonRechercherByPairId);
		JPanel panelNextPrevious = new JPanel(new GridLayout(1,3));
		panelNextPrevious.add(buttonMatches);
		panelNextPrevious.add(buttonPrevious);
		panelNextPrevious.add(buttonNext);
		panelNorth.add(panelNextPrevious, BorderLayout.EAST);
		panelNorth.add(panelSearch, BorderLayout.CENTER);
		
		PaireSimple pair = pairChecker.list.get(0);
		pImage1 = new PanelImage(pair.imag1,pair.nbRelations1, dirImages ,pairChecker,databaseColmap);
		pImage2 = new PanelImage(pair.imag2,pair.nbRelations2, dirImages,pairChecker,databaseColmap);

		GridLayout gridLayOut = new GridLayout(1, 2);
		JPanel panel0 = new JPanel(gridLayOut);
		panel0.add(pImage1);
		panel0.add(pImage2);
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
		this.pImage1.updateImage(pairCurrent.imag1,pairCurrent.nbRelations1);
		this.pImage2.updateImage(pairCurrent.imag2,pairCurrent.nbRelations2);
	}
	
	private void rechercherImagesByName() {
		System.out.println("rechercher "+this.textFieldSearch.getText());
		
			
		iCurrentPair=getSearchPear(this.textFieldSearch.getText());
		if (iCurrentPair <0) {
			iCurrentPair=0;
		}
		if (iCurrentPair >= pairChecker.list.size()) {
			iCurrentPair=pairChecker.list.size()-1;
		}
		this.pairCurrent = pairChecker.list.get(iCurrentPair);
		this.pImage1.updateImage(pairCurrent.imag1,pairCurrent.nbRelations1);
		this.pImage2.updateImage(pairCurrent.imag2,pairCurrent.nbRelations2);
		}

	private void rechercherImagesByPairId() {
		try {
			System.out.println("rechercher by PairId "+this.textFieldSearch.getText());
			long pairId = Long.parseLong(this.textFieldSearch.getText());
			long id1=  UtilDataBase.getImageIdFromPairId_MAX(pairId);
			long id2 = UtilDataBase.getImageIdFromPairId_MIN(pairId);
			System.out.println("rechercher by PairId id1 : "+id1+"  id2 :"+id2);
			String name1 = this.databaseColmap.getNameFromImageId(id1);
			String name2 = this.databaseColmap.getNameFromImageId(id2);
			System.out.println("rechercher by PairId name1 : "+name1+"  name2 :"+name2);
			PaireSimple pairSimple =new PaireSimple(name1+" "+name2);
			System.out.println("rechercher by Pair : "+pairSimple);
			this.iCurrentPair=this.pairChecker.getIndexPair(pairSimple);
			System.out.println("rechercher by PairId iCurrentPair : "+this.iCurrentPair);
			iCurrentPair=getSearchPear(this.textFieldSearch.getText());
			if (iCurrentPair <0) {
				iCurrentPair=0;
			}
			if (iCurrentPair >= pairChecker.list.size()) {
				iCurrentPair=pairChecker.list.size()-1;
			}
			this.pairCurrent = pairChecker.list.get(iCurrentPair);
			System.out.println("rechercher by PairId pairCurrent :"+this.pairCurrent);
			this.pImage1.updateImage(pairCurrent.imag1,pairCurrent.nbRelations1);
			this.pImage2.updateImage(pairCurrent.imag2,pairCurrent.nbRelations2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

	private int getSearchPear(String search) {
		int i=0;
		int j = this.iCurrentPair;
		while(i<this.pairChecker.list.size()) {
			j++;
			if (j >= this.pairChecker.list.size()) {
				j =0;
			}
			PaireSimple pair = this.pairChecker.list.get(j);
			if (pair.searchMatch(search)){
				return j;
			}
			i++;
		}
		return this.iCurrentPair;
	}
	
	private void displayMatches() {
		try {
			this.isMatchesDisplay=!this.isMatchesDisplay;
			List<Match> list =this.databaseColmap.readVerifiedMatches(pairCurrent.imag1, pairCurrent.imag2);
			System.out.println("list match size :"+list.size());
			this.pImage1.isMatchesDisplay= this.isMatchesDisplay;
			this.pImage2.isMatchesDisplay= this.isMatchesDisplay;
			this.pImage1.listIndexMatches=UtilDataBase.getListIndexMatches_2(list);// Pourquoi inversé ??? Je ne sais pas !
			this.pImage2.listIndexMatches=UtilDataBase.getListIndexMatches_1(list);
			this.pImage1.updateListPoints();
			this.pImage2.updateListPoints();
			this.pImage1.canvas.repaint();
			this.pImage2.canvas.repaint();
			//this.pImage1.debugMatches();
			//this.pImage2.debugMatches();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	

}
