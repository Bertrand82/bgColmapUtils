#!/usr/bin/env bash
set -euo pipefail


SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
LOG_DIR="logs"
mkdir -p "$LOG_DIR"
ERRORS_LOG="$LOG_DIR/errors.log"
LOG="$SCRIPT_DIR/avancement.log"
log() {
  # format: 2026-04-30 22:49:37
  printf '%s %s\n' "$(date '+%F %T')" "$*" | tee -a "$LOG"
}



for d in paquet_*/; do
  [ -d "$d" ] || continue
  dossier="${d%/}"

  start_ts=$(date +%s)
  log "Début itération: dossier=$dossier"
  echo "bg=data Dossier=$dossier"

  paquet_log="$LOG_DIR/${dossier}.log"

  # Exécute le paquet en capturant stdout+stderr, et récupère le code retour
  (
    cd "$dossier" && ./processDensePaquet.sh
  ) >"$paquet_log" 2>&1
  rc=$?

  end_ts=$(date +%s)
  duree=$(( end_ts - start_ts ))

  if [ $rc -ne 0 ]; then
    log "ECHEC: dossier=$dossier rc=$rc duree=${duree}s (voir $paquet_log)" | tee -a "$ERRORS_LOG" >&2

    # Optionnel: extraire les dernières lignes dans errors.log pour un résumé rapide
    {
      echo "----- $(date '+%F %T') dossier=$dossier rc=$rc -----"
      tail -n 80 "$paquet_log"
      echo
    } >>"$ERRORS_LOG"

    # continuer avec le paquet suivant    
    continue
  fi

  log "OK: dossier=$dossier duree=${duree}s (log $paquet_log)"
  SLEEP_FIN=5
  echo "Pause $SLEEP_FIN secondes fin de traitement $dossier  duree : ${duree}"
  sleep $SLEEP_FIN
done