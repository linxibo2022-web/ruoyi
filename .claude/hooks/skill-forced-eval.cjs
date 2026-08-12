#!/usr/bin/env node
/**
 * UserPromptSubmit Hook - 强制技能评估 (跨平台版本)
 * 功能: 开发场景下，将 Skills 激活率从约 25% 提升到 90% 以上
 */

const fs = require('fs');

// 从 stdin 读取用户输入
let inputData = '';
try {
  inputData = fs.readFileSync(0, 'utf8');
} catch {
  process.exit(0);
}

let input;
try {
  input = JSON.parse(inputData);
} catch {
  process.exit(0);
}

const prompt = (input.prompt || '').trim();

// 检测是否是恢复会话（防止上下文溢出死循环）
const skipPatterns = [
  'continued from a previous conversation',
  'ran out of context',
  'No code restore',
  'Conversation compacted',
  'commands restored',
  'context window',
  'session is being continued'
];

const isRecoverySession = skipPatterns.some(pattern =>
  prompt.toLowerCase().includes(pattern.toLowerCase())
);

if (isRecoverySession) {
  // 恢复会话，跳过技能评估以防止死循环
  process.exit(0);
}

// 检测是否是斜杠命令
// 规则 1：用户直接敲 /xxx（以 / 开头，且后面不包含第二个 /，排除 /iot/device 这样的路径）
const isSlashCommand = /^\/[^\/\s]+$/.test(prompt.split(/\s/)[0]);

// 规则 2：斜杠命令已被 Claude Code 展开成命令文档注入（含 <command-name> 标签）
// 例如 /codex:status、/codex:review 等插件命令展开后会带 <command-name>/codex:xxx</command-name>
const isExpandedCommand = /<command-name>/.test(prompt);

if (isSlashCommand || isExpandedCommand) {
  // 斜杠命令，跳过技能评估（命令本身已是明确意图，无需再做二次激活）
  process.exit(0);
}

