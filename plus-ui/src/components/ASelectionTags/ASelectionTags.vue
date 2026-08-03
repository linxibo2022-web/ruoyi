<!--
可选中的标签组件

<ASelectionTags
  :items="selectedUsers"
  @close="removeUser"
  @clear="clearAll"
>
  <template #header>
    <span class="text-sm text-gray-500 mb-2 block">已选择用户：</span>
  </template>
</ASelectionTags>
-->
<template>
  <div v-if="visible && items.length > 0" class="mt-2">
    <!-- 头部插槽，可用于添加标题或说明文字 -->
    <slot name="header"></slot>
    <!-- 使用 el-tag 组件渲染每个选中项 -->
    <el-tag
      v-for="item in items"
      :key="getKey(item)"
      :type="type"
      :effect="effect"
      :size="size"
      :color="color"
      class="mr-1 mb-1"
      :closable="closable"
      @close="handleClose(item)"
    >
      <!-- 默认插槽，允许自定义标签内容 -->
      <slot :item="item" name="default">
        {{ formatText(item) }}
      </slot>
    </el-tag>
    <!-- 尾部插槽，可用于添加操作按钮或额外信息 -->
    <slot name="footer">
      <!-- 只有当提供了onClear方法时才显示清空按钮 -->
      <el-button v-if="onClear" size="small" link type="primary" @click="handleClear">
        <el-icon>
          <Delete />
        </el-icon>
        {{ t('button.clean') }}
      </el-button>
    </slot>
  </div>
</template>

<script setup lang="ts" name="ASelectionTags">
const { t } = useI18n()

/**
 * ASelectionTags 组件的属性接口
 * 用于显示一组可选中的标签，支持自定义显示内容和样式
 */
interface ASelectionTagsProps {
  /**
   * 要展示的选中项数组
   * 每个项应该是一个对象，包含唯一标识符和显示信息
   */
  items: any[]

  /**
   * 是否可关闭
   * 为 true 时，标签右侧会显示关闭图标，点击后会触发 close 事件
   * @default true
   */
  closable?: boolean

  /**
   * 是否显示
   * 为 false 时，整个组件将不显示
   * @default true
   */
  visible?: boolean
  /**
   * 标签类型
   * 影响标签的颜色和外观，与 Element Plus 的 Tag 组件类型一致
   * @default 'primary'
   */
  type?: ElTagType
  /**
   * 标签效果
   * 影响标签的显示效果，可选 'light'、'dark' 或 'plain'
   * @default 'light'
   */
  effect?: ElEffect
  /**
   * 标签大小
   * 控制标签的尺寸，可选 'large'、'default' 或 'small'
   * @default 'default'
   */
  size?: ElSize
  /**
   * 标签颜色
   * 自定义标签的背景色，会覆盖 type 属性设置的颜色
   * @default ''
   */
  color?: string
  /**
   * 主键字段名
   * 用于从每个项中提取唯一标识符的字段名
   * @default 'id'
   */
  keyField?: string
  /**
   * 文本格式化函数
   * 用于从每个项中提取显示文本的函数
   * 如果未提供，将尝试从常见字段中获取显示文本
   * @param item 当前项
   * @returns 显示文本
   * @default 自动从常见字段提取
   */
  formatter?: (item: any) => string
  /**
   * 清空选择的回调函数
   * 当用户点击清空按钮时调用
   * 如果提供了此函数，将使用它而不是发出clear事件
   */
  onClear?: () => void
}

const props = withDefaults(defineProps<ASelectionTagsProps>(), {
  closable: true,
  visible: true,
  type: 'success',
  effect: 'light',
  size: 'default',
  color: '',
  keyField: 'id'
})

// 创建默认的格式化函数，不再在 defineProps 中定义
const defaultFormatter = (item: any, keyField: string): string => {
  // 尝试从常见字段中提取显示文本
  // 按优先级顺序尝试: label > name > title > value > text > key > keyField > JSON
  return String(item.label || item.name || item.title || item.value || item.text || item.key || item[keyField] || JSON.stringify(item))
}

/**
 * 组件事件定义
 */
const emit = defineEmits(['close'])

/**
 * 获取项的唯一键
 * 根据 keyField 属性从项中提取唯一标识符
 *
 * @param item 当前项
 * @returns 项的唯一键
 */
const getKey = (item: any): any => {
  return item[props.keyField]
}

/**
 * 获取项的显示文本
 * 使用 formatter 属性指定的格式化函数从项中提取显示文本
 *
 * @param item 当前项
 * @returns 格式化后的显示文本
 */
const formatText = (item: any): string => {
  // 使用提供的 formatter 或默认的格式化函数
  return props.formatter ? props.formatter(item) : defaultFormatter(item, props.keyField)
}

/**
 * 处理标签关闭事件
 * 当用户点击标签的关闭图标时触发
 *
 * @param item 被关闭的项
 */
const handleClose = (item: any): void => {
  const key = getKey(item)
  emit('close', key, item)
}

/**
 * 处理清空所有选中项的事件
 * 当用户点击清空按钮时触发
 */
const handleClear = (): void => {
  // 由于只在有onClear时才显示按钮，所以这里可以直接调用
  props.onClear?.()
}
</script>
