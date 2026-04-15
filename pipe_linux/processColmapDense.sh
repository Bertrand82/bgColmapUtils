#!/usr/bin/env bash
set -euo pipefail
HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"
export COLMAP_EXE_PATH="$HOME/workspaceCpp/colmap/build/src/colmap/exe"
# You must set $COLMAP_EXE_PATH to 
# the directory containing the COLMAP executables.
echo "HOME_COLMAP : $HOME_COLMAP" 
BG_WORK="${BG_WORK:-/data/BG}"
echo "BG_WORK : $BG_WORK"  
LOG_DIR="$BG_WORK/logs"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/colmap_$(date +%Y%m%d_%H%M%S).log"
# redirige stdout+stderr vers le log, tout en gardant l'affichage terminal
exec > >(tee -a "$LOG_FILE") 2>&1

echo "----------- bg --- image_undistorter ----------------------------------------------"    
$HOME_COLMAP/colmap image_undistorter \
  --image_path $BG_WORK/images \
  --input_path $BG_WORK/sparse/0 \
  --output_path $BG_WORK/dense \
  --output_type COLMAP
  
echo "----------- bg --- patch_match_stereo ----------------------------------------------"  
$HOME_COLMAP/colmap patch_match_stereo \
  --workspace_path $BG_WORK/dense \
  --workspace_format COLMAP
  
 echo "----------- bg --- stereo_fusion ----------------------------------------------"  

 $HOME_COLMAP/colmap stereo_fusion \
  --workspace_path $BG_WORK/dense \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path $BG_WORK/dense/fused.ply
  
 echo "----------- bg --- poisson_mesher ----------------------------------------------"  
 $HOME_COLMAP/colmap poisson_mesher \
  --input_path "$BG_WORK/dense/fused.ply" \
  --output_path "$BG_WORK/dense/mesh_poisson.ply"

echo "----------- bg --- patch_match_stereo ----------------------------------------------"  
  
$HOME_COLMAP/colmap patch_match_stereo \
  --workspace_path $BG_WORK/dense \
  --workspace_format COLMAP
  
 echo "----------- bg --- patch_match_stereo ----------------------------------------------"  

$HOME_COLMAP/colmap stereo_fusion \
  --workspace_path $BG_WORK/dense \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path $BG_WORK/dense/fused2.ply
 
 echo "----------- bg --- delaunay_mesher ----------------------------------------------"  

 # $HOME_COLMAP/colmap delaunay_mesher --input_path $BG_WORK/dense --output_path $BG_WORK/dense/mesh_delaunay.plycolmap 
 
 echo "----------- bg --- poisson_mesher ----------------------------------------------"  

 $HOME_COLMAP/colmap poisson_mesher \
  --input_path $BG_WORK/dense/fused2.ply \
  --output_path $BG_WORK/dense/mesh_poisson2.ply
  echo " done -----------------------------------------------------"

  directoryCurrent=$(pwd)
  echo "directoryCurrent = $directoryCurrent"
  cleanup() {
  # Toujours revenir au répertoire de départ
  cd "$directoryCurrent" || true
}
trap cleanup EXIT INT TERM
  cd $BG_WORK/dense
  pwd
  echo " run-colmap-geometric.sh -----------------------------------------------------"
  bash $BG_WORK/dense/run-colmap-geometric.sh 
  echo " run-colmap-photometric.sh -----------------------------------------------------"

  bash $BG_WORK/dense/run-colmap-photometric.sh
  echo " done -------------------------------------------"
  cd $directoryCurrent
  pwd
  
