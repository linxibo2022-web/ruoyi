/**
 * 响应式 Span 组合函数集合
 * @description 提供统一的响应式栅格计算逻辑，包含屏幕响应式、容器响应式等多种模式
 */
import { computed, ref, inject, onMounted, onUnmounted, unref, nextTick, readonly, type Ref } from 'vue'
import { useMediaQuery } from '@vueuse/core'

// ========== 配置常量 ==========

/**
 * 屏幕断点配置
 */
const SCREEN_BREAKPOINTS = {
  xs: '(max-width: 767px)',
  sm: '(min-width: 768px) and (max-width: 991px)',
  md: '(min-width: 992px) and (max-width: 1199px)',
  lg: '(min-width: 1200px) and (max-width: 1919px)',
  xl: '(min-width: 1920px)'
}

/**
 * 容器宽度断点配置
 */
const CONTAINER_BREAKPOINTS = {
  xs: 480, // 超小容器
  sm: 600, // 小容器
  md: 800, // 中等容器
  lg: 1000, // 大容器
  xl: 1200 // 超大容器
}

/**
 * 预设的自动响应式配置
 */
const DEFAULT_RESPONSIVE_CONFIG: ResponsiveSpan = {
  xs: 24, // 手机/超小容器：全宽
  sm: 24, // 小屏/小容器：全宽
  md: 12, // 中屏/中等容器：一行两个
  lg: 12, // 大屏/大容器：一行两个
  xl: 8 // 超大屏/超大容器：一行三个
}

/**
 * 针对小弹窗优化的响应式配置
 * 小弹窗内容有限，应该优先使用全宽布局提升用户体验
 */
const SMALL_MODAL_RESPONSIVE_CONFIG: ResponsiveSpan = {
  xs: 24, // 手机：全宽
  sm: 24, // 小屏/小弹窗：全宽（重点优化）
  md: 24, // 中屏：全宽（重点优化）
  lg: 12, // 大屏：一行两个
  xl: 8 // 超大屏：一行三个
}

// ========== 组合函数 ==========

/**
 * 基于屏幕尺寸的响应式 Span 组合函数
 * @param spanProp 传入的 span 属性值
 * @returns 响应式计算的结果
 */
export const useScreenResponsiveSpan = (spanProp: Ref<SpanType> | SpanType) => {
  // 屏幕尺寸检测
  const isXs = useMediaQuery(SCREEN_BREAKPOINTS.xs)
  const isSm = useMediaQuery(SCREEN_BREAKPOINTS.sm)
  const isMd = useMediaQuery(SCREEN_BREAKPOINTS.md)
  const isLg = useMediaQuery(SCREEN_BREAKPOINTS.lg)
  const isXl = useMediaQuery(SCREEN_BREAKPOINTS.xl)

  /**
   * 计算最终的 span 值
   */
  const computedSpan = computed(() => {
    const span = unref(spanProp)

    // undefined：不使用 el-col 包装
    if (span === undefined) {
      return undefined
    }

    // 数字：直接返回固定值
    if (typeof span === 'number') {
      return span
    }

    // 数字字符串：转换为数字后返回（如 "12" -> 12）
    if (typeof span === 'string' && span !== 'auto') {
      const numSpan = Number(span)
      if (!isNaN(numSpan)) {
        return numSpan
      }
    }

    // 'auto' 或 响应式对象：计算响应式值
    let config: ResponsiveSpan
    if (span === 'auto') {
      config = DEFAULT_RESPONSIVE_CONFIG
    } else {
      // 合并用户配置和默认配置
      config = { ...DEFAULT_RESPONSIVE_CONFIG, ...(span as ResponsiveSpan) }
    }

    // 根据当前屏幕尺寸返回对应值
    if (isXl.value && config.xl !== undefined) return config.xl
    if (isLg.value && config.lg !== undefined) return config.lg
    if (isMd.value && config.md !== undefined) return config.md
    if (isSm.value && config.sm !== undefined) return config.sm
    if (isXs.value && config.xs !== undefined) return config.xs

    // 默认返回全宽
    return 24
  })

  /**
   * 是否应该使用 el-col 包装器
   */
  const shouldUseCol = computed(() => {
    return computedSpan.value !== undefined
  })

  return {
    computedSpan,
    shouldUseCol
  }
}

/**
 * 基于容器宽度的响应式 Span 组合函数
 * @param spanProp 传入的 span 属性值
 * @param containerSelector 容器选择器，默认查找最近的 .el-dialog 或 .el-drawer
 * @returns 响应式计算的结果
 */
