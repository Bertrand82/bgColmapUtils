package bg.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A feature set for one COLMAP image_id:
 * - KeyPoints (float32 matrix) may be null if no keypoints row exists
 * - Descriptors (uint8 matrix) may be null if no descriptors row exists
 */
public final class Features {

  private final long imageId;
  private final KeyPoints keyPoints;          // nullable
  private final Descriptors descriptors;      // nullable

  public Features(long imageId, KeyPoints keyPoints, Descriptors descriptors) {
    this.imageId = imageId;
    this.keyPoints = keyPoints;
    this.descriptors = descriptors;
  }

  public long getImageId() {
    return imageId;
  }

  public KeyPoints getKeyPoints() {
    return keyPoints;
  }

  public Descriptors getDescriptors() {
    return descriptors;
  }

  /**
   * Returns all keypoints as a list of pixel points (x,y).
   * If no keypoints exist, returns an empty list.
   *
   * Note: uses a lightweight immutable Point class defined below
   * (to avoid depending on AWT).
   */
  public List<Point> getListPoints() {
    if (keyPoints == null || keyPoints.getRows() == 0) {
      return Collections.emptyList();
    }

    int n = keyPoints.getRows();
    ArrayList<Point> out = new ArrayList<Point>(n);
    for (int i = 0; i < n; i++) {
      out.add(new Point(keyPoints.x(i), keyPoints.y(i)));
    }
    return out;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(256);
    sb.append("Features{imageId=").append(imageId);

    if (keyPoints == null) {
      sb.append(", keyPoints=null");
    } else {
      sb.append(", keyPoints=").append(keyPoints.getRows()).append("x").append(keyPoints.getCols());
      if (keyPoints.getRows() > 0 && keyPoints.getCols() >= 2) {
        sb.append(", firstXY=(")
          .append(fmt(keyPoints.x(0))).append(", ")
          .append(fmt(keyPoints.y(0))).append(")");
      }
    }

    if (descriptors == null) {
      sb.append(", descriptors=null");
    } else {
      sb.append(", descriptors=").append(descriptors.getRows()).append("x").append(descriptors.getCols());
      if (descriptors.getRows() > 0 && descriptors.getCols() > 0) {
        int n = Math.min(16, descriptors.getCols());
        int[] first = new int[n];
        for (int i = 0; i < n; i++) {
          first[i] = descriptors.getU8(0, i);
        }
        sb.append(", firstDesc[0..").append(n - 1).append("]=").append(Arrays.toString(first));
      }
    }

    sb.append('}');
    return sb.toString();
  }

  private static String fmt(float v) {
    return String.format(java.util.Locale.ROOT, "%.3f", v);
  }


  /**
   * Descriptors: uint8 matrix stored row-major (often cols=128 for SIFT).
   */
  public static final class Descriptors {
    private final int rows;
    private final int cols;
    private final byte[] data; // row-major

    public Descriptors(int rows, int cols, byte[] data) {
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

    public byte[] getData() {
      return data;
    }

    /** Unsigned byte 0..255 (Java 8 compatible). */
    public int getU8(int r, int c) {
      return data[r * cols + c] & 0xFF;
    }
  }
}