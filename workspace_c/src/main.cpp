#include <filesystem>
#include <iostream>
#include <string_view>

#include "get_imageId_by_filename.hpp"
#include "list_images.hpp"
#include "process-metadata.hpp"
#include "update-database.hpp"

static bool fileExists(const char* dbPath) {
  if (!dbPath) {
    return false;
  }

  const std::filesystem::path path(dbPath);
  return std::filesystem::exists(path) && std::filesystem::is_regular_file(path);
}

static void printUsage(const char* programName) {
  std::cout << "Usage: " << programName << " [metadata_csv] [database_db]\n"
            << "\n"
            << "Arguments:\n"
            << "  metadata_csv  Path to metadata CSV file (default: data_test/metadata.csv)\n"
            << "  database_db   Path to COLMAP database file (default: data_test/database.db)\n"
            << "\n"
            << "Options:\n"
            << "  -h, --help    Show this help message\n";
}

int main(int argc, char** argv) {
  for (int i = 1; i < argc; ++i) {
    const std::string_view arg(argv[i]);
    if (arg == "--help" || arg == "-h") {
      printUsage(argv[0]);
      return 0;
    }
  }

  const char* csvPath = (argc >= 2) ? argv[1] : "data_test/metadata.csv";
  const char* dbPath = (argc >= 3) ? argv[2] : "data_test/database.db";
   std::cout << "Database absolute path: " << dbPath  << "\n";

  if (!fileExists(dbPath)) {
    std::cerr << "Database file not found: " << dbPath << "\n";
    return 1;
  }

  list_images(dbPath);
  std::cout << "-----------------------------\n";
  const auto views = processMetadataOutputLines(csvPath );
  std::cout << "Metadata path: "<< csvPath <<" | exists "<<fileExists(csvPath)   <<" | size: " << views.size() << "\n";
  for (const auto& view : views) {
    const bool updated = UpdateDatabase::upsertPosePrior(
        dbPath,
        view,
        UpdateDatabase::CoordinateSystem::Global);

    const auto imageId = get_imageId_by_filename(dbPath, view.fileName.c_str());
    if (imageId) {
      std::cout << view.fileName << " | " << *imageId << " | x :" << view.x << " | y: "  << view.y << " | z: " << view.z
                << " | pose_priors: " << (updated ? "upserted" : "failed") << "\n";
    }
    else {
      std::cout << view.fileName << " | not found\n";
    }
  } 
  return 0;
}