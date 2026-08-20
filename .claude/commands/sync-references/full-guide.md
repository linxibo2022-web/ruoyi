
# /sync - 同步项目文档

全量同步项目管理的三个核心文档，确保数据一致性。

## 🎯 功能概述

- 全量扫描代码和 Git 记录
- 同步三个核心文档的数据
- 检测并修复文档间的不一致
- 生成完整的同步报告

---

## 📋 核心文档

| 文档 | 路径 | 用途 |
|------|------|------|
| 项目状态 | `docs/项目状态.md` | 模块进度、里程碑、活动记录 |
| 待办清单 | `docs/待办清单.md` | TODO/FIXME、任务优先级 |
| 需求文档 | `docs/需求文档.md` | 功能需求、验收标准 |

---

## 📋 执行流程

### 第一步：检查文档状态

```bash
# 检查三个核心文档
FILES=(
    "docs/项目状态.md"
    "docs/待办清单.md"
    "docs/需求文档.md"
)

for file in "${FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "⚠️ 缺少文档: $file"
        MISSING_DOCS+=("$file")
    fi
done

# 如果有缺失，询问是否创建
if [ ${#MISSING_DOCS[@]} -gt 0 ]; then
    echo "💡 建议先运行 /init-docs 创建缺失的文档"
fi
```

### 第二步：扫描代码结构

```bash
# 扫描后端模块
BACKEND_MODULES=()
for pom in ruoyi-modules/ruoyi-*/pom.xml; do
    MODULE_NAME=$(dirname "$pom" | xargs basename)
    # 排除框架模块
    if [[ ! "$MODULE_NAME" =~ ^ruoyi-(common|admin|extend|system|generator)$ ]]; then
        BACKEND_MODULES+=("$MODULE_NAME")
    fi
done

# 统计每个模块的实体数量
for module in "${BACKEND_MODULES[@]}"; do
    ENTITY_COUNT=$(find "ruoyi-modules/$module" -path "*/domain/*.java" \
        ! -name "*Bo.java" ! -name "*Vo.java" | wc -l)
    CONTROLLER_COUNT=$(find "ruoyi-modules/$module" -name "*Controller.java" | wc -l)
    SERVICE_COUNT=$(find "ruoyi-modules/$module" -name "*ServiceImpl.java" | wc -l)
done

# 扫描前端页面
PC_PAGES=$(find plus-ui/src/views/business -name "*.vue" | wc -l)
PC_APIS=$(find plus-ui/src/api/business -name "*Api.ts" | wc -l)

# 扫描移动端页面
MOBILE_PAGES=$(find plus-uniapp/src/pages -name "*.vue" | wc -l)
MOBILE_SUB_PAGES=$(find plus-uniapp/src/pages-sub -name "*.vue" | wc -l)
MOBILE_APIS=$(find plus-uniapp/src/api -name "*Api.ts" | wc -l)
```

### 第三步：扫描 TODO/FIXME

```bash
# 扫描业务模块 TODO
BUSINESS_PATHS=(
    "ruoyi-modules/ruoyi-business/"
    "ruoyi-modules/ruoyi-iot/"
    "ruoyi-modules/ruoyi-mall/"
    "ruoyi-modules/ruoyi-crm/"
    "ruoyi-modules/ruoyi-erp/"
)

TODO_LIST=()
FIXME_LIST=()

for path in "${BUSINESS_PATHS[@]}"; do
    if [ -d "$path" ]; then
        while IFS= read -r line; do
            TODO_LIST+=("$line")
        done < <(grep -rn "TODO" "$path" --include="*.java")

        while IFS= read -r line; do
            FIXME_LIST+=("$line")
        done < <(grep -rn "FIXME" "$path" --include="*.java")
    fi
done

# 扫描前端
while IFS= read -r line; do
    TODO_LIST+=("$line")
done < <(grep -rn "TODO\|FIXME" plus-ui/src/views/business/ --include="*.vue" --include="*.ts")

# 扫描移动端
while IFS= read -r line; do
    TODO_LIST+=("$line")
done < <(grep -rn "TODO\|FIXME" plus-uniapp/src/ --include="*.vue" --include="*.ts")
```

### 第四步：分析 Git 记录

```bash
# 获取最近 30 天的提交统计
git log --since="30 days ago" --pretty=format:"%s" | head -30

# 按模块统计
git log --since="30 days ago" --name-only --pretty=format: | \
    grep -E "^ruoyi-modules/ruoyi-(business|iot|mall|crm|erp)" | \
    cut -d'/' -f2 | sort | uniq -c | sort -rn

# 获取最近提交
RECENT_COMMITS=$(git log --oneline -10)
```

### 第五步：检测文档冲突

```bash
# 比较项目状态中的模块列表与实际代码
# 检测已删除但文档中仍存在的模块
# 检测新增但文档中未记录的模块

# 比较待办清单中的 TODO 与代码中的 TODO
# 检测已完成但未标记的 TODO
# 检测新增的 TODO
```

### 第六步：更新文档

```bash
CURRENT_TIME=$(date '+%Y-%m-%d %H:%M:%S')

# 1. 更新项目状态
# - 更新时间戳
# - 更新模块统计
# - 添加最近活动

# 2. 更新待办清单
# - 同步新的 TODO/FIXME
# - 标记已完成的项目

# 3. 更新需求文档（如果有新模块）
# - 添加新模块的功能描述
```

