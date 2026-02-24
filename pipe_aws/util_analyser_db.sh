#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "COLMAP -colmap model_analyzer "
echo "========================================"
echo

source ./bgInitConfig.sh



docker run --rm \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap model_analyzer --path /data/sparse/0

echo
echo "========================================"
echo "Traitement termine colmap model_analyzer --path /data/sparse/0"
echo "========================================"
