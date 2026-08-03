<!-- 通用过滤树（虚拟滚动 + 搜索防抖，适用于部门/目录等大数据量树形结构，解决搜索卡死） -->
<template>
  <el-card shadow="hover" class="h-full">
    <!-- 可选表头：传入 title 或使用 header 插槽时渲染 -->
    <template v-if="title || $slots.header" #header>
      <slot name="header">
        <span class="font-medium">{{ title }}</span>
      </slot>
    </template>
    <el-input v-model="keyword" :placeholder="placeholder" prefix-icon="Search" clearable />
    <!--
      使用 el-tree-v2（虚拟滚动）替代 el-tree：
      只渲染可视区节点，避免大数据量（上千节点）全量渲染 DOM 导致主线程阻塞卡死。
    -->
    <el-tree-v2
      ref="treeRef"
      class="mt-2"
      :data="data"
      :props="mergedProps"
      :height="height"
      :item-size="itemSize"
      :expand-on-click-node="expandOnClickNode"
      :filter-method="effectiveFilter"
      :highlight-current="highlightCurrent"
      @node-click="(d: any, n: any, e: any) => emit('node-click', d, n, e)"
      @node-expand="(d: any, n: any) => emit('node-expand', d, n)"
      @node-collapse="(d: any, n: any) => emit('node-collapse', d, n)"
    >
      <!-- 自定义节点内容（透传 { node, data }），不传则使用 el-tree-v2 默认渲染 -->
      <template v-if="$slots.default" #default="scope">
        <slot v-bind="scope" />
      </template>
      <!-- 空数据插槽 -->
      <template v-if="$slots.empty" #empty>
        <slot name="empty" />
      </template>
    </el-tree-v2>
  </el-card>
</template>

<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core'

defineOptions({ name: 'ATreeFilter' })

/** 字段映射配置（el-tree-v2 的 props，value 为节点唯一 key） */
interface TreeFieldProps {
  value?: string
  label?: string
  children?: string
  disabled?: string
}

interface Props {
  /** 树数据 */
  data: any[]
  /** 字段映射（与默认值合并）：默认 { value: 'id', label: 'label', children: 'children', disabled: 'disabled' } */
  treeProps?: TreeFieldProps
  /** 虚拟滚动区域高度（px）——el-tree-v2 必填，决定可视区渲染范围 */
  height?: number
  /** 卡片表头标题（不传则不渲染表头，可改用 header 插槽） */
  title?: string
  /** 搜索框占位文案 */
  placeholder?: string
  /** 搜索防抖时长（ms）——避免每次按键都触发全量过滤 */
  debounce?: number
  /** 单个节点高度（px） */
  itemSize?: number
  /** 是否默认展开全部（仅在未传 expandedKeys 受控时生效；虚拟滚动下展开全部也不卡） */
  defaultExpandAll?: boolean
  /** 受控展开的节点 key 列表（传入则以此为准，忽略 defaultExpandAll） */
  expandedKeys?: Array<string | number>
  /** 点击节点是否展开/折叠 */
  expandOnClickNode?: boolean
  /** 是否高亮当前选中节点 */
  highlightCurrent?: boolean
  /** 自定义过滤方法（不传则按 label 字段大小写不敏感匹配） */
  filterMethod?: (query: string, data: any, node: any) => boolean
}

const props = withDefaults(defineProps<Props>(), {
  treeProps: () => ({}),
  height: 500,
  placeholder: '请输入关键词',
  debounce: 300,
  itemSize: 30,
  defaultExpandAll: true,
  expandOnClickNode: false,
  highlightCurrent: true
})

const emit = defineEmits<{
  /** 节点单击 */
  (e: 'node-click', data: any, node: any, event?: MouseEvent): void
  /** 节点展开 */
  (e: 'node-expand', data: any, node: any): void
  /** 节点折叠 */
  (e: 'node-collapse', data: any, node: any): void
}>()

/** 合并后的字段映射（默认值 + 外部传入） */
const mergedProps = computed(() => ({
  value: 'id',
  label: 'label',
  children: 'children',
  disabled: 'disabled',
  ...props.treeProps
}))

/** 树组件引用 */
const treeRef = ref()

/** 搜索关键字 */
const keyword = ref('')

/** 默认过滤方法：按 label 字段大小写不敏感匹配 */
const defaultFilter = (query: string, data: any) => {
  if (!query) return true
  const label = data?.[mergedProps.value.label]
  return typeof label === 'string' && label.toLowerCase().includes(query.toLowerCase())
}

/** 实际生效的过滤方法：优先用外部自定义，否则用默认 */
const effectiveFilter = (query: string, data: any, node: any) =>
  props.filterMethod ? props.filterMethod(query, data, node) : defaultFilter(query, data)

/**
 * 执行过滤（带防抖）。
 * 虚拟滚动只渲染可视区，但 filter 仍需遍历全部节点判定显隐，
 * 故加防抖：连续输入时只在停顿后过滤一次，避免逐字符全量遍历阻塞主线程。
 */
const doFilter = useDebounceFn((value: string) => treeRef.value?.filter(value), props.debounce)
watch(keyword, (value) => doFilter(value))

/** 收集所有「有子节点」的 key（用于 default-expand-all 的等价实现） */
const allParentKeys = computed<Array<string | number>>(() => {
  const keys: Array<string | number> = []
  const { value: valueField, children: childrenField } = mergedProps.value
  const collect = (list: any[]) => {
    list?.forEach((item) => {
      const children = item?.[childrenField]
      if (children && children.length) {
        keys.push(item[valueField])
        collect(children)
      }
    })
  }
  collect(props.data)
  return keys
})

/**
 * 应用展开态。
 * el-tree-v2 没有运行时 default-expand-all，且 default-expanded-keys 仅初始生效，
 * 数据通常异步加载，故在挂载与数据变化时主动调用 setExpandedKeys：
 * - 传入 expandedKeys（受控）→ 用其展开；
 * - 否则 defaultExpandAll 为真 → 展开全部父节点。
 */
const applyExpand = async () => {
  await nextTick()
  if (props.expandedKeys !== undefined) {
    treeRef.value?.setExpandedKeys(props.expandedKeys)
  } else if (props.defaultExpandAll) {
    treeRef.value?.setExpandedKeys(allParentKeys.value)
  }
}

onMounted(applyExpand)
watch(() => props.data, applyExpand)

/** 暴露给父组件的方法 */
defineExpose({
  /** 设置高亮节点（传 undefined 可清空选中） */
  setCurrentKey: (key?: string | number) => treeRef.value?.setCurrentKey(key as any),
  /** 主动触发过滤 */
  filter: (value: string) => treeRef.value?.filter(value),
  /** 设置展开节点 */
  setExpandedKeys: (keys: Array<string | number>) => treeRef.value?.setExpandedKeys(keys),
  /** 获取内部 el-tree-v2 实例（高级用法） */
  getTreeRef: () => treeRef.value
})
</script>
