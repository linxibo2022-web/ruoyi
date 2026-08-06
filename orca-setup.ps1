# Orca 设置脚本 — 创建 worktree 时自动执行 (PowerShell)
# 环境变量: $env:ORCA_ROOT_PATH, $env:ORCA_WORKTREE_PATH, $env:ORCA_WORKSPACE_NAME

$ErrorActionPreference = "Continue"

# ============================================================
# 颜色辅助函数
# ============================================================
function Write-OK    { Write-Host "       ✅ $args" -ForegroundColor Green }
function Write-Warn  { Write-Host "       ⚠️  $args" -ForegroundColor Yellow }
function Write-Err   { Write-Host "       ❌ $args" -ForegroundColor Red }
function Write-Info  { Write-Host "       ℹ️  $args" -ForegroundColor Cyan }
function Write-Step  { param([int]$N,[int]$T,[string]$M) Write-Host "[$N/$T] $M" -ForegroundColor White }

# ============================================================
# 步骤 1: 解析分支/数据库名
# ============================================================
Write-Step 1 7 "解析分支名..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = "erp_sys_$Branch"
Write-Info "分支: $env:ORCA_WORKSPACE_NAME"
Write-Info "数据库: $DB_NAME"

# ============================================================
# 步骤 2: 前置校验检查
# ============================================================
Write-Step 2 7 "前置校验检查..."

# --- 2.1 Git remote 可达性 ---
Write-Info "检查 git remote origin..."
Push-Location $env:ORCA_ROOT_PATH
try {
    $remoteUrl = git remote get-url origin 2>$null
    if (-not $remoteUrl) {
        Write-Err "未找到 git remote origin，请先配置远程仓库"
        exit 1
    }
    git ls-remote --exit-code origin HEAD 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Err "无法连接到远程仓库 $remoteUrl"
        Write-Err "请检查网络或 SSH/HTTPS 认证配置"
        exit 1
    }
    Write-OK "远程仓库可访问: $remoteUrl"
} finally { Pop-Location }

# --- 2.2 MySQL 连接可用性 ---
Write-Info "检查 MySQL 连接..."
$mysqlTest = cmd /c "mysql -u root -proot -e `"SELECT 1;`" 2>&1"
if ($LASTEXITCODE -ne 0) {
    Write-Err "MySQL 连接失败，请确认 MySQL 服务已启动且 root/root 凭据正确"
    Write-Err "错误详情: $mysqlTest"
    exit 1
}
Write-OK "MySQL 连接正常"

# --- 2.3 分支名校验 ---
Write-Info "校验分支名..."
if ($env:ORCA_WORKSPACE_NAME -match '[\\<>:"|?*]') {
    Write-Err "分支名包含非法字符: $env:ORCA_WORKSPACE_NAME"
    exit 1
}
if ($env:ORCA_WORKSPACE_NAME -eq 'main' -or $env:ORCA_WORKSPACE_NAME -eq 'master') {
    Write-Warn "当前操作的是主分支，跳过远程分支创建"
    $IsMainBranch = $true
} else {
    $IsMainBranch = $false
}
Write-OK "分支名校验通过"

# --- 2.4 必要工具检查 ---
Write-Info "检查必要工具..."
$requiredTools = @("git", "mysql")
foreach ($tool in $requiredTools) {
    $found = Get-Command $tool -ErrorAction SilentlyContinue
    if (-not $found) {
        Write-Err "缺少必要工具: $tool，请先安装"
        exit 1
    }
}
Write-OK "必要工具就绪 (git, mysql)"

# ============================================================
# 步骤 3: 创建远程 Git 分支
# ============================================================
Write-Step 3 7 "创建远程 Git 分支..."

if ($IsMainBranch) {
    Write-Warn "主分支无需创建，跳过"
} else {
    Push-Location $env:ORCA_ROOT_PATH
    try {
        # 3.1 确认当前分支
        $currentBranch = git branch --show-current 2>$null
        Write-Info "当前分支: $currentBranch"

        # 3.2 检查远程是否已存在该分支
        $remoteExists = git ls-remote --heads origin $env:ORCA_WORKSPACE_NAME 2>$null
        if ($remoteExists) {
            Write-Warn "远程分支 origin/$env:ORCA_WORKSPACE_NAME 已存在，跳过创建"
        } else {
            # 3.3 如果本地已有该分支则直接推送，否则从当前分支创建
            $localExists = git branch --list $env:ORCA_WORKSPACE_NAME 2>$null
            if (-not $localExists) {
                Write-Info "基于 $currentBranch 创建本地分支 $env:ORCA_WORKSPACE_NAME ..."
                git branch $env:ORCA_WORKSPACE_NAME 2>$null
                if ($LASTEXITCODE -ne 0) {
                    Write-Err "创建本地分支失败"
                    exit 1
                }
            }
            # 3.4 推送远程
            Write-Info "推送 $env:ORCA_WORKSPACE_NAME 到远程 origin ..."
            git push -u origin $env:ORCA_WORKSPACE_NAME 2>&1
            if ($LASTEXITCODE -ne 0) {
                Write-Err "推送远程分支失败，请检查网络和权限"
                exit 1
            }
            Write-OK "远程分支 origin/$env:ORCA_WORKSPACE_NAME 已创建"
        }
    } finally { Pop-Location }
}

# ============================================================
# 步骤 4: 创建独立数据库
# ============================================================
Write-Step 4 7 "创建数据库 $DB_NAME ..."
cmd /c "mysql -u root -proot --default-character-set=utf8mb4 -e `"CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`" 2>&1"
if ($LASTEXITCODE -ne 0) {
    Write-Err "数据库创建失败 (exit code: $LASTEXITCODE)"
    exit 1
}
Write-OK "数据库 $DB_NAME 创建成功（Flyway 将在 Spring Boot 首次启动时自动迁移）"

