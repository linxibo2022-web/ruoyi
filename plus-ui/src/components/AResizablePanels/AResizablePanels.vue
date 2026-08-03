<!-- 可调整面板 -->
<template>
  <div class="resizable-panels" :class="{ 'is-resizing': isResizing, 'is-collapsed': collapsed }">
    <!-- 左侧面板 -->
    <div class="left-panel" :style="leftPanelStyle">
      <div class="panel-content" :class="{ 'collapsed': collapsed }">
        <slot name="left" v-if="showLeftContent"></slot>
      </div>

      <!-- 拖拽手柄 -->
      <div v-if="!disabled && !collapsed" class="resize-handle" @mousedown="startResize" :class="{ 'is-active': isResizing }">
        <!-- 现代化拖拽指示器 -->
        <div class="resize-indicator">
          <svg class="resize-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="8" r="1" fill="currentColor" />
            <circle cx="12" cy="12" r="1" fill="currentColor" />
            <circle cx="12" cy="16" r="1" fill="currentColor" />
          </svg>
        </div>

        <!-- 收起按钮 -->
        <div class="collapse-button" @click="handleCollapse" :title="t('button.collapse')">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 展开按钮（收起状态下显示） -->
    <div v-if="collapsed" class="expand-button" @click="handleExpand" :title="t('button.expand')">
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </div>

    <!-- 右侧面板 -->
    <div class="right-panel" :style="rightPanelStyle">
      <div class="panel-content">
        <slot name="right"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AResizablePanels">
import { ref, computed, watch, onUnmounted } from 'vue'

const { t } = useI18n()

/**
 * 组件属性接口
 */
interface AResizablePanelsProps {
  /** 左侧面板宽度（像素） */
  leftWidth: number
  /** 是否收起 */
  collapsed?: boolean
  /** 最小宽度限制（像素） */
  minWidth?: number
  /** 最大宽度限制（像素） */
  maxWidth?: number
  /** 是否禁用拖拽功能 */
  disabled?: boolean
  /** 面板间距（像素） */
  gutter?: number
}

/**
 * 组件事件接口
 */
interface AResizablePanelsEmits {
  /** 左侧宽度变化事件（支持 v-model） */
  'update:leftWidth': [width: number]
  /** 收起状态变化事件（支持 v-model） */
  'update:collapsed': [collapsed: boolean]
}

// 定义组件属性，设置默认值
const props = withDefaults(defineProps<AResizablePanelsProps>(), {
  leftWidth: undefined,
  collapsed: false,
  minWidth: 200, // 默认最小宽度 200px
  maxWidth: 600, // 默认最大宽度 600px
  disabled: false, // 默认启用拖拽
  gutter: 20 // 默认间距 20px
})

// 定义组件事件
const emit = defineEmits<AResizablePanelsEmits>()

/**
 * 响应式状态
 */
// 是否正在拖拽
const isResizing = ref(false)
// 当前拖拽中的宽度值（用于优化性能）
const currentWidth = ref(props.leftWidth ?? props.minWidth)
// 收起前的宽度，用于展开时恢复
const lastWidth = ref(props.leftWidth ?? props.minWidth)
// 内部收起状态
const collapsed = ref(props.collapsed)
// 控制左侧内容是否显示（用于延迟显示）
const showLeftContent = ref(!props.collapsed)
// 是否正在执行展开动画
const isExpanding = ref(false)
// 收起状态的固定宽度
const collapsedWidth = 0

// 拖拽开始时的鼠标X坐标
let startX = 0
// 拖拽开始时的左侧面板宽度
let startWidth = 0
// 动画帧ID
let animationFrameId: number | null = null

/**
 * 计算属性 - 左侧面板样式
 */
const leftPanelStyle = computed(() => {
  const width = collapsed.value ? collapsedWidth : currentWidth.value
  return {
    width: `${width}px`,
    // 拖拽时禁用过渡动画，收起/展开时启用更平滑的动画
    transition: isResizing.value
      ? 'none'
      : isExpanding.value
        ? 'width 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)' // 更平滑的展开动画
        : 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)' // 收起动画保持原样
  }
})

/**
 * 计算属性 - 右侧面板样式
 */
const rightPanelStyle = computed(() => ({
  marginLeft: `${props.gutter}px`,
  // 拖拽时也禁用过渡动画，展开时使用更平滑的动画
  transition: isResizing.value
    ? 'none'
    : isExpanding.value
      ? 'margin 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      : 'margin 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
}))

/**
 * 节流函数 - 优化鼠标移动事件处理
 */
const throttledUpdateWidth = (newWidth: number) => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }

  animationFrameId = requestAnimationFrame(() => {
    // 应用最小和最大宽度限制
    const constrainedWidth = Math.max(props.minWidth, Math.min(props.maxWidth, newWidth))
    currentWidth.value = constrainedWidth
  })
}

