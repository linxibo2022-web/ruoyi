
# 生产环境部署指南

## 🔴 部署前置流程（最高优先级，必须先走这步再动手）

**默认一律 Docker 部署**（项目自带 Dockerfile + docker-compose）。**除非用户明确说「用原生/JAR/systemd 部署」**，否则不要改方案。

开工前**必须**按「探环境 → 报告并给方案 → 等用户确认」三步走，**严禁探完直接开干**：

### 第 0 步 · 探测目标环境（只读，先全查一遍）
- 系统/架构、CPU、**内存 + swap**、磁盘可用空间
- 已装什么：**Docker / docker compose**、1Panel、MySQL/Redis/Nginx（查端口占用防冲突）
- 域名是否已解析到本机、80/443 公网是否可达（云厂商安全组）
- 本地构建环境：JDK 版本、Maven、Node/pnpm

### 第 1 步 · 把探测结果 + 部署方案讲给用户，停下等确认
一段话汇报现状 + 给出**默认 Docker 方案**，有取舍就给选项。示例：
> 「服务器：裸机 Ubuntu 22.04、**仅 1.6G 内存且无 swap**、未装 Docker。我的计划（默认 Docker）：装 Docker → compose 起 MySQL+Redis+后端+Nginx；因内存紧张会**加 2G swap + 限制容器内存 + JVM `-Xmx512m`**。域名已解析，80/443 请确认安全组已放行。**确认就按此 Docker 方案开始？还是你想用原生/JAR？**」

**必须拿到用户明确「确认 / 继续」才动手**；没回就停着等，不要自作主张往下做。

### 第 2 步 · 若挂了 Reeve MCP → 优先用 Reeve 部署（配方在 Reeve 侧，不在这里）
若当前会话挂了 **Reeve MCP 工具**（`list_servers` / `deploy_upsert_target` / `deploy_run` 等）→ **优先用 Reeve**（凭据托管、产物进工作台、可一键重部署/回滚），别手写 compose 裸 SSH。

> 🔴 **本框架的 Reeve 部署配方住在 Reeve 里，不在本指南**：Reeve 内置了针对 ruoyi-plus-uniapp 的部署技能（`ruoyi-plus-uniapp-deploy`），AI 部署前会自动 `evaluate_skills` 命中、`get_skill` 拿到完整配方照做（共享 MySQL/Redis + 部署目标 Target + host 网络接线 + 自动反代签证书 + 真实接口验收）。**你只需让 AI 走 Reeve、按它的引导确认即可，不用改本仓。**
>
> 本指南**第 2 步以下的手动 / 原生细节**，仅在「**没有 Reeve**」或「用户明确要原生」时参考。

### 部署方式选择（铁律）
- **默认 Docker**——裸机也是「装 Docker + compose 全栈」，**不是**改原生。
- **内存紧张 ≠ 弃 Docker**：正解 = 加 swap + compose `mem_limit` + JVM `-Xmx` + MySQL 关 `performance_schema` / 调小缓冲池，**仍跑 Docker**。
- **只有**用户明确要求原生/JAR，或环境确实禁容器（且用户同意）→ 才走「第四步-A JAR 部署」。
- **任何情况下都不要**自己默默从 Docker 切到原生。

---

## 概述

本指南覆盖 ruoyi-plus-uniapp 项目的完整部署流程，包含三种部署模式和四种前端构建目标：

> **默认 Docker**（含裸机：装 Docker 跑 compose）。下面 JAR 仅用于「用户明确要求原生」或「环境禁容器」且用户同意时——不要因为服务器内存小就擅自选它（低内存正解是 swap + `mem_limit` + 仍用 Docker，见上方前置流程）。

| 部署模式 | 适用场景 | 复杂度 |
|---------|---------|--------|
| **一键自动化部署** | 推荐！一条命令完成全部部署（已有 1Panel / Docker 环境时） | ⭐ |
| **Docker（含裸机从零）** | 默认方案：裸机装 Docker → compose 起全栈；低内存加 swap + mem_limit | ⭐⭐ |
| **JAR 部署** | **仅**用户明确要原生 / 禁容器环境（需用户同意） | ⭐⭐ |

