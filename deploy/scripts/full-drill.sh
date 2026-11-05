#!/usr/bin/env bash
# 网络安全竞赛平台 · 全功能真实业务演练
# 模拟真实用户从建赛到完赛的全过程，逐步抓包打印
# 默认走 Docker 部署入口（csp-frontend nginx :80 反代 /api）；单端口模式传 BASE=http://127.0.0.1:11001
BASE="${BASE:-http://127.0.0.1}"
# Windows Git Bash 里 python3 常是微软商店空壳（执行后无输出），会让 JSON 解析静默失败
PY="$(command -v python || command -v python3)"
TMP=/tmp/drill; rm -rf $TMP; mkdir -p $TMP
PASS=0; FAIL=0; SKIP=0; FAILED_LIST=()

jget(){ "$PY" -c "import sys,json;d=json.load(sys.stdin);
p='$1'.strip('.').split('.')
v=d
try:
  for k in p:
    if k=='':continue
    if k.isdigit(): v=v[int(k)]
    else: v=v[k]
  print(v if v is not None else '')
except Exception: print('')" 2>/dev/null; }

# req METHOD PATH BODYFILE -> 打印 http code + 业务 code
# 用 RT=<token> 前缀临时切换调用方（选手视角必须用选手自己的 token，否则报名关系对不上）
# 先清空 last.json：curl 若在传输前就失败（如 -d 指向不存在的文件），不给断言留旧响应的机会
req(){
  local m=$1 p=$2 bf=$3
  # 请求体既支持文件路径，也支持直接内联的 JSON（内联时落盘，避免 curl -d @ 找不到文件后静默不发请求）
  case "$bf" in
    \{*|\[*) printf '%s' "$bf" > $TMP/inline.json; bf=$TMP/inline.json ;;
    "")      ;;
    *)       [ -f "$bf" ] || { echo "000"; return; } ;;
  esac
  local args=(-s -o $TMP/last.json -w '%{http_code}' -X $m "$BASE$p" -H "Authorization: Bearer ${RT:-$TOKEN}" -H 'Content-Type: application/json')
  [ -n "$bf" ] && args+=(-d @"$bf")
  : > $TMP/last.json
  curl "${args[@]}"
}
login(){ # login USERNAME PASSWORD -> echo accessToken
  printf '{"username":"%s","password":"%s"}' "$1" "$2" > $TMP/lg.json
  curl -s -o $TMP/lgout.json -X POST $BASE/api/auth/login -H 'Content-Type: application/json' -d @$TMP/lg.json
  cat $TMP/lgout.json | jget "data.accessToken"
}
check(){
  local name=$1; shift
  local out=$("$@" 2>/dev/null); local code=$(echo "$out"|tail -1)
  local biz=$(cat $TMP/last.json 2>/dev/null | jget "code")
  local msg=$(cat $TMP/last.json 2>/dev/null | jget "message")
  if [ "$code" = "200" ] && [ "$biz" = "0" ]; then
    echo "  ✅ $name [HTTP $code / code $biz]"; PASS=$((PASS+1))
  else
    echo "  ❌ $name [HTTP $code / code $biz] $msg"; FAIL=$((FAIL+1)); FAILED_LIST+=("$name")
  fi
}
step(){ echo ""; echo "────────────────────────────────────────────"; echo "▶ $1"; echo "────────────────────────────────────────────"; }
expect_fail(){ # 负向断言：业务 code 非 0 才算通过（权限/窗口/参数校验类）
  local name=$1; shift
  local out=$("$@" 2>/dev/null); local code=$(echo "$out"|tail -1)
  local biz=$(cat $TMP/last.json 2>/dev/null | jget "code")
  local msg=$(cat $TMP/last.json 2>/dev/null | jget "message")
  if [ "$biz" != "0" ] && [ -n "$biz" ]; then
    echo "  ✅ $name [HTTP $code / 按预期拒绝 code $biz] $msg"; PASS=$((PASS+1))
  else
    echo "  ❌ $name [HTTP $code / 本应被拒却通过]"; FAIL=$((FAIL+1)); FAILED_LIST+=("$name")
  fi
}
skipif(){ # 依赖服务不可用时的中性跳过，不计入失败
  local name=$1; shift
  echo "  ⏭  $name $*"; SKIP=$((SKIP+1))
}
assert(){ # assert 名称 期望值 实际值 —— 状态机/判分这类要看返回值内容的断言
  if [ "$2" = "$3" ]; then
    echo "  ✅ $1 [=$3]"; PASS=$((PASS+1))
  else
    echo "  ❌ $1 [期望 $2 实际 $3]"; FAIL=$((FAIL+1)); FAILED_LIST+=("$1")
  fi
}

