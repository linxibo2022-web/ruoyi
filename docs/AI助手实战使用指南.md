# AI 助手实战使用指南

> 本文档介绍如何使用 AI 编程助手的命令、技能和工具进行 **ruoyi-plus-uniapp** 全栈三端项目开发。
>
> 本项目支持 **后端（Java）+ PC 端（Vue 3）+ 移动端（UniApp）** 一体化开发。

---

## 与 RuoYi-Vue-Plus 的关键差异

> **警告**：本项目 **不是** ruoyi-vue-plus！AI 已被严格约束遵循本项目规范。

| 对比维度 | RuoYi-Vue-Plus | ruoyi-plus-uniapp（本项目） |
|---------|---------------|---------------------------|
| **前端** | PC 端（plus-ui） | PC 端 + **移动端**（plus-uniapp） |
| **后端架构** | 三层（Controller → Service → Mapper） | **四层**（Controller → Service → **DAO** → Mapper） |
| **包名** | `org.dromara.*` | `plus.ruoyi.*` |
| **Service** | 继承 `ServiceImpl` | **不继承**任何基类 |
| **查询构建** | Service 层直接用 `LambdaQueryWrapper` | **DAO 层** 的 `buildQueryWrapper()` |
| **技能数量** | 33 个 | **44 个**（含移动端、IoT、消息队列等） |
| **Common 模块** | ~20 个 | **36 个**（含 MQTT、RocketMQ、多种支付等） |
| **数据库** | MySQL | MySQL + Oracle + PostgreSQL + **SQL Server** |

---

## 快速入门

### 第一步：了解项目

```
/start
```

AI 会自动扫描项目结构，识别三端（后端 + PC + 移动端）的已有模块，给出项目概览。

### 第二步：开始开发

根据需求选择合适的命令：

| 场景 | 命令 | 说明 |
|------|------|------|
| 从零开发新功能 | `/dev` | 完整流程：需求分析 → 建表 → 后端 + 前端 + 移动端代码生成 |
| 表已存在，生成代码 | `/crud` | 快速生成四层架构 CRUD 代码 |
| 查看项目进度 | `/progress` | 梳理各模块完成情况 |
| 不知道下一步做什么 | `/next` | 获取开发建议 |

### 第三步：检查和收尾

```
/check          → 全栈代码规范检查（后端 + 前端 + 移动端）
/update-status  → 更新项目状态文档
```

---

## 实战案例：开发"优惠券管理"功能

### 场景描述

需要开发一个优惠券管理功能，包含：
- 优惠券模板管理（PC 后台配置）
- 优惠券发放记录
- 移动端领券中心

### Step 1：启动开发流程

```
/dev
```

AI 会引导你完成：
1. 确认功能名称和所属模块（base / mall / crm / iot）
2. 检查是否存在重复功能
3. 设计数据库表结构（雪花 ID，多租户字段）
4. 生成完整的**四层架构**代码（Controller → Service → DAO → Mapper）
5. 生成 PC 端前端代码（API + Types + 页面）
6. 生成移动端代码（API + Types + 页面）

### Step 2：确认需求

AI 会问你：

```
请告诉我要开发的功能：
1. 功能名称？（如：优惠券管理）
2. 所属模块？（base/mall/crm/iot）
3. 需要哪些端？（PC端/移动端/两者都要）
```

你回答：

```
功能名称：优惠券管理
所属模块：mall（商城模块）
需要：PC 端管理 + 移动端领券
```

### Step 3：AI 自动执行

AI 会自动完成：
- 设计表结构（`m_coupon_template`, `m_coupon_record`，表前缀 `m_` 对应 mall 模块）
- 创建字典（优惠券类型、状态等）
- 生成菜单 SQL
- 生成后端四层架构代码：

```
ruoyi-business/src/main/java/plus/ruoyi/business/mall/
├── controller/CouponTemplateController.java
├── service/ICouponTemplateService.java
├── service/impl/CouponTemplateServiceImpl.java   ← 不继承基类
├── dao/ICouponTemplateDao.java
├── dao/impl/CouponTemplateDaoImpl.java           ← buildQueryWrapper()
├── mapper/CouponTemplateMapper.java
└── domain/
    ├── CouponTemplate.java                        ← 继承 TenantEntity
    ├── bo/CouponTemplateBo.java                   ← @AutoMappers
    └── vo/CouponTemplateVo.java
```

