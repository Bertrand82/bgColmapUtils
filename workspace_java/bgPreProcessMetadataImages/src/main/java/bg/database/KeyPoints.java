package bg.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * COLMAP keypoints stored as float32 row-major matrix.
 *
 * Typical COLMAP layout (depends on version/config):
 * - col 0: x
 * - col 1: y
 * - col 2: scale
 * - col 3: orientation
 * - (optional extra columns)
 */
public final class KeyPoints {

  private final int rows;
  private final int cols;
  private final float[] data; // row-major

  public KeyPoints(int rows, int cols, float[] data) {
    if (rows < 0 || cols < 0) {
      throw new IllegalArgumentException("rows/cols must be >= 0");
    }
    if (data == null) {
      throw new IllegalArgumentException("data must not be null");
    }
    if (data.length != rows * cols) {
      throw new IllegalArgumentException("Invalid data length: got " + data.length +
          ", expected " + (rows * cols) + " (rows=" + rows + ", cols=" + cols + ")");
    }
    this.rows = rows;
    this.cols = cols;
    this.data = data;
  }

  public int getRows() {
    return rows;
  }

  public int getCols() {
    return cols;
  }

  public float[] getData() {
    return data;
  }

  public float get(int r, int c) {
    return data[r * cols + c];
  }

  public float x(int keypointIndex) {
    return get(keypointIndex, 0);
  }

  public float y(int keypointIndex) {
    return get(keypointIndex, 1);
  }

  /**
   * Returns all keypoints as a list of pixel points (x,y).
   * If there are no keypoints, returns an empty list.
   */
  public List<Point> getListPoints() {
    if (rows == 0) {
      return Collections.emptyList();
    }
    ArrayList<Point> out = new ArrayList<Point>(rows);
    for (int i = 0; i < rows; i++) {
      out.add(new Point(x(i), y(i)));
    }
    return out;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(128);
    sb.append("KeyPoints{rows=").append(rows).append(", cols=").append(cols);

    if (rows > 0 && cols >= 2) {
      sb.append(", firstXY=(")
        .append(fmt(x(0))).append(", ")
        .append(fmt(y(0))).append(")");
    }

    // Petit aperçu de la 1ère ligne (max 6 valeurs) pour debug
    if (rows > 0 && cols > 0) {
      int n = Math.min(cols, 6);
      float[] firstRow = Arrays.copyOfRange(data, 0, n);
      sb.append(", firstRow[0..").append(n - 1).append("]=").append(Arrays.toString(firstRow));
      if (cols > n) sb.append("...");
    }

    sb.append('}');
    return sb.toString();
  }

  private static String fmt(float v) {
    return String.format(java.util.Locale.ROOT, "%.3f", v);
  }
}