echo "╔══════════════════════════════════════════════════════════╗"
echo "║   网络安全竞赛平台 · 全功能业务演练（真实模拟用户操作）   ║"
echo "╚══════════════════════════════════════════════════════════╝"
SUF=$(date +%s)

step "阶段 0：登录（管理员）"
echo '{"username":"admin","password":"Admin@123"}' > $TMP/login.json
CODE=$(curl -s -o $TMP/last.json -w '%{http_code}' -X POST $BASE/api/auth/login -H 'Content-Type: application/json' -d @$TMP/login.json)
TOKEN=$(cat $TMP/last.json | jget "data.accessToken")
echo "  HTTP=$CODE token_len=${#TOKEN}"

step "阶段 1：用户中心与 RBAC"
check "GET  /auth/me 当前用户"          req GET /api/auth/me
check "GET  /users 用户列表"            req GET "/api/users?page=1&size=10"
check "GET  /roles 角色列表"            req GET "/api/roles?page=1&size=20"
check "GET  /roles/permissions 权限字典" req GET /api/roles/permissions
echo '{"username":"playerA'$SUF'","password":"Player@12345","nickname":"选手A"}' > $TMP/regA.json
check "POST /auth/register 注册选手A"   req POST /api/auth/register $TMP/regA.json
echo '{"username":"playerB'$SUF'","password":"Player@12345","nickname":"选手B"}' > $TMP/regB.json
check "POST /auth/register 注册选手B"   req POST /api/auth/register $TMP/regB.json
echo '{"username":"playerC'$SUF'","password":"Player@12345","nickname":"未报名选手C"}' > $TMP/regC.json
check "POST /auth/register 注册选手C（不报名，用于负向断言）" req POST /api/auth/register $TMP/regC.json

step "阶段 2：题库建设（题目管理）"
cat > $TMP/q1.json <<J
{"bankType":"CTF","category":"WEB","title":"SQL注入入门-$SUF","content":"找到管理后台flag","flag":"flag{sqli_$SUF}","score":100,"difficulty":"EASY","status":1}
J
check "POST /questions 建WEB题"        req POST /api/questions $TMP/q1.json
Q1=$(cat $TMP/last.json | jget "data.id")
cat > $TMP/q2.json <<J
{"bankType":"CTF","category":"MISC","title":"隐写分析-$SUF","content":"图片中藏有flag","flag":"flag{misc_$SUF}","score":200,"difficulty":"MEDIUM","status":1}
J
check "POST /questions 建MISC题"       req POST /api/questions $TMP/q2.json
Q2=$(cat $TMP/last.json | jget "data.id")
cat > $TMP/q3.json <<J
{"bankType":"THEORY","category":"THEORY","title":"等保2.0三道防线-$SUF","content":"简述三道防线","answer":"A","score":50,"difficulty":"EASY","status":1}
J
check "POST /questions 建理论题"       req POST /api/questions $TMP/q3.json
Q3=$(cat $TMP/last.json | jget "data.id")
check "GET  /questions 题库分页"       req GET "/api/questions?page=1&size=10"
check "GET  /questions/{id} 题目详情"  req GET "/api/questions/$Q1"
echo "  📌 Q1=$Q1 Q2=$Q2 Q3=$Q3"

