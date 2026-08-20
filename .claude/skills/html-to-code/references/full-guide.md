
# HTML 设计稿转框架代码指南

## 概述

本技能用于将 HTML/Tailwind CSS 设计稿（通常由 MCP 工具或 AI 生成）转换为符合 ruoyi-plus-uniapp 框架规范的代码。支持两种目标端：

- **PC 端**（plus-ui）：Element Plus 封装组件 + UnoCSS
- **移动端**（plus-uniapp / plus-app）：WD UI 组件 + UnoCSS

支持两种转换粒度：

- **整页转换**：将完整 HTML 页面转为 Vue SFC
- **区块/组件转换**：只转换 HTML 中的某个局部区域

---

## 转换工作流

```
用户提供 HTML 设计稿（文件路径或代码片段）
    ↓
步骤 1：读取并分析 HTML 结构
    ├── 识别页面类型（列表页/详情页/表单页/仪表盘等）
    ├── 识别目标端（PC / 移动端）
    ├── 识别转换粒度（整页 / 区块）
    └── 提取设计系统（主色、圆角、间距）
    ↓
步骤 2：元素映射
    ├── HTML 元素 → 框架组件
    ├── Lucide 图标 → 项目图标
    ├── Tailwind 类 → UnoCSS 类（大部分兼容，少量需调整）
    └── 自定义 CSS → scoped SCSS
    ↓
步骤 3：生成框架代码
    ├── Vue SFC 结构（template + script setup + style）
    ├── 注入数据层（ref、API、路由、事件）
    ├── 应用框架规范（首行注释、组件导入、[err,data] 模式）
    └── 规范检查（禁止项验证）
    ↓
步骤 4：输出
    ├── .vue 文件（页面/组件）
    ├── API 文件（如需要）
    └── Types 文件（如需要）
```

---

## 步骤 1：分析 HTML 设计稿

### 1.1 识别设计稿特征

HTML 设计稿通常具有以下特征：

```html
<!-- 移动端：375×812 手机框架 -->
<meta name="viewport" content="width=375, ...">
<div class="phone-frame"> ... </div>

<!-- PC端：1440px 宽屏布局 -->
<meta name="viewport" content="width=device-width, ...">
<body class="bg-gray-50 font-sans"> ... </body>

<!-- 共同特征 -->
<script src="https://cdn.tailwindcss.com"></script>
<script src="https://unpkg.com/lucide@latest/dist/umd/lucide.min.js"></script>
```

### 1.2 目标端判断规则

| 设计稿特征 | 目标端 | 说明 |
|-----------|-------|------|
| `width=375`、`.phone-frame` | 移动端 | 手机模拟器 |
| `width=device-width`、`1440px` | PC 端 | 桌面布局 |
| 有底部 Tab 栏 | 移动端 | 典型移动端导航 |
| 有侧边栏菜单 + 顶部导航 | PC 端 | 后台管理布局 |
| 用户明确指定 | 按用户指定 | 最高优先级 |

### 1.3 页面类型识别

| 类型 | 特征 | PC端参考 | 移动端参考 |
|------|------|---------|-----------|
| **列表页** | 表格/卡片列表 + 搜索 + 分页 | `ad.vue` 模式 | `Home.vue` 模式 |
| **详情页** | 大图 + 信息区 + 操作栏 | `ADetail` 组件 | 自定义页面 |
| **表单页** | 输入框 + 选择器 + 提交按钮 | `AModal` 弹窗 | `login.vue` 模式 |
| **仪表盘** | 统计卡片 + 图表 + 快捷入口 | `AChart*` 组件 | 自定义页面 |
| **商品/内容展示** | 网格/瀑布流 + 卡片 | 自定义页面 | `wd-paging` 列表 |

---

## 步骤 2：元素映射

### 2.1 HTML → PC 端组件映射

#### 布局组件

| HTML 元素 | 转换为 | 说明 |
|-----------|-------|------|
| `<div class="flex ...">` 行容器 | `<el-row :gutter="10">` | 栅格行 |
| `<div class="w-1/2 ...">` 列容器 | `<el-col :span="12">` | 栅格列 |
| `<div class="rounded-xl border shadow">` 卡片 | `<el-card shadow="hover">` | 卡片容器 |
| `<div class="grid grid-cols-N">` 网格 | `<el-row>` + 多个 `<el-col>` | 栅格布局 |
| `<header>` / `<nav>` | 框架自带布局 | 不需转换 |
| `<aside>` 侧栏 | 框架自带侧栏 | 不需转换 |

#### 表单组件

| HTML 元素 | 转换为 | 禁止使用 |
|-----------|-------|---------|
| `<input type="text">` | `<AFormInput>` | ~~`<el-input>`~~ |
| `<select>` / `<div>` 下拉 | `<AFormSelect>` | ~~`<el-select>`~~ |
| `<input type="date">` | `<AFormDate>` | ~~`<el-date-picker>`~~ |
| `<input type="checkbox">` 开关 | `<AFormSwitch>` | ~~`<el-switch>`~~ |
| `<input type="radio">` | `<AFormRadio>` | ~~`<el-radio>`~~ |
| `<textarea>` | `<AFormInput type="textarea">` | ~~`<el-input type="textarea">`~~ |
| `<input type="file">` 图片 | `<AFormImgUpload>` | ~~`<el-upload>`~~ |
| `<form>` 搜索区域 | `<ASearchForm>` | ~~`<el-form inline>`~~ |
| `<dialog>` / `.modal` | `<AModal>` | ~~`<el-dialog>`~~ |

