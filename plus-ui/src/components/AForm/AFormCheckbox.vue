<!--用法示例
//一般用于搜索栏
<AFormCheckbox v-model="queryParams.types" :options="type_options" label="类型" prop="types" @change="handleQuery"></AFormCheckbox>
//一般用于表单
<AFormCheckbox v-model="queryParams.types" :options="type_options" label="类型" prop="types" :span="12"></AFormCheckbox>
//带提示信息的复选框
<AFormCheckbox v-model="queryParams.types" :options="type_options" label="类型" prop="types" tooltip="请选择类型" :span="12"></AFormCheckbox>
//不含el-form-item容器的简单复选框
<AFormCheckbox v-model="queryParams.types" :options="type_options" label="类型" :show-form-item="false"></AFormCheckbox>
//按钮样式的复选框
<AFormCheckbox v-model="queryParams.types" :options="type_options" label="类型" type="button" :span="12"></AFormCheckbox>
//自定义字段映射
<AFormCheckbox v-model="form.roleIds" :options="roleList" label="角色" value-field="id" label-field="name" :span="12"></AFormCheckbox>
//数组类型绑定值
<AFormCheckbox v-model="form.permissions" :options="permissionOptions" label="权限" :return-array="true" :span="12"></AFormCheckbox>
//单个复选框模式（适用于记住密码等场景）
<AFormCheckbox v-model="loginForm.rememberMe" :options="[{ label: '记住密码', value: true }]" :single-checkbox="true" :show-form-item="false"></AFormCheckbox>
const type_options = [{ label: '类型1', value: '1' }, { label: '类型2', value: '2' }];
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

      <!-- 单个复选框模式 -->
      <el-checkbox
        v-if="singleCheckbox && optionsData.length === 1"
        v-model="singleCheckboxValue"
        :disabled="disabled"
        :size="size"
        :border="border"
        @change="handleSingleChange"
      >
        {{ getLabelFromOption(optionsData[0]) }}
      </el-checkbox>

      <!-- 复选框组模式 -->
      <el-checkbox-group
        v-else
        v-model="checkboxValue"
        :size="size"
        :disabled="disabled"
        :min="min"
        :max="max"
        :text-color="textColor"
        :fill="fill"
        @change="handleChange"
      >
        <template v-if="type === 'button'">
          <el-checkbox-button
            v-for="option in optionsData"
            :key="`button-${getValueFromOption(option)}`"
            :value="getValueFromOption(option)"
            :disabled="isOptionDisabled(option)"
          >
            {{ getLabelFromOption(option) }}
          </el-checkbox-button>
        </template>

        <template v-else>
          <el-checkbox
            v-for="option in optionsData"
            :key="`checkbox-${getValueFromOption(option)}`"
            :value="getValueFromOption(option)"
            :disabled="isOptionDisabled(option)"
            :border="border"
            :size="size"
          >
            {{ getLabelFromOption(option) }}
          </el-checkbox>
        </template>
      </el-checkbox-group>
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

    <!-- 单个复选框模式 -->
    <el-checkbox
      v-if="singleCheckbox && optionsData.length === 1"
      v-model="singleCheckboxValue"
      :disabled="disabled"
      :size="size"
      :border="border"
      @change="handleSingleChange"
    >
      {{ getLabelFromOption(optionsData[0]) }}
    </el-checkbox>

    <!-- 复选框组模式 -->
    <el-checkbox-group
      v-else
      v-model="checkboxValue"
      :size="size"
      :disabled="disabled"
      :min="min"
      :max="max"
      :text-color="textColor"
      :fill="fill"
      @change="handleChange"
    >
      <template v-if="type === 'button'">
        <el-checkbox-button
          v-for="option in optionsData"
          :key="`button-${getValueFromOption(option)}`"
          :value="getValueFromOption(option)"
          :disabled="isOptionDisabled(option)"
        >
          {{ getLabelFromOption(option) }}
        </el-checkbox-button>
      </template>

      <template v-else>
        <el-checkbox
          v-for="option in optionsData"
          :key="`checkbox-${getValueFromOption(option)}`"
          :value="getValueFromOption(option)"
          :disabled="isOptionDisabled(option)"
          :border="border"
          :size="size"
        >
          {{ getLabelFromOption(option) }}
        </el-checkbox>
      </template>
    </el-checkbox-group>
  </el-form-item>

  <!--showFormItem为false 只显示复选框组-->
  <template v-else>
    <!-- 单个复选框模式 -->
    <el-checkbox
      v-if="singleCheckbox && optionsData.length === 1"
      v-model="singleCheckboxValue"
      :disabled="disabled"
      :size="size"
      :border="border"
      @change="handleSingleChange"
    >
      {{ getLabelFromOption(optionsData[0]) }}
    </el-checkbox>

    <!-- 复选框组模式 -->
    <el-checkbox-group
      v-else
      v-model="checkboxValue"
      :size="size"
      :disabled="disabled"
      :min="min"
      :max="max"
      :text-color="textColor"
      :fill="fill"
      @change="handleChange"
    >
      <template v-if="type === 'button'">
        <el-checkbox-button
          v-for="option in optionsData"
          :key="`button-${getValueFromOption(option)}`"
          :value="getValueFromOption(option)"
          :disabled="isOptionDisabled(option)"
        >
          {{ getLabelFromOption(option) }}
        </el-checkbox-button>
      </template>

      <template v-else>
        <el-checkbox
          v-for="option in optionsData"
          :key="`checkbox-${getValueFromOption(option)}`"
          :value="getValueFromOption(option)"
          :disabled="isOptionDisabled(option)"
          :border="border"
          :size="size"
        >
          {{ getLabelFromOption(option) }}
        </el-checkbox>
      </template>
    </el-checkbox-group>
  </template>
