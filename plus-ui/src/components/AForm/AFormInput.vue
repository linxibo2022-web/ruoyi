<!--用法示例
// 传统固定span用法（数字）
<AFormInput label="标签" v-model="queryParams.value" prop="value" :span="12"></AFormInput>

// 固定span用法（字符串，自动转换为数字）
<AFormInput label="标签" v-model="queryParams.value" prop="value" span="12"></AFormInput>

// 显示字数统计（需要设置maxlength）
<AFormInput
  label="备注"
  v-model="form.remark"
  prop="remark"
  type="textarea"
  :maxlength="200"
  show-word-limit
></AFormInput>

// 响应式span用法 - 完整配置
<AFormInput
  label="用户名"
  v-model="form.userName"
  prop="userName"
  :span="{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }"
>
</AFormInput>

// 响应式span用法 - 部分配置（其他会使用默认值24）
<AFormInput
  label="密码"
  v-model="form.password"
  prop="password"
  type="password"
  show-password
  :span="{ md: 12, lg: 8 }"
>
</AFormInput>

// 预设响应式配置
<AFormInput
  label="邮箱"
  v-model="form.email"
  prop="email"
  span="auto"
>
</AFormInput>

// 防止自动填充密码（推荐用于密码输入框）
<AFormInput
  label="密码"
  v-model="form.password"
  prop="password"
  type="password"
  show-password
  prevent-autofill
>
</AFormInput>

// 自动去除首尾空格（失去焦点时触发）
<AFormInput
  label="用户名"
  v-model="form.userName"
  prop="userName"
  trim
>
</AFormInput>

// 一般用于搜索栏（不需要响应式）
<AFormInput label="标签" v-model="queryParams.value" prop="value" @input="handleQuery"></AFormInput>

// 不含el-form-item容器的简单输入框
<AFormInput label="标签" v-model="queryParams.value" :show-form-item="false"></AFormInput>

//支持插槽的输入框
<AFormInput label="标签" v-model="queryParams.value" prop="value" :span="12">
  <template #append>
    <el-button>按钮</el-button>
  </template>
</AFormInput>

// 前缀图标和后缀图标
<AFormInput
  label="搜索"
  v-model="form.keyword"
  prop="keyword"
  prefix-icon="search"
></AFormInput>

<AFormInput
  label="用户名"
  v-model="form.userName"
  prop="userName"
  prefix-icon="user"
  suffix-icon="edit"
