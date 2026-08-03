<!--用法示例
//一般用于搜索栏（🌐 自动支持 i18n，无需额外配置）
<AFormSelect v-model="queryParams.type" :options="sys_enable_status" label="类型" prop="type" @change="handleQuery"></AFormSelect>
//带前缀图标的选择器
<AFormSelect v-model="loginForm.tenantId" :options="tenantList" value-field="tenantId" label-field="companyName" prefix-icon="company">
  <template #prefix>
    <Icon code="company" />
  </template>
</AFormSelect>
//一般用于表单（🌐 options 来自 useDict() 时自动使用 i18n 翻译）
<AFormSelect v-model="queryParams.type" :options="sys_enable_status" label="类型" prop="type" :span="12"></AFormSelect>
//自定义宽度(数字自动添加px单位)
<AFormSelect v-model="queryParams.type" :options="sys_enable_status" label="类型" prop="type" :width="300"></AFormSelect>
//自定义宽度(字符串直接使用)
<AFormSelect v-model="queryParams.type" :options="sys_enable_status" label="类型" prop="type" width="100%"></AFormSelect>
//带提示信息的选择器
<AFormSelect v-model="queryParams.type" :options="sys_enable_status" label="类型" prop="type" tooltip="请选择应用类型" :span="12"></AFormSelect>
//不含el-form-item容器的简单选择框
<AFormSelect v-model="queryParams.type" :options="sys_enable_status" label="类型" :show-form-item="false"></AFormSelect>
//自定义value和label字段的使用方式
<AFormSelect v-model="form.userId" :options="userList" value-field="id" label-field="name" label="用户" :span="12"></AFormSelect>
//单个选项禁用的使用方式(默认根据status=0判断)
<AFormSelect v-model="form.postIds" :options="postOptions" value-field="postId" label-field="postName" multiple label="岗位" :span="12"></AFormSelect>
//自定义禁用条件的使用方式
<AFormSelect v-model="form.roleId" :options="roleList" value-field="id" label-field="roleName" disabled-field="isActive" :disabled-value="false" label="角色"></AFormSelect>
//使用多值禁用条件(当status为0或3时禁用)
<AFormSelect v-model="form.deptId" :options="deptList" value-field="deptId" label-field="deptName" disabled-field="status" :disabled-value="['0', '3']" label="部门" :span="12"></AFormSelect>
//使用函数进行复杂条件判断
<AFormSelect v-model="form.goodsId" :options="productList" value-field="id" label-field="name" :disabled-value="(item) => item.status === '0' || item.stock < 10" label="产品"></AFormSelect>
//强制显示选项值(覆盖全局配置)
<AFormSelect v-model="form.code" :options="codeList" label="代码" :show-value="true"></AFormSelect>
//强制不显示选项值
<AFormSelect v-model="form.name" :options="nameList" label="名称" :show-value="false"></AFormSelect>
//自定义哪些角色显示选项值(默认['superadmin', 'admin'])
<AFormSelect v-model="form.type" :options="typeList" label="类型" :show-value-roles="['developer', 'tester']"></AFormSelect>
//自定义选项内容(插槽提供 item/label/value/disabled 四个参数)
<AFormSelect v-model="form.userId" :options="userList" value-field="id" label-field="name" label="用户" :span="12">
  <template #option="{ item, label, value }">
    <div style="display: flex; align-items: center; gap: 8px">
      <el-avatar :size="24" :src="item.avatar" />
      <span>{{ label }}</span>
      <span style="color: #8492a6; font-size: 0.8em">{{ item.dept }}</span>
    </div>
  </template>
</AFormSelect>

