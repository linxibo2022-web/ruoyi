# Orca Archive — 删除 worktree 时自动执行
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
    param([string]$Num, [string]$Title)
    Write-Host ""
    Write-Host "┌────────────────────────────────────────────────┐" -ForegroundColor Cyan
    Write-Host "│  STEP $Num : $Title" -ForegroundColor Cyan
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
    Write-Host "│  ✔  执行完成" -ForegroundColor Green
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

Write-Banner "Orca Archive" "清理 Worktree"

Write-Detail "Time"   (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Write-Detail "Branch" $env:ORCA_WORKSPACE_NAME

# ---- 团队共享配置 ----
$DB_HOST     = if ($env:DB_HOST)     { $env:DB_HOST }     else { "127.0.0.1" }
$DB_PORT     = if ($env:DB_PORT)     { $env:DB_PORT }     else { "3306" }
$DB_USER     = if ($env:DB_USER)     { $env:DB_USER }     else { "root" }
$DB_PREFIX   = if ($env:DB_PREFIX)   { $env:DB_PREFIX }   else { "erp_sys_" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }
$ProtectedBranches = @("main", "master")

# ---- 步骤 1: 解析数据库名 ----
Write-Step "1/6" "解析数据库名称"

$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = $DB_PREFIX + $Branch
Write-Detail "DB_NAME" $DB_NAME
Write-OK "解析完成"

# ---- 步骤 2: 安全检查 ----
Write-Step "2/6" "安全检查"

if ($ProtectedBranches -contains $env:ORCA_WORKSPACE_NAME) {
    Write-Error "禁止删除受保护分支: $env:ORCA_WORKSPACE_NAME"
    exit 1
}
Write-OK "非受保护分支，通过"

Push-Location $env:ORCA_ROOT_PATH
$remoteHead = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>&1
if (-not $remoteHead) {
    Write-Error "远程分支不存在，无法删除！"
    Pop-Location
    exit 1
}
Write-OK "远程分支存在"

$localBranch = git branch --list $env:ORCA_WORKSPACE_NAME 2>&1
if ($localBranch) {
    $unpushed = git log origin/$env:ORCA_WORKSPACE_NAME..$env:ORCA_WORKSPACE_NAME --oneline 2>&1
    if ($unpushed) {
        Write-Error "存在未推送提交: $unpushed"
        Pop-Location
        exit 1
    }
}
Write-OK "无未推送提交"
Pop-Location

# ---- 步骤 3: 删除数据库 ----
Write-Step "3/6" "删除数据库"

Write-Info "目标: $DB_NAME"

$checkSql = "SHOW DATABASES LIKE '$DB_NAME';"
$mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-e", $checkSql
$dbCheck = & mysql $mysqlArgs 2>&1
if ($dbCheck -match $DB_NAME) {
    $dropSql = "DROP DATABASE IF EXISTS ``$DB_NAME``;"
    $mysqlArgs = "-u", $DB_USER, "-p$DB_PASSWORD", "-e", $dropSql
    & mysql $mysqlArgs 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Error "删除失败 (exit=$LASTEXITCODE)"
        exit 1
    }
    Write-OK "数据库已删除"
} else {
    Write-Warn "数据库不存在，跳过"
}

# ---- 步骤 4: 删除远程分支 ----
Write-Step "4/6" "删除远程分支"

Push-Location $env:ORCA_ROOT_PATH
git push origin --delete $env:ORCA_WORKSPACE_NAME 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-OK "远程分支已删除"
} else {
    Write-Warn "删除远程分支 exit=$LASTEXITCODE（可能已被删除）"
}
Pop-Location

# ---- 步骤 5: 删除本地分支 ----
Write-Step "5/6" "删除本地分支"

Push-Location $env:ORCA_ROOT_PATH
$localExists = git branch --list $env:ORCA_WORKSPACE_NAME 2>&1
if ($localExists) {
    $currentBranch = git branch --show-current 2>&1
    if ($currentBranch -eq $env:ORCA_WORKSPACE_NAME) {
        Write-Info "当前正在目标分支上，先切换到 main/master..."
        git checkout main 2>&1
        if ($LASTEXITCODE -ne 0) { git checkout master 2>&1 }
    }
    git branch -D $env:ORCA_WORKSPACE_NAME 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-OK "本地分支已删除"
    } else {
        Write-Warn "删除本地分支 exit=$LASTEXITCODE"
    }
} else {
    Write-Warn "本地分支不存在，跳过"
}
Pop-Location

# ---- 步骤 6: 清理残留文件 ----
Write-Step "6/6" "清理残留文件"

$count = 0

$localYml = Join-Path $env:ORCA_WORKTREE_PATH "ruoyi-admin\src\main\resources\application-dev-local.yml"
if (Test-Path $localYml) {
    Remove-Item $localYml -Force
    Write-OK "已删除 application-dev-local.yml"
    $count++
}

if ($count -eq 0) {
    Write-Warn "无残留文件需要清理"
}

# ---- Done ----
Write-DoneBox @{
    "DB"     = $DB_NAME
    "Branch" = $env:ORCA_WORKSPACE_NAME
}
exit 0
