#pragma once

#include <string>

struct PoseRow {
  std::string imageName;
  double xx;
  double yy;
  double zz;
  double yaw;
  double pitch;
  double roll;
};
