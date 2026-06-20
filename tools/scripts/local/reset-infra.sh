#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

FORCE=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force)
      FORCE=1
      shift
      ;;
    -h|--help)
      echo "Uso: $0 [--force]"
      exit 0
      ;;
    *)
      echo "Argumento no reconocido: $1" >&2
      exit 1
      ;;
  esac
done

if [[ "$FORCE" -ne 1 ]]; then
  echo "Esto detendra Docker Compose y borrara los volumenes locales de PostgreSQL y MongoDB."
  read -r -p "Escribe RESET para continuar: " confirmation
  if [[ "$confirmation" != "RESET" ]]; then
    echo "Operacion cancelada."
    exit 1
  fi
fi

cd "$REPO_ROOT"
echo "Reiniciando infraestructura local y borrando volumenes..."
docker compose down -v
docker compose up -d

