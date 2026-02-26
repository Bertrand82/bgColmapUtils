# COLMAP Options: Matchers Overview

## Mapper / Matching Modes (high-level)
- `colmap sequential_matcher`: Best for video or ordered image capture (consecutive frames).
- `colmap spatial_matcher`: Use when you have GPS/priors or a spatial structure, to match only nearby images.
- `colmap vocab_tree_matcher`: Visual vocabulary based matching (scales well to large datasets).
- `colmap transitive_matcher`: Completes/extends matches by transitivity (typically used as a complement).
- `colmap image_retrieval` + matching: Use retrieval first, then run matching (depending on COLMAP version/workflow).

## CLI Matchers Available in COLMAP
In COLMAP, the “classic” command-line matchers are generally:

- `colmap exhaustive_matcher`: Matches all image pairs (expensive, **O(N²)**).
- `colmap sequential_matcher`: Matches mostly neighboring images in a sequence (video / ordered acquisition).
- `colmap spatial_matcher`: Matches images that are close in space (useful with GPS/priors).
- `colmap vocab_tree_matcher`: Matching via a visual vocabulary (scalable; “retrieval” then matching).
- `colmap transitive_matcher`: Adds matches by transitivity from existing matches (usually as a complement, not used alone).