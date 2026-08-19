
# 业务模块裁剪指南（module-strip）

## 概述

在 **交付目录**（不是主项目）里语义级裁剪指定业务模块——删目录 + 改 pom.xml，让交付包不带客户不需要的功能。

**作用域硬约束**：
- ✅ 只动交付目录（`../ruoyi-plus-uniapp-delivery` 等）
- ❌ **绝不动主项目**（主项目永远保留全集，不同客户裁出不同子集）

**与 delivery-sync 的协作**：

```
1. /sync-delivery        → 主项目 → 交付目录（文件级镜像，按 .deliveryignore 排除）
                  ↓
2. /strip-modules        → 交付目录里裁剪指定模块（语义级，处理 pom.xml/SQL 关联）
                  ↓
3. mvn compile 验证      → 确认裁剪后仍能编译
```

**配置存储**：复用 `.delivery-sync.json` 的 `stripModules` 字段，不引入新的状态文件。

---

## 与 delivery-sync 的边界

| 场景 | 用 delivery-sync | 用 module-strip |
|------|----------------|----------------|
| 排除整个端目录（plus-uniapp/） | ✅ `.deliveryignore` | ❌ 杀鸡用牛刀 |
| 排除 IDE 配置 / 内部文档 | ✅ `.deliveryignore` | ❌ 不归它管 |
| 删商城（连带改 pom.xml/SQL） | ❌ 做不到 | ✅ |
| 删支付（连带 pay-core/wechat/alipay） | ❌ 做不到 | ✅ |

**判断规则**：删除"目录"+"修改 pom.xml/SQL"组合 = module-strip。仅"删目录" = delivery-sync。

---

## 文件结构

```
.claude/skills/module-strip/
├── SKILL.md                    # 本文档
├── scripts/
│   └── module_strip.py        # 实施脚本
└── presets/                    # 模块裁剪预设（JSON）
    ├── mall.json              # 商城（ruoyi-mall + b_mall_* 表）
    ├── iot.json               # 物联网（ruoyi-common-mqtt + iot_* 表）
    ├── pay.json               # 支付（ruoyi-common-pay-* + payment 模块）
    ├── ai.json                # AI（ruoyi-common-langchain4j + ai_* 表）
    └── crm.json               # 客户关系（crm_* 表）
```

---

## 预设清单格式

每个 `presets/{module}.json` 描述一个模块的完整裁剪指令：

```json
{
  "name": "商城",
  "description": "完整商城模块（ruoyi-mall 后端 + plus-ui 商城前端 + 移动端商城页面）",
  "delete_dirs": [
    "ruoyi-modules/ruoyi-mall",
    "plus-ui/src/views/business/mall",
    "plus-ui/src/api/business/mall",
    "plus-uniapp/src/api/mall",
    "plus-app/api/mall"
  ],
  "delete_files": [
    "script/sql/mall-init.sql"
  ],
  "remove_pom_lines": [
    {
      "file": "ruoyi-modules/pom.xml",
      "patterns": ["<module>ruoyi-mall</module>"]
    },
    {
      "file": "ruoyi-admin/pom.xml",
      "patterns": [
        "<artifactId>ruoyi-mall</artifactId>"
      ]
    }
  ],
  "manual_actions": [
    "从 script/sql/ry_plus_new.sql 移除以 m_ 开头的建表语句和相关菜单初始化",
    "检查前端 plus-ui/src/router/business.ts 是否有静态 import mall 路由",
    "检查 plus-uniapp/src/pages.json 中 mall 子包配置"
  ],
  "depends_on": []
}
```

**字段说明**：

| 字段 | 必填 | 含义 |
|------|------|------|
| `name` | ✅ | 模块中文名（用于报告） |
| `description` | ✅ | 一句话说明 |
| `delete_dirs` | ✅ | 要整个删除的目录（相对交付目录根） |
| `delete_files` | ⚠️ | 要删除的单文件 |
| `remove_pom_lines` | ⚠️ | 从指定 pom.xml 删除匹配 pattern 的行 |
| `manual_actions` | ⚠️ | 脚本无法处理、需用户手动检查的项 |
| `depends_on` | ⚠️ | 依赖其他模块（如 `pay` 依赖某个核心模块） |

`remove_pom_lines` 的 pattern 是字符串包含匹配（不是正则），脚本会**整行删除**任何包含该字符串的行。

---

## 触发命令