/**
 * 处理收起操作
 */
const handleCollapse = (event?: MouseEvent) => {
  // 阻止事件冒泡，防止触发拖拽
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }

  if (!collapsed.value) {
    // 重置展开状态
    isExpanding.value = false

    // 立即隐藏内容
    showLeftContent.value = false

    // 保存当前宽度
    lastWidth.value = currentWidth.value
    collapsed.value = true
    emit('update:collapsed', true)
  }
}

/**
 * 处理展开操作
 */
const handleExpand = (event?: MouseEvent) => {
  // 阻止事件冒泡
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }

  if (collapsed.value) {
    // 标记正在展开，启用更平滑的动画
    isExpanding.value = true

    // 立即更新状态，开始动画
    collapsed.value = false
    currentWidth.value = lastWidth.value
    emit('update:leftWidth', lastWidth.value)
    emit('update:collapsed', false)

    // 立即显示内容，让内容和宽度动画同步
    showLeftContent.value = true

    // 动画完成后重置展开状态
    setTimeout(() => {
      isExpanding.value = false
    }, 400) // 与动画时长一致
  }
}

/**
 * 开始拖拽操作
 * @param event 鼠标按下事件
 */
const startResize = (event: MouseEvent) => {
  // 如果禁用拖拽或已收起，直接返回
  if (props.disabled || collapsed.value) return

  // 阻止默认行为和事件冒泡
  event.preventDefault()
  event.stopPropagation()

  // 确保不在展开状态
  isExpanding.value = false

  // 记录拖拽开始时的状态
  isResizing.value = true
  startX = event.clientX
  startWidth = currentWidth.value

  // 设置拖拽时的全局样式
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'

  // 添加拖拽时的样式类，防止子元素干扰
  document.documentElement.classList.add('resizing-active')

  // 添加全局事件监听器
  document.addEventListener('mousemove', handleMouseMove, { passive: true })
  document.addEventListener('mouseup', stopResize, { once: true })

  // 防止在某些浏览器中出现选择文本的问题
  document.addEventListener('selectstart', preventDefault, { passive: false })
}

/**
 * 阻止默认事件
 */
const preventDefault = (e: Event) => {
  e.preventDefault()
}

/**
 * 处理鼠标移动事件（拖拽过程中）
 * @param event 鼠标移动事件
 */
const handleMouseMove = (event: MouseEvent) => {
  if (!isResizing.value) return

  // 计算鼠标移动的距离
  const deltaX = event.clientX - startX
  // 计算新的左侧面板宽度
  const newWidth = startWidth + deltaX

  // 使用节流优化性能
  throttledUpdateWidth(newWidth)
}

/**
 * 停止拖拽操作
 */
const stopResize = () => {
  if (!isResizing.value) return

  // 清理动画帧
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }

  // 更新拖拽状态
  isResizing.value = false

  // 恢复全局样式
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.documentElement.classList.remove('resizing-active')

  // 移除全局事件监听器
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('selectstart', preventDefault)

  // 发出最终的宽度变化事件
  emit('update:leftWidth', currentWidth.value)
}

/**
 * 监听 props.collapsed 变化，同步内部状态
 */
watch(
  () => props.collapsed,
  (newCollapsed) => {
    collapsed.value = newCollapsed
  }
)

/**
 * 监听 props.leftWidth 变化，同步更新内部状态
 */
watch(
  () => props.leftWidth,
  (newWidth) => {
    if (newWidth !== undefined && !isResizing.value && !collapsed.value) {
      currentWidth.value = newWidth
      lastWidth.value = newWidth
    }
  }
)

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }

  // 确保清理全局状态
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.documentElement.classList.remove('resizing-active')
})
</script>

<style scoped>
/**
 * 主容器样式
 */
