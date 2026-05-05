# Pipe_linux

## 1000 images
- Ça plante de manière inexpliquée.
- Peut-être changer pour un bash/terminal plus robuste : **tmux** (terminal qui détache le job de la session).

## 200 images
- En cours

```bash
~/workspaceCpp/colmap/build/src/colmap/exe/colmap patch_match_stereo --help
~/workspaceCpp/colmap/build/src/colmap/exe/colmap stereo_fusion --help
```
  - 60 images en 4032: ca plante pendant dense/patch_match_stereo 
  - Image size : 4000, sparse de 2000 images , paquet 50 , l'etape fusion échoue (Trop longue arret apres 10 heures)
  
## Plantage par manque de mémoire

### Commande
```bash
sudo dmesg -T | tail -n 200 | egrep -i 'out of memory|oom|killed process|colmap'
```

### Sortie
```text
[sudo] Mot de passe de bertrand : 
[dim. 19 avril 18:03:30 2026] [  pid  ]   uid  tgid total_vm      rss rss_anon rss_file rss_shmem pgtables_bytes swapents oom_score_adj name
[dim. 19 avril 18:03:30 2026] [   1365]   990  1365     4420      213      128       85         0    77824       96          -900 systemd-oomd
[dim. 19 avril 18:03:30 2026] [  43056]  1000 43056  7511161  3403343  3388364    14371       608 32583680   518157           200 colmap
[dim. 19 avril 18:03:30 2026] oom-kill:constraint=CONSTRAINT_NONE,nodemask=(null),cpuset=/,mems_allowed=0,global_oom,task_memcg=/user.slice/user-1000.slice/user@1000.service/app.slice/app-org.gnome.Terminal.slice/vte-spawn-da5a3d92-b9fc-46a6-8658-708c1e2a92c2.scope,task=colmap,pid=43056,uid=1000
[dim. 19 avril 18:03:30 2026] Out of memory: Killed process 43056 (colmap) total-vm:30044644kB, anon-rss:13553456kB, file-rss:57484kB, shmem-rss:2432kB, UID:1000 pgtables:31820kB oom_score_adj:200
[dim. 19 avril 18:03:32 2026] oom_reaper: reaped process 43056 (colmap), now anon-rss:0kB, file-rss:192kB, shmem-rss:176kB
bertrand@bertrand-System-Product-Name:~/bgColmapUtils/pipe_linux$ 
```

## Plantage par manque d'espace disque
```bash
df -h /data
du -h --max-depth=2 /data | sort -h | tail -n 20
```

## Paramètres
- `--StereoFusion.max_image_size 1600` : 1600 = taille max de la plus grande longueur en pixel
- `1600` : 200 images ; 27 mn
-  `max_image_size 1600 4000` 3 paquets de 30 images +10 : 1h30


#### Terminal
tmux

## Conseils Copilot pour instabilité Nvidia

### Constat
- Les logs kernel montrent clairement un plantage du driver NVIDIA côté GSP (firmware “GPU System Processor”) suivi de blocages du modeset :
  - `rpcRmApiFree_GSP … status=0x0000000f`
  - assertions dans `vaspace_api.c`


#### 1) Version de driver
Après reboot (pour retrouver un état propre), donner :

```bash
nvidia-smi
modinfo nvidia | grep -E "version:|filename:"
```
filename:       /lib/modules/6.17.0-22-generic/kernel/nvidia-580-open/nvidia.ko
version:        580.126.09
srcversion:     43ECDFFFD2238CDC4017DFE

Le "GSP" (GPU system Processor) est un microcontroleur embarqué dans les GPU NVidia récents (surtout à partir des des générations RTX "moderne") Il execute un firmware NVIDDIA et prend en charge une partie des fonctions qui étaient historiquement géré par le driver coté CPU.

Le “GSP” est surtout présent/actif sur les drivers récents, et certains combos driver/kernel peuvent être instables selon GPU.

#### 2) Désactiver le firmware GSP (souvent efficace)
Sur beaucoup de configs, forcer `NVreg_EnableGpuFirmware=0` stabilise CUDA.

