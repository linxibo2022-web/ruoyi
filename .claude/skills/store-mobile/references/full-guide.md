
# 移动端状态管理与 Composables 指南

> **适用于**: `plus-uniapp/` 目录下的移动端开发（小程序、H5、APP）

## 核心概念

### Store vs Composable

| 类型 | 特点 | 适用场景 |
|------|------|---------|
| **Store (Pinia)** | 全局单例，响应式，可持久化 | 用户信息、字典缓存、功能开关 |
| **Composable** | 可复用逻辑封装，可单例可多实例 | 支付、分享、认证检查、HTTP请求 |

**选择原则：**
- 需要跨页面共享 → Store
- 需要持久化到本地 → Store
- 只是逻辑复用 → Composable
- 特定功能封装 → Composable

---

## 自动导入说明

以下内容**无需手动 import**：

```typescript
// Pinia
defineStore, storeToRefs

// Vue APIs
ref, reactive, computed, watch, onMounted, isRef, toRefs, readonly, nextTick

// UniApp 生命周期
onLoad, onShow, onHide, onReady
onPullDownRefresh, onReachBottom, onPageScroll

// Stores（自动导入）
useUserStore, useDictStore, useFeatureStore, useTabbarStore

// Composables（自动导入）
useAuth, useToken, useDict, DictTypes
usePayment, useShare, useSubscribe, useWxShare
useI18n, useTheme, useScroll, useEventBus
useWebSocket, useAppInit, initializeApp, waitForInit

// HTTP 请求（自动导入）
http  // useHttp 的默认实例
```

---

## 已有 Store 清单

| Store | 文件 | 用途 | 关键方法/属性 |
|-------|------|------|--------------|
| `useUserStore` | `stores/modules/user.ts` | 用户认证与状态 | token, userInfo, isLoggedIn, loginWithPassword, loginWithMiniapp, loginWithMp, fetchUserInfo, logoutUser, navigateWithUserCheck |
| `useDictStore` | `stores/modules/dict.ts` | 字典数据缓存 | getDict, setDict, getDictLabel, getDictLabels, getDictItem, getDictValue |
| `useFeatureStore` | `stores/modules/feature.ts` | 功能开关 | features, initFeatures, isFeatureEnabled |
| `useTabbarStore` | `stores/modules/tabbar.ts` | 底部导航 | setActive, setList |

---

## 已有 Composables 清单

### 核心 Composables

| Composable | 文件 | 用途 | 关键方法 |
|------------|------|------|---------|
| `useHttp` / `http` | `composables/useHttp.ts` | HTTP 请求 | get, post, put, del, upload, download + 链式调用 |
| `useAuth` | `composables/useAuth.ts` | 权限检查 | hasPermission, hasRole, isSuperAdmin, canAccessRoute |
| `useToken` | `composables/useToken.ts` | Token 管理 | getToken, setToken, removeToken, getAuthHeaders |
| `useAppInit` | `composables/useAppInit.ts` | 应用初始化 | initializeApp, waitForInit |

### 业务 Composables

| Composable | 文件 | 用途 | 关键方法 |
|------------|------|------|---------|
| `useDict` | `composables/useDict.ts` | 字典加载 | 返回响应式字典数组 + dictLoading |
| `usePayment` | `composables/usePayment.ts` | 支付功能 | createOrderAndPay, payOrder, pollOrderStatus |
| `useShare` | `composables/useShare.ts` | 小程序分享 | setShareConfig, triggerShare |
| `useWxShare` | `composables/useWxShare.ts` | 公众号H5分享 | initSdk, setShare, isReady |
| `useSubscribe` | `composables/useSubscribe.ts` | 订阅消息 | subscribe, loadTemplates |
| `useWebSocket` | `composables/useWebSocket.ts` | WebSocket | connect, send, close, status |

### 工具 Composables