step "阶段 3：创建 CTF 赛事并组卷"
cat > $TMP/c.json <<J
{"code":"CTF$SUF","name":"实战演练CTF赛-$SUF","type":"CTF","mode":"TEAM","startAt":"2026-01-01T00:00:00","endAt":"2027-12-31T23:59:59","maxPlayers":100,"description":"全功能演练"}
J
check "POST /contests 创建CTF赛事"     req POST /api/contests $TMP/c.json
CID=$(cat $TMP/last.json | jget "data.id")
check "GET  /contests/{id} 赛事详情"   req GET /api/contests/$CID
echo "{\"questionIds\":[$Q1,$Q2],\"overwrite\":true}" > $TMP/bind.json
check "POST /contests/{id}/questions 组卷" req POST /api/contests/$CID/questions $TMP/bind.json
check "GET  /contests/{id}/questions 赛题列表" req GET /api/contests/$CID/questions
check "GET  /ctf/contests/{id}/challenges 题目弹窗列表" req GET /api/ctf/contests/$CID/challenges
echo "  📌 CID=$CID"

step "阶段 4：赛事状态机 DRAFT→PENDING→SIGNUP"
for st in PENDING SIGNUP; do
  echo "{\"status\":\"$st\"}" > $TMP/st.json
  check "POST /contests/{id}/status -> $st" req POST /api/contests/$CID/status $TMP/st.json
done

step "阶段 5：选手视角报名（登录→建全局战队→以战队报名）"
TOKEN_A=$(login "playerA$SUF" "Player@12345")
echo "  选手A token_len=${#TOKEN_A}"
echo '{"name":"红队-'$SUF'","maxMembers":5}' > $TMP/gt1.json
RT=$TOKEN_A check "POST /teams 选手A建全局战队" req POST /api/teams $TMP/gt1.json
GTEAM1=$(cat $TMP/last.json | jget "data.id")
echo "{\"mode\":\"TEAM\",\"teamId\":$GTEAM1}" > $TMP/en1.json
RT=$TOKEN_A check "POST /ctf/contests/{id}/teams/enroll 战队报名" \
  req POST /api/ctf/contests/$CID/teams/enroll $TMP/en1.json
ENROLL1=$(cat $TMP/last.json | jget "data.id")

TOKEN_B=$(login "playerB$SUF" "Player@12345")
echo "  选手B token_len=${#TOKEN_B}"
echo '{"name":"蓝队-'$SUF'","maxMembers":5}' > $TMP/gt2.json
RT=$TOKEN_B check "POST /teams 选手B建全局战队" req POST /api/teams $TMP/gt2.json
GTEAM2=$(cat $TMP/last.json | jget "data.id")
echo "{\"mode\":\"TEAM\",\"teamId\":$GTEAM2}" > $TMP/en2.json
RT=$TOKEN_B check "POST /ctf/contests/{id}/teams/enroll 战队报名" \
  req POST /api/ctf/contests/$CID/teams/enroll $TMP/en2.json
ENROLL2=$(cat $TMP/last.json | jget "data.id")

check "GET  /contests/{id}/teams 参赛记录列表" req GET "/api/contests/$CID/teams"
check "GET  /teams 我的全局战队"       req GET "/api/teams"
RT=$TOKEN_A check "GET  /ctf/contests/{id}/teams/options 报名可选项" req GET /api/ctf/contests/$CID/teams/options
RT=$TOKEN_A check "GET  /ctf/contests/{id}/teams/mine 我的参赛记录" req GET /api/ctf/contests/$CID/teams/mine
RT=$TOKEN_A expect_fail "报名窗口内重复报名应被拒" req POST /api/ctf/contests/$CID/teams/enroll $TMP/en1.json
echo "  📌 GTEAM1=$GTEAM1 GTEAM2=$GTEAM2 参赛记录 ENROLL1=$ENROLL1 ENROLL2=$ENROLL2"

