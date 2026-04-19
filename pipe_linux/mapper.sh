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

COLMAP="${COLMAP:-$HOME/workspaceCpp/colmap/build/src/colmap/exe/colmap}"
BG_WORK="${BG_WORK:-/data/BG}"

DENSE_DIR="$BG_WORK/dense2"
LOG_DIR="$BG_WORK/logs"
mkdir -p "$DENSE_DIR" "$LOG_DIR"

LOG_FILE="$LOG_DIR/dense_poisson_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP: $COLMAP"
"$COLMAP" --version || true
echo "BG_WORK: $BG_WORK"
echo "Dense dir: $DENSE_DIR"
echo "Log: $LOG_FILE"

# Basic sanity checks
test -d "$BG_WORK/images"
test -d "$BG_WORK/sparse/0"



echo "=== 2) PatchMatch stereo (depth maps) ==="
"$COLMAP" patch_match_stereo \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP
  # Optionnel (si supporté):
  # --PatchMatchStereo.gpu_index 0
  # --PatchMatchStereo.num_iterations 5
  # --PatchMatchStereo.geom_consistency 1

echo "=== 3) Stereo fusion -> dense point cloud ==="
"$COLMAP" stereo_fusion \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path "$DENSE_DIR/fused.ply"

echo "=== 4) Poisson meshing ==="
"$COLMAP" poisson_mesher \
  --input_path "$DENSE_DIR/fused.ply" \
  --output_path "$DENSE_DIR/mesh_poisson.ply"

echo "Done."
echo "Point cloud: $DENSE_DIR/fused.ply"
echo "Poisson mesh: $DENSE_DIR/mesh_poisson.ply"
