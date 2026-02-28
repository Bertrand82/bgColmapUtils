#pragma once

#include <chrono>
#include <optional>
#include <string>
#include <vector>

struct ImageDroneView {
  std::string fileName;
  double x{}, y{}, z{}, yaw{}, pitch{}, roll{};
  int numeroSequence{-1};
  std::optional<std::chrono::system_clock::time_point> date;

  explicit ImageDroneView(const std::string& line);
  std::string toString() const;
};

std::vector<ImageDroneView> processMetadataOutputLines(int argc, char** argv);
std::vector<ImageDroneView> processMetadataOutputLines(const char* csvPath);