---

## 一键自动化部署（推荐）

> 本地构建 → Docker 镜像打包 → SSH 传输 → 服务器加载启动 → 健康检查，全程无需手动 SSH。
> 适合：已有 1Panel 管理 MySQL/Redis/Nginx 的服务器。

### 前提条件

- 本地安装 Docker Desktop 且已启动
- 本地安装 Maven、pnpm
- `pip install paramiko`

### 快速开始

```bash
# 首次使用：创建配置文件（交互式引导）
python deploy.py --init

# 部署后端
python deploy.py backend

# 部署前端 PC
python deploy.py frontend

# 部署 H5 移动端（可选）
python deploy.py h5

# 部署后端 + 前端
python deploy.py all

# 部署后端 + 前端 + H5
python deploy.py full

# 交互式菜单
python deploy.py
```

### 部署流程详解

**后端部署**（~3-5 分钟）：
1. `mvn clean package -DskipTests` 编译 JAR
2. `docker build` 构建极简镜像（只含 JDK + JAR）
3. `docker save | gzip` 导出镜像（约 200MB）
4. paramiko SSH 传输到服务器（带进度显示）
5. 服务器 `docker load` + `docker compose up -d`
6. 自动健康检查 `/actuator/health`

**前端部署**（~1-2 分钟）：
1. `pnpm build` 构建静态文件
2. `tar czf` 打包 dist 目录
3. SSH 传输到服务器
4. 解压到 Nginx HTML 根目录

### 配置文件

配置保存在 `.deploy-config.json`（已在 `.gitignore` 中排除）：

```json
{
  "server": {
    "host": "192.168.1.100",
    "port": 22,
    "username": "root",
    "auth_type": "password",
    "password": "your-password",
    "key_file": ""
  },
  "backend": {
    "deploy_dir": "/opt/ryplus",
    "image_name": "ryplus_uni",
    "image_tag": "latest",
    "server_port": 5500,
    "compose_mode": "single"
  },
  "frontend": {
    "nginx_html_dir": "/docker/nginx/html",
    "pc_subdir": "",
    "h5_subdir": "h5"
  },
  "options": {
    "skip_maven_build": false,
    "skip_frontend_build": false,
    "health_check_timeout": 30
  }
}
```

**配置说明**：

| 字段 | 说明 |
|------|------|
| `server.auth_type` | `password`（密码）或 `key`（SSH 密钥） |
| `backend.compose_mode` | `single`（单实例）或 `cluster`（双实例负载均衡） |
| `frontend.pc_subdir` | PC 前端子目录，空串表示部署到 Nginx 根目录 |
| `options.skip_maven_build` | 跳过 Maven 构建（JAR 已构建好时使用） |
| `options.skip_frontend_build` | 跳过前端构建（dist 已构建好时使用） |

### 首次部署注意事项

1. 运行 `python deploy.py --init` 在服务器上创建目录和 compose 文件
2. SSH 登录服务器，修改 `docker-compose.yml` 中的数据库密码、Redis 密码等
3. 确保 MySQL 和 Redis 已通过 1Panel 安装并运行
4. 确保 Nginx 已配置好反向代理（参考第六步）
5. 运行 `python deploy.py all` 执行首次部署

### 安全设计

- 连接信息存储在本地 `.deploy-config.json`，不会提交到 Git
- 推荐使用内网 IP 连接（内网 IP 泄露无安全风险）
- 支持 SSH 密钥认证（高级用户）
- 服务器上无源码，只有 Docker 镜像和静态文件

### 文件清单

| 文件 | 说明 |
|------|------|
| `deploy.py` | 一键部署脚本（项目根目录） |
| `.deploy-config.json` | 部署配置（本地，不提交 Git） |
| `.deploy-config.example.json` | 配置示例（提交 Git） |

| 前端构建 | 输出 | 部署方式 |
|---------|------|---------|
| **PC 端** | `plus-ui/dist/` | Nginx 静态文件 |
| **H5 端** | `plus-uniapp/dist/build/h5/` | Nginx 静态文件 |
| **微信小程序** | `plus-uniapp/dist/build/mp-weixin/` | 微信开发者工具上传 |
| **APP** | APK/IPA/鸿蒙 | HBuilderX 云打包/本地打包 |

