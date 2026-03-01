#!/usr/bin/env bash

set -euo pipefail
echo "========================================"
echo " COLMAP - exhaustive_matcher v bg 01"
echo "========================================"
source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"
echo "COLMAP_MATCHER : $COLMAP_MATCHER"

echo "============================ matcher =====================================================">>$LOG
START_EPOCH=$(date +%s)
mkdir -p "${OUTPUT_DIR}/sparse" 2>/dev/null || true

if [ -d "${OUTPUT_DIR}/model" ]; then
  # Copie le contenu sans écraser ce qui existe déjà
  cp -an "${WORK_DIRECTORY}/model/." "${OUTPUT_DIR}/sparse/" 2>/dev/null || true
fi

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap ${COLMAP_MATCHER} \
    --database_path /data/database.db \
    --FeatureMatching.use_gpu 0
echo "End docker matcher: ">>$LOG
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Matcher: $DURATION_MIN" >> $LOG

