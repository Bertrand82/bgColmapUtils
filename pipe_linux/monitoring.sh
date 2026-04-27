#!/usr/bin/env bash
set -euo pipefail

INTERVAL=10
OUT="/data/health_10s.log"

export LC_ALL=C

OUT_DIR="$(dirname "$OUT")"
mkdir -p "$OUT_DIR"

while true; do
  ts="$(date '+%Y-%m-%d %H:%M:%S')"

  # Mémoire / swap (en MiB)
  mem_total_mib="$(awk '/^MemTotal:/ {printf "%.0f", $2/1024}' /proc/meminfo)"
  mem_free_mib="$(awk '/^MemAvailable:/ {printf "%.0f", $2/1024}' /proc/meminfo)"
  swap_total_mib="$(awk '/^SwapTotal:/ {printf "%.0f", $2/1024}' /proc/meminfo)"
  swap_free_mib="$(awk '/^SwapFree:/  {printf "%.0f", $2/1024}' /proc/meminfo)"
  swap_used_mib="$((swap_total_mib - swap_free_mib))"

  # Temp CPU "officielle" via sensors: Package id 0 (recommandé)
  cpu_pkg_c="NA"
  if command -v sensors >/dev/null 2>&1; then
    cpu_pkg_c="$(sensors 2>/dev/null | awk '
      /^Package id 0:/ {
        for (i=1; i<=NF; i++) {
          if ($i ~ /°C/) {
            t=$i
            gsub(/[+°C]/,"",t)
            printf "%.1f", t+0
            exit
          }
        }
      }
      END { }
    ')"
    [[ -z "${cpu_pkg_c}" ]] && cpu_pkg_c="NA"
  fi

  # Temp CPU via sensors (max de toutes les sondes) - utile en debug
  cpu_max_c="NA"
  if command -v sensors >/dev/null 2>&1; then
    cpu_max_c="$(sensors 2>/dev/null | awk '
      match($0, /[+-]?[0-9]+(\.[0-9]+)?°C/) {
        t=substr($0, RSTART, RLENGTH)
        gsub(/[+°C]/,"",t)
        if ((t+0) > max) max=(t+0)
      }
      END {
        if (max > 0) printf "%.1f", max
        else printf "NA"
      }
    ')"
  fi

  # Temp via sysfs (thermal_zone0) - peut être autre chose que le CPU
  cpu_sysfs_c="NA"
  if [[ -r /sys/class/thermal/thermal_zone0/temp ]]; then
    cpu_sysfs_c="$(awk '{printf "%.1f", $1/1000}' /sys/class/thermal/thermal_zone0/temp)"
  fi

  # Infos "critiques"
  load1="$(awk '{print $1}' /proc/loadavg)"
  disk_root_used_pct="$(df -P / | awk 'NR==2 {gsub("%","",$5); print $5}')"

  # Compteur total d'événements OOM (depuis boot)
  oom_kills_total="NA"
  if command -v journalctl >/dev/null 2>&1; then
    oom_kills_total="$(journalctl -k --no-pager 2>/dev/null \
      | grep -Eci 'out of memory|oom-killer|killed process' || true)"
  fi

  echo "date=${ts} | mem_total=${mem_total_mib} | mem_free=${mem_free_mib} | swap_used=${swap_used_mib} | swap_total=${swap_total_mib} | cpu_pkg_c=${cpu_pkg_c} | cpu_max_c=${cpu_max_c} | cpu_sysfs_c=${cpu_sysfs_c} | load1=${load1} | disk_root_used_pct=${disk_root_used_pct} | oom_kills_total=${oom_kills_total}" >> "$OUT"

  sleep "$INTERVAL"
done
