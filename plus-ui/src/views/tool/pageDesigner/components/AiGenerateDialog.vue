<!-- AI生成对话框 -->
<template>
  <AModal
    v-model="visible"
    title="AI 智能生成"
    size="medium"
    :show-footer="false"
    @closed="handleClosed"
  >
    <div class="ai-generate-content">
      <!-- 提示信息 -->
      <el-alert type="info" :closable="false" show-icon class="mb-4">
        <template #title>
          <span>描述你想要的页面内容，AI 将自动生成对应的组件配置</span>
        </template>
      </el-alert>

      <!-- 快捷示例 -->
      <div class="quick-examples">
        <span class="examples-label">快捷示例：</span>
        <el-tag
          v-for="example in quickExamples"
          :key="example.label"
          class="example-tag"
          type="info"
          effect="plain"
          @click="handleExampleClick(example.prompt)"
        >
          {{ example.label }}
        </el-tag>
      </div>

      <!-- 输入区域 -->
      <el-input
        v-model="prompt"
        type="textarea"
        :rows="4"
        :placeholder="placeholderText"
        :disabled="loading"
        class="prompt-input"
        @keydown.ctrl.enter="handleGenerate"
      />

      <!-- 生成模式 -->
      <div class="generate-mode">
        <span class="mode-label">生成模式：</span>
        <el-radio-group v-model="generateMode" :disabled="loading">
          <el-radio value="append">追加到现有组件</el-radio>
          <el-radio value="replace">替换所有组件</el-radio>
        </el-radio-group>
      </div>

      <!-- 错误信息 -->
      <el-alert v-if="error" type="error" :closable="false" show-icon class="mt-4">
        <template #title>
          <span>{{ error }}</span>
        </template>
      </el-alert>

      <!-- 生成结果预览 -->
      <div v-if="generatedItems.length > 0" class="preview-section">
        <div class="preview-header">
          <span class="preview-title">生成结果预览</span>
          <span class="preview-count">共 {{ generatedItems.length }} 个组件</span>
        </div>
        <div class="preview-list">
          <div v-for="item in generatedItems" :key="item.id" class="preview-item">
            <el-tag size="small" type="primary">{{ item.type }}</el-tag>
            <span class="item-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" :disabled="!prompt.trim()" @click="handleGenerate">
          <el-icon v-if="!loading" class="mr-1"><MagicStick /></el-icon>
          {{ loading ? '生成中...' : '生成组件' }}
        </el-button>
        <el-button v-if="generatedItems.length > 0" type="success" @click="handleApply">
          应用到画布
        </el-button>
      </div>
    </div>
  </AModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import { useAiChat } from '@/composables/useAiChat'
import type { FormItemSchema, FormItemType } from '../types'
import { FORM_COMPONENT_TYPES, CHART_COMPONENT_TYPES, NON_FORM_COMPONENT_TYPES } from '../types'

defineOptions({ name: 'AiGenerateDialog' })

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'generate', items: FormItemSchema[], mode: 'append' | 'replace'): void
}>()

// 弹窗可见性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 用户输入的提示词
const prompt = ref('')

// 生成模式
const generateMode = ref<'append' | 'replace'>('append')

// 生成的组件列表
const generatedItems = ref<FormItemSchema[]>([])

// 错误信息
const error = ref('')

// 快捷示例
const quickExamples = [
  { label: '用户登录表单', prompt: '生成一个用户登录表单，包含用户名、密码输入框和记住我开关' },
  { label: '用户注册表单', prompt: '生成用户注册表单，包含用户名、邮箱、密码、确认密码和手机号' },
  { label: '商品信息表单', prompt: '生成商品信息表单，包含商品名称、分类选择、价格、库存、商品图片上传和详细描述' },
  { label: '数据统计看板', prompt: '生成数据统计看板，使用 row 和 col 布局，第一行包含4个统计卡片（访问量、用户数、订单数、销售额），第二行包含一个折线图卡片和一个饼图卡片' },
  { label: '搜索筛选表单', prompt: '生成搜索筛选表单，包含关键词搜索、状态选择、日期范围选择' },
  { label: '订单管理看板', prompt: '生成订单管理看板，使用 row 和 col 布局，第一行4个统计卡片（待付款、待发货、已发货、已完成），第二行左侧是订单趋势折线图，右侧是订单来源饼图' }
]

