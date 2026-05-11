#!/usr/bin/env bash
set -euo pipefail

PDAL=~/workspaceCpp/PDAL/build/bin/pdal
COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap
BGOpen3D=~/workspaceCpp/bgOpen3D/build/bgOpen3D

OUT_MERGED="merged_open3D.ply"
OUT_POISSON="merged_open3D_poisson_mesh.ply"


echo "bg=processMergePoissonPLY PDAL=$PDAL"
echo "bg=processMergePoissonPLY OUT_MERGED=$OUT_MERGED"
echo "bg=processMergePoissonPLY OUT_POISSON=$OUT_POISSON"
echo "bg=processMergePoissonPLY COLMAP=$COLMAP"
echo "bg=processMergePoissonPLY BGOpen3D=$BGOpen3D"

# On collecte les PLY existants
ply_files=()
for d in paquet_*/; do
  [ -d "$d" ] || continue
  echo "bg=processMergePoissonPLY Dossier=${d%/}"

  fichierPLY="${d%/}/dense/mesh_poisson.ply"
  if [[ -f "$fichierPLY" ]]; then
    echo "bg=processMergePoissonPLY + $fichierPLY"
    ply_files+=("$fichierPLY")
  else
    echo "bg=processMergePoissonPLY ! manquant: $fichierPLY" >&2
  fi
done

# Vérif: au moins 1 fichier
if (( ${#ply_files[@]} == 0 )); then
  echo "bg=processMergePoissonPLY error= Aucun fichier PLY trouvé (paquet_*/dense/fused.ply)" >&2
  exit 1
fi

# Générer un pipeline BgOpen3D pour fusionner les PLY

 
echo "bg=processMergePoissonPLY ply_files: ${#ply_files[@]} fichiers à fusionner"  


# Exécuter le merge BgOpen3D
echo "bg=processMergePoissonPLY Exécution du pipeline BgOpen3D..."
"$BGOpen3D" --merge "$OUT_MERGED" "${ply_files[@]}"  

# Vérifier que le fichier de sortie a été créé
if [[ -f "$OUT_MERGED" ]]; then
  echo "bg=processMergePoissonPLY Succès: fichier de sortie créé: $OUT_MERGED"
else
  echo "bg=processMergePoissonPLY Échec: fichier de sortie non créé: $OUT_MERGED" >&2
  exit 1
fi 

#Executer le Poisson reconstruction
echo "bg=processMergePoissonPLY Exécution du Poisson reconstruction..."
"$BGOpen3D" --poisson --auto-depth "$OUT_POISSON" "$OUT_MERGED"  

# Vérifier que le fichier de sortie a été créé
if [[ -f "$OUT_POISSON" ]]; then
  echo "bg=processMergePoissonPLY Succès: fichier de sortie créé: $OUT_POISSON"
else
  echo "bg=processMergePoissonPLY Échec: fichier de sortie non créé: $OUT_POISSON" >&2
  exit 1
fi  

