# Orca 存档脚本 — 删除 worktree 时自动执行 (PowerShell)
# 环境变量: $env:ORCA_ROOT_PATH, $env:ORCA_WORKTREE_PATH, $env:ORCA_WORKSPACE_NAME

$ErrorActionPreference = "Stop"
$ScriptStartTime = Get-Date

# 全局异常捕获
trap {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  ❌ Orca Archive 脚本执行失败！" -ForegroundColor Red
    Write-Host "  错误: $_" -ForegroundColor Red
    Write-Host "  位置: $($_.InvocationInfo.ScriptLineNumber) 行" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    exit 1
}

# 启动横幅
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Orca Archive 开始执行" -ForegroundColor Cyan
Write-Host "  时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "  分支: $env:ORCA_WORKSPACE_NAME" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# 颜色辅助函数
# ============================================================
function Write-OK    { Write-Host "       ✅ $args" -ForegroundColor Green }
function Write-Warn  { Write-Host "       ⚠️  $args" -ForegroundColor Yellow }
function Write-Err   { Write-Host "       ❌ $args" -ForegroundColor Red; Write-Host "       错误详情: $args" }
function Write-Info  { Write-Host "       ℹ️  $args" -ForegroundColor Cyan }
function Write-Step  { param([int]$N,[int]$T,[string]$M) Write-Host "[$N/$T] $M" -ForegroundColor White }

# ============================================================
# 团队共享配置（与 orca-setup.ps1 保持一致）
# ============================================================
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }
$ProtectedBranches = @("main", "master")

# ============================================================
# 步骤 1: 解析分支/数据库名
# ============================================================
Write-Step 1 6 "解析数据库名..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch
Write-Info "分支: $env:ORCA_WORKSPACE_NAME"
Write-Info "数据库: $DB_NAME"

# ============================================================
# 步骤 2: 安全检查
# ============================================================
Write-Step 2 6 "安全检查..."

# --- 2.1 主分支保护 ---
if ($ProtectedBranches -contains $env:ORCA_WORKSPACE_NAME) {
    Write-Err "禁止删除受保护分支 ($($Protected -join '/'))！"
    exit 1
}
Write-OK "非主分支，允许删除"

# --- 2.2 验证远程分支存在（防止误删未推送的分支）---
Push-Location $env:ORCA_ROOT_PATH
try {
    Write-Info "确认远程分支 origin/$env:ORCA_WORKSPACE_NAME 存在..."
    $remoteHead = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
    if (-not $remoteHead) {
        Write-Err "远程分支 origin/$env:ORCA_WORKSPACE_NAME 不存在！"
        Write-Err "分支可能从未推送。请先推送或确认分支名是否正确："
        Write-Err "  git push origin $env:ORCA_WORKSPACE_NAME"
        exit 1
    }
    Write-OK "远程分支已确认存在"

    # --- 2.3 检查本地是否有未推送的提交 ---
    Write-Info "检查本地未推送提交..."
    $localBranch = git branch --list $env:ORCA_WORKSPACE_NAME 2>$null
    if ($localBranch) {
        $unpushed = git log origin/$env:ORCA_WORKSPACE_NAME..$env:ORCA_WORKSPACE_NAME --oneline 2>$null
        if ($unpushed) {
            Write-Err "本地分支 $env:ORCA_WORKSPACE_NAME 有未推送的提交："
            $unpushed | ForEach-Object { Write-Err "  $_" }
            Write-Err "请先推送后再删除：git push origin $env:ORCA_WORKSPACE_NAME"
            exit 1
        }
    }
    Write-OK "无未推送提交"
} finally { Pop-Location }

# ============================================================
# 步骤 3: 删除数据库
# ============================================================
Write-Step 3 6 "删除数据库 $DB_NAME ..."

# --- 3.1 确认数据库存在 ---
$dbCheck = cmd /c "mysql -u $DB_USER -p$DB_PASSWORD -e `"SHOW DATABASES LIKE '$DB_NAME';`" 2>&1"
if ($dbCheck -match $DB_NAME) {
    Write-Info "数据库 $DB_NAME 存在，正在删除..."
} else {
    Write-Warn "数据库 $DB_NAME 不存在，跳过删除"
}

# --- 3.2 执行 DROP ---
cmd /c "mysql -u $DB_USER -p$DB_PASSWORD -e `"DROP DATABASE IF EXISTS ``$DB_NAME``;`" 2>&1"
if ($LASTEXITCODE -ne 0) {
    Write-Err "数据库删除失败 (exit code: $LASTEXITCODE)"
    exit 1
}
Write-OK "数据库 $DB_NAME 已删除"