// 占位提示文本
const placeholderText = `请描述你想要的页面内容，例如：
- 生成一个用户注册表单，包含用户名、邮箱、密码、确认密码
- 生成数据看板，使用 row/col 布局，第一行4个统计卡片，第二行2个图表
- 生成搜索筛选表单，包含关键词搜索、状态选择、日期范围

提示：卡片和图表会自动使用 row > col 布局结构
按 Ctrl+Enter 快速生成`

// 使用 AI 对话
const { loading, sendMessage } = useAiChat({
  temperature: 0.7,
  systemPrompt: buildSystemPrompt()
})

// 构建系统提示词
function buildSystemPrompt(): string {
  return `你是一个专业的页面设计助手，负责根据用户描述生成符合规范的页面组件配置。

## 重要：布局规范

### 布局组件说明
- **row** (行容器): 用于包裹多个列，形成一行布局
- **col** (列容器): 放在 row 内部，通过 props.span 控制宽度（1-24栅格）

### 布局使用规则
1. **卡片/图表组件必须放在 row > col 结构中**，否则无法正确显示
2. **表单组件不需要 row/col**，系统会自动使用 el-row 包裹
3. row 组件的 children 数组包含 col 组件
4. col 组件的 children 数组包含实际的卡片/图表组件
5. col 的 props.span 控制列宽：6=1/4, 8=1/3, 12=1/2, 24=整行

### 布局结构示例
\`\`\`json
{
  "id": "row_1", "type": "row", "label": "统计卡片行",
  "props": { "gutter": 20 },
  "children": [
    {
      "id": "col_1", "type": "col", "label": "列1",
      "props": { "span": 6 },
      "children": [
        { "id": "statsCard_1", "type": "statsCard", "label": "访问量", "props": { "title": "今日访问", "value": 8520, "unit": "次" } }
      ]
    },
    {
      "id": "col_2", "type": "col", "label": "列2",
      "props": { "span": 6 },
      "children": [
        { "id": "statsCard_2", "type": "statsCard", "label": "用户数", "props": { "title": "总用户", "value": 1234, "unit": "人" } }
      ]
    }
  ]
}
\`\`\`

## 组件类型及配置规范

### 一、表单组件（直接放在顶层数组，不需要 row/col）

1. **input** (单行输入) - span: 12
   - props: { placeholder, prefixIcon, suffixIcon, maxlength, clearable, showWordLimit }

2. **textarea** (多行文本) - span: 24
   - props: { placeholder, rows(2-20), maxlength, showWordLimit }

3. **password** (密码输入) - span: 12
   - props: { placeholder, maxlength, showPassword, clearable }

4. **number** (数字输入) - span: 12
   - props: { placeholder, min, max, step, precision, controls }

5. **select** (下拉选择) - span: 12
   - props: { placeholder, multiple, filterable, clearable }
   - 需要 options: [{ label: '中文', value: '值' }]

6. **radio/checkbox** (单选/复选) - span: 12
   - props: { type('radio'|'button'), border }
   - 需要 options

7. **switch** (开关) - span: 12
   - props: { activeText, inactiveText }

8. **date/datetime/daterange** (日期) - span: 12
   - props: { placeholder, format, valueFormat, clearable }

9. **imgUpload/fileUpload** (上传) - span: 24
   - props: { limit, fileSize, listType }

10. **editor** (富文本) - span: 24
    - props: { height, placeholder }

### 二、统计卡片（必须放在 row > col 中）

1. **statsCard** (统计卡片) - 建议 col.span: 6
   - props: { title, value, unit, icon, description, trend: { value, isUp } }
   - icon 可选值: user, view, money, goods, order, chart, data

2. **lineStatsCard** (折线统计卡片) - 建议 col.span: 12
   - props: { title, description, subtitle, chartData: [7个数值], stats: [{ label, value }] }

3. **barStatsCard** (柱状统计卡片) - 建议 col.span: 12
   - props: { title, description, chartData: [7个数值], stats: [{ label, value }] }

### 三、图表卡片（必须放在 row > col 中）

1. **pieChartCard** (饼图卡片) - 建议 col.span: 12
   - props: { title, subtitle, height, showLegend, data: [{ name, value }] }

2. **barChartCard** (柱图卡片) - 建议 col.span: 12
   - props: { title, value, unit, height, data: [7个数值], trend: { value, isUp } }

3. **lineChartCard** (折线图卡片) - 建议 col.span: 12
   - props: { title, value, unit, height, smooth: true, showAreaColor: true, data: [7个数值] }

4. **radarChartCard** (雷达图卡片) - 建议 col.span: 12
   - props: { title, height, indicator: [{ name, max }], data: [{ name, value: [数组] }] }

### 四、数据展示卡片（必须放在 row > col 中）

1. **tableCard** (表格卡片) - 建议 col.span: 24 或 12
   - props: { title, columns: [{ prop, label, width }], data: [行数据] }

2. **dataListCard** (数据列表) - 建议 col.span: 12
   - props: { title, maxCount, list: [{ title, status, time, icon }] }

3. **activityCard** (活动时间轴) - 建议 col.span: 12
   - props: { title, activities: [{ user, action, target, time, type }] }

## 生成规则

1. **表单组件**: 直接放在顶层数组，设置 span 属性
2. **卡片/图表组件**: 必须放在 row > col > 组件 的嵌套结构中
3. **id 格式**: \`类型_时间戳_随机数\`
4. **props 对象**: 组件特有属性放在 props 中
5. **不要生成 rules**: 系统自动生成中文验证规则
6. **所有文本中文**: label、title、placeholder 等
7. **数据要真实**: 统计数值、图表数据符合实际场景

## 输出格式

只返回 JSON 数组，不要添加解释文字或 markdown 标记。

### 数据看板示例（4个统计卡片 + 2个图表）:
[
  {
    "id": "row_stats", "type": "row", "label": "统计卡片",
    "props": { "gutter": 16 },
    "children": [
      { "id": "col_1", "type": "col", "label": "列1", "props": { "span": 6 }, "children": [
        { "id": "stats_1", "type": "statsCard", "label": "访问量", "props": { "title": "今日访问", "value": 12580, "unit": "次", "icon": "view", "trend": { "value": 12.5, "isUp": true }, "description": "较昨日" } }
      ]},
      { "id": "col_2", "type": "col", "label": "列2", "props": { "span": 6 }, "children": [
        { "id": "stats_2", "type": "statsCard", "label": "用户数", "props": { "title": "总用户", "value": 8520, "unit": "人", "icon": "user", "trend": { "value": 5.2, "isUp": true }, "description": "较上周" } }
      ]},
      { "id": "col_3", "type": "col", "label": "列3", "props": { "span": 6 }, "children": [
        { "id": "stats_3", "type": "statsCard", "label": "订单数", "props": { "title": "今日订单", "value": 356, "unit": "单", "icon": "order", "trend": { "value": 8.1, "isUp": true }, "description": "较昨日" } }
      ]},
      { "id": "col_4", "type": "col", "label": "列4", "props": { "span": 6 }, "children": [
        { "id": "stats_4", "type": "statsCard", "label": "销售额", "props": { "title": "今日销售", "value": 28650, "unit": "元", "icon": "money", "trend": { "value": 3.2, "isUp": false }, "description": "较昨日" } }
      ]}
    ]
  },
  {
    "id": "row_charts", "type": "row", "label": "图表区域",
    "props": { "gutter": 16 },
    "children": [
      { "id": "col_chart1", "type": "col", "label": "折线图列", "props": { "span": 12 }, "children": [
        { "id": "line_1", "type": "lineChartCard", "label": "访问趋势", "props": { "title": "本周访问趋势", "value": 52000, "unit": "次", "height": 280, "smooth": true, "showAreaColor": true, "data": [5200, 6800, 5900, 7200, 6500, 8100, 7800], "trend": { "value": 15.2, "isUp": true } } }
      ]},
      { "id": "col_chart2", "type": "col", "label": "饼图列", "props": { "span": 12 }, "children": [
        { "id": "pie_1", "type": "pieChartCard", "label": "来源分布", "props": { "title": "访问来源分布", "height": 280, "showLegend": true, "data": [{ "name": "直接访问", "value": 335 }, { "name": "搜索引擎", "value": 450 }, { "name": "外部链接", "value": 180 }, { "name": "社交媒体", "value": 120 }] } }
      ]}
    ]
  }
]

### 表单示例（用户注册）:
[
  { "id": "input_1", "type": "input", "prop": "userName", "label": "用户名", "span": 12, "required": true, "props": { "placeholder": "请输入用户名", "maxlength": 50, "clearable": true } },
  { "id": "input_2", "type": "input", "prop": "email", "label": "邮箱", "span": 12, "required": true, "props": { "placeholder": "请输入邮箱地址", "clearable": true } },
  { "id": "pwd_1", "type": "password", "prop": "password", "label": "密码", "span": 12, "required": true, "props": { "placeholder": "请输入密码", "showPassword": true } },
  { "id": "pwd_2", "type": "password", "prop": "confirmPassword", "label": "确认密码", "span": 12, "required": true, "props": { "placeholder": "请再次输入密码", "showPassword": true } }
]`
}

