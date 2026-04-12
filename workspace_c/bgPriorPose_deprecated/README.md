Cet outil met à jour la table "pose_priors" de colmap. Cette table prerempli les positions gps (grossiere donc) et les utilise dans la construction 3D

Cet outil doit être appelé apres "match" et avant "map"


## Compilation 

cmake -S . -B build
cmake --build build