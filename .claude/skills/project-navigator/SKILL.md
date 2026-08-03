---
name: project-navigator
description: |
  当需要了解项目结构、查找文件、定位代码时自动使用此 Skill。提供项目结构导航和资源索引。

  触发场景：
  - 不知道文件在哪里
  - 想了解项目结构
  - 查找某个功能的代码位置
  - 了解模块职责
  - 查看已有的工具类、组件、API、Store
  - 寻找参考代码

  触发词：项目结构、文件在哪、目录、模块、代码位置、找、定位、结构、在哪里、哪个文件、参考、已有
---

# 项目导航指南

## ⚠️ 重要：项目上下文感知（必须首先判断）

### 核心原则

**在查找任何文件之前，必须先判断用户提问涉及的是哪个项目！**

```
用户提问 → 判断目标项目 → 使用对应项目的路径规则
```

### 项目识别方法

#### 1. 从用户提问中识别项目

| 线索类型 | 识别方法 | 示例 |
|---------|---------|------|
| **明确路径** | 用户给出的路径包含项目名 | `D:/other-project/src/...` |
| **项目名关键词** | 用户提到的项目名称 | 用户明确提到的其他项目名 |
| **技术栈特征** | 根据技术栈判断 | "ant-design-vue" → 非本项目 |
| **文件名特征** | 文件命名风格不同 | `.jsx` 文件 → 可能是 React 项目 |

#### 2. 从当前工作目录判断

```bash
# 检查当前工作目录
pwd

# 如果工作目录不是 ruoyi-plus-uniapp，则不能使用本技能的路径！
```

#### 3. 从用户上下文判断

| 上下文 | 判断 |
|-------|------|
| 用户刚切换到另一个项目目录 | 使用新项目的路径 |
| 用户提到"另一个项目" | 需要先确认项目结构 |
| 用户给出完整路径 | 直接使用用户路径 |

### 处理非本项目的策略

```
如果判断用户提问涉及【非 ruoyi-plus-uniapp 项目】：

1. ❌ 不要使用本技能中的固定路径
2. ✅ 先探索目标项目的实际结构
3. ✅ 使用 Glob/Grep 工具动态查找
4. ✅ 询问用户确认目标项目
```

**正确做法示例：**

```
用户："帮我找 D:/projects/my-vue-app 里的用户模块"

❌ 错误：直接用 ruoyi-plus-uniapp 的路径
   Read D:/projects/my-vue-app/ruoyi-modules/ruoyi-business/...  // 路径不存在！

✅ 正确：先探索目标项目结构
   Glob D:/projects/my-vue-app/**/*user*
   或
   ls D:/projects/my-vue-app/src/
```

---

## 动态路径推断

### 通用项目结构探索

当遇到**任何项目**时，按以下步骤探索：

#### 步骤 1：确定项目类型

```bash
# 检查项目根目录的标志文件
ls [项目根目录]

# 判断依据：
# - pom.xml / build.gradle     → Java 项目
# - package.json               → Node.js/前端项目
# - requirements.txt / setup.py → Python 项目
# - go.mod                     → Go 项目
# - Cargo.toml                 → Rust 项目
```

#### 步骤 2：识别项目框架

**Java 项目框架识别：**
```bash
# 检查 pom.xml 中的依赖
grep -i "spring-boot\|mybatis-plus\|ruoyi" pom.xml
```

| 特征 | 项目类型 |
|------|---------|
| `ruoyi-modules/ruoyi-business/` | 本项目 (ruoyi-plus-uniapp) |
| 其他 `ruoyi-*` 模块 | 其他项目（需动态探索） |
| 标准 Spring Boot 结构 | 自定义项目（需动态探索） |

**前端项目框架识别：**
```bash
# 检查 package.json
cat package.json | grep -i "vue\|react\|angular"
```

| 特征 | 框架 |
|------|------|
| `vue` + `element-plus` | Vue3 + EP |
| `vue` + `ant-design-vue` | Vue3 + Antd |
| `react` | React |
| `@dcloudio/uni-app` | UniApp |

#### 步骤 3：动态查找目标文件

```bash
# 查找特定功能的文件
Glob [项目路径]/**/*[关键词]*

# 查找特定内容
Grep "[关键词]" [项目路径]

# 查找特定类型文件
Glob [项目路径]/**/*.vue
Glob [项目路径]/**/*.java
```

---

## 本项目（ruoyi-plus-uniapp）结构

> **以下内容仅适用于 ruoyi-plus-uniapp 项目！**
> 如果用户提问涉及其他项目，请使用上述动态探索方法。

### 项目整体结构

