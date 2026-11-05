#!/bin/bash
# 构建题库中引用但缺失的 7 个靶场镜像
# 用法: bash dev/images/build-missing.sh
set -e

cd "$(dirname "$0")"

build() {
  local dockerfile="$1"
  local image="$2"
  echo "==> building ${image}  (${dockerfile})"
  docker build -f "${dockerfile}" -t "${image}" .
}

build Dockerfile.ctf-web-upload        ctf/web-upload:latest
build Dockerfile.ctf-pwn-stack         ctf/pwn-stack:latest
build Dockerfile.ctf-pwn-format        ctf/pwn-format:latest
build Dockerfile.ctf-pwn-heap          ctf/pwn-heap:latest
build Dockerfile.ctf-pwn-kernel        ctf/pwn-kernel:latest
build Dockerfile.csp-crypto-aes        csp/crypto-aes:latest
build Dockerfile.csp-web-dynamic-test  csp/web-dynamic-test:latest

echo
echo "==> 构建完成"
docker images --format '{{.Repository}}:{{.Tag}}\t{{.Size}}' | grep -E '^(ctf/|csp/)' | sort
