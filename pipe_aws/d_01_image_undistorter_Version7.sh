#!/usr/bin/env bash
set -euo pipefail

echo "======== dense step 01: image_undistorter ==================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> "$LOG"
echo "===================================================Dense 01 image_undistorter" >> "$LOG"

# Sparse model source: by convention COLMAP writes to /output/sparse/0
SPARSE_MODEL_DIR="${OUTPUT_DIR}/sparse/0"
DENSE_DIR="${OUTPUT_DIR}/dense"

if [[ ! -d "$SPARSE_MODEL_DIR" ]]; then
  echo "ERROR: sparse model not found at: $SPARSE_MODEL_DIR" | tee -a "$LOG"
  echo "Expected files: cameras.bin/images.bin/points3D.bin" | tee -a "$LOG"
  exit 1
fi

mkdir -p "$DENSE_DIR"

docker run --rm \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
  colmap image_undistorter \
    --image_path /images \
    --input_path /output/sparse/0 \
    --output_path /output/dense \
    --output_type COLMAP

echo
echo "========================================"
echo "Process colmap image_undistorter done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))
echo "DURATION_MN Dense_01_image_undistorter : $DURATION_MIN" >> "$LOG"