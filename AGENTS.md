# AGENTS.md - ruoyi-plus-uniapp 项目开发规范

## 对话语言设置
**重要**: 在此代码库中工作时，Codex 必须始终使用中文与用户对话。所有响应、解释、错误信息和技术讨论都应使用简体中文。

## 🔴 文件编码与注释规范（必须遵守）

### 1. 统一编码
- **所有源码与配置文件统一强制使用 UTF-8（无 BOM）**
- 包括但不限于：`.java`、`.vue`、`.ts`、`.js`、`.xml`、`.yml`、`.properties`、`.sql`、`.md`
- **绝对禁止**：UTF-8 with BOM、GBK、GB2312、ANSI、ISO-8859-1 等混用
- 全局编码、项目编码统一强制设置为 UTF-8
- 属性文件（`.properties`）默认编码强制统一为 UTF-8
- 创建 UTF-8 文件时必须强制使用 UTF-8（无 BOM）

### 2. 中文内容规范
- 中文注释、中文日志、中文文档必须可读，不允许乱码（如"鍥藉"）
- 发现乱码优先检查文件实际编码与 IDE 显示编码是否一致
- **所有代码注释必须使用中文**，禁止全英文注释
- 包括：JavaDoc 注释、行内注释、块注释、Vue 模板注释、SQL 注释
- 技术术语、类名、方法名等可保留英文原文，但描述性文字必须为中文
- **正确示例**：
  ```java
  /** 广告管理服务实现 */
  // 根据 ID 查询广告详情
  ```
- **错误示例（禁止）**：
  ```java
  /** Ad management service implementation */
  // Query ad detail by id
  ```

### 3. Windows PowerShell 读取规范
- 在 Windows PowerShell 中读取包含中文的 UTF-8（无 BOM）文件时，禁止直接使用裸 `Get-Content`
- 必须显式指定 UTF-8：`Get-Content -Encoding UTF8 -Path xxx`
- 读取前可设置输出编码，避免终端转码导致中文乱码：
  ```powershell
  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
  $OutputEncoding = [System.Text.Encoding]::UTF8
  ```
- 优先使用 `rg`、`Select-String` 等能正确处理 UTF-8 的工具检索内容

### 4. 注释规范
- 保留已有业务注释，不随意删除历史说明和关键 `//` 注释块
- 新增代码必须补充必要注释，说明"为什么这样做"，避免空泛注释
- 接口方法注释至少包含：用途、参数、返回值、异常/边界行为

### 5. Java 文件保存规则
- 保存 Java 文件时强制 UTF-8 no BOM
- 批量改文件后，必须抽查文件头字节，确认无 `EF BB BF`

### 6. `java: 非法字符: '\ufeff'` 处理规则
- 该报错优先判定为 BOM 问题
- 处理步骤：
  1. 定位报错文件
  2. 移除文件头 BOM
  3. 批量扫描同目录 `.java` 文件是否也有 BOM
  4. 重新编译验证

### 7. 工具链与提交前检查
- IDE 默认编码设置为 UTF-8 且关闭 with BOM
- 提交前执行一次编译（如 `mvn -DskipTests compile`）
- 若涉及注释修改，提交前人工检查中文可读性与注释完整性

### 8. 变更原则
- 功能改动与注释改动尽量分开，便于回溯
- 修编码问题时不改业务逻辑，只做最小必要修改

> ⚠️ **严重警告**: 本项目 **不是 ruoyi-vue-plus 框架**,而是经过深度重构的 **ruoyi-plus-uniapp** 框架!
> **绝对禁止** 参考 ruoyi-vue-plus 的代码风格和架构设计!
> **必须** 严格遵循本项目现有的代码规范和架构模式!

## 术语约定

| 术语 | 含义 | 对应目录 |
|------|------|---------|
| **前端** | PC 端 | `plus-ui/` |
| **移动端(CLI)** | 小程序/H5/不需原生插件的APP | `plus-uniapp/` |
| **移动端(原生APP)** | 需要原生插件的APP/鸿蒙APP | `plus-app/` |
| **后端** | Java 服务 | `ruoyi-modules/` |

## MCP 工具触发

| 触发词 | 工具 | 用途 |
|-------|------|------|
| 深度分析、仔细思考、全面评估 | `sequential-thinking` | 链式推理，多步骤分析 |
| 最佳实践、官方文档、标准写法 | `context7` | Vue/Element Plus/UniApp/MyBatis-Plus 等（⚠️ WD UI 禁用，只参考 `plus-uniapp/src/wd/`） |
| 打开浏览器、截图、检查元素 | `chrome-devtools` | 浏览器调试 |
| 用工作站、workstation、ai-workstation | `ai-workstation` | AI 工作站 MCP（route → get_skill → 执行） |

## 时区约定

**所有日期时间必须使用东八区（UTC+8，Asia/Shanghai）**。获取当前时间时使用：
```bash
TZ=Asia/Shanghai date '+%Y-%m-%d %H:%M'
```
> 适用范围：项目状态文档、待办清单、需求文档、任务跟踪、进度报告等所有涉及时间戳的场景。

---

## 🔴 Skills 技能系统（最高优先级）

> **技能系统确保 AI 在编码前加载领域专业知识，保证代码风格一致**

---

## 技能系统工作原理

本项目的技能文件存储在 `.agents/skills/[skill-name]/SKILL.md` 中。

> **目录迁移边界（2026）**：Codex 遵循 Agent Skills 开放标准，**仅技能目录**迁到 `.agents/skills/`（唯一镜像）。旧的 `.codex/skills/` **已删除**——实测 Codex 0.144 同时扫描新旧两处，双写会导致每个技能重复注入，故只保留 `.agents/skills/`。Codex 的其它组件——`.codex/hooks/`、`.codex/config.toml`、`.codex/agents/`（subagents，注意名叫 agents 但在 `.codex/` 下）、MCP、`~/.codex/prompts/`——**一律留在 `.codex/`，不要迁到 `.agents/`**。

**Codex 启动时**：自动加载所有技能的 `name` 和 `description`
**任务匹配时**：Codex 读取匹配技能的完整 SKILL.md 内容
**需要时**：Codex 可进一步读取技能目录下的 `references/`、`scripts/` 等文件

---

## 技能清单与触发条件

以下是本项目的技能列表。Codex 应根据 `description` 自动判断何时使用：

