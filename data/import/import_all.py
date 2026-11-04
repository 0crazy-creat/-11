#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
题库与靶场模板数据补齐（仅用于功能验证，数据量小）
产出:
  1) deploy/sql/05_import_theory_scenario.sql  —— THEORY 理论题 + SCENARIO 场景题
  2) deploy/sql/06_arena_more_templates.sql    —— AWD 靶场模板（共 12 个）
数据来源:
  - 理论题: siennafaleiro/CompTIA-A-Plus-Practice-Questions（GitHub 真实题库，缓存于 data/import/cache）
  - 场景题: 依据需求文档赛制（CFS 场景化渗透 / 应急响应）构造的实操场景，题面为真实攻防链路
"""
import os, re, json, base64

ROOT = "/home/ai/网安平台项目"
CACHE = os.path.join(ROOT, "data/import/cache")
OUT_T = os.path.join(ROOT, "deploy/sql/05_import_theory_scenario.sql")
OUT_A = os.path.join(ROOT, "deploy/sql/06_arena_more_templates.sql")

def sqlq(s):
    if s is None:
        return "NULL"
    s = str(s).replace("\\", "\\\\").replace("'", "\\'").replace("\x00", "")
    return "'" + s + "'"

# ---------------------------------------------------------------- 理论题
# 从 GitHub 缓存中的 CompTIA 试卷解析（key 为 base64(urlsafe) 文件名）
def load_theory():
    items = []
    if not os.path.isdir(CACHE):
        return items
    for fn in sorted(os.listdir(CACHE)):
        path = os.path.join(CACHE, fn)
        try:
            head = open(path, encoding="utf-8", errors="ignore").read(200)
        except Exception:
            continue
        if "Practice Exam" not in head and "### 0" not in head:
            continue
        txt = open(path, encoding="utf-8", errors="ignore").read()
        if "**Answer:**" not in txt:
            continue
        blocks = re.split(r"\n###\s+", "\n" + txt)
        for b in blocks[1:]:
            lines = b.splitlines()
            if not lines:
                continue
            title = re.sub(r"^\d+\.\s*", "", lines[0].strip())
            body = "\n".join(lines[1:]).strip()
            if len(body) < 40:
                continue
            opts = re.findall(r"^-\s*([a-d])\)\s*(.+?)\s*$", body, re.M)
            ans_m = re.search(r"\*\*Answer:\*\*\s*([a-d])", body)
            if not opts or not ans_m:
                continue
            answer = ans_m.group(1)
            options = {k: re.sub(r"[✅\s]+$", "", v).strip() for k, v in opts}
            stem = re.split(r"^-\s*[a-d]\)", body, 1, flags=re.M)[0].strip()
            stem = re.sub(r"\*\*Answer:\*\*.*$", "", stem, flags=re.S).strip()
            content = stem + "\n\n" + "\n".join(f"- {k}) {v}" for k, v in sorted(options.items()))
            items.append({
                "title": title[:200],
                "content": content,
                "answer": answer,          # 理论题判分：直接比对选项字母
                "options": options,
            })
    return items

# 若 GitHub 缓存不足 10 道，用公开安全知识补足（真实考点，非编造）
FALLBACK_THEORY = [
    ("SQL 注入防御的最有效手段", "以下哪种措施对防御 SQL 注入最有效？", ["对用户输入做字符串拼接", "使用参数化查询（预编译语句）", "前端 JS 校验输入长度", "关闭数据库错误回显"], "b"),
    ("XSS 与 HttpOnly", "为降低 XSS 导致会话 Cookie 被窃取的风险，应优先设置？", ["Cookie 的 HttpOnly 与 Secure 属性", "Content-Length 响应头", "Cache-Control: no-store", "Accept-Encoding: gzip"], "a"),
    ("CSRF 防御", "以下哪项是防御 CSRF 的标准做法？", ["校验 Origin/Referer 并配合 CSRF Token", "使用 HTTPS", "对响应做 gzip 压缩", "限制请求体大小"], "a"),
    ("Linux 提权排查", "发现 SUID 位文件可用于提权，排查命令正确的是？", ["find / -perm -4000 -type f 2>/dev/null", "ls -l /etc/passwd", "cat /proc/cpuinfo", "df -h"], "a"),
    ("日志分析", "Web 访问日志中出现大量 ../ 序列，最可能对应哪种攻击？", ["目录穿越（Path Traversal）", "缓冲区溢出", "DNS 劫持", "ARP 欺骗"], "a"),
    ("密码存储", "用户口令在数据库中的正确存储方式是？", ["明文存储", "MD5 单次哈希", "加盐的慢哈希（bcrypt/scrypt/Argon2）", "Base64 编码"], "c"),
    ("Nmap 扫描", "Nmap 中用于探测服务版本的命令参数是？", ["-sV", "-sS", "-Pn", "-O"], "a"),
    ("TLS 配置", "关于 TLS 1.2/1.3 的说法正确的是？", ["TLS 1.3 已废弃 RSA 密钥交换，默认前向安全", "TLS 1.2 不支持任何加密套件", "TLS 与 IPSec 是同一协议", "TLS 只能用于 HTTPS"], "a"),
    ("WebShell 检测", "以下哪项最有助于发现 PHP WebShell？", ["静态扫描 eval/system 等危险函数调用并核对文件完整性", "提高 PHP 内存上限", "开启 opcache", "调整 keepalive 超时"], "a"),
    ("应急响应顺序", "主机失陷后应急响应的第一步通常是？", ["立即格式化重装", "隔离受影响主机并保留现场证据", "直接删除可疑文件", "重启服务"], "b"),
    ("Windows 提权", "Windows 中查看当前用户权限的命令是？", ["whoami /priv", "ipconfig /all", "netstat -ano", "tasklist"], "a"),
    ("Docker 安全", "容器逃逸风险最高的配置是？", ["--privileged 并挂载 /var/run/docker.sock", "只读根文件系统", "非 root 用户运行", "限制 CPU 与内存"], "a"),
    ("WAF 绕过", "以下哪种方式常用于绕过基于关键字的 WAF？", ["大小写混写与注释分割（UnIoN/**/SeLeCt）", "使用标准 GET 请求", "开启 HTTP/2", "设置 User-Agent"], "a"),
    ("密码学基础", "RSA 安全性依赖于哪个数学难题？", ["大整数分解", "离散对数（椭圆曲线）", "格基约减", "子集和"], "a"),
    ("抓包分析", "Wireshark 中过滤 HTTP POST 请求的显示过滤器是？", ["http.request.method==\"POST\"", "tcp.port==80", "ip.addr==1.1.1.1", "dns"], "a"),
    ("权限模型", "RBAC 模型的核心要素是？", ["用户-角色-权限三级映射", "仅用户与权限直连", "基于属性的策略", "基于时间窗口"], "a"),
    ("文件上传", "防止上传 Webshell 的关键校验不包括？", ["校验文件扩展名白名单", "校验文件内容魔数", "重命名并存放于非解析目录", "仅依赖前端 accept 属性"], "d"),
    ("反序列化", "Java 反序列化漏洞的典型触发点是？", ["ObjectInputStream.readObject 处理不可信数据", "String.format 拼接", "ArrayList.add", "Thread.sleep"], "a"),
    ("缓冲区溢出", "栈溢出利用中用于覆盖返回地址的关键是？", ["精确控制溢出长度定位 EIP/RIP", "增加线程数", "开启 ASLR", "使用静态链接"], "a"),
    ("安全编码", "关于 XXE 的说法正确的是？", ["解析 XML 时禁用外部实体（DTD）可缓解", "XXE 只影响 JSON 接口", "开启 DTD 校验即可防御", "XXE 与 XML 无关"], "a"),
]

def build_theory_rows():
    rows, seq = [], 0
    parsed = load_theory()
    src = parsed[:12] if parsed else []
    for it in src:
        seq += 1
        rows.append(dict(
            id=2100000000000000000 + seq, bank_type="THEORY", category="SINGLE",
            title=it["title"], content=it["content"], answer=it["answer"],
            flag=None, score=10, difficulty="EASY", writeup=None,
            ext=json.dumps({"source": "github:siennafaleiro/CompTIA-A-Plus-Practice-Questions",
                            "options": it["options"]}, ensure_ascii=False),
        ))
    # 补足到 14 道
    for t, stem, opts, ans in FALLBACK_THEORY:
        if len(rows) >= 14:
            break
        seq += 1
        option_map = dict(zip("abcd", opts))
        rows.append(dict(
            id=2100000000000000000 + seq, bank_type="THEORY", category="SINGLE",
            title=t, content=stem + "\n\n" + "\n".join(f"- {k}) {v}" for k, v in option_map.items()),
            answer=ans, flag=None, score=10, difficulty="MEDIUM", writeup=None,
            ext=json.dumps({"source": "csp-builtin-seed", "options": option_map}, ensure_ascii=False),
        ))
    return rows

# ---------------------------------------------------------------- 场景题
SCENARIOS = [
    ("场景1：Web 入口点定位与利用链复现", "WEB", "MEDIUM", 300,
     "某企业官网（Nginx + PHP 7.4 + MySQL 5.7）对外开放，安全团队巡检时怀疑存在上传点未做严格校验。\n\n"
     "**任务**：\n1. 通过目录爆破发现隐藏上传接口；\n2. 构造可绕过扩展名校验的 Webshell 并获取命令执行；\n3. 读取靶机 `/flag.txt` 提交。\n\n"
     "**提示**：注意 `Content-Type` 与双写扩展名。",
     "flag{scenario_web_upload_2026}"),
    ("场景2：SQL 注入到数据脱敏外带", "WEB", "MEDIUM", 300,
     "某教务系统查询接口 `?id=1` 存在注入。数据库为 MySQL，`sql_mode` 未开启严格模式，应用屏蔽了 union 关键字。\n\n"
     "**任务**：\n1. 使用报错注入或布尔盲注确认注入点；\n2. 获取 `admin` 表 `password` 字段的前 8 位；\n3. 组合成 flag 提交。\n\n"
     "**提示**：`updatexml` / `extractvalue` 可用。",
     "flag{scenario_sqli_blind_2026}"),
    ("场景3：JWT 弱密钥与越权", "WEB", "HARD", 400,
     "某 API 使用 HS256 签发 JWT，签发逻辑为 `sign(payload, \"secret\")`。前端 `role` 字段由 payload 决定。\n\n"
     "**任务**：\n1. 抓取普通用户 token 并解析 payload；\n2. 爆破弱密钥（字典含常见弱口令）；\n3. 伪造 `role=admin` 的 token 访问 `/api/admin/flag`。",
     "flag{scenario_jwt_admin_2026}"),
    ("场景4：Linux 主机应急响应与后门排查", "EMERGENCY", "MEDIUM", 300,
     "一台对外提供 SSH 的 CentOS 7 主机疑似被入侵，`/tmp` 下存在可疑文件，`crontab` 有异常条目。\n\n"
     "**任务**：\n1. 定位持久化后门（计划任务 + 启动项）；\n2. 排查异常进程与对外连接；\n3. 找到攻击者留下的 flag 文件并提交。\n\n"
     "**提示**：`crontab -l`、`/etc/rc.local`、`ss -antp`。",
     "flag{scenario_ir_persistence_2026}"),
    ("场景5：内存取证与流量还原", "FORENSICS", "HARD", 400,
     "给定一份 pcap 与一份内存镜像，攻击者通过 DNS 隧道外带数据。\n\n"
     "**任务**：\n1. 从流量中提取 DNS 查询的子域名并拼接；\n2. 对拼接结果做 Base64 解码得到外带内容；\n3. 从内容中提取 flag。",
     "flag{scenario_dns_tunnel_2026}"),
    ("场景6：内网横向与域控权限提升", "PENTEST", "HARD", 500,
     "已获得一台内网 Windows 主机普通权限，域内存在 Kerberoasting 风险账户。\n\n"
     "**任务**：\n1. 信息收集：确认域控 IP 与 SPN 账户；\n2. 请求服务票据并离线破解；\n3. 使用获得的口令访问域控共享读取 flag。",
     "flag{scenario_ad_kerberoast_2026}"),
    ("场景7：Web 日志分析与攻击溯源", "FORENSICS", "MEDIUM", 300,
     "提供 10 万行 Nginx access.log，其中混有扫描、注入与上传尝试。\n\n"
     "**任务**：\n1. 统计攻击者 IP 的请求特征；\n2. 找出成功返回 200 的 Webshell 路径；\n3. 提交该路径的 MD5 前 8 位作为 flag。",
     "flag{scenario_log_forensics_2026}"),
    ("场景8：Docker 容器逃逸验证", "CLOUD", "HARD", 400,
     "目标容器以 `--privileged` 启动并挂载了宿主机 `/var/run/docker.sock`。\n\n"
     "**任务**：\n1. 通过 Docker Socket 创建挂载宿主机根目录的容器；\n2. 读取宿主机 `/root/flag.txt`；\n3. 提交 flag。",
     "flag{scenario_docker_escape_2026}"),
    ("场景9：反序列化漏洞利用", "WEB", "INSANE", 500,
     "某 Java 应用使用 Commons-Collections 3.2.1，接收 Base64 序列化数据并直接 `readObject`。\n\n"
     "**任务**：\n1. 使用 ysoserial 生成 CommonsCollections1 载荷；\n2. 触发命令执行反弹 shell；\n3. 读取 `/flag` 提交。",
     "flag{scenario_java_deser_2026}"),
    ("场景10：无线与密码学综合", "CRYPTO", "MEDIUM", 300,
     "截获一段 WPA2 四次握手包与一个 RSA 公钥（e=3，同一明文以三种不同填充加密）。\n\n"
     "**任务**：\n1. 对握手包字典爆破得到 WiFi 口令；\n2. 利用小指数攻击恢复明文；\n3. 明文即为 flag。",
     "flag{scenario_crypto_wifi_2026}"),
]

def build_scenario_rows():
    rows = []
    for i, (title, cat, diff, score, content, flag) in enumerate(SCENARIOS, start=1):
        rows.append(dict(
            id=2101000000000000000 + i, bank_type="SCENARIO", category=cat,
            title=title, content=content, answer=None, flag=flag,
            score=score, difficulty=diff, writeup=None,
            ext=json.dumps({"source": "csp-scenario-seed", "sceneType": cat}, ensure_ascii=False),
        ))
    return rows

# ---------------------------------------------------------------- AWD 靶场模板
# 复用已构建好的基础镜像（csp/arena-basic-web:1.0）与本地已有镜像，全部低资源
TEMPLATES = [
    ("awd-web-basic",   "AWD Web 基础靶场",         "WEB",      "csp/arena-basic-web:1.0", 80,  "EASY",   "Nginx 静态站点，含 flag 注入，用于 AWD 基础轮次", "flag{awd_basic_2026}",   "0.5", "128m", 120),
    ("awd-web-sqli",    "AWD SQL 注入靶场",         "WEB",      "csp/arena-basic-web:1.0", 80,  "MEDIUM", "模拟存在注入点的业务站，考察修补与利用",           "flag{awd_sqli_2026}",    "0.5", "128m", 120),
    ("awd-web-upload",  "AWD 文件上传靶场",         "WEB",      "csp/arena-basic-web:1.0", 80,  "MEDIUM", "存在上传校验缺陷，考察 WebShell 检测与加固",       "flag{awd_upload_2026}",  "0.5", "128m", 120),
    ("awd-web-lfi",     "AWD 文件包含靶场",         "WEB",      "csp/arena-basic-web:1.0", 80,  "MEDIUM", "存在本地文件包含，考察路径限制策略",               "flag{awd_lfi_2026}",     "0.5", "128m", 120),
    ("awd-web-xss",     "AWD XSS 靶场",             "WEB",      "csp/arena-basic-web:1.0", 80,  "EASY",   "存储型 XSS，考察输出编码与 CSP 配置",              "flag{awd_xss_2026}",     "0.5", "128m", 120),
    ("awd-web-ssrf",    "AWD SSRF 靶场",            "WEB",      "csp/arena-basic-web:1.0", 80,  "HARD",   "URL 抓取接口存在 SSRF，考察出网管控",              "flag{awd_ssrf_2026}",    "0.5", "128m", 120),
    ("awd-web-rce",     "AWD 命令执行靶场",         "WEB",      "csp/arena-basic-web:1.0", 80,  "HARD",   "存在命令拼接点，考察输入白名单",                   "flag{awd_rce_2026}",     "0.5", "128m", 120),
    ("awd-pwn-stack",   "AWD PWN 栈溢出靶场",       "PWN",      "kid-linux:1.0",           80,  "HARD",   "32 位栈溢出，考察 NX/Canary 缓解与利用",           "flag{awd_pwn_stack_2026}","0.5", "128m", 120),
    ("awd-crypto-rsa",  "AWD 密码学靶场",           "CRYPTO",   "csp/arena-basic-web:1.0", 80,  "MEDIUM", "RSA 参数误用，考察密钥校验",                       "flag{awd_crypto_2026}",  "0.5", "128m", 120),
    ("awd-misc-forensic","AWD 流量分析靶场",        "MISC",     "csp/arena-basic-web:1.0", 80,  "EASY",   "给定 pcap 供分析，考察取证与外带识别",             "flag{awd_misc_2026}",    "0.5", "128m", 120),
    ("awd-reverse-bin", "AWD 逆向分析靶场",         "REVERSE",  "kid-linux:1.0",           80,  "MEDIUM", "二进制程序校验逻辑，考察静态与动态分析",           "flag{awd_reverse_2026}", "0.5", "128m", 120),
    ("awd-linux-privesc","AWD Linux 提权靶场",      "LINUX",    "kid-linux:1.0",           80,  "HARD",   "SUID 误配置，考察提权排查与加固",                  "flag{awd_privesc_2026}", "0.5", "128m", 120),
]

def build_template_sql():
    lines = ["-- 自动生成：AWD 靶场模板扩充（仅用于功能验证）", "SET NAMES utf8mb4;",
             "USE csp_arena;", ""]
    for i, (code, name, cat, img, port, diff, desc, flag, cpu, mem, ttl) in enumerate(TEMPLATES, start=1):
        tid = 9200000000000000000 + i
        lines.append(
            "INSERT INTO arena_template (id,code,name,category,image,container_port,difficulty,description,default_flag,"
            "cpu_limit,memory_limit,ttl_minutes,status,created_at,updated_at,deleted) VALUES ("
            f"{tid},{sqlq(code)},{sqlq(name)},{sqlq(cat)},{sqlq(img)},{port},{sqlq(diff)},{sqlq(desc)},{sqlq(flag)},"
            f"{sqlq(cpu)},{sqlq(mem)},{ttl},1,NOW(),NOW(),0) "
            f"ON DUPLICATE KEY UPDATE name=VALUES(name),image=VALUES(image),description=VALUES(description),"
            f"difficulty=VALUES(difficulty),status=1,updated_at=NOW();"
        )
    lines.append("")
    lines.append("SELECT category, COUNT(*) AS cnt FROM arena_template WHERE deleted=0 GROUP BY category;")
    return "\n".join(lines) + "\n"



# ---------------------------------------------------------------- CTF（离线缓存）
CTF_CACHE = os.path.join(ROOT, "data/import/cache")

def _cache_read(repo, path):
    """按缓存文件名规则读取（base64 urlsafe 编码 repo/path，截断 180）"""
    key = os.path.join(CTF_CACHE, base64.urlsafe_b64encode(f"{repo}/{path}".encode()).decode()[:180] + ".txt")
    if os.path.exists(key):
        return open(key, encoding="utf-8").read()
    return None

def build_ctf_rows():
    rows = []
    repo = "le31ei/ctf_challenges"
    # 缓存里以 desc.yaml 结尾的文件即题目元信息
    metas = []
    for fn in sorted(os.listdir(CTF_CACHE)):
        if not fn.endswith(".txt"):
            continue
        raw = open(os.path.join(CTF_CACHE, fn), encoding="utf-8", errors="ignore").read()
        if "hardlevel:" not in raw or "flag:" not in raw:
            continue
        meta = {}
        for line in raw.splitlines():
            if ":" in line and not line.startswith((" ", "\t", "#")):
                k, v = line.split(":", 1)
                meta[k.strip()] = v.strip().strip('"').strip("'")
        if meta.get("name"):
            metas.append(meta)
    # 去重（按 name）
    seen, uniq = set(), []
    for m in metas:
        if m["name"] in seen:
            continue
        seen.add(m["name"]); uniq.append(m)

    DIFF = {"1": "EASY", "2": "MEDIUM", "3": "HARD", "4": "INSANE"}
    SCORE = {"EASY": 100, "MEDIUM": 200, "HARD": 300, "INSANE": 500}
    for i, m in enumerate(uniq[:12], start=1):
        cat = (m.get("category") or "misc").upper()
        if cat == "MISC": cat = "MISC"
        diff = DIFF.get(str(m.get("hardlevel") or "1"), "EASY")
        # 找对应的 Readme（同目录）
        writeup = None
        folder = m["name"]
        for cand in (f"{folder}/Readme.md", f"{folder}/README.md"):
            pass
        # 简化：从缓存里找包含该题名 且以 Readme/README 结尾的内容
        for fn in os.listdir(CTF_CACHE):
            raw = open(os.path.join(CTF_CACHE, fn), encoding="utf-8", errors="ignore").read(400)
            if raw.startswith(f"# 题目：{m['name']}"):
                writeup = open(os.path.join(CTF_CACHE, fn), encoding="utf-8", errors="ignore").read()
                break
        rows.append(dict(
            id=2200000000000000000 + i, bank_type="CTF", category=cat,
            title=m["name"], content=m.get("description") or m["name"],
            answer=None, flag=m.get("flag") or None,
            score=SCORE.get(diff, 100), difficulty=diff, writeup=writeup,
            ext=json.dumps({"source": f"github:{repo}", "isDocker": m.get("is_docker")}, ensure_ascii=False),
        ))
    return rows

# ---------------------------------------------------------------- 主流程
def main():
    ctf = build_ctf_rows()
    theory = build_theory_rows()
    scen = build_scenario_rows()
    rows = ctf + theory + scen

    with open(OUT_T, "w", encoding="utf-8") as f:
        f.write("-- 自动生成：理论题 + 场景题导入（验证用，量小）\n")
        f.write(f"-- CTF: {len(ctf)} 道 | 理论题: {len(theory)} 道 | 场景题: {len(scen)} 道 | 合计: {len(rows)}\n")
        f.write("SET NAMES utf8mb4;\nUSE csp;\n")
        for r in rows:
            f.write(
                "INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,writeup,status,ext,deleted,created_at,updated_at) VALUES ("
                f"{r['id']},{sqlq(r['bank_type'])},{sqlq(r['category'])},{sqlq(r['title'])},"
                f"{sqlq(r['content'])},{sqlq(r['answer'])},{sqlq(r['flag'])},{r['score']},"
                f"{sqlq(r['difficulty'])},{sqlq(r['writeup'])},1,{sqlq(r['ext'])},0,NOW(),NOW()) "
                "ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),answer=VALUES(answer),"
                "flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),ext=VALUES(ext),status=1;\n"
            )
        f.write("\nSELECT bank_type, COUNT(*) FROM question WHERE deleted=0 GROUP BY bank_type;\n")

    with open(OUT_A, "w", encoding="utf-8") as f:
        f.write(build_template_sql())

    print(f"[CTF] {len(ctf)} 道")
    print(f"[理论题] {len(theory)} 道")
    print(f"[场景题] {len(scen)} 道")
    print(f"[靶场模板] {len(TEMPLATES)} 个")
    print(f"SQL: {OUT_T}")
    print(f"SQL: {OUT_A}")

if __name__ == "__main__":
    main()