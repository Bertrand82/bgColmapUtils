

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
 
 Avec la nouvelle version 12.8 de Cuda, ca ne marche plus du tout.
 
 Je met a jour colmap depuis git
 J'ai recuperé la version courante de colmap (branche main) . Ca marche beaucoup mieux (la fusion trouve des images, c'est un bug referencé par colmap mais copilot m'a laissé pataugé toute une journée)
  - Erreur /plantage dans patchMatch : patch_match_stereo
  - Copilot conseille de prendre une release:4.04
git fetch --tags
git checkout 4.0.4
  - Meme plantage pendant patch_match_stereo (Nb Thread : 2, taille paquet:
  - diminution size max de 4000 à 2000: Ca plante
  - diminution  size max  de 2000 à 1500 : Ca plante : E20260523 14:55:52.121922  4692 cudacc.cc:59] CUDA error at /home/bertrand/workspaceCpp/colmap/src/colmap/mvs/gpu_mat.h:188 - unspecified launch failure
  - diminution de  size max  1500 à 1000 : Ca plaante  cudacc.cc:51]  Sweep 3: 0.2769s, 
  - diminution de nombre de thread de 2 à 1 --> ca plante:  cudacc.cc:51 Mais ca dure un peu plus longtemps
  - Diminution de la taille des paquets d'images : de 30 à 20 : Ca plante . E20260523 17:02:37.369056 55997 cudacc.cc:78] CUDA error at /home/bertrand/workspaceCpp/colmap/src/colmap/mvs/patch_match_cuda.cu:1491 - unspecified launch failure
  
  Re-installation de nvidia-driver-580-open
  Installation cuda 13.2
  Recompilation colmap
  A l'execution: E20260524 12:14:50.534783  3679 cudacc.cc:59] CUDA error at /home/bertrand/workspaceCpp/colmap/src/colmap/mvs/patch_match_cuda.cu:1682 - the provided PTX was compiled with an unsupported toolchain.terminate called after throwing an instance of 'colmap::AggregateException'
  Le PTX est un langage intermediaire pour CUDA (ecrit par nvcc)
  Re-installation de CUDA 13.0 et installation de nvcc 13.0 ((Cuda Compiler driver))
  Ca plante au 5eme Paquets de 30 images
   Je re-installe nvidia-driver-595-open
	sudo apt install -y nvidia-driver-595-open
 	sudo apt install -y cuda-toolkit-13-0
 	
 	Ca plante encore
 	Je plonge dans le bios:
 	Je remet les valeurs usine. Je galère pour enlever SecureBoot
 	Ca marche! Plus de plantage. Retour à size_max=4000; Taille paquet: 30
 
 
## Question copilot:
 Est ce qu'il y a des issue colmap 4.04 sur des crashes inexpliqués pendant dans des patch_match_stereo avec CUDA 12.8
 avec E20260523 14:55:52.121922  4692 cudacc.cc:59] CUDA error at /home/bertrand/workspaceCpp/colmap/src/colmap/mvs/gpu_mat.h:188 - unspecified launch failure
 
### Demarche d'investigation en cas de probemes :
  - Ce sont les datas (images ici) ou les outils (colmap, cuda, driver,carte) ou les requete sur les outils (PatchMatchStereo.max_image_size,PatchMatchStereo.cache_size,PatchMatchStereo.num_threads,PatchMatchStereo.num_iterations,PatchMatchStereo.geom_consistency ...)? Remarque : copilot suspecte d'abord les images (les data) puis les requetes sur les outils, et n'interroge pas trop les versions utilisés. Colmap est manifestement pas très stable.
   - Les outils : Recenser tous les logiciels et leurs versions (ici colmap, cuda, driver nvidia)
   
   
## SecureBoot enabled
https://www.asus.com/fr/support/faq/1049829/
Je ne saurai pas le refaire. J'ai pataugé 1 heure
voir photo dans telephone
BIOS>UEFI BIOS Utility -Advanced Mode
Boot>Secure Boot Menu
Attention, si l'on met les param par defaut, on enable secureBoot, l'on ne peut plus installer ce qui vient de nvidia (driver)

