#pragma once

#include "process-metadata.hpp"

class UpdateDatabase {
 public:
  enum class CoordinateSystem : int {
    Local = 0,
    Global = 1,
  };

  static bool upsertPosePrior(
      const char* dbPath,
      const ImageDroneView& view,
      CoordinateSystem coordinateSystem = CoordinateSystem::Global);
};
