
# 多环境配置 指南

## 概述

本项目采用**分层配置**结构，后端用 Spring Boot `profile`，前端/移动端用 Vite `.env.*`。不同环境（dev/prod）通过启动命令或构建参数切换，**敏感值一律用 `${ENV:default}` 外部化**（密钥不落 Git）。

| 端 | 配置格式 | 切换方式 |
|----|---------|---------|
| 后端（ruoyi-admin） | `application.yml` + `application-{profile}.yml` | `--spring.profiles.active=prod` |
| 前端（plus-ui） | `env/.env` + `env/.env.development` + `env/.env.production` | `pnpm dev` / `pnpm build` |
| 移动端（plus-uniapp） | `env/.env` + `env/.env.development` + `env/.env.production` | Vite mode 自动匹配 |
| 移动端（plus-app） | `env/.env` + `env/.env.development` + `env/.env.production` | HBuilderX / `pnpm dev:app` |

---

## 后端：Spring Boot Profile

### 文件结构

```
ruoyi-admin/src/main/resources/
├── application.yml              # 通用配置（所有环境共享）
├── application-dev.yml          # 开发环境
└── application-prod.yml         # 生产环境
```

### `application.yml`（共享配置）

存放**所有环境都一样**或**用占位符外部化**的配置：

```yaml
app:
  id: ryplus_uni
  title: ryplus-uni后台管理
  license: ${APP_LICENSE:YOUR0LICENSE0KEY0HERE}
  version: ${revision}

tenant:
  enable: true

spring:
  profiles:
    active: @profiles.active@      # 由 Maven 打包注入
```

### `application-dev.yml`（开发专属）

```yaml
app:
  upload-path: ${APP_UPLOAD_PATH:D:\download\ruoyi\uploadPath}
  base-api: ${APP_BASE_API:http://127.0.0.1:5500}

spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/ry_plus_new?...
          username: root
          password: ${DB_PASSWORD:root}
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
```

### `application-prod.yml`（生产专属）

```yaml
app:
  upload-path: /data/uploadPath
  base-api: ${APP_BASE_API:https://api.yourdomain.com}

spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: ${DB_URL}
          username: ${DB_USER}
          password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
```

### 切换 profile

| 方式 | 命令 |
|------|------|
| IDEA 运行 | 添加 VM option：`-Dspring.profiles.active=prod` |
| jar 启动 | `java -jar ruoyi-admin.jar --spring.profiles.active=prod` |
| Docker | `ENV SPRING_PROFILES_ACTIVE=prod` |
| Maven 打包 | `mvn clean package -Pprod`（通过 `@profiles.active@` 占位） |

### 占位符规范

| 语法 | 说明 |
|------|------|
| `${ENV_VAR}` | 必填，未设置会启动失败 |
| `${ENV_VAR:default}` | 可选，未设置用 `default` |
| `${ENV_VAR:}` | 可选，未设置用空字符串 |
| `@profiles.active@` | Maven Resource 过滤注入（非 Spring 占位） |

---

## 前端（plus-ui）

### 文件结构

```
plus-ui/env/
├── .env                   # 共享配置
├── .env.development       # 开发环境
└── .env.production        # 生产环境
```

### `.env`（共享）

```bash
# 应用 ID（每个项目唯一，避免不同项目键产生冲突）
VITE_APP_ID = 'ryplus_uni'
VITE_APP_TITLE = 'ryplus-uni后台管理'
VITE_APP_CONTEXT_PATH = '/'

# 接口加密（前后端必须一致）
VITE_APP_API_ENCRYPT = 'true'
VITE_APP_RSA_PUBLIC_KEY = 'MFwwDQYJ...'
VITE_APP_RSA_PRIVATE_KEY = 'MIIBOw...'

# 推送方式开关
VITE_APP_WEBSOCKET = 'true'
VITE_APP_SSE = 'false'
```

### `.env.development`