```
ruoyi-plus-uniapp/
├── ruoyi-admin/                      # 后端启动入口
│   └── src/main/resources/
│       ├── application.yml           # 主配置
│       └── application-dev.yml       # 开发环境配置（数据库连接）
│
├── ruoyi-common/                     # 通用工具模块（20+个子模块）
│   ├── ruoyi-common-core/           # 核心工具（StringUtils, MapstructUtils）
│   ├── ruoyi-common-mybatis/        # MyBatis 扩展（DAO 基类, PageResult）
│   ├── ruoyi-common-tenant/         # 多租户（TenantEntity）
│   ├── ruoyi-common-redis/          # Redis 缓存
│   ├── ruoyi-common-satoken/        # 权限认证
│   ├── ruoyi-common-excel/          # Excel 导入导出
│   └── ...
│
├── ruoyi-modules/                    # 业务功能模块
│   ├── ruoyi-system/                # 系统管理模块
│   ├── ruoyi-generator/             # 代码生成器
│   └── ruoyi-business/              # ⭐ 自定义业务模块
│       └── src/main/java/plus/ruoyi/business/
│           ├── base/                # 基础业务（广告、绑定、平台、支付）
│           ├── mall/                # 商城业务（商品、SKU、订单）
│           ├── job/                 # 任务调度
│           └── api/                 # API 接口层
│               ├── app/             # 移动端 API
│               └── pc/              # PC 端 API
│
├── plus-ui/                         # 前端管理端 (PC Web)
│   └── src/
│       ├── api/                     # API 定义
│       ├── components/              # 公共组件
│       ├── stores/                  # Pinia 状态管理
│       ├── views/                   # 页面视图
│       └── types/                   # TypeScript 类型
│
├── plus-uniapp/                     # 移动端 (生产环境)
│   └── src/
│       ├── api/                     # API 定义
│       ├── pages/                   # 页面
│       ├── composables/             # 组合函数
│       ├── stores/                  # 状态管理
│       └── wd/                      # WD UI 组件
│
├── plus-app/                        # 移动端 APP 专用
├── plus-uniapp-demo/                # 移动端演示模板
│
├── script/sql/                      # 数据库脚本
│   ├── ry_plus_sys.sql             # 系统表
│   ├── ry_plus_app.sql             # 业务表
│   └── ry_plus_job.sql             # 任务表
│
└── docs/                            # 文档目录
```

---

## 后端模块位置

### 已有业务模块

| 模块 | 位置 | 表前缀 | 说明 |
|------|------|--------|------|
| **广告配置** (Ad) | `ruoyi-business/base/` | `b_` | ⭐ 参考模块 |
| **账号绑定** (Bind) | `ruoyi-business/base/` | `b_` | 多平台绑定 |
| **平台配置** (Platform) | `ruoyi-business/base/` | `b_` | 微信/支付宝配置 |
| **支付配置** (Payment) | `ruoyi-business/base/` | `b_` | 支付渠道配置 |
| **商品管理** (Goods) | `ruoyi-business/mall/` | `m_` | SPU 商品 |
| **商品SKU** (GoodsSku) | `ruoyi-business/mall/` | `m_` | SKU 规格 |
| **订单管理** (Order) | `ruoyi-business/mall/` | `m_` | 订单管理 |

### 代码结构（以广告模块为例）

```
ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/
├── controller/
│   └── AdController.java          # PC 端控制器
├── service/
│   ├── IAdService.java            # 服务接口
│   └── impl/
│       └── AdServiceImpl.java     # 服务实现（不继承基类）
├── dao/
│   ├── IAdDao.java                # DAO 接口
│   └── impl/
│       └── AdDaoImpl.java         # DAO 实现（buildQueryWrapper）
├── mapper/
│   └── AdMapper.java              # Mapper 接口
└── domain/
    ├── Ad.java                    # 实体类（继承 TenantEntity）
    ├── bo/
    │   └── AdBo.java              # 业务对象（@AutoMappers）
    └── vo/
        └── AdVo.java              # 视图对象
```

### 核心工具类位置

| 工具类 | 位置 | 说明 |
|--------|------|------|
| `MapstructUtils` | `ruoyi-common-core` | 对象转换（必须使用） |
| `StringUtils` | `ruoyi-common-core` | 字符串工具 |
| `DateUtils` | `ruoyi-common-core` | 日期工具 |
| `ServiceException` | `ruoyi-common-core` | 业务异常 |
| `TenantEntity` | `ruoyi-common-tenant` | 租户实体基类 |
| `IBaseDao` | `ruoyi-common-mybatis` | DAO 基类接口 |
| `BaseDaoImpl` | `ruoyi-common-mybatis` | DAO 基类实现 |
| `PlusLambdaQuery` | `ruoyi-common-mybatis` | 查询构建器 |
| `PlusLambdaUpdate` | `ruoyi-common-mybatis` | 更新构建器 |
| `PageQuery` | `ruoyi-common-mybatis` | 分页查询参数 |
| `PageResult` | `ruoyi-common-mybatis` | 分页结果 |

