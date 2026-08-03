// 布局管理
import { reactive, computed, watch, readonly } from 'vue'
import { useWindowSize, useDark } from '@vueuse/core'
import { localCache } from '@/utils/cache'
import { SystemConfig, LanguageCode, MenuLayoutMode, type LayoutSetting } from '@/systemConfig'
import zhCN from 'element-plus/es/locale/lang/zh-cn'
import enUS from 'element-plus/es/locale/lang/en'
import type { RouteLocationNormalized } from 'vue-router'

/**
 * 统一布局状态管理
 */

// ==================== 类型定义 ====================

/**
 * 设备类型定义
 * @description 用于响应式布局的设备类型识别
 */
type DeviceType = 'pc' | 'mobile' | 'tablet'

/**
 * 区域语言类型定义
 * @description Element Plus 本地化配置类型
 */
type LocaleType = typeof zhCN

/**
 * 侧边栏开关状态常量
 */
const SIDEBAR_OPEN = '1'
const SIDEBAR_CLOSED = '0'

/**
 * 本地存储键名
 * @description 统一的配置存储键，简化存储管理
 */
const CACHE_KEY = 'layout-config'

/**
 * 侧边栏状态接口
 * @description 侧边栏的完整状态信息
 */
interface SidebarState {
  /** 是否打开侧边栏 */
  opened: boolean
  /** 是否禁用切换动画 */
  withoutAnimation: boolean
  /** 是否完全隐藏侧边栏（用于特殊页面） */
  hide: boolean
}

/**
 * 标签视图状态接口
 * @description 多标签页功能的状态管理
 */
interface TagsViewState {
  /** 已访问的视图列表 */
  visitedViews: RouteLocationNormalized[]
  /** 缓存的视图名称列表 */
  cachedViews: string[]
  /** iframe 视图列表 */
  iframeViews: RouteLocationNormalized[]
}

/**
 * 布局状态接口
 * @description 定义整个应用布局的完整状态结构
 */
interface LayoutState {
  /** 当前设备类型，影响布局响应式行为 */
  device: DeviceType
  /** 侧边栏状态配置 */
  sidebar: SidebarState
  /** 当前页面标题，用于动态标题显示 */
  title: string
  /** 是否显示设置面板 */
  showSettings: boolean
  /** 是否启用页面切换动画效果 */
  animationEnable: boolean
  /** 标签视图状态，管理多标签页功能 */
  tagsView: TagsViewState
  /** 布局配置，包含所有UI相关设置 */
  config: LayoutSetting
}

// ==================== 默认配置 ====================

/**
 * 语言映射配置
 * @description 将语言代码映射到 Element Plus 本地化配置
 */
const LANGUAGE_MAP: Record<LanguageCode, LocaleType> = {
  [LanguageCode.zh_CN]: zhCN,
  [LanguageCode.en_US]: enUS
}

/**
 * 默认布局配置
 * @description 系统启动时的默认配置，基于 SystemConfig.ui
 */
const DEFAULT_CONFIG: LayoutSetting = {
  // 标题配置
  title: SystemConfig.ui.title,

  // 布局相关配置
  topNav: SystemConfig.ui.topNav,
  menuLayout: SystemConfig.ui.menuLayout,
  tagsView: SystemConfig.ui.tagsView,
  fixedHeader: SystemConfig.ui.fixedHeader,
  sidebarLogo: SystemConfig.ui.sidebarLogo,
  dynamicTitle: SystemConfig.ui.dynamicTitle,
  layout: SystemConfig.ui.layout,

  // 外观主题配置
  theme: SystemConfig.ui.theme,
  sideTheme: SystemConfig.ui.sideTheme,
  dark: SystemConfig.ui.dark,

  // 功能配置
  showSettings: SystemConfig.ui.showSettings,
  animationEnable: SystemConfig.ui.animationEnable,

  // 用户偏好配置
  sidebarStatus: SystemConfig.ui.sidebarStatus,
  size: SystemConfig.ui.size,
  language: SystemConfig.ui.language,

  // 选择器配置
  showSelectValue: SystemConfig.ui.showSelectValue,

  // 水印配置
  watermark: SystemConfig.ui.watermark,
  watermarkContent: SystemConfig.ui.watermarkContent
}

