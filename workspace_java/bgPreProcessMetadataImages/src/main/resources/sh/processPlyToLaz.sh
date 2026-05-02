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