---

## 前端资源位置 (plus-ui)

### 目录结构

```
plus-ui/src/
├── api/                           # API 定义
│   ├── business/                  # 业务 API
│   │   ├── base/                  # base 模块
│   │   │   └── ad/
│   │   │       ├── adApi.ts       # API 方法
│   │   │       └── adTypes.ts     # 类型定义
│   │   └── mall/                  # mall 模块
│   └── system/                    # 系统 API
├── components/                    # 公共组件
├── stores/                        # Pinia Store
│   └── modules/
│       ├── user.ts               # 用户状态
│       ├── dict.ts               # 字典状态
│       ├── feature.ts            # 功能开关
│       ├── permission.ts         # 权限路由
│       ├── notice.ts             # 通知消息
│       └── aiChat.ts             # AI 聊天
├── views/                        # 页面视图
│   ├── business/                 # 业务页面
│   └── system/                   # 系统页面
└── types/
    └── icons.d.ts                # 图标类型（817个图标）
```

### 已有 Store

| Store | 文件 | 说明 |
|-------|------|------|
| user | `stores/modules/user.ts` | 用户认证、权限 |
| dict | `stores/modules/dict.ts` | 字典数据 |
| feature | `stores/modules/feature.ts` | 功能开关 |
| permission | `stores/modules/permission.ts` | 路由权限 |
| notice | `stores/modules/notice.ts` | 通知消息 |
| aiChat | `stores/modules/aiChat.ts` | AI 聊天 |

---

## 移动端资源位置 (plus-uniapp)

### 目录结构

```
plus-uniapp/src/
├── api/                           # API 定义
│   ├── app/                       # 应用 API
│   │   ├── home/                  # 首页
│   │   ├── wxShare/               # 微信分享
│   │   └── subscribe/             # 消息订阅
│   └── system/                    # 系统 API
│       ├── auth/                  # 认证
│       │   └── authApi.ts
│       ├── core/user/             # 用户
│       └── dict/                  # 字典
├── pages/                         # 页面
│   ├── index/                     # 首页
│   ├── login/                     # 登录
│   └── mine/                      # 我的
├── composables/                   # ⭐ 组合函数
│   ├── useAuth.ts                 # 认证
│   ├── useToken.ts                # Token
│   ├── useDict.ts                 # 字典
│   ├── useWebSocket.ts            # WebSocket
│   ├── useI18n.ts                 # 国际化
│   ├── useTheme.ts                # 主题
│   ├── useShare.ts                # 分享
│   ├── useWxShare.ts              # 微信分享
│   ├── usePayment.ts              # 支付
│   ├── useSubscribe.ts            # 订阅
│   ├── useAppInit.ts              # 初始化
│   ├── useScroll.ts               # 滚动
│   ├── useEventBus.ts             # 事件总线
│   └── useHttp.ts                 # HTTP
├── stores/                        # 状态管理
│   └── modules/
│       ├── user.ts                # 用户
│       ├── dict.ts                # 字典
│       ├── feature.ts             # 功能开关
│       └── tabbar.ts              # 导航栏
└── wd/                            # WD UI 组件
    └── components/
        └── wd-icon/
            └── wd-icon.vue        # 图标定义（24-420行）
```

### 已有 Composables

| 组合函数 | 说明 | 关键方法 |
|---------|------|---------|
| `useAuth` | 认证逻辑 | login, logout |
| `useToken` | Token 管理 | getToken, setToken |
| `useDict` | 字典数据 | 返回响应式字典数组 |
| `useWebSocket` | WebSocket | connect, send |
| `useI18n` | 国际化 | t |
| `useTheme` | 主题切换 | toggleTheme |
| `useShare` | 通用分享 | share |
| `useWxShare` | 微信分享 | wxShare |
| `usePayment` | 支付功能 | pay |
| `useSubscribe` | 消息订阅 | subscribe |
| `useAppInit` | 应用初始化 | init |
| `useScroll` | 滚动处理 | scrollTo |
| `useEventBus` | 事件总线 | emit, on |
| `useHttp` | HTTP 请求 | get, post |

### 已有 Store