# ============================================================
# 步骤 4: 删除远程分支
# ============================================================
Write-Step 4 6 "删除远程分支 origin/$env:ORCA_WORKSPACE_NAME ..."
Push-Location $env:ORCA_ROOT_PATH
try {
    git push origin --delete $env:ORCA_WORKSPACE_NAME 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-OK "远程分支已删除"
    } else {
        Write-Warn "远程分支删除失败，请检查网络或分支权限"
    }
} finally { Pop-Location }

# ============================================================
# 步骤 5: 删除本地分支
# ============================================================
Write-Step 5 6 "删除本地分支 $env:ORCA_WORKSPACE_NAME ..."
Push-Location $env:ORCA_ROOT_PATH
try {
    # 5.1 检查本地分支是否存在
    $localExists = git branch --list $env:ORCA_WORKSPACE_NAME 2>$null
    if (-not $localExists) {
        Write-Warn "本地分支不存在，跳过"
    } else {
        # 5.2 如果当前在该分支上，先切走
        $currentBranch = git branch --show-current 2>$null
        if ($currentBranch -eq $env:ORCA_WORKSPACE_NAME) {
            Write-Info "当前位于待删除分支，切换到 main..."
            git checkout main 2>$null
            if ($LASTEXITCODE -ne 0) {
                git checkout master 2>$null
            }
        }
        # 5.3 强制删除本地分支
        git branch -D $env:ORCA_WORKSPACE_NAME 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-OK "本地分支已删除"
        } else {
            Write-Warn "本地分支删除失败（可能已被工作树锁定，删除工作树后自动消除）"
        }
    }
} finally { Pop-Location }

# ============================================================
# 步骤 6: 清理残留信息
# ============================================================
Write-Step 6 6 "清理残留信息..."

$hasResidual = $false

# --- 6.1 清理 worktree 内的 .env.branch ---
$branchEnvFile = "$env:ORCA_WORKTREE_PATH\.env.branch"
if (Test-Path $branchEnvFile) {
    Remove-Item $branchEnvFile -Force
    Write-OK "已删除 .env.branch ($branchEnvFile)"
    $hasResidual = $true
}

# --- 6.2 清理 worktree 内复制的敏感配置 ---
$resLocalYml = "$env:ORCA_WORKTREE_PATH\ruoyi-admin\src\main\resources\application-dev-local.yml"
if (Test-Path $resLocalYml) {
    Remove-Item $resLocalYml -Force
    Write-OK "已删除 application-dev-local.yml (worktree 副本)"
    $hasResidual = $true
}

$uiLocalEnv = "$env:ORCA_WORKTREE_PATH\plus-ui\env\.env.development.local"
if (Test-Path $uiLocalEnv) {
    Remove-Item $uiLocalEnv -Force
    Write-OK "已删除 .env.development.local (worktree 副本)"
    $hasResidual = $true
}

# --- 6.3 检查主仓库中是否还有该分支的跟踪引用 ---
Push-Location $env:ORCA_ROOT_PATH
try {
    $pruneResult = git remote prune origin --dry-run 2>$null
    if ($pruneResult) {
        Write-Info "检测到远程已删但本地仍缓存的分支引用，执行清理..."
        git remote prune origin 2>$null
        Write-OK "已清理过期远程引用"
        $hasResidual = $true
    }
} finally { Pop-Location }

# --- 6.4 检查是否有孤儿 worktree 残留 ---
Push-Location $env:ORCA_ROOT_PATH
try {
    $worktreeList = git worktree list 2>$null
    if ($worktreeList -match $env:ORCA_WORKSPACE_NAME) {
        Write-Warn "检测到工作树仍存在于 git worktree list 中"
        Write-Info "Orca 将在删除工作树目录后自动清理此记录"
        $hasResidual = $true
    }
} finally { Pop-Location }

if (-not $hasResidual) {
    Write-OK "无残留信息"
}

# ============================================================
# 最终总结
# ============================================================
Write-Host ''
Write-Host "============================================" -ForegroundColor Green
Write-Host "  ✅ Orca 工作空间清理完成" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "  已删除数据库 : $DB_NAME"
Write-Host "  已删除远程分支 : origin/$env:ORCA_WORKSPACE_NAME"
Write-Host "  已清理本地分支及残留文件"
Write-Host "============================================" -ForegroundColor Green
