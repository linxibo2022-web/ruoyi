<!--
  ADetail 通用详情弹窗/抽屉组件

  使用示例：

  1. 基本弹窗使用
  <ADetail
    v-model="visible"
    title="用户详情"
    :data="userData"
    :fields="userFields"
  />

  2. 抽屉模式显示
  <ADetail
    v-model="visible"
    title="用户详情"
    :data="userData"
    :fields="userFields"
    mode="drawer"
    size="large"
  />

  3. 密码字段显示
  <ADetail
    v-model="visible"
    title="平台配置详情"
    :data="platformData"
    :fields="[
      { prop: 'name', label: '名称' },
      { prop: 'secret', label: '密钥', type: 'password' },
      { prop: 'token', label: '接口token', type: 'password' }
    ]"
  />

  4. 分组显示
  <ADetail
    v-model="visible"
    title="支付配置详情"
    :data="paymentData"
    :fields="[
      { prop: 'name', label: '姓名', group: '基本信息' },
      { prop: 'phone', label: '手机', group: '基本信息' },
      { prop: 'mchKey', label: '商户密钥', type: 'password', group: '密钥信息' }
    ]"
    mode="drawer"
    direction="rtl"
  />

  5. 自定义插槽
  <ADetail
    v-model="visible"
    title="订单详情"
    :data="orderData"
    :fields="orderFields"
  >
    <template #content>
      <h4>订单商品</h4>
      <el-table :data="orderData.items" border>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" />
      </el-table>
    </template>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleEdit">编辑</el-button>
    </template>
  </ADetail>

  6. 地区字段显示
  <ADetail
    v-model="visible"
    title="用户详情"
    :data="userData"
    :fields="[
      { prop: 'name', label: '姓名' },
      { prop: 'areaCode', label: '所在地区', type: 'region' }
    ]"
  />
  // areaCode 为地区编码（如 '110101'），会自动显示为 '北京市 / 市辖区 / 东城区'
-->

