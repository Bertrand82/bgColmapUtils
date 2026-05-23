## Générer un set d'image avec les metadata (metadataCSV.txt) et le fichiers de match (match.txt)

  - bg.display.together.gui.MainDisplayToGether
  

## Changement Version CUDA et drivers NVIDIA

Penser à recompiler colmap après une mise à jour du driver nvidia
Pour recompiler colmap: 
	- S'assurer nvcc (nvidia cc) est installé ( >nvcc --version)
	

## Pour passer ubuntu en mode terminal 

La carte nvidia semble mieux marcher et plus vite sans le mode graphique.
'''
sudo systemctl stop gdm
'''
## Prompt comment merger des mesh: 
 
  
  Comment merger plusieurs meshes en ligne de commande ?


  - Mettre plusieurs objets mesh .ply dans un seul fichier
  - concaténer plusieurs meshes dans un seul mesh/fichier
  - Fusionner géométriquement les meshes
  - souder les sommets communs
  - enlever les doublons
  - nettoyer les intersections
  - Faire une union booléenne


Je cherche des solutions en ligne de commande, de préférence avec des outils comme MeshLab, Open3D ou autres.
Merci de préciser pour chaque solution :

 - la commande exacte
 - la différence entre concaténation, fusion géométrique et union booléenne
  - les avantages / limites de chaque outil
  
Si possible, donne aussi :
  - la solution la plus robuste pour automatiser ça dans un script.
  
## commande linux

nvidia-smi
watch -n 1 nvidia-smi
nvidia-smi -q
  