.resizable-panels {
  display: flex;
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

/**
 * 拖拽状态下的样式
 * 防止拖拽时选中文字或触发其他交互
 */
.resizable-panels.is-resizing {
  user-select: none;
}

/**
 * 左侧面板样式
 */
.left-panel {
  position: relative;
  flex-shrink: 0;
  background-color: transparent;
  /* 过渡动画通过计算属性动态控制 */
}

/**
 * 右侧面板样式
 */
.right-panel {
  flex: 1;
  min-width: 0;
  background-color: transparent;
  /* 过渡动画通过计算属性动态控制 */
}

/**
 * 面板内容区域样式
 */
.panel-content {
  width: 100%;
  height: 100%;
  overflow: auto;
  /* 优化滚动性能 */
  -webkit-overflow-scrolling: touch;
  /* 添加更平滑的透明度和变换过渡 */
  transition:
    opacity 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    transform 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  opacity: 1;
  transform: translateX(0);
}

.panel-content.collapsed {
  overflow: hidden;
  opacity: 0;
  /* 内容稍微向左偏移，营造更自然的消失效果 */
  transform: translateX(-10px);
}

/**
 * 拖拽手柄样式
 */
.resize-handle {
  position: absolute;
  top: 50%;
  right: -10px;
  width: 20px;
  height: 80px;
  cursor: col-resize;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  z-index: 100;
  transform: translateY(-50%);
  /* 优化拖拽性能 */
  will-change: transform;
}

/**
 * 拖拽指示器容器
 */
.resize-indicator {
  width: 6px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background-color: rgba(156, 163, 175, 0.2);
  border: 1px solid rgba(209, 213, 219, 0.3);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(4px);
  /* 优化渲染性能 */
  will-change: transform, background-color;
}

/**
 * 拖拽指示器中的图标
 */
.resize-icon {
  width: 12px;
  height: 12px;
  color: rgba(107, 114, 128, 0.7);
  transition: color 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/**
 * 收起按钮样式
 */
.collapse-button {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 4px;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(4px);
}

.collapse-button svg {
  width: 14px;
  height: 14px;
  color: rgb(59, 130, 246);
}

/**
 * 展开按钮样式
 */
.expand-button {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(59, 130, 246, 0.9);
  border: 1px solid rgb(59, 130, 246);
  border-radius: 6px;
  cursor: pointer;
  z-index: 101;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);
}

.expand-button svg {
  width: 16px;
  height: 16px;
  color: white;
  /* 添加图标动画 */
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.expand-button:hover {
  background-color: rgb(59, 130, 246);
  transform: translateY(-50%) scale(1.05);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.expand-button:hover svg {
  transform: translateX(2px);
}

/**
 * 拖拽手柄悬停效果
 */
.resize-handle:hover .resize-indicator {
  background-color: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.2);
  transform: scale(1.05);
}

.resize-handle:hover .resize-icon {
  color: rgb(59, 130, 246);
}

.resize-handle:hover .collapse-button {
  opacity: 1;
  transform: scale(1);
}

.collapse-button:hover {
  background-color: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.3);
  transform: scale(1.05);
}

/**
 * 拖拽激活状态
 */
.resize-handle.is-active .resize-indicator {
  background-color: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.3);
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.resize-handle.is-active .resize-icon {
  color: rgb(59, 130, 246);
}

/**
 * 响应式适配
 * 在小屏幕设备上调整拖拽手柄样式
 */
@media (max-width: 768px) {
  .resize-handle {
    width: 24px;
    right: -12px;
    height: 90px;
  }

  .resize-indicator {
    width: 8px;
    height: 45px;
  }

  .resize-icon {
    width: 14px;
    height: 14px;
  }

  .collapse-button {
    width: 22px;
    height: 22px;
  }

  .expand-button {
    width: 32px;
    height: 32px;
    left: 8px;
  }
}

/**
 * 支持暗色主题
 * 当系统偏好暗色模式时的样式调整
 */
@media (prefers-color-scheme: dark) {
  .resize-indicator {
    background-color: rgba(75, 85, 99, 0.3);
    border-color: rgba(107, 114, 128, 0.3);
  }

  .resize-icon {
    color: rgba(156, 163, 175, 0.8);
  }

  .resize-handle:hover .resize-indicator {
    background-color: rgba(59, 130, 246, 0.2);
    border-color: rgba(59, 130, 246, 0.4);
  }

  .resize-handle:hover .resize-icon {
    color: rgb(96, 165, 250);
  }

  .resize-handle.is-active .resize-indicator {
    background-color: rgba(59, 130, 246, 0.25);
    border-color: rgba(59, 130, 246, 0.5);
  }

  .resize-handle.is-active .resize-icon {
    color: rgb(96, 165, 250);
  }

  .collapse-button {
    background-color: rgba(96, 165, 250, 0.15);
    border-color: rgba(96, 165, 250, 0.3);
  }

  .collapse-button svg {
    color: rgb(96, 165, 250);
  }

  .expand-button {
    background-color: rgba(96, 165, 250, 0.9);
    border-color: rgb(96, 165, 250);
  }

  .expand-button:hover {
    background-color: rgb(96, 165, 250);
  }
}

/**
 * 减少动画效果（用户偏好设置）
 */
@media (prefers-reduced-motion: reduce) {
  .resize-handle,
  .resize-indicator,
  .resize-icon,
  .left-panel,
  .right-panel,
  .collapse-button,
  .expand-button,
  .panel-content {
    transition: none !important;
    animation: none !important;
  }
}
</style>

<!-- 全局样式 -->
<style>
/**
 * 拖拽时的全局样式
 * 防止在拖拽过程中触发其他交互
 */
.resizing-active * {
  pointer-events: none !important;
}

.resizing-active .resize-handle {
  pointer-events: auto !important;
}

.resizing-active .collapse-button {
  pointer-events: auto !important;
}
</style>