#### 数据展示

| HTML 元素 | 转换为 | 说明 |
|-----------|-------|------|
| `<table>` | `<el-table>` + `<el-table-column>` | 数据表格 |
| `<span class="bg-green">` 状态标签 | `<el-tag>` + 字典 | 带字典映射 |
| `<div class="flex gap">` 分页 | `<Pagination>` | 项目封装分页 |
| `<span class="absolute">` 徽标 | `<el-badge>` | 数字徽标 |
| `<div>` Tab 切换 | `<el-tabs>` | 标签页 |
| `<img>` 图片 | `<el-image>` | 带预览 |

#### 操作组件

| HTML 元素 | 转换为 | 说明 |
|-----------|-------|------|
| `<button>` 主按钮 | `<el-button type="primary">` | 主操作 |
| `<button>` 危险按钮 | `<el-button type="danger">` | 删除等 |
| `<a>` 文字链接按钮 | `<el-button link type="primary">` | 表格内操作 |
| `<button>` 工具栏按钮 | `<el-button plain icon="Plus">` | 带图标 |

### 2.2 HTML → 移动端组件映射

#### 布局组件

| HTML 元素 | 转换为 | 说明 |
|-----------|-------|------|
| `<div>` | `<view>` | **所有 div 转为 view** |
| `<span>` | `<text>` 或 `<view>` | 行内文本 |
| `<img>` | `<wd-img>` 或 `<image>` | 图片 |
| `<header>` 导航栏 | `<wd-navbar>` | 顶部导航 |
| `<div class="fixed bottom">` 底栏 | `<view class="fixed bottom-0">` + `pb-safe` | 安全区适配 |
| `<div class="overflow-y-auto">` | `<scroll-view scroll-y>` | 滚动容器 |
| `<div class="phone-frame">` | 删除 | 手机模拟器壳，不需要 |
| `.status-bar` | 删除 | 系统自带状态栏 |
| `.safe-area-bottom` | `pb-safe` | UnoCSS 自定义规则 |
| `.home-indicator` | 删除 | 系统自带 |

#### 表单组件

| HTML 元素 | 转换为 | 禁止使用 |
|-----------|-------|---------|
| `<form>` | `<wd-form :model :rules>` | ~~`<uni-forms>`~~ |
| `<input type="text">` | `<wd-input>` | ~~`<uni-field>`~~ |
| `<select>` | `<wd-select>` 或 `<wd-picker>` | ~~`<uni-data-select>`~~ |
| `<input type="date">` | `<wd-datetimepicker>` | - |
| `<textarea>` | `<wd-textarea>` | - |
| `<input type="number">` 加减器 | `<wd-input-number>` | - |
| `<input type="radio">` | `<wd-radio-group>` | - |
| `<input type="checkbox">` | `<wd-checkbox-group>` | - |
| `<input type="search">` | `<wd-search>` | - |

#### 数据展示

| HTML 元素 | 转换为 | 说明 |
|-----------|-------|------|
| 卡片列表（分页） | `<wd-paging :fetch :params>` | 分页列表 |
| `<div class="swiper">` 轮播 | `<wd-swiper>` | 轮播图 |
| `<div class="flex step">` 步骤 | `<wd-steps>` | 订单进度 |
| `<div class="tabs">` | `<wd-tabs>` | 标签页 |
| `<div class="sidebar">` | `<wd-sidebar>` | 侧边分类栏 |
| `<span>` 标签 | `<wd-tag>` | 状态/分类标签 |
| `<div>` 星级评分 | `<wd-rate>` | 评分 |
| 空状态提示 | `<wd-status-tip>` | 无数据 |

#### 操作组件

| HTML 元素 | 转换为 | 说明 |
|-----------|-------|------|
| `<button>` | `<wd-button>` | 按钮 |
| `<button class="rounded-full">` | `<wd-button round>` | 圆角按钮 |
| `alert()` / `confirm()` | `useMessage().confirm()` | 确认弹窗 |
| Toast 提示 | `useToast().success()` | 消息提示 |
| 弹出层 | `<wd-popup>` | 底部/中间弹窗 |
| 下拉刷新 | `<wd-pull-refresh>` | 列表刷新 |

### 2.3 图标映射（Lucide → 项目图标）

设计稿使用 Lucide Icons，需要映射到项目图标体系。

**移动端**：使用 `<wd-icon name="xxx" />` + 项目 iconfont（font_4969054）

| Lucide 图标 | wd-icon name | 说明 |
|-------------|-------------|------|
| `arrow-left` | `arrow-left` | 返回 |
| `search` | `search` | 搜索 |
| `shopping-cart` | `cart` | 购物车 |
| `user` | `user` | 用户 |
| `home` | `home` | 首页 |
| `heart` | `heart` | 收藏 |
| `star` | `star` | 评分/收藏 |
| `plus` | `add` | 添加 |
| `minus` | `minus` | 减少 |
| `check` | `check` | 勾选 |
| `chevron-right` | `arrow-right` | 右箭头 |
| `clock` | `clock` | 时间 |
| `map-pin` | `location` | 定位 |
| `share-2` | `share` | 分享 |
| `bell` | `notification` | 通知 |
| `trash-2` | `delete` | 删除 |

> **找不到对应图标时**：查阅 icon-management 技能中的完整图标列表，或使用 UnoCSS 图标类 `i-lucide-{name}`。

**PC 端**：使用 Element Plus 内置图标或 UnoCSS Iconify