<template>
  <AModal
    v-model="visible"
    :title="title"
    :mode="mode"
    :size="size"
    :width="width"
    :direction="direction"
    :closable="closable"
    :mask-closable="maskClosable"
    :keyboard="keyboard"
    :destroy-on-close="destroyOnClose"
    :append-to-body="appendToBody"
    :before-close="beforeClose"
    :fullscreen="fullscreen"
    :show-footer="showFooter"
    :footer-type="footerType"
    :footer-align="footerAlign"
    :loading="loading"
    :confirm-text="confirmText"
    :cancel-text="cancelText"
    @confirm="handleConfirm"
    @cancel="handleCancel"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 自定义标题插槽 -->
    <template v-if="$slots.header" #header>
      <slot name="header"></slot>
    </template>

    <!-- 详情内容区域 -->
    <div :id="printElementId" class="detail-content">
      <!-- 分组显示模式 -->
      <div v-if="hasGroups" class="grouped-content">
        <div v-for="group in groupedFields" :key="group.title" class="field-group">
          <h4 class="group-title">{{ group.title }}</h4>
          <el-descriptions :column="group.column || column" :border="border" :size="descriptionSize" :label-width="labelWidth">
            <el-descriptions-item v-for="field in group.fields" :key="field.prop" :label="field.label" :span="field.span">
              <template v-if="field.slot">
                <slot :name="field.slot" :data="data" :field="field" :value="getFieldValue(field)"></slot>
              </template>
              <template v-else-if="field.type === 'password'">
                <div v-if="getFieldValue(field)" class="flex items-center gap-2">
                  <span class="flex-1 min-w-0 break-all">
                    {{ passwordVisible[field.prop] ? getFieldValue(field) : '******' }}
                  </span>
                  <el-button
                    link
                    type="primary"
                    :icon="passwordVisible[field.prop] ? 'Hide' : 'View'"
                    @click="togglePasswordVisibility(field.prop)"
                    class="flex-shrink-0 ml-2 print-hidden"
                  >
                    {{ passwordVisible[field.prop] ? t('detail.hide') : t('detail.show') }}
                  </el-button>
                </div>
                <span v-else>-</span>
              </template>
              <template v-else-if="field.type === 'copyable'">
                <div v-if="getFieldValue(field)" class="flex items-center gap-2">
                  <span class="flex-1 min-w-0 break-all">
                    {{ getFieldValue(field) }}
                  </span>
                  <el-button
                    link
                    type="primary"
                    icon="CopyDocument"
                    @click="copy(getFieldValue(field), t('table.copyFieldSuccess', { field: field.label }))"
                    class="flex-shrink-0 ml-2 print-hidden"
                  >
                    {{ t('detail.copy') }}
                  </el-button>
                </div>
                <span v-else>-</span>
              </template>
              <template v-else-if="field.type === 'html'">
                <div v-if="getFieldValue(field)" class="relative">
                  <div class="max-h-80 overflow-y-auto leading-relaxed py-2" v-html="DOMPurify.sanitize(getFieldValue(field))"></div>
                  <el-button
                    link
                    type="primary"
                    icon="CopyDocument"
                    size="large"
                    class="absolute top-0 right-0 p-1 print-hidden"
                    @click="copy(getFieldValue(field), t('table.copyFieldSuccess', { field: field.label }))"
                    :title="t('detail.copyContent')"
                  />
                </div>
                <span v-else>-</span>
              </template>
              <template v-else-if="field.type === 'file'">
                <el-link v-if="getFieldValue(field)" type="primary" :href="getFieldValue(field)" target="_blank" :download="getFileName(field)">
                  {{ t('detail.viewAttachment') }}
                </el-link>
                <span v-else>-</span>
              </template>
              <template v-else>
                <component :is="getFieldComponent(field)" v-bind="getFieldProps(field)">
                  {{ formatValue(field, getFieldValue(field)) }}
                </component>
              </template>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 普通显示模式 -->
      <el-descriptions v-else :column="column" :border="border" :size="descriptionSize" :label-width="labelWidth">
        <el-descriptions-item v-for="field in visibleFields" :key="field.prop" :label="field.label" :span="field.span">
          <template v-if="field.slot">
            <slot :name="field.slot" :data="data" :field="field" :value="getFieldValue(field)"></slot>
          </template>
          <template v-else-if="field.type === 'password'">
            <div v-if="getFieldValue(field)" class="flex items-center gap-2">
              <span class="flex-1 min-w-0 break-all">
                {{ passwordVisible[field.prop] ? getFieldValue(field) : '******' }}
              </span>
              <el-button
                link
                type="primary"
                :icon="passwordVisible[field.prop] ? 'Hide' : 'View'"
                @click="togglePasswordVisibility(field.prop)"
                class="flex-shrink-0 ml-2 print-hidden"
              >
                {{ passwordVisible[field.prop] ? t('detail.hide') : t('detail.show') }}
              </el-button>
            </div>
            <span v-else>-</span>
          </template>
          <template v-else-if="field.type === 'copyable'">
            <div v-if="getFieldValue(field)" class="flex items-center gap-2">
              <span class="flex-1 min-w-0 break-all">
                {{ getFieldValue(field) }}
              </span>
              <el-button
                link
                type="primary"
                icon="CopyDocument"
                @click="copy(getFieldValue(field), t('table.copyFieldSuccess', { field: field.label }))"
                class="flex-shrink-0 ml-2 print-hidden"
              >
                {{ t('detail.copy') }}
              </el-button>
            </div>
            <span v-else>-</span>
          </template>
          <template v-else-if="field.type === 'html'">
            <div v-if="getFieldValue(field)" class="relative">
              <div class="max-h-80 overflow-y-auto leading-relaxed py-2" v-html="DOMPurify.sanitize(getFieldValue(field))"></div>
              <el-button
                link
                type="primary"
                icon="CopyDocument"
                size="large"
                class="absolute top-0 right-0 p-1 print-hidden"
                @click="copy(getFieldValue(field), t('table.copyFieldSuccess', { field: field.label }))"
                :title="t('detail.copyContent')"
              />
            </div>
            <span v-else>-</span>
          </template>
          <template v-else-if="field.type === 'file'">
            <el-link v-if="getFieldValue(field)" type="primary" :href="getFieldValue(field)" target="_blank" :download="getFileName(field)">
              {{ t('detail.viewAttachment') }}
            </el-link>
            <span v-else>-</span>
          </template>
          <template v-else>
            <component :is="getFieldComponent(field)" v-bind="getFieldProps(field)">
              {{ formatValue(field, getFieldValue(field)) }}
            </component>
          </template>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 自定义内容插槽 -->
      <template v-if="$slots.content">
        <div class="custom-content">
          <slot name="content" :data="data" :fields="fields"></slot>
        </div>
      </template>
    </div>

    <!-- 自定义页脚插槽 -->
    <template v-if="$slots.footer" #footer>
      <slot name="footer" :data="data" :close="handleClose"></slot>
    </template>

    <!-- 默认页脚（包含打印按钮） -->
    <template v-else-if="showFooter" #footer>
      <div class="detail-footer">
        <el-button v-if="showPrint" @click="handlePrint"> {{ t('detail.print') }} </el-button>
        <el-button @click="handleCancel">
          {{ cancelText || t('dialog.close') }}
        </el-button>
      </div>
    </template>
  </AModal>
