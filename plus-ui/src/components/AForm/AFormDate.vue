<!--用法示例
//一般用于搜索栏
<AFormDate label="创建时间" v-model="queryParams.time" prop="time" @change="handleQuery"></AFormDate>
//一般用于表单
<AFormDate label="创建时间" v-model="queryParams.time" prop="time" :span="12"></AFormDate>
//带提示信息的日期选择器
<AFormDate label="创建时间" v-model="queryParams.time" prop="time" tooltip="请选择创建时间" :span="12"></AFormDate>
//不含el-form-item容器的简单日期选择器
<AFormDate label="创建时间" v-model="queryParams.time" :show-form-item="false"></AFormDate>
//日期范围选择器（默认自动补全时间：开始 00:00:00，结束 23:59:59）
<AFormDate label="时间范围" v-model="queryParams.dateRange" type="daterange" :span="12"></AFormDate>
//带快捷选项的日期选择器
<AFormDate label="日期" v-model="form.date" :shortcuts="dateShortcuts" :span="12"></AFormDate>
//日期范围选择器 - 关闭自动补全时间（仅用于查询 date 类型字段）
<AFormDate label="时间范围" v-model="queryParams.dateRange" type="daterange" :auto-fill-time="false"></AFormDate>
-->
<template>
  <!--span有值且showFormItem为true 显示ElCol ElFormItem-->
  <el-col :span="computedSpan" v-if="shouldUseCol && showFormItem">
    <el-form-item :label-width="labelWidth" :prop="prop">
      <template #label>
        <span>{{ computedLabel }}</span>
        <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
          <el-icon>
            <QuestionFilled />
          </el-icon>
        </el-tooltip>
      </template>

      <el-date-picker
        v-model="dateValue"
        :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
        :type="type"
        :format="dateFormat"
        :value-format="dateValueFormat"
        :range-separator="rangeSeparator"
        :size="size"
        :clearable="clearable"
        :disabled="disabled"
        :readonly="readonly"
        :editable="editable"
        :start-placeholder="startPlaceholder || t('formDate.startDate')"
        :end-placeholder="endPlaceholder || t('formDate.endDate')"
        :time-arrow-control="timeArrowControl"
        :validate-event="validateEvent"
        :disabled-date="disabledDate"
        :shortcuts="shortcuts"
        :default-value="defaultValue"
        :default-time="defaultTime"
        :name="computedName"
        :unlink-panels="unlinkPanels"
        :prefix-icon="prefixIcon"
        :style="{ width: '100%' }"
        @change="handleChange"
        @blur="handleBlur"
        @focus="handleFocus"
        @calendar-change="handleCalendarChange"
        @panel-change="handlePanelChange"
        @visible-change="handleVisibleChange"
      >
      </el-date-picker>
    </el-form-item>
  </el-col>

  <!--span无值且showFormItem为true 显示ElFormItem-->
  <el-form-item :label-width="labelWidth" :prop="prop" v-else-if="!shouldUseCol && showFormItem">
    <template #label>
      <span>{{ computedLabel }}</span>
      <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
        <el-icon>
          <QuestionFilled />
        </el-icon>
      </el-tooltip>
    </template>

    <el-date-picker
      v-model="dateValue"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :type="type"
      :format="dateFormat"
      :value-format="dateValueFormat"
      :range-separator="rangeSeparator"
      :size="size"
      :clearable="clearable"
      :disabled="disabled"
      :readonly="readonly"
      :editable="editable"
      :start-placeholder="startPlaceholder || t('formDate.startDate')"
      :end-placeholder="endPlaceholder || t('formDate.endDate')"
      :time-arrow-control="timeArrowControl"
      :validate-event="validateEvent"
      :disabled-date="disabledDate"
      :shortcuts="shortcuts"
      :default-value="defaultValue"
      :default-time="defaultTime"
      :name="computedName"
      :unlink-panels="unlinkPanels"
      :prefix-icon="prefixIcon"
      :style="{ width: formatUnit(width || 240) }"
      @change="handleChange"
      @blur="handleBlur"
      @focus="handleFocus"
      @calendar-change="handleCalendarChange"
      @panel-change="handlePanelChange"
      @visible-change="handleVisibleChange"
    >
    </el-date-picker>
  </el-form-item>

  <!--showFormItem为false 只显示日期选择器-->
  <template v-else>
    <el-date-picker
      v-model="dateValue"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :type="type"
      :format="dateFormat"
      :value-format="dateValueFormat"
      :range-separator="rangeSeparator"
      :size="size"
      :clearable="clearable"
      :disabled="disabled"
      :readonly="readonly"
      :editable="editable"
      :start-placeholder="startPlaceholder || t('formDate.startDate')"
      :end-placeholder="endPlaceholder || t('formDate.endDate')"
      :time-arrow-control="timeArrowControl"
      :validate-event="validateEvent"
      :disabled-date="disabledDate"
      :shortcuts="shortcuts"
      :default-value="defaultValue"
      :default-time="defaultTime"
      :name="computedName"
      :unlink-panels="unlinkPanels"
      :prefix-icon="prefixIcon"
      :style="{ width: formatUnit(width || 240) }"
      @change="handleChange"
      @blur="handleBlur"
      @focus="handleFocus"
      @calendar-change="handleCalendarChange"
      @panel-change="handlePanelChange"
      @visible-change="handleVisibleChange"
    >
    </el-date-picker>
  </template>
