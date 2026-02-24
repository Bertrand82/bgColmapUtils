#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "COLMAP - Reconstruction 3D optimisee"
echo "========================================"
echo
source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"
echo process only sparse/0 TODO process all

docker run --rm \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap model_converter \
    --input_path /data/sparse/0 \
    --output_path /data/sparse/0/points3D.ply \
    --output_type PLY

echo
echo "========================================"
echo Traitement termine colmap model_converter .PLY
echo "========================================"
read -rp "Appuyez sur Entrée pour continuer..."