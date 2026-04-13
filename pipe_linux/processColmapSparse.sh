#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

COLMAP_CMD="/home/bertrand/workspaceCpp/colmap/build/src/colmap/exe/colmap"

echo "COLMAP binaire utilisé: $COLMAP_CMD"
"$COLMAP_CMD" --version
BG_WORK="${BG_WORK:-/data/BG}"

echo "COLMAP EXTRACTOR"
# "$COLMAP_CMD" feature_extractor --help
echo "xxxxxxxxxxxxxxx ls $BG_WORK"

ls -la "$BG_WORK"
echo "xxxxxx Extraction features xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Extraction des features
"$COLMAP_CMD" feature_extractor \
  --database_path "$BG_WORK/database.db" \
  --image_path "$BG_WORK/images" \
  --ImageReader.single_camera 1 \
  --ImageReader.camera_model SIMPLE_RADIAL \
  --FeatureExtraction.use_gpu 0 \
  --FeatureExtraction.num_threads 1 \
  --log_level 2

echo "xxxxxx MATCH  xxxxxxxxxxxxxxxxxx"
# "$COLMAP_CMD" matches_importer --help

# Import des matches (fichier match.txt)
"$COLMAP_CMD" matches_importer \
  --database_path "$BG_WORK/database.db" \
  --FeatureMatching.use_gpu 0 \
  --match_list_path "$BG_WORK/match.txt"

echo "xxxxxxxxxx Rajout des position gps estimés write  xxxxxxxxxxxxxxxxxxx"
~/bgColmapUtils/bgPosePriorsProvider_4_1_0 --write "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo "----- controle Rajout des position gps estimés"
~/bgColmapUtils/bgPosePriorsProvider_4_1_0 --check "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo "----- Importation des données  gps done "

# Créer le dossier sparse 
echo "xxxxxx mkdir $BG_WORK/sparse"
mkdir -p "$BG_WORK/sparse"

# Reconstruction sparse
echo "xxxxxx mapper xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
"$COLMAP_CMD" mapper \
  --database_path "$BG_WORK/database.db" \
  --image_path "$BG_WORK/images" \
  --output_path "$BG_WORK/sparse"

echo "xxxxxx CONVERTER xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Export PLY
"$COLMAP_CMD" model_converter \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/0/points3D.ply" \
  --output_type PLY

# Export TXT (cameras.txt / images.txt / points3D.txt)
"$COLMAP_CMD" model_converter \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/0" \
  --output_type TXT
  
echo "xxxxx Fin processColmap.sh xxxxxxxxxxxxxxxxxxxxxxxxx"
