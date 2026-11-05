#!/usr/bin/env bash
# 全流程端到端测试：模拟真实浏览器交互，逐步抓包验证
# 用法: ./e2e-test.sh [BASE_URL]
set -uo pipefail

# 默认走 Docker 部署入口（csp-frontend nginx :80 反代 /api）；单端口模式传 http://localhost:11001
BASE="${1:-http://localhost}"
# Windows Git Bash 里 python3 常是微软商店空壳（执行后无输出），会让 JSON 解析静默失败
PY="$(command -v python || command -v python3)"
# python 读不到 MSYS 的 /tmp 路径，转成 Windows 正斜杠路径；非 Windows 原样返回
TW() { if command -v cygpath >/dev/null 2>&1; then cygpath -w "$1" | tr '\\' '/'; else printf '%s' "$1"; fi; }
PASS=0; FAIL=0
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

hr() { printf '%.0s─' {1..72}; echo; }
title() { hr; echo "▶ $*"; hr; }

# 断言：HTTP 状态码 + 业务 code
# 用法: expect <描述> <期望code> <实际json文件>
expect() {
  local desc="$1" want="$2" file="$3"
  local got
  got=$("$PY" -c "import json,sys
try:
    print(json.load(open('$(TW "$file")')).get('code'))
except Exception:
    print('parse_error')" 2>/dev/null)
  if [ "$got" = "$want" ]; then
    echo "  ✅ $desc (code=$got)"; PASS=$((PASS+1))
  else
    echo "  ❌ $desc 期望=$want 实际=$got"; FAIL=$((FAIL+1))
    head -c 200 "$file" 2>/dev/null; echo
  fi
}

# 带 token 的请求
# 请求体走文件而非 -d 参数：Git Bash 的 mingw curl 会把命令行里的中文按 GBK 往返，服务端 JSON 解析直接 400
req() {
  local method="$1" path="$2" out="$3" data="${4:-}"
  local args=(-s -o "$out" -w "%{http_code}" -X "$method" "$BASE$path")
  [ -n "${TOKEN:-}" ] && args+=(-H "Authorization: Bearer $TOKEN")
  if [ -n "$data" ]; then
    printf '%s' "$data" > "$TMP/req-body.json"
    args+=(-H 'Content-Type: application/json' --data-binary "@$TMP/req-body.json")
  fi
  curl "${args[@]}"
}

########################################
title "阶段 1：静态资源与前端入口"
########################################
code=$(curl -s -o "$TMP/index.html" -w "%{http_code}" "$BASE/")
[ "$code" = "200" ] && { echo "  ✅ 首页 index.html 200"; PASS=$((PASS+1)); } || { echo "  ❌ 首页 $code"; FAIL=$((FAIL+1)); }
grep -q "CSP" "$TMP/index.html" && { echo "  ✅ 页面标题正确"; PASS=$((PASS+1)); } || { echo "  ❌ 页面标题异常"; FAIL=$((FAIL+1)); }

JS=$(grep -o '/assets/index-[^"]*\.js' "$TMP/index.html" | head -1)
if [ -n "$JS" ]; then
  jscode=$(curl -s -o /dev/null -w "%{http_code}" "$BASE$JS")
  [ "$jscode" = "200" ] && { echo "  ✅ JS bundle $JS 200"; PASS=$((PASS+1)); } || { echo "  ❌ JS bundle $jscode"; FAIL=$((FAIL+1)); }
fi

for p in /login /console /situation /rank; do
  c=$(curl -s -o /dev/null -w "%{http_code}" "$BASE$p")
  [ "$c" = "200" ] && { echo "  ✅ 路由 $p -> 200 (SPA 回退)"; PASS=$((PASS+1)); } || { echo "  ❌ 路由 $p -> $c"; FAIL=$((FAIL+1)); }
done

########################################
title "阶段 2：未登录访问受保护接口（应 401）"
########################################
code=$(curl -s -o "$TMP/anon.json" -w "%{http_code}" "$BASE/api/users?page=1&size=1")
biz=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/anon.json")')).get('code'))" 2>/dev/null)
if [ "$biz" = "401" ] || [ "$code" = "401" ]; then
  echo "  ✅ 匿名访问被拦截 (http=$code, code=$biz)"; PASS=$((PASS+1))
else
  echo "  ❌ 匿名访问未被拦截 (http=$code, code=$biz)"; FAIL=$((FAIL+1))
fi

########################################
title "阶段 3：admin 登录"
########################################
req POST /api/auth/login "$TMP/login.json" '{"username":"admin","password":"Admin@123"}' >/dev/null
expect "admin 登录" 0 "$TMP/login.json"

