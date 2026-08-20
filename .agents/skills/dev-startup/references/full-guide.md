
# 本地开发启动指南 (dev-startup)

## 概述

本技能负责帮助用户**在本地从零把项目跑起来**：环境检查 → 基础工具安装 → 数据库初始化 → 启动后端 → 启动前端 / 移动端 → 健康验证。

**适用范围**：本地开发机首次启动、缺工具、装依赖、启动报错（启动相关）。
**不适用范围**：
- 生产部署 / Docker / Nginx → `deployment-guide`
- `application-*.yml` 字段含义、`.env` 详解 → `env-config`
- 启动成功后业务跑出 bug → `bug-detective`
- 数据库建表 / 字典 / 菜单 → `database-ops`

> **核心原则**：先检查、后安装、按依赖顺序、给明确下载地址、不让用户猜命令。

---

## 第 0 步：明确要启动什么（先问清楚）

项目分 4 端，**不一定全部都要装**。先确认用户要跑哪个：

| 端 | 目录 | 必须前置 | 启动方式 |
|---|---|---|---|
| **后端** | `ruoyi-admin/` | JDK 21 + Maven + 数据库 + Redis | IDEA / `mvn spring-boot:run` |
| **PC 前端** | `plus-ui/` | Node 22 + pnpm + 后端已起 | `pnpm dev`（默认端口 80） |
| **移动端 CLI**（H5/小程序/APP） | `plus-uniapp/` | Node 22 + pnpm + 后端已起 | `pnpm dev:h5` / `dev:mp-weixin` / `dev:app` |
| **移动端原生 APP**（HBuilderX 专用） | `plus-app/` | HBuilderX + 后端已起 | HBuilderX 运行 |

**最小启动组合**：
- 只调试后端接口 → 装后端栈即可
- 调试 PC 管理后台 → 后端 + PC 前端
- 调试小程序 → 后端 + 移动端 CLI + 微信开发者工具
- 调试原生/鸿蒙 APP → 后端 + plus-app + HBuilderX

---

## 第 1 步：环境检查（先看缺什么）

> ⚠️ **必须先跑这一步**，按检查结果决定要装什么，不要盲目安装。

### 1.1 一键检查脚本（Windows bash / Mac / Linux 通用）

```bash
echo "==== JDK ===="     && java -version 2>&1 | head -1
echo "==== Maven ===="   && mvn -v 2>&1 | head -1
echo "==== Node ===="    && node -v
echo "==== nvm ===="     && nvm version 2>&1 || nvm --version 2>&1
echo "==== pnpm ===="    && pnpm -v 2>&1
echo "==== Git ===="     && git --version
echo "==== MySQL ===="   && mysql --version 2>&1 || echo "未安装"
echo "==== Redis ===="   && redis-cli -v 2>&1 || echo "未安装"
```

### 1.2 期望版本

| 工具 | 最低版本 | 推荐版本 | 检查命令 |
|---|---|---|---|
| **JDK** | 21 | Liberica JDK 21 | `java -version` |
| **Maven** | 3.8 | 3.9.x | `mvn -v` |
| **Node** | 22.x | 22.x LTS | `node -v` |
| **nvm** | 任意 | nvm-windows 1.2.x / nvm 0.39+ | `nvm version` |
| **pnpm** | 10.x | 10.33+ | `pnpm -v` |
| **Git** | 2.x | 2.40+ | `git --version` |
| **MySQL** | 8.0 | 8.0.42+ | `mysql --version` |
| **Redis** | 7.0 | 7.2+ | `redis-cli -v` |

### 1.3 检查结果 → 决策

```
缺 JDK     → 第 2.1 节
缺 Maven   → 第 2.2 节
缺 Node    → 第 2.3 节（先装 nvm 再装 Node）
缺 pnpm    → 第 2.4 节
缺 数据库  → 第 2.5 节
缺 Redis   → 第 2.6 节
缺 IDE     → 第 2.7 节
全部齐全   → 跳到第 3 步
```

---

## 第 2 步：基础工具安装（缺什么装什么）

> **顺序很重要**：JDK → Maven → nvm → Node → pnpm → 数据库 → Redis → IDE。
> 后面的工具依赖前面的环境（如 pnpm 需要 Node，Maven 需要 JDK）。

