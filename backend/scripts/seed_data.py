#!/usr/bin/env bash
set -euo pipefail

# Resolve repo root regardless of where the script is called from
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Waiting for Postgres to be healthy..."

# Wait up to 60 seconds for the postgres container
for i in $(seq 1 60); do
    if docker exec codebridge_postgres pg_isready -U codebridge_user -d codebridge_db >/dev/null 2>&1; then
        echo "Postgres is ready."
        break
    fi
    if [ "$i" -eq 60 ]; then
        echo "ERROR: Postgres did not become healthy in 60s." >&2
        exit 1
    fi
    sleep 1
done

echo "Running seed script..."

# Run seed script inside the backend container (has DB access via docker network)
docker exec codebridge_backend python scripts/seed_data.py

echo "Seed data loaded successfully."

# TO MAKE EXECUTABLE: chmod +x scripts/seed-data.sh