```
/strip-modules                       # 裁剪 .delivery-sync.json 中 stripModules 列表的所有模块
/strip-modules mall iot              # 临时指定模块（覆盖配置）
/strip-modules --list                # 列出所有可用预设
/strip-modules --preview             # 预览要做的改动，不实际执行
/strip-modules --verify              # 裁剪后跑 mvn compile 验证
/strip-modules --rollback            # 重新跑 /sync-delivery 恢复（无独立回滚机制）
```

---

## 配置示例

`.delivery-sync.json` 增加 `stripModules`：

```json
{
  "deliveryDir": "../ruoyi-plus-uniapp-delivery",
  "lastSyncCommit": "ce1607f8b",
  "stripModules": ["mall", "iot"],
  "presets": {
    "default": { "extraExclude": [] },
    "客户A-纯OA": {
      "extraExclude": ["plus-uniapp/", "plus-app/"],
      "stripModules": ["mall", "iot", "pay", "ai", "crm"]
    },
    "客户B-商城版": {
      "extraExclude": [],
      "stripModules": ["iot", "ai"]
    }
  }
}
```

> **重要**：`stripModules` 配在预设里时（如 "客户A-纯OA"），切到该预设会自动应用对应的裁剪。

---

## 工作流程

### 标准流程（/sync-delivery 之后）

```
1. /sync-delivery         （生成/更新交付目录）
2. /strip-modules         （裁剪 stripModules 列表的模块）
3. /strip-modules --verify（mvn compile 验证）
```

或一行式（顺序执行）：

```
/sync-delivery && /strip-modules --verify
```

### 脚本内部流程

```
1. 读 .delivery-sync.json，确认有 stripModules（或命令行临时指定）
2. 检查交付目录存在 + .delivery-sync-marker 存在（确认是受管目录）
3. 对每个模块：
   a. 加载 presets/{module}.json
   b. 检查 depends_on 链路（防止漏裁）
   c. 删 delete_dirs（rmtree）
   d. 删 delete_files
   e. 处理 remove_pom_lines（逐行扫描，删匹配行）
   f. 收集 manual_actions 到报告末尾
4. （可选 --verify）在交付目录跑 mvn compile
5. 输出报告：
   - 每个模块删了多少文件 / 改了多少 pom 行
   - manual_actions 待办清单
   - 编译结果（如启用 --verify）
```

### 安全检查

| 检查 | 触发条件 | 处置 |
|------|---------|------|
| 交付目录不存在 | 路径不存在 | 提示先 /sync-delivery |
| 缺 `.delivery-sync-marker` | 误指向无关目录 | 拒绝执行 |
| 预设文件缺失 | `presets/{module}.json` 不存在 | 报错并列出可用预设 |
| 删除目录不存在 | 已被删过/路径错 | 跳过并 warning（不报错） |
| pom.xml 删行后语法错 | XML 不再合法 | --verify 时由 mvn 拦截 |

---

## 内置模块预设说明

### `mall.json` - 商城

| 涉及内容 | 路径 |
|---------|------|
| 后端整模块 | `ruoyi-modules/ruoyi-mall/` |
| 父 pom 引用 | `ruoyi-modules/pom.xml` 中 `<module>ruoyi-mall</module>` |
| admin 依赖 | `ruoyi-admin/pom.xml` 中 `<artifactId>ruoyi-mall</artifactId>` |
| 前端 | `plus-ui/src/views/business/mall/`、`plus-ui/src/api/business/mall/` |
| 移动端 | `plus-uniapp/src/api/mall/`、`plus-app/api/mall/` |

### `iot.json` - 物联网

| 涉及内容 | 路径 |
|---------|------|
| 后端业务 | `ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/iot/` |
| common 依赖 | 不删 ruoyi-common-mqtt（可能被业务复用） |
| 前端 | `plus-ui/src/views/business/iot/` |
| 移动端 | `plus-uniapp/src/api/iot/`、`plus-app/api/iot/` |

### `pay.json` - 支付

| 涉及内容 | 路径 |
|---------|------|
| common 模块 | `ruoyi-common/ruoyi-common-pay/` 整个父模块（含 5 个子模块） |
| 业务模块 | `ruoyi-modules/ruoyi-business/.../payment/` |
| pom 引用 | 多处 `<artifactId>ruoyi-common-pay-*</artifactId>` |
| 前端 | `plus-ui/src/views/business/pay/` |

