<!--用法示例
//地区选择（自动使用内置地区数据）
<AFormCascader v-model="form.areaCode" mode="region" label="地区" prop="areaCode" :span="12"></AFormCascader>

//地区选择（手动传入地区数据）
<AFormCascader v-model="form.areaCode" :options="regionData" label="地区" prop="areaCode" :span="12"></AFormCascader>

//自定义宽度(数字自动添加px单位)
<AFormCascader v-model="form.areaCode" mode="region" label="地区" prop="areaCode" :width="300"></AFormCascader>

//自定义宽度(字符串直接使用)
<AFormCascader v-model="form.categoryId" :options="categoryTree" label="分类" prop="categoryId" width="100%"></AFormCascader>

//部门级联选择
<AFormCascader
  v-model="form.deptId"
  :options="deptTree"
  label="部门"
  prop="deptId"
  value-field="id"
  label-field="name"
  children-field="children"
  :span="12">
</AFormCascader>

//商品分类级联选择
<AFormCascader
  v-model="form.categoryId"
  :options="categoryTree"
  label="商品分类"
  prop="categoryId"
  value-field="categoryId"
  label-field="categoryName"
  children-field="subCategories"
  :span="12">
</AFormCascader>

//菜单权限级联选择
<AFormCascader
  v-model="form.menuIds"
  :options="menuTree"
  label="菜单权限"
  prop="menuIds"
  value-field="menuId"
  label-field="menuName"
  children-field="children"
  :multiple="true"
  :show-all-levels="false"
  :span="12">
</AFormCascader>

//自定义字段映射的级联选择
<AFormCascader
  v-model="form.orgCode"
  :options="orgData"
  label="组织架构"
  prop="orgCode"
  value-field="code"
  label-field="title"
  children-field="items"
  separator=" → "
  :span="12">
</AFormCascader>

//带禁用状态的级联选择
<AFormCascader
  v-model="form.regionId"
  :options="regionTree"
  label="业务区域"
  prop="regionId"
  value-field="id"
  label-field="name"
  children-field="children"
  disabled-field="status"
  disabled-value="0"
  :span="12">
</AFormCascader>

//获取完整路径信息
<AFormCascader
  v-model="form.pathId"
  :options="treeData"
  label="路径选择"
  prop="pathId"
  output-format="full"
  @path-change="handlePathChange"
  :span="12">
</AFormCascader>

const handlePathChange = (pathInfo) => {
  console.log('完整路径信息:', pathInfo);
  // pathInfo 格式：{ codes: ['1', '1-1', '1-1-1'], labels: ['一级', '二级', '三级'], value: '1-1-1', label: '一级 / 二级 / 三级' }
};

// 示例数据格式
// 默认数据格式（使用 value/label/children 字段）
const defaultTreeData = [
  {
    value: '1',
    label: '总公司',
    children: [
      {
        value: '1-1',
        label: '技术部',
        children: [
          { value: '1-1-1', label: '前端组' },
          { value: '1-1-2', label: '后端组' }
        ]
      },
      {
        value: '1-2',
        label: '产品部',
        children: [
          { value: '1-2-1', label: 'UI设计组' },
          { value: '1-2-2', label: '产品策划组' }
        ]
      }
    ]
  },
  {
    value: '2',
    label: '分公司',
    children: [
      {
        value: '2-1',
        label: '上海分公司',
        children: [
          { value: '2-1-1', label: '销售部' },
          { value: '2-1-2', label: '市场部' }
        ]
      }
    ]
  }
];

// 带禁用状态的数据格式
const dataWithDisabled = [
  {
    value: '1',
    label: '总公司',
    disabled: false, // 或者使用自定义禁用字段
    children: [
      {
        value: '1-1',
        label: '技术部',
        disabled: false,
        children: [
          { value: '1-1-1', label: '前端组', disabled: false },
          { value: '1-1-2', label: '后端组', disabled: true } // 禁用状态
        ]
      }
    ]
  }
];