</template>

<script setup lang="ts" name="ADetail">
import DOMPurify from 'dompurify'
import DictTag from '@/components/DictTag/DictTag.vue'
import ImagePreview from '@/components/ImagePreview/ImagePreview.vue'
import { copy } from '@/utils/function'

const { t } = useI18n()

/** 分组配置接口 */
interface GroupConfig {
  /** 分组标题 */
  title: string
  /** 分组下的字段列表 */
  fields: FieldConfig[]
  /** 分组内的列数 */
  column?: number
}

/** 组件Props类型定义 */
interface ADetailProps {
  /** 弹窗显示状态 */
  modelValue: boolean
  /** 弹窗标题 */
  title: string
  /** 要显示的数据对象 */
  data: Record<string, any>
  /** 字段配置数组 */
  fields: FieldConfig[]

  // ========== AModal 相关属性 ==========
  /** 模态框模式：dialog-对话框，drawer-抽屉 */
  mode?: 'dialog' | 'drawer'
  /** 预设尺寸：small-小，medium-中等，large-大，xl-超大 */
  size?: 'small' | 'medium' | 'large' | 'xl'
  /** 自定义宽度/尺寸 */
  width?: string | number
  /** 抽屉弹出方向 */
  direction?: 'ltr' | 'rtl' | 'ttb' | 'btt'
  /** 是否显示关闭按钮 */
  closable?: boolean
  /** 是否可以通过点击遮罩层关闭 */
  maskClosable?: boolean
  /** 是否可以通过 ESC 键关闭 */
  keyboard?: boolean
  /** 关闭时是否销毁内部元素 */
  destroyOnClose?: boolean
  /** 是否将模态框挂载到 body 元素下 */
  appendToBody?: boolean
  /** 关闭前的回调函数 */
  beforeClose?: (done: () => void) => void
  /** 是否全屏显示 */
  fullscreen?: boolean
  /** 是否显示底部操作区域 */
  showFooter?: boolean
  /** 底部按钮类型：default-确定+取消，close-only-仅关闭按钮 */
  footerType?: 'default' | 'close-only'
  /** 底部按钮对齐方式 */
  footerAlign?: 'left' | 'center' | 'right'
  /** 内容区域是否显示加载状态 */
  loading?: boolean
  /** 确认按钮文本 */
  confirmText?: string
  /** 取消按钮文本 */
  cancelText?: string

  // ========== 详情展示相关属性 ==========
  /** 每行显示的列数 */
  column?: number
  /** 是否显示边框 */
  border?: boolean
  /** 描述列表组件尺寸 */
  descriptionSize?: 'large' | 'default' | 'small'
  /** 标签宽度 */
  labelWidth?: string

  // ========== 打印功能相关属性 ==========
  /** 是否显示打印按钮 */
  showPrint?: boolean
}

/** 组件Props定义 */
const props = withDefaults(defineProps<ADetailProps>(), {
  title: '详情',
  data: () => ({}),
  size: 'large', // 默认使用 large 尺寸，为详情内容提供更充足的空间
  closable: true,
  maskClosable: true,
  keyboard: true,
  destroyOnClose: true,
  appendToBody: true,
  direction: 'rtl',
  fullscreen: false,
  showFooter: true,
  footerType: 'close-only',
  footerAlign: 'right',
  loading: false,
  confirmText: undefined,
  cancelText: undefined,
  column: 1,
  border: true,
  descriptionSize: 'default',
  labelWidth: '120px',
  showPrint: false
})

