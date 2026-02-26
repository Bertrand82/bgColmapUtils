

xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

https://hub.docker.com/r/colmap/colmap/tags?page=1


*************************************************** GPU (Graphics Processing Unit)
--FeatureExtraction.use_gpu=false
If you do not have a CUDA-enabled GPU but some other GPU, you can use all COLMAP functionality except the dense reconstruction part. However, you can use external dense reconstruction software as an alternative, as described in the:ref:`Tutorial <dense-reconstruction>`. If you have a GPU with low compute power or you want to execute COLMAP on a machine without an attached display and without CUDA support, you can run all steps on the CPU by specifying the appropriate options (e.g., ``--FeatureExtraction.use_gpu=false`` for the feature extraction step).
But note that this might result in a significant slow-down of the reconstruction pipeline. Please, also note that feature extraction on the CPU can consume excessive RAM for large images in the default settings, which might require manually reducing the maximum image size using
``--FeatureExtraction.max_image_size`` and/or setting
``--SiftExtraction.first_octave 0`` or by manually limiting the number of threads using ``--FeatureExtraction.num_threads``.


====================================================== feature_extractor
colmap feature_extractor --help
I20260221 20:49:56.229118 18720 option_manager.cc:937] COLMAP 3.13.0 (Commit 0b31f98 on 2025-11-07 without CUDA)
I20260221 20:49:56.230188 18720 option_manager.cc:939] Options can either be specified via command-line or by defining them in a .ini project file passed to `--project_path`.
  -h [ --help ]
  --default_random_seed arg (=0)
  --log_to_stderr arg (=1)
  --log_level arg (=0)
  --project_path arg
  --database_path arg
  --image_path arg
  --camera_mode arg (=-1)
  --image_list_path arg
  --descriptor_normalization arg (=l1_root)
                                        {'l1_root', 'l2'}
  --ImageReader.mask_path arg
  --ImageReader.camera_model arg (=SIMPLE_RADIAL)
  --ImageReader.single_camera arg (=0)
  --ImageReader.single_camera_per_folder arg (=0)
  --ImageReader.single_camera_per_image arg (=0)
  --ImageReader.existing_camera_id arg (=-1)
  --ImageReader.camera_params arg
  --ImageReader.default_focal_length_factor arg (=1.2)
  --ImageReader.camera_mask_path arg
  --FeatureExtraction.type arg (=SIFT)
  --FeatureExtraction.num_threads arg (=-1)
  --FeatureExtraction.use_gpu arg (=1)
  --FeatureExtraction.gpu_index arg (=-1)
  --SiftExtraction.max_image_size arg (=3200)
  --SiftExtraction.max_num_features arg (=8192)
  --SiftExtraction.first_octave arg (=-1)
  --SiftExtraction.num_octaves arg (=4)
  --SiftExtraction.octave_resolution arg (=3)
  --SiftExtraction.peak_threshold arg (=0.00667)
  --SiftExtraction.edge_threshold arg (=10)
  --SiftExtraction.estimate_affine_shape arg (=0)
  --SiftExtraction.max_num_orientations arg (=2)
  --SiftExtraction.upright arg (=0)
  --SiftExtraction.domain_size_pooling arg (=0)
  --SiftExtraction.dsp_min_scale arg (=0.167)
  --SiftExtraction.dsp_max_scale arg (=3)
  --SiftExtraction.dsp_num_scales arg (=10)
============================================================  exhaustive_matcher 
colmap exhaustive_matcher --help
I20260221 21:17:37.286848  3532 option_manager.cc:937] COLMAP 3.13.0 (Commit 0b31f98 on 2025-11-07 without CUDA)
I20260221 21:17:37.288066  3532 option_manager.cc:939] Options can either be specified via command-line or by defining them in a .ini project file passed to `--project_path`.
  -h [ --help ]
  --default_random_seed arg (=0)
  --log_to_stderr arg (=1)
  --log_level arg (=0)
  --project_path arg
  --database_path arg
  --FeatureMatching.type arg (=SIFT)
  --FeatureMatching.num_threads arg (=-1)
  --FeatureMatching.use_gpu arg (=1)
  --FeatureMatching.gpu_index arg (=-1)
  --FeatureMatching.guided_matching arg (=0)
  --FeatureMatching.rig_verification arg (=0)
  --FeatureMatching.max_num_matches arg (=32768)
  --SiftMatching.max_ratio arg (=0.8)
  --SiftMatching.max_distance arg (=0.7)
  --SiftMatching.cross_check arg (=1)
  --SiftMatching.cpu_brute_force_matcher arg (=0)
  --TwoViewGeometry.min_num_inliers arg (=15)
  --TwoViewGeometry.multiple_models arg (=0)
  --TwoViewGeometry.compute_relative_pose arg (=0)
  --TwoViewGeometry.detect_watermark arg (=1)
  --TwoViewGeometry.multiple_ignore_watermark arg (=1)
  --TwoViewGeometry.watermark_detection_max_error arg (=4)
  --TwoViewGeometry.filter_stationary_matches arg (=0)
  --TwoViewGeometry.stationary_matches_max_error arg (=4)
  --TwoViewGeometry.max_error arg (=4)
  --TwoViewGeometry.confidence arg (=0.999)
  --TwoViewGeometry.max_num_trials arg (=10000)
  --TwoViewGeometry.min_inlier_ratio arg (=0.25)
  --TwoViewGeometry.random_seed arg (=-1)
  --ExhaustiveMatching.block_size arg (=50)