const { sys_enable_status } = useDict(DictTypes.sys_enable_status);
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

      <el-select
        v-model="selectValue"
        :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
        :size="size"
        :allow-create="allowCreate"
        :filterable="filterable"
        :clearable="clearable"
        :multiple="multiple"
        :disabled="disabled"
        :multiple-limit="multipleLimit"
        :collapse-tags="collapseTags"
        :collapse-tags-tooltip="collapseTagsTooltip"
        :style="{ width: computedWidth }"
        @focus="handleFocus"
        @change="handleChange"
        @clear="handleClear"
        @visible-change="handleVisibleChange"
        @remove-tag="handleRemoveTag"
      >
        <!-- 前缀图标插槽 -->
        <template v-if="$slots.prefix" #prefix>
          <slot name="prefix"></slot>
        </template>

        <!-- 自定义选项插槽模式 -->
        <template v-if="$slots.option">
          <el-option
            v-for="item in optionsData"
            :key="normalizeValue(getValueFromItem(item))"
            :label="getLabelFromItem(item)"
            :value="normalizeValue(getValueFromItem(item))"
            :disabled="isOptionDisabled(item)"
          >
            <slot
              name="option"
              :item="item"
              :label="getLabelFromItem(item)"
              :value="normalizeValue(getValueFromItem(item))"
              :disabled="isOptionDisabled(item)"
            ></slot>
          </el-option>
        </template>

        <!-- 默认选项渲染 -->
        <template v-else>
          <el-option
            v-for="item in optionsData"
            :key="normalizeValue(getValueFromItem(item))"
            :label="getLabelFromItem(item)"
            :value="normalizeValue(getValueFromItem(item))"
            :disabled="isOptionDisabled(item)"
          >
            <span style="float: left">{{ getLabelFromItem(item) }}</span>
            <span v-if="computedShowValue" style="float: right; color: #8492a6; font-size: 0.8em">{{ normalizeValue(getValueFromItem(item)) }}</span>
          </el-option>
        </template>

        <!-- 空数据插槽 -->
        <template v-if="$slots.empty" #empty>
          <slot name="empty"></slot>
        </template>
      </el-select>
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

    <el-select
      v-model="selectValue"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :size="size"
      :allow-create="allowCreate"
      :filterable="filterable"
      :clearable="clearable"
      :multiple="multiple"
      :disabled="disabled"
      :multiple-limit="multipleLimit"
      :collapse-tags="collapseTags"
      :collapse-tags-tooltip="collapseTagsTooltip"
      :style="{ width: computedWidth }"
      @focus="handleFocus"
      @change="handleChange"
      @clear="handleClear"
      @visible-change="handleVisibleChange"
      @remove-tag="handleRemoveTag"
    >
      <!-- 前缀图标插槽 -->
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix"></slot>
      </template>

      <!-- 自定义选项插槽模式 -->
      <template v-if="$slots.option">
        <el-option
          v-for="item in optionsData"
          :key="normalizeValue(getValueFromItem(item))"
          :label="getLabelFromItem(item)"
          :value="normalizeValue(getValueFromItem(item))"
          :disabled="isOptionDisabled(item)"
        >
          <slot
            name="option"
            :item="item"
            :label="getLabelFromItem(item)"
            :value="normalizeValue(getValueFromItem(item))"
            :disabled="isOptionDisabled(item)"
          ></slot>
        </el-option>
      </template>

      <!-- 默认选项渲染 -->
      <template v-else>
        <el-option
          v-for="item in optionsData"
          :key="normalizeValue(getValueFromItem(item))"
          :label="getLabelFromItem(item)"
          :value="normalizeValue(getValueFromItem(item))"
          :disabled="isOptionDisabled(item)"
        >
          <span style="float: left">{{ getLabelFromItem(item) }}</span>
          <span v-if="computedShowValue" style="float: right; color: #8492a6; font-size: 0.8em">{{ normalizeValue(getValueFromItem(item)) }}</span>
        </el-option>
      </template>

      <!-- 空数据插槽 -->
      <template v-if="$slots.empty" #empty>
        <slot name="empty"></slot>
      </template>
    </el-select>
  </el-form-item>

  <!--showFormItem为false 只显示选择框-->
  <template v-else>
    <el-select
      v-model="selectValue"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :size="size"
      :allow-create="allowCreate"
      :filterable="filterable"
      :clearable="clearable"
      :multiple="multiple"
      :disabled="disabled"
      :multiple-limit="multipleLimit"
      :collapse-tags="collapseTags"
      :collapse-tags-tooltip="collapseTagsTooltip"
      :style="{ width: computedWidth }"
      @focus="handleFocus"
      @change="handleChange"
      @clear="handleClear"
      @visible-change="handleVisibleChange"
      @remove-tag="handleRemoveTag"
    >
      <!-- 前缀图标插槽 -->
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix"></slot>
      </template>

      <!-- 自定义选项插槽模式 -->
      <template v-if="$slots.option">
        <el-option
          v-for="item in optionsData"
          :key="normalizeValue(getValueFromItem(item))"
          :label="getLabelFromItem(item)"
          :value="normalizeValue(getValueFromItem(item))"
          :disabled="isOptionDisabled(item)"
        >
          <slot
            name="option"
            :item="item"
            :label="getLabelFromItem(item)"
            :value="normalizeValue(getValueFromItem(item))"
            :disabled="isOptionDisabled(item)"
          ></slot>
        </el-option>
      </template>

      <!-- 默认选项渲染 -->
      <template v-else>
        <el-option
          v-for="item in optionsData"
          :key="normalizeValue(getValueFromItem(item))"
          :label="getLabelFromItem(item)"
          :value="normalizeValue(getValueFromItem(item))"
          :disabled="isOptionDisabled(item)"
        >
          <span style="float: left">{{ getLabelFromItem(item) }}</span>
          <span v-if="computedShowValue" style="float: right; color: #8492a6; font-size: 0.8em">{{ normalizeValue(getValueFromItem(item)) }}</span>
        </el-option>
      </template>

      <!-- 空数据插槽 -->
      <template v-if="$slots.empty" #empty>
        <slot name="empty"></slot>
      </template>
    </el-select>
  </template>
