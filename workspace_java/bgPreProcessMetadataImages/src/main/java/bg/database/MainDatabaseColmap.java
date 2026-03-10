package bg.database;

public class MainDatabaseColmap {

	public static void main(String[] args) throws Exception{
		String dbPath;
		long imageId;
		if (args.length < 2) {
			System.err.println("Usage: java ColmapDbReader <path/to/database.db> <image_id>");
			dbPath = "D:\\aws_drones_images\\generated_pitch\\vol_pitch_60\\database.db";
			imageId = 4;
		} else {
			dbPath = args[0];
			imageId = Long.parseLong(args[1]);
		}
		DatabaseColmap colMapDb =new DatabaseColmap(dbPath);
		
		
	}


}