></AFormInput>
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

      <!-- 文本输入框 -->
      <el-input
        v-if="type !== 'number'"
        v-model="inputValue"
        :type="type"
        :autosize="autosize"
        :show-password="showPassword"
        :maxlength="maxlength"
        :show-word-limit="showWordLimit"
        :disabled="disabled"
        :placeholder="placeholder || `${t('placeholder.input')}${computedLabel}`"
        :clearable="clearable"
        :size="size"
        :rows="rows"
        :readonly="isReadonly"
        :style="width ? { width: typeof width === 'number' ? `${width}px` : width } : {}"
        @focus="handleFocus"
        @blur="handleBlur"
        @change="handleChange"
        @keyup.enter="handleEnter"
        @clear="handleClear"
      >
        <!-- 前缀插槽 -->
        <template v-if="$slots.prepend" #prepend>
          <slot name="prepend"></slot>
        </template>
        <!-- 后缀插槽 -->
        <template v-if="$slots.append" #append>
          <slot name="append"></slot>
        </template>
        <!-- 前缀图标插槽 -->
        <template v-if="$slots.prefix || prefixIcon" #prefix>
          <slot name="prefix">
            <Icon v-if="prefixIcon" :code="prefixIcon as IconCode" />
          </slot>
        </template>
        <!-- 后缀图标插槽 -->
        <template v-if="$slots.suffix || suffixIcon" #suffix>
          <slot name="suffix">
            <Icon v-if="suffixIcon" :code="suffixIcon as IconCode" />
          </slot>
        </template>
      </el-input>

      <!-- 数字输入框 -->
      <el-input-number
        v-if="type === 'number'"
        v-model="numberValue"
        :step="step"
        :min="min"
        :max="max"
        :step-strictly="stepStrictly"
        :precision="precision"
        :size="inputNumberSize"
        :controls="controls"
        :controls-position="controlsPosition"
        :placeholder="placeholder || `${t('placeholder.input')}${computedLabel}`"
        :disabled="disabled"
        :style="width ? { width: typeof width === 'number' ? `${width}px` : width } : { width: '100%' }"
        @blur="handleBlur"
        @change="handleChange"
      ></el-input-number>
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

    <!-- 文本输入框 -->
    <el-input
      v-if="type !== 'number'"
      v-model="inputValue"
      :type="type"
      :autosize="autosize"
      :show-password="showPassword"
      :maxlength="maxlength"
      :show-word-limit="showWordLimit"
      :disabled="disabled"
      :placeholder="placeholder || `${t('placeholder.input')}${computedLabel}`"
      :clearable="clearable"
      :size="size"
      :rows="rows"
      :readonly="isReadonly"
      :style="width ? { width: typeof width === 'number' ? `${width}px` : width } : {}"
      @focus="handleFocus"
      @blur="handleBlur"
      @change="handleChange"
      @keyup.enter="handleEnter"
      @clear="handleClear"
    >
      <!-- 前缀插槽 -->
      <template v-if="$slots.prepend" #prepend>
        <slot name="prepend"></slot>
      </template>
      <!-- 后缀插槽 -->
      <template v-if="$slots.append" #append>
        <slot name="append"></slot>
      </template>
      <!-- 前缀图标插槽 -->
      <template v-if="$slots.prefix || prefixIcon" #prefix>
        <slot name="prefix">
          <Icon v-if="prefixIcon" :code="prefixIcon as IconCode" />
        </slot>
      </template>
      <!-- 后缀图标插槽 -->
      <template v-if="$slots.suffix || suffixIcon" #suffix>
        <slot name="suffix">
          <Icon v-if="suffixIcon" :code="suffixIcon as IconCode" />
        </slot>
      </template>
    </el-input>

    <!-- 数字输入框 -->
    <el-input-number
      v-if="type === 'number'"
      v-model="numberValue"
      :step="step"
      :min="min"
      :max="max"
      :step-strictly="stepStrictly"
      :precision="precision"
      :size="inputNumberSize"
      :controls="controls"
      :controls-position="controlsPosition"
      :placeholder="placeholder || `${t('placeholder.input')}${computedLabel}`"
      :disabled="disabled"
      :style="width ? { width: typeof width === 'number' ? `${width}px` : width } : { width: '100%' }"
      @blur="handleBlur"
      @change="handleChange"
    ></el-input-number>
  </el-form-item>

  <!--showFormItem为false 只显示输入框-->
  <template v-else>
    <!-- 文本输入框 -->
    <el-input
      v-if="type !== 'number'"
      v-model="inputValue"
      :type="type"
      :autosize="autosize"
      :show-password="showPassword"
      :maxlength="maxlength"
      :show-word-limit="showWordLimit"
      :disabled="disabled"
      :placeholder="placeholder || `${t('placeholder.input')}${computedLabel}`"
      :clearable="clearable"
      :size="size"
      :rows="rows"
      :readonly="isReadonly"
      :style="width ? { width: typeof width === 'number' ? `${width}px` : width } : {}"
      @focus="handleFocus"
      @blur="handleBlur"
      @change="handleChange"
      @keyup.enter="handleEnter"
      @clear="handleClear"
    >
      <!-- 前缀插槽 -->
      <template v-if="$slots.prepend" #prepend>
        <slot name="prepend"></slot>
      </template>
      <!-- 后缀插槽 -->
      <template v-if="$slots.append" #append>
        <slot name="append"></slot>
      </template>
      <!-- 前缀图标插槽 -->
      <template v-if="$slots.prefix || prefixIcon" #prefix>
        <slot name="prefix">
          <Icon v-if="prefixIcon" :code="prefixIcon as IconCode" />
        </slot>
      </template>
      <!-- 后缀图标插槽 -->
      <template v-if="$slots.suffix || suffixIcon" #suffix>
        <slot name="suffix">
          <Icon v-if="suffixIcon" :code="suffixIcon as IconCode" />
        </slot>
      </template>
    </el-input>

    <!-- 数字输入框 -->
    <el-input-number
      v-if="type === 'number'"
      v-model="numberValue"
      :step="step"
      :min="min"
      :max="max"
      :step-strictly="stepStrictly"
      :precision="precision"
      :size="inputNumberSize"
      :controls="controls"
      :controls-position="controlsPosition"
      :placeholder="placeholder || `${t('placeholder.input')}${computedLabel}`"
      :disabled="disabled"
      :style="width ? { width: typeof width === 'number' ? `${width}px` : width } : { width: '100%' }"
      @blur="handleBlur"
      @change="handleChange"
    ></el-input-number>
  </template>
