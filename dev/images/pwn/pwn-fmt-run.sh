#!/bin/bash
# 格式化字符串靶场：flag 优先取平台注入的 FLAG，落到 /flag.txt 与 /tmp/flag.txt
gcc /tmp/vuln.c -o /tmp/vuln -fno-stack-protector

FLAG_VALUE="${FLAG:-flag{pwn_fmt_challenge_2026}}"
printf '%s\n' "$FLAG_VALUE" > /flag.txt
cp /flag.txt /tmp/flag.txt
chmod 444 /flag.txt /tmp/flag.txt

exec /usr/sbin/xinetd -dontfork -filelog /dev/stdout
