#!/usr/bin/env bash
set -euo pipefail

CWD="$(pwd)"
WS="${CWD}/colmap_project"

echo "======== exhaustive_matcher ================================"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${WS}:/data" \
  colmap/colmap \
  colmap exhaustive_matcher \
    --database_path /data/database.db \
    --FeatureMatching.use_gpu 0