### 2.1 JDK 21 安装

**推荐**：BellSoft Liberica JDK（项目 Dockerfile 用的就是这个）

**下载地址**：
- 官网：https://bell-sw.com/pages/downloads/
- 直接下载页：https://bell-sw.com/pages/downloads/?version=java-21-lts&package=jdk
- 备选 OpenJDK：https://jdk.java.net/21/

**安装后验证**：
```bash
java -version
# 期望输出：openjdk version "21.x.x" ...
javac -version
# 期望输出：javac 21.x.x
```

**Windows 配置 JAVA_HOME**：
1. 系统环境变量新增 `JAVA_HOME` = JDK 安装目录（如 `D:\software\dev\java\jdk21`）
2. `Path` 追加 `%JAVA_HOME%\bin`
3. 新开终端验证 `java -version`

### 2.2 Maven 3.8+ 安装

**下载地址**：https://maven.apache.org/download.cgi（选 Binary zip archive）

**安装步骤**：
1. 解压到无空格目录，如 `D:\software\dev\maven`
2. 配置 `MAVEN_HOME` 环境变量
3. `Path` 追加 `%MAVEN_HOME%\bin`
4. 验证：`mvn -v`

**强烈建议配置阿里云镜像**（否则首次构建会慢得离谱）：

编辑 `<MAVEN_HOME>/conf/settings.xml`，在 `<mirrors>` 里加：

```xml
<mirror>
  <id>aliyun-public</id>
  <name>aliyun public</name>
  <url>https://maven.aliyun.com/repository/public</url>
  <mirrorOf>central</mirrorOf>
</mirror>
<mirror>
  <id>aliyun-spring</id>
  <name>aliyun spring</name>
  <url>https://maven.aliyun.com/repository/spring</url>
  <mirrorOf>spring-milestones,spring-snapshots</mirrorOf>
</mirror>
```

本地仓库建议放 D 盘避免 C 盘膨胀：
```xml
<localRepository>D:/software/dev/maven/repository</localRepository>
```

### 2.3 Node 22（必须用 nvm 管理，不要直接装）

> 项目其它仓库可能用 Node 18 / 20，**必须用 nvm 切换**避免污染。

#### 2.3.1 装 nvm

**Windows（推荐 nvm-windows）**：
- 下载地址：https://github.com/coreybutler/nvm-windows/releases
- 选最新的 `nvm-setup.exe` 直接安装
- 安装后新开终端验证：`nvm version`

**Mac / Linux（用 nvm-sh）**：
- 官网：https://github.com/nvm-sh/nvm
- 一键安装：
  ```bash
  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
  ```
- 重新加载 shell 后验证：`nvm --version`

#### 2.3.2 用 nvm 装 Node 22

```bash
# 列出可用版本
nvm list available    # Windows
nvm ls-remote          # Mac/Linux

# 安装 Node 22 LTS
nvm install 22
nvm use 22

# 验证
node -v   # v22.x.x
npm -v
```

**Windows 注意**：装完 Node 后如果 `node -v` 报"不是内部命令"，可能是 nvm-windows 的 symlink 没生效，重启终端或电脑即可。

### 2.4 pnpm 10 安装

**前置**：Node 22 已就绪。

```bash
# 全局安装最新版（最简单）
npm install -g pnpm

# 或指定版本
npm install -g pnpm@10.33.0

# 验证
pnpm -v   # 10.x.x
```

**镜像加速**（强烈推荐，否则装包会卡 npmjs.org）：

```bash
pnpm config set registry https://registry.npmmirror.com
pnpm config get registry   # 验证
```

> 项目 `pnpm-lock.yaml` 已经是基于 npmmirror 的，使用淘宝源更顺滑。

### 2.5 数据库安装（任选其一）

> 项目支持 4 种数据库，**只需装一种**。开发推荐 MySQL 8。

#### 方案 A：MySQL 8（最常用）

- 下载地址：https://dev.mysql.com/downloads/mysql/
- 选 8.0.x（项目已验证 8.0.42）
- Windows 选 MSI Installer，Mac 用 `brew install mysql@8.0`
- 安装时**记住 root 密码**

