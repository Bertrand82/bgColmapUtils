## BgColmapUtil


  - Pipeline for generating a 3D model from a list of images on aws .
  - Pipeline pour générer un modele 3D sur une machine ubuntu ayant une carte graphique nvidia.

Ce repository rassemble les utilitaires necessaires à la mise en oeuvre de colmap , et aux traitements de lots d'images.

Il comprend : 
  - bgPreProcessMetadataImages: Utilitaires java qui permet de créer des lots d'images , avec les scripts sh permettant de les traiter. Les scripts sont le projet java. 
  - bgPosePriorsProvider: Utilitaire en C permettant de traiter les pose (les positions de la camera) et de les injecter dans le db colmap
  - Des scripts sh utilisables sur les machines aws. Je n'utilise plus de machines aws (pas de budget) mais le les garde au cas ou ca redeviendrai utile

## Install docker (ubuntu, debian)

```bash
sudo apt-get install -y docker.io
sudo usermod -aG docker ubuntu
sudo apt-get install -y git
sudo apt-get install -y sqlite3
```

### Create  "shared" group
```bash
sudo groupadd -f shared
sudo usermod -aG shared ubuntu
sudo usermod -aG shared bertrand
```
### Clone git

#### Generate keys
```bash
sudo -iu bertrand
sudo adduser bertrand

mkdir -p ~/.ssh
chmod 700 ~/.ssh
ssh-keygen -t ed25519 -C "bertrand@github" -f ~/.ssh/id_ed25519
```
clé privée : ~/.ssh/id_ed25519
clé publique : ~/.ssh/id_ed25519.pub

Tester la connection
```bash
ssh -T git@github.com
```

```bash
git clone git@github.com:Bertrand82/bgColmapUtils.git
```

### right to run docker

```bash
sudo usermod -aG docker ubuntu
sudo usermod -aG docker bertrand
```


### 