- 生成 PC 端代码：

```
plus-ui/src/api/business/mall/couponTemplate/
├── couponTemplateApi.ts     ← http/Result/PageResult 已自动导入
└── couponTemplateTypes.ts   ← Query/Bo/Vo 类型定义
```

- 生成移动端代码：

```
plus-uniapp/src/api/app/coupon/
├── couponApi.ts
└── couponTypes.ts
```

### Step 4：对已有表快速生成代码

如果表已存在，可以跳过 `/dev` 直接使用：

```
/crud
```

AI 会生成完整的四层架构代码：
- Entity（继承 `TenantEntity`，`@TableName("m_coupon_template")`）
- BO（`@AutoMappers` 映射）
- VO（视图对象）
- Mapper（继承 `BaseMapper<Entity>`）
- DAO 接口 + 实现（`buildQueryWrapper()` 核心方法）
- Service 接口 + 实现（**不继承** ServiceImpl）
- Controller（标准 RESTful API，路径如 `/pageCouponTemplates`）

### Step 5：检查代码规范

```
/check
```

AI 会检查三端代码是否符合项目规范：
- 后端：包名 `plus.ruoyi.*`、四层架构完整性、`MapstructUtils.convert()` 使用
- PC 端：是否使用 `AForm*` 封装组件（禁止 `el-input`）、`[err, data]` API 调用格式
- 移动端：是否使用 WD UI 组件（禁止 `uni-forms`）、`import from '@/wd'`

---

## 命令速查表

### 核心开发命令

| 命令 | 用途 | 使用时机 |
|------|------|---------|
| `/start` | 项目快速了解 | 首次接触项目、新会话开始 |
| `/dev` | 从零开发新功能 | 表不存在，需要完整流程 |
| `/crud` | 快速生成 CRUD | 表已存在，只需生成代码 |
| `/check` | 全栈代码规范检查 | 开发完成后检查 |
| `/progress` | 查看项目进度 | 了解各模块完成情况 |
| `/next` | 下一步建议 | 不知道该做什么 |

### 文档管理命令

| 命令 | 用途 | 使用时机 |
|------|------|---------|
| `/init-docs` | 初始化项目文档 | 新项目首次配置（支持空白模板 / 扫描代码） |
| `/update-status` | 更新项目状态 | 完成功能后更新 |
| `/add-todo` | 添加待办事项 | 记录待完成任务 |
| `/sync` | 同步项目文档 | 定期整理项目文档 |

### 上游同步命令（本项目独有）

| 命令 | 用途 | 使用时机 |
|------|------|---------|
| `/sync-local` | 上游代码同步 | 同步上游 RuoYi-Vue-Plus 代码更新 |
| `/sync-wot-local` | WOT Design Uni 同步 | 同步 WD UI 组件库更新 |
| `/sync-unibest-local` | Unibest 框架同步 | 同步 Unibest 移动端框架更新 |

---

## 技能系统

AI 会根据你的问题**自动评估并激活**相关技能，无需手动调用。每个技能包含该领域的最佳实践、代码模板和规范约束。

### 自动激活机制

本项目配置了 **强制技能评估 Hook**（`skill-forced-eval.js`），确保：
- 每次提问时自动评估匹配的技能（激活率 90%+）
- 匹配的技能会被**逐个串行激活**
- 所有技能加载完毕后才开始实现

> 你只需要自然地描述需求，不需要关心技能如何激活。

### 常见问题 → 技能映射