// ==================== 工具函数 ====================

/**
 * 创建侧边栏初始状态
 * @param sidebarStatus 侧边栏状态字符串 ('1' 表示打开, '0' 表示关闭)
 * @returns 侧边栏状态对象
 */
const createSidebarState = (sidebarStatus?: string): SidebarState => ({
  opened: sidebarStatus ? !!+sidebarStatus : true,
  withoutAnimation: false,
  hide: false
})

/**
 * 创建标签视图初始状态
 * @returns 标签视图状态对象，包含三个空数组
 */
const createTagsViewState = (): TagsViewState => ({
  visitedViews: [], // 已访问的视图
  cachedViews: [], // 缓存的视图名称
  iframeViews: [] // iframe 视图
})

// ==================== 主要逻辑 ====================

/**
 * 全局布局状态实例
 * @description 确保单例模式，全局唯一的状态管理实例
 */
let layoutStateInstance: ReturnType<typeof createLayoutState> | null = null

/**
 * 创建布局状态实例
 * @description 创建并初始化整个布局管理系统的状态和方法
 * @returns 布局管理的完整接口
 */
function createLayoutState() {
  // 从本地缓存加载配置，如果不存在则使用默认配置
  const cachedConfig = localCache.getJSON<LayoutSetting>(CACHE_KEY) || { ...DEFAULT_CONFIG }

  // 创建响应式状态对象
  const state = reactive<LayoutState>({
    device: 'pc',
    sidebar: createSidebarState(cachedConfig.sidebarStatus),
    title: SystemConfig.ui.title,
    showSettings: SystemConfig.ui.showSettings,
    animationEnable: SystemConfig.ui.animationEnable,
    tagsView: createTagsViewState(),
    config: { ...cachedConfig }
  })

  // ==================== 菜单布局初始化同步 ====================
  // 确保 menuLayout 与 topNav/sidebar.hide 状态一致
  if (state.config.menuLayout === MenuLayoutMode.Horizontal) {
    state.config.topNav = true
    state.sidebar.hide = true // 水平布局隐藏侧边栏
  } else if (state.config.menuLayout === MenuLayoutMode.Mixed) {
    state.config.topNav = true
    state.sidebar.hide = false // 混合布局显示侧边栏
  } else if (state.config.menuLayout === MenuLayoutMode.DualColumn) {
    state.config.topNav = false
    state.sidebar.hide = false // 双列布局本质是侧边栏形态，显示侧边栏
  } else if (state.config.menuLayout === MenuLayoutMode.Vertical) {
    state.config.topNav = false
    state.sidebar.hide = false // 垂直布局显示侧边栏
  }

  // ==================== 计算属性工厂函数 ====================

  /**
   * 创建配置项的响应式计算属性
   * @template T 配置键的类型
   * @param key 配置项的键名
   * @returns 具有 getter/setter 的计算属性
   * @description 简化配置项计算属性的创建，支持读写操作
   */
  const createConfigGetter = <T extends keyof LayoutSetting>(key: T) =>
    computed<LayoutSetting[T]>({
      get: () => state.config[key],
      set: (value) => {
        if (value !== undefined) {
          state.config[key] = value
        }
      }
    })

  // 配置相关计算属性 - 用户偏好设置
  const size = createConfigGetter('size') // 组件尺寸
  const language = createConfigGetter('language') // 界面语言

  // 配置相关计算属性 - 主题外观
  const theme = createConfigGetter('theme') // 主题色
  const sideTheme = createConfigGetter('sideTheme') // 侧边栏主题
  const dark = createConfigGetter('dark') // 暗黑模式

  // 配置相关计算属性 - 布局设置
  const topNav = createConfigGetter('topNav') // 顶部导航
  const menuLayout = createConfigGetter('menuLayout') // 菜单布局模式
  const tagsView = createConfigGetter('tagsView') // 标签视图
  const fixedHeader = createConfigGetter('fixedHeader') // 固定头部
  const sidebarLogo = createConfigGetter('sidebarLogo') // 侧边栏Logo
  const dynamicTitle = createConfigGetter('dynamicTitle') // 动态标题

  // 配置相关计算属性 - 选择器设置
  const showSelectValue = createConfigGetter('showSelectValue') // 选择器显示值

  // 配置相关计算属性 - 水印设置
  const watermark = createConfigGetter('watermark') // 是否显示水印
  const watermarkContent = createConfigGetter('watermarkContent') // 水印内容

  // 语言本地化映射
  const locale = computed<LocaleType>(() => LANGUAGE_MAP[language.value])

  // ==================== 暗黑模式处理 ====================

  /**
   * 暗黑模式管理
   * @description 使用 VueUse 的 useDark，但禁用其内置存储，由我们自己管理
   */
  const isDark = useDark({
    storage: {
      getItem: () => null,
      setItem: () => {},
      removeItem: () => {}
    }
  })

  // 初始化时同步暗黑模式状态
  isDark.value = state.config.dark

  // 监听配置中的暗黑模式变化，同步到 VueUse
  watch(dark, (newValue) => {
    isDark.value = newValue
  })

  // 监听 VueUse 的暗黑模式变化，同步到配置
  watch(isDark, (newValue) => {
    dark.value = newValue
  })

  // ==================== 响应式设计处理 ====================

  const { width } = useWindowSize()
  const BREAKPOINT = 992 // 移动端断点，小于此宽度切换为移动端布局

  /**
   * 监听窗口宽度变化，自动切换设备类型和侧边栏状态
   * @description
   * - 宽度 < 992px: 切换为移动端，关闭侧边栏
   * - 宽度 >= 992px: 切换为PC端，打开侧边栏
   */
  watch(width, () => {
    const isMobile = width.value - 1 < BREAKPOINT

    // 如果当前是移动端状态，确保侧边栏关闭
    if (state.device === 'mobile') {
      closeSideBar()
    }

    if (isMobile) {
      toggleDevice('mobile')
      closeSideBar()
    } else {
      toggleDevice('pc')
      openSideBar()
    }
  })

  // ==================== 配置持久化 ====================

  /**
   * 监听配置变化并持久化到本地存储
   * @description 深度监听配置对象，自动保存到 localStorage
   * 同时同步更新侧边栏的打开状态
   */
  watch(
    () => state.config,
    (newConfig) => {
      localCache.setJSON(CACHE_KEY, newConfig)
      // 同步更新侧边栏状态
      state.sidebar.opened = newConfig.sidebarStatus ? !!+newConfig.sidebarStatus : true
    },
    { deep: true }
  )

  // ==================== 文档标题管理 ====================

  const appTitle = SystemConfig.app.title

  /**
   * 更新浏览器标签页标题
   * @description 根据动态标题设置决定显示格式
   * - 启用动态标题: "当前页面标题 - 应用名称"
   * - 禁用动态标题: 使用系统默认标题
   */
  const updateDocumentTitle = (): void => {
    document.title = dynamicTitle.value ? `${state.title} - ${appTitle}` : SystemConfig.ui.title
  }

  // 监听动态标题设置和当前标题变化
  watch([dynamicTitle, () => state.title], updateDocumentTitle)

  // 初始化时设置文档标题
  updateDocumentTitle()

  // ==================== 侧边栏管理方法 ====================

  /**
   * 更新侧边栏状态的通用方法
   * @param status 侧边栏状态 ('1' 打开, '0' 关闭)
   * @param withoutAnimation 是否禁用动画效果
   */
  const updateSidebarStatus = (status: string, withoutAnimation = false) => {
    state.config.sidebarStatus = status
    state.sidebar.withoutAnimation = withoutAnimation
    state.sidebar.opened = status === SIDEBAR_OPEN
  }

  /**
   * 切换侧边栏开关状态
   * @param withoutAnimation 是否禁用切换动画，默认 false
   */
  const toggleSideBar = (withoutAnimation = false): void => {
    if (state.sidebar.hide) return

    const newStatus = state.config.sidebarStatus === SIDEBAR_OPEN ? SIDEBAR_CLOSED : SIDEBAR_OPEN
    updateSidebarStatus(newStatus, withoutAnimation)
  }

  /**
   * 打开侧边栏
   * @param withoutAnimation 是否禁用动画效果，默认 false
   */
  const openSideBar = (withoutAnimation = false): void => {
    updateSidebarStatus(SIDEBAR_OPEN, withoutAnimation)
  }

  /**
   * 关闭侧边栏
   * @param withoutAnimation 是否禁用动画效果，默认 false
   */
  const closeSideBar = (withoutAnimation = false): void => {
    updateSidebarStatus(SIDEBAR_CLOSED, withoutAnimation)
  }

  /**
   * 设置侧边栏隐藏状态（用于某些特殊页面完全隐藏侧边栏）
   * @param status true 隐藏, false 显示
   */
  const toggleSideBarHide = (status: boolean): void => {
    state.sidebar.hide = status
  }

  // ==================== 设备和用户偏好设置方法 ====================

  /**
   * 切换设备类型
   * @param device 设备类型：'pc' | 'mobile' | 'tablet'
   */
  const toggleDevice = (device: DeviceType): void => {
    state.device = device
  }

  /**
   * 设置组件尺寸
   * @param newSize Element Plus 组件尺寸
   */
  const setSize = (newSize: ElSize): void => {
    size.value = newSize
  }

  /**
   * 切换界面语言
   * @param lang 语言代码
   */
  const changeLanguage = (lang: LanguageCode): void => {
    language.value = lang
  }

  /**
   * 切换暗黑模式
   * @param value true 启用暗黑模式, false 禁用
   */
  const toggleDark = (value: boolean): void => {
    dark.value = value
  }

  /**
   * 设置当前页面标题
   * @param value 页面标题，为空则不更新
   */
  const setTitle = (value: string): void => {
    if (!value) return
    state.title = value
    updateDocumentTitle()
  }

  /**
   * 重置页面标题为系统默认标题
   */
  const resetTitle = (): void => {
    state.title = SystemConfig.ui.title
    updateDocumentTitle()
  }

  /**
   * 保存布局设置
   * @param newConfig 新的布局配置，为空则重置为默认配置
   */
  const saveSettings = (newConfig?: Partial<LayoutSetting>): void => {
    if (newConfig) {
      Object.assign(state.config, newConfig)
    } else {
      state.config = { ...DEFAULT_CONFIG }
    }
  }

  /**
   * 重置所有配置为系统默认值
   */
  const resetConfig = (): void => {
    state.config = { ...DEFAULT_CONFIG }
  }

  // ==================== 标签视图管理方法 ====================

  /**
   * 标签视图管理器
   * @description 封装所有标签视图相关的操作方法
   */
  const tagsViewMethods = {
    /**
     * 获取已访问视图列表的副本
     * @returns 已访问视图数组的浅拷贝
     */
    getVisitedViews: () => [...state.tagsView.visitedViews],

    /**
     * 获取 iframe 视图列表的副本
     * @returns iframe 视图数组的浅拷贝
     */
    getIframeViews: () => [...state.tagsView.iframeViews],

    /**
     * 获取缓存视图名称列表的副本
     * @returns 缓存视图名称数组的浅拷贝
     */
    getCachedViews: () => [...state.tagsView.cachedViews],

    /**
     * 添加视图到已访问和缓存列表
     * @param view 路由视图对象
     */
    addView(view: RouteLocationNormalized) {
      this.addVisitedView(view)
      this.addCachedView(view)
    },

    /**
     * 添加视图到已访问列表
     * @param view 路由视图对象
     * @description 如果视图已存在则不重复添加
     */
    addVisitedView(view: RouteLocationNormalized) {
      if (state.tagsView.visitedViews.some((v) => v.path === view.path)) return

      state.tagsView.visitedViews.push({
        ...view,
        title: view.meta?.title || 'no-name'
      })
    },

    /**
     * 添加视图到缓存列表
     * @param view 路由视图对象
     * @description 只缓存有名称且未设置 noCache 的视图
     */
    addCachedView(view: RouteLocationNormalized) {
      const viewName = view.name as string
      if (!viewName || state.tagsView.cachedViews.includes(viewName)) return
      if (!view.meta?.noCache) {
        state.tagsView.cachedViews.push(viewName)
      }
    },

    /**
     * 添加 iframe 视图
     * @param view 路由视图对象
     */
    addIframeView(view: RouteLocationNormalized) {
      if (state.tagsView.iframeViews.some((v) => v.path === view.path)) return

      state.tagsView.iframeViews.push({
        ...view,
        title: view.meta?.title || 'no-name'
      })
    },

    /**
     * 删除指定视图
     * @param view 要删除的路由视图
     * @returns Promise 包含删除后的视图列表
     */
    async delView(view: RouteLocationNormalized) {
      await this.delVisitedView(view)
      if (!this.isDynamicRoute(view)) {
        await this.delCachedView(view)
      }
      return {
        visitedViews: this.getVisitedViews(),
        cachedViews: this.getCachedViews()
      }
    },

    /**
     * 从已访问列表中删除视图
     * @param view 要删除的路由视图
     * @returns Promise 包含更新后的已访问视图列表
     */
    async delVisitedView(view: RouteLocationNormalized) {
      const index = state.tagsView.visitedViews.findIndex((v) => v.path === view.path)
      if (index > -1) {
        state.tagsView.visitedViews.splice(index, 1)
      }
      return this.getVisitedViews()
    },

    /**
     * 从缓存列表中删除视图
     * @param view 要删除的路由视图，为空则清空所有缓存
     * @returns Promise 包含更新后的缓存视图列表
     */
    async delCachedView(view?: RouteLocationNormalized) {
      if (view) {
        const viewName = view.name as string
        const index = state.tagsView.cachedViews.indexOf(viewName)
        if (index > -1) {
          state.tagsView.cachedViews.splice(index, 1)
        }
      } else {
        state.tagsView.cachedViews = []
      }
      return this.getCachedViews()
    },

    /**
     * 删除 iframe 视图
     * @param view 要删除的路由视图
     * @returns Promise 包含更新后的 iframe 视图列表
     */
    async delIframeView(view: RouteLocationNormalized) {
      state.tagsView.iframeViews = state.tagsView.iframeViews.filter((item) => item.path !== view.path)
      return this.getIframeViews()
    },

    /**
     * 删除除指定视图外的其他所有视图
     * @param view 要保留的路由视图
     * @returns Promise 包含删除后的视图列表
     */
    async delOthersViews(view: RouteLocationNormalized) {
      await this.delOthersVisitedViews(view)
      await this.delOthersCachedViews(view)
      return {
        visitedViews: this.getVisitedViews(),
        cachedViews: this.getCachedViews()
      }
    },

    /**
     * 删除除指定视图外的其他已访问视图
     * @param view 要保留的路由视图
     * @returns Promise 包含更新后的已访问视图列表
     * @description 保留固定的视图（meta.affix=true）和指定视图
     */
    async delOthersVisitedViews(view: RouteLocationNormalized) {
      state.tagsView.visitedViews = state.tagsView.visitedViews.filter((v) => v.meta?.affix || v.path === view.path)
      return this.getVisitedViews()
    },

    /**
     * 删除除指定视图外的其他缓存视图
     * @param view 要保留的路由视图
     * @returns Promise 包含更新后的缓存视图列表
     */
    async delOthersCachedViews(view: RouteLocationNormalized) {
      const viewName = view.name as string
      const index = state.tagsView.cachedViews.indexOf(viewName)

      if (index > -1) {
        state.tagsView.cachedViews = state.tagsView.cachedViews.slice(index, index + 1)
      } else {
        state.tagsView.cachedViews = []
      }
      return this.getCachedViews()
    },

    /**
     * 删除所有视图
     * @returns Promise 包含删除后的视图列表
     * @description 保留固定的已访问视图，清空所有缓存
     */
    async delAllViews() {
      await this.delAllVisitedViews()
      await this.delAllCachedViews()
      return {
        visitedViews: this.getVisitedViews(),
        cachedViews: this.getCachedViews()
      }
    },

    /**
     * 删除所有已访问视图
     * @returns Promise 包含更新后的已访问视图列表
     * @description 只保留固定的视图（meta.affix=true）
     */
    async delAllVisitedViews() {
      state.tagsView.visitedViews = state.tagsView.visitedViews.filter((tag) => tag.meta?.affix)
      return this.getVisitedViews()
    },

    /**
     * 清空所有缓存视图
     * @returns Promise 包含空的缓存视图列表
     */
    async delAllCachedViews() {
      state.tagsView.cachedViews = []
      return this.getCachedViews()
    },

    /**
     * 删除指定视图右侧的所有标签
     * @param view 基准视图，该视图右侧的标签将被删除
     * @returns Promise 包含更新后的已访问视图列表
     * @description 保留指定视图及其左侧的视图，删除右侧的视图和对应缓存
     */
    async delRightTags(view: RouteLocationNormalized) {
      const index = state.tagsView.visitedViews.findIndex((v) => v.path === view.path)
      if (index === -1) return this.getVisitedViews()

      state.tagsView.visitedViews = state.tagsView.visitedViews.filter((item, idx) => {
        if (idx <= index || item.meta?.affix) return true

        const cacheIndex = state.tagsView.cachedViews.indexOf(item.name as string)
        if (cacheIndex > -1) {
          state.tagsView.cachedViews.splice(cacheIndex, 1)
        }
        return false
      })

      return this.getVisitedViews()
    },

    /**
     * 删除指定视图左侧的所有标签
     * @param view 基准视图，该视图左侧的标签将被删除
     * @returns Promise 包含更新后的已访问视图列表
     * @description 保留指定视图及其右侧的视图，删除左侧的视图和对应缓存
     */
    async delLeftTags(view: RouteLocationNormalized) {
      const index = state.tagsView.visitedViews.findIndex((v) => v.path === view.path)
      if (index === -1) return this.getVisitedViews()

      state.tagsView.visitedViews = state.tagsView.visitedViews.filter((item, idx) => {
        if (idx >= index || item.meta?.affix) return true

        const cacheIndex = state.tagsView.cachedViews.indexOf(item.name as string)
        if (cacheIndex > -1) {
          state.tagsView.cachedViews.splice(cacheIndex, 1)
        }
        return false
      })

      return this.getVisitedViews()
    },

    /**
     * 更新已访问视图的信息
     * @param view 包含新信息的路由视图
     * @description 根据路径查找并更新对应的已访问视图
     */
    updateVisitedView(view: RouteLocationNormalized) {
      const target = state.tagsView.visitedViews.find((v) => v.path === view.path)
      if (target) {
        Object.assign(target, view)
      }
    },

    /**
     * 判断是否为动态路由
     * @param view 路由视图对象
     * @returns true 如果是动态路由，false 否则
     * @description 检查路由路径是否包含动态参数（如 :id）
     */
    isDynamicRoute(view: RouteLocationNormalized): boolean {
      return view.matched.some((m) => m.path.includes(':'))
    }
  }

  // ==================== 返回公共接口 ====================

  return {
    // ==================== 只读状态 ====================

    /**
     * 只读的完整状态对象
     * @description 防止外部直接修改状态，确保数据流的单向性
     */
    state: readonly(state),

    // ==================== 基础状态计算属性 ====================

    /** 当前设备类型 */
    device: computed(() => state.device),
    /** 侧边栏状态 */
    sidebar: computed(() => state.sidebar),
    /** 当前页面标题 */
    title: computed(() => state.title),
    /** 是否显示设置面板 */
    showSettings: computed(() => state.showSettings),
    /** 是否启用动画效果 */
    animationEnable: computed(() => state.animationEnable),

    // ==================== 用户偏好配置 ====================

    /** 界面语言设置 */
    language,
    /** Element Plus 本地化配置 */
    locale,
    /** 组件尺寸设置 */
    size,

    // ==================== 主题外观配置 ====================

    /** 主题色配置 */
    theme,
    /** 侧边栏主题配置 */
    sideTheme,
    /** 暗黑模式配置 */
    dark,

    // ==================== 布局功能配置 ====================

    /** 顶部导航栏显示配置 */
    topNav,
    /** 菜单布局模式配置 */
    menuLayout,
    /** 标签视图显示配置 */
    tagsView,
    /** 固定头部配置 */
    fixedHeader,
    /** 侧边栏Logo显示配置 */
    sidebarLogo,
    /** 动态标题配置 */
    dynamicTitle,

    // ==================== 选择器配置 ====================

    /** 选择器显示值配置 */
    showSelectValue,

    // ==================== 水印配置 ====================

    /** 是否显示水印 */
    watermark,
    /** 水印内容 */
    watermarkContent,

    // ==================== 标签视图状态 ====================

    /** 已访问的视图列表 */
    visitedViews: computed(() => state.tagsView.visitedViews),
    /** 缓存的视图名称列表 */
    cachedViews: computed(() => state.tagsView.cachedViews),
    /** iframe 视图列表 */
    iframeViews: computed(() => state.tagsView.iframeViews),

    // ==================== 侧边栏操作方法 ====================

    /** 切换侧边栏开关状态 */
    toggleSideBar,
    /** 打开侧边栏 */
    openSideBar,
    /** 关闭侧边栏 */
    closeSideBar,
    /** 设置侧边栏隐藏状态 */
    toggleSideBarHide,

    // ==================== 设备和偏好设置方法 ====================

    /** 切换设备类型 */
    toggleDevice,
    /** 设置组件尺寸 */
    setSize,
    /** 切换界面语言 */
    changeLanguage,
    /** 切换暗黑模式 */
    toggleDark,
    /** 设置页面标题 */
    setTitle,
    /** 重置页面标题 */
    resetTitle,
    /** 保存布局设置 */
    saveSettings,
    /** 重置所有配置 */
    resetConfig,

    // ==================== 标签视图操作方法 ====================

    /** 展开标签视图管理器的所有方法 */
    ...tagsViewMethods
  }
}

