#!/usr/bin/env python3
import csv
import sqlite3
import sys
from pathlib import Path

DIR_HOME = Path("/data/vol_pitch_60/")
DIR_IMAGES = DIR_HOME / "images"
DIR_OUTPUT = DIR_HOME / "output"
DIR_SPARSE = DIR_OUTPUT / "sparse"
DIR_MERGED = DIR_SPARSE / "merged"

DB_PATH = DIR_OUTPUT / "database.db"

METADATA_CSV = DIR_HOME / "metadata.csv"
MATCH_TXT = DIR_HOME / "match.txt"
MERGED_IMAGES_TXT = DIR_MERGED / "images.txt"
MERGED_POINTS3D_TXT = DIR_MERGED / "points3D.txt"


def echo_and_check_dir(p: Path) -> None:
    print(str(p))
    if not p.exists():
        raise FileNotFoundError(f"Missing path: {p}")
    if not p.is_dir():
        raise NotADirectoryError(f"Not a directory: {p}")


def count_files_in_dir(p: Path) -> int:
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
    if not path.exists():
        raise FileNotFoundError(f"Missing file: {path}")
    with path.open("r", encoding="utf-8", errors="replace", newline="") as f:
        return sum(1 for _ in csv.reader(f))


def count_colmap_data_lines(path: Path) -> int:
    """Counts non-empty, non-comment lines (i.e., actual data rows)."""
    if not path.exists():
        raise FileNotFoundError(f"Missing file: {path}")
    n = 0
    with path.open("r", encoding="utf-8", errors="replace") as f:
        for line in f:
            s = line.strip()
            if not s or s.startswith("#"):
                continue
            n += 1
    return n


def count_images_in_colmap_images_txt(path: Path) -> int:
    """
    COLMAP images.txt format: for each image:
      - one data line with: IMAGE_ID, QW QX QY QZ, TX TY TZ, CAMERA_ID, NAME
      - one data line with: POINTS2D[] (can be empty)
    Comments start with '#'. Blank lines may appear.
    So number of images = (number of non-comment data lines) / 2 (floor).
    """
    data_lines = count_colmap_data_lines(path)
    return data_lines // 2


def main() -> int:
    try:
        print("== Checking directories ==")
        echo_and_check_dir(DIR_HOME)
        echo_and_check_dir(DIR_IMAGES)
        echo_and_check_dir(DIR_OUTPUT)
        echo_and_check_dir(DIR_SPARSE)
        echo_and_check_dir(DIR_MERGED)

        print("\n== Counting files in images directory ==")
        print(f"Files in DIR_IMAGES: {count_files_in_dir(DIR_IMAGES)}")

        print("\n== SQLite stats ==")
        print(f"Rows in database.images: {sqlite_count(DB_PATH, 'SELECT COUNT(*) FROM images;')}")
        print(f"Rows in database.matches (pairs): {sqlite_count(DB_PATH, 'SELECT COUNT(*) FROM matches;')}")

        print("\n== Counting lines in files ==")
        print(f"Lines in {METADATA_CSV.name}: {count_lines_csv(METADATA_CSV)}")
        print(f"Lines in {MATCH_TXT.name}: {count_lines_text(MATCH_TXT)}")
        print(f"Lines in {MERGED_IMAGES_TXT}: {count_lines_text(MERGED_IMAGES_TXT)}")
        print(f"Lines in {MERGED_POINTS3D_TXT}: {count_lines_text(MERGED_POINTS3D_TXT)}")

        print("\n== COLMAP model counts (excluding comments) ==")
        n_model_images = count_images_in_colmap_images_txt(MERGED_IMAGES_TXT)
        n_points3d = count_colmap_data_lines(MERGED_POINTS3D_TXT)
        print(f"Reconstructed images in merged/images.txt: {n_model_images}")
        print(f"Points3D in merged/points3D.txt: {n_points3d}")

        return 0
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())