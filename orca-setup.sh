#!/bin/bash
# ============================================================
# Orca 设置脚本 — 创建 worktree 时自动执行
# ============================================================
# 功能:
#   1. 根据分支名创建独立数据库
#   2. 从本地安全目录复制敏感配置文件到 worktree
#   3. 生成分支专属环境变量文件 .env.branch
#
# Orca 注入变量:
#   $ORCA_ROOT_PATH      - 主仓库根路径
#   $ORCA_WORKTREE_PATH  - 新 worktree 根路径
#   $ORCA_WORKSPACE_NAME - 分支名 (如 feature/coupon)
# ============================================================

set -e  # 任一步失败即中断

# ---------- 1. 分支名转数据库名 ----------
# 将 feature/coupon → erp_sys_feature_coupon
# 特殊字符统一替换为下划线，确保数据库名合法
echo "[1/4] 解析分支名..."
BRANCH=$(echo "$ORCA_WORKSPACE_NAME" | sed 's/[^a-zA-Z0-9_]/_/g')
DB_NAME="erp_sys_${BRANCH}"
echo "       分支: ${ORCA_WORKSPACE_NAME}"
echo "       数据库: ${DB_NAME}"

# ---------- 2. 创建数据库 ----------
# 使用 utf8mb4 编码建库，与项目 SQL 文件编码一致
echo "[2/4] 创建数据库 ${DB_NAME}..."
mysql -u root -proot --default-character-set=utf8mb4 \
  -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if [ $? -eq 0 ]; then
  echo "       ✅ 数据库 ${DB_NAME} 创建成功"
else
  echo "       ❌ 数据库创建失败，请检查 MySQL 连接"
  exit 1
fi

# ---------- 3. 复制本地敏感配置 ----------
# 从 ORCA_ROOT_PATH/.local-env/ 复制到 worktree 对应位置
# 这些文件在 .gitignore 中，不会被提交到远程
echo "[3/4] 复制本地敏感配置文件..."
LOCAL_ENV="$ORCA_ROOT_PATH/.local-env"

# 后端敏感配置（DB密码、Redis密码、密钥等）
if [ -f "$LOCAL_ENV/application-dev-local.yml" ]; then
  cp "$LOCAL_ENV/application-dev-local.yml" \
     "$ORCA_WORKTREE_PATH/ruoyi-admin/src/main/resources/"
  echo "       ✅ application-dev-local.yml"
else
  echo "       ⚠️  application-dev-local.yml 不存在，跳过（启动可能缺少密码配置）"
fi

# 前端敏感配置
if [ -f "$LOCAL_ENV/.env.development.local" ]; then
  cp "$LOCAL_ENV/.env.development.local" "$ORCA_WORKTREE_PATH/plus-ui/env/"
  echo "       ✅ .env.development.local"
else
  echo "       ⚠️  .env.development.local 不存在，跳过"
fi

# ---------- 4. 生成 .env.branch ----------
# 覆盖 application-dev.yml 中的 ${DB_NAME:erp_sys} 默认值
echo "[4/4] 生成分支环境变量..."
cat > "$ORCA_WORKTREE_PATH/.env.branch" << EOF
# 分支专属环境变量（Orca 自动生成，勿手动修改）
# 覆盖 application-dev.yml 中的 \${DB_NAME:erp_sys} 默认值
DB_NAME=${DB_NAME}
EOF
echo "       ✅ .env.branch 已生成 (DB_NAME=${DB_NAME})"

echo ""
echo "============================================"
echo "  ✅ Orca 工作空间初始化完成"
echo "  数据库: ${DB_NAME}"
echo "  Flyway 将在 Spring Boot 首次启动时自动迁移"
echo "============================================"
