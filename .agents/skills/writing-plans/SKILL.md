---
name: writing-plans
description: |
  当需要把已确定的方案/需求拆解成"可直接执行的细颗粒计划"时自动使用此 Skill。补齐 brainstorm（方案）与 dev-loop（执行）之间的「计划层」断层，产出带精确文件路径、验证命令、规范提交的任务台账。

  触发场景：
  - 头脑风暴/需求已定，要拆成可执行的实施计划
  - add-todo 的一句话待办太粗，需要细到"文件+命令+验收"
  - 复杂功能开发前，先出一份可被 /dev-loop 自主执行的任务台账
  - 把 docs/需求文档.md / docs/brainstorm-*.md 落成 docs/tasks/active 任务卡

  触发词：写计划、制定计划、实施计划、拆解任务、任务拆解、计划层、把方案落地、详细步骤、可执行计划、计划文档、writing-plans、开发计划、实施方案
---

# 计划层（writing-plans）指南

## 概述

本技能是框架 SDLC 链路上的「计划层」，补齐 `brainstorm`（产出方案）与 `dev-loop`（执行）之间长期缺失的一环。

```
brainstorm(方案)  →  【writing-plans(计划契约)】  →  plan-executor(父 Agent 编排)  →  update-status(聚合)
```

它把"方案级文档"翻译成**可被人或 `/dev-loop` 直接执行的细颗粒计划**：每个任务带精确文件路径、2-5 分钟勾选步骤、验证命令、规范提交信息。**产物落 `docs/tasks/active/*.md`**（复用 task-tracker 台账），从而 `update-status` 无需任何改造即可聚合，`dev-loop` 直接消费。

> **本技能采用"契约式细颗粒计划"，并做了本框架适配**：不照搬"所有任务都写完整代码"（那对 CRUD 是浪费、与 codegen 重复、且会生成违规代码），改为"颗粒度反比于框架自动生成程度"。

---

## 何时用 / 何时不用

| 场景 | 用本技能? | 改用 |
|------|:---:|------|
| 方案已定，要拆成可执行计划 | ✅ | - |
| 复杂多步/跨端/跨模块功能开发前 | ✅ | - |
| 还在发散、不知道怎么做 | ❌ | `brainstorm` |
| 单条小事/Bug 修复/小改动 | ❌ | `/add-todo` |
| 计划已就位，要开始执行 | ❌ | `/dev-loop` 或 `/dev` |
| 只是要个任务台账容器/格式 | ❌（本技能负责"内容"，不只是容器） | `task-tracker` |

---

## 核心原则：颗粒度 = 反比于「框架自动生成程度」

本框架有 codegen（`/dev`、`/crud`）+ 35 个 common 模块，**大量代码不该由计划层重写**。

| 两端 | 毛病 |
|------|------|
| add-todo（太粗） | 只有一句话"做什么"，执行时还要现想文件/命令/验收 |
| 全量写完整代码（太细） | 每个任务都写满完整代码 → 对 CRUD 浪费、与 codegen 重复、易写出不合 `plus.ruoyi` 规约的代码 |

**甜点 = 能 codegen 的只编排、不写代码；框架不生成的才写代码骨架。**

### 颗粒度判定表

| 任务类型 | 框架自动生成? | 颗粒度 | 计划写什么（≠重写代码） |
|---------|:---:|:---:|---|
| 建表 DDL | 半自动 | 中 | 完整建表 SQL（雪花ID/审计字段/`tenant_id`，single 去租户）+ 字典项清单 |
| 标准 CRUD 后端（Entity~Controller~Mapper） | ✅ 全自动 `/dev` | 粗·编排 | "配 codegen → 调 `/dev` → 校验四层生成"，**不写 Java** |
| 标准 CRUD 前端（API/Types/列表/表单页） | ✅ 半自动 | 粗·编排 | 参考 `ad.vue`/`adApi.ts`、用 `A*` 组件、页清单，**不逐行写** |
| 非 CRUD 后端逻辑（自定义 Service/事件/回调/算法） | ❌ | 细·骨架 | 方法签名 + 关键实现骨架 + 为什么 + 复用哪个 common 模块 |
| 第三方集成（pay/sms/oss/ai/mqtt…） | ❌ | 细·骨架 | 接口 + 实现类 + 配置项 + 复用 common + 失败降级 |
| 复杂查询/统计 | ❌ | 细·骨架 | DAO `buildQueryWrapper` 写法 + `likeCast` + SQL 骨架 |
| 定制 UI 页（大屏/工作台/图表，非标准 CRUD） | ❌ | 细·转码 | 引用 `docs/prototypes/*.html` → `html-to-code` + 组件映射 + e2e 截图门 |
| 权限/菜单/字典配置 | 半自动 | 中 | 菜单 SQL + 权限标识 + 字典项清单 |
| 多端联动/状态管理 | ❌ | 中·骨架 | store 结构 + composable + API `[err,data]` 调用 |

