-- ================================================================
-- 23_seed_classic_challenges.sql
-- 经典题目统一播种：CTF 每方向 2 道 + 分方向理论题库
-- 幂等：ON DUPLICATE KEY UPDATE，可重复执行
-- 适配统一题库表 question (bank_type = CTF / THEORY)
-- ================================================================
SET NAMES utf8mb4;
USE csp;

-- ----------------------------------------------------------------
-- 一、CTF 经典题（每方向 2 道）
-- category: Web / Crypto / Misc / Reverse / Pwn / Blockchain
-- id 段: 9310xxxxxxxxxxxxxxx 避免与既有题目冲突
-- ----------------------------------------------------------------

-- ===== Web =====
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,tags,writeup,status,ext,deleted,created_at,updated_at) VALUES
(183100000000000001,'CTF','Web','SQL 注入入门',
 '## 题目描述\n\n登录框存在 SQL 注入漏洞，绕过登录获取 admin 的 flag。\n\n**提示**\n- 尝试万能密码 `'' or 1=1#`\n- 观察报错信息\n\n**难度** ⭐',
 NULL,'flag{sql_injection_101}',100,'EASY','["SQL注入","Web","基础"]',
 '## Writeup\n\n用户名字段输入 `'' or 1=1#`，密码任意，即可绕过登录。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/web-sql-easy:latest","exposePort":80,"cpuLimit":0.5,"memoryLimit":256}',0,NOW(),NOW()),

(183100000000000002,'CTF','Web','文件上传绕过后缀',
 '## 题目描述\n\n上传头像功能存在黑名单绕过漏洞，上传 webshell 读取 flag。\n\n**提示**\n- 黑名单仅过滤 `.php`\n- 试试 `.phtml` / `.php5` / 大小写\n\n**难度** ⭐⭐',
 NULL,'flag{upload_bypass_phtml}',200,'MEDIUM','["文件上传","Web","绕过"]',
 '## Writeup\n\n将 `shell.php` 改名为 `shell.phtml` 上传，访问 `http://<host>/upload/shell.phtml`，`?cmd=cat /flag`。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/web-upload:latest","exposePort":80,"cpuLimit":0.5,"memoryLimit":256}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),writeup=VALUES(writeup),ext=VALUES(ext),status=1;

-- ===== Crypto =====
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,tags,writeup,status,ext,deleted,created_at,updated_at) VALUES
(183100000000000003,'CTF','Crypto','RSA 基础入门',
 '## 题目描述\n\n已知 RSA 公钥参数与密文，求明文 flag。\n\n```\nn = 0x... (略)\ne = 65537\nc = 0x... (略)\n```\n\n**提示**\n- 尝试分解 n\n- 参考附件 `rsa_challenge.txt`\n\n**难度** ⭐⭐',
 NULL,'flag{rsa_basic_decrypt}',100,'EASY','["RSA","Crypto","数学"]',
 '## Writeup\n\n用 `factordb` 或 `yafu` 分解 n 得 p,q，计算 d = inverse(e, (p-1)(q-1))，m = pow(c,d,n)，long_to_bytes 得 flag。',1,
 '{"challengeType":"STATIC_ATTACHMENT","attachment":"/attachments/rsa_challenge.txt"}',0,NOW(),NOW()),

(183100000000000004,'CTF','Crypto','AES-CBC 字节翻转',
 '## 题目描述\n\n服务使用 AES-CBC 加密，密钥未知但已知 IV 可控，目标是让解密后的 `admin=0` 变为 `admin=1`。\n\n**提示**\n- CBC 模式下前一密文块影响后一明文块\n- 翻转前一块对应字节即可\n\n**难度** ⭐⭐⭐',
 NULL,'flag{aes_cbc_bitflip}',200,'MEDIUM','["AES","CBC","字节翻转"]',
 '## Writeup\n\nCBC 解密满足 P_i = D(C_i) XOR C_{i-1}。要改 P_i 中某字节，只需修改 C_{i-1} 中相同位置字节，与目标差异异或即可。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/crypto-aes:latest","exposePort":8080,"cpuLimit":0.5,"memoryLimit":256}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),writeup=VALUES(writeup),ext=VALUES(ext),status=1;

-- ===== Misc =====
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,tags,writeup,status,ext,deleted,created_at,updated_at) VALUES
(183100000000000005,'CTF','Misc','图片隐写入门',
 '## 题目描述\n\n附件是一张 PNG 图片，flag 藏在图片中。\n\n**提示**\n- `binwalk` 看附加数据\n- `strings` 搜 `flag`\n- `stegsolve` 逐通道查看\n\n**难度** ⭐',
 NULL,'flag{stego_png_lsb}',100,'EASY','["隐写","Misc","图片","LSB"]',
 '## Writeup\n\n`binwalk -e image.png` 或 `zsteg image.png`，可发现 LSB 中藏有 flag。',1,
 '{"challengeType":"STATIC_ATTACHMENT","attachment":"/attachments/stego_challenge.txt"}',0,NOW(),NOW()),

