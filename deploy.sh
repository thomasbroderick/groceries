#!/bin/bash
set -e

REMOTE_HOST="root@216.128.137.221"
REMOTE_DIR="/opt/groceries"
JAR_NAME="groceries-0.0.1-SNAPSHOT.jar"
SSH_SOCKET="/tmp/groceries-deploy-ssh"
SSH_OPTS="-o ControlMaster=auto -o ControlPath=$SSH_SOCKET -o ControlPersist=60"

echo "Building..."
./gradlew build -x test

echo "Deploying to $REMOTE_HOST..."
ssh $SSH_OPTS "$REMOTE_HOST" "mkdir -p $REMOTE_DIR"
scp -o ControlMaster=auto -o ControlPath="$SSH_SOCKET" "build/libs/$JAR_NAME" "$REMOTE_HOST:$REMOTE_DIR/$JAR_NAME"

echo "Restarting service..."
ssh $SSH_OPTS "$REMOTE_HOST" "systemctl restart groceries"

ssh $SSH_OPTS -O exit "$REMOTE_HOST" 2>/dev/null || true

echo "Done. Check status with: ssh $REMOTE_HOST 'systemctl status groceries'"
