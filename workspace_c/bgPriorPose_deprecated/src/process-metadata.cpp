// g++ -std=c++20 -O2 process_csv_min.cpp -o process_csv_min
// ./process_csv_min              (lit ../data-test/metadata.csv)
// ./process_csv_min path/to.csv  (optionnel)

#include <filesystem>
#include <fstream>
#include <iomanip>
#include <regex>
#include <sstream>
#include <string>
#include <vector>

#include "process-metadata.hpp"

namespace fs = std::filesystem;

ImageDroneView::ImageDroneView(const std::string& line) {
  std::stringstream ss(line);
  std::string t[7];
  for (int i = 0; i < 7; ++i) std::getline(ss, t[i], ',');
  fileName = t[0];
  x = std::stod(t[1]); y = std::stod(t[2]); z = std::stod(t[3]);
  yaw = std::stod(t[4]); pitch = std::stod(t[5]); roll = std::stod(t[6]);

  std::smatch m;
  if (std::regex_match(fileName, m, std::regex(R"(^DJI_\d{14}_(\d+)_.*$)", std::regex::icase)))
    numeroSequence = std::stoi(m[1].str());

  if (std::regex_search(fileName, m, std::regex(R"((\d{14}))"))) {
    std::tm tm{};
    auto s = m[1].str();
    tm.tm_year = std::stoi(s.substr(0, 4)) - 1900;
    tm.tm_mon  = std::stoi(s.substr(4, 2)) - 1;
    tm.tm_mday = std::stoi(s.substr(6, 2));
    tm.tm_hour = std::stoi(s.substr(8, 2));
    tm.tm_min  = std::stoi(s.substr(10, 2));
    tm.tm_sec  = std::stoi(s.substr(12, 2));
    date = std::chrono::system_clock::from_time_t(std::mktime(&tm));
  }
}

std::string ImageDroneView::toString() const {
  auto f = [](double v) { std::ostringstream o; o << std::fixed << std::setw(6) << std::setprecision(1) << v; return o.str(); };
  std::ostringstream o;
  o << " | fileName=" << fileName
    << "| x=" << f(x) << "| y=" << f(y) << "| z=" << f(z)
    << "| yaw=" << f(yaw) << "| pitch=" << f(pitch) << "| roll=" << f(roll)
    << "| numeroSequence=" << numeroSequence << "| date=" << (date ? "ok" : "null") << "]";
  return o.str();
}

struct ProcessCsv {
  std::vector<ImageDroneView> list;
  explicit ProcessCsv(const fs::path& f) {
    std::ifstream in(f);
    for (std::string line; std::getline(in, line);)
      if (!line.empty()) list.emplace_back(line);
  }
  const ImageDroneView* getImageDroneView(const std::string& name) const {
    for (auto& v : list) if (v.fileName == name) return &v;
    return nullptr;
  }
};

std::vector<ImageDroneView> processMetadataOutputLines(const char* csvPath) {
  fs::path csv = csvPath ? fs::path(csvPath) : fs::path("../data-test/metadata.csv");
  ProcessCsv p(csv);
  return p.list;
}
