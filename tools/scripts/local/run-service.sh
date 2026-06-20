#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

SERVICE=""
PORT=""
SKIP_INFRA=0
DRY_RUN=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    -s|--service)
      SERVICE="$2"
      shift 2
      ;;
    -p|--port)
      PORT="$2"
      shift 2
      ;;
    --skip-infra)
      SKIP_INFRA=1
      shift
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    -h|--help)
      echo "Uso: $0 --service <service> [--port <port>] [--skip-infra] [--dry-run]"
      exit 0
      ;;
    *)
      if [[ -z "$SERVICE" ]]; then
        SERVICE="$1"
        shift
      else
        echo "Argumento no reconocido: $1" >&2
        exit 1
      fi
      ;;
  esac
done

if [[ -z "$SERVICE" ]]; then
  echo "Debes indicar un servicio." >&2
  valid_service_names >&2
  exit 1
fi

resolve_service "$SERVICE"

EFFECTIVE_PORT="${PORT:-$SERVICE_DEFAULT_PORT}"
export "$SERVICE_PORT_ENV=$EFFECTIVE_PORT"

if [[ "$SKIP_INFRA" -ne 1 ]]; then
  "$SCRIPT_DIR/up-infra.sh"
fi

echo "Servicio: $SERVICE_NAME"
echo "Puerto: $EFFECTIVE_PORT ($SERVICE_PORT_ENV)"
echo "Modulo: $SERVICE_MODULE"

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "$SERVICE_PORT_ENV=$EFFECTIVE_PORT \"$MAVEN_WRAPPER\" spring-boot:run -pl $SERVICE_MODULE"
  exit 0
fi

cd "$REPO_ROOT"
"$MAVEN_WRAPPER" spring-boot:run -pl "$SERVICE_MODULE"

