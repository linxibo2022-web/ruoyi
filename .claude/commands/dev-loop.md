# /dev-loop - 自主循环开发（RuoYi-Plus-UniApp，一轮一最小提交 · 编排既有技能）

配合内置 `/loop`：`/loop /dev-loop` 自主连续开发，直到任务台账做完。本命令是「一轮的标准作业」，由 `/loop` 自动续期——干净复用原生循环，不自造。

🔴 **核心原则：本命令是「编排器」，不重复造轮子。** CRUD 走 `/dev`、原型转码走 `html-to-code`、前端组件照 `ui-pc`/`ui-mobile`、截图验收复用 `e2e-test-pc`/`e2e-test-mobile`、进度汇总用 `/update-status`。

## 用法

- `/loop /dev-loop` —— 自主连续：每轮一个最小可提交单元，自动续期，直到台账全勾完才停。
- `/dev-loop` —— 只手动跑一轮（调试 / 单步推进用）。

## 参数（可选 · 圈定范围）

不带参数 = 全量按台账顺序。带一个范围参数只做一部分：
- `/loop /dev-loop mall` —— 只做 mall 模块相关未勾任务
- `/loop /dev-loop M2` —— 只做某里程碑
- `/dev-loop <编号或关键词>` —— 单步：只做匹配那一条（不带 `/loop`）

规则：有参数 → 每轮只在台账里筛选匹配该范围的未勾 `[ ]`；做完即停。无参数 → 全量。`/loop` 续期会带着参数，范围每轮都在。

## 真相源（每轮开工必读，不可凭记忆）

1. `CLAUDE.md` / `.claude/PROJECT.md` / `.claude/framework-config.json`（**框架模块 vs 业务模块边界**，别动框架）
2. **`docs/需求文档.md`** —— 需求（只读输入，不改）
3. **原型** —— `docs/prototypes/*`（/kickoff 经工作站出的原型）
4. **任务台账** —— `docs/tasks/active/*.md`（`[ ]/[x]` 进度，task-tracker 格式）

## 前置门（缺则不进循环）

- **需求 / 台账都找不到** → 不瞎编，停下提示：「先走 `/kickoff` 或准备好需求 + 任务清单，再 `/loop /dev-loop`」。
- **有需求但无台账** → 第 0 轮自举：**激活 `writing-plans`** 读 `docs/需求文档.md` + 原型，拆**细颗粒可执行计划**（颗粒度=反比于框架自动生成程度：CRUD 编排 `/dev`、非 CRUD 写代码骨架；每条含 文件路径/框架约束/验证命令/规范 commit/依赖），写入 `docs/tasks/active/`，提交后**结束本轮**。

## 每轮标准流程（台账已存在时）

1. 读真相源，选 `docs/tasks/active/` 里第一个未勾 `[ ]` 任务。**严禁一轮做多个。**
2. **判断类型 → 复用对应技能（不手搓）**：
   - **新业务 CRUD 模块** → **走 `/dev`**（建表 → 配 codegen → 菜单/字典/权限 → 双模式生成代码）。这是它的强项，别自己写 10 个文件。
   - **原型稿转页面** → **走 `html-to-code`**（HTML 原型 → 框架规范前端/移动端代码）。
   - **前端页面/组件** → 照 `ui-pc`（plus-ui：Vue3 + Element Plus 封装组件 ASearchForm/AModal 等，禁原生 el-）/ `ui-mobile`（plus-uniapp：WD UI 组件）。
   - **后端非 CRUD 逻辑** → 照 `crud-development`/`api-development`：包名 `plus.ruoyi.business.[module]`，Entity 继承 `TenantEntity`，查询在 DAO `buildQueryWrapper`，转换用 `MapstructUtils`。
3. **验证门（必须全绿才提交）**：
   - 后端：`mvn -pl ruoyi-admin -am compile`（受影响模块 `mvn -pl <模块> -am compile`；大改 `mvn test`）。
   - 前端 plus-ui：`pnpm -C plus-ui build`（或 type-check）。
   - 移动端 plus-uniapp：`pnpm -C plus-uniapp build:h5`。
   - **UI 任务额外过「🎨 界面保真截图闭环」（见下），不达标 = 未完成。**
   - 红了当场修绿。**验证门全绿后，过下方「两段 review」才可提交。**
4. **更新 `docs/tasks/active/`**：该任务 `[ ]`→`[x]`，补一行「实际改了什么 / 坑」，更新「当前进度」「下一步」。🔴 **不动 `docs/项目状态.md` / `docs/待办清单.md` / `docs/需求文档.md` 三文档**（它们由 `/update-status` 从 tasks+git 自动汇总）。
5. 🔴 **两段 review 通过后，最小提交**：逐个 `git add` 本轮改动的**具体文件**（禁止 `git add -A`），`git commit` 用**规范 message `feat/fix(scope): …`**。⚠️ 必须规范——`/update-status` 和 `/progress` 从 git message 抓业务提交，`docs/chore/style` 开头会被过滤、不计进度。
6. 输出本轮小结：✅完成哪个任务 | 📊进度 X/总 | ⏭️下一个。
- **里程碑边界 / 完成一个模块 / 循环收尾**：调一次 **`/update-status`** 把 tasks+git 汇总进 项目状态/待办/需求 三文档；大改后期跑 `/check` 规范回归。