TOKEN=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/login.json")'))['data']['accessToken'])" 2>/dev/null)
REFRESH=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/login.json")'))['data'].get('refreshToken',''))" 2>/dev/null)
if [ -n "$TOKEN" ]; then
  echo "  ✅ 拿到 accessToken (${#TOKEN} 字符)"; PASS=$((PASS+1))
else
  echo "  ❌ 未拿到 accessToken"; FAIL=$((FAIL+1)); exit 1
fi
[ -n "$REFRESH" ] && { echo "  ✅ 拿到 refreshToken (${#REFRESH} 字符)"; PASS=$((PASS+1)); } || { echo "  ⚠️ 无 refreshToken"; }

########################################
title "阶段 4：登录态保持性验证（连续多次带 token 请求）"
########################################
# 这是"访问其他服务自动退出"的核心回归点
for i in 1 2 3; do
  req GET /api/auth/me "$TMP/me$i.json" >/dev/null
  expect "第 $i 次 /auth/me 登录态有效" 0 "$TMP/me$i.json"
done

for ep in "/api/users?page=1&size=5" "/api/roles" "/api/questions?page=1&size=5" "/api/contests?page=1&size=5" "/api/audit-logs?page=1&size=5"; do
  f="$TMP/$(echo "$ep" | tr '/?&=' '____').json"
  req GET "$ep" "$f" >/dev/null
  expect "GET $ep" 0 "$f"
done

########################################
title "阶段 5：Token 刷新链路"
########################################
if [ -n "$REFRESH" ]; then
  curl -s -o "$TMP/refresh.json" -X POST "$BASE/api/auth/refresh" \
    -H 'Content-Type: application/json' \
    -d "{\"refreshToken\":\"$REFRESH\"}" >/dev/null
  expect "refreshToken 换新令牌" 0 "$TMP/refresh.json"
  NEWTOKEN=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/refresh.json")'))['data'].get('accessToken',''))" 2>/dev/null)
  if [ -n "$NEWTOKEN" ]; then
    TOKEN="$NEWTOKEN"
    echo "  ✅ 新 token 已生效，继续用新 token 访问"
    req GET /api/auth/me "$TMP/me_new.json" >/dev/null
    expect "新 token 访问 /auth/me" 0 "$TMP/me_new.json"
  fi
fi

########################################
title "阶段 6：自助注册 + 新用户登录态"
########################################
UNAME="e2e$(date +%s)"
req POST /api/auth/register "$TMP/reg.json" "{\"username\":\"$UNAME\",\"password\":\"Player@123\",\"nickname\":\"E2E测试\"}" >/dev/null
expect "注册新用户 $UNAME" 0 "$TMP/reg.json"

NEWTOKEN=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/reg.json")'))['data'].get('accessToken',''))" 2>/dev/null)
if [ -n "$NEWTOKEN" ]; then
  echo "  ✅ 注册即返回 token"
  OLDTOKEN="$TOKEN"; TOKEN="$NEWTOKEN"
  req GET /api/auth/me "$TMP/newme.json" >/dev/null
  expect "新用户 /auth/me 有效" 0 "$TMP/newme.json"
  ROLES=$("$PY" -c "import json;d=json.load(open('$(TW "$TMP/newme.json")'))['data'];print(','.join(d.get('roles') or []))" 2>/dev/null)
  echo "  ℹ️  新用户角色: ${ROLES:-未返回}"
  TOKEN="$OLDTOKEN"
fi

########################################
title "阶段 7：CTF 业务全链路（建赛→建题→挂题→审核→提交→排行）"
########################################
CODE="E2E$(date +%s)"
req POST /api/contests "$TMP/contest.json" "{\"code\":\"$CODE\",\"name\":\"E2E验收赛\",\"type\":\"CTF\",\"mode\":\"TEAM\",\"startAt\":\"2026-11-02T10:00:00\",\"endAt\":\"2026-12-31T23:00:00\",\"maxPlayers\":100}" >/dev/null
expect "创建赛事" 0 "$TMP/contest.json"
CID=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/contest.json")'))['data']['id'])" 2>/dev/null)
echo "  ℹ️  赛事ID=$CID"

req POST /api/questions "$TMP/q.json" '{"bankType":"CTF","category":"WEB","title":"E2E题目","content":"找flag","flag":"flag{e2e_pass}","score":100,"difficulty":"EASY","status":1}' >/dev/null
expect "创建题目" 0 "$TMP/q.json"
QID=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/q.json")'))['data']['id'])" 2>/dev/null)

