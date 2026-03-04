#!/usr/bin/env bash
set -euo pipefail

echo "======== Geometric_verifier =============================================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> $LOG
echo "===================================================Geometric_verifier =========================">>$LOG


# S'assure que /data/sparse existe côté host
mkdir -p "${OUTPUT_DIR}/sparse"
mkdir -p "${OUTPUT_DIR}/sparse/model"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
  colmap geometric_verifier \
    --database_path /output/database.db \
    --FeatureMatching.use_gpu 0
echo
echo "========================================"
echo "Process colmap geometric_verifier done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Mapper : $DURATION_MIN" >> $LOG

