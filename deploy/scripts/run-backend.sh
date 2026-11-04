#!/usr/bin/env bash
# 后端启动脚本：统一 JDK / 端口 / 日志，便于本地与联调复现。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
export JAVA_HOME="$ROOT/.toolchain/jdk-17.0.21+9"
PORT="${1:-8080}"
LOG="${2:-/tmp/csp-app.log}"
JAR="$ROOT/backend/csp-platform/target/csp-platform.jar"
[ -f "$JAR" ] || { echo "缺少 $JAR，请先 mvn package"; exit 1; }

if command -v ss >/dev/null && ss -ltn 2>/dev/null | grep -q ":$PORT "; then
  echo "端口 $PORT 已被占用，请换端口：$0 <port>"
  exit 2
fi

nohup setsid "$JAVA_HOME/bin/java" -jar "$JAR" \
  --spring.profiles.active=dev --server.port="$PORT" > "$LOG" 2>&1 < /dev/null &
echo "启动中 port=$PORT pid=$! log=$LOG"
