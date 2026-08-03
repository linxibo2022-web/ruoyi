---
name: app-adapter
description: |
  当需要为 plus-app（原生 APP 项目）开发页面、组件、API 时自动使用此 Skill。提供 plus-app 与 plus-uniapp 的差异适配。

  触发场景：
  - 用户明确提到 plus-app 或 APP 端开发
  - 需要使用原生插件（nativeplugins）
  - 需要 HBuilderX 构建或真机调试
  - 需要鸿蒙 APP 适配
  - 需要 APP 专属配置（地图、客服、分享域名）

  触发词：plus-app、APP端、原生APP、原生插件、HBuilderX、鸿蒙APP、harmony、nativeplugins、APP打包、真机调试、APP专属、APP配置
---

# APP 端适配指南（plus-app）

## 概述

本项目有两个移动端项目，代码规范 80% 相同，但目录结构和平台能力存在差异：

| 项目 | 用途 | 构建方式 | 目录结构 |
|------|------|---------|---------|
| **plus-uniapp** | 小程序 / H5 / 不需原生插件的 APP | Vite CLI | `src/` 下组织 |
| **plus-app** | 需要原生插件的 APP / 鸿蒙 APP | HBuilderX | 扁平化（无 `src/`） |

**本技能仅提供差异内容**，通用规范（组件库、API 格式、样式规范）请参考 `ui-mobile`、`store-mobile` 等技能。

---

## 核心差异：目录结构映射

> **最重要的差异**：plus-app 没有 `src/` 目录层级，所有代码直接在项目根目录下。

| 代码类型 | plus-uniapp 路径 | plus-app 路径 |
|---------|-----------------|--------------|
| API 定义 | `plus-uniapp/src/api/` | `plus-app/api/` |
| 页面 | `plus-uniapp/src/pages/` | `plus-app/pages/` |
| 分包页面 | `plus-uniapp/src/pages-sub/` | `plus-app/pages-sub/` |
| 组件 | `plus-uniapp/src/components/` | `plus-app/components/` |
| Composables | `plus-uniapp/src/composables/` | `plus-app/composables/` |
| Store | `plus-uniapp/src/stores/` | `plus-app/stores/` |
| 工具函数 | `plus-uniapp/src/utils/` | `plus-app/utils/` |
| WD UI | `plus-uniapp/src/wd/` | `plus-app/wd/` |
| 布局 | `plus-uniapp/src/layouts/` | `plus-app/layouts/` |
| 国际化 | `plus-uniapp/src/locales/` | `plus-app/locales/` |
| 静态资源 | `plus-uniapp/src/static/` | `plus-app/static/` |
| 样式 | `plus-uniapp/src/style/` | `plus-app/style/` |
| 类型定义 | `plus-uniapp/src/types/` | `plus-app/types/` |
| 入口文件 | `plus-uniapp/src/App.vue` | `plus-app/App.vue` |
| 主入口 | `plus-uniapp/src/main.ts` | `plus-app/main.ts` |
| 环境变量 | `plus-uniapp/env/` | `plus-app/env/` |

### `@/` 别名差异

| 项目 | `@/` 指向 |
|------|----------|
| plus-uniapp | `plus-uniapp/src/` |
| plus-app | `plus-app/`（项目根目录） |

代码中的 `@/` 引用无需修改，因为两端的 Vite 配置已分别设置了正确的 alias。

---

## 参考代码位置（plus-app）

| 开发类型 | plus-app 参考文件 |
|---------|-----------------|
| **列表页** | `plus-app/components/tabbar/Home.vue` |
| **登录/表单页** | `plus-app/pages/auth/login.vue` |
| **API 定义** | `plus-app/api/app/home/homeApi.ts` |
| **类型定义** | `plus-app/api/app/home/homeTypes.ts` |
| **Composable** | `plus-app/composables/useHttp.ts` |
| **Store** | `plus-app/stores/modules/user.ts` |

