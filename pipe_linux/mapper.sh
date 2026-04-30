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
COLMAP_CMD=$COLMAP
BG_WORK=/data/BG
# max_image_size=4032
max_image_size=1512
DENSE_DIR="$BG_WORK/dense"
LOG_DIR="$DENSE_DIR/logs"
mkdir -p "$DENSE_DIR" "$LOG_DIR"

LOG_FILE="$LOG_DIR/dense_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP: $COLMAP"
"$COLMAP" --version || true
echo "bg=data BG_WORK=$BG_WORK"
echo "bg=data dense dir: $DENSE_DIR"
echo "bg=data LOG_FILE=$LOG_FILE"
echo "bg=data max_image_size=$max_image_size"

# Basic sanity checks
test -d "$BG_WORK/images"
test -d "$BG_WORK/sparse/0"

"$COLMAP" model_analyzer --path /data/BG/sparse/0
SPARSE_ROOT=$BG_WORK/sparse
for m in "$SPARSE_ROOT"/*; do
  [ -d "$m" ] || continue
  [ -f "$m/cameras.bin" ] || continue
  [ -f "$m/images.bin" ]  || continue
  [ -f "$m/points3D.bin" ]|| continue

"$COLMAP_CMD" model_converter \
  --input_path "$m" \
  --output_path "$m/points3D.ply" \
  --output_type PLY

# Export TXT (cameras.txt / images.txt / points3D.txt)
echo "bg=colmap process=sparse etape=model_converter_TXT  date=$(date -Is)"

"$COLMAP_CMD" model_converter \
  --input_path "$m" \
  --output_path "$m" \
  --output_type TXT
  echo "=== Model: $m ==="
  "$COLMAP_CMD" model_analyzer --path "$m"
  echo
done
# "$COLMAP"  --help
# "$COLMAP" patch_match_stereo --help