| 你说的话 | 自动激活技能 | 作用 |
|---------|-------------|------|
| "帮我开发用户管理模块" | `crud-development` | 四层架构 CRUD 开发规范 |
| "怎么设计这个表" | `database-ops` | 数据库设计规范（多库兼容） |
| "这个接口怎么设计" | `api-development` | RESTful API 设计规范 |
| "接口响应慢怎么优化" | `performance-doctor` | 性能优化指南 |
| "这个报错怎么解决" | `bug-detective` | Bug 排查方法论 |
| "怎么加数据权限" | `data-permission` | 行级数据权限控制 |
| "怎么用 Redis 缓存" | `redis-cache` | 缓存策略和分布式锁 |
| "怎么写单元测试" | `test-development` | JUnit5 + Mockito 测试规范 |
| "怎么发短信/邮件" | `notification-system` | 短信 + 邮件 + 统一消息推送 |
| "怎么接入微信登录" | `social-login` | OAuth2 第三方登录 |
| "定时任务怎么写" | `scheduled-jobs` | @Scheduled + SnailJob + 延迟队列 |
| "怎么处理多租户" | `multi-tenant` | 多租户数据隔离方案 |
| "文件上传怎么做" | `file-oss-management` | OSS 文件存储 |
| "怎么用 MapstructUtils" | `utils-toolkit` | 全栈工具类使用指南 |
| "JSON 精度丢失怎么办" | `json-serialization` | JSON 序列化规范 |
| "WebSocket 怎么用" | `realtime-communication` | WebSocket + SSE 实时通信 |
| "怎么接入 AI 大模型" | `ai-langchain4j` | LangChain4j AI 集成 |
| "移动端页面怎么写" | `ui-mobile` | WD UI 组件库指南 |
| "移动端页面不够大气" | `ui-design-mobile` | 移动端设计思维指南 |
| "怎么用 RocketMQ" | `message-queue` | 消息队列开发 |
| "怎么对接 IoT 设备" | `iot-mqtt` | MQTT 物联网通信 |
| "怎么调用高德地图 API" | `third-party-api` | 第三方 API 集成 |
| "怎么加国际化" | `i18n-development` | 全栈国际化方案 |

### 技能完整分类（共 44 个）

#### 后端开发类（8 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `crud-development` | CRUD、增删改查、Entity、Service、DAO | **四层架构** CRUD 开发规范 |
| `api-development` | API、接口设计、RESTful | API 路径和响应规范 |
| `database-ops` | 建表、SQL、字典、菜单 | 数据库设计（多库兼容） |
| `backend-annotations` | 注解、@SerialMap、@RateLimiter | 框架注解使用指南 |
| `error-handler` | 异常、ServiceException、try-catch | 全局异常处理机制 |
| `utils-toolkit` | 工具类、MapstructUtils、StringUtils | 全栈工具类速查 |
| `json-serialization` | JSON、序列化、BigDecimal 精度 | JSON 处理规范 |
| `test-development` | 单元测试、JUnit5、Mockito | 测试开发规范 |

#### 安全与权限类（4 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `security-guard` | Sa-Token、权限、加密、脱敏 | 认证授权和数据安全 |
| `data-permission` | 数据权限、部门隔离、@DataPermission | 行级数据权限控制 |
| `multi-tenant` | 多租户、TenantEntity、租户隔离 | 多租户数据隔离方案 |
| `social-login` | 微信登录、OAuth、JustAuth | 第三方社交登录 |

#### 中间件与集成类（9 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `redis-cache` | Redis、缓存、@Cacheable、分布式锁 | 缓存策略和 Redis 操作 |
| `scheduled-jobs` | 定时任务、SnailJob、@Scheduled | 定时任务开发（3 种方案） |
| `realtime-communication` | WebSocket、SSE、实时推送 | 实时通信（双向 + 单向） |
| `file-oss-management` | 文件上传、OSS、MinIO | 文件存储管理 |
| `notification-system` | 短信、邮件、消息推送 | 短信 + 邮件 + 统一消息 |
| `message-queue` | RocketMQ、消息队列、MQ | 消息队列开发 |
| `iot-mqtt` | MQTT、物联网、IoT | MQTT 设备通信 |
| `third-party-api` | 高德地图、火山引擎、Forest | 第三方 API 集成 |
| `ai-langchain4j` | AI、大模型、ChatGPT、DeepSeek | LangChain4j AI 集成 |

#### 前端开发类（2 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `ui-pc` | AForm、AModal、Element Plus 封装 | PC 端 71 个自定义组件 |
| `store-pc` | Pinia、useUserStore、状态管理 | PC 端状态管理 |

#### 移动端开发类（4 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `ui-mobile` | wd- 组件、小程序、WD UI | 移动端 99+ 组件 + 14 个 Composables |
| `ui-design-mobile` | 页面布局、间距、留白、大气 | 移动端设计思维指南 |
| `store-mobile` | useAuth、Composable、移动端 Store | 移动端状态管理 |
| `uniapp-platform` | 条件编译、ifdef、平台判断 | 多端条件编译 |

#### 微信与支付类（2 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `payment-integration` | 微信支付、支付宝、退款 | 多渠道支付集成 |
| `wechat-integration` | 小程序登录、订阅消息、JSSDK | 微信生态集成 |

