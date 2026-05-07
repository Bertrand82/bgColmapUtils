#!/usr/bin/env bash
set -euo pipefail

PDAL=~/workspaceCpp/PDAL/build/bin/pdal
OUT="merged.ply"

echo "bg=data PDAL=$PDAL"
echo "bg=data OUT=$OUT"

# On collecte les PLY existants
ply_files=()
for d in paquet_*/; do
  [ -d "$d" ] || continue
  dossier="${d%/}"
  echo "bg=data dossier=$dossier"
  echo "bg=processMergePLY step=debut_process Dossier=$dossier date=$(date -Is)"

  fichierPLY="${d%/}/dense/fused.ply"
  if [[ -f "$fichierPLY" ]]; then
    echo "bg=data + $fichierPLY"
    ply_files+=("$fichierPLY")
    echo "bg=processMergePLY step=mergePLY Dossier=$dossier date=$(date -Is)"
  else
    echo "bg=processMergePLY step=mergePLYWarning Dossier=$dossier date=$(date -Is) warning=FilePlYAbsent"
  fi
done

# Vérif: au moins 1 fichier
if (( ${#ply_files[@]} == 0 )); then
  echo "bg=error Aucun fichier PLY trouvé (paquet_*/dense/fused.ply)" >&2
  exit 1
fi

# Générer un pipeline PDAL
echo "bg=pdal process=merge  etape=merge  date=$(date -Is)"
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
echo "bg=pdal process=merge  etape=pipeline  date=$(date -Is)"
echo "bg=data Pipeline=$tmp_json"
"$PDAL" pipeline "$tmp_json"
echo "bg=pdal process=merge  etape=fin  date=$(date -Is)"
echo "bg=data OK merged -> $OUT"