</template>

<script setup lang="ts" name="AFormSelect">
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
 * 选择器组件的Props接口定义
 * @description 定义了选择器组件的所有属性和类型
 */
interface AFormSelectProps {
  /**
   * 绑定值，支持字符串、数字或数组
   * @default undefined
   */
  modelValue?: string | number | Array<string | number>

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
   * 表单域model数据字段名
   * @default ''
   */
  prop?: string

  /**
   * 是否显示表单项
   * @default true
   */
  showFormItem?: boolean

  /**
   * 是否允许创建新条目
   * @default false
   */
  allowCreate?: boolean

  /**
   * 是否可过滤
   * @default true
   */
  filterable?: boolean

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
   * 是否多选
   * @default false
   */
  multiple?: boolean

  /**
   * 组件尺寸
   * @default ''
   */
  size?: '' | 'default' | 'small' | 'large'

  /**
   * 选项数据数组
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
   * 是否显示选项值
   * @default undefined - 使用全局配置或角色判断
   */
  showValue?: boolean

  /**
   * 哪些角色默认显示选项值
   * @default ['superadmin', 'admin']
   */
  showValueRoles?: string[]

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
   * 提示信息
   * @default ''
   */
  tooltip?: string

  /**
   * 多选时用户最多可以选择的项目数，为 0 则不限制
   * @default 0
   */
  multipleLimit?: number

  /**
   * 多选时是否将选中值按文字的形式展示
   * @default false
   */
  collapseTags?: boolean

  /**
   * 当鼠标悬停于折叠标签的文本时，是否显示所有选中的标签在一个 tooltip 中
   * @default false
   */
  collapseTagsTooltip?: boolean

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
   * 选择框宽度，支持数字或字符串
   * - 数字：自动添加 px 单位，如 300 -> '300px'
   * - 字符串：直接使用，如 '100%', '20rem'
   * @default undefined - 使用默认宽度
   */
  width?: number | string
}

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormSelectProps>(), {
  modelValue: undefined,
  label: '',
  labelWidth: undefined,
  placeholder: '',
  prop: '',
  showFormItem: true,
  allowCreate: false,
  filterable: true,
  clearable: true,
  disabled: false,
  multiple: false,
  size: '',
  span: undefined,
  showValue: undefined,
  showValueRoles: () => ['superadmin', 'admin'],
  // 默认字段名称为字典数据的格式
  valueField: 'value',
  labelField: 'label',
  // 默认禁用字段设置
  disabledField: 'status',
  disabledValue: '0',
  useItemDisabled: true,
  tooltip: '',
  multipleLimit: 0,
  collapseTags: false,
  collapseTagsTooltip: false,
  width: undefined
})

const emit = defineEmits(['update:modelValue', 'change', 'focus', 'clear', 'visible-change', 'remove-tag'])

// ========== 布局和用户状态 ==========
const layout = useLayout()
const userStore = useUserStore()

/**
 * 计算属性：智能判断是否显示选项值
 * 优先级：props.showValue > 用户手动配置 > 角色判断 > 默认值(false)
 */
const computedShowValue = computed(() => {
  // 1. 优先使用 props 传入的值（允许单独控制）
  if (props.showValue !== undefined) {
    return props.showValue
  }

  // 2. 使用用户手动配置（如果明确设置过）
  const cachedValue = layout.showSelectValue.value
  if (cachedValue !== undefined) {
    return cachedValue
  }

  // 3. 根据角色判断（检查用户角色是否在显示值角色列表中）
  const userRoles = userStore.roles
  if (userRoles && userRoles.length > 0 && props.showValueRoles) {
    return userRoles.some((role) => props.showValueRoles.includes(role))
  }

  // 4. 默认不显示
  return false
})