---

## plus-app 独有能力

### 1. 原生插件（nativeplugins）

```
plus-app/nativeplugins/
└── sand-plugin-wifi/           # WiFi 原生插件示例
    ├── android/
    │   └── sand-plugin-wifi-release.aar
    ├── ios/
    │   └── sand_plugin_wifi.framework/
    └── package.json
```

**使用原生插件**：在 `manifest.json` 的 `app-plus.nativePlugins` 中配置，HBuilderX 打包时自动集成。

### 2. 鸿蒙配置（harmony-configs）

```
plus-app/harmony-configs/       # 鸿蒙系统专属配置（预留）
```

### 3. APP 专属系统配置（systemConfig.ts）

plus-app 的 `systemConfig.ts` 比 plus-uniapp 多了 `platforms` 配置：

```typescript
// plus-app/systemConfig.ts
export const SystemConfig = deepFreeze({
  app: { id, title, env },
  api: { baseUrl },
  security: { apiEncrypt, rsaPublicKey, rsaPrivateKey },
  // plus-app 独有
  platforms: {
    mapKey: '...',         // 地图服务密钥
    serviceUrl: '...',     // 在线客服链接
    shareDomain: '...',    // 分享页面域名
    analyticsId: '...',    // 数据统计服务 ID
  },
  features: { websocket: true },
})
```

### 4. APP 图标资源

```
plus-app/static/app/icons/
├── android/    # 72x72 ~ 192x192（4 种尺寸）
└── ios/        # 20x20 ~ 1024x1024（17 种尺寸）
```

### 5. manifest.json（静态配置）

plus-app 使用**静态** `manifest.json`（非 `.config.ts`），包含：
- APP 权限声明（CAMERA、WIFI_STATE 等 16 个权限）
- 原生插件配置
- iOS/Android 图标配置
- 启动页配置

### 6. 页面配置差异

| 配置文件 | plus-uniapp | plus-app |
|---------|------------|----------|
| 页面配置 | `pages.config.mts` | `pages.config.ts` |
| Manifest | `manifest.config.ts`（动态） | `manifest.json`（静态） |
| APP 特殊 | 无 | `'app-plus': { 'bounce': 'none' }` |

### 7. nvue 原生渲染（plus-app 性能场景）

nvue 使用 weex 引擎做**原生渲染**（非 webview），适合长列表/复杂滚动/原生地图/Canvas 动画等性能敏感场景。**仅 APP-PLUS 平台生效**，H5/小程序下不会被解析。

#### 框架已为 plus-app 默认支持 nvue（无需任何配置）

| 已配置项 | 位置 | 作用 |
|---------|------|------|
| `nvueCompiler: "uni-app"` | `plus-app/manifest.json` → `app-plus` | 组件编译器，模板用 `<view>` 而非 weex `<div>` |
| `nvueStyleCompiler: "uni-app"` | 同上 | 样式编译器，CSS 兼容写法 |
| `nvueLaunchMode: "fast"` | 同上 | nvue 页面快速启动 |
| `extensions: ['vue', 'nvue']` | `plus-app/vite/plugins/uni-pages.ts` | 让自动路由扫描 `.nvue` |

**使用者只需建 `.nvue` 文件即可**，不需要改任何配置。框架已统一处理。

#### 文件命名规范（强制）

按 CLAUDE.md 的页面命名规则：**用业务语义名，禁用通用占位词**。

| ❌ 禁止 | ✅ 正确（业务语义） |
|--------|-------------------|
| `pages/demo/native.nvue` | `pages/login/login.nvue` |
| `pages/list/list.nvue` | `pages/goods/goodsList.nvue` |
| `pages/index/index.nvue`（业务页面） | `pages/order/orderDetail.nvue` |
| `pages/test/test.nvue` | `pages/scan/scanCode.nvue` |

`pages.json` 自动生成时按文件路径去后缀，路径直接就是业务语义：

