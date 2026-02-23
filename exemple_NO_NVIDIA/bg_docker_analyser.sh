#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "COLMAP -colmap model_analyzer "
echo "========================================"
echo

CWD="$(pwd)"
WS="${CWD}/colmap_project"



docker run --rm \
  -v "${WS}:/data" \
  colmap/colmap \
  colmap model_analyzer --path /data/sparse/0

echo
echo "========================================"
echo "Traitement termine colmap model_analyzer --path /data/sparse/0"
echo "========================================"
