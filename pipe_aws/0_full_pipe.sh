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

./1_feature_extractor.sh
./2_metadata2database
./3_matcher.sh
./4_mapper.sh
./5_geometric_verifier.sh
./6_point_triangulator.sh
./7_converter_ply.sh
./8_converter_txt.sh
./9_archive_and_clean.sh
echo
echo "========================================"
echo "Pipeline terminé avec succès."
echo "========================================"

