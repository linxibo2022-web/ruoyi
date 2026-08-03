<!-- AI优化对话框 -->
<template>
  <AModal
    v-model="visible"
    title="AI 一键优化"
    size="medium"
    :show-footer="false"
    @closed="handleClosed"
  >
    <div class="ai-optimize-content">
      <!-- 提示信息 -->
      <el-alert type="info" :closable="false" show-icon class="mb-4">
        <template #title>
          <span>AI 将根据组件的标签名称，自动优化字段名、占位文本等属性</span>
        </template>
      </el-alert>

      <!-- 优化选项 -->
      <div class="optimize-options">
        <span class="options-label">优化范围：</span>
        <el-checkbox-group v-model="optimizeOptions" :disabled="loading">
          <el-checkbox value="prop">字段名称 (prop)</el-checkbox>
          <el-checkbox value="placeholder">占位提示 (placeholder)</el-checkbox>
          <el-checkbox value="required">必填标记 (required)</el-checkbox>
          <el-checkbox value="props">组件属性 (props)</el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 当前组件预览 -->
      <div class="current-items">
        <div class="items-header">
          <span class="items-title">待优化组件</span>
          <span class="items-count">共 {{ optimizableItems.length }} 个</span>
        </div>
        <div v-if="optimizableItems.length > 0" class="items-list">
          <div v-for="item in optimizableItems" :key="item.id" class="item-row">
            <el-tag size="small" type="primary">{{ item.type }}</el-tag>
            <span class="item-label">{{ item.label }}</span>
            <span class="item-prop">{{ item.prop }}</span>
          </div>
        </div>
        <el-empty v-else description="没有可优化的组件" :image-size="60" />
      </div>

      <!-- 错误信息 -->
      <el-alert v-if="error" type="error" :closable="false" show-icon class="mt-4">
        <template #title>
          <span>{{ error }}</span>
        </template>
      </el-alert>

      <!-- 优化结果预览 -->
      <div v-if="optimizedItems.length > 0" class="result-section">
        <div class="result-header">
          <span class="result-title">优化结果预览</span>
          <el-tag type="success" size="small">已优化 {{ optimizedItems.length }} 个</el-tag>
        </div>
        <div class="result-list">
          <div v-for="item in optimizedItems" :key="item.id" class="result-item">
            <div class="result-label">
              <el-tag size="small">{{ item.type }}</el-tag>
              <span>{{ item.label }}</span>
            </div>
            <div class="result-changes">
              <div v-if="item.changes.prop" class="change-row">
                <span class="change-label">字段名:</span>
                <span class="change-old">{{ item.oldProp }}</span>
                <el-icon class="change-arrow"><Right /></el-icon>
                <span class="change-new">{{ item.changes.prop }}</span>
              </div>
              <div v-if="item.changes.placeholder" class="change-row">
                <span class="change-label">占位符:</span>
                <span class="change-new">{{ item.changes.placeholder }}</span>
              </div>
              <div v-if="item.changes.required !== undefined" class="change-row">
                <span class="change-label">必填:</span>
                <el-tag :type="item.changes.required ? 'danger' : 'info'" size="small">
                  {{ item.changes.required ? '是' : '否' }}
                </el-tag>
              </div>
              <div v-if="item.changes.props" class="change-row props-change">
                <span class="change-label">属性:</span>
                <div class="props-list">
                  <template v-for="(value, key) in item.changes.props" :key="key">
                    <el-tag v-if="typeof value !== 'object'" size="small" type="success" class="prop-tag">
                      {{ key }}: {{ formatPropValue(value) }}
                    </el-tag>
                    <el-tag v-else size="small" type="success" class="prop-tag">
                      {{ key }}: [对象]
                    </el-tag>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="optimizableItems.length === 0 || optimizeOptions.length === 0"
          @click="handleOptimize"
        >
          <el-icon v-if="!loading" class="mr-1"><MagicStick /></el-icon>
          {{ loading ? '优化中...' : '开始优化' }}
        </el-button>
        <el-button v-if="optimizedItems.length > 0" type="success" @click="handleApply">
          应用优化
        </el-button>
      </div>
    </div>
  </AModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { MagicStick, Right } from '@element-plus/icons-vue'
