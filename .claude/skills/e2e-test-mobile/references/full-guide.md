
# 移动端 H5 端到端自动化测试（e2e-test-mobile）

## 概述

本技能指导用 **aicoder 内置浏览器 MCP 工具**对**移动端 plus-uniapp 的 H5 端**做真实浏览器的端到端（E2E）自动化测试。移动端自动化的关键路径是：**启动 H5 端（默认 :5173）→ 用浏览器驱动 WD UI 页面 → 模拟用户操作 → 五维断言 → 沉淀测试计划与报告**。

> **PC 后台管理（plus-ui）测试请用 `e2e-test-pc`**；本技能只管移动端 H5。
> **plus-app（原生 APP）** 无法用浏览器直接测；但其大部分页面与 plus-uniapp 同构，可借本技能在 plus-uniapp 的 H5 上做等价验证（原生插件能力除外）。

---

## 一、能力与边界

| 维度 | 说明 |
|------|------|
| 测试对象 | plus-uniapp 的 **H5 端**（`:5173`，真实浏览器渲染） |
| 驱动工具 | aicoder `browser_*` 系列 MCP 工具 |
| 验证维度 | UI 渲染 / 数据正确 / 接口健康 / 无 JS 报错 / 交互闭环（五维） |
| 产出 | `docs/tests/` 下的测试计划 + Bug 记录 + 测试报告 |
| **不负责** | 后端单测（→ `test-development`）、PC 后台（→ `e2e-test-pc`）、原生插件能力（需真机/HBuilderX）、小程序专有 API（条件编译 `#ifdef MP` 分支） |

**与相邻技能的边界**：
- 「PC 后台业务验收」→ `e2e-test-pc`
- 「移动端页面长什么样、用什么组件」→ `ui-mobile` / `ui-design-mobile`
- 「页面打不开要排查原因」→ 先 `bug-detective`，再用本技能验证修复

---

## 二、移动端 H5 的三个关键差异（务必先记住）

相比 PC，移动端测试有三处本质不同，配方都围绕它们：

1. **必须先有 H5 在跑**。plus-uniapp 默认 H5 端口 `:5173`（见 `plus-uniapp/vite.config.ts`）。
   > ⚠️ 端口不确定**必须先问用户**。本技能不主动启动（避免与其他会话冲突），需要启动时引导用户用 aicoder 任务系统起 H5。
2. **uni H5 是 hash 路由**。地址形如 `http://localhost:5173/#/pages/index/index`，跳页面要带 `#/`。`browser_wait_for(urlContains=...)` 判断导航也要用 hash 片段。
3. **WD UI 组件编译后 DOM class 不稳定**。`wd-button`/`wd-input`/`wd-cell` 编译结果不像 Element Plus 那样可预测，**硬写 class 选择器极易失效**。→ **移动端一律先 `browser_snapshot` 探查，再配合 `browser_eval` 按文本/结构定位**（见第五节）。

---

## 三、aicoder 浏览器工具速查（移动端测试视角）

| 测试动作 | 工具 | 移动端要点 |
|---------|------|-----------|
| 开窗/拿 label | `browser_open` / `browser_list_windows` | 入口，先做 |
| 跳转 | `browser_navigate(label,url)` | **带 hash**：`.../#/pages/xxx` |
| **探查可操作元素** | `browser_snapshot(label)` | **移动端必做第一步**，拿 buttons/inputs/links |
| 点击 | `browser_click(label,selector)` | 只认 CSS；WD 组件多用 eval 兜底 |
| 输入 | `browser_type(label,selector,text)` | wd-input 内部仍是原生 `input` |
| 等待 | `browser_wait_for(label,{selector\|urlContains})` | 取代 sleep |
| 滚动 | `browser_scroll(label,...)` | **触发 wd-paging 上拉加载更多** |
| 结果断言 | `browser_assert(label,assertions[])` | exists/textContains/count 等 |
| 读列表数据 | `browser_extract(label,{container,fields})` | 提取 wd-card/wd-cell 列表 |
| 读单值/复杂判断 | `browser_eval_result(label,js)` | 读列表长度、计算值 |
| 执行 JS 操作 | `browser_eval(label,js)` | **文本匹配点击的兜底** |
| 接口健康 | `browser_network(label)` | 看 `/app/.../page{X}s` 是否 200 |
| JS 报错 | `browser_console(label)` | uni 运行时/业务 error |
| 登录态读写 | `browser_storage_get/set` | 复用登录态 |
| 失败存证 | `browser_screenshot(label)` | Bug 留截图（也能看移动视觉） |

