<!--
字典标签组件 DictTag
使用示例：

// 字典标签（原有功能）
<DictTag :options="statusOptions" :value="'1,3'" />
<DictTag :options="statusOptions" :value="[1, 3]" />

// 🌐 国际化支持（自动推断 - 推荐方式）
// 如果 options 来自 useDict()，组件会自动识别字典类型并使用 i18n 翻译
// 无需传入 dict-type 属性！
<DictTag :options="sys_user_gender" :value="row.gender" />
<DictTag :options="sys_enable_status" :value="row.status" />

// 🌐 国际化支持（显式指定）
// 如果 options 不是来自 useDict()，可以手动传入 dict-type
<DictTag :options="customOptions" :value="row.type" dict-type="sys_user_gender" />

// 自定义字段名的字典标签
<DictTag :options="customOptions" :value="'active'" value-field="status" label-field="name" />

// 地区标签
<DictTag mode="region" :value="'110101'" />
<DictTag mode="region" :value="['110101', '310101']" />

// 自定义级联数据标签
<DictTag mode="cascader" :options="deptTree" :value="'1-1-1'" value-field="id" label-field="name" children-field="children" />

// 自定义样式
<DictTag mode="region" :value="'110101'" tag-type="success" />
<DictTag :options="statusOptions" :value="'1'" :show-value="false" />

// 自定义分隔符和连接符
<DictTag mode="region" :value="'110101'" path-separator=" → " />
<DictTag :options="statusOptions" :value="'1|3'" separator="|" />
-->

<template>
  <div>
    <!-- 遍历处理后的标签数据并显示 -->
    <template v-for="(item, index) in displayItems" :key="index">
      <!-- 无标签类型或样式类时显示普通文本 -->
      <span v-if="!item.tagType && (!item.tagClass || item.tagClass === '')" :class="item.tagClass">
        {{ item.label }}{{ index < displayItems.length - 1 ? ' ' : '' }}
      </span>
      <!-- 有标签类型或样式类时显示el-tag组件 -->
      <el-tag v-else :size="size" :disable-transitions="true" :type="item.tagType || tagType" :class="item.tagClass">
        {{ item.label }}
      </el-tag>
      <!-- 标签间距 -->
      <span v-if="index < displayItems.length - 1" class="tag-spacer"> </span>
    </template>

    <!-- 显示未匹配的值（如有） -->
    <template v-if="unmatchedValues.length > 0 && showValue">
      <span v-if="displayItems.length > 0" class="tag-spacer"> </span>
      <span class="unmatched-values">{{ unmatchedValues.join(' ') }}</span>
    </template>
  </div>
</template>

<script setup lang="ts" name="DictTag">
/**
 * DictTag 组件
 *
 * 增强版字典标签组件，支持：
 * 1. 普通字典选项转换（支持自定义字段名）
 * 2. 地区代码转换
 * 3. 自定义级联数据转换
 * 4. 国际化支持（通过 dictType 属性）
 */

const { t, te } = useI18n()

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

// 显示项接口
interface DisplayItem {
  label: string
  tagType?: ElTagType
  tagClass?: string
}

interface Props {
  /**
   * 组件模式
   * - dict: 字典模式（默认）
   * - region: 地区模式
   * - cascader: 级联模式
   */
  mode?: 'dict' | 'region' | 'cascader'

  /** 字典选项数组（dict 和 cascader 模式使用），支持普通数组或响应式引用 */
  options?: Array<DictItem | any> | import('vue').Ref<Array<DictItem | any>>

  /**
   * 要显示的值，可以是：
   * - 单个数字或字符串
   * - 数字或字符串数组
   * - 用分隔符分隔的字符串（默认分隔符为逗号）
   * - null 或 undefined（将不显示任何内容）
   */
  value: number | string | Array<number | string> | null | undefined

  /** 是否显示未匹配的值 */
  showValue?: boolean

  /** 当值是字符串时用于分割的分隔符 */
  separator?: string

  /** 地区/级联路径的连接符 */
  pathSeparator?: string

  /** 统一的标签类型（会被选项中的 elTagType 覆盖） */
  tagType?: ElTagType

  /** 统一的标签样式类（会被选项中的 elTagClass 覆盖） */
  tagClass?: string

  /**标签大小*/
  size?: ElSize

  // 通用字段映射（dict、cascader 模式都可使用）
  /** value字段名称 */
  valueField?: string
  /** label字段名称 */
  labelField?: string
  /** children字段名称（仅 cascader 模式使用） */
  childrenField?: string

  /**
   * 字典类型（用于国际化支持）
   * - 如果 options 来自 useDict()，会自动推断字典类型，无需手动传入
   * - 如果 options 不是来自 useDict()，可以手动指定此参数
   * - 组件会优先使用 i18n 翻译（键格式：dict.{dictType}.{value}）
   * - 如果没有 i18n 翻译，则使用 options 中的 label
   * @example dictType="sys_user_gender"
   */
  dictType?: string
}

// 组件属性默认值
const props = withDefaults(defineProps<Props>(), {
  mode: 'dict',
  options: () => [],
  showValue: true,
  separator: ',',
  pathSeparator: ' / ',
  tagType: 'info',
  tagClass: '',
  valueField: 'value',
  labelField: 'label',
  childrenField: 'children',
  dictType: ''
})

// 解包 options，支持普通数组和 Ref
const resolvedOptions = computed(() => unref(props.options) || [])

/**
 * 自动推断字典类型
 * 优先使用 props.dictType，否则尝试从 options 数组的 _dictType 属性获取
 */