// 自定义字段的数据格式（需要配置 value-field/label-field/children-field）
const customFieldData = [
  {
    id: '1',           // 对应 value-field="id"
    name: '总公司',     // 对应 label-field="name"
    items: [           // 对应 children-field="items"
      {
        id: '1-1',
        name: '技术部',
        items: [
          { id: '1-1-1', name: '前端组' },
          { id: '1-1-2', name: '后端组' }
        ]
      }
    ]
  }
];

// 地区数据格式（element-china-area-data 的格式）
const regionDataFormat = [
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '市辖区',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110102', label: '西城区' }
        ]
      }
    ]
  }
];
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

      <el-cascader
        v-model="cascaderValue"
        :options="normalizedOptions"
        :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
        :size="size"
        :disabled="disabled"
        :clearable="clearable"
        :filterable="filterable"
        :separator="separator"
        :show-all-levels="showAllLevels"
        :collapse-tags="collapseTags"
        :collapse-tags-tooltip="collapseTagsTooltip"
        :max-collapse-tags="maxCollapseTags"
        :tag-type="tagType"
        :filter-method="filterMethod"
        :debounce="debounce"
        :before-filter="beforeFilter"
        :popper-class="popperClass"
        :teleported="teleported"
        :props="cascaderProps"
        :style="{ width: computedWidth }"
        @change="handleChange"
        @expand-change="handleExpandChange"
        @blur="handleBlur"
        @focus="handleFocus"
        @visible-change="handleVisibleChange"
        @remove-tag="handleRemoveTag"
      >
        <!-- 前缀图标插槽 -->
        <template v-if="$slots.prefix" #prefix>
          <slot name="prefix"></slot>
        </template>

        <!-- 空数据插槽 -->
        <template v-if="$slots.empty" #empty>
          <slot name="empty"></slot>
        </template>
      </el-cascader>
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

    <el-cascader
      v-model="cascaderValue"
      :options="normalizedOptions"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :size="size"
      :disabled="disabled"
      :clearable="clearable"
      :filterable="filterable"
      :separator="separator"
      :show-all-levels="showAllLevels"
      :collapse-tags="collapseTags"
      :collapse-tags-tooltip="collapseTagsTooltip"
      :max-collapse-tags="maxCollapseTags"
      :tag-type="tagType"
      :filter-method="filterMethod"
      :debounce="debounce"
      :before-filter="beforeFilter"
      :popper-class="popperClass"
      :teleported="teleported"
      :props="cascaderProps"
      :style="{ width: computedWidth }"
      @change="handleChange"
      @expand-change="handleExpandChange"
      @blur="handleBlur"
      @focus="handleFocus"
      @visible-change="handleVisibleChange"
      @remove-tag="handleRemoveTag"
    >
      <!-- 前缀图标插槽 -->
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix"></slot>
      </template>

      <!-- 空数据插槽 -->
      <template v-if="$slots.empty" #empty>
        <slot name="empty"></slot>
      </template>
    </el-cascader>
  </el-form-item>

  <!--showFormItem为false 只显示选择框-->
  <template v-else>
    <el-cascader
      v-model="cascaderValue"
      :options="normalizedOptions"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :size="size"
      :disabled="disabled"
      :clearable="clearable"
      :filterable="filterable"
      :separator="separator"
      :show-all-levels="showAllLevels"
      :collapse-tags="collapseTags"
      :collapse-tags-tooltip="collapseTagsTooltip"
      :max-collapse-tags="maxCollapseTags"
      :tag-type="tagType"
      :filter-method="filterMethod"
      :debounce="debounce"
      :before-filter="beforeFilter"
      :popper-class="popperClass"
      :teleported="teleported"
      :props="cascaderProps"
      :style="{ width: computedWidth }"
      @change="handleChange"
      @expand-change="handleExpandChange"
      @blur="handleBlur"
      @focus="handleFocus"
      @visible-change="handleVisibleChange"
      @remove-tag="handleRemoveTag"
    >
      <!-- 前缀图标插槽 -->
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix"></slot>
      </template>

      <!-- 空数据插槽 -->
      <template v-if="$slots.empty" #empty>
        <slot name="empty"></slot>
      </template>
    </el-cascader>
  </template>