Créer un fichier :
```bash
sudo tee /etc/modprobe.d/nvidia-gsp.conf >/dev/null <<'EOF'
options nvidia NVreg_EnableGpuFirmware=0
EOF
```

Regénérer l’initramfs et reboot :
```bash
sudo update-initramfs -u
sudo reboot
```

Retester :
```bash
nvidia-smi
```

Pour verifier l'etat de EnableGpuFirmware:
```bash
cat /proc/driver/nvidia/params | grep -i EnableGpuFirmware
```

Commande :
```bash
sudo ubuntu-drivers devices
```

Puis par exemple :
```bash
sudo apt install nvidia-driver-XXX
sudo reboot
```

*(À préciser selon la sortie de `nvidia-smi` / version.)*

#### 4) Vérifs matériel (si ça persiste malgré driver)
Un hang GPU répété sous charge peut aussi être :
- surchauffe
- alim limite / câble PCIe
- undervolt/overclock

#### 5 Changement de driver
sudo apt update
sudo apt install nvidia-driver-580
sudo apt remove nvidia-driver-580-open
sudo reboot

nvidia-smi
dpkg -l | grep nvidia-driver
modinfo nvidia | head

Writing photometric output for DJI_20260207181546_0080_D.JPG
[0;31mE20260427 20:57:02.104234 22683 cudacc.cc:59] CUDA error at /home/bertrand/workspaceCpp/colmap/src/colmap/mvs/gpu_mat.h:24
89 occurence de  "Writing photometric output for"
## Prompt fusion 

  - 1-Je fais un sparse des 2000 images : feature_extractor, matches_importer, mapper
  - 2- Je fais image_undistorter les 2000 images
  - 3- J'extraie 20 fois 100 images de ./dense/images dans ./dense_i/images (i de 1 à 20)
  - 4-Je lance patch_match_stereo puis stereo_fusion sur dense_i
  - 5 - J'ai 20 fusion_i.PLY : 
  - Q Question Comment je les fusionne ?

  - Point crucial avant de fusionner
    - Tes fusion_i.ply seront dans le même repère uniquement si chaque dense_i utilise le même sparse/poses que le global (ou un sous-modèle cohérent extrait du global).
  - oui: fusion = concat + dédup.
  - R Fusion en ligne de commande (recommandé) : PDAL


## Indicateur qualité modelisation
colmap model_analyzer \
  --path /chemin/vers/sparse/0
  
## Prompt visualisation 

  - Visualiser fused.ply (COLMAP dense point cloud) sur un site web en 3D avec CesiumJS + 3D Tiles
  - J'ai un fichier fused.ply en sortie de colmap (un nuage de points dense)
  - Je veux le visualiser sur un site web en 3D
  - Q : Comment je fais
  - R1 Conversion fused.ply → fused.laz (recommandé): pdal translate fused.ply fused.laz
  - R2 Vérifier rapidement : pdal info fused.laz | head
  - R3 Convertir LAZ → 3D Tiles (tileset.json + .pnts)
    - avec 3d-tiles-tools (Node.js) 
    - installation 
     ```bash sudo apt-get install -y nodejs npm
		npm i -g 3d-tiles-tools```
	- execution
	 ```bash 3d-tiles-tools tiler --input fused.laz --output tiles_fused --format pnts ```
	- Servir les tuiles en HTTP (obligatoire)
	- Viewer web CesiumJS (minimal)
  
## Installer pipe pour convertir en tiles

```
sudo apt update
sudo apt install -y python3-venv python3-pip

python3 -m venv ~/venv/py3dtiles
source ~/venv/py3dtiles/bin/activate
pip install -U pip wheel
pip install py3dtiles
```

## Convertir en tiles :
```bash
~/workspaceCpp/PDAL/build/bin/pdal translate merged.ply fused.las
```
puis

```bash
/home/bertrand/venv/py3dtiles/bin/py3dtiles convert fused.las --out tiles_fused --overwrite --spec-version 1.0
```
## Visualiser les tiles avec Cesium
Serveur local:(Attention serveur pyton ne marche pas pour les gros nuage de point), utiliser npx
```bash
cd tiles_fused
python3 -m http.server 8000
```
Voir dans 
  - Cesium Sandcastle (rapide si tu as internet) Ouvre https://sandcastle.cesium.com/

