#!/usr/bin/env bash
# 全量接口发包/收包验证：模拟浏览器点击，逐一请求并校验响应
BASE="${BASE:-http://localhost:11001}"
TMP=$(mktemp -d)
PASS=0; FAIL=0; SKIP=0
SUF=$(date +%s)
declare -a FAILURES

log()  { printf '%s\n' "$*"; }
ok()   { PASS=$((PASS+1)); printf '  ✅ %s\n' "$1"; }
bad()  { FAIL=$((FAIL+1)); FAILURES+=("$1"); printf '  ❌ %s\n' "$1"; }
skip() { SKIP=$((SKIP+1)); printf '  ⏭  %s\n' "$1"; }

# req METHOD PATH [BODY] -> 输出响应体到 $TMP/last.json，返回 http code
req() {
  local method="$1" path="$2" body="${3:-}"
  local args=(-s -o "$TMP/last.json" -w '%{http_code}' -X "$method" "$BASE$path")
  [ -n "$TOKEN" ] && args+=(-H "Authorization: Bearer $TOKEN")
  [ -n "$body" ] && args+=(-H 'Content-Type: application/json' -d "$body")
  curl "${args[@]}"
}

# check NAME METHOD PATH [BODY] [EXPECT_CODE]
check() {
  local name="$1" method="$2" path="$3" body="${4:-}" want="${5:-0}"
  local code; code=$(req "$method" "$path" "$body")
  local rcode; rcode=$(python3 -c "import json;print(json.load(open('$TMP/last.json')).get('code','-'))" 2>/dev/null || echo "-")
  if [ "$rcode" = "$want" ]; then
    ok "$name [HTTP $code / code $rcode]"
  else
    local msg; msg=$(python3 -c "import json;d=json.load(open('$TMP/last.json'));print(d.get('message','')[:60])" 2>/dev/null || echo "非JSON")
    bad "$name [HTTP $code / code $rcode] $msg"
  fi
}

jget() { python3 -c "import json,sys;d=json.load(open('$TMP/last.json'));print(eval('d'+sys.argv[1]))" "$1" 2>/dev/null; }

log "════════════════════════════════════════════════════════════════"
log "  全量接口发包/收包验证  BASE=$BASE"
log "════════════════════════════════════════════════════════════════"

# ── 阶段 1：认证 ────────────────────────────────────────────────
log ""
log "▶ 阶段 1：认证与用户中心（浏览器登录流程）"
TOKEN=""
check "POST /auth/register 注册"  POST /api/auth/register "{\"username\":\"ft$SUF\",\"password\":\"Full@12345\",\"nickname\":\"全量测试\"}" 0
NEWTOKEN=$(jget "['data']['accessToken']")
[ -n "$NEWTOKEN" ] && ok "注册返回 token" || bad "注册未返回 token"
check "POST /auth/login 登录"    POST /api/auth/login    '{"username":"admin","password":"Admin@123"}' 0
TOKEN=$(jget "['data']['accessToken']")
REFRESH=$(jget "['data']['refreshToken']")
[ -n "$TOKEN" ] && ok "登录返回 accessToken" || bad "登录未返回 token"
check "GET  /auth/me 当前用户"   GET  /api/auth/me 2>/dev/null
check "POST /auth/refresh 刷新"  POST /api/auth/refresh "{\"refreshToken\":\"$REFRESH\"}" 0
# 改密用新注册用户，避免污染 admin 凭据
# 改密：用 admin 自身验证接口可用（旧密码正确时成功改回）
_save=$TOKEN
check "POST /auth/password 改密" POST /api/auth/password '{"oldPassword":"Admin@123","newPassword":"Admin@999"}' 0
# 注：改密后旧 token 失效，admin 密码由测试脚本末尾的 DB 复位保障
TOKEN=$_save
check "POST /auth/logout 登出"   POST /api/auth/logout 2>/dev/null

