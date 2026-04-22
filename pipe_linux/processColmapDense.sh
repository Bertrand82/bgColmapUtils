#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   BG_WORK=/data/BG ./dense_poisson.sh
# Expects:
#   $BG_WORK/images
#   $BG_WORK/sparse/0   (COLMAP sparse model)
# Produces:
#   $BG_WORK/dense/fused.ply
#   $BG_WORK/dense/mesh_poisson.ply

COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap
BG_WORK=/data/BG

DENSE_DIR="$BG_WORK/dense"
LOG_DIR="$DENSE_DIR/logs"
mkdir -p "$DENSE_DIR" "$LOG_DIR"

LOG_FILE="$LOG_DIR/dense_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP: $COLMAP"
"$COLMAP" --version || true
echo "BG_WORK: $BG_WORK"
echo "Dense dir: $DENSE_DIR"
echo "Log: $LOG_FILE"

# Basic sanity checks
test -d "$BG_WORK/images"
test -d "$BG_WORK/sparse/0"

echo "bg dense === 1) Undistort images (dense workspace) ==="
"$COLMAP" image_undistorter \
  --image_path "$BG_WORK/images" \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$DENSE_DIR" \
  --output_type COLMAP \
  --max_image_size 1400 \
  --num_threads 4
  # Optionnel (si supporté par ton build, recommandé pour aller plus vite):
  # --max_image_size 2000

echo "bg dense === 2) PatchMatch stereo (depth maps) ==="
"$COLMAP" patch_match_stereo \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --PatchMatchStereo.max_image_size 1400 \
  --PatchMatchStereo.cache_size 8 \
  --PatchMatchStereo.num_threads 1
  



echo "bg dense === 3) Stereo fusion -> dense point cloud ==="
"$COLMAP" stereo_fusion \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path "$DENSE_DIR/fused.ply" \
  --StereoFusion.use_cache 1 \
  --StereoFusion.cache_size 8 \
  --StereoFusion.num_threads 4 \
  --StereoFusion.check_num_images 10 \
  --StereoFusion.max_image_size 1400

echo "bg dense === 4) Poisson meshing ==="
"$COLMAP" poisson_mesher \
  --input_path "$DENSE_DIR/fused.ply" \
  --output_path "$DENSE_DIR/mesh_poisson.ply" \
  --PoissonMeshing.depth 11 \
  --PoissonMeshing.trim 10 \
  --PoissonMeshing.point_weight 1 \
  --PoissonMeshing.color 1 \
  --PoissonMeshing.num_threads 4

echo "bg dense === Done."
echo "Point cloud: $DENSE_DIR/fused.ply"
echo "Poisson mesh: $DENSE_DIR/mesh_poisson.ply"
