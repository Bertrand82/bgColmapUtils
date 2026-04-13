#!/usr/bin/env bash
set -euo pipefail

echo "COLMAP EXTRACTOR"

ls /data/BG
echo "xxxxx START "

HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"

# (optionnel) afficher l'aide
# "$HOME_COLMAP/colmap" feature_extractor --help

"$HOME_COLMAP/colmap" feature_extractor \
  --database_path /data/BG/database.db \
  --image_path /data/BG/images \
  --ImageReader.single_camera 1 \
  --ImageReader.camera_model SIMPLE_RADIAL \
  --FeatureExtraction.use_gpu 1 \
  --FeatureExtraction.num_threads 1 \
  --SiftExtraction.max_num_features 8192 \
  --log_level 2
