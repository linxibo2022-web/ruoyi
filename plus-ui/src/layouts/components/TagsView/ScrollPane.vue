<!-- ScrollPane.vue -->
<!--
  页签滚动容器组件
  提供标签页的水平滚动功能，支持鼠标滚轮和自动定位
  主要功能：
  1. 鼠标滚轮水平滚动（往上滚动向左移动，往下滚动向右移动）
  2. 自动滚动到指定标签位置
  3. 滚动事件监听和转发
  4. 完全隐藏滚动条但保持滚动功能
-->
<template>
  <el-scrollbar ref="scrollContainerRef" :vertical="false" class="scroll-container" @wheel.prevent="handleMouseWheel">
    <slot />
  </el-scrollbar>
</template>

<script setup lang="ts" name="ScrollPane">
import type { RouteLocationNormalized, RouteLocationNormalizedGeneric } from 'vue-router'

/**
 * 组件事件定义
 */
const emits = defineEmits<{
  scroll: [] // 滚动事件
}>()

// ==================== 常量配置 ====================

/**
 * 标签间距（px）
 * 用于计算滚动位置时的间距调整
 */
const TAG_SPACING = 4

// ==================== 响应式数据 ====================

/**
 * 滚动容器的 Element UI Scrollbar 组件引用
 */
const scrollContainerRef = ref<ElScrollbarInstance>()

// ==================== 计算属性 ====================

/**
 * 获取滚动包装器的 DOM 元素
 * 用于直接操作滚动位置和监听滚动事件
 * @returns 滚动包装器的 HTML 元素或 undefined
 */
const scrollWrapper = computed(() => {
  const wrapRef = scrollContainerRef.value?.$refs.wrapRef
  return wrapRef as HTMLElement | undefined
})

/**
 * 获取标签视图 store 中的访问过的视图列表
 */
const visitedViews = computed(() => {
  const layout = useLayout()
  return layout.visitedViews.value
})

// ==================== 事件处理方法 ====================

/**
 * 处理鼠标滚轮事件
 * 实现自定义的水平滚动行为：往上滚动向左移动，往下滚动向右移动
 * @param e - 鼠标滚轮事件对象
 */
const handleMouseWheel = (e: WheelEvent): void => {
  // 获取滚轮的滚动量，兼容不同浏览器
  const eventDelta = (e as any).wheelDelta || -e.deltaY * 40
  const wrapper = scrollWrapper.value

  if (wrapper) {
    // deltaY < 0 (往上滚动) → eventDelta > 0 → scrollLeft 减少 → 向左移动
    // deltaY > 0 (往下滚动) → eventDelta < 0 → scrollLeft 增加 → 向右移动
    wrapper.scrollLeft = wrapper.scrollLeft - eventDelta / 4
  }
}

/**
 * 处理滚动事件
 * 监听滚动容器的滚动事件并转发给父组件
 */
const handleScrollEvent = (): void => {
  emits('scroll')
}

// ==================== 公共方法 ====================

/**
 * 将视图滚动到指定的目标标签位置
 * 根据目标标签在标签列表中的位置，智能计算最佳的滚动位置
 *
 * 滚动策略：
 * 1. 如果目标是第一个标签，滚动到最左侧
 * 2. 如果目标是最后一个标签，滚动到最右侧
 * 3. 其他情况，确保目标标签及其前后标签都在视口内
 *
 * @param currentTag - 目标路由标签对象
 */
const moveToTarget = (currentTag: RouteLocationNormalized): void => {
  // 获取容器和包装器元素
  const container = scrollContainerRef.value?.$el as HTMLElement
  const wrapper = scrollWrapper.value

  // 基础验证
  if (!container || !wrapper) {
    console.warn('ScrollPane: 容器或包装器元素未找到')
    return
  }

  const containerWidth = container.offsetWidth
  const views = visitedViews.value

  // 处理空数组情况
  if (views.length === 0) {
    console.warn('ScrollPane: 没有可访问的视图')
    return
  }

  const firstTag = views[0]
  const lastTag = views[views.length - 1]

  // 处理边界情况：第一个标签
  if (firstTag === currentTag) {
    wrapper.scrollLeft = 0
    return
  }

  // 处理边界情况：最后一个标签
  if (lastTag === currentTag) {
    wrapper.scrollLeft = wrapper.scrollWidth - containerWidth
    return
  }

  // 计算目标标签的索引位置
  const currentIndex = views.findIndex((item) => item === currentTag)

  // 索引验证
  if (currentIndex === -1) {
    console.warn('ScrollPane: 目标标签在视图列表中未找到')
    return
  }

  // 边界检查：如果是第一个或最后一个，直接返回（已在上面处理）
  if (currentIndex === 0 || currentIndex === views.length - 1) {
    return
  }

  // 查找目标标签前后的 DOM 元素
  const { prevTagElement, nextTagElement } = findAdjacentTagElements(views as RouteLocationNormalized[], currentIndex)

  // 确保找到了前后标签的 DOM 元素
  if (!prevTagElement || !nextTagElement) {
    console.warn('ScrollPane: 未找到目标标签的前后 DOM 元素')
    return
  }

  // 计算最佳滚动位置
  calculateOptimalScrollPosition(wrapper, containerWidth, prevTagElement, nextTagElement)
}