// 生成唯一 ID
function generateId(type: string): string {
  return `${type}_${Date.now()}_${Math.random().toString(36).substring(2, 5)}`
}

// 点击示例
function handleExampleClick(examplePrompt: string) {
  prompt.value = examplePrompt
}

// 生成组件
async function handleGenerate() {
  if (!prompt.value.trim() || loading.value) return

  error.value = ''
  generatedItems.value = []

  try {
    const response = await sendMessage(prompt.value)

    // 解析 JSON 响应
    const jsonMatch = response.match(/\[[\s\S]*\]/)
    if (!jsonMatch) {
      throw new Error('AI 返回的格式不正确，请重试')
    }

    const items = JSON.parse(jsonMatch[0]) as FormItemSchema[]

    // 验证并修复组件配置
    const validatedItems = items.map((item) => {
      // 确保有有效的 id
      if (!item.id) {
        item.id = generateId(item.type)
      }

      // 确保有有效的 type
      if (!isValidComponentType(item.type)) {
        console.warn(`无效的组件类型: ${item.type}，已跳过`)
        return null
      }

      // 确保有 prop 和 label
      if (!item.prop) {
        item.prop = `field_${Date.now()}`
      }
      if (!item.label) {
        item.label = item.prop
      }

      // 设置默认 span
      if (item.span === undefined) {
        item.span = getDefaultSpan(item.type)
      }

      // 确保有 props 对象（PropertyPanel 需要访问）
      if (!item.props) {
        item.props = {}
      }

      // 删除 AI 可能生成的 rules 字段，让系统自动生成中文验证规则
      delete item.rules

      return item
    }).filter(Boolean) as FormItemSchema[]

    if (validatedItems.length === 0) {
      throw new Error('没有生成有效的组件，请重新描述')
    }

    generatedItems.value = validatedItems
  } catch (err: any) {
    error.value = err.message || '生成失败，请重试'
    console.error('AI 生成失败:', err)
  }
}

