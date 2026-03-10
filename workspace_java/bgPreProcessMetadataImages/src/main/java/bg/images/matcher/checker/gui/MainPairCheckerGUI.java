package bg.images.matcher.checker.gui;

import bg.images.matcher.checker.MainPairChecker;
import bg.images.matcher.checker.PairChecker;

public class MainPairCheckerGUI {

	public static void main(String[] args) throws Exception{
		   PairChecker pairChecker = new PairChecker(MainPairChecker.filePairs);
	       System.out.println("pairChecker done");
	       System.out.println("fileDatabase exists : "+MainPairChecker.fileDataBase.exists()+" "+MainPairChecker.fileDataBase.getCanonicalPath());
	       System.out.println("dirImages exists : "+MainPairChecker.dirImages.exists()+" "+MainPairChecker.dirImages.getCanonicalPath());
	       new PairCheckerGUI(pairChecker,MainPairChecker.dirImages,MainPairChecker.fileDataBase);
	}

}