</template>

<script setup lang="ts" name="AFormCheckbox">
const { t, te, isChinese } = useI18n()

/**
 * 获取字典标签的国际化翻译
 * @param dictType 字典类型
 * @param value 字典值
 * @returns 国际化翻译或 null
 */
const getDictI18nLabel = (dictType: string, value: string | number): string | null => {
  const key = `dict.${dictType}.${String(value)}`
  if (te(key)) {
    return t(key)
  }
  return null
}

// 定义禁用条件的类型
type DisabledCondition = string | number | boolean | Array<string | number | boolean> | ((item: any) => boolean)

/**
 * 复选框组组件的Props接口定义
 */
interface AFormCheckboxProps {
  /**
   * 绑定值，支持逗号分隔的字符串或数组
   * @default ''
   */
  modelValue?: string | Array<string | number> | boolean

  /**
   * 标签文本
   * @default ''
   */
  label?: string

  /**
   * 标签宽度，支持数字或字符串
   * @default undefined
   */
  labelWidth?: string | number

  /**
   * 表单域model数据字段名
   * @default ''
   */
  prop?: string

  /**
   * 组件尺寸
   * @default ''
   */
  size?: '' | 'default' | 'small' | 'large'

  /**
   * 复选框选项数组
   * @default []
   */
  options: any[]

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
   * 是否禁用
   * @default false
   */
  disabled?: boolean

  /**
   * 是否显示表单项
   * @default true
   */
  showFormItem?: boolean

  /**
   * 提示信息
   * @default ''
   */
  tooltip?: string

  /**
   * 复选框类型
   * @default 'checkbox'
   */
  type?: 'checkbox' | 'button'

  /**
   * 是否显示边框
   * @default false
   */
  border?: boolean

  /**
   * 选中时的文字颜色
   * @default '#ffffff'
   */
  textColor?: string

  /**
   * 选中时的填充色和边框色
   * @default '#409EFF'
   */
  fill?: string

  /**
   * 可被勾选的 checkbox 的最小数量
   * @default undefined
   */
  min?: number

  /**
   * 可被勾选的 checkbox 的最大数量
   * @default undefined
   */
  max?: number

  /**
   * value字段名称，用于自定义选项值的字段
   * @default 'value'
   */
  valueField?: string

  /**
   * label字段名称，用于自定义选项标签的字段
   * @default 'label'
   */
  labelField?: string

  /**
   * 禁用条件字段名称，根据该字段判断选项是否禁用
   * @default 'status'
   */
  disabledField?: string

