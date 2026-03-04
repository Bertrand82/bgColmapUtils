@echo off
setlocal
cd /d "%~dp0"

set "COLMAP_ROOT=C:\Program Files (x86)\colmap\colmap-x64-windows-nocuda"
set "COLMAP_EXE=%COLMAP_ROOT%\bin\colmap.exe"

set "PATH=%COLMAP_ROOT%\bin;%PATH%"
set "QT_PLUGIN_PATH=%COLMAP_ROOT%\plugins"
set "QT_QPA_PLATFORM_PLUGIN_PATH=%COLMAP_ROOT%\plugins\platforms"
set "QT_QPA_PLATFORM=windows"

REM Repartir de zero (DB neuve)
del /q "colmap_project\database.db" 2>nul

REM Sortie sparse (garde prior)
if exist "colmap_project\sparse" (
  for /d %%D in ("colmap_project\sparse\*") do (
    if /I not "%%~nxD"=="prior" rmdir /s /q "%%D"
  )
) else (
  mkdir "colmap_project\sparse"
)

"%COLMAP_EXE%" feature_extractor ^
  --database_path "colmap_project\database.db" ^
  --image_path "colmap_project\images" ^
  --ImageReader.single_camera 1 ^
  --ImageReader.camera_model SIMPLE_RADIAL
if errorlevel 1 (pause & exit /b %errorlevel%)

"%COLMAP_EXE%" sequential_matcher ^
  --database_path "colmap_project\database.db" ^
  --SequentialMatching.overlap 50
if errorlevel 1 (pause & exit /b %errorlevel%)

"%COLMAP_EXE%" mapper ^
  --database_path "colmap_project\database.db" ^
  --image_path "colmap_project\images" ^
  --input_path "colmap_project\sparse\prior" ^
  --output_path "colmap_project\sparse"
if errorlevel 1 (pause & exit /b %errorlevel%)

pause
endlocal