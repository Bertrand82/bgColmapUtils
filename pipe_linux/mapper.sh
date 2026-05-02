# --- Export pour tous les modèles sparse/* (0,1,2,...) ---
echo "bg=colmap process=sparse etape=export_models_start date=$(date -Is)"

shopt -s nullglob

COLMAP_CMD="/home/bertrand/workspaceCpp/colmap/build/src/colmap/exe/colmap"
BG_WORK="/data/BG"
SPARSE_ROOT="$BG_WORK/sparse"

found_any=0
echo "BG SPARSE_ROOT =$SPARSE_ROOT "
for model_dir in "$SPARSE_ROOT"/*; do
  [[ -d "$model_dir" ]] || continue
  model_name="$(basename "$model_dir")"

  # Option: ne garder que les dossiers qui sont des entiers
  if [[ ! "$model_name" =~ ^[0-9]+$ ]]; then
    echo "bg  model_name =$model_name step=skip" 
    continue
  fi
  echo "bg  model_name =$model_name step=loop" 
  # Détecte si un modèle COLMAP existe dans ce dossier
  has_bin=0
  [[ -f "$model_dir/cameras.bin" && -f "$model_dir/images.bin" && -f "$model_dir/points3D.bin" ]] && has_bin=1
  has_txt=0
  [[ -f "$model_dir/cameras.txt" && -f "$model_dir/images.txt" && -f "$model_dir/points3D.txt" ]] && has_txt=1

  if [[ $has_bin -eq 0 && $has_txt -eq 0 ]]; then
    echo "bg=colmap process=sparse model=$model_name etape=skip reason=no_model_files dir=$model_dir date=$(date -Is)"
    continue
  fi

  found_any=1
  echo "bg=colmap process=sparse model=$model_name etape=export_start dir=$model_dir date=$(date -Is)"

  # Export PLY
  "$COLMAP_CMD" model_converter \
    --input_path "$model_dir" \
    --output_path "$model_dir/points3D.ply" \
    --output_type PLY

  # Export TXT (cameras.txt / images.txt / points3D.txt)
  "$COLMAP_CMD" model_converter \
    --input_path "$model_dir" \
    --output_path "$model_dir" \
    --output_type TXT

  # Analyse
  "$COLMAP_CMD" model_analyzer --path "$model_dir"

  echo "bg=colmap process=sparse model=$model_name etape=export_done dir=$model_dir date=$(date -Is)"
done

if [[ $found_any -eq 0 ]]; then
  echo "bg=colmap process=sparse etape=export_models_none reason=no_sparse_models_found root=$SPARSE_ROOT date=$(date -Is)"
fi

echo "bg=colmap process=sparse etape=export_models_end date=$(date -Is)"