import { useAiChat } from '@/composables/useAiChat'
import type { FormItemSchema, FormItemType } from '../types'
import { FORM_COMPONENT_TYPES, NON_FORM_COMPONENT_TYPES, CONTAINER_TYPES } from '../types'

defineOptions({ name: 'AiOptimizeDialog' })

const props = defineProps<{
  modelValue: boolean
  items: FormItemSchema[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'optimize', updates: Array<{ id: string; updates: Partial<FormItemSchema> }>): void
}>()

// 弹窗可见性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 优化选项
const optimizeOptions = ref<string[]>(['prop', 'placeholder', 'required', 'props'])

// 优化结果
interface OptimizedItem {
  id: string
  type: FormItemType
  label: string
  oldProp: string
  changes: {
    prop?: string
    placeholder?: string
    required?: boolean
    props?: Record<string, any>
  }
}
const optimizedItems = ref<OptimizedItem[]>([])

// 错误信息
const error = ref('')

// 递归收集可优化的组件（表单组件 + 卡片/图表组件）
function collectOptimizableItems(items: FormItemSchema[]): FormItemSchema[] {
  const result: FormItemSchema[] = []
  // 排除的组件类型（纯布局组件）
  const excludeTypes: FormItemType[] = ['row', 'col', 'divider', 'icon']

  for (const item of items) {
    // 收集表单组件和非表单组件（排除纯布局组件）
    if (!excludeTypes.includes(item.type)) {
      result.push(item)
    }
    // 递归处理子组件
    if (item.children && item.children.length > 0) {
      result.push(...collectOptimizableItems(item.children))
    }
  }
  return result
}

// 判断是否是表单组件
function isFormComponentType(type: FormItemType): boolean {
  return FORM_COMPONENT_TYPES.includes(type)
}

// 格式化属性值显示
function formatPropValue(value: any): string {
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (typeof value === 'number') return String(value)
  if (typeof value === 'string') return value.length > 20 ? value.slice(0, 20) + '...' : value
  if (Array.isArray(value)) return `[${value.length}项]`
  return String(value)
}

// 可优化的组件列表
const optimizableItems = computed(() => collectOptimizableItems(props.items))

// 使用 AI 对话
const { loading, sendMessage } = useAiChat({
  temperature: 0.3,
  systemPrompt: buildSystemPrompt()
})

