---
name: e2e-test-pc
description: |
  当需要用 aicoder 内置浏览器对 PC 后台管理端（plus-ui）做端到端自动化测试、业务验收、回归测试时自动使用此 Skill。

  触发场景：
  - 开发完一个后台业务模块后，要在真实浏览器里验收 CRUD 功能（测新加业务）
  - 需要把某个后台模块/整条业务流跑一遍并产出测试报告（测完整业务）
  - 需要回归测试，逐个用例落实、记录 Bug
  - 需要验证页面渲染、接口返回、控制台无报错、交互闭环
  - 需要登录态注入/绕过验证码做自动化

  触发词：自动化测试、E2E、端到端测试、浏览器测试、回归测试、冒烟测试、业务验收、UI测试、后台测试、PC测试、plus-ui测试、测一遍、跑一遍、aicoder浏览器、browser自动化、测试报告、测试用例执行
---

# PC 后台管理端到端自动化测试（e2e-test-pc）

## 概述

本技能指导用 **aicoder 内置浏览器 MCP 工具**对 **PC 后台管理端（plus-ui）**做真实浏览器的端到端（E2E）自动化测试。区别于 `test-development`（写后端 JUnit 代码），本技能在**真实运行的前后端环境**里，模拟用户操作页面、断言渲染结果、检查接口与控制台、验证交互闭环，并把测试过程沉淀为可中断恢复的测试计划与报告。

> **移动端 H5（plus-uniapp）测试请用 `e2e-test-mobile`**；本技能只管 PC 后台。

---

## 一、能力与边界

| 维度 | 说明 |
|------|------|
| 测试对象 | plus-ui 后台管理页面（真实浏览器渲染） |
| 驱动工具 | aicoder `browser_*` 系列 MCP 工具 |
| 验证维度 | UI 渲染 / 数据正确 / 接口健康 / 无 JS 报错 / 交互闭环（五维） |
| 产出 | `docs/tests/` 下的测试计划 + Bug 记录 + 测试报告 |
| **不负责** | 后端单测（→ `test-development`）、移动端（→ `e2e-test-mobile`）、性能压测（→ `performance-doctor`） |

**与相邻技能的边界**：
- 「写后端测试代码 / Mock / 覆盖率」→ `test-development`
- 「移动端 H5 业务验收」→ `e2e-test-mobile`
- 「页面打不开要排查为什么」→ 先 `bug-detective` 定位，再用本技能验证修复

---

## 二、前置条件（开测前必须确认）

1. **前后端已启动**。后端默认 `:5500`（本框架 `application.yml` 是 `server.port: ${SERVER_PORT:5500}`，不是通用的 8080），前端默认 `:80`（来自 `plus-ui/env/.env.development` 的 `VITE_APP_PORT`，被占则顺延，看 vite 启动输出）。
   > ⚠️ 端口不确定时**必须先问用户**，不要假设。本技能不主动用 shell spawn 服务（避免孤儿进程/与其他会话冲突）；需要启动时用 aicoder 任务系统启动（`list_project_tasks` 看任务 → `resolve_and_start_task` 按 task_id 启动）。
   > **服务就绪检测**：用 `Get-NetTCPConnection -LocalPort 5500 -State Listen`（瞬时、可靠）判断端口是否监听，**不要**做长时间 HTTP 健康轮询——`/actuator/health` 路径未必命中，且长轮询体验差。前端再补一个 `curl --noproxy '*' http://localhost:80` 确认 HTTP 层 200 即可。
2. **测试账号**：开发环境默认 `superadmin / admin123`、租户 `000000`（见 `login.vue` 的 `initializeFormData`，dev + 默认租户会自动填充）。
3. **验证码状态**：登录页验证码由系统参数 `sys.account.captchaEnabled` 控制。自动化前建议在【系统管理 → 参数设置】关掉，或走下文「登录态复用」绕过。

### 打开浏览器拿 label（所有操作的第一步）

```
browser_list_windows                 # 看是否已有窗口
# 为空 → 直接开一个嵌入面板：
browser_open(url="http://localhost:80")   # 返回 { label, url }
```
> 之后所有 `browser_*` 工具都要带这个 `label`。下文示例统一用 `label="embed-1"` 占位，实际用返回值。

---

## 三、aicoder 浏览器工具速查（PC 测试视角）

