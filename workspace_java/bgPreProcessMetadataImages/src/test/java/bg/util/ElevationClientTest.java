package bg.util;

public class ElevationClientTest {

	public static void main(String[] args) throws Exception{
		Double longitude = -80.1292d;
		Double latitude = 25.7770d;
		Double elevation = ElevationClient.getElevationOpenElevation(latitude, longitude);
		System.out.println("Elevation :"+elevation);
		longitude = -80.23d;
		System.out.println("Elevation :"+ElevationClient.getElevationOpenElevation(latitude, longitude));
	}

	
}
