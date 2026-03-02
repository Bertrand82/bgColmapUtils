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
export MERGED2="merged2"
mkdir -p ${OUTPUT_DIR}/sparse/${MERGED2}
cp -a ${OUTPUT_DIR}/sparse/0/. /${OUTPUT_DIR}/sparse/$MERGED2/
while [ -d "${BASE}/${j}" ]; do
echo "=== Processing ${BASE}/${i} ==="
MERGED_OUT="merged_out__${i}_${j}"

export MERGED_OUT
mkdir -p ${OUTPUT_DIR}/sparse/${MERGED_OUT}

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
    --input_path1 /output/sparse/$MERGED2 \
    --input_path2 /output/sparse/$j \
    --output_path /output/sparse/$MERGED_OUT \
    --max_reproj_error 64

echo "--------> merge done  $MERGED_OUT "
echo "---------> start converting"
 docker run --rm \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  -v "${OUTPUT_DIR}:/output" \
  colmap/colmap \
  colmap model_converter \
    --input_path /output/sparse/${MERGED_OUT} \
    --output_path /output/sparse/${MERGED_OUT} \
    --output_type TXT

  
  echo "convert done : dossier  $MERGED_">>$LOG
  export BASE="$OUTPUT_DIR/sparse"
  wc -l ${BASE}/${MERGED_OUT}/images.txt>>$LOG
  echo "Terminé: dossier  $MERGED_OUT  ">>$LOG
  wc -l ${BASE}/${MERGED_OUT}/images.txt>>$LOG
  cp -a ${OUTPUT_DIR}/sparse/${MERGED_OUT}/. /${OUTPUT_DIR}/sparse/$MERGED2/
# rm -rf ${OUTPUT_DIR}/sparse/${MERGED_OUT}
  i=$((i+1))
  j=$((j+1))
 done   
echo
echo "========================================"
echo "Process colmap model_merger done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN model_merger : $DURATION_MIN" >> $LOG

