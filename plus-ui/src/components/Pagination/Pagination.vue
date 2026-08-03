<!-- 分页器 -->
<template>
  <div v-show="!hidden && total > 0" class="pagination-container" :class="[`pagination-${float}`]">
    <el-pagination
      :size="size"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :background="background"
      :layout="layout"
      :page-sizes="pageSizes"
      :pager-count="pagerCount"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts" name="Pagination">
import { scrollTo } from '@/utils/scroll'
import { useMediaQuery } from '@vueuse/core'

/**
 * 分页事件参数接口
 */
interface PaginationEvent {
  page: number
  limit: number
}

/**
 * 分页组件的属性接口
 */
interface PaginationProps {
  /**
   * 总记录数
   * @default 0
   */
  total?: number

  /**
   * 当前页码
   * @default 1
   */
  page?: number

  /**
   * 每页显示记录数
   * @default 20
   */
  limit?: number

  /**
   * 可选的每页显示记录数
   * @default [10, 50, 200, 1000]
   */
  pageSizes?: number[]

  /**
   * 页码按钮的数量
   * 移动端默认5个，PC端默认7个
   * @default 根据屏幕宽度自适应
   */
  pagerCount?: number

  /**
   * 组件大小
   */
  size?: ElSize

  /**
   * 分页布局
   * @default 'total, sizes, prev, pager, next, jumper'
   */
  layout?: string

  /**
   * 是否为分页按钮添加背景色
   * @default true
   */
  background?: boolean

  /**
   * 切换页码时是否自动滚动到顶部
   * @default true
   */
  autoScroll?: boolean

  /**
   * 滚动动画持续时间（毫秒）
   * @default 800
   */
  scrollDuration?: number

  /**
   * 是否隐藏分页组件
   * @default false
   */
  hidden?: boolean

  /**
   * 分页组件的对齐方式
   * @default 'right'
   */
  float?: 'left' | 'right' | 'center'

  /**
   * 是否在页码改变时重置到第一页（当页码超出范围时）
   * @default true
   */
  resetOnSizeChange?: boolean
}

// 使用 withDefaults 定义 props，提供默认值和类型
const props = withDefaults(defineProps<PaginationProps>(), {
  total: 0,
  page: 1,
  limit: 20,
  pageSizes: () => [10, 50, 200, 1000],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  autoScroll: true,
  scrollDuration: 800,
  hidden: false,
  float: 'right',
  resetOnSizeChange: true,
  pagerCount: undefined
})

// 定义 emit 事件
const emit = defineEmits<{
  'update:page': [page: number]
  'update:limit': [limit: number]
  'pagination': [event: PaginationEvent]
  'size-change': [size: number]
  'current-change': [current: number]
}>()

// 使用媒体查询检测屏幕尺寸
const isMobile = useMediaQuery('(max-width: 991px)')

// 计算 pagerCount
const pagerCount = computed(() => {
  if (props.pagerCount !== undefined) {
    return props.pagerCount
  }
  return isMobile.value ? 5 : 7
})

// 当前页码的计算属性
const currentPage = computed({
  get: () => props.page,
  set: (val: number) => emit('update:page', val)
})

// 每页显示记录数的计算属性
const pageSize = computed({
  get: () => props.limit,
  set: (val: number) => emit('update:limit', val)
})

// 计算总页数
const totalPages = computed(() => Math.ceil(props.total / props.limit))

/**
 * 执行滚动操作
 */
const performScroll = async () => {
  if (props.autoScroll) {
    await nextTick()
    scrollTo(0, props.scrollDuration)
  }
}

/**
 * 触发分页事件
 */
const emitPaginationEvent = (page: number, limit: number) => {
  const event: PaginationEvent = { page, limit }
  emit('pagination', event)
}

/**
 * 处理每页显示记录数变更
 */
const handleSizeChange = async (val: number) => {
  let newPage = currentPage.value

  // 检查当前页是否超出新的总页数
  if (props.resetOnSizeChange) {
    const newTotalPages = Math.ceil(props.total / val)
    if (currentPage.value > newTotalPages) {
      newPage = Math.max(1, newTotalPages)
      currentPage.value = newPage
    }
  }

  // 触发事件
  emit('size-change', val)
  emitPaginationEvent(newPage, val)

  // 执行滚动
  await performScroll()
}

/**
 * 处理当前页码变更
 */
const handleCurrentChange = async (val: number) => {
  // 触发事件
  emit('current-change', val)
  emitPaginationEvent(val, pageSize.value)

  // 执行滚动
  await performScroll()
}

// 暴露组件方法供父组件调用
defineExpose({
  /**
   * 跳转到指定页码
   */
  goToPage: (page: number) => {
    currentPage.value = Math.max(1, Math.min(page, totalPages.value))
  },

  /**
   * 跳转到第一页
   */
  goToFirst: () => {
    currentPage.value = 1
  },

  /**
   * 跳转到最后一页
   */
  goToLast: () => {
    currentPage.value = totalPages.value
  },

  /**
   * 获取当前分页信息
   */
  getPaginationInfo: () => ({
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    total: props.total,
    totalPages: totalPages.value
  })
})
</script>

<style lang="scss" scoped>
.pagination-container {
  padding: 16px 0;
  display: flex;

  &.pagination-left {
    justify-content: flex-start;
  }

  &.pagination-right {
    justify-content: flex-end;
  }

  &.pagination-center {
    justify-content: center;
  }

  .el-pagination {
    display: inline-flex;
  }
}

// 响应式样式
@media (max-width: 768px) {
  .pagination-container {
    padding: 12px 0;

    :deep(.el-pagination) {
      justify-content: center;

      .el-pagination__total,
      .el-pagination__jump {
        display: none;
      }
    }
  }
}
</style>
