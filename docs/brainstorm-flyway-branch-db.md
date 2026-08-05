# 头脑风暴：工作树分支独立数据库体系

**创建时间**: 2026-08-05 14:00
**最后更新**: 2026-08-05 14:00
**状态**: 已确定方案

---

## 背景与目标

每次 Orca 创建 worktree 时自动创建独立数据库，删除 worktree 时自动清理，实现每个分支的数据库完全隔离。

核心需求：
1. Orca 创建工作树时，利用设置脚本创建新数据库
2. 数据库变更使用 Flyway 管理
3. 删除工作树时同时删除分支及对应数据库
4. 敏感配置按 env 范式管理（本地存储，不入仓库）
5. 使用 Orca 注入变量：`$ORCA_ROOT_PATH`、`$ORCA_WORKTREE_PATH`、`$ORCA_WORKSPACE_NAME`

## 方案讨论

### 方案 A：Shell 驱动（脚本直连 MySQL + Flyway CLI）

设置脚本用 `mysql` CLI 建库，再用 `flyway` CLI 执行迁移。

- **优点**: 实现简单，两个 shell 脚本
- **缺点**: 依赖 mysql CLI + Flyway CLI 都在 PATH，跨平台差

### 方案 B：Spring Boot 启动时自动迁移 ✅

设置脚本只建库 + 复制敏感文件，Flyway 迁移交给 Spring Boot 启动时自动执行。

- **优点**: 迁移与代码版本天然绑定，仅需 mysql CLI 建库
- **缺点**: 需要集成 Flyway 到 Spring Boot

### 方案 C：Flyway Java API 嵌入式

用 Java 程序直调 Flyway API 建库 + 迁移，零外部依赖。

- **优点**: 跨平台最佳，零外部依赖
- **缺点**: 开发量大，收益不高

## 最终结论

**选定方案**: B — Spring Boot 启动时自动迁移

**选择理由**:
1. 迁移脚本和代码在同一个分支里，永远同步
2. 设置脚本极简，只做建库 + 复制敏感文件
3. Spring Boot 集成 Flyway 是业界标准做法

### 数据库命名规则

```
分支名: feature/coupon
    ↓ sanitize（/ → _）
数据库名: erp_sys_feature_coupon
```

### 完整流程

```
Orca 创建 worktree
  │
  ├─ 1. git worktree add → 裸代码（无敏感文件）
  ├─ 2. 设置脚本:
  │     ├─ mysql CREATE DATABASE erp_sys_feature_xxx
  │     ├─ cp .local-env/* → worktree（敏感文件入位）
  │     └─ 生成 .env.branch（DB_NAME=erp_sys_feature_xxx）
  ├─ 3. Spring Boot 启动:
  │     ├─ 读 .env.branch → ${DB_NAME}
  │     ├─ Flyway 自动 migrate → 库就绪
  │     └─ 应用启动完成
  │
  ├─ 开发中: git push → .gitignore 自动排除敏感文件 ✅
  │
  └─ Orca 删除 worktree
       ├─ 存档脚本: mysql DROP DATABASE
       └─ 删 worktree → 敏感文件一起清除
```

### 目录结构

```
# 本地敏感配置（不入 git）
~/.erp-local-env/                          # 或 ORCA_ROOT_PATH/.local-env/
├── application-dev-local.yml              # DB密码、Redis密码、密钥等
├── .env.development.local                 # 前端开发环境密钥
└── .env.production.local                  # 生产环境密钥

# 仓库内
erp_sys/
├── ruoyi-admin/src/main/resources/
│   ├── db/migration/                      # Flyway 迁移脚本
│   │   ├── V1__sys_tables.sql
│   │   ├── V2__app_tables.sql
│   │   ├── V3__job_tables.sql
│   │   └── V4__new_tables.sql
│   ├── application-dev.yml               # ${DB_NAME:erp_sys} 占位
│   └── application-dev-local.yml         # ← .gitignore 拦截
├── env/
│   ├── .env                               # 共享配置
│   ├── .env.development                   # 开发环境
│   ├── .env.production                    # 生产环境
│   └── .env.branch                       # ← Orca 生成，.gitignore
├── .env.branch                            # ← Orca 生成，.gitignore
├── .gitignore
└── orca-setup.sh                          # Orca 设置脚本
```

### .gitignore 追加

```
# env 本地敏感文件
**/application-dev-local.yml
**/application-prod-local.yml
**/.env.*.local
**/.env.branch
```

### Orca 设置脚本（orca-setup.sh）

