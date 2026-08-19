---
name: ui-pc
description: |
  前端（plus-ui）组件库完整指南。包含 71 个自定义组件、17 个 Composables、18 个工具模块。

  触发场景：
  - 开发前端后台管理页面
  - 使用 AForm*、AModal、ADetail、ACard*、AChart*、AAi* 等组件
  - 使用 Element Plus 组件
  - 表格、表单、弹窗、图表、卡片等前端 UI
  - 使用 Composables（useDict、useTableHeight 等）
  - 使用 Utils（format、crypto 等）

  触发词：el-、AForm、AModal、ADetail、ASearchForm、ACard、AChart、AAi、TableToolbar、Pagination、DictTag、前端组件、后台页面、管理端、useDict、useTableHeight、useI18n、国际化、i18n、t()

  适用目录：plus-ui/**
---

# 前端组件库完整指南

> **适用于**: `plus-ui/` 目录下的前端后台管理页面开发

---

## 🔴 强制工具使用规则（最高优先级）

> **在编写任何前端代码之前，必须遵守以下工具使用规则！**

### 禁止 → 替代 映射表

| 场景 | ❌ 禁止写法 | ✅ 必须使用 | 导入语句 |
|------|-----------|-----------|---------|
| 成功提示 | `ElMessage.success()` | `showMsgSuccess()` | `import { showMsgSuccess } from '@/utils/modal'` |
| 错误提示 | `ElMessage.error()` | `showMsgError()` | `import { showMsgError } from '@/utils/modal'` |
| 警告提示 | `ElMessage.warning()` | `showMsgWarning()` | `import { showMsgWarning } from '@/utils/modal'` |
| 确认框 | `ElMessageBox.confirm()` | `showConfirm()` | `import { showConfirm } from '@/utils/modal'` |
| 警告弹窗 | `ElMessageBox.alert()` | `showAlert()` | `import { showAlert } from '@/utils/modal'` |
| 输入弹窗 | `ElMessageBox.prompt()` | `showPrompt()` | `import { showPrompt } from '@/utils/modal'` |
| 加载遮罩 | `ElLoading.service()` | `showLoading()` / `hideLoading()` | `import { showLoading, hideLoading } from '@/utils/modal'` |
| 通知 | `ElNotification.success()` | `showNotifySuccess()` | `import { showNotifySuccess } from '@/utils/modal'` |
| 表单验证 | `formRef.validate()` | `toValidate(formRef)` | `import { toValidate } from '@/utils/to'` |
| API 调用 | `try { await api() } catch` | `const [err, data] = await api()` | 无需导入 |
| 布尔判断 | `status === '1'` | `isTrue(status)` | `import { isTrue } from '@/utils/boolean'` |
| 状态切换 | `status = status === '1' ? '0' : '1'` | `toggleStatus(status)` | `import { toggleStatus } from '@/utils/boolean'` |
| 日期范围 | 手动拼接 params | `addDateRange(params, range, field)` | `import { addDateRange } from '@/utils/date'` |
| 文件导出 | 手写 download 逻辑 | `useDownload().exportExcel()` | 自动导入 |

### 场景代码模板

#### 1. 删除操作（必须复制此模板）

```typescript
import { showConfirm, showMsgSuccess } from '@/utils/modal'

/** 删除操作 */
const handleDelete = async (row?: XxxVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map(item => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.name || row.id : selectionItems.value.map(item => item.name || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteXxxs(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}
```

#### 2. 表单提交（必须复制此模板）

```typescript
import { toValidate } from '@/utils/to'
import { showMsgSuccess } from '@/utils/modal'

/** 提交表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(formRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.id) {
    ;[err] = await updateXxx(form.value)
  } else {
    ;[err] = await addXxx(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}
```

#### 3. 状态切换（必须复制此模板）

```typescript
import { isTrue, toggleStatus } from '@/utils/boolean'
import { showConfirm, showMsgSuccess } from '@/utils/modal'

/** 状态切换 */
const handleStatusChange = async (row: XxxVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.name || row.id}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)  // 回滚状态
    return
  }
  const [updateErr] = await updateXxx(row)
  if (!updateErr) {
    await getList()
    showMsgSuccess(`${text}${t('成功')}`)
  } else {
    row.status = toggleStatus(row.status)  // 回滚状态
  }
}
```

#### 4. 查看详情（必须复制此模板）

```typescript
/** 查看详情 */
const handleView = async (row: XxxVo) => {
  const [err, data] = await getXxx(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('xxx', 'XXX详情')}`
    viewDialog.value.visible = true
  }
}
```

#### 5. 数据导出（必须复制此模板）

```typescript
/** 导出数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Xxx Data', 'XXX数据'), '/xxx/exportXxxs', queryParams.value)
}
```

---

## 核心原则

> **必须优先使用项目自定义组件**（AForm*、ASearchForm、AModal、ACard*、AChart* 等），而不是直接使用 Element Plus 原生组件。

---

## 组件总览（71 个组件）

### 表单组件族 (AForm*) - 13 个

| 组件 | 用途 | 必须使用场景 |
|------|------|-------------|
| `AFormInput` | 输入框 | **所有文本/数字/密码输入** |
| `AFormSelect` | 下拉选择 | **所有选择框** |
| `AFormDate` | 日期选择 | **所有日期/时间选择** |
| `AFormSwitch` | 开关 | **所有状态切换** |
| `AFormRadio` | 单选 | **所有单选场景** |
| `AFormCheckbox` | 多选 | **所有多选场景** |
| `AFormTreeSelect` | 树形选择 | **部门/分类选择** |
| `AFormCascader` | 级联选择 | **地区/多级分类选择** |
| `AFormImgUpload` | 图片上传 | **所有图片上传** |
| `AFormFileUpload` | 文件上传 | **所有文件上传** |
| `AFormEditor` | 富文本编辑器 | **所有富文本内容** |
| `AFormMap` | 地图选点 | **地址/坐标选择** |
| `AFormInputWithAi` | AI 增强输入框 | **需要 AI 辅助的输入** |

### 卡片组件族 (ACard*) - 22 个

| 组件 | 用途 | 适用场景 |
|------|------|---------|
| `AStatsCard` | 统计卡片 | KPI 展示、带趋势/进度条 |
| `ALineStatsCard` | 折线统计卡片 | 带迷你折线图的统计 |
| `ABarStatsCard` | 柱状统计卡片 | 带迷你柱状图的统计 |
| `ALineChartCard` | 折线图卡片 | 趋势数据展示 |
| `ABarChartCard` | 柱状图卡片 | 对比数据展示 |
| `APieChartCard` | 饼图卡片 | 占比数据展示 |
| `ARadarChartCard` | 雷达图卡片 | 多维数据展示 |
| `AMapChartCard` | 地图卡片 | 地理数据展示 |
| `ADataCard` | 数据卡片 | 通用数据展示 |
| `ADataListCard` | 数据列表卡片 | 列表形式数据 |
| `AFormCard` | 表单卡片 | 表单容器 |
| `ATableCard` | 表格卡片 | 表格容器 |
| `AInfoCard` | 信息卡片 | 详情信息展示 |
| `AImageCard` | 图片卡片 | 图片展示 |
| `AUserCard` | 用户卡片 | 用户信息展示 |
| `AProfileCard` | 资料卡片 | 个人资料展示 |
| `APricingCard` | 价格卡片 | 套餐/价格展示 |
| `ASocialCard` | 社交卡片 | 社交信息展示 |
| `AActivityCard` | 活动卡片 | 活动/动态展示 |
| `ATimelineListCard` | 时间线卡片 | 时间线展示 |
| `AWeatherCard` | 天气卡片 | 天气信息展示 |
| `ANotificationCard` | 通知卡片 | 通知消息展示 |
| `AEmptyCard` | 空状态卡片 | 无数据展示 |

### 图表组件族 (AChart*) - 10 个

| 组件 | 底层 | 用途 |
|------|------|------|
| `AChart` | ECharts | 基础图表容器 |
| `ALineChart` | ECharts | 折线图 |
| `ABarChart` | ECharts | 柱状图 |
| `APieChart` | ECharts | 饼图 |
| `ARadarChart` | ECharts | 雷达图 |
| `AScatterChart` | ECharts | 散点图 |
| `ABarBidirectionalChart` | ECharts | 双向柱状图 |
| `ABarHorizontalChart` | ECharts | 水平柱状图 |
| `ACandlestickChart` | ECharts | K线图 |
| `AMapChart` | ECharts + GeoJSON | 地理地图 |

### AI 组件族 (AAi*) - 5 个

| 组件 | 用途 |
|------|------|
| `AAiAssistant` | AI 助手面板 |
| `AAiTextOptimizer` | 文本优化建议 |
| `AAiDataGenerator` | 智能数据生成 |
| `AAiContentReviewer` | 内容审核 |
| `AFormInputWithAi` | 输入框集成 AI |

### 主题组件 (ATheme*) - 4 个

| 组件 | 用途 |
|------|------|
| `AThemeSvg` | SVG 主题化 |
| `AGeometricBackground` | 几何背景 |
| `AWatermark` | 水印组件 |
| `AThemeColorPicker` | 颜色选择器 |

### 布局与容器组件 - 10 个

| 组件 | 用途 | 必须使用场景 |
|------|------|-------------|
| `ASearchForm` | 搜索表单容器 | **所有列表页的搜索区域** |
| `AModal` | 弹窗/抽屉 | **所有弹窗（Dialog/Drawer）** |
| `ADetail` | 详情展示 | **所有详情查看** |
| `TableToolbar` | 表格工具栏 | **所有列表页工具栏** |
| `Pagination` | 分页组件 | **所有分页场景** |
| `AResizablePanels` | 可调整面板 | 分栏布局 |
| `ATableColumnSettings` | 表格列设置 | 自定义表格列 |
| `ASelectionTags` | 选择标签组 | 多选标签展示 |
| `AImportExcel` | Excel导入 | **数据导入功能** |
| `AOssMediaManager` | OSS媒体管理器 | 媒体资源管理 |

### 展示组件 - 7 个

| 组件 | 用途 |
|------|------|
| `DictTag` | 字典标签 |
| `Icon` | 图标组件 |
| `IconSelect` | 图标选择器 |
| `ImagePreview` | 图片预览 |
| `UserSelect` | 用户选择器 |
| `ARecharge` | 充值组件 |
| `IFrameContainer` | IFrame 容器 |

---

## Composables 完整列表（17 个）

| Hook | 用途 | 关键返回值/方法 |
|------|------|----------------|
| `useDict` | 字典数据获取 | `{ sys_enable_status, dictLoading }` |
| `useTableHeight` | 表格高度计算 | `{ tableHeight, queryFormRef, showSearch }` |
| `useAuth` | 认证管理 | `login, logout, getUser, checkPermission` |
| `useToken` | Token 管理 | `getToken, setToken, removeToken` |
| `useDownload` | 文件下载 | `download, downloadUrl, downloadBlob` |
| `usePrint` | 打印功能 | `print, printElement` |
| `useI18n` | 国际化（增强版） | `t`, `currentLanguage`, `isChinese`, `setLanguage` |
| `useTheme` | 主题管理 | `setTheme, toggleDarkMode` |
| `useLayout` | 布局状态 | `sidebarOpened, device, toggleSidebar` |
| `useWS` | WebSocket | `connect, send, disconnect, onMessage` |
| `useSSE` | Server-Sent Events | `connect, close, onData` |
| `useHttp` | HTTP 请求 | `get, post, put, delete, upload` |
| `useAiChat` | AI 聊天 | `sendMessage, getHistory, clearHistory` |
| `useAnimation` | 动画管理 | `searchAnimate, transitionAnimate` |
| `useDialog` | 对话框管理 | `openDialog, closeDialog` |
| `useSelection` | 选择管理 | `selectedItems, toggleSelect, clearSelect` |
| `useResponsiveSpan` | 响应式栅格 | `computedSpan, shouldUseCol` |

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
//   { label: '启用', value: '1', tagType: 'success', ... },
//   { label: '停用', value: '0', tagType: 'danger', ... }
// ]
```

### useTableHeight 详细用法

```typescript
// 自动计算表格高度（响应式）
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// tableHeight: 计算后的表格高度
// queryFormRef: 搜索表单 ref（用于重置）
// showSearch: 搜索区域显示状态
```

### useI18n 用法（增强版国际化，已自动导入）

```typescript
// useI18n 已自动导入，直接使用即可
const { t, isChinese, setLanguage } = useI18n()

// t() 常用方式：
t('button.add')                    // 1. 传统 i18n 键
t('User Name', '用户名')           // 2. 双参数（英文, 中文）- 最常用
t('', { field: 'Name', comment: '名称' })  // 3. 对象参数
```

> 更多用法参考源码：`plus-ui/src/composables/useI18n.ts`

---

## Utils 工具函数库（18 个模块）

### format.ts - 格式化工具（30+ 函数）

```typescript
import {
  formatNumber,      // 数字千位分隔：1234567 → 1,234,567
  formatCurrency,    // 货币格式：1234.5 → ¥1,234.50
  formatAmount,      // 金额格式化
  formatPercent,     // 百分比：0.125 → 12.5%
  formatFileSize,    // 文件大小：1024 → 1 KB
  formatDistance,    // 距离格式化
  formatDuration,    // 时长：3661000 → 1小时1分钟1秒
  formatPrivacy,     // 通用脱敏
  formatIDCard,      // 身份证脱敏：110***********1234
  formatPhone,       // 电话脱敏：138****8888
  formatBankCard,    // 银行卡脱敏
  formatIP,          // IP 脱敏
  formatStringLength,// 字符串截断
  formatFileName,    // 文件名格式化
  formatURL,         // URL 格式化
  formatBoolean,     // 布尔值格式化
  formatStatusColor  // 状态颜色
} from '@/utils/format'
```

### modal.ts - 弹窗提示工具（高频使用）

> **⚠️ 必须使用此模块，禁止直接使用 ElMessage/ElMessageBox**

```typescript
import {
  // 消息提示（同步）
  showMsg,           // 一般信息
  showMsgSuccess,    // ✅ 成功提示（最常用）
  showMsgError,      // ❌ 错误提示
  showMsgWarning,    // ⚠️ 警告提示

  // 弹窗提示（异步，返回 [err, data]）
  showAlert,         // 警告弹窗
  showAlertSuccess,  // 成功弹窗
  showAlertError,    // 错误弹窗
  showAlertWarning,  // 警告弹窗

  // 通知（右上角）
  showNotify,        // 一般通知
  showNotifySuccess, // 成功通知
  showNotifyError,   // 错误通知
  showNotifyWarning, // 警告通知

  // 交互弹窗（异步，返回 [err, data]）
  showConfirm,       // ✅ 确认框（最常用：删除、状态切换）
  showPrompt,        // 输入框

  // 加载遮罩
  showLoading,       // 显示加载
  hideLoading        // 隐藏加载
} from '@/utils/modal'

// 使用示例
const [confirmErr] = await showConfirm('确定删除?')
if (confirmErr) return  // 用户取消

showMsgSuccess('操作成功')
```

### to.ts - 异步处理工具（高频使用）

> **⚠️ 必须使用 [err, data] 格式，禁止 try-catch**

```typescript
import {
  to,              // 基础 Promise 包装
  toValidate,      // ✅ 表单验证（最常用）
  toAll,           // 并行请求
  toWithTimeout,   // 带超时
  toWithRetry,     // 带重试
  toWithDefault,   // 带默认值
  toSequence,      // 串行执行
  toSync,          // 同步函数包装
  toWithLog        // 带日志（调试用）
} from '@/utils/to'

// 表单验证
const [validateErr] = await toValidate(formRef)
if (validateErr) return

// API 调用（已自动返回 [err, data] 格式）
const [err, data] = await pageXxxs(params)
if (!err) {
  dataList.value = data.records
}
```

### boolean.ts - 布尔值工具

```typescript
import {
  isTrue,        // ✅ 判断是否为真（'1', 1, true, 'true', 'yes'）
  isFalse,       // 判断是否为假
  toggleStatus   // ✅ 切换状态 '1' <-> '0'
} from '@/utils/boolean'

// 状态判断
const text = isTrue(row.status) ? '启用' : '停用'

// 状态回滚
row.status = toggleStatus(row.status)
```

### date.ts - 日期工具

```typescript
import {
  addDateRange,    // ✅ 添加日期范围到查询参数（最常用）
  formatDate,      // 格式化日期
  parseDate,       // 解析日期
  isBefore,        // 日期比较
  isAfter          // 日期比较
} from '@/utils/date'

// 日期范围查询（自动添加 beginXxx 和 endXxx 参数）
addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
// 结果: { ...params, beginCreateTime: 'xxx', endCreateTime: 'xxx' }
```

### 其他工具模块

| 模块 | 主要函数 | 用途 |
|------|---------|------|
| `crypto.ts` | `encrypt, decrypt, md5, sha256` | 加密/解密 |
| `tree.ts` | `buildTree, flatTree, getTreePaths` | 树形结构处理 |
| `cache.ts` | `localCache, sessionCache` | 本地/会话存储 |
| `object.ts` | `deepClone, merge, pick, omit` | 对象操作 |
| `validators.ts` | `validateEmail, validatePhone, validateUrl` | 验证器 |
| `colors.ts` | `rgb2hex, hex2rgb, lighten, darken` | 颜色处理 |
| `string.ts` | `camelCase, kebabCase, capitalize` | 字符串处理 |
| `class.ts` | `addClass, removeClass, hasClass` | 类名操作 |
| `scroll.ts` | `scrollToTop, scrollToElement` | 滚动处理 |
| `function.ts` | `debounce, throttle, once` | 函数工具 |
| `tab.ts` | `openTab, getActiveTab, closeTab` | 标签管理 |
| `rsa.ts` | `rsaEncrypt, rsaDecrypt` | RSA 加密 |
| `themeAnimation.ts` | `animateThemeChange` | 主题切换动画 |

---

## Store 状态管理（6 个模块）

| Store | 位置 | 职责 |
|-------|------|------|
| `useUserStore` | `stores/modules/user.ts` | 用户信息、登录状态 |
| `usePermissionStore` | `stores/modules/permission.ts` | 权限路由管理 |
| `useDictStore` | `stores/modules/dict.ts` | 字典数据缓存 |
| `useAiChatStore` | `stores/modules/aiChat.ts` | AI 聊天记录 |
| `useNoticeStore` | `stores/modules/notice.ts` | 通知消息 |
| `useFeatureStore` | `stores/modules/feature.ts` | 功能特性开关 |

```typescript
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()
userStore.user       // 用户信息
userStore.roles      // 角色列表
userStore.permissions // 权限列表
userStore.login()    // 登录
userStore.logout()   // 登出
```

---

## 响应式 span 系统

AForm* 组件支持多种 span 配置方式：

```vue
<!-- 1. 固定数字 -->
<AFormInput label="名称" v-model="form.name" :span="12" />

<!-- 2. 字符串数字（自动转换） -->
<AFormInput label="名称" v-model="form.name" span="12" />

<!-- 3. 响应式对象 -->
<AFormInput
  label="名称"
  v-model="form.name"
  :span="{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }"
/>

<!-- 4. 预设值 "auto"（智能响应式） -->
<AFormInput label="名称" v-model="form.name" span="auto" />
```

**响应式断点：**
- `xs` (< 576px) - 手机竖屏
- `sm` (≥ 576px) - 手机横屏
- `md` (≥ 768px) - 平板
- `lg` (≥ 992px) - 小屏电脑
- `xl` (≥ 1200px) - 大屏电脑
- `xxl` (≥ 1400px) - 超大屏

---

## 标准列表页结构

### 1. 搜索区域 - ASearchForm

```vue
<!-- 搜索表单与查询逻辑示例 -->
<template>
  <!-- ✅ 正确：使用 ASearchForm + AForm* 组件 -->
  <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
    <AFormInput
      label="模糊搜索"
      prop="searchValue"
      v-model="queryParams.searchValue"
      @input="handleQuery"
    />
    <AFormSelect
      label="状态"
      v-model="queryParams.status"
      prop="status"
      :options="sys_enable_status"
      @change="handleQuery"
    />
    <AFormDate
      v-model="dateRangeCreateTime"
      prop="createTime"
      type="daterange"
      label="创建时间"
      @change="handleQuery"
    />
  </ASearchForm>

  <!-- ❌ 错误：直接使用 el-form + el-input -->
</template>

<script setup lang="ts">
const { tableHeight, queryFormRef, showSearch } = useTableHeight()
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  searchValue: undefined,
  status: undefined
})

