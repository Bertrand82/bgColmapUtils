#!/usr/bin/env bash
set -euo pipefail


 # Skip si fused.ply existe déjà dans le dossier
if [ -f "fused.las" ]; then
  echo "Le fichier fused.ply existe, le repertoire a été traité, arrêt du traitement."
  exit 0
fi

echo "bg=processDensePaquet Le fichier fused.ply n'existe pas, on continue."

COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap

SCRIPT_PATH="$(readlink -f -- "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname -- "$SCRIPT_PATH")"
echo "SCRIPT_DIR = $SCRIPT_DIR"
BG_WORK="$(readlink -f -- "$SCRIPT_DIR/..")"
echo "BG_WORK=$BG_WORK"
# max_image_size=4032
# max_image_size=1512
# max_image_size=2024 // working par paquet de 20 images
max_image_size=4000
DENSE_DIR="$SCRIPT_DIR/dense"
LOG_DIR="$DENSE_DIR/logs"
mkdir -p "$DENSE_DIR" "$LOG_DIR"

LOG_FILE="$LOG_DIR/dense_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "COLMAP: $COLMAP"
"$COLMAP" --version || true
echo "bg=processDensePaquet BG_WORK=$BG_WORK"
echo "bg=processDensePaquet DENSE_DIR=$DENSE_DIR"
echo "bg=processDensePaquet LOG_FILE=$LOG_FILE"
echo "bg=processDensePaquet max_image_size=$max_image_size"
echo "bg=processDensePaquet SCRIPT_DIR=$SCRIPT_DIR"


mkdir -p "$SCRIPT_DIR/sparse/0" "$LOG_DIR"

echo "bg=processDensePaquet process=dense  etape=model_converter  comment=converti_bin_en_txt date=$(date -Is)"
$COLMAP model_converter \
  --input_path "$SCRIPT_DIR" \
  --output_path "$SCRIPT_DIR/sparse/0" \
  --output_type BIN
  
  	for f in images.bin points3D.bin cameras.bin; do
  		p="$SCRIPT_DIR/sparse/0/$f"
  		if [ -f "$p" ]; then
   				 printf '%s\t%s bytes\n' "$p" "$(stat -c '%s' "$p")"
  		else
    			printf '%s\tMISSING\n' "$p"
  		fi
	done

  echo "bg=processDensePaquet process=dense  etape=image_undistorter   date=$(date -Is)"
"$COLMAP" image_undistorter \
  --image_path "$BG_WORK/images" \
  --input_path "$SCRIPT_DIR/sparse/0" \
  --output_path "$DENSE_DIR" \
  --output_type COLMAP \
  --max_image_size $max_image_size \
  --num_threads 4
  # Optionnel (si supporté par ton build, recommandé pour aller plus vite):
  # --max_image_size 2000

echo "bg=processDensePaquet process=dense  etape=patch_match_stereo   date=$(date -Is)"
"$COLMAP" patch_match_stereo \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --PatchMatchStereo.max_image_size $max_image_size \
  --PatchMatchStereo.cache_size 12 \
  --PatchMatchStereo.num_threads 2 \
  --PatchMatchStereo.num_iterations 3 \
  --PatchMatchStereo.allow_missing_files 1 \
 # --PatchMatchStereo.num_samples 10
  

echo "bg=processDensePaquet process=dense  etape=stereo_fusion   date=$(date -Is)"
"$COLMAP" stereo_fusion \
  --workspace_path "$DENSE_DIR" \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path "$DENSE_DIR/fused.ply" \
  --StereoFusion.use_cache 1 \
  --StereoFusion.cache_size 8 \
  --StereoFusion.num_threads 4 \
  --StereoFusion.check_num_images 16 \
  --StereoFusion.max_image_size $max_image_size

echo "bg=processDensePaquet process=dense  etape=poisson_mesher   date=$(date -Is)"
"$COLMAP" poisson_mesher \
  --input_path "$DENSE_DIR/fused.ply" \
  --output_path "$DENSE_DIR/mesh_poisson.ply" \
  --PoissonMeshing.depth 11 \
  --PoissonMeshing.trim 10 \
  --PoissonMeshing.point_weight 1 \
  --PoissonMeshing.color 1 \
  --PoissonMeshing.num_threads 4
  

OUT_LAS="fused.las"
PDAL="$HOME/workspaceCpp/PDAL/build/bin/pdal"
echo "bg=colmap process="PDAL" PDAL=$PDAL step=translate_to_las OUT_LAS=$OUT_LAS date=$(date -Is)"
"$PDAL" translate "$DENSE_DIR/fused.ply" "$OUT_LAS"

echo "bg=processDensePaquet process=dense  etape=FIN   date=$(date -Is)"
echo "bg=processDensePaquet fused.ply=$DENSE_DIR/fused.ply"
echo "bg=processDensePaquet mesh_poisson.ply=$DENSE_DIR/mesh_poisson.ply"