| 测试动作 | 工具 | 关键点 |
|---------|------|--------|
| 开窗/拿 label | `browser_open` / `browser_list_windows` | 入口，先做 |
| 跳转 | `browser_navigate(label,url)` | 进入菜单页 |
| **探查可操作元素** | `browser_snapshot(label)` | 返回 buttons/inputs/links（各 40），**规划操作先看它** |
| 点击 | `browser_click(label,selector)` | **只认 CSS 选择器**，点第一个命中 |
| 输入 | `browser_type(label,selector,text)` | 自动清空再输，触发 input/change |
| 等待 | `browser_wait_for(label,{selector\|urlContains\|state})` | **取代 sleep 猜时间**，默认 10s |
| 结果断言 | `browser_assert(label,assertions[])` | exists/textContains/valueEquals/urlContains/count |
| 读列表数据 | `browser_extract(label,{container,fields})` | 提取表格行做数据校验 |
| 读单值断言 | `browser_eval_result(label,js)` | 复杂判断/读取计算值 |
| 执行 JS 操作 | `browser_eval(label,js)` | **文本匹配点击的兜底**（见下） |
| 接口健康 | `browser_network(label)` | 看 `/pageXxxs` 是否 200、有无 500 |
| JS 报错 | `browser_console(label)` | 看有无 error/未捕获异常 |
| 登录态读写 | `browser_storage_get/set(label)` | 复用登录态 |
| 失败存证 | `browser_screenshot(label)` | Bug 留截图 |
| 回归复用 | `browser_recording_get/replay` | 录一次重放 |

### ⚠️ 文本匹配点击：browser_click 不支持按文字选元素

`browser_click` 只接受 CSS 选择器，**无法**直接点"文字是『新增』的按钮"。两条出路：
- **优先**：用稳定的结构化 CSS 选择器（见第五节选择器表）
- **兜底**：用 `browser_eval` 按文本找元素再 `.click()`：
  ```js
  // browser_eval 的 script
  [...document.querySelectorAll('.el-button')]
    .find(b => b.textContent.trim().includes('新增'))?.click()
  ```

---

## 四、登录态获取（两条路径）

### 路径 A：真实表单登录（推荐，顺带测了登录页）

```
1. browser_open("http://localhost:80")            → 拿 label
2. browser_wait_for(label, selector=".login-btn") # 等登录表单出现
3. browser_snapshot(label)                         # 确认表单字段
4. # 开发环境默认租户会自动填 superadmin/admin123，但仍显式补填更稳：
   browser_type(label, "input[placeholder*='用户']", "superadmin")
   browser_type(label, "input[type='password']", "admin123")
   # 若验证码开着：提示用户去【参数设置】关闭，或人工识图输入到 .captcha-input
5. browser_click(label, ".login-btn")
6. browser_wait_for(label, urlContains="/index")   # 或等地址栏不再含 /login
7. browser_network(label)                            # 确认 /auth/login 返回 200
8. browser_console(label)                            # 确认无 error
```

登录页关键选择器（来自 `login.vue`）：
| 元素 | 选择器 |
|------|--------|
| 租户ID | 第 1 个 `.form-input input` |
| 用户名 | `input[placeholder*='用户']` |
| 密码 | `input[type='password']` |
| 验证码输入 | `.captcha-input input` |
| 验证码图片 | `.captcha-image` |
| 登录按钮 | `.login-btn` |

### 路径 B：登录态复用（多用例时省去重复登录）

token 存在 localStorage，键为 `{app.id}:token`（`app.id` 来自 `VITE_APP_ID`，见 `plus-ui/.env*` / `SystemConfig.app.id`），值是带过期时间的**包装对象**（`CacheWrapper`），不是裸 token。**因此不要手工拼**，正确做法：

```
# 第一次真实登录后，把整份登录态导出当夹具
const dump = browser_storage_get(label)            # 拿到所有 localStorage
# 后续用例直接回灌 + 刷新
browser_storage_set(label, items=dump, clear=false)
browser_navigate(label, "http://localhost:80/index")
```
> 这样既不依赖验证码，也不用每个用例重新登录。token 7 天有效。

---

## 五、标准 CRUD 冒烟测试配方（核心，可套用任意后台模块）

**关键洞察**：本框架所有后台 CRUD 页面都遵循同一结构（参考 `plus-ui/src/views/business/base/ad/ad.vue`）：
`ASearchForm` 搜索栏 + `el-card` 工具栏（新增/修改/删除/导入/导出）+ `el-table` 表格 + `Pagination` 分页 + `AModal` 表单弹窗 + `ADetail` 详情弹窗。
**所以同一套配方能套用到优惠券、商品、客户……换模块只需改菜单路径、字段名、API 路径。**