| Lucide 图标 | Element Plus Icon | UnoCSS 类 |
|-------------|------------------|-----------|
| `search` | `Search` | `i-ep-search` |
| `plus` | `Plus` | `i-ep-plus` |
| `edit` / `pencil` | `Edit` | `i-ep-edit` |
| `trash-2` | `Delete` | `i-ep-delete` |
| `arrow-left` | `ArrowLeft` | `i-ep-arrow-left` |
| `chevron-right` | `ArrowRight` | `i-ep-arrow-right` |
| `user` | `User` | `i-ep-user` |
| `settings` | `Setting` | `i-ep-setting` |
| `download` | `Download` | `i-ep-download` |
| `upload` | `Upload` | `i-ep-upload` |

### 2.4 样式转换规则

#### Tailwind → UnoCSS 兼容性

UnoCSS 兼容绝大部分 Tailwind 类名，**大部分可直接保留**。需要注意的差异：

| Tailwind 写法 | UnoCSS 写法 | 说明 |
|---------------|------------|------|
| `class="hover:bg-blue-500 hover:text-white"` | `class="hover:(bg-blue-500 text-white)"` | 变体组（推荐） |
| `bg-[#FF6B35]` | `bg-[#FF6B35]` | 任意值，兼容 |
| `w-[220px]` | `w-[220px]` | 任意值，兼容 |
| `text-[13px]` | `text-[13px]` | 任意值，兼容 |
| `grid-cols-5` | `grid-cols-5` | 兼容 |
| `space-y-4` | `space-y-4` | 兼容 |

#### 移动端额外规则

| 原始 | 转换为 | 原因 |
|------|-------|------|
| `px` 固定尺寸 | `rpx`（宽高/间距） | 响应式适配 |
| `height: 812px` | 删除 | 手机框架尺寸 |
| `width: 375px` | `w-full` | 全屏宽度 |
| `font-family: Poppins` | 删除 | 使用系统字体 |
| `border-radius: 44px` | 删除 | 手机框架圆角 |
| 自定义 CSS 动画 | 评估保留 | 简单动画可保留 |

#### PC 端额外规则

| 原始 | 转换为 | 原因 |
|------|-------|------|
| 自定义 Tailwind config 颜色 | UnoCSS theme 变量 | `text-primary` → `var(--el-color-primary)` |
| `max-w-[1200px]` | 框架布局自带 | 不需要手动限宽 |
| 侧边栏 `fixed left-0` | 框架自带侧栏 | 使用框架布局 |
| 顶部导航 `sticky top-0` | 框架自带头部 | 使用框架布局 |

#### 需要删除的设计稿专属代码

以下是 HTML 设计稿中用于展示效果但不需要转换的部分：

```html
<!-- 删除：手机模拟器壳 -->
<div class="phone-frame"> ... </div>
<div class="status-bar"> ... </div>
<div class="home-indicator"></div>

<!-- 删除：CDN 引用 -->
<script src="https://cdn.tailwindcss.com"></script>
<script src="https://unpkg.com/lucide@latest/..."></script>

<!-- 删除：Tailwind config（已有 UnoCSS 配置） -->
<script>tailwind.config = { ... }</script>

<!-- 删除：Google Fonts 引用 -->
<link href="https://fonts.googleapis.com/..." />

<!-- 删除：Lucide 初始化脚本 -->
<script>lucide.createIcons();</script>

<!-- 评估：自定义 <style> -->
<!-- 简单动画/过渡 → 保留到 <style scoped lang="scss"> -->
<!-- 复杂布局 hack → 用框架组件替代 -->
```

---

## 步骤 3：生成框架代码

### 3.1 PC 端页面模板

#### 列表页（后台管理型）

```vue
<!-- xxx管理 -->
<template>
  <div>
    <!-- 搜索区域 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="名称" prop="name" v-model="queryParams.name" @input="handleQuery" />
      <AFormSelect label="状态" prop="status" v-model="queryParams.status" :options="sys_enable_status" @change="handleQuery" />
    </ASearchForm>

    <!-- 数据区域 -->
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['xxx:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['xxx:remove']">删除</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </el-row>
      </template>

      <el-table ref="tableRef" v-loading="isLoading" :data="dataList" :height="tableHeight" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <!-- 根据设计稿列定义 -->
        <el-table-column label="名称" prop="name" />
        <el-table-column label="状态" prop="status">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['xxx:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['xxx:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新增/修改弹窗 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <!-- 根据设计稿表单字段 -->
          <AFormInput label="名称" v-model="form.name" prop="name" span="auto" />
          <AFormSelect label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto" />
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="XxxPage">
// 按实际需求导入 API
import { pageXxxs, getXxx, addXxx, updateXxx, deleteXxxs } from '@/api/business/xxx/xxxApi'
import type { XxxQuery, XxxBo, XxxVo } from '@/api/business/xxx/xxxTypes'

const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const isLoading = ref(true)
const dataList = ref<XxxVo[]>([])
const total = ref(0)

const queryParams = ref<XxxQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
})

const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageXxxs(queryParams.value)
  if (!err) {
    dataList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

onMounted(() => {
  getList()
})
</script>
```

#### 展示页（非 CRUD，如仪表盘/商城首页）

