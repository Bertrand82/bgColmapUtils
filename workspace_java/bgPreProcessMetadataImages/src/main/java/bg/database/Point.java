package bg.database;

/**
 * Simple pixel point (float precision, immutable).
 * COLMAP keypoints are sub-pixel, hence float.
 */
public final class Point {
  public final float x;
  public final float y;

  public Point(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /** Rounded x for painting in integer pixel coordinates. */
  public int xi() {
    return Math.round(x);
  }

  /** Rounded y for painting in integer pixel coordinates. */
  public int yi() {
    return Math.round(y);
  }

  @Override
  public String toString() {
    return "Point{x=" + fmt(x) + ", y=" + fmt(y) + "}";
  }

  private static String fmt(float v) {
    return String.format(java.util.Locale.ROOT, "%.3f", v);
  }
}