# Orca 存档脚本 — 删除 worktree 时自动执行 (PowerShell)
# 环境变量: $env:ORCA_ROOT_PATH, $env:ORCA_WORKTREE_PATH, $env:ORCA_WORKSPACE_NAME

$ErrorActionPreference = "Continue"

Write-Host "[1/5] 解析数据库名..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = "erp_sys_$Branch"
Write-Host "       分支: $env:ORCA_WORKSPACE_NAME"
Write-Host "       数据库: $DB_NAME"

Write-Host "[2/5] 安全检查：验证远程分支..."
Set-Location $env:ORCA_ROOT_PATH
if ($env:ORCA_WORKSPACE_NAME -eq "main" -or $env:ORCA_WORKSPACE_NAME -eq "master") {
    Write-Host "       ⚠️  拒绝删除主分支"
    exit 1
}
$remoteHead = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
if (-not $remoteHead) {
    Write-Host "       ❌ 分支未推送到远程，拒绝删除！"
    Write-Host "       请先: git push origin $env:ORCA_WORKSPACE_NAME"
    exit 1
}
Write-Host "       ✅ 远程分支已确认"

Write-Host "[3/5] 删除数据库 $DB_NAME ..."
cmd /c "mysql -u root -proot -e `"DROP DATABASE IF EXISTS ``$DB_NAME`;`" 2>&1"
if ($LASTEXITCODE -ne 0) { Write-Host "       ❌ 删除失败"; exit 1 }
Write-Host "       ✅ 数据库已删除"

Write-Host "[4/5] 删除本地分支 $env:ORCA_WORKSPACE_NAME ..."
git branch -D $env:ORCA_WORKSPACE_NAME 2>$null
if ($LASTEXITCODE -eq 0) { Write-Host "       ✅ 已删除" } else { Write-Host "       ⚠️  未找到（可能已删）" }

Write-Host "[5/5] 删除远程分支 origin/$env:ORCA_WORKSPACE_NAME ..."
git push origin --delete $env:ORCA_WORKSPACE_NAME 2>$null
if ($LASTEXITCODE -eq 0) { Write-Host "       ✅ 已删除" } else { Write-Host "       ⚠️  删除失败（检查网络）" }

Write-Host "============================================"
Write-Host "  ✅ 清理完成 | 数据库/本地/远程分支均已删除"
Write-Host "============================================"

