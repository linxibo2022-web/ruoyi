---
name: project-init
description: |
  当用户需要基于本框架创建新项目、初始化新项目时自动使用此 Skill。提供完整的交互式项目初始化流程：模板更新检测、分支选择、移动端选择、标识符替换、目录复制、Git 仓库创建、数据库初始化、启动引导。

  触发场景：
  - 用户说"我要开发一个新项目"或"创建一个新项目"
  - 需要基于 ruoyi-plus-uniapp 框架初始化新的业务系统
  - 需要修改项目唯一标识符和端口配置
  - 需要为新项目创建 Git 仓库并推送代码
  - 需要初始化新项目的数据库

  触发词：新项目、创建项目、初始化项目、开新项目、项目初始化、new project、init project、新建项目、开发新项目、独立部署
---

# 新项目初始化指南

## 概述

本技能用于基于 ruoyi-plus-uniapp 框架（模板仓库）创建全新的独立项目。**模板仓库始终保持不变**，所有操作在新目录中进行，支持反复创建新项目。

**核心理念**：模板仓库 = 只读源 → 复制到新目录 → 在新目录中初始化

**完整流程**：

```
阶段零：环境准备
├── 检测模板仓库更新（git fetch + 差异展示）
├── 选择分支（master/single/workflow）
├── 选择保留的前端/移动端项目（多选）
├── 确定标识符、标题、端口
└── 确定 Git 仓库地址

阶段一：创建新项目目录
├── git archive 导出目标分支到新目录（无需切换分支）
├── 清理新目录中不需要的子项目
└── 在新目录初始化 Git

阶段二：代码初始化（在新目录中）
├── 全局替换标识符、标题、端口
└── 验证替换结果

阶段三：Git 提交 & 推送
├── git add + 初始提交
├── git remote add origin
└── git push

阶段四：数据库初始化

阶段五：启动引导
```

---

## 阶段零：环境准备（交互式）

### Step 0.1：检测模板仓库更新（仅展示，不拉取）

在模板仓库目录中执行：

```bash
# 拉取远程最新信息（仅元数据，不修改本地文件）
git fetch origin

# 检查各分支与远程的差异
for branch in master single workflow; do
  BEHIND=$(git rev-list $branch..origin/$branch --count 2>/dev/null)
  if [ "$BEHIND" -gt 0 ]; then
    echo "[$branch] 有 $BEHIND 个新提交："
    git log $branch..origin/$branch --oneline --no-merges
  else
    echo "[$branch] 已是最新 ✓"
  fi
done
```

**展示更新摘要**（此时仅展示，不询问是否拉取——需要先选分支）：

```
模板仓库更新检测：

  master   — 有 3 个新提交（最近：feat(generator): 优化字典转换...）
  single   — 已是最新 ✓
  workflow — 有 1 个新提交（最近：fix(workflow): 修复审批...）
```

### Step 0.2：选择分支

**必须询问用户**：
- 开发什么项目？（简要描述业务场景）

**根据业务场景推荐分支**：

| 分支 | 适用场景 | 特性 |
|------|---------|------|
| `master` | SaaS 平台、多组织系统、需要租户隔离 | 多租户支持，`TenantEntity` 基类 |
| `single` | 单体应用、内部管理系统、个人项目 | 无租户隔离，更简洁 |
| `workflow` | OA 系统、审批流、工单系统 | 多租户 + 工作流引擎 |

**推荐话术示例**：
```
您描述的是一个「电商系统」，建议使用 master（多租户）分支，
因为电商平台通常需要支持多个商家入驻，租户隔离能很好地支持这个需求。

如果是纯自营电商（不需要多商家），也可以选择 single（非多租户）分支。

请确认您的选择：master / single / workflow？
```

**用户选择分支后，检查该分支是否有更新**：

如果 Step 0.1 显示该分支有新提交，询问用户：
```
该分支（master）有 3 个新提交，是否拉取最新代码？（推荐：是）
```

如果用户确认拉取：
```bash
git pull origin {branch}
```

> **注意**：仅拉取用户选择的分支，不影响其他分支。

### Step 0.3：选择保留的前端/移动端项目（多选）

**合并为一个多选步骤**，减少交互轮次：

```
请选择需要保留的前端项目（多选）：

  ✅ [1] plus-ui        — PC 后台管理（必选，不可取消）
  □  [2] plus-uniapp   — 小程序 / H5 / CLI构建APP
  □  [3] plus-app      — 原生APP / 鸿蒙APP（需 HBuilderX）
  ━━━━━━━━━━━━━━━━━━━━━━
  ⊘  plus-uniappx      — UniApp X（尚不可用，暂不支持）
  ⊘  plus-uniapp-demo  — 组件示例（仅供参考，不保留）

请输入编号（如：2 或 2,3 或 all）：
```

**选择规则**：
- `plus-ui` 始终保留，不可取消
- `plus-uniapp` 和 `plus-app` 可多选（有些项目同时需要小程序 + 原生APP）
- `plus-uniappx` 和 `plus-uniapp-demo` 始终不保留
- 输入 `all` = 保留 plus-ui + plus-uniapp + plus-app
- 回复"仅 PC 端"或不选 = 仅保留 plus-ui

**记录用户选择**，后续步骤（复制、替换、清理、启动引导）都依赖此选择。

### Step 0.4：确定项目标识符和端口

**标识符规则**：
- 仅英文字母和下划线
- 长度不超过 20 个字符
- 全小写
- 建议与项目名称相关
- **标识符同时也是新项目的目录名**

**根据项目名推荐标识符**：

| 项目描述 | 推荐标识符 | 推荐标题 |
|---------|----------|---------|
| 电商系统 | `mall_shop` | mall-shop后台管理 |
| CRM客户管理 | `crm_sys` | crm-sys后台管理 |
| 物联网平台 | `iot_platform` | iot-platform后台管理 |
| 内部办公 | `oa_office` | oa-office后台管理 |
| 博客系统 | `blog_app` | blog-app后台管理 |
| 教育平台 | `edu_learn` | edu-learn后台管理 |

**端口分配建议**（避开常用端口）：

