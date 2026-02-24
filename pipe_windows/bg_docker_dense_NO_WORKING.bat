@echo off
setlocal
echo ========================================
echo COLMAP - Dense reconstruction (MVS)
echo ========================================
echo.

set "WS=%CD%\colmap_project"
echo %WS%

REM (optionnel) repartir de zero cote dense
mkdir colmap_project\dense

REM 1) Undistort images for dense reconstruction
docker run --rm ^
  --memory=12g ^
  --memory-swap=16g ^
  --shm-size=4g ^
  -v "%WS%:/data" ^
  colmap/colmap ^
  colmap image_undistorter ^
    --image_path /data/images ^
    --input_path /data/sparse/0 ^
    --output_path /data/dense ^
    --output_type COLMAP
if errorlevel 1 (pause & exit /b %errorlevel%)

REM 2) Compute depth maps (PatchMatch Stereo)
docker run --rm ^
  --memory=12g ^
  --memory-swap=16g ^
  --shm-size=4g ^
  -v "%WS%:/data" ^
  colmap/colmap ^
  colmap patch_match_stereo ^
    --workspace_path /data/dense ^
    --workspace_format COLMAP ^
    --PatchMatchStereo.geom_consistency true
if errorlevel 1 (pause & exit /b %errorlevel%)

REM 3) Fuse depth maps into dense point cloud
docker run --rm ^
  --memory=12g ^
  --memory-swap=16g ^
  --shm-size=4g ^
  -v "%WS%:/data" ^
  colmap/colmap ^
  colmap stereo_fusion ^
    --workspace_path /data/dense ^
    --workspace_format COLMAP ^
    --input_type geometric ^
    --output_path /data/dense/fused.ply
if errorlevel 1 (pause & exit /b %errorlevel%)

echo.
echo Dense point cloud written to:
echo   %WS%\dense\fused.ply
echo ========================================
pause
endlocal