</template>

<script setup lang="ts" name="AFormDate">
import { formatUnit } from '@/utils/format'

const { t, te, isChinese } = useI18n()

/**
 * 日期选择器快捷选项接口
 */
interface DateShortcut {
  text: string
  value: Date | (() => Date) | (() => [Date, Date])
}

/**
 * 日期选择器组件属性接口定义
 */
interface AFormDateProps {
  /** 绑定值，可以是字符串、数字或数组 */
  modelValue?: string | number | any[] | undefined

  /** 日期选择器类型: date/datetime/daterange等 */
  type?: ElDatePickerType

  /** 表单标签文本 */
  label?: string

  /** 表单标签宽度，可以是数字或字符串 */
  labelWidth?: string | number

  /** 组件宽度，可以是数字或字符串 */
  width?: string | number

  /** 占位符文本 */
  placeholder?: string

  /** 表单域model数据字段名 */
  prop?: string

  /** 日期格式化显示格式 */
  format?: string

  /** 日期值格式化，即v-model的格式 */
  valueFormat?: string

  /** 是否显示为表单项，false则不使用ElFormItem包装 */
  showFormItem?: boolean

  /** 组件尺寸 */
  size?: '' | 'default' | 'small' | 'large'

  /**
   * 栅格布局的列占比，支持数字、数字字符串、响应式对象或预设字符串
   * - 数字：固定span值，如 12
   * - 数字字符串：如 "12"，会自动转换为数字
   * - 响应式对象：{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }
   * - 预设字符串：'auto' - 自动响应式布局
   * @default undefined
   */
  span?: SpanType

  /** 是否显示清除按钮 */
  clearable?: boolean

  /** 是否禁用 */
  disabled?: boolean

  /** 是否只读 */
  readonly?: boolean

  /** 是否可编辑 */
  editable?: boolean

  /** 提示信息 */
  tooltip?: string

  /** 选择范围时的分隔符 */
  rangeSeparator?: string

  /** 范围选择时开始日期的占位内容 */
  startPlaceholder?: string

  /** 范围选择时结束日期的占位内容 */
  endPlaceholder?: string

  /** 是否使用箭头进行时间选择 */
  timeArrowControl?: boolean

  /** 输入时是否触发表单的校验 */
  validateEvent?: boolean

  /** 一个用来判断该日期是否被禁用的函数 */
  disabledDate?: (time: Date) => boolean

  /** 设置快捷选项，需要传入数组对象 */
  shortcuts?: DateShortcut[]

  /** 可选，选择器打开时默认显示的时间 */
  defaultValue?: Date | [Date, Date]

  /** 范围选择时选中日期所使用的当日内具体时刻 */
  defaultTime?: Date | [Date, Date]

  /** 原生属性 */
  name?: string | [string, string]

  /** 在范围选择器里取消两个日期面板之间的联动 */
  unlinkPanels?: boolean

  /** 自定义头部图标的类名 */
  prefixIcon?: string

  /**
   * 是否自动补全时间（仅对 daterange 类型有效）
   * - 开始日期自动补 00:00:00
   * - 结束日期自动补 23:59:59
   * 用于查询数据库 datetime 字段时避免结束当天数据丢失
   * @default true
   */
  autoFillTime?: boolean

  /**
   * 响应式模式
   * - 'screen': 基于屏幕尺寸
   * - 'container': 基于容器尺寸（弹窗场景推荐）
   * - 'modal-size': 基于 AModal 的 size 属性
   * @default 'screen'
   */
  responsiveMode?: 'screen' | 'container' | 'modal-size'

  /**
   * 当 responsiveMode 为 'modal-size' 时使用
   * 应该传入 AModal 的 size 属性值
   */
  modalSize?: 'small' | 'medium' | 'large' | 'xl'
}

/**
 * 定义组件属性并设置默认值
 */
const props = withDefaults(defineProps<AFormDateProps>(), {
  modelValue: undefined,
  type: 'datetime',
  label: '',
  labelWidth: undefined,
  width: undefined,
  placeholder: '',
  prop: '',
  format: '',
  valueFormat: '',
  showFormItem: true,
  size: '',
  span: undefined,
  clearable: true,
  disabled: false,
  readonly: false,
  editable: true,
  tooltip: '',
  rangeSeparator: '-',
  startPlaceholder: '',
  endPlaceholder: '',
  timeArrowControl: false,
  validateEvent: true,
  disabledDate: undefined,
  shortcuts: () => [],
  defaultValue: undefined,
  defaultTime: undefined,
  name: undefined,
  unlinkPanels: false,
  prefixIcon: '',
  autoFillTime: true  // 默认开启，修复 daterange 查询 datetime 字段时结束当天数据丢失的问题
})