| Composable | 文件 | 用途 | 关键方法 |
|------------|------|------|---------|
| `useScroll` | `composables/useScroll.ts` | 滚动管理 | scrollTop, updateScrollTop, scrollToTop, shouldShowBacktop |
| `useI18n` | `composables/useI18n.ts` | 国际化 | t, setLocale |
| `useTheme` | `composables/useTheme.ts` | 主题切换 | toggleTheme, isDark |
| `useEventBus` | `composables/useEventBus.ts` | 事件总线 | emit, on, off |

---

## HTTP 请求（重点）

### 基础用法

```typescript
// http 是自动导入的全局实例
const [err, data] = await http.get<UserVo[]>('/api/users')
if (!err) {
  console.log(data)  // UserVo[] 类型
}

// POST 请求
const [err, result] = await http.post<LoginResult>('/api/login', {
  userName: 'admin',
  password: '123456'
})

// PUT 请求
const [err] = await http.put('/api/users', userData)

// DELETE 请求
const [err] = await http.del(`/api/users/${id}`)
```

### 链式调用（项目特色）

```typescript
// 禁用认证（公开接口）
const [err, captcha] = await http.noAuth().get<CaptchaVo>('/auth/imgCode')

// 启用加密
const [err, token] = await http.encrypt().post<LoginResult>('/api/login', data)

// 组合配置
const [err, result] = await http
  .noAuth()           // 禁用认证
  .encrypt()          // 启用加密
  .skipWait()         // 跳过初始化等待
  .timeout(30000)     // 设置超时 30s
  .noMsgError()       // 禁用错误提示（自定义处理）
  .post<any>('/api/register', registerData)

// 禁用防重复提交
const [err] = await http.noRepeatSubmit().post('/api/submit', data)

// 禁用租户信息
const [err] = await http.noTenant().get('/api/public/info')
```

### 链式方法说明

| 方法 | 作用 | 场景 |
|------|------|------|
| `noAuth()` | 禁用 Token 认证 | 登录、注册、公开接口 |
| `encrypt()` | 启用请求加密 | 敏感数据传输 |
| `skipWait()` | 跳过应用初始化等待 | 初始化阶段的请求 |
| `noTenant()` | 禁用租户 ID | 跨租户接口 |
| `noRepeatSubmit()` | 禁用防重复提交 | 允许快速重复请求 |
| `noMsgError()` | 禁用错误提示 | 自定义错误处理 |
| `timeout(ms)` | 设置超时时间 | 长时间请求 |

### 文件上传

> **🔴 禁止直接调用 `uni.uploadFile`**。项目已封装两套机制：
> - **UI 场景**（图片墙、头像、附件列表）→ 用 `<wd-upload>` 组件（见 `ui-mobile`）
> - **程序化场景**（选图后直传、裁剪后上传）→ 用 `http.upload()`（下方）
> 完整决策树与对比见 `file-oss-management`。

```typescript
// ✅ http.upload 自动处理 baseUrl / Token / Content-Language / 统一错误
const [err, result] = await http.upload<UploadResult>({
  url: '/resource/oss/upload',     // 相对路径，baseUrl 自动拼接
  filePath: tempFilePath,
  name: 'file',
  formData: { /* 可选附加字段 */ }
})
if (!err) {
  console.log(result.url)  // UploadResult { url, fileName, fileSize, eTag, ossId? }
}
```

---

## 应用初始化（useAppInit）

### 平台差异处理

| 平台 | 租户ID | 自动登录 | WebSocket |
|------|--------|---------|-----------|
| 微信小程序 | ✅ 自动获取 | ✅ 静默登录 | ✅ 自动连接 |
| 微信公众号H5 | ✅ 自动获取 | ✅ 授权登录 | ✅ 自动连接 |
| 支付宝小程序 | ✅ 自动获取 | ✅ 静默登录 | ✅ 自动连接 |
| APP | ❌ 跳过 | ❌ 手动 | ✅ 登录后连接 |
| 普通H5 | ❌ 跳过 | ❌ 手动 | ✅ 登录后连接 |

### 使用方式

```typescript
// 在 App.vue 中初始化
onLaunch(async () => {
  await initializeApp()  // 自动导入
})

// 在请求前等待初始化完成（http 内部自动处理）
// 如果需要手动等待：
await waitForInit(10000)  // 10秒超时
```

