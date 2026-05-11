#!/usr/bin/env bash
set -euo pipefail

PDAL=~/workspaceCpp/PDAL/build/bin/pdal
COLMAP=~/workspaceCpp/colmap/build/src/colmap/exe/colmap
BGOpen3D=~/workspaceCpp/bgOpen3D/build/bgOpen3D

OUT_MERGED="merged_open3D.ply"
OUT_MERGED_FILTRED="merged_open3D_filtred.ply"
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
    echo "bg=processMergePoissonPLY fichier=$fichierPLY"
    ply_files+=("$fichierPLY")
  else
    echo "bg=processMergePoissonPLY manquant=$fichierPLY" >&2
  fi
done

# Vérif: au moins 1 fichier
if (( ${#ply_files[@]} == 0 )); then
  echo "bg=processMergePoissonPLY error=Aucun fichier PLY trouvé (paquet_*/dense/fused.ply)" >&2
  exit 1
fi

# Générer un pipeline BgOpen3D pour fusionner les PLY

 
echo "bg=processMergePoissonPLY ply_files=${#ply_files[@]} "  


# Exécuter le merge BgOpen3D
echo "bg=processMergePoissonPLY step=merge date=$(date -Is)"
"$BGOpen3D" --merge "$OUT_MERGED" "${ply_files[@]}"  

# Exécuter le post_merge BgOpen3D
echo "bg=processMergePoissonPLY step=post-merge date=$(date -Is)"
"$BGOpen3D" --post-merge --dedup-eps 0.005 --voxel 0.005 "$OUT_MERGED" "$OUT_MERGED_FILTRED"  



# Vérifier que le fichier de sortie a été créé
if [[ -f "$OUT_MERGED" ]]; then
  echo "bg=processMergePoissonPLY Succès: fichier de sortie créé: $OUT_MERGED date=$(date -Is)"
else
  echo "bg=processMergePoissonPLY Échec: fichier de sortie non créé: $OUT_MERGED date=$(date -Is)" >&2
  exit 1
fi 

#Executer le Poisson reconstruction
echo "bg=processMergePoissonPLY step=poisson date=$(date -Is)"
"$BGOpen3D" --poisson --auto-depth "$OUT_POISSON" "$OUT_MERGED"  

# Vérifier que le fichier de sortie a été créé
if [[ -f "$OUT_POISSON" ]]; then
  echo "bg=processMergePoissonPLY Succès: fichier de sortie créé: $OUT_POISSON date=$(date -Is)"
else
  echo "bg=processMergePoissonPLY Échec: fichier de sortie non créé: $OUT_POISSON date=$(date -Is)" >&2
  exit 1
fi  
echo "bg=processMergePoissonPLY step=fin date=$(date -Is)" >&2
  