========================================================
>colmap mapper --help
I20260221 21:13:49.949915  8152 option_manager.cc:937] COLMAP 3.13.0 (Commit 0b31f98 on 2025-11-07 without CUDA)
I20260221 21:13:49.951078  8152 option_manager.cc:939] Options can either be specified via command-line or by defining them in a .ini project file passed to `--project_path`.
  -h [ --help ]
  --default_random_seed arg (=0)
  --log_to_stderr arg (=1)
  --log_level arg (=0)
  --project_path arg
  --database_path arg
  --image_path arg
  --input_path arg
  --output_path arg
  --Mapper.min_num_matches arg (=15)
  --Mapper.ignore_watermarks arg (=0)
  --Mapper.multiple_models arg (=1)
  --Mapper.max_num_models arg (=50)
  --Mapper.max_model_overlap arg (=20)
  --Mapper.min_model_size arg (=10)
  --Mapper.init_image_id1 arg (=-1)
  --Mapper.init_image_id2 arg (=-1)
  --Mapper.init_num_trials arg (=200)
  --Mapper.extract_colors arg (=1)
  --Mapper.num_threads arg (=-1)
  --Mapper.random_seed arg (=-1)
  --Mapper.min_focal_length_ratio arg (=0.1)
  --Mapper.max_focal_length_ratio arg (=10)
  --Mapper.max_extra_param arg (=1)
  --Mapper.ba_refine_focal_length arg (=1)
  --Mapper.ba_refine_principal_point arg (=0)
  --Mapper.ba_refine_extra_params arg (=1)
  --Mapper.ba_refine_sensor_from_rig arg (=1)
  --Mapper.ba_local_function_tolerance arg (=0)
  --Mapper.ba_local_max_num_iterations arg (=25)
  --Mapper.ba_global_frames_ratio arg (=1.1)
  --Mapper.ba_global_points_ratio arg (=1.1)
  --Mapper.ba_global_frames_freq arg (=500)
  --Mapper.ba_global_points_freq arg (=250000)
  --Mapper.ba_global_function_tolerance arg (=0)
  --Mapper.ba_global_max_num_iterations arg (=50)
  --Mapper.ba_global_max_refinements arg (=5)
  --Mapper.ba_global_max_refinement_change arg (=0.0005)
  --Mapper.ba_local_max_refinements arg (=2)
  --Mapper.ba_local_max_refinement_change arg (=0.001)
  --Mapper.ba_use_gpu arg (=0)
  --Mapper.ba_gpu_index arg (=-1)
  --Mapper.ba_min_num_residuals_for_cpu_multi_threading arg (=50000)
  --Mapper.snapshot_path arg
  --Mapper.snapshot_frames_freq arg (=0)
  --Mapper.fix_existing_frames arg (=0)
  --Mapper.init_min_num_inliers arg (=100)
  --Mapper.init_max_error arg (=4)
  --Mapper.init_max_forward_motion arg (=0.95)
  --Mapper.init_min_tri_angle arg (=16)
  --Mapper.init_max_reg_trials arg (=2)
  --Mapper.abs_pose_max_error arg (=12)
  --Mapper.abs_pose_min_num_inliers arg (=30)
  --Mapper.abs_pose_min_inlier_ratio arg (=0.25)
  --Mapper.filter_max_reproj_error arg (=4)
  --Mapper.filter_min_tri_angle arg (=1.5)
  --Mapper.max_reg_trials arg (=3)
  --Mapper.ba_local_num_images arg (=6)
  --Mapper.ba_local_min_tri_angle arg (=6)
  --Mapper.ba_global_ignore_redundant_points3D arg (=0)
  --Mapper.ba_global_ignore_redundant_points3D_min_coverage_gain arg (=0.05)
  --Mapper.image_list_path arg
  --Mapper.constant_rig_list_path arg
  --Mapper.constant_camera_list_path arg
  --Mapper.max_runtime_seconds arg (=-1)
  --Mapper.tri_max_transitivity arg (=1)
  --Mapper.tri_create_max_angle_error arg (=2)
  --Mapper.tri_continue_max_angle_error arg (=2)
  --Mapper.tri_merge_max_reproj_error arg (=4)
  --Mapper.tri_complete_max_reproj_error arg (=4)
  --Mapper.tri_complete_max_transitivity arg (=5)
  --Mapper.tri_re_max_angle_error arg (=5)
  --Mapper.tri_re_min_ratio arg (=0.2)
  --Mapper.tri_re_max_trials arg (=1)
  --Mapper.tri_min_angle arg (=1.5)
  --Mapper.tri_ignore_two_view_tracks arg (=1)

C:\Users\bertr\Pictures\bgColmapExemple\exemple_0>








































































