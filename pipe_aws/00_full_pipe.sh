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

./01_feature_extractor.sh
./02_metadata2database
./03_matcher.sh
./04_mapper.sh
./05_model_merger.sh
./06_geometric_verifier.sh
./07_point_triangulator.sh
./08_converter_ply.sh
./09_converter_txt.sh
./10_archive_and_clean.sh
echo
echo "========================================"
echo "Pipeline terminé avec succès."
echo "========================================"

