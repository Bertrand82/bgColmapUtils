# COLMAP Notes

## Docker

[Docker image tag](https://hub.docker.com/r/colmap/colmap/tags?page=1)


## GPU (Graphics Processing Unit)

**Disable GPU for feature extraction:**
- `--FeatureExtraction.use_gpu=false`

If you do not have a CUDA-enabled GPU but some other GPU, you can use all COLMAP functionality except the dense reconstruction part. However, you can use external dense reconstruction software as an alternative, as described in the:ref:`Tutorial <dense-reconstruction>`. If you have a GPU with low compute power or you want to execute COLMAP on a machine without an attached display and without CUDA support, you can run all steps on the CPU by specifying the appropriate options (e.g., `--FeatureExtraction.use_gpu=false` for the feature extraction step).

But note that this might result in a significant slow-down of the reconstruction pipeline. Please, also note that feature extraction on the CPU can consume excessive RAM for large images in the default settings, which might require manually reducing the maximum image size using `--FeatureExtraction.max_image_size` and/or setting `--SiftExtraction.first_octave 0` or by manually limiting the number of threads using `--FeatureExtraction.num_threads`.

---

## feature_extractor

Command:
- `colmap feature_extractor --help`

Output:

```text
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
```

---

## exhaustive_matcher

Command:
- `colmap exhaustive_matcher --help`

Output:

```text
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
  --FeatureMatching.gpu_index](#)