> 嵌入面板按容器宽度自适应渲染移动布局，功能测试不依赖精确视口；需要看移动视觉效果用 `browser_screenshot`。

### ⚠️ 文本匹配点击：browser_click 不支持按文字选元素

WD 按钮文字（如"立即购买""登录"）无法直接用 `browser_click` 点。兜底用 `browser_eval`：
```js
// 按文本找任意元素点击（适配 wd-button 等自定义组件）
[...document.querySelectorAll('*')]
  .filter(el => el.children.length === 0 || el.tagName.includes('-'))
  .find(el => el.textContent.trim() === '立即购买')?.click()
// 或更稳：先 browser_snapshot 看 buttons 列表，按返回的结构定位
```

---

## 四、登录态获取（两条路径）

### 路径 A：真实表单登录（推荐，H5 默认展示密码登录表单）

来自 `plus-uniapp/src/pages/auth/login.vue`：H5 端 `autoLogging=false`，展示账号密码表单；开发环境 + 默认租户会在 1 秒后自动填 `superadmin/admin123`。

```
1. browser_open("http://localhost:5173/#/pages/auth/login")   → 拿 label
2. browser_wait_for(label, selector="input")                    # 等表单渲染
3. browser_snapshot(label)                                       # 看清用户名/密码/登录按钮
4. browser_type(label, <用户名 input>, "superadmin")            # 选择器据 snapshot 定
   browser_type(label, <密码 input>, "admin123")
   # 验证码若开着：提示去后台【参数设置】关 sys.account.captchaEnabled，或人工输入
5. browser_eval(label, "[...document.querySelectorAll('*')].find(e=>e.textContent.trim()==='登录')?.click()")
6. browser_wait_for(label, urlContains="pages/index")            # 等跳首页
7. browser_network(label)  # /auth/login 200    browser_console(label)  # 无 error
```

### 路径 B：登录态复用（多用例省去重复登录）

token 存在 localStorage，键为 `{app.id}:token`（`app.id` 来自 `VITE_APP_ID`，见 `plus-uniapp/.env*`），值是带过期时间的**包装对象**（`CacheWrapper`），**不要手工拼**。正确做法：真实登录一次后导出整份登录态当夹具复用：

```
const dump = browser_storage_get(label)             # 真实登录后导出
# 后续用例回灌 + 刷新
browser_storage_set(label, items=dump, clear=false)
browser_navigate(label, "http://localhost:5173/#/pages/index/index")
```

---

## 五、移动端测试配方

移动端不是后台 CRUD，而是 C 端业务（首页列表 / 详情 / 表单 / 下单支付）。参考页面：`plus-uniapp/src/components/tabbar/Home.vue`（wd-paging 商品列表）、`plus-uniapp/src/pages/auth/login.vue`（wd-form 表单）。

### WD UI 元素定位策略（snapshot 优先）

| 页面元素 | 源码组件 | 定位策略 |
|---------|---------|---------|
| 导航栏标题 | `wd-navbar` | `browser_assert` textContains 标题文本 |
| 输入框 | `wd-input` | 内部是原生 `input`，`browser_snapshot` 拿 inputs 后用其选择器 |
| 按钮 | `wd-button` | `browser_eval` 按文本点击（见上） |
| 列表容器 | `wd-paging` | 滚动它触发加载更多 |
| 列表项 | `wd-card`/`wd-cell` | `browser_extract` container 用 snapshot 实测的卡片选择器 |
| 标签 | `wd-tag` | textContains（如"热销""新品"） |
| 轮播 | `wd-swiper` | exists 断言 |
| Toast 提示 | `useToast()` | 渲染为 `.wd-toast`（以 snapshot 实测为准），textContains 文案 |
| 弹窗 | `wd-popup` / `wd-message` | exists + 内部文本断言 |

> **原则**：移动端**不预设 class**，每个新页面先 `browser_snapshot` 拿到真实可操作元素，再写选择器；找不到就 `browser_eval` 按文本/层级定位。

### 移动端标准用例（按页面类型选用，每个含五维验证）

