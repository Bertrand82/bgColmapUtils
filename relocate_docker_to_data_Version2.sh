#!/usr/bin/env bash
set -euo pipefail

DATA_ROOT="/data/docker"
DAEMON_JSON="/etc/docker/daemon.json"

echo "==> Checking that /data exists..."
if [[ ! -d /data ]]; then
  echo "ERROR: /data does not exist. Mount your EBS volume on /data first."
  exit 1
fi

echo "==> Stopping Docker (if running)..."
# Stop docker if present
if systemctl list-unit-files | grep -q '^docker\.service'; then
  sudo systemctl stop docker || true
fi

echo "==> Creating Docker data-root on EBS: ${DATA_ROOT}"
sudo mkdir -p "${DATA_ROOT}"
sudo chown root:root "${DATA_ROOT}"
sudo chmod 711 "${DATA_ROOT}"

echo "==> Writing Docker daemon config: ${DAEMON_JSON}"
sudo mkdir -p "$(dirname "${DAEMON_JSON}")"
cat <<EOF | sudo tee "${DAEMON_JSON}" >/dev/null
{
  "data-root": "${DATA_ROOT}"
}
EOF

echo "==> Removing old Docker data from root disk..."
# WARNING: this deletes all images/containers/volumes/caches previously stored on root disk
sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd

echo "==> Starting Docker..."
sudo systemctl start docker
sudo systemctl enable docker >/dev/null 2>&1 || true

echo "==> Docker status:"
sudo systemctl --no-pager --full status docker || true

echo "==> Docker Root Dir (should be ${DATA_ROOT}):"
docker info 2>/dev/null | grep -i "Docker Root Dir" || true

echo "Done."