```vue
<!-- xxx页面 -->
<template>
  <div>
    <!-- 直接使用 UnoCSS 类还原设计稿布局 -->
    <div class="grid grid-cols-4 gap-4 mb-4">
      <el-card v-for="item in statsList" :key="item.label" shadow="hover">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-text-secondary text-sm">{{ item.label }}</div>
            <div class="text-2xl font-bold mt-2">{{ item.value }}</div>
          </div>
          <div class="w-12 h-12 rounded-xl flex-center" :class="item.bgClass">
            <i :class="item.icon" class="text-xl" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 其他内容区域 -->
  </div>
</template>

<script setup lang="ts" name="XxxDashboard">
const statsList = ref([
  { label: '总订单', value: '1,234', icon: 'i-ep-document', bgClass: 'bg-primary/10 text-primary' },
  // ...
])
</script>
```

### 3.2 移动端页面模板

#### 列表页

```vue
<!-- xxx列表 -->
<template>
  <view class="min-h-100vh bg-gray-50">
    <wd-navbar title="标题" />

    <!-- 搜索区域（如有） -->
    <view class="px-24rpx pt-16rpx">
      <wd-search v-model="keyword" placeholder="搜索..." @search="handleSearch" />
    </view>

    <!-- 分页列表 -->
    <wd-paging :fetch="fetchList" :params="queryParams">
      <template #default="{ list }">
        <view class="px-24rpx">
          <view
            v-for="item in list"
            :key="item.id"
            class="bg-white rounded-16rpx mb-16rpx p-24rpx"
            @click="goDetail(item.id)"
          >
            <!-- 根据设计稿卡片布局 -->
            <view class="flex items-center justify-between">
              <text class="text-base font-medium">{{ item.name }}</text>
              <wd-tag :type="item.status === '1' ? 'success' : 'danger'">
                {{ item.statusLabel }}
              </wd-tag>
            </view>
            <text class="text-sm text-gray-500 mt-8rpx">{{ item.description }}</text>
          </view>
        </view>
      </template>
    </wd-paging>
  </view>
</template>

<script setup lang="ts">
import { pageXxxs } from '@/api/xxx/xxxApi'
import type { XxxQuery, XxxVo } from '@/api/xxx/xxxTypes'

const keyword = ref('')
const queryParams = ref<XxxQuery>({})

const fetchList = async (params: XxxQuery) => {
  const [err, data] = await pageXxxs(params)
  if (!err) return data
}

const goDetail = (id: string | number) => {
  uni.navigateTo({ url: `/pages-sub/xxx/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
/* 自定义样式 */
</style>
```

#### 表单页

```vue
<!-- xxx表单 -->
<template>
  <view class="min-h-100vh bg-gray-50">
    <wd-navbar title="提交信息" show-back />

    <view class="p-24rpx">
      <wd-form ref="formRef" :model="formData" :rules="formRules">
        <wd-cell-group border>
          <wd-input
            v-model="formData.name"
            label="名称"
            prop="name"
            placeholder="请输入名称"
            clearable
          />
          <wd-select
            v-model="formData.type"
            label="类型"
            prop="type"
            :columns="typeOptions"
            placeholder="请选择类型"
          />
          <wd-textarea
            v-model="formData.remark"
            label="备注"
            prop="remark"
            placeholder="请输入备注"
          />
        </wd-cell-group>
      </wd-form>

      <view class="mt-32rpx px-24rpx">
        <wd-button type="primary" block @click="handleSubmit">提交</wd-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useToast } from '@/wd'
import type { FormInstance } from '@/wd'

const toast = useToast()
const formRef = ref<FormInstance>()

const formData = ref({
  name: '',
  type: '',
  remark: '',
})

const formRules = ref({
  name: [{ required: true, message: '请输入名称' }],
  type: [{ required: true, message: '请选择类型' }],
})

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  const [err] = await addXxx(formData.value)
  if (!err) {
    toast.success('提交成功')
    uni.navigateBack()
  }
}
</script>

<style lang="scss" scoped>
/* 自定义样式 */
</style>
```

#### 商品展示页（非 CRUD）

```vue
<!-- xxx展示 -->
<template>
  <view class="min-h-100vh bg-white">
    <wd-navbar title="首页" />

    <!-- 轮播图 -->
    <wd-swiper :list="bannerList" autoplay />

    <!-- 功能入口 -->
    <view class="grid grid-cols-4 gap-16rpx px-24rpx py-24rpx">
      <view
        v-for="item in menuList"
        :key="item.title"
        class="flex flex-col items-center"
        @click="handleMenuClick(item)"
      >
        <view
          class="w-88rpx h-88rpx rounded-24rpx flex items-center justify-center mb-8rpx"
          :style="{ background: item.bgColor }"
        >
          <wd-icon :name="item.icon" size="44rpx" color="#fff" />
        </view>
        <text class="text-2xs text-gray-600">{{ item.title }}</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="px-24rpx">
      <view class="grid grid-cols-2 gap-16rpx">
        <view
          v-for="item in goodsList"
          :key="item.id"
          class="bg-white rounded-16rpx overflow-hidden border border-gray-100"
          @click="goDetail(item.id)"
        >
          <wd-img :src="item.image" width="100%" height="320rpx" mode="aspectFill" />
          <view class="p-16rpx">
            <text class="text-sm font-medium line-clamp-2">{{ item.name }}</text>
            <view class="flex items-center justify-between mt-12rpx">
              <text class="text-primary font-bold">¥{{ item.price }}</text>
              <wd-button type="primary" size="small" round icon="add" @click.stop="addToCart(item)" />
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useToast } from '@/wd'

const toast = useToast()
const bannerList = ref<string[]>([])
const menuList = ref([
  { title: '分类', icon: 'grid', bgColor: '#FF6B35' },
  { title: '优惠券', icon: 'ticket', bgColor: '#10B981' },
])
const goodsList = ref([])

