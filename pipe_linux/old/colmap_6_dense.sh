#!/usr/bin/env bash
set -euo pipefail
HOME_COLMAP="$HOME/workspaceCpp/colmap/build/src/colmap/exe"
$HOME_COLMAP/colmap image_undistorter \
  --image_path /data/BG/images \
  --input_path /data/BG/sparse/0 \
  --output_path /data/BG/dense \
  --output_type COLMAP
  

$HOME_COLMAP/colmap patch_match_stereo \
  --workspace_path /data/BG/dense \
  --workspace_format COLMAP
  
  
 $HOME_COLMAP/colmap stereo_fusion \
  --workspace_path /data/BG/dense \
  --workspace_format COLMAP \
  --input_type geometric \
  --output_path /data/BG/dense/fused.ply
  
  
$HOME_COLMAP/colmap poisson_mesher \
  --input_path /data/BG/dense \
  --output_path /data/BG/dense/mesh_poisson.ply
