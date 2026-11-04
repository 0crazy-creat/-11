#!/usr/bin/env bash
# 单端口验收服务：11001 同时提供前端 SPA 与后端 API
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
export JAVA_HOME="$ROOT/.toolchain/jdk-17.0.21+9"
PORT=11001
LOG=/tmp/csp-11001.log
JAR="$ROOT/backend/csp-platform/target/csp-platform.jar"
[ -f "$JAR" ] || { echo "缺少 jar，请先构建"; exit 1; }
if command -v ss >/dev/null && ss -ltn 2>/dev/null | grep -q ":$PORT "; then
  echo "端口 $PORT 已被占用"; exit 2
fi
nohup setsid "$JAVA_HOME/bin/java" -jar "$JAR" \
  --spring.profiles.active=dev --server.port=$PORT > "$LOG" 2>&1 < /dev/null &
echo "启动中 port=$PORT pid=$! log=$LOG"