const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}
</script>
```

### 2. 工具栏区域 - TableToolbar

```vue
<el-card shadow="hover">
  <template #header>
    <el-row :gutter="10" class="mb-2">
      <!-- 操作按钮 -->
      <el-col :span="1.5" v-permi="['xxx:add']">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">
          {{ t('新增') }}
        </el-button>
      </el-col>
      <el-col :span="1.5" v-permi="['xxx:update']">
        <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
          {{ t('修改') }}
        </el-button>
      </el-col>
      <el-col :span="1.5" v-permi="['xxx:delete']">
        <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
          {{ t('删除') }}
        </el-button>
      </el-col>

      <!-- 导入组件 -->
      <el-col :span="1.5" v-permi="['xxx:import']">
        <AImportExcel
          v-slot="{ openImportExcel }"
          title="数据名称"
          templateUrl="/xxx/template"
          importUrl="/xxx/import"
          @import-success="getList"
        >
          <el-button type="info" plain icon="Top" @click="openImportExcel">
            {{ t('导入') }}
          </el-button>
        </AImportExcel>
      </el-col>

      <el-col :span="1.5" v-permi="['xxx:export']">
        <el-button type="warning" plain icon="Download" @click="handleExport">
          {{ t('导出') }}
        </el-button>
      </el-col>

      <!-- ✅ 工具栏组件 -->
      <TableToolbar
        v-model:showSearch="showSearch"
        @reset-query="resetQuery"
        @query-table="getList"
        :table-columns="detailFields"
        :table-data="dataList"
      />
    </el-row>
  </template>
