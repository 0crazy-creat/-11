#!/bin/bash
# CSP 网安平台 - 快速开发编译脚本
# 用法: ./dev-build.sh [backend|frontend|all]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 构建后端
build_backend() {
    log_info "开始构建后端..."
    
    # 使用 Docker 构建，挂载 Maven 仓库
    docker run --rm \
        -v "$BACKEND_DIR:/app" \
        -v "$PROJECT_DIR/dev/m2repo:/root/.m2/repository" \
        -v "$PROJECT_DIR/dev/settings.xml:/usr/share/maven/ref/settings.xml" \
        -w /app \
        maven:3.9-eclipse-temurin-21 \
        mvn clean package -DskipTests -T 1C -q
    
    if [ $? -eq 0 ]; then
        log_info "后端构建成功!"
        deploy_backend
    else
        log_error "后端构建失败!"
        exit 1
    fi
}

# 部署后端（热更新）
deploy_backend() {
    log_info "部署后端到运行中的容器..."
    
    # 停止旧容器
    docker stop csp-backend 2>/dev/null || true
    docker rm csp-backend 2>/dev/null || true
    
    # 重新构建镜像
    docker build -t csp-backend:dev "$BACKEND_DIR"
    
    # 启动新容器
    docker run -d \
        --name csp-backend \
        --restart unless-stopped \
        -p 8080:8080 \
        --add-host host.docker.internal:host-gateway \
        --env-file "$BACKEND_DIR/.env.backend" \
        -v "$BACKEND_DIR/logs:/app/logs" \
        csp-backend:dev
    
    log_info "后端部署完成! 等待服务启动..."
    sleep 5
    
    # 检查健康状态
    if docker logs csp-backend 2>&1 | grep -q "Started CspApplication"; then
        log_info "后端启动成功!"
    else
        log_warn "后端可能还在启动中，请检查: docker logs csp-backend"
    fi
}

# 构建前端
build_frontend() {
    log_info "开始构建前端..."
    
    cd "$FRONTEND_DIR"
    npm run build
    
    if [ $? -eq 0 ]; then
        log_info "前端构建成功!"
        deploy_frontend
    else
        log_error "前端构建失败!"
        exit 1
    fi
}

# 部署前端（热更新）
deploy_frontend() {
    log_info "部署前端到 nginx 容器..."
    
    # 前端使用 volume 挂载，只需重启 nginx
    docker restart csp-frontend
    
    log_info "前端部署完成!"
}

# 仅编译变更的 Java 文件（增量编译）
compile_java_incremental() {
    log_info "增量编译 Java 文件..."
    
    # 找到最近修改的 Java 文件（5分钟内）
    CHANGED_FILES=$(find "$BACKEND_DIR" -name "*.java" -mmin -5 -type f)
    
    if [ -z "$CHANGED_FILES" ]; then
        log_info "没有发现变更的 Java 文件"
        return 0
    fi
    
    log_info "发现变更的文件:"
    echo "$CHANGED_FILES" | sed 's/^/  - /'
    
    # 使用 Docker 内的 Maven 进行增量编译
    docker run --rm \
        -v "$BACKEND_DIR:/app" \
        -v "$PROJECT_DIR/dev/m2repo:/root/.m2/repository" \
        -v "$PROJECT_DIR/dev/settings.xml:/usr/share/maven/ref/settings.xml" \
        -w /app \
        maven:3.9-eclipse-temurin-21 \
        mvn compile -DskipTests -q
    
    if [ $? -eq 0 ]; then
        log_info "增量编译成功!"
        
        # 打包并部署
        docker run --rm \
            -v "$BACKEND_DIR:/app" \
            -v "$PROJECT_DIR/dev/m2repo:/root/.m2/repository" \
            -v "$PROJECT_DIR/dev/settings.xml:/usr/share/maven/ref/settings.xml" \
            -w /app \
            maven:3.9-eclipse-temurin-21 \
            mvn package -DskipTests -pl csp-platform -am -q
        
        deploy_backend
    else
        log_error "增量编译失败!"
        exit 1
    fi
}

# 监控文件变化并自动编译
watch_and_build() {
    log_info "开始监控文件变化... (Ctrl+C 退出)"
    
    if ! command -v inotifywait &> /dev/null; then
        log_error "需要安装 inotify-tools: apt-get install inotify-tools"
        exit 1
    fi
    
    while true; do
        inotifywait -r -e modify,create,delete \
            --include '\.(java|vue|js|ts|css)$' \
            "$BACKEND_DIR" "$FRONTEND_DIR" 2>/dev/null
        
        log_info "检测到文件变化，开始编译..."
        
        # 判断变化类型
        if echo "$CHANGED_FILES" | grep -q "\.java$"; then
            compile_java_incremental
        fi
        
        if echo "$CHANGED_FILES" | grep -qE "\.(vue|js|ts|css)$"; then
            build_frontend
        fi
        
        sleep 2
    done
}

# 主入口
case "$1" in
    backend)
        build_backend
        ;;
    frontend)
        build_frontend
        ;;
    deploy-backend)
        deploy_backend
        ;;
    deploy-frontend)
        deploy_frontend
        ;;
    incremental)
        compile_java_incremental
        ;;
    watch)
        watch_and_build
        ;;
    all)
        build_backend
        build_frontend
        ;;
    *)
        echo "用法: $0 {backend|frontend|all|deploy-backend|deploy-frontend|incremental|watch}"
        echo ""
        echo "命令说明:"
        echo "  backend         - 完整构建后端并部署"
        echo "  frontend        - 完整构建前端并部署"
        echo "  all             - 构建前后端并部署"
        echo "  deploy-backend  - 仅部署后端（使用已有的 jar）"
        echo "  deploy-frontend - 仅部署前端（使用已有的 dist）"
        echo "  incremental     - 增量编译变更的 Java 文件"
        echo "  watch           - 监控文件变化并自动编译"
        exit 1
        ;;
esac

log_info "完成!"