| 技能名称 | 触发条件（description） |
|---------|----------------------|
| `add-skill` | 为框架添加新技能、修改现有技能、编写技能文档、同步双系统 |
| `crud-development` | CRUD 开发、业务模块、Entity/Service/DAO 创建 |
| `api-development` | API 设计、RESTful 接口、接口规范 |
| `database-ops` | 数据库操作、SQL、建表、字典、菜单配置 |
| `backend-annotations` | 后端注解、@SerialMap、@RateLimiter |
| `error-handler` | 异常处理、ServiceException、错误处理 |
| `multi-tenant` | 多租户/租户隔离/tenant_id/TenantEntity/租户切换/TenantHelper/动态租户/排除表 |
| `data-permission` | 数据权限、@DataPermission、行级权限、数据隔离 |
| `security-guard` | 安全、Sa-Token、认证授权、加密 |
| `utils-toolkit` | 工具类、StringUtils、MapstructUtils |
| `ui-pc` | 前端组件、AForm、AModal、Element Plus 封装 |
| `ui-mobile` | 移动端、wd-组件、小程序、APP、WD UI（plus-uniapp 和 plus-app 通用） |
| `ui-design-mobile` | 移动端设计、页面布局、间距、留白（plus-uniapp 和 plus-app 通用） |
| `app-adapter` | plus-app/APP端/原生APP/原生插件/HBuilderX/鸿蒙APP/harmony/nativeplugins/APP打包/真机调试 |
| `store-pc` | 前端 Store、Pinia、useUserStore |
| `store-mobile` | 移动端 Store、useAuth、Composable |
| `uniapp-platform` | 条件编译、ifdef、平台判断 |
| `delivery-sync` | 交付/交付物/交付副本/交付目录/客户版本/对外版本/剥离技能/移除技能体系/去掉git/镜像/deliver/delivery/对外发布/同步交付/交付同步 |
| `module-strip` | 模块裁剪/裁剪模块/删除模块/移除模块/stripModules/module-strip/strip-modules/商城裁剪/IoT裁剪/支付裁剪/AI裁剪/不要mall/不要iot/不要pay/不要ai/裁掉商城/裁掉IoT/裁掉支付/裁掉AI |
| `deployment-guide` | 部署/上线/发布/生产环境/Docker/Compose/构建/JAR/密钥/Nginx/1Panel/容器编排/小程序发布/APP打包 |
| `payment-integration` | 支付、微信支付、支付宝、退款 |
| `wechat-integration` | 微信、小程序登录、订阅消息 |
| `file-oss-management` | 文件上传、OSS、云存储、MinIO |
| `ai-langchain4j` | AI、大模型、ChatGPT、DeepSeek、langchain4j、MCP、Model Context Protocol、MCP Server、对外提供 MCP、工具调用、函数调用、McpToolProvider、McpClient、AiServices、TokenStream、Spring AI、RAG、知识库、流式对话 |
| `media-processing` | 图片处理、二维码、水印、Excel |
| `bug-detective` | Bug 排查、报错、异常、不工作 |
| `performance-doctor` | 性能优化、慢查询、缓存 |
| `redis-cache` | Redis、缓存、Cache、@Cacheable、@CacheEvict、@CachePut、RedisUtils、CacheUtils、分布式锁、RLock、限流、RateLimiter、发布订阅、缓存穿透、缓存雪崩、缓存击穿 |
| `code-patterns` | 代码规范、命名、禁止事项、Git 提交 |
| `architecture-design` | 架构设计、模块划分、重构 |
| `project-navigator` | 项目结构、文件定位 |
| `git-workflow` | Git、提交、commit、分支 |
| `tech-decision` | 技术选型、方案对比 |
| `brainstorm` | 头脑风暴、创意、方案设计 |
| `design-review` | 方案评审/设计评审/审查方案/检查方案/把关/设计审查/检查漏洞/架构评审/需求评审 |
| `collaborating-with-codex` | Codex 协作/多模型/原型/Diff/代码审查/codex-plugin-cc/codex 插件/官方插件/codex review/codex rescue/adversarial-review/review-gate |
| `collaborating-with-gemini` | Gemini 协作/多模型/前端原型/UI 设计/样式审查 |
| `task-tracker` | 任务跟踪、进度管理、继续任务 |
| `writing-plans` | 写计划/制定计划/实施计划/拆解任务/计划层/把方案落地/详细步骤/可执行计划/计划文档/开发计划 |
| `test-development` | 测试/单元测试/@Test/JUnit5/Mockito/断言/测试用例/测试覆盖率 |
| `e2e-test-pc` | 自动化测试/E2E/端到端测试/浏览器测试/回归测试/冒烟测试/业务验收/UI 测试/后台测试/PC 测试/plus-ui 测试/aicoder 浏览器/测试报告 |
| `e2e-test-mobile` | 移动端测试/H5 测试/小程序网页版测试/plus-uniapp 测试/移动端自动化/移动端 E2E/移动端回归/wd-paging 测试/WD UI 测试/移动端冒烟/uniapp 测试 |
| `i18n-development` | 国际化/多语言/i18n/翻译/t()/语言切换/MessageUtils/content-language/LanguageCode/useI18n/messages.properties/locale/$t/zh_CN/en_US |
| `icon-management` | 图标/icon/菜单图标/换图标/加图标/图标管理/IconSelect/wd-icon/iconfont/图标选择 |
| `scheduled-jobs` | 定时任务/SnailJob/延迟队列/@Scheduled/任务调度/重试机制/工作流编排/分布式执行 |
| `json-serialization` | JSON 序列化/反序列化/数据转换/JsonUtils/日期格式/精度/BigDecimal/Long/类型转换/JSON 验证 |
| `realtime-communication` | WebSocket/SSE/实时推送/在线聊天/消息推送/双向通信/服务端推送/流式响应/EventSource/心跳/在线状态/WebSocketUtils/SseMessageUtils/publishMessage/useWS/useSSE/useWebSocket |
| `notification-system` | 短信/SMS/邮件/Mail/Email/消息推送/MessagePushService/MessageChannel/通知/验证码/SmsFactory/MailUtils/sendText/sendHtml/统一消息/消息路由/多通道 |
| `message-queue` | RocketMQ/消息队列/MQ/异步消息/延迟消息/事务消息/RMSendUtil/RMTopicUtil/DelayLevel/Topic/消费者/生产者/削峰填谷/系统解耦/sendAsync/sendDelay/sendTransaction/RocketMQMessageListener |
| `social-login` | 社交登录/第三方登录/OAuth2/微信登录/QQ登录/GitHub登录/Gitee登录/钉钉登录/企业微信/SSO/单点登录/JustAuth/SocialUtils/socialBind/socialUnbind/授权回调/AuthRequest/socialCallback/MaxKey/TopIAM/账号绑定 |
| `third-party-api` | 高德地图/火山引擎/TTS/语音合成/地理编码/逆地理编码/IP定位/天气查询/距离计算/第三方API/HTTP客户端/Forest/GaodeMapClient/VolcengineTtsClient/声明式HTTP/ForestInterceptor/外部服务集成 |
| `project-migration` | 迁移项目/项目迁移/重构项目/代码迁移/继续迁移/迁移进度/迁移蓝图/架构迁移/框架迁移/项目重构/导入项目/搬迁代码 |
| `project-init` | 新项目/创建项目/初始化项目/开新项目/项目初始化/标识符替换/Git仓库创建/数据库初始化/启动引导 |
| `framework-sync` | 框架同步/upstream/同步框架/拉取更新/合并上游/同步上游/框架更新/upstream sync/同步原仓库/.framework-sync.json |
| `iot-mqtt` | MQTT/物联网/IoT/设备通信/设备消息/mica-mqtt/MqttClientTemplate/publish/subscribe/QoS/Topic/EMQX/Mosquitto/共享订阅/设备上线/设备离线/遗嘱消息/保留消息/传感器数据 |
| `html-to-code` | HTML转代码/设计稿转换/原型转代码/HTML转前端/HTML转移动端/UI原型转换/HTML转Vue/区块转换/组件转换/Tailwind转UnoCSS |
| `log-audit` | 操作日志/登录日志/审计/@Log/sys_oper_log/sys_logininfor/DictOperType/LogAspect/excludeParamNames/isSaveRequestData/日志记录/审计追踪/日志脱敏 |
| `env-config` | 环境配置/profile/application.yml/application-dev.yml/application-prod.yml/.env/.env.development/.env.production/VITE_APP_/多环境/环境变量/SPRING_PROFILES_ACTIVE/配置切换 |
| `exp-sediment` | 沉淀经验/经验沉淀/总结会话/记下来/self-evolution/自我进化/反哺框架/经验审计/技能漏洞/新增禁令/避免再踩坑/对应 `/exp` 命令；以及消费信号：以前怎么处理/之前的方案/之前踩过/历史经验/查记录/查笔记/有没有遇到过/上次怎么解决/类似问题 |
| `dev-startup` | 本地启动/首次启动/跑起来/装环境/装依赖/pnpm install/mvn install/启动后端/启动前端/启动移动端/JDK安装/Node安装/nvm/pnpm/HBuilderX/IDEA运行/端口占用/镜像超时/健康检查/ECONNRESET |

---

## 🚨 强制执行规则

### 规则 1：任务匹配时必须读取技能

当用户请求与上述任何技能的 `description` 匹配时，Codex **必须**：

1. 读取对应的 `SKILL.md` 文件
2. 按照技能中的指令执行
3. 如果技能目录有 `references/`，按需读取相关文件

### 规则 2：多技能组合

复杂任务可能匹配多个技能，Codex 应：

1. 识别所有相关技能
2. 按依赖顺序读取（如：先 `database-ops` 再 `crud-development`）
3. 综合所有技能的规范执行

### 规则 3：响应中标注已使用技能

在涉及代码的响应中，简要说明使用了哪些技能：

```
已参考技能：crud-development, database-ops

[实现代码...]
```

---

## 技能文件位置（按优先级）

| 优先级 | 位置 | 说明 |
|-------|------|------|
| 1 | `.agents/skills/` | 项目级技能（本项目使用） |
| 2 | `~/.agents/skills/` | 用户级技能 |
| 3 | `/etc/codex/skills/` | 系统级技能 |

---

## 用户手动触发方式

用户可以通过以下方式显式调用技能：

1. **斜杠命令**：输入 `/skills` 打开技能选择器
2. **$ 前缀**：输入 `$skill-name` 直接触发（如 `$crud-development`）

---

## 📋 示例：技能如何被触发

### 示例 1：用户请求 "帮我开发一个优惠券管理功能"

**Codex 自动匹配**：
- `crud-development`（关键词：开发、管理功能）
- `database-ops`（需要建表）
- `api-development`（需要接口）

**Codex 执行**：
```
1. 读取 .agents/skills/crud-development/SKILL.md
2. 读取 .agents/skills/database-ops/SKILL.md
3. 读取 .agents/skills/api-development/SKILL.md
4. 按技能规范编写代码
```

### 示例 2：用户请求 "连接数据库查询用户表结构"

**Codex 自动匹配**：
- `database-ops`（关键词：数据库、表结构）

**Codex 执行**：
```
1. 读取 .agents/skills/database-ops/SKILL.md
2. 按技能中的数据库连接规范执行查询
```

