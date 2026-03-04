#!/usr/bin/env bash
set -euo pipefail


echo "========================================"
echo " COLMAP - patch_match_stereo"
echo "========================================"
echo
source ./bgInitConfig.sh
mkdir -p "$OUTPUT_DIR"
touch $LOG
echo "================= patch_match_stereo =====================================================">>$LOG
echo "Start : $(date '+%Y-%m-%d %H:%M:%S')  " >> $LOG
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
echo "Nb nombre de CPU logiques visibles : $(nproc)"

echo "========  patch_match_stereo ================================"
touch $LOG
echo "$(date '+%Y-%m-%d %H:%M:%S') -  start patch_match_stereo  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" >> $LOG
START_EPOCH=$(date +%s)

docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
   colmap patch_match_stereo \
    --workspace_path /output/dense \
    --workspace_format COLMAP \
    --PatchMatchStereo.geom_consistency true    
    --log_level 0

echo "End patch_match_stereo: ">>$LOG
END_EPOCH=$(date +%s)
DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN patch_match_stereo (minute): $DURATION_MIN" >> $LOG
NB_IMAGES_DB=$(sqlite3 "$DATABASE_PATH" "SELECT COUNT(*) FROM images;")
echo "NB_IMAGES_DB : $NB_IMAGES_DB" >> $LOG
DUREE_EXTRACTION_MOYENNE=$(( DURATION_SEC / NB_IMAGES_DB ))
echo "Duree moyenne patch_match_stereo (seconde) : $DUREE_EXTRACTION_MOYENNE" >> $LOG

