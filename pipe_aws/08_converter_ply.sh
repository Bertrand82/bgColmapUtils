#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "COLMAP - Reconstruction 3D optimisee"
echo "========================================"
echo
source ./bgInitConfig.sh
echo "============================================================ 4 Convert result ================="
echo "OUTPUT_DIR:  $OUTPUT_DIR"
START_EPOCH=$(date +%s)
BASE="/$OUTPUT_DIR/sparse"
docker run --rm \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap model_converter \
    --input_path /data/sparse/${MERGED} \
    --output_path /data/sparse/${MERGED}/points3D.ply \
    --output_type PLY
echo "Terminé:  dossier ${MERGED} ."
i=0
# Boucle sur le NOM de chaque sous-répertoire (niveau 1) de $BASE
# Ex: si $BASE=/data/sparse et qu'il contient /data/sparse/0 /data/sparse/1 /data/sparse/merged
# alors subdir_name prendra: 0, 1, merged
# Niveau 1 sous $BASE, en ignorant "." et ".." (au cas où)
for dir in "$BASE"/*/; do
  [ -d "$dir" ] || break

  subdir_name="$(basename "${dir%/}")"
  [[ "$subdir_name" == "." || "$subdir_name" == ".." || "$subdir_name" == "model" ]] && continue

#---------------------
  echo "------dir--->$subdir_name"
  ls -l ${OUTPUT_DIR}/sparse/${subdir_name}
	

docker run --rm \
  -v "${OUTPUT_DIR}:/data" \
  colmap/colmap \
  colmap model_converter \
    --input_path /data/sparse/${subdir_name} \
    --output_path /data/sparse/${subdir_name}/points3D.ply \
    --output_type PLY

  echo "Terminé: dossier ${BASE}/$subdir_name ."
  i=$((i+1))
mkdir -p  ${OUTPUT_DIR}/result
cp ${OUTPUT_DIR}/sparse/${subdir_name}/points3D.ply ${OUTPUT_DIR}/result/points3D_${subdir_name}.ply   
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