## Visualisateur

  - py3dtiles

    - Gratuit, open-source
    - Techno : Python
    - Note (1-4) : 2/4
    - Viewer :
        - Aucun viewer officiel integre (outil de conversion CLI)
        - Usage typique : visualiser avec CesiumJS (3D Tiles) ou tout viewer compatible 3D Tiles
 
    - Commentaire : Conversion locale LAS/LAZ/PLY vers 3D Tiles. Peu de controle fin du pavage/LOD selon version.

- Cesium ion

    - Payant (quota gratuit limite), proprietaire (SaaS)
    - Techno : Cloud / API
    - Note (1-4) : 4/4
    - Viewer :
        - Visualisation via Cesium (web) et ecosysteme Cesium
        - On recupere un tileset 3D Tiles pret a etre consomme (CesiumJS, Cesium for Unreal, etc.)
    
    - Commentaire : Generation de 3D Tiles optimisee (LOD/streaming). Necessite upload et token, moins adapte au 100% offline.

- Potree

    - Gratuit, open-source
    - Techno : JavaScript (WebGL)
    - Note (1-4) : 4/4
    - Viewer :
       - Potree Viewer (web) : oui, c'est le produit principal
       - Fonctionne avec donnees converties au format Potree (octree)
    
    - Commentaire : Viewer web de nuages de points tres performant (octree). Pas du 3D Tiles mais excellent pour la visu.

  - PotreeConverter

    - Gratuit, open-source
    - Techno : C++ (CLI)
    - Note (1-4) : 4/4
    - Viewer :
        - Potree (Potree Viewer) pour visualiser le resultat

    - Commentaire : Conversion LAS/LAZ/PLY/XYZ vers format Potree (octree). Permet souvent un pavage fin (spacing/levels).

  - Entwine (EPT)

     - Gratuit, open-source
     - Techno : C++ (CLI)
     - Note (1-4) : 4/4
     - Viewer :
        - CesiumJS peut consommer certains flux/convertis, mais EPT n'est pas du 3D Tiles
        - Viewers/clients varies selon pipeline (EPT est surtout un format de diffusion/pipeline)
     
     - Commentaire : Generation EPT (octree) pour gros nuages, bon LOD/tiling. EPT != 3D Tiles.

  - COPC + PDAL

     - Gratuit, open-source
     - Techno : C++ (PDAL) + ecosysteme
     - Note (1-4) : 3/4
     - Viewer :
         - Pas de viewer "COPC officiel" unique
         - Options : viewers SIG/nuages de points selon outils, ou conversion vers Potree/3D Tiles pour la visu web
     - Commentaire : COPC est un LAZ hierarchique optimise (stockage/streaming). Pas du 3D Tiles directement, mais excellent format pivot.

  - deck.gl (PointCloudLayer)

     - Gratuit, open-source
     - Techno : JavaScript / TypeScript
     - Note (1-4) : 3/4
     - Commentaire : Brique de rendu web (WebGL) pour points. Pas un pipeline complet de conversion/3D Tiles.
     - Viewer :
        - deck.gl lui-meme sert de couche de rendu (integrable dans une appli web)
        - Pas un viewer "cles en main" complet (c'est une librairie)
  

- ArcGIS (Online/Enterprise)

     - Payant, proprietaire
     - Techno : Stack ESRI (web + serveur)
     - Note (1-4) : 3/4
     - Viewer :
        - ArcGIS Pro (desktop), Scene Viewer (web), etc.
        - Viewer integre a la plateforme ESRI
     
     - Commentaire : Publication SIG industrielle (scenes 3D/points). Integre mais couteux et moins flexible pour du 3D Tiles controle.

## Echec


## DONE
- batch de surveillance machine: ram + temperature
- réconciliation modèles 3D
- Convertis en 3D Tiles Cesium + viewer CesiumJS.
- Convertis en 3D Tiles PotreeConverter 

## TODO
- outil de visualisation 3D et de spécification de trajectoires
- test images satellites ou Google
- outil d'exploitation des logs COLMAP ( ajustage)
- Recuperer images depuis un drone