---

## 输入与输出

### 输入（每次必读，不可凭记忆）
1. `docs/需求文档.md` —— 需求（只读）
2. `docs/brainstorm-*.md` —— 已确定的方案（若有）
3. `CLAUDE.md` / `.claude/framework-config.json` —— 框架模块 vs 业务模块边界（**别动框架**）
4. 参考代码：`plus.ruoyi.business.base`（后端四层）、`plus-ui/.../ad/ad.vue`（PC 页）、`plus-uniapp`（移动端）

### 输出
- 写入 `docs/tasks/active/task-{YYYYMMDD-HHMMSS}-{业务简称}.md`
- **复用 task-tracker 富模板**（需求/方案/实现步骤/关键决策/当前进度/变更记录）
- 「实现步骤」区即可执行计划，`/dev-loop` 逐条消费、回写 `[x]`

> 不另起 `docs/plans/` 目录——落 `docs/tasks/active/` 才能让 `update-status` 零改造聚合。

---

## 计划头部模板（契约头）

每份计划开头必须有：

```markdown
## 计划契约头
- **目标**：{一句话}
- **架构**：{2-3 句：模块归属、四层落点、关键技术}
- **技术栈**：{涉及的 common 模块 / 框架能力}
- **模块归属**：ruoyi-modules/ruoyi-{module}（包 plus.ruoyi.business.{module}）
- **表前缀**：{b_ / m_ / crm_ / iot_}
- **端支持**：PC / 移动端 / 两者
- **文件清单表**：

| 文件 | 操作 | 负责什么 |
|------|------|---------|
| `ruoyi-modules/ruoyi-business/.../XxxController.java` | 新增 | 接口入口 |
| `plus-ui/src/views/business/.../xxx.vue` | 新增 | 管理页面 |
| ... | | |
```

---

## 任务条目标准结构（无论粗细都必带）

```markdown
- [ ] N. {任务标题}（类型：CRUD编排 / 非CRUD骨架 / UI转码 / 配置）
  - 文件：{精确路径，只落业务目录}
  - 框架约束：{该任务相关的规约提醒}
  - 步骤：{2-5 分钟勾选粒度}
  - 验证：{mvn -pl xxx -am compile / pnpm -C plus-ui build / e2e 截图对照原型}
  - 提交：{feat/fix(scope): ...}
- 依赖：{前置任务编号}
```

## 面向 plan-executor 的任务契约（强制）

当计划将由 `plan-executor` 自动执行时，以上最小条目还必须补全下列字段。目标是让执行 Agent 依据明确事实推进，禁止自行猜测业务需求。

```markdown
- [ ] T-03. {任务标题}（类型：CRUD编排 / 非CRUD骨架 / UI转码 / 配置）
  - 任务目标：{可验证的业务结果，不写泛泛的“完成开发”}
  - 前置条件：{依赖任务完成状态、运行环境、必要数据}
  - 依赖：{上游任务 ID；无则写“无”}
  - 后继任务：{直接依赖本任务的任务 ID；无则写“无”}
  - 执行模式：串行 / 并行组 P{n}
  - 文件所有权：{本任务唯一可修改的精确路径或目录}
  - 共享契约：{DTO、接口、表、路由等；无则写“无”}
  - 允许改动：{文件、配置或数据的明确范围}
  - 禁止改动：{不得触及的模块、公共契约或数据}
  - 实施步骤：{2-5 分钟粒度的可执行步骤}
  - 测试标准：{测试类、场景、输入、预期结果；无测试需说明原因}
  - 验收标准：{机器可验证的断言，不得只写“功能正常”}
  - 验证命令：{精确命令、预期退出码和关键输出}
  - 审查清单：{本任务专属约束 + 必须通过的 check 项}
  - 失败处理边界：{允许自动修复的范围、最大重试次数}
  - 完成证据：{测试输出、检查结果、受影响文件清单}
  - 提交：{feat/fix(scope): ...}
```