step "阶段 6：报名审核（管理员）"
check "POST /ctf/contests/{id}/teams/{tid}/review 审核红队" req POST "/api/ctf/contests/$CID/teams/$ENROLL1/review?status=APPROVED"
check "POST /ctf/contests/{id}/teams/{tid}/review 审核蓝队" req POST "/api/ctf/contests/$CID/teams/$ENROLL2/review?status=APPROVED"
check "GET  /ctf/contests/{id}/teams/{tid}/members 参赛成员" req GET /api/ctf/contests/$CID/teams/$ENROLL1/members

step "阶段 7：开赛 RUNNING"
echo '{"status":"RUNNING"}' > $TMP/run.json
check "POST /contests/{id}/status -> RUNNING" req POST /api/contests/$CID/status $TMP/run.json

step "阶段 8：做题（选手视角真实提交 flag，含错误/重复/未报名）"
TOKEN_C=$(login "playerC$SUF" "Player@12345")
cat > $TMP/sub_wrong.json <<J
{"contestId":$CID,"questionId":$Q1,"content":"flag{wrong_guess}","submitType":"FLAG","deviceId":"drill-A-$SUF","startTs":$(( $(date +%s%3N) - 45000 ))}
J
RT=$TOKEN_A check "POST /ctf/submissions 选手A提交错误flag" req POST /api/ctf/submissions $TMP/sub_wrong.json
RT=$TOKEN_C expect_fail "POST /ctf/submissions 未报名选手答题应被拒" req POST /api/ctf/submissions $TMP/sub_wrong.json
cat > $TMP/sub_ok1.json <<J
{"contestId":$CID,"questionId":$Q1,"content":"flag{sqli_$SUF}","submitType":"FLAG","deviceId":"drill-A-$SUF","startTs":$(( $(date +%s%3N) - 60000 ))}
J
RT=$TOKEN_A check "POST /ctf/submissions 选手A提交正确flag(Q1)" req POST /api/ctf/submissions $TMP/sub_ok1.json
echo "  判分结果: $(cat $TMP/last.json | jget 'data.score') 分 / 首杀: $(cat $TMP/last.json | jget 'data.firstBlood')"
cat > $TMP/sub_ok2.json <<J
{"contestId":$CID,"questionId":$Q2,"content":"flag{misc_$SUF}","submitType":"FLAG","deviceId":"drill-A-$SUF","startTs":$(( $(date +%s%3N) - 120000 ))}
J
RT=$TOKEN_A check "POST /ctf/submissions 选手A提交正确flag(Q2)" req POST /api/ctf/submissions $TMP/sub_ok2.json
RT=$TOKEN_B check "POST /ctf/submissions 选手B同题首解（非首杀）" req POST /api/ctf/submissions $TMP/sub_ok1.json
RT=$TOKEN_A check "POST /ctf/submissions 重复提交(200 但不加分)" req POST /api/ctf/submissions $TMP/sub_ok2.json
echo "  重复提交得分: $(cat $TMP/last.json | jget 'data.score')"

step "阶段 9：排行榜与提交记录"
check "GET  /ctf/contests/{id}/scoreboard 排行榜" req GET "/api/ctf/contests/$CID/scoreboard"
RT=$TOKEN_A check "GET  /ctf/contests/{id}/scoreboard/mine 我的排名" req GET /api/ctf/contests/$CID/scoreboard/mine
RT=$TOKEN_A check "GET  /ctf/contests/{id}/solved 已解题目" req GET /api/ctf/contests/$CID/solved
RT=$TOKEN_A check "GET  /ctf/contests/{id}/submissions 提交记录" req GET "/api/ctf/contests/$CID/submissions?page=1&size=10"
check "POST /scoreboard/refresh 刷新排行" req POST /api/ctf/contests/$CID/scoreboard/refresh