### 示例 3：用户输入 "$bug-detective 这个接口报错了"

**用户显式触发**：`$bug-detective`

**Codex 执行**：
```
1. 立即读取 .agents/skills/bug-detective/SKILL.md
2. 按技能中的排查流程处理
```

---

## 🚫 禁止行为

| 禁止 | 原因 | 正确做法 |
|-----|------|---------|
| ❌ 任务匹配技能但不读取 SKILL.md | 代码风格不一致 | ✅ 自动读取匹配的技能文件 |
| ❌ 只读取部分匹配的技能 | 遗漏关键规范 | ✅ 读取所有匹配的技能 |
| ❌ 凭记忆编写代码 | 可能使用旧规范 | ✅ 每次都读取最新技能文件 |

---

## ✅ 自检清单

Codex 在回复代码前应确认：

- [ ] 是否识别了任务涉及的所有领域？
- [ ] 是否读取了所有匹配的 SKILL.md 文件？
- [ ] 代码是否符合技能文件中的规范？
- [ ] 是否在响应中简要说明了使用的技能？

---

## 🎯 核心原则

**技能系统的目标**：确保每一行代码都符合项目规范。

- **隐式触发**：Codex 根据任务自动匹配并读取技能（推荐）
- **显式触发**：用户用 `$skill-name` 或 `/skills` 手动调用
- **渐进加载**：只在需要时读取完整内容，保持上下文精简

---

## 📄 文档生成规范

**🚫 绝不主动生成文档**: AI 绝对禁止主动创建任何文档文件（*.md、README等），除非用户明确要求。

**📁 文档存放位置**: 即使用户明确要求生成文档，所有文档都**必须**生成到项目根目录的 `docs/` 目录下，而不是其他位置。

**示例**：
- ✅ 正确: `docs/api-design.md`
- ✅ 正确: `docs/database-schema.md`
- ❌ 错误: `README.md` (根目录)
- ❌ 错误: `ruoyi-modules/ruoyi-business/docs/xxx.md` (模块内)

---

## 🚫 AI 首要禁令

### ❌ 绝对禁止模仿 ruoyi-vue-plus 框架 ⭐⭐⭐⭐⭐

**本项目与 ruoyi-vue-plus 的核心区别:**

| 对比项 | ruoyi-vue-plus | 本项目 (ruoyi-plus-uniapp) |
|--------|---------------|---------------------------|
| **包名前缀** | `com.ruoyi` | `plus.ruoyi` |
| **Service继承** | ✅ 继承 `IBaseService` 和 `ServiceImpl` | ❌ **不继承任何基类** |
| **DAO层** | ❌ 没有DAO层,Service直接构建查询 | ✅ **独立DAO层,负责构建查询** |
| **Mapper继承** | 继承 `BaseMapperPlus` | ✅ 只继承 `BaseMapper<Entity>` |
| **查询构建位置** | Service层直接构建 | ✅ **DAO层专门方法构建** |
| **对象转换** | 使用 `BeanUtil` 或手动 | ✅ **统一使用 MapstructUtils** |
| **实体基类** | 继承 `BaseEntity` | ✅ **继承 TenantEntity** |
| **BO注解** | 使用 `@TableField` 映射 | ✅ **使用 @AutoMappers 自动映射** |

**AI 必须做到:**

1. ✅ **写代码前先 Read 本项目的现有代码**,而不是凭印象或参考其他框架
2. ✅ **严格遵循本项目的包名 `plus.ruoyi`**
3. ✅ **Service 不继承任何基类**,直接实现接口
4. ✅ **DAO 层必须有 `buildQueryWrapper()` 方法**
5. ✅ **Entity 继承 `TenantEntity` 而非 `BaseEntity`**
6. ✅ **BO 使用 `@AutoMappers` 注解自动映射**

---

## 🤖 AI 写代码前强制检查清单

**⚠️ 每次生成代码前，AI 必须按顺序执行以下步骤（强制执行）：**

### ✅ 步骤 1: 学习本项目现有代码风格 (强制)

```bash
# 错误示范 - 不要这样做
Read ruoyi-vue-plus的代码  # ❌ 绝对禁止!

# 正确做法 - 必须这样做
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/service/impl/AdServiceImpl.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/dao/impl/AdDaoImpl.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/controller/AdController.java
```

**必须学习的要点**：

1. **包名结构**: `plus.ruoyi.business.base.*` (不是 `com.ruoyi.*`)
2. **Service 实现**: 不继承任何基类,直接实现接口
3. **DAO 层设计**: 独立的 buildQueryWrapper() 方法
4. **对象转换**: 统一使用 MapstructUtils.convert()
5. **实体继承**: 继承 TenantEntity (不是 BaseEntity)
6. **BO 映射**: 使用 @AutoMappers 注解

### ✅ 步骤 2: 检查工具类是否已存在 (强制)

```bash
# 使用 Grep 搜索相关功能
Grep pattern: "方法名关键词" path: ruoyi-common/*/utils/
```

### ✅ 步骤 3: 自检生成的代码 (强制)

生成代码后必须检查：

- [ ] ❌ 包名是否是 `com.ruoyi.*`? (必须是 `plus.ruoyi.*`)
- [ ] ❌ Service 是否继承了 `ServiceImpl` 或 `IBaseService`?
- [ ] ❌ DAO 是否缺少 `buildQueryWrapper()` 方法?
- [ ] ❌ 代码中是否有完整包名 (如 `com.ruoyi.xxx.ClassName`)?
- [ ] ❌ 是否使用了 `Map<String, Object>` 传递业务数据?
- [ ] ✅ Entity 是否继承 `TenantEntity`?
- [ ] ✅ BO 是否使用 `@AutoMappers` 注解?
- [ ] ✅ 是否所有类型都先 `import` 再使用简单类名?

**🔴 如果发现问题，立即修正后再输出给用户**

---

## 🔴 多会话并发自动避让协议（L1/L2/L3 三层触发）

> 用户可能同时开多个 Codex / Claude Code 会话操作同一仓库。本会话必须**自动感知并避让**其他会话的工作，**默认静默执行，不打扰用户**。
> 设计原则：宁可绕路，绝不覆盖；宁可静默放弃，绝不擅自 stash / reset / checkout。

### L1 — 启动时探测（首次响应前，仅执行一次）

```bash
git status -s
git branch --show-current
```

- 把"未提交文件清单"和"当前分支"记入会话上下文，整个会话复用，**不向用户复述**
- 若清单非空且与本次任务无关 → 视为"他者占用区"，本会话**不修改、不 stash、不 checkout、不 reset** 这些文件
- 若清单非空且与本次任务相关（用户接续之前的工作）→ 当作己方未完成工作正常处理

### L2 — 修改文件前（按需触发，单文件粒度）

修改任意已存在文件**之前**，执行：

```bash
git log -1 --format="%ar|%s" <file>
```

判定规则（严格按此执行，不询问）：

| 条件 | 处置 |
|------|------|
| 距今 ≥ 15 分钟 | ✅ 自由修改 |
| 距今 < 15 分钟 + 文件**不在** L1 未提交清单 | ✅ 自由修改（已提交的近期改动不冲突） |
| 距今 < 15 分钟 + 文件**在** L1 未提交清单 + 可绕开（新增功能/换路径） | ⚠️ **静默换路径绕开**，不告知用户 |
| 距今 < 15 分钟 + 文件**在** L1 未提交清单 + 必须改同文件 | 🛑 **此时唯一允许打扰用户一次**："`<file>` 15min 内有未提交改动，疑似其他会话占用，是否继续？" |

### L3 — 提交前（强校验，必做）

`git commit` 前：

```bash
git diff --cached --name-only
```

- 对照本会话明确改过的文件清单（自维护）
- 越界文件 → **静默 `git restore --staged <file>`**，仅提交本会话范围内文件
- 逐个 `git add <具体文件>`，**禁止** `git add -A` / `git add .`
- commit message 末尾可附 `[scope: <模块>]` 标识本次会话范围

### 跨会话操作禁令（不询问、直接禁止）

| 禁令 | 原因 |
|------|------|
| ❌ `git stash` / `git stash pop` | 会污染其他会话的工作区 |
| ❌ `git reset --hard` | 会丢其他会话的未提交改动 |
| ❌ `git checkout <file>`（丢弃改动） | 同上 |
| ❌ `git checkout <branch>`（切分支） | 除非用户明确指示 |
| ❌ `git add -A` / `git add .` | 可能误提交他者文件，必须逐个 add |
| ❌ `git clean -fd` | 会删他者未跟踪文件 |
| ❌ kill 端口 / `taskkill /F` 进程 | 他者 dev server 可能在用 |
| ❌ 删除 `docs/tasks/active/` 下非本会话任务文档 | 同上 |
| ❌ `npx kill-port` 不属于本会话启动的端口 | 同上 |

### 高并发场景升级 → git worktree