#### 质量与排查类（4 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `bug-detective` | Bug、报错、异常排查 | Bug 排查方法论 |
| `performance-doctor` | 性能、慢查询、SQL 优化 | 性能诊断和优化 |
| `code-patterns` | 代码规范、命名、禁止事项 | 全栈编码规范速查 |
| `media-processing` | 图片处理、二维码、Excel | 媒体处理工具 |

#### 规划与协作类（8 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `architecture-design` | 架构设计、模块划分、重构 | 系统架构规划 |
| `tech-decision` | 技术选型、方案对比 | 技术决策分析 |
| `brainstorm` | 头脑风暴、方案设计、怎么做 | 创意方案探索 |
| `i18n-development` | 国际化、多语言、i18n | 全栈国际化方案 |
| `project-navigator` | 项目结构、文件在哪 | 项目导航和定位 |
| `git-workflow` | Git、提交、分支、合并 | Git 工作流规范 |
| `task-tracker` | 任务跟踪、记录进度 | 开发任务持久化跟踪 |
| `add-skill` | 添加技能、创建技能文档 | 扩展技能系统 |

#### 多模型协作 & 图片生成（3 个）

| 技能 | 触发词 | 说明 |
|------|-------|------|
| `collaborating-with-codex` | Codex、多模型、算法分析 | 委托 OpenAI Codex 处理任务 |
| `collaborating-with-gemini` | Gemini、前端原型、UI 设计 | 委托 Google Gemini 处理任务 |

---

## Agent 系统

项目配置了两个自动代理，会在特定场景下自动介入：

| Agent | 自动触发场景 | 作用 |
|-------|-------------|------|
| `code-reviewer` | 完成功能开发后 | 自动审查代码是否符合项目规范 |
| `project-manager` | 功能开发完成、使用 `/update-status` | 自动更新项目状态文档和待办清单 |

### code-reviewer 自动审查的内容

- 包名是否为 `plus.ruoyi.*`（不是 `com.ruoyi` 或 `org.dromara`）
- 是否遵循**四层架构**（Controller → Service → DAO → Mapper）
- Service 是否**不继承** ServiceImpl
- 查询条件是否在 **DAO 层** 的 `buildQueryWrapper()` 中构建
- BO 是否使用 `@AutoMappers`（复数形式）
- 是否使用 `MapstructUtils.convert()` 而非 `BeanUtils`
- PC 端是否使用封装组件（`AForm*` 而非 `el-input`）
- 移动端是否使用 WD UI（`wd-*` 而非 `uni-forms`）
- API 调用是否使用 `[err, data]` 格式

### project-manager 自动更新的文档

- `docs/项目状态.md` — 功能完成状态
- `docs/待办清单.md` — 待办事项变更

---

## Hook 系统（本项目独有）

本项目配置了 3 个自动化 Hook，在特定事件时自动执行：

| Hook | 文件 | 触发时机 | 作用 |
|------|------|---------|------|
| `skill-forced-eval` | `skill-forced-eval.js` | 每次用户提问 | 强制评估并激活匹配技能（激活率 90%+） |
| `pre-tool-use` | `pre-tool-use.js` | 工具调用前 | 工具使用前置检查 |
| `stop` | `stop.js` | 会话结束 | 清理和收尾操作 |

### 强制技能评估 Hook 工作原理

```
用户提问
  → Hook 注入技能评估指令
    → AI 评估匹配的技能列表
      → 逐个串行调用 Skill() 加载技能
        → 所有技能加载完毕
          → 开始实现
```

这确保了 AI 在编写任何代码之前，都会先加载相关领域的专业知识和项目规范。

---

## MCP 工具

AI 助手集成了三个 MCP 工具，通过特定触发词激活：

| 触发词 | 工具 | 用途 | 使用场景 |
|-------|------|------|---------|
| 深度分析、仔细思考、全面评估 | `sequential-thinking` | 链式推理 | 复杂架构决策、多方案对比分析 |
| 最佳实践、官方文档、标准写法 | `context7` | 查阅文档 | 查询 MyBatis-Plus/Sa-Token/Vue 3 最新用法 |
| 打开浏览器、截图、检查元素 | `chrome-devtools` | 浏览器调试 | 前端页面调试、接口测试、UI 检查 |

### 使用示例

```
"仔细思考一下这个缓存方案的优缺点"
→ AI 使用 sequential-thinking 进行多步推理

"查一下 MyBatis-Plus 最新的官方文档怎么用批量插入"
→ AI 使用 context7 查阅最新文档

"帮我打开浏览器看看这个页面的布局"
→ AI 使用 chrome-devtools 进行浏览器操作
```

