#!/usr/bin/env bash
set -euo pipefail




COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap

SCRIPT_PATH="$(readlink -f -- "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname -- "$SCRIPT_PATH")"
echo "SCRIPT_DIR = $SCRIPT_DIR"
BG_WORK="$(readlink -f -- "$SCRIPT_DIR/..")"
echo "BG_WORK=$BG_WORK"
# max_image_size=4032
# max_image_size=1512bg
# max_image_size=2024 // working par paquet de 20 images
max_image_size=4000
PatchMatchStereo_num_threads=2
PatchMatchStereo_num_iterations=3
PatchMatchStereo_cache_size=16
DENSE_DIR="$SCRIPT_DIR/dense"
LOG_DIR="$SCRIPT_DIR"
mkdir -p "$DENSE_DIR" "$LOG_DIR"

LOG_FILE="$LOG_DIR/debug.log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP: $COLMAP"
"$COLMAP" --version || true
echo "bg=processDensePaquet BG_WORK=$BG_WORK"
echo "bg=processDensePaquet DENSE_DIR=$DENSE_DIR"
echo "bg=processDensePaquet LOG_FILE=$LOG_FILE"
echo "bg=processDensePaquet max_image_size=$max_image_size"
echo "bg=processDensePaquet PatchMatchStereo_num_threads=$PatchMatchStereo_num_threads"
echo "bg=processDensePaquet PatchMatchStereo_num_iterations=$PatchMatchStereo_num_iterations"
echo "bg=processDensePaquet PatchMatchStereo_cache_size=$PatchMatchStereo_cache_size"



echo "bg=debug SCRIPT_DIR=$SCRIPT_DIR"


mkdir -p "$SCRIPT_DIR/sparse/0" "$LOG_DIR"


nvidia-smi
sleep 10 
echo "bg=processDensePaquet process=dense  etape=patch_match_stereo   date=$(date -Is)"
"$COLMAP" patch_match_stereo \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --PatchMatchStereo.max_image_size 4000 \
  --PatchMatchStereo.cache_size 16 \
  --PatchMatchStereo.num_threads 2 \
  --PatchMatchStereo.num_iterations 3 \
  --PatchMatchStereo.geom_consistency 1 \
  --PatchMatchStereo.filter 1


 # --PatchMatchStereo.allow_missing_files 1 \
 # --PatchMatchStereo.num_samples 10
echo "bg=processDensePaquet process=dense  etape=patch_match_stereo  step="done" date=$(date -Is)"
nvidia-smi 
sleep 10 
echo "bg=processDensePaquet process=dense  etape=stereo_fusion  step="start"  date=$(date -Is)"
"$COLMAP" stereo_fusion \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path "$DENSE_DIR/fused.ply" \
  --StereoFusion.use_cache 1 \
  --StereoFusion.cache_size 8 \
  --StereoFusion.num_threads 4 \
  --StereoFusion.check_num_images 16 \
  --StereoFusion.max_image_size 4000
  
echo "bg=processDensePaquet process=dense  etape=stereo_fusion  step="done"  date=$(date -Is)"
  
sleep 10
echo "bg=processDensePaquet process=dense  etape=poisson_mesher step="start"  date=$(date -Is)"
"$COLMAP" poisson_mesher \
  --input_path "$DENSE_DIR/fused.ply" \
  --output_path "$DENSE_DIR/mesh_poisson.ply" \
  --PoissonMeshing.depth 11 \
  --PoissonMeshing.trim 10 \
  --PoissonMeshing.point_weight 1 \
  --PoissonMeshing.color 1 \
  --PoissonMeshing.num_threads 4
  
echo "bg=processDensePaquet process=dense  etape=poisson_mesher  step="done" date=$(date -Is)"
OUT_LAS="fused.las"
PDAL="$HOME/workspaceCpp/PDAL/build/bin/pdal"
echo "bg=colmap process="PDAL" PDAL=$PDAL step=translate_to_las OUT_LAS=$OUT_LAS date=$(date -Is)"
"$PDAL" translate "$DENSE_DIR/fused.ply" "$OUT_LAS"

echo "bg=processDensePaquet process=dense  etape=FIN   date=$(date -Is)"
echo "bg=processDensePaquet fused.ply=$DENSE_DIR/fused.ply"
echo "bg=processDensePaquet mesh_poisson.ply=$DENSE_DIR/mesh_poisson.ply"
nb_images=$(find "$SCRIPT_DIR/dense/images" -maxdepth 1 -type f | wc -l)
echo "bg=processDensePaquet nb_images=$nb_images"
echo bg suppresion de stereo : $DENSE_DIR/stereo
rm -rf --one-file-system $DENSE_DIR/stereo
echo "bg=processDensePaquet   etape=FIN   date=$(date -Is)"

