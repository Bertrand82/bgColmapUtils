@echo off
echo ========================================
echo COLMAP - Reconstruction 3D optimisee
echo ========================================
echo.

docker run --rm ^
  -v "%CD%\colmap_project:/data" ^
  colmap/colmap ^
  colmap model_analyzer --path /data/sparse/0

echo.
echo ========================================
echo Traitement termine COLMAP
echo ========================================
pause