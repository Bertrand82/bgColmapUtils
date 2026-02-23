#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo " COLMAP - feature_extractor  bg 01"
echo "========================================"
echo

# Dossier courant (équivalent %CD% sous Windows)
CWD="$(pwd)"
WS="${CWD}/colmap_project"

# Crée les dossiers 
mkdir -p "${WS}/sparse/prior/bg"

echo "========  feature_extractor ================================"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${WS}:/data" \
  colmap/colmap \
  colmap feature_extractor \
    --database_path /data/database.db \
    --image_path /data/images \
    --ImageReader.single_camera 1 \
    --ImageReader.camera_model SIMPLE_RADIAL \
    --FeatureExtraction.use_gpu 0

