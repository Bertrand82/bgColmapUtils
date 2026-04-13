#!/usr/bin/env bash
set -euo pipefail
HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"

$HOME_COLMAP/colmap model_converter --help

echo ------------------

$HOME_COLMAP/colmap model_converter \
    --input_path /data/BG/sparse/0 \
    --output_path /data/BG/sparse/0/points3D.ply \
    --output_type PLY
	