### 依赖与并行规则

- 依赖必须构成有向无环图；计划生成时应检查循环依赖和缺失任务 ID。
- 只有依赖全部完成、文件所有权不重叠、共享契约不冲突的任务才可放入同一并行组。
- 建表、数据迁移、公共 DTO/API、字典/菜单、共享路由和最终集成验证默认串行。
- 每个任务的验收标准必须由计划级架构契约或需求级成功标准推导；任务级标准可被父 Agent 按上位契约自动重基线，但不得降低上位成功标准。

### 低 Token 计划原则

- 每个任务只保留与自身相关的文件、命令、测试和契约，禁止复制整份需求或通用规范。
- 标准 CRUD 继续只写 codegen 编排；非 CRUD 只写必要骨架，避免在计划中重复完整代码。
- 默认不把 E2E 写为执行门禁；仅当需求明确要求 UI 自动化验收时，才单独声明其前置条件和成本。

---

## 🔴 框架约束注入清单（适配本体系的命门）

计划生成的每段代码/编排都必须遵守。**这是本框架计划层的命门——脱离这些约束，生成的"完整代码"会违规。**

### 后端（Java）
- 包名 `plus.ruoyi.business.{module}`（禁 `com.ruoyi`）
- 四层：Controller → Service（`implements IXxxService`，**禁 `extends ServiceImpl`**）→ DAO（`buildQueryWrapper`）→ Mapper
- Entity 继承 `TenantEntity`（**single 分支同步时改 `BaseEntity`**）
- 对象转换 `MapstructUtils.convert()`（禁 `BeanUtil.copyProperties`）
- 主键雪花 ID（禁 `AUTO_INCREMENT`）
- 查询在 DAO `buildQueryWrapper`；String 用 `like()`、其他类型 `likeCast()`
- API 路径：`pageXxxs` / `listXxxs` / `getXxx/{id}` / `addXxx` / `updateXxx` / `deleteXxxs/{ids}`
- 返回具体 VO（禁 `Map<String,Object>`）
- **先 import 再用短类名**（禁内联全限定名）
- 普通 CRUD 用 JavaDoc（禁 `@Schema`）
- 当前时间 `DateUtils.getNowDate()` / `new Date()`（禁 `LocalDateTime.now()`）
- 字段名：`phone`（非 phonenumber）、`is_deleted`（非 del_flag）、`is_external_link`（非 is_frame）
- 业务异常 `ServiceException.of()`

### 前端 PC（plus-ui）
- 用 `A*` 封装组件：`ASearchForm`/`AFormInput`/`AFormSelect`/`AFormDate`/`AModal`/`AFormSwitch`（**禁原生 `el-*`**）
- 页面文件名用业务名 `xxx.vue`（禁 `index.vue`）；首行 `<!-- 描述 -->`
- API 调用 `const [err, data] = await xxxApi()`（禁 `try/catch`）
- 消息用项目封装（禁 `ElMessage`）

### 移动端（plus-uniapp / plus-app）
- 用 `wd-*` 组件，`import ... from '@/wd'`（**禁 `wot-design-uni`**、禁 `uni-forms`/`uni-field`）
- 提示 `useToast().success()`（禁 `uni.showToast()`）
- 样式 `rpx` 单位、CSS 用 `/* */` 注释
- API 调用 `[err, data]` 格式

### 通用
- 文件只落业务目录：`ruoyi-modules/ruoyi-business/`、`plus-ui/src/views/business`、`plus-uniapp/src`；**不动框架模块**（`ruoyi-common`/`ruoyi-admin`/`ruoyi-extend`/`ruoyi-system`/`ruoyi-generator` + `plus-ui` 的 `views/system`·`views/tool`，见 `.claude/framework-config.json`）
- UTF-8 无 BOM；东八区时间；不按名杀宿主进程；规范 commit `feat/fix(scope)`