# ── 阶段 2：RBAC ────────────────────────────────────────────────
log ""
log "▶ 阶段 2：RBAC 角色与权限"
check "GET  /roles 角色列表"          GET  /api/roles 2>/dev/null
check "GET  /roles/1 角色详情"        GET  /api/roles/1 2>/dev/null
check "GET  /roles/permissions 权限树" GET /api/roles/permissions 2>/dev/null
check "POST /roles 创建角色"          POST /api/roles "{\"code\":\"T$SUF\",\"name\":\"测试角色\",\"description\":\"e2e\"}" 0
NEWROLE=$(jget "['data']")
check "PUT  /roles/$NEWROLE 更新角色"  PUT  "/api/roles/$NEWROLE" "{\"code\":\"T$SUF\",\"name\":\"测试角色2\"}" 0
check "PUT  /roles/$NEWROLE/permissions 授权" PUT "/api/roles/$NEWROLE/permissions" '{"permissions":["contest:view"]}' 0

# ── 阶段 3：用户管理 ────────────────────────────────────────────
log ""
log "▶ 阶段 3：用户管理"
check "GET  /users 分页"        GET  "/api/users?page=1&size=5" 2>/dev/null
check "GET  /users/{admin} 详情"  GET  /api/users/1000000000000000001 2>/dev/null
check "GET  /users/{admin}/roles 角色" GET /api/users/1000000000000000001/roles 2>/dev/null
check "POST /users 建用户"      POST /api/users "{\"username\":\"ue$SUF\",\"password\":\"User@12345\",\"nickname\":\"E2E用户\"}" 0
UID2=$(jget "['data']")
check "PUT  /users/$UID2 更新"   PUT  "/api/users/$UID2" "{\"username\":\"ue$SUF\",\"nickname\":\"E2E改\"}" 0
check "PUT  /users/$UID2/status 启停" PUT "/api/users/$UID2/status" '{"status":1}' 0
check "POST /users/$UID2/password/reset 重置" POST "/api/users/$UID2/password/reset" '{"newPassword":"Reset@12345"}' 0
check "DELETE /users/$UID2 删除" DELETE "/api/users/$UID2" 2>/dev/null

# ── 阶段 4：题库 ────────────────────────────────────────────────
log ""
log "▶ 阶段 4：题库管理"
check "GET  /questions 分页"    GET "/api/questions?page=1&size=5" 2>/dev/null
check "POST /questions 建题"    POST /api/questions '{"bankType":"CTF","category":"WEB","title":"全量题","content":"找flag","flag":"flag{full_test}","score":200,"difficulty":"MEDIUM","status":1}' 0
QID=$(jget "['data']['id']")
check "GET  /questions/$QID 详情" GET "/api/questions/$QID" 2>/dev/null
check "PUT  /questions/$QID 更新" PUT "/api/questions/$QID" '{"bankType":"CTF","category":"WEB","title":"全量题改","content":"找flag2","flag":"flag{full_test}","score":200,"difficulty":"MEDIUM","status":1}' 0
check "POST /questions/import 批量导入(空)" POST /api/questions/import "[]" 0

# ── 阶段 5：赛事 ────────────────────────────────────────────────
log ""
log "▶ 阶段 5：赛事管理与状态机"
check "GET  /contests 分页"      GET "/api/contests?page=1&size=5" 2>/dev/null
check "POST /contests 建赛"      POST /api/contests "{\"code\":\"FULL$(date +%s)\",\"name\":\"全量验证赛\",\"type\":\"CTF\",\"mode\":\"TEAM\",\"startAt\":\"2026-01-01T00:00:00\",\"endAt\":\"2027-12-31T23:59:59\",\"maxPlayers\":100}" 0
CID=$(jget "['data']['id']")
check "GET  /contests/$CID 详情"  GET "/api/contests/$CID" 2>/dev/null
check "PUT  /contests/$CID 更新"  PUT "/api/contests/$CID" "{\"code\":\"FX$SUF\",\"name\":\"全量验证赛改\",\"type\":\"CTF\",\"mode\":\"TEAM\",\"startAt\":\"2026-01-01T00:00:00\",\"endAt\":\"2027-12-31T23:59:59\",\"maxPlayers\":100}" 0
check "POST /contests/$CID/status -> PENDING" POST "/api/contests/$CID/status" '{"status":"PENDING"}' 0
check "POST /contests/$CID/status -> SIGNUP"  POST "/api/contests/$CID/status" '{"status":"SIGNUP"}' 0
check "POST /contests/$CID/status 非法流转应拒(预期409)" POST "/api/contests/$CID/status" '{"status":"DRAFT"}' 409
check "POST /contests/$CID/questions 挂题" POST "/api/contests/$CID/questions" "{\"questionIds\":[$QID],\"visible\":1}" 0
check "GET  /contests/$CID/questions 题目列表" GET "/api/contests/$CID/questions" 2>/dev/null

