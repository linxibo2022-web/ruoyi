/**
 * 页面设计器类型定义
 */

/** 页面组件类型 */
export type FormItemType =
  // 基础输入
  | 'input'
  | 'textarea'
  | 'number'
  | 'password'
  // 选择组件
  | 'select'
  | 'radio'
  | 'checkbox'
  | 'switch'
  // 日期组件
  | 'date'
  | 'datetime'
  | 'daterange'
  | 'datetimerange'
  | 'time'
  // 高级选择
  | 'cascader'
  | 'treeSelect'
  // 上传组件
  | 'imgUpload'
  | 'fileUpload'
  // 高级输入
  | 'editor'
  // 卡片组件 - 统计类
  | 'statsCard'
  | 'lineStatsCard'
  | 'barStatsCard'
  // 卡片组件 - 图表类
  | 'pieChartCard'
  | 'barChartCard'
  | 'lineChartCard'
  | 'radarChartCard'
  | 'mapChartCard'
  // 卡片组件 - 数据展示类
  | 'dataCard'
  | 'tableCard'
  | 'dataListCard'
  | 'activityCard'
  | 'timelineListCard'
  // 卡片组件 - 用户信息类
  | 'userCard'
  | 'profileCard'
  | 'socialCard'
  // 卡片组件 - 特殊功能类
  | 'formCard'
  | 'pricingCard'
  | 'imageCard'
  | 'infoCard'
  | 'weatherCard'
  | 'notificationCard'
  | 'emptyCard'
  // 图表组件 (独立)
  | 'lineChart'
  | 'barChart'
  | 'pieChart'
  | 'radarChart'
  | 'scatterChart'
  | 'mapChart'
  // 布局组件
  | 'row'
  | 'col'
  | 'divider'
  | 'alert'
  | 'collapse'
  // 展示组件
  | 'icon'

/** 组件分类 */
export type ComponentCategory = '基础组件' | '选择组件' | '日期组件' | '上传组件' | '高级组件' | '卡片组件' | '图表组件' | '布局组件' | '展示组件'

/** 选项数据 */
export interface OptionItem {
  label: string
  value: string | number | boolean
  disabled?: boolean
}

/** 验证规则 */
export interface FormRule {
  required?: boolean
  message?: string
  trigger?: 'blur' | 'change' | ('blur' | 'change')[]
  min?: number
  max?: number
  type?: 'string' | 'number' | 'boolean' | 'array' | 'email' | 'url'
  pattern?: string
  validator?: string // 自定义验证函数名
}

/** 表单项配置 */
export interface FormItemSchema {
  id: string
  type: FormItemType
  prop: string
  label: string
  span?: number | 'auto' | ResponsiveSpan
  required?: boolean
  rules?: FormRule[]
  defaultValue?: any
  placeholder?: string
  tooltip?: string
  disabled?: boolean // 支持表达式
  visible?: boolean | string // 支持表达式
  props?: Record<string, any> // 组件特有属性
  options?: OptionItem[] // 选项数据
  children?: FormItemSchema[] // 嵌套子组件（用于 row/col 布局容器）
}

/** 响应式布局 */
export interface ResponsiveSpan {
  xs?: number
  sm?: number
  md?: number
  lg?: number
  xl?: number
}

/** 表单布局类型 */
export type FormLayoutType = 'dialog' | 'drawer' | 'page'

/** 弹窗尺寸 */
export type DialogSize = 'small' | 'medium' | 'large' | 'xl'

/** 表单配置 */
export interface FormSchema {
  name: string
  description?: string
  labelWidth: string
  labelPosition: 'left' | 'right' | 'top'
  layout: FormLayoutType
  dialogSize?: DialogSize
  drawerDirection?: 'ltr' | 'rtl' | 'ttb' | 'btt'
  items: FormItemSchema[]
  gutter?: number
}

/** 属性配置项类型 */
export type PropConfigType =
  | 'input'
  | 'number'
  | 'switch'
  | 'select'
  | 'radio'
  | 'options-editor'
  | 'rules-editor'
  | 'span-editor'
  | 'dict-select'
  | 'icon-select'