// 构建系统提示词
function buildSystemPrompt(): string {
  return `你是一个专业的页面设计优化专家，负责根据组件的中文标签和类型，优化组件的字段命名和属性配置。

## 任务
根据输入的组件信息，优化以下内容：
1. **prop** - 字段名（仅表单组件需要）
2. **placeholder** - 占位提示（仅表单组件需要）
3. **required** - 是否必填（仅表单组件需要）
4. **props** - 组件特有属性（所有组件都可能需要优化）

## 表单组件优化规则

### prop 字段命名规范
- 使用英文驼峰命名
- 根据标签语义生成合理名称：
  - "用户名" → userName
  - "手机号/电话" → phone, phoneNumber, mobile
  - "邮箱" → email
  - "密码" → password
  - "确认密码" → confirmPassword
  - "商品名称" → goodsName, productName
  - "商品分类" → categoryId, category
  - "价格" → price, amount
  - "库存" → stock, inventory
  - "状态" → status, state
  - "备注" → remark, notes
  - "描述/简介" → description, intro
  - "创建时间" → createTime, createdAt
  - "更新时间" → updateTime, updatedAt
  - "开始日期" → startDate, beginDate
  - "结束日期" → endDate
  - "日期范围" → dateRange
  - "地址" → address
  - "头像" → avatar
  - "图片" → image, images, picture
  - "附件/文件" → files, attachments
  - "内容" → content
  - "标题" → title
  - "排序" → sort, order
- 如果 prop 已经是规范命名（非 field_xxx），可保持不变

### placeholder 占位提示
- 输入类组件："请输入XXX"
- 选择类组件："请选择XXX"
- 日期类组件："请选择日期"/"选择开始日期"/"选择结束日期"
- 上传类组件：不需要 placeholder

### required 必填判断
- **必填**：用户名、密码、手机号、邮箱、名称、标题等核心字段
- **非必填**：备注、描述、简介等补充字段

## 卡片/图表组件优化规则

### statsCard (统计卡片)
- props.title: 简洁的统计指标名称
- props.value: 合理的示例数值
- props.unit: 适当的单位（人、次、元、件等）
- props.icon: 合适的图标名（user, view, money, goods, order 等）
- props.description: "较昨日"、"较上周"等对比描述
- props.trend: { value: 百分比数值, isUp: 是否上涨 }

### lineStatsCard / barStatsCard (趋势统计卡片)
- props.title: 描述趋势的标题
- props.chartData: 7个合理的数值组成的数组，呈现合理趋势
- props.stats: [{ label: '今日', value: '数值' }, { label: '本周', value: '数值' }]

### pieChartCard (饼图卡片)
- props.title: 分布/占比类标题
- props.data: [{ name: '分类名', value: 数值 }]，3-5个分类，数值要合理

### lineChartCard / barChartCard (图表卡片)
- props.title: 图表标题
- props.value: 汇总数值
- props.unit: 单位
- props.data: 7个数值的数组
- props.smooth: true (折线图)
- props.showAreaColor: true (折线图)

### tableCard (表格卡片)
- props.title: 表格标题
- props.columns: 根据业务场景设计合理的列
- props.data: 2-3条示例数据

### dataListCard (数据列表卡片)
- props.title: 列表标题
- props.list: [{ title, status, time, icon }]

## 输出格式

返回 JSON 数组，每个元素包含：
- id: 组件ID
- 其他需要更新的字段（prop, placeholder, required, props）

注意：props 是嵌套对象，需要完整返回要更新的 props 对象。

示例输出：
[
  { "id": "input_xxx", "prop": "userName", "placeholder": "请输入用户名", "required": true },
  { "id": "statsCard_xxx", "props": { "title": "今日访问量", "value": 12580, "unit": "次", "icon": "view", "trend": { "value": 15.2, "isUp": true }, "description": "较昨日" } },
  { "id": "pieChartCard_xxx", "props": { "title": "订单来源分布", "data": [{ "name": "PC端", "value": 450 }, { "name": "移动端", "value": 380 }, { "name": "小程序", "value": 220 }] } }
]

只返回 JSON 数组，不要添加任何解释文字或 markdown 标记。`
}

// 开始优化
async function handleOptimize() {
  if (optimizableItems.value.length === 0 || optimizeOptions.value.length === 0) return

  error.value = ''
  optimizedItems.value = []

  // 构建请求内容，包含更多组件信息
  const itemsInfo = optimizableItems.value.map((item) => {
    const info: Record<string, any> = {
      id: item.id,
      type: item.type,
      label: item.label,
      isFormComponent: isFormComponentType(item.type)
    }

    // 表单组件添加 prop 信息
    if (isFormComponentType(item.type)) {
      info.currentProp = item.prop
      info.currentPlaceholder = item.placeholder || ''
      info.currentRequired = item.required || false
    }

    // 添加当前的 props 信息（用于优化组件属性）
    if (item.props && Object.keys(item.props).length > 0) {
      info.currentProps = item.props
    }

    return info
  })

  const prompt = `请优化以下组件的属性：
${JSON.stringify(itemsInfo, null, 2)}

需要优化的属性：${optimizeOptions.value.join(', ')}

注意：
- isFormComponent=true 的是表单组件，需要优化 prop, placeholder, required
- isFormComponent=false 的是卡片/图表组件，主要优化 props 内的属性
- 根据组件的 label 和 type 生成符合业务场景的配置`

  try {
    const response = await sendMessage(prompt)

    // 解析 JSON 响应
    const jsonMatch = response.match(/\[[\s\S]*\]/)
    if (!jsonMatch) {
      throw new Error('AI 返回的格式不正确，请重试')
    }

    const updates = JSON.parse(jsonMatch[0]) as Array<{
      id: string
      prop?: string
      placeholder?: string
      required?: boolean
      props?: Record<string, any>
    }>

    // 构建优化结果预览
    optimizedItems.value = updates
      .map((update) => {
        const originalItem = optimizableItems.value.find((item) => item.id === update.id)
        if (!originalItem) return null

        const changes: OptimizedItem['changes'] = {}
        const isFormComp = isFormComponentType(originalItem.type)

        // 表单组件的优化选项
        if (isFormComp) {
          if (optimizeOptions.value.includes('prop') && update.prop && update.prop !== originalItem.prop) {
            changes.prop = update.prop
          }
          if (optimizeOptions.value.includes('placeholder') && update.placeholder) {
            changes.placeholder = update.placeholder
          }
          if (optimizeOptions.value.includes('required') && update.required !== undefined) {
            changes.required = update.required
          }
        }

        // 所有组件都可以优化 props
        if (optimizeOptions.value.includes('props') && update.props && Object.keys(update.props).length > 0) {
          changes.props = update.props
        }

        // 如果没有任何变更，跳过
        if (Object.keys(changes).length === 0) return null

        return {
          id: originalItem.id,
          type: originalItem.type,
          label: originalItem.label,
          oldProp: originalItem.prop || '',
          changes
        }
      })
      .filter(Boolean) as OptimizedItem[]

    if (optimizedItems.value.length === 0) {
      error.value = '所有组件已经是最优状态，无需优化'
    }
  } catch (err: any) {
    error.value = err.message || '优化失败，请重试'
    console.error('AI 优化失败:', err)
  }
}

