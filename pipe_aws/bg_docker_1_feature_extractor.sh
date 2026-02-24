#!/usr/bin/env bash
set -euo pipefail


echo "========================================"
echo " COLMAP - feature_extractor v bg 02"
echo "========================================"
echo
source ./bgInitConfig.sh
echo "IMAGES_DIR: $IMAGES_DIR"
NB_IMAGES=$(find "$IMAGES_DIR" -maxdepth 1 -type f | wc -l)
echo "Image count: $NB_IMAGES"
echo "OUTPUT_DIR:  $OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "========  feature_extractor ================================"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
   colmap feature_extractor \
    --database_path /output/database.db \
    --image_path /images \
    --ImageReader.single_camera 1 \
    --ImageReader.camera_model SIMPLE_RADIAL \
    --FeatureExtraction.use_gpu 0 \
    --FeatureExtraction.num_threads 1 \
    --SiftExtraction.num_threads 1 \
    --log_level 0