---

## 认证与权限

### useUserStore vs useAuth

| 功能 | useUserStore | useAuth |
|------|-------------|---------|
| **定位** | 状态管理 | 权限检查 |
| **登录/登出** | ✅ | ❌ |
| **用户信息管理** | ✅ | ❌ |
| **权限检查** | ❌ | ✅ |
| **角色检查** | ❌ | ✅ |
| **路由控制** | ❌ | ✅ |

### useUserStore 使用

```typescript
const userStore = useUserStore()

// 登录
const [err] = await userStore.loginWithPassword({
  userName: 'admin',
  password: '123456',
  code: '1234',
  uuid: 'uuid'
})

// 小程序一键登录
const [err] = await userStore.loginWithMiniapp()

// 获取用户信息
const [err] = await userStore.fetchUserInfo()

// 检查登录状态
if (userStore.isLoggedIn) {
  console.log('用户名:', userStore.userInfo?.nickName)
}

// 登出
await userStore.logoutUser()

// 带用户信息检查的跳转
userStore.navigateWithUserCheck({ url: '/pages/order/order' })
```

### useAuth 使用

```typescript
const {
  isLoggedIn,
  hasPermission,
  hasRole,
  isSuperAdmin,
  isTenantAdmin,
  canAccessRoute
} = useAuth()

// 检查权限
if (hasPermission('system:user:add')) {
  // 有新增用户权限
}

// 检查多个权限（满足任一）
if (hasPermission(['system:user:add', 'system:user:edit'])) {
  // 有新增或编辑权限
}

// 检查角色
if (hasRole('admin')) {
  // 是管理员
}

// 检查是否超级管理员
if (isSuperAdmin()) {
  // 拥有所有权限
}

// 路由访问控制
if (canAccessRoute(route)) {
  // 可以访问该路由
}
```

---

## 字典使用（重点）

### useDict vs useDictStore

```typescript
// ✅ 方式1：useDict - 获取字典数组（用于选择器）
const { sys_user_gender, sys_enable_status, dictLoading } = useDict(
  DictTypes.sys_user_gender,
  DictTypes.sys_enable_status
)

// 在模板中使用
// <wd-picker :columns="sys_user_gender" v-model="form.gender" :loading="dictLoading" />


// ✅ 方式2：useDictStore - 获取字典标签（用于显示）
const dictStore = useDictStore()
const label = dictStore.getDictLabel('sys_user_gender', '0')  // 返回 '男'
const labels = dictStore.getDictLabels('sys_user_gender', ['0', '1'])  // 返回 ['男', '女']
const item = dictStore.getDictItem('sys_user_gender', '0')  // 返回完整对象


// ❌ 错误：useDict 没有 getDictLabel 方法
// const { getDictLabel } = useDict(...)  // 不存在这个方法！
```

### 完整字典使用示例

```vue
<!-- 字典选择器与标签显示完整示例 -->
<template>
  <view>
    <!-- 选择器：使用 useDict 返回的数组 -->
    <wd-picker
      v-model="form.gender"
      label="性别"
      :columns="sys_user_gender"
      :loading="dictLoading"
    />

    <!-- 显示标签：使用 useDictStore -->
    <wd-cell title="性别" :value="genderLabel" />

    <!-- 列表中显示 -->
    <wd-cell
      v-for="item in list"
      :key="item.id"
      :title="item.name"
    >
      <wd-tag :type="item.status === '1' ? 'success' : 'danger'">
        {{ dictStore.getDictLabel('sys_enable_status', item.status) }}
      </wd-tag>
    </wd-cell>
  </view>
</template>

<script setup lang="ts">
// useDict 和 useDictStore 都是自动导入的

// 获取字典数组（用于选择器）
const { sys_user_gender, dictLoading } = useDict(DictTypes.sys_user_gender)

// 获取字典 Store（用于获取标签）
const dictStore = useDictStore()

const form = reactive({
  gender: ''
})

// 计算属性：获取性别标签
const genderLabel = computed(() => {
  return dictStore.getDictLabel('sys_user_gender', form.gender)
})
</script>
```

