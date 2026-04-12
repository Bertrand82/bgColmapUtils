# bgPosePriorsProvider_4_1_0

Outil C++17 pour lire un CSV metadataCSV.txt et alimenter la table pose_priors d'une base COLMAP SQLite.

Version cible COLMAP: 4.1.0.dev0 (commit 3587d22d, 2026-04-02).

## Entrées / sortie

- Entrées:
  - base SQLite COLMAP (database.db)
  - CSV metadataCSV.txt avec 7 colonnes: imageName, xx, yy, zz, yaw, pitch, roll
- Sortie:
  - lignes écrites dans la table pose_priors

## Build

Prerequis:
- CMake >= 3.16
- Compilateur C++17
- SQLite3 (dev)
- Eigen3

Commandes:

```bash
cmake -S . -B build
cmake --build build
```

Executable genere:

```bash
./build/bgPosePriorsProvider_4_1_0
```

## Commandes disponibles

### 1) Inspection schema

```bash
./build/bgPosePriorsProvider_4_1_0 --inspect data/database.db
```

Affiche sqlite_master et PRAGMA table_info pour images et pose_priors.

### 2) Dry-run (aucune ecriture)

```bash
./build/bgPosePriorsProvider_4_1_0 --dry-run data/database.db data/metadataCSV.txt
```

Lit le CSV, resout image_id via requete preparee, puis affiche:
- rows read
- images found
- images not found

### 3) Ecriture dans pose_priors

```bash
./build/bgPosePriorsProvider_4_1_0 \
  --write data/database.db data/metadataCSV.txt \
  --sigma 5.0 \
  --gravity 0,0,-1 \
  --corr-sensor-id 1 \
  --corr-sensor-type 0 \
  --coordinate-system 0 \
  --cov-order row
```

Comportement:
- transaction globale BEGIN/COMMIT
- rollback automatique si erreur
- strategie DELETE+INSERT par corr_data_id (image_id)
- resume imprime: rows read/inserted/replaced/missing image

### 4) Verification post-ecriture

```bash
./build/bgPosePriorsProvider_4_1_0 --check data/database.db data/metadataCSV.txt
```

Calcule via SQL le nombre de lignes pose_priors pour les images traitees:
- resolution image_id depuis images
- SELECT COUNT(*) dans pose_priors pour chaque image trouvee
- affiche les images sans prior et un resume global

Code retour:
- 0: check valide (toutes les images traitees ont >= 1 pose_prior)
- 3: check invalide (au moins une image traitee sans pose_prior)

## Format CSV attendu

Une ligne par image, 7 champs separes par virgule:

```text
imageName,xx,yy,zz,yaw,pitch,roll
```

Parser robuste:
- ignore lignes vides
- ignore lignes commentaires (# ou //)
- exige exactement 7 champs
- parse double et accepte NaN

## Mapping ecriture pose_priors

Colonnes renseignees:
- corr_data_id = image_id (resolu par nom image)
- corr_sensor_id = option CLI --corr-sensor-id (defaut 1)
- corr_sensor_type = option CLI --corr-sensor-type (defaut 0)
- position = BLOB float64 [xx, yy, zz]
- position_covariance = BLOB float64 3x3 diag(sigma^2)
- gravity = BLOB float64 [gx, gy, gz]
- coordinate_system = option CLI --coordinate-system (defaut 0)

Colonnes non renseignees explicitement:
- pose_prior_id (PRIMARY KEY): gere par SQLite.

## Hypotheses et compatibilite BLOB

Pour COLMAP 3587d22d, lecture cote COLMAP:
- position: Eigen::Vector3d (3 doubles, 24 octets)
- position_covariance: Eigen::Matrix3d (9 doubles, 72 octets)
- gravity: Eigen::Vector3d (3 doubles, 24 octets)

Ordre des coefficients covariance dans le BLOB:
- configurable avec --cov-order
- row = row-major
- col = column-major

## Contraintes FK

Si des FK existent sur corr_sensor_id, corr_sensor_type, coordinate_system dans pose_priors, l'outil echoue avec message explicite (pas d'auto-creation des lignes referencees dans cette version).

## Codes de retour

- 0: succes
- 1: usage invalide
- 2: erreur d'execution
- 3: echec du check
