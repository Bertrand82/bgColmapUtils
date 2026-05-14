package bg.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bg.display.divide.Paquet;

public class UtilPositionGps2 {

	public static MinMaxBounds computeMinMax(List<PositionGps2> positions) {
		if (positions == null || positions.isEmpty()) {
			throw new IllegalArgumentException("positions is null or empty");
		}

		PositionGps2 first = positions.get(0);
		double xMin = first.getX();
		double xMax = xMin;
		double yMin = first.getY();
		double yMax = yMin;

		for (PositionGps2 pos : positions) {
			double x = pos.getX();
			double y = pos.getY();

			if (x < xMin)
				xMin = x;
			if (x > xMax)
				xMax = x;
			if (y < yMin)
				yMin = y;
			if (y > yMax)
				yMax = y;
		}

		return new MinMaxBounds(xMin, yMin, xMax, yMax);
	}

	public static List<List<PositionGps2>> extractPaquets(List<PositionGps2> listAllPositions, int paquetSize,double tauxRecouvrement) {
		List<List<PositionGps2>> listList = new ArrayList<List<PositionGps2>>();
		List<BeanPositionGps2> listCurrent = initListBeanPosition(listAllPositions);
		while (listCurrent.size() > 0) {
			System.out.println("listCurrent size : "+listCurrent.size());
			BeanPositionGps2 pPLusAuNord = getPositionGpsNordestBeans(listCurrent);
			initDistance(listCurrent, pPLusAuNord);
			listCurrent.sort(Comparator.comparingDouble(BeanPositionGps2::getDistance2));			
			List<BeanPositionGps2> firstN2 = new ArrayList<>(	
					listCurrent.subList(0, Math.min(paquetSize, listCurrent.size())));
			List<PositionGps2> firsNSimple = toListPostions(firstN2);
			listList.add(firsNSimple);
			int taillePaquetCurrent =  Math.min(firstN2.size(), listCurrent.size());
			int tailleRemoved = (int) (taillePaquetCurrent*(1-tauxRecouvrement));
			listCurrent.subList(0, tailleRemoved).clear();;
		}
		return listList;
	}

	private static List<PositionGps2> toListPostions(List<BeanPositionGps2> firstN) {
		List<PositionGps2> list = new ArrayList<PositionGps2>();
		firstN.forEach(b -> list.add(b.position));
		return list;
	}

	private static void initDistance(List<BeanPositionGps2> list, BeanPositionGps2 pt) {
		for (BeanPositionGps2 bean : list) {
			bean.initDistance(pt);
		}

	}

	private static List<BeanPositionGps2> initListBeanPosition(List<PositionGps2> listAllPositions) {
		List<BeanPositionGps2> list = new ArrayList<UtilPositionGps2.BeanPositionGps2>();
		for (PositionGps2 p : listAllPositions) {
			BeanPositionGps2 bean = new BeanPositionGps2(p);
			list.add(bean);
		}
		return list;
	}

	private static PositionGps2 getPositionGpsNordest(List<PositionGps2> positions) {
		if (positions.size() == 0) {
			return null;
		}
		PositionGps2 pp = positions.get(0);
		for (PositionGps2 p : positions) {
			if (p.getX() > pp.getX()) {
				pp = p;
			}
		}
		return pp;
	}

	private static BeanPositionGps2 getPositionGpsNordestBeans(List<BeanPositionGps2> beans) {
		if (beans.size() == 0) {
			return null;
		}
		BeanPositionGps2 pp = beans.get(0);
		for (BeanPositionGps2 bean : beans) {
			if (bean.position.getX() > pp.position.getX()) {
				pp = bean;
			}
		}
		return pp;
	}

	public static class BeanPositionGps2 {
		PositionGps2 position;
		double distanceAbs;
		double distance2;

		public BeanPositionGps2(PositionGps2 p) {
			this.position = p;
		}

		public void initDistance(BeanPositionGps2 pt) {
			double dx = pt.position.getX() - position.getX();
			double dy = pt.position.getY() - position.getY();
			this.distanceAbs = Math.abs(dx)
					+ Math.abs(dy);
			distance2 = dx*dx+dy*dy;
		}

		public double getDistanceAbs() {
			return distanceAbs;
		}
		public double getDistance2() {
			return distance2;
		}

	}

	public static class MinMaxBounds {
		public final double xMin;
		public final double yMin;
		public final double xMax;
		public final double yMax;

		public MinMaxBounds(double xMin, double yMin, double xMax, double yMax) {
			this.xMin = xMin;
			this.yMin = yMin;
			this.xMax = xMax;
			this.yMax = yMax;
		}

		public double getDy() {
			return yMax - yMin;
		}

		public double getDx() {
			return xMax - xMin;
		}
	}

}
