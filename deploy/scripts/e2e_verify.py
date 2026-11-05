#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
业务流端到端验证（角色真实分离）：
  CTF 团队赛 / 理论个人赛 / 场景实操赛 —— 建赛 → 组卷 → 报名 → 答题 → 判分 → 榜单 全链路
  + 态势感知与平台统计
  + 独立靶场服务（未部署时明确跳过，不记为失败）

契约要点（与现役后端一致，勿再用已废弃的「建队即报名」写法）：
  1. 战队是全局资源：POST /api/teams（创建者自动为队长）
  2. 报名是赛事维度的：POST /api/ctf/contests/{cid}/teams/enroll
     body = {"mode":"TEAM","teamId":<全局战队id>} 或 {"mode":"INDIVIDUAL"}
  3. 报名窗口只在 SIGNUP 阶段开放，开赛后 409；未报名答题 400
  4. 赛事状态机逐级推进：DRAFT→PENDING→SIGNUP→RUNNING→FINISHED→ARCHIVED
  5. mode 必须与报名方式匹配：TEAM 赛事只能战队报名，INDIVIDUAL 赛事只能个人报名
"""
import json
import os
import sys
import time
import urllib.error
import urllib.request

BASE = os.environ.get("BASE", "http://127.0.0.1")
ARENA = os.environ.get("ARENA", "http://127.0.0.1:11002/api")
ADMIN_PASS = os.environ.get("ADMIN_PASS", "Admin@123")
PLAYER_PASS = "Flow@12345"
HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.environ.get(
    "OUT", os.path.normpath(os.path.join(HERE, "..", "..", "docs", "业务流端到端验证结果.json")))

PASS = FAIL = SKIP = 0
FAILED = []
RESULT = []


def record(name, ok, detail):
    RESULT.append({"item": name, "ok": ok, "detail": str(detail)[:300]})


def check(name, ok, detail=""):
    global PASS, FAIL
    if ok:
        PASS += 1
        print(f"  ✅ {name} {detail}")
    else:
        FAIL += 1
        FAILED.append(name)
        print(f"  ❌ {name} {detail}")
    record(name, ok, detail)


def skip(name, detail=""):
    global SKIP
    SKIP += 1
    print(f"  ⏭  {name}（跳过） {detail}")
    record(name, None, detail)


def msg_of(d):
    return d.get("message") if isinstance(d, dict) else d


def num(v):
    """榜单/判分字段在不同序列化路径下可能是 str 或 int，统一成数值再比较。"""
    try:
        return float(v)
    except (TypeError, ValueError):
        return 0.0


class Client:
    """带独立登录态的调用方：管理员 / 队长 / 队员 / 路人。"""

    def __init__(self, label, base=BASE, tok=None):
        self.label = label
        self.base = base
        self.tok = tok

    def req(self, method, path, body=None, token=True, raw=False, timeout=30):
        data = None
        headers = {"Content-Type": "application/json"}
        if token and self.tok:
            headers["Authorization"] = "Bearer " + self.tok
        if body is not None:
            data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        r = urllib.request.Request(self.base + path, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(r, timeout=timeout) as resp:
                txt = resp.read().decode("utf-8", "ignore")
                return resp.status, (txt if raw else json.loads(txt))
        except urllib.error.HTTPError as e:
            txt = e.read().decode("utf-8", "ignore")
            try:
                return e.code, json.loads(txt)
            except Exception:
                return e.code, txt

    def data(self, method, path, body=None):
        """取业务 data；失败直接抛带后端 message 的异常，不让错误静默变形。"""
        st, d = self.req(method, path, body)
        if not isinstance(d, dict):
            raise RuntimeError(f"[{self.label}] {method} {path} 非 JSON 响应 HTTP={st}")
        if d.get("code") != 0:
            raise RuntimeError(f"[{self.label}] {method} {path} -> code={d.get('code')} "
                               f"msg={d.get('message')} HTTP={st}")
        return d.get("data")

    def login(self, username, password):
        d = self.data("POST", "/api/auth/login", {"username": username, "password": password})
        self.tok = d["accessToken"]
        return self


ADMIN = None


def advance(cid, targets):
    """状态机逐级推进，返回 (失败的目标状态, 后端响应)。"""
    for st in targets:
        _, d = ADMIN.req("POST", f"/api/contests/{cid}/status", {"status": st})
        if not (isinstance(d, dict) and d.get("code") == 0):
            return st, d
    return None, None


def new_player(username, nickname):
    st, d = ADMIN.req("POST", "/api/auth/register", {
        "username": username, "password": PLAYER_PASS,
        "nickname": nickname, "realName": username, "email": f"{username}@flow.test"})
    ok = isinstance(d, dict) and d.get("code") == 0
    check(f"注册选手账号 {username}", ok, msg_of(d))
    if not ok:
        raise RuntimeError(f"注册 {username} 失败：{msg_of(d)}")
    return Client(username).login(username, PLAYER_PASS)


def questions(bank_type, n):
    return (ADMIN.data("GET", f"/api/questions?page=1&size=50&bankType={bank_type}")
            .get("records") or [])[:n]


def run_ctf(suffix):
    print("\n▶ A. CTF 团队赛全流程（建赛→组卷→报名→答题→判分→榜单）")
    cid = ADMIN.data("POST", "/api/contests", {
        "code": f"FCTF{suffix}", "name": "流程-CTF团队赛", "type": "CTF", "mode": "TEAM",
        "maxPlayers": 100, "description": "业务流端到端验证赛"})["id"]
    check("CTF 建赛（mode=TEAM）", bool(cid), f"id={cid}")

    qs = questions("CTF", 3)
    if len(qs) < 3:
        check("CTF 题库可用题数 ≥3", False, f"实际={len(qs)}")
        return None, None
    ADMIN.data("POST", f"/api/contests/{cid}/questions",
               {"questionIds": [q["id"] for q in qs], "overwrite": True})
    check("CTF 组卷（3 题）", True)

    bad, err = advance(cid, ["PENDING", "SIGNUP"])
    check("推进到 SIGNUP（报名窗口开启）", bad is None, msg_of(err))
    if bad:
        return None, None

    captain = new_player(f"cap{suffix}", "流程队长")
    team = captain.data("POST", "/api/teams", {"name": f"流程战队{suffix}", "maxMembers": 5})
    check("队长建全局战队", bool(team.get("id")), f"teamId={team['id']}")
    ct = captain.data("POST", f"/api/ctf/contests/{cid}/teams/enroll",
                      {"mode": "TEAM", "teamId": team["id"]})
    check("以战队报名参赛", bool(ct.get("id")),
          f"参赛记录={ct['id']} 成员={ct.get('members')} 状态={ct.get('status')}")

    opts = captain.data("GET", f"/api/ctf/contests/{cid}/teams/options")
    check("报名可选项回显已报名", opts.get("enrolled") is True, f"mode={opts.get('mode')}")

    _, dup = captain.req("POST", f"/api/ctf/contests/{cid}/teams/enroll",
                         {"mode": "TEAM", "teamId": team["id"]})
    check("重复报名被拒（409）", isinstance(dup, dict) and dup.get("code") == 409, msg_of(dup))

    bad, err = advance(cid, ["RUNNING"])
    check("推进到 RUNNING（答题窗口开启）", bad is None, msg_of(err))
    if bad:
        return None, None

    _, late = captain.req("POST", f"/api/ctf/contests/{cid}/teams/enroll", {"mode": "INDIVIDUAL"})
    check("开赛后报名被拒（409）", isinstance(late, dict) and late.get("code") == 409, msg_of(late))

    play = captain.data("GET", f"/api/ctf/contests/{cid}/challenges/play")
    check("选手端题目列表", len(play) >= 3, f"count={len(play)}")

    admin_view = ADMIN.data("GET", f"/api/ctf/contests/{cid}/challenges")
    flags = {q["id"]: q.get("flag") for q in admin_view}

    # 精确判定：既不能出现 flag/answer 字段，也不能把真实 flag 值混在题面里下发
    leaked = []
    for c in play:
        blob = json.dumps(c, ensure_ascii=False)
        keys = set(c.keys())
        if keys & {"flag", "answer", "flagTemplate"}:
            leaked.append((c.get("id"), "字段泄露"))
        real = flags.get(c.get("id"))
        if real and real in blob:
            leaked.append((c.get("id"), "题面含真实 flag"))
    check("选手端题目不下发 flag", not leaked, f"泄露={leaked}")

    r1 = captain.data("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": qs[0]["id"], "content": flags[qs[0]["id"]],
        "submitType": "FLAG", "deviceId": f"flow-{suffix}"})
    check("CTF 正确 flag 判分", r1.get("correct") is True,
          f"correct={r1.get('correct')} 本题={r1.get('score')} 累计={r1.get('totalScore')}")

    r2 = captain.data("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": qs[1]["id"], "content": "flag{definitely_wrong}",
        "submitType": "FLAG", "deviceId": f"flow-{suffix}"})
    check("CTF 错误 flag 判分", r2.get("correct") is False, f"correct={r2.get('correct')}")

    stranger = new_player(f"out{suffix}", "流程路人")
    _, nofa = stranger.req("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": qs[2]["id"], "content": flags[qs[2]["id"]],
        "submitType": "FLAG"})
    check("未报名选手答题被拒", isinstance(nofa, dict) and nofa.get("code") != 0, msg_of(nofa))

    board = captain.data("GET", f"/api/ctf/contests/{cid}/scoreboard")
    mine = [b for b in board if str(b.get("teamId")) == str(ct["id"])]
    check("CTF 计分板含本队且得分>0",
          len(mine) == 1 and num(mine[0].get("score")) > 0,
          f"榜单条数={len(board)} 本队得分={mine[0].get('score') if mine else '缺失'}")

    solved = captain.data("GET", f"/api/ctf/contests/{cid}/solved")
    check("CTF 解题列表可查", isinstance(solved, list), f"已解={len(solved or [])}")
    return cid, team["id"]


def run_theory(suffix):
    print("\n▶ B. 理论个人赛全流程")
    cid = ADMIN.data("POST", "/api/contests", {
        "code": f"FTH{suffix}", "name": "流程-理论个人赛", "type": "THEORY",
        "mode": "INDIVIDUAL", "maxPlayers": 100, "description": "业务流端到端验证赛"})["id"]
    check("理论建赛（mode=INDIVIDUAL）", bool(cid), f"id={cid}")

    qs = questions("THEORY", 3)
    if len(qs) < 3:
        check("理论题库可用题数 ≥3", False, f"实际={len(qs)}")
        return
    ADMIN.data("POST", f"/api/contests/{cid}/questions",
               {"questionIds": [q["id"] for q in qs], "overwrite": True})
    check("理论组卷（3 题）", True)

    bad, err = advance(cid, ["PENDING", "SIGNUP"])
    check("推进到 SIGNUP", bad is None, msg_of(err))
    if bad:
        return

    player = new_player(f"thy{suffix}", "流程理论选手")
    ct = player.data("POST", f"/api/ctf/contests/{cid}/teams/enroll", {"mode": "INDIVIDUAL"})
    check("个人报名参赛", bool(ct.get("id")), f"参赛记录={ct['id']}")

    t = player.data("POST", "/api/teams", {"name": f"流程理论战队{suffix}", "maxMembers": 5})
    _, merr = player.req("POST", f"/api/ctf/contests/{cid}/teams/enroll",
                         {"mode": "TEAM", "teamId": t["id"]})
    check("个人赛拒绝战队报名（409）", isinstance(merr, dict) and merr.get("code") == 409, msg_of(merr))

    bad, err = advance(cid, ["RUNNING"])
    check("推进到 RUNNING", bad is None, msg_of(err))
    if bad:
        return

    paper = player.data("GET", f"/api/ctf/contests/{cid}/theory/paper")
    qlist = paper.get("questions") or []
    qids = [q.get("id") or q.get("questionId") for q in qlist]
    check("理论取试卷", len(qids) > 0 and paper.get("questionCount") == len(qids),
          f"题数={paper.get('questionCount')} 总分={paper.get('totalScore')}")
    check("试卷不下发正确答案", not any("answer" in json.dumps(q).lower() for q in qlist))

    rep = player.data("POST", f"/api/ctf/contests/{cid}/theory/submit", [
        {"questionId": q["id"], "answer": q.get("answer"), "deviceId": f"flow-theory-{suffix}"}
        for q in qs])
    check("理论整卷交卷判分", (rep.get("correctCount") or 0) >= 1,
          f"正确={rep.get('correctCount')}/{rep.get('totalCount')} 得分={rep.get('gainedScore')}")

    board = player.data("GET", f"/api/ctf/contests/{cid}/scoreboard")
    check("理论计分板有成绩", len(board) >= 1 and any(num(b.get("score")) > 0 for b in board),
          f"条数={len(board)}")


def run_scenario(suffix, team_id):
    print("\n▶ C. 场景实操赛全流程")
    cid = ADMIN.data("POST", "/api/contests", {
        "code": f"FSC{suffix}", "name": "流程-场景实操赛", "type": "SCENARIO",
        "mode": "TEAM", "maxPlayers": 100, "description": "业务流端到端验证赛"})["id"]
    check("场景建赛", bool(cid), f"id={cid}")

    qs = questions("SCENARIO", 3)
    if not qs:
        check("场景题库可用题数 ≥1", False, "实际=0（SCENARIO 题目不足）")
        return
    ADMIN.data("POST", f"/api/contests/{cid}/questions",
               {"questionIds": [q["id"] for q in qs], "overwrite": True})
    check(f"场景组卷（{len(qs)} 题）", True)

    bad, err = advance(cid, ["PENDING", "SIGNUP"])
    check("推进到 SIGNUP", bad is None, msg_of(err))
    if bad:
        return

    captain = Client(f"cap{suffix}").login(f"cap{suffix}", PLAYER_PASS)
    ct = captain.data("POST", f"/api/ctf/contests/{cid}/teams/enroll",
                      {"mode": "TEAM", "teamId": team_id})
    check("战队报名场景赛", bool(ct.get("id")), f"参赛记录={ct['id']}")

    bad, err = advance(cid, ["RUNNING"])
    check("推进到 RUNNING", bad is None, msg_of(err))
    if bad:
        return

    admin_view = ADMIN.data("GET", f"/api/ctf/contests/{cid}/challenges")
    flag = next((q.get("flag") for q in admin_view if q.get("flag")), None)
    if not flag:
        check("场景题证据 flag 可用", False, "题目未配置 flag")
        return
    r = captain.data("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": qs[0]["id"], "content": flag, "submitType": "FLAG"})
    check("场景 flag 判分", r.get("correct") is True,
          f"correct={r.get('correct')} 得分={r.get('score')}")

    board = captain.data("GET", f"/api/ctf/contests/{cid}/scoreboard")
    check("场景计分板有成绩", any(num(b.get("score")) > 0 for b in board), f"条数={len(board)}")


def run_situation(cid):
    print("\n▶ D. 态势感知与统计")
    if not cid:
        skip("态势接口", "无进行中的 CTF 赛事")
    else:
        ov = ADMIN.data("GET", f"/api/situation/overview?contestId={cid}")
        check("态势总览", bool(ov), f"赛事={ov.get('contestName')}")
        rk = ADMIN.data("GET", f"/api/situation/ranking?contestId={cid}&limit=10")
        check("态势排行榜有成绩", len(rk) >= 1, f"条数={len(rk)}")
        sc = ADMIN.data("GET", f"/api/situation/screen/{cid}")
        check("态势大屏聚合", bool(sc), f"字段数={len(sc) if isinstance(sc, dict) else '-'}")
    st = ADMIN.data("GET", "/api/stats/overview")
    check("平台统计", bool(st), str(st)[:140])


def run_arena():
    print("\n▶ E. 独立靶场服务（csp-arena）")
    probe = Client("arena", ARENA)
    try:
        st, d = probe.req("GET", "/templates?page=1&size=50", token=False, timeout=5)
    except Exception as e:
        skip("靶场服务", f"{ARENA} 不可达（{type(e).__name__}）——服务未部署，"
                        "AWD/动态 flag/靶机生命周期需求无法实测")
        return
    if not isinstance(d, dict):
        skip("靶场服务", f"响应非 JSON HTTP={st}")
        return
    recs = (d.get("data") or {}).get("records") or []
    check("靶场模板 ≥10", len(recs) >= 10, f"count={len(recs)}")
    target = next((r for r in recs if r.get("code") == "awd-web-sqli"), recs[0] if recs else None)
    if not target:
        check("靶机创建", False, "无可用模板")
        return
    _, ins = probe.req("POST", "/instances", {"templateId": target["id"], "ttlMinutes": 5}, token=False)
    ok = isinstance(ins, dict) and ins.get("code") == 0
    inst = (ins.get("data") or {}) if ok else {}
    check("创建靶机", ok, f"{target.get('code')} -> {inst.get('accessUrl')}")
    if not ok:
        return
    try:
        hcode, _ = Client("t", inst.get("accessUrl", "")).req("GET", "", token=False, raw=True, timeout=15)
        check("靶机首页可访问", hcode == 200, f"HTTP={hcode}")
    except Exception as e:
        check("靶机首页可访问", False, type(e).__name__)
    iid = inst["id"]
    _, rr = probe.req("POST", f"/instances/{iid}/reset", {}, token=False)
    check("重置靶机", isinstance(rr, dict) and rr.get("code") == 0)
    _, dr = probe.req("POST", f"/instances/{iid}/destroy", {}, token=False)
    check("销毁靶机", isinstance(dr, dict) and dr.get("code") == 0)


def main():
    global ADMIN
    suffix = int(time.time())
    ADMIN = Client("admin")
    try:
        ADMIN.login("admin", ADMIN_PASS)
        ADMIN.data("GET", "/api/questions?page=1&size=1")
    except Exception as e:
        print(f"❌ 基线不可用：管理员登录/题库探活失败（{e}）")
        return 1
    print(f"基线 {BASE}  靶场 {ARENA}  管理员登录 OK  账号后缀={suffix}")

    try:
        ctf, team_id = run_ctf(suffix)
        run_theory(suffix)
        if team_id:
            run_scenario(suffix, team_id)
        else:
            skip("场景实操赛", "CTF 段未建立战队，无法复用")
        run_situation(ctf)
        run_arena()
    except Exception as e:
        check("流程未因异常中断", False, f"{type(e).__name__}: {e}")

    print("\n" + "═" * 56)
    print(f"通过 {PASS} / 失败 {FAIL} / 跳过 {SKIP}")
    if FAILED:
        print("失败项：" + "、".join(FAILED))
    try:
        out = os.path.abspath(OUT)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, "w", encoding="utf-8") as fp:
            json.dump({"base": BASE, "pass": PASS, "fail": FAIL, "skip": SKIP,
                       "failed": FAILED, "items": RESULT}, fp, ensure_ascii=False, indent=2)
        print("结果已写入", out)
    except Exception as e:
        print("结果落盘失败:", e)
    return 0 if FAIL == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
