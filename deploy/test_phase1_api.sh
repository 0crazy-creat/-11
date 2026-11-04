#!/bin/bash
# Phase 1 API 集成测试脚本
# 测试所有新增 API 端点

set -e

BASE_URL="http://localhost:8080"
ADMIN_TOKEN=""  # 需要登录后获取
USER_TOKEN=""   # 需要登录后获取

echo "========================================="
echo "Phase 1 API 集成测试"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_api() {
    local method=$1
    local url=$2
    local data=$3
    local desc=$4
    
    echo -e "${YELLOW}测试: ${desc}${NC}"
    echo "  ${method} ${url}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$url" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $ADMIN_TOKEN" \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$url" \
            -H "Authorization: Bearer $ADMIN_TOKEN")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "  ${GREEN}✓ 成功 (HTTP $http_code)${NC}"
        echo "  响应: $(echo $body | jq -c '.' 2>/dev/null || echo $body)"
    else
        echo -e "  ${RED}✗ 失败 (HTTP $http_code)${NC}"
        echo "  响应: $body"
    fi
    echo ""
}

# ========================================
# 1. ABAC 策略管理 API
# ========================================
echo "========================================="
echo "1. ABAC 策略管理"
echo "========================================="
echo ""

# 创建策略
test_api "POST" "/api/admin/abac/policies" '{
    "policyName": "测试策略-院校隔离",
    "description": "仅允许同院校用户查看赛事",
    "resourceType": "contest",
    "action": "view",
    "conditionExpr": "user.orgCode == resource.orgCode",
    "effect": "ALLOW",
    "priority": 10,
    "enabled": 1
}' "创建 ABAC 策略"

# 查询策略列表
test_api "GET" "/api/admin/abac/policies" "" "查询策略列表"

# 测试策略评估
test_api "POST" "/api/admin/abac/test" '{
    "userId": 1,
    "resourceType": "contest",
    "resourceId": "100",
    "action": "view",
    "resource": {"orgCode": "ORG001"}
}' "测试策略评估"

# ========================================
# 2. 队伍邀请码 API
# ========================================
echo "========================================="
echo "2. 队伍邀请码"
echo "========================================="
echo ""

# 创建战队（会自动生成邀请码）
test_api "POST" "/api/ctf/contests/100/teams" '{
    "name": "测试战队",
    "code": "TEST01",
    "description": "测试用战队",
    "slogan": "测试口号"
}' "创建战队（自动生成邀请码）"

# 生成邀请码（如果战队已存在）
# test_api "POST" "/api/ctf/contests/100/teams/1/invite-code" "" "生成邀请码"

# 查询战队信息（应包含邀请码）
test_api "GET" "/api/ctf/contests/100/teams/mine" "" "查询我的战队"

# 通过邀请码加入（需要另一个用户）
# test_api "POST" "/api/ctf/contests/100/teams/join-by-code" '{
#     "inviteCode": "ABCDEFGH"
# }' "通过邀请码加入"

# 刷新邀请码
# test_api "PUT" "/api/ctf/contests/100/teams/1/invite-code" "" "刷新邀请码"

# ========================================
# 3. CTF 反作弊分析 API
# ========================================
echo "========================================="
echo "3. CTF 反作弊分析"
echo "========================================="
echo ""

# 查询用户风险摘要
test_api "GET" "/api/ctf/contests/100/anti-cheat/users/1/risk" "" "查询用户风险摘要"

# 查询高风险用户列表
test_api "GET" "/api/ctf/contests/100/anti-cheat/high-risk?minRiskScore=60" "" "查询高风险用户"

# ========================================
# 4. AWD 流量取证 API
# ========================================
echo "========================================="
echo "4. AWD 流量取证"
echo "========================================="
echo ""

# 启动流量捕获
test_api "POST" "/api/awd/contests/100/traffic/capture/start?roundNo=1&teamId=1&targetId=1" "" "启动流量捕获"

# 查询捕获列表
test_api "GET" "/api/awd/contests/100/traffic/captures?roundNo=1&teamId=1" "" "查询捕获列表"

# 查询捕获详情（假设 captureId=1）
# test_api "GET" "/api/awd/contests/100/traffic/capture/1" "" "查询捕获详情"

# 停止捕获
# test_api "POST" "/api/awd/contests/100/traffic/capture/1/stop" "" "停止捕获"

# 查询分析结果
# test_api "GET" "/api/awd/contests/100/traffic/capture/1/analysis" "" "查询分析结果"

# 手动触发分析
# test_api "POST" "/api/awd/contests/100/traffic/capture/1/analyze" "" "手动触发分析"

# 清理过期文件
# test_api "DELETE" "/api/awd/contests/100/traffic/cleanup" "" "清理过期文件"

echo "========================================="
echo "测试完成"
echo "========================================="
