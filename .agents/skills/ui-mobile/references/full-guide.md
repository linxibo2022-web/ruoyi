---
name: ui-mobile
description: |
  移动端（plus-uniapp）组件库完整指南。包含 99+ WD UI 组件、14 个 Composables、4 个 Store 模块、多平台适配。

  触发场景：
  - 开发移动端页面（小程序、H5、APP）
  - 使用 WD UI 组件（wd-*）
  - 移动端列表、表单、弹窗、分页
  - useToast、useMessage、usePayment 等
  - 平台条件编译（#ifdef）

  触发词：wd-、WD UI、移动端组件、小程序组件、wd-button、wd-cell、wd-form、wd-popup、wd-paging、useToast、useMessage、usePayment、plus-uniapp

  适用目录：plus-uniapp/**
---

# 移动端组件库完整指南

> **适用于**: `plus-uniapp/` 目录下的移动端开发（微信小程序、H5、APP）

## 核心原则

> **必须使用 WD UI 组件**（wot-design-uni），禁止使用 uni-ui 或其他组件库。
> **useToast/useMessage 必须从 `@/wd` 导入**，不是 `'wot-design-uni'`！

---

## 组件总览（99+ 组件）

### 基础组件 - 8 个

| 组件 | 用途 | 常用属性 |
|------|------|---------|
| `wd-button` | 按钮 | `type`, `size`, `plain`, `loading`, `disabled` |
| `wd-icon` | 图标 | `name`, `size`, `color` |
| `wd-text` | 文本 | `text`, `lines`, `mode`, `format` |
| `wd-img` | 图片 | `src`, `width`, `height`, `lazy-load`, `mode` |
| `wd-transition` | 过渡动画 | `show`, `name`, `duration` |
| `wd-resize` | 监听元素尺寸 | `@resize` |
| `wd-config-provider` | 全局配置 | `theme`, `theme-vars` |
| `wd-floating-panel` | 浮动面板 | `anchors`, `height` |

### 表单组件 - 18 个

| 组件 | 用途 | 必须使用场景 |
|------|------|-------------|
| `wd-form` | 表单容器 | **所有表单页面** |
| `wd-input` | 输入框 | **文本/数字/密码输入** |
| `wd-textarea` | 多行输入 | **多行文本输入** |
| `wd-picker` | 选择器 | **单选/多选** |
| `wd-picker-view` | 内嵌选择器 | 自定义选择器 |
| `wd-col-picker` | 多列选择器 | 多级联动选择 |
| `wd-datetime-picker` | 日期时间选择 | **日期/时间选择** |
| `wd-datetime-picker-view` | 内嵌日期选择 | 自定义日期选择 |
| `wd-calendar` | 日历 | 日期范围选择 |
| `wd-calendar-view` | 内嵌日历 | 自定义日历 |
| `wd-search` | 搜索框 | **搜索输入** |
| `wd-switch` | 开关 | **状态切换** |
| `wd-checkbox` | 复选框 | **多选** |
| `wd-checkbox-group` | 复选框组 | 多选分组 |
| `wd-radio` | 单选框 | **单选** |
| `wd-radio-group` | 单选框组 | 单选分组 |
| `wd-rate` | 评分 | 星级评价 |
| `wd-slider` | 滑块 | 区间选择 |
| `wd-input-number` | 数字输入 | 数量增减 |
| `wd-upload` | 文件上传 | **图片/文件上传**（零配置，action/header 自带默认值） |

> **🔴 上传必读**：移动端上传**必须**用 `<wd-upload>`（UI 场景）或 `http.upload()`（程序化场景），**禁止**直接调用 `uni.uploadFile`。完整对比与决策树见 `file-oss-management` 技能。
| `wd-select-picker` | 选择选择器 | 复杂选择 |
| `wd-number-keyboard` | 数字键盘 | 安全数字输入 |
| `wd-password-input` | 密码输入 | 密码/验证码输入 |

### 展示组件 - 25 个

| 组件 | 用途 | 适用场景 |
|------|------|---------|
| `wd-cell` | 单元格 | 列表项、导航入口 |
| `wd-cell-group` | 单元格组 | 列表分组 |
| `wd-card` | 卡片 | 内容容器 |
| `wd-collapse` | 折叠面板 | 展开收起 |
| `wd-collapse-item` | 折叠项 | 折叠内容 |
| `wd-swiper` | 轮播图 | 图片/内容轮播 |
| `wd-grid` | 宫格 | 图标导航、快捷入口 |
| `wd-grid-item` | 宫格项 | 宫格内容 |
| `wd-tag` | 标签 | 状态显示、分类标记 |
| `wd-badge` | 徽标 | 数量提示、红点 |
| `wd-progress` | 进度条 | 进度展示 |
| `wd-circle` | 环形进度 | 圆形进度 |
| `wd-countdown` | 倒计时 | 时间倒计 |
| `wd-skeleton` | 骨架屏 | 加载占位 |
| `wd-steps` | 步骤条 | 流程展示 |
| `wd-step` | 步骤项 | 步骤内容 |
| `wd-divider` | 分割线 | 内容分隔 |
| `wd-curtain` | 幕帘 | 广告弹窗 |
| `wd-img-cropper` | 图片裁剪 | 头像裁剪 |
| `wd-notice-bar` | 通知栏 | 滚动公告 |
| `wd-table` | 表格 | 数据表格 |
| `wd-count-to` | 数字动画 | 数字滚动 |
| `wd-watermark` | 水印 | 背景水印 |
| `wd-gap` | 间隔 | 布局间隔 |
| `wd-sticky` | 吸顶 | 滚动吸顶 |

### 导航组件 - 10 个

| 组件 | 用途 | 必须使用场景 |
|------|------|-------------|
| `wd-navbar` | 导航栏 | **页面导航** |
| `wd-tabs` | 标签页 | **内容切换** |
| `wd-tab` | 标签页项 | 标签内容 |
| `wd-tabbar` | 底部导航 | **主导航** |
| `wd-tabbar-item` | 底部导航项 | 导航项 |
| `wd-sidebar` | 侧边栏 | 分类导航 |
| `wd-sidebar-item` | 侧边栏项 | 分类项 |
| `wd-drop-menu` | 下拉菜单 | 筛选条件 |
| `wd-drop-menu-item` | 下拉菜单项 | 筛选项 |
| `wd-index-bar` | 索引栏 | 字母索引 |
| `wd-index-anchor` | 索引锚点 | 索引锚点 |
| `wd-segmented` | 分段器 | 视图切换 |
| `wd-backtop` | 回到顶部 | 长页面返回 |
| `wd-pagination` | 分页 | 传统分页 |
| `wd-popover` | 气泡弹出 | 提示信息 |

### 反馈组件 - 15 个

| 组件 | 用途 | 适用场景 |
|------|------|---------|
| `wd-popup` | 弹出层 | **自定义弹窗** |
| `wd-action-sheet` | 动作面板 | 操作选项 |
| `wd-toast` | 轻提示 | 操作反馈 |
| `wd-message-box` | 消息弹窗 | 确认操作 |
| `wd-notify` | 消息通知 | 顶部通知 |
| `wd-status-tip` | 状态提示 | **空状态/错误/缺省页** |
| `wd-loadmore` | 加载更多 | **列表底部加载** |
| `wd-loading` | 加载中 | 加载状态 |
| `wd-overlay` | 遮罩层 | 背景遮罩 |
| `wd-tooltip` | 文字提示 | 提示信息 |
| `wd-swipe-action` | 滑动操作 | 左滑删除 |
| `wd-sort-button` | 排序按钮 | 列表排序 |
| `wd-fab` | 悬浮按钮 | 快捷操作 |
| `wd-pull-refresh` | 下拉刷新 | 页面刷新 |
| `wd-reach-bottom` | 触底加载 | 列表加载 |

### 项目封装组件 - 核心

| 组件 | 用途 | **必须使用** |
|------|------|-------------|
| `wd-paging` | **分页组件** | **所有列表分页** |

---

## wd-paging 分页组件（核心）

> **这是项目最重要的分页组件**，必须用于所有列表页面！
>
> 🔴 **心智模型**：把「分页接口函数」交给 `:fetch`，组件自己管页码、数据、缓存、触底加载、tabs、搜索。
> 页面**不持有列表数据**，也**不要**自己写 `pageNum` / `pageSize` / 触底逻辑。
>
> 权威参考实现：`plus-uniapp/src/components/tabbar/Home.vue`
> 组件源码（改动前必读）：`plus-uniapp/src/wd/components/wd-paging/wd-paging.vue`

### 基础用法

```vue
<!-- wd-paging 分页列表基础用法 -->
<template>
  <wd-paging
    ref="paging"
    :fetch="pageItems"
    :params="queryParams"
    :scroll-top="scrollTop"
    @scroll-to-top="handleScrollToTop"
  >
    <!-- 🔴 插槽名是 item，不是 default -->
    <template #item="{ item }: { item: ItemVo }">
      <wd-cell :title="item.name" :label="item.description" is-link />
    </template>

    <!-- 空状态（可选，不写则用组件默认空态） -->
    <template #empty>
      <wd-status-tip image="search" tip="暂无数据" />
    </template>
  </wd-paging>
</template>

<script setup lang="ts">
import type { PagingInstance } from '@/wd'
import { pageItems } from '@/api/app/xxx/xxxApi'
import type { ItemVo } from '@/api/app/xxx/xxxTypes'

// useScroll 已自动导入
const { scrollTop, scrollToTop } = useScroll()
const paging = ref<PagingInstance>()

/* 附加查询参数：组件内部 deep watch，变化后自动刷新，不需要手动调 refresh */
const queryParams = ref({
  orderByColumn: 'createTime',
  isAsc: 'desc',
  status: '1',
})

