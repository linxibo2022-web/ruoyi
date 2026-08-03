<!--
// 1. 基础对话框模式
<AModal v-model="dialogVisible" title="新增用户" @confirm="handleSubmit" @cancel="handleCancel">
  <el-form>
  </el-form>
</AModal>

// 2. 抽屉模式
<AModal
  v-model="drawerVisible"
  title="用户详情"
  mode="drawer"
  direction="rtl"
  size="large"
  :show-footer="false"
>
  <UserDetail :user="selectedUser" />
</AModal>

// 3. 自定义底部
<AModal v-model="customVisible" title="自定义操作">
  <template #footer>
    <el-button @click="customVisible = false">取消</el-button>
    <el-button type="warning" @click="handleSave">保存草稿</el-button>
    <el-button type="primary" @click="handlePublish">发布</el-button>
  </template>
</AModal>

// 4. 无底部按钮
<AModal
  v-model="viewVisible"
  title="查看详情"
  :show-footer="false"
  mode="drawer"
>
  <DetailView :data="detailData" />
</AModal>

// 5. 全屏对话框
<AModal
  v-model="fullscreenVisible"
  title="大数据分析"
  :fullscreen="true"
>
  <DataAnalysis />
</AModal>

// 6. 带确认关闭
<AModal
  v-model="confirmCloseVisible"
  title="编辑文档"
  :before-close="handleBeforeClose"
>
  <TextEditor v-model="content" />
</AModal>

// 7. 可拖动对话框
<AModal
  v-model="movableVisible"
  title="可拖动对话框"
  :movable="true"
>
  <p>可以拖动标题栏移动此对话框</p>
</AModal>
-->
<template>
  <!-- 对话框模式 -->
  <el-dialog
    v-if="isDialogMode"
    v-model="visible"
    :title="title"
    :width="computedWidth"
    :fullscreen="actualFullscreen"
    :close-on-click-modal="maskClosable"
    :close-on-press-escape="keyboard"
    :append-to-body="appendToBody"
    :show-close="closable"
    :destroy-on-close="destroyOnClose"
    :before-close="beforeClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 自定义标题插槽（带全屏切换按钮） -->
    <template #header>
      <div class="amodal-header">
        <span class="amodal-header__title">
          <slot name="header">{{ title }}</slot>
        </span>
        <!-- 全屏按钮容器：使用绝对定位放在关闭按钮左边 -->
        <div v-if="showFullscreenToggle" class="amodal-fullscreen-wrapper">
          <el-tooltip :content="actualFullscreen ? t('navbar.exitFull') : t('navbar.full')" placement="bottom">
            <button class="amodal-fullscreen-btn" type="button" @click="toggleFullscreen">
              <Icon :code="actualFullscreen ? 'exit-fullscreen' : 'fullscreen'" :size="16" />
            </button>
          </el-tooltip>
        </div>
      </div>
    </template>

    <!-- 主要内容区域，支持加载状态 -->
    <div v-loading="loading" class="amodal-content">
      <slot />
    </div>

    <!-- 默认底部操作按钮 -->
    <template v-if="showFooter && !$slots.footer" #footer>
      <div :class="footerClass">
        <slot name="footer">
          <!-- 根据 footerType 显示不同的按钮组合 -->
          <template v-if="footerType === 'close-only'">
            <el-button @click="handleCancel">{{ cancelText || t('Close', '关闭') }}</el-button>
          </template>
          <template v-else>
            <el-button @click="handleCancel">{{ cancelText || t('Cancel', '取消') }}</el-button>
            <el-button type="primary" :loading="loading" @click="handleConfirm">
              {{ confirmText || t('Confirm', '确定') }}
            </el-button>
          </template>
        </slot>
      </div>
    </template>

    <!-- 自定义底部插槽 -->
    <template v-else-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-dialog>

  <!-- 抽屉模式 -->
  <el-drawer
    v-else
    v-model="visible"
    :title="title"
    :size="computedWidth"
    :direction="direction"
    :close-on-click-modal="maskClosable"
    :with-header="true"
    :show-close="closable"
    :destroy-on-close="destroyOnClose"
    :append-to-body="appendToBody"
    :before-close="beforeClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 自定义标题插槽 -->
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>

    <!-- 主要内容区域，支持加载状态 -->
    <div v-loading="loading" class="amodal-content">
      <slot />
    </div>

    <!-- 默认底部操作按钮 -->
    <template v-if="showFooter && !$slots.footer" #footer>
      <div :class="footerClass">
        <slot name="footer">
          <!-- 根据 footerType 显示不同的按钮组合 -->
          <template v-if="footerType === 'close-only'">
            <el-button @click="handleCancel">{{ cancelText || t('Close', '关闭') }}</el-button>
          </template>
          <template v-else>
            <el-button @click="handleCancel">{{ cancelText || t('Cancel', '取消') }}</el-button>
            <el-button type="primary" :loading="loading" @click="handleConfirm">
              {{ confirmText || t('Confirm', '确定') }}
            </el-button>
          </template>
        </slot>
      </div>
    </template>

    <!-- 自定义底部插槽 -->
    <template v-else-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, provide, watch, nextTick, onUnmounted, ref } from 'vue'

