#!/bin/bash

set -euo pipefail

SOURCE_DIR="${1:-.}"
OUTPUT_MESH="${2:-merged_mesh_poisson.ply}"
WELD_EPS="${3:-0.01}"

BGOpen3D=~/workspaceCpp/bgOpen3D/build/bgOpen3D

mapfile -t MESH_FILES < <(find "$SOURCE_DIR" -type f -path "*/paquet_*/dense/mesh_poisson.ply" | sort)

if [ ${#MESH_FILES[@]} -eq 0 ]; then
  echo "Aucun fichier trouvé"
  exit 1
fi

if [ ${#MESH_FILES[@]} -eq 1 ]; then
  cp "${MESH_FILES[0]}" "$OUTPUT_MESH"
  echo "Un seul fichier, recopié vers $OUTPUT_MESH"
  exit 0
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

CURRENT_MERGE="$TMP_DIR/current_merge.ply"

"$BGOpen3D" --mergeMesh --weld-eps "$WELD_EPS" \
  "$CURRENT_MERGE" "${MESH_FILES[0]}" "${MESH_FILES[1]}"

for ((i=2; i<${#MESH_FILES[@]}; i++)); do
  NEXT_OUTPUT="$TMP_DIR/merge_$i.ply"
  "$BGOpen3D" --mergeMesh --weld-eps "$WELD_EPS" \
    "$NEXT_OUTPUT" "$CURRENT_MERGE" "${MESH_FILES[$i]}"
  mv "$NEXT_OUTPUT" "$CURRENT_MERGE"
done

cp "$CURRENT_MERGE" "$OUTPUT_MESH"
echo "Résultat final : $OUTPUT_MESH"