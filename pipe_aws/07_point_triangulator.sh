#!/usr/bin/env bash
set -euo pipefail


echo "========================================"
echo " COLMAP - point_triangulator v bg 02"
echo "========================================"
echo
source ./bgInitConfig.sh
mkdir -p "$OUTPUT_DIR"
touch $LOG
echo "================= point_triangulator  =====================================================">>$LOG
echo "Start : $(date '+%Y-%m-%d %H:%M:%S')  " >> $LOG
# CPU (modèle), nb threads, RAM dispo (en Mo)
CPU_MODEL=$(lscpu | awk -F: '/Model name/ {gsub(/^[ \t]+/,"",$2); print $2; exit}')
NB_THREADS=$(nproc)
RAM_AVAIL_MB=$(free -m | awk '/Mem:/ {print $7}')

echo "MACHINE: CPU=$CPU_MODEL | THREADS=$NB_THREADS | RAM_AVAIL_MB=${RAM_AVAIL_MB}MB" >> $LOG
echo "NVIDIA_USE_GPU : $NVIDIA_USE_GPU" >> "$LOG"


echo "IMAGES_DIR: $IMAGES_DIR"
echo "MODEL_DIR: $MODEL_DIR"
NB_IMAGES=$(find "$IMAGES_DIR" -maxdepth 1 -type f | wc -l)
echo "Image count in file :   $NB_IMAGES">>$LOG
echo "OUTPUT_DIR:  $OUTPUT_DIR">>$LOG
echo "DATABASE_NAME : $DATABASE_NAME">>$LOG
echo "DATABASE_PATH : $DATABASE_PATH">>$LOG
echo "MODEL_DIR     : $MODEL_DIR">>$LOG
echo "LOG        : $LOG"


echo "========  point_triangulator  ================================">>$LOG
touch $LOG
echo "$(date '+%Y-%m-%d %H:%M:%S') -   Début du pipeline feature extraction" >> $LOG
START_EPOCH=$(date +%s)

mkdir -p  "${OUTPUT_DIR}/manually/sparse/model"
mkdir -p  "${OUTPUT_DIR}/triangulated/sparse/model"
echo Copie le contenu sans écraser ce qui existe déjà>>$LOG
cp -r "${WORK_DIRECTORY}/model/." "${OUTPUT_DIR}/manually/sparse/model" 2>>$LOG || true



docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  --shm-size="4g" \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
   colmap point_triangulator \
    --database_path /output/database.db \
    --image_path /images \
    --input_path /output/sparse/$MERGED \
    --output_path /output/triangulated/sparse/model \
    --log_level 0

echo "End docker extractor: ">>$LOG
END_EPOCH=$(date +%s)
DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Points_Triangulator: $DURATION_MIN" >> $LOG

