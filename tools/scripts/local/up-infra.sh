#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

cd "$REPO_ROOT"
echo "Levantando infraestructura local con Docker Compose..."
docker compose up -d