step "阶段 10：态势感知大屏（比赛进行中的实时数据）"
check "GET  /situation/overview 总览" req GET "/api/situation/overview?contestId=$CID"
check "GET  /situation/ranking 排行" req GET "/api/situation/ranking?contestId=$CID&limit=10"
check "GET  /situation/screen/{id} 大屏" req GET "/api/situation/screen/$CID"
check "GET  /situation/3d 3D大屏" req GET "/api/situation/3d?contestId=$CID"
check "GET  /situation/monitor 靶机监控" req GET "/api/situation/monitor?contestId=$CID"
check "GET  /situation/team/{cid}/{tid} 战队战况" req GET "/api/situation/team/$CID/$ENROLL1"
check "GET  /teams/{id}/profile 战队主页" req GET "/api/teams/$GTEAM1/profile"

step "阶段 11：通知与公告"
echo "{\"title\":\"演练通知-$SUF\",\"content\":\"比赛进行中\",\"level\":\"INFO\"}" > $TMP/n.json
check "POST /notify/send 发送通知" req POST /api/notify/send $TMP/n.json
check "GET  /notify/my 我的通知" req GET "/api/notify/my?page=1&size=10"
check "GET  /notify/unread-count 未读数" req GET /api/notify/unread-count
echo "{\"title\":\"平台公告-$SUF\",\"content\":\"演练公告\",\"type\":\"NOTICE\",\"status\":1}" > $TMP/a.json
check "POST /announcements 发公告" req POST /api/announcements $TMP/a.json
check "GET  /announcements/latest 最新公告" req GET /api/announcements/latest

step "阶段 12：理论赛（独立赛事：个人赛模式）"
cat > $TMP/ct.json <<J
{"code":"TH$SUF","name":"演练理论赛-$SUF","type":"THEORY","mode":"INDIVIDUAL","startAt":"2026-01-01T00:00:00","endAt":"2027-12-31T23:59:59","maxPlayers":100}
J
check "POST /contests 创建理论赛" req POST /api/contests $TMP/ct.json
TCID=$(cat $TMP/last.json | jget "data.id")
echo "{\"questionIds\":[$Q3],\"overwrite\":true}" > $TMP/tbind.json
check "POST 理论赛组卷" req POST /api/contests/$TCID/questions $TMP/tbind.json
for st in PENDING SIGNUP; do
  echo "{\"status\":\"$st\"}" > $TMP/tst.json
  check "理论赛状态 -> $st" req POST /api/contests/$TCID/status $TMP/tst.json
done
RT=$TOKEN_B check "理论赛个人报名" req POST /api/ctf/contests/$TCID/teams/enroll '{"mode":"INDIVIDUAL"}'
echo '{"status":"RUNNING"}' > $TMP/trun.json
check "理论赛状态 -> RUNNING" req POST /api/contests/$TCID/status $TMP/trun.json
RT=$TOKEN_B check "GET  /ctf/contests/{id}/theory/paper 理论试卷" req GET /api/ctf/contests/$TCID/theory/paper
printf '[{"questionId":%s,"answer":"A","deviceId":"drill-B-%s"}]' "$Q3" "$SUF" > $TMP/thsub.json
RT=$TOKEN_B check "POST /ctf/contests/{id}/theory/submit 交卷判分" req POST /api/ctf/contests/$TCID/theory/submit $TMP/thsub.json
echo "  交卷结果: $(cat $TMP/last.json | jget 'data.correctCount')/$(cat $TMP/last.json | jget 'data.totalCount') 得分=$(cat $TMP/last.json | jget 'data.gainedScore') 明细=$(cat $TMP/last.json | jget 'data.details')"
assert "理论题标准答案 A 判为正确" "1" "$(cat $TMP/last.json | jget 'data.correctCount')"
RT=$TOKEN_C check "POST 未报名选手整卷交卷（接口不报错，但按题跳过）" req POST /api/ctf/contests/$TCID/theory/submit $TMP/thsub.json
assert "未报名选手交卷 correctCount 为 0" "0" "$(cat $TMP/last.json | jget 'data.correctCount')"

