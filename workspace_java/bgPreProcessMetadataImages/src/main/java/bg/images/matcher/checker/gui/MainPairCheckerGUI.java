package bg.images.matcher.checker.gui;

import bg.images.matcher.checker.MainPaiChecker;
import bg.images.matcher.checker.PairChecker;

public class MainPairCheckerGUI {

	public static void main(String[] args) throws Exception{
		   PairChecker pairChecker = new PairChecker(MainPaiChecker.filePairs);
	       System.out.println("pairChecker done");
	       new PairCheckerGUI(pairChecker,MainPaiChecker.dirImages);
	}

}