const goDetail = (id: string | number) => {
  uni.navigateTo({ url: `/pages-sub/goods/detail?id=${id}` })
}

const addToCart = (item: any) => {
  toast.success('已加入购物车')
}
</script>

<style lang="scss" scoped>
/* 自定义样式 */
</style>
```

### 3.3 单组件/区块转换

当只需要转换 HTML 中的某个区块时，提取该区块并生成独立组件：

**PC 端组件**：

```vue
<!-- 统计卡片 -->
<template>
  <el-card shadow="hover" class="stat-card">
    <div class="flex items-center justify-between">
      <div>
        <div class="text-text-secondary text-sm">{{ label }}</div>
        <div class="text-2xl font-bold mt-2">{{ value }}</div>
        <div class="text-xs mt-1" :class="trend > 0 ? 'text-success' : 'text-danger'">
          {{ trend > 0 ? '+' : '' }}{{ trend }}% 较上周
        </div>
      </div>
      <div class="w-12 h-12 rounded-xl flex-center" :class="bgClass">
        <i :class="icon" class="text-xl" />
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  trend: number
  icon: string
  bgClass: string
}>()
</script>

<style lang="scss" scoped>
.stat-card {
  transition: all 0.2s ease;
  &:hover {
    transform: translateY(-2px);
  }
}
</style>
```

**移动端组件**：

```vue
<!-- 商品卡片 -->
<template>
  <view
    class="bg-white rounded-16rpx overflow-hidden"
    @click="$emit('click', item)"
  >
    <wd-img :src="item.image" width="100%" height="320rpx" mode="aspectFill" />
    <view class="p-16rpx">
      <text class="text-sm font-medium line-clamp-2">{{ item.name }}</text>
      <view class="flex items-center justify-between mt-12rpx">
        <text class="text-primary font-bold text-base">¥{{ item.price }}</text>
        <wd-button type="primary" size="small" round icon="add" @click.stop="$emit('add', item)" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  item: {
    id: string | number
    image: string
    name: string
    price: number
  }
}>()

defineEmits<{
  click: [item: any]
  add: [item: any]
}>()
</script>
```

---

## 步骤 4：规范检查清单

转换完成后，必须通过以下检查：

### 通用检查

- [ ] **首行注释**：`.vue` 文件第一行是 `<!-- 中文描述 -->`
- [ ] **API 调用**：使用 `const [err, data] = await api()` 模式
- [ ] **无 try-catch**：API 调用不使用 try-catch
- [ ] **删除设计稿壳**：无 phone-frame、status-bar、CDN 引用
- [ ] **删除 Tailwind config**：不保留 `tailwind.config` 块
- [ ] **删除 Google Fonts**：不保留字体 CDN

### PC 端检查

- [ ] **封装组件**：使用 `AFormInput` 而非 `el-input`
- [ ] **搜索区域**：使用 `ASearchForm` 而非 `el-form inline`
- [ ] **弹窗**：使用 `AModal` 而非 `el-dialog`
- [ ] **UnoCSS 兼容**：Tailwind 类已适配为 UnoCSS（变体组等）
- [ ] **主题色**：使用 `text-primary` 而非硬编码颜色

### 移动端检查

- [ ] **view 替代 div**：所有 `<div>` 已转为 `<view>`
- [ ] **WD 组件**：使用 `wd-*` 而非 `uni-*` 组件
- [ ] **导入路径**：`from '@/wd'` 而非 `from 'wot-design-uni'`
- [ ] **消息提示**：`useToast()` 而非 `uni.showToast()`
- [ ] **rpx 单位**：固定尺寸使用 rpx
- [ ] **安全区**：底部固定栏有 `pb-safe`
- [ ] **CSS 注释**：使用 `/* */` 而非 `//`

---

## 设计系统颜色处理

### 原则

设计稿的自定义颜色**不直接硬编码**，而是映射到项目主题变量：

| 设计稿颜色用途 | PC 端 | 移动端 |
|--------------|-------|--------|
| 主色/品牌色 | `text-primary` / `bg-primary` | `text-primary` |
| 成功/绿色 | `text-success` | `text-success` |
| 警告/橙色 | `text-warning` | `text-warning` |
| 危险/红色 | `text-danger` | `text-error` |
| 信息/蓝色 | `text-info` | `text-info` |
| 正文文字 | `text-text-base` | `text-gray-900` |
| 次要文字 | `text-text-secondary` | `text-gray-500` |
| 背景色 | `bg-bg-page` | `bg-gray-50` |
| 卡片背景 | `bg-white` | `bg-white` |
| 边框 | `border-border` | `border-gray-100` |

### 装饰性颜色

对于设计稿中的**装饰性渐变、配色**（非语义化），可保留 UnoCSS 任意值：

```html
<!-- 设计稿 -->
<div class="bg-gradient-to-r from-orange-400 to-pink-500">

<!-- 转换后（保留，UnoCSS 支持） -->
<view class="bg-gradient-to-r from-orange-400 to-pink-500">
```

---

## 常见转换场景

### 场景 1：购物车底栏

```html
<!-- 设计稿 HTML -->
<div class="fixed bottom-0 left-0 right-0 bg-white border-t px-4 py-3 flex items-center justify-between">
  <div class="flex items-center">
    <div class="relative">
      <i data-lucide="shopping-bag" class="w-7 h-7 text-primary"></i>
      <span class="absolute -top-2 -right-2 bg-primary text-white text-xs w-5 h-5 rounded-full flex items-center justify-center">3</span>
    </div>
    <span class="ml-3 text-xl font-bold">¥128.00</span>
  </div>
  <button class="bg-primary text-white px-8 py-3 rounded-full font-medium">去结算</button>
</div>
```