// ========== 使用智能响应式逻辑 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

/**
 * 计算属性：处理宽度值
 * 支持数字（自动添加px）和字符串（直接使用）
 */
const computedWidth = computed(() => {
  if (props.width === undefined) {
    return undefined
  }
  return typeof props.width === 'number' ? `${props.width}px` : props.width
})

// 创建内部值引用，用于存储处理后的值
const internalValue = ref(undefined)

// 存储原始值的类型，用于保持输出类型的一致性
const originalValueType = ref<'string' | 'array' | 'other'>('other')

/**
 * 检测数组元素的最适合类型
 * 优先级：字符串 > 数字（所有都是数字且在安全范围内） > 其他类型
 * @param arr 要检测的数组
 * @returns 'string' | 'number' | 'other'
 */
const detectArrayElementType = (arr: any[]): 'string' | 'number' | 'other' => {
  if (!Array.isArray(arr) || arr.length === 0) {
    return 'string' // 默认返回字符串类型
  }

  let hasString = false
  let hasNumber = false
  let hasOther = false
  let hasLargeNumber = false // 标记是否有超出安全范围的数字

  for (const item of arr) {
    if (typeof item === 'string') {
      hasString = true
      // 检查字符串是否是超大数字
      if (/^\d+$/.test(item) && item.length > 15) {
        hasLargeNumber = true
      }
      break // 发现字符串立即返回，因为字符串优先级最高
    } else if (typeof item === 'number') {
      hasNumber = true
      // 检查数字是否超出JavaScript安全整数范围
      if (!Number.isSafeInteger(item)) {
        hasLargeNumber = true
      }
    } else {
      hasOther = true
    }
  }

  // 优先级判断：
  // 1. 字符串优先：只要有一个字符串就返回字符串类型
  if (hasString) {
    return 'string'
  }
  // 2. 如果有超出安全范围的数字，强制使用字符串类型
  else if (hasLargeNumber) {
    return 'string'
  }
  // 3. 数字其次：只有当没有字符串，且没有其他类型，全都是安全范围内的数字时才返回数字类型
  else if (hasNumber && !hasOther) {
    return 'number'
  }
  // 4. 其他兜底：有非字符串、非数字的类型
  else {
    return 'other'
  }
}

/**
 * 规范化值，确保类型一致性
 * @param value 任意值
 * @returns 标准化后的字符串值
 */
const normalizeValue = (value: any) => {
  // 确保值为字符串类型进行比较，避免 2 !== "2" 的情况
  return value !== null && value !== undefined ? String(value) : ''
}

/**
 * 转换数组元素类型，根据检测结果进行转换
 * @param values 字符串数组
 * @param targetType 目标类型
 * @returns 转换后的数组
 */
const convertArrayValues = (values: string[], targetType: 'string' | 'number' | 'other'): any[] => {
  if (targetType === 'number') {
    return values.map((v) => (isNaN(Number(v)) ? v : Number(v)))
  }
  return values
}

// 监听 modelValue 的变化，并处理字符串和数组的转换
watch(
  () => props.modelValue,
  (val) => {
    if (props.multiple) {
      // 确定并记录原始值类型
      if (typeof val === 'string') {
        originalValueType.value = 'string'
        // 字符串类型，按逗号分割
        internalValue.value = val ? val.split(',').filter((v) => v.trim() !== '') : []
      } else if (Array.isArray(val)) {
        originalValueType.value = 'array'
        // 数组类型，规范化每个值（不需要检测类型，因为输出时会重新检测）
        internalValue.value = val.map((v) => normalizeValue(v)).filter((v) => v !== '')
      } else {
        originalValueType.value = 'other'
        // 其他情况设为空数组
        internalValue.value = []
      }
    } else {
      // 单选模式不需要记录类型，直接规范化值
      internalValue.value = val !== undefined ? normalizeValue(val) : undefined
    }
  },
  { immediate: true }
)

