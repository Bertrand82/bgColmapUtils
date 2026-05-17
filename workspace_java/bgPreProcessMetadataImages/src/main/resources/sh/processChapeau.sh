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


# DEBUT BOUCLE
for d in paquet_*/; do
  [ -d "$d" ] || continue
  dossier="${d%/}"
 
  # Skip si fused.ply existe déjà dans le dossier
  if [ -f "$dossier/fused.las" ]; then
    log "SKIP: dossier=$dossier car fichier $dossier/fused.ply déjà présent"
    echo "bg=chapeau step=skip_process_dossier Dossier=$dossier reason=fused.ply_exists date=$(date -Is)"
    continue
  fi

  start_ts=$(date +%s)
  log "Début itération: dossier=$dossier"
  echo "bg=chapeau step=debut_process_dossier Dossier=$dossier date=$(date -Is)"

  paquet_log="$LOG_DIR/${dossier}.log"

  # Exécute le paquet en capturant stdout+stderr, et récupère le code retour
  (
    cd "$dossier" && ./processDensePaquet.sh
  ) >"$paquet_log" 2>&1
  rc=$?

  end_ts=$(date +%s)
  duree=$(( end_ts - start_ts ))
  echo "bg=chapeau step=fin_process_dossier Dossier=$dossier date=$(date -Is) duree=$duree"
  if [ $rc -ne 0 ]; then
    log "ECHEC: dossier=$dossier rc=$rc duree=${duree}s (voir $paquet_log)" | tee -a "$ERRORS_LOG" >&2

    # Optionnel: extraire les dernières lignes dans errors.log pour un résumé rapide
    {
      echo "----- $(date '+%F %T') dossier=$dossier rc=$rc -----"
      tail -n 80 "$paquet_log"
      echo
    } >>"$ERRORS_LOG"
	log "bg=chapeau step=fin_KO dossier=$dossier duree=${duree}s (log $paquet_log)"
    # continuer avec le paquet suivant    
    continue
  fi

  log "bg=chapeau step=fin_OK dossier=$dossier duree=${duree}s (log $paquet_log)"
  echo "bg=colmap process=chapeau  etape=process.end   date=$(date -Is)"
  SLEEP_FIN=5
  echo SLEEP $SLEEP_FIN secondes
  sleep $SLEEP_FIN
  
done

# FIN  BOUCLE
echo "bg=colmap process=chapeau  etape=copyPoisson     date=$(date -Is)"
./copy_mesh_poisson.sh
echo "bg=colmap process=chapeau  etape=mergePLY     date=$(date -Is)"
./processMergePLY.sh
echo "bg=colmap process=chapeau  etape=PlyToLaz    date=$(date -Is)"
./processPlyToLaz.sh
echo "bg=colmap process=chapeau  etape=MergePlyToLaz    date=$(date -Is)"
./processMergePLYtoLaz.sh
echo "bg=colmap process=chapeau  etape=LasToPotree     date=$(date -Is)"
./processLasToPotree.sh