// ==================== 私有辅助方法 ====================

/**
 * 查找指定索引标签的前后相邻 DOM 元素
 * @param views - 视图列表
 * @param currentIndex - 当前标签索引
 * @returns 包含前后标签 DOM 元素的对象
 */
const findAdjacentTagElements = (
  views: RouteLocationNormalizedGeneric[],
  currentIndex: number
): { prevTagElement: HTMLElement | null; nextTagElement: HTMLElement | null } => {
  // 获取所有标签的 DOM 元素
  const tagListDom = document.getElementsByClassName('tags-view-item') as HTMLCollectionOf<HTMLElement>

  let prevTagElement: HTMLElement | null = null
  let nextTagElement: HTMLElement | null = null

  // 获取前后标签的路径用于匹配
  const prevTagPath = views[currentIndex - 1]?.path
  const nextTagPath = views[currentIndex + 1]?.path

  // 遍历 DOM 元素查找匹配的前后标签
  for (let i = 0; i < tagListDom.length; i++) {
    const tagDom = tagListDom[i]
    const tagPath = tagDom.dataset.path

    if (tagPath === prevTagPath) {
      prevTagElement = tagDom
    }
    if (tagPath === nextTagPath) {
      nextTagElement = tagDom
    }

    // 如果都找到了，提前退出循环
    if (prevTagElement && nextTagElement) {
      break
    }
  }

  return { prevTagElement, nextTagElement }
}

/**
 * 计算并设置最佳滚动位置
 * 确保目标标签的前后标签都在视口范围内
 * @param wrapper - 滚动包装器元素
 * @param containerWidth - 容器宽度
 * @param prevTagElement - 前一个标签的 DOM 元素
 * @param nextTagElement - 后一个标签的 DOM 元素
 */
const calculateOptimalScrollPosition = (
  wrapper: HTMLElement,
  containerWidth: number,
  prevTagElement: HTMLElement,
  nextTagElement: HTMLElement
): void => {
  // 计算后一个标签右边界位置（包含间距）
  const afterNextTagOffsetLeft = nextTagElement.offsetLeft + nextTagElement.offsetWidth + TAG_SPACING

  // 计算前一个标签左边界位置（包含间距）
  const beforePrevTagOffsetLeft = prevTagElement.offsetLeft - TAG_SPACING

  // 判断是否需要向右滚动（后一个标签超出右边界）
  if (afterNextTagOffsetLeft > wrapper.scrollLeft + containerWidth) {
    wrapper.scrollLeft = afterNextTagOffsetLeft - containerWidth
  }
  // 判断是否需要向左滚动（前一个标签超出左边界）
  else if (beforePrevTagOffsetLeft < wrapper.scrollLeft) {
    wrapper.scrollLeft = beforePrevTagOffsetLeft
  }
}

// ==================== 生命周期钩子 ====================

/**
 * 组件挂载时的初始化操作
 * 添加滚动事件监听器
 */
onMounted(() => {
  const wrapper = scrollWrapper.value
  if (wrapper) {
    wrapper.addEventListener('scroll', handleScrollEvent, true)
  }
})

/**
 * 组件卸载前的清理操作
 * 移除滚动事件监听器，防止内存泄漏
 */
onBeforeUnmount(() => {
  const wrapper = scrollWrapper.value
  if (wrapper) {
    wrapper.removeEventListener('scroll', handleScrollEvent)
  }
})

// ==================== 方法导出 ====================

/**
 * 向父组件暴露的公共方法
 */
defineExpose({
  moveToTarget
})
</script>

<style lang="scss" scoped>
/*
  滚动容器样式
  主要功能：
  1. 设置容器基础样式
  2. 完全隐藏滚动条（支持所有主流浏览器）
  3. 保持滚动功能正常工作
*/
.scroll-container {
  /* 基础容器样式 */
  white-space: nowrap; /* 防止标签换行 */
  position: relative;
  overflow: hidden; /* 隐藏溢出内容 */
  width: 100%;

  /* 隐藏 Element UI 的滚动条组件 */
  :deep(.el-scrollbar__bar) {
    display: none !important;
  }

  /* 滚动包装器样式配置 */
  :deep(.el-scrollbar__wrap) {
    height: 49px; /* 固定高度，与标签容器高度匹配 */
    overflow-x: auto; /* 允许水平滚动 */

    /* 兼容 Webkit 内核浏览器（Chrome, Safari, Edge） - 使用更高优先级 */
    &::-webkit-scrollbar {
      display: none !important; /* 隐藏滚动条 */
      width: 0 !important;
      height: 0 !important;
    }
    &::-webkit-scrollbar-track {
      display: none !important;
    }

    &::-webkit-scrollbar-thumb {
      display: none !important;
    }

    /* 兼容 Firefox */
    scrollbar-width: none !important; /* 隐藏滚动条 */
    scrollbar-color: transparent transparent !important;
    -ms-overflow-style: none !important; /* 兼容 IE/Edge */

    /* Webkit 内核浏览器（Chrome、Safari）：隐藏滚动条 */
    &::-webkit-scrollbar {
      display: none;
    }
  }
}
</style>
