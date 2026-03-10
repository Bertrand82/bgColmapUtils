package bg.database;

import java.util.ArrayList;
import java.util.List;

public class UtilDataBase {
	
	  /**
	   * Encodage COLMAP pair_id.
	   * pair_id = min(image_id) * kMax + max(image_id), avec kMax = 2147483647 (2^31-1).
	   */
	  public static long pairId(long imageId1, long imageId2) {
	    final long kMax = 2147483647L;
	    long a = Math.min(imageId1, imageId2);
	    long b = Math.max(imageId1, imageId2);
	    return a * kMax + b;
	  }

	  /**
	   * Décodage COLMAP pair_id -> (image_id_min, image_id_max).
	   *
	   * Formule inverse:
	   *   image_id_min = pair_id / kMax
	   *   image_id_max = pair_id % kMax
	   */
	  public static long getImageIdFromPairId_MAX(long pairId) {
		    final long kMax = 2147483647L;

		   
		    long imageIdMin = pairId / kMax;
		    long imageIdMax = pairId % kMax;

		    return imageIdMax;
		  }
	  
	  public static long getImageIdFromPairId_MIN(long pairId) {
		    final long kMax = 2147483647L;

		   
		    long imageIdMin = pairId / kMax;
		    long imageIdMax = pairId % kMax;

		    return imageIdMin;
		  }

	  public static List<Integer> getListIndexMatches_1(List<Match> listMatches) {
		  List<Integer>  list = new ArrayList<Integer>();
		  for (Match m : listMatches) {
			  list.add(m.idx1);
		  }
		  return list;
	  }
	  public static List<Integer> getListIndexMatches_2(List<Match> listMatches) {
		  List<Integer>  list = new ArrayList<Integer>();
		  for (Match m : listMatches) {
			  list.add(m.idx2);
		  }
		  return list;
	  }

}
