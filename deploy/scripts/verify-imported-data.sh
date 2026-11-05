#!/usr/bin/env bash
# 旧版把 CTF/理论/场景混在一场赛事里且不推进状态，必然失败。
# 现统一使用按赛制分离、含状态机推进与报名的完整端到端验证脚本。
# Windows Git Bash 里 python3 常是微软商店空壳（执行后无输出），必须优先取 python。
set -e
cd "$(dirname "$0")/../.."
PY="$(command -v python || command -v python3)"
exec "$PY" deploy/scripts/e2e_verify.py "$@"
