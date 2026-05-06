Pipeline for generating a 3D model from a list of images

See pipe_aws


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