| 端口 | 用途 | 默认值 | 建议范围 |
|------|------|--------|---------|
| 后端端口 | Spring Boot 服务 | 5500 | 5501-5599, 8900-8999 |
| 前端端口 | Vite Dev Server | 80 | 80, 81, 82 |

> **注意**：前端端口仅影响本地开发，生产环境通过 nginx 代理。

**必须询问用户一次性确认**：
```
根据您的项目「电商系统」，建议配置：
- 项目标识符：mall_shop
- 项目标题：mall-shop后台管理
- 后端端口：5501（原框架默认 5500）
- 前端端口：80（保持默认）
- 新目录：{模板仓库同级}/mall_shop

请确认或自定义。
```

### Step 0.5：Git 仓库

> **🔴 最高优先级强制规则：所有新建仓库必须是私有仓库（private），绝对禁止创建公开仓库。**
>
> - 适用范围：Gitee、GitCode、GitHub 等所有代码托管平台
> - 即使用户未明确指定可见性，默认私有
> - 如果用户明确要求公开，必须先警告风险（代码泄露、敏感配置暴露等）并二次确认后才可执行
> - 自动创建脚本中 `private: true` / `--private` 必须硬编码，不允许省略或注释

**必须询问用户**：
```
请选择 Git 仓库方式（⚠️ 新仓库默认创建为私有）：
1. 提供已有的仓库地址（Gitee/GitCode/GitHub/其他）
2. 我来自动创建私有仓库（支持 Gitee/GitCode API Token 或 gh CLI）
3. 稍后手动创建

请提供仓库地址或选择方式。
```

**自动创建示例**（必须使用私有）：

```bash
# ===== GitHub（需要 gh CLI 已登录）=====
# 🔴 必须使用 --private，绝对禁止 --public
gh repo create {项目名} --private --description "{项目描述}" --confirm

# ===== Gitee（使用 ~/.gitee_token）=====
# 🔴 必须使用 Python 发送（避免 Windows GBK 中文乱码），且 'private': True 硬编码
GITEE_TOKEN=$(cat ~/.gitee_token)
PYTHONIOENCODING=utf-8 python -c "
import urllib.request, json
data = json.dumps({
    'access_token': '${GITEE_TOKEN}',
    'name': '{项目名}',
    'description': '{项目描述}',
    'private': True,
    'auto_init': False
}).encode('utf-8')
req = urllib.request.Request('https://gitee.com/api/v5/user/repos', data=data, method='POST')
req.add_header('Content-Type', 'application/json;charset=UTF-8')
resp = urllib.request.urlopen(req)
print(json.loads(resp.read().decode('utf-8')).get('html_url'))
"

# ===== GitCode（使用 credential manager 中的 token）=====
# 🔴 必须使用 Python 发送，且 'private': True 硬编码
TOKEN=$(printf "protocol=https\nhost=gitcode.com\n" | git credential fill | grep password | cut -d= -f2)
PYTHONIOENCODING=utf-8 python -c "
import urllib.request, json, sys
token = sys.argv[1]
data = json.dumps({
    'name': '{项目名}',
    'description': '{项目描述}',
    'private': True,
    'auto_init': False
}).encode('utf-8')
req = urllib.request.Request('https://api.gitcode.com/api/v5/user/repos', data=data, method='POST')
req.add_header('Content-Type', 'application/json;charset=UTF-8')
req.add_header('Authorization', f'Bearer {token}')
resp = urllib.request.urlopen(req)
print(json.loads(resp.read().decode('utf-8')).get('html_url'))
" "$TOKEN"
```

**如果无法自动创建**，提示用户（⚠️ 必须强调私有）：
```
请在 Gitee/GitCode/GitHub 手动创建一个【私有】空仓库（不要初始化 README）：
  - 可见性（Visibility）：必须选择 "Private / 私有"
  - 初始化选项：都不要勾选（README、.gitignore、License 全部留空）

创建完成后，提供仓库地址，例如：
- https://gitee.com/username/project-name.git
- https://gitcode.com/username/project-name.git
- https://github.com/username/project-name.git
```

**⚠️ 创建后自检**：无论使用哪种方式，创建完成后必须回显仓库可见性供用户确认：
```
✅ 仓库已创建：https://gitee.com/xxx/{项目名}.git
   可见性：Private（私有）
   如果显示为 Public，请立即在仓库设置中改为 Private！
```

### Step 0.6：确认汇总

在开始执行前，向用户展示完整的配置汇总：

```
━━━━━━━━━━ 项目初始化配置确认 ━━━━━━━━━━

  分支：master（多租户）
  标识符：mall_shop
  标题：mall-shop后台管理
  后端端口：5501
  前端端口：80

  保留项目：
    ✅ plus-ui（PC 后台）
    ✅ plus-uniapp（小程序/H5）
    ❌ plus-app（未选择）

  新目录：D:\desktop\my\framework\ruoyi-plus-uniapp\mall_shop
  Git 仓库：https://gitee.com/xxx/mall_shop.git
  仓库可见性：🔒 Private（私有）  ← 强制，不可更改

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

确认无误后开始初始化？(Y/n)
```

---

## 阶段一：创建新项目目录

### Step 1.1：使用 git archive 导出目标分支到新目录

**核心命令**：`git archive` 可以从任意分支导出文件，**无需切换分支**，自动排除 `.git/` 和未跟踪文件（如 `node_modules/`、`.claude/projects/`）。

```bash
TEMPLATE_DIR="$(pwd)"
PARENT_DIR="$(dirname "$TEMPLATE_DIR")"
NEW_DIR="$PARENT_DIR/{标识符}"

# 检查新目录是否已存在
if [ -d "$NEW_DIR" ]; then
  echo "⚠️ 目录 $NEW_DIR 已存在！请确认是否覆盖。"
  exit 1
fi

# 创建新目录并导出
mkdir -p "$NEW_DIR"
cd "$TEMPLATE_DIR"
git archive {branch} | tar -x -C "$NEW_DIR"
```

**git archive 的优势**：

