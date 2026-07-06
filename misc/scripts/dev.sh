#!/usr/bin/env bash

MODE="api"

for arg in "$@"; do
  case "$arg" in
    --mock)
      MODE="mock"
      ;;
    --api)
      MODE="api"
      ;;
    *)
      echo "Usage: $0 [--mock|--api]"
      echo "  --mock  use mock auth (no backend needed)"
      echo "  --api   use real backend auth (default)"
      exit 1
      ;;
  esac
done

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
BACKEND_DIR="$ROOT_DIR/jtech-tasklist-backend"
FRONTEND_DIR="$ROOT_DIR/jtech-tasklist-frontend"

cleanup() {
  local exit_code=$?
  echo ""
  echo "Shutting down..."
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
  wait "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
  if [ "$MODE" = "api" ]; then
    (cd "$BACKEND_DIR" && docker compose down) || true
  fi
  echo "Done."
  exit "$exit_code"
}

trap cleanup EXIT INT TERM

if [ "$MODE" = "api" ]; then
  echo "Starting PostgreSQL..."
  (cd "$BACKEND_DIR" && docker compose up -d)
  echo "Waiting for PostgreSQL to be ready..."
  until (cd "$BACKEND_DIR" && docker compose exec -T postgres pg_isready -U postgres -q) 2>/dev/null; do
    sleep 1
  done

  echo "Starting backend..."
  (cd "$BACKEND_DIR" && ./gradlew bootRun) &
  BACKEND_PID=$!
fi

echo "Starting frontend..."
(cd "$FRONTEND_DIR" && VITE_AUTH_MODE="$MODE" npm run dev) &
FRONTEND_PID=$!

echo ""
echo "Frontend: http://localhost:5173 (mode: $MODE)"
if [ "$MODE" = "api" ]; then
  echo "Backend:  http://localhost:8080"
fi
echo "Press Ctrl+C to stop."
echo ""

wait