# ── 阶段 6：战队与报名 ──────────────────────────────────────────
log ""
log "▶ 阶段 6：战队与报名审核"
check "POST /contests/$CID/teams 建队" POST "/api/contests/$CID/teams" '{"name":"全量战队","code":"FULL-T"}' 0
TID=$(jget "['data']['id']")
check "GET  /contests/$CID/teams 列表" GET "/api/contests/$CID/teams" 2>/dev/null
check "GET  /contests/$CID/teams/$TID 详情" GET "/api/contests/$CID/teams/$TID" 2>/dev/null
check "GET  /contests/$CID/teams/$TID/members 成员" GET "/api/contests/$CID/teams/$TID/members" 2>/dev/null
check "POST /contests/$CID/teams/$TID/review 审核通过" POST "/api/contests/$CID/teams/$TID/review?status=APPROVED" 2>/dev/null
check "POST /contests/$CID/status -> RUNNING" POST "/api/contests/$CID/status" '{"status":"RUNNING"}' 0

# ── 阶段 7：CTF 答题与排行 ──────────────────────────────────────
log ""
log "▶ 阶段 7：CTF 答题、判分、排行"
check "POST /ctf/submissions 错误flag" POST /api/ctf/submissions "{\"contestId\":$CID,\"questionId\":$QID,\"content\":\"flag{no}\",\"submitType\":\"FLAG\"}" 0
check "POST /ctf/submissions 正确flag" POST /api/ctf/submissions "{\"contestId\":$CID,\"questionId\":$QID,\"content\":\"flag{full_test}\",\"submitType\":\"FLAG\"}" 0
SCORE=$(jget "['data']['score']")
[ -n "$SCORE" ] && [ "$SCORE" != "0" ] && ok "判分得分=$SCORE" || bad "判分得分异常=$SCORE"
check "GET  /ctf/contests/$CID/solved 已解" GET "/api/ctf/contests/$CID/solved" 2>/dev/null
check "GET  /ctf/contests/$CID/submissions 提交记录" GET "/api/ctf/contests/$CID/submissions" 2>/dev/null
check "GET  /ctf/contests/$CID/scoreboard 排行" GET "/api/ctf/contests/$CID/scoreboard" 2>/dev/null
check "GET  /ctf/contests/$CID/scoreboard/mine 我的排名" GET "/api/ctf/contests/$CID/scoreboard/mine" 2>/dev/null
check "POST /ctf/contests/$CID/scoreboard/refresh 刷新榜" POST "/api/ctf/contests/$CID/scoreboard/refresh" 2>/dev/null

# ── 阶段 8：理论赛 ──────────────────────────────────────────────
log ""
log "▶ 阶段 8：理论赛"
skip "GET  /theory/paper（当前赛事非 THEORY，预期 4xx）"
check "GET  /ctf/contests/$CID/theory/paper 非理论赛应拒(预期400)" GET "/api/ctf/contests/$CID/theory/paper" "" 400

# ── 阶段 9：态势感知 ────────────────────────────────────────────
log ""
log "▶ 阶段 9：态势感知（大屏）"
check "GET /situation/overview/$CID 总览" GET "/api/situation/overview/$CID" 2>/dev/null
check "GET /situation/ranking/$CID 排行"  GET "/api/situation/ranking/$CID?topN=5" 2>/dev/null
check "GET /situation/screen/$CID 大屏"   GET "/api/situation/screen/$CID?topN=5" 2>/dev/null
check "GET /situation/team/$CID/$TID 战队主页" GET "/api/situation/team/$CID/$TID" 2>/dev/null
check "GET /situation/targets/$CID 靶机"  GET "/api/situation/targets/$CID" 2>/dev/null

