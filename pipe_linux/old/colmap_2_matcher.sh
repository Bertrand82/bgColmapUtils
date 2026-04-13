#!/usr/bin/env bash
set -euo pipefail
HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"
# (Optionnel) afficher l'aide
$HOME_COLMAP/colmap matches_importer --help

$HOME_COLMAP/colmap matches_importer \
  --database_path /data/BG/database.db \
  --match_list_path /data/BG/match.txt \
  --FeatureMatching.use_gpu 1