step "阶段 13：审计与归档"
check "GET  /audit-logs 审计日志" req GET "/api/audit-logs?page=1&size=10"
echo '{"status":"FINISHED"}' > $TMP/fin.json
check "POST /contests/{id}/status -> FINISHED" req POST /api/contests/$CID/status $TMP/fin.json
echo "{\"filingNo\":\"FILE-$SUF\",\"filingOrg\":\"演练主办方\",\"remark\":\"全功能演练归档\"}" > $TMP/ar.json
check "POST /archives/contest/{id} 赛事归档（带备案编号）" req POST /api/archives/contest/$CID $TMP/ar.json
check "GET  /archives/contest/{id} 归档查询" req GET /api/archives/contest/$CID

step "阶段 14：AWD 攻防赛（独立赛制）"
cat > $TMP/cawd.json <<J
{"code":"AWD$SUF","name":"演练AWD赛-$SUF","type":"AWD","mode":"TEAM","startAt":"2026-01-01T00:00:00","endAt":"2027-12-31T23:59:59","maxPlayers":100}
J
check "POST /contests 创建AWD赛事" req POST /api/contests $TMP/cawd.json
ACID=$(cat $TMP/last.json | jget "data.id")
echo "  📌 ACID=$ACID"
# 报名只允许在 SIGNUP 窗口内，且必须走「全局战队 → 赛事报名」；此前脚本在 RUNNING 后建队，必然 409
for st in PENDING SIGNUP; do
  echo "{\"status\":\"$st\"}" > $TMP/st2.json
  check "AWD赛事状态 -> $st" req POST /api/contests/$ACID/status $TMP/st2.json
done
echo '{"name":"攻防红队-'$SUF'","maxMembers":5}' > $TMP/ag1.json
RT=$TOKEN_A check "POST /teams AWD红队全局战队" req POST /api/teams $TMP/ag1.json
AGTEAM1=$(cat $TMP/last.json | jget "data.id")
echo "{\"mode\":\"TEAM\",\"teamId\":$AGTEAM1}" > $TMP/age1.json
RT=$TOKEN_A check "AWD 红队报名" req POST /api/ctf/contests/$ACID/teams/enroll $TMP/age1.json
ATEAM1=$(cat $TMP/last.json | jget "data.id")
echo '{"name":"攻防蓝队-'$SUF'","maxMembers":5}' > $TMP/ag2.json
RT=$TOKEN_B check "POST /teams AWD蓝队全局战队" req POST /api/teams $TMP/ag2.json
AGTEAM2=$(cat $TMP/last.json | jget "data.id")
echo "{\"mode\":\"TEAM\",\"teamId\":$AGTEAM2}" > $TMP/age2.json
RT=$TOKEN_B check "AWD 蓝队报名" req POST /api/ctf/contests/$ACID/teams/enroll $TMP/age2.json
ATEAM2=$(cat $TMP/last.json | jget "data.id")
echo "  📌 AWD 参赛记录 ATEAM1=$ATEAM1 ATEAM2=$ATEAM2"
cat > $TMP/atk.json <<J
{"contestId":$ACID,"attackerTeamId":$ATEAM1,"victimTeamId":$ATEAM2,"questionId":$Q1,"flag":"flag{fake_attack}"}
J
cat > $TMP/atk_self.json <<J
{"contestId":$ACID,"attackerTeamId":$ATEAM1,"victimTeamId":$ATEAM1,"questionId":$Q1,"flag":"flag{fake_attack}"}
J
echo '{"status":"RUNNING"}' > $TMP/st2.json
check "AWD赛事状态 -> RUNNING" req POST /api/contests/$ACID/status $TMP/st2.json