**初始化**：
```sql
CREATE DATABASE ry_plus_uni DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

#### 方案 B：PostgreSQL

- 下载：https://www.postgresql.org/download/

#### 方案 C：Oracle

- 下载：https://www.oracle.com/database/technologies/xe-downloads.html

#### 方案 D：SQL Server

- 下载：https://www.microsoft.com/sql-server/sql-server-downloads

### 2.6 Redis 7 安装

**Windows**：
- 推荐用 Microsoft 维护的版本（虽然停在 5.x 但够用）：https://github.com/microsoftarchive/redis/releases
- 或用 Memurai（兼容 Redis 7）：https://www.memurai.com/get-memurai
- 或装 WSL2 + Redis（最接近生产）

**Mac**：
```bash
brew install redis
brew services start redis
```

**Linux**：
```bash
sudo apt install redis-server   # Debian/Ubuntu
sudo systemctl start redis
```

**验证**：
```bash
redis-cli ping   # 期望返回 PONG
```

> 默认无密码。若设了密码，记下来后面 `application-dev.yml` 要配。

### 2.7 IDE 与开发工具

| 工具 | 用途 | 下载地址 |
|---|---|---|
| **IDEA Ultimate** | 后端主 IDE（推荐） | https://www.jetbrains.com/idea/download/ |
| **VSCode** | 前端 IDE（备选） | https://code.visualstudio.com/ |
| **HBuilderX** | `plus-app` 必需（原生插件/真机/鸿蒙打包） | https://www.dcloud.io/hbuilderx.html |
| **微信开发者工具** | 小程序调试 | https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html |
| **Navicat / DBeaver** | 数据库 GUI | https://www.dbeaver.io/download/（DBeaver 免费） |
| **Redis 客户端** | Redis GUI | https://github.com/qishibo/AnotherRedisDesktopManager/releases |

---

## 第 3 步：数据库初始化

### 3.1 创建数据库

```sql
-- MySQL
CREATE DATABASE ry_plus_uni DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3.2 导入 SQL 脚本（按数据库类型选目录）

| 数据库 | SQL 目录 | 必导脚本 |
|---|---|---|
| MySQL | `script/sql/` | `ry_plus_sys.sql`、`ry_plus_app.sql`、`ry_plus_new.sql` |
| Oracle | `script/sql/oracle/` | `oracle_*.sql` 同名脚本 |
| PostgreSQL | `script/sql/postgres/` | `postgres_*.sql` |
| SQL Server | `script/sql/sqlserver/` | `sqlserver_*.sql` |

**导入顺序**：sys → app → new → job（如启用定时任务）

**MySQL 命令行导入**：
```bash
mysql -uroot -p ry_plus_uni < script/sql/ry_plus_sys.sql
mysql -uroot -p ry_plus_uni < script/sql/ry_plus_app.sql
mysql -uroot -p ry_plus_uni < script/sql/ry_plus_new.sql
```

### 3.3 修改 `application-dev.yml`

打开 `ruoyi-admin/src/main/resources/application-dev.yml`，确认 / 修改：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/ry_plus_uni?...
          username: root
          password: 你的MySQL密码
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: 你的Redis密码（无则留空）
```

> **只启用一个 datasource**！其他数据库类型要用 `#` 注释掉，否则启动会找全部驱动。

---

## 第 4 步：启动后端（3 种方式，推荐前两种）

### 方式 A：IDEA 运行（推荐 ★★★★★）

> **AI 边界**：AI 不能点 IDEA GUI 按钮，这一步必须由用户完成。

1. IDEA 打开项目根目录
2. 等 Maven 同步完成（右下角进度条）
3. 找到 `ruoyi-admin/src/main/java/plus/ruoyi/RuoyiPlus.java`
4. 点击类名左侧绿色三角，选 **Run 'RuoyiPlus.main()'**
5. 控制台看到 `Started RuoyiPlus in X.XX seconds` 即启动成功

### 方式 B：命令行 Maven（推荐 ★★★★）

> ⚠️ **真实踩坑**：直接 `mvn -pl ruoyi-admin -am spring-boot:run` 会报 `No plugin found for prefix 'spring-boot'`！必须分两步：