/**
 * 统一布局状态管理 Hook
 *
 * @description
 * 这是一个基于 Vue 3 Composition API 的布局状态管理系统，提供了完整的布局、主题、
 * 侧边栏、标签视图等功能的状态管理和操作方法。
 *
 * @features
 * - **统一状态管理**: 使用单一配置对象管理所有布局相关状态
 * - **响应式设计**: 自动适配不同设备尺寸，动态调整布局
 * - **主题系统**: 支持亮色/暗色主题切换，主题色自定义
 * - **多语言支持**: 集成 Element Plus 国际化
 * - **标签视图**: 完整的多标签页管理功能
 * - **持久化存储**: 自动保存用户配置到本地存储
 * - **类型安全**: 完整的 TypeScript 类型支持
 * - **单例模式**: 确保全局状态的唯一性
 *
 * @example
 * ```typescript
 * // 基础使用
 * const layout = useLayout()
 *
 * // 侧边栏操作
 * layout.toggleSideBar()              // 切换侧边栏
 * layout.openSideBar(true)            // 打开侧边栏（无动画）
 * layout.closeSideBar()               // 关闭侧边栏
 *
 * // 主题操作
 * layout.toggleDark(true)             // 启用暗黑模式
 * layout.theme.value = '#ff6b6b'      // 设置主题色
 * layout.changeLanguage('en_US')      // 切换英文
 *
 * // 标签视图操作
 * layout.addView(route)               // 添加标签
 * layout.delView(route)               // 删除标签
 * layout.delOthersViews(route)        // 删除其他标签
 *
 * // 页面标题管理
 * layout.setTitle('用户管理')         // 设置页面标题
 * layout.resetTitle()                 // 重置为默认标题
 *
 * // 响应式状态访问
 * console.log(layout.device.value)    // 当前设备类型
 * console.log(layout.sidebar.value)   // 侧边栏状态
 * ```
 *
 * @author 抓蛙师
 */
export const useLayout = () => {
  if (!layoutStateInstance) {
    layoutStateInstance = createLayoutState()
  }
  return layoutStateInstance
}
