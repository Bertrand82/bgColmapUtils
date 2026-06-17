package bg.util;

import java.awt.Point;
import java.util.List;

public class PositionBean2 {
	public PositionGps2 gps;
	private PositionMetaData2 positionMetaData;
	public int px; // Position afficher dans Panel en pixel
	public int py;
	public int pxCorrected;
	public int pyCorrected;

	public PositionBean2(PositionGps2 gps2, PositionMetaData2 positionMetaData) {
		this.gps = gps2;
		this.positionMetaData = positionMetaData;
		
	}

	public void updatePosition(double scale, double xMin, double yMin) {

		this.px = (int) (scale * (gps.getX() - xMin));
		this.py = (int) (scale * (gps.getY() - yMin));
		if (this.positionMetaData == null) {
			this.pxCorrected = this.px;
			this.pyCorrected = this.py;
		} else {
			this.pxCorrected = (int) (scale * (positionMetaData.getxCorrected() - xMin));
			this.pyCorrected = (int) (scale * (positionMetaData.getyCorrected() - yMin));
		}

	}

	@Override
	public String toString() {
		return "Bean [gps=" + gps + ", px=" + px + ", py=" + py + "]";
	}

	public int distance(List<Point> listPointsInterret) {
		if (listPointsInterret.size() == 0) {
			return 0;
		}
		int distance = distance(listPointsInterret.getFirst());
		for (Point ppp : listPointsInterret) {
			int d1 = distance(ppp);
			if (d1 < distance) {
				distance = d1;
			}
		}
		return distance;

	}

	private int distance(Point pp) {
		return Math.abs(px - pp.x) + Math.abs(py - pp.y);

	}

	public PositionMetaData2 getPositionMetaData() {
		if (this.positionMetaData == null) {
			this.positionMetaData= new PositionMetaData2( gps);
			
		}
		return positionMetaData;
	}
	
	

}