#!/usr/bin/env bash
set -euo pipefail

# Se placer dans le dossier du script (comme %~dp0 en .bat)
cd "$(dirname "$0")"

SRC="main.cpp"
OUT="./bgTransform"
INPUT="metadata.csv"

echo "Compiling..."
g++ -std=c++17 -O2 -Wall -Wextra "$SRC" -o "$OUT"
echo "Compile done"

echo "Running..."
"$OUT" "$INPUT"