| 对比项 | git archive | rsync/robocopy |
|--------|-------------|----------------|
| 是否需要切换分支 | ❌ 不需要 | ✅ 需要先 checkout |
| 是否排除 .git/ | ✅ 自动排除 | 需手动 --exclude |
| 是否排除 node_modules/ | ✅ 自动排除（未跟踪） | 需手动 --exclude |
| 是否排除 .claude/projects/ | ✅ 自动排除（未跟踪） | 需手动 --exclude |
| Windows 兼容性 | ✅ Git Bash 自带 | rsync 可能不可用 |
| 模板仓库是否受影响 | ✅ 完全不受影响 | 需要 stash + restore |

**自动排除的内容**（因为未被 git 跟踪）：

| 排除项 | 原因 |
|--------|------|
| `.git/` | git archive 不导出 .git 目录 |
| `node_modules/` | 在 .gitignore 中，未跟踪 |
| `.claude/projects/` | 个人 memory 数据，未跟踪 |
| `.codex/projects/` | 个人数据，未跟踪 |

**自动包含的内容**（已被 git 跟踪）：

| 保留项 | 原因 |
|--------|------|
| `.claude/skills/` | 项目级技能，新项目同样需要 |
| `.claude/commands/` | 项目级命令 |
| `.claude/hooks/` | Hook 配置 |
| `.agents/skills/` | Codex 技能镜像 |
| `plus-uniapp-demo/` | 已跟踪，需在 Step 1.2 清理 |
| `plus-uniappx/` | 已跟踪，需在 Step 1.2 清理 |
| `CLAUDE.md` | 项目规范文档 |
| `AGENTS.md` | Agent 配置 |

### Step 1.2：清理新目录中不需要的子项目

`git archive` 会导出所有已跟踪文件，需要手动清理不需要的子项目：

```bash
cd "$NEW_DIR"

# 始终删除（不可选）
rm -rf plus-uniapp-demo/
rm -rf plus-uniappx/

# 根据 Step 0.3 用户选择，删除未选中的移动端目录
# 如果用户未选择 plus-uniapp
rm -rf plus-uniapp/

# 如果用户未选择 plus-app
rm -rf plus-app/
```

### Step 1.3：在新目录初始化 Git

```bash
cd "$NEW_DIR"

git init
git checkout -b main
```

> **默认使用全新 Git 历史**：新项目不需要框架的开发历史，一个干净的初始提交更合理。
> 模板仓库完全不受影响，可反复用于创建新项目。

---

## 阶段二：代码初始化（在新目录中执行）

> **🔴 以下所有操作都在新目录 `{NEW_DIR}` 中执行，不要在模板目录中操作！**

### Step 2.1：确定旧值（根据分支）

> **🔴 关键：三个分支的旧标识符、标题、端口各不相同！**
> 必须根据用户选择的分支动态确定旧值，不能硬编码 master 分支的值。

#### 各分支旧值映射表（必须先查此表）

| 属性 | master | single | workflow |
|------|--------|--------|----------|
| **旧标识符（下划线版）** | `ryplus_uni` | `ryplus_uni_single` | `ryplus_uni_workflow` |
| **旧连字符版** | `ryplus-uni` | `ryplus-uni-single` | `ryplus-uni-workflow` |
| **旧标题** | `ryplus-uni后台管理` | `ryplus-uni-single后台` | `ryplus-uni-workflow后台管理` |
| **旧 pom 描述** | `ryplus-uni多租户管理系统` | `ryplus-uni-single管理系统` | `ryplus-uni-workflow多租户管理系统` |
| **旧端口** | 5500 | 5504 | 5503 |

**⚠️ 注意事项**：
- single 分支标题后缀是 `后台` 而非 `后台管理`
- 各分支的旧值差异意味着**不能通用写死替换目标**，必须先确定分支再查表

#### 动态检测旧值

也可从 `application.yml` 中读取实际旧值：

```bash
# 读取当前分支的旧标识符
OLD_ID=$(grep "^  id:" ruoyi-admin/src/main/resources/application.yml | awk '{print $2}')
# 读取当前分支的旧标题
OLD_TITLE=$(grep "^  title:" ruoyi-admin/src/main/resources/application.yml | awk '{print $2}')
# 读取当前分支的旧端口
OLD_PORT=$(grep "port:" ruoyi-admin/src/main/resources/application.yml | head -1 | grep -oP '\d{4}')
```

### Step 2.2：替换唯一标识符

将 `{旧标识符}` → `{新标识符}`（如 `ryplus_uni_single` → `mall_shop`）

**影响的文件类别**：

| 类别 | 文件列表 | 替换内容 |
|------|---------|---------|
| **后端配置** | `ruoyi-admin/src/main/resources/application.yml` | `app.id` |
| **后端构建** | `ruoyi-admin/pom.xml` | `<artifactId>`（如有引用） |
| **后端部署** | `ruoyi-admin/Dockerfile` | jar 文件名 |
| **部署脚本** | `script/bin/ry.sh`, `script/bin/ry.bat` | AppName |
| **IDEA 配置** | `.run/RuoyiPlus.run.xml` | Docker 镜像标签 |
| **数据库连接** | `ruoyi-admin/src/main/resources/application-dev.yml` | `DB_NAME` 默认值 |
| **SQL 脚本** | `script/sql/ry_plus_sys.sql` | OSS bucket 前缀 |
| **SQL 脚本** | `script/sql/ry_plus_job.sql` | SnailJob 组名 |
| **Docker** | `script/docker/compose/*.yml` | 容器名、镜像名、DB_NAME |
| **Nginx** | `script/docker/nginx/conf/nginx.conf` | 反向代理路径 |
| **前端/移动端** | `*/env/.env`, `*/env/.env.production` | `VITE_APP_ID`、API 路径 |
| **前端/移动端** | `*/systemConfig.ts`, `*/systemConfig.uts` | 默认值 fallback |
| **文档** | `README.md` | 项目名称引用 |

**⚠️ 替换注意事项**：
1. 只替换文本文件，跳过 `.git/`、`node_modules/`、二进制文件
2. SQL 文件中的替换要特别注意，不要破坏 SQL 语法
3. **先替换长字符串再替换短字符串**（如 `ryplus_uni_single` 先于 `ryplus_uni`，防止部分匹配）
4. **只替换用户保留的子项目中的文件**（未选择的目录已在 Step 1.4 删除）

### Step 2.3：替换端口

将 `{旧端口}` → `{新端口}`（如 `5504` → `5501`）