```vue
<!-- 转换后：移动端 -->
<view class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 px-24rpx py-16rpx flex items-center justify-between pb-safe z-50">
  <view class="flex items-center">
    <view class="relative">
      <wd-icon name="cart" size="56rpx" color="var(--wot-color-theme)" />
      <view class="absolute -top-8rpx -right-8rpx bg-primary text-white text-2xs w-36rpx h-36rpx rounded-full flex items-center justify-center">
        {{ cartCount }}
      </view>
    </view>
    <text class="ml-16rpx text-xl font-bold">¥{{ totalPrice }}</text>
  </view>
  <wd-button type="primary" round @click="goCheckout">去结算</wd-button>
</view>
```

### 场景 2：商品筛选/排序栏

```html
<!-- 设计稿 HTML -->
<div class="flex gap-2 border-b pb-3">
  <button class="px-4 py-1.5 rounded-full bg-red-600 text-white text-sm">综合</button>
  <button class="px-4 py-1.5 rounded-full bg-gray-100 text-gray-600 text-sm">销量</button>
  <button class="px-4 py-1.5 rounded-full bg-gray-100 text-gray-600 text-sm">价格 ↑</button>
</div>
```

```vue
<!-- 转换后：移动端 -->
<wd-tabs v-model="activeSort" @change="handleSortChange">
  <wd-tab title="综合" name="default" />
  <wd-tab title="销量" name="sales" />
  <wd-tab title="价格" name="price" />
</wd-tabs>
```

### 场景 3：后台统计卡片区域

```html
<!-- 设计稿 HTML -->
<div class="grid grid-cols-4 gap-5">
  <div class="bg-white rounded-xl p-5 border hover:shadow-md transition-all">
    <div class="flex items-center justify-between mb-3">
      <span class="text-gray-500 text-sm">今日订单</span>
      <div class="w-9 h-9 bg-blue-50 rounded-lg flex items-center justify-center">
        <i data-lucide="clipboard-list" class="w-5 h-5 text-blue-500"></i>
      </div>
    </div>
    <div class="text-2xl font-bold">1,234</div>
    <div class="text-xs text-green-500 mt-1">+12.5% 较昨日</div>
  </div>
</div>
```

```vue
<!-- 转换后：PC 端 -->
<el-row :gutter="20" class="mb-4">
  <el-col :span="6" v-for="item in statsList" :key="item.label">
    <el-card shadow="hover">
      <div class="flex items-center justify-between mb-3">
        <span class="text-text-secondary text-sm">{{ item.label }}</span>
        <div class="w-9 h-9 rounded-lg flex-center" :class="item.iconBg">
          <i :class="item.icon" class="text-lg" />
        </div>
      </div>
      <div class="text-2xl font-bold">{{ item.value }}</div>
      <div class="text-xs mt-1" :class="item.trend > 0 ? 'text-success' : 'text-danger'">
        {{ item.trend > 0 ? '+' : '' }}{{ item.trend }}% 较昨日
      </div>
    </el-card>
  </el-col>
</el-row>
```

---

## 与其他技能联动

| 转换后需求 | 下一步技能 | 说明 |
|-----------|-----------|------|
| 需要完整 CRUD 后端 | `crud-development` | 配套后端接口 |
| 需要 API 接口定义 | `api-development` | API 规范 |
| 需要建表 | `database-ops` | 数据库设计 |
| 需要调整移动端设计 | `ui-design-mobile` | 设计优化 |
| 需要使用特定组件 | `ui-pc` / `ui-mobile` | 组件用法详情 |
| 需要图标选择 | `icon-management` | 图标库查询 |

---

## 常见错误

### ❌ 直接复制 HTML 结构不做转换

```html
<!-- 错误：保留了 div、原生 input -->
<div class="flex">
  <input type="text" class="border rounded px-3 py-2" />
  <button class="bg-blue-500 text-white px-4 py-2 rounded">搜索</button>
</div>
```

### ✅ 正确转换为框架组件

```vue
<!-- PC端 -->
<ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
  <AFormInput label="搜索" v-model="queryParams.keyword" @input="handleQuery" />
</ASearchForm>

<!-- 移动端 -->
<wd-search v-model="keyword" placeholder="搜索..." @search="handleSearch" />
```

### ❌ 保留 Lucide 图标引用

```html
<i data-lucide="shopping-cart" class="w-6 h-6"></i>
```

### ✅ 转换为项目图标

```vue
<!-- 移动端 -->
<wd-icon name="cart" size="48rpx" />

<!-- PC端 -->
<el-icon><ShoppingCart /></el-icon>
<!-- 或 UnoCSS -->
<i class="i-ep-shopping-cart text-xl" />
```

### ❌ 保留手机模拟器壳

```html
<div class="phone-frame">
  <div class="status-bar">9:41</div>
  <!-- 内容 -->
  <div class="home-indicator"></div>
</div>
```

### ✅ 只保留内容区域

```vue
<view class="min-h-100vh bg-gray-50">
  <wd-navbar title="页面标题" />
  <!-- 内容 -->
</view>
```

---

## 移动端踩坑指南（实战经验）

> 以下问题来自实际转换过程中遇到的坑，转换时必须注意。

### 坑 1：底部操作栏必须用 fixed 定位

