#!/usr/bin/env bash
# 网络安全竞赛平台 · 全功能真实业务演练
# 模拟真实用户从建赛到完赛的全过程，逐步抓包打印
BASE=http://127.0.0.1:11001
TMP=/tmp/drill; rm -rf $TMP; mkdir -p $TMP
PASS=0; FAIL=0; FAILED_LIST=()

jget(){ python3 -c "import sys,json;d=json.load(sys.stdin);
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
req(){
  local m=$1 p=$2 bf=$3
  local args=(-s -o $TMP/last.json -w '%{http_code}' -X $m "$BASE$p" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json')
  [ -n "$bf" ] && args+=(-d @"$bf")
  curl "${args[@]}"
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

step "阶段 5：队伍报名（选手视角真实操作）"
echo "{\"name\":\"红队-$SUF\",\"code\":\"RED$SUF\"}" > $TMP/t1.json
check "POST /contests/{id}/teams 建红队" req POST /api/contests/$CID/teams $TMP/t1.json
TEAM1=$(cat $TMP/last.json | jget "data.id")
echo "{\"name\":\"蓝队-$SUF\",\"code\":\"BLUE$SUF\"}" > $TMP/t2.json
check "POST /contests/{id}/teams 建蓝队" req POST /api/contests/$CID/teams $TMP/t2.json
TEAM2=$(cat $TMP/last.json | jget "data.id")
check "GET  /contests/{id}/teams 队伍列表" req GET "/api/contests/$CID/teams?page=1&size=10"
check "GET  /teams 全局战队视图"       req GET "/api/teams?page=1&size=10"
echo "  📌 TEAM1=$TEAM1 TEAM2=$TEAM2"

step "阶段 6：报名审核（管理员通过）"
check "POST /teams/{tid}/review 审核红队" req POST "/api/contests/$CID/teams/$TEAM1/review?status=APPROVED"
check "POST /teams/{tid}/review 审核蓝队" req POST "/api/contests/$CID/teams/$TEAM2/review?status=APPROVED"
check "GET  /teams/{tid}/members 队伍成员" req GET /api/contests/$CID/teams/$TEAM1/members

step "阶段 7：开赛 RUNNING"
echo '{"status":"RUNNING"}' > $TMP/run.json
check "POST /contests/{id}/status -> RUNNING" req POST /api/contests/$CID/status $TMP/run.json

step "阶段 8：做题（真实提交 flag，含错误与重复提交）"
cat > $TMP/sub_wrong.json <<J
{"contestId":$CID,"questionId":$Q1,"content":"flag{wrong_guess}","submitType":"FLAG","startTs":$(( $(date +%s%3N) - 45000 ))}
J
check "POST /ctf/submissions 提交错误flag" req POST /api/ctf/submissions $TMP/sub_wrong.json
cat > $TMP/sub_ok1.json <<J
{"contestId":$CID,"questionId":$Q1,"content":"flag{sqli_$SUF}","submitType":"FLAG","startTs":$(( $(date +%s%3N) - 60000 ))}
J
check "POST /ctf/submissions 提交正确flag(Q1)" req POST /api/ctf/submissions $TMP/sub_ok1.json
echo "  判分结果: $(cat $TMP/last.json | jget 'data.score') 分 / 首杀: $(cat $TMP/last.json | jget 'data.firstBlood')"
cat > $TMP/sub_ok2.json <<J
{"contestId":$CID,"questionId":$Q2,"content":"flag{misc_$SUF}","submitType":"FLAG","startTs":$(( $(date +%s%3N) - 120000 ))}
J
check "POST /ctf/submissions 提交正确flag(Q2)" req POST /api/ctf/submissions $TMP/sub_ok2.json
check "POST /ctf/submissions 重复提交(应不计分)" req POST /api/ctf/submissions $TMP/sub_ok2.json
echo "  重复提交得分: $(cat $TMP/last.json | jget 'data.score')"

step "阶段 9：排行榜与提交记录"
check "GET  /ctf/contests/{id}/scoreboard 排行榜" req GET "/api/ctf/contests/$CID/scoreboard"
check "GET  /ctf/contests/{id}/scoreboard/mine 我的排名" req GET /api/ctf/contests/$CID/scoreboard/mine
check "GET  /ctf/contests/{id}/solved 已解题目" req GET /api/ctf/contests/$CID/solved
check "GET  /ctf/contests/{id}/submissions 提交记录" req GET "/api/ctf/contests/$CID/submissions?page=1&size=10"
check "POST /scoreboard/refresh 刷新排行" req POST /api/ctf/contests/$CID/scoreboard/refresh

step "阶段 10：态势感知大屏（比赛进行中的实时数据）"
check "GET  /situation/overview 总览" req GET "/api/situation/overview?contestId=$CID"
check "GET  /situation/ranking 排行" req GET "/api/situation/ranking?contestId=$CID&limit=10"
check "GET  /situation/screen 大屏" req GET "/api/situation/screen?contestId=$CID&topN=10"
check "GET  /situation/3d 3D大屏" req GET "/api/situation/3d?contestId=$CID"
check "GET  /situation/monitor 靶机监控" req GET "/api/situation/monitor?contestId=$CID"
check "GET  /situation/team/{tid} 战队主页" req GET "/api/situation/team/$CID/$TEAM1"

step "阶段 11：通知与公告"
echo "{\"title\":\"演练通知-$SUF\",\"content\":\"比赛进行中\",\"level\":\"INFO\"}" > $TMP/n.json
check "POST /notify/send 发送通知" req POST /api/notify/send $TMP/n.json
check "GET  /notify/my 我的通知" req GET "/api/notify/my?page=1&size=10"
check "GET  /notify/unread-count 未读数" req GET /api/notify/unread-count
echo "{\"title\":\"平台公告-$SUF\",\"content\":\"演练公告\",\"type\":\"NOTICE\",\"status\":1}" > $TMP/a.json
check "POST /announcements 发公告" req POST /api/announcements $TMP/a.json
check "GET  /announcements/latest 最新公告" req GET /api/announcements/latest

step "阶段 12：理论赛"
echo '{"status":"FINISHED"}' > $TMP/fin.json
check "GET  /ctf/contests/{id}/theory/paper 理论试卷" req GET /api/ctf/contests/$CID/theory/paper

step "阶段 13：审计与归档"
check "GET  /audit-logs 审计日志" req GET "/api/audit-logs?page=1&size=10"
check "POST /archives/contest/{id} 赛事归档" req POST /api/archives/contest/$CID
check "GET  /archives/contest/{id} 归档查询" req GET /api/archives/contest/$CID

step "阶段 14：AWD 攻防赛（独立赛制）"
cat > $TMP/cawd.json <<J
{"code":"AWD$SUF","name":"演练AWD赛-$SUF","type":"AWD","mode":"TEAM","startAt":"2026-01-01T00:00:00","endAt":"2027-12-31T23:59:59","maxPlayers":100}
J
check "POST /contests 创建AWD赛事" req POST /api/contests $TMP/cawd.json
ACID=$(cat $TMP/last.json | jget "data.id")
echo "  📌 ACID=$ACID"
for st in PENDING SIGNUP RUNNING; do
  echo "{\"status\":\"$st\"}" > $TMP/st2.json
  check "AWD赛事状态 -> $st" req POST /api/contests/$ACID/status $TMP/st2.json
done
echo "{\"name\":\"攻防红队-$SUF\",\"code\":\"AR$SUF\"}" > $TMP/at1.json
check "POST AWD赛事建队红" req POST /api/contests/$ACID/teams $TMP/at1.json
ATEAM1=$(cat $TMP/last.json | jget "data.id")
echo "{\"name\":\"攻防蓝队-$SUF\",\"code\":\"AB$SUF\"}" > $TMP/at2.json
check "POST AWD赛事建队蓝" req POST /api/contests/$ACID/teams $TMP/at2.json
ATEAM2=$(cat $TMP/last.json | jget "data.id")
check "审核AWD红队" req POST "/api/contests/$ACID/teams/$ATEAM1/review?status=APPROVED"
check "审核AWD蓝队" req POST "/api/contests/$ACID/teams/$ATEAM2/review?status=APPROVED"

cat > $TMP/round.json <<J
{"contestId":$ACID,"roundNo":1,"durationS":600,"mode":"AWD","provisionTargets":false}
J
check "POST /awd/rounds 创建第1轮" req POST /api/awd/rounds $TMP/round.json
check "GET  /awd/rounds/{cid}/current 当前轮" req GET /api/awd/rounds/$ACID/current
check "POST /awd/rounds/{cid}/{no}/start 开始轮次" req POST /api/awd/rounds/$ACID/1/start
check "GET  /awd/rounds/{cid}/{no} 轮次详情" req GET /api/awd/rounds/$ACID/1
check "GET  /awd/score/rules 计分规则" req GET /api/awd/score/rules
check "GET  /awd/score/team 队伍积分" req GET "/api/awd/score/team?contestId=$ACID&teamId=$ATEAM1"
check "GET  /awd/score/leaderboard 攻防排行" req GET "/api/awd/score/leaderboard?contestId=$ACID&topN=10"
cat > $TMP/atk.json <<J
{"contestId":$ACID,"attackerTeamId":$ATEAM1,"victimTeamId":$ATEAM2,"questionId":$Q1,"flag":"flag{fake_attack}"}
J
check "POST /awd/score/attack 攻击提交" req POST /api/awd/score/attack $TMP/atk.json
check "POST /awd/rounds/{cid}/{no}/check 轮次检查" req POST /api/awd/rounds/$ACID/1/check
check "POST /awd/rounds/{cid}/{no}/finish 结束轮次" req POST /api/awd/rounds/$ACID/1/finish
check "GET  /awd/rounds/{cid}/{no}/next-states 状态流转" req GET /api/awd/rounds/$ACID/1/next-states

step "阶段 15：靶场服务（11002 独立靶场）"
AB=http://127.0.0.1:11002
printf "  %-32s " "GET /arena/health 健康"
curl -s -o $TMP/ah.json -w "HTTP=%{http_code} " $AB/api/arena/health; echo "code=$(cat $TMP/ah.json|jget code)"
printf "  %-32s " "GET /templates 模板"
curl -s -o $TMP/at.json -w "HTTP=%{http_code} " "$AB/api/templates?page=1&size=10"; echo "code=$(cat $TMP/at.json|jget code)"
printf "  %-32s " "POST /instances 创建靶机"
curl -s -o $TMP/ai.json -w "HTTP=%{http_code} " -X POST $AB/api/instances -H 'Content-Type: application/json' -d '{"templateId":9100000000000000001,"ownerId":1,"ownerName":"admin"}'; echo "code=$(cat $TMP/ai.json|jget code)"
IID=$(cat $TMP/ai.json|jget data.id); AURL=$(cat $TMP/ai.json|jget data.accessUrl)
echo "  📌 靶机 IID=$IID URL=$AURL"
if [ -n "$AURL" ]; then printf "  %-32s " "访问靶机首页"; curl -s -o /dev/null -w "HTTP=%{http_code}\n" "$AURL/"; fi
printf "  %-32s " "GET /instances 实例列表"
curl -s -o $TMP/al.json -w "HTTP=%{http_code} " "$AB/api/instances?page=1&size=10"; echo "code=$(cat $TMP/al.json|jget code)"
printf "  %-32s " "POST /instances/{id}/reset 重置"
curl -s -o /dev/null -w "HTTP=%{http_code}\n" -X POST "$AB/api/instances/$IID/reset"
printf "  %-32s " "DELETE /instances/{id} 销毁"
curl -s -o /dev/null -w "HTTP=%{http_code}\n" -X DELETE "$AB/api/instances/$IID"

step "阶段 16：统计与收尾"
check "GET  /stats/overview 平台统计" req GET /api/stats/overview
check "GET  /situation/overview 全站态势" req GET /api/situation/overview

echo ""
echo "════════════════════════════════════════════════════════════"
echo "  演练汇总：通过 $PASS / 失败 $FAIL"
if [ $FAIL -gt 0 ]; then echo "  失败项："; for f in "${FAILED_LIST[@]}"; do echo "    - $f"; done; fi
echo "════════════════════════════════════════════════════════════"
