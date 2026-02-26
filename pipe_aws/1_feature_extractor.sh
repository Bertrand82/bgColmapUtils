#!/usr/bin/env bash
set -euo pipefail


echo "========================================"
echo " COLMAP - feature_extractor v bg 02"
echo "========================================"
echo
source ./bgInitConfig.sh
touch $LOG
echo "$(date '+%Y-%m-%d %H:%M:%S') -   Début du pipeline feature extraction" > $LOG
# CPU (modèle), nb threads, RAM dispo (en Mo)
CPU_MODEL=$(lscpu | awk -F: '/Model name/ {gsub(/^[ \t]+/,"",$2); print $2; exit}')
NB_THREADS=$(nproc)
RAM_AVAIL_MB=$(free -m | awk '/Mem:/ {print $7}')

echo "MACHINE: CPU=$CPU_MODEL | THREADS=$NB_THREADS | RAM_AVAIL_MB=${RAM_AVAIL_MB}MB" >> $LOG
echo "IMAGES_DIR: $IMAGES_DIR"

NB_IMAGES=$(find "$IMAGES_DIR" -maxdepth 1 -type f | wc -l)
echo "Image count in file :   $NB_IMAGES">>$LOG
echo "OUTPUT_DIR:  $OUTPUT_DIR">>$LOG
echo "DATABASE_NAME : $DATABASE_NAME">>$LOG
echo "DATABASE_PATH : $DATABASE_PATH">>$LOG
echo "LOG        : $LOG"
mkdir -p "$OUTPUT_DIR"

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

echo "Fin docker">>$LOG
END_EPOCH=$(date +%s)
DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN = $DURATION_MIN" >> $LOG
NB_IMAGES_DB=$(sqlite3 "$DATABASE_PATH" "SELECT COUNT(*) FROM images;")
echo "NB_IMAGES_DB = $NB_IMAGES_DB" >> $LOG