</el-card>
```

### 3. 表格区域

```vue
<el-table
  ref="tableRef"
  v-loading="isLoading"
  :data="dataList"
  :height="tableHeight"
  stripe
  @selection-change="handleSelectionChange"
>
  <el-table-column type="selection" width="50" align="center" />
  <el-table-column :label="t('名称')" prop="name" align="center" />

  <!-- 图片列 -->
  <el-table-column :label="t('图片')" prop="img" align="center" width="80">
    <template #default="{ row }">
      <ImagePreview :src="row.img" />
    </template>
  </el-table-column>

  <!-- 字典状态列 + 开关 -->
  <el-table-column :label="t('状态')" prop="status" align="center">
    <template #default="{ row }">
      <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
    </template>
  </el-table-column>

  <!-- 操作列（只有图标，无文字） -->
  <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
    <template #default="{ row }">
      <el-tooltip :content="t('查看')" placement="top">
        <el-button v-permi="['xxx:query']" link type="primary" icon="View" @click="handleView(row)" />
      </el-tooltip>
      <el-tooltip :content="t('修改')" placement="top">
        <el-button v-permi="['xxx:update']" link type="success" icon="Edit" @click="handleUpdate(row)" />
      </el-tooltip>
      <el-tooltip :content="t('删除')" placement="top">
        <el-button v-permi="['xxx:delete']" link type="danger" icon="Delete" @click="handleDelete(row)" />
      </el-tooltip>
    </template>
  </el-table-column>