</template>

<!-- 普通 script 块：真正的模块级作用域，所有组件实例共享 -->
<script lang="ts">
// ========== 模块级别缓存（所有 AFormCascader 实例共享）==========
// 这里的变量只会在模块首次加载时初始化一次，不会随组件销毁重置
let cachedRegionData: any[] | null = null
let loadingPromise: Promise<any[]> | null = null

export { cachedRegionData, loadingPromise }
</script>

<script setup lang="ts" name="AFormCascader">
import type { CascaderOption } from 'element-plus'

const { t, te, isChinese } = useI18n()

// 输出格式类型
type OutputFormat = 'value' | 'number' | 'label' | 'full'

// 禁用条件类型
type DisabledCondition = string | number | boolean | Array<string | number | boolean> | ((item: any) => boolean)

// 完整路径信息类型
interface PathInfo {
  values: any[]
  labels: string[]
  value: any
  label: string
}

/**
 * 通用级联选择器组件的Props接口定义
 * @description 支持任意树形数据的级联选择，可自定义字段映射
 */
interface AFormCascaderProps {
  /**
   * 绑定值，支持字符串、数字或数组
   * @default undefined
   */
  modelValue?: string | number | Array<string | number>

  /**
   * 组件模式：
   * - region: 地区选择模式（自动使用内置地区数据）
   * - custom: 自定义模式（需要传入 options）
   * @default 'custom'
   */
  mode?: 'region' | 'custom'

  /**
   * 级联选择器的选项数据
   * 当 mode="region" 时可不传，会自动使用内置地区数据
   * 当 mode="custom" 时必须传入
   * @default []
   */
  options?: any[]

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
   * 是否禁用
   * @default false
   */
  disabled?: boolean

  /**
   * 是否显示清除按钮
   * @default true
   */
  clearable?: boolean

  /**
   * 是否可搜索选项
   * @default false
   */
  filterable?: boolean

  /**
   * 组件尺寸
   * @default ''
   */
  size?: ElSize

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
   * 提示信息
   * @default ''
   */
  tooltip?: string

  /**
   * 选项分隔符
   * @default ' / '
   */
  separator?: string

  /**
   * 输入框中是否显示选中值的完整路径
   * @default true
   */
  showAllLevels?: boolean

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
   * 最多显示的标签数量
   * @default undefined
   */
  maxCollapseTags?: number

  /**
   * 标签类型
   * @default 'info'
   */
  tagType?: ElTagType

  /**
   * 自定义搜索逻辑
   * @default undefined
   */
  filterMethod?: (node: any, keyword: string) => boolean

  /**
   * 搜索关键词输入的去抖延迟，单位毫秒
   * @default 300
   */
  debounce?: number

  /**
   * 筛选之前的钩子
   * @default undefined
   */
  beforeFilter?: (value: string) => boolean | Promise<any>

  /**
   * 自定义浮层类名
   * @default ''
   */
  popperClass?: string

  /**
   * 是否将弹层放置于 body 内
   * @default true
   */
  teleported?: boolean

  /**
   * 输出格式：
   * - value: 原始值格式（推荐）
   * - number: 数值型
   * - label: 标签文本
   * - full: 完整对象信息（通过 path-change 事件获取）
   * @default 'value'
   */
  outputFormat?: OutputFormat

  /**
   * 是否允许选择任意一级的选项
   * @default false
   */
  changeOnSelect?: boolean

  /**
   * 是否多选
   * @default false
   */
  multiple?: boolean

