#!/usr/bin/env bash
set -euo pipefail

echo "======== mapper =============================================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> $LOG
echo "===================================================Mapper =========================">>$LOG
echo " COLMAP_MATCHER :$COLMAP_MATCHER"
# #exhaustive_matcher COLMAP_MATCHER, sequential_matcher , spatial_matcher , vocab_tree_matcher , transitive_matcher

# S'assure que /data/sparse existe côté host
mkdir -p "${OUTPUT_DIR}/sparse"

docker run --rm \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
  colmap mapper \
    --image_path /images \
    --database_path /output/database.db \
    --output_path /output/sparse \
    --Mapper.ba_use_gpu 0

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