⚠️ **支付涉及 5 个子模块（pay-core/wechat/alipay/unionpay/balance）**，预设统一处理。

### `ai.json` - AI 对话

| 涉及内容 | 路径 |
|---------|------|
| common 模块 | `ruoyi-common/ruoyi-common-langchain4j/` |
| 业务模块 | `ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/ai/` |
| 前端 | `plus-ui/src/views/business/ai/`（含 AAi* 组件） |
| pom 引用 | 多处 `<artifactId>ruoyi-common-langchain4j</artifactId>` |

---

## 用户决策点

每次执行 `/strip-modules` 前，必须确认：

- [ ] 已先执行过 `/sync-delivery`（交付目录已是最新版本）
- [ ] 该模块**真的不需要**（裁完不能恢复，只能重新 sync-delivery 拉回完整版）
- [ ] manual_actions 清单中的人工步骤已知晓（脚本不会自动处理 SQL/前端路由）
- [ ] 准备执行 `--verify` 验证编译

---

## 不做的事（边界声明）

- ❌ **不动主项目**（主项目永远保留全集）
- ❌ **不处理 SQL 内容**（SQL 是大段文本，难以精准识别"哪几条 INSERT 属于该模块"，列入 manual_actions）
- ❌ **不处理前端路由**（vue-router 动态导入不会构建失败，删了文件路由自动失效，无需改路由表）
- ❌ **不做"反向裁剪"**（删完无法回滚，要回退就重新 sync-delivery 拉一份）
- ❌ **不处理菜单数据**（菜单 SQL 由用户在 manual_actions 提示后手动处理）

---

## 实战示例

### 示例 1：客户只要基础 OA 功能（无商城/IoT/支付/AI）

```bash
# 1. 配置预设
# 编辑 .delivery-sync.json：
#   "activePreset": "客户A-纯OA"
#   "客户A-纯OA": { "stripModules": ["mall", "iot", "pay", "ai", "crm"] }

# 2. 同步交付目录
/sync-delivery --preset 客户A-纯OA

# 3. 裁剪模块
/strip-modules --verify

# 输出：
# 模块裁剪报告
# ├── mall    删除 152 文件 / 改 2 个 pom.xml
# ├── iot     删除 47 文件 / 改 0 个 pom.xml
# ├── pay     删除 89 文件 / 改 6 个 pom.xml
# ├── ai      删除 23 文件 / 改 2 个 pom.xml
# └── crm     删除 31 文件 / 改 1 个 pom.xml
# 
# 编译验证: ✅ mvn compile 退出码 0
# 
# 待手动处理（manual_actions）:
# 1. 从 script/sql/ry_plus_new.sql 移除 m_/iot_/crm_ 开头的建表
# 2. ...
```

### 示例 2：临时只裁掉 AI（不动配置）

```bash
/strip-modules ai --verify
```

### 示例 3：先看看会改什么再决定

```bash
/strip-modules --preview
# 输出 dry-run 风格的预览，不实际执行
```

---

## 添加新预设的流程

需要为新业务模块（如 `ticket` 工单系统）添加裁剪预设时：

1. 在主项目里找出该模块涉及的所有路径（目录、pom 引用）
2. 写 `presets/ticket.json`
3. 测试：先在交付目录手动备份，跑 `/strip-modules ticket --preview` 看清单
4. 实际执行：`/strip-modules ticket --verify`
5. 验证 mvn compile 通过

---

## 触发自检

执行前必查：
- [ ] 交付目录存在（`.delivery-sync.json` 中 `deliveryDir` 路径有效）
- [ ] 交付目录根有 `.delivery-sync-marker`（确认是受管目录）
- [ ] 要裁剪的模块在 `presets/` 下有对应 JSON
- [ ] 主项目当前的 git status 不会被脚本影响（脚本不动主项目）

执行后必查：
- [ ] 交付目录的指定模块目录已删除
- [ ] pom.xml 中相关引用已移除
- [ ] （如启用 --verify）`mvn compile` 通过
- [ ] manual_actions 清单已展示给用户

---

## 与其他技能的关系

| 关系 | 技能 | 说明 |
|------|------|------|
| 上游 | `delivery-sync` | 必须先生成交付目录 |
| 下游 | （无） | 终点 |
| 替代 | `delivery-sync` 的预设 `extraExclude` | 仅删目录、不改 pom 时使用 extraExclude 即可 |
| 关联 | `deployment-guide` | 裁完模块后部署文档可能需要相应调整 |
