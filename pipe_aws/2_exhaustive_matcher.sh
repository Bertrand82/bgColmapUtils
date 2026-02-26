#!/usr/bin/env bash

set -euo pipefail
echo "========================================"
echo " COLMAP - exhaustive_matcher v bg 01"
echo "========================================"
source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"
echo "COLMAP_MATCHER : $COLMAP_MATCHER"

echo "======== exhaustive_matcher ================================"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap ${COLMAP_MATCHER} \
    --database_path /data/database.db \
    --FeatureMatching.use_gpu 0

