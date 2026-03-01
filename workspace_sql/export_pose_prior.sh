sqlite3 -header -csv database.db \
"SELECT
  image_id,
  ieee754_from_blob(CAST(substr(position, 8,1)||substr(position, 7,1)||substr(position, 6,1)||substr(position, 5,1)||substr(position, 4,1)||substr(position, 3,1)||substr(position, 2,1)||substr(position, 1,1) AS BLOB)) AS position_x,
  ieee754_from_blob(CAST(substr(position,16,1)||substr(position,15,1)||substr(position,14,1)||substr(position,13,1)||substr(position,12,1)||substr(position,11,1)||substr(position,10,1)||substr(position, 9,1) AS BLOB)) AS position_y,
  ieee754_from_blob(CAST(substr(position,24,1)||substr(position,23,1)||substr(position,22,1)||substr(position,21,1)||substr(position,20,1)||substr(position,19,1)||substr(position,18,1)||substr(position,17,1) AS BLOB)) AS position_z
FROM pose_priors
ORDER BY image_id;" \
> pose_priors_xyz.csv