若用户明确"并行开发"或预计 30+ 分钟同时改**不同模块**，主动建议：

```bash
# 通用 git 方式（适用于 Codex CLI）
git worktree add ../proj-feature-x -b feature-x
cd ../proj-feature-x
codex   # 在独立 worktree 中启动新 Codex 会话
```

3-5 个并行最佳，5+ 会撞 API 速率限制。
> 注意：worktree **不能**隔离数据库、Redis、端口（8080/80/5173）。同时跑 dev server 仍需手动错开端口或 profile。

---

## ⚠️ 页面开发强制要求（最高优先级）

**在开发任何页面（前端或移动端）之前，必须严格遵守以下流程：**

### 1. 必须先参考现有代码

| 开发类型 | 必须先阅读的参考代码 | 路径 |
|---------|-------------------|------|
| **前端页面** | Ad 模块页面 | `plus-ui/src/views/business/base/ad/ad.vue` |
| **前端 API** | Ad API 定义 | `plus-ui/src/api/business/base/ad/adApi.ts`<br>`plus-ui/src/api/business/base/ad/adTypes.ts` |
| **移动端主页面** | 首页组件（完整示例） | `plus-uniapp/src/components/tabbar/Home.vue` |
| **移动端登录页** | 登录页面（表单示例） | `plus-uniapp/src/pages/auth/login.vue` |
| **移动端 API** | Home API 定义 | `plus-uniapp/src/api/app/home/homeApi.ts`<br>`plus-uniapp/src/api/app/home/homeTypes.ts` |
| **WD UI 封装组件** | 项目封装的 WD 组件 | `plus-uniapp/src/wd/components/wd-*/` |

### 2. 强制使用封装组件（禁止使用原生组件）

#### 前端 (plus-ui)

```vue
<!-- ❌ 禁止：使用原生 Element Plus 组件 -->
<el-form inline>
  <el-input v-model="form.name" />
  <el-select v-model="form.status" />
  <el-dialog v-model="visible" />
</el-form>

<!-- ✅ 必须：使用项目封装的 A* 组件 -->
<ASearchForm>
  <AFormInput v-model="form.name" label="名称" prop="name" />
  <AFormSelect v-model="form.status" :options="statusOptions" />
</ASearchForm>
<AModal v-model="visible" />
```

#### 移动端 (plus-uniapp)

```vue
<!-- ❌ 禁止：使用 uni-ui 组件 -->
<uni-forms>
  <uni-field v-model="form.name" />
</uni-forms>

<!-- ✅ 必须：使用 WD UI 组件 -->
<wd-form>
  <wd-input v-model="form.name" label="名称" />
</wd-form>
```

### 3. 开发前检查清单

- [ ] **已阅读**对应模块的参考代码（Ad 模块或类似页面）
- [ ] **已了解**项目封装的组件库（AForm*、AModal 等或 wd-*）
- [ ] **已确认**API 调用方式（`[err, data]` 格式）
- [ ] **已熟悉**项目代码风格（组件布局、变量命名、文件结构）
- [ ] **不使用**任何原生 UI 组件（el-*、uni-*）

### 4. 违规示例与后果

```typescript
// ❌ 严重违规：未参考现有代码，直接写
// 后果：代码风格不一致，维护困难，可能重复造轮子

// ❌ 严重违规：使用原生组件
import { ElMessage } from 'element-plus'
ElMessage.success('操作成功')
// 后果：破坏项目封装，导致样式不统一

// ✅ 正确做法：先读取 ad.vue 和 adApi.ts
// 1. Read plus-ui/src/views/business/base/ad/ad.vue
// 2. Read plus-ui/src/api/business/base/ad/adApi.ts
// 3. 按照相同风格编写新代码
```

### 5. 紧急提醒

> **在编写任何 Vue 组件或 API 调用代码之前，必须：**
> 1. 使用 Read 工具读取对应的参考代码
> 2. 理解项目的组件封装模式
> 3. 100% 复制项目现有的代码风格
>
> **否则代码将被要求重写！**

---

## 📋 标准代码模板 (基于广告模块真实代码)

> ⚠️ **重要**: 以下模板来自本项目的真实代码,**不是** ruoyi-vue-plus 的代码!

### 1. Entity 实体类

```java
package plus.ruoyi.business.base.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;  // ✅ 继承 TenantEntity
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 广告配置对象 b_ad
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_ad")
public class Ad extends TenantEntity {  // ✅ 继承 TenantEntity,不是 BaseEntity

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 广告名称
     */
    private String adName;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;
}
```

### 2. BO 业务对象

```java
package plus.ruoyi.business.base.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;  // ✅ 使用 AutoMappers
import io.github.linpeilie.annotations.AutoMapper;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 广告配置业务对象 b_ad
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({  // ✅ 自动映射配置
    @AutoMapper(target = Ad.class, reverseConvertGenerate = false),
    @AutoMapper(target = AdVo.class)
})
public class AdBo extends BaseEntity {

    /**
     * 主键id
     */
    @NotNull(message = "主键id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 广告名称
     */
    @NotBlank(message = "广告名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String adName;

    /**
     * 状态
     */
    private String status;
}
```

### 3. VO 视图对象

```java
package plus.ruoyi.business.base.domain.vo;

import plus.ruoyi.business.base.domain.Ad;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 广告配置视图对象 b_ad
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Ad.class)
public class AdVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @ExcelProperty(value = "主键id")
    private Long id;

    /**
     * 广告名称
     */
    @ExcelProperty(value = "广告名称")
    private String adName;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_enable_status")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;
}
```

### 4. Controller 控制器

```java
package plus.ruoyi.business.base.controller;

import java.util.List;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.service.IAdService;
import plus.ruoyi.common.openapi.annotation.OpenApi;

/**
 * 广告配置
 *
 * @author 抓蛙师
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/ad")
public class AdController {

    private final IAdService adService;  // ✅ 只注入Service

    /**
     * 查询广告配置列表
     */
    @SaCheckPermission("base:ad:query")
    @GetMapping("/pageAds")
    public R<PageResult<AdVo>> pageAds(AdBo bo, PageQuery pageQuery) {
        return R.ok(adService.page(bo, pageQuery));
    }

    /**
     * 获取广告配置详细信息
     */
    @SaCheckPermission("base:ad:query")
    @GetMapping("/getAd/{id}")
    public R<AdVo> getAd(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(adService.get(id));
    }

    /**
     * 新增广告配置
     */
    @SaCheckPermission("base:ad:add")
    @Log(title = "广告配置", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addAd")
    public R<Long> addAd(@Validated(AddGroup.class) @RequestBody AdBo bo) {
        return R.ok(adService.add(bo));
    }

    /**
     * 修改广告配置
     */
    @SaCheckPermission("base:ad:update")
    @Log(title = "广告配置", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateAd")
    public R<Void> updateAd(@Validated(EditGroup.class) @RequestBody AdBo bo) {
        return R.status(adService.update(bo));
    }

    /**
     * 删除广告配置
     */
    @SaCheckPermission("base:ad:delete")
    @Log(title = "广告配置", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteAds/{ids}")
    public R<Void> deleteAds(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(adService.batchDelete(List.of(ids)));
    }

    /**
     * 导出广告配置列表
     */
    @SaCheckPermission("base:ad:export")
    @Log(title = "广告配置", operType = DictOperType.EXPORT)
    @PostMapping("/exportAds")
    public void exportAds(AdBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<AdVo> pageResult = adService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "广告配置", AdVo.class, response);
    }

    /**
     * 导入广告配置
     */
    @Log(title = "广告配置", operType = DictOperType.IMPORT)
    @SaCheckPermission("base:ad:import")
    @PostMapping(value = "/importAds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importAds(MultipartFile file) throws Exception {
        ExcelResult<AdVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), AdVo.class, true);
        List<AdBo> boList = MapstructUtils.convert(excelResult.getList(), AdBo.class);
        adService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }
}
```

### 5. Service 接口

```java
package plus.ruoyi.business.base.service;

import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import java.util.Collection;
import java.util.List;

/**
 * 广告配置服务接口
 *
 * @author 抓蛙师
 */
public interface IAdService {  // ✅ 不继承IBaseService

    /**
     * 根据ID查询
     */
    AdVo get(Long id);

    /**
     * 查询列表
     */
    List<AdVo> list(AdBo bo);

    /**
     * 分页查询
     */
    PageResult<AdVo> page(AdBo bo, PageQuery pageQuery);

    /**
     * 新增
     */
    Long add(AdBo bo);

    /**
     * 修改
     */
    boolean update(AdBo bo);

    /**
     * 批量删除
     */
    boolean batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     */
    boolean batchSave(List<AdBo> boList);
}
```

### 6. Service 实现类