/** 属性配置项 */
export interface PropConfig {
  prop: string
  label: string
  type: PropConfigType
  defaultValue?: any
  options?: OptionItem[]
  min?: number
  max?: number
  step?: number
  placeholder?: string
  tooltip?: string
  showCondition?: (item: FormItemSchema) => boolean
}

/** 组件配置 */
export interface ComponentConfig {
  type: FormItemType
  name: string
  icon: string
  category: ComponentCategory
  defaultProps: Record<string, any>
  propsConfig: PropConfig[]
}

/** 历史记录项 */
export interface HistoryRecord {
  timestamp: number
  schema: FormSchema
  action: string
}

/** 设计器状态 */
export interface DesignerState {
  schema: FormSchema
  selectedId: string | null
  history: HistoryRecord[]
  historyIndex: number
  isDragging: boolean
  clipboard: FormItemSchema | null
}

// ==================== 组件分类常量 ====================

/** 表单组件类型（需要 el-form 包裹，有 v-model） */
export const FORM_COMPONENT_TYPES: FormItemType[] = [
  'input', 'textarea', 'number', 'password',
  'select', 'radio', 'checkbox', 'switch',
  'date', 'datetime', 'daterange', 'datetimerange', 'time',
  'cascader', 'treeSelect',
  'imgUpload', 'fileUpload',
  'editor'
]

/** 容器组件类型（可嵌套子组件） */
export const CONTAINER_TYPES: FormItemType[] = ['row', 'col']

/** 布局组件类型（默认占整行） */
export const LAYOUT_COMPONENT_TYPES: FormItemType[] = [
  'row', 'col', 'divider', 'alert', 'collapse'
]

/** 独立图表组件类型 */
export const CHART_COMPONENT_TYPES: FormItemType[] = [
  'lineChart', 'barChart', 'pieChart', 'radarChart',
  'scatterChart', 'mapChart'
]

/** 非表单组件类型（不需要 v-model 和 label） */
export const NON_FORM_COMPONENT_TYPES: FormItemType[] = [
  // 卡片组件 - 统计类
  'statsCard', 'lineStatsCard', 'barStatsCard',
  // 卡片组件 - 图表类
  'pieChartCard', 'barChartCard', 'lineChartCard', 'radarChartCard', 'mapChartCard',
  // 卡片组件 - 数据展示类
  'dataCard', 'tableCard', 'dataListCard', 'activityCard', 'timelineListCard',
  // 卡片组件 - 用户信息类
  'userCard', 'profileCard', 'socialCard',
  // 卡片组件 - 特殊功能类
  'formCard', 'pricingCard', 'imageCard', 'infoCard', 'weatherCard', 'notificationCard', 'emptyCard',
  // 图表组件
  'lineChart', 'barChart', 'pieChart', 'radarChart', 'scatterChart', 'mapChart',
  // 布局组件
  'row', 'col', 'divider', 'alert', 'collapse',
  // 展示组件
  'icon'
]

/** 判断是否为表单组件 */
export function isFormComponent(type: FormItemType): boolean {
  return FORM_COMPONENT_TYPES.includes(type)
}

/** 递归判断 items 中是否包含表单组件 */
export function hasFormComponents(items: FormItemSchema[]): boolean {
  for (const item of items) {
    if (isFormComponent(item.type)) {
      return true
    }
    // 递归检查容器内的子组件
    if (item.children && item.children.length > 0) {
      if (hasFormComponents(item.children)) {
        return true
      }
    }
  }
  return false
}

/** 获取组件的 span 值 */
export function getItemSpan(item: FormItemSchema): number {
  // 列容器的 span 在 props.span 里
  if (item.type === 'col') {
    const span = item.props?.span
    if (typeof span === 'number') return span
    return 12
  }
  // 行容器占满整行
  if (item.type === 'row') {
    return 24
  }
  // 其他组件的 span 在 item.span 里
  const span = item.span
  if (typeof span === 'number') return span
  if (span === 'auto') return 12
  return 12
}

/** 根据 span 计算宽度百分比 */
export function getItemWidth(item: FormItemSchema): string {
  const spanValue = getItemSpan(item)
  return `${(spanValue / 24) * 100}%`
}
