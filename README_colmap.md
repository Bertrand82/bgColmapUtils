# Notes AWS / EC2 / ECS / COLMAP

## Liens utiles (AWS Console)

- **Page d’accueil AWS**  
  https://eu-north-1.console.aws.amazon.com/console/home?region=eu-north-1

- **CloudWatch — Tableaux de bord**  
  https://eu-north-1.console.aws.amazon.com/cloudwatch/home?region=eu-north-1#dashboards/

- **IAM (Identity and Access Management)**  
  https://us-east-1.console.aws.amazon.com/iam/home?region=eu-north-1#/home

- **EC2 — Instances**  
  https://eu-north-1.console.aws.amazon.com/ec2/home?region=eu-north-1#Instances

- **ECS — Prise en main**  
  https://eu-north-1.console.aws.amazon.com/ecs/v2/getStarted?region=eu-north-1

---

## Compte AWS (informations générales)

- **Région par défaut** : `eu-north-1`
- **ID de compte** : `2485-0811-9320`
- **Nom du compte** : `montpezat82`
- **Couleur du compte** : non définie

Sections courantes :
- Compte
- Organisation
- Service Quotas
- Gestion de la facturation et des coûts
- Informations d’identification de sécurité

> Pour modifier les droits, il faut se connecter en **root** (avec l’adresse e-mail associée au compte).

---

## Accès CLI : méthode recommandée (IAM / Identity Center)

Pour un compte personnel, le plus simple est de :
- **créer un utilisateur IAM** (ou activer **IAM Identity Center**),
- puis configurer **AWS CLI** avec des **Access Keys**.

Tu ne peux pas te connecter à AWS CLI uniquement avec l’e-mail / mot de passe de la console.

### 1) Créer des identifiants pour la CLI (dans la console AWS)

1. Aller sur https://console.aws.amazon.com/
2. Rechercher **IAM**
3. Ouvrir **IAM (Identity and Access Management)**
4. Créer un utilisateur (ex. `bertrand-cli`)
5. Cocher **Provide user access to the AWS Management Console** seulement si tu veux aussi un accès console (optionnel pour la CLI)
6. Donner des permissions (ex. `AdministratorAccess` temporairement, puis restreindre ensuite)
7. Dans l’utilisateur IAM : **Security credentials → Create access key**
8. Choisir le cas d’usage **Command Line Interface (CLI)**
9. Copier `Access key ID` et `Secret access key` (le secret ne sera plus affiché après)

### 2) Configurer AWS CLI

```bat
aws configure
```

Renseigner :
- AWS Access Key ID = `AKIA...`
- AWS Secret Access Key = `...`
- Default region name = `eu-north-1`
- Default output format = `json`

### 3) Tester

```bat
aws sts get-caller-identity
aws iam get-user
aws configure list
aws ec2 describe-regions --output table
```

Changer / lire la région :

```bat
aws configure set region eu-north-1
aws configure get region
```

Désactiver une access key IAM :

```bat
aws iam update-access-key --user-name Bertrand --access-key-id AKIA... --status Inactive
```

---

## Connexion SSH à une instance EC2

Syntaxe générale :

```bash
ssh -i "C:\chemin\vers\ta-cle.pem" ubuntu@IP_PUBLIQUE
```

Exemple (instance) :
- **Instance** : `i-0555abd2ed4a1c790`
- **IP publique** : `51.20.69.230`
- Lien console :  
  https://eu-north-1.console.aws.amazon.com/ec2/home?region=eu-north-1#ConnectToInstance:instanceId=i-0555abd2ed4a1c790

Selon l’AMI :

- AMI Ubuntu :
```bash
ssh -i C:\Users\bertr\bg_amazon\bg_00_test_amazon.pem ubuntu@51.20.69.230
```

- AMI Amazon Linux :
```bash
ssh -i C:\Users\bertr\bg_amazon\bg_00_test_amazon.pem ec2-user@51.20.69.230
```

- AMI Debian :
```bash
ssh -i C:\Users\bertr\.ssh\aws_shubaka.pem admin@51.20.69.230
```

Vérifier la version de Linux :

```bash
cat /etc/os-release
```

---

## SCP : copier un dossier complet

```bash
scp -i C:\Users\bertr\bg_amazon\bg_00_test_amazon.pem -r C:\Users\bertr\Pictures\bgColmapExemple\exemple_NO_NVIDIA ec2-user@51.20.69.230:~/bg
```

---

## Docker (sur la machine)

Vérifier / activer Docker :

```bash
sudo systemctl status docker --no-pager
sudo systemctl enable --now docker
```

---

## EC2 vs ECS

### EC2 (Elastic Compute Cloud)
- Tu loues une machine virtuelle (instance) : CPU / RAM / disque / réseau.
- Tu gères l’OS (Ubuntu, Amazon Linux…), les installations (Docker, drivers NVIDIA, etc.).
- Idéal si tu veux une machine pour tester, faire du SSH, installer COLMAP, lancer des scripts, déboguer.