```bash
VITE_APP_ENV='development'
VITE_APP_PORT='80'
VITE_APP_BASE_API='/dev-api'
VITE_APP_BASE_API_PORT='5500'
VITE_APP_MONITOR_ADMIN='http://127.0.0.1:9090/admin/applications'
```

### `.env.production`

```bash
VITE_APP_ENV='production'
VITE_APP_BASE_API='/ryplus_uni'          # nginx 反向代理前缀
VITE_APP_MONITOR_ADMIN='/admin/applications'
VITE_BUILD_COMPRESS='gzip'
```

### 启动命令

```bash
cd plus-ui
pnpm dev          # 使用 .env + .env.development，端口 80
pnpm build:prod   # 使用 .env + .env.production
pnpm build:dev    # 使用 .env + .env.development 打包
```

### 关键字段

| 字段 | 用途 | 必改 |
|------|------|------|
| `VITE_APP_ID` | 缓存 key 前缀，多项目必须不同 | ✅ 新项目 |
| `VITE_APP_BASE_API` | 前端请求前缀（配合 proxy） | ✅ 每个环境 |
| `VITE_APP_RSA_*_KEY` | 接口加密密钥 | 🔴 生产必换 |
| `VITE_APP_API_ENCRYPT` | 前后端加密开关，必须与后端一致 | ⚠️ |

---

## 移动端（plus-uniapp / plus-app 通用）

### 文件结构（两端相同）

```
plus-uniapp/env/              # 或 plus-app/env/
├── .env
├── .env.development
└── .env.production
```

### `.env.development`

```bash
# 变量必须以 VITE_ 为前缀才能暴露给外部读取
VITE_APP_ENV='development'
VITE_APP_BASE_API='https://api.ruoyikj.top/frp/5500'
VITE_APP_BASE_API_PORT='5500'
VITE_DELETE_CONSOLE=false
VITE_SHOW_SOURCEMAP=true
```

### `.env.production`

```bash
VITE_APP_ENV='production'
VITE_APP_BASE_API='https://api.yourdomain.com'
VITE_DELETE_CONSOLE=true
VITE_SHOW_SOURCEMAP=false
```

### 启动命令

| 平台 | 开发 | 生产构建 |
|------|------|---------|
| H5 | `pnpm dev:h5` | `pnpm build:h5` |
| 微信小程序 | `pnpm dev:mp-weixin` | `pnpm build:mp-weixin` |
| APP（CLI） | `pnpm dev:app` | `pnpm build:app` |
| APP（HBuilderX） | HBuilderX 菜单 → 运行 / 发行 | 同 |

### 使用方式（代码内）

```typescript
// 读取环境变量（类型为 string）
const apiBase = import.meta.env.VITE_APP_BASE_API
const isDev = import.meta.env.VITE_APP_ENV === 'development'

// ⚠️ 不要在运行时代码中直接用 process.env
```

---

## 标识符规划（多项目必读）

基于框架新建项目时，必须保证以下标识符**每个项目唯一**，否则多项目共存（同一浏览器/同一设备）会互相覆盖缓存：

| 层 | 字段 | 位置 |
|----|------|------|
| 后端 | `app.id` | `application.yml` |
| 前端 | `VITE_APP_ID` | `plus-ui/env/.env` |
| 移动端 | `VITE_APP_ID`（通常与前端同名） | `plus-uniapp/env/.env`、`plus-app/env/.env` |
| 后端端口 | `server.port` | `application.yml` |
| 前端端口 | `VITE_APP_PORT` | `plus-ui/env/.env.development` |

**参考**：使用 `project-init` 技能初始化新项目会自动处理标识符替换。

---

## 取值类型规范：环境变量值统一用字符串

环境变量（`.env` / `application.yml` 占位符值 / **docker compose `environment:`**）传到进程后**本质都是字符串**，源头就按字符串写、布尔也加引号，避免 YAML/解析器的隐式类型转换踩坑。