export const useContainerResponsiveSpan = (spanProp: Ref<SpanType> | SpanType, containerSelector?: string) => {
  const containerWidth = ref(0)
  const resizeObserver = ref<ResizeObserver | null>(null)
  const container = ref<HTMLElement | null>(null)

  /**
   * 获取容器元素
   */
  const findContainer = (): HTMLElement | null => {
    if (containerSelector) {
      return document.querySelector(containerSelector)
    }

    // 自动查找最近的弹窗容器
    const dialogContainer = document.querySelector('.el-dialog')
    const drawerContainer = document.querySelector('.el-drawer')

    return (dialogContainer as HTMLElement) || (drawerContainer as HTMLElement)
  }

  /**
   * 更新容器宽度
   */
  const updateContainerWidth = () => {
    if (container.value) {
      containerWidth.value = container.value.offsetWidth
    }
  }

  /**
   * 初始化监听
   */
  const initializeObserver = () => {
    container.value = findContainer()

    if (container.value) {
      updateContainerWidth()

      // 创建 ResizeObserver 监听容器尺寸变化
      resizeObserver.value = new ResizeObserver(() => {
        updateContainerWidth()
      })

      resizeObserver.value.observe(container.value)
    }
  }

  /**
   * 清理监听
   */
  const cleanup = () => {
    if (resizeObserver.value) {
      resizeObserver.value.disconnect()
      resizeObserver.value = null
    }
  }

  /**
   * 计算当前容器对应的断点
   */
  const currentBreakpoint = computed(() => {
    const width = containerWidth.value

    if (width >= CONTAINER_BREAKPOINTS.xl) return 'xl'
    if (width >= CONTAINER_BREAKPOINTS.lg) return 'lg'
    if (width >= CONTAINER_BREAKPOINTS.md) return 'md'
    if (width >= CONTAINER_BREAKPOINTS.sm) return 'sm'
    return 'xs'
  })

  /**
   * 计算最终的 span 值
   */
  const computedSpan = computed(() => {
    const span = unref(spanProp)

    // undefined：不使用 el-col 包装
    if (span === undefined) {
      return undefined
    }

    // 数字：直接返回固定值
    if (typeof span === 'number') {
      return span
    }

    // 数字字符串：转换为数字后返回（如 "12" -> 12）
    if (typeof span === 'string' && span !== 'auto') {
      const numSpan = Number(span)
      if (!isNaN(numSpan)) {
        return numSpan
      }
    }

    // 'auto' 或 响应式对象：计算响应式值
    let config: ResponsiveSpan
    if (span === 'auto') {
      config = DEFAULT_RESPONSIVE_CONFIG
    } else {
      // 合并用户配置和默认配置
      config = { ...DEFAULT_RESPONSIVE_CONFIG, ...(span as ResponsiveSpan) }
    }

    // 根据当前容器断点返回对应值
    const breakpoint = currentBreakpoint.value
    return config[breakpoint] || 24
  })

  /**
   * 是否应该使用 el-col 包装器
   */
  const shouldUseCol = computed(() => {
    return computedSpan.value !== undefined
  })

  // 生命周期管理
  onMounted(() => {
    // 延迟初始化，确保 DOM 已渲染
    nextTick(() => {
      initializeObserver()
    })
  })

  onUnmounted(() => {
    cleanup()
  })

  return {
    computedSpan,
    shouldUseCol,
    containerWidth: readonly(containerWidth),
    currentBreakpoint: readonly(currentBreakpoint)
  }
}

/**
 * 基于 Modal 尺寸的响应式 Span 组合函数
 * @param spanProp span 配置
 * @param modalSize AModal 的 size 属性值
 */