(183100000000000006,'CTF','Misc','流量分析 HTTP 回传',
 '## 题目描述\n\n分析 pcap 流量包，找到攻击者回传的 flag。\n\n**提示**\n- Wireshark 过滤 `http`\n- 关注 POST 请求与响应体\n\n**难度** ⭐⭐',
 NULL,'flag{pcap_http_exfil}',200,'MEDIUM','["流量分析","Misc","Wireshark","pcap"]',
 '## Writeup\n\nWireshark 过滤 `http.request.method==POST`，追踪 TCP 流，在响应中找到 `flag{pcap_http_exfil}`。',1,
 '{"challengeType":"STATIC_ATTACHMENT","attachment":"/attachments/traffic_challenge.txt"}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),writeup=VALUES(writeup),ext=VALUES(ext),status=1;

-- ===== Reverse =====
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,tags,writeup,status,ext,deleted,created_at,updated_at) VALUES
(183100000000000007,'CTF','Reverse','CrackMe 静态分析',
 '## 题目描述\n\n一个 ELF 程序要求输入正确密码，输对后输出 flag。\n\n**提示**\n- `strings` / IDA 看主逻辑\n- 密码校验通常是逐字节比较\n\n**难度** ⭐',
 NULL,'flag{crackme_static_pass}',100,'EASY','["逆向","Reverse","CrackMe"]',
 '## Writeup\n\nIDA 打开，定位 `main`，找到比较函数，提取硬编码字符串即为密码，输入后程序打印 flag。',1,
 '{"challengeType":"STATIC_ATTACHMENT","attachment":"/attachments/crackme_challenge.txt"}',0,NOW(),NOW()),

(183100000000000008,'CTF','Reverse','UPX 脱壳 + 算法还原',
 '## 题目描述\n\n附件为 UPX 加壳的 ELF，脱壳后分析校验算法。\n\n**提示**\n- `upx -d` 脱壳\n- 关注循环中的异或运算\n\n**难度** ⭐⭐',
 NULL,'flag{upx_unpack_xor_alg}',200,'MEDIUM','["加壳","Reverse","UPX","异或"]',
 '## Writeup\n\n`upx -d crackme` 脱壳，IDA 中找到异或循环，反向写出解密脚本即可得到 flag。',1,
 '{"challengeType":"STATIC_ATTACHMENT","attachment":"/attachments/elf_challenge.txt"}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),writeup=VALUES(writeup),ext=VALUES(ext),status=1;

-- ===== Pwn =====
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,tags,writeup,status,ext,deleted,created_at,updated_at) VALUES
(183100000000000009,'CTF','Pwn','栈溢出 ret2text',
 '## 题目描述\n\n程序存在栈溢出，且内置 `backdoor()` 函数会读 flag。\n\n**提示**\n- 计算偏移到返回地址\n- 覆盖为 `backdoor` 地址\n\n**难度** ⭐',
 NULL,'flag{ret2text_basic}',150,'EASY','["栈溢出","Pwn","ret2text"]',
 '## Writeup\n\n`cyclic` 定位偏移，`p64(backdoor_addr)` 拼接，发送 payload 后拿到 shell 或直接读 flag。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/pwn-bof:latest","exposePort":9999,"cpuLimit":0.5,"memoryLimit":256}',0,NOW(),NOW()),

