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
MERGED="merged_${i}_${j}"
export MERGED
mkdir -p ${OUTPUT_DIR}/sparse/${MERGED}
echo "-------------------->start merging"
docker run --rm \
  --memory="12g" \
  --memory-swap="16g" \
  --shm-size="4g" \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  -v "${OUTPUT_DIR}:/output" \
  colmap/colmap \
  colmap  model_merger \
    --input_path1 /output/sparse/$i \
    --input_path2 /output/sparse/$j \
    --output_path /output/sparse/$MERGED \
    --max_reproj_error 64

echo "--------> merge done  $MERGED "
echo "---------> start converting"
 docker run --rm \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  -v "${OUTPUT_DIR}:/output" \
  colmap/colmap \
  colmap model_converter \
    --input_path /output/sparse/${MERGED} \
    --output_path /output/sparse/${MERGED} \
    --output_type TXT

  
  echo "convert done : dossier  $MERGED">>$LOG
  export BASE="$OUTPUT_DIR/sparse"
  wc -l ${BASE}/${MERGED}/images.txt>>$LOG
  echo "Terminé: dossier  $MERGED  ">>$LOG
  wc -l ${BASE}/${MERGED}/images.txt>>$LOG
    
echo
echo "========================================"
echo "Process colmap model_merger done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN model_merger : $DURATION_MIN" >> $LOG