  /**
   * 是否严格的遵守父子节点不互相关联
   * @default false
   */
  checkStrictly?: boolean

  /**
   * value字段名称，用于指定节点的值字段
   * @default 'value'
   */
  valueField?: string

  /**
   * label字段名称，用于指定节点的标签字段
   * @default 'label'
   */
  labelField?: string

  /**
   * children字段名称，用于指定节点的子节点字段
   * @default 'children'
   */
  childrenField?: string

  /**
   * 禁用条件字段名称，根据该字段判断选项是否禁用
   * @default 'disabled'
   */
  disabledField?: string

  /**
   * 禁用条件值，可以是单值、数组或函数
   * @default undefined
   */
  disabledValue?: DisabledCondition

  /**
   * 是否使用选项自身的disabled属性判断禁用状态
   * @default true
   */
  useItemDisabled?: boolean

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
   * 级联选择器宽度，支持数字或字符串
   * - 数字：自动添加 px 单位，如 300 -> '300px'
   * - 字符串：直接使用，如 '100%', '20rem'
   * @default undefined - 使用默认宽度
   */
  width?: number | string
}

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormCascaderProps>(), {
  modelValue: undefined,
  //默认是地区选择
  mode: 'region',
  options: () => [],
  label: '',
  labelWidth: undefined,
  placeholder: '',
  prop: '',
  showFormItem: true,
  disabled: false,
  clearable: true,
  filterable: false,
  size: undefined,
  span: undefined,
  tooltip: '',
  separator: ' / ',
  showAllLevels: true,
  collapseTags: false,
  collapseTagsTooltip: false,
  maxCollapseTags: undefined,
  tagType: 'info',
  filterMethod: undefined,
  debounce: 300,
  beforeFilter: undefined,
  popperClass: '',
  teleported: true,
  outputFormat: 'value',
  changeOnSelect: false,
  multiple: false,
  checkStrictly: false,
  valueField: 'value',
  labelField: 'label',
  childrenField: 'children',
  disabledField: 'disabled',
  disabledValue: undefined,
  useItemDisabled: true,
  width: undefined
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'expand-change',
  'blur',
  'focus',
  'visible-change',
  'remove-tag',
  'path-change' // 用于获取完整路径信息的事件
])

// ========== 根据响应式模式选择对应的组合函数 ==========
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

// 创建内部值引用
const internalValue = ref<string | number | Array<string | number>>()

// 存储原始值的类型
const originalValueType = ref<'string' | 'array' | 'other'>('other')

// 跟踪数组元素的原始类型
const arrayElementType = ref<'number' | 'string' | 'other'>('string')

/**
 * 级联选择器配置
 * 注意: 因为 normalizedOptions 已经将数据标准化为 {value, label, children} 格式
 * 所以这里必须使用标准字段名,而不是用户自定义的字段名
 */
const cascaderProps = computed(() => ({
  value: 'value', // 使用标准字段名,不是 props.valueField
  label: 'label', // 使用标准字段名,不是 props.labelField
  children: 'children', // 使用标准字段名,不是 props.childrenField
  disabled: (data: any) => isNodeDisabled(data),
  checkStrictly: props.checkStrictly,
  multiple: props.multiple,
  emitPath: true
}))

// 动态引入地区数据（cachedRegionData 和 loadingPromise 在普通 <script> 块中定义）
const regionData = ref<any[]>([])
const isLoadingRegionData = ref(false)

/**
 * 动态加载地区数据（带模块级缓存）
 * 优化：全局只加载一次，多个组件实例共享数据
 */