---

## 滚动管理（useScroll）

### 核心功能

```typescript
const {
  scrollTop,           // 只读的滚动位置
  updateScrollTop,     // 更新滚动位置
  resetScrollTop,      // 重置到顶部
  scrollToTop,         // 滚动到顶部（带动画）
  isScrolled,          // 是否已滚动
  shouldShowBacktop,   // 是否显示返回顶部按钮
  createScrollViewHandler,  // scroll-view 处理器
  createPageScrollHandler   // 页面滚动处理器
} = useScroll()
```

### 在页面中使用

```vue
<!-- 页面滚动与返回顶部示例 -->
<template>
  <view>
    <!-- 内容区域 -->
    <view class="content">...</view>

    <!-- 返回顶部按钮 -->
    <wd-backtop :scroll-top="scrollTop" :top="600" @click="scrollToTop" />
  </view>
</template>

<script setup lang="ts">
const { scrollTop, updateScrollTop, resetScrollTop, scrollToTop } = useScroll()

// 页面显示时重置
onShow(() => {
  resetScrollTop()
})

// 监听页面滚动
onPageScroll((e) => {
  updateScrollTop(e.scrollTop)
})
</script>
```

### 在 scroll-view 中使用

```vue
<!-- scroll-view 中使用滚动管理示例 -->
<template>
  <scroll-view
    scroll-y
    :scroll-top="scrollTopValue"
    @scroll="handleScroll"
  >
    <view class="content">...</view>
  </scroll-view>

  <wd-backtop :scroll-top="scrollTop" @click="scrollToTop" />
</template>

<script setup lang="ts">
const { scrollTop, scrollToTop, createScrollViewHandler } = useScroll()
const { scrollTopValue, handleScroll } = createScrollViewHandler()
</script>
```

---

## 支付功能（usePayment）

### 完整 API

```typescript
const {
  loading,              // 支付中状态（readonly）
  availableMethods,     // 当前平台可用的支付方式（readonly）
  createOrderAndPay,    // 创建订单并支付
  payOrder,             // 支付已有订单
  queryOrderStatus,     // 查询订单状态
  pollOrderStatus,      // 轮询订单状态
  getTradeType,         // 获取交易类型
  fetchAvailableMethods,// 获取可用支付方式
  getPlatformInfo       // 获取平台信息
} = usePayment()
```

### 创建订单并支付

```typescript
import { useToast } from '@/wd'
import { PaymentMethod } from '@/api/common/mall/order/orderTypes'

const toast = useToast()
const { createOrderAndPay, loading } = usePayment()

const handlePay = async () => {
  const [err, result] = await createOrderAndPay({
    orderData: {
      goodsId: goods.value.id,
      skuId: selectedSku.value.id,
      quantity: quantity.value
    },
    paymentMethod: PaymentMethod.WECHAT,
    // tradeType 会根据平台自动选择
  })

  if (!err) {
    toast.success('支付成功')
    uni.redirectTo({ url: '/pages/order/result?status=success' })
  } else {
    toast.error(err.message || '支付失败')
  }
}
```

### 支付已有订单

```typescript
const { payOrder } = usePayment()

const handlePayOrder = async (orderNo: string) => {
  const [err, result] = await payOrder({
    orderNo,
    paymentMethod: PaymentMethod.WECHAT
  })

  if (!err) {
    toast.success('支付成功')
  }
}
```

### 平台支付方式判断

```typescript
const { availableMethods, getPlatformInfo } = usePayment()

// 获取平台信息
const platformInfo = getPlatformInfo()
console.log(platformInfo)
// {
//   platform: 'mp-weixin',
//   supportsWechatPay: true,
//   supportsAlipayPay: false,
//   supportsBalancePay: true,
//   isWechatEnvironment: true,
//   recommendedTradeTypes: { wechat: 'JSAPI', alipay: 'APP' }
// }

// 可用的支付方式
console.log(availableMethods.value)  // ['WECHAT', 'BALANCE']
```

