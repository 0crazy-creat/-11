#!/usr/bin/env bash
# 一键热部署：源码改动 → 重新打包 → 重启容器（跳过 docker build）
# 用法: ./hotdeploy.sh [be|fe|all]   默认 all
set -e
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
WHAT="${1:-all}"
JDK="$ROOT/.toolchain/jdk-17.0.21+9"
MVN="$ROOT/.toolchain/apache-maven-3.9.9/bin/mvn"
export JAVA_HOME="$JDK"; export PATH="$JDK/bin:$PATH"

if [ "$WHAT" = "fe" ] || [ "$WHAT" = "all" ]; then
  echo "[1/4] 前端构建..."
  ( cd "$ROOT/frontend" && npm run build >/tmp/fe.log 2>&1 ) || { tail -20 /tmp/fe.log; exit 1; }
  rm -rf "$ROOT/backend/csp-platform/src/main/resources/static/"*
  cp -r "$ROOT/frontend/dist/"* "$ROOT/backend/csp-platform/src/main/resources/static/"
  echo "      静态资源已更新"
fi

if [ "$WHAT" = "be" ] || [ "$WHAT" = "fe" ] || [ "$WHAT" = "all" ]; then
  echo "[2/4] 后端打包（含最新 static）..."
  ( cd "$ROOT/backend" && "$MVN" -o -q -s ../.m2/settings.xml \
      -Dmaven.repo.local=../.m2repo -DskipTests package >/tmp/be.log 2>&1 ) \
      || { tail -30 /tmp/be.log; exit 1; }
  echo "      jar 已生成"
fi

echo "[3/4] 注入运行容器..."
docker cp "$ROOT/backend/csp-platform/target/csp-platform.jar" csp-platform-app:/app/app.jar

echo "[4/4] 重启应用进程..."
docker restart csp-platform-app >/dev/null
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:11001/ 2>/dev/null || echo 000)
  [ "$code" = "200" ] && { echo "✅ 就绪 (${i}s)  http://127.0.0.1:11001"; exit 0; }
  sleep 1
done
echo "❌ 60s 未就绪，查看日志: docker logs csp-platform-app --tail 80"
exit 1