设计稿中的底部操作栏（如「加入购物车 + 立即购买」）**不能放在 scroll-view 内部**，否则会跟着内容滚动。

```vue
<!-- ❌ 错误：底部栏在 scroll-view 内，会跟着滚 -->
<scroll-view scroll-y>
  <!-- 内容 -->
  <view class="bg-white border-t">底部操作栏</view>
</scroll-view>

<!-- ✅ 正确：底部栏用 fixed 定位 + scroll-view 内加占位 -->
<scroll-view scroll-y>
  <!-- 内容 -->
  <view class="h-200rpx" /> <!-- 底部占位，防遮挡 -->
</scroll-view>
<view class="fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-100">
  <!-- 底部操作栏内容 -->
</view>
```

**同时别忘了安全区**：
```scss
.bottom-bar {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
```

### 坑 2：wd-button 的 min-width 会覆盖 custom-style 的 width

`wd-button` 的 medium 尺寸内部有 `min-width: 240rpx`，导致通过 `custom-style="width: 180rpx"` 设置宽度**不生效**（min-width 优先级更高）。

```vue
<!-- ❌ 错误：custom-style 设置 width 不生效 -->
<wd-button size="medium" custom-style="width: 180rpx;">按钮</wd-button>

<!-- ✅ 正确：用 custom-class + :deep 覆盖 min-width -->
<wd-button size="medium" custom-class="my-btn">按钮</wd-button>

<style lang="scss" scoped>
:deep(.my-btn) {
  min-width: 220rpx !important;
  width: 220rpx !important;
}
</style>
```

### 坑 3：flex 布局中 gap 在小程序可能不生效

小程序的部分运行时对 CSS `gap` 支持不完善，两个元素可能紧贴在一起。

```vue
<!-- ❌ 可能不生效 -->
<view style="display: flex; gap: 16rpx;">
  <wd-button>A</wd-button>
  <wd-button>B</wd-button>
</view>

<!-- ✅ 用 margin 替代 gap -->
<wd-button custom-style="margin-right: 16rpx;">A</wd-button>
<wd-button>B</wd-button>
```

### 坑 4：flex 子元素溢出父容器（min-width: auto 问题）

flex 子元素默认 `min-width: auto`，内容宽度可能撑破父容器的 padding。

```vue
<!-- ❌ 错误：按钮可能溢出右侧 padding -->
<view class="flex px-32rpx">
  <view class="flex-1">内容</view>
</view>

<!-- ✅ 正确：加 min-width: 0 约束 -->
<view class="flex px-32rpx">
  <view style="flex: 1; min-width: 0;">内容</view>
</view>
```

### 坑 5：标签/角标不要自己拼 view，用 wd-tag

自己用 `<view>` + `<text>` 拼的标签，内边距和圆角在不同平台表现不一致。

```vue
<!-- ❌ 自己拼：内边距在各平台不一致 -->
<view class="bg-red-500 rounded-full px-16rpx py-4rpx">
  <text class="text-white text-[20rpx]">9折</text>
</view>

<!-- ✅ 用 wd-tag 组件：自带一致的样式 -->
<wd-tag type="danger" size="small" round>9折</wd-tag>
```

### 坑 6：转换后的样式检查清单（新增）

在移动端检查清单基础上，额外检查：

- [ ] **底部固定栏**：使用 `fixed bottom-0 left-0 right-0 z-50`，不放在 scroll-view 内
- [ ] **底部占位**：scroll-view 底部有 `h-200rpx` 占位 view
- [ ] **安全区适配**：固定底栏有 `padding-bottom: env(safe-area-inset-bottom)`
- [ ] **按钮宽度**：如需自定义宽度，用 `custom-class` + `:deep()` + `!important` 覆盖 `min-width`
- [ ] **flex gap**：不依赖 `gap`，改用 `margin` 实现间距
- [ ] **标签组件**：折扣/状态标签用 `wd-tag`，不自己拼 view
- [ ] **信息卡片**：大面积渐变卡片（标签+标题+描述+按钮）要完整还原，不能简化成一行横条
- [ ] **装饰元素**：渐变卡片中的半透明装饰圆（`bg-white/10`）要保留
- [ ] **功能列表图标**：每项有独立颜色背景方块的列表，不能用 `wd-cell` 内置 icon 代替
- [ ] **禁止 scroll-view 固定高度**：Tab 子组件不能用 `scroll-view style="height: calc(...)"`，用普通 `view`
- [ ] **禁止自定义 CSS class**：全部用 UnoCSS 内联类，style scoped 只写 `:deep`
- [ ] **wd-icon 图标验证**：确认图标名在可用列表中，不存在的用 emoji 代替

### 坑 7：渐变卡片中的装饰元素不能丢

原型中的渐变卡片通常有半透明装饰圆和右侧装饰图标，转换时**必须保留**，否则卡片会很单调。

```vue
<!-- ❌ 简化成一行横条 -->
<view class="bg-sky-50 rounded-2xl px-32rpx py-24rpx flex items-center">
  <text>💉</text>
  <text>流感疫苗接种提醒</text>
</view>

<!-- ✅ 完整还原渐变卡片 -->
<view class="mx-32rpx bg-gradient-to-br from-sky-500 to-cyan-400 rounded-2xl px-32rpx py-32rpx relative overflow-hidden">
  <!-- 装饰圆 -->
  <view class="absolute -right-40rpx -top-40rpx w-200rpx h-200rpx rounded-full bg-white/10" />
  <view class="relative z-10">
    <wd-tag size="small" round custom-style="background: rgba(255,255,255,0.25); color: #fff; border: none;">健康提醒</wd-tag>
    <text class="text-white font-bold text-base mt-16rpx block">春季疫苗接种提醒</text>
    <text class="text-white/80 text-[24rpx] mt-8rpx block">流感高发季，建议及时接种</text>
    <view class="mt-20rpx">
      <wd-button size="small" round custom-style="background: #fff; color: #0ea5e9; border: none;">了解详情</wd-button>
    </view>
  </view>
</view>
```