// 应用优化
function handleApply() {
  if (optimizedItems.value.length === 0) return

  const updates = optimizedItems.value.map((item) => {
    const originalItem = optimizableItems.value.find((i) => i.id === item.id)
    const updateData: Partial<FormItemSchema> = {}

    // 复制基础字段
    if (item.changes.prop) updateData.prop = item.changes.prop
    if (item.changes.placeholder) updateData.placeholder = item.changes.placeholder
    if (item.changes.required !== undefined) updateData.required = item.changes.required

    // 合并 props（保留原有属性，覆盖新属性）
    if (item.changes.props) {
      updateData.props = {
        ...(originalItem?.props || {}),
        ...item.changes.props
      }
    }

    return { id: item.id, updates: updateData }
  })

  emit('optimize', updates)
  visible.value = false
}

// 弹窗关闭时重置状态
function handleClosed() {
  optimizedItems.value = []
  error.value = ''
}

// 监听弹窗打开，重置状态
watch(visible, (val) => {
  if (val) {
    optimizedItems.value = []
    error.value = ''
  }
})
</script>

<style scoped lang="scss">
.ai-optimize-content {
  .optimize-options {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: var(--el-fill-color-light);
    border-radius: 8px;

    .options-label {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin-right: 16px;
      flex-shrink: 0;
    }
  }

  .current-items {
    margin-bottom: 16px;
    padding: 16px;
    background: var(--el-fill-color-light);
    border-radius: 8px;

    .items-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .items-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }

      .items-count {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }

    .items-list {
      max-height: 150px;
      overflow-y: auto;

      .item-row {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 10px;
        margin-bottom: 4px;
        background: var(--el-bg-color);
        border-radius: 4px;
        font-size: 13px;

        .item-label {
          flex: 1;
          color: var(--el-text-color-regular);
        }

        .item-prop {
          color: var(--el-text-color-placeholder);
          font-family: monospace;
          font-size: 12px;
        }
      }
    }
  }

  .result-section {
    margin-top: 20px;
    padding: 16px;
    background: var(--el-color-success-light-9);
    border: 1px solid var(--el-color-success-light-5);
    border-radius: 8px;

    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .result-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--el-color-success);
      }
    }

    .result-list {
      max-height: 200px;
      overflow-y: auto;

      .result-item {
        padding: 10px 12px;
        margin-bottom: 8px;
        background: var(--el-bg-color);
        border-radius: 6px;

        &:last-child {
          margin-bottom: 0;
        }

        .result-label {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          font-size: 14px;
          font-weight: 500;
        }

        .result-changes {
          .change-row {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 4px 0;
            font-size: 13px;

            .change-label {
              color: var(--el-text-color-secondary);
              min-width: 50px;
              flex-shrink: 0;
            }

            .change-old {
              color: var(--el-text-color-placeholder);
              text-decoration: line-through;
              font-family: monospace;
            }

            .change-arrow {
              color: var(--el-color-success);
            }

            .change-new {
              color: var(--el-color-success);
              font-family: monospace;
              font-weight: 500;
            }

            &.props-change {
              align-items: flex-start;

              .props-list {
                display: flex;
                flex-wrap: wrap;
                gap: 4px;

                .prop-tag {
                  font-family: monospace;
                  font-size: 11px;
                }
              }
            }
          }
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
