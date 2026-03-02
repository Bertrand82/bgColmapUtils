#!/usr/bin/env bash
set -euo pipefail

echo "======== merger =============================================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> $LOG
echo "=================================================== model_merger =========================">>$LOG


# S'assure que /data/sparse existe côté host
mkdir -p "${OUTPUT_DIR}/sparse"
mkdir -p "${OUTPUT_DIR}/sparse/model"
export i=0
export j=1
merged="merged_${i}_${j}"
export merged
mkdir ${OUTPUT_DIR}/sparse/${merged}
docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  -v "${OUTPUT_DIR}:/output" \
  -v "${IMAGES_DIR}:/images" \
  colmap/colmap \
  colmap  model_merger \
    --input_path1 /output/sparse/$i \
    --input_path2 /output/sparse/$j \
    --output_path /output/sparse/$merged \
    --max_reproj_error 64
  colmap model_converter \
    --input_path /data/sparse/$merged \
    --output_path /data/sparse/$merged\
    --output_type TXT

  
  echo "Terminé: dossier  $merged ">>$LOG
  export BASE="$OUTPUT_DIR/sparse"
  wc -l ${BASE}/${merged}/images.txt>>$LOG
    
echo
echo "========================================"
echo "Process colmap model_merger done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN model_merger : $DURATION_MIN" >> $LOG