</el-table>

<!-- ✅ 分页组件 -->
<Pagination
  v-model:page="queryParams.pageNum"
  v-model:limit="queryParams.pageSize"
  :total="total"
  @pagination="getList"
/>
```

### 4. 弹窗表单 - AModal + AForm*

```vue
<!-- ✅ 正确：使用 AModal -->
<AModal
  v-model="dialog.visible"
  :title="dialog.title"
  :loading="buttonLoading"
  @confirm="submitForm"
  @cancel="cancel"
>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="auto">
    <el-row :gutter="10">
      <!-- ✅ 使用 AForm* 系列组件 -->
      <AFormInput label="名称" v-model="form.name" prop="name" span="auto" />
      <AFormInput label="描述" v-model="form.description" prop="description" type="textarea" span="auto" />
      <AFormSelect label="类型" v-model="form.type" prop="type" :options="typeOptions" span="auto" />
      <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto" />
      <AFormImgUpload label="图片" v-model="form.img" prop="img" span="auto" />
      <AFormDate label="日期" v-model="form.date" prop="date" type="date" span="auto" />
    </el-row>
  </el-form>
</AModal>
```

### 5. 详情弹窗 - ADetail

```vue
<ADetail
  v-model="viewDialog.visible"
  :title="viewDialog.title"
  :data="viewData"
  :fields="detailFields"
