#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""真实业务端到端验证：建赛→组卷→状态流转→答题判分→计分板→靶机全生命周期"""
import json, urllib.request, urllib.error, time, sys, subprocess

B = "http://127.0.0.1:11001"
A = "http://127.0.0.1:11002/api"
PASS = FAIL = 0
FAILED = []

def call(method, url, body=None, tok=None, expect=200):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if tok: req.add_header("Authorization", "Bearer " + tok)
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            return r.status, json.loads(r.read().decode() or "{}")
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try: return e.code, json.loads(raw)
        except: return e.code, {"raw": raw}

def check(name, ok, detail=""):
    global PASS, FAIL
    if ok:
        PASS += 1; print(f"  ✅ {name} {detail}")
    else:
        FAIL += 1; FAILED.append(name); print(f"  ❌ {name} {detail}")

def q(sql):
    r = subprocess.run(["docker","exec","csp-mysql","mysql","-uroot","-pcsp_root_2026",
                        "--default-character-set=utf8mb4","-N","-e",sql],
                       capture_output=True, text=True)
    return r.stdout.strip()

# ---------- 0. 登录 ----------
st, r = call("POST", B+"/api/auth/login", {"username":"admin","password":"Admin@123"})
TOK = r["data"]["accessToken"]
check("登录", st==200 and len(TOK)>100, f"token_len={len(TOK)}")

# ---------- 准备题目 ----------
ctf = q("select id,flag from csp.question where bank_type='CTF' and deleted=0 and flag is not null order by id limit 3")
ctf_rows = [l.split("\t") for l in ctf.splitlines() if l]
th = q("select id,answer from csp.question where bank_type='THEORY' and deleted=0 order by id limit 3")
th_rows = [l.split("\t") for l in th.splitlines() if l]
sc = q("select id,flag from csp.question where bank_type='SCENARIO' and deleted=0 order by id limit 3")
sc_rows = [l.split("\t") for l in sc.splitlines() if l]
print(f"\n题库：CTF={len(ctf_rows)} 理论={len(th_rows)} 场景={len(sc_rows)}")

def build_contest(code, name, ctype, qids):
    st, r = call("POST", B+"/api/contests", {
        "code": code, "name": name, "type": ctype, "mode": "TEAM",
        "startAt": "2026-11-02T00:00:00", "endAt": "2027-12-31T23:59:59",
        "description": "端到端验证"}, TOK)
    if st!=200 or r.get("code")!=0:
        return None, f"建赛失败 {r}"
    cid = r["data"]["id"]
    # 组卷
    st, r2 = call("POST", f"{B}/api/contests/{cid}/questions",
                  {"questionIds":[int(x[0]) for x in qids]}, TOK)
    if r2.get("code")!=0:
        return cid, f"组卷失败 {r2}"
    # 状态流转 DRAFT→PENDING→SIGNUP→RUNNING
    for s in ("PENDING","SIGNUP","RUNNING"):
        st, r3 = call("POST", f"{B}/api/contests/{cid}/status", {"status":s}, TOK)
        if r3.get("code")!=0:
            return cid, f"流转 {s} 失败 {r3}"
    return cid, None

TS = int(time.time())

# ---------- A. CTF ----------
print("\n▶ A. CTF 赛全流程")
cid, err = build_contest(f"E2ECTF{TS}", "E2E-CTF验证赛", "CTF", ctf_rows)
check("CTF 建赛+组卷+进行中", err is None, err or f"id={cid}")
if cid:
    st, r = call("GET", f"{B}/api/ctf/contests/{cid}/challenges", tok=TOK)
    check("CTF 题目列表", r.get("code")==0 and len(r["data"])==3, f"count={len(r.get('data') or [])}")
    qid, flag = int(ctf_rows[0][0]), ctf_rows[0][1]
    st, r = call("POST", B+"/api/ctf/submissions",
                 {"contestId":cid,"questionId":qid,"content":flag,"submitType":"FLAG"}, TOK)
    check("CTF 正确 flag 判分", r.get("code")==0 and r["data"].get("correct") is True,
          f"correct={r.get('data',{}).get('correct')} score={r.get('data',{}).get('score')}")
    st, r = call("POST", B+"/api/ctf/submissions",
                 {"contestId":cid,"questionId":qid,"content":"flag{wrong}","submitType":"FLAG"}, TOK)
    check("CTF 错误 flag 判分", r.get("code")==0 and r["data"].get("correct") is False,
          f"correct={r.get('data',{}).get('correct')}")
    st, r = call("GET", f"{B}/api/ctf/contests/{cid}/scoreboard", tok=TOK)
    check("CTF 计分板", r.get("code")==0, f"条数={len(r.get('data') or [])}")
    st, r = call("GET", f"{B}/api/ctf/contests/{cid}/solved", tok=TOK)
    check("CTF 已解列表", r.get("code")==0, f"已解={len(r.get('data') or [])}")

