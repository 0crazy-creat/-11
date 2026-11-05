#!/usr/bin/env bash
# 一键热部署（与当前 Docker 运行拓扑对齐）
#   后端：maven 容器打包 → docker cp 注入 csp-backend:/app/app.jar → 重启（容器内没有 jar 挂载，必须 cp）
#   前端：npm run build → csp-frontend 直接 bind mount frontend/dist，构建即上线，无需重启
# 用法: ./hotdeploy.sh [be|fe|all]   默认 all
# Git Bash 需带 MSYS_NO_PATHCONV=1，否则 -v /work 这类容器挂载参数会被转成 Windows 路径
# 可覆盖：DOCKER（docker 可执行文件）、CSP_BUILD_DIR、PROBE、BE_CONTAINER
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
# Git Bash 里 `docker`（无扩展名）是 Docker Desktop 的 sh 包装，只会回显 powershell 命令而不执行，
# 必须用 docker.exe。其余平台用 DOCKER=... 覆盖。
DOCKER="${DOCKER:-$(command -v docker.exe || command -v docker)}"
WHAT="${1:-all}"
# 必须是纯 ASCII 路径：源码目录含中文时 Docker bind mount 会失败，先复制出来再编译。
# Git Bash 的 /tmp 落在含中文的用户目录下，因此按仓库所在盘符拼一个干净的盘根目录。
DRIVE="$(cygpath -w "$ROOT" 2>/dev/null | cut -d: -f1)"
[ -n "$DRIVE" ] || DRIVE="C"
WORK="${CSP_BUILD_DIR:-${DRIVE}:/csp-build}"
JAR_NAME="csp-platform/target/csp-platform.jar"
PROBE="${PROBE:-http://127.0.0.1:8080/api/contests}"
BE_CONTAINER="${BE_CONTAINER:-csp-backend}"

deploy_fe() {
  echo "[fe] 前端构建：$ROOT/frontend → dist/（bind mount 即生效）"
  ( cd "$ROOT/frontend" && npm run build ) || { echo "❌ 前端构建失败"; return 1; }
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1/" 2>/dev/null || echo 000)
  echo "✅ 前端就绪  http://127.0.0.1/  [HTTP $code]"
}

deploy_be() {
  echo "[be] 同步源码到 ASCII 工作区：$WORK"
  rm -rf "$WORK" && mkdir -p "$WORK"
  ( cd "$ROOT/backend" && tar --exclude='target' -cf - . ) | ( cd "$WORK" && tar -xf - )
  [ -f "$WORK/pom.xml" ] || { echo "❌ 源码同步失败"; return 1; }

  echo "[be] maven 容器打包（日志 $WORK/build.log）"
  MSYS_NO_PATHCONV=1 "$DOCKER" run --rm -v "$WORK:/work" -v csp-m2:/root/.m2 -w /work \
    maven:3.9-eclipse-temurin-21 mvn -B -s settings.xml -DskipTests package > "$WORK/build.log" 2>&1
  [ $? -eq 0 ] || { echo "❌ 打包失败，末 30 行："; tail -30 "$WORK/build.log"; return 1; }
  [ -f "$WORK/$JAR_NAME" ] || { echo "❌ 未产出 jar：$JAR_NAME"; return 1; }

  echo "[be] 注入 $BE_CONTAINER 并重启"
  "$DOCKER" cp "$WORK/$JAR_NAME" "$BE_CONTAINER:/app/app.jar" || return 1
  "$DOCKER" restart "$BE_CONTAINER" >/dev/null || return 1
  for i in $(seq 1 90); do
    code=$(curl -s -o /dev/null -w '%{http_code}' "$PROBE" 2>/dev/null || echo 000)
    [ "$code" = "200" ] && { echo "✅ 后端就绪 (${i}s)  $PROBE"; return 0; }
    sleep 1
  done
  echo "❌ 90s 未就绪，查看日志: $DOCKER logs $BE_CONTAINER --tail 100"
  return 1
}

case "$WHAT" in
  fe)  deploy_fe ;;
  be)  deploy_be ;;
  all) deploy_fe && deploy_be ;;
  *)   echo "用法: $0 [be|fe|all]"; exit 2 ;;
esac
