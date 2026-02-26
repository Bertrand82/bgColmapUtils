#!/usr/bin/env bash

# Chemin absolu du dossier où se trouve ce fichier (peu importe d'où tu le "source")
export WORK_DIRECTORY="/data/vol_0"


# (optionnel) quelques chemins utiles
export IMAGES_DIR="${WORK_DIRECTORY}/images"
export OUTPUT_DIR="${WORK_DIRECTORY}/output"
#exhaustive_matcher COLMAP_MATCHER, sequential_matcher , spatial_matcher , vocab_tree_matcher , transitive_matcher
export COLMAP_MATCHER="sequential_matcher"
export LOG="$OUTPUT_DIR/log.txt"
export DATABASE_NAME="database.db"
export DATABASE_PATH="$OUTPUT_DIR/$DATABASE_NAME"
echo " OUTPUT_DIR     : $OUTPUT_DIR  "
echo " IMAGES_DIR     : $IMAGES_DIR "
echo " COLMAP_MATCHER : $COLMAP_MATCHER"