function handleScrollToTop() {
  scrollToTop(300)
}

/* 只有「数据被外部改动」时才手动刷新，如新增 / 编辑 / 删除之后 */
function reload() {
  paging.value?.refresh()
}
</script>
```

> `:fetch` 直接传 API 函数即可 —— 项目 http 封装返回的就是 `[err, data]` 元组，组件内部按元组解构。

### 带 tabs + 内置搜索

```vue
<!-- 带 tabs 和搜索的分页列表 -->
<template>
  <view class="page">
    <wd-paging
      ref="paging"
      :fetch="pageItems"
      :params="queryParams"
      :tabs="tabsConfig"
      :scroll-top="scrollTop"
      show-search
      search-placeholder="搜索名称/编号"
      @scroll-to-top="handleScrollToTop"
      @tab-change="handleTabChange"
    >
      <template #item="{ item }: { item: ItemVo }">
        <wd-cell :title="item.name" is-link @click="goDetail(item.id)">
          <template #value>
            <wd-tag :type="item.status === '1' ? 'success' : 'danger'">
              {{ item.status === '1' ? '正常' : '停用' }}
            </wd-tag>
          </template>
        </wd-cell>
      </template>
    </wd-paging>
  </view>
</template>

<script setup lang="ts">
import type { PagingInstance } from '@/wd'
import { pageItems } from '@/api/app/xxx/xxxApi'
import type { ItemVo } from '@/api/app/xxx/xxxTypes'