const inferredDictType = computed((): string => {
  // 优先使用显式传入的 dictType
  if (props.dictType) {
    return props.dictType
  }
  // 尝试从 options 数组获取 _dictType（由 useDict 自动附加）
  const options = unref(props.options)
  if (options && '_dictType' in options) {
    return (options as any)._dictType || ''
  }
  return ''
})

// 地区数据
const regionData = ref<any[]>([])
const isLoadingRegionData = ref(false)

/**
 * 动态加载地区数据
 */
const loadRegionData = async () => {
  if (regionData.value.length > 0 || isLoadingRegionData.value) {
    return
  }

  try {
    isLoadingRegionData.value = true
    const { regionData: data } = await import('element-china-area-data')
    regionData.value = data
  } catch (error) {
    console.warn('加载地区数据失败,请安装 element-china-area-data:', error)
    regionData.value = []
  } finally {
    isLoadingRegionData.value = false
  }
}

// 监听模式变化，自动加载地区数据
watch(
  () => props.mode,
  (newMode) => {
    if (newMode === 'region') {
      loadRegionData()
    }
  },
  { immediate: true }
)

/**
 * 处理传入的值，确保转换为字符串数组格式
 *
 * - 处理空值、null和undefined为空数组
 * - 将单个值转换为单元素数组
 * - 将字符串按分隔符分割
 * - 将所有元素转换为字符串以便统一比较
 */
const processedValues = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') {
    return []
  }

  return Array.isArray(props.value)
    ? props.value.map((item) => String(item))
    : String(props.value)
        .split(props.separator)
        .filter((v) => v.trim() !== '')
})

/**
 * 获取对象字段值
 */
const getFieldValue = (obj: any, field: string): any => {
  if (typeof obj !== 'object' || obj === null) {
    return obj
  }
  return obj[field]
}

/**
 * 根据地区代码获取完整地区路径
 */
const getRegionPath = (code: string): string => {
  if (!regionData.value.length) {
    return code
  }

  const findPath = (data: any[], targetCode: string, path: string[] = []): string[] | null => {
    for (const item of data) {
      const currentPath = [...path, item.label]

      if (item.value === targetCode) {
        return currentPath
      }

      if (item.children && item.children.length > 0) {
        const result = findPath(item.children, targetCode, currentPath)
        if (result) {
          return result
        }
      }
    }
    return null
  }

  const path = findPath(regionData.value, code)
  return path ? path.join(props.pathSeparator) : code
}

/**
 * 根据级联数据获取完整路径
 */
const getCascaderPath = (code: string): string => {
  if (!resolvedOptions.value.length) {
    return code
  }

  const findPath = (data: any[], targetCode: string, path: string[] = []): string[] | null => {
    for (const item of data) {
      const itemValue = getFieldValue(item, props.valueField)
      const itemLabel = getFieldValue(item, props.labelField)
      const currentPath = [...path, itemLabel]

      if (String(itemValue) === code) {
        return currentPath
      }

      const children = getFieldValue(item, props.childrenField)
      if (Array.isArray(children) && children.length > 0) {
        const result = findPath(children, targetCode, currentPath)
        if (result) {
          return result
        }
      }
    }
    return null
  }

  const path = findPath(resolvedOptions.value, code)
  return path ? path.join(props.pathSeparator) : code
}

/**
 * 处理显示项
 */
const displayItems = computed((): DisplayItem[] => {
  if (!processedValues.value.length) {
    return []
  }

  const items: DisplayItem[] = []

  for (const value of processedValues.value) {
    let displayItem: DisplayItem

    switch (props.mode) {
      case 'region':
        displayItem = {
          label: getRegionPath(value),
          tagType: props.tagType,
          tagClass: props.tagClass
        }
        break

      case 'cascader':
        displayItem = {
          label: getCascaderPath(value),
          tagType: props.tagType,
          tagClass: props.tagClass
        }
        break

      case 'dict':
      default:
        // 支持自定义字段名的字典模式
        const option = resolvedOptions.value.find((opt) => {
          const optionValue = getFieldValue(opt, props.valueField)
          return String(optionValue) === value
        })

        if (option) {
          // 优先使用 i18n 翻译，如果有 dictType 的话（支持自动推断）
          let label: string
          if (inferredDictType.value) {
            const i18nLabel = getDictI18nLabel(inferredDictType.value, value)
            label = i18nLabel || getFieldValue(option, props.labelField)
          } else {
            label = getFieldValue(option, props.labelField)
          }

          displayItem = {
            label,
            tagType: option.elTagType || props.tagType,
            tagClass: option.elTagClass || props.tagClass
          }
        } else {
          continue // 未匹配的值不添加到 displayItems 中
        }
        break
    }

    items.push(displayItem)
  }

  return items
})

/**
 * 计算未匹配的值数组
 *
 * 找出在processedValues中存在但在options中不存在的值
 * 这些值将作为纯文本显示（如果showValue为true）
 */
const unmatchedValues = computed(() => {
  if (!processedValues.value.length) {
    return []
  }

  // 地区和级联模式下，理论上都能找到对应的标签（即使是兜底显示原值）
  if (props.mode === 'region' || props.mode === 'cascader') {
    return []
  }

  // 字典模式下，找出未匹配的值（支持自定义字段名）
  if (!resolvedOptions.value.length) {
    return processedValues.value
  }

  return processedValues.value.filter((item) => {
    return !resolvedOptions.value.some((option) => {
      const optionValue = getFieldValue(option, props.valueField)
      return String(optionValue) === item
    })
  })
})
</script>

<style lang="scss" scoped>
/* 标签间距 */
.tag-spacer {
  margin-right: 8px;
}

/* 未匹配值的样式 */
.unmatched-values {
  color: #909399;
  font-size: 12px;
}

/* 相邻标签的间距 */
.el-tag + .el-tag {
  margin-left: 8px;
}
</style>