> **注意**：WD UI（移动端组件库）的文档禁止通过 context7 查阅，请直接参考项目内的 `plus-uniapp/src/wd/` 目录。

---

## 多模型协作

对于特定场景，可以让 AI 将任务委托给其他模型：

### Codex 协作（后端/算法）

```
"用 Codex 分析一下这个排序算法的复杂度"
"让 Codex 帮忙审查这段代码"
"用 Codex 生成一个数据库优化方案"
```

前置要求：已安装 `npm install -g @openai/codex` 并配置 API Key。

### Gemini 协作（前端/UI）

```
"用 Gemini 帮忙设计这个页面的布局"
"让 Gemini 写一个 Vue 组件原型"
"用 Gemini 审查一下这个 CSS 样式"
```

前置要求：已安装 Gemini CLI 并配置 API Key。

> **提示**：后端任务优先使用 Codex，前端/UI 任务优先使用 Gemini。Gemini 对后端逻辑理解有缺陷。

---

## 高级用法

### 任务跟踪（跨会话恢复）

对于复杂的多步骤开发任务，可以使用任务跟踪功能：

```
"创建一个优惠券管理功能的任务跟踪"
```

AI 会在 `docs/tasks/active/` 下创建任务文档，记录：
- 需求描述和实现步骤
- 方案设计与对比（多个方案、选择理由）
- 技术调研结果
- 每步的完成状态
- 关键决策和遇到的问题
- 相关文件索引

下次会话恢复：

```
"继续上次的优惠券功能"
```

AI 会读取任务文档，从上次中断的地方继续，包括恢复方案上下文和进度。

### 技能组合

复杂任务会自动组合多个技能。例如：

```
"帮我开发一个带数据权限的订单管理模块，需要 PC 端和移动端"
```

AI 会依次激活：
1. `database-ops` — 设计订单表结构（多库兼容）
2. `crud-development` — 生成四层架构代码
3. `data-permission` — 添加数据权限配置
4. `ui-pc` — PC 端管理页面
5. `ui-mobile` — 移动端订单页面

### 三端协同开发

```
"帮我开发一个实时聊天功能，后端用 WebSocket，PC 端和移动端都要"
```

AI 会激活：
1. `realtime-communication` — WebSocket 后端实现
2. `ui-pc` — PC 端 `useWS` Composable
3. `ui-mobile` — 移动端 `useWebSocket` Composable
4. `store-pc` + `store-mobile` — 消息状态管理

### 技术选型分析

```
"用 Redis 缓存还是本地缓存好？仔细分析一下"
```

AI 会激活 `tech-decision` + `sequential-thinking`，给出多维度对比分析（含本项目技术栈约束）。

### 上游代码同步

本项目基于多个上游框架，定期需要同步更新：

```
/sync-local          → 同步 RuoYi-Vue-Plus 后端更新
/sync-wot-local      → 同步 WOT Design Uni 组件库更新
/sync-unibest-local  → 同步 Unibest 移动端框架更新
```

---

## 项目核心规范速查

### 后端规范

| 规范 | 正确做法 | 错误做法 |
|------|---------|---------|
| 包名 | `plus.ruoyi.business.mall` | ~~`com.ruoyi.mall`~~ |
| Service | `implements IOrderService` | ~~`extends ServiceImpl<>`~~ |
| 查询构建 | DAO 层 `buildQueryWrapper()` | ~~Service 层直接用 LambdaQueryWrapper~~ |
| 对象转换 | `MapstructUtils.convert()` | ~~`BeanUtil.copyProperties()`~~ |
| 主键策略 | 雪花 ID | ~~`AUTO_INCREMENT`~~ |
| 类引用 | `import` 后用短类名 | ~~内联全限定名~~ |
| API 路径 | `/pageOrders`、`/getOrder/{id}` | ~~`/page`、`/{id}`~~ |

### PC 端规范

| 规范 | 正确做法 | 错误做法 |
|------|---------|---------|
| 输入框 | `<AFormInput>` | ~~`<el-input>`~~ |
| 弹窗 | `<AModal>` | ~~`<el-dialog>`~~ |
| 搜索表单 | `<ASearchForm>` | ~~`<el-form>`~~ |
| API 调用 | `const [err, data] = await api()` | ~~`try { await api() } catch {}`~~ |
| 消息提示 | 项目封装 | ~~`ElMessage.success()`~~ |

