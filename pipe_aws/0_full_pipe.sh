#!/usr/bin/env bash
set -euo pipefail

# Lance tout le pipeline COLMAP dans l'ordre.
# À exécuter depuis le répertoire pipe_aws :
0_full_pipe.sh
1_feature_extractor.sh
2_metadata2database
3_matcher.sh
4_mapper.sh
5_converter_ply.sh
6_converter_txt.sh
7_archive_and_clean.sh
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
./5_converter_ply.sh
./6_converter_txt.sh
./7_archive_and_clean.sh
echo
echo "========================================"
echo "Pipeline terminé avec succès."
echo "========================================"
