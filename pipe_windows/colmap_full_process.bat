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

echo MATCH
 
 
colmap matches_importer  --help 
 
colmap matches_importer ^
--database_path D:/TEMP/database.db ^
--FeatureMatching.use_gpu 0 ^
--match_list_path D:/TEMP/match.txt

echo MAPPER

mkd D:/TEMP/sparse
colmap mapper ^
    --database_path D:/TEMP/database.db ^
    --image_path D:/TEMP/images ^
    --output_path D:/TEMP/sparse
	
echo CONVERTER
 colmap model_converter ^
    --input_path D:/TEMP/sparse/0 ^
    --output_path D:/TEMP/sparse/0/points3D.ply ^
    --output_type PLY
	
colmap model_converter ^
    --input_path D:/TEMP/sparse/0 ^
    --output_path D:/TEMP/sparse/0 ^
    --output_type TXT
	
