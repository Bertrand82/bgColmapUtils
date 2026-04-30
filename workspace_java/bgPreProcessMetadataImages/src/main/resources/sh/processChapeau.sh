#!/usr/bin/env bash
set -euo pipefail


SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
LOG="$SCRIPT_DIR/avancement.log"
log() {
  # format: 2026-04-30 22:49:37
  printf '%s %s\n' "$(date '+%F %T')" "$*" | tee -a "$LOG"
}



for d in paquet_*/; do
  [ -d "$d" ] || continue
  dossier="${d%/}"
  log "Début itération: dossier=$dossier"
  echo "bg=data Dossier=${d%/}"

  (
    cd "$d" || exit 1
    ./processDensePaquet.sh
  ) || {
    echo "Erreur dans $d" >&2
    exit 1
  }
  echo "Pause 30s fin de traitement $dossier"
  sleep 30
done