| Store | 说明 | 关键方法 |
|-------|------|---------|
| user | 用户认证 | loginWithPassword, loginWithMiniapp, fetchUserInfo, logoutUser |
| dict | 字典数据 | loadDict, getDictLabel |
| feature | 功能开关 | loadFeatures, isEnabled |
| tabbar | 底部导航 | setActive, setList |

---

## 配置文件位置

| 配置 | 位置 | 说明 |
|------|------|------|
| 后端主配置 | `ruoyi-admin/src/main/resources/application.yml` | |
| 后端开发配置 | `ruoyi-admin/src/main/resources/application-dev.yml` | 数据库连接 |
| 前端环境配置 | `plus-ui/.env.*` | |
| 移动端环境配置 | `plus-uniapp/env/.env.*` | |

---

## 数据库脚本位置

| 脚本 | 位置 | 说明 |
|------|------|------|
| 系统表 | `script/sql/ry_plus_sys.sql` | 用户、角色、菜单等 |
| 业务表 | `script/sql/ry_plus_app.sql` | 广告、绑定等业务表 |
| 任务表 | `script/sql/ry_plus_job.sql` | 定时任务 |
| 新业务表 | `script/sql/ry_plus_new.sql` | 新增业务表 |

---

## 快速查找

### 我想找...（仅限本项目）

| 需求 | 位置 |
|------|------|
| 参考后端代码 | `ruoyi-business/base/` 的广告模块 |
| 看 Entity 怎么写 | `ruoyi-business/base/domain/Ad.java` |
| 看 Service 怎么写 | `ruoyi-business/base/service/impl/AdServiceImpl.java` |
| 看 DAO 怎么写 | `ruoyi-business/base/dao/impl/AdDaoImpl.java` |
| 前端 API 怎么写 | `plus-ui/src/api/business/base/ad/` |
| 移动端 API 怎么写 | `plus-uniapp/src/api/` |
| PC 端图标列表 | `plus-ui/src/types/icons.d.ts` |
| 移动端图标列表 | `plus-uniapp/src/wd/components/wd-icon/wd-icon.vue` (24-420行) |
| 数据库表结构 | `script/sql/ry_plus_app.sql` |
| 工具类 | `ruoyi-common/ruoyi-common-core/` |
| DAO 基类 | `ruoyi-common/ruoyi-common-mybatis/` |

---

## 移动端目录说明

| 目录 | 用途 | 运行方式 |
|------|------|----------|
| **plus-uniapp** | 生产环境 | HBuilderX 运行 |
| **plus-app** | APP 专用 | HBuilderX 打包 |
| **plus-uniapp-demo** | 开发演示 | `pnpm run dev:h5/mp-weixin/app` |

---

## 本项目识别特征

> 用于确认当前操作的是否为 ruoyi-plus-uniapp 项目

### 本项目独有特征

| 特征 | 说明 |
|------|------|
| `ruoyi-modules/ruoyi-business/` | 业务模块目录 |
| `plus-ui/` | PC 前端目录（非 ruoyi-ui） |
| `plus-uniapp/` | 移动端目录 |
| 四层架构（Controller → Service → DAO → Mapper） | 有独立的 DAO 层 |
| `plus.ruoyi.*` 包名 | 不是 `com.ruoyi.*` |

### 前端项目对比

| 类型 | 目录特征 | 文件类型 |
|------|---------|---------|
| Vue 3 + EP | `views/`, `components/` | `.vue` |
| Vue 3 + Antd | `pages/`, `components/` | `.vue` |
| React | `pages/`, `components/` | `.jsx/.tsx` |
| UniApp | `pages/`, `wd/` | `.vue` + `pages.json` |
| 微信小程序原生 | `pages/`, `components/` | `.wxml/.wxss/.js` |

---

## 检查清单

### 文件查找前检查

- [ ] **用户提到的路径是否在本项目内？**
  - 是 → 使用本技能的路径映射
  - 否 → 使用动态探索方法

- [ ] **用户的工作目录是什么？**
  - 本项目 → 可以使用相对路径
  - 其他项目 → 必须使用用户指定的路径

- [ ] **用户提到的技术栈是否与本项目一致？**
  - 一致 → 继续
  - 不一致 → 可能是其他项目，需要确认

### 动态查找命令

```bash
# 查找 Java 类
Glob [项目路径]/**/*[类名]*.java

# 查找 Vue 组件
Glob [项目路径]/**/*[组件名]*.vue

# 查找包含特定内容的文件
Grep "[关键词]" [项目路径] --include="*.java"

# 查找 API 定义
Glob [项目路径]/**/api/**/*Api.ts

# 查找配置文件
Glob [项目路径]/**/application*.yml
```
