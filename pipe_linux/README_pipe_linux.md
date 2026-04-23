### Pipe_linux

#### 1000 images
- Ça plante de manière inexpliquée.
- Peut-être changer pour un bash/terminal plus robuste : **tmux** (terminal qui détache le job de la session).

#### 200 images
- En cours
```bash
~/workspaceCpp/colmap/build/src/colmap/exe/colmap patch_match_stereo --help
~/workspaceCpp/colmap/build/src/colmap/exe/colmap stereo_fusion --help
```
#### Plantage par manque de mémoire

Commande :

```bash
sudo dmesg -T | tail -n 200 | egrep -i 'out of memory|oom|killed process|colmap'
```

Sortie :

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


#### Plantage par manque d'espace disque
```bash
df -h /data
du -h --max-depth=2 /data | sort -h | tail -n 20
```

#### Parametres 
--StereoFusion.max_image_size 1600 : 1600 taille max de la plus grande longueur en pixel
1600 : 200 images ; 27 mn

#### Console
tmux

#### todo
  - outil de spécification de trajectoires
  - test images satellites ou google 
  - reconcilliation modeles 3D 
  - outil d'exploitation des logs colmap