### 移动端规范

| 规范 | 正确做法 | 错误做法 |
|------|---------|---------|
| 组件导入 | `import { useToast } from '@/wd'` | ~~`from 'wot-design-uni'`~~ |
| 表单 | `<wd-form>` + `<wd-input>` | ~~`<uni-forms>` + `<uni-field>`~~ |
| 样式单位 | `rpx` | ~~`px`~~ |
| CSS 注释 | `/* 注释 */` | ~~`// 注释`~~ |
| 消息提示 | `useToast().success()` | ~~`uni.showToast()`~~ |

---

## 开发流程最佳实践

### 推荐流程

```
1. /start          → 了解项目现状
2. /dev            → 开发新功能（完整流程）
   或 /crud        → 快速生成代码（表已存在）
3. /check          → 全栈代码规范检查
4. /update-status  → 更新项目状态
5. /progress       → 查看整体进度
```

### 遇到问题时

| 问题类型 | 怎么问 | 激活技能 |
|---------|-------|---------|
| 不知道文件在哪 | "xxx 功能的代码在哪个文件" | `project-navigator` |
| 不知道怎么写 | "怎么实现 xxx 功能" | `crud-development` |
| 代码报错 | "这个报错怎么解决：[粘贴错误]" | `bug-detective` |
| 性能问题 | "这个接口响应慢，怎么优化" | `performance-doctor` |
| 技术选型 | "用 Redis 还是本地缓存好" | `tech-decision` |
| 安全问题 | "怎么加权限控制" | `security-guard` |
| 多租户 | "怎么实现数据隔离" | `multi-tenant` |
| 移动端样式 | "这个页面看起来太拥挤" | `ui-design-mobile` |
| 条件编译 | "小程序和 H5 怎么区分" | `uniapp-platform` |
| IoT 对接 | "怎么接收设备 MQTT 消息" | `iot-mqtt` |

---

## 注意事项

### 推荐做法

1. **先用 `/start`** — 每次新会话先让 AI 了解项目
2. **描述清晰** — 说明功能名称、所属模块（base/mall/crm/iot）、需要哪些端
3. **分步开发** — 复杂功能拆分成多个小任务
4. **及时检查** — 开发完成后用 `/check` 检查规范
5. **跟踪任务** — 复杂功能使用任务跟踪，方便跨会话恢复
6. **善用触发词** — "仔细思考"激活深度分析，"查官方文档"查阅最新 API
7. **指定端** — 告诉 AI 需要 PC 端还是移动端，避免遗漏

### 避免做法

1. **不要跳过 `/start`** — AI 需要了解项目上下文
2. **不要模糊描述** — "帮我写个功能" 太模糊，应具体说明
3. **不要一次性要求太多** — 分步骤完成更可靠
4. **不要手动调用技能** — AI 会自动评估和激活
5. **不要用原生组件** — PC 端用 `AForm*`，移动端用 `wd-*`
6. **不要参考 ruoyi-vue-plus** — 架构完全不同，会导致错误代码

---

## 常见问题 FAQ

### Q1: `/dev` 和 `/crud` 有什么区别？

| 对比项 | `/dev` | `/crud` |
|--------|--------|---------|
| 适用场景 | 从零开始开发 | 表已存在 |
| 是否设计表 | 是 | 跳过 |
| 是否生成字典/菜单 | 是 | 跳过 |
| 生成范围 | 后端 + 前端 + 移动端 | 后端为主 |
| 执行速度 | 较慢（完整流程） | 快速 |
| 推荐场景 | 正式开发 | 快速原型、已有表结构 |

### Q2: 技能需要手动激活吗？

不需要。项目配置了**强制技能评估 Hook**，每次提问时 AI 会自动评估并激活相关技能。你只需要自然地描述需求即可。

### Q3: 本项目和 RuoYi-Vue-Plus 能混用代码吗？

**绝对不能**。两个项目的架构差异巨大：
- 包名不同（`plus.ruoyi.*` vs `org.dromara.*`）
- 分层不同（四层 vs 三层，本项目有 DAO 层）
- Service 设计不同（不继承 vs 继承 ServiceImpl）
- BO 注解不同（`@AutoMappers` vs `@AutoMapper`）

### Q4: 移动端和 PC 端用同一套后端 API 吗？