```java
package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.base.dao.IAdDao;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.business.base.service.IAdService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 广告配置服务实现
 *
 * @author 抓蛙师
 */
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements IAdService {  // ✅ 不继承任何基类

    private final IAdDao adDao;  // ✅ 只注入DAO

    /**
     * 根据ID查询
     */
    @Override
    public AdVo get(Long id) {
        Ad entity = adDao.getById(id);
        return MapstructUtils.convert(entity, AdVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<AdVo> list(AdBo bo) {
        PlusLambdaQuery<Ad> wrapper = adDao.buildQueryWrapper(bo);  // ✅ DAO层构建查询
        List<Ad> entities = adDao.list(wrapper);
        return MapstructUtils.convert(entities, AdVo.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<AdVo> page(AdBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Ad> wrapper = adDao.buildQueryWrapper(bo);  // ✅ DAO层构建查询
        PageResult<Ad> entityPage = adDao.page(wrapper, pageQuery);
        return entityPage.convert(AdVo.class);  // ✅ 使用PageResult自带的convert方法
    }

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AdBo bo) {
        Ad entity = MapstructUtils.convert(bo, Ad.class);
        beforeSave(entity);  // ✅ 保存前钩子
        adDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(AdBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("广告配置ID不能为空");
        }
        if (!adDao.exists(bo.getId())) {
            throw ServiceException.of("广告配置不存在");
        }
        Ad entity = MapstructUtils.convert(bo, Ad.class);
        beforeSave(entity);
        return adDao.updateById(entity);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);  // ✅ 删除前钩子
        return adDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<AdBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return true;
        }
        List<Ad> entities = new ArrayList<>(boList.size());
        for (AdBo bo : boList) {
            Ad entity = MapstructUtils.convert(bo, Ad.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return adDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     */
    protected void beforeSave(Ad entity) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 删除前钩子方法
     * 子类可重写此方法实现关联数据校验、清理等逻辑
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 默认实现为空,子类按需重写
    }
}
```

### 7. DAO 接口

```java
package plus.ruoyi.business.base.dao;

import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

/**
 * 广告配置DAO接口
 *
 * @author 抓蛙师
 */
public interface IAdDao extends IBaseDao<Ad> {

    /**
     * 根据业务对象构建查询条件
     * ✅ 这是本项目的核心设计 - 所有查询条件在DAO层构建
     */
    PlusLambdaQuery<Ad> buildQueryWrapper(AdBo bo);
}
```

### 8. DAO 实现类

```java
package plus.ruoyi.business.base.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.base.dao.IAdDao;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.mapper.AdMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import java.util.Map;

/**
 * 广告配置数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class AdDaoImpl extends BaseDaoImpl<AdMapper, Ad> implements IAdDao {

    /**
     * 构建查询条件
     * ✅ 这是本项目的核心设计 - 所有查询条件在此方法中构建
     */
    @Override
    public PlusLambdaQuery<Ad> buildQueryWrapper(AdBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Ad> lqw = PlusLambdaQuery.of();

        // ✅ 精确匹配查询条件 - 自动处理null值
        lqw.eq(Ad::getId, bo.getId());
        lqw.eq(Ad::getAppid, bo.getAppid());
        lqw.eq(Ad::getAdName, bo.getAdName());
        lqw.eq(Ad::getStatus, bo.getStatus());

        // ✅ 时间范围查询
        lqw.between(Ad::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // ✅ 模糊搜索 - 使用 searchValue 搜索多个字段
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(Ad::getAdName, searchValue)
                .or().like(Ad::getDescription, searchValue)
            );
        }
        return lqw;
    }
}
```

### 9. Mapper 接口

```java
package plus.ruoyi.business.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.base.domain.Ad;

/**
 * 广告配置Mapper接口
 * ✅ 只继承 BaseMapper,不继承 BaseMapperPlus
 *
 * @author 抓蛙师
 */
// ✅ 可选：数据权限注解
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface AdMapper extends BaseMapper<Ad> {
    // ✅ 无需额外方法,BaseDaoImpl已提供完整CRUD
}
```

### 10. 前端 API 定义

```typescript
// plus-ui/src/api/business/base/ad/adApi.ts
import type { AdQuery, AdBo, AdVo } from './adTypes'

/**
 * 查询广告配置列表
 */
export const pageAds = (query?: AdQuery): Result<PageResult<AdVo>> => {
  return http.get<PageResult<AdVo>>('/base/ad/pageAds', query)
}

/**
 * 查询广告配置详细
 */
export const getAd = (id: string | number): Result<AdVo> => {
  return http.get<AdVo>(`/base/ad/getAd/${id}`)
}

/**
 * 新增广告配置
 */
export const addAd = (data: AdBo): Result<string | number> => {
  return http.post<string | number>('/base/ad/addAd', data)
}

/**
 * 修改广告配置
 */
export const updateAd = (data: AdBo): Result<void> => {
  return http.put<void>('/base/ad/updateAd', data)
}

/**
 * 删除广告配置
 */
export const deleteAds = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/base/ad/deleteAds/${ids}`)
}
```

### 11. 前端类型定义

```typescript
// plus-ui/src/api/business/base/ad/adTypes.ts

/** 广告配置查询类型 */
export interface AdQuery extends PageQuery {
  id?: string | number
  appid?: string | number
  adName?: string
  status?: string
  createTime?: string
}

/** 广告配置表单类型 */
export interface AdBo {
  id?: string | number
  appid?: string | number
  adName?: string
  status?: string
  remark?: string
}

/** 广告配置视图类型 */
export interface AdVo {
  id: string | number
  appid: string | number
  adName: string
  status: string
  createTime: string
  updateTime: string
  remark: string
}
```

---

## 🚫 绝对禁止事项 (AI 必须 100% 遵守)

### ❌ 禁止 1: 使用完整类型引用（零容忍规则）⭐⭐⭐

**错误示范**（绝对禁止）：

```java
// ❌ 致命错误 - 直接使用完整包名
public plus.ruoyi.common.core.domain.R<AdVo> getAd(Long id) {
    return plus.ruoyi.common.core.domain.R.ok(adService.get(id));
}
```

**正确做法**（必须这样写）：

```java
// ✅ 第一步：import 导入
import plus.ruoyi.common.core.domain.R;

// ✅ 第二步：使用简单类名
public R<AdVo> getAd(Long id) {
    return R.ok(adService.get(id));
}
```

### ❌ 禁止 2: 使用 Map 封装业务数据（必须创建类）⭐⭐⭐

**错误示范**：

```java
// ❌ 错误 - 使用 Map 传递业务数据
public Map<String, Object> getAd(Long id) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", ad.getId());
    return result;
}
```

**正确做法**：

```java
// ✅ 正确 - 创建 VO 类
public AdVo getAd(Long id) {
    Ad ad = adDao.getById(id);
    return MapstructUtils.convert(ad, AdVo.class);
}
```

### ❌ 禁止 3: Service 层直接构建查询（必须在 DAO 层）⭐⭐⭐

**错误示范**：

```java
// ❌ 错误 - Service层直接构建查询
@Service
public class AdServiceImpl implements IAdService {
    private final AdMapper adMapper;  // ❌ 直接注入Mapper

    public PageResult<AdVo> page(AdBo bo, PageQuery pageQuery) {
        // ❌ Service层构建查询条件
        PlusLambdaQuery<Ad> lqw = PlusLambdaQuery.of();
        lqw.eq(Ad::getStatus, bo.getStatus());
        // ...
    }
}
```

**正确做法**：

```java
// ✅ 正确 - Service注入DAO,调用buildQueryWrapper
@Service
public class AdServiceImpl implements IAdService {
    private final IAdDao adDao;  // ✅ 注入DAO

    public PageResult<AdVo> page(AdBo bo, PageQuery pageQuery) {
        // ✅ 调用DAO层的buildQueryWrapper方法
        PlusLambdaQuery<Ad> wrapper = adDao.buildQueryWrapper(bo);
        PageResult<Ad> entityPage = adDao.page(wrapper, pageQuery);
        return entityPage.convert(AdVo.class);
    }
}
```

### ❌ 禁止 4: Service 继承基类（本项目不使用）⭐⭐⭐

**错误示范**：

```java
// ❌ 错误 - 继承了ServiceImpl (这是ruoyi-vue-plus的做法)
@Service
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {
    // ...
}
```

**正确做法**：

```java
// ✅ 正确 - 不继承任何基类
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements IAdService {
    private final IAdDao adDao;  // ✅ 只注入DAO
    // ...
}
```

---

## ⚠️ Bash/Shell 禁止项（最常犯错误！）

```bash
# ❌ 禁止：使用 > nul（Windows 会创建名为 nul 的文件！）
command > nul
command 2> nul