**仅替换以下文件中的端口**（精确匹配，不要全局替换）：

| 文件 | 替换内容 | 说明 |
|------|---------|------|
| `ruoyi-admin/src/main/resources/application.yml` | `${SERVER_PORT:{旧端口}}` → `${SERVER_PORT:{新端口}}` | 后端服务端口 |
| `ruoyi-admin/src/main/resources/application-dev.yml` | `http://127.0.0.1:{旧端口}` → `http://127.0.0.1:{新端口}` | 回调地址 |
| `plus-ui/env/.env.development` | `VITE_APP_BASE_API_PORT='{旧端口}'` → `'{新端口}'` | 前端代理目标 |
| `plus-uniapp/env/.env.development`（如保留） | URL 中的 `{旧端口}` → `{新端口}` | 移动端 API 地址 |
| `plus-app/env/.env.development`（如保留） | URL 中的 `{旧端口}` → `{新端口}` | APP 端 API 地址 |

**⚠️ 端口替换注意**：
- `application.yml` 中的 springdoc 注释里的端口也要替换
- Docker compose 文件中的端口映射也要替换
- 不要替换 Redis 端口 6379、MySQL 端口 3306 等

### Step 2.4：替换标题和连字符版

> **重要**：各分支的旧标题格式不一致（见 2.1 映射表），必须用当前分支的实际旧值！

**替换策略：使用全局搜索替换，而非固定文件列表**

```bash
# 排除 .git/ .claude/ .codex/ node_modules/
grep -r "{旧标识符}" --include="*.yml" --include="*.ts" --include="*.js" \
  --include="*.json" --include="*.xml" --include="*.sql" --include="*.sh" \
  --include="*.bat" --include="*.conf" --include="*.vue" --include="*.md" \
  --include="*.html" --include="*.mts" --include="*.uts" \
  --exclude-dir=.git --exclude-dir=.claude --exclude-dir=.codex \
  --exclude-dir=node_modules .
```

**替换执行顺序（先长后短，避免误替换）**：

1. **先替换带后缀的完整标题**（各分支不同）：
   - master: `ryplus-uni后台管理` → `{连字符版}后台管理`
   - single: `ryplus-uni-single后台` → `{连字符版}后台管理`
   - workflow: `ryplus-uni-workflow后台管理` → `{连字符版}后台管理`

2. **再替换 pom 描述**（各分支不同）：
   - master: `ryplus-uni多租户管理系统` → `{连字符版}管理系统`（或根据分支类型调整）
   - single: `ryplus-uni-single管理系统` → `{连字符版}管理系统`
   - workflow: `ryplus-uni-workflow多租户管理系统` → `{连字符版}多租户管理系统`

3. **最后替换裸标识符**（连字符版和下划线版）：
   - `{旧标识符}` → `{新标识符}`（下划线版，最后替换）
   - `{旧连字符版}` → `{新连字符版}`（如果存在独立的连字符版）

**影响的文件范围**（仅包含用户保留的子项目）：

| 项目 | 文件 | 包含的旧值类型 |
|------|------|---------------|
| **后端** | `application.yml` | 标识符 + 标题 |
| **后端** | `pom.xml`（根目录） | 描述 |
| **plus-ui**（必选） | `env/.env`, `package.json`, `index.html` | 标识符 + 标题 |
| **plus-ui**（必选） | `src/systemConfig.ts`, `src/layouts/components/Sidebar/Logo.vue` | 连字符版 |
| **plus-uniapp**（如保留） | `env/.env`, `package.json`, `index.html` | 标识符 + 标题 |
| **plus-uniapp**（如保留） | `manifest.config.ts`, `pages.config.mts` | 连字符版 |
| **plus-uniapp**（如保留） | `src/pages.json`, `src/manifest.json` | 连字符版 |
| **plus-app**（如保留） | `env/.env`, `package.json`, `index.html` | 标识符 + 标题 |
| **plus-app**（如保留） | `manifest.json`, `pages.json`, `pages.config.ts` | 连字符版 |
| **文档** | `README.md` | 项目名称 |

**⚠️ 注意**：三个分支的命名现已统一规范化——标识符用下划线（如 `ryplus_uni_workflow`），显示名用连字符（如 `ryplus-uni-workflow`）。替换时需分别处理两种格式。

### Step 2.5：验证替换结果

```bash
# 验证标识符替换完成（在新目录中执行）
grep -rn "ryplus_uni\|ryplus-uni" --include="*.yml" --include="*.yaml" \
  --include="*.ts" --include="*.js" --include="*.json" --include="*.xml" \
  --include="*.sql" --include="*.sh" --include="*.bat" --include="*.conf" \
  --include="*.vue" --include="*.md" --include="*.html" --include="*.mts" \
  --include="*.uts" \
  --exclude-dir=.git --exclude-dir=.claude --exclude-dir=.codex \
  --exclude-dir=node_modules .

# 注意：.claude/ 和 .codex/ 下的技能文档中可能包含 ryplus_uni 作为示例，这些不需要替换
# 如果还有残留，需要补充替换
```

---

## 阶段三：Git 提交 & 推送

### Step 3.1：初始提交

```bash
cd "$NEW_DIR"

git add -A
git commit -m "init: 基于 ruoyi-plus-uniapp 框架初始化 {项目标题}"
```

### Step 3.2：关联远程仓库并推送

```bash
# 添加远程仓库
git remote add origin {用户提供的仓库地址}

# 推送到远程
git push -u origin main
```

> **如果用户选择"稍后手动创建"**，跳过推送步骤，提示：
> ```
> Git 仓库已本地初始化。创建远程仓库后，执行：
> cd {NEW_DIR}
> git remote add origin {仓库地址}
> git push -u origin main
> ```

### Step 3.3：配置 upstream remote 和同步元数据

> **重要**：此步骤为后续框架同步（`/framework-sync`）提供基础配置。

```bash
cd "$NEW_DIR"

# 添加框架仓库作为 upstream（使用模板仓库的远程地址）
UPSTREAM_URL=$(cd "$TEMPLATE_DIR" && git remote get-url origin)
git remote add upstream "$UPSTREAM_URL"

# 获取创建时框架的 commit hash
INIT_COMMIT=$(cd "$TEMPLATE_DIR" && git rev-parse {branch})
INIT_DATE=$(TZ=Asia/Shanghai date '+%Y-%m-%dT%H:%M:%S+08:00')
```