---

## 第一步：生产环境安全密钥检查（两种模式通用）

> **⚠️ 最高优先级！部署前必须完成密钥更换。**

### 1.1 需要更换的密钥清单

| 密钥 | 配置位置 | 默认值 | 影响范围 |
|------|---------|--------|---------|
| **JWT Secret** | `application.yml` → `sa-token.jwt-secret-key` | `uDkkASPQVN5iR4eN` | Token 签名验证 |
| **RSA 公钥** | 后端 `application.yml` + 所有前端 `env/.env` | 框架默认值 | API 请求/响应加密 |
| **RSA 私钥** | 后端 `application.yml` + 所有前端 `env/.env` | 框架默认值 | API 请求/响应加密 |
| **AES 加密密码** | `application.yml` → `mybatis-encryptor.password` | 空 | 数据库字段加密 |
| **OpenAPI Secret** | `application.yml` → `open-api.secret-key` | `q3XA19UeJExvCqynPOnyYUcr4zwOVCyi` | 开放平台密钥加密 |

### 1.2 JWT Secret 更换

```bash
# 生成 16 位随机字符串作为 JWT 密钥
openssl rand -base64 16 | tr -dc 'A-Za-z0-9' | head -c 16
```

**配置位置**（环境变量方式）：
- JAR 部署：设置环境变量 `JWT_SECRET_KEY=你的新密钥`
- Docker 部署：在 compose 文件中修改 `JWT_SECRET_KEY: 你的新密钥`

**配置位置**（直接修改）：
- `ruoyi-admin/src/main/resources/application.yml` 第 155 行：
  ```yaml
  sa-token:
    jwt-secret-key: ${JWT_SECRET_KEY:你的新密钥}
  ```

### 1.3 RSA 密钥对更换

RSA 密钥用于 API 请求/响应加密（`@ApiEncrypt`），前后端必须配对。

```bash
# 生成 RSA 密钥对（Java 代码方式，在项目中运行）
# 或使用 openssl：
openssl genrsa -out private.pem 512
openssl rsa -in private.pem -pubout -out public.pem
# 然后将 PEM 转为 Base64 单行格式
```

**需要同步修改的文件**（4-5 个）：

| 文件 | 字段 | 说明 |
|------|------|------|
| `plus-ui/env/.env` | `VITE_APP_RSA_PUBLIC_KEY` / `VITE_APP_RSA_PRIVATE_KEY` | PC 前端 |
| `plus-uniapp/env/.env` | `VITE_APP_RSA_PUBLIC_KEY` / `VITE_APP_RSA_PRIVATE_KEY` | 移动端 CLI |
| `plus-app/env/.env` | `VITE_APP_RSA_PUBLIC_KEY` / `VITE_APP_RSA_PRIVATE_KEY` | 原生 APP |
| Docker compose | `API_RESPONSE_PUBLIC_KEY` / `API_REQUEST_PRIVATE_KEY` | 后端容器 |
| `application.yml` | 通过环境变量覆盖（或直接修改 prod 配置） | 后端 JAR |

**注意**：
- 后端使用的 RSA 公钥/私钥 与前端使用的是**不同的密钥对**
- 后端：`API_RESPONSE_PUBLIC_KEY`（加密响应）+ `API_REQUEST_PRIVATE_KEY`（解密请求）
- 前端：`VITE_APP_RSA_PUBLIC_KEY`（加密请求）+ `VITE_APP_RSA_PRIVATE_KEY`（解密响应 + 本地存储加密）

### 1.4 AES 数据库字段加密密码

如果启用了 `@EncryptField` 数据库字段加密：

```bash
# 生成 16/24/32 位 AES 密钥
openssl rand -base64 24 | tr -dc 'A-Za-z0-9' | head -c 32
```

**配置位置**：
```yaml
# application.yml 第 201-206 行
mybatis-encryptor:
  enable: true
  algorithm: AES
  password: ${ENCRYPT_PASSWORD:你的AES密钥}
```

### 1.5 密钥更换检查清单