// 使用计算属性实现双向绑定
const selectValue = computed({
  get() {
    return internalValue.value
  },
  set(value) {
    internalValue.value = value

    // 输出值时保持与输入值相同的类型
    if (props.multiple && Array.isArray(value)) {
      // 多选模式，重新检测当前值的类型
      const filteredValues = value.map((v) => normalizeValue(v)).filter((v) => v !== '')

      // 重新检测类型，考虑当前选中的值
      const currentArrayType = detectArrayElementType(filteredValues)

      if (originalValueType.value !== 'array') {
        // 如果原始值是字符串，返回逗号分隔的字符串
        emit('update:modelValue', filteredValues.join(','))
      } else {
        // 如果原始值是数组，根据检测到的类型返回相应格式的数组
        emit('update:modelValue', convertArrayValues(filteredValues, currentArrayType))
      }
    } else {
      // 单选模式，直接返回规范化的值
      emit('update:modelValue', value !== null && value !== undefined ? normalizeValue(value) : value)
    }
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

/**
 * 从选项中获取value值
 * @param item 选项对象
 * @returns 选项的值
 */
const getValueFromItem = (item: any) => {
  // 如果item是简单类型，直接返回
  if (typeof item !== 'object' || item === null) {
    return item
  }
  // 否则获取指定字段的值
  return item[props.valueField]
}

/**
 * 从选项中获取label值（支持 i18n 自动翻译）
 * @param item 选项对象
 * @returns 选项的标签（优先使用 i18n 翻译）
 */
const getLabelFromItem = (item: any) => {
  // 如果item是简单类型，直接返回
  if (typeof item !== 'object' || item === null) {
    return item
  }

  const originalLabel = item[props.labelField]
  const value = item[props.valueField]

  // 如果有推断出的字典类型，优先使用 i18n 翻译
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
 * @param item 选项对象
 * @returns 是否禁用
 */
const isOptionDisabled = (item: any): boolean => {
  // 如果项不是对象，则不禁用
  if (typeof item !== 'object' || item === null) {
    return false
  }

  // 1. 首先检查是否使用选项自身的disabled属性
  if (props.useItemDisabled && 'disabled' in item && item.disabled) {
    return true
  }

  // 2. 如果未设置禁用字段或该字段在对象中不存在，则不禁用
  if (!props.disabledField || !(props.disabledField in item)) {
    return false
  }

  const fieldValue = item[props.disabledField]
  const disabledCondition = props.disabledValue

  // 3. 根据disabledValue的类型执行不同的比较逻辑

  // 如果disabledValue是函数，则调用函数进行判断
  if (typeof disabledCondition === 'function') {
    return disabledCondition(item)
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
 * 选择框变更处理函数
 * @param val 选择的值
 */
const handleChange = (val: any) => {
  // 多选模式下，重新检测类型并保持与v-model相同的输出类型
  if (props.multiple && Array.isArray(val)) {
    const filteredValues = val.map((v) => normalizeValue(v)).filter((v) => v !== '')

    // 重新检测当前值的类型
    const currentArrayType = detectArrayElementType(filteredValues)

    if (originalValueType.value === 'string') {
      // 原始值是字符串，change事件也返回字符串
      emit('change', filteredValues.join(','))
    } else {
      // 原始值是数组，根据检测到的类型返回相应格式的数组
      emit('change', convertArrayValues(filteredValues, currentArrayType))
    }
  } else {
    // 单选模式
    emit('change', val !== null && val !== undefined ? normalizeValue(val) : val)
  }
}

/**
 * 聚焦处理函数
 */
const handleFocus = () => {
  emit('focus')
}

/**
 * 清除处理函数
 */
const handleClear = () => {
  internalValue.value = props.multiple ? [] : undefined

  // 清除时也保持类型一致性
  if (props.multiple) {
    if (originalValueType.value === 'string') {
      emit('update:modelValue', '')
    } else {
      emit('update:modelValue', [])
    }
  } else {
    emit('update:modelValue', undefined)
  }

  emit('clear')
}

/**
 * 下拉框显示/隐藏状态变化
 * @param visible 是否可见
 */
const handleVisibleChange = (visible: boolean) => {
  emit('visible-change', visible)
}

/**
 * 多选模式下移除tag时触发
 * @param tag 被移除的tag值
 */
const handleRemoveTag = (tag: any) => {
  emit('remove-tag', tag)
}
</script>
<style>
/* 全局控制所有选择器下拉菜单高度 */
.el-select-dropdown {
  max-height: 360px !important;
}

.el-select-dropdown .el-scrollbar {
  max-height: 360px !important;
}

.el-select-dropdown .el-select-dropdown__list {
  max-height: 360px !important;
  overflow-y: auto !important;
}

/* 强制覆盖任何可能的样式 */
[class*='el-select'].is-disabled * {
  color: #777777 !important;
  -webkit-text-fill-color: #777777 !important;
}
</style>
