#pragma once

#include "PoseRow.h"

#include <string>
#include <vector>

std::vector<PoseRow> ParseMetadataCsv(const std::string& csv_path);
