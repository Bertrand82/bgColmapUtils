CXX ?= g++
CXXFLAGS ?= -std=c++20 -O2 -Wall -Wextra -Iinclude
LDFLAGS ?= -lsqlite3

SRC := src/process-metadata.cpp src/get_imageId_by_filename.cpp src/list_images.cpp src/update-database.cpp src/main.cpp
BUILD_DIR := build
BIN := $(BUILD_DIR)/metadata2database
CSV := data_test/metadata.csv

.PHONY: all run clean

all: $(BIN)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

$(BIN): $(SRC) | $(BUILD_DIR)
	$(CXX) $(CXXFLAGS) $^ -o $@ $(LDFLAGS)

run: $(BIN)
	./$(BIN) $(CSV)

clean:
	rm -f $(BIN)