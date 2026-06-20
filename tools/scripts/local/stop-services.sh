#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

ONLY=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --only)
      ONLY+=("$2")
      shift 2
      ;;
    -h|--help)
      echo "Uso: $0 [--only <service>]..."
      exit 0
      ;;
    *)
      ONLY+=("$1")
      shift
      ;;
  esac
done

if [[ "${#ONLY[@]}" -eq 0 ]]; then
  SELECTED_SERVICES=("${SERVICE_NAMES[@]}")
else
  SELECTED_SERVICES=("${ONLY[@]}")
fi

for requested_service in "${SELECTED_SERVICES[@]}"; do
  resolve_service "$requested_service"

  pid_path="$(pid_path_for_service "$SERVICE_NAME")"
  pid="$(service_pid "$SERVICE_NAME" || true)"

  if [[ -z "$pid" ]]; then
    echo "$SERVICE_NAME no tiene un proceso registrado."
    rm -f "$pid_path"
    continue
  fi

  echo "Deteniendo $SERVICE_NAME con PID $pid..."
  stop_process_tree "$pid"
  sleep 1

  if process_is_running "$pid"; then
    kill -9 "$pid" 2>/dev/null || true
  fi

  rm -f "$pid_path"
done

