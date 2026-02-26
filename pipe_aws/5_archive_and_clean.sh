#!/usr/bin/env bash
#set -euo pipefail
#
echo  "------------------------------------------"
echo  " Archive and Clean "
echo  "------------------------------------------"
echo
source ./bgInitConfig.sh
echo "OUTPUT_DIR:  $OUTPUT_DIR"

ARCHIVE="${OUTPUT_DIR}__ARCHIVE___$(date +%Y_%m_%d__%H_%M_%S)"
echo "ARCHIVE    : $ARCHIVE"
mv $OUTPUT_DIR $ARCHIVE
cp bgInitConfig.sh ${ARCHIVE}/IniConfig.sh.archive



#
