#!/usr/bin/env bash
set -euo pipefail


for d in paquet_*/; do
  [ -d "$d" ] || continue
  echo "bg=data Dossier=${d%/}"
done

for d in paquet_*/; do
  [ -d "$d" ] || continue
  echo "bg=data Dossier=${d%/}"

  (
    cd "$d" || exit 1
    ./processDensePaquet.sh
  ) || {
    echo "Erreur dans $d" >&2
    exit 1
  }
done