---

## 分享配置

### 小程序分享

```typescript
const { setShareConfig } = useShare()

onLoad(() => {
  setShareConfig({
    title: '分享标题',
    path: `/pages/detail/detail?id=${id}`,
    imageUrl: goods.value.image
  })
})
```

### 公众号 H5 分享

```typescript
const { initSdk, setShare, isReady } = useWxShare()

onMounted(async () => {
  await initSdk()
  if (isReady.value) {
    setShare({
      title: '分享标题',
      desc: '分享描述',
      link: location.href,
      imgUrl: goods.value.image
    })
  }
})
```

---

## 创建新 Store

### 标准模板

```typescript
// stores/modules/xxx.ts
// ✅ defineStore、ref、computed 已自动导入

export const useXxxStore = defineStore('xxx', () => {
  // ========== 状态 ==========
  const data = ref<XxxVo | null>(null)
  const list = ref<XxxVo[]>([])
  const loading = ref(false)

  // ========== 计算属性 ==========
  const hasData = computed(() => !!data.value)
  const isEmpty = computed(() => list.value.length === 0)

  // ========== 方法 ==========

  /**
   * 加载数据
   */
  const loadData = async (id: number) => {
    loading.value = true
    try {
      const [err, res] = await http.get<XxxVo>(`/api/xxx/${id}`)
      if (!err) {
        data.value = res
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置
   */
  const reset = () => {
    data.value = null
    list.value = []
    loading.value = false
  }

  return {
    data,
    list,
    loading,
    hasData,
    isEmpty,
    loadData,
    reset
  }
})
```

---

## 创建新 Composable

### 标准模板

```typescript
// composables/useXxx.ts

export const useXxx = () => {
  // ========== 状态 ==========
  const loading = ref(false)
  const data = ref<XxxData | null>(null)
  const error = ref<string | null>(null)

  // ========== 方法 ==========

  /**
   * 执行操作
   */
  const execute = async (params: XxxParams) => {
    loading.value = true
    error.value = null

    const [err, res] = await http.post<XxxData>('/api/xxx', params)

    if (err) {
      error.value = err.message
      return false
    }

    data.value = res
    loading.value = false
    return true
  }

  /**
   * 重置
   */
  const reset = () => {
    data.value = null
    error.value = null
  }

  // ========== 返回 ==========
  return {
    loading: readonly(loading),
    data: readonly(data),
    error: readonly(error),
    execute,
    reset
  }
}
```

---

## 持久化存储

> ⚠️ **注意**：本项目**不使用** pinia-plugin-persist 插件，而是通过 **cache 工具**实现持久化。

### 使用 cache 工具（推荐）

```typescript
import { cache } from '@/utils/cache'

// ========== 基础用法 ==========

// 存储（任意类型，自动序列化）
cache.set('theme', 'dark')                     // 字符串
cache.set('count', 42)                         // 数字
cache.set('isActive', true)                    // 布尔值
cache.set('userInfo', { id: 1, name: 'admin' }) // 对象
cache.set('tags', ['frontend', 'mobile'])      // 数组

// 设置过期时间（秒）
cache.set('token', 'abc123', 7 * 24 * 3600)   // 7天过期
cache.set('tempData', { temp: true }, 3600)   // 1小时过期

// 读取（保持原始类型）
const theme = cache.get<string>('theme')       // "dark" (string)
const count = cache.get<number>('count')       // 42 (number)
const userInfo = cache.get<UserInfo>('userInfo') // { id: 1, name: 'admin' }

// 删除
cache.remove('userToken')

// 检查是否存在
if (cache.has('userToken')) {
  // 用户已登录
}

// 清除所有应用缓存
cache.clearAll()

// 手动清理过期数据
cache.cleanup()

// 获取缓存统计
const stats = cache.getStats()
// { totalKeys, appKeys, currentSize, limitSize, usagePercent }
```

### cache 工具特点

