cmake_minimum_required(VERSION 3.20)
project(metadata2database LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_executable(metadata2database
  src/process-metadata.cpp
  src/get_imageId_by_filename.cpp
  src/list_images.cpp
  src/update-database.cpp
  src/main.cpp
)

target_include_directories(metadata2database PRIVATE include)

# --- SQLite3 ---
find_package(SQLite3 REQUIRED)
target_link_libraries(metadata2database PRIVATE SQLite::SQLite3)