const loadRegionData = async () => {
  // 使用缓存
  if (cachedRegionData) {
    regionData.value = cachedRegionData
    return
  }

  // 避免并发重复加载（多个组件同时初始化时）
  if (loadingPromise) {
    regionData.value = await loadingPromise
    return
  }

  try {
    isLoadingRegionData.value = true
    loadingPromise = import('element-china-area-data').then((m) => m.regionData)
    cachedRegionData = await loadingPromise
    regionData.value = cachedRegionData
  } catch (error) {
    console.warn('Failed to load region data. Please install element-china-area-data:', error)
    regionData.value = []
  } finally {
    isLoadingRegionData.value = false
    loadingPromise = null
  }
}

/**
 * 获取最终使用的选项数据
 */
const finalOptions = computed(() => {
  // 如果明确传入了 options，优先使用用户提供的数据
  if (props.options && props.options.length > 0) {
    return props.options
  }

  // 如果是地区模式，使用地区数据
  if (props.mode === 'region') {
    return regionData.value
  }

  // 默认返回空数组
  return []
})

// 监听模式变化，自动加载地区数据
watch(
  () => props.mode,
  (newMode) => {
    if (newMode === 'region' && regionData.value.length === 0) {
      loadRegionData()
    }
  },
  { immediate: true }
)

// 监听 options 变化，如果用户提供了 options，则不需要加载地区数据
watch(
  () => props.options,
  (newOptions) => {
    if (newOptions && newOptions.length > 0) {
      // 用户提供了数据，无需加载地区数据
      return
    }

    // 如果是地区模式且用户没有提供数据，则加载地区数据
    if (props.mode === 'region') {
      loadRegionData()
    }
  },
  { immediate: true }
)

/**
 * 标准化选项数据，确保符合 CascaderOption 格式
 */
const normalizedOptions = computed((): CascaderOption[] => {
  return normalizeTreeData(finalOptions.value)
})

/**
 * 递归标准化树形数据
 * @param data 原始数据
 * @returns 标准化后的数据
 */
const normalizeTreeData = (data: any[]): CascaderOption[] => {
  if (!Array.isArray(data)) {
    return []
  }

  return data.map((item) => {
    const normalized: CascaderOption = {
      value: getFieldValue(item, props.valueField),
      label: getFieldValue(item, props.labelField),
      disabled: isNodeDisabled(item)
    }

    const children = getFieldValue(item, props.childrenField)
    if (Array.isArray(children) && children.length > 0) {
      normalized.children = normalizeTreeData(children)
    }

    return normalized
  })
}

/**
 * 获取对象字段值
 * @param obj 对象
 * @param field 字段名
 * @returns 字段值
 */
const getFieldValue = (obj: any, field: string): any => {
  if (typeof obj !== 'object' || obj === null) {
    return obj
  }
  return obj[field]
}

/**
 * 判断节点是否禁用
 * @param node 节点数据
 * @returns 是否禁用
 */
const isNodeDisabled = (node: any): boolean => {
  if (typeof node !== 'object' || node === null) {
    return false
  }

  // 1. 首先检查是否使用节点自身的disabled属性
  if (props.useItemDisabled && 'disabled' in node && node.disabled) {
    return true
  }

  // 2. 如果未设置禁用字段或禁用值，则不禁用
  if (!props.disabledField || props.disabledValue === undefined) {
    return false
  }

  // 3. 如果禁用字段在节点中不存在，则不禁用
  if (!(props.disabledField in node)) {
    return false
  }

  const fieldValue = node[props.disabledField]
  const disabledCondition = props.disabledValue

  // 4. 根据disabledValue的类型执行不同的比较逻辑
  if (typeof disabledCondition === 'function') {
    return disabledCondition(node)
  }

  if (Array.isArray(disabledCondition)) {
    return disabledCondition.map((v) => normalizeValue(v)).includes(normalizeValue(fieldValue))
  }

  return normalizeValue(fieldValue) === normalizeValue(disabledCondition)
}

/**
 * 规范化值，确保类型一致性
 * @param value 任意值
 * @returns 标准化后的字符串值
 */
const normalizeValue = (value: any) => {
  return value !== null && value !== undefined ? String(value) : ''
}

