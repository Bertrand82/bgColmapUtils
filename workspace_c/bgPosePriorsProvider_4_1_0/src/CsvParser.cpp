#include "CsvParser.h"

#include <algorithm>
#include <cctype>
#include <cmath>
#include <fstream>
#include <limits>
#include <stdexcept>
#include <string>
#include <vector>

namespace {

std::string Trim(const std::string& input) {
  const auto begin = std::find_if_not(input.begin(), input.end(), [](unsigned char c) {
    return std::isspace(c) != 0;
  });

  if (begin == input.end()) {
    return "";
  }

  const auto end = std::find_if_not(input.rbegin(), input.rend(), [](unsigned char c) {
    return std::isspace(c) != 0;
  }).base();

  return std::string(begin, end);
}

bool IsCommentLine(const std::string& line) {
  if (line.empty()) {
    return false;
  }
  return line[0] == '#' || (line.size() >= 2 && line[0] == '/' && line[1] == '/');
}

std::vector<std::string> SplitCsv7(const std::string& line) {
  std::vector<std::string> fields;
  fields.reserve(7);

  std::size_t start = 0;
  while (start <= line.size()) {
    const std::size_t comma = line.find(',', start);
    if (comma == std::string::npos) {
      fields.push_back(Trim(line.substr(start)));
      break;
    }

    fields.push_back(Trim(line.substr(start, comma - start)));
    start = comma + 1;
  }

  if (fields.size() != 7) {
    throw std::runtime_error("Expected exactly 7 CSV fields");
  }

  return fields;
}

double ParseDoubleOrNan(const std::string& token) {
  std::string lowered;
  lowered.reserve(token.size());
  for (char c : token) {
    lowered.push_back(static_cast<char>(std::tolower(static_cast<unsigned char>(c))));
  }

  if (lowered == "nan" || lowered == "+nan" || lowered == "-nan") {
    return std::numeric_limits<double>::quiet_NaN();
  }

  std::size_t parsed_len = 0;
  double value = 0.0;
  try {
    value = std::stod(token, &parsed_len);
  } catch (const std::exception&) {
    throw std::runtime_error("Invalid numeric token: '" + token + "'");
  }

  if (parsed_len != token.size()) {
    throw std::runtime_error("Invalid numeric token: '" + token + "'");
  }

  return value;
}

}  // namespace

std::vector<PoseRow> ParseMetadataCsv(const std::string& csv_path) {
  std::ifstream input(csv_path);
  if (!input.is_open()) {
    throw std::runtime_error("Cannot open CSV file: " + csv_path);
  }

  std::vector<PoseRow> rows;
  std::string line;
  std::size_t line_number = 0;

  while (std::getline(input, line)) {
    ++line_number;
    const std::string trimmed = Trim(line);

    if (trimmed.empty() || IsCommentLine(trimmed)) {
      continue;
    }

    std::vector<std::string> fields;
    try {
      fields = SplitCsv7(trimmed);
    } catch (const std::exception& e) {
      throw std::runtime_error("CSV parse error at line " + std::to_string(line_number) +
                               ": " + e.what());
    }

    PoseRow row;
    row.imageName = fields[0];
    if (row.imageName.empty()) {
      throw std::runtime_error("CSV parse error at line " + std::to_string(line_number) +
                               ": imageName cannot be empty");
    }

    try {
      row.xx = ParseDoubleOrNan(fields[1]);
      row.yy = ParseDoubleOrNan(fields[2]);
      row.zz = ParseDoubleOrNan(fields[3]);
      row.yaw = ParseDoubleOrNan(fields[4]);
      row.pitch = ParseDoubleOrNan(fields[5]);
      row.roll = ParseDoubleOrNan(fields[6]);
    } catch (const std::exception& e) {
      throw std::runtime_error("CSV parse error at line " + std::to_string(line_number) +
                               ": " + e.what());
    }

    rows.push_back(std::move(row));
  }

  return rows;
}
