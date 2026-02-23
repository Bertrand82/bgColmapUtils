@echo off
echo ========================================
echo COLMAP - Reconstruction 3D (manuel) bg 01
echo ========================================
echo.
@echo off

mkdir "%CD%\colmap_project\sparse" 2>null
mkdir "%CD%\colmap_project\sparse\prior" 2>null
mkdir "%CD%\colmap_project\sparse\prior\bg" 2>null
echo REPERTOIRE_IMAGE  :  %REPERTOIRE_IMAGES%
echo ========  feature_extractor ================================


echo ========================================
echo COLMAP - Reconstruction 3D (manuel) bg 01
echo ========================================
echo.
echo REPERTOIRE_IMAGE : %REPERTOIRE_IMAGES%
echo 
echo ========  feature_extractor ================================
echo.

REM IMPORTANT: assure-toi que colmap_project existe

docker run --rm ^
  --memory="12g" ^
  --memory-swap="16g" ^
  --shm-size="4g" ^
  -v "%CD%\colmap_project:/data" ^
  -v "%REPERTOIRE_IMAGES%:/images" ^
  colmap/colmap ^
  colmap feature_extractor ^
    --database_path /data/database.db ^
    --image_path /images ^
    --ImageReader.single_camera 1 ^
    --ImageReader.camera_model SIMPLE_RADIAL ^
	--FeatureExtraction.use_gpu 0 ^
	--FeatureExtraction.num_threads 1 ^
    --log_level 2
	  
pause

echo xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx