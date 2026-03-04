@echo off
setlocal
cd /d "%~dp0"

set "COLMAP_ROOT=C:\Program Files (x86)\colmap\colmap-x64-windows-nocuda"
set "COLMAP_EXE=%COLMAP_ROOT%\bin\colmap.exe"

set "PATH=%COLMAP_ROOT%\bin;%PATH%"
set "QT_PLUGIN_PATH=%COLMAP_ROOT%\plugins"
set "QT_QPA_PLATFORM_PLUGIN_PATH=%COLMAP_ROOT%\plugins\platforms"
set "QT_QPA_PLATFORM=windows"

del /q "colmap_project\database.db" 2>nul
rmdir /s /q "colmap_project\sparse_noprior" 2>nul

mkdir "colmap_project\sparse_noprior" 2>nul

echo [1/3] feature_extractor
"%COLMAP_EXE%" feature_extractor ^
  --database_path "colmap_project\database.db" ^
  --image_path "colmap_project\images" ^
  --ImageReader.single_camera 1 ^
  --ImageReader.camera_model SIMPLE_RADIAL ^
  --SiftExtraction.max_num_features 20000
if errorlevel 1 (pause & exit /b %errorlevel%)

echo [2/3] exhaustive_matcher
"%COLMAP_EXE%" exhaustive_matcher ^
  --database_path "colmap_project\database.db"
if errorlevel 1 (pause & exit /b %errorlevel%)

echo [3/3] mapper (permissif)
"%COLMAP_EXE%" mapper ^
  --database_path "colmap_project\database.db" ^
  --image_path "colmap_project\images" ^
  --output_path "colmap_project\sparse_noprior" ^
  --Mapper.init_min_num_inliers 30 ^
  --Mapper.abs_pose_min_num_inliers 15
if errorlevel 1 (pause & exit /b %errorlevel%)

pause
endlocal