**生成 `.framework-sync.json`**：

```json
{
  "upstream": {
    "url": "{UPSTREAM_URL}",
    "branch": "{用户选择的分支}",
    "initCommit": "{INIT_COMMIT}",
    "initDate": "{INIT_DATE}"
  },
  "identifierMap": {
    "old_id": "{旧标识符}",
    "new_id": "{新标识符}",
    "old_hyphen": "{旧连字符版}",
    "new_hyphen": "{新连字符版}",
    "old_port": "{旧端口}",
    "new_port": "{新端口}",
    "old_title": "{旧标题}",
    "new_title": "{新标题}"
  },
  "retainedProjects": ["plus-ui", "plus-uniapp"],
  "lastSync": null,
  "lastCommit": "{INIT_COMMIT}",
  "syncs": []
}
```

> `retainedProjects` 根据 Step 0.3 用户的选择填充（始终包含 `plus-ui`）。

将 `.framework-sync.json` 加入 git 并追加提交：

```bash
git add .framework-sync.json
git commit -m "chore: 添加框架同步配置"
```

---

## 阶段四：数据库初始化（交互式+自动）

### Step 4.1：确认数据库信息

**必须询问用户**：
```
请确认数据库配置：

1. 数据库类型：
   - MySQL（默认，推荐）
   - PostgreSQL
   - Oracle
   - SQL Server

2. 连接信息（默认值，如需修改请告知）：
   - 地址：127.0.0.1
   - 端口：3306
   - 用户名：root
   - 密码：root

是否使用默认配置？
```

### Step 4.1.5：启用对应数据库的依赖与配置（⚠️ 非 MySQL 必做）

> **🔴 关键步骤**：框架默认只启用了 **MySQL** 驱动 + anyline 适配器，并且 `application-dev.yml` 中只配置了 MySQL 的 master/slave 数据源。
> 用户若选择 PostgreSQL / Oracle / SQL Server，**必须先解开 pom 注释 + 替换 yml 数据源**，否则项目启动会直接报"找不到驱动"或"连接失败"。
>
> **MySQL 用户跳过本步骤**，直接进入 Step 4.2。

#### 1. 框架默认状态（事实清单）

**`ruoyi-common/ruoyi-common-mybatis/pom.xml`**：

| 行号 | 内容 | 默认状态 |
|------|------|----------|
| 34-37 | `mysql-connector-j`（MySQL JDBC 驱动） | ✅ 启用 |
| 40-43 | `ojdbc8`（Oracle JDBC 驱动） | ❌ 注释 |
| 44-47 | `orai18n`（Oracle 国际化） | ❌ 注释 |
| 48-51 | `postgresql`（PostgreSQL JDBC 驱动） | ❌ 注释 |
| 52-55 | `mssql-jdbc`（SQL Server JDBC 驱动） | ❌ 注释 |
| 91-96 | `anyline-data-jdbc-mysql`（MySQL anyline 适配器） | ✅ 启用 |
| 107-111 | `anyline-data-jdbc-oracle` | ❌ 注释 |
| 112-116 | `anyline-data-jdbc-postgresql` | ❌ 注释 |
| 117-121 | `anyline-data-jdbc-mssql` | ❌ 注释 |

**`ruoyi-admin/src/main/resources/application-dev.yml`**：

| 行号范围 | 内容 | 默认状态 |
|---------|------|----------|
| 45-52 | master 数据源（MySQL URL + Driver） | ✅ 启用 |
| 54-60 | slave 数据源（MySQL URL + Driver） | ✅ 启用 |
| 61-67 | Oracle 数据源示例 | ❌ 注释 |
| 69-75 | PostgreSQL 数据源示例 | ❌ 注释 |
| 77-83 | SQL Server 数据源示例 | ❌ 注释 |

> 行号以 master 分支为准，single/workflow 可能略有偏移，定位用关键字（`postgresql` / `ojdbc8` / `mssql-jdbc` / `anyline-data-jdbc-xxx` / `driverClassName`）更稳。

#### 2. 按数据库类型的操作清单

> 思路：**取消对应数据库行的注释 → 替换 master/slave 数据源为对应数据库配置**。
> dynamic-datasource 支持多驱动共存（无依赖冲突），保留 MySQL 驱动也无害，但 master/slave 的 `driverClassName` + `url` **必须**与所选数据库匹配，否则启动即失败。

##### PostgreSQL

**Step A — 修改 `ruoyi-common/ruoyi-common-mybatis/pom.xml`**：
- 取消第 48-51 行（postgresql JDBC 驱动）的注释
- 取消第 112-116 行（anyline-data-jdbc-postgresql）的注释

**Step B — 修改 `ruoyi-admin/src/main/resources/application-dev.yml`**：

把 master / slave 的 `driverClassName` + `url` 替换为：

```yaml
        master:
          type: ${spring.datasource.type}
          driverClassName: org.postgresql.Driver
          url: jdbc:postgresql://${DB_HOST:127.0.0.1}:${DB_PORT:5432}/${DB_NAME:{新标识符}}?useUnicode=true&characterEncoding=utf8&useSSL=${DB_SSL:false}&autoReconnect=true&reWriteBatchedInserts=true
          username: ${DB_USERNAME:postgres}
          password: ${DB_PASSWORD:postgres}
        slave:
          lazy: true
          type: ${spring.datasource.type}
          driverClassName: org.postgresql.Driver
          url: jdbc:postgresql://${DB_SLAVE_HOST:127.0.0.1}:${DB_SLAVE_PORT:5432}/${DB_SLAVE_NAME:{新标识符}}?useUnicode=true&characterEncoding=utf8&useSSL=${DB_SLAVE_SSL:false}&autoReconnect=true&reWriteBatchedInserts=true
          username: ${DB_SLAVE_USERNAME:postgres}
          password: ${DB_SLAVE_PASSWORD:postgres}
```

> 端口默认 **5432**（非 3306），用户名默认 **postgres**（非 root）。

##### Oracle

