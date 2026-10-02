#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Starting full stack..."

cd "$REPO_ROOT/infra"
docker-compose up -d

echo "Waiting for backend health at http://localhost:8000/health..."

# Wait up to 90 seconds for backend to report healthy
for i in $(seq 1 90); do
    if curl -sf http://localhost:8000/health >/dev/null 2>&1; then
        echo "Stack running."
        exit 0
    fi
    if [ "$i" -eq 90 ]; then
        echo "ERROR: Backend did not become healthy in 90s." >&2
        echo "Check logs: docker logs codebridge_backend" >&2
        exit 1
    fi
    sleep 1
done

# TO MAKE EXECUTABLE: chmod +x scripts/run-full-stack.sh
