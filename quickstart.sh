#!/bin/bash
# Phase 1 快速启动脚本
# 一键完成数据库迁移、后端编译、服务启动

set -e

echo "========================================="
echo "Phase 1 快速启动"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 检查依赖
echo -e "${BLUE}检查依赖...${NC}"
command -v docker >/dev/null 2>&1 || { echo -e "${RED}错误: 未安装 Docker${NC}"; exit 1; }
command -v mysql >/dev/null 2>&1 || { echo -e "${YELLOW}警告: 未安装 mysql 客户端，将使用 Docker${NC}"; }
echo -e "${GREEN}✓ 依赖检查完成${NC}"
echo ""

# 步骤 1: 数据库迁移
echo -e "${BLUE}步骤 1/4: 数据库迁移${NC}"
echo "请输入 MySQL root 密码："
read -s MYSQL_PASSWORD

echo "执行迁移脚本..."
for script in deploy/sql/18_abac_policy.sql deploy/sql/19_team_invite_code.sql deploy/sql/20_anti_cheat_deep.sql deploy/sql/21_awd_traffic_capture.sql; do
    echo "  执行 $(basename $script)..."
    if command -v mysql >/dev/null 2>&1; then
        mysql -hlocalhost -uroot -p"$MYSQL_PASSWORD" csp_platform < "$script"
    else
        docker exec -i mysql mysql -uroot -p"$MYSQL_PASSWORD" csp_platform < "$script"
    fi
done
echo -e "${GREEN}✓ 数据库迁移完成${NC}"
echo ""

# 步骤 2: 后端编译
echo -e "${BLUE}步骤 2/4: 后端编译${NC}"
cd backend
echo "使用阿里云镜像编译..."
docker run --rm \
  -v "${PWD}:/app" \
  -v "${PWD}/settings.xml:/root/.m2/settings.xml" \
  -w /app \
  maven:3.9-eclipse-temurin-21 \
  mvn clean package -DskipTests
cd ..
echo -e "${GREEN}✓ 后端编译完成${NC}"
echo ""

# 步骤 3: 启动后端服务
echo -e "${BLUE}步骤 3/4: 启动后端服务${NC}"
cd deploy
docker-compose up -d backend
cd ..
echo "等待服务启动..."
sleep 10

# 检查服务状态
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo -e "${GREEN}✓ 后端服务启动成功${NC}"
else
    echo -e "${RED}✗ 后端服务启动失败，请检查日志${NC}"
    exit 1
fi
echo ""

# 步骤 4: 前端构建（可选）
echo -e "${BLUE}步骤 4/4: 前端构建${NC}"
read -p "是否构建前端？(y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd frontend
    npm install
    npm run build
    cd ..
    echo -e "${GREEN}✓ 前端构建完成${NC}"
    
    # 部署到 nginx
    read -p "是否部署到 nginx？(y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        sudo cp -r frontend/dist/* /usr/share/nginx/html/
        sudo systemctl reload nginx
        echo -e "${GREEN}✓ 前端部署完成${NC}"
    fi
else
    echo -e "${YELLOW}跳过前端构建${NC}"
fi
echo ""

# 完成
echo "========================================="
echo -e "${GREEN}Phase 1 部署完成！${NC}"
echo "========================================="
echo ""
echo "访问地址："
echo "  前端: http://localhost"
echo "  后端 API: http://localhost:8080"
echo "  API 文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "下一步："
echo "  1. 运行 API 测试: ./deploy/test_phase1_api.sh"
echo "  2. 或使用 Postman: deploy/postman_collection_phase1.json"
echo ""
