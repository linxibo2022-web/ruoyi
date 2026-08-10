# Orca Setup — 创建 worktree 时自动执行
# env: ORCA_ROOT_PATH, ORCA_WORKTREE_PATH, ORCA_WORKSPACE_NAME
param()

$ErrorActionPreference = "Continue"

# ═══════════════════════════════════════════════════════════════
# 控制台输出辅助函数
# ═══════════════════════════════════════════════════════════════

function Write-Banner {
    param([string]$Title, [string]$Status = "")
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Magenta
    $line = "║  $Title"
    if ($Status) { $line += "  >>  $Status" }
    $line = $line.PadRight(49) + "║"
    Write-Host $line -ForegroundColor Magenta
    Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Magenta
}

function Write-Step {
    param([string]$Label, [string]$Title)
    Write-Host ""
    Write-Host "┌────────────────────────────────────────────────┐" -ForegroundColor Cyan
    Write-Host "│  [$Label]  $Title" -ForegroundColor Cyan
    Write-Host "└────────────────────────────────────────────────┘" -ForegroundColor Cyan
}

function Write-OK { param([string]$Msg) Write-Host "    [✓] $Msg" -ForegroundColor Green }
function Write-Error { param([string]$Msg) Write-Host "    [✗] $Msg" -ForegroundColor Red }
function Write-Warn { param([string]$Msg) Write-Host "    [!] $Msg" -ForegroundColor Yellow }
function Write-Info { param([string]$Msg) Write-Host "        $Msg" -ForegroundColor Gray }
function Write-Detail { param([string]$Label, [string]$Value)
    Write-Host "        " -NoNewline
    Write-Host $Label -NoNewline -ForegroundColor DarkCyan
    Write-Host " : " -NoNewline -ForegroundColor Gray
    Write-Host $Value -ForegroundColor White
}

function Write-DoneBox {
    param([hashtable]$Items)
    Write-Host ""
    Write-Host "┌────────────────────────────────────────────────┐" -ForegroundColor Green
    Write-Host "│  ✔  初始化完成" -ForegroundColor Green
    foreach ($key in $Items.Keys) {
        $val = if ($null -eq $Items[$key]) { "" } else { $Items[$key] }
        $k = "│  $key"
        $line = "$k : $val"
        $line = $line.PadRight(49) + "│"
        Write-Host $line -ForegroundColor Green
    }
    Write-Host "└────────────────────────────────────────────────┘" -ForegroundColor Green
}

# ═══════════════════════════════════════════════════════════════
# 脚本主体
# ═══════════════════════════════════════════════════════════════

Write-Banner "Orca Setup" "初始化 Worktree"

Write-Detail "Branch"   $env:ORCA_WORKSPACE_NAME
Write-Detail "Worktree" $env:ORCA_WORKTREE_PATH

# ---- 团队共享配置 ----
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }

$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch

Write-Detail "DB" "$DB_USER@$DB_HOST`:$DB_PORT/$DB_NAME"

# ================================================================
# Part A: Git 分支（失败不影响数据库创建）
# ================================================================
Write-Step "A" "Git 分支检查与创建"

Write-Info "git fetch --prune..."

git fetch --prune origin 2>$null
$IsMain = ($Branch -eq "main" -or $Branch -eq "master")
if (-not $IsMain) {
    try {
        Push-Location $env:ORCA_ROOT_PATH -ErrorAction Stop
        $remoteExists = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
        if ($remoteExists) {
            Write-OK "远程分支已存在，无需创建"
        } else {
            $localExists = git branch --list $env:ORCA_WORKSPACE_NAME 2>$null
            if (-not $localExists) { git branch $env:ORCA_WORKSPACE_NAME 2>$null }
            git push -u origin $env:ORCA_WORKSPACE_NAME 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-OK "远程分支创建成功"
            } else {
                Write-Warn "推送失败（可能权限不足或网络问题）"
            }
        }
    } catch {
        Write-Warn "Git 步骤异常: $_"
    } finally {
        Pop-Location -ErrorAction SilentlyContinue
    }
} else {
    Write-Info "main/master 分支，跳过创建"
}

# ================================================================
# Part B: 数据库创建 + Flyway（必定执行）
# ================================================================
Write-Step "B" "数据库初始化 & 迁移"

Write-Info "目标库: $DB_NAME"

# 建库
$createSql = "CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "--default-character-set=utf8mb4", "-e", $createSql
& mysql $mysqlArgs 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Error "创建数据库失败！请检查 MySQL 连接与权限"
    exit 1
}
Write-OK "数据库就绪"

# 检查是否空库，是则执行 Flyway
$countSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-N", "-e", $countSql
$result = & mysql $mysqlArgs 2>$null
$tableCount = 0
if ($LASTEXITCODE -eq 0 -and $result) { $tableCount = [int]($result -replace '\D', '') }

if ($tableCount -gt 0) {
    Write-Warn "已有 $tableCount 张表，跳过 Flyway 迁移"
} else {
    Write-Info "空库，开始执行 Flyway 迁移..."
    $migrationDir = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources\db\migration"
    if (Test-Path $migrationDir) {
        $files = Get-ChildItem $migrationDir -Filter "V*.sql" | Sort-Object Name
        $total = $files.Count
        $idx = 0
        foreach ($f in $files) {
            $idx++
            $sqlFile = $f.FullName -replace '\\', '/'
            cmd /c "mysql --default-character-set=utf8mb4 -u $DB_USER -p$DB_PASSWORD $DB_NAME < `"$sqlFile`" 2>&1" | Out-Null
            if ($LASTEXITCODE -ne 0) {
                Write-Warn "[$idx/$total] $($f.Name) — 有警告（可能已存在）"
            } else {
                Write-OK "[$idx/$total] $($f.Name)"
            }
        }
        Write-OK "Flyway 迁移完成 ($total 个文件)"
    } else {
        Write-Warn "迁移目录不存在，跳过: $migrationDir"
    }
}

# ================================================================
# Part C: 复制本地配置
# ================================================================
Write-Step "C" "复制本地配置文件"

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
    Write-OK "配置已复制 + 注入 DB_NAME=$DB_NAME"
} else {
    Write-Warn "未找到源配置文件: $sourceFile"
}

# ================================================================
# Done
# ================================================================
Write-DoneBox @{
    "DB"     = $DB_NAME
    "Branch" = $env:ORCA_WORKSPACE_NAME
}
exit 0