**Step A — pom.xml**：
- 取消第 40-43 行（ojdbc8）的注释
- 取消第 44-47 行（orai18n）的注释
- 取消第 107-111 行（anyline-data-jdbc-oracle）的注释

**Step B — application-dev.yml** 替换 master / slave：

```yaml
        master:
          type: ${spring.datasource.type}
          driverClassName: oracle.jdbc.OracleDriver
          url: jdbc:oracle:thin:@//${DB_HOST:127.0.0.1}:${DB_PORT:1521}/${DB_NAME:XE}
          username: ${DB_USERNAME:ROOT}
          password: ${DB_PASSWORD:root}
        slave:
          lazy: true
          type: ${spring.datasource.type}
          driverClassName: oracle.jdbc.OracleDriver
          url: jdbc:oracle:thin:@//${DB_SLAVE_HOST:127.0.0.1}:${DB_SLAVE_PORT:1521}/${DB_SLAVE_NAME:XE}
          username: ${DB_SLAVE_USERNAME:ROOT}
          password: ${DB_SLAVE_PASSWORD:root}
```

> 端口默认 **1521**，`DB_NAME` 为服务名（如 XE / ORCL），不是 schema。

##### SQL Server

**Step A — pom.xml**：
- 取消第 52-55 行（mssql-jdbc）的注释
- 取消第 117-121 行（anyline-data-jdbc-mssql）的注释

**Step B — application-dev.yml** 替换 master / slave：

```yaml
        master:
          type: ${spring.datasource.type}
          driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
          url: jdbc:sqlserver://${DB_HOST:127.0.0.1}:${DB_PORT:1433};DatabaseName=${DB_NAME:{新标识符}};SelectMethod=cursor;encrypt=${DB_ENCRYPT:false};rewriteBatchedStatements=true
          username: ${DB_USERNAME:SA}
          password: ${DB_PASSWORD:root}
        slave:
          lazy: true
          type: ${spring.datasource.type}
          driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
          url: jdbc:sqlserver://${DB_SLAVE_HOST:127.0.0.1}:${DB_SLAVE_PORT:1433};DatabaseName=${DB_SLAVE_NAME:{新标识符}};SelectMethod=cursor;encrypt=${DB_SLAVE_ENCRYPT:false};rewriteBatchedStatements=true
          username: ${DB_SLAVE_USERNAME:SA}
          password: ${DB_SLAVE_PASSWORD:root}
```

> 端口默认 **1433**，用户名默认 **SA**，URL 用分号 `;` 而非问号 `?`。

#### 3. MySQL 驱动是否保留

- **推荐保留**：不删除 pom.xml 中的 MySQL 驱动和 anyline-mysql 适配器，多驱动共存无冲突，后续如需第二数据源接 MySQL 时直接可用。
- **如果用户明确"完全不用 MySQL"**：可注释 pom.xml 第 33-37 行（mysql-connector-j）；anyline-data-jdbc-mysql 保留无害。

#### 4. 验证

```bash
cd "$NEW_DIR"

# 验证 pom.xml 中对应驱动已启用（应能看到非注释行）
grep -n "postgresql\|ojdbc8\|mssql-jdbc" ruoyi-common/ruoyi-common-mybatis/pom.xml \
  | grep -v "<!--"

# 验证 application-dev.yml 中 master/slave 的 driverClassName 已切换
grep -n "driverClassName" ruoyi-admin/src/main/resources/application-dev.yml \
  | grep -v "^\s*#"

# Maven 解析依赖（验证 pom 语法和依赖可下载）
mvn -DskipTests -pl ruoyi-common/ruoyi-common-mybatis -am dependency:resolve 2>&1 | tail -5
```

#### 5. ⚠️ 易踩坑提示

- **anyline 适配器必须配套**：只解开 JDBC 驱动注释、漏掉 anyline 适配器，启动时建表/同步会报错。**驱动 + anyline 必须同步启用**。
- **DB_NAME 不要含连字符**：PostgreSQL 数据库名建议用下划线版（如 `mall_shop`），与标识符一致。
- **Oracle 服务名 vs SID**：`@//host:port/SERVICE` 是服务名格式；如果用 SID 则是 `@host:port:SID`，不要混。
- **SQL Server encrypt**：JDBC 12.x 默认 `encrypt=true`，自签证书会失败，本地开发设 `encrypt=false`。
- **UTF-8 无 BOM 保存**：修改 pom.xml / application-dev.yml 后必须保持 UTF-8 无 BOM，否则可能触发 `非法字符: '﻿'`。

---

### Step 4.2：创建数据库 & 导入数据

#### MySQL 数据库创建

```sql
-- 主业务数据库
CREATE DATABASE IF NOT EXISTS `{新标识符}`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- SnailJob 调度数据库（可选，建议独立库）
CREATE DATABASE IF NOT EXISTS `{新标识符}_snailjob`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

#### SQL 导入顺序

| 顺序 | 脚本文件 | 目标数据库 | 必需性 | 说明 |
|------|---------|-----------|--------|------|
| 1 | `script/sql/ry_plus_sys.sql` | `{标识符}` | **必须** | 系统核心表（用户、角色、菜单、字典等） |
| 2 | `script/sql/ry_plus_app.sql` | `{标识符}` | 推荐 | 业务表（广告、绑定、平台、商品等） |
| 3 | `script/sql/ry_plus_new.sql` | `{标识符}` | 按需 | 新增业务表 |
| 4 | `script/sql/ry_plus_job.sql` | `{标识符}_snailjob` | 按需 | SnailJob 定时任务表 |

**MySQL 命令行导入**：

```bash
# 导入系统表（必须最先）
mysql -h {host} -P {port} -u {user} -p{password} {标识符} < script/sql/ry_plus_sys.sql

# 导入业务表
mysql -h {host} -P {port} -u {user} -p{password} {标识符} < script/sql/ry_plus_app.sql

# 导入新业务表（如有）
mysql -h {host} -P {port} -u {user} -p{password} {标识符} < script/sql/ry_plus_new.sql

