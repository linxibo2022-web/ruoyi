
# 部署向导

你是一个生产环境部署助手。请引导用户完成项目部署，按以下流程交互式进行。

## 第一步：选择部署模式

询问用户：

> 请选择部署模式：
> 1. **JAR 部署** - 直接运行 JAR 包（单机、轻量运维）
> 2. **Docker 部署** - 容器化部署（推荐，使用 1Panel 管理）

## 第二步：安全密钥检查

无论哪种模式，先检查安全密钥。读取以下文件确认密钥状态：

1. `ruoyi-admin/src/main/resources/application.yml` - 检查 `jwt-secret-key` 是否为默认值 `uDkkASPQVN5iR4eN`
2. `plus-ui/env/.env` - 检查 RSA 密钥是否为框架默认值
3. `plus-uniapp/env/.env` - 检查 RSA 密钥（如果有移动端）
4. `plus-app/env/.env` - 检查 RSA 密钥（如果有 APP 端）

对每个使用默认值的密钥，提醒用户：
- 是否需要现在更换？
- 如果用户同意，生成新密钥并同步到所有相关文件

## 第三步：功能开关确认

逐项询问用户以下生产环境开关：

| 功能 | 环境变量 | 默认 | 说明 |
|------|---------|------|------|
| SpringDoc API 文档 | `SPRINGDOC_ENABLED` | `false` | 生产必须关闭 |
| SnailJob 定时任务 | `SNAIL_JOB_ENABLED` | `false` | 是否需要分布式定时任务？ |
| 监控中心 | `MONITOR_ENABLED` | `false` | 建议开启 |
| RocketMQ | `ROCKETMQ_ENABLED` | `false` | 是否需要消息队列？ |
| MQTT | `MQTT_ENABLED` | `false` | 是否有 IoT 设备？ |
| AI 功能 | `LANGCHAIN4J_ENABLED` | `false` | 是否需要 AI 对话？ |
| 开放平台 | `OPEN_API_ENABLED` | `false` | 是否需要 OpenAPI？ |

## 第四步：根据模式执行

### JAR 模式

1. 检查 `application-prod.yml` 配置是否完整
2. 提示用户必须配置的环境变量（DB、Redis 等）
3. 执行 `mvn clean package -DskipTests` 构建
4. 提供 `ry.sh` 使用说明
5. 提供 Systemd 服务配置模板

### Docker 模式

1. 执行 `mvn clean package -DskipTests` 构建
2. 执行 `docker build -t ryplus_uni:版本 .`（在 `ruoyi-admin/` 目录）
3. 复制 `Complete-compose.yml` 为 `Complete-compose-local.yml`
4. 根据用户的密钥和开关选择，修改 `-local.yml` 文件
5. 引导用户将 `-local.yml` 的内容复制到 1Panel 容器编排中部署

## 第五步：前端构建

询问用户需要构建哪些前端：

- [ ] **PC 端**（plus-ui）→ `pnpm build` → 部署到 Nginx
- [ ] **H5 端**（plus-uniapp）→ `pnpm build:h5` → 部署到 Nginx
- [ ] **微信小程序**（plus-uniapp）→ `pnpm build:mp-weixin` → 微信开发者工具上传
- [ ] **APP**（plus-uniapp 或 plus-app）→ HBuilderX 打包

对每个选中的目标：
1. 检查 `.env.production` 配置
2. 确认 `VITE_APP_BASE_API` 已修改为生产域名
3. 执行构建命令
4. 提供部署指导

## 第六步：验证

部署完成后，引导用户验证：

1. 后端 API 可访问
2. 前端页面可访问
3. 登录功能正常
4. 文件上传功能正常（如果用了 OSS）

## 注意事项

- 激活 `deployment-guide` 技能获取完整参考
- 所有密钥生成使用 `openssl rand` 命令
- `-local.yml` 文件已在 `.gitignore` 中，不会被提交
- 使用中文与用户交互
