#!/usr/bin/env bash
set -euo pipefail

# Lance tout le pipeline COLMAP dans l'ordre.
# À exécuter depuis le répertoire pipe_aws :
#   cd pipe_aws
#   ./0_all.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "Pipeline complet: 1 -> 4"
echo "Working dir: $(pwd)"
echo "========================================"


echo
echo "[1/4] feature_extractor"
./1_feature_extractor.sh

echo
echo "[2/4] exhaustive_matcher"
./2_exhaustive_matcher.sh

echo
echo "[3/4] mapper"
./3_mapper.sh

echo
echo "[4/4] converter_ply"
./4_converter_ply.sh

echo
echo "========================================"
echo "Pipeline terminé avec succès."
echo "========================================"
