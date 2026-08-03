---
name: store-pc
description: |
  PC 端（plus-ui）状态管理与 Composables 指南。包含 Pinia Store、Composables 组合式函数的创建和使用规范。

  触发场景：
  - 在 PC 后台创建/使用 Store
  - Pinia 状态管理
  - 跨组件数据共享（PC端）
  - 持久化存储（localCache/sessionCache）
  - HTTP 请求链式调用
  - 权限判断
  - 字典数据加载

  触发词：PC Store、Pinia、defineStore、useUserStore、useDictStore、PC状态管理、useHttp、http、链式调用、useAuth、useToken、useDict、useTableHeight、localCache、sessionCache、缓存、持久化

  适用目录：plus-ui/**
---

# PC 端状态管理指南

> **适用于**: `plus-ui/` 目录下的 PC 后台管理系统

## 自动导入说明

以下内容**无需手动 import**：

```typescript
// Pinia
defineStore, storeToRefs

// Vue APIs
ref, reactive, computed, watch, watchEffect
onMounted, onUnmounted, onBeforeMount, onBeforeUnmount
nextTick, toRefs, toRef, unref, isRef
provide, inject, defineProps, defineEmits, defineExpose

// Vue Router
useRouter, useRoute

// HTTP 请求（全局可用）
http           // useHttp 的默认实例
Result         // API 返回类型
PageResult     // 分页返回类型
PageQuery      // 分页查询参数
```

**需要手动 import 的**：

```typescript
// Stores
import { useUserStore } from '@/stores/modules/user'
import { useDictStore } from '@/stores/modules/dict'

// Composables
import { useToken } from '@/composables/useToken'
import { useAuth } from '@/composables/useAuth'
import { useDict, DictTypes } from '@/composables/useDict'
import { useTableHeight } from '@/composables/useTableHeight'

// 缓存工具
import { localCache, sessionCache } from '@/utils/cache'
```

---

## 已有 Store 清单

| Store | 文件 | 用途 | 关键方法 |
|-------|------|------|---------|
| `useUserStore` | `stores/modules/user.ts` | 用户认证、权限 | loginUser, logoutUser, fetchUserInfo, updateAvatar |
| `useDictStore` | `stores/modules/dict.ts` | 字典数据缓存 | loadDict, getDictLabel |
| `useFeatureStore` | `stores/modules/feature.ts` | 功能开关 | initFeatures, canUseOpenApi |
| `usePermissionStore` | `stores/modules/permission.ts` | 路由权限 | generateRoutes |
| `useNoticeStore` | `stores/modules/notice.ts` | 通知消息 | - |
| `useAiChatStore` | `stores/modules/aiChat.ts` | AI 聊天 | sendMessage, createSession |

---

## Composables 清单

> Composables 是可复用的组合式函数，封装了特定业务逻辑。

### 核心 Composables

| Composable | 文件 | 用途 | 关键方法/属性 |
|------------|------|------|--------------|
| `useHttp` / `http` | `composables/useHttp.ts` | HTTP 请求（支持链式调用） | get, post, put, del, noAuth, encrypt |
| `useToken` | `composables/useToken.ts` | Token 管理 | getToken, setToken, removeToken |
| `useAuth` | `composables/useAuth.ts` | 权限判断 | hasPermission, hasRole, isLoggedIn |
| `useDict` | `composables/useDict.ts` | 字典数据加载 | DictTypes 枚举，返回响应式字典数组 |
| `useTableHeight` | `composables/useTableHeight.ts` | 表格高度自适应 | tableHeight, queryFormRef |
| `useDialog` | `composables/useDialog.ts` | 弹窗控制 | visible, open, close |
| `useSelection` | `composables/useSelection.ts` | 表格选择 | selectionItems, handleSelectionChange |
| `useDownload` | `composables/useDownload.ts` | 文件下载 | download |
| `usePrint` | `composables/usePrint.ts` | 打印功能 | print |
| `useI18n` | `composables/useI18n.ts` | 国际化 | t |
| `useTheme` | `composables/useTheme.ts` | 主题切换 | toggleTheme |
| `useWS` | `composables/useWS.ts` | WebSocket | connect, send, close |
| `useSSE` | `composables/useSSE.ts` | SSE 推送 | connect, onMessage |
| `useAiChat` | `composables/useAiChat.ts` | AI 聊天 | sendMessage |
| `useLayout` | `composables/useLayout.ts` | 布局控制 | - |
| `useAnimation` | `composables/useAnimation.ts` | 动画效果 | - |

### useHttp 链式调用

```typescript
import { http } from '@/composables/useHttp'

// 基础用法
const [err, data] = await http.get('/api/xxx')
const [err, data] = await http.post('/api/xxx', body)
const [err, data] = await http.put('/api/xxx', body)
const [err, data] = await http.del('/api/xxx')

// ✅ 链式调用（按需组合）
const [err, data] = await http
  .noAuth()            // 不携带 token
  .encrypt()           // 加密请求体
  .noRepeatSubmit()    // 防重复提交
  .noTenant()          // 不携带租户ID
  .noMsgError()        // 不显示错误消息
  .timeout(30000)      // 自定义超时（毫秒）
  .post('/api/xxx', body)
```

### useAuth 权限判断

```typescript
import { useAuth } from '@/composables/useAuth'

const auth = useAuth()

// 权限检查
if (auth.hasPermission('system:user:add')) { ... }
if (auth.hasRole('admin')) { ... }
if (auth.isLoggedIn()) { ... }
if (auth.isSuperAdmin()) { ... }
if (auth.isTenantAdmin()) { ... }

// 路由权限
if (auth.canAccessRoute(route)) { ... }
const routes = auth.filterAuthorizedRoutes(allRoutes)
```

### useDict 字典加载

```typescript
import { useDict, DictTypes } from '@/composables/useDict'

// 加载字典（返回响应式数组）
const { sys_enable_status, sys_user_gender } = useDict(
  DictTypes.sys_enable_status,
  DictTypes.sys_user_gender
)

// 在模板中使用
<AFormSelect v-model="form.status" :options="sys_enable_status" />
<DictTag :options="sys_user_gender" :value="row.gender" />
```

### useTableHeight 表格高度

```typescript
import { useTableHeight } from '@/composables/useTableHeight'

// 自动计算表格高度，适应容器
const { tableHeight, queryFormRef, showSearch, calculateTableHeight } = useTableHeight()

// 模板中使用
<ASearchForm ref="queryFormRef" v-show="showSearch">
  ...
</ASearchForm>
<el-table :height="tableHeight">
  ...
</el-table>
```

---

## 创建新 Store

### 标准模板

```typescript
// stores/modules/xxx.ts
// ✅ defineStore、ref、computed 已自动导入，无需手动 import

export const useXxxStore = defineStore('xxx', () => {
  // ========== 状态 ==========
  const list = ref<XxxVo[]>([])
  const loading = ref(false)
  const current = ref<XxxVo | null>(null)

  // ========== 计算属性 ==========
  const count = computed(() => list.value.length)
  const isEmpty = computed(() => list.value.length === 0)
  const hasSelected = computed(() => current.value !== null)

  // ========== 方法 ==========

  /**
   * 获取列表
   */
  const fetchList = async (params?: XxxQuery) => {
    loading.value = true
    try {
      const [err, data] = await pageXxxs(params)
      if (!err) {
        list.value = data.records
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置当前选中项
   */
  const setCurrent = (item: XxxVo | null) => {
    current.value = item
  }

  /**
   * 重置状态
   */
  const reset = () => {
    list.value = []
    current.value = null
    loading.value = false
  }

  // ========== 返回 ==========
  return {
    // 状态
    list,
    loading,
    current,
    // 计算属性
    count,
    isEmpty,
    hasSelected,
    // 方法
    fetchList,
    setCurrent,
    reset
  }
})
```

---

## 使用 Store

### 基本用法

```typescript
import { useXxxStore } from '@/stores/modules/xxx'

const xxxStore = useXxxStore()

// 访问状态（非响应式）
console.log(xxxStore.list)
console.log(xxxStore.count)

// 调用方法
await xxxStore.fetchList()
xxxStore.setCurrent(item)
xxxStore.reset()
```

### 响应式解构

```typescript
import { useXxxStore } from '@/stores/modules/xxx'

const xxxStore = useXxxStore()

// ✅ 正确：使用 storeToRefs 解构状态
const { list, loading, current } = storeToRefs(xxxStore)

// ✅ 正确：方法直接解构（方法不需要响应式）
const { fetchList, setCurrent, reset } = xxxStore

// ❌ 错误：直接解构状态会丢失响应式
// const { list, loading } = xxxStore
```

### 在模板中使用

```vue
<!-- Store 数据在模板中的使用示例 -->
<template>
  <div v-loading="xxxStore.loading">
    <div v-for="item in xxxStore.list" :key="item.id">
      {{ item.name }}
    </div>
    <div v-if="xxxStore.isEmpty">暂无数据</div>
  </div>
</template>

<script setup lang="ts">
import { useXxxStore } from '@/stores/modules/xxx'

const xxxStore = useXxxStore()

onMounted(() => {
  xxxStore.fetchList()
})
</script>
```

---

## 持久化存储

> ⚠️ **重要**：本项目**不使用** pinia-plugin-persist 插件，而是通过 `localCache` / `sessionCache` 工具实现持久化。

### 缓存工具说明

```typescript
// 位置：@/utils/cache
import { localCache, sessionCache } from '@/utils/cache'

// localCache - 基于 localStorage，数据持久保存
// sessionCache - 基于 sessionStorage，关闭浏览器后清除
```

### 缓存工具 API

```typescript
// 设置缓存（支持过期时间）
localCache.set('key', value)                    // 永久保存
localCache.set('key', value, 3600)              // 1小时后过期（秒）

// 获取缓存
const value = localCache.get<T>('key')          // 返回 T | null

// 删除缓存
localCache.remove('key')

// 检查是否存在
const exists = localCache.has('key')

// 清空所有缓存
localCache.clear()

// 获取所有 key
const keys = localCache.keys()
```

### 在 Store 中使用缓存

```typescript
// stores/modules/xxx.ts
import { localCache } from '@/utils/cache'

const CACHE_KEY = 'xxx_data'

export const useXxxStore = defineStore('xxx', () => {
  // 初始化时从缓存读取
  const data = ref(localCache.get<XxxData>(CACHE_KEY) || defaultValue)

  // 保存到缓存
  const saveToCache = () => {
    localCache.set(CACHE_KEY, data.value)
  }

  // 监听变化自动保存（可选）
  watch(data, saveToCache, { deep: true })

  // 清除缓存
  const clearCache = () => {
    localCache.remove(CACHE_KEY)
    data.value = defaultValue
  }

  return { data, saveToCache, clearCache }
})
```

### useToken 实现示例

```typescript
// composables/useToken.ts
import { localCache } from '@/utils/cache'

export const useToken = () => {
  const TOKEN_KEY = 'token'

  const getToken = (): string | null => localCache.get(TOKEN_KEY)

  const setToken = (accessToken: string, expireSeconds?: number): void => {
    localCache.set(TOKEN_KEY, accessToken, expireSeconds)
  }

  const removeToken = (): void => localCache.remove(TOKEN_KEY)

  return { getToken, setToken, removeToken }
}
```

---

## 常用模式

### 1. 列表 + 详情模式

```typescript
export const useXxxStore = defineStore('xxx', () => {
  // ========== 列表相关 ==========
  const list = ref<XxxVo[]>([])
  const listLoading = ref(false)
  const listParams = reactive<XxxQuery>({
    pageNum: 1,
    pageSize: 10,
    keyword: ''
  })
  const total = ref(0)

  // ========== 详情相关 ==========
  const detail = ref<XxxVo | null>(null)
  const detailLoading = ref(false)

  // ========== 列表方法 ==========
  const loadList = async (params?: Partial<XxxQuery>) => {
    if (params) {
      Object.assign(listParams, params)
    }
    listLoading.value = true
    try {
      const [err, data] = await pageXxxs(listParams)
      if (!err) {
        list.value = data.records
        total.value = data.total
      }
    } finally {
      listLoading.value = false
    }
  }

  const resetList = () => {
    listParams.pageNum = 1
    listParams.keyword = ''
    loadList()
  }

  // ========== 详情方法 ==========
  const loadDetail = async (id: number) => {
    detailLoading.value = true
    try {
      const [err, data] = await getXxx(id)
      if (!err) {
        detail.value = data
      }
    } finally {
      detailLoading.value = false
    }
  }

  const clearDetail = () => {
    detail.value = null
  }

  return {
    // 列表
    list, listLoading, listParams, total,
    loadList, resetList,
    // 详情
    detail, detailLoading,
    loadDetail, clearDetail
  }
})
```

### 2. 字典缓存模式

```typescript
export const useDictStore = defineStore('dict', () => {
  // 缓存 Map
  const cache = ref<Map<string, DictData[]>>(new Map())
  const loading = ref<Set<string>>(new Set())

  /**
   * 获取字典（带缓存）
   */
  const getDict = async (dictType: string): Promise<DictData[]> => {
    // 已缓存，直接返回
    if (cache.value.has(dictType)) {
      return cache.value.get(dictType)!
    }

    // 正在加载，等待
    if (loading.value.has(dictType)) {
      return new Promise((resolve) => {
        const check = setInterval(() => {
          if (cache.value.has(dictType)) {
            clearInterval(check)
            resolve(cache.value.get(dictType)!)
          }
        }, 50)
      })
    }

    // 发起请求
    loading.value.add(dictType)
    try {
      const [err, data] = await fetchDictData(dictType)
      if (!err) {
        cache.value.set(dictType, data)
        return data
      }
      return []
    } finally {
      loading.value.delete(dictType)
    }
  }

  /**
   * 获取字典标签
   */
  const getDictLabel = (dictType: string, value: string): string => {
    const items = cache.value.get(dictType) || []
    const item = items.find(i => i.value === value)
    return item?.label || value
  }

  /**
   * 清空缓存
   */
  const clearCache = (dictType?: string) => {
    if (dictType) {
      cache.value.delete(dictType)
    } else {
      cache.value.clear()
    }
  }

  return {
    cache,
    getDict,
    getDictLabel,
    clearCache
  }
})
```

### 3. 用户认证模式

```typescript
// stores/modules/user.ts
import { useToken } from '@/composables/useToken'

export const useUserStore = defineStore('user', () => {
  // ✅ 使用 useToken 管理 token 持久化
  const tokenUtils = useToken()
  const token = ref(tokenUtils.getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.nickName || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  /**
   * 登录
   */
  const loginUser = async (loginForm: LoginForm) => {
    const [err, data] = await loginApi(loginForm)
    if (!err) {
      token.value = data.access_token
      tokenUtils.setToken(data.access_token, data.expire_in)  // ✅ 持久化到 localCache
      await fetchUserInfo()
    }
    return [err, data]
  }

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async () => {
    const [err, data] = await getUserInfoApi()
    if (!err) {
      userInfo.value = data.user
      roles.value = data.roles
      permissions.value = data.permissions
    }
    return [err, data]
  }

  /**
   * 登出
   */
  const logoutUser = async () => {
    await logoutApi()
    reset()
  }

  /**
   * 重置状态
   */
  const reset = () => {
    token.value = ''
    tokenUtils.removeToken()  // ✅ 清除 localCache
    userInfo.value = null
    roles.value = []
    permissions.value = []
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    userName,
    avatar,
    loginUser,      // ✅ 方法名修正
    fetchUserInfo,  // ✅ 方法名修正
    logoutUser,     // ✅ 方法名修正
    reset
  }
})  // ✅ 无需 persist 配置，通过 useToken 实现持久化
```

---

## 最佳实践

### 1. 何时使用 Store

| 场景 | 是否使用 Store |
|------|---------------|
| 多个页面/组件共享的数据 | ✅ 使用 |
| 用户登录状态、权限 | ✅ 使用 |
| 全局配置、主题设置 | ✅ 使用 |
| 字典数据缓存 | ✅ 使用 |
| 页面内部状态 | ❌ 用 ref/reactive |
| 组件内部状态 | ❌ 用 ref/reactive |
| 父子组件传值 | ❌ 用 props/emit |
| 兄弟组件通信（简单） | ❌ 用 provide/inject |

### 2. Store 命名规范

```typescript
// ✅ 正确：use + 业务名 + Store
export const useUserStore = defineStore('user', ...)
export const useCartStore = defineStore('cart', ...)
export const useDictStore = defineStore('dict', ...)

// ❌ 错误：不规范的命名
export const userStore = defineStore('user', ...)
export const useUser = defineStore('user', ...)
```

### 3. 避免循环依赖

```typescript
// ❌ 避免 Store 之间循环引用
// userStore.ts
import { useOrderStore } from './order'  // orderStore 又引用 userStore

// ✅ 正确做法：在组件层协调，或使用事件
```

### 4. 重置 Store（登出时）

```typescript
// 在 useUserStore 中
const logout = async () => {
  // 重置所有相关 Store
  const dictStore = useDictStore()
  const permissionStore = usePermissionStore()

  reset()
  dictStore.clearCache()
  permissionStore.reset()

  // 跳转登录页
  router.push('/login')
}
```

---

## 与组件配合使用

### 在列表页使用

```vue
<script setup lang="ts">
import { useXxxStore } from '@/stores/modules/xxx'

const xxxStore = useXxxStore()
const { list, listLoading, total } = storeToRefs(xxxStore)

// 查询参数（本地管理，不放 Store）
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const getList = () => {
  xxxStore.loadList(queryParams)
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

onMounted(() => {
  getList()
})
</script>
```

---

## 参考文件

### Stores

- Store 目录：`plus-ui/src/stores/modules/`
- 用户 Store：`plus-ui/src/stores/modules/user.ts`
- 字典 Store：`plus-ui/src/stores/modules/dict.ts`
- 功能开关 Store：`plus-ui/src/stores/modules/feature.ts`

### Composables

- Composables 目录：`plus-ui/src/composables/`
- HTTP 请求：`plus-ui/src/composables/useHttp.ts`（链式调用）
- Token 管理：`plus-ui/src/composables/useToken.ts`
- 权限判断：`plus-ui/src/composables/useAuth.ts`
- 字典加载：`plus-ui/src/composables/useDict.ts`
- 表格高度：`plus-ui/src/composables/useTableHeight.ts`

### 工具类

- 缓存工具：`plus-ui/src/utils/cache.ts`（localCache / sessionCache）
