@echo off

echo ========  feature_extractor ================================



docker run --rm ^
  --memory="12g" ^
  --memory-swap="16g" ^
  --memory-swap="16g" ^
  --shm-size="4g" ^
  -v "%CD%\colmap_project:/data" ^
  colmap/colmap ^
  colmap mapper ^
    --database_path /data/database.db ^
    --image_path /data/images ^
    --output_path /data/sparse

echo.
echo ========================================
echo Traitement termine COLMAP
echo ========================================
pause