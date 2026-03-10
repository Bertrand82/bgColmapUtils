package bg.database;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Ajoute/colle ces morceaux dans ta classe existante ColmapDbReader (Java 1.8 OK)
public class ColmapDbReader___DEPRECATED {

  /** Couple (idx1, idx2) d’indices de keypoints. */


  // ---- dépendances vers tes champs/méthodes existants ----
  private Connection connection;
  private void ensureOpen() throws SQLException { /* déjà dans ta classe */ }

  private static int safeMul(int a, int b) { /* déjà dans ta classe */ return 0; }

  // -------------------------------------------------------



  /**
   * Lit les correspondances "géométriquement vérifiées" (two_view_geometries)
   * entre 2 images. Java 1.8: conversion bytes->int via ByteBuffer LE.
   *
   * @return liste de Match (peut être vide si pas de géométrie pour cette paire)
   */
  public static List<Match> readVerifiedMatches(long imageId1, long imageId2, Connection connection) throws SQLException {
   // ensureOpen();
    long pid =UtilDataBase. pairId(imageId1, imageId2);

    final String sql = "SELECT rows, cols, data FROM two_view_geometries WHERE pair_id = ?";
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = connection.prepareStatement(sql);
      ps.setLong(1, pid);
      rs = ps.executeQuery();

      if (!rs.next()) {
        return new ArrayList<Match>(0);
      }

      int rows = rs.getInt(1);
      int cols = rs.getInt(2);
      byte[] blob = rs.getBytes(3);

      if (cols != 2) {
        throw new IllegalStateException("Expected cols=2 in two_view_geometries, got " + cols);
      }

      int expectedBytes = safeMul(safeMul(rows, cols), 4); // int32
      if (blob == null) {
        throw new IllegalStateException("two_view_geometries blob is null (pair_id=" + pid + ")");
      }
      if (blob.length != expectedBytes) {
        throw new IllegalStateException("two_view_geometries blob size mismatch: got " + blob.length
            + " bytes, expected " + expectedBytes + " (rows=" + rows + ", cols=" + cols + ")");
      }

      ByteBuffer bb = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN);
      ArrayList<Match> out = new ArrayList<Match>(rows);
      for (int i = 0; i < rows; i++) {
        int idx1 = bb.getInt();
        int idx2 = bb.getInt();
        out.add(new Match(idx1, idx2));
      }
      return out;

    } finally {
      if (rs != null) try { rs.close(); } catch (Exception ignore) {}
      if (ps != null) try { ps.close(); } catch (Exception ignore) {}
    }
  }

  /**
   * Construit l'ensemble des indices keypoints à dessiner pour un côté:
   * side=1 -> prend idx1 ; side=2 -> prend idx2
   */
  public static Set<Integer> toKeypointIndexSet(List<Match> matches, int side) {
    if (side != 1 && side != 2) {
      throw new IllegalArgumentException("side must be 1 or 2");
    }
    HashSet<Integer> out = new HashSet<Integer>(matches.size() * 2);
    for (Match m : matches) {
      out.add(side == 1 ? m.idx1 : m.idx2);
    }
    return out;
  }
}