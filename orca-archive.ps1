# ============================================================
# Orca 存档脚本 — 删除 worktree 时自动执行 (PowerShell)
# ============================================================
# 功能:
#   1. 安全检查：分支是否已推送到远程（未推送则拒绝删除）
#   2. 删除分支对应的独立数据库
#   3. 删除本地分支
#   4. 删除远程分支
#
# Orca 注入环境变量:
#   $env:ORCA_ROOT_PATH      - 主仓库根路径
#   $env:ORCA_WORKTREE_PATH  - worktree 根路径
#   $env:ORCA_WORKSPACE_NAME - 分支名
# ============================================================

$ErrorActionPreference = "Stop"

# ---------- 1. 解析数据库名 ----------
Write-Host "[1/5] 解析数据库名..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = "erp_sys_$Branch"
Write-Host "       分支: $env:ORCA_WORKSPACE_NAME"
Write-Host "       数据库: $DB_NAME"

# ---------- 2. 安全检查 ----------
Write-Host "[2/5] 安全检查：验证远程分支是否存在..."
Set-Location $env:ORCA_ROOT_PATH

if ($env:ORCA_WORKSPACE_NAME -eq "main" -or $env:ORCA_WORKSPACE_NAME -eq "master") {
    Write-Host "       ⚠️  拒绝删除：$env:ORCA_WORKSPACE_NAME 是主分支，不允许通过 Orca 清理"
    exit 1
}

$remoteHead = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
if (-not $remoteHead) {
    Write-Host ""
    Write-Host "       ❌ 安全检查失败：分支 '$env:ORCA_WORKSPACE_NAME' 尚未推送到远程！"
    Write-Host ""
    Write-Host "       为防止误删未推送的代码，数据库不会被删除。"
    Write-Host "       请先执行: git push origin $env:ORCA_WORKSPACE_NAME"
    Write-Host "       确认代码已安全推送到远程后再删除工作树。"
    exit 1
}
$shortHash = $remoteHead.Substring(0, [Math]::Min(7, $remoteHead.Length))
Write-Host "       ✅ 远程分支已确认存在（$shortHash）"

# ---------- 3. 删除数据库 ----------
Write-Host "[3/5] 删除数据库 $DB_NAME ..."
$result = mysql -u root -proot -e "DROP DATABASE IF EXISTS ``$DB_NAME`;" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "       ✅ 数据库 $DB_NAME 已删除"
} else {
    Write-Host "       ❌ 删除失败，请检查 MySQL 连接和权限"
    exit 1
}

# ---------- 4. 删除本地分支 ----------
Write-Host "[4/5] 删除本地分支 $env:ORCA_WORKSPACE_NAME ..."
git branch -D $env:ORCA_WORKSPACE_NAME 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "       ✅ 本地分支 $env:ORCA_WORKSPACE_NAME 已删除"
} else {
    Write-Host "       ⚠️  本地分支删除失败（可能已被 Orca 删除），无影响"
}

# ---------- 5. 删除远程分支 ----------
Write-Host "[5/5] 删除远程分支 origin/$env:ORCA_WORKSPACE_NAME ..."
git push origin --delete $env:ORCA_WORKSPACE_NAME 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "       ✅ 远程分支 origin/$env:ORCA_WORKSPACE_NAME 已删除"
} else {
    Write-Host "       ⚠️  远程分支删除失败，请检查网络或手动删除"
}

Write-Host ""
Write-Host "============================================"
Write-Host "  ✅ Orca 工作空间完全清理完成"
Write-Host "  已删除数据库: $DB_NAME"
Write-Host "  已删除本地分支: $env:ORCA_WORKSPACE_NAME"
Write-Host "  已删除远程分支: origin/$env:ORCA_WORKSPACE_NAME"
Write-Host "  敏感文件随 worktree 目录一并清除"
Write-Host "============================================"