是的，完全共用：
- 同一套后端 API（`ruoyi-admin`）
- PC 端：`plus-ui` 调用后端
- 移动端：`plus-uniapp` 调用后端
- 权限控制通过 `Sa-Token` 统一管理

### Q5: 可以同时开发多个功能吗？

建议一个一个来。完成一个功能后再开始下一个，避免上下文混乱。对于复杂任务，可以使用"任务跟踪"来记录进度。

### Q6: 跨会话怎么恢复之前的工作？

两种方式：
1. **任务跟踪**：说"继续上次的 xxx 任务"，AI 会从 `docs/tasks/active/` 读取任务文档恢复
2. **直接说明**：说"上次我在开发 xxx 功能，继续"，AI 会扫描项目找到相关代码

### Q7: 怎么让 AI 查阅最新的框架文档？

在问题中加入"最佳实践"、"官方文档"、"标准写法"等触发词：

```
"MyBatis-Plus 批量插入的官方标准写法是什么"
```

AI 会通过 context7 工具查阅最新文档。

> **注意**：WD UI 不走 context7，AI 会直接读取项目内的 `plus-uniapp/src/wd/` 源码。

### Q8: 怎么让 AI 做更深入的分析？

在问题中加入"仔细思考"、"深度分析"、"全面评估"等触发词：

```
"仔细分析一下这个方案的优缺点和潜在风险"
```

AI 会使用 sequential-thinking 进行多步链式推理。

### Q9: 新增业务模块应该放在哪个 package？

根据业务领域选择：

| 业务类型 | 模块 | 包路径 | 表前缀 |
|---------|------|--------|--------|
| 基础通用 | base | `plus.ruoyi.business.base` | `b_` |
| 商城电商 | mall | `plus.ruoyi.business.mall` | `m_` |
| 客户管理 | crm | `plus.ruoyi.business.crm` | `crm_` |
| 物联网 | iot | `plus.ruoyi.business.iot` | `iot_` |

### Q10: 怎么同步上游框架的更新？

本项目基于三个上游框架，各有专门的同步命令：

```
/sync-local          → 后端（RuoYi-Vue-Plus）
/sync-wot-local      → 移动端组件库（WOT Design Uni）
/sync-unibest-local  → 移动端框架（Unibest）
```

---

## 快速参考卡片

```
┌─────────────────────────────────────────────────────────────┐
│                ruoyi-plus-uniapp AI 助手速查                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🚀 核心命令                                                │
│  /start ............. 了解项目                              │
│  /dev ............... 完整开发新功能（三端）                 │
│  /crud .............. 快速生成 CRUD（四层架构）              │
│  /check ............. 全栈代码规范检查                       │
│  /progress .......... 查看项目进度                          │
│  /next .............. 下一步建议                            │
│                                                             │
│  📋 文档命令                                                │
│  /init-docs ......... 初始化项目文档                        │
│  /update-status ..... 更新项目状态                          │
│  /add-todo .......... 添加待办事项                          │
│  /sync .............. 同步项目文档                          │
│                                                             │
│  🔄 上游同步命令                                            │
│  /sync-local ........ 同步后端上游代码                      │
│  /sync-wot-local .... 同步 WD UI 组件库                     │
│  /sync-unibest-local  同步 Unibest 框架                     │
│                                                             │
│  💡 触发词                                                  │
│  "仔细思考" ......... 激活深度分析                          │
│  "官方文档" ......... 查阅最新 API                          │
│  "打开浏览器" ....... 浏览器调试                            │
│                                                             │
│  🤖 自然提问即可，44 个技能自动激活                          │
│  "怎么加缓存" ........... → redis-cache                    │
│  "这个Bug怎么修" ........ → bug-detective                  │
│  "帮我写测试" ........... → test-development               │
│  "移动端页面怎么写" ..... → ui-mobile                      │
│  "怎么接入 IoT 设备" .... → iot-mqtt                       │
│  "怎么发送消息队列" ..... → message-queue                  │
│  "帮我接入微信登录" ..... → social-login                   │
│                                                             │
│  ⚠️ 核心约束                                                │
│  包名: plus.ruoyi.*    架构: 四层（含 DAO）                 │
│  Service 不继承        查询在 DAO.buildQueryWrapper()       │
│  PC 用 AForm*          移动端用 wd-*                        │
│  API 格式: [err, data] = await api()                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```
