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



log "bg=processChapeauPost  step="mergePLY" date=$(date -Is)"
  echo "bg=processChapeauPost process=chapeau  etape=mergePLY     date=$(date -Is)"
  ./processMergePLY.sh
  log "bg=processChapeauPost  step="PlyToLaz" date=$(date -Is)"
  echo "bg=processChapeauPost process=chapeau  etape=PlyToLaz    date=$(date -Is)"
  ./processPlyToLaz.sh
  log "bg=processChapeauPost  step="MergePlyToLaz" date=$(date -Is)"
  echo "bg=processChapeauPost process=chapeau  etape=MergePlyToLaz    date=$(date -Is)"
  ./processMergePLYtoLaz.sh
   log "bg=processChapeauPost  step="LasToPotree" date=$(date -Is)"
  echo "bg=processChapeauPost process=chapeau  etape=LasToPotree     date=$(date -Is)"
  ./processLasToPotree.sh
  echo "bg=processChapeauPost process=chapeau  etape=Fin     date=$(date -Is)"
