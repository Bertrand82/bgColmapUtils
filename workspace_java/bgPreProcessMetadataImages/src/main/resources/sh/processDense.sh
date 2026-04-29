#!/usr/bin/env bash
set -euo pipefail

COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap

SCRIPT_PATH="$(readlink -f -- "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname -- "$SCRIPT_PATH")"
echo "SCRIPT_DIR = $SCRIPT_DIR"
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
echo "bg=data SCRIPT_DIR=$SCRIPT_DIR"




$COLMAP model_converter \
  --input_path "$SCRIPT_DIR" \
  --output_path "$SCRIPT_DIR/sparse_subset/0" \
  --output_type BIN