# 导入定时任务表（独立数据库）
mysql -h {host} -P {port} -u {user} -p{password} {标识符}_snailjob < script/sql/ry_plus_job.sql
```

#### 其他数据库

| 数据库 | SQL 文件目录 | 说明 |
|--------|------------|------|
| PostgreSQL | `script/sql/postgres/` | `postgres_ry_plus_sys.sql` 等 |
| Oracle | `script/sql/oracle/` | `oracle_ry_plus_sys.sql` 等 |
| SQL Server | `script/sql/sqlserver/` | `sqlserver_ry_plus_sys.sql` 等 |

**如果无法自动连接数据库**，输出手动导入步骤：
```
无法自动连接数据库，请手动执行以下步骤：

1. 使用数据库管理工具（Navicat/DBeaver/命令行）连接到数据库
2. 创建数据库 `{标识符}`，字符集 utf8mb4，排序规则 utf8mb4_general_ci
3. 按以下顺序导入 SQL 文件：
   ① script/sql/ry_plus_sys.sql（系统核心表，必须最先）
   ② script/sql/ry_plus_app.sql（业务表）
   ③ script/sql/ry_plus_new.sql（新业务表，如有）
4. 如需定时任务，创建独立数据库 `{标识符}_snailjob`，导入：
   ④ script/sql/ry_plus_job.sql

完成后请告知。
```

#### 更新数据库连接配置

导入完成后，确认 `application-dev.yml` 中的数据库配置已正确：

```yaml
# application-dev.yml 中需要确认的配置
datasource:
  master:
    url: jdbc:mysql://{host}:{port}/{新标识符}?useUnicode=true&characterEncoding=utf8&...
    username: {用户名}
    password: {密码}
```

如果用户的数据库连接信息与默认值不同，需要修改 `application-dev.yml`。

---

## 阶段五：启动引导

### Step 5.1：提示启动步骤

**根据用户保留的子项目，动态生成启动指引**：

```
项目初始化完成！按以下步骤启动：

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📁 项目目录：{NEW_DIR}

1. 启动后端
   - 确保 Redis 已启动（默认 127.0.0.1:6379）
   - 确保 MySQL 已启动且数据已导入
   - IDEA 中运行 RuoyiPlus.java
     位置：ruoyi-admin/src/main/java/plus/ruoyi/RuoyiPlus.java
   - 或命令行：mvn spring-boot:run -pl ruoyi-admin
   - 访问：http://localhost:{后端端口}

2. 启动前端（PC 管理后台）
   cd plus-ui
   pnpm install
   pnpm run dev
   访问：http://localhost:{前端端口}
```

**如果保留了 plus-uniapp，追加**：
```
3. 启动移动端（plus-uniapp）
   cd plus-uniapp
   pnpm install
   pnpm run dev:h5          # H5 模式
   pnpm run dev:mp-weixin   # 微信小程序
```

**如果保留了 plus-app，追加**：
```
4. 启动 APP 端（plus-app）
   使用 HBuilderX 打开 plus-app 目录
   运行 → 运行到浏览器/真机/模拟器
```

```
默认账号：
  管理员：admin / admin123
  普通用户：test / admin123

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

环境要求：
- JDK 17+（推荐 21）
- Node.js 18+
- pnpm 8+
- Redis 6.x/7.x（禁用 7.4）
- MySQL 5.7+（推荐 8.0+）
```

### Step 5.2：起飞自主开发（接 /loop /dev-loop）

孵化完成后，输出**复制即用**的起飞指令，让用户在新项目里一句话进入自主连续开发：

```
━━━━━━━━━━ 到新项目里新开会话粘这一句即可起飞 ━━━━━━━━━━

/loop /dev-loop

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

说明（给用户看，无需复制）：
- 新项目已带 `/dev-loop`（随框架技能继承）+ 已就位的需求 / 原型（若经 `/kickoff` 而来）。
- `/loop /dev-loop` = 自主连续开发：每轮挑 `docs/tasks/active/` 第一个未做项 →（CRUD 复用 `/dev`、原型转码用 `html-to-code`、组件照 `ui-pc`/`ui-mobile`）→ 验证门（`mvn` + 前端 build + `e2e-test` 浏览器截图对照原型）→ 勾任务台账 + 规范 commit，直到台账做完才停。
- **第 0 轮**：无任务台账时，按 `docs/需求文档.md` + 原型自动拆出 `docs/tasks/active/` 任务卡。
- 进度/待办/需求三文档由 `/update-status` 在里程碑汇总，dev-loop 每轮只动 `docs/tasks/active/`。

---

## 完整替换清单（精确文件列表）

> **以下清单中的旧值用 `{旧标识符}` 表示，实际值取决于所选分支（见 2.1 映射表）。**
> **仅列出用户保留的子项目中的文件。**

### 标识符替换清单（所有出现位置）

