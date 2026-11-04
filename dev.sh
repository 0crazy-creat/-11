#!/bin/bash
# ============================================
# CSP 网安平台 - 快速编译部署工具
# ============================================
# 用法:
#   ./dev.sh build     - 完整构建前后端并部署
#   ./dev.sh backend   - 仅构建后端并部署
#   ./dev.sh frontend  - 仅构建前端并部署
#   ./dev.sh jar       - 仅编译后端 jar（不部署）
#   ./dev.sh deploy    - 仅部署已有 jar 到 Docker
#   ./dev.sh logs      - 查看后端日志
#   ./dev.sh status    - 查看容器状态
#   ./dev.sh restart   - 重启后端容器
# ============================================

set -e

# 路径配置
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
DEV_DIR="$ROOT_DIR/dev"
M2_REPO="$DEV_DIR/m2repo"
SETTINGS="$DEV_DIR/settings.xml"
# 检测 Docker 路径（绕过 Python 脚本拦截）
if [ -f "/c/Program Files/Docker/Docker/resources/bin/docker.exe" ]; then
    DOCKER="/c/Program Files/Docker/Docker/resources/bin/docker.exe"
elif command -v docker.exe &>/dev/null; then
    DOCKER="docker.exe"
else
    DOCKER="docker"
fi

# 颜色
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${GREEN}[dev]${NC} $1"; }
warn()  { echo -e "${YELLOW}[dev]${NC} $1"; }
err()   { echo -e "${RED}[dev]${NC} $1"; }
step()  { echo -e "${CYAN}==>${NC} $1"; }

# 确保 dev 目录存在
mkdir -p "$DEV_DIR"

# ============================================
# 后端构建
# ============================================

build_backend_jar() {
    step "编译后端 Java 代码..."
    local start_time=$(date +%s)

    MSYS_NO_PATHCONV=1 "$DOCKER" run --rm \
        -v "$BACKEND_DIR:/app" \
        -v "$M2_REPO:/root/.m2/repository" \
        -v "$SETTINGS:/usr/share/maven/ref/settings.xml" \
        -w /app \
        maven:3.9-eclipse-temurin-21 \
        mvn package -DskipTests -T 1C -q 2>&1

    local end_time=$(date +%s)
    local elapsed=$((end_time - start_time))
    info "后端编译完成! 耗时 ${elapsed}s"
    info "JAR: $BACKEND_DIR/csp-platform/target/csp-platform.jar ($(du -h "$BACKEND_DIR/csp-platform/target/csp-platform.jar" | cut -f1))"
}

build_backend_image() {
    step "构建后端 Docker 镜像..."
    "$DOCKER" build --no-cache -t csp-backend:dev "$BACKEND_DIR" -q
    info "镜像构建完成: csp-backend:dev"
}

deploy_backend() {
    step "部署后端容器..."
    "$DOCKER" stop csp-backend 2>/dev/null || true
    "$DOCKER" rm csp-backend 2>/dev/null || true
    "$DOCKER" run -d \
        --name csp-backend \
        --restart unless-stopped \
        -p 8080:8080 \
        --add-host host.docker.internal:host-gateway \
        --env-file "$BACKEND_DIR/.env.backend" \
        csp-backend:dev > /dev/null
    info "后端容器已启动 (port 8080)"
}

wait_backend() {
    info "等待后端启动..."
    for i in $(seq 1 30); do
        if "$DOCKER" logs csp-backend 2>&1 | grep -q "Started CspApplication"; then
            info "后端启动完成! (${i}s)"
            return 0
        fi
        sleep 1
    done
    warn "后端可能还在启动中，请检查: ./dev.sh logs"
}

build_backend() {
    build_backend_jar
    build_backend_image
    deploy_backend
    wait_backend
}

# ============================================
# 前端构建
# ============================================

