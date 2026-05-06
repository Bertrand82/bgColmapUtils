#!/usr/bin/env bash
set -euo pipefail

py3dtiles="/home/bertrand/venv/py3dtiles/bin/py3dtiles"
OUT_DIR="tiled_merged_fused"

echo "bg=data py3dtiles=$py3dtiles"
echo "bg=data OUT_DIR=$OUT_DIR"

files=()

for d in paquet_*/; do
  [ -d "$d" ] || continue
  echo "bg=data Dossier=${d%/}"

  fichierLAS="${d%/}/fused.las"
  if [[ -f "$fichierLAS" ]]; then
    echo "bg=data + $fichierLAS"
    files+=("$fichierLAS")
    echo "bg=processMergePLYtoLaz step=fetchFiles  fichierLAS=$fichierLAS"
  else
    echo "bg=processMergePLYtoLaz step=fetchFiles  fichierLAS=$fichierLAS  warning=manquant"
  fi
done



"$py3dtiles" convert "${files[@]}" \
  --out "$OUT_DIR" \
  --overwrite \
  --spec-version 1.0 \
  --color_scale 256

echo "bg=data OK -> $OUT_DIR/tileset.json"