```json
{ "path": "pages/goods/goodsList" }
```

#### 混合策略（最推荐）：vue + nvue 同名共存

同一目录建同名的 `.vue` + `.nvue`，编译器按平台自动选：

```
plus-app/pages/goods/
├── goodsList.vue    ← H5 / 小程序使用（可用 wd-ui）
└── goodsList.nvue   ← APP 端优先使用（原生渲染，性能好）
```

`pages.json` 只注册一条 `path: "pages/goods/goodsList"`，编译器按平台分发。这是兼顾"多端兼容"和"APP 性能"的最优解。

#### nvue 文件示例（业务页面）

```vue
<!-- 商品长列表（APP 端原生渲染）：plus-app/pages/goods/goodsList.nvue -->
<template>
  <view class="container">
    <text class="title">商品列表</text>
  </view>
</template>

<script setup lang="ts">
</script>

<style>
.container { flex: 1; background-color: #f5f5f5; padding: 32rpx; }
.title { font-size: 36rpx; color: #333; }
</style>
```

#### nvue 页面跑不出来时按顺序排查

1. 运行命令是 `pnpm dev:app` 或 HBuilderX 真机/模拟器吗？（H5/小程序看不到 nvue）
2. 删除 `plus-app/node_modules/.vite/` 和自动生成的 `pages.json`，重新构建
3. 检查文件命名是否符合业务语义规范（不是 `list`、`test`、`demo` 等占位名）
4. 检查 nvue 内是否用了不支持的写法（见 `uniapp-platform` 的硬性约束表）

> **详细的 nvue 硬性限制**（仅 flex 布局、文字必须 `<text>`、wd-ui 不可用等）参见 `uniapp-platform` 技能的"nvue 原生渲染"章节。

---

## 构建与调试

### 开发运行

| 方式 | plus-uniapp | plus-app |
|------|------------|----------|
| H5 | `pnpm dev:h5` | HBuilderX → 运行到浏览器 |
| 小程序 | `pnpm dev:mp-weixin` | 不适用 |
| APP | `pnpm dev:app` | HBuilderX → 运行到真机/模拟器 |

### 打包发布

| 方式 | plus-uniapp | plus-app |
|------|------------|----------|
| H5 | `pnpm build:h5` | 不适用 |
| 小程序 | `pnpm build:mp-weixin` | 不适用 |
| APP | 不常用 | HBuilderX → 发行 → 原生 APP-云打包 |

### 重要提示

- plus-app **不支持命令行构建 APP**，必须使用 HBuilderX
- 原生插件只在 HBuilderX 打包时生效，`pnpm dev` 无法测试原生能力
- 真机调试需要 USB 连接或同一局域网

---

## 条件编译注意

plus-app 主要关注 APP 平台的条件编译：

```typescript
// plus-app 中最常用的条件编译
// #ifdef APP-PLUS
// APP 专属代码（如调用原生插件）
// #endif

// #ifndef MP-WEIXIN
// 非小程序环境的代码
// #endif
```

plus-app 中**不需要**编写 `#ifdef MP-WEIXIN` 的分支（因为它不会运行小程序）。

---

## 依赖差异

| 维度 | plus-uniapp | plus-app |
|------|------------|----------|
| 核心依赖 | 63 个（全平台） | 4 个（精简） |
| 核心包 | vue, pinia, @dcloudio/uni-app 等 | vue, pinia, crypto-js, jsencrypt |
| WD UI | 100 个组件 | 100 个组件（独立封装） |

---

## 通用规范（两端完全相同）

以下规范对 plus-uniapp 和 plus-app **完全一致**，无需区分：

