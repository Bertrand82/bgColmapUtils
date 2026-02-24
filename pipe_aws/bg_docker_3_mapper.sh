#!/usr/bin/env bash
set -euo pipefail

echo "======== mapper ================================"

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"


# S'assure que /data/sparse existe côté host
mkdir -p "${OUTPUT_DIR}/sparse"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap mapper \
    --database_path /data/database.db \
    --image_path /data/images \
    --output_path /data/sparse

echo
echo "========================================"
echo "Traitement termine colmap mapper
echo "========================================"
