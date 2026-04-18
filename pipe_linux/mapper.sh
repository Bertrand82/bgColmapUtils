COLMAP_CMD="/home/bertrand/workspaceCpp/colmap/build/src/colmap/exe/colmap"
BG_WORK=/data/BG



echo "xxxxxx CONVERTER xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Export PLY
"$COLMAP_CMD" model_converter \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/0/points3D.ply" \
  --output_type PLY

# Export TXT (cameras.txt / images.txt / points3D.txt)
"$COLMAP_CMD" model_converter \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/0" \
  --output_type TXT
  
echo "xxxxx Fin processColmap.sh xxxxxxxxxxxxxxxxxxxxxxxxx"