/** 组件事件定义 */
const emit = defineEmits(['update:modelValue', 'change', 'blur', 'focus', 'calendar-change', 'panel-change', 'visible-change'])

// ========== 根据响应式模式选择对应的组合函数 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

/** 组件数据值，用于双向绑定 */
const dateValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    // autoFillTime 模式下，el-date-picker 通过 defaultTime 自动填充时间
    // 这里直接透传值即可
    emit('update:modelValue', value)
  }
})

/** 使用计算属性计算label标签显示国际化 */
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

/** 日期显示格式 */
const dateFormat = ref('')

/** 日期值格式 */
const dateValueFormat = ref('')

/** 计算name属性 */
const computedName = computed(() => {
  // 如果没有设置name或name为空，则不传递name属性
  if (!props.name || (typeof props.name === 'string' && props.name === '')) {
    return undefined
  }

  // 对于范围选择器，name应该是数组格式
  if (props.type.includes('range')) {
    if (Array.isArray(props.name)) {
      return props.name as [string, string]
    } else {
      return [props.name, props.name] as [string, string]
    }
  }

  // 单选模式返回字符串
  return props.name as string
})
const defaultTime = computed(() => {
  // 如果用户自定义了默认时间，则使用用户的设置
  if (props.defaultTime) {
    return props.defaultTime
  }

  // 只有在日期范围选择器时才设置默认时间
  if (props.type.includes('range')) {
    if (props.type === 'datetimerange') {
      return [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)] as [Date, Date]
    }
    // daterange 类型且启用 autoFillTime 时，开始日期 00:00:00，结束日期 23:59:59
    if (props.type === 'daterange' && props.autoFillTime) {
      return [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)] as [Date, Date]
    }
  }
  return undefined
})

/**
 * 初始化日期格式
 * 根据type类型设置不同的日期显示格式和值格式
 */
const initDateFormat = () => {
  // 如果用户自定义了格式，则优先使用用户的设置
  if (props.format && props.valueFormat) {
    dateFormat.value = props.format
    dateValueFormat.value = props.valueFormat
    return
  }

  switch (props.type) {
    // 不同日期类型的格式设置
    case 'datetime':
      dateFormat.value = props.format || 'YYYY-MM-DD HH:mm:ss'
      dateValueFormat.value = props.valueFormat || 'YYYY-MM-DD HH:mm:ss'
      break
    case 'date':
    case 'dates':
      dateFormat.value = props.format || 'YYYY-MM-DD'
      dateValueFormat.value = props.valueFormat || 'YYYY-MM-DD'
      break
    case 'month':
    case 'months':
      dateFormat.value = props.format || 'YYYY-MM'
      dateValueFormat.value = props.valueFormat || 'YYYY-MM'
      break
    case 'year':
    case 'years':
      dateFormat.value = props.format || 'YYYY'
      dateValueFormat.value = props.valueFormat || 'YYYY'
      break
    case 'week':
      dateFormat.value = props.format || t('formDate.weekFormat')
      dateValueFormat.value = props.valueFormat || 'YYYY-MM-DD'
      break
    case 'daterange':
      dateFormat.value = props.format || 'YYYY-MM-DD'
      // 如果启用 autoFillTime，valueFormat 需要带时间以支持时间补全后的值回显
      dateValueFormat.value = props.valueFormat || (props.autoFillTime ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD')
      break
    case 'datetimerange':
      dateFormat.value = props.format || 'YYYY-MM-DD HH:mm:ss'
      dateValueFormat.value = props.valueFormat || 'YYYY-MM-DD HH:mm:ss'
      break
    case 'monthrange':
      dateFormat.value = props.format || 'YYYY-MM'
      dateValueFormat.value = props.valueFormat || 'YYYY-MM'
      break
    default:
      dateFormat.value = props.format || 'YYYY-MM-DD'
      dateValueFormat.value = props.valueFormat || 'YYYY-MM-DD'
  }
}

/**
 * 日期选择变化处理函数
 * @param val 变化后的日期值
 */
const handleChange = (val: any) => {
  emit('change', val)
}

/**
 * 失去焦点事件
 * @param event 事件对象
 */
const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

/**
 * 获得焦点事件
 * @param event 事件对象
 */
const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

/**
 * 日历面板变化事件
 * @param val 变化后的值
 */
const handleCalendarChange = (val: any) => {
  emit('calendar-change', val)
}

/**
 * 面板变化事件
 * @param date 日期
 * @param mode 模式
 * @param view 视图
 */
const handlePanelChange = (date: any, mode: any, view: any) => {
  emit('panel-change', date, mode, view)
}

/**
 * 日期选择器显示/隐藏事件
 * @param visible 是否可见
 */
const handleVisibleChange = (visible: boolean) => {
  emit('visible-change', visible)
}

// 监听 type 变化，重新初始化格式
watch(
  () => [props.type, props.format, props.valueFormat],
  () => {
    initDateFormat()
  },
  { immediate: true }
)

/**
 * 组件挂载时初始化日期格式
 */
onMounted(() => {
  initDateFormat()
})
</script>

<style>
/* 可以添加自定义样式 */
</style>