# ✅ 正确：不使用任何输出重定向，或使用跨平台方式
command
# 如果必须抑制输出，使用：
command > /dev/null 2>&1
```

**为什么会出错**：Windows 的 `nul` 设备在某些 Shell 环境下不被识别，会被当作普通文件名创建。

---

## API 路径规范

| 操作 | HTTP方法 | 路径格式 | 示例 |
|------|---------|---------|------|
| 分页查询 | GET | `/page{实体复数}` | `/pageAds` |
| 列表查询 | GET | `/list{实体复数}` | `/listAds` |
| 获取详情 | GET | `/get{实体}/{id}` | `/getAd/{id}` |
| 新增 | POST | `/add{实体}` | `/addAd` |
| 修改 | PUT | `/update{实体}` | `/updateAd` |
| 删除 | DELETE | `/delete{实体复数}/{ids}` | `/deleteAds/{ids}` |

---

## 前端核心规范 (plus-ui)

### 必须使用的组件

| 场景 | 必须使用 | 禁止使用 |
|------|---------|---------|
| 搜索表单 | `ASearchForm` | `el-form inline` |
| 输入框 | `AFormInput` | `el-input` |
| 下拉框 | `AFormSelect` | `el-select` |
| 日期选择 | `AFormDate` | `el-date-picker` |
| 弹窗 | `AModal` | `el-dialog` |
| 开关 | `AFormSwitch` | `el-switch` |

### API 定义

```typescript
// xxxApi.ts - http/Result/PageResult/PageQuery 已自动导入，无需 import
import type { XxxQuery, XxxBo, XxxVo } from './xxxTypes'

export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get('/base/xxx/pageXxxs', query)
}

export const getXxx = (id: string | number): Result<XxxVo> => {
  return http.get(`/base/xxx/getXxx/${id}`)
}

export const addXxx = (data: XxxBo): Result<string | number> => {
  return http.post('/base/xxx/addXxx', data)
}

export const updateXxx = (data: XxxBo): Result<void> => {
  return http.put('/base/xxx/updateXxx', data)
}

export const deleteXxxs = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del(`/base/xxx/deleteXxxs/${ids}`)
}
```

### 类型定义

```typescript
// xxxTypes.ts
export interface XxxQuery extends PageQuery {  // PageQuery 全局可用
  xxxName?: string
  status?: string
}

export interface XxxBo {
  id?: string | number
  xxxName?: string
  status?: string
}

export interface XxxVo {
  id: string | number
  xxxName: string
  status: string
  createTime: string
}
```

### 异步处理规范

```typescript
// ✅ 正确：使用 [err, data] 格式
const [err, data] = await pageXxxs(queryParams.value)
if (!err) {
  dataList.value = data.records
}

// ❌ 错误：使用 try-catch
try {
  const data = await pageXxxs(queryParams.value)
} catch (error) { ... }
```

---

## 移动端核心规范 (plus-uniapp)

### WD UI 组件导入

```typescript
// ⚠️ useToast/useMessage 必须手动导入
import { useToast, useMessage } from '@/wd'  // 不是 'wot-design-uni'！

const toast = useToast()
toast.success('操作成功')
```

### 样式单位

```scss
// ✅ 使用 rpx
.box {
  width: 200rpx;
  padding: 24rpx;
}

// ✅ CSS 注释必须用 /* */
/* 这是注释 */

// ❌ 禁止 // 注释
// 这样会报错
```

---

## 开发流程

#### 前端开发流程

```
1. 收到前端开发任务
   ↓
2. 【强制】Read plus-ui/src/views/business/base/ad/ad.vue
   ↓
3. 【强制】Read plus-ui/src/api/business/base/ad/adApi.ts 和 adTypes.ts
   ↓
4. 【强制】了解使用的封装组件：AFormInput、AFormSelect、AModal、ASearchForm 等
   ↓
5. 按照相同的代码风格编写（组件选择、API 调用、变量命名）
   ↓
6. 100% 使用 A* 组件，0% 使用 el-* 组件
```

#### 移动端开发流程

```
1. 收到移动端开发任务
   ↓
2. 【强制】根据任务类型 Read 参考代码：
   - 列表页 → src/components/tabbar/Home.vue
   - 表单页 → src/pages/auth/login.vue
   - API → src/api/app/home/homeApi.ts
   ↓
3. 【强制】了解使用的 WD 组件：wd-form、wd-input、wd-paging、wd-navbar 等
   ↓
4. 【强制】了解导入方式：import { useToast } from '@/wd'
   ↓
5. 按照相同的代码风格编写（目录结构、API 调用、组件使用）
   ↓
6. 100% 使用 wd-* 组件，0% 使用 uni-* 组件
```

---

## 常见错误速查

### 后端错误

| 错误写法 | 正确写法 |
|---------|---------|
| `plus.ruoyi.xxx.Xxx` (内联全限定名) | `import plus.ruoyi.xxx.Xxx;` 然后用 `Xxx` |
| `BeanUtil.copyProperties()` | `MapstructUtils.convert()` |
| `extends ServiceImpl<>` | `implements IXxxService` |
| `new LambdaQueryWrapper` (Service层) | `buildQueryWrapper()` (DAO层) |
| `extends BaseEntity` | `extends TenantEntity` |
| `@GetMapping("/page")` | `@GetMapping("/pageXxxs")` |
| `@GetMapping("/{id}")` | `@GetMapping("/getXxx/{id}")` |
| `like(Xxx::getId, value)` | `likeCast(Xxx::getId, value)` |
| `phonenumber` (原版RuoYi字段) | `phone`（本项目手机号字段） |
| `del_flag` (原版RuoYi字段) | `is_deleted`（本项目逻辑删除字段） |
| `AUTO_INCREMENT` | 不用，使用雪花ID |
| `R.ok(stringValue)` (返回String到data) | `R.ok(null, stringValue)` |
| `@Schema(description = "xxx")` | 普通CRUD不用，仅 @OpenApi 接口需要 |
| `is_frame`（菜单表字段名） | `is_external_link`（本项目菜单表字段名） |
| `visible='0'`, `status='0'`（菜单默认值） | `visible='1'`, `status='1'`（1=积极 0=消极） |

### 前端错误

| 错误写法 | 正确写法 | 适用端 |
|---------|---------|--------|
| **未读现有代码直接开发** | **先 Read ad.vue 等参考代码** | **前端/移动端** |
| `from 'wot-design-uni'` | `from '@/wd'` | 移动端 |
| `<el-dialog>` | `<AModal>` | 前端 |
| `<el-input>` | `<AFormInput>` | 前端 |
| `<el-select>` | `<AFormSelect>` | 前端 |
| `<el-form inline>` | `<ASearchForm>` | 前端 |
| `<el-switch>` | `<AFormSwitch>` | 前端 |
| `<uni-forms>` | `<wd-form>` | 移动端 |
| `<uni-field>` | `<wd-input>` | 移动端 |
| `try { await api() }` | `const [err, data] = await api()` | 前端/移动端 |
| `ElMessage.success()` | 使用项目封装的消息组件 | 前端 |
| `uni.showToast()` | `useToast().success()` | 移动端 |

---

## ⚡ 核心原则

**🎯 第一原则**: 写代码前必须先 Read 本项目的现有代码,严格遵循本项目的架构设计

**强制执行流程**:

1. **写代码前（必须）**：使用 `Read` 工具阅读本项目同模块现有代码
   ```bash
   # ✅ 正确示例：阅读本项目的代码
   Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/service/impl/AdServiceImpl.java
   Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/dao/impl/AdDaoImpl.java
   ```

2. **必须学习的要点**：
   - 包名结构: `plus.ruoyi.*` (不是 `com.ruoyi.*`)
   - Service 实现: 不继承任何基类
   - DAO 层设计: 独立的 buildQueryWrapper() 方法
   - 实体继承: TenantEntity (不是 BaseEntity)
   - BO 映射: @AutoMappers 注解

3. **参考顺序**：
   - ✅ 优先参考本项目的现有代码
   - ✅ 其次参考本文档的标准模板
   - ❌ 绝对禁止参考 ruoyi-vue-plus 的代码

---

## 🔧 组件注册与自动配置规范

**参考模块**: `ruoyi-common-langchain4j`、`ruoyi-common-rocketmq`

### 核心原则

1. ✅ **配置类中集中注册所有 Bean**，不使用 `@ComponentScan`
2. ✅ **使用显式构造函数打印配置信息**（早期执行）
3. ✅ **移除工具类内部的 `@Component` 静态类**
4. ✅ **静态工具类提供 `init()` 方法，由配置类调用**

---

### 标准做法：自动配置类

#### 1️⃣ 普通服务 Bean 注册（参考 langchain4j）

```java
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "langchain4j", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(LangChain4jProperties.class)
public class LangChain4jAutoConfiguration {

    /**
     * ✅ 显式构造函数：打印配置信息（早期执行）
     */
    public LangChain4jAutoConfiguration(LangChain4jProperties properties) {
        log.info("========================================");
        log.info("LangChain4j 模块初始化完成");
        log.info("默认提供商: {}", properties.getDefaultProvider());
        log.info("默认模型: {}", properties.getDefaultModel());
        log.info("========================================");
    }

