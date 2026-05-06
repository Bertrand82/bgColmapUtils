package bg;

import java.io.File;

import bg.display.together.gui.MainDisplayToGether;

public class Main {

	public static void main(String[] args) throws Exception{
		if (args.length > 0) {
			switch (args[0]) {
				case "--help":
					System.out.println("Usage: MainDisplayToGether [OPTION]");
					System.out.println("Options:");
					System.out.println("  --help   Show this help message");
					System.out.println("  --gui    Launch the display together GUI");
					return;
				case "--gui":
					launchGui();
					return;
				default:
					System.err.println("Unknown option: " + args[0]);
					System.err.println("Use --help for usage information.");
					return;
			}
		}
		launchGui();
	}

	private static void launchGui() throws Exception {
		MainDisplayToGether.main(new String[] { "--gui" });
	}

}
