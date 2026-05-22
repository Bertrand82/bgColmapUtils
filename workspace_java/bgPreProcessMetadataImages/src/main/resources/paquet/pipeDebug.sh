#!/usr/bin/env bash
set -euo pipefail

COLMAP_EXE_PATH=~/workspaceCpp/colmap/build/src/colmap/exe
export COLMAP_EXE_PATH
COLMAP=$COLMAP_EXE_PATH/colmap
export COLMAP


SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_SCRIPT="$SCRIPT_DIR/dense/run-colmap-geometric.sh"
cd dense


if [[ ! -f "$TARGET_SCRIPT" ]]; then
  echo "Erreur: script introuvable: $TARGET_SCRIPT" >&2
  exit 1
fi

echo "Using COLMAP=$COLMAP"
echo "Running $TARGET_SCRIPT"

bash "$TARGET_SCRIPT">> logDebug.txt 2>&1
cd ..
echo "FIN pipeDebug"