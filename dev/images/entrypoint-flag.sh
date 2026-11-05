#!/bin/sh
# 统一入口：把平台注入的 FLAG 落到 /flag.txt，供靶场内读取，然后交回原启动命令
if [ -n "$FLAG" ] && [ ! -f /flag.txt ]; then
  printf '%s\n' "$FLAG" > /flag.txt 2>/dev/null || true
fi
exec "$@"
