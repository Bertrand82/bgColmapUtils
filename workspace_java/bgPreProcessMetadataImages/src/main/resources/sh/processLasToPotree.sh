#!/usr/bin/env bash
set -euo pipefail

SCRIPT_PATH="$(readlink -f -- "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname -- "$SCRIPT_PATH")"
PDAL="$HOME/workspaceCpp/PDAL/build/bin/pdal"
POTREE="$HOME/workspaceCpp/PotreeConverter/build/PotreeConverter"

DIR_OUT="$SCRIPT_DIR/potree_cloud"
FILE_LAS="$SCRIPT_DIR/fused.las"

echo "bg=data POTREE=$POTREE step=generatePotree FILE_LAS=$FILE_LAS DIR_OUT=$DIR_OUT date=$(date -Is)"
"$POTREE" --help 

mkdir -p $DIR_OUT

"$POTREE" --help 

"$POTREE" "$FILE_LAS" -o "$DIR_OUT" --generate-page fused



echo "bg=data  step=generatePotreefin date=$(date -Is)"

