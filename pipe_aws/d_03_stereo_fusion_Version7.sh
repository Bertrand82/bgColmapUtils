#!/usr/bin/env bash
set -euo pipefail

echo "======== dense step 03: stereo_fusion ======================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> "$LOG"
echo "===================================================Dense 03 stereo_fusion" >> "$LOG"

DENSE_DIR="${OUTPUT_DIR}/dense"
mkdir -p "$DENSE_DIR"

docker run --rm \
  -v "${OUTPUT_DIR}:/output" \
  colmap/colmap \
  colmap stereo_fusion \
    --workspace_path /output/dense \
    --workspace_format COLMAP \
    --input_type geometric \
    --output_path /output/dense/fused.ply

echo
echo "========================================"
echo "Process colmap stereo_fusion done"
echo "Output: ${DENSE_DIR}/fused.ply"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))
echo "DURATION_MN Dense_03_stereo_fusion : $DURATION_MIN" >> "$LOG"