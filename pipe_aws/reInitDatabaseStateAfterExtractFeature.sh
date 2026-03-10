#!/usr/bin/env bash
set -euo pipefail

ARCHIVE_DIR="/data/vol_pitch_60/output__ARCHIVE___2026_03_09__14_41_04"
TEMP_DIR="${ARCHIVE_DIR}/TEMP"

SRC_DB="${ARCHIVE_DIR}/database.db"
DST_DB="${TEMP_DIR}/database.db"

mkdir -p "${TEMP_DIR}"

# Copie la DB dans TEMP
cp -f "${SRC_DB}" "${DST_DB}"

# Vide les tables de matching (sans toucher aux features)
sqlite3 "${DST_DB}" <<'SQL'
PRAGMA foreign_keys=OFF;
DELETE FROM matches;
DELETE FROM two_view_geometries;
SQL

# Optionnel: compacter la DB
sqlite3 "${DST_DB}" "VACUUM;"

echo "OK: DB reset (features conservées, matches supprimés) -> ${DST_DB}"
