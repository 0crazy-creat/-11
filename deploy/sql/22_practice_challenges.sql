-- ================================================================
-- 练习题数据：CTF 各分类典型题目
-- ================================================================

-- Web 分类
INSERT INTO question (id, bank_type, category, title, content, difficulty, score, tags, flag, ext) VALUES
(900001, 'CTF', 'Web', 'SQL注入入门', '## 题目描述\n\n请找到数据库中的flag。\n\n**提示**: 经典的SQL注入漏洞\n\n**难度**: ⭐', 'EASY', 100, '["SQL注入","Web","基础"]', 'flag{sql_injection_is_fun}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900002, 'CTF', 'Web', 'XSS反射型', '## 题目描述\n\n找到管理员cookie中的flag。\n\n**提示**: 反射型XSS\n\n**难度**: ⭐⭐', 'MEDIUM', 200, '["XSS","Web","Cookie"]', 'flag{xss_reflected_cookie}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900003, 'CTF', 'Web', '文件上传漏洞', '## 题目描述\n\n上传一个webshell获取flag。\n\n**提示**: 绕过文件上传检查\n\n**难度**: ⭐⭐⭐', 'HARD', 300, '["文件上传","Web","Shell"]', 'flag{upload_webshell_success}', '{"challengeType":"STATIC_CONTAINER","imageName":"ctf/web-upload","port":80}'),

(900004, 'CTF', 'Web', 'SSRF漏洞', '## 题目描述\n\n利用SSRF读取内网flag文件。\n\n**提示**: file:// 协议\n\n**难度**: ⭐⭐', 'MEDIUM', 250, '["SSRF","Web","内网"]', 'flag{ssrf_file_protocol}', '{"challengeType":"STATIC_ATTACHMENT"}');

-- Misc 分类
INSERT INTO question (id, bank_type, category, title, content, difficulty, score, tags, flag, ext) VALUES
(900005, 'CTF', 'Misc', '隐写术入门', '## 题目描述\n\n图片中隐藏了flag，请找出来。\n\n**提示**: 使用binwalk或stegsolve\n\n**难度**: ⭐', 'EASY', 100, '["隐写","Misc","图片"]', 'flag{stego_hidden_data}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900006, 'CTF', 'Misc', '流量分析', '## 题目描述\n\n分析pcap文件，找到传输的flag。\n\n**提示**: Wireshark过滤HTTP流\n\n**难度**: ⭐⭐', 'MEDIUM', 200, '["流量分析","Misc","Wireshark"]', 'flag{pcap_http_flag}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900007, 'CTF', 'Misc', '压缩包破解', '## 题目描述\n\n破解加密压缩包获取flag。\n\n**提示**: 弱口令爆破\n\n**难度**: ⭐', 'EASY', 150, '["压缩包","Misc","密码"]', 'flag{zip_crack_success}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900008, 'CTF', 'Misc', '内存取证', '## 题目描述\n\n分析内存镜像，找到进程中的flag。\n\n**提示**: Volatility工具\n\n**难度**: ⭐⭐⭐', 'HARD', 350, '["内存取证","Misc","Volatility"]', 'flag{memory_forensics_master}', '{"challengeType":"STATIC_ATTACHMENT"}');

-- Crypto 分类
INSERT INTO question (id, bank_type, category, title, content, difficulty, score, tags, flag, ext) VALUES
(900009, 'CTF', 'Crypto', '凯撒密码', '## 题目描述\n\n解密：synt{prfne_pnrfne}\n\n**提示**: ROT13\n\n**难度**: ⭐', 'EASY', 100, '["凯撒密码","Crypto","古典"]', 'flag{caesar_caesar}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900010, 'CTF', 'Crypto', 'RSA基础', '## 题目描述\n\n已知n,e,c，求明文。\n\n**参数**:\nn = 0x...\ne = 65537\nc = 0x...\n\n**难度**: ⭐⭐', 'MEDIUM', 250, '["RSA","Crypto","数学"]', 'flag{rsa_basic_decrypt}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900011, 'CTF', 'Crypto', 'AES加密', '## 题目描述\n\n解密AES-CBC模式的密文。\n\n**提示**: 注意IV的处理\n\n**难度**: ⭐⭐', 'MEDIUM', 200, '["AES","Crypto","对称加密"]', 'flag{aes_cbc_decrypt}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900012, 'CTF', 'Crypto', 'RSA共模攻击', '## 题目描述\n\n同一消息用不同公钥加密，恢复明文。\n\n**提示**: 扩展欧几里得算法\n\n**难度**: ⭐⭐⭐', 'HARD', 400, '["RSA","Crypto","共模攻击"]', 'flag{rsa_common_modulus}', '{"challengeType":"STATIC_ATTACHMENT"}');

-- Reverse 分类
INSERT INTO question (id, bank_type, category, title, content, difficulty, score, tags, flag, ext) VALUES
(900013, 'CTF', 'Reverse', 'CrackMe入门', '## 题目描述\n\n逆向分析程序，找到正确密码。\n\n**提示**: 使用IDA Pro或Ghidra\n\n**难度**: ⭐', 'EASY', 100, '["逆向","Reverse","CrackMe"]', 'flag{crackme_easy}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900014, 'CTF', 'Reverse', 'UPX加壳', '## 题目描述\n\n脱壳并分析程序逻辑。\n\n**提示**: upx -d 脱壳\n\n**难度**: ⭐⭐', 'MEDIUM', 200, '["加壳","Reverse","UPX"]', 'flag{upx_unpack_success}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900015, 'CTF', 'Reverse', 'Android逆向', '## 题目描述\n\n分析APK文件，找到隐藏的flag。\n\n**提示**: jadx反编译\n\n**难度**: ⭐⭐⭐', 'HARD', 350, '["Android","Reverse","APK"]', 'flag{android_reverse_master}', '{"challengeType":"STATIC_ATTACHMENT"}'),

(900016, 'CTF', 'Reverse', 'OLLVM混淆', '## 题目描述\n\n分析被OLLVM混淆的程序。\n\n**提示**: 动态调试+控制流平坦化\n\n**难度**: ⭐⭐⭐⭐', 'INSANE', 500, '["混淆","Reverse","OLLVM"]', 'flag{ollvm_deobfuscate}', '{"challengeType":"STATIC_ATTACHMENT"}');

-- Pwn 分类
INSERT INTO question (id, bank_type, category, title, content, difficulty, score, tags, flag, ext) VALUES
(900017, 'CTF', 'Pwn', '栈溢出入门', '## 题目描述\n\n利用栈溢出获取shell。\n\n**提示**: 覆盖返回地址\n\n**难度**: ⭐', 'EASY', 150, '["栈溢出","Pwn","基础"]', 'flag{stack_overflow_basic}', '{"challengeType":"STATIC_CONTAINER","imageName":"ctf/pwn-stack","port":9999}'),

(900018, 'CTF', 'Pwn', '格式化字符串', '## 题目描述\n\n利用格式化字符串漏洞读取flag。\n\n**提示**: %x %n 的使用\n\n**难度**: ⭐⭐', 'MEDIUM', 250, '["格式化字符串","Pwn","漏洞"]', 'flag{format_string_attack}', '{"challengeType":"STATIC_CONTAINER","imageName":"ctf/pwn-format","port":9999}'),

(900019, 'CTF', 'Pwn', '堆溢出利用', '## 题目描述\n\n利用堆溢出劫持控制流。\n\n**提示**: unlink攻击\n\n**难度**: ⭐⭐⭐', 'HARD', 400, '["堆溢出","Pwn","unlink"]', 'flag{heap_overflow_unlink}', '{"challengeType":"STATIC_CONTAINER","imageName":"ctf/pwn-heap","port":9999}'),

(900020, 'CTF', 'Pwn', '内核提权', '## 题目描述\n\n利用内核漏洞获取root权限。\n\n**提示**: dirty_cow (CVE-2016-5195)\n\n**难度**: ⭐⭐⭐⭐', 'INSANE', 600, '["内核","Pwn","提权"]', 'flag{kernel_privilege_escalation}', '{"challengeType":"STATIC_CONTAINER","imageName":"ctf/pwn-kernel","port":9999}');

-- 更新题目的解出人数（随机值）
UPDATE question SET ext = JSON_SET(COALESCE(ext, '{}'), '$.solvedCount', FLOOR(10 + RAND() * 100)) WHERE bank_type = 'CTF';