export const useModalSizeResponsiveSpan = (spanProp: Ref<SpanType> | SpanType, modalSize: Ref<string> | string = 'medium') => {
  // 屏幕尺寸检测 - 用于判断手机模式
  const isXs = useMediaQuery(SCREEN_BREAKPOINTS.xs)
  const isSm = useMediaQuery(SCREEN_BREAKPOINTS.sm)
  const isMd = useMediaQuery(SCREEN_BREAKPOINTS.md)
  const isLg = useMediaQuery(SCREEN_BREAKPOINTS.lg)
  const isXl = useMediaQuery(SCREEN_BREAKPOINTS.xl)

  /**
   * 获取当前实际屏幕断点 - 优先级最高
   * 即使在弹窗中，也要基于真实屏幕宽度判断是否是手机模式
   */
  const getActualScreenBreakpoint = () => {
    if (isXl.value) return 'xl'
    if (isLg.value) return 'lg'
    if (isMd.value) return 'md'
    if (isSm.value) return 'sm'
    if (isXs.value) return 'xs'
    return 'md' // 默认中等
  }

  /**
   * 根据 modal size 映射到容器断点
   */
  const getBreakpointBySize = (size: string) => {
    const sizeToBreakpoint: Record<string, keyof ResponsiveSpan> = {
      small: 'sm',
      medium: 'md',
      large: 'lg',
      xl: 'xl'
    }
    return sizeToBreakpoint[size] || 'md'
  }

  /**
   * 根据 modal size 选择最适合的响应式配置
   */
  const getConfigByModalSize = (size: string) => {
    // 小弹窗使用优化的配置，优先全宽布局
    if (size === 'small') {
      return SMALL_MODAL_RESPONSIVE_CONFIG
    }
    // 其他尺寸使用默认配置
    return DEFAULT_RESPONSIVE_CONFIG
  }

  /**
   * 计算最终的 span 值
   */
  const computedSpan = computed(() => {
    const span = unref(spanProp)
    const size = unref(modalSize)

    // undefined：不使用 el-col 包装
    if (span === undefined) {
      return undefined
    }

    // 数字：直接返回固定值
    if (typeof span === 'number') {
      return span
    }

    // 数字字符串：转换为数字后返回（如 "12" -> 12）
    if (typeof span === 'string' && span !== 'auto') {
      const numSpan = Number(span)
      if (!isNaN(numSpan)) {
        return numSpan
      }
    }

    // 'auto' 或 响应式对象：计算响应式值
    let config: ResponsiveSpan
    if (span === 'auto') {
      // 根据弹窗尺寸智能选择配置
      config = getConfigByModalSize(size)
    } else {
      // 用户自定义配置，合并默认配置
      config = { ...getConfigByModalSize(size), ...(span as ResponsiveSpan) }
    }

    // 优先使用实际屏幕断点（手机模式），其次使用 modal size 断点
    const screenBreakpoint = getActualScreenBreakpoint()
    // 如果是手机模式，直接返回 config['xs'] 或 config['sm']
    if ((screenBreakpoint === 'xs' || screenBreakpoint === 'sm') && config[screenBreakpoint] !== undefined) {
      return config[screenBreakpoint]
    }

    // 非手机模式，根据 modal size 获取对应断点的值
    const breakpoint = getBreakpointBySize(size)
    return config[breakpoint] || 24
  })

  const shouldUseCol = computed(() => {
    return computedSpan.value !== undefined
  })

  return {
    computedSpan,
    shouldUseCol
  }
}

/**
 * 统一的响应式逻辑入口函数 - 智能版本
 * @param spanProp span 配置
 * @param options 配置选项
 */
export const useResponsiveSpan = (
  spanProp: Ref<SpanType> | SpanType,
  options: {
    mode?: 'screen' | 'container' | 'modal-size'
    modalSize?: Ref<string> | string
    containerSelector?: string
  } = {}
) => {
  const { mode, modalSize, containerSelector } = options

  /**
   * 尝试从父级AModal获取弹窗尺寸
   * 如果组件在AModal内部，会自动获取其size属性
   */
  const injectedModalSize = inject<Ref<string>>('modalSize', ref('medium'))

  /**
   * 智能选择modalSize：优先使用props传入的值，否则使用注入的值
   */
  const actualModalSize = computed(() => {
    const userModalSize = unref(modalSize)
    return userModalSize || injectedModalSize.value
  })

  /**
   * 智能选择响应式模式：如果在弹窗环境中，优先使用modal-size模式
   */
  const actualMode = computed(() => {
    // 如果明确指定了模式，使用指定的模式
    if (mode) {
      return mode
    }

    // 如果能获取到modalSize（说明在弹窗中），使用modal-size模式
    if (injectedModalSize.value || unref(modalSize)) {
      return 'modal-size'
    }

    // 否则使用默认的container模式
    return 'container'
  })

  switch (actualMode.value) {
    case 'screen':
      return useScreenResponsiveSpan(spanProp)

    case 'container':
      return useContainerResponsiveSpan(spanProp, containerSelector)

    case 'modal-size':
    default:
      return useModalSizeResponsiveSpan(spanProp, actualModalSize)
  }
}