- [ ] JWT Secret 已更换（不是默认的 `uDkkASPQVN5iR4eN`）
- [ ] RSA 密钥对已更换（前后端配对）
- [ ] AES 加密密码已设置（如果使用字段加密）
- [ ] OpenAPI Secret 已更换（如果启用开放平台）
- [ ] 所有前端项目的 `.env` 文件已同步更新 RSA 密钥

---

## 第二步：生产环境配置检查

### 2.1 配置文件位置

| 文件 | 用途 |
|------|------|
| `ruoyi-admin/src/main/resources/application.yml` | 主配置（所有环境共用） |
| `ruoyi-admin/src/main/resources/application-prod.yml` | 生产环境覆盖配置 |

### 2.2 功能开关确认

部署前需要逐项确认以下开关：

| 功能 | 环境变量 | 生产默认值 | 建议 |
|------|---------|-----------|------|
| **SpringDoc API 文档** | `SPRINGDOC_ENABLED` | `false` | ⚠️ 生产必须关闭 |
| **SnailJob 定时任务** | `SNAIL_JOB_ENABLED` | `false` | 按需开启 |
| **监控中心** | `MONITOR_ENABLED` | `false` | 建议开启 |
| **P6Spy SQL 日志** | `P6SPY_ENABLED` | `false` | 生产必须关闭 |
| **RocketMQ** | `ROCKETMQ_ENABLED` | `false` | 按需开启 |
| **MQTT** | `MQTT_ENABLED` | `false` | IoT 场景开启 |
| **邮件服务** | `MAIL_ENABLED` | `false` | 按需开启 |
| **AI 功能** | `LANGCHAIN4J_ENABLED` | `false` | 按需开启 |
| **开放平台** | `OPEN_API_ENABLED` | `false` | 按需开启 |

### 2.3 必须配置的环境变量

以下环境变量在生产环境中**没有安全默认值**，必须显式提供：

```bash
# 数据库（必须）
DB_HOST=你的数据库地址
DB_PORT=3306
DB_NAME=ryplus_uni
DB_USERNAME=你的数据库用户名
DB_PASSWORD=你的数据库密码

# Redis（必须，密码无默认值）
REDIS_HOST=你的Redis地址
REDIS_PORT=6379
REDIS_PASSWORD=你的Redis密码

# JWT 密钥（必须更换）
JWT_SECRET_KEY=你的新JWT密钥

# 应用基础 URL（支付回调等需要）
APP_BASE_API=https://你的域名
```

---

## 第三步：后端构建

### 3.1 Maven 构建

```bash
# 在项目根目录执行
# 跳过测试构建（推荐生产使用）
mvn clean package -DskipTests

# 构建产物位置
# ruoyi-admin/target/ryplus_uni.jar
```

**构建前检查**：
- [ ] `application-prod.yml` 配置已检查
- [ ] 密钥已更换
- [ ] 数据库已创建并导入初始数据

### 3.2 构建产物

| 文件 | 位置 | 说明 |
|------|------|------|
| `ryplus_uni.jar` | `ruoyi-admin/target/` | Spring Boot 可执行 JAR |

---

## 第四步-A：JAR 部署模式

### 4.1 服务器准备

