#!/bin/bash
# Phase 1 数据库迁移脚本
# 执行所有新增表的创建

set -e

echo "========================================="
echo "Phase 1 数据库迁移"
echo "========================================="
echo ""

# 数据库连接配置（根据实际情况修改）
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASS="root123"
DB_NAME="csp_platform"

MYSQL_CMD="mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME"

echo "数据库: $DB_NAME @ $DB_HOST:$DB_PORT"
echo ""

# 迁移脚本列表
MIGRATIONS=(
    "18_abac_policy.sql"
    "19_team_invite_code.sql"
    "20_anti_cheat_deep.sql"
    "21_awd_traffic_capture.sql"
)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for migration in "${MIGRATIONS[@]}"; do
    echo "执行迁移: $migration"
    if $MYSQL_CMD < "$SCRIPT_DIR/$migration"; then
        echo "✓ $migration 执行成功"
    else
        echo "✗ $migration 执行失败"
        exit 1
    fi
    echo ""
done

echo "========================================="
echo "所有迁移执行完成！"
echo "========================================="
echo ""
echo "新增表："
echo "  - abac_policy"
echo "  - abac_audit_log"
echo "  - contest_team_captain_transfer"
echo "  - contest_team_invite"
echo "  - device_fingerprint"
echo "  - cheat_audit_log"
echo "  - awd_traffic_capture"
echo "  - awd_traffic_analysis"
echo ""
echo "扩展表："
echo "  - contest_team (新增邀请码字段)"
echo ""