/**
 * 根据路径数组和输出格式获取最终输出值
 * @param pathArray 选中的路径数组
 * @returns 格式化后的输出值
 */
const getFormattedOutput = (pathArray: any[]): any => {
  if (!pathArray || pathArray.length === 0) {
    return props.multiple ? [] : undefined
  }

  if (props.multiple && Array.isArray(pathArray[0])) {
    // 多选模式
    return pathArray.map((path) => formatSinglePath(path))
  } else {
    // 单选模式
    return formatSinglePath(pathArray)
  }
}

/**
 * 获取完整路径信息（用于 path-change 事件）
 * @param pathArray 选中的路径数组
 * @returns 完整路径信息
 */
const getFullPathInfo = (pathArray: any[]): PathInfo | PathInfo[] | undefined => {
  if (!pathArray || pathArray.length === 0) {
    return props.multiple ? [] : undefined
  }

  if (props.multiple && Array.isArray(pathArray[0])) {
    // 多选模式
    return pathArray.map((path) => formatToFullInfo(path))
  } else {
    // 单选模式
    return formatToFullInfo(pathArray)
  }
}

/**
 * 格式化单个路径
 * @param path 路径数组
 * @returns 格式化后的值
 */
const formatSinglePath = (path: any[]): any => {
  switch (props.outputFormat) {
    case 'number':
      return Number(path[path.length - 1])
    case 'label':
      return getPathLabels(path).join(props.separator)
    case 'full':
      // full 格式时，modelValue 返回原始值，完整信息通过事件提供
      return path[path.length - 1]
    case 'value':
    default:
      return path[path.length - 1]
  }
}

/**
 * 格式化为完整信息对象
 * @param path 路径数组
 * @returns 完整信息对象
 */
const formatToFullInfo = (path: any[]): PathInfo => {
  return {
    values: path,
    labels: getPathLabels(path),
    value: path[path.length - 1],
    label: getPathLabels(path).join(props.separator)
  }
}

/**
 * 根据值路径获取对应的标签数组
 * @param values 值数组
 * @returns 标签数组
 */
const getPathLabels = (values: any[]): string[] => {
  const labels: string[] = []
  let currentData: any[] = finalOptions.value

  for (const value of values) {
    const found = currentData.find((item: any) => normalizeValue(getFieldValue(item, props.valueField)) === normalizeValue(value))
    if (found) {
      labels.push(getFieldValue(found, props.labelField))
      const children = getFieldValue(found, props.childrenField)
      currentData = Array.isArray(children) ? children : []
    } else {
      labels.push(String(value)) // 兜底处理
    }
  }

  return labels
}

/**
 * 将输入值转换为级联选择器需要的格式
 * @param value 输入值
 * @returns 转换后的路径数组
 */
const convertInputToPath = (value: any): any => {
  if (value === undefined || value === null || value === '') {
    return props.multiple ? [] : undefined
  }

  if (props.multiple) {
    if (typeof value === 'string') {
      // 字符串形式的多个值，按逗号分割
      return value
        .split(',')
        .filter((v) => v.trim() !== '')
        .map((v) => findPathByValue(v.trim()))
    } else if (Array.isArray(value)) {
      // 数组形式
      return value.map((v) => findPathByValue(v))
    }
  } else {
    // 单选模式
    return findPathByValue(value)
  }

  return props.multiple ? [] : undefined
}

/**
 * 根据值查找完整路径
 * @param targetValue 目标值
 * @returns 完整路径数组
 */
const findPathByValue = (targetValue: any): any[] => {
  const target = normalizeValue(targetValue)

  const findPath = (data: any[], path: any[] = []): any[] | null => {
    for (const item of data) {
      const itemValue = getFieldValue(item, props.valueField)
      const currentPath = [...path, itemValue]

      if (normalizeValue(itemValue) === target) {
        return currentPath
      }

      const children = getFieldValue(item, props.childrenField)
      if (Array.isArray(children) && children.length > 0) {
        const result = findPath(children, currentPath)
        if (result) {
          return result
        }
      }
    }
    return null
  }

  return findPath(finalOptions.value) || [targetValue]
}

