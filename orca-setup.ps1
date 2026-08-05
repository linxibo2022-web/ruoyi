# ============================================================
# Orca 设置脚本 — 创建 worktree 时自动执行 (PowerShell)
# ============================================================
# 功能:
#   1. 根据分支名创建独立数据库
#   2. 从本地安全目录复制敏感配置文件到 worktree
#   3. 生成分支专属环境变量文件 .env.branch
#
# Orca 注入环境变量:
#   $env:ORCA_ROOT_PATH      - 主仓库根路径
#   $env:ORCA_WORKTREE_PATH  - 新 worktree 根路径
#   $env:ORCA_WORKSPACE_NAME - 分支名 (如 feature/coupon)
# ============================================================

$ErrorActionPreference = "Stop"

# ---------- 1. 分支名转数据库名 ----------
Write-Host "[1/4] 解析分支名..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = "erp_sys_$Branch"
Write-Host "       分支: $env:ORCA_WORKSPACE_NAME"
Write-Host "       数据库: $DB_NAME"

# ---------- 2. 创建数据库 ----------
Write-Host "[2/4] 创建数据库 $DB_NAME ..."
$sql = "CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
$result = mysql -u root -proot --default-character-set=utf8mb4 -e $sql 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "       ✅ 数据库 $DB_NAME 创建成功"
} else {
    Write-Host "       ❌ 数据库创建失败，请检查 MySQL 连接"
    Write-Host "       $result"
    exit 1
}

# ---------- 3. 复制本地敏感配置 ----------
Write-Host "[3/4] 复制本地敏感配置文件..."
$LocalEnv = "$env:ORCA_ROOT_PATH\.local-env"

if (Test-Path "$LocalEnv\application-dev-local.yml") {
    Copy-Item "$LocalEnv\application-dev-local.yml" "$env:ORCA_WORKTREE_PATH\ruoyi-admin\src\main\resources\" -Force
    Write-Host "       ✅ application-dev-local.yml"
} else {
    Write-Host "       ⚠️  application-dev-local.yml 不存在，跳过"
}

if (Test-Path "$LocalEnv\.env.development.local") {
    Copy-Item "$LocalEnv\.env.development.local" "$env:ORCA_WORKTREE_PATH\plus-ui\env\" -Force
    Write-Host "       ✅ .env.development.local"
} else {
    Write-Host "       ⚠️  .env.development.local 不存在，跳过"
}

# ---------- 4. 生成 .env.branch ----------
Write-Host "[4/4] 生成分支环境变量..."
@"
# 分支专属环境变量（Orca 自动生成，勿手动修改）
# 覆盖 application-dev.yml 中的 `${DB_NAME:erp_sys}` 默认值
DB_NAME=$DB_NAME
"@ | Out-File -FilePath "$env:ORCA_WORKTREE_PATH\.env.branch" -Encoding utf8
Write-Host "       ✅ .env.branch 已生成 (DB_NAME=$DB_NAME)"

Write-Host ""
Write-Host "============================================"
Write-Host "  ✅ Orca 工作空间初始化完成"
Write-Host "  数据库: $DB_NAME"
Write-Host "  Flyway 将在 Spring Boot 首次启动时自动迁移"
Write-Host "============================================"