# ---------- B. 理论 ----------
print("\n▶ B. 理论赛全流程")
cid_t, err = build_contest(f"E2ETH{TS}", "E2E-理论验证赛", "THEORY", th_rows)
check("理论 建赛+组卷+进行中", err is None, err or f"id={cid_t}")
if cid_t:
    st, r = call("GET", f"{B}/api/ctf/contests/{cid_t}/theory/paper", tok=TOK)
    check("理论 取试卷", r.get("code")==0 and r["data"].get("questionCount")==3,
          f"题数={r.get('data',{}).get('questionCount')}")
    qid, ans = int(th_rows[0][0]), th_rows[0][1]
    st, r = call("POST", B+"/api/ctf/submissions",
                 {"contestId":cid_t,"questionId":qid,"content":ans,"submitType":"ANSWER"}, TOK)
    check("理论 正确答案判分", r.get("code")==0 and r["data"].get("correct") is True,
          f"correct={r.get('data',{}).get('correct')}")
    st, r = call("POST", B+"/api/ctf/submissions",
                 {"contestId":cid_t,"questionId":qid,"content":"z","submitType":"ANSWER"}, TOK)
    check("理论 错误答案判分", r.get("code")==0 and r["data"].get("correct") is False,
          f"correct={r.get('data',{}).get('correct')}")
    # 整卷交卷
    payload = {"answers":[{"questionId":int(x[0]),"answer":x[1]} for x in th_rows]}
    st, r = call("POST", f"{B}/api/ctf/contests/{cid_t}/theory/submit", payload, TOK)
    check("理论 整卷交卷", r.get("code")==0,
          f"正确={r.get('data',{}).get('correctCount')}/{r.get('data',{}).get('totalCount')} 得分={r.get('data',{}).get('gainedScore')}")

# ---------- C. 场景 ----------
print("\n▶ C. 场景赛全流程")
cid_s, err = build_contest(f"E2ESC{TS}", "E2E-场景验证赛", "SCENARIO", sc_rows)
check("场景 建赛+组卷+进行中", err is None, err or f"id={cid_s}")
if cid_s:
    qid, flag = int(sc_rows[0][0]), sc_rows[0][1]
    st, r = call("POST", B+"/api/ctf/submissions",
                 {"contestId":cid_s,"questionId":qid,"content":flag,"submitType":"FLAG"}, TOK)
    check("场景 flag 判分", r.get("code")==0 and r["data"].get("correct") is True,
          f"correct={r.get('data',{}).get('correct')}")
    st, r = call("GET", f"{B}/api/ctf/contests/{cid_s}/scoreboard", tok=TOK)
    check("场景 计分板", r.get("code")==0, f"条数={len(r.get('data') or [])}")

# ---------- D. 态势 ----------
print("\n▶ D. 态势感知与统计")
st, r = call("GET", f"{B}/api/situation/overview/{cid}", tok=TOK)
check("态势总览", r.get("code")==0, r.get("data",{}).get("contestName"))
st, r = call("GET", f"{B}/api/situation/ranking/{cid}?limit=10", tok=TOK)
check("态势排行榜", r.get("code")==0, f"条数={len(r.get('data') or [])}")
st, r = call("GET", f"{B}/api/stats/overview", tok=TOK)
check("平台统计", r.get("code")==0, str(r.get("data"))[:80])

# ---------- E. 靶场 ----------
print("\n▶ E. 独立靶场服务（AWD 模板 → 真实靶机）")
st, r = call("GET", A+"/templates?page=1&size=50")
tpls = r["data"]["records"]
check("靶场模板 ≥10", len(tpls)>=10, f"count={len(tpls)}")
awd = next((t for t in tpls if t["code"]=="awd-web-sqli"), tpls[0])
st, r = call("POST", A+"/instances", {"templateId": int(awd["id"]), "ttlMinutes": 30})
iid = r.get("data",{}).get("id"); url = r.get("data",{}).get("accessUrl")
check("创建靶机", bool(iid), f"{awd['code']} -> {url}")
if iid:
    ok = False
    for _ in range(30):
        try:
            with urllib.request.urlopen(url, timeout=5) as resp:
                ok = resp.status == 200; break
        except Exception:
            time.sleep(1)
    check("靶机首页可访问", ok, f"HTTP 200 {url}")
    st, r = call("POST", f"{A}/instances/{iid}/reset")
    check("重置靶机", r.get("code")==0)
    st, r = call("POST", f"{A}/instances/{iid}/destroy")
    check("销毁靶机", r.get("code")==0 and r.get("data",{}).get("destroyed",0)>=1, str(r.get("data")))

print("\n" + "═"*52)
print(f"通过 {PASS} / 失败 {FAIL}")
if FAILED: print("失败项:", ", ".join(FAILED))
sys.exit(0 if FAIL==0 else 1)