| 编号 | 用例 | 操作序列 | 断言 |
|------|------|---------|------|
| TC-01 | 首页/列表加载 | navigate 到页面 → `wait_for` 列表项出现 | tabbar/导航栏 exists；列表项 count>0；`network` `/page{X}s`==200；`console` 无 error |
| TC-02 | 上拉加载更多 | `browser_scroll` 到底部 → `wait_for` 新项 | 列表项数量增加；`network` 出现 `pageNum=2` 请求 |
| TC-03 | 下拉刷新/Tab 切换 | 切 `wd-paging` 的 tab 或触发刷新 | 列表数据按 tab 变化；接口带对应参数 |
| TC-04 | 进详情 | 点列表项 → `wait_for(urlContains="detail")` | 详情页关键字段渲染、与列表一致 |
| TC-05 | 表单提交 | 填 `wd-form` 各项 → 点提交 → `wait_for` toast | toast textContains "成功"；接口 200；列表/状态更新 |
| TC-06 | 表单校验 | 必填留空 → 提交 | 校验错误提示出现；未发提交请求（`network` 无该 POST） |
| TC-07 | 登录流程 | 见第四节路径 A | 跳转首页；token 已写入 storage |

---

## 六、五维断言模式（同 PC，缺一不算通过）

1. **UI 可见性** — 关键元素/文案存在：`browser_assert([{type:'textContains',selector:'.wd-navbar',expected:'首页'}])`
2. **数据正确性** — 列表渲染、字典/价格格式、计数：`browser_extract` 取卡片字段比对
3. **接口健康** — `browser_network` 检查 `/app/.../page{X}s`、`/order/...` 等 status==200，无 500/401
4. **无 JS 报错** — `browser_console` 过滤 error（uni 运行时报错常在此暴露）
5. **交互闭环** — 加购后角标 +1、下单后订单列表出现该单：操作前后 `browser_eval_result` 比对

---

## 七、与 task-tracker 集成：测试记录结构

沉淀到**专用目录** `docs/tests/`（与开发任务 `docs/tasks/` 分开）：

```
docs/tests/
├── plans/        # 测试计划（含用例清单，逐个 checkbox）
│   └── test-{YYYYMMDD}-移动端-{模块}.md
└── reports/      # 测试报告（按月归档）
    └── {YYYY-MM}/report-{YYYYMMDD}-移动端-{模块}.md
```

### 测试计划模板（plans/）

```markdown
# 测试计划：移动端 {模块名}

**状态**: 🟢 进行中 | 🔵 已完成
**创建时间**: {东八区 YYYY-MM-DD HH:MM}
**环境**: H5 http://localhost:5173 ｜ 后端 :5500 ｜ 账号 superadmin ｜ 租户 000000
**被测页面**: hash 路由 `/#/pages/...`
**API 前缀**: `/app/...`

## 用例清单
- [ ] TC-01 首页/列表加载：列表渲染 + page 200 + 无 console error
- [ ] TC-02 上拉加载更多：滚到底 → 列表项增多、pageNum 递增
- [ ] TC-03 Tab 切换/下拉刷新：数据按 tab 变化
- [ ] TC-04 进详情：字段渲染、与列表一致
- [ ] TC-05 表单提交：填表 → toast 成功 → 状态更新
- [ ] TC-06 表单校验：必填留空 → 阻止提交
- [ ] TC-07 登录流程：密码登录 → 跳首页 → token 写入

## Bug 记录
（失败用例追加：现象 / 截图路径 / 接口响应 / console 报错 / 复现步骤）

## 执行结果
通过 0 / 7 ｜ 进度 0%
```

> 时间统一东八区：`TZ=Asia/Shanghai date '+%Y-%m-%d %H:%M'`。

---

## 八、"逐个测试逐个落实"执行循环（核心工作方式）

```
1. 读测试计划，找第一个 - [ ] 用例
2. 按操作序列驱动 H5（先 snapshot 再操作）
3. 跑五维断言
4. 判定：
   ├─ 通过 → - [ ] 改 - [x]，追加「实际结果」
   └─ 失败 → 「Bug 记录」追加一条 + browser_screenshot 存证，用例标 - [⚠]