cat > $TMP/round.json <<J
{"contestId":$ACID,"roundNo":1,"durationS":600,"mode":"AWD","provisionTargets":false}
J
check "POST /awd/rounds 创建第1轮" req POST /api/awd/rounds $TMP/round.json
check "GET  /awd/rounds 轮次列表" req GET "/api/awd/rounds?contestId=$ACID"
check "POST /awd/score/register 登记红队积分" req POST "/api/awd/score/register?contestId=$ACID&teamId=$ATEAM1"
check "POST /awd/score/register 登记蓝队积分" req POST "/api/awd/score/register?contestId=$ACID&teamId=$ATEAM2"
check "GET  /awd/rounds/{cid}/current 当前轮（未开局应为空）" req GET /api/awd/rounds/$ACID/current
expect_fail "未开局提交攻击应被拒" req POST /api/awd/score/attack $TMP/atk.json
check "POST /awd/rounds/{cid}/{no}/start 开局 RUNNING" req POST /api/awd/rounds/$ACID/1/start
check "GET  /awd/rounds/{cid}/{no}/running 攻击窗口态" req GET /api/awd/rounds/$ACID/1/running
check "GET  /awd/rounds/{cid}/{no} 轮次详情" req GET /api/awd/rounds/$ACID/1
check "GET  /awd/score/rules 计分规则" req GET /api/awd/score/rules
check "GET  /awd/score/team 队伍积分" req GET "/api/awd/score/team?contestId=$ACID&teamId=$ATEAM1"
check "GET  /awd/score/leaderboard 攻防排行" req GET "/api/awd/score/leaderboard?contestId=$ACID&topN=10"
check "POST /awd/score/attack 攻击提交（错误flag判负但接口通）" req POST /api/awd/score/attack $TMP/atk.json
expect_fail "攻击自己的靶机应被拒" req POST "/api/awd/score/attack" $TMP/atk_self.json
# AWDP 防御修补：AwdTeamResolver 按 contest_team_member 解析当前登录用户所属战队，
# 管理员 token 解析不出战队会被判「缺少战队ID」，所以必须切到红队选手自己的 token
cat > $TMP/patch.json <<J
{"contestId":$ACID,"roundNo":1,"questionId":$Q1,"description":"修复 SQL 注入：拼接查询改为预编译参数","verifyUrl":"http://127.0.0.1:20000/healthz"}
J
RT=$TOKEN_A check "POST /awd/score/patch 红队提交修补说明" req POST /api/awd/score/patch $TMP/patch.json
PGAIN=$(cat $TMP/last.json | jget "data.gainedScore")
req GET /api/awd/score/rules >/dev/null; PB=$(jget "data.patchBonus" < $TMP/last.json)
assert "修补加分 = 计分规则 patchBonus" "$PB" "$PGAIN"
req GET "/api/awd/score/team?contestId=$ACID&teamId=$ATEAM1" >/dev/null
assert "修补加分计入红队防御分" "$PB" "$(jget 'data.defenseScore' < $TMP/last.json)"
RT=$TOKEN_A expect_fail "同轮同题重复修补不重复加分" req POST /api/awd/score/patch $TMP/patch.json
RT=$TOKEN
check "GET  /awd/rounds/{cid}/{no}/next-states 状态流转（RUNNING）" req GET /api/awd/rounds/$ACID/1/next-states
check "POST /awd/rounds/{cid}/{no}/check 收卷 CHECKING" req POST /api/awd/rounds/$ACID/1/check
RST=$(req GET /api/awd/rounds/$ACID/1 >/dev/null; jget "data.status" < $TMP/last.json)
echo "  📌 收卷后轮次状态=$RST"
# 调度器（AwdRoundScheduler）会在 check 完成后自动 CHECKING→SETTLING，因此结算入口按实际状态选
if [ "$RST" = "CHECKING" ]; then
  check "POST /awd/rounds/{cid}/{no}/settle 裁判结算入终态" req POST /api/awd/rounds/$ACID/1/settle
elif [ "$RST" = "SETTLING" ]; then
  check "POST /awd/rounds/tick 由 SETTLING 收敛到 FINISHED" req POST /api/awd/rounds/tick
