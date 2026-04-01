echo COLMAP EXTRACTOR


colmap feature_extractor --help
echo xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
dir D:\TEMP\imagesQuery
echo xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
colmap feature_extractor ^
    --database_path D:/TEMP/database.db ^
    --image_path D:/TEMP/imagesQuery ^
    --ImageReader.single_camera 1 ^
    --ImageReader.camera_model SIMPLE_RADIAL ^
	--FeatureExtraction.use_gpu 0 ^
	--FeatureExtraction.num_threads 1 ^
    --log_level 2


pause
