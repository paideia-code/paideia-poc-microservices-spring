#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/_common.sh"

ONLY=()
SKIP_INFRA=0
DRY_RUN=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --only)
      ONLY+=("$2")
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
      echo "Uso: $0 [--only <service>]... [--skip-infra] [--dry-run]"
      exit 0
      ;;
    *)
      ONLY+=("$1")
      shift
      ;;
  esac
done

ensure_dir "$RUN_DIR"
ensure_dir "$LOG_DIR"

if [[ "$SKIP_INFRA" -ne 1 ]]; then
  "$SCRIPT_DIR/up-infra.sh"
fi

if [[ "${#ONLY[@]}" -eq 0 ]]; then
  SELECTED_SERVICES=("${SERVICE_NAMES[@]}")
else
  SELECTED_SERVICES=("${ONLY[@]}")
fi

for requested_service in "${SELECTED_SERVICES[@]}"; do
  resolve_service "$requested_service"

  existing_pid="$(service_pid "$SERVICE_NAME" || true)"
  if [[ -n "$existing_pid" ]]; then
    echo "$SERVICE_NAME ya esta corriendo con PID $existing_pid."
    continue
  fi

  pid_path="$(pid_path_for_service "$SERVICE_NAME")"
  out_log="$LOG_DIR/$SERVICE_NAME.out.log"
  err_log="$LOG_DIR/$SERVICE_NAME.err.log"

  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "$SERVICE_NAME: $SERVICE_PORT_ENV=$SERVICE_DEFAULT_PORT nohup \"$MAVEN_WRAPPER\" spring-boot:run -pl $SERVICE_MODULE"
    continue
  fi

  echo "Levantando $SERVICE_NAME en puerto $SERVICE_DEFAULT_PORT..."
  (
    cd "$REPO_ROOT"
    export "$SERVICE_PORT_ENV=$SERVICE_DEFAULT_PORT"
    nohup "$MAVEN_WRAPPER" spring-boot:run -pl "$SERVICE_MODULE" >"$out_log" 2>"$err_log" &
    echo "$!" >"$pid_path"
  )
done

if [[ "$DRY_RUN" -ne 1 ]]; then
  echo "Logs: $LOG_DIR"
fi

