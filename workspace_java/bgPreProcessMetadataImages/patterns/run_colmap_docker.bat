@echo off
setlocal
cd /d "%~dp0"

REM Ajuste l'image Docker si besoin
set "IMAGE=colmap/colmap:latest"

REM Chemin Windows de ton projet (doit contenir images\)
set "PROJ_WIN=D:\aws_drones_images\generated\vol_1\colmap_project"

REM Chemin dans le conteneur
set "PROJ_C=/workspace"

del /q "%PROJ_WIN%\database.db" 2>nul

docker run --rm ^
  -v "%PROJ_WIN%:%PROJ_C%" ^
  %IMAGE% ^
  colmap feature_extractor ^
    --database_path "%PROJ_C%/database.db" ^
    --image_path "%PROJ_C%/images" ^
    --ImageReader.single_camera 1 ^
    --ImageReader.camera_model SIMPLE_RADIAL ^
    --FeatureExtraction.use_gpu false 

docker run --rm ^
  -v "%PROJ_WIN%:%PROJ_C%" ^
  %IMAGE% ^
  colmap sequential_matcher ^
    --database_path "%PROJ_C%/database.db" ^
    --SequentialMatching.overlap 15

docker run --rm ^
  -v "%PROJ_WIN%:%PROJ_C%" ^
  %IMAGE% ^
  colmap mapper ^
    --database_path "%PROJ_C%/database.db" ^
    --image_path "%PROJ_C%/images" ^
    --output_path "%PROJ_C%/sparse"

pause
endlocal