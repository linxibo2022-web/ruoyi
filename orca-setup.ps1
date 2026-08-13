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

# 密码改用 MYSQL_PWD 环境变量传递，避免出现在进程命令行中（仅本进程及子进程可见）
$env:MYSQL_PWD = $DB_PASSWORD
# 所有 mysql 调用显式携带 -h/-P 参数（PS 5.1 不支持 splatting 混合参数，故不用参数数组）

# ---- 解析项目名称 ----
# 优先级: ORCA_PROJECT_NAME > 远程仓库名 > 根目录名
if ($env:ORCA_PROJECT_NAME) {
    $ProjectName = $env:ORCA_PROJECT_NAME
} else {
    try {
        Push-Location $env:ORCA_ROOT_PATH -ErrorAction Stop
        $remoteUrl = git remote get-url origin 2>$null
        if ($remoteUrl -match '[:/]([^/]+?)(?:\.git)?$') {
            $ProjectName = $matches[1] -replace '[^a-zA-Z0-9_\-]', '_'
        } else {
            $ProjectName = (Split-Path $env:ORCA_ROOT_PATH -Leaf) -replace '[^a-zA-Z0-9_\-]', '_'
        }
    } catch {
        $ProjectName = (Split-Path $env:ORCA_ROOT_PATH -Leaf) -replace '[^a-zA-Z0-9_\-]', '_'
    } finally {
        Pop-Location -ErrorAction SilentlyContinue
    }
}
Write-Detail "Project" $ProjectName

$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
# 端口派生: 分支名 MD5 映射到 5505~5599（5504 是主工作区默认端口，必须避开）
# 保证多个 orca 工作树同时跑后端时端口互不冲突
$md5 = [BitConverter]::ToString([System.Security.Cryptography.MD5]::Create().ComputeHash([Text.Encoding]::UTF8.GetBytes($Branch))).Replace('-','').ToLower()
$PORT_RANGE = 95
# 注意必须用 ToInt64：8 位十六进制可能超过 Int32 上限导致溢出为负数，端口会掉出 5505~5599 区间
$BranchPort = 5505 + ([Convert]::ToInt64($md5.Substring(0,8), 16) % $PORT_RANGE)
# DB 命名: 前缀_项目名_分支名，确保不同项目之间隔离
$DB_NAME = $DB_PREFIX + $ProjectName + '_' + $Branch
# 限制数据库名长度（MySQL 最大 64 字符）
if ($DB_NAME.Length -gt 64) {
    $hash = [BitConverter]::ToString([System.Security.Cryptography.MD5]::Create().ComputeHash([Text.Encoding]::UTF8.GetBytes($DB_NAME))).Replace('-','').Substring(0,8).ToLower()
    # 前缀部分可能短于截断长度（如 erp_sys_develop），直接 Substring 会抛异常，先判断长度
    $head = $DB_PREFIX + $ProjectName
    $maxHead = 55 - $hash.Length
    if ($head.Length -gt $maxHead) { $head = $head.Substring(0, $maxHead) }
    $DB_NAME = $head + '_' + $hash
    Write-Warn "数据库名超长，已截断: $DB_NAME"
}

Write-Detail "DB" "$DB_USER@$DB_HOST`:$DB_PORT/$DB_NAME"

# ================================================================
# 环境预检：自动发现 mysql
# ================================================================
Write-Step "0" "环境预检"

try {
    $mysqlPath = (Get-Command mysql -ErrorAction Stop).Source
    Write-Info "mysql: $mysqlPath"
} catch {
    Write-Error "未找到 mysql！请确保 MySQL 已安装且在 PATH 中"
    Write-Info "安装指引: https://dev.mysql.com/downloads/mysql/"
    exit 1
}

$preCheckResult = & $mysqlPath -h $DB_HOST -P $DB_PORT -u $DB_USER -e "SELECT 1;" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Error "mysql 连接失败 ($DB_USER@$DB_HOST`:$DB_PORT)"
    Write-Info "错误详情: $preCheckResult"
    Write-Info "请检查 MySQL 服务是否启动、用户名密码是否正确"
    exit 1
}
Write-OK "mysql 连接正常"

# ================================================================
# Part A: Git 分支（失败不影响数据库创建）
# ================================================================
Write-Step "A" "Git 分支检查与创建"

