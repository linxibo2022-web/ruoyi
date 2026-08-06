# Orca Setup — 创建 worktree 时自动执行
# env: ORCA_ROOT_PATH, ORCA_WORKTREE_PATH, ORCA_WORKSPACE_NAME
param()

$ErrorActionPreference = "Continue"

function Log { param([string]$Msg) Write-Output $Msg }

Log ""
Log "========== Orca Setup Start =========="
Log "Time:     $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Log "Branch:   $env:ORCA_WORKSPACE_NAME"
Log "Worktree: $env:ORCA_WORKTREE_PATH"
Log "Root:     $env:ORCA_ROOT_PATH"

# ---- 团队共享配置 ----
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }

Log "DB: $DB_USER@$DB_HOST`:$DB_PORT"

# ---- 步骤 1: 解析分支/数据库名 ----
Log "[1/8] Parse branch name..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch
$IsMain = ($Branch -eq "main" -or $Branch -eq "master")
Log "       DB_NAME = $DB_NAME"

# ---- 步骤 2: 前置校验 ----
Log "[2/8] Validate prerequisites..."

Push-Location $env:ORCA_ROOT_PATH
$remoteUrl = git remote get-url origin 2>&1
Log "       Git remote: $remoteUrl"

$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-e", "SELECT 1;"
$mysqlOut = & mysql $mysqlArgs 2>&1
if ($LASTEXITCODE -ne 0) {
    Log "       ERROR: MySQL check failed: $mysqlOut"
    Pop-Location
    exit 1
}
Log "       MySQL OK"

$gitOk = $null -ne (Get-Command git -ErrorAction SilentlyContinue)
$mysqlOk = $null -ne (Get-Command mysql -ErrorAction SilentlyContinue)
Log "       git=$gitOk mysql=$mysqlOk"
Pop-Location

# ---- 步骤 3: 创建远程分支 ----
Log "[3/8] Create remote branch..."
if (-not $IsMain) {
    Push-Location $env:ORCA_ROOT_PATH
    $remoteExists = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>&1
    if ($remoteExists) {
        Log "       Remote branch already exists, skip."
    } else {
        git branch $env:ORCA_WORKSPACE_NAME 2>&1
        $pushOut = git push -u origin $env:ORCA_WORKSPACE_NAME 2>&1
        if ($LASTEXITCODE -ne 0) {
            Log "       WARN: Push issue: $pushOut"
        } else {
            Log "       Remote branch created."
        }
    }
    Pop-Location
}

# ---- 步骤 4: 创建数据库 ----
Log "[4/8] Create database $DB_NAME..."
$createSql = "CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "--default-character-set=utf8mb4", "-e", $createSql
& mysql $mysqlArgs 2>&1
if ($LASTEXITCODE -ne 0) {
    Log "       ERROR: Create database failed (exit=$LASTEXITCODE)"
    exit 1
}
Log "       Database $DB_NAME created."

# ---- 步骤 5: Flyway 建表 ----
Log "[5/8] Run Flyway migrations..."
$migrationDir = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources\db\migration"
Log "       Migration dir: $migrationDir"
if (Test-Path $migrationDir) {
    $files = Get-ChildItem $migrationDir -Filter "V*.sql" | Sort-Object Name
    Log "       Found $($files.Count) migration files"
    foreach ($f in $files) {
        $sqlFile = $f.FullName
        $cmd = "mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < `"$sqlFile`" 2>&1"
        cmd /c $cmd
        if ($LASTEXITCODE -ne 0) {
            Log "       ERROR: $($f.Name) failed (exit=$LASTEXITCODE)"
            exit 1
        }
        Log "       $($f.Name) OK"
    }
    Log "       All migrations done."
} else {
    Log "       WARN: Migration dir not found, skip."
}

# ---- 步骤 6: 检查依赖 ----
Log "[6/8] Check dependencies..."
foreach ($tool in @("javac","mvn","node","pnpm")) {
    $found = Get-Command $tool -ErrorAction SilentlyContinue
    if ($found) { Log "       $tool = found" } else { Log "       WARN: $tool not found" }
}

# ---- 步骤 7: 复制本地敏感配置 ----
Log "[7/8] Copy local config..."
$ORCA_ROOT = $env:ORCA_ROOT_PATH.TrimEnd('\')
$LocalConfigDir = Join-Path (Split-Path $ORCA_ROOT -Parent) "orca-local-config"
if ($env:ORCA_LOCAL_CONFIG) { $LocalConfigDir = $env:ORCA_LOCAL_CONFIG }
$sourceFile = Join-Path $LocalConfigDir "application-dev-local.yml"
$targetDir  = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources"
Log "       Source: $sourceFile"
Log "       Target: $targetDir"

if (Test-Path $sourceFile) {
    if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }
    Copy-Item $sourceFile $targetDir -Force
    Log "       Copied successfully."
} else {
    Log "       WARN: Source NOT FOUND — check D:\ruoyi_project\orca-local-config\"
}

# ---- 步骤 8: 生成 .env.branch ----
Log "[8/8] Generate .env.branch..."
$envContent = "# Orca branch env`r`nDB_NAME=$DB_NAME`r`n"
$envFile = Join-Path $env:ORCA_WORKTREE_PATH ".env.branch"
$envContent | Out-File -FilePath $envFile -Encoding UTF8
Log "       Created: $envFile"

# ---- Done ----
Log ""
Log "========== Orca Setup Done =========="
Log "   DB:      $DB_NAME"
Log "   Branch:  $env:ORCA_WORKSPACE_NAME"
Log "======================================"
exit 0
