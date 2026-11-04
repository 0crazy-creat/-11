#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
数据驱动端到端验证：
  CTF 赛 / 理论赛 / 场景赛 三套完整流程 + 态势 + 靶场靶机
只验证功能，不产生大量资源占用。
"""
import json, urllib.request, urllib.error, sys

BASE = "http://127.0.0.1:11001"
ARENA = "http://127.0.0.1:11002/api"
TOK = None
PASS = FAIL = 0
FAILED = []


def req(method, path, body=None, base=BASE, token=True, raw=False):
    url = base + path
    data = None
    headers = {"Content-Type": "application/json"}
    if token and TOK:
        headers["Authorization"] = "Bearer " + TOK
    if body is not None:
        data = json.dumps(body, ensure_ascii=False).encode()
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r, timeout=30) as resp:
            txt = resp.read().decode("utf-8", "ignore")
            return resp.status, (txt if raw else json.loads(txt))
    except urllib.error.HTTPError as e:
        txt = e.read().decode("utf-8", "ignore")
        try:
            return e.code, json.loads(txt)
        except Exception:
            return e.code, txt


def check(name, ok, detail=""):
    global PASS, FAIL
    if ok:
        PASS += 1
        print(f"  ✅ {name} {detail}")
    else:
        FAIL += 1
        FAILED.append(name)
        print(f"  ❌ {name} {detail}")


def login():
    global TOK
    _, d = req("POST", "/api/auth/login", {"username": "admin", "password": "Admin@123"}, token=False)
    TOK = d["data"]["accessToken"]
    print(f"登录成功 token={len(TOK)}")


def pick(bank_type, n):
    _, d = req("GET", f"/api/questions?page=1&size=50&bankType={bank_type}")
    recs = d["data"]["records"]
    return recs[:n]


def make_contest(code, name, ctype):
    _, d = req("POST", "/api/contests", {
        "code": code, "name": name, "type": ctype, "mode": "TEAM",
        "maxPlayers": 100, "description": "数据驱动验证赛"
    })
    if d.get("code") != 0:
        print("   创建赛事失败:", d)
        return None
    cid = d["data"]["id"] if isinstance(d["data"], dict) else d["data"]
    print(f"   赛事 {code} id={cid}")
    return cid



def to_running(cid):
    """DRAFT -> PENDING -> SIGNUP -> RUNNING 逐级推进（状态机不允许跳跃）"""
    for st in ("PENDING", "SIGNUP", "RUNNING"):
        code, d = req("POST", f"/api/contests/{cid}/status", {"status": st})
        if code != 200 or (isinstance(d, dict) and d.get("code") != 0):
            print(f"    ! 状态->{st} 失败 {d}")
            return False
    return True


def enroll(cid, tag):
    """报名：创建战队即自动成为队长并报名（答题前置条件）"""
    code, d = req("POST", f"/api/ctf/contests/{cid}/teams",
                  {"contestId": cid, "name": f"{tag}队"})
    ok = code == 200 and isinstance(d, dict) and d.get("code") == 0
    check(f"{tag} 报名建队", ok, str((d or {}).get("message") if isinstance(d,dict) else d))
    return ok

def run_ctf():
    print("\n▶ A. CTF 赛事全流程")
    import time
    cid = make_contest("VCTF%d" % int(time.time()), "验证-CTF赛", "CTF")
    if not cid:
        check("CTF 建赛", False); return
    check("CTF 建赛", True, f"id={cid}")

    qs = pick("CTF", 3)
    ids = [q["id"] for q in qs]
    _, d = req("POST", f"/api/contests/{cid}/questions", {"questionIds": ids, "overwrite": True})
    check("CTF 组卷(3题)", d.get("code") == 0, f"{d.get('message','')}")
    check("赛事推进到 RUNNING", to_running(cid))
    enroll(cid, "CTF")

    _, d = req("GET", f"/api/contests/{cid}/questions")
    cnt = len(d.get("data") or [])
    check("CTF 题目列表", cnt >= 3, f"count={cnt}")

    # 正确 flag
    q = qs[0]
    _, d = req("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": q["id"], "content": q["flag"], "submitType": "FLAG"
    })
    ok = d.get("code") == 0 and d.get("data", {}).get("correct") is True
    check("CTF 正确 flag 判分", ok, str(d.get("data") or d.get("message")))

    # 错误 flag（另一题，避免重复提交锁）
    q2 = qs[1]
    _, d = req("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": q2["id"], "content": "flag{wrong_xxx}", "submitType": "FLAG"
    })
    ok = d.get("code") == 0 and d.get("data", {}).get("correct") is False
    check("CTF 错误 flag 判分", ok, str(d.get("data") or d.get("message")))

    _, d = req("GET", f"/api/ctf/contests/{cid}/scoreboard")
    check("CTF 计分板", d.get("code") == 0, f"条数={len(d.get('data') or [])}")

    _, d = req("GET", f"/api/ctf/contests/{cid}/solved")
    check("CTF 已解列表", d.get("code") == 0, f"已解={len(d.get('data') or [])}")

    _, d = req("GET", f"/api/ctf/contests/{cid}/challenges")
    check("CTF 题目管理列表", d.get("code") == 0, f"条数={len(d.get('data') or [])}")
    return cid


def run_theory():
    print("\n▶ B. 理论赛全流程")
    import time
    cid = make_contest("VTH%d" % int(time.time()), "验证-理论赛", "THEORY")
    if not cid:
        check("理论 建赛", False); return
    check("理论 建赛", True, f"id={cid}")

    qs = pick("THEORY", 3)
    ids = [q["id"] for q in qs]
    _, d = req("POST", f"/api/contests/{cid}/questions", {"questionIds": ids, "overwrite": True})
    check("理论 组卷(3题)", d.get("code") == 0, f"{d.get('message','')}")
    check("赛事推进到 RUNNING", to_running(cid))
    enroll(cid, "理论")

    # 试卷
    _, d = req("GET", f"/api/ctf/contests/{cid}/theory/paper")
    check("理论 取试卷", d.get("code") == 0, f"题数={(d.get('data') or {}).get('questionCount')}")

    # 单题：正确答案
    q = qs[0]
    _, d = req("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": q["id"], "content": q["answer"], "submitType": "ANSWER"
    })
    ok = d.get("code") == 0 and d.get("data", {}).get("correct") is True
    check("理论 正确答案判分", ok, str(d.get("data") or d.get("message")))

    # 单题：错误答案
    q2 = qs[1]
    wrong = "z" if (q2["answer"] or "") != "z" else "y"
    _, d = req("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": q2["id"], "content": wrong, "submitType": "ANSWER"
    })
    ok = d.get("code") == 0 and d.get("data", {}).get("correct") is False
    check("理论 错误答案判分", ok, str(d.get("data") or d.get("message")))

    # 整卷交卷（用第3题）
    q3 = qs[2]
    _, d = req("POST", f"/api/ctf/contests/{cid}/theory/submit",
        [{"questionId": q3["id"], "answer": q3["answer"], "deviceId": "verify-py"}])
    ok = d.get("code") == 0 and (d.get("data") or {}).get("correctCount", 0) >= 1
    check("理论 整卷交卷", ok, str(d.get("data") or d.get("message")))
    return cid


def run_scenario():
    print("\n▶ C. 场景赛全流程")
    import time
    cid = make_contest("VSC%d" % int(time.time()), "验证-场景赛", "SCENARIO")
    if not cid:
        check("场景 建赛", False); return
    check("场景 建赛", True, f"id={cid}")

    qs = pick("SCENARIO", 3)
    ids = [q["id"] for q in qs]
    _, d = req("POST", f"/api/contests/{cid}/questions", {"questionIds": ids, "overwrite": True})
    check("场景 组卷(3题)", d.get("code") == 0, f"{d.get('message','')}")
    check("赛事推进到 RUNNING", to_running(cid))
    enroll(cid, "场景")

    q = qs[0]
    _, d = req("POST", "/api/ctf/submissions", {
        "contestId": cid, "questionId": q["id"], "content": q["flag"], "submitType": "FLAG"
    })
    ok = d.get("code") == 0 and d.get("data", {}).get("correct") is True
    check("场景 flag 判分", ok, str(d.get("data") or d.get("message")))

    _, d = req("GET", f"/api/ctf/contests/{cid}/scoreboard")
    check("场景 计分板", d.get("code") == 0, f"条数={len(d.get('data') or [])}")
    return cid


def run_situation(cid_ctf):
    print("\n▶ D. 态势感知与统计")
    if cid_ctf:
        _, d = req("GET", f"/api/situation/overview?contestId={cid_ctf}")
        check("态势总览", d.get("code") == 0, str((d.get("data") or {}).get("contestName")))
        _, d = req("GET", f"/api/situation/ranking?contestId={cid_ctf}&limit=10")
        check("态势排行榜", d.get("code") == 0, f"条数={len(d.get('data') or [])}")
        _, d = req("GET", f"/api/situation/screen/{cid_ctf}")
        check("态势大屏", d.get("code") == 0)
    _, d = req("GET", "/api/stats/overview")
    check("平台统计", d.get("code") == 0, str(d.get("data")))


def run_arena():
    print("\n▶ E. 独立靶场服务（模板 + 真实靶机）")
    _, d = req("GET", "/templates?page=1&size=50", base=ARENA, token=False)
    recs = (d.get("data") or {}).get("records") or []
    check("靶场模板 ≥10", len(recs) >= 10, f"count={len(recs)}")

    req("POST", "/instances/destroy-all", {}, base=ARENA, token=False)

    # 找一个已存在镜像的模板创建靶机
    target = None
    for r in recs:
        if r["code"] == "awd-web-sqli":
            target = r; break
    if not target:
        target = recs[0]
    _, d = req("POST", "/instances", {"templateId": target["id"], "ttlMinutes": 5}, base=ARENA, token=False)
    ok = d.get("code") == 0
    inst = d.get("data") or {}
    check("创建靶机", ok, f"{target['code']} -> {inst.get('accessUrl')}")

    if ok:
        url = inst.get("accessUrl")
        code, _ = req("GET", "", base=url, token=False, raw=True)
        check("靶机首页可访问", code == 200, f"HTTP={code}")
        iid = inst["id"]
        _, d = req("POST", f"/instances/{iid}/reset", {}, base=ARENA, token=False)
        check("重置靶机", d.get("code") == 0)
        _, d = req("POST", f"/instances/{iid}/destroy", {}, base=ARENA, token=False)
        check("销毁靶机", d.get("code") == 0)


def main():
    login()
    c1 = run_ctf()
    run_theory()
    run_scenario()
    run_situation(c1)
    run_arena()
    print("\n" + "═" * 50)
    print(f"通过 {PASS} / 失败 {FAIL}")
    if FAILED:
        print("失败项:", ", ".join(FAILED))
    return 0 if FAIL == 0 else 1


if __name__ == "__main__":
    sys.exit(main())