## 子代理执行 + 两段 review（提交前必过）

让执行更干净、提交更可靠——主循环负责"调度 + review + 提交"，把"实现"下放。

### 子代理执行（复杂任务可选）
- 仅当任务可独立验收、能减少上下文漂移且没有共享文件/契约写入时，用 **Agent 工具派一个新鲜子代理**执行单条 `docs/tasks/active/` 任务；定位、短答、紧急联调、持续用户判断和共享契约改动由主循环处理。
- 每张派单必须使用以下 contract：**目标**、**范围**、**模式**（只读/实现）、**唯一文件所有权**、**约束**（含 `plus.ruoyi`、四层、`TenantEntity`、`buildQueryWrapper`、`A*`/`wd-*`、API 路径）、**验收**（命令与预期）和**回传**（改动文件、验证证据、风险/阻塞，≤1500 中文字符）。
- 子代理不得越过所有权、领取后续任务或勾选台账；主循环负责依赖/冲突裁决、汇总验收、台账回写和最终结论。安全/写入任务遇运行时路由、Hook 或角色不可用时停止报告；只读且可验证的任务可降级由主循环接管。

### 两段 review（每个任务 · 验证门绿后、提交前 · 必过）
1. **第一段 · 功能正确性**：审"是否真正达成任务目标、边界 / 异常 / 空态、有无引入回归"。复杂逻辑可调 **`collaborating-with-codex`**（review-gate / 对抗审查）取独立第二意见。
2. **第二段 · 框架规范**：跑 **`/check`**（全栈规范检查），审包名 `plus.ruoyi`、四层架构、`TenantEntity`、`buildQueryWrapper`、`MapstructUtils`、`A*`/`wd-*` 组件、API 路径、无内联全限定名、UTF-8 无 BOM。
- 🔴 **任一段不过 → 返工，不提交**；两段都过才进上方第 5 步「最小提交」。

## 🎨 界面保真截图闭环（涉及 UI 的任务必过 · 复用 e2e-test）

- 起前端（plus-ui：`pnpm -C plus-ui run dev`；plus-uniapp：`pnpm -C plus-uniapp run dev:h5`，用 `run_in_background`，**不 kill 已有进程**）。
- **复用 `e2e-test-pc` / `e2e-test-mobile` 的 aicoder 浏览器能力**：`mcp__aicoder__browser_navigate` 打开运行页 → `mcp__aicoder__browser_screenshot` 截「现状」。
- 打开对应原型 `docs/prototypes/*` 当「目标」，逐条比对：① 布局/分区 ② 主/辅/状态色 ③ 间距留白 ④ 字号字重行高 ⑤ 组件类型（plus-ui 封装组件 / WD 组件用对）⑥ 文案 ⑦ 交互态。**任一不符 = 未完成，返工到达标再提交。**
- 有差异只改视图层 → 重新截图确认消除；关键差异没消除就继续修、别急着提交。
- 🔁 **失败降级（本会话级）**：第一次调浏览器就失败 → 本会话后续不反复重试，改「对照原型源码 + 用户提供截图」核对，仍逐条过 7 项。

## 决策分流 / 卡点熔断 / 停止条件

- **决策分流**：可逆/低风险 → 按默认继续记 `docs/BLOCKERS.md`，不空等；不可逆/合规/改库结构影响存量/缺外部凭据 → 停下问。
- **卡点熔断**：连续 3 轮无进展，或同一任务 2 轮验证门修不绿 → 停止，报错误原文 + 判断 + 建议。
- **停止条件**：`docs/tasks/active/` 全部 `[x]` + 验收全绿 → **先跑一次 `/update-status` 汇总三文档** → 输出「✅ 全部计划功能已实现」+ 验收自检，停止循环（不再续期）。

## 红线（绝不触碰）

- 🔴 **包名 `plus.ruoyi` 不改**；**不动框架模块**（ruoyi-common/admin/extend + ruoyi-system/ruoyi-generator，见 `framework-config.json`），业务只在 `ruoyi-modules/ruoyi-business/` 下加。
- **复用既有技能**（/dev、html-to-code、e2e-test、ui-*），别重造 CRUD / 原型转码 / 截图。
- 🔴 **规范 commit message**（feat/fix(scope)），否则进度统计抓不到。
- 不 `git add -A`；**不碰用户未提交的改动**；不 `git reset --hard`/`clean`；本地提交不推远端（除非明确要求）；仓库私有。
- 商业产品 → 反捏造：不臆造数据/案例/字段，不确定标「待确认」。
- 严守 `CLAUDE.md` / `.claude/PROJECT.md` 项目禁令（**优先级最高**）。