const { t } = useI18n()

/**
 * AModal 组件属性接口定义
 */
interface AModalProps {
  /** 控制模态框显示/隐藏状态 */
  modelValue: boolean

  /** 模态框模式：dialog-对话框，drawer-抽屉 */
  mode?: 'dialog' | 'drawer'

  /** 模态框标题 */
  title?: string

  /** 自定义宽度/尺寸，可以是具体数值或百分比 */
  width?: string | number

  /** 预设尺寸：small-小，medium-中等，large-大，xl-超大 */
  size?: 'small' | 'medium' | 'large' | 'xl'

  // ========== 行为控制相关 ==========
  /** 是否显示关闭按钮 */
  closable?: boolean

  /** 是否可以通过点击遮罩层关闭 */
  maskClosable?: boolean

  /** 是否可以通过 ESC 键关闭 */
  keyboard?: boolean

  /** 关闭时是否销毁内部元素 */
  destroyOnClose?: boolean

  /** 是否将模态框挂载到 body 元素下 */
  appendToBody?: boolean

  /** 关闭前的回调函数，可以用来阻止关闭 */
  beforeClose?: (done: () => void) => void

  /** 是否可以拖动（仅对话框模式有效，全屏模式下无效） */
  movable?: boolean

  // ========== 抽屉模式特有属性 ==========
  /** 抽屉弹出方向：ltr-左到右，rtl-右到左，ttb-上到下，btt-下到上 */
  direction?: 'ltr' | 'rtl' | 'ttb' | 'btt'

  // ========== 内容控制相关 ==========
  /** 是否显示底部操作区域 */
  showFooter?: boolean

  /** 底部按钮类型：default-确定+取消，close-only-仅关闭按钮 */
  footerType?: 'default' | 'close-only'

  /** 底部按钮对齐方式 */
  footerAlign?: 'left' | 'center' | 'right'

  /** 内容区域是否显示加载状态 */
  loading?: boolean

  /** 是否全屏显示（仅对话框模式有效） */
  fullscreen?: boolean

  /** 是否显示全屏切换按钮（仅对话框模式有效） */
  showFullscreenToggle?: boolean

  // ========== 按钮文本自定义 ==========
  /** 确认按钮文本 */
  confirmText?: string

  /** 取消按钮文本 */
  cancelText?: string
}

/**
 * 设置组件默认属性值
 */
const props = withDefaults(defineProps<AModalProps>(), {
  modelValue: false,
  mode: 'dialog', // 默认使用对话框模式
  size: 'medium', // 默认中等尺寸
  closable: true, // 默认显示关闭按钮
  maskClosable: false, // 默认可点击遮罩关闭
  keyboard: true, // 默认支持 ESC 键关闭
  destroyOnClose: true, // 默认关闭时销毁内容
  appendToBody: true, // 默认挂载到 body
  movable: false, // 默认不可拖动
  direction: 'rtl', // 抽屉默认从右侧弹出
  showFooter: true, // 默认显示底部操作区
  footerType: 'default', // 默认显示确定+取消按钮
  footerAlign: 'right', // 默认底部按钮右对齐
  loading: false, // 默认无加载状态
  fullscreen: false, // 默认非全屏
  showFullscreenToggle: false // 默认不显示全屏切换按钮
})

/**
 * 定义组件可触发的事件
 */
const emit = defineEmits<{
  /** 更新 modelValue，实现 v-model 双向绑定 */
  'update:modelValue': [value: boolean]

  /** 用户点击确认按钮时触发 */
  'confirm': []

  /** 用户点击取消按钮时触发 */
  'cancel': []

  /** 模态框开始打开时触发 */
  'open': []

  /** 模态框完全打开后触发 */
  'opened': []

  /** 模态框开始关闭时触发 */
  'close': []

  /** 模态框完全关闭后触发 */
  'closed': []

  /** 全屏状态变化时触发 */
  'fullscreen-change': [fullscreen: boolean]
}>()

// ========== 响应式计算属性 ==========

/**
 * 实现 v-model 双向绑定的计算属性
 */
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**
 * 判断当前是否为对话框模式
 */
const isDialogMode = computed(() => props.mode === 'dialog')

/**
 * 内部全屏状态（用于切换按钮控制）
 */
const internalFullscreen = ref(false)

/**
 * 切换全屏状态
 */
const toggleFullscreen = () => {
  internalFullscreen.value = !internalFullscreen.value
  emit('fullscreen-change', internalFullscreen.value)
}

