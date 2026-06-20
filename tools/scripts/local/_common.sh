#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
RUN_DIR="$REPO_ROOT/scripts/local/.run"
LOG_DIR="$REPO_ROOT/scripts/local/logs"
MAVEN_WRAPPER="$REPO_ROOT/mvnw"

SERVICE_NAMES=(
  "course-service"
  "enrollment-service"
  "payment-service"
  "user-service"
  "content-service"
  "notification-service"
)

valid_service_names() {
  printf '%s\n' \
    "course-service / course" \
    "enrollment-service / enrollment" \
    "payment-service / payment" \
    "user-service / user" \
    "content-service / content" \
    "notification-service / notification"
}

resolve_service() {
  local name="$1"

  case "$name" in
    course-service|course)
      SERVICE_NAME="course-service"
      SERVICE_MODULE="services/course-service"
      SERVICE_PORT_ENV="COURSE_SERVICE_PORT"
      SERVICE_DEFAULT_PORT="8081"
      ;;
    enrollment-service|enrollment)
      SERVICE_NAME="enrollment-service"
      SERVICE_MODULE="services/enrollment-service"
      SERVICE_PORT_ENV="ENROLLMENT_SERVICE_PORT"
      SERVICE_DEFAULT_PORT="8082"
      ;;
    payment-service|payment)
      SERVICE_NAME="payment-service"
      SERVICE_MODULE="services/payment-service"
      SERVICE_PORT_ENV="PAYMENT_SERVICE_PORT"
      SERVICE_DEFAULT_PORT="8083"
      ;;
    user-service|user)
      SERVICE_NAME="user-service"
      SERVICE_MODULE="services/user-service"
      SERVICE_PORT_ENV="USER_SERVICE_PORT"
      SERVICE_DEFAULT_PORT="8084"
      ;;
    content-service|content)
      SERVICE_NAME="content-service"
      SERVICE_MODULE="services/content-service"
      SERVICE_PORT_ENV="CONTENT_SERVICE_PORT"
      SERVICE_DEFAULT_PORT="8085"
      ;;
    notification-service|notification)
      SERVICE_NAME="notification-service"
      SERVICE_MODULE="services/notification-service"
      SERVICE_PORT_ENV="NOTIFICATION_SERVICE_PORT"
      SERVICE_DEFAULT_PORT="8086"
      ;;
    *)
      echo "Servicio invalido '$name'. Valores validos:" >&2
      valid_service_names >&2
      return 1
      ;;
  esac
}

ensure_dir() {
  mkdir -p "$1"
}

pid_path_for_service() {
  local service_name="$1"
  echo "$RUN_DIR/$service_name.pid"
}

process_is_running() {
  local pid="$1"
  kill -0 "$pid" 2>/dev/null
}

service_pid() {
  local service_name="$1"
  local pid_path
  pid_path="$(pid_path_for_service "$service_name")"

  if [[ ! -f "$pid_path" ]]; then
    return 1
  fi

  local pid
  pid="$(tr -d '[:space:]' < "$pid_path")"
  if [[ -z "$pid" ]]; then
    return 1
  fi

  if process_is_running "$pid"; then
    echo "$pid"
    return 0
  fi

  return 1
}

health_url_for_current_service() {
  local port="${!SERVICE_PORT_ENV:-}"
  if [[ -z "$port" ]]; then
    port="$SERVICE_DEFAULT_PORT"
  fi

  echo "http://localhost:$port/actuator/health"
}

stop_process_tree() {
  local pid="$1"
  local children
  children="$(pgrep -P "$pid" 2>/dev/null || true)"

  for child in $children; do
    stop_process_tree "$child"
  done

  if process_is_running "$pid"; then
    kill "$pid" 2>/dev/null || true
  fi
}