</template>

<script setup lang="ts" name="AFormInput">
const { t, te, isChinese } = useI18n()

/**
 * 输入组件的Props接口定义
 * @description 定义了输入组件的所有属性和类型
 */
interface AFormInputProps {
  /**
   * 绑定值，支持字符串或数字
   * @default undefined
   */
  modelValue?: string | number | null | undefined

  /**
   * 标签文本
   * @default ''
   */
  label?: string

  /**
   * 标签宽度，支持数字或字符串
   * @default undefined
   */
  labelWidth?: number | string

  /**
   * 占位符文本
   * @default ''
   */
  placeholder?: string

  /**
   * 组件宽度，支持数字或字符串
   * @default undefined
   */
  width?: number | string

  /**
   * 表单域model数据字段名
   * @default ''
   */
  prop?: string

  /**
   * 最大长度
   * @default 255
   */
  maxlength?: number | string

  /**
   * 是否显示字数统计
   * @default false
   */
  showWordLimit?: boolean

  /**
   * 是否显示表单项
   * @default true
   */
  showFormItem?: boolean

  /**
   * 是否显示密码可见性切换按钮
   * @default false
   */
  showPassword?: boolean

  /**
   * 输入框类型
   * @default 'text'
   */
  type?: 'text' | 'textarea' | 'number' | 'password'

  /**
   * 文本域自适应配置
   * @default { minRows: 2, maxRows: 30 }
   */
  autosize?: { minRows?: number; maxRows?: number }

  /**
   * 组件尺寸
   * @default ''
   */
  size?: '' | 'default' | 'small' | 'large'

  /**
   * 栅格占据的列数，支持数字、数字字符串、响应式对象或预设字符串
   * - 数字：固定span值，如 12
   * - 数字字符串：如 "12"，会自动转换为数字
   * - 响应式对象：{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }
   * - 预设字符串：'auto' - 自动响应式布局
   * @default undefined
   */
  span?: SpanType

  /**
   * 是否显示清除按钮
   * @default true
   */
  clearable?: boolean

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean

  /**
   * 文本域行数
   * @default 3
   */
  rows?: number

  /**
   * 提示信息
   * @default ''
   */
  tooltip?: string

  // 数字输入框特有属性
  /**
   * 数字输入框最小值
   * @default undefined
   */
  min?: number

  /**
   * 数字输入框最大值
   * @default undefined
   */
  max?: number

  /**
   * 数字输入框步长
   * @default 1
   */
  step?: number

  /**
   * 是否只能输入 step 的倍数
   * @default false
   */
  stepStrictly?: boolean

  /**
   * 数值精度
   * @default undefined
   */
  precision?: number

  /**
   * 是否使用控制按钮
   * @default true
   */
  controls?: boolean

  /**
   * 控制按钮位置
   * @default ''
   */
  controlsPosition?: '' | 'right'