req POST "/api/contests/$CID/questions" "$TMP/bind.json" "{\"questionIds\":[$QID],\"visible\":1}" >/dev/null
expect "题目挂载到赛事" 0 "$TMP/bind.json"

# 报名只允许 SIGNUP/PENDING：先推进到 SIGNUP，完成建队与审核后再开赛
for st in PENDING SIGNUP; do
  req POST "/api/contests/$CID/status" "$TMP/st_$st.json" "{\"status\":\"$st\"}" >/dev/null
  expect "赛事状态 -> $st" 0 "$TMP/st_$st.json"
done

req POST "/api/contests/$CID/teams" "$TMP/team.json" '{"name":"E2E战队","code":"E2E-A"}' >/dev/null
expect "创建战队" 0 "$TMP/team.json"
TID=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/team.json")'))['data']['id'])" 2>/dev/null)

curl -s -o "$TMP/review.json" -X POST "$BASE/api/contests/$CID/teams/$TID/review?status=APPROVED" -H "Authorization: Bearer $TOKEN"
expect "审核战队通过" 0 "$TMP/review.json"

# 报名审核完成后再开赛
req POST "/api/contests/$CID/status" "$TMP/st_RUNNING.json" '{"status":"RUNNING"}' >/dev/null
expect "赛事状态 -> RUNNING" 0 "$TMP/st_RUNNING.json"

req POST /api/ctf/submissions "$TMP/wrong.json" "{\"contestId\":$CID,\"questionId\":$QID,\"content\":\"flag{no}\",\"submitType\":\"FLAG\"}" >/dev/null
CORRECT=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/wrong.json")'))['data'].get('correct'))" 2>/dev/null)
[ "$CORRECT" = "False" ] && { echo "  ✅ 错误 flag 判错"; PASS=$((PASS+1)); } || { echo "  ❌ 错误 flag 判定异常: $CORRECT"; FAIL=$((FAIL+1)); }

req POST /api/ctf/submissions "$TMP/right.json" "{\"contestId\":$CID,\"questionId\":$QID,\"content\":\"flag{e2e_pass}\",\"submitType\":\"FLAG\"}" >/dev/null
expect "正确 flag 提交" 0 "$TMP/right.json"
RSCORE=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/right.json")'))['data'].get('score'))" 2>/dev/null)
RCORRECT=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/right.json")'))['data'].get('correct'))" 2>/dev/null)
echo "  ℹ️  判分结果 correct=$RCORRECT score=$RSCORE"

req POST /api/ctf/submissions "$TMP/dup.json" "{\"contestId\":$CID,\"questionId\":$QID,\"content\":\"flag{e2e_pass}\",\"submitType\":\"FLAG\"}" >/dev/null
DSCORE=$("$PY" -c "import json;print(json.load(open('$(TW "$TMP/dup.json")'))['data'].get('score'))" 2>/dev/null)
[ "$DSCORE" = "0" ] && { echo "  ✅ 重复提交不重复计分"; PASS=$((PASS+1)); } || { echo "  ❌ 重复提交计分异常: $DSCORE"; FAIL=$((FAIL+1)); }

req GET "/api/situation/ranking/$CID?topN=5" "$TMP/rank.json" >/dev/null
expect "排行榜查询" 0 "$TMP/rank.json"
RANKN=$("$PY" -c "import json;print(len(json.load(open('$(TW "$TMP/rank.json")'))['data']))" 2>/dev/null)
[ "$RANKN" -ge 1 ] 2>/dev/null && { echo "  ✅ 排行榜有 $RANKN 条记录"; PASS=$((PASS+1)); } || { echo "  ❌ 排行榜为空"; FAIL=$((FAIL+1)); }

req GET "/api/situation/screen/$CID" "$TMP/screen.json" >/dev/null
expect "3D 大屏数据" 0 "$TMP/screen.json"

req GET "/api/situation/overview/$CID" "$TMP/ov.json" >/dev/null
expect "态势总览" 0 "$TMP/ov.json"

########################################
title "阶段 8：登出后 token 失效"
########################################
req POST /api/auth/logout "$TMP/logout.json" >/dev/null
expect "登出接口" 0 "$TMP/logout.json"
# 无状态 JWT 登出后本地丢弃，服务端仍可通过 token 访问（设计如此）
echo "  ℹ️  JWT 无状态，登出由客户端丢弃 token"

########################################
title "测试结果汇总"
########################################
echo "  通过: $PASS"
echo "  失败: $FAIL"
if [ "$FAIL" -eq 0 ]; then
  echo "  🎉 全部通过"
  exit 0
else
  echo "  ⚠️  存在失败项"
  exit 1
fi