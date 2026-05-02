#!/usr/bin/env bash
set -euo pipefail

PDAL=~/workspaceCpp/PDAL/build/bin/pdal
IN="merged.ply"
OUT_LAZ="fused.laz"
OUT_LAS="fused.las"
echo "bg=data PDAL=$PDAL step=translate_to_laz IN=$IN OUT_LAZ=$OUT_LAZ"
echo "bg=data IN=$IN"
#pdal translate fused.ply fused.laz
# ~/workspaceCpp/PDAL/build/bin/pdal translate merged.ply fused.laz
"$PDAL translate $IN $OUT_LAZ" 
# ~/workspaceCpp/PDAL/build/bin/pdal translate fused.laz fused.las
# ~/workspaceCpp/PDAL/build/bin/pdal translate merged.ply fused.las
echo "bg=data PDAL=$PDAL step=translate_to_las  OUT_LAS=$OUT_LAS"
"$PDAL translate $IN IN=$IN $OUT_LAS" 
#Vérifier rapidement : pdal info fused.laz | head
# ~/workspaceCpp/PDAL/build/bin/pdal info merged.ply fused.laz
"$PDAL info $OUT | head" 


py3dtiles=/home/bertrand/venv/py3dtiles/bin/py3dtiles
echo "bg=data py3dtiles=$py3dtiles"
# pip install -U "py3dtiles[las]" // Installer le support las pour py3dtiles eventuellement
# /home/bertrand/venv/py3dtiles/bin/py3dtiles convert fused.las --out tiles_fused --overwrite --spec-version 1.0
"$py3dtiles convert fused.las --out tiles_fused --overwrite --spec-version 1.0"