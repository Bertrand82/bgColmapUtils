# Initialize the configuration (`bgInitConfig.sh`) before running the scripts

This repository uses `bgInitConfig.sh` to define variables used by the scripts.

It must be configured before each run (Where are the images? Where is the output directory? and latter Which COLMAP version? With or without NVIDIA?)

This file is sourced by each of the  scripts.

The processing scripts must be run in order (`1_feature_extractor`, then `2_matcher`, etc.).

TODO
  - a global sscript
  - implement other mapper 
  - clean output before start
  - use gps infos

