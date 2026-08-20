# 部署文档

## 环境要求

| 组件 | 最低版本 | 推荐版本 |
|------|---------|---------|
| JDK | 17 | 21 |
| Maven | 3.8 | 3.9+ |
| Node.js | 18 | 20+ |
| pnpm | 8 | 10+ |
| MySQL | 5.7 | 8.0+ |
| Redis | 6.0 | 7.0+ |

## 数据库初始化

```bash
# 1. 创建数据库
mysql -uroot -p -e "CREATE DATABASE ry_plus_new CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入 SQL（按顺序）
mysql -uroot -p ry_plus_new < script/sql/ry_plus_new.sql
```

## 后端启动

### 开发环境

```bash
# 1. 安装依赖
mvn clean install -DskipTests

# 2. 配置 ruoyi-admin/src/main/resources/application-dev.yml
#    修改数据库/Redis 连接信息

# 3. 启动主类
mvn -pl ruoyi-admin spring-boot:run
# 或在 IDEA 中运行 RuoYiApplication
```

### 生产环境

```bash
# 1. 打包
mvn clean package -DskipTests -Pprod

# 2. 启动
java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=prod
```

## PC 前端启动

```bash
cd plus-ui
pnpm install

# 开发
pnpm dev

# 生产构建
pnpm build:prod
```

## 移动端启动（plus-uniapp）

```bash
cd plus-uniapp
pnpm install

# H5 开发
pnpm dev:h5

# 微信小程序开发（产物在 dist/dev/mp-weixin/，用微信开发者工具打开）
pnpm dev:mp-weixin

# 生产构建
pnpm build:h5
pnpm build:mp-weixin
```

## 移动端启动（plus-app）

```bash
cd plus-app
pnpm install
# 用 HBuilderX 打开本目录
# 选择「运行 → 运行到手机或模拟器 → 标准基座/自定义基座」
```

## 配置说明

### 后端配置

- `ruoyi-admin/src/main/resources/application.yml` 主配置
- `ruoyi-admin/src/main/resources/application-dev.yml` 开发环境
- `ruoyi-admin/src/main/resources/application-prod.yml` 生产环境

### 前端环境变量

- `plus-ui/env/.env.development` 开发
- `plus-ui/env/.env.production` 生产

### 移动端环境变量

- `plus-uniapp/env/.env.development`
- `plus-uniapp/env/.env.production`

## 安全密钥

⚠️ **生产环境必须更换以下密钥**：

| 密钥 | 位置 |
|------|------|
| JWT 密钥 | `application.yml` 中 `sa-token.jwt-secret-key` |
| RSA 密钥对 | `application.yml` 中 `rsa.private-key` / `rsa.public-key` |
| AES 密钥 | `application.yml` 中 `mybatis-encryptor.password` |

## 常见问题

详见项目内代码注释，或联系交付方。
