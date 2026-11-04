#!/bin/bash
# 独立靶场服务全量验证（可重复执行）
B=http://127.0.0.1:11002/api
PASS=0; FAIL=0
ck(){ if [ "$2" = "$3" ]; then echo "  ✅ $1"; PASS=$((PASS+1)); else echo "  ❌ $1 (期望 $2 实际 $3)"; FAIL=$((FAIL+1)); fi; }
code(){ python3 -c "import sys,json;print(json.load(sys.stdin).get('code'))" 2>/dev/null <<<"$1" || echo "-"; }
jget(){ python3 -c "import sys,json;d=json.load(sys.stdin);print(eval('d'+sys.argv[1]))" "$1" 2>/dev/null <<<"$2"; }

echo "════════ 靶场服务验证 ════════"
R=$(curl -s $B/arena/health)
ck "健康检查" "0" "$(code "$R")"
ck "Docker 可用" "True" "$(jget "['data']['dockerAvailable']" "$R")"
ck "模板列表" "0" "$(code "$(curl -s "$B/templates?page=1&size=5")")"
ck "启用模板" "0" "$(code "$(curl -s "$B/templates/enabled")")"
ck "统计接口" "0" "$(code "$(curl -s "$B/instances/stats")")"
ck "监控接口" "0" "$(code "$(curl -s "$B/instances/monitor")")"
ck "审计日志" "0" "$(code "$(curl -s "$B/instances/logs?limit=5")")"

echo "▶ 生命周期"
R=$(curl -s -X POST "$B/instances?ownerId=1&ownerName=admin" -H 'Content-Type: application/json' \
     -d '{"templateId":9100000000000000001,"ttlMinutes":10}')
ck "创建靶机" "0" "$(code "$R")"
IID=$(jget "['data']['id']" "$R"); URL=$(jget "['data']['accessUrl']" "$R")
sleep 3
ck "靶机首页可访问" "200" "$(curl -s -m 8 -o /dev/null -w '%{http_code}' "$URL/")"
ck "flag.txt 可读" "200" "$(curl -s -m 8 -o /dev/null -w '%{http_code}' "$URL/flag.txt")"
ck "healthz 可读" "200" "$(curl -s -m 8 -o /dev/null -w '%{http_code}' "$URL/healthz")"
ck "响应头含 flag" "yes" "$(curl -s -m 8 -D - -o /dev/null "$URL/" | grep -qi 'x-target-flag' && echo yes || echo no)"
ck "重置靶机" "0" "$(code "$(curl -s -X POST "$B/instances/$IID/reset?actorId=1")")"
ck "健康巡检" "0" "$(code "$(curl -s -X POST "$B/instances/$IID/health-check?actorId=1")")"
ck "销毁靶机" "0" "$(code "$(curl -s -X POST "$B/instances/$IID/destroy?actorId=1")")"
ck "不存在实例应404" "404" "$(code "$(curl -s "$B/instances/999999")")"
ck "缺参数应400" "400" "$(code "$(curl -s -X POST "$B/instances" -H 'Content-Type: application/json' -d '{}')")"

echo "════════ 通过 $PASS / 失败 $FAIL ════════"