# ── 阶段 10：AWD ────────────────────────────────────────────────
log ""
log "▶ 阶段 10：AWD 轮次与靶机"
check "GET  /awd/rounds 轮次列表" GET "/api/awd/rounds?contestId=$CID" 2>/dev/null
check "POST /awd/rounds 建轮次"   POST /api/awd/rounds "{\"contestId\":$CID,\"roundNo\":1,\"durationS\":600}" 0
check "GET  /awd/rounds/$CID/current 当前轮" GET "/api/awd/rounds/$CID/current" 2>/dev/null
check "GET  /awd/rounds/$CID/1/next-states 可达状态" GET "/api/awd/rounds/$CID/1/next-states" 2>/dev/null
check "POST /awd/rounds/$CID/1/start 开轮" POST "/api/awd/rounds/$CID/1/start" 2>/dev/null
check "GET  /awd/rounds/$CID/1 轮次详情" GET "/api/awd/rounds/$CID/1" 2>/dev/null
check "GET  /awd/targets 靶机列表" GET "/api/awd/targets?contestId=$CID" 2>/dev/null
check "GET  /awd/targets/mine 我的靶机" GET "/api/awd/targets/mine?contestId=$CID" 2>/dev/null
check "GET  /awd/score/leaderboard 攻防榜" GET "/api/awd/score/leaderboard?contestId=$CID" 2>/dev/null
check "GET  /awd/score/rules 计分规则" GET "/api/awd/score/rules" 2>/dev/null
check "GET  /awd/score/team 队伍得分" GET "/api/awd/score/team?contestId=$CID&teamId=$TID" 2>/dev/null

# ── 阶段 11：审计/通知/归档 ─────────────────────────────────────
log ""
log "▶ 阶段 11：审计、通知、归档"
check "GET  /audit-logs 审计分页" GET "/api/audit-logs?page=1&size=5" 2>/dev/null
check "GET  /notify/my 我的通知"  GET "/api/notify/my" 2>/dev/null
check "GET  /notify/unread-count 未读数" GET "/api/notify/unread-count" 2>/dev/null
check "POST /notify/read-all 全部已读" POST "/api/notify/read-all" 2>/dev/null
check "POST /notify/send 发通知"  POST /api/notify/send '{"title":"全量测试","content":"测试通知","targetType":"ALL"}' 0
check "GET  /archives 归档分页"   GET "/api/archives?page=1&size=5" 2>/dev/null
check "POST /contests/$CID/status -> FINISHED" POST "/api/contests/$CID/status" '{"status":"FINISHED"}' 0
check "POST /archives/contest/$CID 归档赛事" POST "/api/archives/contest/$CID" '{"filingNo":"E2E-001","filingOrg":"测试","remark":"e2e"}' 0

# ── 阶段 12：清理 ───────────────────────────────────────────────
log ""
log "▶ 阶段 12：清理与越权校验"
check "DELETE /roles/$NEWROLE 删角色" DELETE "/api/roles/$NEWROLE" 2>/dev/null
check "DELETE /questions/$QID 删题" DELETE "/api/questions/$QID" 2>/dev/null
NOAUTH=$(curl -s "$BASE/api/users?page=1&size=1" | python3 -c "import json,sys;print(json.load(sys.stdin).get('code'))" 2>/dev/null)
[ "$NOAUTH" = "401" ] && ok "未带 token 访问受保护接口 -> code 401" || bad "未带 token 应为 code 401，实际 $NOAUTH"

# 复位 admin 密码，保证人工验收可登录
NEWHASH=$(python3 -c "import bcrypt;print(bcrypt.hashpw(b'Admin@123', bcrypt.gensalt(rounds=10)).decode())" 2>/dev/null)
if [ -n "$NEWHASH" ]; then
  docker exec csp-mysql mysql -uroot -pcsp_root_2026 -D csp -e "UPDATE sys_user SET password='$NEWHASH' WHERE username='admin';" 2>/dev/null
  log "  🔑 admin 密码已复位为 Admin@123"
fi

log ""
log "════════════════════════════════════════════════════════════════"
log "  汇总：通过 $PASS / 失败 $FAIL / 跳过 $SKIP"
if [ "$FAIL" -gt 0 ]; then
  log "  失败项："
  for f in "${FAILURES[@]}"; do log "    - $f"; done
fi
log "════════════════════════════════════════════════════════════════"
rm -rf "$TMP"