// 验证组件类型是否有效
function isValidComponentType(type: string): type is FormItemType {
  const allTypes = [...FORM_COMPONENT_TYPES, ...CHART_COMPONENT_TYPES, ...NON_FORM_COMPONENT_TYPES]
  return allTypes.includes(type as FormItemType)
}

// 获取默认 span
function getDefaultSpan(type: FormItemType): number {
  // 图表和卡片组件默认占整行或半行
  if (CHART_COMPONENT_TYPES.includes(type)) {
    return 24
  }
  if (type.includes('Card')) {
    return 12
  }
  // 表单组件默认半行
  return 12
}

// 应用到画布
function handleApply() {
  if (generatedItems.value.length === 0) return
  emit('generate', generatedItems.value, generateMode.value)
  visible.value = false
}

// 弹窗关闭时重置状态
function handleClosed() {
  prompt.value = ''
  generatedItems.value = []
  error.value = ''
}

// 监听弹窗打开，重置状态
watch(visible, (val) => {
  if (val) {
    generatedItems.value = []
    error.value = ''
  }
})
</script>

<style scoped lang="scss">
.ai-generate-content {
  .quick-examples {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;

    .examples-label {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }

    .example-tag {
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        color: var(--el-color-primary);
        border-color: var(--el-color-primary);
      }
    }
  }

  .prompt-input {
    :deep(.el-textarea__inner) {
      font-size: 14px;
      line-height: 1.6;
    }
  }

  .generate-mode {
    display: flex;
    align-items: center;
    margin-top: 16px;

    .mode-label {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin-right: 12px;
    }
  }

  .preview-section {
    margin-top: 20px;
    padding: 16px;
    background: var(--el-fill-color-light);
    border-radius: 8px;

    .preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .preview-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }

      .preview-count {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }

    .preview-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      max-height: 200px;
      overflow-y: auto;

      .preview-item {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px 10px;
        background: var(--el-bg-color);
        border-radius: 4px;
        font-size: 13px;

        .item-label {
          color: var(--el-text-color-regular);
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>
