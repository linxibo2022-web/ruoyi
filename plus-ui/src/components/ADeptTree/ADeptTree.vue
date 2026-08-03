<!-- 部门树（基于 ATreeFilter 预置部门字段与文案；虚拟滚动 + 搜索防抖，解决大数据量卡死） -->
<template>
  <ATreeFilter
    ref="treeFilterRef"
    :data="data"
    :height="height"
    :title="title"
    :placeholder="placeholder"
    :debounce="debounce"
    :item-size="itemSize"
    :default-expand-all="defaultExpandAll"
    @node-click="(d: any) => emit('node-click', d)"
  >
    <!-- 透传自定义表头插槽 -->
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
  </ATreeFilter>
</template>

<script setup lang="ts">
import type { SysDeptTreeVo } from '@/api/system/core/dept/deptTypes'

defineOptions({ name: 'ADeptTree' })

interface Props {
  /** 部门树数据 */
  data: SysDeptTreeVo[]
  /** 虚拟滚动区域高度（px） */
  height?: number
  /** 卡片表头标题（不传则不渲染表头） */
  title?: string
  /** 搜索框占位文案 */
  placeholder?: string
  /** 搜索防抖时长（ms） */
  debounce?: number
  /** 单个节点高度（px） */
  itemSize?: number
  /** 是否默认展开全部 */
  defaultExpandAll?: boolean
}

withDefaults(defineProps<Props>(), {
  height: 500,
  placeholder: '请输入部门名称',
  debounce: 300,
  itemSize: 30,
  defaultExpandAll: true
})

const emit = defineEmits<{
  /** 节点单击事件，回传选中的部门节点数据 */
  (e: 'node-click', data: SysDeptTreeVo): void
}>()

/** ATreeFilter 实例引用 */
const treeFilterRef = ref()

/** 暴露给父组件的方法（委托给 ATreeFilter） */
defineExpose({
  /** 设置高亮节点（传 undefined 可清空选中，用于重置查询） */
  setCurrentKey: (key?: string | number) => treeFilterRef.value?.setCurrentKey(key),
  /** 主动触发过滤 */
  filter: (value: string) => treeFilterRef.value?.filter(value)
})
</script>