| 特性 | 说明 |
|------|------|
| **自动前缀** | 添加应用前缀，防止多应用冲突 |
| **类型保持** | 存取时保持原始数据类型（对象、数组、数字等） |
| **过期支持** | 支持设置过期时间（秒为单位） |
| **自动清理** | 应用启动时自动清理过期数据 |
| **泛型支持** | 完整的 TypeScript 泛型支持 |

### 典型示例：useToken

```typescript
// composables/useToken.ts（实际项目代码）
import { cache } from '@/utils/cache'

export const useToken = () => {
  const TOKEN_KEY = 'token'

  // 获取 token
  const getToken = (): string | null => {
    return cache.get(TOKEN_KEY)
  }

  // 设置 token（7天过期）
  const setToken = (accessToken: string): void => {
    cache.set(TOKEN_KEY, accessToken, 7 * 24 * 3600)
  }

  // 移除 token
  const removeToken = (): void => {
    cache.remove(TOKEN_KEY)
  }

  return { getToken, setToken, removeToken }
}
```

### 在 Store 中使用 cache

```typescript
// stores/modules/settings.ts
import { cache } from '@/utils/cache'

const SETTINGS_KEY = 'app-settings'

export const useSettingsStore = defineStore('settings', () => {
  // 从缓存初始化
  const settings = ref(cache.get<Settings>(SETTINGS_KEY) || {
    theme: 'light',
    fontSize: 14
  })

  // 保存设置
  const saveSettings = (newSettings: Partial<Settings>) => {
    Object.assign(settings.value, newSettings)
    cache.set(SETTINGS_KEY, settings.value)  // 持久化
  }

  return { settings, saveSettings }
})
```

### 原生 uni.storage（不推荐）

```typescript
// ⚠️ 不推荐直接使用，优先使用 cache 工具
// 存储
uni.setStorageSync('key', value)

// 读取
const value = uni.getStorageSync('key')

// 删除
uni.removeStorageSync('key')
```

---

## 常用模式

### 1. 列表 + 分页

```typescript
const pageNum = ref(1)
const pageSize = 10
const list = ref<ItemVo[]>([])
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

const loadList = async (refresh = false) => {
  if (refresh) {
    pageNum.value = 1
    finished.value = false
  }

  if (finished.value) return

  loading.value = true
  const [err, data] = await http.get<PageResult<ItemVo>>('/api/items', {
    pageNum: pageNum.value,
    pageSize
  })

  if (!err) {
    if (refresh) {
      list.value = data.records
    } else {
      list.value.push(...data.records)
    }
    total.value = data.total
    finished.value = list.value.length >= total.value
  }

  loading.value = false
}

// 下拉刷新
onPullDownRefresh(async () => {
  await loadList(true)
  uni.stopPullDownRefresh()
})

// 上拉加载
onReachBottom(() => {
  if (!finished.value && !loading.value) {
    pageNum.value++
    loadList()
  }
})

onLoad(() => {
  loadList()
})
```

### 2. 页面间传值

```typescript
// 方式1：URL 参数（推荐简单数据）
uni.navigateTo({
  url: `/pages/detail/detail?id=${id}&type=${type}`
})

// 接收
onLoad((options) => {
  const { id, type } = options
})

// 方式2：事件总线（复杂数据）
const eventBus = useEventBus()
eventBus.emit('orderCreated', orderData)

// 接收
eventBus.on('orderCreated', (data) => {
  // 处理
})

// 方式3：Store（需要持久化或多页面共享）
const orderStore = useOrderStore()
orderStore.setCurrentOrder(order)
```

---

## 最佳实践

### 1. HTTP 请求

```typescript
// ✅ 使用 [err, data] 格式
const [err, data] = await http.get<UserVo>('/api/user')
if (!err) {
  userInfo.value = data
}

// ✅ 链式配置
const [err] = await http.noAuth().encrypt().post('/api/login', data)

// ❌ 不要使用 try-catch
try {
  const data = await http.get('/api/user')  // 错误！
} catch (e) {}
```

### 2. Store vs Composable 选择

