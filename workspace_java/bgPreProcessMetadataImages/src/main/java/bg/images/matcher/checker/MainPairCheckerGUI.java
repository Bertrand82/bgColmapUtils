package bg.images.matcher.checker;

import bg.images.matcher.checker.gui.PairCheckerGUI;

public class MainPairCheckerGUI {

	public static void main(String[] args) throws Exception{
		   PairChecker pairChecker = new PairChecker(MainPaiChecker.filePairs);
	       System.out.println("pairChecker done");
	       new PairCheckerGUI(pairChecker,MainPaiChecker.dirImages);
	}

}