// 监听 modelValue 的变化
watch(
  () => props.modelValue,
  (val) => {
    if (props.multiple) {
      // 确定并记录原始值类型
      if (typeof val === 'string') {
        originalValueType.value = 'string'
      } else if (Array.isArray(val)) {
        originalValueType.value = 'array'
        if (val.length > 0) {
          arrayElementType.value = typeof val[0] === 'number' ? 'number' : 'string'
        }
      } else {
        originalValueType.value = 'other'
      }
    }

    internalValue.value = convertInputToPath(val)
  },
  { immediate: true }
)

/**
 * 监听选项数据变化（解决异步加载地区数据时的双向绑定问题）
 * 场景：modelValue 在 options/regionData 加载完成前就已设置，需要在数据加载后重新处理
 */
watch(
  () => finalOptions.value,
  (newOptions, oldOptions) => {
    // 选项从空变为有值时，且 modelValue 已有值，重新转换路径
    const wasEmpty = !oldOptions || oldOptions.length === 0
    const nowHasData = newOptions && newOptions.length > 0
    const hasModelValue = props.modelValue !== undefined && props.modelValue !== null && props.modelValue !== ''

    if (wasEmpty && nowHasData && hasModelValue) {
      internalValue.value = convertInputToPath(props.modelValue)
    }
  }
)

// 使用计算属性实现双向绑定
const cascaderValue = computed({
  get() {
    return internalValue.value
  },
  set(value) {
    internalValue.value = value

    // 输出格式化后的值
    const formattedValue = getFormattedOutput(value as any[])

    // 发出完整路径信息事件
    if (props.outputFormat === 'full' || value) {
      const fullInfo = getFullPathInfo(value as any[])
      emit('path-change', fullInfo)
    }

    if (props.multiple && Array.isArray(formattedValue)) {
      // 多选模式，根据原始类型决定返回格式
      if (originalValueType.value === 'string') {
        emit('update:modelValue', formattedValue.join(','))
      } else {
        // 保持数组元素类型一致
        if (arrayElementType.value === 'number' && props.outputFormat === 'number') {
          emit(
            'update:modelValue',
            formattedValue.map((v) => Number(v))
          )
        } else {
          emit('update:modelValue', formattedValue)
        }
      }
    } else {
      // 单选模式
      emit('update:modelValue', formattedValue)
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

/**
 * 级联选择器变更处理函数
 * @param value 选择的值
 */
const handleChange = (value: any) => {
  const formattedValue = getFormattedOutput(value)

  // 发出完整路径信息事件（当需要完整信息时使用）
  if (props.outputFormat === 'full' || value) {
    const fullInfo = getFullPathInfo(value)
    emit('path-change', fullInfo)
  }

  if (props.multiple && Array.isArray(formattedValue)) {
    // 多选模式
    if (originalValueType.value === 'string') {
      emit('change', formattedValue.join(','))
    } else {
      if (arrayElementType.value === 'number' && props.outputFormat === 'number') {
        emit(
          'change',
          formattedValue.map((v) => Number(v))
        )
      } else {
        emit('change', formattedValue)
      }
    }
  } else {
    // 单选模式
    emit('change', formattedValue)
  }
}

/**
 * 展开状态变化处理函数
 * @param value 当前展开的节点数组
 */
const handleExpandChange = (value: any) => {
  emit('expand-change', value)
}

/**
 * 失焦处理函数
 */
const handleBlur = () => {
  emit('blur')
}

/**
 * 聚焦处理函数
 */
const handleFocus = () => {
  emit('focus')
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
<style scoped lang="scss">
:deep(.el-cascader) {
  width: 100% !important;
}
</style>