```bash
# 第 1 步：先把所有模块装到本地仓库（只需一次，之后改代码可省略）
mvn install -DskipTests

# 第 2 步：启动后端（不要加 -am）
mvn spring-boot:run -pl ruoyi-admin -DskipTests
```

**原因**：`-am` 会把 spring-boot:run 应用到所有依赖模块，但只有 `ruoyi-admin` 配了 spring-boot-maven-plugin。

### 方式 C：java -jar（验证打包用，不推荐日常开发）

```bash
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ryplus_uni.jar --spring.profiles.active=dev
```

### 启动成功标志

控制台出现：
```
[main] plus.ruoyi.RuoyiPlus  : Started RuoyiPlus in 7.xxx seconds (process running for 8.xxx)
```

后端默认监听 **5500 端口**（具体看 `application.yml` 的 `server.port`）。

---

## 第 5 步：启动 PC 前端（plus-ui）

### 5.1 安装依赖

```bash
cd plus-ui
pnpm install
```

### 5.2 安装失败处理（真实踩坑）

**症状**：`ERR_PNPM_META_FETCH_FAIL` / `ECONNRESET` / 卡在某个包很久

**原因**：npmmirror.com 偶发抖动，并发太高拉爆。

**解决**：降并发重试（不用删 node_modules，pnpm 会从缓存继续）：

```bash
pnpm install --network-concurrency 8
```

仍然失败时：
```bash
pnpm store prune       # 清损坏缓存
pnpm install --network-concurrency 4
```

### 5.3 启动 dev server

**方式 A：终端**
```bash
pnpm dev
```

**方式 B：IDEA / WebStorm 里点**
- 打开 `plus-ui/package.json`
- 找到 `"scripts"` 块的 `"dev": "vite serve --mode development"` 这一行
- 点行号左侧的**绿色三角**

### 5.4 启动成功标志

控制台出现：
```
VITE v6.x.x  ready in xxx ms
➜  Local:   http://localhost:80/
```

> 端口 80 被占用会自动顺延 81、82。浏览器访问控制台显示的实际地址。

---

## 第 6 步：启动移动端

### 6.1 plus-uniapp（H5 / 小程序 / CLI 构建 APP）

```bash
cd plus-uniapp
pnpm install                # 同样可能遇到镜像问题，处理方式同 5.2

# 选一种启动
pnpm dev:h5                 # H5（浏览器）
pnpm dev:mp-weixin          # 微信小程序（用微信开发者工具打开 dist/dev/mp-weixin）
pnpm dev:app                # APP（HBuilderX 打开项目运行到模拟器/真机）
```

**小程序调试**：
1. 跑 `pnpm dev:mp-weixin`
2. 打开微信开发者工具
3. **导入项目** → 选择 `plus-uniapp/dist/dev/mp-weixin` 目录
4. 填入测试 AppID（或用项目自己的）

### 6.2 plus-app（HBuilderX 专用，原生 APP / 鸿蒙）

> **AI 边界**：HBuilderX 是 GUI 工具，AI 无法操作。以下步骤必须由用户在 HBuilderX 里手动完成。

1. 打开 HBuilderX
2. **文件 → 打开目录** → 选 `plus-app/`
3. 顶部菜单 **运行**：
   - 运行到浏览器 → 选 Chrome
   - 运行到小程序模拟器 → 选微信开发者工具
   - 运行到手机或模拟器 → 选 Android / iOS
   - 运行到鸿蒙 → 选 HarmonyOS

---

## 第 7 步：健康检查（避免捏造端点）

> **真实踩坑**：AI 经常凭"感觉"编出 `/auth/getCaptchaConfig` 之类不存在的 URL。**必须用下方白名单**。

### 7.1 项目真实存在的健康端点

| 端点 | 说明 | 验证命令 |
|---|---|---|
| `GET /actuator/health` | Spring Boot 健康检查 | `curl --noproxy '*' http://localhost:5500/actuator/health` |
| `GET /auth/imgCode` | 图形验证码（开放） | `curl --noproxy '*' http://localhost:5500/auth/imgCode` |
| `POST /auth/userLogin` | 登录（需要 body） | 用前端登录验证 |

