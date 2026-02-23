@echo off
echo ========================================
echo COLMAP - Reconstruction 3D optimisee
echo ========================================
echo.

docker run --rm ^
  -v "%CD%\colmap_project:/data" ^
  colmap/colmap ^
  colmap model_converter ^
    --input_path /data/sparse/0 ^
    --output_path /data/sparse/0/points3D.ply ^
    --output_type PLY

echo.
echo ========================================
echo Traitement termine COLMAP
echo ========================================
pause