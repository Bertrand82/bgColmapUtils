#!/usr/bin/env python3
import os
import sys
import csv
import sqlite3
from pathlib import Path


DIR_HOME = Path("/data/vol_pitch_60/")
DIR_IMAGES = DIR_HOME / "images"
DIR_OUTPUT = DIR_HOME / "output"
DB_PATH = DIR_OUTPUT / "database.db"
METADATA_CSV = DIR_HOME / "metadata.csv"
MATCH_TXT = DIR_OUTPUT / "match.txt"


def echo_and_check_dir(p: Path) -> None:
    print(str(p))
    if not p.exists():
        raise FileNotFoundError(f"Missing path: {p}")
    if not p.is_dir():
        raise NotADirectoryError(f"Not a directory: {p}")


def count_files_in_dir(p: Path) -> int:
    # counts regular files only (not directories); includes all extensions
    return sum(1 for x in p.iterdir() if x.is_file())


def sqlite_count(db_path: Path, sql: str) -> int:
    if not db_path.exists():
        raise FileNotFoundError(f"Missing sqlite database: {db_path}")
    conn = sqlite3.connect(str(db_path))
    try:
        cur = conn.cursor()
        cur.execute(sql)
        row = cur.fetchone()
        return int(row[0]) if row and row[0] is not None else 0
    finally:
        conn.close()


def count_lines_text(path: Path) -> int:
    if not path.exists():
        raise FileNotFoundError(f"Missing file: {path}")
    with path.open("r", encoding="utf-8", errors="replace") as f:
        return sum(1 for _ in f)


def count_lines_csv(path: Path) -> int:
    # Counts all rows (including header if present)
    if not path.exists():
        raise FileNotFoundError(f"Missing file: {path}")
    with path.open("r", encoding="utf-8", errors="replace", newline="") as f:
        reader = csv.reader(f)
        return sum(1 for _ in reader)


def main() -> int:
    try:
        print("== Checking directories ==")
        echo_and_check_dir(DIR_HOME)
        echo_and_check_dir(DIR_IMAGES)
        echo_and_check_dir(DIR_OUTPUT)

        print("\n== Counting files/images ==")
        n_files_images_dir = count_files_in_dir(DIR_IMAGES)
        print(f"Files in DIR_IMAGES: {n_files_images_dir}")

        print("\n== SQLite stats ==")
        n_images_table = sqlite_count(DB_PATH, "SELECT COUNT(*) FROM images;")
        print(f"Rows in database.images: {n_images_table}")

        # In COLMAP database, "matches" stores pair_id plus blob data; typically 1 row per image pair that has matches.
        n_pairs_matches = sqlite_count(DB_PATH, "SELECT COUNT(*) FROM matches;")
        print(f"Rows in database.matches (pairs): {n_pairs_matches}")

        print("\n== Output files ==")
        n_metadata_lines = count_lines_csv(METADATA_CSV)
        print(f"Lines in metadata.csv: {n_metadata_lines}")

        n_match_lines = count_lines_text(MATCH_TXT)
        print(f"Lines in match.txt: {n_match_lines}")

        return 0

    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