# ============================================================
# 步骤 5: 检查并安装项目依赖
# ============================================================
Write-Step 5 7 "检查项目运行依赖..."

# --- 5.1 检测 scoop 是否可用 ---
$scoopAvailable = $null -ne (Get-Command scoop -ErrorAction SilentlyContinue)
if ($scoopAvailable) {
    Write-OK "scoop 已就绪，将自动安装缺失依赖"
} else {
    Write-Warn "scoop 未安装，仅检查已有依赖（不会自动安装）"
    Write-Info "建议安装 scoop: Set-ExecutionPolicy RemoteSigned -Scope CurrentUser; irm get.scoop.sh | iex"
}

# --- 5.2 检查各依赖 ---
$dependencies = @(
    @{ Name = "JDK";     Check = "javac"; ScoopPkg = "openjdk";    MinVersion = $null },
    @{ Name = "Maven";   Check = "mvn";   ScoopPkg = "maven";      MinVersion = $null },
    @{ Name = "Node.js"; Check = "node";  ScoopPkg = "nodejs";     MinVersion = $null },
    @{ Name = "pnpm";    Check = "pnpm";  ScoopPkg = "pnpm";       MinVersion = $null }
)

foreach ($dep in $dependencies) {
    $found = Get-Command $dep.Check -ErrorAction SilentlyContinue
    if ($found) {
        # 获取版本号
        $version = & $dep.Check --version 2>&1 | Select-Object -First 1
        Write-OK "$($dep.Name): $version"
    } else {
        if ($scoopAvailable) {
            Write-Warn "$($dep.Name) 未找到，通过 scoop 安装 $($dep.ScoopPkg)..."
            scoop install $dep.ScoopPkg 2>&1
            if ($LASTEXITCODE -ne 0) {
                Write-Err "$($dep.Name) 安装失败，请手动安装"
            } else {
                Write-OK "$($dep.Name) 安装成功"
            }
        } else {
            Write-Warn "$($dep.Name) 未安装，请手动安装后重试（建议使用 scoop: scoop install $($dep.ScoopPkg)）"
        }
    }
}

# ============================================================
# 步骤 6: 复制本地敏感配置（从本地磁盘固定路径）
# ============================================================
Write-Step 6 7 "复制本地敏感配置..."
$LocalConfigPath = "$env:ORCA_ROOT_PATH\..\orca-local-config"
$targetRes = "$env:ORCA_WORKTREE_PATH\ruoyi-admin\src\main\resources"

if (Test-Path "$LocalConfigPath\application-dev-local.yml") {
    if (!(Test-Path $targetRes)) { New-Item -ItemType Directory -Path $targetRes -Force | Out-Null }
    Copy-Item "$LocalConfigPath\application-dev-local.yml" $targetRes -Force
    Write-OK "application-dev-local.yml (from orca-local-config)"
} else { Write-Warn "application-dev-local.yml 不存在于 $LocalConfigPath，跳过" }

# ============================================================
# 步骤 7: 生成分支环境变量
# ============================================================
Write-Step 7 7 "生成分支环境变量..."
@"
# 分支专属环境变量（Orca 自动生成，勿手动修改）
DB_NAME=$DB_NAME
"@ | Out-File -FilePath "$env:ORCA_WORKTREE_PATH\.env.branch" -Encoding utf8
Write-OK ".env.branch (DB_NAME=$DB_NAME)"

# ============================================================
# 最终校验总结
# ============================================================
Write-Host ''
Write-Host "============================================" -ForegroundColor Green
Write-Host "  ✅ Orca 工作空间初始化完成" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "  数据库   : $DB_NAME (Flyway 将在首次启动时迁移)"
Write-Host "  远程分支 : $env:ORCA_WORKSPACE_NAME"
Write-Host "  配置就绪 : application-dev-local.yml / .env.development.local / .env.branch"
Write-Host "============================================" -ForegroundColor Green
