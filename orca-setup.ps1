# Orca Setup — 创建 worktree 时自动执行
# env: ORCA_ROOT_PATH, ORCA_WORKTREE_PATH, ORCA_WORKSPACE_NAME
param()

$ErrorActionPreference = "Continue"
function Log { param([string]$Msg) Write-Output $Msg }

Log ""
Log "========== Orca Setup =========="
Log "Branch:   $env:ORCA_WORKSPACE_NAME"
Log "Worktree: $env:ORCA_WORKTREE_PATH"

# ---- 团队共享配置 ----
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }

$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch

Log "DB: $DB_USER@$DB_HOST`:$DB_PORT/$DB_NAME"

# ================================================================
# Part A: Git 分支（失败不影响数据库创建）
# ================================================================
Log "[A] Git branch..."
Log "       git fetch --prune..."
git fetch --prune origin 2>$null
$IsMain = ($Branch -eq "main" -or $Branch -eq "master")
if (-not $IsMain) {
    try {
        Push-Location $env:ORCA_ROOT_PATH -ErrorAction Stop
        $remoteExists = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
        if ($remoteExists) {
            Log "       Remote branch exists, skip."
        } else {
            $localExists = git branch --list $env:ORCA_WORKSPACE_NAME 2>$null
            if (-not $localExists) { git branch $env:ORCA_WORKSPACE_NAME 2>$null }
            git push -u origin $env:ORCA_WORKSPACE_NAME 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) { Log "       Created." } else { Log "       WARN: Push failed." }
        }
    } catch {
        Log "       WARN: Git step failed: $_"
    } finally {
        Pop-Location -ErrorAction SilentlyContinue
    }
}

# ================================================================
# Part B: 数据库创建 + Flyway（必定执行）
# ================================================================
Log "[B] Database $DB_NAME..."

# 建库
$createSql = "CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "--default-character-set=utf8mb4", "-e", $createSql
& mysql $mysqlArgs 2>$null
if ($LASTEXITCODE -ne 0) {
    Log "       ERROR: Create database failed."
    exit 1
}
Log "       Database ready."

# 检查是否空库，是则执行 Flyway
$countSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-N", "-e", $countSql
$result = & mysql $mysqlArgs 2>$null
$tableCount = 0
if ($LASTEXITCODE -eq 0 -and $result) { $tableCount = [int]($result -replace '\D', '') }

if ($tableCount -gt 0) {
    Log "       $tableCount tables exist, skip migrations."
} else {
    Log "       Empty database, running migrations..."
    $migrationDir = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources\db\migration"
    if (Test-Path $migrationDir) {
        $files = Get-ChildItem $migrationDir -Filter "V*.sql" | Sort-Object Name
        foreach ($f in $files) {
            $sqlFile = $f.FullName
            cmd /c "mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < `"$sqlFile`" 2>&1" | Out-Null
            if ($LASTEXITCODE -ne 0) {
                Log "       WARN: $($f.Name) has errors (may already exist)."
            }
            Log "       $($f.Name) done."
        }
        Log "       Migrations complete."
    } else {
        Log "       WARN: Migration dir not found, skip."
    }
}

# ================================================================
# Part C: 复制本地配置
# ================================================================
Log "[C] Copy local config..."
$ORCA_ROOT = $env:ORCA_ROOT_PATH.TrimEnd('\')
$LocalConfigDir = if ($env:ORCA_LOCAL_CONFIG) { $env:ORCA_LOCAL_CONFIG } else {
    Join-Path (Split-Path $ORCA_ROOT -Parent) "orca-local-config"
}
$sourceFile = Join-Path $LocalConfigDir "application-dev-local.yml"
$targetFile = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources\application-dev-local.yml"

if (Test-Path $sourceFile) {
    $targetDir = Split-Path $targetFile -Parent
    if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }
    Copy-Item $sourceFile $targetFile -Force
    Add-Content -Path $targetFile -Value "`r`n# -- Orca 分支数据库 --`r`nDB_NAME: $DB_NAME`r`n" -Encoding UTF8
    Log "       Config copied + DB_NAME=$DB_NAME"
} else {
    Log "       WARN: $sourceFile not found."
}

# ================================================================
# Done
# ================================================================
Log ""
Log "========== Done =========="
Log "DB:      $DB_NAME"
Log "Branch:  $env:ORCA_WORKSPACE_NAME"
Log "=========================="
exit 0
