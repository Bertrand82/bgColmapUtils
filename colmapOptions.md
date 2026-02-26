# COLMAP Options: Matchers Overview

## Mapper / Matching Modes (high-level)
- `colmap sequential_matcher`: Best for video or ordered image capture (consecutive frames).
- `colmap spatial_matcher`: Use when you have GPS/priors or a spatial structure, to match only nearby images.
- `colmap vocab_tree_matcher`: Visual vocabulary based matching (scales well to large datasets).
- `colmap transitive_matcher`: Completes/extends matches by transitivity (typically used as a complement).
- `colmap image_retrieval` + matching: Use retrieval first, then run matching (depending on COLMAP version/workflow).


Spatial matcher is the best since  we have access to the XYZ-location of each image.