5. 更新通过数/进度
6. 回第 1 步，直到全部处理完
7. 生成报告到 docs/tests/reports/{YYYY-MM}/
```
> 中断恢复：读最新 `docs/tests/plans/*.md`，从第一个 `- [ ]` 续跑。

---

## 九、两大场景的落地流程

### 场景 A：测新加业务（开发完移动端页面马上验收）
> 触发："测一下新加的移动端 xx 页""刚写的 H5 列表跑一遍"

```
1. 确认 H5 已起、端口、被测页面 hash 路由
2. 登录（路径 A）→ navigate 到该页
3. 先 browser_snapshot 摸清元素 → 按页面类型选标准用例（列表/详情/表单）
4. 走「逐个测试逐个落实」循环
5. 出单页/单模块测试报告
```

### 场景 B：测完整业务（跨页面流程 / 全量回归）
> 触发："把移动端下单流程测一遍""移动端全量回归"

```
1. 创建主测试计划：把端到端流程的每一步列成 checkbox
2. 商城下单流程示例：
   登录 → 首页商品列表(TC-01) → 上拉加载(TC-02) → 进商品详情(TC-04)
   → 加入购物车(闭环断言角标) → 创建订单 → 发起支付(usePayment)
   → 订单列表核对状态
   每步既是用例，又验证上一步数据流入下一步
3. 走循环逐步标记 → 汇总完整报告
```
> 支付流程涉及第三方，沙箱/测试环境下断言到「拉起支付参数正确」即可，必要时 `browser_network_mock` 模拟回调。

---

## 十、常见坑（务必避免）

| 坑 | 后果 | 正确做法 |
|----|------|---------|
| 不先 `browser_open`/`list_windows` 拿 label | 工具全失败 | 第一步永远先拿 label |
| navigate 不带 `#/` | 跳不到 uni 页面 | hash 路由：`http://localhost:5173/#/pages/xxx` |
| 硬写 WD 组件 class | 选择器失效 | **先 snapshot 再定位**，文本/层级兜底 |
| `browser_click` 传文字 | 点不到 wd-button | 用 `browser_eval` 按文本 click |
| 用 sleep 等加载 | 不稳定 | `browser_wait_for(selector/urlContains)` |
| 上拉加载没滚动就断言 | 数据没变 | 先 `browser_scroll` 到底再 `wait_for` 新项 |
| 只测 H5 就断言小程序逻辑 | 误判 | `#ifdef MP` 分支 H5 测不到，需真机/小程序工具 |
| 造数据不清理 | 污染、重复跑失败 | 流程里回收自己造的数据 |
| 改其它会话测试文档 | 跨会话冲突 | 只动本会话 `docs/tests/`，逐个 `git add` |
| 只看页面没看接口/console | 漏判 | 五维必含 `network`+`console` |
| 验证码开着硬登 | 登录失败 | 关 `sys.account.captchaEnabled` 或登录态复用 |

---

## 十一、端到端实操示例（首页列表 + 上拉加载，节选）

```
# 0. 开窗 + 登录
browser_open("http://localhost:5173/#/pages/auth/login")   # → label=embed-1
browser_wait_for("embed-1", selector="input")
browser_snapshot("embed-1")                                 # 定位用户名/密码 input
browser_type("embed-1", "<用户名input选择器>", "superadmin")
browser_type("embed-1", "<密码input选择器>", "admin123")
browser_eval("embed-1", "[...document.querySelectorAll('*')].find(e=>e.textContent.trim()==='登录')?.click()")
browser_wait_for("embed-1", urlContains="pages/index")

# 1. TC-01 首页列表加载
browser_snapshot("embed-1")                                 # 摸清列表卡片结构
browser_assert("embed-1", [{type:'textContains', selector:'.wd-navbar', expected:'首页'}])
n1 = browser_eval_result("embed-1", "document.querySelectorAll('.wd-card').length")
browser_network("embed-1")    # /app/home/pageGoods == 200
browser_console("embed-1")    # 无 error

# 2. TC-02 上拉加载更多
browser_scroll("embed-1", ...到底部...)
browser_wait_for("embed-1", selector=".wd-card")            # 等新项渲染
n2 = browser_eval_result("embed-1", "document.querySelectorAll('.wd-card').length")
# 断言 n2 > n1，且 network 出现 pageNum=2

# 3. 失败则 browser_screenshot 存证 → 写入 Bug 记录
```

---

## 十二、技能产出清单

完成一次测试后，应交付：
- [ ] `docs/tests/plans/test-{日期}-移动端-{模块}.md`（用例全部处理、已标记）
- [ ] `docs/tests/reports/{年-月}/report-{日期}-移动端-{模块}.md`（汇总 + Bug 清单 + 结论）
- [ ] 失败用例截图存证
- [ ] 给用户的口头小结：通过 X / 失败 Y / 阻塞 Z，关键 Bug 一句话点出
