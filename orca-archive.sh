#!/bin/bash
# ============================================================
# Orca 存档脚本 — 删除 worktree 时自动执行
# ============================================================
# 功能:
#   1. 安全检查：分支是否已推送到远程（未推送则拒绝删除）
#   2. 删除分支对应的独立数据库
#   3. 删除本地分支
#   4. 删除远程分支
#
# Orca 注入变量:
#   $ORCA_ROOT_PATH      - 主仓库根路径
#   $ORCA_WORKTREE_PATH  - worktree 根路径
#   $ORCA_WORKSPACE_NAME - 分支名
# ============================================================

set -e

# ---------- 1. 解析数据库名 ----------
echo "[1/5] 解析数据库名..."
BRANCH=$(echo "$ORCA_WORKSPACE_NAME" | sed 's/[^a-zA-Z0-9_]/_/g')
DB_NAME="erp_sys_${BRANCH}"
echo "       分支: ${ORCA_WORKSPACE_NAME}"
echo "       数据库: ${DB_NAME}"

# ---------- 2. 安全检查：分支是否已推送到远程 ----------
echo "[2/5] 安全检查：验证远程分支是否存在..."
cd "$ORCA_ROOT_PATH"

# 跳过 main 分支（主分支永不删除）
if [ "$ORCA_WORKSPACE_NAME" = "main" ] || [ "$ORCA_WORKSPACE_NAME" = "master" ]; then
  echo "       ⚠️  拒绝删除：${ORCA_WORKSPACE_NAME} 是主分支，不允许通过 Orca 清理"
  exit 1
fi

# 检查远程分支是否存在
REMOTE_HEAD=$(git ls-remote --heads origin "$ORCA_WORKSPACE_NAME" 2>/dev/null)
if [ -z "$REMOTE_HEAD" ]; then
  echo ""
  echo "       ❌ 安全检查失败：分支 '${ORCA_WORKSPACE_NAME}' 尚未推送到远程！"
  echo ""
  echo "       为防止误删未推送的代码，数据库不会被删除。"
  echo "       请先执行: git push origin ${ORCA_WORKSPACE_NAME}"
  echo "       确认代码已安全推送到远程后再删除工作树。"
  echo ""
  echo "       （如果确实要强制删除，请手动执行："
  echo "        mysql -u root -proot -e \"DROP DATABASE IF EXISTS \`${DB_NAME}\`;\")"
  exit 1
fi
echo "       ✅ 远程分支已确认存在（${REMOTE_HEAD:0:7}）"

# ---------- 3. 删除数据库 ----------
echo "[3/5] 删除数据库 ${DB_NAME}..."
mysql -u root -proot -e "DROP DATABASE IF EXISTS \`${DB_NAME}\`;"
if [ $? -eq 0 ]; then
  echo "       ✅ 数据库 ${DB_NAME} 已删除"
else
  echo "       ❌ 删除失败，请检查 MySQL 连接和权限"
  exit 1
fi

# ---------- 4. 删除本地分支 ----------
echo "[4/5] 删除本地分支 ${ORCA_WORKSPACE_NAME}..."
# 先切回主仓库，确保不在要删的分支上
git -C "$ORCA_ROOT_PATH" branch -D "$ORCA_WORKSPACE_NAME" 2>/dev/null
if [ $? -eq 0 ]; then
  echo "       ✅ 本地分支 ${ORCA_WORKSPACE_NAME} 已删除"
else
  echo "       ⚠️  本地分支删除失败（可能已被 Orca 删除），无影响"
fi

# ---------- 5. 删除远程分支 ----------
echo "[5/5] 删除远程分支 origin/${ORCA_WORKSPACE_NAME}..."
git -C "$ORCA_ROOT_PATH" push origin --delete "$ORCA_WORKSPACE_NAME" 2>/dev/null
if [ $? -eq 0 ]; then
  echo "       ✅ 远程分支 origin/${ORCA_WORKSPACE_NAME} 已删除"
else
  echo "       ⚠️  远程分支删除失败，请检查网络或手动删除"
fi

echo ""
echo "============================================"
echo "  ✅ Orca 工作空间完全清理完成"
echo "  已删除数据库: ${DB_NAME}"
echo "  已删除本地分支: ${ORCA_WORKSPACE_NAME}"
echo "  已删除远程分支: origin/${ORCA_WORKSPACE_NAME}"
echo "  敏感文件随 worktree 目录一并清除"
echo "============================================"
