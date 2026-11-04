# Phase 1 数据库迁移脚本 (PowerShell)
# 执行所有新增表的创建

$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Phase 1 数据库迁移" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 数据库连接配置（根据实际情况修改）
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_USER = "root"
$DB_PASS = "root123"
$DB_NAME = "csp_platform"

Write-Host "数据库: $DB_NAME @ ${DB_HOST}:${DB_PORT}" -ForegroundColor Gray
Write-Host ""

# 迁移脚本列表
$migrations = @(
    "18_abac_policy.sql",
    "19_team_invite_code.sql",
    "20_anti_cheat_deep.sql",
    "21_awd_traffic_capture.sql"
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

foreach ($migration in $migrations) {
    Write-Host "执行迁移: $migration" -ForegroundColor Yellow
    
    $filePath = Join-Path $scriptDir "sql\$migration"
    
    try {
        Get-Content $filePath -Raw | mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME
        Write-Host "✓ $migration 执行成功" -ForegroundColor Green
    } catch {
        Write-Host "✗ $migration 执行失败" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit 1
    }
    Write-Host ""
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "所有迁移执行完成！" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "新增表：" -ForegroundColor White
Write-Host "  - abac_policy"
Write-Host "  - abac_audit_log"
Write-Host "  - contest_team_captain_transfer"
Write-Host "  - contest_team_invite"
Write-Host "  - device_fingerprint"
Write-Host "  - cheat_audit_log"
Write-Host "  - awd_traffic_capture"
Write-Host "  - awd_traffic_analysis"
Write-Host ""
Write-Host "扩展表：" -ForegroundColor White
Write-Host "  - contest_team (新增邀请码字段)"
Write-Host ""