```typescript
// ✅ 全局状态 → Store
const userStore = useUserStore()
const dictStore = useDictStore()

// ✅ 功能封装 → Composable
const { pay } = usePayment()
const { scrollTop } = useScroll()

// ✅ 页面状态 → 本地 ref/reactive
const form = reactive({ name: '', phone: '' })
const list = ref<Item[]>([])
```

### 3. 避免在 Composable 中直接使用 Store

```typescript
// ❌ 不推荐：Composable 依赖 Store
export const useXxx = () => {
  const userStore = useUserStore()  // 紧耦合
  // ...
}

// ✅ 推荐：通过参数传入
export const useXxx = (userId?: number) => {
  // ...
}

// 使用时
const userStore = useUserStore()
const { xxx } = useXxx(userStore.userId)
```

### 4. 认证检查

```typescript
// ✅ 权限检查用 useAuth
const { hasPermission } = useAuth()
if (hasPermission('system:user:add')) {
  // ...
}

// ✅ 登录状态和用户信息用 useUserStore
const userStore = useUserStore()
if (userStore.isLoggedIn) {
  console.log(userStore.userInfo?.nickName)
}
```

### 5. Store 内 Composable 调用位置

> **🔴 强制规则**：在 `defineStore(() => {...})` 的 setup 函数内，所有 composable（`useXxx()`）**必须在顶层调用一次**，把返回值挂到本地常量上；**禁止**在 `computed` / `watch` / 方法体内重复调用同一个 composable。

**正确示例**（参考框架的 `user.ts`）：

```typescript
export const useUserStore = defineStore(USER_MODULE, () => {
  // ✅ setup 顶层调用一次，复用 token 工具实例
  const tokenUtils = useToken()
  const token = ref(tokenUtils.getToken())

  // ✅ computed 内只引用，不再次调用 composable
  const isLoggedIn = computed(() => token.value.length > 0)

  return { token, isLoggedIn }
})
```

**错误示例**：

```typescript
// ❌ 在 computed 内部调用 composable
export const useTabbarStore = defineStore('tabbar', () => {
  const visibleIndices = computed(() => {
    const { currentRole } = useMinorRole()  // 每次求值都重新调用！
    return tabbarVisibleByRole[currentRole.value]
  })
  return { visibleIndices }
})

// ❌ 在方法/事件处理函数里调用 composable
const onClick = () => {
  const { hasPermission } = useAuth()  // 不该在这里调用
  if (hasPermission('xxx')) { ... }
}
```

**为什么禁止**：

1. **响应式追踪混乱** — composable 内若用 `ref()/reactive()` 创建本地状态，每次调用都会创建新的响应式对象，外部 `watch` / 模板拿不到稳定引用
2. **副作用累积** — composable 内的 `watch` / `onMounted` 注册会随每次 computed 求值线性增长，造成内存泄漏与重复请求
3. **Pinia 跨 Store 依赖陷阱** — 在 computed 内调用 `useOtherStore()` 会绕过 Pinia 的初始化顺序检查，可能拿到未初始化的状态

**正确的修复模式**：

```typescript
export const useTabbarStore = defineStore('tabbar', () => {
  // ✅ setup 顶层调用一次
  const { currentRole } = useMinorRole()

  // ✅ computed 只引用 currentRole.value，不重新调用 composable
  const visibleIndices = computed(() => tabbarVisibleByRole[currentRole.value])

  return { visibleIndices }
})
```

---

## 参考文件

- Store 目录：`plus-uniapp/src/stores/modules/`
- Composables 目录：`plus-uniapp/src/composables/`
- 用户 Store：`plus-uniapp/src/stores/modules/user.ts`
- 字典 Composable：`plus-uniapp/src/composables/useDict.ts`
- HTTP 请求：`plus-uniapp/src/composables/useHttp.ts`
- Token 管理：`plus-uniapp/src/composables/useToken.ts`
- 滚动管理：`plus-uniapp/src/composables/useScroll.ts`
- 应用初始化：`plus-uniapp/src/composables/useAppInit.ts`
- 支付功能：`plus-uniapp/src/composables/usePayment.ts`
- **缓存工具**：`plus-uniapp/src/utils/cache.ts`
