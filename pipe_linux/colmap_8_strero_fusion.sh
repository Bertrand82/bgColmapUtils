#!/usr/bin/env bash
set -euo pipefail

HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"
echo $HOME_COLMAP
ls $HOME_COLMAP

  


  
 $HOME_COLMAP/colmap stereo_fusion \
  --workspace_path /data/BG/dense \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path /data/BG/dense/fused.ply
  
 $HOME_COLMAP/colmap delaunay_mesher --input_path /data/BG/dense --output_path /data/BG/dense/mesh_delaunay.plycolmap 
 
 $HOME_COLMAP/colmap poisson_mesher \
  --input_path /data/BG/dense \
  --output_path /data/BG/dense/mesh_poisson.ply
