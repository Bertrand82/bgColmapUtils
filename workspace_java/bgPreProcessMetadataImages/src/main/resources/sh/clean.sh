#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./delete_stereo_dirs.sh /chemin/vers/projets
# Exemple:
#   ./delete_stereo_dirs.sh /data/BG
#
# Supprime tous les répertoires nommés exactement "stereo" (ex: dense/stereo)
# dans l'arborescence donnée.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
ROOT="$SCRIPT_DIR"

echo "Recherche des répertoires 'stereo' sous: $ROOT"
mapfile -d '' STEREO_DIRS < <(find "$ROOT" -type d -name stereo -print0)

if [ "${#STEREO_DIRS[@]}" -eq 0 ]; then
  echo "Aucun répertoire 'stereo' trouvé."
  exit 0
fi

echo "Répertoires trouvés (${#STEREO_DIRS[@]}):"
printf ' - %s\n' "${STEREO_DIRS[@]}"

echo
read -r -p "Confirmer suppression (tape 'oui') : " CONFIRM
if [ "$CONFIRM" != "oui" ]; then
  echo "Annulé."
  exit 1
fi

for d in "${STEREO_DIRS[@]}"; do
  echo "Suppression: $d"
  rm -rf --one-file-system "$d"
done

echo "Terminé."