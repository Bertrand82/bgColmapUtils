#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo " COLMAP - feature_extractor  bg 01"
echo "========================================"
echo

WS="$(pwd)/colmap_project"

# Crée les dossiers
mkdir -p "${WS}/sparse/prior/bg"

echo "========  feature_extractor ================================"

colmap feature_extractor \
  --database_path "${WS}/database.db" \
  --image_path "${WS}/images" \
  --ImageReader.single_camera 1 \
  --ImageReader.camera_model SIMPLE_RADIAL \
  --SiftExtraction.use_gpu 0