/**
 * 实际使用的全屏状态
 * 优先使用外部 prop，否则使用内部状态
 */
const actualFullscreen = computed(() => props.fullscreen || internalFullscreen.value)

/**
 * 不同尺寸对应的宽度配置
 */
const sizeMap = {
  small: { dialog: '600px', drawer: '600px' },
  medium: { dialog: '800px', drawer: '800px' },
  large: { dialog: '1000px', drawer: '1000px' },
  xl: { dialog: '1200px', drawer: '1200px' }
}

/**
 * 计算最终使用的宽度/尺寸
 * 优先使用 width 属性，否则根据 size 和 mode 自动选择
 */
const computedWidth = computed(() => {
  if (props.width) {
    return props.width
  }

  const currentSize = sizeMap[props.size]
  return isDialogMode.value ? currentSize.dialog : currentSize.drawer
})

/**
 * 计算底部操作区的样式类名
 */
const footerClass = computed(() => {
  const alignClass = {
    left: 'amodal-footer-left',
    center: 'amodal-footer-center',
    right: 'amodal-footer-right'
  }
  return ['amodal-footer', alignClass[props.footerAlign]]
})

// ========== 事件处理函数 ==========

/**
 * 处理确认按钮点击事件
 */
const handleConfirm = () => {
  emit('confirm')
}

/**
 * 处理取消按钮点击事件
 * 同时关闭模态框
 */
const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

/**
 * 模态框开始打开时的处理
 */
const handleOpen = () => {
  emit('open')
}

/**
 * 模态框完全打开后的处理
 */
const handleOpened = () => {
  emit('opened')
}

/**
 * 模态框开始关闭时的处理
 */
const handleClose = () => {
  emit('close')
}

/**
 * 模态框完全关闭后的处理
 */
const handleClosed = () => {
  // 重置内部全屏状态
  internalFullscreen.value = false
  emit('closed')
}

// ========== 拖动功能实现 ==========

/**
 * 拖动状态
 */
interface DragState {
  isDragging: boolean
  startX: number
  startY: number
  initialLeft: number
  initialTop: number
}

const dragState: DragState = {
  isDragging: false,
  startX: 0,
  startY: 0,
  initialLeft: 0,
  initialTop: 0
}

/**
 * 初始化拖动功能
 */
const initDrag = () => {
  // 只在对话框模式、非全屏、开启拖动功能时启用
  if (!isDialogMode.value || actualFullscreen.value || !props.movable) {
    return
  }

  nextTick(() => {
    const dialogElement = document.querySelector('.el-dialog') as HTMLElement
    if (!dialogElement) return

    const headerElement = dialogElement.querySelector('.el-dialog__header') as HTMLElement
    if (!headerElement) return

    // 设置头部可拖动样式
    headerElement.style.cursor = 'move'
    headerElement.style.userSelect = 'none'

    // 添加事件监听
    headerElement.addEventListener('mousedown', handleMouseDown)
  })
}

/**
 * 清理拖动功能
 */
const cleanupDrag = () => {
  const dialogElement = document.querySelector('.el-dialog') as HTMLElement
  if (dialogElement) {
    const headerElement = dialogElement.querySelector('.el-dialog__header') as HTMLElement
    if (headerElement) {
      headerElement.style.cursor = ''
      headerElement.style.userSelect = ''
      headerElement.removeEventListener('mousedown', handleMouseDown)
    }
  }
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

/**
 * 鼠标按下事件
 */
const handleMouseDown = (e: MouseEvent) => {
  // 如果点击的是关闭按钮或其他按钮，不触发拖动
  const target = e.target as HTMLElement
  if (target.closest('.el-dialog__close') || target.closest('.el-dialog__headerbtn') || target.closest('button')) {
    return
  }

  const dialogElement = document.querySelector('.el-dialog') as HTMLElement
  if (!dialogElement) return

  dragState.isDragging = true
  dragState.startX = e.clientX
  dragState.startY = e.clientY

  // 获取当前对话框的位置
  const rect = dialogElement.getBoundingClientRect()
  dragState.initialLeft = rect.left
  dragState.initialTop = rect.top

  // 添加鼠标移动和释放事件
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)

  // 设置拖动时的光标样式
  const headerElement = dialogElement.querySelector('.el-dialog__header') as HTMLElement
  if (headerElement) {
    headerElement.style.cursor = 'grabbing'
  }

  // 防止文本选中
  e.preventDefault()
}

/**
 * 鼠标移动事件
 */