  /**
   * 禁用条件值，可以是单值、数组或函数
   * 当disabledField字段的值等于此值时，选项被禁用
   * @default '0'
   */
  disabledValue?: DisabledCondition

  /**
   * 是否使用选项自身的disabled属性判断禁用状态
   * @default true
   */
  useItemDisabled?: boolean

  /**
   * 是否返回数组类型，false时返回逗号分隔的字符串
   * @default false
   */
  returnArray?: boolean

  /**
   * 是否为单个复选框模式（适用于记住密码等单选场景）
   * @default false
   */
  singleCheckbox?: boolean

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

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormCheckboxProps>(), {
  modelValue: '',
  label: '',
  labelWidth: undefined,
  prop: '',
  size: '',
  span: undefined,
  disabled: false,
  showFormItem: true,
  tooltip: '',
  type: 'checkbox',
  border: false,
  textColor: '#ffffff',
  fill: '#409EFF',
  min: undefined,
  max: undefined,
  valueField: 'value',
  labelField: 'label',
  disabledField: 'status',
  disabledValue: '0',
  useItemDisabled: true,
  returnArray: false,
  singleCheckbox: false
})

const emit = defineEmits(['update:modelValue', 'change'])

// ========== 根据响应式模式选择对应的组合函数 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

// 内部数组值，用于存储复选框选中项
const internalValue = ref<(string | number)[]>([])

// 存储原始值的类型，用于保持输出类型的一致性
const originalValueType = ref<'string' | 'array' | 'boolean'>('string')

// 跟踪数组元素的原始类型
const arrayElementType = ref<'number' | 'string'>('string')

// 单个复选框的值
const singleCheckboxValue = computed({
  get() {
    if (props.singleCheckbox) {
      return Boolean(props.modelValue)
    }
    return false
  },
  set(value: boolean) {
    if (props.singleCheckbox && props.options.length > 0) {
      const optionValue = getValueFromOption(props.options[0])
      emit('update:modelValue', value ? optionValue : false)
    }
  }
})

/**
 * 规范化值，确保类型一致性
 * @param value 任意值
 * @returns 标准化后的值
 */
const normalizeValue = (value: any) => {
  return value !== null && value !== undefined ? String(value) : ''
}

/**
 * 转换数组元素类型，保持与原始输入一致
 * @param values 字符串数组
 * @returns 转换后的数组
 */
const convertArrayValues = (values: (string | number)[]): any[] => {
  if (arrayElementType.value === 'number') {
    return values.map((v) => (isNaN(Number(v)) ? v : Number(v)))
  }
  return values
}

// 监听 modelValue，将逗号分隔的字符串转为数组或处理数组类型
watch(
  () => props.modelValue,
  (val) => {
    if (props.singleCheckbox) {
      originalValueType.value = 'boolean'
      return
    }

    if (typeof val === 'string') {
      originalValueType.value = 'string'
      // 字符串类型，按逗号分割
      internalValue.value = val ? val.split(',').filter((v) => v.trim() !== '') : []
    } else if (Array.isArray(val)) {
      originalValueType.value = 'array'
      // 检测数组元素类型
      if (val.length > 0) {
        arrayElementType.value = typeof val[0] === 'number' ? 'number' : 'string'
      }
      // 数组类型，规范化每个值
      internalValue.value = val.map((v) => normalizeValue(v)).filter((v) => v !== '')
    } else {
      originalValueType.value = 'string'
      // 其他情况设为空数组
      internalValue.value = []
    }
  },
  { immediate: true }
)

// 使用计算属性计算国际化标签文本
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

// 确保options是有效数组
const optionsData = computed(() => {
  return Array.isArray(props.options) ? props.options : []
})

/**
 * 自动推断字典类型
 * 从 options 数组的 _dictType 属性获取（由 useDict 自动附加）
 */
const inferredDictType = computed((): string => {
  const options = props.options
  if (options && '_dictType' in options) {
    return (options as any)._dictType || ''
  }
  return ''
})

