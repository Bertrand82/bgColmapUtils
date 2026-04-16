package bg.util;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PositionMetaData2UtilCloser {
	
	
	public static Set<PositionMetaData2> searchClosest(PositionMetaData2 metaData0, List<PositionMetaData2> list2,int nDate,int nDistance){
		Set<PositionMetaData2> setPosition = new HashSet<PositionMetaData2>();
		setPosition.addAll(searchClosestByDate(metaData0, list2, nDate));
		setPosition.addAll(searchClosestByDistance(metaData0, list2, nDistance));
		return setPosition;
	}

	public static Set<PositionMetaData2> searchClosestByDistance(PositionMetaData2 metaData0, List<PositionMetaData2> list2,
			int nMax) {

		Set<PositionMetaData2> setPosition = new HashSet<PositionMetaData2>();
		list2.sort(Comparator.comparingDouble(p -> p.distanceTo(metaData0)));
		for(PositionMetaData2 p : list2) {
			if (p.equals(metaData0)) {
				
			}else {
				setPosition.add(p);
			}
			if (setPosition.size()>nMax) {
				break;
			}
		}
		return setPosition;

	}
	
	public static Set<PositionMetaData2> searchClosestByDate(PositionMetaData2 metaData0, List<PositionMetaData2> list2,
			int nMax) {

		Set<PositionMetaData2> setPosition = new HashSet<PositionMetaData2>();
		list2.sort(Comparator.comparingDouble(p -> {if (p ==null) return 10000;return p.dateTo(metaData0);}));
		for(PositionMetaData2 p : list2) {
			if (p.equals(metaData0)) {
				
			}else {
				setPosition.add(p);
			}
			if (setPosition.size()>nMax) {
				break;
			}
		}
		return setPosition;

	}

}
