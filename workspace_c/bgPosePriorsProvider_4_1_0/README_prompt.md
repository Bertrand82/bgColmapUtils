Tu es Copilot dans VS Code. On va procéder en MULTI-ÉTAPES. À chaque étape: 
1) propose exactement ce que tu vas faire, 
2) génère les fichiers/modifs correspondants, 
3) termine par une checklist “OK pour passer à l’étape suivante ?”.

Contexte (source de vérité)
- Lis et respecte `README.md` de ce repo (format CSV, usage, objectifs).
- Exemple de base SQLite dans `./data` (à utiliser pour valider le schéma réel).
- Version COLMAP cible: COLMAP 4.1.0.dev0 commit 3587d22d (2026-04-02) disponible dans `~/workspaceCpp/colmap`.
- But: application C++ `bgPosePriorsProvider_4_1_0` qui lit `metadataCSV.txt` et écrit dans `database.db` table `pose_priors`.
- Layout: headers dans `include/`, sources dans `src/`.
- C++17, dépendances minimales: sqlite3 + Eigen.

Important (compatibilité BLOB / COLMAP)
- Pour ce commit, les champs BLOB sont lus côté COLMAP avec:
  - position = Eigen::Vector3d (3 doubles)
  - position_covariance = Eigen::Matrix3d (9 doubles)
  - gravity = Eigen::Vector3d (3 doubles)
- Donc les BLOB à écrire doivent être des tableaux binaires de `double` (float64) en mémoire, ordre: 
  - Vector3d: x,y,z 
  - Matrix3d: 3x3 (documente l’ordre choisi; par défaut Eigen stocke column-major mais ici on écrit une séquence de doubles: explique et rends-le configurable si nécessaire).

Règles de conversation
- Ne code pas tout d’un coup.
- Si une information manque (colonnes exactes de `pose_priors`, contraintes, FK), fais d’abord une étape d’inspection et prépare le code pour s’adapter.
- Pas de gros copier/coller depuis COLMAP; réimplémentation minimale.

ÉTAPE 1 — “Inspection & design”
Objectif: sécuriser le schéma et le contrat d’écriture avant d’écrire la logique.
Actions à faire:
- Ajouter un petit utilitaire C++ (ou un mode interne) qui:
  - ouvre `database.db`
  - affiche `sqlite_master` pour vérifier l’existence de `images` et `pose_priors`
- Proposer ensuite (texte) un mapping clair: quelles colonnes on va renseigner et comment (valeurs par défaut).
Livrables:
- `CMakeLists.txt` minimal (build d’un exécutable)
- `src/main.cpp` provisoire avec un mode `--inspect <dbPath>`
- Une mini couche RAII SQLite dans `include/...` + `src/...` (Database, Statement).
Fin de l’étape: afficher dans la console un exemple de sortie attendue.

ÉTAPE 2 — “Parser CSV & résolution image_id”
Objectif: lire `metadataCSV.txt` et résoudre `image_id`.
Actions:
- Implémenter un parser robuste:
  - ignore lignes vides/commentaires
  - split par virgule en 7 champs
  - parse double avec gestion de `NaN`
- Implémenter `GetImageIdByName(name)` via requête préparée.
- Ajouter un mode `--dry-run`:
  - lit tout, résout les ids, mais n’écrit rien.
  - sort un résumé: nb lignes, nb images trouvées, nb non trouvées.
Livrables:
- `include/.../CsvParser.h` + `src/CsvParser.cpp`
- `include/.../PoseRow.h` (struct: imageName, xx, yy, zz, yaw, pitch, roll)
- intégration dans `main.cpp` (options CLI simples).

ÉTAPE 3 — “Écriture pose_priors (INSERT/UPSERT)”
Objectif: écrire réellement dans `pose_priors` de manière compatible schéma.
Actions:
- Après inspection (étape 1), générer la requête d’insertion adaptée au schéma r��el:
  - gérer PK (AUTOINCREMENT) si nécessaire
  - gérer colonnes NOT NULL
  - gérer contraintes d’unicité si présentes (UPSERT ou DELETE+INSERT).
- Construire les BLOB:
  - `position` depuis (xx,yy,zz)
  - `position_covariance` par défaut (diag(sigma^2)), sigma configurable par CLI `--sigma 5.0`
  - `gravity` par défaut configurable (ex: `--gravity 0,0,-1`).
- Définir et documenter:
  - `corr_data_id`, `corr_sensor_id`, `corr_sensor_type`, `coordinate_system`
  - si ces champs doivent référencer d’autres tables, soit:
    - créer/assurer l’existence des lignes référencées, soit
    - échouer avec message clair si impossible.
- Transactions: entourer tout le batch dans une transaction (BEGIN/COMMIT) + rollback en cas d’erreur.
Livrables:
- `include/.../PosePriorsWriter.h` + `src/PosePriorsWriter.cpp`
- Mise à jour `main.cpp` pour le flux complet.

ÉTAPE 4 — “Validation & documentation”
Objectif: rendre l’outil utilisable et vérifiable.
Actions:
- Ajouter `--check`:
  - compte les lignes insérées pour les images traitées (SELECT COUNT(*))
- Messages d’erreur clairs, codes de retour.
- Mettre à jour `README.md`:
  - build (cmake)
  - run
  - exemples (dry-run, inspect, write)
  - hypothèses (covariance/gravity/enums)
Livrables:
- README mis à jour
- Exemples de commandes

Démarre maintenant par l’ÉTAPE 1 uniquement. 
Avant d’écrire le moindre code d’écriture dans `pose_priors`, assure-toi d’avoir le schéma exact via l’inspection.