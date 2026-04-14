#!/usr/bin/env bash
set -euo pipefail
BG_WORK="/data/BG"
echo $BG_WORK
echo test bgPosePriorsProvider_4_1_0
./build/bgPosePriorsProvider_4_1_0 --write "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo test bgPosePriorsProvider_4_1_0 done