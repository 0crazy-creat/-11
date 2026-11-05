#!/bin/bash
# 修复 5 个存在历史缺陷的原靶场镜像：保留 :orig 备份后用同 tag 重建，题库引用无需改动
set -e
cd "$(dirname "$0")"

backup() {
  local repo="$1"
  if ! docker image inspect "${repo}:orig" >/dev/null 2>&1; then
    echo "==> 备份 ${repo}:latest -> ${repo}:orig"
    docker tag "${repo}:latest" "${repo}:orig"
  else
    echo "==> 已存在 ${repo}:orig，跳过备份"
  fi
}

build() {
  local dockerfile="$1"
  local repo="$2"
  echo "==> 重建 ${repo}:latest  (${dockerfile})"
  docker build -f "${dockerfile}" -t "${repo}:latest" .
}

backup csp/pwn-bof
backup csp/pwn-fmt
backup csp/web-sql-easy
backup csp/blockchain-contract
backup csp/misc-forensics

build Dockerfile.fix-pwn-bof              csp/pwn-bof
build Dockerfile.fix-pwn-fmt             csp/pwn-fmt
build Dockerfile.fix-web-sql-easy        csp/web-sql-easy
build Dockerfile.fix-blockchain-contract csp/blockchain-contract
build Dockerfile.fix-misc-forensics      csp/misc-forensics

echo
echo "==> 完成。如需回滚：docker tag <repo>:orig <repo>:latest"
