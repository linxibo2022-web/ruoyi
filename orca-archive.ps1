# Orca Archive — 删除 worktree 时自动执行
# env: ORCA_ROOT_PATH, ORCA_WORKTREE_PATH, ORCA_WORKSPACE_NAME
param()

$ErrorActionPreference = "Continue"

function Log { param([string]$Msg) Write-Output $Msg }

Log ""
Log "========== Orca Archive Start =========="
Log "Time:    $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Log "Branch:  $env:ORCA_WORKSPACE_NAME"

# ---- 团队共享配置 ----
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }
$ProtectedBranches = @("main", "master")

# ---- 步骤 1: 解析数据库名 ----
Log "[1/6] Parse DB name..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch
Log "       DB_NAME = $DB_NAME"

# ---- 步骤 2: 安全检查 ----
Log "[2/6] Safety checks..."

if ($ProtectedBranches -contains $env:ORCA_WORKSPACE_NAME) {
    Log "       ERROR: Cannot delete protected branch: $env:ORCA_WORKSPACE_NAME"
    exit 1
}
Log "       Not protected, OK."

Push-Location $env:ORCA_ROOT_PATH
$remoteHead = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>&1
if (-not $remoteHead) {
    Log "       ERROR: Remote branch not found!"
    Pop-Location
    exit 1
}
Log "       Remote branch exists."

$localBranch = git branch --list $env:ORCA_WORKSPACE_NAME 2>&1
if ($localBranch) {
    $unpushed = git log origin/$env:ORCA_WORKSPACE_NAME..$env:ORCA_WORKSPACE_NAME --oneline 2>&1
    if ($unpushed) {
        Log "       ERROR: Unpushed commits: $unpushed"
        Pop-Location
        exit 1
    }
}
Log "       No unpushed commits."
Pop-Location

# ---- 步骤 3: 删除数据库 ----
Log "[3/6] Drop database $DB_NAME..."
$checkSql = "SHOW DATABASES LIKE '$DB_NAME';"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-e", $checkSql
$dbCheck = & mysql $mysqlArgs 2>&1
if ($dbCheck -match $DB_NAME) {
    $dropSql = "DROP DATABASE IF EXISTS ``$DB_NAME``;"
    $mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-e", $dropSql
    & mysql $mysqlArgs 2>&1
    if ($LASTEXITCODE -ne 0) {
        Log "       ERROR: Drop failed (exit=$LASTEXITCODE)"
        exit 1
    }
    Log "       Database dropped."
} else {
    Log "       Database not found, skip."
}

# ---- 步骤 4: 删除远程分支 ----
Log "[4/6] Delete remote branch..."
Push-Location $env:ORCA_ROOT_PATH
git push origin --delete $env:ORCA_WORKSPACE_NAME 2>&1
Log "       exit=$LASTEXITCODE"
Pop-Location

# ---- 步骤 5: 删除本地分支 ----
Log "[5/6] Delete local branch..."
Push-Location $env:ORCA_ROOT_PATH
$localExists = git branch --list $env:ORCA_WORKSPACE_NAME 2>&1
if ($localExists) {
    $currentBranch = git branch --show-current 2>&1
    if ($currentBranch -eq $env:ORCA_WORKSPACE_NAME) {
        git checkout main 2>&1
        if ($LASTEXITCODE -ne 0) { git checkout master 2>&1 }
    }
    git branch -D $env:ORCA_WORKSPACE_NAME 2>&1
    Log "       exit=$LASTEXITCODE"
} else {
    Log "       Local branch not found, skip."
}
Pop-Location

# ---- 步骤 6: 清理残留文件 ----
Log "[6/6] Clean up residual files..."
$count = 0

$branchEnv = Join-Path $env:ORCA_WORKTREE_PATH ".env.branch"
if (Test-Path $branchEnv) { Remove-Item $branchEnv -Force; Log "       Removed .env.branch"; $count++ }

$localYml = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources\application-dev-local.yml"
if (Test-Path $localYml) { Remove-Item $localYml -Force; Log "       Removed application-dev-local.yml"; $count++ }

if ($count -eq 0) { Log "       No residual files." }

# ---- Done ----
Log ""
Log "========== Orca Archive Done =========="
Log "   DB:      $DB_NAME"
Log "   Branch:  $env:ORCA_WORKSPACE_NAME"
Log "========================================"
exit 0
