#!/usr/bin/env bash
set -euo pipefail

PDAL="$HOME/workspaceCpp/PDAL/build/bin/pdal"
IN="merged.ply"
OUT_LAZ="fused.laz"
OUT_LAS="fused.las"

echo "bg=data PDAL=$PDAL step=translate_to_laz IN=$IN OUT_LAZ=$OUT_LAZ date=$(date -Is)"
"$PDAL" translate "$IN" "$OUT_LAZ"

echo "bg=data PDAL=$PDAL step=translate_to_las IN=$IN OUT_LAS=$OUT_LAS date=$(date -Is)"
"$PDAL" translate "$IN" "$OUT_LAS"

py3dtiles="/home/bertrand/venv/py3dtiles/bin/py3dtiles"
echo "bg=data step=py3dtiles state="start" file=tiles_fused py3dtiles=$py3dtiles date=$(date -Is)"
# /home/bertrand/venv/py3dtiles/bin/py3dtiles convert fused.las --out tiles_fused2--overwrite --spec-version 1.0 --color_scale 256
"$py3dtiles" convert "$OUT_LAS" --out tiled_fused --overwrite --spec-version 1.0 --color_scale 256
echo "bg=data step=py3dtiles state="done" file=tiles_fused date=$(date -Is)"

echo "bg=data  step=fin date=$(date -Is)"