// 使用计算属性实现双向绑定
const checkboxValue = computed({
  get() {
    return internalValue.value
  },
  set(value: (string | number)[]) {
    internalValue.value = value

    // 根据 returnArray 和原始类型决定返回格式
    if (props.returnArray || originalValueType.value === 'array') {
      // 返回数组格式
      if (value && value.length > 0) {
        const filteredValues = value.filter((v) => v !== null && v !== undefined && v !== '')
        emit('update:modelValue', convertArrayValues(filteredValues))
      } else {
        emit('update:modelValue', [])
      }
    } else {
      // 返回字符串格式
      if (value && value.length > 0) {
        const filteredValues = value.filter((v) => v !== null && v !== undefined && v !== '')
        emit('update:modelValue', filteredValues.join(','))
      } else {
        emit('update:modelValue', '')
      }
    }
  }
})

/**
 * 从选项中获取value值
 * @param option 选项对象
 * @returns 选项的值
 */
const getValueFromOption = (option: any) => {
  // 如果option是简单类型，直接返回
  if (typeof option !== 'object' || option === null) {
    return option
  }
  // 否则获取指定字段的值
  return option[props.valueField]
}

/**
 * 从选项中获取label值（支持国际化）
 * @param option 选项对象
 * @returns 选项的标签（优先返回 i18n 翻译）
 */
const getLabelFromOption = (option: any) => {
  // 如果option是简单类型，直接返回
  if (typeof option !== 'object' || option === null) {
    return option
  }
  // 获取原始标签和值
  const originalLabel = option[props.labelField]
  const value = option[props.valueField]

  // 如果有字典类型，优先使用 i18n 翻译
  if (inferredDictType.value && value !== undefined) {
    const i18nLabel = getDictI18nLabel(inferredDictType.value, value)
    if (i18nLabel) {
      return i18nLabel
    }
  }

  return originalLabel
}

/**
 * 判断选项是否应该被禁用
 * 支持多种禁用判断方式
 * @param option 选项对象
 * @returns 是否禁用
 */
const isOptionDisabled = (option: any): boolean => {
  // 如果项不是对象，则不禁用
  if (typeof option !== 'object' || option === null) {
    return false
  }

  // 1. 首先检查是否使用选项自身的disabled属性
  if (props.useItemDisabled && 'disabled' in option && option.disabled) {
    return true
  }

  // 2. 如果未设置禁用字段或该字段在对象中不存在，则不禁用
  if (!props.disabledField || !(props.disabledField in option)) {
    return false
  }

  const fieldValue = option[props.disabledField]
  const disabledCondition = props.disabledValue

  // 3. 根据disabledValue的类型执行不同的比较逻辑

  // 如果disabledValue是函数，则调用函数进行判断
  if (typeof disabledCondition === 'function') {
    return disabledCondition(option)
  }

  // 如果disabledValue是数组，则检查字段值是否在数组中
  if (Array.isArray(disabledCondition)) {
    // 确保比较类型一致
    return disabledCondition.map((v) => normalizeValue(v)).includes(normalizeValue(fieldValue))
  }

  // 否则进行简单值比较，确保比较类型一致
  return normalizeValue(fieldValue) === normalizeValue(disabledCondition)
}

/**
 * 处理变化事件
 * @param val 变化后的值数组
 */
const handleChange = (val: (string | number)[]) => {
  // 根据 returnArray 和原始类型决定 change 事件的返回格式
  if (props.returnArray || originalValueType.value === 'array') {
    // 返回数组格式
    if (val && val.length > 0) {
      const filteredValues = val.filter((v) => v !== null && v !== undefined && v !== '')
      emit('change', convertArrayValues(filteredValues))
    } else {
      emit('change', [])
    }
  } else {
    // 返回字符串格式
    if (val && val.length > 0) {
      const filteredValues = val.filter((v) => v !== null && v !== undefined && v !== '')
      emit('change', filteredValues.join(','))
    } else {
      emit('change', '')
    }
  }
}

/**
 * 处理单个复选框变化事件
 * @param val 变化后的值
 */
const handleSingleChange = (val: boolean) => {
  if (props.options.length > 0) {
    const optionValue = getValueFromOption(props.options[0])
    emit('change', val ? optionValue : false)
  }
}
</script>
