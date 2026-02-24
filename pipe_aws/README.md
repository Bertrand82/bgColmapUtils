# Initialize the configuration (`bgInitConfig.sh`) before running the scripts

This repository uses `bgInitConfig.sh` to define variables used by the scripts.

It must be configured before each run (Where are the images? Where is the output directory? Which COLMAP version? With or without NVIDIA?)

This file is sourced by each of the other bash scripts.

The processing scripts must be run in order (`bg_1_xxx`, then `bg_2_xxx`, etc.).