```
# 后端配置（始终替换）
ruoyi-admin/src/main/resources/application.yml          → app.id、title
ruoyi-admin/src/main/resources/application-dev.yml       → DB_NAME 默认值（主库+从库）
ruoyi-admin/pom.xml                                      → <artifactId>
ruoyi-admin/Dockerfile                                   → jar 文件名、注释
pom.xml（根目录）                                        → <description>

# 部署脚本（始终替换）
script/bin/ry.sh                                         → AppName
script/bin/ry.bat                                        → AppName
.run/RuoyiPlus.run.xml                                   → imageTag

# Docker 部署（始终替换）
script/docker/compose/RuoyiPlus-compose.yml              → 服务名、镜像名、容器名、DB_NAME、DB_USERNAME
script/docker/compose/SnailjobServer-compose.yml         → DB_NAME、DB_USERNAME
script/docker/compose/Complete-compose.yml               → 相关配置

# Nginx（始终替换）
script/docker/nginx/conf/nginx.conf                      → 反向代理 location 路径

# SQL 脚本（始终替换）
script/sql/ry_plus_sys.sql                               → OSS bucket 前缀（6处）
script/sql/ry_plus_job.sql                               → SnailJob group_name（3处）
script/sql/postgres/postgres_ry_plus_sys.sql             → OSS bucket 前缀
script/sql/postgres/postgres_ry_plus_job.sql             → SnailJob group_name
script/sql/sqlserver/sqlserver_ry_plus_sys.sql            → OSS bucket 前缀
script/sql/sqlserver/sqlserver_ry_plus_job.sql            → SnailJob group_name
script/sql/oracle/oracle_ry_plus_job.sql                 → SnailJob group_name

# 前端 (plus-ui)（始终替换）
plus-ui/env/.env                                         → VITE_APP_ID、VITE_APP_TITLE
plus-ui/env/.env.production                              → VITE_APP_BASE_API 路径
plus-ui/package.json                                     → "name"、"description"
plus-ui/index.html                                       → <title>、logo-text
plus-ui/src/systemConfig.ts                              → 默认值 fallback
plus-ui/src/layouts/components/Sidebar/Logo.vue          → APP_TITLE

# 移动端 (plus-uniapp)（仅保留时替换）
plus-uniapp/env/.env                                     → VITE_APP_ID、VITE_APP_TITLE
plus-uniapp/env/.env.production                          → VITE_APP_BASE_API
plus-uniapp/package.json                                 → "name"、"description"
plus-uniapp/index.html                                   → <title>
plus-uniapp/manifest.config.ts                           → description
plus-uniapp/pages.config.mts                             → navigationBarTitleText
plus-uniapp/src/pages.json                               → navigationBarTitleText
plus-uniapp/src/manifest.json                            → name、description、title

# APP 端 (plus-app)（仅保留时替换）
plus-app/env/.env                                        → VITE_APP_ID、VITE_APP_TITLE
plus-app/env/.env.production                             → VITE_APP_BASE_API
plus-app/package.json                                    → "name"、"description"
plus-app/index.html                                      → <title>
plus-app/manifest.json                                   → name、description
plus-app/pages.json                                      → navigationBarTitleText
plus-app/pages.config.ts                                 → navigationBarTitleText

# 文档（始终替换）
README.md                                                → 项目名称
```

### 端口替换清单（精确位置）

```
# 旧端口取决于分支：master=5500, single=5504, workflow=5503
ruoyi-admin/src/main/resources/application.yml           → server.port 默认值、springdoc 注释
ruoyi-admin/src/main/resources/application-dev.yml       → app.base-api
plus-ui/env/.env.development                             → VITE_APP_BASE_API_PORT

# 以下仅保留对应子项目时替换
plus-uniapp/env/.env.development                         → VITE_APP_BASE_API URL 中的端口
plus-app/env/.env.development                            → VITE_APP_BASE_API URL 中的端口
```

### 不需要替换的文件

以下文件包含 `ryplus_uni` 但**不应替换**（技能文档中的示例引用）：

```
.claude/skills/*/SKILL.md                                → 技能文档示例
.claude/commands/*.md                                    → 命令文档示例
.agents/skills/*/SKILL.md                                 → 技能文档示例
```

---

## 注意事项

### 1. 模板仓库保持不变

这是新流程的核心原则。所有修改操作都在新目录中进行，模板仓库仅作为只读源。好处：
- 可反复创建新项目，无需重新克隆
- 模板仓库可随时拉取上游更新
- 多个新项目可共用同一个模板

### 2. Git 历史策略

**默认行为**：新项目使用 `git init` 创建全新历史。

**如果用户明确要求保留框架历史**，可改用克隆方式：
```bash
# 仅在用户明确要求时使用
git clone --single-branch -b {branch} {模板目录} {新目录}
cd {新目录}
git remote remove origin
git remote add origin {用户的仓库地址}
```
但默认不推荐，因为框架历史对新项目意义不大。

### 3. 替换顺序

1. **先复制目录** → 创建干净的新项目目录
2. **再全局替换** → 替换标识符和端口
3. **再初始提交** → git add + commit
4. **再推送代码** → push 到远程仓库
5. **最后导入数据库** → 使用已替换的 SQL 文件

### 4. SQL 文件中的标识符

SQL 文件中的 `ryplus_uni` 出现在数据内容中（如 OSS bucket 名称），替换后 SQL 仍然合法。

### 5. SnailJob 独立数据库

SnailJob（定时任务调度器）建议使用独立数据库 `{标识符}_snailjob`，避免与主业务数据混在一起。

### 6. 多环境配置

替换时注意 `.env`、`.env.development`、`.env.production` 三个环境文件都要处理：
- `.env` — 通用配置（标识符）
- `.env.development` — 开发环境（本地端口、API 地址）
- `.env.production` — 生产环境（域名、API 路径）

### 7. 生产环境 API 路径

生产环境的 API 路径格式为 `/{标识符}`（如 `/mall_shop`），用于 nginx 反向代理区分不同项目。

### 8. Windows 兼容性

- 使用 `git archive` 导出文件，Git Bash 自带，无需额外工具
- 路径使用正斜杠 `/`（Git Bash 环境）
- 不使用 `> nul` 重定向，使用 `> /dev/null 2>&1`

---

## 常见问题

### Q1: 标识符可以包含横线吗？

**A:** 不建议。标识符用于数据库名、Redis 前缀等，横线在某些场景下需要转义。建议用下划线 `_`。

### Q2: 模板仓库有本地修改怎么办？

**A:** `git archive` 从 Git 仓库中导出已提交的文件，不受工作区修改影响，模板仓库完全不会被改动。

### Q3: 替换后如何验证？

**A:** Step 2.5 提供了全局搜索命令。排除 `.claude/` 和 `.codex/` 目录后不应有任何旧标识符残留。

### Q4: 数据库连接失败怎么办？

**A:** 提供手动导入步骤，让用户通过 Navicat/DBeaver/命令行自行导入。确认导入成功后继续。

### Q5: 前端端口冲突怎么办？

**A:** 开发环境默认端口 80，如果冲突可改为 81、82 或其他。修改 `plus-ui/env/.env.development` 中的 `VITE_APP_PORT`。

### Q6: 新目录已存在怎么办？

**A:** Step 1.2 会检测目标目录是否存在。如果已存在，提示用户确认是否覆盖或使用其他标识符。

### Q7: 可以同时创建多个项目吗？

**A:** 可以。每次运行初始化流程都会创建一个新的同级目录，互不影响。模板仓库始终不变。

### Q8: 如何更新已创建的项目？

**A:** 已创建的项目与模板仓库是独立的 Git 仓库，不会自动同步更新。如需同步框架更新，参考 `project-migration` 技能。
