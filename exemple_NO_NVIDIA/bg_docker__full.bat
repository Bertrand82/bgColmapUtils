@echo off
echo ========================================
echo COLMAP - Reconstruction 3D (manuel) bg 01
echo ========================================
echo.


mkdir "%CD%\colmap_project\sparse" 2>null
mkdir "%CD%\colmap_project\sparse\prior" 2>null
mkdir "%CD%\colmap_project\sparse\prior\bg" 2>null


echo ========  feature_extractor ================================

docker run --rm ^
  --memory="12g" ^
  --memory-swap="16g" ^
  --shm-size="4g" ^
  -v "%CD%\colmap_project:/data" ^
  colmap/colmap ^
  colmap feature_extractor ^
    --database_path /data/database.db ^
    --image_path /data/images ^
    --ImageReader.single_camera 1 ^
    --ImageReader.camera_model SIMPLE_RADIAL ^
	--FeatureExtraction.use_gpu 0
	  
pause

docker run --rm ^
  --memory="12g" ^
  --memory-swap="16g" ^
  --shm-size="4g" ^
  -v "%CD%\colmap_project:/data" ^
  colmap/colmap ^
  colmap exhaustive_matcher ^
    --database_path /data/database.db ^
	--FeatureMatching.use_gpu 0


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