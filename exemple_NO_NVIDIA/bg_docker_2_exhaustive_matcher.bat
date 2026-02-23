@echo off



docker run --rm ^
  --memory="12g" ^
  --memory-swap="16g" ^
  --shm-size="4g" ^
  -v "%CD%\colmap_project:/data" ^
  colmap/colmap ^
  colmap exhaustive_matcher ^
    --database_path /data/database.db ^
	--FeatureMatching.use_gpu 0


pause