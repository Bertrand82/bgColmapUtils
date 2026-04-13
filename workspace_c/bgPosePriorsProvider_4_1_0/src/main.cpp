#include "CsvParser.h"
#include "Database.h"
#include "PosePriorsWriter.h"
#include "Statement.h"

#include <array>
#include <exception>
#include <iostream>
#include <optional>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

namespace {

constexpr int kExitSuccess = 0;
constexpr int kExitUsageError = 1;
constexpr int kExitRuntimeError = 2;
constexpr int kExitCheckMismatch = 3;

void PrintUsage(const char* program) {
  std::cerr << "Usage:\n"
            << "  " << program << " --inspect <dbPath>\n"
            << "  " << program << " --dry-run <dbPath> <csvPath>\n"
            << "  " << program << " --check <dbPath> <csvPath>\n"
            << "  " << program
            << " --write <dbPath> <csvPath> [--sigma 5.0] [--gravity 0,0,-1]"
               " [--corr-sensor-id 1] [--corr-sensor-type 0]"
               " [--coordinate-system 0] [--cov-order row|col]\n";
}

std::int64_t ParseInt64OrThrow(const std::string& value, const std::string& option_name) {
  std::size_t parsed_len = 0;
  long long parsed = 0;
  try {
    parsed = std::stoll(value, &parsed_len);
  } catch (const std::exception&) {
    throw std::runtime_error("Invalid integer for " + option_name + ": " + value);
  }
  if (parsed_len != value.size()) {
    throw std::runtime_error("Invalid integer for " + option_name + ": " + value);
  }
  return static_cast<std::int64_t>(parsed);
}

double ParseDoubleOrThrow(const std::string& value, const std::string& option_name) {
  std::size_t parsed_len = 0;
  double parsed = 0.0;
  try {
    parsed = std::stod(value, &parsed_len);
  } catch (const std::exception&) {
    throw std::runtime_error("Invalid double for " + option_name + ": " + value);
  }
  if (parsed_len != value.size()) {
    throw std::runtime_error("Invalid double for " + option_name + ": " + value);
  }
  return parsed;
}

std::array<double, 3> ParseGravityOrThrow(const std::string& value) {
  std::array<double, 3> gravity{};
  std::stringstream ss(value);
  std::string token;
  int index = 0;

  while (std::getline(ss, token, ',')) {
    if (index >= 3) {
      throw std::runtime_error("--gravity expects exactly 3 comma-separated values");
    }
    gravity[static_cast<std::size_t>(index)] = ParseDoubleOrThrow(token, "--gravity");
    ++index;
  }

  if (index != 3) {
    throw std::runtime_error("--gravity expects exactly 3 comma-separated values");
  }

  return gravity;
}

PosePriorsWriterOptions ParseWriterOptionsOrThrow(int argc, char** argv, int start_index) {
  PosePriorsWriterOptions options;

  for (int i = start_index; i < argc; ++i) {
    const std::string arg = argv[i];
    if (arg == "--sigma") {
      if (i + 1 >= argc) {
        throw std::runtime_error("Missing value for --sigma");
      }
      options.sigma = ParseDoubleOrThrow(argv[++i], "--sigma");
    } else if (arg == "--gravity") {
      if (i + 1 >= argc) {
        throw std::runtime_error("Missing value for --gravity");
      }
      options.gravity = ParseGravityOrThrow(argv[++i]);
    } else if (arg == "--corr-sensor-id") {
      if (i + 1 >= argc) {
        throw std::runtime_error("Missing value for --corr-sensor-id");
      }
      options.corrSensorId = ParseInt64OrThrow(argv[++i], "--corr-sensor-id");
    } else if (arg == "--corr-sensor-type") {
      if (i + 1 >= argc) {
        throw std::runtime_error("Missing value for --corr-sensor-type");
      }
      options.corrSensorType = ParseInt64OrThrow(argv[++i], "--corr-sensor-type");
    } else if (arg == "--coordinate-system") {
      if (i + 1 >= argc) {
        throw std::runtime_error("Missing value for --coordinate-system");
      }
      options.coordinateSystem = ParseInt64OrThrow(argv[++i], "--coordinate-system");
    } else if (arg == "--cov-order") {
      if (i + 1 >= argc) {
        throw std::runtime_error("Missing value for --cov-order");
      }
      const std::string mode = argv[++i];
      if (mode == "row") {
        options.covarianceRowMajor = true;
      } else if (mode == "col") {
        options.covarianceRowMajor = false;
      } else {
        throw std::runtime_error("--cov-order must be 'row' or 'col'");
      }
    } else {
      throw std::runtime_error("Unknown option: " + arg);
    }
  }

  return options;
}

std::optional<long long> GetImageIdByName(Statement& select_stmt, const std::string& image_name) {
  select_stmt.Reset();
  select_stmt.ClearBindings();
  select_stmt.BindText(1, image_name);

  if (select_stmt.Step()) {
    return select_stmt.ColumnInt64(0);
  }

  return std::nullopt;
}

void InspectDatabase(const std::string& db_path) {
  Database db(db_path);

  std::cout << "[inspect] sqlite_master entries for images and pose_priors\n";
  Statement master_stmt(
      db,
      "SELECT type, name, tbl_name, sql "
      "FROM sqlite_master "
      "WHERE type='table' AND name IN ('images','pose_priors') "
      "ORDER BY name");

  int found = 0;
  while (master_stmt.Step()) {
    ++found;
    std::cout << "----------------------------------------\n";
    std::cout << "type: " << master_stmt.ColumnText(0) << "\n";
    std::cout << "name: " << master_stmt.ColumnText(1) << "\n";
    std::cout << "table: " << master_stmt.ColumnText(2) << "\n";
    std::cout << "sql: " << master_stmt.ColumnText(3) << "\n";
  }

  if (found == 0) {
    std::cout << "No images/pose_priors table found in sqlite_master.\n";
  }

  for (const std::string& table_name : {std::string("images"), std::string("pose_priors")}) {
    std::cout << "\n[inspect] PRAGMA table_info(" << table_name << ")\n";
    Statement pragma_stmt(db, "PRAGMA table_info(" + table_name + ")");

    bool has_rows = false;
    while (pragma_stmt.Step()) {
      has_rows = true;
      const char* cid = pragma_stmt.ColumnText(0);
      const char* name = pragma_stmt.ColumnText(1);
      const char* type = pragma_stmt.ColumnText(2);
      const char* not_null = pragma_stmt.ColumnText(3);
      const char* default_value = pragma_stmt.ColumnText(4);
      const char* pk = pragma_stmt.ColumnText(5);

      std::cout << "  cid=" << cid << ", name=" << name << ", type=" << type
                << ", notnull=" << not_null << ", dflt_value=" << default_value
                << ", pk=" << pk << "\n";
    }

    if (!has_rows) {
      std::cout << "  (table missing or empty schema)\n";
    }
  }
}

void DryRunResolveImageIds(const std::string& db_path, const std::string& csv_path) {
  const std::vector<PoseRow> rows = ParseMetadataCsv(csv_path);
  Database db(db_path);
  Statement lookup_stmt(db, "SELECT image_id FROM images WHERE name = ?1");

  std::size_t found_count = 0;
  std::size_t missing_count = 0;

  for (const PoseRow& row : rows) {
    const std::optional<long long> image_id = GetImageIdByName(lookup_stmt, row.imageName);
    if (image_id.has_value()) {
      ++found_count;
    } else {
      ++missing_count;
      std::cout << "[dry-run] image not found: " << row.imageName << "\n";
    }
  }

  std::cout << "[dry-run] summary\n";
  std::cout << "  rows read: " << rows.size() << "\n";
  std::cout << "  images found: " << found_count << "\n";
  std::cout << "  images not found: " << missing_count << "\n";
}

void WritePosePriors(const std::string& db_path,
                     const std::string& csv_path,
                     const PosePriorsWriterOptions& options) {
  const std::vector<PoseRow> rows = ParseMetadataCsv(csv_path);
  Database db(db_path);
  PosePriorsWriter writer(db, options);
  const PosePriorsWriteStats stats = writer.Write(rows);

  std::cout << "[write] config\n";
  std::cout << "  sigma: " << options.sigma << "\n";
  std::cout << "  gravity: " << options.gravity[0] << "," << options.gravity[1] << ","
            << options.gravity[2] << "\n";
  std::cout << "  corr_sensor_id: " << options.corrSensorId << "\n";
  std::cout << "  corr_sensor_type: " << options.corrSensorType << "\n";
  std::cout << "  coordinate_system: " << options.coordinateSystem << "\n";
  std::cout << "  covariance order: " << (options.covarianceRowMajor ? "row" : "col") << "\n";

  std::cout << "[write] summary\n";
  std::cout << "  rows read: " << stats.rowsRead << "\n";
  std::cout << "  rows inserted: " << stats.rowsInserted << "\n";
  std::cout << "  rows replaced: " << stats.rowsReplaced << "\n";
  std::cout << "  rows missing image: " << stats.rowsMissingImage << "\n";
}

bool CheckPosePriorsForCsv(const std::string& db_path, const std::string& csv_path) {
  const std::vector<PoseRow> rows = ParseMetadataCsv(csv_path);
  Database db(db_path);

  Statement lookup_stmt(db, "SELECT image_id FROM images WHERE name = ?1");
  Statement count_stmt(db, "SELECT COUNT(*) FROM pose_priors WHERE corr_data_id = ?1");

  std::size_t rows_missing_image = 0;
  std::size_t rows_with_pose_prior = 0;
  std::size_t rows_without_pose_prior = 0;
  std::size_t total_pose_prior_rows_for_treated_images = 0;

  for (const PoseRow& row : rows) {
    const std::optional<long long> image_id = GetImageIdByName(lookup_stmt, row.imageName);
    if (!image_id.has_value()) {
      ++rows_missing_image;
      continue;
    }

    count_stmt.Reset();
    count_stmt.ClearBindings();
    count_stmt.BindInt64(1, *image_id);

    long long pose_count = 0;
    if (count_stmt.Step()) {
      pose_count = count_stmt.ColumnInt64(0);
    }

    total_pose_prior_rows_for_treated_images += static_cast<std::size_t>(pose_count);
    if (pose_count > 0) {
      ++rows_with_pose_prior;
    } else {
      ++rows_without_pose_prior;
      std::cout << "[check] missing pose_prior for image: " << row.imageName << "\n";
    }
  }

  std::cout << "[check] summary\n";
  std::cout << "  rows read: " << rows.size() << "\n";
  std::cout << "  rows missing image: " << rows_missing_image << "\n";
  std::cout << "  treated images (found in images): " << (rows.size() - rows_missing_image) << "\n";
  std::cout << "  treated images with >=1 pose_prior: " << rows_with_pose_prior << "\n";
  std::cout << "  treated images with 0 pose_prior: " << rows_without_pose_prior << "\n";
  std::cout << "  pose_priors rows for treated images (SELECT COUNT(*)): "
            << total_pose_prior_rows_for_treated_images << "\n";

  return rows_without_pose_prior == 0;
}

}  // namespace

int main(int argc, char** argv) {
  try {
    if (argc == 3 && std::string(argv[1]) == "--inspect") {
      InspectDatabase(argv[2]);
      return kExitSuccess;
    }

    if (argc == 4 && std::string(argv[1]) == "--dry-run") {
      DryRunResolveImageIds(argv[2], argv[3]);
      return kExitSuccess;
    }

    if (argc == 4 && std::string(argv[1]) == "--check") {
      const bool ok = CheckPosePriorsForCsv(argv[2], argv[3]);
      return ok ? kExitSuccess : kExitCheckMismatch;
    }

    if (argc >= 4 && std::string(argv[1]) == "--write") {
      const PosePriorsWriterOptions options = ParseWriterOptionsOrThrow(argc, argv, 4);
      WritePosePriors(argv[2], argv[3], options);
      return kExitSuccess;
    }

    PrintUsage(argv[0]);
    return kExitUsageError;
  } catch (const std::exception& e) {
    std::cerr << "Error: " << e.what() << "\n";
    return kExitRuntimeError;
  }
}
