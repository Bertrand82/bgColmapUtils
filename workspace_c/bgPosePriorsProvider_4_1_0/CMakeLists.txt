cmake_minimum_required(VERSION 3.16)

project(bgPosePriorsProvider_4_1_0 VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(SQLite3 REQUIRED)
find_package(Eigen3 3.3 REQUIRED NO_MODULE)

add_executable(${PROJECT_NAME}
	src/main.cpp
	src/CsvParser.cpp
	src/Database.cpp
	src/PosePriorsWriter.cpp
	src/Statement.cpp
)

target_include_directories(${PROJECT_NAME}
	PRIVATE
		include
)

target_link_libraries(${PROJECT_NAME}
	PRIVATE
		SQLite::SQLite3
		Eigen3::Eigen
)
