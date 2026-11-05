#!/bin/sh
# 模拟内核模块靶场：保留 /proc 风格的读写与 exploit 流程
FLAG="${FLAG:-flag{kernel_pwn_demo}}"
printf '%s\n' "$FLAG" > /flag.txt

cat <<'EOF'
=== Kernel Lab ===
device: /proc/csp_vuln  (ioctl 0x1337 write / 0x1338 read)
commands: read | write <hex> | exploit | help | quit
EOF

while IFS= read -r line; do
  case "$line" in
    read)
      echo "csp_vuln: 0x0000000000000000 (size=0x1000, refcount=1)"
      ;;
    write*)
      echo "ioctl(0x1337) ok: heap object overwritten, refcount -> 0"
      ;;
    exploit)
      echo "[+] refcount 归零后释放，抢占同一 slab"
      echo "[+] root shell acquired"
      echo "[+] flag:"
      cat /flag.txt
      ;;
    help)
      echo "read | write <hex> | exploit | quit"
      ;;
    quit|exit)
      break
      ;;
    "")
      ;;
    *)
      echo "unknown command: $line"
      ;;
  esac
done
