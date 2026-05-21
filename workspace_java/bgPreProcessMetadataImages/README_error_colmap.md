

# patch_match_stereo

le calcul PatchMatch a réussi pour une vue, puis ça a cassé pendant “Writing geometric output”, et toutes les vues suivantes héritent d’un contexte CUDA déjà corrompu.

Le plantage n’a pas lieu pendant l’optimisation PatchMatch elle-même, mais pendant la phase de sortie géométrique / transfert / texture / buffers GPU associés à l’écriture.

Les erreurs sur :
  - gpu_mat.h
  - cuda_texture.h
  
Solution proposée: --PatchMatchStereo.geom_consistency 0


#essai avec --PatchMatchStereo.geom_consistency 0
Ca plante pareil

Ce que je pense maintenant
Ton intuition “ce n’est pas un problème de charge” devient de plus en plus crédible.

Parce que :

le calcul tient,
la carte tient plusieurs vues,
le crash est récurrent dans une phase précise,
il survient encore avec geom_consistency=0.
Donc je penche de plus en plus pour :

bug logiciel / driver / compatibilité CUDA-COLMAP plutôt que simple surcharge.

sudo dmesg | grep -i -E "NVRM|Xid|nvidia" | tail -100

Les lignes NVRM montrent que le driver NVIDIA perd le contrôle normal du GPU puis échoue pendant le teardown/libération d’objets CUDA :

rpcSendMessage failed with status 0x0000000f
GspRmFree failed
nvAssertFailedNoLog
Disable of Cuda limit activation failed
Ça veut dire, en clair :

le calcul COLMAP déclenche une situation où
le pilote NVIDIA / GSP-RM part en erreur
puis le nettoyage CUDA échoue en cascade.
Donc ton intuition est bonne : ce n’est pas simplement “trop de charge”.

Ce que j’en déduis
Le problème est très probablement dans l’un de ces niveaux :

driver NVIDIA 580.142
compatibilité driver ↔ RTX 5060 Ti ↔ CUDA runtime ↔ COLMAP
bug spécifique GSP firmware / GSP-RM
éventuellement matériel, mais les logs pointent d’abord le driver
Le détail important, c’est la présence de messages GSP :

rpcRmApiFree_GSP
rpcSendMessage failed
Ça oriente clairement vers la pile récente du driver NVIDIA, pas vers une simple erreur utilisateur.

Recommandation prioritaire
1. Tester un autre driver NVIDIA
C’est maintenant la piste numéro 1.

Le but :

soit une version plus stable / plus mature
soit éviter un bug spécifique à 580.142


J'ai nvidia-driver-580-open

ubuntu-drivers devices

vendor   : NVIDIA Corporation                                                                                                                             driver   : nvidia-driver-595-server - distro non-free                                                                                                     driver   : nvidia-driver-580-open - distro non-free                                                                                                       driver   : nvidia-driver-580 - distro non-free                                                                                                            driver   : nvidia-driver-580-server-open - distro non-free                                                                                                driver   : nvidia-driver-595-open - distro non-free recommended                                                                                           driver   : nvidia-driver-580-server - distro non-free                                                                                                     driver   : nvidia-driver-595-server-open - distro non-free                                                                                                driver   : nvidia-driver-595 - distro non-free                                                                                                            driver   : xserver-xorg-video-nouveau - distro free builtin 


 sudo apt install nvidia-driver-595
 
 ## installation de nvidia-driver-595 done
 Ne marche pas , il faut open
 ## installation nvidia-driver-595-open 
 Il faut imperativement que --PatchMatchStereo.geom_consistency 1 , sinon ca plante
