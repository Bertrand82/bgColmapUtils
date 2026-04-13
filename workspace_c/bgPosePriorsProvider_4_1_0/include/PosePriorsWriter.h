#pragma once

#include "Database.h"
#include "PoseRow.h"

#include <array>
#include <cstddef>
#include <cstdint>
#include <vector>

struct PosePriorsWriterOptions {
  double sigma = 5.0;
  std::array<double, 3> gravity{0.0, 0.0, -1.0};
  std::int64_t corrSensorId = 1;
  std::int64_t corrSensorType = 0;
  std::int64_t coordinateSystem = 0;
  bool covarianceRowMajor = true;
};

struct PosePriorsWriteStats {
  std::size_t rowsRead = 0;
  std::size_t rowsInserted = 0;
  std::size_t rowsMissingImage = 0;
  std::size_t rowsReplaced = 0;
};

class PosePriorsWriter {
 public:
  PosePriorsWriter(Database& database, PosePriorsWriterOptions options);

  PosePriorsWriteStats Write(const std::vector<PoseRow>& rows);

 private:
  void ValidateForeignKeysOrThrow();
  std::vector<double> BuildPositionBlob(const PoseRow& row) const;
  std::vector<double> BuildPositionCovarianceBlob() const;
  std::vector<double> BuildGravityBlob() const;

  Database& database_;
  PosePriorsWriterOptions options_;
};
