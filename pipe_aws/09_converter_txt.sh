#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "COLMAP - conversion bin en txt"
echo "========================================"
echo
source ./bgInitConfig.sh
echo "============================================================ 4 Convert result ================= ">>$LOG
echo "OUTPUT_DIR:  $OUTPUT_DIR"
START_EPOCH=$(date +%s)
BASE="$OUTPUT_DIR/sparse"

i=0
while [ -d "${BASE}/${i}" ]; do
	echo "=== Processing ${BASE}/${i} ==="
	

docker run --rm \
  --user "$(id -u):$(id -g)" \
  --group-add "$(getent group bg_shared | cut -d: -f3)" \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap model_converter \
    --input_path /data/sparse/${i} \
    --output_path /data/sparse/${i} \
    --output_type TXT

  
  echo "Terminé: dossier  ${BASE}/${i} .">>$LOG
  wc -l ${BASE}/${i}/images.txt>>$LOG
  i=$((i+1))
done

echo "Fin Conversion en PLY  ${BASE}   (arrêt)."
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))  # arrondi à la minute supérieure
echo "DURATION_MN Converter: $DURATION_MIN" >> $LOG
echo process only sparse/0 TODO process all


echo "========================================"
echo Traitement termine colmap model_converter .PLY
echo "========================================"
