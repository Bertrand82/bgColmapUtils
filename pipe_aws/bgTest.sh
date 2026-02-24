#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo " test v bg 02"
echo "========================================"
source ./bgInitConfig.sh

echo "IMAGES_DIR: $IMAGES_DIR"
NB_IMAGES=$(find "$IMAGES_DIR" -maxdepth 1 -type f | wc -l)
echo "Image count: $NB_IMAGES"
echo "OUTPUT_DIR:  $OUTPUT_DIR"



docker run --rm \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images:ro" \
  colmap/colmap \
  bash -lc 'ls -lah /images; echo "---"; file /images/* | head -n 20'