### ECS (Elastic Container Service)
- Orchestrateur de conteneurs (similaire à Kubernetes, mais AWS).
- Tu décris une **task** (image Docker, CPU/RAM, variables, volumes) et ECS gère l’exécution, le redémarrage et la mise à l’échelle.
- Tu ne gères pas forcément les serveurs directement.

Exemple de type d’instance EC2 : `g4dn.xlarge`.

---

## EBS (Amazon Elastic Block Store)

- **Rôle** : stockage en mode “bloc” (comme un disque/SSD) attachable à une instance EC2 sous forme de volume (ex. `/dev/xvda`, `/dev/nvme...`).
- **Utilité** : stocker le système (volume root) et/ou des données (volumes supplémentaires) de façon persistante.
- **Caractéristique clé** : les données persistent généralement même si l’instance est arrêtée (et parfois même supprimée selon `DeleteOnTermination`).
- **Fonctions courantes** : choix du type (`gp3`, `io2`, `st1`, `sc1`…), performances (IOPS/débit), snapshots, chiffrement.

---

## Autre instance / connexions (regroupées)

- **IP** : `13.49.228.58`
- **DNS** : `ec2-13-49-228-58.eu-north-1.compute.amazonaws.com`

Connexions SSH (doublons supprimés) :

```bash
ssh -i C:\Users\bertr\bg_amazon\bg_test_image_2.pem ubuntu@13.49.228.58
ssh -i C:\Users\bertr\.ssh\id_ed25519 bertrand@13.49.228.58
```

Copie vers `/data` :

```bash
scp -i C:\Users\bertr\bg_amazon\bg_test_image_2.pem -r C:\Users\bertr\Pictures\images_test ubuntu@13.49.228.58:/data
```

Installation paquets :

```bash
sudo apt-get install -y docker.io
sudo usermod -aG docker ubuntu
sudo apt-get install -y git
sudo apt-get install -y sqlite3
```

Groupes / permissions :

```bash
sudo groupadd shared 2>/dev/null || true
sudo usermod -aG shared ubuntu
sudo usermod -aG shared alice
```

Git :

```bash
git remote set-url origin git@github.com:Bertrand82/bgColmapUtils
git reset --hard origin/main
```

---

## EBS / instance : éléments notés

- **Volume** : `vol-05c3eed33ac008d93`
- **Type d’instance** : `c7i-flex.large`
- **Nom interne** : `ip-172-31-42-244.eu-north-1.compute.internal`

Problème noté : impossibilité d’attacher `vol-05c3eed33ac008d93` à l’instance `c7i-flex.large`.

---

## COLMAP : matchers / mappers

### Mappers (logique “pipeline” / stratégie)
- `colmap sequential_matcher` : pour une vidéo / prise de vue dans l’ordre (images consécutives)
- `colmap spatial_matcher` : si tu as des positions GPS (ou des informations spatiales) pour ne matcher que des images proches
- `colmap vocab_tree_matcher` : matching basé sur un vocabulaire visuel (scalable sur gros jeux de données)
- `colmap transitive_matcher` : complète des matches par transitivité (utile en complément)
- `colmap image_retrieval` + matching : retrieval puis matching (selon versions)

### Matchers (commandes)
- `colmap exhaustive_matcher` : toutes les paires d’images (coûteux en N²)
- `colmap sequential_matcher` : images voisines (séquence)
- `colmap spatial_matcher` : images proches spatialement (GPS/priors)
- `colmap vocab_tree_matcher` : vocabulaire visuel (retrieval puis matching)
- `colmap transitive_matcher` : matches par transitivité (complément)

Récapitulatif :
- `exhaustive_matcher`
- `sequential_matcher`
- `spatial_matcher`
- `vocab_tree_matcher`
- `transitive_matcher`

Exemple de timings (30 images) :
- `feature_extractor` : 5.412 minutes
- `mapper` : 2.091 minutes

---

## Commandes diverses (fichiers / droits)

Afficher un fichier :

```bash
cat nom_du_fichier.txt
```

Passer en utilisateur `bertrand` :

```bash
sudo -iu bertrand
```

Appliquer des droits (dossier partagé) :

```bash
sudo chgrp -R shared /chemin/du/dossier
sudo find /chemin/du/dossier -type d -exec chmod 750 {} \;
sudo find /chemin/du/dossier -type f -exec chmod 640 {} \;
sudo find /chemin/du/dossier -type f -name "*.sh" -exec chmod 750 {} \;
```

---

## SQLite / pose_priors (GPS)

`pose_priors` : données GPS.

Exemples :

```bash
sqlite3 "$DATABASE_PATH" "SELECT * FROM pose_priors LIMIT 5;"
sqlite3 database.db "SELECT * FROM pose_priors LIMIT 5;"

sqlite3 "$DATABASE_PATH" "SELECT name FROM images ORDER BY image_id;"
sqlite3 database.db "SELECT name FROM images ORDER BY image_id;"

sqlite3 database.db ".tables"
sqlite3 database.db "SELECT COUNT(*) FROM pose_priors;"
sqlite3 database.db "PRAGMA table_info(pose_priors);"
```

---

## Note clavier (Windows)

- `AltGr` + `2`, puis `Espace` → `~`