---

## UI 任务的原型处理

`writing-plans` **不产 HTML、不手搓原型**。

- 原型**已存在**（brainstorm/kickoff 经工作站 ui-studio 产出 `docs/prototypes/*.html`）→ 计划步骤 = `用 html-to-code 把 docs/prototypes/X.html 转成 plus-ui/plus-uniapp 页面 + e2e 截图对照原型`。
- 原型**不存在** → 计划**回标一步**「先走 brainstorm/工作站 ui-studio 出原型」，不让 writing-plans 或 dev-loop 现编 HTML。

---

## 验证门（复用 dev-loop，不自定义）

- 后端：`mvn -pl ruoyi-modules/ruoyi-{module} -am -DskipTests compile`（受影响模块）
- 前端：`pnpm -C plus-ui build`（或 type-check）
- 移动端：`pnpm -C plus-uniapp build:h5`
- 默认不跑 E2E，避免在常规任务中消耗浏览器与 Token；仅当任务契约明确要求 UI 自动化验收时，才调用 `e2e-test-pc` 或 `e2e-test-mobile`。

---

## 实战示例

### 示例 1：标准 CRUD（粗·编排，不写代码）

> 需求：优惠券模板管理（m_coupon_template），PC 后台 CRUD。

```markdown
- [ ] 1. 建表 + 字典（类型：配置）
  - 文件：`script/sql/ry_plus_new.sql`
  - 框架约束：雪花ID、审计字段、tenant_id（single 去）、表前缀 m_
  - 步骤：写 m_coupon_template 建表 SQL；加字典 coupon_type（满减/折扣）
  - 验证：SQL 在本地库执行通过
  - 提交：feat(mall): 优惠券模板建表与字典
  - 依赖：无

- [ ] 2. 后端四层 CRUD（类型：CRUD编排）
  - 文件：`ruoyi-modules/ruoyi-mall/.../couponTemplate/*`（codegen 生成）
  - 框架约束：包名 plus.ruoyi.business.mall、TenantEntity、buildQueryWrapper、API 路径规范
  - 步骤：配 codegen → 调 `/dev` 生成 Entity/BO/VO/Service/DAO/Controller/Mapper → 校验四层完整
  - 验证：`mvn -pl ruoyi-modules/ruoyi-mall -am -DskipTests compile`
  - 提交：feat(mall): 优惠券模板后端 CRUD
  - 依赖：1

- [ ] 3. PC 前端页面（类型：CRUD编排）
  - 文件：`plus-ui/src/views/business/mall/couponTemplate/couponTemplate.vue` + `api/.../couponTemplateApi.ts` + `Types.ts`
  - 框架约束：参考 ad.vue；用 ASearchForm/AModal/AFormInput；API [err,data]；首行注释
  - 步骤：codegen 出 API/Types → 列表页 + 表单弹窗 → 联调
  - 验证：`pnpm -C plus-ui build`
  - 提交：feat(mall): 优惠券模板管理页
  - 依赖：2
```

### 示例 2：非 CRUD 业务逻辑（细·骨架，写代码）

> 需求：用户领券（高并发、防超发、防重复领取）。

```markdown
- [ ] 4. 领券服务（类型：非CRUD骨架）
  - 文件：`ruoyi-modules/ruoyi-mall/.../service/impl/CouponReceiveServiceImpl.java`
  - 框架约束：plus.ruoyi.business.mall、implements 接口、复用 ruoyi-common-redis(RLock)、ruoyi-common-idempotent、ServiceException.of()、先 import 再短类名
  - 步骤（骨架）：
    ```
    // 复用 RedisUtils 分布式锁 + Lua 原子扣减库存
    RLock lock = RedisUtils.getClient().getLock("coupon:receive:" + templateId);
    try {
        if (!lock.tryLock(...)) throw ServiceException.of("领取太频繁");
        // 1. 校验：是否已领(查 m_coupon_record) → 已领抛 ServiceException
        // 2. Lua 原子扣库存：不足抛 ServiceException
        // 3. MapstructUtils 转换写 m_coupon_record（雪花ID、TenantEntity 自动填租户）
    } finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }  // 必须 finally 释放
    ```
  - 验证：`mvn -pl ruoyi-modules/ruoyi-mall -am -DskipTests compile`；并发测试
  - 提交：feat(mall): 用户领券防超发与防重复
  - 依赖：1,2
```

### 示例 3：定制 UI 页（细·转码，引用原型）

> 需求：移动端"领券中心"（非标准 CRUD，需视觉还原）。

```markdown
- [ ] 5. 领券中心页（类型：UI转码）
  - 文件：`plus-uniapp/src/pages-sub/mall/coupon/center.vue`
  - 前置：原型 `docs/prototypes/coupon/center.html`（若缺 → 先走 brainstorm/工作站 ui-studio 补，本任务挂起）
  - 框架约束：wd-* 组件 from '@/wd'、useToast()、rpx、API [err,data]
  - 步骤：html-to-code 转 center.html → 套 wd-paging 列表 + wd-button 领取 → 接示例4 领券 API
  - 验证：`pnpm -C plus-uniapp build:h5`；**e2e 截图对照原型**（e2e-test-mobile）
  - 提交：feat(mall): 移动端领券中心页
  - 依赖：4
```

---

## 与体系衔接 / 关联技能边界

| 关系对象 | 边界 |
|---------|------|
| `brainstorm` | 上游：产"方案"；本技能读 `docs/brainstorm-*.md` 拆"计划" |
| `task-tracker` | 容器：本技能复用其模板/落点；task-tracker=台账格式，writing-plans=细颗粒计划生产者 |
| `plan-executor` | 下游：按任务依赖图调度子 Agent，执行一个可运行批次并回写证据 |
| `/dev-loop` | 兼容路径：按顺序逐条执行台账 |
| `update-status` | 已扫描 `docs/tasks/active/`，零改造聚合三文档 |
| `/dev` | CRUD 任务**编排调用 /dev**，本技能不重造 codegen |
| `/add-todo` | add-todo=单条快速待办；writing-plans=成体系计划，并存不冲突 |
| `html-to-code` | UI 任务引用，本技能不产 HTML |

**链路**：`brainstorm` 定方案 → `writing-plans` 产出带依赖图的计划契约 → `plan-executor` 执行可运行批次 → 里程碑 `/update-status` 聚合。`/dev-loop` 保留为兼容的串行执行路径。

---

## 常见错误与最佳实践

### ❌ 错误 1：给标准 CRUD 写完整代码
把标准 CRUD 的 Entity/Service/Controller 全写进计划 → 与 `/dev` codegen 重复、易违规。
✅ **正确**：CRUD 只写"配 codegen → 调 /dev → 校验"的编排步骤。

### ❌ 错误 2：不注入框架约束
计划里写 `com.ruoyi`、`extends ServiceImpl`、`el-input`、`uni.showToast` → 执行出来全是违规代码。
✅ **正确**：每个任务带"框架约束"行，照"约束注入清单"。

### ❌ 错误 3：自己产 HTML 充原型
在计划里手搓 HTML 或让 dev-loop 现编页面 → 违反 CLAUDE.md。
✅ **正确**：引用 `docs/prototypes/*.html`（工作站产）走 html-to-code；缺则回标补原型。

### ❌ 错误 4：计划落错目录
写到 `docs/plans/` 或散文档 → `update-status` 聚合不到、`dev-loop` 找不到。
✅ **正确**：一律落 `docs/tasks/active/*.md`。

### ❌ 错误 5：任务颗粒度失衡
非 CRUD 复杂逻辑只写一句话，或给配置类任务写满代码。
✅ **正确**：照"颗粒度判定表"——反比于框架自动生成程度。

---

## 双系统同步

本技能为 Skill，须同步 Codex 镜像（按 `add-skill` 规范）：
- 主：`.claude/skills/writing-plans/SKILL.md`
- 镜像：`.agents/skills/writing-plans/SKILL.md`（内容完全相同，`diff` 无差异）
- 登记：`.claude/hooks/skill-forced-eval.cjs` 技能列表 + `AGENTS.md` 技能表
- single 分支同步时：约束清单内 `TenantEntity` 改 `BaseEntity`