else
  skipif "轮次已处于 $RST，无需再结算"
fi
RST=$(req GET /api/awd/rounds/$ACID/1 >/dev/null; jget "data.status" < $TMP/last.json)
assert "轮次终态为 FINISHED" "FINISHED" "$RST"
expect_fail "终态轮次不可重复结算" req POST /api/awd/rounds/$ACID/1/settle
check "GET  /awd/rounds/{cid}/{no}/next-states 状态流转（终态）" req GET /api/awd/rounds/$ACID/1/next-states
check "GET  /awd/contests/{cid}/overview 攻防总览" req GET /api/awd/contests/$ACID/overview
check "GET  /awd/contests/{cid}/teams 攻防战队" req GET /api/awd/contests/$ACID/teams
check "GET  /awd/contests/{cid}/scoreboard 攻防计分板" req GET /api/awd/contests/$ACID/scoreboard
check "GET  /awd/contests/{cid}/rounds 轮次（兼容接口）" req GET /api/awd/contests/$ACID/rounds
check "GET  /awd/contests/{cid}/history 攻防历史" req GET /api/awd/contests/$ACID/history

step "阶段 15：靶场服务（11002 独立靶场，未部署则整段跳过）"
AB=http://127.0.0.1:11002
APC=$(curl -s -o /dev/null -m 3 -w '%{http_code}' "$AB/api/templates?page=1&size=1" 2>/dev/null)
if [ "$APC" = "000" ] || [ -z "$APC" ]; then
  skipif "靶场服务 11002 不可达（HTTP $APC），本段 6 项检查跳过"
else
  printf "  %-32s " "GET /templates 模板"
  curl -s -o $TMP/at.json -w "HTTP=%{http_code} " "$AB/api/templates?page=1&size=10"; echo "code=$(cat $TMP/at.json|jget code)"
  TID=$(cat $TMP/at.json | jget "data.records.0.id")
  printf "  %-32s " "POST /instances 创建靶机"
  curl -s -o $TMP/ai.json -w "HTTP=%{http_code} " -X POST $AB/api/instances -H 'Content-Type: application/json' -d "{\"templateId\":$TID,\"ownerId\":1,\"ownerName\":\"admin\"}"; echo "code=$(cat $TMP/ai.json|jget code)"
  IID=$(cat $TMP/ai.json|jget data.id); AURL=$(cat $TMP/ai.json|jget data.accessUrl)
  echo "  📌 靶机 IID=$IID URL=$AURL"
  if [ -n "$AURL" ]; then printf "  %-32s " "访问靶机首页"; curl -s -o /dev/null -w "HTTP=%{http_code}\n" "$AURL/"; fi
  printf "  %-32s " "GET /instances 实例列表"
  curl -s -o $TMP/al.json -w "HTTP=%{http_code} " "$AB/api/instances?page=1&size=10"; echo "code=$(cat $TMP/al.json|jget code)"
  printf "  %-32s " "POST /instances/{id}/reset 重置"
  curl -s -o /dev/null -w "HTTP=%{http_code}\n" -X POST "$AB/api/instances/$IID/reset"
  printf "  %-32s " "DELETE /instances/{id} 销毁"
  curl -s -o /dev/null -w "HTTP=%{http_code}\n" -X DELETE "$AB/api/instances/$IID"
fi

step "阶段 16：统计与收尾"
check "GET  /stats/overview 平台统计" req GET /api/stats/overview
check "GET  /situation/overview 全站态势" req GET /api/situation/overview

echo ""
echo "════════════════════════════════════════════════════════════"
echo "  演练汇总：通过 $PASS / 失败 $FAIL / 跳过 $SKIP"
if [ $FAIL -gt 0 ]; then echo "  失败项："; for f in "${FAILED_LIST[@]}"; do echo "    - $f"; done; fi
echo "════════════════════════════════════════════════════════════"