| 规范 | 说明 |
|------|------|
| WD UI 组件库 | 都用 `wd-*` 组件，都从 `@/wd` 导入 |
| API 调用格式 | `const [err, data] = await api()` |
| API 文件命名 | `xxxApi.ts` + `xxxTypes.ts` |
| Store 模式 | Pinia，模块化 |
| Composables | useAuth, useToken, useHttp 等 |
| 样式单位 | rpx |
| CSS 注释 | `/* */`（禁止 `//`） |
| 消息提示 | `useToast()` / `useMessage()`（从 `@/wd`） |
| 组件导入禁令 | 禁止 `from 'wot-design-uni'`，必须 `from '@/wd'` |

---

## 开发流程（plus-app）

```
1. 收到 plus-app 开发任务
   ↓
2. 【强制】确认文件写到 plus-app/ 目录（不是 plus-uniapp/src/）
   ↓
3. 【强制】Read plus-app 的参考代码：
   - 列表页 → plus-app/components/tabbar/Home.vue
   - 表单页 → plus-app/pages/auth/login.vue
   - API → plus-app/api/app/home/homeApi.ts
   ↓
4. 按照 plus-app 的目录结构创建文件（无 src/ 前缀）
   ↓
5. 使用通用规范编写代码（组件、API、样式与 plus-uniapp 相同）
```

---

## 常见错误

| 错误 | 正确做法 |
|------|---------|
| 把文件写到 `plus-uniapp/src/pages/` | 写到 `plus-app/pages/` |
| 把文件写到 `plus-app/src/pages/` | plus-app 没有 `src/`，直接 `plus-app/pages/` |
| 参考 `plus-uniapp/src/composables/` 的路径 | 参考 `plus-app/composables/` |
| 为 plus-app 编写 `#ifdef MP-WEIXIN` | plus-app 不运行小程序，不需要此分支 |
| 使用 `pnpm build:app` 打包 | plus-app 必须用 HBuilderX 打包 |
| nvue 文件命名用 `list.nvue` / `native.nvue` / `demo.nvue` | 必须用业务语义名：`goodsList.nvue`、`scanCode.nvue`、`login.nvue` |
| 在 H5/小程序下测试 nvue 页面 | nvue 仅 APP-PLUS 生效，必须 HBuilderX 真机/APP 模拟器 |
| nvue 页面里用 wd-ui 组件 / `v-show` / 百分比尺寸 | nvue 是 weex 引擎，不兼容这些；详见 `uniapp-platform` 硬性约束表 |

---

## 何时使用 plus-app vs plus-uniapp

| 需求 | 选择 |
|------|------|
| 开发微信小程序 | plus-uniapp |
| 开发 H5 网页 | plus-uniapp |
| 开发普通 APP（无原生需求） | plus-uniapp（`pnpm dev:app`） |
| 需要原生插件（蓝牙、WiFi 等） | **plus-app** |
| 需要鸿蒙 APP | **plus-app** |
| 需要 APP 商店发布（iOS/Android） | **plus-app** |
| 快速原型/测试 | plus-uniapp（命令行更快） |

---

## 🔗 关联技能边界

本技能专注于 **plus-app 与 plus-uniapp 的差异适配**（原生插件、HBuilderX、鸿蒙、APP 专属配置）。遇到以下场景请改用其他技能：

| 场景 | 应使用技能 | 判断关键词 |
|------|-----------|-----------|
| WD UI 组件、Composables 通用用法 | `ui-mobile` | "wd-"、"useToast"、"组件" |
| 页面设计、布局、留白 | `ui-design-mobile` | "看起来乱"、"排版" |
| 条件编译、跨端判断 | `uniapp-platform` | "#ifdef"、"isApp"、"isMpWeixin" |
| APP 打包发布到应用商店 | `deployment-guide` | "APP 打包"、"发布"、"上架" |

**三技能分工**：
- `app-adapter` = **plus-app 独有差异**（原生插件、鸿蒙配置）
- `ui-mobile` = **两端通用的组件用法**
- `ui-design-mobile` = **两端通用的设计思维**

**激活优先级**：在 plus-app 项目下开发时，`app-adapter` + `ui-mobile` 应同时激活。