    /**
     * ✅ 注册服务 Bean：使用 @ConditionalOnMissingBean 支持用户自定义
     */
    @Bean
    @ConditionalOnMissingBean  // ✅ 按返回类型判断
    public ModelFactory modelFactory(LangChain4jProperties properties) {
        return new ModelFactory(properties);  // ✅ 返回具体类型
    }

    @Bean
    @ConditionalOnMissingBean
    public ChatService chatService(ModelFactory modelFactory, ...) {
        return new ChatService(modelFactory, ...);
    }
}
```

**要点：**
- ✅ 使用 `@ConditionalOnMissingBean` **按类型判断**，允许用户自定义覆盖
- ✅ 返回**具体类型**（如 `ModelFactory`），不返回 `Object`
- ✅ 构造函数中打印配置信息

---

#### 2️⃣ 静态工具类初始化（参考 rocketmq）

```java
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "rocketmq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RocketMQProperties.class)
public class RocketMQAutoConfiguration {

    private final RocketMQProperties properties;

    /**
     * ✅ 显式构造函数：打印配置信息（早期执行）
     */
    public RocketMQAutoConfiguration(RocketMQProperties properties) {
        this.properties = properties;
        log.info("========================================");
        log.info("🚀 RocketMQ 模块开始初始化");
        log.info("  - NameServer: {}", properties.getNameServer());
        log.info("  - 集群名称: {}", properties.getClusterName());
        log.info("========================================");
    }

    /**
     * ✅ 初始化器 Bean：不使用 @ConditionalOnMissingBean
     * 因为初始化器必须执行，不应该被覆盖
     */
    @Bean
    public Object rmSendUtilInitializer(RocketMQTemplate rocketMQTemplate) {
        RMSendUtil.init(rocketMQTemplate, properties);
        return new Object();  // 占位对象
    }

    @Bean
    public Object rmTopicUtilInitializer() {
        RMTopicUtil.init(properties);
        return new Object();
    }

    /**
     * ✅ 启动完成日志：在所有初始化器执行后打印（晚期执行）
     */
    @Bean
    public Object rocketMQStartupLogger() {
        log.info("========================================");
        log.info("✅ RocketMQ 客户端启动完成！");
        log.info("========================================");

        // 执行诊断
        RMDiagnosticUtil.diagnose();

        return new Object();
    }
}
```

**要点：**
- ✅ 初始化器 Bean **不使用** `@ConditionalOnMissingBean`（必须执行）
- ✅ 构造函数打印配置信息（早期）
- ✅ Bean 方法打印启动状态（晚期）
- ✅ 形成**两阶段日志**：配置阶段 → 启动完成

---

### 静态工具类设计

#### ✅ 正确做法

```java
@Slf4j
public class RMSendUtil {

    private static RocketMQTemplate rocketMQTemplate;
    private static RocketMQProperties properties;

    private RMSendUtil() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    /**
     * ✅ 提供公开的静态初始化方法
     * 由 RocketMQAutoConfiguration 调用
     */
    public static void init(RocketMQTemplate template, RocketMQProperties props) {
        rocketMQTemplate = template;
        properties = props;
        log.debug("RMSendUtil 初始化完成");  // ✅ 使用 debug 级别，避免重复日志
    }

    /**
     * ✅ 保持静态调用方式
     */
    public static SendResult send(String topic, Object message) {
        checkTemplateAvailable();
        return rocketMQTemplate.syncSend(topic, message);
    }
}
```

**要点：**
- ✅ 提供 `init()` 静态方法，由配置类调用
- ✅ `init()` 方法使用 `log.debug()` 级别，避免重复日志
- ✅ 移除内部的 `@Component` 静态类
- ✅ 保持静态调用方式不变

---

#### ❌ 错误做法（禁止）

```java
@Slf4j
public class RMSendUtil {

    private static RocketMQTemplate rocketMQTemplate;

    // ❌ 禁止：使用内部 @Component 类
    @Component
    @ConditionalOnProperty(prefix = "rocketmq", name = "enabled", havingValue = "true")
    static class RocketMQTemplateHolder implements InitializingBean {

        @Autowired
        private RocketMQTemplate template;

        @Override
        public void afterPropertiesSet() {
            RMSendUtil.rocketMQTemplate = this.template;
            log.info("✅ RMSendUtil 初始化完成");  // ❌ 日志分散
        }
    }

    public static SendResult send(String topic, Object message) {
        return rocketMQTemplate.syncSend(topic, message);
    }
}
```

**问题：**
- ❌ 使用内部 `@Component` 静态类，组件注册分散
- ❌ 需要配合 `@ComponentScan` 使用
- ❌ 日志输出分散，不清晰
- ❌ 违反单一职责原则（工具类不应该包含 Spring 组件）

---

### @ConditionalOnMissingBean 使用规范

#### ✅ 何时使用？

**场景：注册服务 Bean，允许用户自定义覆盖**

```java
@Bean
@ConditionalOnMissingBean  // ✅ 按返回类型判断
public ModelFactory modelFactory(LangChain4jProperties properties) {
    return new ModelFactory(properties);  // ✅ 返回具体类型
}
```

**条件：**
- ✅ 返回**具体类型**（如 `ModelFactory`、`ChatService`）
- ✅ 用户可能想自定义实现
- ✅ 按**返回类型**判断，不使用 `name` 属性

---

#### ❌ 何时不使用？

**场景：初始化器 Bean，必须执行**

```java
@Bean
// ✅ 不使用 @ConditionalOnMissingBean
public Object rmSendUtilInitializer(RocketMQTemplate rocketMQTemplate) {
    RMSendUtil.init(rocketMQTemplate, properties);
    return new Object();  // 返回占位对象
}
```

**理由：**
- ✅ 初始化器必须执行，不应该被覆盖
- ✅ 如果用户想禁用，应该通过 `xxx.enabled=false` 控制
- ✅ 返回 `Object` 无意义，只是触发方法执行

---

#### ❌ 禁止使用 name 属性

```java
// ❌ 错误：使用 name 属性
@Bean
@ConditionalOnMissingBean(name = "rmSendUtilInitializer")
public Object rmSendUtilInitializer(...) {
    return new Object();
}
```

**问题：**
- ❌ 用户需要知道具体的 Bean 名称才能覆盖
- ❌ 返回 `Object` 类型，无法按类型判断
- ❌ 对于初始化器来说，覆盖没有意义

---


## 🔄 四层架构数据流转 (本项目独有)

```
用户请求 → Controller → Service → DAO → Mapper → 数据库
         ↓          ↓        ↓      ↓       ↓
      接收参数   业务逻辑  构建查询  执行SQL   返回数据
         ↓          ↓        ↓      ↓       ↓
      返回R<T>  调用DAO  buildQueryWrapper  CRUD  Entity
```

**关键要点**：

1. **Controller 层**:
   - 只注入 Service,不注入 DAO
   - 参数校验使用 `@Validated` + 分组校验
   - 返回值统一使用 `R<T>` 包装

2. **Service 层**:
   - ❌ **不继承任何基类** (这是与 ruoyi-vue-plus 的核心区别)
   - 只注入 DAO,不注入 Mapper
   - 通过 `adDao.buildQueryWrapper(bo)` 获取查询条件
   - 使用 `MapstructUtils.convert()` 进行对象转换

3. **DAO 层**:
   - ✅ **本项目独有的架构设计**
   - 继承 `BaseDaoImpl<Mapper, Entity>`
   - 实现 `buildQueryWrapper(Bo)` 方法构建查询条件
   - 提供完整的CRUD方法

4. **Mapper 层**:
   - 只继承 `BaseMapper<Entity>`
   - ❌ 不继承 `BaseMapperPlus`
   - 无需额外方法,BaseDaoImpl 已提供完整CRUD

---

## 📁 项目结构速查

```
ruoyi-plus-uniapp/                    ✅ 本项目
├── ruoyi-admin/                      # 后端启动入口
├── ruoyi-common/                     # 通用工具模块
│   ├── ruoyi-common-core/           # 核心工具
│   ├── ruoyi-common-mybatis/        # MyBatis扩展 (含DAO层基类)
│   ├── ruoyi-common-tenant/         # 租户管理 (含TenantEntity)
│   └── ...
├── ruoyi-modules/
│   └── ruoyi-business/              # 业务模块
│       ├── controller/              # PC端控制器
│       ├── api/                     # 移动端API
│       │   ├── app/                 # 移动端接口
│       │   └── pc/                  # PC端接口(新)
│       ├── service/                 # 业务服务层 (不继承基类)
│       │   └── impl/                # 服务实现
│       ├── dao/                     # ✅ 数据访问层 (本项目独有)
│       │   └── impl/                # DAO实现 (含buildQueryWrapper)
│       ├── mapper/                  # MyBatis映射器
│       └── domain/                  # 领域模型
│           ├── Entity.java          # 实体类 (继承TenantEntity)
│           ├── bo/                  # 业务对象 (使用@AutoMappers)
│           └── vo/                  # 视图对象
├── plus-ui/                         # 前端管理端
└── plus-uniapp/                     # 移动端
```

## 参考代码位置

### 后端标准模块 (Ad)
```
ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/
├── controller/AdController.java
├── service/IAdService.java
├── service/impl/AdServiceImpl.java
├── dao/IAdDao.java
├── dao/impl/AdDaoImpl.java
├── mapper/AdMapper.java
└── domain/
    ├── Ad.java
    ├── bo/AdBo.java
    └── vo/AdVo.java
