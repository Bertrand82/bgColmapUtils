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
cp -an "${WORK_DIRECTORY}/match.txt" "${OUTPUT_DIR}" 
docker run --rm \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap matches_importer \
    --database_path /data/database.db \
    --match_list_path  /data/match.txt \
    --FeatureMatching.use_gpu 0
	
echo "End docker matcher: ">>$LOG
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Matcher: $DURATION_MIN" >> $LOG
#colmap matches_importer --database_path database.db --match_list_path pairs.txt --SiftMatching.guided_matching 1
