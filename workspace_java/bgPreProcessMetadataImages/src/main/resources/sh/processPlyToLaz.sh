#!/usr/bin/env bash
set -euo pipefail

PDAL="$HOME/workspaceCpp/PDAL/build/bin/pdal"
IN="merged.ply"
OUT_LAZ="fused.laz"
OUT_LAS="fused.las"

echo "bg=data PDAL=$PDAL step=translate_to_laz IN=$IN OUT_LAZ=$OUT_LAZ"
"$PDAL" translate "$IN" "$OUT_LAZ"

echo "bg=data PDAL=$PDAL step=translate_to_las OUT_LAS=$OUT_LAS"
"$PDAL" translate "$IN" "$OUT_LAS"

echo "bg=data step=info_laz file=$OUT_LAZ"
"$PDAL" info "$OUT_LAZ" | head

py3dtiles="/home/bertrand/venv/py3dtiles/bin/py3dtiles"
echo "bg=data py3dtiles=$py3dtiles"
"$py3dtiles" convert "$OUT_LAS" --out tiles_fused --overwrite --spec-version 1.0