  /**
   * 数字输入框尺寸
   * @default ''
   */
  inputNumberSize?: '' | 'default' | 'small' | 'large'

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

  /**
   * 是否防止浏览器自动填充
   * 主要用于密码输入框，防止浏览器自动填充密码
   * @default false
   */
  preventAutofill?: boolean

  /**
   * 前缀图标
   * 输入框头部图标，使用 Icon 组件的 code
   * @default undefined
   */
  prefixIcon?: string

  /**
   * 后缀图标
   * 输入框尾部图标，使用 Icon 组件的 code
   * @default undefined
   */
  suffixIcon?: string

  /**
   * 是否在失去焦点时自动去除首尾空格
   * @default false
   */
  trim?: boolean
}

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormInputProps>(), {
  modelValue: undefined,
  label: '',
  labelWidth: undefined,
  placeholder: '',
  width: undefined,
  prop: '',
  maxlength: undefined,
  showWordLimit: true,
  showFormItem: true,
  showPassword: false,
  type: 'text',
  autosize: () => ({
    minRows: 2,
    maxRows: 30
  }),
  size: '',
  span: undefined,
  clearable: true,
  disabled: false,
  rows: 3,
  tooltip: '',
  min: undefined,
  max: undefined,
  step: 1,
  stepStrictly: false,
  precision: undefined,
  controls: true,
  controlsPosition: '',
  inputNumberSize: '',
  preventAutofill: false,
  prefixIcon: undefined,
  suffixIcon: undefined,
  trim: false
})

const emit = defineEmits(['update:modelValue', 'input', 'blur', 'change', 'enter'])

// ========== 使用智能响应式逻辑 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

// ========== 防自动填充逻辑 ==========
/**
 * 控制输入框的只读状态
 * 当启用防自动填充且类型为密码时，初始设置为只读
 * 用户聚焦时会移除只读状态，从而防止浏览器自动填充
 */
const isReadonly = ref(props.preventAutofill && props.type === 'password')

/**
 * 处理聚焦事件
 * 如果启用了防自动填充功能，在聚焦时移除 readonly 属性
 * @param event 聚焦事件
 */
const handleFocus = (event: FocusEvent) => {
  // 如果启用了防自动填充且是密码类型
  if (props.preventAutofill && props.type === 'password') {
    isReadonly.value = false
    // 确保从 DOM 元素上移除 readonly 属性
    const target = event.target as HTMLInputElement
    if (target) {
      target.removeAttribute('readonly')
    }
  }
}

// ========== 原有逻辑保持不变 ==========

// 文本输入框的计算属性
const inputValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
    emit('input', value)
  }
})

// 数字输入框的计算属性
const numberValue = computed({
  get() {
    return props.modelValue as number
  },
  set(value: number) {
    emit('update:modelValue', value)
    emit('input', value)
  }
})

// 使用计算属性计算label标签显示国际化
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

/**
 * 处理失去焦点事件
 * @param val 当前值
 */
const handleBlur = (val: any) => {
  // 如果启用了 trim 功能且值为字符串类型，则去除首尾空格
  if (props.trim && typeof props.modelValue === 'string') {
    const trimmedValue = props.modelValue.trim()
    if (trimmedValue !== props.modelValue) {
      emit('update:modelValue', trimmedValue)
      emit('input', trimmedValue)
    }
  }
  emit('blur', val)
}

/**
 * 处理变化事件
 * @param val 变化后的值
 */
const handleChange = (val: any) => {
  emit('change', val)
}

/**
 * 处理回车事件
 * @param val 当前值
 */
const handleEnter = (val: any) => {
  emit('enter', val)
}

/**
 * 处理清除事件
 */
const handleClear = () => {
  emit('update:modelValue', '')
}
</script>

<style>
/* 改变禁用状态下输入框的文字颜色为黑色 */
.el-input.is-disabled .el-input__inner {
  color: #777777;
  -webkit-text-fill-color: #777777;
}
</style>
