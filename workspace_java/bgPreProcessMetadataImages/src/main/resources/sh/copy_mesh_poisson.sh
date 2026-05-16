#!/bin/bash

SOURCE_DIR="${1:-.}"
DEST_DIR="$SOURCE_DIR/poisson"

mkdir -p "$DEST_DIR"

find "$SOURCE_DIR" -type f -path "*/paquet_*/mesh_poisson.ply" | while read -r file; do
  paquet_dir=$(echo "$file" | sed -n 's|.*/\(paquet_[^/]*\)/.*|\1|p')
  cp "$file" "$DEST_DIR/mesh_poisson_${paquet_dir}.ply"
  echo "Sous-répertoire: $paquet_dir -> $DEST_DIR/mesh_poisson_${paquet_dir}.ply"
done