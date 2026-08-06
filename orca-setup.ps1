# Orca Setup — 创建 worktree 时自动执行
# env: ORCA_ROOT_PATH, ORCA_WORKTREE_PATH, ORCA_WORKSPACE_NAME
param()

$ErrorActionPreference = "Continue"

# 日志函数
function Log { param([string]$Msg) Write-Host $Msg }

Log ""
Log "========== Orca Setup =========="
Log "Time:    $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Log "Branch:  $env:ORCA_WORKSPACE_NAME"
Log "Worktree: $env:ORCA_WORKTREE_PATH"
Log "Root:    $env:ORCA_ROOT_PATH"
Log "=================================="

# ---- 团队共享配置 ----
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }

$LocalConfigPath = if ($env:ORCA_LOCAL_CONFIG) {
    $env:ORCA_LOCAL_CONFIG
} else {
    $ORCA_ROOT = $env:ORCA_ROOT_PATH.TrimEnd('\')
    Join-Path (Split-Path $ORCA_ROOT -Parent) "orca-local-config"
}

Log "DB:  $DB_USER@$DB_HOST`:$DB_PORT"
Log "LocalConfig: $LocalConfigPath"
Log ""

# ---- 步骤 1: 解析分支/数据库名 ----
Log "[1/7] Parse branch name..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch
$IsMain = ($Branch -eq "main" -or $Branch -eq "master")
Log "       DB_NAME = $DB_NAME"
Log "       IsMain  = $IsMain"

# ---- 步骤 2: 前置校验 ----
Log "[2/7] Validate prerequisites..."

# 2.1 Git remote
Push-Location $env:ORCA_ROOT_PATH
$remoteUrl = git remote get-url origin 2>&1
Log "       Git remote: $remoteUrl"

# 2.2 MySQL
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-e", "SELECT 1;"
$mysqlOut = & mysql $mysqlArgs 2>&1
Log "       MySQL test: exit=$LASTEXITCODE"
if ($LASTEXITCODE -ne 0) {
    Log "       ERROR: MySQL check failed: $mysqlOut"

Pop-Location
    exit 1
}

# 2.3 Tools
$gitOk = $null -ne (Get-Command git -ErrorAction SilentlyContinue)
$mysqlOk = $null -ne (Get-Command mysql -ErrorAction SilentlyContinue)
Log "       git=$gitOk mysql=$mysqlOk"
Pop-Location

# ---- 步骤 3: 创建远程分支 ----
Log "[3/7] Create remote branch..."
if ($IsMain) {
    Log "       Main branch, skip."
} else {
    Push-Location $env:ORCA_ROOT_PATH
    $remoteExists = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>&1
    if ($remoteExists) {
        Log "       Remote branch already exists, skip push."
    } else {
        $localBranch = git branch --list $env:ORCA_WORKSPACE_NAME 2>&1
        if (-not $localBranch) {
            git branch $env:ORCA_WORKSPACE_NAME 2>&1
            Log "       Created local branch $env:ORCA_WORKSPACE_NAME"
        }
        $pushOut = git push -u origin $env:ORCA_WORKSPACE_NAME 2>&1
        Log "       Push: exit=$LASTEXITCODE"
        if ($LASTEXITCODE -ne 0) {
            Log "       WARN: Push may have failed (branch might already exist): $pushOut"
        } else {
            Log "       Remote branch created."
        }
    }
    Pop-Location
}

# ---- 步骤 4: 创建数据库 ----
Log "[4/7] Create database $DB_NAME..."
$createDbSql = "CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "--default-character-set=utf8mb4", "-e", $createDbSql
$dbOut = & mysql $mysqlArgs 2>&1
Log "       exit=$LASTEXITCODE"
if ($LASTEXITCODE -ne 0) {
    Log "       ERROR: $dbOut"

exit 1
}
Log "       Database $DB_NAME ready."

# ---- 步骤 5: 检查依赖 ----
Log "[5/7] Check dependencies..."
$deps = @("javac","mvn","node","pnpm")
foreach ($d in $deps) {
    $found = Get-Command $d -ErrorAction SilentlyContinue
    if ($found) {
        $ver = & $d --version 2>&1 | Select-Object -First 1
        Log "       $d = $ver"
    } else {
        Log "       WARN: $d not found"
    }
}

# ---- 步骤 6: 复制本地配置 ----
Log "[6/7] Copy local config..."
$targetRes = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources"
$sourceFile = Join-Path $LocalConfigPath "application-dev-local.yml"
Log "       Source: $sourceFile"
Log "       Target: $targetRes"

if (Test-Path $sourceFile) {
    if (-not (Test-Path $targetRes)) { New-Item -ItemType Directory -Path $targetRes -Force | Out-Null }
    Copy-Item $sourceFile $targetRes -Force
    Log "       Copied successfully."
} else {
    Log "       WARN: Source file not found! Skipping."
}

# ---- 步骤 7: 生成 .env.branch ----
Log "[7/7] Generate .env.branch..."
$envContent = "# Orca branch env`r`nDB_NAME=$DB_NAME`r`n"
$envFile = Join-Path $env:ORCA_WORKTREE_PATH ".env.branch"
$envContent | Out-File -FilePath $envFile -Encoding UTF8
Log "       $envFile"
Log "       DB_NAME=$DB_NAME"

# ---- Done ----
Log ""
Log "========== Orca Setup Complete =========="
Log "DB:        $DB_NAME"
Log "Branch:    $env:ORCA_WORKSPACE_NAME"
Log "Config:    $targetRes\application-dev-local.yml"
Log "Env file:  $envFile"
Log "=========================================="

exit 0