### 渲染后选择器对照表（Element Plus 封装）

| 页面元素 | 源码组件 | 渲染后 CSS 选择器 |
|---------|---------|------------------|
| 搜索输入框 | `AFormInput` | `.el-form .el-input__inner` |
| 搜索下拉 | `AFormSelect` | `.el-form .el-select` |
| 新增按钮 | `el-button` Plus | `.el-card__header .el-button--primary`（兜底：eval 找"新增"） |
| 顶部修改 | `el-button` Edit | `.el-card__header .el-button--success` |
| 顶部删除 | `el-button` Delete | `.el-card__header .el-button--danger` |
| 表格 | `el-table` | `.el-table` |
| 表格数据行 | — | `.el-table__body-wrapper tbody tr` |
| 行多选框 | — | `tbody tr .el-checkbox` |
| 行内-查看 | link button View | `tbody tr .el-button--primary` |
| 行内-修改 | link button Edit | `tbody tr .el-button--success` |
| 行内-删除 | link button Delete | `tbody tr .el-button--danger` |
| 分页条 | `Pagination` | `.el-pagination` |
| 总记录数 | — | `.el-pagination__total` |
| 下一页 | — | `.el-pagination .btn-next` |
| 表单弹窗 | `AModal` | `.el-overlay:not([style*='none']) .el-dialog` |
| 弹窗标题 | — | `.el-dialog__title` |
| 弹窗确定 | `AModal` footer | `.el-dialog__footer .el-button--primary` |
| 弹窗内输入 | `AFormInput` | `.el-dialog .el-input__inner` |
| 成功提示 | `showMsgSuccess` | `.el-message--success` |
| 二次确认框 | `showConfirm` | `.el-message-box` |
| 确认框-确定 | — | `.el-message-box__btns .el-button--primary` |

### 七个标准用例（每个都含五维验证）

| 编号 | 用例 | 操作序列 | 断言 |
|------|------|---------|------|
| TC-01 | 页面加载 | navigate 到模块菜单 → `wait_for(selector=".el-table")` | `assert` 表格 exists；`network` 看 `/page{X}s`==200；`console` 无 error |
| TC-02 | 列表渲染 | — | `extract` 首行字段非空；`assert` `.el-pagination__total` textContains 数字，字典列已翻译成中文（非编码值） |
| TC-03 | 新增 | 点新增 → `wait_for(".el-dialog")` → `type` 各必填 → 点弹窗确定 → `wait_for(".el-message--success")` | toast textContains "成功"；弹窗消失（`notExists` 可见 dialog）；列表行数 +1 |
| TC-04 | 搜索 | `type` 搜索框（本框架多为 `@input` 实时搜，无需点按钮）→ `wait_for` 表格刷新 | 结果行均含关键词；`network` 最新 `/page{X}s` 带对应查询参数 |
| TC-05 | 编辑 | 点行内修改 → `wait_for(".el-dialog")` → 改字段 → 确定 → 成功提示 | 列表对应行显示新值 |
| TC-06 | 删除 | 点行内删除 → `wait_for(".el-message-box")` → 点确认框确定 → 成功提示 | 行数 -1（**删自己 TC-03 造的数据，清理现场**） |
| TC-07 | 表单校验 | 点新增 → 必填留空 → 点确定 | `assert` `.el-form-item__error` exists；弹窗仍在（未提交） |
| TC-08 | 分页 | 若 total>pageSize：点 `.btn-next` → `wait_for` 刷新 | `network` 最新请求 `pageNum=2`；首行数据变化 |

> 字段名、菜单路径、API 路径（`/base/ad` → 实际模块）按被测模块替换。必填字段从页面 `el-form` 的 `rules` 或带 `*` 的 label 识别。

---

## 六、五维断言模式（区分"真测试"和"看一眼"）

每个用例至少覆盖以下维度，缺一不算通过：

1. **UI 可见性** — 关键元素存在 + 文案正确
   `browser_assert([{type:'exists',selector:'.el-table'},{type:'textContains',selector:'.el-message--success',expected:'成功'}])`
2. **数据正确性** — 列表渲染、字典翻译成中文、分页计数
   `browser_extract` 取行数据比对；`browser_assert` count 断言行数