> 不在白名单内的接口路径，**必须先 grep Controller 文件**确认存在再调用，不要凭空捏造。

### 7.2 Windows 系统代理踩坑

**症状**：明明后端起来了，`curl http://localhost:5500/` 返回 502。

**原因**：Windows 配了系统代理（如公司 VPN / Clash），curl 默认走代理，到不了 localhost。

**解决**：加 `--noproxy '*'` 跳过代理：
```bash
curl --noproxy '*' http://localhost:5500/actuator/health
```

### 7.3 浏览器访问

- 后端 Swagger（如启用）：`http://localhost:5500/doc.html`
- PC 前端：`http://localhost:80/`（或 81、82）
- H5：`http://localhost:5173/`（或 vite 提示的端口）

---

## 第 8 步：端口约定与冲突处理

| 端 | 默认端口 | 配置位置 |
|---|---|---|
| 后端 | 5500 | `application.yml` 的 `server.port` |
| PC 前端 | 80 → 81 → 82（自动顺延） | `plus-ui/vite.config.ts` |
| 移动端 H5 | 5173 | `plus-uniapp/vite.config.ts` |
| MySQL | 3306 | 数据库配置 |
| Redis | 6379 | 数据库配置 |

### 端口被占用怎么办

```bash
# 查谁占了端口
netstat -ano | findstr :5500     # Windows
lsof -i :5500                     # Mac/Linux

# 优雅杀指定端口（推荐 ★★★★★）
npx kill-port 5500
```

### ⛔ 全局禁令：不要 `taskkill /IM node.exe`

```bash
# ❌ 绝对禁止：会杀掉 Claude Code CLI 自身的 Node 进程，会话立即终止！
taskkill /F /IM node.exe

# ✅ 正确：用 kill-port 或定位 PID 后精准杀
npx kill-port 5173
```

---

## AI 边界声明（重要）

| 操作 | AI 能不能做 |
|---|---|
| 跑 shell 命令、读日志、读配置 | ✅ 能 |
| 改 `application-dev.yml` 等文本配置 | ✅ 能 |
| 调 curl 验证健康端点 | ✅ 能 |
| **点 IDEA 的 Run 按钮** | ❌ 不能（GUI 操作） |
| **点 HBuilderX 的运行按钮** | ❌ 不能（GUI 操作） |
| **微信开发者工具导入项目** | ❌ 不能（GUI 操作） |
| **交互式登录**（`gcloud auth login` 等） | ❌ 不能（建议用户在终端 `! 命令` 自行执行） |
| 装 JDK / Node 等需要 sudo 或 GUI 的安装包 | ❌ 通常不能（要由用户跑安装程序） |

> **沟通原则**：用户说"用 IDEA 启动"时，AI 应直接说"GUI 操作我做不了，你点这里：xxx"，**不要**给一堆等价命令凑数。

---

## 常见错误速查（来自真实踩坑）

### 错误 1：`No plugin found for prefix 'spring-boot' in current project`

**触发**：`mvn -pl ruoyi-admin -am spring-boot:run`

**解决**：先 `mvn install -DskipTests`，再 `mvn spring-boot:run -pl ruoyi-admin`（**去掉 `-am`**）。

### 错误 2：`ERR_PNPM_META_FETCH_FAIL` / `ECONNRESET`

**触发**：`pnpm install`

**解决**：`pnpm install --network-concurrency 8`，仍失败降到 4，再不行清缓存 `pnpm store prune`。

### 错误 3：`curl localhost:xxx` 返回 502 / 拒绝连接

**触发**：Windows 配了系统代理时访问本地服务

**解决**：`curl --noproxy '*' http://localhost:xxx/...`

### 错误 4：`java: 非法字符: '\ufeff'`

**触发**：Java 源文件被某些工具改成了 UTF-8 with BOM

**解决**：见 CLAUDE.md「文件编码」章节，移除文件头 `EF BB BF` 三字节，统一改为 UTF-8 无 BOM。

### 错误 5：Redis 连不上 / Auth 失败

**触发**：`application-dev.yml` 里密码与 Redis 实际密码不一致

**解决**：
```bash
redis-cli
> CONFIG GET requirepass        # 看实际密码
> AUTH 你的密码                   # 验证
```
然后改 yml 里 `spring.data.redis.password`。