```bash
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
echo "[1/4] 解析分支名..."
BRANCH=$(echo "$ORCA_WORKSPACE_NAME" | sed 's/[^a-zA-Z0-9_]/_/g')
DB_NAME="erp_sys_${BRANCH}"
echo "       分支: ${ORCA_WORKSPACE_NAME}"
echo "       数据库: ${DB_NAME}"

# ---------- 2. 创建数据库 ----------
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
echo "[3/4] 复制本地敏感配置文件..."
LOCAL_ENV="$ORCA_ROOT_PATH/.local-env"

if [ -f "$LOCAL_ENV/application-dev-local.yml" ]; then
  cp "$LOCAL_ENV/application-dev-local.yml" \
     "$ORCA_WORKTREE_PATH/ruoyi-admin/src/main/resources/"
  echo "       ✅ application-dev-local.yml"
else
  echo "       ⚠️  application-dev-local.yml 不存在，跳过"
fi

if [ -f "$LOCAL_ENV/.env.development.local" ]; then
  cp "$LOCAL_ENV/.env.development.local" "$ORCA_WORKTREE_PATH/plus-ui/env/"
  echo "       ✅ .env.development.local"
else
  echo "       ⚠️  .env.development.local 不存在，跳过"
fi

# ---------- 4. 生成 .env.branch ----------
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
```

### Orca 存档脚本（orca-archive.sh）

```bash
#!/bin/bash
# ============================================================
# Orca 存档脚本 — 删除 worktree 时自动执行
# ============================================================
# 功能:
#   1. 删除分支对应的独立数据库
#
# Orca 注入变量:
#   $ORCA_ROOT_PATH      - 主仓库根路径
#   $ORCA_WORKTREE_PATH  - worktree 根路径
#   $ORCA_WORKSPACE_NAME - 分支名
# ============================================================

set -e

echo "[1/2] 解析数据库名..."
BRANCH=$(echo "$ORCA_WORKSPACE_NAME" | sed 's/[^a-zA-Z0-9_]/_/g')
DB_NAME="erp_sys_${BRANCH}"
echo "       数据库: ${DB_NAME}"

echo "[2/2] 删除数据库 ${DB_NAME}..."
mysql -u root -proot -e "DROP DATABASE IF EXISTS \`${DB_NAME}\`;"
if [ $? -eq 0 ]; then
  echo "       ✅ 数据库 ${DB_NAME} 已删除"
else
  echo "       ❌ 删除失败"
  exit 1
fi

echo ""
echo "============================================"
echo "  ✅ Orca 工作空间清理完成"
echo "  已删除数据库: ${DB_NAME}"
echo "  敏感文件随 worktree 目录一并清除"
echo "============================================"
```

### 运行时效果示例

```
[1/4] 解析分支名...
       分支: feature/coupon
       数据库: erp_sys_feature_coupon
[2/4] 创建数据库 erp_sys_feature_coupon...
       ✅ 数据库 erp_sys_feature_coupon 创建成功
[3/4] 复制本地敏感配置文件...
       ✅ application-dev-local.yml
       ✅ .env.development.local
[4/4] 生成分支环境变量...
       ✅ .env.branch 已生成 (DB_NAME=erp_sys_feature_coupon)

============================================
  ✅ Orca 工作空间初始化完成
  数据库: erp_sys_feature_coupon
  Flyway 将在 Spring Boot 首次启动时自动迁移
============================================
```

## 实施步骤

1. [ ] 将 4 个 SQL 文件转为 Flyway 迁移脚本 `V1~V4__xxx.sql`，放到 `db/migration/`
2. [ ] `ruoyi-admin/pom.xml` 添加 `flyway-core` + `flyway-mysql` 依赖
3. [ ] `application-dev.yml` 添加 `spring.flyway.*` 配置
4. [ ] 创建 `.local-env/application-dev-local.yml`（敏感配置模板）
5. [ ] 创建 Orca 设置脚本 `orca-setup.sh`
6. [ ] 创建 Orca 存档脚本 `orca-archive.sh`
7. [ ] 更新 `.gitignore`
8. [ ] 验证端到端流程

## 风险与注意事项

| 风险 | 应对策略 |
|------|---------|
| Flyway 迁移和现有手工导入冲突 | 首次 `baseline-on-migrate: true`，跳过已存在的表 |
| 迁移脚本写错导致启动失败 | Flyway `validate-on-migrate` 校验，本地先测 |
| 分支合并后迁移版本号冲突 | V 编号递增，合并时确保不重复 |
| 分支名含特殊字符（如 `/`） | `sed` 替换为 `_` |
| MySQL 客户端编码问题 | `--default-character-set=utf8mb4` 强制 UTF-8 |
| 敏感文件未正确忽略 | 本地验证 `git status` 确认无泄漏 |
| 误删未推送的分支数据库 | `orca-archive.sh` 内置安全检查：`git ls-remote` 验证远程分支存在才删库 |

## 讨论记录

### 2026-08-05 初始讨论
- 选定方案 B：Spring Boot 启动时自动迁移
- 确定数据库命名规则为 `erp_sys_<分支名>`，特殊字符替换为 `_`
- 确定敏感配置按 env 范式：`.local-env/` 本地存放，脚本复制，`.gitignore` 拦截
- 确定脚本需要详细注释和步骤状态输出
