#!/bin/bash
# 独立靶场服务全量功能验证
B=${B:-http://127.0.0.1:11002/api}
PASS=0; FAIL=0; SKIP=0
ok(){ echo "  ✅ $1"; PASS=$((PASS+1)); }
no(){ echo "  ❌ $1"; FAIL=$((FAIL+1)); }
sk(){ echo "  ⏭  $1"; SKIP=$((SKIP+1)); }
jget(){ python3 -c "import json,sys;d=json.load(sys.stdin);print(eval('d'+sys.argv[1]))" "$1" 2>/dev/null; }

chk(){
  local name="$1" method="$2" url="$3" body="$4" expect="${5:-0}"
  local resp code
  if [ "$method" = "GET" ]; then
    resp=$(curl -s -m 15 "$B$url")
  else
    resp=$(curl -s -m 30 -X "$method" "$B$url" -H 'Content-Type: application/json' -d "$body")
  fi
  code=$(echo "$resp" | jget "['code']")
  if [ "$code" = "$expect" ]; then ok "$name [code=$code]"; else no "$name [code=$code] $resp" | head -c 300; fi
  echo "$resp" > /tmp/arena_last.json
}

echo "════════════════════════════════════════════════════════"
echo "  独立靶场服务 · 全量功能验证  ($B)"
echo "════════════════════════════════════════════════════════"

echo "▶ 阶段 1：健康与基础信息"
chk "GET /arena/health 健康检查" GET /arena/health "" 0
DOCKER=$(cat /tmp/arena_last.json | jget "['data']['dockerAvailable']")
[ "$DOCKER" = "True" ] && ok "Docker 可用性 = $DOCKER" || no "Docker 不可用"

echo "▶ 阶段 2：模板管理 CRUD"
chk "GET /templates 模板分页" GET "/templates?page=1&size=10" "" 0
chk "GET /templates/enabled 启用模板" GET /templates/enabled "" 0
chk "GET /templates/9100000000000000001 模板详情" GET /templates/9100000000000000001 "" 0
chk "GET /templates/code/basic-web 按编码查" GET /templates/code/basic-web "" 0

SUF=$(date +%s)
chk "POST /templates 新建模板" POST /templates \
  "{\"code\":\"test-$SUF\",\"name\":\"验证模板\",\"category\":\"WEB\",\"image\":\"csp/arena-basic-web:1.0\",\"containerPort\":80,\"difficulty\":\"EASY\",\"ttlMinutes\":60}" 0
NEW_TPL=$(cat /tmp/arena_last.json | jget "['data']['id']")
chk "POST /templates 重复编码应拒" POST /templates \
  "{\"code\":\"test-$SUF\",\"name\":\"重复\",\"image\":\"x\"}" 409
chk "PUT /templates/$NEW_TPL 更新模板" PUT "/templates/$NEW_TPL" \
  "{\"code\":\"test-$SUF\",\"name\":\"验证模板-改\",\"category\":\"WEB\",\"image\":\"csp/arena-basic-web:1.0\",\"containerPort\":80}" 0
chk "PUT /templates/$NEW_TPL/status 停用" PUT "/templates/$NEW_TPL/status?status=0" "" 0
chk "DELETE /templates/$NEW_TPL 删除" DELETE "/templates/$NEW_TPL" "" 0

echo "▶ 阶段 3：靶机生命周期"
chk "POST /instances 创建靶机" POST "/instances?ownerId=1&ownerName=admin" \
  "{\"templateId\":9100000000000000001,\"ttlMinutes\":30}" 0
IID=$(cat /tmp/arena_last.json | jget "['data']['id']")
IURL=$(cat /tmp/arena_last.json | jget "['data']['accessUrl']")
IFLAG=$(cat /tmp/arena_last.json | jget "['data']['flag']")
echo "     实例 ID=$IID  URL=$IURL  FLAG=$IFLAG"

sleep 3
HTTP=$(curl -s -m 10 -o /tmp/t.html -w "%{http_code}" "$IURL/")
[ "$HTTP" = "200" ] && ok "靶机首页可访问 HTTP=$HTTP" || no "靶机首页访问失败 HTTP=$HTTP"

HFLAG=$(curl -s -m 10 -D - -o /dev/null "$IURL/" | grep -i 'X-Target-Flag' | tr -d '\r')
echo "$HFLAG" | grep -q "$IFLAG" && ok "响应头 flag 注入正确" || no "响应头 flag 不符: $HFLAG"

F2=$(curl -s -m 10 "$IURL/flag.txt")
[ "$F2" = "$IFLAG" ] && ok "flag.txt 内容正确" || no "flag.txt 不符: $F2"

HZ=$(curl -s -m 10 "$IURL/healthz")
[ "$HZ" = "ok" ] && ok "healthz 探活正常" || no "healthz 异常: $HZ"

chk "GET /instances/$IID 实例详情" GET "/instances/$IID?showFlag=true" "" 0
chk "GET /instances/mine 我的靶机" GET "/instances/mine?ownerId=1" "" 0
chk "POST /instances/$IID/health-check 单台巡检" POST "/instances/$IID/health-check?actorId=1" "" 0
chk "POST /instances/$IID/reset 重置靶机" POST "/instances/$IID/reset?actorId=1" "" 0
NEWFLAG=$(cat /tmp/arena_last.json | jget "['data']['flag']")
NEWURL=$(cat /tmp/arena_last.json | jget "['data']['accessUrl']")
[ "$NEWFLAG" != "$IFLAG" ] && ok "重置后 flag 已变更" || no "重置后 flag 未变"
sleep 3
F3=$(curl -s -m 10 "$NEWURL/flag.txt")
[ "$F3" = "$NEWFLAG" ] && ok "重置后新 flag 生效" || no "重置后 flag 不匹配: $F3 vs $NEWFLAG"

echo "▶ 阶段 4：并发与端口池"
chk "创建实例 #2" POST "/instances?ownerId=2&ownerName=u2" \
  "{\"templateId\":9100000000000000002,\"ttlMinutes\":30}" 0
I2=$(cat /tmp/arena_last.json | jget "['data']['id']")
U2=$(cat /tmp/arena_last.json | jget "['data']['accessUrl']")
chk "创建实例 #3（nginx）" POST "/instances?ownerId=3&ownerName=u3" \
  "{\"templateId\":9100000000000000003,\"ttlMinutes\":30}" 0
I3=$(cat /tmp/arena_last.json | jget "['data']['id']")
U3=$(cat /tmp/arena_last.json | jget "['data']['accessUrl']")
sleep 3
[ "$U2" != "$NEWURL" ] && ok "端口互不冲突: $NEWURL / $U2 / $U3" || no "端口重复"
H2=$(curl -s -m 10 -o /dev/null -w "%{http_code}" "$U2/")
[ "$H2" = "200" ] && ok "实例#2 可访问" || no "实例#2 不可访问 HTTP=$H2"
H3=$(curl -s -m 10 -o /dev/null -w "%{http_code}" "$U3/")
[ "$H3" = "200" ] && ok "实例#3(nginx) 可访问" || no "实例#3 不可访问 HTTP=$H3"

echo "▶ 阶段 5：统计与监控"
chk "GET /instances/stats 总览统计" GET /instances/stats "" 0
RUN=$(cat /tmp/arena_last.json | jget "['data']['runningCount']")
[ "$RUN" -ge 3 ] 2>/dev/null && ok "运行中实例数 = $RUN" || no "运行中实例数异常: $RUN"
chk "GET /instances/monitor 靶机监控" GET "/instances/monitor?ownerId=1" "" 0
chk "GET /instances 分页列表" GET "/instances?page=1&size=10" "" 0
chk "GET /instances?status=RUNNING 状态过滤" GET "/instances?status=RUNNING" "" 0

echo "▶ 阶段 6：巡检与回收"
chk "POST /instances/health-check-all 全量巡检" POST /instances/health-check-all "" 0
chk "POST /instances/reclaim-expired 过期回收" POST /instances/reclaim-expired "" 0

echo "▶ 阶段 7：审计日志"
chk "GET /instances/logs 操作日志" GET "/instances/logs?limit=20" "" 0
LOGN=$(cat /tmp/arena_last.json | python3 -c "import json,sys;print(len(json.load(sys.stdin)['data']))" 2>/dev/null)
[ "$LOGN" -gt 0 ] 2>/dev/null && ok "审计日志条数 = $LOGN" || no "审计日志为空"

echo "▶ 阶段 8：异常与边界"
chk "创建不存在的模板应拒" POST "/instances?ownerId=1" "{\"templateId\":999999999999}" 404
chk "缺少必填参数应拒" POST "/instances?ownerId=1" "{}" 400
chk "查询不存在实例应拒" GET "/instances/999999999999" "" 404
chk "不存在的接口返回 404 业务码" GET "/not-exist-api" "" 404
chk "重复创建超限应拒" POST "/instances?ownerId=1&ownerName=admin" \
  "{\"templateId\":9100000000000000001,\"ttlMinutes\":30}" 409

echo "▶ 阶段 9：清理"
chk "POST /instances/destroy-all 全部销毁" POST "/instances/destroy-all?ownerId=1" "" 0
chk "销毁实例#2" POST "/instances/$I2/destroy?actorId=2" "" 0
chk "销毁实例#3" POST "/instances/$I3/destroy?actorId=3" "" 0
chk "GET /instances/stats 清理后统计" GET /instances/stats "" 0
RUN2=$(cat /tmp/arena_last.json | jget "['data']['runningCount']")
[ "$RUN2" = "0" ] && ok "清理后运行中 = 0" || no "清理后仍有运行实例: $RUN2"

echo "════════════════════════════════════════════════════════"
echo "  汇总：通过 $PASS / 失败 $FAIL / 跳过 $SKIP"
echo "════════════════════════════════════════════════════════"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
