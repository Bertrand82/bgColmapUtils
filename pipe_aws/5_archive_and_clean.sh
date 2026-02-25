#!/usr/bin/env bash
#set -euo pipefail
#
echo  "------------------------------------------"
echo  " Archive and Clean "
echo  "------------------------------------------"
echo
source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"

ARCHIVE="${OUTPUT_DIR}__$(date +%Y_%m_%d)"
echo "ARCHIVE    : $ARCHIVE"
mv $OUTPUT_DIR $ARCHIVE



#