/>

<script setup lang="ts">
const viewDialog = ref<DialogState>({ visible: false, title: '' })
const viewData = ref({})

// 详情字段配置
const detailFields = ref<FieldConfig[]>([
  { prop: 'id', label: '主键' },
  { prop: 'name', label: '名称' },
  { prop: 'img', label: '图片', type: 'image' },
  { prop: 'status', label: '状态', type: 'dict', dictOptions: sys_enable_status },
  { prop: 'createTime', label: '创建时间', type: 'datetime' },
  { prop: 'remark', label: '备注' }
])

const handleView = async (row: DataVo) => {
  const [err, data] = await getData(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}详情`
    viewDialog.value.visible = true
  }
}
</script>
```

---

## AModal 完整用法

```vue
<!-- 对话框模式（默认） -->
<AModal v-model="visible" title="标题" mode="dialog" size="medium">
  内容
</AModal>

<!-- 抽屉模式 -->
<AModal v-model="visible" title="标题" mode="drawer" direction="rtl" size="large">
  内容
</AModal>

<!-- 尺寸选项：'small' | 'medium' | 'large' | 'xl' -->

<!-- 可拖动对话框 -->
<AModal v-model="visible" title="拖动我" :movable="true">
  内容
</AModal>

<!-- 全屏对话框 -->
<AModal v-model="visible" title="全屏" :fullscreen="true">
  内容
</AModal>

<!-- 只有关闭按钮（详情查看） -->
<AModal v-model="visible" title="详情" :show-footer="true" footer-type="close-only">
  内容
</AModal>

<!-- 关闭前回调（阻止关闭） -->
<AModal v-model="visible" title="确认" :before-close="handleBeforeClose">
  内容
</AModal>
```

---

## 卡片组件示例

### AStatsCard - 统计卡片

```vue
<el-row :gutter="20">
  <!-- 基础统计 -->
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="总用户数" :value="8520" icon="user" />
  </el-col>

  <!-- 带趋势指标 -->
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="活跃用户"
      :value="1234"
      icon="hot"
      :trend="{ value: 12.5, isUp: true }"
    />
  </el-col>

  <!-- 带进度条 -->
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="月度目标"
      :value="85000"
      :target="100000"
      unit="元"
      :show-progress="true"
    />
  </el-col>
</el-row>
```

### 图表卡片组合

```vue
<el-row :gutter="20">
  <el-col :span="12">
    <ALineChartCard
      title="销售趋势"
      :data="salesData"
      :x-axis="dateList"
    />
  </el-col>
  <el-col :span="12">
    <APieChartCard
      title="分类占比"
      :data="categoryData"
    />
  </el-col>
</el-row>
```

---

## API 层规范

### 文件结构

```
api/business/base/xxx/
├── xxxApi.ts          # API 调用定义
├── xxxTypes.ts        # 类型定义
```

### xxxApi.ts 示例

```typescript
// http/Result/PageResult/PageQuery 已自动导入
import type { XxxQuery, XxxBo, XxxVo } from './xxxTypes'

export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get('/base/xxx/pageXxxs', query)
}

export const getXxx = (id: string | number): Result<XxxVo> => {
  return http.get(`/base/xxx/getXxx/${id}`)
}

export const addXxx = (data: XxxBo): Result<string | number> => {
  return http.post('/base/xxx/addXxx', data)
}

export const updateXxx = (data: XxxBo): Result<void> => {
  return http.put('/base/xxx/updateXxx', data)
}

export const deleteXxxs = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del(`/base/xxx/deleteXxxs/${ids}`)
}
```

### xxxTypes.ts 示例

```typescript
export interface XxxQuery extends PageQuery {
  xxxName?: string
  status?: string
}

export interface XxxBo {
  id?: string | number
  xxxName?: string
  status?: string
}

export interface XxxVo {
  id: string | number
  xxxName: string
  status: string
  createTime: string
}
```

### API 调用规范

```typescript
// ✅ 正确：使用 [err, data] 格式
const [err, data] = await pageXxxs(queryParams.value)
if (!err) {
  dataList.value = data.records
}

// ❌ 错误：使用 try-catch
try {
  const data = await pageXxxs(queryParams.value)
} catch (error) { ... }
```

---

## 自动导入说明

以下内容**无需手动 import**：

```typescript
// Vue APIs
ref, reactive, computed, watch, watchEffect
onMounted, onUnmounted, nextTick, toRefs

// Pinia
defineStore, storeToRefs

// Element Plus
ElMessage, ElMessageBox, ElNotification

// 工具
dayjs
http  // HTTP 请求工具

// 类型
Result, PageResult, PageQuery, DialogState, FieldConfig

// 项目组件（全局注册）
Icon, DictTag, Pagination, ASearchForm, AModal, ADetail, TableToolbar, ImagePreview, AImportExcel
AFormInput, AFormSelect, AFormDate, AFormSwitch, AFormRadio, AFormCheckbox
AFormTreeSelect, AFormCascader, AFormImgUpload, AFormFileUpload, AFormEditor
AStatsCard, ALineChart, ABarChart, APieChart, ARadarChart, AMapChart
AAiAssistant, AAiTextOptimizer, AAiDataGenerator, AAiContentReviewer

// Composables（自动导入）
useDict, DictTypes, useTableHeight, useDownload, usePrint, useAuth, useToken, useI18n
```

---

## Vue 文件命名规范

> **禁止使用 `index.vue` 作为业务页面文件名**（首页/仪表盘除外）

| 文件类型 | 命名规则 | 示例 |
|---------|---------|------|
| **业务页面** | 小写业务名.vue | `ad.vue`, `goods.vue`, `feedback.vue` |
| **树表页面** | 业务名Tree.vue | `categoryTree.vue` |
| **子表组件** | 大驼峰Child.vue | `OrderItemChild.vue`, `DictDataChild.vue` |
| **辅助组件** | 大驼峰.vue | `SkuSpecEditor.vue`, `AssignUsers.vue` |
| **公共组件** | A前缀大驼峰.vue | `AFormInput.vue`, `AModal.vue` |

**目录结构**：`views/{模块}/{业务名}/{业务名}.vue`（如 `views/business/base/ad/ad.vue`）

---

## 快速开发检查清单

在开发新页面前，必须：

- [ ] 阅读 `plus-ui/src/views/business/base/ad/ad.vue` 页面实现
- [ ] 查阅 `plus-ui/src/api/business/base/ad/adApi.ts` 的 API 规范
- [ ] 了解项目的 AForm* 组件用法
- [ ] 理解 ASearchForm 和 AModal 的使用模式
- [ ] 检查对应的字典类型（useDict）
- [ ] 验证权限指令（v-permi）的使用
- [ ] 确认 API 返回值格式 `[err, data]`
- [ ] 确保使用项目封装的组件，不使用原生 Element Plus 组件
- [ ] 添加国际化支持（使用 `useI18n` 的 `t()` 函数，禁止直接使用 vue-i18n）
- [ ] 实现响应式布局（使用 span 配置）

---

## 参考文件位置

| 用途 | 路径 |
|------|------|
| 标准 CRUD 页面 | `plus-ui/src/views/business/base/ad/ad.vue` |
| API 定义规范 | `plus-ui/src/api/business/base/ad/adApi.ts` |
| 类型定义规范 | `plus-ui/src/api/business/base/ad/adTypes.ts` |
| 表单输入组件 | `plus-ui/src/components/AForm/AFormInput.vue` |
| 搜索表单组件 | `plus-ui/src/components/ASearchForm/ASearchForm.vue` |
| 模态框组件 | `plus-ui/src/components/AModal/AModal.vue` |
| 卡片组件 | `plus-ui/src/components/ACard/` |
| 图表组件 | `plus-ui/src/components/AChart/` |
| AI 组件 | `plus-ui/src/components/AAi/` |
| 字典 Hooks | `plus-ui/src/composables/useDict.ts` |
| 国际化 Hooks | `plus-ui/src/composables/useI18n.ts` |
| 格式化工具 | `plus-ui/src/utils/format.ts` |
| 布局管理 | `plus-ui/src/composables/useLayout.ts` |
| 用户 Store | `plus-ui/src/stores/modules/user.ts` |