3. **接口健康** — 业务接口 200 且无 500/403
   `browser_network` 过滤 `/page{X}s`、`/add{X}` 等，检查 status
4. **无 JS 报错** — 控制台无 error
   `browser_console` 过滤 level==='error'，有则记 Bug
5. **交互闭环** — 增后列表多一条、删后少一条
   操作前后各 `browser_eval_result("document.querySelectorAll('tbody tr').length")` 比对

---

## 七、与 task-tracker 集成：测试记录结构

测试过程沉淀到**专用目录** `docs/tests/`（与开发任务 `docs/tasks/` 分开，职责清晰）：

```
docs/tests/
├── plans/        # 测试计划（含用例清单，逐个 checkbox）
│   └── test-{YYYYMMDD}-{模块}.md
└── reports/      # 测试报告（执行结果，按月归档）
    └── {YYYY-MM}/report-{YYYYMMDD}-{模块}.md
```

### 测试计划模板（plans/）

```markdown
# 测试计划：{模块名} 后台 CRUD

**状态**: 🟢 进行中 | 🔵 已完成
**创建时间**: {东八区 YYYY-MM-DD HH:MM}
**环境**: 前端 http://localhost:80 ｜ 后端 :5500 ｜ 账号 superadmin ｜ 租户 000000
**被测页面**: 菜单路径 / 路由 `/business/.../xxx`
**API 前缀**: `/base/xxx`

## 用例清单
- [ ] TC-01 页面加载：表格渲染 + page{X}s 200 + 无 console error
- [ ] TC-02 列表渲染：数据非空、字典已翻译、分页计数正确
- [ ] TC-03 新增：填表提交 → toast 成功 → 行数 +1
- [ ] TC-04 搜索：按关键字查 → 结果命中、接口带参
- [ ] TC-05 编辑：改一条 → 对应行更新
- [ ] TC-06 删除：删 TC-03 数据 → 行数 -1（清理现场）
- [ ] TC-07 表单校验：必填留空 → 阻止提交并提示
- [ ] TC-08 分页：翻页 → pageNum 变化、数据变化

## Bug 记录
（失败用例追加：现象 / 截图路径 / 接口响应 / console 报错 / 复现步骤）

## 执行结果
通过 0 / 8 ｜ 进度 0%
```

> 时间统一用东八区：`TZ=Asia/Shanghai date '+%Y-%m-%d %H:%M'`。

---

## 八、"逐个测试逐个落实"执行循环（最核心的工作方式）

无论测新加业务还是完整业务，都走这个循环：

```
1. 读测试计划文档，找到第一个未完成用例 - [ ]
2. 按该用例的操作序列驱动浏览器（browser_* 工具）
3. 跑完五维断言
4. 判定：
   ├─ 全部通过 → 把 - [ ] 改成 - [x]，在用例后追加「实际结果」
   └─ 任一失败 → 在「Bug 记录」区追加一条（现象+截图+接口+console+复现），
                  用例标记 - [⚠]（保留待修），browser_screenshot 存证
5. 更新「执行结果」的通过数/进度百分比
6. 回到第 1 步，直到所有用例处理完
7. 全部完成 → 生成测试报告到 docs/tests/reports/{YYYY-MM}/，
   汇总 通过X/失败Y/阻塞Z + Bug 清单 + 结论
```

> 中断恢复：下次读最新 `docs/tests/plans/*.md`，从第一个 `- [ ]` 接着跑（同 task-tracker 理念）。

---

## 九、两大场景的落地流程

### 场景 A：测新加业务（开发完一个模块马上验收）
> 触发："测一下新加的优惠券""刚开发的 xx 模块跑一遍"

```
1. 确认前后端已起、端口、被测模块菜单路径
2. 登录（路径 A）→ 进入该模块菜单页
3. 套用「标准 CRUD 冒烟配方」生成 TC-01~08（按模块字段裁剪）
4. 走「逐个测试逐个落实」循环
5. 出单模块测试报告
```
特点：单模块、快、用例标准化、几分钟出结果。

### 场景 B：测完整业务（跨模块流程 / 全量回归）
> 触发："把整个下单流程测一遍""全量回归"