---

## 📊 输出格式

```markdown
## 📊 项目文档同步报告

**同步时间**: 2026-01-21 15:30:00
**扫描范围**: 全量

---

### 📈 代码统计

#### 后端模块

| 模块 | Entity | Controller | Service | DAO | 完整度 |
|------|--------|------------|---------|-----|--------|
| ruoyi-business | 8 | 8 | 8 | 8 | 100% |
| ruoyi-mall | 5 | 5 | 5 | 5 | 100% |
| **合计** | **13** | **13** | **13** | **13** | **100%** |

#### 前端页面

| 端 | 页面数 | API数 | 状态 |
|----|--------|-------|------|
| PC端 | 15 | 12 | 🟢 正常 |
| 移动端 | 10 | 8 | 🟢 正常 |

---

### 🔍 TODO/FIXME 统计

| 类型 | 后端 | 前端 | 移动端 | 合计 |
|------|------|------|--------|------|
| TODO | 5 | 3 | 2 | 10 |
| FIXME | 1 | 1 | 0 | 2 |

**新发现**（本次同步新增）：
- `[TODO]` 完善权限校验 - `UserServiceImpl.java:45`
- `[FIXME]` 修复并发问题 - `OrderServiceImpl.java:128`

---

### 📝 Git 活动（最近 30 天）

**提交统计**：45 次提交

| 模块 | 提交数 | 占比 |
|------|--------|------|
| ruoyi-business | 25 | 56% |
| plus-ui | 12 | 27% |
| plus-uniapp | 8 | 17% |

**最近提交**：
- `abc1234` 完成用户反馈功能
- `def5678` 修复登录 Bug
- `ghi9012` 优化查询性能

---

### ⚠️ 文档冲突检测

| 问题 | 详情 | 处理方式 |
|------|------|---------|
| 🔴 模块不存在 | 项目状态中记录了 `ruoyi-pay`，但代码中不存在 | 已删除记录 |
| 🟡 新模块未记录 | 发现新模块 `ruoyi-crm`，未在项目状态中 | 已添加 |
| 🟢 TODO 已完成 | 待办清单中的 3 个 TODO 在代码中已删除 | 已标记完成 |

---

### ✅ 同步结果

| 文档 | 更新项 | 状态 |
|------|--------|------|
| 项目状态.md | 时间戳、模块统计、活动记录 | ✅ 已更新 |
| 待办清单.md | 新增 2 条、完成 3 条 | ✅ 已更新 |
| 需求文档.md | 添加 crm 模块描述 | ✅ 已更新 |

---

### 💡 建议

1. **紧急**：处理 2 个 FIXME
2. **重要**：清理 10 个 TODO
3. **建议**：完善 crm 模块的需求描述
```

---

## ⚠️ 注意事项

### 1. 扫描范围

**只扫描业务模块**：
```
✅ ruoyi-business
✅ ruoyi-iot
✅ ruoyi-mall
✅ ruoyi-crm
✅ ruoyi-erp
```

**不扫描框架模块**：
```
❌ ruoyi-common（通用工具）
❌ ruoyi-admin（启动模块）
❌ ruoyi-extend（扩展模块）
❌ ruoyi-system（系统模块）
❌ ruoyi-generator（代码生成）
```

### 2. 冲突处理

| 冲突类型 | 处理方式 |
|---------|---------|
| 文档中有、代码中无 | 询问用户是否删除 |
| 代码中有、文档中无 | 自动添加 |
| TODO 已删除 | 自动标记完成 |

### 3. 备份机制

同步前会自动备份原文档到 `docs/.backup/` 目录。

---

## 📖 示例对话

### 示例 1：全量同步

```
用户: /sync

AI:
正在执行全量同步...

📂 扫描代码结构...
  - 后端模块: 2 个
  - 前端页面: 15 个
  - 移动端页面: 10 个

🔍 扫描 TODO/FIXME...
  - TODO: 10 个
  - FIXME: 2 个

📝 分析 Git 记录...
  - 最近 30 天: 45 次提交

⚠️ 检测到冲突...
  - 发现 1 个新模块未记录
  - 发现 3 个已完成的 TODO

✅ 同步完成！

[输出详细报告...]
```

### 示例 2：文档缺失

```
用户: /sync

AI:
⚠️ 检测到缺少文档:
  - docs/项目状态.md
  - docs/待办清单.md

是否先创建缺失的文档？

1. **创建空白模板** - 运行 /init-docs
2. **扫描代码生成** - 运行 /init-docs --scan
3. **跳过同步** - 手动创建后再同步

请选择（1/2/3）：
```

---

## 🎯 与其他命令的配合

| 场景 | 推荐流程 |
|------|---------|
| 每周同步 | `/sync` → 查看报告 → 处理问题 |
| 项目初始化 | `/init-docs --scan` → `/sync` |
| 功能开发后 | `/dev` 完成 → `/sync` 同步 |
| 版本发布前 | `/sync` → `/check` → `/progress` |

---

## 🎉 总结

`/sync` 命令帮助您：
- ⚡ 全量扫描代码和文档
- 🔍 检测文档间的不一致
- 📊 生成完整的同步报告
- ✅ 保持三个核心文档的数据一致性
