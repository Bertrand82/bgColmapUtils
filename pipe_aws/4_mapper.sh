#!/usr/bin/env bash
set -euo pipefail

echo "======== mapper =============================================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> $LOG
echo "===================================================Mapper =========================">>$LOG


# S'assure que /data/sparse existe côté host
mkdir -p "${OUTPUT_DIR}/sparse"

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
  colmap mapper \
    --database_path /output/database.db \
    --image_path /images \
    --output_path /output/sparse

echo
echo "========================================"
echo "Process colmap mapper done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Mapper : $DURATION_MIN" >> $LOG
NB_POSE_PRIORS=$(sqlite3 "$DATABASE_PATH" "SELECT COUNT(*) FROM pose_priors;" 2>/dev/null || echo NA)
echo "NB_POSE_PRIORS=$NB_POSE_PRIORS" >> "$LOG"
