#include "update-database.hpp"

#include <sqlite3.h>

#include <array>
#include <iostream>

#include "get_imageId_by_filename.hpp"

bool UpdateDatabase::upsertPosePrior(
    const char* dbPath,
    const ImageDroneView& view,
    CoordinateSystem coordinateSystem) {
  if (!dbPath) {
    std::cerr << "Database path is null\n";
    return false;
  }

  const auto imageId = get_imageId_by_filename(dbPath, view.fileName.c_str());
  if (!imageId) {
    std::cerr << "Image not found for filename: " << view.fileName << "\n";
    return false;
  }

  sqlite3* db = nullptr;
  if (sqlite3_open(dbPath, &db) != SQLITE_OK) {
    std::cerr << "Cannot open database: "
              << (db ? sqlite3_errmsg(db) : "unknown") << "\n";
    if (db) sqlite3_close(db);
    return false;
  }

  const std::array<double, 3> position = {view.x, view.y, view.z};
  const std::array<double, 9> covariance = {
      1.0, 0.0, 0.0,
      0.0, 1.0, 0.0,
      0.0, 0.0, 1.0,
  };

  const char* sql =
      "INSERT INTO pose_priors "
      "(image_id, position, coordinate_system, position_covariance) "
      "VALUES (?, ?, ?, ?) "
      "ON CONFLICT(image_id) DO UPDATE SET "
      "position = excluded.position, "
      "coordinate_system = excluded.coordinate_system, "
      "position_covariance = excluded.position_covariance;";

  sqlite3_stmt* stmt = nullptr;
  if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
    std::cerr << "SQLite prepare error: " << sqlite3_errmsg(db) << "\n";
    sqlite3_close(db);
    return false;
  }

  if (sqlite3_bind_int(stmt, 1, *imageId) != SQLITE_OK ||
      sqlite3_bind_blob(
          stmt,
          2,
          position.data(),
          static_cast<int>(position.size() * sizeof(double)),
          SQLITE_TRANSIENT) != SQLITE_OK ||
      sqlite3_bind_int(stmt, 3, static_cast<int>(coordinateSystem)) != SQLITE_OK ||
      sqlite3_bind_blob(
          stmt,
          4,
          covariance.data(),
          static_cast<int>(covariance.size() * sizeof(double)),
          SQLITE_TRANSIENT) != SQLITE_OK) {
    std::cerr << "SQLite bind error: " << sqlite3_errmsg(db) << "\n";
    sqlite3_finalize(stmt);
    sqlite3_close(db);
    return false;
  }

  const int rc = sqlite3_step(stmt);
  if (rc != SQLITE_DONE) {
    std::cerr << "SQLite step error: " << sqlite3_errmsg(db) << "\n";
    sqlite3_finalize(stmt);
    sqlite3_close(db);
    return false;
  }

  sqlite3_finalize(stmt);
  sqlite3_close(db);
  return true;
}
