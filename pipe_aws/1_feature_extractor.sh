#!/usr/bin/env bash
set -euo pipefail


echo "========================================"
echo " COLMAP - feature_extractor v bg 02"
echo "========================================"
echo
source ./bgInitConfig.sh
mkdir -p "$OUTPUT_DIR"
touch $LOG
echo "Début du pipeline feature extraction : $(date '+%Y-%m-%d %H:%M:%S')  " > $LOG
# CPU (modèle), nb threads, RAM dispo (en Mo)
CPU_MODEL=$(lscpu | awk -F: '/Model name/ {gsub(/^[ \t]+/,"",$2); print $2; exit}')
NB_THREADS=$(nproc)
RAM_AVAIL_MB=$(free -m | awk '/Mem:/ {print $7}')

echo "MACHINE: CPU=$CPU_MODEL | THREADS=$NB_THREADS | RAM_AVAIL_MB=${RAM_AVAIL_MB}MB" >> $LOG
echo "NVIDIA_USE_GPU : $NVIDIA_USE_GPU" >> "$LOG"

export NVIDIA_USE_GPU
echo "IMAGES_DIR: $IMAGES_DIR"

NB_IMAGES=$(find "$IMAGES_DIR" -maxdepth 1 -type f | wc -l)
echo "Image count in file :   $NB_IMAGES">>$LOG
echo "OUTPUT_DIR:  $OUTPUT_DIR">>$LOG
echo "DATABASE_NAME : $DATABASE_NAME">>$LOG
echo "DATABASE_PATH : $DATABASE_PATH">>$LOG
echo "LOG        : $LOG"


echo "========  feature_extractor ================================"
touch $LOG
echo "$(date '+%Y-%m-%d %H:%M:%S') -   Début du pipeline feature extraction" > log.txt
START_EPOCH=$(date +%s)




docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
   colmap feature_extractor \
    --database_path /output/database.db \
    --image_path /images \
    --ImageReader.single_camera 1 \
    --ImageReader.camera_model SIMPLE_RADIAL \
    --FeatureExtraction.use_gpu 0 \
    --FeatureExtraction.num_threads 1 \
    --log_level 0

echo "End docker extractor: ">>$LOG
END_EPOCH=$(date +%s)
DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Extractor: $DURATION_MIN" >> $LOG
NB_IMAGES_DB=$(sqlite3 "$DATABASE_PATH" "SELECT COUNT(*) FROM images;")
echo "NB_IMAGES_DB : $NB_IMAGES_DB" >> $LOG
# Table keypoints: (image_id, rows, cols, data). "rows" = nb keypoints pour l'image.
NB_KEYPOINTS_TOTAL="$(sqlite3 "$DATABASE_PATH" "SELECT COALESCE(SUM(rows),0) FROM keypoints;" 2>/dev/null || echo NA)"
echo "NB_KEYPOINTS_TOTAL : $NB_KEYPOINTS_TOTAL">>$LOG
# --- Moyenne keypoints / image (sur images qui ont des keypoints) ---
AVG_KEYPOINTS_PER_IMAGE="$(sqlite3 "$DATABASE_PATH" "
  SELECT CASE
    WHEN COUNT(*)=0 THEN 0
    ELSE ROUND(AVG(rows), 2)
  END
  FROM keypoints;
" 2>/dev/null || echo NA)"

echo "AVG_KEYPOINTS_PER_IMAGE : $AVG_KEYPOINTS_PER_IMAGE" >> $LOG
# NB images dans la DB qui n'ont PAS de keypoints
NB_IMAGES_WITHOUT_KEYPOINTS=$(sqlite3 "$DATABASE_PATH" "
  SELECT COUNT(*)
  FROM images i
  LEFT JOIN keypoints k ON k.image_id = i.image_id
  WHERE k.image_id IS NULL;
" 2>/dev/null || echo NA)

echo "NB_IMAGES_WITHOUT_KEYPOINTS=$NB_IMAGES_WITHOUT_KEYPOINTS" >> "$LOG"
