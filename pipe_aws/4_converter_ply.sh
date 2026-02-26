#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "COLMAP - Reconstruction 3D optimisee"
echo "========================================"
echo
source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"
START_EPOCH=$(date +%s)
BASE="/data/images_test/output/sparse"

i=0
while [ -d "${BASE}/${i}" ]; do
	echo "=== Processing sparse/${i} ==="
	

docker run --rm \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap model_converter \
    --input_path /data/sparse/${i} \
    --output_path /data/sparse/${i}/points3D.ply \
    --output_type PLY

  
  i=$((i+1))
  echo "Terminé: aucun dossier ${BASE}/${i} ."
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
