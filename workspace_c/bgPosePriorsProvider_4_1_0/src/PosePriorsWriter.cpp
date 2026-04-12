#include "PosePriorsWriter.h"

#include "Statement.h"

#include <Eigen/Dense>

#include <cmath>
#include <optional>
#include <stdexcept>
#include <string>
#include <vector>

namespace {

class TransactionGuard {
 public:
  explicit TransactionGuard(Database& database) : database_(database) {
    Statement begin_stmt(database_, "BEGIN TRANSACTION");
    begin_stmt.Step();
  }

  ~TransactionGuard() {
    if (!committed_) {
      try {
        Statement rollback_stmt(database_, "ROLLBACK");
        rollback_stmt.Step();
      } catch (...) {
      }
    }
  }

  void Commit() {
    Statement commit_stmt(database_, "COMMIT");
    commit_stmt.Step();
    committed_ = true;
  }

 private:
  Database& database_;
  bool committed_ = false;
};

std::optional<std::int64_t> ResolveImageId(Statement& select_stmt, const std::string& image_name) {
  select_stmt.Reset();
  select_stmt.ClearBindings();
  select_stmt.BindText(1, image_name);
  if (select_stmt.Step()) {
    return static_cast<std::int64_t>(select_stmt.ColumnInt64(0));
  }
  return std::nullopt;
}

}  // namespace

PosePriorsWriter::PosePriorsWriter(Database& database, PosePriorsWriterOptions options)
    : database_(database), options_(options) {
  if (options_.sigma <= 0.0) {
    throw std::runtime_error("sigma must be > 0");
  }
  ValidateForeignKeysOrThrow();
}

PosePriorsWriteStats PosePriorsWriter::Write(const std::vector<PoseRow>& rows) {
  PosePriorsWriteStats stats;
  stats.rowsRead = rows.size();

  Statement lookup_image_stmt(database_, "SELECT image_id FROM images WHERE name = ?1");
  Statement check_existing_stmt(database_, "SELECT COUNT(*) FROM pose_priors WHERE corr_data_id = ?1");
  Statement delete_existing_stmt(database_, "DELETE FROM pose_priors WHERE corr_data_id = ?1");
  Statement insert_stmt(database_,
                        "INSERT INTO pose_priors ("
                        "corr_data_id, corr_sensor_id, corr_sensor_type, "
                        "position, position_covariance, gravity, coordinate_system"
                        ") VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)");

  const std::vector<double> covariance_blob = BuildPositionCovarianceBlob();
  const std::vector<double> gravity_blob = BuildGravityBlob();

  TransactionGuard tx(database_);

  for (const PoseRow& row : rows) {
    const std::optional<std::int64_t> image_id = ResolveImageId(lookup_image_stmt, row.imageName);
    if (!image_id.has_value()) {
      ++stats.rowsMissingImage;
      continue;
    }

    check_existing_stmt.Reset();
    check_existing_stmt.ClearBindings();
    check_existing_stmt.BindInt64(1, *image_id);
    long long existing_count = 0;
    if (check_existing_stmt.Step()) {
      existing_count = check_existing_stmt.ColumnInt64(0);
    }

    delete_existing_stmt.Reset();
    delete_existing_stmt.ClearBindings();
    delete_existing_stmt.BindInt64(1, *image_id);
    delete_existing_stmt.Step();

    if (existing_count > 0) {
      ++stats.rowsReplaced;
    }

    insert_stmt.Reset();
    insert_stmt.ClearBindings();
    insert_stmt.BindInt64(1, *image_id);
    insert_stmt.BindInt64(2, options_.corrSensorId);
    insert_stmt.BindInt64(3, options_.corrSensorType);
    insert_stmt.BindBlob(4, BuildPositionBlob(row));
    insert_stmt.BindBlob(5, covariance_blob);
    insert_stmt.BindBlob(6, gravity_blob);
    insert_stmt.BindInt64(7, options_.coordinateSystem);
    insert_stmt.Step();

    ++stats.rowsInserted;
  }

  tx.Commit();
  return stats;
}

void PosePriorsWriter::ValidateForeignKeysOrThrow() {
  Statement fk_stmt(database_, "PRAGMA foreign_key_list(pose_priors)");

  while (fk_stmt.Step()) {
    const std::string from_column = fk_stmt.ColumnText(3);

    if (from_column == "corr_sensor_id" || from_column == "corr_sensor_type" ||
        from_column == "coordinate_system") {
      throw std::runtime_error(
          "pose_priors has FK constraints on sensor/coordinate fields. "
          "This tool does not auto-create referenced rows; please prepare referenced tables "
          "or remove these constraints.");
    }
  }
}

std::vector<double> PosePriorsWriter::BuildPositionBlob(const PoseRow& row) const {
  return {row.xx, row.yy, row.zz};
}

std::vector<double> PosePriorsWriter::BuildPositionCovarianceBlob() const {
  const double sigma2 = options_.sigma * options_.sigma;
  const Eigen::Matrix3d covariance = sigma2 * Eigen::Matrix3d::Identity();

  std::vector<double> blob(9, 0.0);
  if (options_.covarianceRowMajor) {
    int index = 0;
    for (int r = 0; r < 3; ++r) {
      for (int c = 0; c < 3; ++c) {
        blob[static_cast<std::size_t>(index++)] = covariance(r, c);
      }
    }
  } else {
    int index = 0;
    for (int c = 0; c < 3; ++c) {
      for (int r = 0; r < 3; ++r) {
        blob[static_cast<std::size_t>(index++)] = covariance(r, c);
      }
    }
  }

  return blob;
}

std::vector<double> PosePriorsWriter::BuildGravityBlob() const {
  return {options_.gravity[0], options_.gravity[1], options_.gravity[2]};
}