```

### 前端 (plus-ui)

**API 定义**
```
plus-ui/src/api/business/base/ad/
├── adApi.ts          # API 接口定义
└── adTypes.ts        # 类型定义
```

**页面**
```
plus-ui/src/views/business/base/ad/ad.vue   # 标准CRUD页面
```

### 移动端 (plus-uniapp)

> **⚡ 路由自动生成**：项目使用 `uni-pages` 插件，新建页面文件后**无需手动修改 `pages.json`**，插件会自动扫描 `pages/` 和 `pages-sub/` 目录生成路由。

**目录结构**
```
plus-uniapp/src/
├── pages/                          # 主页面目录
│   ├── auth/
│   │   └── login.vue              # ✅ 登录页（表单示例）
│   ├── index/
│   │   └── index.vue              # 主入口（Tabbar容器）
│   └── my/
│       └── settings.vue           # 设置页
├── pages-sub/                     # 子页面目录（分包）
│   └── admin/
│       └── user/
│           └── user.vue           # 业务子页面示例
├── components/                    # 业务组件
│   ├── auth/
│   │   └── AuthModal.vue          # 认证弹窗
│   └── tabbar/
│       ├── Home.vue               # ✅ 首页（完整示例：列表+分页+支付）
│       ├── Menu.vue               # 菜单页
│       └── My.vue                 # 我的页
├── api/                           # API 定义
│   └── app/
│       └── home/
│           ├── homeApi.ts         # ✅ API 接口定义
│           └── homeTypes.ts       # ✅ 类型定义
├── composables/                   # Composables
│   ├── useAuth.ts                 # 认证
│   ├── usePayment.ts              # 支付
│   └── useScroll.ts               # 滚动
└── wd/                            # WD UI 封装
    ├── index.ts                   # 导出（useToast/useMessage）
    └── components/                # 封装的 WD 组件
        ├── wd-form/
        ├── wd-input/
        ├── wd-button/
        ├── wd-navbar/
        ├── wd-paging/             # 分页组件
        └── ...                    # 80+ WD 组件
```

**推荐参考顺序**（移动端开发）
1. **首页组件**：`src/components/tabbar/Home.vue`（包含：列表、分页、wd-paging、支付）
2. **登录页面**：`src/pages/auth/login.vue`（包含：表单、wd-form、wd-input、验证）
3. **API 定义**：`src/api/app/home/homeApi.ts` 和 `homeTypes.ts`
4. **WD 组件**：`src/wd/components/wd-*/`（按需查看）

## 快速命令

| 命令 | 用途 |
|------|------|
| `/dev` | 开发新功能（完整流程） |
| `/crud` | 快速生成 CRUD |
| `/check` | 代码规范检查 |
| `/init-docs` | 初始化项目文档（支持空白模板/扫描代码） |
| `/progress` | 查看项目进度 |
| `/add-todo` | 快速添加待办事项 |

---

## 🛠️ 工具类完整清单

### 后端核心工具类

| 工具类 | 功能 | 常用方法 |
|--------|------|---------|
| **StringUtils** | 字符串操作 | `isNotBlank()` `isBlank()` `split()` |
| **MapstructUtils** | 对象转换 | `convert(source, TargetClass)` |
| **DateUtils** | 日期处理 | `formatDate()` `parseDate()` |
| **ObjectUtils** | 对象判断 | `isNull()` `isNotNull()` `isEmpty()` |
| **CollUtil** | 集合操作 | `isEmpty()` `isNotEmpty()` (Hutool) |
| **ServiceException** | 业务异常 | `ServiceException.of("错误信息")` |

---

### like vs likeCast（跨数据库兼容）

| 字段类型 | 方法 | 说明 |
|---------|------|------|
| `String` | `like()` | 直接模糊匹配 |
| `Long/Integer/Date` | `likeCast()` | 自动类型转换（PostgreSQL 必须） |

---

## 🎨 图标使用规范

### 前端图标 (PC Web)
- **图标类型文件**: `plus-ui/src/types/icons.d.ts`
- **组件**: `<Icon name="图标名" />`
- **使用前**: 先 Grep 搜索图标名是否存在于类型文件中

### 移动端图标 (Uniapp)
- **图标类型文件**: `plus-uniapp/src/wd/components/wd-icon/wd-icon.vue` (第 24-420 行)
- **组件**: `<wd-icon name="图标名" />`
- **支持三种图标**:
    - 字体图标: `home`, `user-fill` 等
    - UnoCSS 图标: `i-carbon-user` (以 `i-` 开头)
    - 图片图标: 图片路径
- **使用前**: 先 Read/Grep 确认图标名存在

---

## 💾 数据库设计规范

### ⚠️ 设计数据库前必须阅读
**强制要求**: 设计数据库表结构前,**必须先阅读** `.claude/数据库设计规范.md`

### 布尔值和状态字段
- **状态字段** (`status`): `1`=正常(积极), `0`=停用(消极)
- **删除标记** (`is_deleted`): `0`=正常, `1`=已删除
- **是否字段** (`is_xxx`): `1`=是, `0`=否

### 参考现有表结构
设计前先查看项目现有表结构: `script/sql/ry_plus_app.sql` (如 `b_ad`, `b_bind` 等)

---

## 🤖 AI 最终检查清单

**在输出代码给用户之前，AI 必须回答以下问题：**

### 📝 架构设计检查

- [ ] ❌ 我是否参考了 ruoyi-vue-plus 的代码？（必须为 NO）
- [ ] ✅ 我是否 Read 了本项目的现有代码？（必须为 YES）
- [ ] ✅ 包名是否是 `plus.ruoyi.*`？（必须为 YES）
- [ ] ✅ Service 是否不继承任何基类？（必须为 YES）
- [ ] ✅ DAO 是否有 `buildQueryWrapper()` 方法？（必须为 YES）
- [ ] ✅ Entity 是否继承 `TenantEntity`？（必须为 YES）
- [ ] ✅ BO 是否使用 `@AutoMappers` 注解？（必须为 YES）

### 🔍 代码质量检查

- [ ] ✅ 代码中是否有完整包名？（必须为 NO）
- [ ] ✅ 是否使用了 `Map<String, Object>` 传递业务数据？（必须为 NO）
- [ ] ✅ 是否所有类型都先 import 再使用？（必须为 YES）
- [ ] ✅ 是否使用 `MapstructUtils.convert()` 转换对象？（必须为 YES）

### 🔴 如果任何一项检查失败，必须修正后再输出！

---

## 📖 快速对比表 (本项目 vs ruoyi-vue-plus)

| 项目 | ruoyi-vue-plus | 本项目 (ruoyi-plus-uniapp) |
|------|---------------|---------------------------|
| **包名** | `com.ruoyi` | `plus.ruoyi` |
| **Service** | 继承 `ServiceImpl` | ❌ 不继承任何基类 |
| **DAO层** | 无独立DAO层 | ✅ 有独立DAO层 + buildQueryWrapper |
| **Mapper** | 继承 `BaseMapperPlus` | 继承 `BaseMapper` |
| **Entity** | 继承 `BaseEntity` | 继承 `TenantEntity` |
| **BO映射** | 手动配置 | `@AutoMappers` 自动 |
| **对象转换** | `BeanUtil` | `MapstructUtils` |

**🔴 重要**: 本项目与 ruoyi-vue-plus 是完全不同的架构设计,必须严格遵循本项目的规范！


## 📖 深度参考 (按需查阅)

开发遇到问题时查阅对应指南:

- **后端开发**: `.claude/后端开发指南.md` - 架构理解、业务开发
- **前端开发**: `.claude/前端开发指南.md` - Vue3组件、状态管理
- **移动端开发**: `.claude/移动端开发指南.md` - WD UI组件库详解
- **工具类使用**: `.claude/工具类使用指南.md` - 工具类完整用法
- **数据库设计**: `.claude/数据库设计规范.md` - 表设计、索引优化

---

> 最后提醒: 写代码前先 Read 本项目现有代码,不要凭印象或参考其他框架!