### 坑 8：功能列表要用自定义布局还原彩色图标

原型中"更多功能"列表每项都有**独立颜色的图标背景方块**，不能简单用 `wd-cell` 的 `icon` 属性替代（因为 `wd-cell` 图标没有背景色）。

```vue
<!-- ❌ wd-cell 内置 icon：没有彩色背景 -->
<wd-cell title="就诊记录" icon="clipboard" is-link />

<!-- ✅ 自定义布局：还原彩色图标背景 -->
<view class="flex items-center justify-between px-32rpx py-28rpx" @click="handleClick">
  <view class="flex items-center">
    <view class="w-64rpx h-64rpx rounded-lg flex items-center justify-center" style="background: #eff6ff;">
      <wd-icon name="shop" size="32rpx" color="#3b82f6" />
    </view>
    <text class="text-sm text-gray-700 ml-24rpx">就诊记录</text>
  </view>
  <wd-icon name="arrow-right" size="32rpx" color="#d1d5db" />
</view>
```

### 坑 9：wd-search 在渐变背景上的白色底

`wd-search` 组件自带白色背景和内边距，在渐变色头部使用时需要去掉。

```vue
<!-- ✅ 用 custom-class + :deep 去掉白色背景 -->
<wd-search custom-class="my-search" placeholder="搜索..." />

<style lang="scss" scoped>
:deep(.my-search) {
  background: transparent !important;
  padding: 0 !important;
}
</style>
```

### 坑 10：wd-tag 自定义颜色要用 custom-style

`wd-tag` 预设的 `type`（success/warning/danger/primary）颜色可能不匹配设计稿，需要用 `custom-style` 精确控制。

```vue
<!-- ❌ type 颜色不匹配设计稿 -->
<wd-tag type="warning">会员标签</wd-tag>

<!-- ✅ custom-style 精确控制颜色 -->
<wd-tag size="small" round custom-style="background: #facc15; color: #713f12; border: none;">🌿 鲜享会员</wd-tag>
<wd-tag size="small" round custom-style="background: rgba(255,255,255,0.25); color: #fff; border: none;">健康提醒</wd-tag>
```

### 坑 11：禁止在 Tab 子组件内使用 scroll-view 固定高度

Demo 模板的每个 Tab 页面（如 Index/Category/Cart/My）是作为**子组件**嵌入主入口的 `wd-tabbar` 容器中的。父组件已经提供了滚动能力，子组件**不能**再套一个固定高度的 `scroll-view`，否则会导致：
- 内容区域和列表之间出现**大面积空白**
- 底部内容被截断
- 滚动行为异常（嵌套滚动冲突）

```vue
<!-- ❌ 错误：子组件内用 scroll-view 固定高度 -->
<scroll-view scroll-y style="height: calc(100vh - 200rpx);">
  <!-- 内容 -->
</scroll-view>

<!-- ✅ 正确：用普通 view，让父组件控制滚动 -->
<view>
  <!-- 内容自然流动 -->
</view>
```

**例外**：只有侧边栏分类页（如 wd-sidebar + 右侧内容）需要内部 scroll-view，因为左右两列需要独立滚动。

### 坑 12：禁止使用自定义 CSS class，全部用 UnoCSS 内联类

转换时不要写 `.grid-section`, `.func-item`, `.timeline-dot` 等自定义 CSS class。全部用 UnoCSS/Tailwind 内联类写在 `class=""` 里。`<style scoped>` 只保留 `:deep()` 等必要样式。

```vue
<!-- ❌ 错误：自定义 CSS class -->
<view class="grid-section">
  <view class="grid-item">...</view>
</view>
<style scoped>
.grid-section { display: grid; ... }
.grid-item { ... }
</style>

<!-- ✅ 正确：UnoCSS 内联类 -->
<view class="grid grid-cols-4 gap-16rpx px-32rpx py-24rpx">
  <view class="flex flex-col items-center gap-12rpx">...</view>
</view>
<style lang="scss" scoped>
/* 只写 :deep 样式 */
</style>
```

### 坑 13：wd-icon 图标名不存在时用 emoji 代替

项目的 wd-icon 图标库有限，以下图标名**不存在**，使用会显示为空方块：
- `bell`, `download`, `bookmark`, `bar-chart`, `clipboard`, `file-text`, `activity`, `users`

**可用图标名**（已确认）：
`home, shop, user, cart, search, notification, setting, star, star-fill, heart, heart-fill, clock, time, location, edit, delete, share, info, service, goods, view, chat, add, close, check, check-outline-fill, arrow-left, arrow-right, fill-arrow-down, mobile, laptop, camera, play-fill, warn-bold, wallet, ticket, decrease`

找不到对应图标时，用 emoji 代替：
```vue
<!-- ❌ 图标不存在，显示空方块 -->
<wd-icon name="download" size="40rpx" color="#fff" />

<!-- ✅ 用 emoji 代替 -->
<text class="text-[40rpx]">⬇️</text>
```
