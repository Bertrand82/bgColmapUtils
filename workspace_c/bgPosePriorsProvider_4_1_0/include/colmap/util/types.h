#pragma once

#include <cstdint>

namespace colmap {

using pose_prior_t = std::int64_t;
using data_t = std::int64_t;

constexpr pose_prior_t kInvalidPosePriorId = static_cast<pose_prior_t>(-1);
constexpr data_t kInvalidDataId = static_cast<data_t>(-1);

}  // namespace colmap