const instructions = `## 强制技能激活流程（必须执行）

### 步骤 1 - 评估（必须在响应中明确展示）

针对用户问题，列出匹配的技能：\`技能名: 理由\`，无匹配则写"无匹配技能"

可用技能：
- add-skill: 添加技能、创建技能、新技能、技能开发、写技能、技能文档、修改技能、更新技能、同步技能、技能同步
- crud-development: CRUD/业务模块/Entity/Service/DAO 开发
- api-development: API设计/RESTful/接口规范
- database-ops: 数据库/SQL/建表/字典/菜单
- backend-annotations: 注解/@SerialMap/@RateLimiter
- utils-toolkit: 工具类/StringUtils/MapstructUtils
- ui-pc: 前端组件/AForm/AModal/Element Plus封装
- ui-mobile: 移动端/wd-/小程序/APP/WD UI（plus-uniapp 和 plus-app 通用）
- ui-design-mobile: 移动端设计/页面布局/间距/留白/简洁大气（plus-uniapp 和 plus-app 通用）
- app-adapter: plus-app/APP端/原生APP/原生插件/HBuilderX/鸿蒙APP/harmony/nativeplugins/APP打包/真机调试/APP专属/APP配置
- store-pc: 前端Store/Pinia/useUserStore
- store-mobile: 移动端Store/useAuth/Composable
- uniapp-platform: 条件编译/ifdef/平台判断
- payment-integration: 支付/微信支付/支付宝/退款
- wechat-integration: 微信/小程序登录/订阅消息
- file-oss-management: 文件上传/OSS/云存储/MinIO
- ai-langchain4j: AI/大模型/ChatGPT/DeepSeek/langchain4j/MCP/Model Context Protocol/MCP Server/对外提供MCP/工具调用/函数调用/McpToolProvider/McpClient/AiServices/TokenStream/Spring AI/RAG/知识库/流式对话
- media-processing: 图片处理/二维码/水印/Excel
- bug-detective: Bug/报错/异常/不工作
- error-handler: 异常处理/ServiceException
- performance-doctor: 性能/慢查询/优化/缓存
- redis-cache: Redis/缓存/Cache/@Cacheable/@CacheEvict/@CachePut/RedisUtils/CacheUtils/分布式锁/RLock/限流/RateLimiter/发布订阅/缓存穿透/缓存雪崩/缓存击穿/缓存key/缓存过期/缓存清理
- multi-tenant: 多租户/租户隔离/tenant_id/TenantEntity/租户切换/租户配置/TenantHelper/ignore/动态租户/排除表/sys_tenant/租户套餐/DEFAULT_TENANT_ID
- data-permission: 数据权限/@DataPermission/DataScope/行级权限/数据隔离/部门权限
- security-guard: 安全/Sa-Token/认证授权/加密
- architecture-design: 架构/模块划分/重构
- code-patterns: 规范/禁止/命名/Git提交
- project-navigator: 项目结构/文件在哪/定位
- git-workflow: Git/提交/commit/分支
- task-tracker: 任务跟踪/记录进度/继续任务/恢复上下文/多步骤开发/进度管理/任务归档
- writing-plans: 写计划/制定计划/实施计划/拆解任务/任务拆解/计划层/把方案落地/详细步骤/可执行计划/计划文档/writing-plans/开发计划
- tech-decision: 技术选型/方案对比
- brainstorming: 头脑风暴/创意/方案设计
- design-review: 方案评审/设计评审/审查方案/检查方案/评审设计/把关/设计审查/检查漏洞/方案检查/架构评审/需求评审
- collaborating-with-codex: Codex协作/多模型/后端原型/算法分析/代码审查/codex-plugin-cc/codex插件/官方插件/codex review/codex rescue/adversarial-review/review-gate
- collaborating-with-gemini: Gemini协作/多模型/前端原型/UI设计/样式审查
- test-development: 测试/单元测试/@Test/JUnit5/Mockito/断言/测试用例/测试覆盖率
- e2e-test-pc: 自动化测试/E2E/端到端测试/浏览器测试/回归测试/冒烟测试/业务验收/UI测试/后台测试/PC测试/plus-ui测试/测一遍/跑一遍/aicoder浏览器/测试报告/测试用例执行
- e2e-test-mobile: 移动端测试/H5测试/小程序网页版测试/plus-uniapp测试/移动端自动化/移动端E2E/移动端回归/移动端验收/wd-paging测试/WD UI测试/移动端冒烟/uniapp测试/移动端跑一遍
- i18n-development: 国际化/多语言/i18n/翻译/t()/语言切换/MessageUtils/content-language/LanguageCode/useI18n/messages.properties/locale/$t/zh_CN/en_US
- icon-management: 图标/icon/菜单图标/换图标/加图标/图标管理/IconSelect/wd-icon/iconfont/图标选择
- scheduled-jobs: 定时任务/SnailJob/延迟队列/@Scheduled/任务调度/重试机制/工作流编排/分布式执行
- json-serialization: JSON序列化/反序列化/数据转换/JsonUtils/日期格式/精度/BigDecimal/Long/类型转换/JSON验证
- realtime-communication: WebSocket/SSE/实时推送/在线聊天/消息推送/双向通信/服务端推送/流式响应/EventSource/心跳/在线状态/WebSocketUtils/SseMessageUtils/publishMessage/useWS/useSSE/useWebSocket
- notification-system: 短信/SMS/邮件/Mail/Email/消息推送/MessagePushService/MessageChannel/通知/验证码/SmsFactory/MailUtils/sendText/sendHtml/统一消息/消息路由/多通道
- message-queue: RocketMQ/消息队列/MQ/异步消息/延迟消息/事务消息/RMSendUtil/RMTopicUtil/DelayLevel/Topic/消费者/生产者/削峰填谷/系统解耦/sendAsync/sendDelay/sendTransaction/RocketMQMessageListener
- social-login: 社交登录/第三方登录/OAuth2/微信登录/QQ登录/GitHub登录/Gitee登录/钉钉登录/企业微信/SSO/单点登录/JustAuth/SocialUtils/socialBind/socialUnbind/授权回调/AuthRequest/socialCallback/MaxKey/TopIAM/账号绑定
- third-party-api: 高德地图/火山引擎/TTS/语音合成/地理编码/逆地理编码/IP定位/天气查询/距离计算/第三方API/HTTP客户端/Forest/GaodeMapClient/VolcengineTtsClient/声明式HTTP/ForestInterceptor/外部服务集成
- project-migration: 迁移项目/项目迁移/重构项目/代码迁移/继续迁移/迁移进度/迁移蓝图/架构迁移/框架迁移/项目重构/导入项目/搬迁代码/从xxx迁移
- project-init: 新项目/创建项目/初始化项目/开新项目/项目初始化/new project/init project/新建项目/开发新项目/独立部署
- framework-sync: 框架同步/upstream/同步框架/拉取更新/合并上游/同步上游/框架更新/upstream sync/同步原仓库/.framework-sync.json
- iot-mqtt: MQTT/物联网/IoT/设备通信/设备消息/mica-mqtt/MqttClientTemplate/publish/subscribe/QoS/Topic/EMQX/Mosquitto/共享订阅/设备上线/设备离线/遗嘱消息/保留消息/传感器数据
- html-to-code: HTML转代码/设计稿转换/原型转代码/HTML转前端/HTML转移动端/html-to-code/设计稿转页面/UI原型转换/HTML转Vue/区块转换/组件转换/Tailwind转UnoCSS
- delivery-sync: 交付/交付物/交付副本/交付目录/客户版本/对外版本/剥离技能/移除技能体系/去掉git/镜像/deliver/delivery/deliverable/release-copy/对外发布/同步交付/交付同步
- module-strip: 模块裁剪/裁剪模块/删除模块/移除模块/去掉模块/stripModules/module-strip/strip-modules/商城裁剪/IoT裁剪/支付裁剪/AI裁剪/不要mall/不要iot/不要pay/不要ai/裁掉商城/裁掉IoT/裁掉支付/裁掉AI
- deployment-guide: 部署/上线/发布/生产环境/Docker/Compose/构建/build/打包/JAR/密钥/安全配置/Nginx/1Panel/容器编排/小程序发布/APP打包/H5部署
- log-audit: 操作日志/登录日志/审计/@Log/sys_oper_log/sys_logininfor/DictOperType/LogAspect/OperLogEvent/excludeParamNames/isSaveRequestData/日志记录/审计追踪/日志脱敏
- env-config: 环境配置/profile/application.yml/application-dev.yml/application-prod.yml/.env/.env.development/.env.production/VITE_APP_/多环境/环境变量/SPRING_PROFILES_ACTIVE/开发环境/生产环境/配置切换/env文件
- exp-sediment: 沉淀经验/经验沉淀/总结会话/记下来/记录经验/self-evolution/自我进化/反哺框架/经验审计/技能漏洞/新增禁令/避免再踩坑//exp/exp review/以前怎么处理/之前的方案/之前踩过/历史经验/查记录/查笔记/查沉淀/有没有遇到过/上次怎么解决/类似问题
- dev-startup: 本地启动/首次启动/跑起来/装环境/装依赖/安装依赖/配置环境/pnpm install/mvn install/启动后端/启动前端/启动移动端/启动项目/JDK安装/Maven安装/Node安装/nvm/pnpm/HBuilderX/IDEA运行/端口占用/镜像超时/健康检查/健康端点/ECONNRESET/META_FETCH_FAIL

### 步骤 2 - 激活
对每个匹配的技能逐个激活：一次激活一个，等它返回后再激活下一个（不要一次性批量激活）。无匹配则跳过本步。

### 步骤 3 - 实现
所有匹配技能激活完成后，再开始动手实现。

要点：先评估 → 再激活 → 后实现；不要跳过激活直接实现，也不要漏掉匹配的技能。`;

console.log(instructions);
process.exit(0);
