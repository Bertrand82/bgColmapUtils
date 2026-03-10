package bg.database;

public final class Match {

	//
	/**
	 * idx1 = index du keypoint dans la table keypoints de la première image de la
	 * paire
	 */

	public final int idx1;
	/**
	 * idx2 = index du keypoint dans la table keypoints de la deuxième image de la
	 * paire
	 */
	public final int idx2;

	public Match(int idx1, int idx2) {
		this.idx1 = idx1;
		this.idx2 = idx2;
	}

	@Override
	public String toString() {
		return "Match{idx1=" + idx1 + ", idx2=" + idx2 + "}";
	}
}