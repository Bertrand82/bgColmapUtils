#!/usr/bin/env bash
set -euo pipefail

# (Optionnel) afficher l'aide
HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"

$HOME_COLMAP/colmap mapper --help

mkdir /data/BG/sparse
$HOME_COLMAP/colmap mapper \
    --database_path /data/BG/database.db \
    --image_path /data/BG/images \
    --output_path /data/BG/sparse
	

