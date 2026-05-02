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
  - 5 - J'ai 20 fusion_i.PLY : Comment je les fusionne ?

  - Point crucial avant de fusionner
Tes fusion_i.ply seront dans le même repère uniquement si chaque dense_i utilise le même sparse/poses que le global (ou un sous-modèle cohérent extrait du global).
Si dans chaque dense_i tu as bien un sparse/ (copié ou subset) correspondant aux images du batch, alors oui: fusion = concat + dédup.
  - Fusion en ligne de commande (recommandé) : PDAL

```bash 
  colmap model_subset \
  --input_path "$BG_WORK/sparse/0" \
  --output_path "$BG_WORK/sparse/subset" \
  --image_list_path "$BG_WORK/image_list.txt"
```
## Indicateur qualité modelisation
colmap model_analyzer \
  --path /chemin/vers/sparse/0

## DONE
- batch de surveillance machine: ram + temperature
- réconciliation modèles 3D

## TODO
- outil de spécification de trajectoires
- test images satellites ou Google
- outil d'exploitation des logs COLMAP
- Recuperer images depuis un drone