(1831000000000000001,'CTF','Pwn','格式化字符串泄露',
 '## 题目描述\n\n程序存在格式化字符串漏洞，flag 在栈上，通过 `%p` 泄露。\n\n**提示**\n- 寻找 flag 在栈上的偏移\n- `%s` 可读取任意地址\n\n**难度** ⭐⭐',
 NULL,'flag{format_string_leak}',250,'MEDIUM','["格式化字符串","Pwn","泄露"]',
 '## Writeup\n\n输入 `%8$p.%9$p...` 定位栈上 flag 地址，构造 payload 读取。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/pwn-fmt:latest","exposePort":9999,"cpuLimit":0.5,"memoryLimit":256}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),writeup=VALUES(writeup),ext=VALUES(ext),status=1;

-- ===== Blockchain =====
INSERT INTO question (id,bank_type,category,title,content,answer,flag,score,difficulty,tags,writeup,status,ext,deleted,created_at,updated_at) VALUES
(1831000000000000002,'CTF','Blockchain','重入攻击',
 '## 题目描述\n\n合约提款前未更新余额，存在重入漏洞，请清空合约余额获取 flag。\n\n**提示**\n- `fallback` 中再次调用 `withdraw`\n- Checks-Effects-Interactions 原则\n\n**难度** ⭐⭐⭐',
 NULL,'flag{reentrancy_attack}',300,'HARD','["智能合约","Blockchain","重入"]',
 '## Writeup\n\n部署攻击合约，在 `fallback` 中递归调用 `withdraw`，直到合约余额为 0，触发 `getFlag()`。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/blockchain-contract:latest","exposePort":8545,"cpuLimit":0.5,"memoryLimit":512}',0,NOW(),NOW()),

(1831000000000000003,'CTF','Blockchain','整数下溢',
 '## 题目描述\n\n旧版 Solidity 合约 `transfer` 未做下溢检查，利用它给自己加余额。\n\n**提示**\n- `balance - amount` 下溢成极大值\n- 版本 < 0.8.0\n\n**难度** ⭐⭐',
 NULL,'flag{integer_underflow}',250,'MEDIUM','["智能合约","Blockchain","整数溢出"]',
 '## Writeup\n\n调用 `transfer(attacker, 1)` 当余额为 0 时，`0-1` 下溢为 `2^256-1`，攻击者余额暴涨。',1,
 '{"challengeType":"STATIC_CONTAINER","imageName":"csp/blockchain-contract:latest","exposePort":8545,"cpuLimit":0.5,"memoryLimit":512}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),flag=VALUES(flag),score=VALUES(score),difficulty=VALUES(difficulty),writeup=VALUES(writeup),ext=VALUES(ext),status=1;


-- ----------------------------------------------------------------
-- 二、理论题库（分方向单选题，供 TheoryController 组卷）
-- bank_type = THEORY, answer 为标准答案
-- id 段: 9320xxxxxxxxxxxxxxx
-- ----------------------------------------------------------------
INSERT INTO question (id,bank_type,category,title,content,answer,score,difficulty,tags,status,ext,deleted,created_at,updated_at) VALUES
-- Web 理论
(183200000000000001,'THEORY','Web','SQL 注入防御最佳实践',
 '下列哪项能最有效地防御 SQL 注入？\n\nA. 前端输入长度限制\nB. 使用参数化查询（预编译语句）\nC. 隐藏数据库报错\nD. 仅过滤单引号',
 'B',10,'EASY','["SQL注入","防御"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(183200000000000002,'THEORY','Web','XSS 类型辨析',
 '反射型 XSS 与存储型 XSS 的核心区别是？\n\nA. 是否使用 JavaScript\nB. 恶意脚本是否被持久化存储\nC. 是否影响 Cookie\nD. 是否绕过 CSP',
 'B',10,'EASY','["XSS","分类"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(183200000000000003,'THEORY','Web','CSRF 与 CORS',
 '防御 CSRF 最常用的手段是？\n\nA. 设置 HttpOnly\nB. 校验 CSRF Token / SameSite Cookie\nC. 使用 HTTPS\nD. 输入过滤',
 'B',10,'MEDIUM','["CSRF","防御"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),

-- Crypto 理论
(183200000000000004,'THEORY','Crypto','对称与非对称加密',
 '下列属于非对称加密算法的是？\n\nA. AES\nB. DES\nC. RSA\nD. RC4',
 'C',10,'EASY','["加密算法","分类"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(183200000000000005,'THEORY','Crypto','哈希函数特性',
 '关于哈希函数，下列说法错误的是？\n\nA. 输出定长\nB. 抗碰撞性\nC. 可逆还原原文\nD. 雪崩效应',
 'C',10,'EASY','["哈希","特性"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(183200000000000006,'THEORY','Crypto','RSA 安全性基础',
 'RSA 的安全性主要基于哪一数学难题？\n\nA. 离散对数\nB. 大整数分解\nC. 椭圆曲线离散对数\nD. 格基约减',
 'B',10,'MEDIUM','["RSA","数学基础"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),

-- Misc 理论
(183200000000000007,'THEORY','Misc','隐写与加密区别',
 '隐写术（Steganography）的主要目标是？\n\nA. 使数据不可读\nB. 隐藏数据的存在性\nC. 压缩数据\nD. 校验数据完整性',
 'B',10,'EASY','["隐写","概念"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(183200000000000008,'THEORY','Misc','流量分析工具',
 '分析 pcap 流量包最常用的工具是？\n\nA. Nmap\nB. Wireshark\nC. Burp Suite\nD. Metasploit',
 'B',10,'EASY','["流量分析","工具"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),

-- Reverse 理论
(183200000000000009,'THEORY','Reverse','常见加壳与脱壳',
 'UPX 属于哪类工具/技术？\n\nA. 反编译工具\nB. 可执行文件压缩壳\nC. 调试器\nD. 静态分析框架',
 'B',10,'EASY','["UPX","加壳"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(1832000000000000001,'THEORY','Reverse','静态与动态分析',
 '关于动态分析，下列描述正确的是？\n\nA. 不运行程序即可分析\nB. 在程序运行时观察寄存器/内存\nC. 只能分析源码\nD. 无需调试器',
 'B',10,'EASY','["动态分析","调试"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),

-- Pwn 理论
(1832000000000000002,'THEORY','Pwn','栈溢出与保护机制',
 '现代二进制防护中，用于检测栈溢出的机制是？\n\nA. ASLR\nB. Canary（栈保护）\nC. NX\nD. PIE',
 'B',10,'MEDIUM','["栈保护","Canary"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(1832000000000000003,'THEORY','Pwn','ROP 技术原理',
 'ROP（Return-Oriented Programming）主要用来绕过哪一防护？\n\nA. Canary\nB. NX（不可执行栈）\nC. ASLR\nD. 全部',
 'B',10,'HARD','["ROP","NX"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),

-- 安全法规/意识 理论
(1832000000000000004,'THEORY','Security','网络安全法基本要求',
 '《中华人民共和国网络安全法》规定，网络运营者应当履行的安全保护义务不包括？\n\nA. 制定网络安全事件应急预案\nB. 采取防范计算机病毒的技术措施\nC. 对用户个人信息严格保密\nD. 允许任何第三方自由访问用户数据',
 'D',10,'EASY','["网络安全法","合规"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW()),
(1832000000000000005,'THEORY','Security','密码强度与存储',
 '存储用户口令时，最推荐的做法是？\n\nA. 明文存储\nB. MD5 存储\nC. 加盐 + 慢哈希（如 bcrypt/argon2）\nD. Base64 编码',
 'C',10,'MEDIUM','["口令存储","哈希"]',1,'{"theoryType":"SINGLE"}',0,NOW(),NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),answer=VALUES(answer),score=VALUES(score),difficulty=VALUES(difficulty),status=1;

SELECT '23_seed_classic_challenges.sql 执行完成' AS msg,
       (SELECT COUNT(*) FROM question WHERE id BETWEEN 183100000000000001 AND 9310000000000000012) AS ctf_cnt,
       (SELECT COUNT(*) FROM question WHERE id BETWEEN 183200000000000001 AND 9320000000000000014) AS theory_cnt;