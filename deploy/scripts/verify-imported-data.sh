#!/usr/bin/env bash
# 已废弃：旧版把 CTF/理论/场景混在一场赛事里且不推进状态，必然失败。
# 现统一使用按赛制分离、含状态机推进与报名的完整端到端验证脚本。
set -e
cd "$(dirname "$0")/../.."
exec python3 deploy/scripts/e2e_verify.py "$@"