| 位置 | 写法 | 说明 |
|------|------|------|
| 前端/移动端 `.env` | `VITE_APP_API_ENCRYPT = 'true'` | 一律带引号（项目现状） |
| docker compose `environment:` | `P6SPY_ENABLED: "false"` | ✅ 布尔值**必须加引号**当字符串 |
| docker compose `environment:` | `P6SPY_ENABLED: false` | ❌ 裸布尔：YAML 解析成 boolean，旧版 compose 直接报 `must be a string`；且有「挪威问题」(`no`→false、`on/off/yes` 被强转) |

> ⚠️ **边界**：只对「环境变量值 / 本该是字符串的值」加引号。compose **自身的结构布尔字段**（`init: true`、`tty: false`、`privileged: false` 等）要保持**裸布尔、不加引号**——那些位置 compose 要的是真 boolean，加引号反而错。

---

## ✅ 正确做法

| 场景 | 做法 |
|------|------|
| 密码 / 密钥 / Token | 用 `${ENV_VAR}`，生产通过环境变量注入 |
| 本地开发调试值 | 写在 `.env.development` / `application-dev.yml` |
| 端口、URL、路径 | 按环境拆分到对应 profile / .env 文件 |
| 共享的不变配置 | 放 `application.yml` / `.env` |
| 前后端加密开关 | `VITE_APP_API_ENCRYPT` 与后端 `api-decrypt-enabled` 保持一致 |
| 新建项目 | 立即修改 `app.id` + `VITE_APP_ID` + 端口 |

## ❌ 常见错误

| 错误 | 后果 | 正确做法 |
|------|------|---------|
| 密钥写死在 `application.yml` | 泄露到 Git | 用 `${DB_PASSWORD}` 外部化 |
| 生产 RSA 密钥沿用框架默认值 | 接口加密被公开密钥破解 | 生产必须重新生成 |
| `VITE_APP_ID` 多项目重复 | localStorage 互相覆盖、登录串号 | 每项目独立 ID |
| `.env` 里改了但没生效 | Vite 只读**启动时**值 | 重启 dev server |
| 前端改 `VITE_APP_BASE_API` 后仍 CORS 报错 | proxy 未配 | 检查 `vite.config.ts` 的 proxy |
| 后端写 `profiles: active: dev` 硬编码 | 生产环境无法切换 | 用 `@profiles.active@` 由 Maven 注入 |
| 在 `.env.production` 留 `VITE_SHOW_SOURCEMAP=true` | sourcemap 泄露源码 | 生产必须 false |
| `.env` 文件提交包含真实生产密钥 | 密钥泄露 | 生产密钥只在部署机器设 |

---

## 常用排查

### 症状 1：改了配置没生效
- 前端：重启 `pnpm dev`（Vite 不监听 env 变化）
- 后端：IDEA 里重启应用；检查 profile 是否正确激活（启动日志搜 `The following profiles are active`）

### 症状 2：生产读取开发值
- 检查 Docker / 部署脚本是否传了 `SPRING_PROFILES_ACTIVE=prod`
- 前端：检查 `pnpm build` 用的是 `build:prod` 还是 `build:dev`

### 症状 3：接口 401 / 加密解密错误
- 前端 `VITE_APP_API_ENCRYPT` 与后端 `security.api-decrypt-enabled` 必须一致
- 前后端 RSA 公私钥必须成对（更换时两端同时换）

---

## 🔗 关联技能边界

| 场景 | 应使用技能 |
|------|-----------|
| 部署到生产服务器、Docker 编排、密钥轮换 | `deployment-guide` |
| 基于框架新建项目（包含标识符替换） | `project-init` |
| 接口加密开关、Sa-Token、RSA 用法 | `security-guard` |
| 文件上传目录（`app.upload-path`）、OSS 配置 | `file-oss-management` |
| 排查"为什么配置没生效"的 Bug | `bug-detective` |

**本技能专注**：「配置文件的结构、切换机制、字段含义」，不涉及部署流程与密钥生成细节。