/** 组件事件定义 */
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'confirm': []
  'cancel': []
  'close': []
  'open': []
  'opened': []
  'closed': []
  'print': [elementId: string]
}>()

/** 弹窗显示状态的双向绑定 */
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/** 生成唯一的打印元素ID */
const printElementId = computed(() => {
  return `detail-print-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
})

/** 密码字段显示状态管理 */
const passwordVisible = ref<Record<string, boolean>>({})

/** 打印功能实例 */
const { printElement } = usePrint()

/** 切换密码显示状态 */
const togglePasswordVisibility = (fieldProp: string) => {
  passwordVisible.value[fieldProp] = !passwordVisible.value[fieldProp]
}

/** 处理打印功能 */
const handlePrint = async () => {
  try {
    await printElement(printElementId.value)
    emit('print', printElementId.value)
  } catch (error) {
    console.error('打印失败:', error)
  }
}

/** 初始化密码字段状态 */
const initPasswordFields = () => {
  const passwordFields = props.fields.filter((field) => field.type === 'password')
  passwordFields.forEach((field) => {
    if (!(field.prop in passwordVisible.value)) {
      passwordVisible.value[field.prop] = false
    }
  })
}

/** 监听字段变化，重新初始化密码状态 */
watch(() => props.fields, initPasswordFields, { immediate: true, deep: true })

/** 监听弹窗关闭，重置密码显示状态 */
watch(visible, (newVisible) => {
  if (!newVisible) {
    // 弹窗关闭时重置所有密码字段为隐藏状态
    Object.keys(passwordVisible.value).forEach((key) => {
      passwordVisible.value[key] = false
    })
  }
})

/**
 * 检测值是否是 JSON 字符串
 */
const isJsonString = (value: any): boolean => {
  if (typeof value !== 'string') {
    return false
  }
  // JSON 字符串必须以 { 或 [ 开头
  const trimmed = value.trim()
  if (!trimmed.startsWith('{') && !trimmed.startsWith('[')) {
    return false
  }
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

/** 过滤可见字段并规范化 span 属性 */
const visibleFields = computed(() => {
  return props.fields
    .filter((field) => {
      if (typeof field.hidden === 'function') {
        return !field.hidden(props.data)
      }
      return !field.hidden
    })
    .map((field) => {
      // 获取字段值
      const value = field.prop.split('.').reduce((obj, key) => obj?.[key], props.data)

      // 自动检测 JSON 并设置类型
      let finalType = field.type

      // 如果字段值是 JSON 字符串，且用户没有指定类型，自动设为 copyable
      if (!field.type && isJsonString(value)) {
        finalType = 'copyable'
      }

      return {
        ...field,
        type: finalType,
        // 确保每个字段都有明确的 span 值，默认为 1
        span: field.span ?? 1
      }
    })
})

/** 判断是否有分组 */
const hasGroups = computed(() => {
  return visibleFields.value.some((field) => field.group)
})

/** 按分组整理字段 */
const groupedFields = computed((): GroupConfig[] => {
  const groups: Record<string, FieldConfig[]> = {}

  visibleFields.value.forEach((field) => {
    const groupName = field.group || t('detail.basicInfo')
    if (!groups[groupName]) {
      groups[groupName] = []
    }
    groups[groupName].push(field)
  })

  return Object.entries(groups).map(([title, fields]) => ({
    title,
    fields,
    column: props.column
  }))
})

/**
 * 获取字段值
 * 支持嵌套属性如 'user.profile.name'
 */
const getFieldValue = (field: FieldConfig) => {
  return field.prop.split('.').reduce((obj, key) => obj?.[key], props.data)
}

/**
 * 获取文件名
 * 从URL中提取文件名
 */
const getFileName = (field: FieldConfig) => {
  const url = getFieldValue(field)
  if (!url) return ''
  try {
    const urlObj = new URL(url)
    const pathname = urlObj.pathname
    return pathname.substring(pathname.lastIndexOf('/') + 1) || 'attachment'
  } catch {
    return 'attachment'
  }
}

/**
 * 获取字段渲染组件
 */
const getFieldComponent = (field: FieldConfig) => {
  if (field.type === 'dict' && field.dictOptions) {
    return DictTag
  }
  if (field.type === 'image') {
    return ImagePreview
  }
  if (field.type === 'region') {
    return DictTag
  }
  return 'span'
}

/**
 * 获取字段组件属性
 */
const getFieldProps = (field: FieldConfig) => {
  if (field.type === 'dict' && field.dictOptions) {
    return {
      options: unref(field.dictOptions),
      value: getFieldValue(field)
    }
  }
  if (field.type === 'image') {
    const defaultConfig = { width: 60, height: 60 }
    const imageConfig = { ...defaultConfig, ...field.imageConfig }
    return {
      src: getFieldValue(field),
      showAll: true,
      ...imageConfig
    }
  }
  if (field.type === 'region') {
    return {
      mode: 'region',
      value: getFieldValue(field)
    }
  }
  return {}
}

/**
 * 格式化显示值
 * 根据字段类型或自定义格式化函数处理显示内容
 */
const formatValue = (field: FieldConfig, value: any): string => {
  // 空值处理
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  // 字典类型、图片类型、密码类型、富文本类型、文件类型、可复制类型和地区类型不需要格式化，由对应组件或模板处理
  if (
    field.type === 'dict' ||
    field.type === 'image' ||
    field.type === 'password' ||
    field.type === 'html' ||
    field.type === 'file' ||
    field.type === 'copyable' ||
    field.type === 'region'
  ) {
    // 对于 copyable 类型，检测是否是 JSON 并格式化
    if (field.type === 'copyable' && isJsonString(value)) {
      try {
        const jsonObj = JSON.parse(value)
        return JSON.stringify(jsonObj, null, 2)
      } catch {
        return String(value)
      }
    }
    return ''
  }

  // 自定义格式化函数优先
  if (field.formatter) {
    return field.formatter(value, props.data)
  }

  // 自动检测并格式化 JSON 字符串
  if (isJsonString(value)) {
    try {
      const jsonObj = JSON.parse(value)
      return JSON.stringify(jsonObj, null, 2)
    } catch {
      return String(value)
    }
  }

  // 根据类型自动格式化
  switch (field.type) {
    case 'date':
      return new Date(value).toLocaleDateString()
    case 'datetime':
      return new Date(value).toLocaleString()
    case 'currency':
      return `¥${Number(value).toLocaleString()}`
    case 'boolean':
      return value ? t('table.booleanYes') : t('table.booleanNo')
    case 'array':
      return Array.isArray(value) ? value.join(', ') : String(value)
    default:
      return String(value)
  }
}

// ========== AModal 事件处理 ==========

/** 确认按钮处理 */
const handleConfirm = () => {
  emit('confirm')
}

/** 取消按钮处理 */
const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

/** 弹窗打开事件 */
const handleOpen = () => {
  emit('open')
}

/** 弹窗打开完成事件 */
const handleOpened = () => {
  emit('opened')
}

/** 关闭弹窗处理 */
const handleClose = () => {
  visible.value = false
  emit('close')
}

/** 弹窗关闭完成事件 */
const handleClosed = () => {
  emit('closed')
}

/** 暴露给父组件的方法 */
defineExpose({
  /** 打开弹窗 */
  open: () => {
    visible.value = true
  },
  /** 关闭弹窗 */
  close: handleClose,
  /** 打印详情 */
  print: handlePrint
})
</script>

<style scoped>
/** 详情内容区域 */
.detail-content {
  position: relative;
}

/** 详情页脚 */
.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/** 分组内容容器 */
.grouped-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/** 字段分组样式 */
.field-group {
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 16px;
}

/** 分组标题样式 */
.group-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 8px;
}

/** 自定义内容区域 */
.custom-content {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/** 弹窗页脚样式 */
.dialog-footer {
  text-align: right;
}

/** 打印时隐藏的元素 */
@media print {
  .print-hidden {
    display: none !important;
  }
}
/** 修复长英文内容导致标签被挤压的问题 */
:deep(.el-descriptions) {
  table-layout: fixed; /* 固定表格布局 */
}

:deep(.el-descriptions__label) {
  min-width: v-bind(labelWidth);
  width: v-bind(labelWidth);
  flex-shrink: 0;
  word-break: keep-all;
  white-space: nowrap;
}

:deep(.el-descriptions__content) {
  word-break: break-all;
  overflow-wrap: break-word;
  max-width: 0; /* 配合 table-layout: fixed 使用 */
}
</style>
