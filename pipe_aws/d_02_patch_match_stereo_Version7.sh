#!/usr/bin/env bash
set -euo pipefail

echo "======== dense step 02: patch_match_stereo =================================="
START_EPOCH=$(date +%s)

source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR" >> "$LOG"
echo "===================================================Dense 02 patch_match_stereo" >> "$LOG"

DENSE_DIR="${OUTPUT_DIR}/dense"
if [[ ! -d "$DENSE_DIR" ]]; then
  echo "ERROR: dense workspace not found at: $DENSE_DIR" | tee -a "$LOG"
  echo "Run d_01_image_undistorter.sh first." | tee -a "$LOG"
  exit 1
fi

docker run --rm \
  -v "${OUTPUT_DIR}:/output" \
  colmap/colmap \
  colmap patch_match_stereo \
    --workspace_path /output/dense \
    --workspace_format COLMAP \
    --PatchMatchStereo.geom_consistency true

echo
echo "========================================"
echo "Process colmap patch_match_stereo done"
echo "========================================"
END_EPOCH=$(date +%s)

DURATION_SEC=$((END_EPOCH - START_EPOCH))
DURATION_MIN=$(( (DURATION_SEC + 59) / 60 ))
echo "DURATION_MN Dense_02_patch_match_stereo : $DURATION_MIN" >> "$LOG"