### 错误 6：MySQL 连接被拒 / `Access denied for user 'root'`

**触发**：`application-dev.yml` 里 username/password 错，或 MySQL 8 默认 root 只允许 `localhost`

**解决**：
```sql
-- 用 root 连上后授权
CREATE USER 'root'@'%' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

### 错误 7：捏造接口路径

**触发**：AI 凭印象写出 `/auth/getCaptchaConfig` 这类不存在的 URL

**预防**：调任何接口前先 grep Controller：
```bash
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping" ruoyi-modules/ruoyi-system/src/main/java/plus/ruoyi/system/auth/controller/
```

### 错误 8：Windows bash 里写 `> nul`

**触发**：`some-cmd > nul`

**结果**：Windows 把 `nul` 当文件名，会创建一个名为 `nul` 的文件。

**解决**：跨平台用 `> /dev/null 2>&1`（项目 bash 环境支持）。

---

## 完整启动检查清单

启动前：
- [ ] 已读 `application-dev.yml`，确认启用的数据库类型
- [ ] 数据库已创建并导入 SQL（sys / app / new）
- [ ] Redis 已启动并能 `redis-cli ping` 返回 PONG
- [ ] `java -version` / `node -v` / `pnpm -v` 全部满足版本要求

后端启动：
- [ ] `mvn install -DskipTests` 一次（首次或拉新代码后）
- [ ] IDEA 运行 `RuoyiPlus.main()` 或 `mvn spring-boot:run -pl ruoyi-admin`
- [ ] 控制台看到 `Started RuoyiPlus in X.XX seconds`
- [ ] `curl --noproxy '*' http://localhost:5500/actuator/health` 返回 `{"status":"UP"}`

前端启动：
- [ ] `cd plus-ui && pnpm install` 完成（失败用 `--network-concurrency 8`）
- [ ] `pnpm dev` 启动并打印 `Local: http://localhost:80/`
- [ ] 浏览器能打开登录页

移动端启动（按需）：
- [ ] `plus-uniapp`：`pnpm dev:h5` / `dev:mp-weixin` / `dev:app`
- [ ] `plus-app`：HBuilderX 打开项目并选运行目标

---

## 与其他技能的边界

| 场景 | 用哪个技能 |
|---|---|
| **本地从零跑起来**、装环境、首次启动 | **dev-startup**（本技能） |
| 启动成功后业务报错、空指针、接口 500 | `bug-detective` |
| `application-*.yml` / `.env` 字段含义 | `env-config` |
| 生产环境部署、Docker、Nginx | `deployment-guide` |
| 数据库建表、字典、菜单 SQL | `database-ops` |
| 前端 / 移动端组件用法 | `ui-pc` / `ui-mobile` |
| 想换个新机器搬整套环境 | 先 `dev-startup` 装工具 → `env-config` 配参数 |

---

## 快速参考

### 一键脑图

```
拉代码
  ↓
环境检查（第 1 步）
  ↓ 缺什么补什么（第 2 步）
JDK 21 → Maven → nvm → Node 22 → pnpm → MySQL → Redis → IDE
  ↓
建库 + 导 SQL + 改 yml（第 3 步）
  ↓
启动后端（第 4 步：IDEA 推荐 / mvn install + spring-boot:run）
  ↓ 后端 OK 后
启动 PC 前端（第 5 步：pnpm install + pnpm dev）
  ↓ 按需
启动移动端（第 6 步）
  ↓
健康检查（第 7 步：/actuator/health + /auth/imgCode）
  ↓
开发！
```

### 救命命令清单

```bash
# 后端启动失败
mvn install -DskipTests && mvn spring-boot:run -pl ruoyi-admin -DskipTests

# 前端 pnpm 装包失败
pnpm install --network-concurrency 8

# 端口被占
npx kill-port 5500

# Windows 系统代理穿透
curl --noproxy '*' http://localhost:5500/actuator/health

# 检查后端真实端点（防 AI 捏造）
grep -rn "@GetMapping\|@PostMapping" ruoyi-modules/ruoyi-system/src/main/java/plus/ruoyi/system/auth/controller/
```
