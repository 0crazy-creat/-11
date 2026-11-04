#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从 GitHub（走 api.github.com，raw 域名被墙）拉取真实数据并生成 SQL：
  1) CTF 实战题：le31ei/ctf_challenges  desc.yaml + Readme.md
  2) 理论题：siennafaleiro/CompTIA-A-Plus-Practice-Questions  Markdown 试卷
输出：deploy/sql/04_import_questions.sql
"""
import json, urllib.request, urllib.parse, base64, re, os, sys, time

API = "https://api.github.com"
OUT = "/home/ai/网安平台项目/deploy/sql/04_import_questions.sql"
CACHE = "/home/ai/网安平台项目/data/import/cache"
os.makedirs(CACHE, exist_ok=True)

def api(url, retry=3):
    last = None
    for i in range(retry):
        try:
            req = urllib.request.Request(url, headers={
                "Accept": "application/vnd.github+json",
                "User-Agent": "csp-import",
            })
            with urllib.request.urlopen(req, timeout=60) as r:
                buf = b""
                while True:
                    try:
                        chunk = r.read(65536)
                    except Exception:
                        break
                    if not chunk:
                        break
                    buf += chunk
                if not buf:
                    raise RuntimeError("empty body")
                return json.loads(buf.decode("utf-8", "ignore"))
        except Exception as e:
            last = e
            if i == retry - 1:
                raise
            time.sleep(2.0)
    raise last

def content(repo, path, branch):
    """读文件（base64 解码），带本地缓存"""
    key = os.path.join(CACHE, base64.urlsafe_b64encode(f"{repo}/{path}".encode()).decode()[:180] + ".txt")
    if os.path.exists(key):
        return open(key, encoding="utf-8").read()
    d = api(f"{API}/repos/{repo}/contents/{urllib.parse.quote(path)}?ref={branch}")
    txt = ""
    if d.get("encoding") == "base64":
        txt = base64.b64decode(d["content"]).decode("utf-8", "ignore")
    open(key, "w", encoding="utf-8").write(txt)
    return txt

def sqlq(s):
    if s is None:
        return "NULL"
    s = str(s).replace("\\", "\\\\").replace("'", "\\'").replace("\x00", "")
    return "'" + s + "'"

def parse_desc(txt):
    """解析极简 YAML（key: value 单层）"""
    d = {}
    for line in txt.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or ":" not in line:
            continue
        k, v = line.split(":", 1)
        d[k.strip()] = v.strip().strip('"').strip("'")
    return d

DIFF_MAP = {"1": "EASY", "2": "MEDIUM", "3": "HARD", "4": "INSANE"}
SCORE_MAP = {"EASY": 100, "MEDIUM": 200, "HARD": 300, "INSANE": 500}

rows = []
seq = 0

def nid():
    global seq
    seq += 1
    return 9300000000000000000 + seq

# ---------------- 1. CTF 实战题 ----------------
CTF_REPO, CTF_BR = "le31ei/ctf_challenges", "main"
tree = api(f"{API}/repos/{CTF_REPO}/git/trees/{CTF_BR}?recursive=1")
descs = sorted(x["path"] for x in tree["tree"]
               if x["type"] == "blob" and x["path"].endswith("desc.yaml"))
print(f"[CTF] 找到 {len(descs)} 道题", flush=True)

ctf_cnt = 0
for dp in descs:
    try:
        meta = parse_desc(content(CTF_REPO, dp, CTF_BR))
    except Exception as e:
        print("  skip(meta)", dp, e); continue
    folder = dp.rsplit("/", 1)[0]
    name = meta.get("name") or folder.split("/")[-1]
    cat = (meta.get("category") or folder.split("/")[0]).upper()
    if cat == "MISC": cat = "MISC"
    desc = meta.get("description") or ""
    flag = meta.get("flag") or ""
    lvl = str(meta.get("hardlevel") or "1")
    diff = DIFF_MAP.get(lvl, "EASY")
    score = SCORE_MAP[diff]
    # Readme 作为 writeup（解题过程）
    wu = ""
    for cand in ("Readme.md", "README.md", "readme.md"):
        try:
            wu = content(CTF_REPO, f"{folder}/{cand}", CTF_BR)
            if wu: break
        except Exception:
            continue
    rows.append(dict(
        id=nid(), bank_type="CTF", category=cat, title=name,
        content=desc or name, answer=None, flag=flag or None,
        score=score, difficulty=diff, writeup=wu or None,
        ext=json.dumps({"repo": CTF_REPO, "path": folder, "source": "github"},
                       ensure_ascii=False),
    ))
    ctf_cnt += 1

print(f"[CTF] 入库 {ctf_cnt} 道", flush=True)

# ---------------- 2. 理论题 ----------------
TH_REPO, TH_BR = "siennafaleiro/CompTIA-A-Plus-Practice-Questions", "main"
try:
    ttree = api(f"{API}/repos/{TH_REPO}/git/trees/{TH_BR}?recursive=1")
    mds = sorted(x["path"] for x in ttree["tree"]
                 if x["type"] == "blob" and x["path"].startswith("Practice Exam") and x["path"].endswith(".md"))
except Exception as e:
    print("[理论] 仓库读取失败", e); mds = []

print(f"[理论] 找到 {len(mds)} 份试卷", flush=True)

th_cnt = 0
for mp in mds:
    try:
        txt = content(TH_REPO, mp, TH_BR)
    except Exception as e:
        print("  skip", mp, e); continue
    # 按 "### NN. Title" 切题
    blocks = re.split(r"\n###\s+", "\n" + txt)
    for b in blocks[1:]:
        lines = b.splitlines()
        if not lines: continue
        title = re.sub(r"^\d+\.\s*", "", lines[0].strip())
        body = "\n".join(lines[1:]).strip()
        if len(body) < 40: continue
        # 抽选项
        opts = re.findall(r"^-\s*([a-d])\)\s*(.+?)\s*$", body, re.M)
        ans_m = re.search(r"\*\*Answer:\*\*\s*([a-d])", body)
        answer = ans_m.group(1) if ans_m else ""
        if not opts or not answer:
            continue
        options = {k: v.rstrip("✅ ").strip() for k, v in opts}
        # 题干（去掉选项与答案行）
        stem = re.split(r"^-\s*[a-d]\)", body, 1, flags=re.M)[0].strip()
        content_md = stem + "\n\n" + "\n".join(f"{k}. {v}" for k, v in sorted(options.items()))
        rows.append(dict(
            id=nid(), bank_type="THEORY", category="SINGLE", title=title[:200],
            content=content_md, answer=json.dumps({"correct": answer, "options": options},
                                                  ensure_ascii=False),
            flag=None, score=10, difficulty="EASY", writeup=None,
            ext=json.dumps({"repo": TH_REPO, "path": mp, "source": "github"},
                           ensure_ascii=False),
        ))
        th_cnt += 1

print(f"[理论] 入库 {th_cnt} 道", flush=True)

# ---------------- 生成 SQL ----------------
with open(OUT, "w", encoding="utf-8") as f:
    f.write("-- 自动生成：GitHub 真实题目导入（验证用）\n")
    f.write(f"-- CTF: {ctf_cnt} 道 | 理论: {th_cnt} 道 | 合计: {len(rows)}\n")
    f.write("SET NAMES utf8mb4;\n")
    for r in rows:
        f.write(
            "INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,writeup,status,ext) VALUES ("
            f"{r['id']},{sqlq(r['bank_type'])},{sqlq(r['category'])},{sqlq(r['title'])},"
            f"{sqlq(r['content'])},{sqlq(r['answer'])},{sqlq(r['flag'])},{r['score']},"
            f"{sqlq(r['difficulty'])},{sqlq(r['writeup'])},1,{sqlq(r['ext'])});\n"
        )
    f.write("SELECT bank_type, COUNT(*) FROM question GROUP BY bank_type;\n")

print(f"SQL 已生成: {OUT}  共 {len(rows)} 行", flush=True)