#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

COLMAP_CMD="/home/bertrand/workspaceCpp/colmap/build/src/colmap/exe/colmap"
COLMAP_EXE_PATH="$HOME/workspaceCpp/colmap/build/src/colmap/exe"
echo "bg sparse COLMAP binaire utilisé: $COLMAP_CMD"
"$COLMAP_CMD" --version
BG_WORK="/data/BG"

SPARSE_DIR="$BG_WORK/sparse"
LOG_DIR="$SPARSE_DIR/logs"
mkdir -p "$SPARSE_DIR"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/colmap_sparse$(date +%Y%m%d_%H%M%S).log"
# redirige stdout+stderr vers le log, tout en gardant l'affichage terminal
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP EXTRACTOR"
# "$COLMAP_CMD" feature_extractor --help
echo "bg=colmap process=sparse  etape=start ls $BG_WORK"

ls -la "$BG_WORK"
echo "b=colmap process=sparse etape=feature_extractor "

# Extraction des features
"$COLMAP_CMD" feature_extractor \
  --database_path "$BG_WORK/database.db" \
  --image_path "$BG_WORK/images" \
  --ImageReader.single_camera 1 \
  --ImageReader.camera_model SIMPLE_RADIAL \
  --FeatureExtraction.use_gpu 1 \
  --FeatureExtraction.num_threads 7 \
  --log_level 2

echo "bg=colmap process=sparse etape=MATCH "
# "$COLMAP_CMD" matches_importer --help

# Import des matches (fichier match.txt)
"$COLMAP_CMD" matches_importer \
  --database_path "$BG_WORK/database.db" \
  --FeatureMatching.use_gpu 0 \
  --match_list_path "$BG_WORK/match.txt"

echo "bg=colmap process=sparse etape=add_pose_gps"
~/bgColmapUtils/bgPosePriorsProvider_4_1_0 --write "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo "bg=colmap process=sparse etape=controle_gps
~/bgColmapUtils/bgPosePriorsProvider_4_1_0 --check "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo "bg=colmap process=sparse etape="mkd_sparse"

# Créer le dossier sparse 
echo "xxxxxx mkdir $BG_WORK/sparse"


# Reconstruction sparse
echo "bg=colmap process=sparse etape=mapper "


"$COLMAP_CMD" mapper \
  --database_path "$BG_WORK/database.db" \
  --image_path "$BG_WORK/images" \
  --output_path "$BG_WORK/sparse" \
  --Mapper.num_threads 6 \
  --Mapper.multiple_models 1 \
  --Mapper.ba_use_gpu 0 \
  --Mapper.ba_global_frames_freq 1200 \
  --Mapper.ba_global_points_freq 800000 \
  --Mapper.ba_global_max_num_iterations 15 \
  --Mapper.ba_global_max_refinements 1 \
  --Mapper.ba_local_max_num_iterations 20 \
  --Mapper.tri_ignore_two_view_tracks 1 \
  --Mapper.filter_max_reproj_error 4 \
  --Mapper.abs_pose_min_num_inliers 30

echo "bg=colmap process=sparse etape=model_converter_PLY"

# Export PLY
"$COLMAP_CMD" model_converter \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/0/points3D.ply" \
  --output_type PLY

# Export TXT (cameras.txt / images.txt / points3D.txt)
echo "bg=colmap process=sparse etape=model_converter_TXT"

"$COLMAP_CMD" model_converter \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/0" \
  --output_type TXT
  
echo "bg=colmap process=sparse etape=appel_process_dense"


./processColmapDense.sh
