@echo off
REM Скрипт запуска проекта (Неделя 4): поднимает PostgreSQL и бэкенд через Docker Compose.
REM Требуется: Docker Desktop (или Docker + Docker Compose).

set COMPOSE_CMD=
where docker >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Docker not found in PATH. Install Docker Desktop or Docker Engine.
    exit /b 1
)

docker compose version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set COMPOSE_CMD=docker compose
) else (
    docker-compose version >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        set COMPOSE_CMD=docker-compose
    ) else (
        echo Docker Compose not found. Install Docker Desktop or docker-compose.
        exit /b 1
    )
)

cd /d "%~dp0.."
echo Starting PostgreSQL and backend...
%COMPOSE_CMD% up -d
if %ERRORLEVEL% neq 0 (
    echo Failed to start. Check Docker is running.
    exit /b 1
)
echo Started. Backend: http://localhost:8080  Postgres: localhost:5432
