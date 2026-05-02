#!/usr/bin/env bash
set -euo pipefail

PDAL=~/workspaceCpp/PDAL/build/bin/pdal
IN="merged.ply"
OUT=""
echo "bg=data PDAL=$PDAL"
echo "bg=data IN=$IN"
#pdal translate fused.ply fused.laz
# ~/workspaceCpp/PDAL/build/bin/pdal translate merged.ply fused.laz
"$PDAL translate $IN $OUT" 

#Vérifier rapidement : pdal info fused.laz | head
# ~/workspaceCpp/PDAL/build/bin/pdal info fused.laz
"$PDAL info $OUT | head" 

