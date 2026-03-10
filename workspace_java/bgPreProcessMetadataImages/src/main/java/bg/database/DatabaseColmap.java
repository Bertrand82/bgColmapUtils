package bg.database;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * COLMAP SQLite database reader (Java 8).
 *
 * Reads: - keypoints: float32 little-endian matrix (rows x cols) -> KeyPoints -
 * descriptors: uint8 matrix (rows x cols) -> Features.Descriptors
 */
public class DatabaseColmap implements AutoCloseable {

	private final String dbPath;
	private final String jdbcUrl;
	private Connection connection;

	public DatabaseColmap(File dbPathFile) throws Exception {
		this(dbPathFile.getCanonicalPath());
	}

	public DatabaseColmap(String dbPath) {
		if (dbPath == null || dbPath.trim().isEmpty()) {
			throw new IllegalArgumentException("dbPath must not be null/empty");
		}
		this.dbPath = dbPath;
		this.jdbcUrl = "jdbc:sqlite:" + dbPath;
	}

	public void open() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			return;
		}
		connection = DriverManager.getConnection(jdbcUrl);
	}

	@Override
	public void close() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public boolean isOpen() throws SQLException {
		return connection != null && !connection.isClosed();
	}

	public String getDbPath() {
		return dbPath;
	}

	public long getImageIdFromName(String imageName) throws SQLException {
		if (imageName == null || imageName.trim().isEmpty()) {
			throw new IllegalArgumentException("imageName must not be null/empty");
		}
		ensureOpen();

		final String sql = "SELECT image_id FROM images WHERE name = ? LIMIT 1";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, imageName);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new java.util.NoSuchElementException("Image not found in DB: name='" + imageName + "'");
				}
				return rs.getLong(1);
			}
		}
	}

	public String getNameFromImageId(long imageId) throws SQLException {
		ensureOpen();

		final String sql = "SELECT name FROM images WHERE image_id = ? LIMIT 1";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, imageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new java.util.NoSuchElementException("Image not found in DB: image_id=" + imageId);
				}
				return rs.getString(1);
			}
		}
	}

	public KeyPoints readKeyPoints(long imageId) throws SQLException {
		ensureOpen();

		final String sql = "SELECT rows, cols, data FROM keypoints WHERE image_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, imageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				int rows = rs.getInt(1);
				int cols = rs.getInt(2);
				byte[] blob = rs.getBytes(3);

				int expectedBytes = safeMul(safeMul(rows, cols), 4); // float32
				if (blob == null) {
					throw new IllegalStateException("Keypoints blob is null (imageId=" + imageId + ")");
				}
				if (blob.length != expectedBytes) {
					throw new IllegalStateException(
							"Keypoints blob size mismatch: got " + blob.length + " bytes, expected " + expectedBytes
									+ " (rows=" + rows + ", cols=" + cols + ", imageId=" + imageId + ")");
				}

				float[] data = decodeFloat32LE(blob, rows * cols);
				return new KeyPoints(rows, cols, data);
			}
		}
	}

	public Features.Descriptors readDescriptors(long imageId) throws SQLException {
		ensureOpen();

		final String sql = "SELECT rows, cols, data FROM descriptors WHERE image_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, imageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				int rows = rs.getInt(1);
				int cols = rs.getInt(2);
				byte[] blob = rs.getBytes(3);

				int expectedBytes = safeMul(rows, cols); // uint8
				if (blob == null) {
					throw new IllegalStateException("Descriptors blob is null (imageId=" + imageId + ")");
				}
				if (blob.length != expectedBytes) {
					throw new IllegalStateException(
							"Descriptors blob size mismatch: got " + blob.length + " bytes, expected " + expectedBytes
									+ " (rows=" + rows + ", cols=" + cols + ", imageId=" + imageId + ")");
				}

				return new Features.Descriptors(rows, cols, blob);
			}
		}
	}

	public Features readFeatures(long imageId) throws SQLException {
		KeyPoints kp = readKeyPoints(imageId);
		Features.Descriptors desc = readDescriptors(imageId);
		return new Features(imageId, kp, desc);
	}

	public void printFirstRows(long imageId) throws SQLException {
		Features fs = readFeatures(imageId);
		if (fs.getKeyPoints() == null) {
			System.out.println("No keypoints for image_id=" + imageId);
			return;
		}

		KeyPoints kps = fs.getKeyPoints();
		System.out.println("KeyPoints: rows=" + kps.getRows() + " cols=" + kps.getCols());
		float[] firstKp = Arrays.copyOfRange(kps.getData(), 0, kps.getCols());
		System.out.println("First keypoint row: " + Arrays.toString(firstKp));

		if (fs.getDescriptors() != null) {
			Features.Descriptors desc = fs.getDescriptors();
			System.out.println("Descriptors: rows=" + desc.getRows() + " cols=" + desc.getCols());

			int n = Math.min(16, desc.getCols());
			int[] first16 = new int[n];
			for (int i = 0; i < n; i++) {
				first16[i] = desc.getU8(0, i);
			}
			System.out.println("First descriptor first " + n + " bytes: " + Arrays.toString(first16));
		}
	}

	// --------------------------
	// Internal helpers
	// --------------------------

	private void ensureOpen() throws SQLException {
		if (!isOpen()) {
			open();
		}
	}

	private static float[] decodeFloat32LE(byte[] blob, int floatCount) {
		ByteBuffer bb = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN);
		float[] out = new float[floatCount];
		for (int i = 0; i < out.length; i++) {
			out[i] = bb.getFloat();
		}
		return out;
	}

	private static int safeMul(int a, int b) {
		long r = (long) a * (long) b;
		if (r > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Multiplication overflow: " + a + " * " + b);
		}
		return (int) r;
	}

	public List<Match> readVerifiedMatches(String imageName1, String imageName2) throws SQLException {
		long id1 = getImageIdFromName(imageName1);
		long id2 = getImageIdFromName(imageName2);
		System.out.println("readVerifiedMatches id1 :" + id1 + "  id2 : " + id2);
		return readVerifiedMatches(id1, id2);
	}

	/**
	 * Lit les correspondances "géométriquement vérifiées" (two_view_geometries)
	 * entre 2 images. Java 1.8: conversion bytes->int via ByteBuffer LE.
	 *
	 * @return liste de Match (peut être vide si pas de géométrie pour cette paire)
	 */
	public List<Match> readVerifiedMatches(long imageId1, long imageId2) throws SQLException {
		ensureOpen();
		long pid = UtilDataBase.pairId(imageId1, imageId2);

		System.out.println("PairId " + pid);
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
			if (rs != null)
				try {
					rs.close();
				} catch (Exception ignore) {
				}
			if (ps != null)
				try {
					ps.close();
				} catch (Exception ignore) {
				}
		}
	}
}