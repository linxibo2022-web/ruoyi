# Orca 设置脚本 — 创建 worktree 时自动执行
# 环境变量: ORCA_ROOT_PATH, ORCA_WORKTREE_PATH, ORCA_WORKSPACE_NAME

Write-Host "[1/4] 解析分支名..."
$Branch = $env:ORCA_WORKSPACE_NAME -replace '[^a-zA-Z0-9_]', '_'
$DB_NAME = "erp_sys_$Branch"
Write-Host "       分支: $env:ORCA_WORKSPACE_NAME"
Write-Host "       数据库: $DB_NAME"

Write-Host "[2/4] 创建数据库 $DB_NAME ..."
cmd /c "mysql -u root -proot --default-character-set=utf8mb4 -e `"CREATE DATABASE IF NOT EXISTS ``$DB_NAME`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`" 2>&1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "       ❌ 创建失败 (code: $LASTEXITCODE)"
    exit 1
}
Write-Host "       ✅ 数据库创建成功"

Write-Host "[3/4] 复制本地敏感配置..."
$LocalEnv = "$env:ORCA_ROOT_PATH\.local-env"
$targetRes = "$env:ORCA_WORKTREE_PATH\ruoyi-admin\src\main\resources"
$targetUi = "$env:ORCA_WORKTREE_PATH\plus-ui\env"
if (Test-Path "$LocalEnv\application-dev-local.yml") {
    if (!(Test-Path $targetRes)) { New-Item -ItemType Directory -Path $targetRes -Force | Out-Null }
    Copy-Item "$LocalEnv\application-dev-local.yml" $targetRes -Force
    Write-Host "       ✅ application-dev-local.yml"
} else { Write-Host "       ⚠️  application-dev-local.yml 不存在，跳过" }
if (Test-Path "$LocalEnv\.env.development.local") {
    if (!(Test-Path $targetUi)) { New-Item -ItemType Directory -Path $targetUi -Force | Out-Null }
    Copy-Item "$LocalEnv\.env.development.local" $targetUi -Force
    Write-Host "       ✅ .env.development.local"
} else { Write-Host "       ⚠️  .env.development.local 不存在，跳过" }

Write-Host "[4/4] 生成分支环境变量..."
@"
# 分支专属环境变量（Orca 自动生成）
DB_NAME=$DB_NAME
"@ | Out-File -FilePath "$env:ORCA_WORKTREE_PATH\.env.branch" -Encoding utf8
Write-Host "       ✅ .env.branch (DB_NAME=$DB_NAME)"

Write-Host "============================================"
Write-Host "  ✅ 初始化完成 | 数据库: $DB_NAME"
Write-Host "============================================"
