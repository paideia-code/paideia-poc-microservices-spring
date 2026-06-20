#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

TIMEOUT_SECONDS=1

while [[ $# -gt 0 ]]; do
  case "$1" in
    --timeout)
      TIMEOUT_SECONDS="$2"
      shift 2
      ;;
    -h|--help)
      echo "Uso: $0 [--timeout <seconds>]"
      exit 0
      ;;
    *)
      echo "Argumento no reconocido: $1" >&2
      exit 1
      ;;
  esac
done

printf '%-22s %-42s %s\n' "Service" "Url" "Status"
printf '%-22s %-42s %s\n' "-------" "---" "------"

for service in "${SERVICE_NAMES[@]}"; do
  resolve_service "$service"
  url="$(health_url_for_current_service)"

  if response="$(curl -fsS --max-time "$TIMEOUT_SECONDS" "$url" 2>/dev/null)"; then
    if [[ "$response" == *'"status":"UP"'* ]]; then
      status="UP"
    else
      status="UNKNOWN"
    fi
  else
    status="UNAVAILABLE"
  fi

  printf '%-22s %-42s %s\n' "$SERVICE_NAME" "$url" "$status"
done

