### Pour demarrer serveur http :

Attention : python3 ne marche pas pour les gros nuages de points. Utiliser npx


```bash
npx http-server -p 8000 --cors
```

### Pour voir la place restant sur le disque

```bash
df -h /data
```

### Pour passer en mode terminal

Arrêter l’interface graphique pour cette session.
Ça coupe l’interface graphique et laisse le système en mode texte.
Depuis un terminal, lance :

```bash
sudo systemctl stop gdm3
```


Pour relancer l’interface graphique :

```bash
sudo systemctl start gdm3
```

## Pour avoir un terminal ssh qui n'arrete pas le process si la liaison est coupé

```bash
tmux
tmux new -s bg
tmux ls
tmux attach -t bg
tmux a -t bg
```
### Param nvidia
```bash
nvidia-smi
```