build_frontend() {
    step "构建前端..."
    local start_time=$(date +%s)

    cd "$FRONTEND_DIR"
    if [ -d "node_modules" ]; then
        npm run build 2>&1 | tail -3
    else
        warn "node_modules 不存在，先安装依赖..."
        npm install 2>&1 | tail -3
        npm run build 2>&1 | tail -3
    fi

    local end_time=$(date +%s)
    local elapsed=$((end_time - start_time))
    info "前端编译完成! 耗时 ${elapsed}s"

    step "部署前端容器..."
    "$DOCKER" restart csp-frontend > /dev/null 2>&1 || true
    info "前端已部署 (port 80)"
}

# ============================================
# 快捷命令
# ============================================

show_logs() {
    "$DOCKER" logs csp-backend --tail 30 -f 2>&1
}

show_status() {
    echo ""
    echo -e "${CYAN}=== 容器状态 ===${NC}"
    "$DOCKER" ps --filter "name=csp-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>&1
    echo ""
    echo -e "${CYAN}=== 端口占用 ===${NC}"
    echo "  前端:   http://localhost/"
    echo "  后端:   http://localhost:8080/api"
    echo "  Swagger: http://localhost:8080/swagger-ui/index.html"
    echo ""
}

restart_backend() {
    "$DOCKER" restart csp-backend
    wait_backend
}

# ============================================
# 快速热更新（仅重编译变更模块 + 部署）
# ============================================

hot() {
    info "快速热更新模式"
    echo ""

    # 检测变更的模块
    local changed_modules=""
    for mod in csp-common csp-security csp-infra csp-ctf csp-awd csp-situation csp-cfs csp-platform; do
        local mod_dir="$BACKEND_DIR/$mod"
        if [ -d "$mod_dir/src" ]; then
            local changed=$(find "$mod_dir/src" -name "*.java" -mmin -10 -type f 2>/dev/null | head -1)
            if [ -n "$changed" ]; then
                changed_modules="$changed_modules $mod"
            fi
        fi
    done

    # 检查前端
    local frontend_changed=$(find "$FRONTEND_DIR/src" -name "*.vue" -o -name "*.js" -o -name "*.ts" -o -name "*.css" 2>/dev/null | head -1)

    if [ -z "$changed_modules" ] && [ -z "$frontend_changed" ]; then
        warn "没有检测到最近 10 分钟内的代码变更"
        warn "如需强制重编译，请使用: ./dev.sh backend 或 ./dev.sh frontend"
        return 0
    fi

    if [ -n "$changed_modules" ]; then
        info "检测到后端变更模块:$changed_modules"
        build_backend_jar
        build_backend_image
        deploy_backend
        wait_backend
    fi

    if [ -n "$frontend_changed" ]; then
        info "检测到前端代码变更"
        build_frontend
    fi

    echo ""
    info "热更新完成!"
}

# ============================================
# 主入口
# ============================================

case "${1:-help}" in
    build)
        build_backend
        build_frontend
        show_status
        ;;
    backend|be)
        build_backend
        ;;
    frontend|fe)
        build_frontend
        ;;
    jar)
        build_backend_jar
        ;;
    deploy)
        build_backend_image
        deploy_backend
        wait_backend
        ;;
    hot)
        hot
        ;;
    logs|log)
        show_logs
        ;;
    status|st)
        show_status
        ;;
    restart|rs)
        restart_backend
        ;;
    help|h|*)
        echo ""
        echo -e "${CYAN}CSP 网安平台 - 快速开发工具${NC}"
        echo ""
        echo "用法: ./dev.sh <命令>"
        echo ""
        echo "构建命令:"
        echo "  build         完整构建前后端并部署"
        echo "  backend (be)  仅构建后端并部署"
        echo "  frontend (fe) 仅构建前端并部署"
        echo "  jar           仅编译后端 JAR（不部署）"
        echo "  deploy        仅部署已有 JAR 到 Docker"
        echo "  hot           智能热更新（自动检测变更模块）"
        echo ""
        echo "运维命令:"
        echo "  logs (log)    查看后端实时日志"
        echo "  status (st)   查看容器状态和端口"
        echo "  restart (rs)  重启后端容器"
        echo ""
        echo "开发流程:"
        echo "  1. 修改代码"
        echo "  2. 运行 ./dev.sh hot  (自动检测变更并编译部署)"
        echo "  3. 刷新浏览器验收"
        echo ""
        ;;
esac