$IsMain = ($Branch -eq "main" -or $Branch -eq "master")
if (-not $IsMain) {
    try {
        Push-Location $env:ORCA_ROOT_PATH -ErrorAction Stop
        Write-Info "git fetch --prune..."
        git fetch --prune origin 2>$null
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
& $mysqlPath -h $DB_HOST -P $DB_PORT -u $DB_USER --default-character-set=utf8mb4 -e $createSql 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "创建数据库失败！请检查 MySQL 连接与权限"
    exit 1
}
Write-OK "数据库就绪"

# 检查是否空库，是则执行 Flyway
$countSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';"
$result = & $mysqlPath -h $DB_HOST -P $DB_PORT -u $DB_USER -N -e $countSql 2>&1
$tableCount = 0
if ($LASTEXITCODE -ne 0) {
    # 查询失败不能当作空库处理，否则会误对已有数据的库执行全量迁移
    Write-Error "表计数查询失败，无法判断是否空库，已中止"
    Write-Info "错误详情: $result"
    exit 1
}
if ($result) { $tableCount = [int](($result -join '') -replace '\D', '') }

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
            $sqlFile = $f.FullName
            $proc = Start-Process -FilePath $mysqlPath -ArgumentList "-h", $DB_HOST, "-P", $DB_PORT, "-u", $DB_USER, "--default-character-set=utf8mb4", $DB_NAME -RedirectStandardInput $sqlFile -NoNewWindow -Wait -PassThru
            if ($proc.ExitCode -ne 0) {
                Write-Warn "[$idx/$total] $($f.Name) — 导入失败 (exit=$($proc.ExitCode))"
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

    # 幂等注入：先清掉历史注入段（DB_NAME/SERVER_PORT/APP_BASE_API 由本脚本统一管理），再追加新段
    # 用 .NET 显式无 BOM 的 UTF8 读写：PS 5.1 的 -Encoding UTF8 会写入 BOM，违反项目 UTF-8 无 BOM 规范
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    $lines = [System.IO.File]::ReadAllLines($targetFile)
    $clean = $lines | Where-Object { $_ -notmatch '^# -- Orca 分支' -and $_ -notmatch '^(DB_NAME|SERVER_PORT|APP_BASE_API):' }
    [System.IO.File]::WriteAllLines($targetFile, $clean, $utf8NoBom)
    $injectBlock = "`r`n# -- Orca 分支专属配置（自动生成，勿手改） --`r`nDB_NAME: $DB_NAME`r`nSERVER_PORT: $BranchPort`r`nAPP_BASE_API: http://127.0.0.1:$BranchPort`r`n"
    [System.IO.File]::AppendAllText($targetFile, $injectBlock, $utf8NoBom)
    Write-OK "配置已复制 + 注入 DB_NAME=$DB_NAME, SERVER_PORT=$BranchPort"
} else {
    Write-Warn "未找到源配置文件: $sourceFile"
}

# ================================================================
# Part D: 分支 upstream 桥接（失败静默降级，不影响启动）
# ================================================================
# 背景: orca 创建的本地分支名带 remote owner 前缀（如 linxibo2022-web/dev），
# 而远程分支名为纯 workspace 名（dev），两者不同名导致 VS Code 显示"发布分支"，
# 误点会把 owner/分支 这个新名字推到 GitHub。这里把 worktree 实际分支的
# upstream 指到 origin/<workspace>，让提交按钮、push/pull 都走对远程分支。
Write-Step "D" "分支 upstream 桥接"

if (-not $IsMain) {
    try {
        Push-Location $env:ORCA_WORKTREE_PATH -ErrorAction Stop
        $worktreeBranch = git rev-parse --abbrev-ref HEAD 2>$null
        if ($worktreeBranch -and $worktreeBranch -ne "HEAD" -and $worktreeBranch -ne $env:ORCA_WORKSPACE_NAME) {
            $remoteHead = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
            if ($remoteHead) {
                git branch --set-upstream-to="origin/$env:ORCA_WORKSPACE_NAME" $worktreeBranch 2>$null
                if ($LASTEXITCODE -eq 0) {
                    Write-OK "upstream 已桥接: $worktreeBranch -> origin/$env:ORCA_WORKSPACE_NAME"
                } else {
                    Write-Warn "upstream 桥接失败（不影响启动，VS Code 将显示发布分支）"
                }
            }
        } else {
            Write-Info "本地分支名与远程一致或未检出，跳过桥接"
        }
    } catch {
        Write-Warn "Part D 跳过: $_"
    } finally {
        Pop-Location -ErrorAction SilentlyContinue
    }
}

# ================================================================
# Done
# ================================================================
Write-DoneBox @{
    "DB"     = $DB_NAME
    "Port"   = $BranchPort
    "Branch" = $env:ORCA_WORKSPACE_NAME
}
exit 0
