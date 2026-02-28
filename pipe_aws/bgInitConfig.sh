#!/usr/bin/env bash

# Chemin absolu du dossier où se trouve ce fichier (peu importe d'où tu le "source")
export WORK_DIRECTORY="/data/vol_0"


# (optionnel) quelques chemins utiles
export IMAGES_DIR="${WORK_DIRECTORY}/images"
export OUTPUT_DIR="${WORK_DIRECTORY}/output"
#exhaustive_matcher COLMAP_MATCHER, sequential_matcher , spatial_matcher , vocab_tree_matcher , transitive_matcher
export COLMAP_MATCHER="sequential_matcher"
export LOG="$OUTPUT_DIR/log.txt"
mkdir -p $OUTPUT_DIR
touch $LOG
export DATABASE_NAME="database.db"
export DATABASE_PATH="$OUTPUT_DIR/$DATABASE_NAME"
export NVIDIA_USE_GPU=0

if command -v nvidia-smi >/dev/null 2>&1 && nvidia-smi -L >/dev/null 2>&1; then
  NVIDIA_USE_GPU=1
  echo "NVIDIA_GPU=YES" >> $LOG
  nvidia-smi -L | sed 's/^/NVIDIA_GPU_INFO: /'
else
  echo "NVIDIA_GPU=NO" >> $LOG
fi

export NVIDIA_USE_GPU
echo "NVIDIA_USE_GPU=$NVIDIA_USE_GPU" 
echo " OUTPUT_DIR     : $OUTPUT_DIR  "
echo " IMAGES_DIR     : $IMAGES_DIR "
echo " COLMAP_MATCHER : $COLMAP_MATCHER"
echo " NVIDIA_USE_GPU : $NVIDIA_USE_GPU"

DB_SIZE_BYTES=$(stat -c '%s' "$DATABASE_PATH" 2>/dev/null || echo 0)
DB_SIZE_HUMAN=$(du -h "$DATABASE_PATH" 2>/dev/null | awk '{print $1}' || echo NA)

echo "DB_SIZE_BYTES=$DB_SIZE_BYTES" >> $LOG
echo "DB_SIZE_HUMAN=$DB_SIZE_HUMAN" >> $LOG
