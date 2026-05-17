#!/bin/bash

set -e

SOURCE_DIR="${1:-.}"
OUTPUT_MESH="${2:-merged_mesh_poisson.ply}"
#--weld-eps: >= 0 (0 desactive la soudure geometrique, > 0 active la soudure des sommets proches)
WELD_EPS="${3:-0.01}"

# BGOpen3D="/home/bertrand/workspaceCpp/bgOpen3D/build/bgOpen3D"
BGOpen3D=~/workspaceCpp/bgOpen3D/build/bgOpen3D
echo "bg=processMergePoisson BGOpen3D=$BGOpen3D"
echo "bg=processMergePoisson WELD_EPS=$WELD_EPS"
echo "bg=processMergePoisson OUTPUT_MESH=$OUTPUT_MESH"
echo "bg=processMergePoisson SOURCE_DIR=$SOURCE_DIR"

mapfile -t MESH_FILES < <(find "$SOURCE_DIR" -type f -path "*/paquet_*/dense/mesh_poisson.ply" | sort)
echo "bg=processMergePoisson MESH_FILES=$MESH_FILES"

if [ ${#MESH_FILES[@]} -eq 0 ]; then
  echo "bg=processMergePoisson Aucun fichier trouvé pour le motif paquet_*/dense/mesh_poisson.ply"
  exit 1
fi

echo "bg=processMergePoisson Fichiers trouvés :"
for f in "${MESH_FILES[@]}"; do
  echo "bg=processMergePoisson  $f"
done
echo "bg=processMergePoisson step=start"

"$BGOpen3D" --mergeMesh --weld-eps "$WELD_EPS" "$OUTPUT_MESH" "${MESH_FILES[@]}"
echo "bg=processMergePoisson step=done OUTPUT_MESH=$OUTPUT_MESH"
