package bg.util;

import java.io.File;
import java.util.List;

import bg.metadata.MetaData;

public class PositionMetaData2Factory {

	public static PositionMetaData2 extractPosition(MetaData metaData) {
		if (metaData == null) {
			return null;
		}
		PositionMetaData2 pos = new PositionMetaData2();
		pos.setxCorrected(metaData.xCorrected);
		pos.setyCorrected(metaData.yCorrected);
		pos.setrView(metaData.rView);
		pos.setDate(metaData.date);
		pos.setXx(metaData.x);
		pos.setYy(metaData.y);
		pos.setZz(metaData.z);
		pos.setImageName(metaData.fileName);
		pos.setAltitudeMeters(metaData.z);
		pos.setPitch(metaData.pitch);
		pos.setRoll(metaData.roll);
		pos.setYaw(metaData.yaw);
		return pos;
	}

	public static PositionMetaData2 extractPosition(PositionGps2 pGps2, List<MetaData> listMetaData) {
		String imageName=pGps2.getImageName();
		MetaData metaData = getMetaData(listMetaData, imageName);
		if (metaData==null) {
			System.err.println("metadAta is null "+imageName+"   size: "+listMetaData.size());
		}else {
			metaData.updatePositionCorrectedn(pGps2);
		}
		
		return extractPosition(metaData);
	}

	private static MetaData getMetaData(List<MetaData> listMetaData, String fileName) {
		for (MetaData md :listMetaData) {
			if (md.fileName.equals(fileName)) {
				return md;
			}
		}
		return null;
	}
}
