#!/usr/bin/env bash
set -euo pipefail

COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap

SCRIPT_PATH="$(readlink -f -- "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname -- "$SCRIPT_PATH")"
echo "SCRIPT_DIR = $SCRIPT_DIR"
BG_WORK="$(readlink -f -- "$SCRIPT_DIR/..")"
echo "BG_WORK=$BG_WORK"
# max_image_size=4032
max_image_size=1512
DENSE_DIR="$SCRIPT_DIR/dense"
LOG_DIR="$DENSE_DIR/logs"
mkdir -p "$DENSE_DIR" "$LOG_DIR"

LOG_FILE="$LOG_DIR/dense_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP: $COLMAP"
"$COLMAP" --version || true
echo "bg=data BG_WORK=$BG_WORK"
echo "bg=data DENSE_DIR=$DENSE_DIR"
echo "bg=data LOG_FILE=$LOG_FILE"
echo "bg=data max_image_size=$max_image_size"
echo "bg=data SCRIPT_DIR=$SCRIPT_DIR"


mkdir -p "$SCRIPT_DIR/sparse/0" "$LOG_DIR"

echo "bg=colmap process=dense  etape=model_converter   date=$(date -Is)"
$COLMAP model_converter \
  --input_path "$SCRIPT_DIR" \
  --output_path "$SCRIPT_DIR/sparse/0" \
  --output_type BIN
  
  echo "bg=colmap process=dense  etape=image_undistorter   date=$(date -Is)"
"$COLMAP" image_undistorter \
  --image_path "$BG_WORK/images" \
  --input_path "$SCRIPT_DIR/sparse/0" \
  --output_path "$DENSE_DIR" \
  --output_type COLMAP \
  --max_image_size $max_image_size \
  --num_threads 4
  # Optionnel (si supporté par ton build, recommandé pour aller plus vite):
  # --max_image_size 2000

echo "bg=colmap process=dense  etape=patch_match_stereo   date=$(date -Is)"
"$COLMAP" patch_match_stereo \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --PatchMatchStereo.max_image_size $max_image_size \
  --PatchMatchStereo.cache_size 6 \
  --PatchMatchStereo.num_threads 1 \
  --PatchMatchStereo.num_iterations 3 \
  --PatchMatchStereo.allow_missing_files 1 \
 # --PatchMatchStereo.num_samples 10
  

echo "bg=colmap process=dense  etape=stereo_fusion   date=$(date -Is)"
"$COLMAP" stereo_fusion \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path "$DENSE_DIR/fused.ply" \
  --StereoFusion.use_cache 1 \
  --StereoFusion.cache_size 8 \
  --StereoFusion.num_threads 4 \
  --StereoFusion.check_num_images 10 \
  --StereoFusion.max_image_size $max_image_size

echo "bg=colmap process=dense  etape=poisson_mesher   date=$(date -Is)"
"$COLMAP" poisson_mesher \
  --input_path "$DENSE_DIR/fused.ply" \
  --output_path "$DENSE_DIR/mesh_poisson.ply" \
  --PoissonMeshing.depth 11 \
  --PoissonMeshing.trim 10 \
  --PoissonMeshing.point_weight 1 \
  --PoissonMeshing.color 1 \
  --PoissonMeshing.num_threads 4

echo "bg=colmap process=dense  etape=FIN   date=$(date -Is)"
echo "bg=data fused.ply=$DENSE_DIR/fused.ply"
echo "bg=data mesh_poisson.ply=$DENSE_DIR/mesh_poisson.ply"



