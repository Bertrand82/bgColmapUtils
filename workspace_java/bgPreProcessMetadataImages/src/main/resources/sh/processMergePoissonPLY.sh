#!/usr/bin/env bash
set -euo pipefail

PDAL=~/workspaceCpp/PDAL/build/bin/pdal
OUT="merged_poisson.ply"
COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap

echo "bg=processMergePoissonPLY PDAL=$PDAL"
echo "bg=processMergePoissonPLY OUT=$OUT"
echo "bg=processMergePoissonPLY COLMAP=$COLMAP"

# On collecte les PLY existants
ply_files=()
for d in paquet_*/; do
  [ -d "$d" ] || continue
  echo "bg=processMergePoissonPLY Dossier=${d%/}"

  fichierPLY="${d%/}/dense/mesh_poisson.ply"
  if [[ -f "$fichierPLY" ]]; then
    echo "bg=processMergePoissonPLY + $fichierPLY"
    ply_files+=("$fichierPLY")
  else
    echo "bg=processMergePoissonPLY ! manquant: $fichierPLY" >&2
  fi
done

# Vérif: au moins 1 fichier
if (( ${#ply_files[@]} == 0 )); then
  echo "bg=processMergePoissonPLY error= Aucun fichier PLY trouvé (paquet_*/dense/fused.ply)" >&2
  exit 1
fi

# Générer un pipeline PDAL
echo "bg=processMergePoissonPLY process=merge  etape=merge  date=$(date -Is)"
tmp_json="$(mktemp /tmp/pdal-merge-XXXXXX.json)"
trap 'rm -f "$tmp_json"' EXIT

{
  echo '{'
  echo '  "pipeline": ['
  for i in "${!ply_files[@]}"; do
    f="${ply_files[$i]}"
    # JSON: échapper les backslash et guillemets si jamais
    esc="${f//\\/\\\\}"
    esc="${esc//\"/\\\"}"
    if (( i < ${#ply_files[@]} - 1 )); then
      echo "    \"${esc}\","
    else
      echo "    \"${esc}\","
    fi
  done
  cat <<EOF
    {
      "type": "writers.ply",
      "filename": "$OUT"
    }
  ]
}
EOF
} > "$tmp_json"

echo "bg=processMergePoissonPLY tmp_json=$tmp_json"
echo "bg=processMergePoissonPLY Pipeline contenu:"
cat "$tmp_json"

echo "bg=processMergePoissonPLY process=merge  etape=pipeline  date=$(date -Is)"
# "$PDAL" pipeline "$tmp_json"
echo "bg=processDensePaquet process=dense  etape=poisson_mesher   date=$(date -Is)"
"$COLMAP" poisson_mesher \
  --input_path "merged.ply" \
  --output_path "$OUT" \
  --PoissonMeshing.depth 11 \
  --PoissonMeshing.trim 10 \
  --PoissonMeshing.point_weight 1 \
  --PoissonMeshing.color 1 \
  --PoissonMeshing.num_threads 4
echo "bg=processMergePoissonPLY process=merge  etape=fin  date=$(date -Is)"
echo "bg=processMergePoissonPLY OK merged -> $OUT"

