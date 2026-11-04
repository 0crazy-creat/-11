#!/bin/sh
set -e

# 注入 flag：写进 nginx 配置作为响应头（正确语法），并在页面暴露一个查询接口
if [ -n "$FLAG" ]; then
  cat > /etc/nginx/conf.d/flag.conf <<NGINX
add_header X-Target-Flag "$FLAG" always;
NGINX
  # 同时落一个可读取的 flag 文件，供 /flag 路径回显
  printf '%s' "$FLAG" > /usr/share/nginx/html/flag.txt
fi

exec nginx -g "daemon off;"