```
1. 创建主测试计划：列出所有待测模块或一条端到端业务流的每一步，逐个 checkbox
2. 跨模块流程示例（商城下单）：
   登录 → 浏览商品列表 → 查看详情 → 加购物车 → 创建订单 → 支付 → 订单列表核对状态
   每一步既是一个用例，又验证上一步的数据流入下一步
3. 走「逐个测试逐个落实」循环，逐步标记
4. 汇总完整测试报告（通过/失败/阻塞 + Bug 清单）
```
特点：多模块、可中断恢复、产出完整报告。

---

## 十、常见坑（务必避免）

| 坑 | 后果 | 正确做法 |
|----|------|---------|
| 不先 `browser_list_windows`/`browser_open` 拿 label | 所有工具调用失败 | 第一步永远先拿 label |
| 用 `sleep`/猜时间等加载 | 不稳定，偶发失败 | 用 `browser_wait_for(selector/urlContains)` |
| `browser_click` 传文字当选择器 | 点不到 | 用结构化 CSS，或 `browser_eval` 按文本找 |
| 弹窗未等出现就填表 | 填空 | 先 `wait_for(".el-dialog")` 再 type |
| 多个同名元素 | 点错 | 选择器加上下文（`.el-dialog .xxx` / `tbody tr:first-child .xxx`） |
| 造了测试数据不清理 | 污染数据、影响重复跑 | 用例链路里删掉自己造的数据（TC-06） |
| 改了其它会话的测试文档 | 跨会话冲突 | 只动本会话 `docs/tests/` 文件，逐个 `git add` |
| 验证码开着硬登 | 登录失败 | 关 `sys.account.captchaEnabled` 或用登录态复用 |
| 只看页面没看接口/console | 漏判后端错/前端异常 | 五维断言必须含 `browser_network`+`browser_console` |
| 字典列断言原始码值 | 误判 | 断言中文标签（字典已翻译）而非 `0/1` |
| 用 `.el-dialog` 的 `display` 判弹窗是否关闭 | 误判（AModal 关闭后 DOM 仍残留） | 以「成功 toast + 接口 200 + 分页计数变化」作为提交闭环判定 |
| 假设页面路由 = 源码目录（如 `/business/mall/goods`） | 导航 404 | 路由由菜单动态生成，先 `browser_snapshot` 拿 links 找真实路由（如 `/mallManage/goods`） |

---

## 十一、端到端实操示例（新增功能验收，节选）

```
# 0. 开窗 + 登录
browser_open("http://localhost:80")                       # → label=embed-1
browser_wait_for("embed-1", selector=".login-btn")
browser_type("embed-1", "input[placeholder*='用户']", "superadmin")
browser_type("embed-1", "input[type='password']", "admin123")
browser_click("embed-1", ".login-btn")
browser_wait_for("embed-1", urlContains="/index")

# 1. TC-01 进入广告配置菜单页
browser_navigate("embed-1", "http://localhost:80/business/base/ad")
browser_wait_for("embed-1", selector=".el-table")
browser_assert("embed-1", [{type:'exists', selector:'.el-table'}])
browser_network("embed-1")     # 核对 /base/ad/pageAds == 200
browser_console("embed-1")     # 无 error

# 2. TC-03 新增
before = browser_eval_result("embed-1", "document.querySelectorAll('tbody tr').length")
browser_eval("embed-1", "[...document.querySelectorAll('.el-card__header .el-button')].find(b=>b.textContent.includes('新增')).click()")
browser_wait_for("embed-1", selector=".el-dialog")
browser_type("embed-1", ".el-dialog input[placeholder*='广告名称'],.el-dialog .el-input__inner", "E2E测试广告")
browser_click("embed-1", ".el-dialog__footer .el-button--primary")
browser_wait_for("embed-1", selector=".el-message--success")
browser_assert("embed-1", [{type:'textContains', selector:'.el-message--success', expected:'成功'}])
after = browser_eval_result("embed-1", "document.querySelectorAll('tbody tr').length")
# 断言 after == before + 1

# 3. 失败则 browser_screenshot 存证 → 写入 Bug 记录
```

---

## 十二、技能产出清单

完成一次测试后，应交付：
- [ ] `docs/tests/plans/test-{日期}-{模块}.md`（用例全部处理，通过/失败已标记）
- [ ] `docs/tests/reports/{年-月}/report-{日期}-{模块}.md`（汇总 + Bug 清单 + 结论）
- [ ] 失败用例的截图存证
- [ ] 给用户的口头小结：通过 X / 失败 Y / 阻塞 Z，关键 Bug 一句话点出
