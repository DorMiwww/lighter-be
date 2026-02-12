#!/usr/bin/env bash
set -euo pipefail

SERVER="webdock"
REMOTE_DIR="~/lighter-be"

echo "Building fat JAR..."
./gradlew buildFatJar

echo "Deploying to $SERVER..."
ssh "$SERVER" "mkdir -p $REMOTE_DIR/build/libs"
scp build/libs/*-all.jar "$SERVER:$REMOTE_DIR/build/libs/"
scp Dockerfile docker-compose.yaml .dockerignore "$SERVER:$REMOTE_DIR/"

echo "Restarting container..."
ssh "$SERVER" "cd $REMOTE_DIR && docker compose up --build -d"

echo "Done!"
