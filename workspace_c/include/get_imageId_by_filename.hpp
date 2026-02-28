#pragma once

#include <optional>

std::optional<int> get_imageId_by_filename(const char* dbPath, const char* fileName);
