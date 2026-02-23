#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo " COLMAP - feature_extractor  bg 01"
echo "========================================"
echo
ls ../../
REPERTOIRE_IMAGES="$(realpath ../../images_test)"
echo "REPERTOIRE_IMAGES: $REPERTOIRE_IMAGES"
find "$REPERTOIRE_IMAGES" -maxdepth 1 -type f | wc -l

CWD="$(pwd)"
WS="${CWD}/colmap_project"

mkdir -p "${WS}/sparse/prior/bg"

echo "========  feature_extractor ================================"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${WS}:/data" \
  -v "${REPERTOIRE_IMAGES}:/images:ro" \
  colmap/colmap \
  colmap feature_extractor \
    --database_path /data/database.db \
    --image_path /images \
    --ImageReader.single_camera 1 \
    --ImageReader.camera_model SIMPLE_RADIAL \
    --FeatureExtraction.use_gpu 0 \
    --FeatureExtraction.num_threads 1 \
    --log_level 2