**系统要求**：
- JDK 21+（推荐 [BellSoft Liberica JDK](https://bell-sw.com/)）
- MySQL 8.0+
- Redis 7.0+
- Nginx（可选，反向代理）

### 4.2 上传和启动

```bash
# 1. 上传 JAR 和启动脚本到服务器
scp ruoyi-admin/target/ryplus_uni.jar user@server:/opt/ryplus/
scp script/bin/ry.sh user@server:/opt/ryplus/

# 2. SSH 登录服务器
ssh user@server

# 3. 设置环境变量（方式一：export）
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=127.0.0.1
export DB_PASSWORD=你的数据库密码
export REDIS_PASSWORD=你的Redis密码
export JWT_SECRET_KEY=你的JWT密钥
export SPRINGDOC_ENABLED=false

# 3. 设置环境变量（方式二：systemd 服务文件中配置）
# 参见下方 4.4 节

# 4. 启动
cd /opt/ryplus
chmod +x ry.sh
./ry.sh start

# 5. 查看日志
tail -f logs/ryplus_uni.jar.log

# 其他命令
./ry.sh stop      # 停止
./ry.sh restart   # 重启
./ry.sh status    # 状态
```

### 4.3 启动脚本参数

`ry.sh` 默认 JVM 参数：
```bash
-Xms512m -Xmx1024m           # 堆内存
-XX:MetaspaceSize=128m         # 元空间初始值
-XX:MaxMetaspaceSize=512m      # 元空间最大值
-XX:+HeapDumpOnOutOfMemoryError # OOM 时 dump 堆
-XX:+UseZGC                    # 使用 ZGC 垃圾收集器
```

**生产环境建议调整**：
```bash
# 4C8G 服务器建议
-Xms2g -Xmx4g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m

# 8C16G 服务器建议
-Xms4g -Xmx8g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=1g
```

### 4.4 Systemd 服务（推荐）

创建 `/etc/systemd/system/ryplus.service`：

```ini
[Unit]
Description=RyPlus Application
After=network.target mysql.service redis.service

[Service]
Type=forking
User=ryplus
Group=ryplus
WorkingDirectory=/opt/ryplus
ExecStart=/opt/ryplus/ry.sh start
ExecStop=/opt/ryplus/ry.sh stop
ExecReload=/opt/ryplus/ry.sh restart

# 环境变量
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=DB_HOST=127.0.0.1
Environment=DB_PASSWORD=你的数据库密码
Environment=REDIS_PASSWORD=你的Redis密码
Environment=JWT_SECRET_KEY=你的JWT密钥
Environment=SPRINGDOC_ENABLED=false

Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 启用和管理
systemctl daemon-reload
systemctl enable ryplus
systemctl start ryplus
systemctl status ryplus
journalctl -u ryplus -f   # 查看日志
```

---

## 第四步-B：Docker 部署模式

### 4.1 Docker 构建镜像

```bash
# 在项目根目录（先完成 Maven 构建）
mvn clean package -DskipTests

# 构建 Docker 镜像
cd ruoyi-admin
docker build -t ryplus_uni:5.6.1 .

# 验证镜像
docker images | grep ryplus_uni
```

**Dockerfile 关键参数**：
- 基础镜像：`bellsoft/liberica-openjdk-rocky:21.0.10-cds`
- 工作目录：`/ruoyi/server`
- 默认端口：`8080`（SERVER_PORT）、`28080`（SNAIL_PORT）、`5005`（DEBUG_PORT）
- GC 策略：ZGC

### 4.2 Docker Compose 配置

项目提供了完整的参考 compose 文件：

```
script/docker/compose/Complete-compose.yml    # 完整参考（含所有服务和详细注释）
```

**Complete-compose.yml 包含的服务**：

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| mysql | `mysql:8.0.42` | 3306 | 主数据库 |
| nginx-web | `nginx:1.23.4` | 80, 443 | 反向代理 |
| redis | `redis:7.2.8` | 6379 | 缓存 |
| minio | `minio/minio:RELEASE.2025-04-22T22-12-26Z` | 9000, 9001 | 对象存储 |
| ryplus_uni | `ryplus_uni:5.6.1` | 5500 | 应用实例 1 |
| ryplus_uni2 | `ryplus_uni:5.6.1` | 5501 | 应用实例 2 |
| ryplus_uni_monitor | `ryplus_uni:5.6.1` | 9090 | 监控中心 |
| snailjob | `opensnail/snail-job:1.4.0` | 8800, 17888 | 定时任务调度 |

### 4.3 生成本地 Compose 配置

> **核心原则**：不修改 `Complete-compose.yml` 原文件，而是创建 `*-local.yml` 用于实际部署。`*-local.yml` 文件已在 `.gitignore` 中排除。

```bash
# 复制模板为本地文件
cp script/docker/compose/Complete-compose.yml script/docker/compose/Complete-compose-local.yml
```

**在本地文件中修改以下内容**：

1. **数据库密码**：`MYSQL_ROOT_PASSWORD`、`DB_PASSWORD`
2. **Redis 密码**：`REDIS_PASSWORD`
3. **JWT 密钥**：`JWT_SECRET_KEY`
4. **RSA 密钥对**：`API_RESPONSE_PUBLIC_KEY`、`API_REQUEST_PRIVATE_KEY`
5. **MinIO 密码**：`MINIO_ROOT_PASSWORD`
6. **监控密码**：`MONITOR_PASSWORD`
7. **域名和 URL**：`APP_BASE_API`
8. **授权码**：`APP_LICENSE`、`APP_OFFLINE_LICENSE`
9. **按需开关**：`SNAIL_JOB_ENABLED`、`LANGCHAIN4J_ENABLED`、`OPEN_API_ENABLED` 等
10. **SpringDoc**：确认 `SPRINGDOC_ENABLED: false`

### 4.4 使用 1Panel 进行容器编排（推荐）

> **生产环境推荐使用 1Panel 进行容器编排管理**，而非命令行操作。

**1Panel 操作流程**：

1. **安装 1Panel**（如果尚未安装）：
   ```bash
   curl -sSL https://resource.fit2cloud.com/1panel/package/quick_start.sh -o quick_start.sh && bash quick_start.sh
   ```

2. **上传 compose 文件**：
   - 登录 1Panel 管理面板
   - 进入 **容器管理** → **编排**
   - 创建新的编排，粘贴 `Complete-compose-local.yml` 的内容

3. **配置和启动**：
   - 在 1Panel 中检查所有环境变量
   - 确认卷挂载路径存在
   - 点击 **部署** 启动所有服务

4. **监控和管理**：
   - 通过 1Panel 查看各容器日志
   - 通过 1Panel 重启/停止单个服务
   - 通过 1Panel 查看资源使用情况

### 4.5 IDEA Docker 构建（开发环境）

IDEA 中主要用于**构建镜像**，不用于生产编排：

1. 配置 Docker 连接（Settings → Docker → +）
2. 右键 `Dockerfile` → Build Image
3. 构建完成后将镜像推送到服务器或镜像仓库

---

## 第五步：前端构建与部署

### 5.1 PC 端构建（plus-ui）

```bash
cd plus-ui

# 安装依赖
pnpm install

# 构建生产版本
pnpm build

# 构建产物
# plus-ui/dist/
```

**生产环境配置** `plus-ui/env/.env.production`：
```bash
# 后端 API 代理路径（Nginx 反向代理到后端）
VITE_APP_BASE_API='/ryplus_uni'
# 监控中心路径
VITE_APP_MONITOR_ADMIN='/admin/applications'
# SnailJob 路径
VITE_APP_SNAIL_JOB_ADMIN='/snail-job'
```

**Nginx 部署 PC 端**：
```nginx
# 将 dist/ 目录上传到服务器
# /docker/nginx/html/admin/   → PC 管理后台
```

### 5.2 H5 端构建（plus-uniapp）

```bash
cd plus-uniapp

# 安装依赖
pnpm install

# 构建 H5 生产版本
pnpm build:h5

# 构建产物
# plus-uniapp/dist/build/h5/
```

**生产环境配置** `plus-uniapp/env/.env.production`：
```bash
# H5 使用完整 URL（不走 Nginx 代理）
VITE_APP_BASE_API='https://你的域名/ryplus_uni'
```

**Nginx 部署 H5 端**：
```nginx
# 将 dist/build/h5/ 上传到服务器
# /docker/nginx/html/h5/   → H5 移动端
```

### 5.3 微信小程序构建（plus-uniapp）

```bash
cd plus-uniapp

# 构建微信小程序
pnpm build:mp-weixin

# 构建产物
# plus-uniapp/dist/build/mp-weixin/
```

**发布流程**：

1. **打开微信开发者工具**
2. **导入项目**：选择 `plus-uniapp/dist/build/mp-weixin/` 目录
3. **填写 AppID**：使用你的小程序 AppID
4. **上传**：点击右上角「上传」按钮
5. **提交审核**：登录微信公众平台 → 版本管理 → 提交审核
6. **发布上线**：审核通过后发布

**小程序生产配置注意**：
- `plus-uniapp/env/.env.production` 中的 `VITE_APP_BASE_API` 必须是 HTTPS 域名
- 域名必须在微信公众平台「开发管理」→「开发设置」→「服务器域名」中配置
- request 合法域名添加：`https://你的域名`
- uploadFile 合法域名添加：`https://你的域名`
- downloadFile 合法域名添加：OSS 域名

### 5.4 APP 构建（plus-uniapp 或 plus-app）

#### 方式一：CLI 构建 APP（plus-uniapp，不需要原生插件）

```bash
cd plus-uniapp

# 构建 APP（需要 HBuilderX CLI 或 uni-app CLI）
pnpm build:app
```

#### 方式二：HBuilderX 构建 APP（plus-app，需要原生插件）

1. **使用 HBuilderX 打开** `plus-app/` 项目
2. **配置 manifest.json**：
   - 基础配置：应用名称、AppID、版本号
   - 模块配置：勾选需要的原生模块
   - 原生插件：配置 `nativeplugins/` 下的插件
3. **云打包**：
   - 发行 → 原生 App-云打包
   - 选择 Android/iOS 证书
   - 等待打包完成下载 APK/IPA
4. **本地打包**（高级）：
   - 需要 Android Studio / Xcode 环境
   - 发行 → 原生 App-本地打包

#### 鸿蒙 APP（plus-app）

```bash
cd plus-app
# 鸿蒙配置在 harmony-configs/ 目录
# 需要 DevEco Studio 开发环境
```

### 5.5 前端构建检查清单

- [ ] `.env.production` 中的 `VITE_APP_BASE_API` 已修改为生产域名
- [ ] RSA 密钥已更换（`.env` 文件中）
- [ ] PC 端 `dist/` 已上传到 Nginx 静态目录
- [ ] H5 端 `dist/build/h5/` 已上传到 Nginx 静态目录
- [ ] 小程序已通过微信开发者工具上传并提交审核
- [ ] 小程序域名白名单已在微信公众平台配置

---

## 第六步：Nginx 配置

### 6.1 反向代理配置参考

```nginx
# 后端负载均衡（两个实例）
upstream ryplus_server {
    server 127.0.0.1:5500 weight=1;
    server 127.0.0.1:5501 weight=1;
}

server {
    listen 80;
    server_name 你的域名;

    # PC 管理后台
    location / {
        root /docker/nginx/html/admin;
        try_files $uri $uri/ /index.html;
        index index.html;
    }

    # H5 移动端
    location /h5 {
        alias /docker/nginx/html/h5;
        try_files $uri $uri/ /h5/index.html;
        index index.html;
    }

    # 后端 API 反向代理
    location /ryplus_uni/ {
        proxy_pass http://ryplus_server/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 监控中心反向代理
    location /admin/ {
        proxy_pass http://127.0.0.1:9090/admin/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # SnailJob 反向代理
    location /snail-job/ {
        proxy_pass http://127.0.0.1:8800/snail-job/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 6.2 HTTPS 配置

```nginx
server {
    listen 443 ssl;
    server_name 你的域名;

    ssl_certificate /etc/nginx/cert/你的域名.pem;
    ssl_certificate_key /etc/nginx/cert/你的域名.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # ... 其余配置同上
}

# HTTP 自动跳转 HTTPS
server {
    listen 80;
    server_name 你的域名;
    return 301 https://$host$request_uri;
}
```

---

## 生产环境完整检查清单

### 安全检查

- [ ] JWT Secret 已更换
- [ ] RSA 密钥对已更换（前后端同步）
- [ ] AES 加密密钥已设置（如需要）
- [ ] 数据库密码已修改（非默认值）
- [ ] Redis 密码已设置
- [ ] MinIO 密码已修改
- [ ] 监控中心密码已修改
- [ ] SpringDoc API 文档已关闭（`SPRINGDOC_ENABLED=false`）

### 服务检查

- [ ] 数据库已创建并导入初始数据
- [ ] Redis 服务正常运行
- [ ] 后端服务启动成功
- [ ] PC 前端已构建并部署
- [ ] H5/小程序已构建（如需要）
- [ ] Nginx 反向代理配置正确
- [ ] HTTPS 证书已配置

### 功能开关检查

- [ ] SnailJob 定时任务：已确认是否开启
- [ ] 监控中心：已确认是否开启
- [ ] RocketMQ：已确认是否开启
- [ ] MQTT：已确认是否开启
- [ ] 邮件服务：已确认是否开启
- [ ] AI 功能：已确认是否开启
- [ ] 开放平台：已确认是否开启

### 小程序专项检查（如发布小程序）

- [ ] 微信公众平台服务器域名已配置
- [ ] request 合法域名已添加
- [ ] uploadFile 合法域名已添加
- [ ] 小程序 AppID 已正确配置
- [ ] 已通过微信开发者工具上传代码
- [ ] 已提交审核

---

## 常见问题

### Q1: JAR 启动后无法访问？

检查顺序：
1. `./ry.sh status` 确认进程在运行
2. 检查日志 `tail -f logs/ryplus_uni.jar.log`
3. 检查端口 `netstat -tlnp | grep 8080`
4. 检查防火墙 `firewall-cmd --list-ports`

### Q2: Docker compose 启动后应用连不上数据库？

- 所有服务使用 `network_mode: "host"`，直接用 `127.0.0.1` 连接
- 确认 MySQL 容器已启动完成：`docker logs mysql`
- 确认数据库已初始化：手动导入 `script/sql/ry_plus_new.sql`

### Q3: 小程序请求失败？

1. 检查域名是否已在微信公众平台配置
2. 确认 `.env.production` 中的 `VITE_APP_BASE_API` 是 HTTPS
3. 在微信开发者工具中查看 Network 请求详情

### Q4: 前后端 RSA 加密不匹配？

- 后端和前端使用的是**两对不同的 RSA 密钥**
- 前端用后端公钥加密请求，后端用对应私钥解密
- 后端用前端公钥加密响应，前端用对应私钥解密
- 确保 compose 中的 `API_RESPONSE_PUBLIC_KEY`/`API_REQUEST_PRIVATE_KEY` 与前端 `.env` 中的密钥配对

### Q5: SnailJob 需要额外初始化吗？

- SnailJob 需要独立的数据库，导入 `script/sql/ry_job.sql`
- 在 compose 中设置 `SNAIL_JOB_ENABLED: true`
- SnailJob 管理界面默认端口 `8800`

### Q6: 如何启用远程调试？

```yaml
# compose 中设置
DEBUG_ARGS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```
然后通过 SSH 隧道连接：
```bash
ssh -L 5005:127.0.0.1:5005 user@server
```
IDEA 中创建 Remote JVM Debug 配置，连接 `localhost:5005`。

---

## 文件位置速查

| 文件 | 路径 | 说明 |
|------|------|------|
| 主配置 | `ruoyi-admin/src/main/resources/application.yml` | 所有环境共用 |
| 生产配置 | `ruoyi-admin/src/main/resources/application-prod.yml` | 生产环境覆盖 |
| Dockerfile | `ruoyi-admin/Dockerfile` | Docker 镜像构建 |
| JAR 启动脚本 | `script/bin/ry.sh` | JAR 模式启停 |
| Compose 模板 | `script/docker/compose/Complete-compose.yml` | Docker 编排参考 |
| Nginx 配置参考 | `script/docker/nginx/conf/nginx.conf` | Nginx 配置模板 |
| MySQL 初始化 | `script/sql/ry_plus_new.sql` | 数据库初始化 |
| SnailJob 初始化 | `script/sql/ry_job.sql` | 定时任务数据库 |
| PC 前端生产配置 | `plus-ui/env/.env.production` | PC 构建配置 |
| 移动端生产配置 | `plus-uniapp/env/.env.production` | H5/小程序构建配置 |
| APP 生产配置 | `plus-app/env/.env.production` | APP 构建配置 |
| RSA 密钥（PC） | `plus-ui/env/.env` | PC 端 RSA 密钥 |
| RSA 密钥（移动端） | `plus-uniapp/env/.env` | 移动端 RSA 密钥 |
| RSA 密钥（APP） | `plus-app/env/.env` | APP 端 RSA 密钥 |