const handleMouseMove = (e: MouseEvent) => {
  if (!dragState.isDragging) return

  const dialogElement = document.querySelector('.el-dialog') as HTMLElement
  if (!dialogElement) return

  // 计算移动距离
  const deltaX = e.clientX - dragState.startX
  const deltaY = e.clientY - dragState.startY

  // 计算新位置
  const newLeft = dragState.initialLeft + deltaX
  const newTop = dragState.initialTop + deltaY

  // 只限制顶部不能拖出视口，其他方向可以拖出
  // 这样用户可以根据需要把对话框移到屏幕边缘或部分移出屏幕
  const boundedTop = Math.max(0, newTop)

  // 应用位置变换
  dialogElement.style.position = 'fixed'
  dialogElement.style.margin = '0'
  dialogElement.style.left = `${newLeft}px`
  dialogElement.style.top = `${boundedTop}px`
}

/**
 * 鼠标释放事件
 */
const handleMouseUp = () => {
  if (!dragState.isDragging) return

  dragState.isDragging = false

  const dialogElement = document.querySelector('.el-dialog') as HTMLElement
  if (dialogElement) {
    const headerElement = dialogElement.querySelector('.el-dialog__header') as HTMLElement
    if (headerElement) {
      headerElement.style.cursor = 'move'
    }
  }

  // 移除事件监听
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

/**
 * 重置对话框位置
 */
const resetPosition = () => {
  nextTick(() => {
    const dialogElement = document.querySelector('.el-dialog') as HTMLElement
    if (dialogElement) {
      dialogElement.style.position = ''
      dialogElement.style.margin = ''
      dialogElement.style.left = ''
      dialogElement.style.top = ''
    }
  })
}

// 监听对话框打开状态，初始化或清理拖动功能
watch(visible, (newVal) => {
  if (newVal) {
    // 重置位置
    resetPosition()
    // 初始化拖动功能
    initDrag()
  } else {
    // 清理拖动功能
    cleanupDrag()
  }
})

// 监听movable、fullscreen、mode属性变化
watch([() => props.movable, () => props.fullscreen, () => props.mode], () => {
  if (visible.value) {
    cleanupDrag()
    resetPosition()
    initDrag()
  }
})

// 组件卸载时清理
onUnmounted(() => {
  cleanupDrag()
})

// ========== 为子组件提供上下文 ==========

/**
 * 提供弹窗尺寸给子组件使用
 * 子组件（如AFormInput）可以通过inject获取弹窗尺寸，实现智能响应式布局
 */
provide(
  'modalSize',
  computed(() => props.size)
)
</script>

<style scoped lang="scss">
/**
 * 内容区域基础样式
 * 设置最小高度，避免内容过少时显示异常
 */
.amodal-content {
  min-height: 100px;
}

/**
 * 底部操作区域样式
 */
.amodal-footer {
  display: flex;
  gap: 12px; /* 按钮之间的间距 */

  /* 左对齐 */
  &-left {
    justify-content: flex-start;
  }

  /* 居中对齐 */
  &-center {
    justify-content: center;
  }

  /* 右对齐（默认） */
  &-right {
    justify-content: flex-end;
  }
}

/**
 * 抽屉组件样式调整
 * 使用 :deep() 穿透组件样式
 */
:deep(.el-drawer) {
  /* 抽屉头部样式 */
  .el-drawer__header {
    padding: 16px 24px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    margin-bottom: 0;
  }

  /* 抽屉主体内容样式 */
  .el-drawer__body {
    padding: 24px;
  }
}

/**
 * 对话框组件样式调整
 */
:deep(.el-dialog) {
  /* 对话框头部样式 */
  .el-dialog__header {
    padding: 16px 24px 8px;
  }

  /* 对话框主体内容样式 */
  .el-dialog__body {
    padding: 16px 24px;
  }

  /* 对话框底部样式 */
  .el-dialog__footer {
    padding: 8px 24px 16px;
  }
}

/**
 * 加载遮罩样式优化
 */
:deep(.el-loading-mask) {
  border-radius: 4px;
}

/**
 * 自定义标题栏样式（带全屏切换按钮）
 */
.amodal-header {
  display: flex;
  align-items: center;
  width: 100%;
}

.amodal-header__title {
  flex: 1;
  font-size: var(--el-dialog-title-font-size);
  font-weight: 500;
  color: var(--el-text-color-primary);
  line-height: 24px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/**
 * 全屏按钮容器 - 使用绝对定位放在关闭按钮左边
 * 参考原 gen.vue 的实现：top: 6px, right: 54px
 */
.amodal-fullscreen-wrapper {
  position: absolute;
  top: 6px;
  right: 46px;
}

/**
 * 全屏按钮样式 - 与 el-dialog 关闭按钮风格一致
 */
.amodal-fullscreen-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: none;
  border-radius: 5px;
  background: transparent;
  cursor: pointer;
  color: var(--el-color-info);
  outline: none;
  transition: color 0.2s;

  &:hover {
    color: var(--el-text-color-primary);
    background: var(--el-fill-color-light);
  }

  &:focus {
    outline: none;
  }
}
</style>
