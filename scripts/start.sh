#!/bin/sh
# Скрипт запуска проекта (Неделя 4): PostgreSQL + бэкенд через Docker Compose. Требуется Docker (и docker compose).

set -e
cd "$(dirname "$0")/.."

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker not found. Install Docker Engine or Docker Desktop."
    exit 1
fi

if docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo "Docker Compose not found. Install docker-compose or use Docker Desktop."
    exit 1
fi

echo "Starting PostgreSQL and backend..."
$COMPOSE_CMD up -d
echo "Started. Backend: http://localhost:8080  Postgres: localhost:5432"