const { scrollTop, scrollToTop } = useScroll()
const paging = ref<PagingInstance>()

const queryParams = ref({
  orderByColumn: 'createTime',
  isAsc: 'desc',
})

/* 每个 tab 的 data 会直接合并进查询参数；每个 tab 的数据独立缓存 */
const tabsConfig = ref([
  { name: 'all', title: '全部', data: {} },
  { name: 'normal', title: '正常', data: { status: '1' } },
  { name: 'disabled', title: '停用', data: { status: '0' } },
])

function handleScrollToTop() {
  scrollToTop(300)
}

function handleTabChange({ index, name }: { index: number, name: string | number }) {
  console.log('切换到 tab', index, name)
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages-sub/xxx/detail?id=${id}` })
}
</script>
```

> 🔴 `show-search` 的关键词以 **`searchValue`** 字段传给 `fetch`，后端查询对象必须支持该字段。
> 🔴 `tabs` 每个 tab 的数据**独立缓存**：切回已访问过的 tab 不会重新请求。需要强制拉新数据时，
> 先 `paging.value?.clearTabData()` 再 `paging.value?.refresh()`。

### wd-paging 关键属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `fetch` | `(query) => Result<PageResult<T>>` | — | **必填**。分页接口函数，直接传 API 方法 |
| `params` | `Record<string, any>` | `{}` | 附加查询参数，deep watch，变化自动刷新 |
| `page-size` | `number` | `10` | 每页条数（`params.pageSize` 优先级更高） |
| `auto` | `boolean` | `true` | 挂载后自动加载首屏 |
| `scroll-top` | `number` | `0` | 外部传入的页面滚动距离，用于控制回顶按钮显隐 |
| `tabs` | `PagingTabItem[]` | `[]` | 顶部标签页，每项 `{ name, title, data, badgeProps?, radioGroupConfig? }` |
| `default-tab` | `number \| string` | `0` | 默认选中的 tab（索引或 name） |
| `tabs-fixed` | `boolean` | `false` | tabs 吸顶 |
| `radio-group-config` | `PagingRadioGroupConfig` | — | tabs 下方的单选筛选条 `{ field, defaultValue, options }` |
| `show-search` | `boolean` | `false` | 显示内置搜索框（关键词字段名 `searchValue`） |
| `show-total` | `boolean` | `false` | 搜索框右侧显示总条数 |
| `disabled-auto-load` | `boolean` | `false` | 禁用触底自动翻页 |
| `max-records` | `number` | `0` | 最多显示条数，`0` = 不限 |
| `show-manual-load-button` | `boolean` | `false` | 禁用自动加载时显示「点击加载更多」按钮 |
| `empty-text` / `empty-image` | `string` | `暂无数据` / `content` | 空态文案与图片 |
| `navbar-height` / `tabs-height` | `number` | `0` / `48` | 吸顶偏移计算用（px） |

### wd-paging 插槽

| 插槽 | 作用域参数 | 说明 |
|------|-----------|------|
| `item` | `item`、`index`、`currentTab`、`currentTabData`、`currentRadioValue`、`currentRadioData` | **列表项，必写** |
| `after-items` | 上述 + `currentPageData`、`displayRecords` | 列表末尾追加内容（空态下也显示） |
| `empty` | `currentTab`、`currentTabData`、`currentRadioValue`、`currentRadioData` | 自定义空态 |

### wd-paging 事件

| 事件 | 回调参数 | 说明 |
|------|---------|------|
| `load` | `(data, tabIndex?)` | 每次加载成功 |
| `search` | `(keyword)` | 内置搜索框触发搜索 |
| `tab-change` | `({ index, name, tab })` | 切换 tab |
| `radio-change` | `({ value, option, field, tabIndex })` | 切换单选筛选 |
| `error` | `(error)` | 加载失败 |
| `scroll-to-top` | — | 点击回顶按钮，**由页面负责实际滚动** |

### wd-paging 实例方法（`PagingInstance`）

| 方法 | 说明 |
|------|------|
| `refresh()` | 重新加载第一页（新增 / 编辑 / 删除后调用） |
| `loadMore()` | 手动加载下一页 |
| `switchTab(nameOrIndex)` | 切换 tab（⚠️ 只改选中态，**不会**自动拉数据） |
| `switchRadio(value)` | 切换单选筛选并重新加载 |
| `clearTabData(tabIndex?)` | 清空指定 tab 缓存（不传 = 当前 tab） |
| `clearTabRadioData(tabIndex?, radioValue?)` | 清空指定 tab+radio 组合缓存 |
| `clearAllData()` | 清空全部缓存，回到初始状态 |

只读属性：`pageData`、`loading`、`currentTabIndex`、`currentTabData`、`currentRadioValue`、`currentRadioData`、`isReachEnd`、`displayRecords`、`canLoadMore()`

### 🔴 wd-paging 常见误用（下列 API 都不存在）

| ❌ 错误写法 | ✅ 实际 API |
|------------|-----------|
| `v-model="dataList"` | 没有 v-model。数据由组件内部持有，页面通过 `#item` 插槽消费 |
| `@query="fn"` + `pagingRef.complete(...)` | `:fetch="apiFn"`，组件自动调用并处理结果 |
| `:api="loadData"` | `:fetch="loadData"` |
| `<template #default="{ item }">` | `<template #item="{ item }">` |
| `pagingRef.reload()` | `paging.refresh()` |
| `pagingRef.endRefresh()` | 不存在，组件自己管加载态 |
| `:fixed="true"` / `default-page-size` / `concat` | 不存在。每页条数用 `:page-size`（或 `params.pageSize`） |
| 页面自己写 `pageNum++` / 触底监听 | 组件内置 IntersectionObserver 自动翻页 |

---

## Composables 完整列表（14 个）

### 核心 Composables

| Hook | 用途 | 关键方法 |
|------|------|---------|
| `useAuth` | 认证管理 | `checkLogin()`, `requireLogin()`, `logout()` |
| `useToken` | Token 管理 | `getToken()`, `setToken()`, `removeToken()` |
| `useHttp` | HTTP 请求 | 自动处理加密、Token、错误 |
| `usePayment` | 支付功能 | `createOrderAndPay()`, `payOrder()`, `pollOrderStatus()` |
| `useDict` | 字典数据 | `loadDict()`, `getDictLabel()`, `DictTypes` |
| `useShare` | 分享功能 | `share()`, `setShareInfo()` |
| `useWxShare` | 微信分享 | `initSdk()`, `setShare()`, `wxPay()` |
| `useSubscribe` | 订阅消息 | `subscribe()`, `loadTemplates()` |

### 工具 Composables

| Hook | 用途 | 关键方法 |
|------|------|---------|
| `useI18n` | 国际化 | `t()`, `setLocale()`, `locale` |
| `useTheme` | 主题切换 | `toggleTheme()`, `isDark`, `setTheme()` |
| `useScroll` | 滚动控制 | `scrollTo()`, `scrollTop`, `isReachBottom` |
| `useEventBus` | 事件总线 | `emit()`, `on()`, `off()`, `once()` |
| `useWebSocket` | WebSocket | `connect()`, `send()`, `close()`, `onMessage` |
| `useAppInit` | 应用初始化 | `init()`, `checkUpdate()` |

### useI18n 用法（增强版国际化，已自动导入）

```typescript
// useI18n 已自动导入，直接使用即可
const { t, isChinese, setLanguage } = useI18n()

// t() 常用方式：
t('button.add')                    // 1. 传统 i18n 键
t('User Name', '用户名')           // 2. 双参数（英文, 中文）- 最常用
t('', { field: 'Name', comment: '名称' })  // 3. 对象参数
```

> 更多用法参考源码：`plus-uniapp/src/composables/useI18n.ts`

### useAuth 详细用法

```typescript
import { useAuth } from '@/composables/useAuth'

const { checkLogin, requireLogin, logout, isLoggedIn } = useAuth()

// 检查登录状态（不跳转）
if (checkLogin()) {
  // 已登录
}

// 要求登录（未登录自动跳转登录页）
const doSomething = () => {
  if (!requireLogin()) return
  // 执行需要登录的操作
}

// 登出
const handleLogout = async () => {
  await logout()
  uni.reLaunch({ url: '/pages/index/index' })
}
```

### usePayment 详细用法

```typescript
import { usePayment } from '@/composables/usePayment'
import { useToast } from '@/wd'

const { createOrderAndPay, payOrder, pollOrderStatus, payLoading } = usePayment()
const toast = useToast()

// 创建订单并支付（完整流程）
const handlePay = async (goodsId: number, amount: number) => {
  const result = await createOrderAndPay({
    goodsId,
    amount,
    payType: 'wxpay'  // wxpay | alipay
  })

  if (result.success) {
    toast.success('支付成功')
    // 刷新订单状态
  } else if (result.cancelled) {
    toast.info('已取消支付')
  } else {
    toast.error(result.message || '支付失败')
  }
}

// 轮询订单状态（支付后验证）
const checkOrderStatus = async (orderNo: string) => {
  const isPaid = await pollOrderStatus(orderNo, {
    maxAttempts: 10,
    interval: 1000
  })
  return isPaid
}
```

### useWebSocket 详细用法

```typescript
import { useWebSocket } from '@/composables/useWebSocket'

const {
  connect,
  send,
  close,
  isConnected,
  lastMessage,
  onMessage
} = useWebSocket()

// 连接
onMounted(() => {
  connect('/ws/chat')
})

// 监听消息
onMessage((data) => {
  console.log('收到消息:', data)
})

// 发送消息
const sendMessage = (content: string) => {
  send({ type: 'chat', content })
}

// 断开
onUnmounted(() => {
  close()
})
```

### useDict 详细用法

```typescript
import { useDict, DictTypes } from '@/composables/useDict'

// 获取字典数据
const { sys_enable_status, sys_user_gender, dictLoading } = useDict(
  DictTypes.sys_enable_status,
  DictTypes.sys_user_gender
)

// 返回格式：
// [
//   { label: '启用', value: '1', tagType: 'success' },
//   { label: '停用', value: '0', tagType: 'danger' }
// ]

// 在模板中使用
// <wd-tag :type="getDictType(item.status)">{{ getDictLabel(item.status) }}</wd-tag>
```

---

## Store 状态管理（4 个模块）

| Store | 位置 | 职责 |
|-------|------|------|
| `useUserStore` | `stores/modules/user.ts` | 用户信息、登录状态、权限 |
| `useDictStore` | `stores/modules/dict.ts` | 字典数据缓存 |
| `useFeatureStore` | `stores/modules/feature.ts` | 功能特性开关 |
| `useTabbarStore` | `stores/modules/tabbar.ts` | Tabbar 状态管理 |

### useUserStore 用法

```typescript
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()

// 用户信息
userStore.user          // 用户对象
userStore.token         // 访问令牌
userStore.isLoggedIn    // 是否登录
userStore.roles         // 角色列表
userStore.permissions   // 权限列表

// 方法
await userStore.login(loginData)   // 登录
await userStore.logout()           // 登出
await userStore.getInfo()          // 获取用户信息
userStore.hasRole('admin')         // 角色判断
userStore.hasPermission('xxx:add') // 权限判断
```

### useTabbarStore 用法

```typescript
import { useTabbarStore } from '@/stores/modules/tabbar'

const tabbarStore = useTabbarStore()

// 当前选中的 tab
tabbarStore.activeTab

// 切换 tab
tabbarStore.setActiveTab(1)

// 显示/隐藏 tabbar
tabbarStore.showTabbar = false
```

---

## Utils 工具函数（13 个模块）

| 模块 | 主要函数 | 用途 |
|------|---------|------|
| `http.ts` | `http.get/post/put/del` | HTTP 请求封装 |
| `auth.ts` | `getToken, setToken, removeToken` | Token 管理 |
| `cache.ts` | `localCache, sessionCache` | 本地存储 |
| `crypto.ts` | `encrypt, decrypt` | RSA+AES 加密 |
| `format.ts` | `formatDate, formatMoney, formatPhone` | 格式化 |
| `validate.ts` | `isPhone, isEmail, isIdCard` | 校验函数 |
| `platform.ts` | `isH5, isWeixin, isApp` | 平台判断 |
| `navigate.ts` | `navigateTo, redirectTo, reLaunch` | 路由跳转 |
| `share.ts` | `shareToWechat, shareToTimeline` | 分享功能 |
| `image.ts` | `chooseImage, compressImage, uploadImage` | 图片处理 |
| `location.ts` | `getLocation, openLocation` | 定位功能 |
| `permission.ts` | `requestPermission, checkPermission` | 权限申请 |
| `update.ts` | `checkUpdate, downloadUpdate` | 版本更新 |

---

## API 层规范

### 文件结构

```
api/app/xxx/
├── xxxApi.ts          # API 调用定义
├── xxxTypes.ts        # 类型定义
```

### xxxApi.ts 示例

```typescript
// http/Result/PageResult/PageQuery 已自动导入
import type { XxxQuery, XxxBo, XxxVo } from './xxxTypes'

export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get('/app/xxx/pageXxxs', query)
}

export const getXxx = (id: string | number): Result<XxxVo> => {
  return http.get(`/app/xxx/getXxx/${id}`)
}

export const addXxx = (data: XxxBo): Result<string | number> => {
  return http.post('/app/xxx/addXxx', data)
}

export const updateXxx = (data: XxxBo): Result<void> => {
  return http.put('/app/xxx/updateXxx', data)
}

export const deleteXxxs = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del(`/app/xxx/deleteXxxs/${ids}`)
}
```

### API 调用规范

```typescript
// ✅ 正确：使用 [err, data] 格式
const [err, data] = await pageXxxs(queryParams)
if (!err) {
  dataList.value = data.records
}

// ❌ 错误：使用 try-catch
try {
  const data = await pageXxxs(queryParams)
} catch (error) { ... }
```

---

## 平台条件编译

### #ifdef 用法

```vue
<!-- 平台条件编译示例 -->
<template>
  <!-- 仅微信小程序 -->
  <!-- #ifdef MP-WEIXIN -->
  <button open-type="share">分享</button>
  <!-- #endif -->

  <!-- 仅 H5 -->
  <!-- #ifdef H5 -->
  <wd-button @click="shareToWechat">分享到微信</wd-button>
  <!-- #endif -->

  <!-- 仅 APP -->
  <!-- #ifdef APP-PLUS -->
  <wd-button @click="scanCode">扫码</wd-button>
  <!-- #endif -->

  <!-- 除了微信小程序 -->
  <!-- #ifndef MP-WEIXIN -->
  <view>非微信小程序内容</view>
  <!-- #endif -->
</template>

<script setup lang="ts">
// 仅微信小程序
// #ifdef MP-WEIXIN
import { wxLogin } from '@/utils/wx'
// #endif

// 仅 H5
// #ifdef H5
import { initWxSdk } from '@/utils/wxH5'
// #endif
</script>

<style lang="scss" scoped>
/* 仅微信小程序 */
/* #ifdef MP-WEIXIN */
.wx-only {
  display: block;
}
/* #endif */
</style>
```

### 常用平台标识

| 标识 | 平台 |
|------|------|
| `H5` | H5 网页 |
| `MP-WEIXIN` | 微信小程序 |
| `MP-ALIPAY` | 支付宝小程序 |
| `MP-BAIDU` | 百度小程序 |
| `APP-PLUS` | APP（iOS/Android） |
| `APP-ANDROID` | Android APP |
| `APP-IOS` | iOS APP |

### 平台判断工具

```typescript
import { isH5, isWeixin, isApp, isMpWeixin, isMpAlipay } from '@/utils/platform'

if (isH5()) {
  // H5 专属逻辑
}

if (isMpWeixin()) {
  // 微信小程序专属逻辑
}
```

---

## 安全与加密

### HTTP 请求加密

项目使用 RSA + AES 混合加密：

```typescript
// 加密配置（自动处理，无需手动调用）
// 1. 生成随机 AES 密钥
// 2. 使用 AES 加密请求体
// 3. 使用 RSA 公钥加密 AES 密钥
// 4. 请求头携带加密后的 AES 密钥

// 请求拦截器自动处理加密
// 响应拦截器自动处理解密
```

### Token 安全存储

```typescript
import { useToken } from '@/composables/useToken'

const { getToken, setToken, removeToken, getRefreshToken } = useToken()

// Token 存储在 storage 中，自动加密
// 支持 Token 刷新机制
```

---

## Toast 和 Message

### 导入方式（重要！）

```typescript
// ⚠️ 必须从 @/wd 导入，不是 'wot-design-uni'！
import { useToast, useMessage } from '@/wd'

const toast = useToast()
const message = useMessage()
```

### Toast 用法

```typescript
// 成功提示
toast.success('操作成功')

// 错误提示
toast.error('操作失败')

// 警告提示
toast.warning('警告信息')

// 普通提示
toast.info('提示信息')

// 加载提示
toast.loading('加载中...')

// 关闭加载
toast.close()
```

### Message 用法

```typescript
// 确认弹窗
const handleDelete = async () => {
  const { action } = await message.confirm({
    title: '提示',
    msg: '确定要删除吗？'
  })

  if (action === 'confirm') {
    const [err] = await deleteItem(id)
    if (!err) {
      toast.success('删除成功')
    }
  }
}

// 输入弹窗
const handleRename = async () => {
  const { action, value } = await message.prompt({
    title: '重命名',
    msg: '请输入新名称',
    inputValue: currentName
  })

  if (action === 'confirm' && value) {
    // 执行重命名
  }
}

// 提示弹窗
await message.alert({
  title: '提示',
  msg: '操作完成'
})
```

---

## 标准页面模板

### 列表页（使用 wd-paging）

```vue
<!-- 标准列表页模板 -->
<template>
  <view class="page">
    <!-- 搜索栏 -->
    <wd-search
      v-model="keyword"
      placeholder="搜索"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <!-- 分页列表 -->
    <wd-paging
      ref="paging"
      :fetch="pageXxxs"
      :params="queryParams"
      :scroll-top="scrollTop"
      @scroll-to-top="handleScrollToTop"
    >
      <template #item="{ item }: { item: XxxVo }">
        <wd-cell
          :title="item.name"
          :label="item.createTime"
          is-link
          @click="goDetail(item.id)"
        >
          <template #value>
            <wd-tag :type="item.status === '1' ? 'success' : 'danger'">
              {{ item.status === '1' ? '启用' : '停用' }}
            </wd-tag>
          </template>
        </wd-cell>
      </template>

      <template #empty>
        <wd-status-tip image="search" tip="暂无数据" />
      </template>
    </wd-paging>
  </view>
</template>

<script setup lang="ts">
import type { PagingInstance } from '@/wd'
import { pageXxxs } from '@/api/app/xxx/xxxApi'
import type { XxxVo } from '@/api/app/xxx/xxxTypes'

const { scrollTop, scrollToTop } = useScroll()
const paging = ref<PagingInstance>()
const keyword = ref('')

/* params 变化组件会自动刷新，所以搜索只需改 params，不用手动调 refresh */
const queryParams = computed(() => ({
  orderByColumn: 'createTime',
  isAsc: 'desc',
  searchValue: keyword.value,
}))

function handleScrollToTop() {
  scrollToTop(300)
}

function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages-sub/xxx/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--wd-bg-color);
}
</style>
```

### 表单页

```vue
<!-- 标准表单页模板 -->
<template>
  <view class="page">
    <wd-form ref="formRef" :model="form">
      <wd-cell-group title="基本信息">
        <wd-input
          v-model="form.name"
          label="名称"
          label-width="80px"
          placeholder="请输入名称"
          clearable
          prop="name"
          :rules="[{ required: true, message: '请输入名称' }]"
        />

        <wd-textarea
          v-model="form.description"
          label="描述"
          label-width="80px"
          placeholder="请输入描述"
          :maxlength="200"
          show-word-limit
          prop="description"
        />

        <wd-picker
          v-model="form.type"
          label="类型"
          label-width="80px"
          :columns="typeOptions"
          placeholder="请选择类型"
          prop="type"
          :rules="[{ required: true, message: '请选择类型' }]"
        />

        <wd-datetime-picker
          v-model="form.date"
          label="日期"
          label-width="80px"
          type="date"
          placeholder="请选择日期"
          prop="date"
        />

        <wd-cell title="状态" title-width="80px">
          <wd-switch v-model="form.status" />
        </wd-cell>
      </wd-cell-group>

      <wd-cell-group title="图片上传">
        <wd-cell>
          <!-- ✅ wd-upload 的 action 默认 /resource/oss/upload，header 默认注入 Token，无需手动配置 -->
          <wd-upload
            v-model:file-list="form.images"
            :limit="9"
            @success="onUploadSuccess"
          />
        </wd-cell>
      </wd-cell-group>
    </wd-form>

    <!-- 底部按钮 -->
    <view class="footer safe-area-bottom">
      <wd-button type="primary" block :loading="submitting" @click="handleSubmit">
        提交
      </wd-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useToast } from '@/wd'
import { addXxx } from '@/api/app/xxx/xxxApi'

const toast = useToast()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  name: '',
  description: '',
  type: '',
  date: '',
  status: true,
  images: []
})

const typeOptions = [
  { label: '类型A', value: 'A' },
  { label: '类型B', value: 'B' },
  { label: '类型C', value: 'C' }
]

const onUploadSuccess = () => {
  toast.success('上传成功')
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const [err] = await addXxx(form)
    if (!err) {
      toast.success('保存成功')
      uni.navigateBack()
    }
  } catch (e) {
    // 校验失败
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--wd-bg-color);
  padding-bottom: 120rpx;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: #fff;
}

.safe-area-bottom {
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
</style>
```

---

## 自动导入说明

以下内容**无需手动 import**：

```typescript
// Vue APIs
ref, reactive, computed, watch, watchEffect
onMounted, onUnmounted, nextTick, toRefs

// UniApp 生命周期
onLoad, onShow, onHide, onReady
onPullDownRefresh, onReachBottom
onShareAppMessage, onShareTimeline

// Pinia
defineStore, storeToRefs

// HTTP 请求
http

// 类型
Result, PageResult, PageQuery

// Stores
useUserStore, useDictStore, useFeatureStore, useTabbarStore

// Composables
useAuth, useToken, useDict, DictTypes
usePayment, useShare, useSubscribe
useI18n, useTheme, useScroll, useEventBus
useWebSocket, useWxShare, useAppInit
```

### 需要手动导入

```typescript
// ⚠️ WD UI hooks 必须手动导入
import { useToast, useMessage } from '@/wd'

// ⚠️ PagingInstance 类型
import type { PagingInstance } from '@/wd'
```

---

## 样式规范

### 单位使用

```scss
/* ✅ 使用 rpx（响应式像素） */
.box {
  width: 200rpx;
  padding: 24rpx;
  font-size: 28rpx;
}

/* ✅ 固定尺寸使用 px */
.icon {
  width: 44px;
  height: 44px;
}

/* ✅ 安全区域 */
.footer {
  padding-bottom: env(safe-area-inset-bottom);
}

/* ✅ CSS 注释必须用这种格式 */
/* 不能用 // 注释 */
```

### 主题变量

```scss
/* 使用 WD UI 主题变量 */
.text-primary {
  color: var(--wd-color-theme);
}

.bg-page {
  background: var(--wd-bg-color);
}

.text-secondary {
  color: var(--wd-color-content);
}

.border {
  border-color: var(--wd-border-color);
}
```

---

## 目录结构规范

> **⚡ 路由自动生成**：项目使用 `@uni-helper/vite-plugin-uni-pages` 插件，新建页面文件后**无需手动修改 `pages.json`**，插件会自动扫描 `pages/` 和 `pages-sub/` 目录生成路由配置。

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
│   └── xxx/
│       ├── list.vue               # 列表页
│       └── detail.vue             # 详情页
├── components/                    # 业务组件
│   ├── auth/
│   │   └── AuthModal.vue          # 认证弹窗
│   └── tabbar/
│       ├── Home.vue               # ✅ 首页（列表+分页+支付示例）
│       ├── Menu.vue               # 菜单页
│       └── My.vue                 # 我的页
├── api/                           # API 定义
│   └── app/
│       └── xxx/
│           ├── xxxApi.ts          # ✅ API 接口定义
│           └── xxxTypes.ts        # ✅ 类型定义
├── composables/                   # Composables
│   ├── useAuth.ts                 # 认证
│   ├── usePayment.ts              # 支付
│   └── useScroll.ts               # 滚动
├── stores/                        # Pinia Stores
│   └── modules/
│       ├── user.ts                # 用户状态
│       ├── dict.ts                # 字典缓存
│       ├── feature.ts             # 功能开关
│       └── tabbar.ts              # Tabbar状态
├── utils/                         # 工具函数
│   ├── http.ts                    # HTTP 封装
│   ├── crypto.ts                  # 加密工具
│   └── platform.ts                # 平台判断
└── wd/                            # WD UI 封装
    ├── index.ts                   # 导出（useToast/useMessage）
    └── components/                # 99+ WD 组件
        ├── wd-button/
        ├── wd-cell/
        ├── wd-form/
        ├── wd-paging/             # ✅ 核心分页组件
        └── ...
```

---

## 分包配置规范

> **⚠️ 禁止手动修改 `pages.json`！** 路由由 `uni-pages` 插件自动生成。

### 何时需要分包

以**主包大小**为准（微信开发者工具「详情」→「本地代码」查看）：

| 主包大小 | 是否分包 |
|---------|---------|
| < 1.5MB | ❌ 不需要 |
| ≥ 1.5MB | ✅ 考虑分包 |

> 微信小程序主包限制 2MB，提前分包避免超限。

### 分包分类规范

| 分包 | 目录 | 包含内容 |
|------|------|---------|
| `admin` | `pages-sub/admin/` | 后台管理、数据统计、系统设置 |
| `mall` | `pages-sub/mall/` | 商品、购物车、结算、售后 |
| `order` | `pages-sub/order/` | 订单列表、订单详情、物流 |
| `user` | `pages-sub/user/` | 个人资料、地址管理、收藏 |
| `content` | `pages-sub/content/` | 文章、资讯、帮助中心 |
| `activity` | `pages-sub/activity/` | 营销活动、优惠券、拼团 |

> **原则**：按业务域划分，同一业务的页面放同一分包。

### 添加分包（两步）

**1. 在 `uni-pages.ts` 注册**

```typescript
// plus-uniapp/vite/plugins/uni-pages.ts
subPackages: [
  'src/pages-sub/admin',
  'src/pages-sub/mall',   // ✅ 新增
],
```

**2. 创建页面文件**

```
plus-uniapp/src/pages-sub/mall/
├── goods/list.vue
└── goods/detail.vue
```

插件自动生成路由，**无需其他操作**。

### 跳转分包页面

```typescript
uni.navigateTo({ url: '/pages-sub/mall/goods/detail?id=123' })
```

---

## 参考文件位置

| 用途 | 路径 |
|------|------|
| 首页完整示例（列表+分页+支付） | `plus-uniapp/src/components/tabbar/Home.vue` |
| 登录页（表单+验证） | `plus-uniapp/src/pages/auth/login.vue` |
| API 定义示例 | `plus-uniapp/src/api/app/home/homeApi.ts` |
| 类型定义示例 | `plus-uniapp/src/api/app/home/homeTypes.ts` |
| WD 组件封装 | `plus-uniapp/src/wd/components/wd-*/` |
| 认证 Composable | `plus-uniapp/src/composables/useAuth.ts` |
| 支付 Composable | `plus-uniapp/src/composables/usePayment.ts` |
| 用户 Store | `plus-uniapp/src/stores/modules/user.ts` |
| HTTP 封装 | `plus-uniapp/src/utils/http.ts` |
| 官方文档 | https://wot-design-uni.cn/ |

---

## 快速开发检查清单

在开发新页面前，必须：

- [ ] 阅读 `plus-uniapp/src/components/tabbar/Home.vue` 列表页实现
- [ ] 阅读 `plus-uniapp/src/pages/auth/login.vue` 表单页实现
- [ ] 查阅 `plus-uniapp/src/api/app/home/homeApi.ts` 的 API 规范
- [ ] 了解 wd-paging 分页组件用法
- [ ] 理解 useToast/useMessage 从 `@/wd` 导入
- [ ] 确认 API 返回值格式 `[err, data]`
- [ ] 确保使用 WD UI 组件，不使用 uni-ui
- [ ] 使用 rpx 单位，CSS 注释用 `/* */`
- [ ] 平台特定代码使用 #ifdef 条件编译

---

## 🔗 关联技能边界

本技能专注于**移动端组件库的具体用法**（WD UI 组件、Composables、API）。遇到以下场景请改用其他技能：

| 场景 | 应使用技能 | 判断关键词 |
|------|-----------|-----------|
| 页面布局、视觉风格、留白间距（设计思维） | `ui-design-mobile` | "看起来乱"、"不够简洁"、"怎么排版" |
| 开发 plus-app 原生项目（不是 plus-uniapp） | `app-adapter` | "plus-app"、"原生插件"、"HBuilderX"、"鸿蒙" |
| 跨平台条件编译（小程序/H5/APP 差异） | `uniapp-platform` | "#ifdef"、"isMpWeixin"、"条件编译" |
| 移动端状态管理、Composables | `store-mobile` | "useAuth"、"Pinia"、"cache" |

**三技能分工**：
- `ui-mobile` = **技术层**（怎么用组件）
- `ui-design-mobile` = **设计层**（怎么排版好看）
- `app-adapter` = **项目层**（plus-app 独有差异）

三者适用于 `plus-uniapp/**` 和 `plus-app/**` 通用（除 `app-adapter` 专属 plus-app）。
