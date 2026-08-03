/**
 * 系统全局配置
 * @description 集中管理所有应用配置，提供类型安全访问
 * @module @/systemConfig
 */
/**
 * 系统支持的语言枚举
 * @description 定义应用程序支持的所有语言选项
 */
export enum LanguageCode {
  /**
   * 中文(简体)
   */
  zh_CN = 'zh_CN',

  /**
   * 英文(美国)
   */
  en_US = 'en_US'
}

/** 侧边栏主题枚举 */
export enum SideTheme {
  /** 深色主题 */
  Dark = 'theme-dark',
  /** 浅色主题 */
  Light = 'theme-light'
}

/** 菜单布局模式枚举 */
export enum MenuLayoutMode {
  /** 垂直布局（左侧边栏） */
  Vertical = 'vertical',
  /** 混合布局（顶部+左侧） */
  Mixed = 'mixed',
  /** 水平布局（纯顶部） */
  Horizontal = 'horizontal',
  /** 双列布局（左图标列 + 右子菜单列） */
  DualColumn = 'dual-column'
}

/**
 * 预定义主题色配置
 * @description 系统提供的预设主题色列表，用于主题色选择器
 */
export const PREDEFINED_THEME_COLORS = [
  '#5D87FF', // 默认蓝色
  '#B48DF3', // 紫色
  '#1D84FF', // 深蓝
  '#60C041', // 绿色
  '#38C0FC', // 青色
  '#F9901F', // 橙色
  '#FF80C8' // 粉色
] as const

/**
 * 系统全局配置
 */
export const SystemConfig: SystemConfigType = {
  /**
   * 应用基础信息
   */
  app: {
    /** 应用唯一ID */
    id: import.meta.env.VITE_APP_ID || 'erp_sys',
    /** 应用名称/标题 */
    title: import.meta.env.VITE_APP_TITLE || 'ERP系统',
    /** 应用运行环境 */
    env: import.meta.env.VITE_APP_ENV || 'development',
    /** 应用访问路径前缀 */
    contextPath: import.meta.env.VITE_APP_CONTEXT_PATH || '/',
    /** 是否启用前台首页 */
    enableFrontend: import.meta.env.VITE_ENABLE_FRONTEND === 'true'
  },

  /**
   * API配置
   */
  api: {
    /** API基础路径 */
    baseUrl: import.meta.env.VITE_APP_BASE_API || '/dev-api',
    /** 后端服务端口 */
    port: Number(import.meta.env.VITE_APP_BASE_API_PORT || 5500),
    /** 请求超时时间(毫秒) */
    timeout: 10000
  },

  /**
   * 安全配置
   */
  security: {
    /** 接口加密功能开关 */
    apiEncrypt: import.meta.env.VITE_APP_API_ENCRYPT === 'true',
    /** RSA公钥 - 用于加密传输 */
    rsaPublicKey: import.meta.env.VITE_APP_RSA_PUBLIC_KEY || '',
    /** RSA私钥 - 用于解密响应 */
    rsaPrivateKey: import.meta.env.VITE_APP_RSA_PRIVATE_KEY || ''
  },

  /**
   * 外部服务地址
   */
  services: {
    /** 监控系统地址 */
    monitor: import.meta.env.VITE_APP_MONITOR_ADMIN || '',
    /** SnailJob控制台地址 */
    snailJob: import.meta.env.VITE_APP_SNAILJOB_ADMIN || '',
    /** 仓库地址 */
    gitUrl: import.meta.env.VITE_APP_GIT_URL || '',
    /** 文档地址 */
    docUrl: import.meta.env.VITE_APP_DOC_URL || ''
  },

  /**
   * UI及布局设置
   */
  ui: {
    /** 网页标题 */
    title: import.meta.env.VITE_APP_TITLE || 'ERP系统',
    /** 主题色 */
    theme: '#5d87ff',
    /** 侧边栏主题 */
    sideTheme: SideTheme.Dark,
    /** 是否显示设置面板 */
    showSettings: true,
    /** 是否显示顶部导航 */
    topNav: false,
    /** 菜单布局模式 */
    menuLayout: MenuLayoutMode.Vertical,
    /** 是否显示多标签导航 */
    tagsView: true,
    /** 是否固定头部 */
    fixedHeader: true,
    /** 是否显示侧边栏Logo */
    sidebarLogo: true,
    /** 是否显示动态标题 */
    dynamicTitle: true,
    /** 是否启用动画效果 */
    animationEnable: false,
    /** 是否启用暗黑模式 */
    dark: false,
    /** 导航栏布局 */
    layout: '',
    /** 侧边栏状态 */
    sidebarStatus: '1',
    /** 布局大小 */
    size: 'default' as ElSize,
    /** 语言设置 */
    language: LanguageCode.zh_CN,
    /** 是否在选择器中显示选项值 (undefined 表示根据角色自动判断) */
    showSelectValue: undefined,
    /** 是否显示水印 */
    watermark: false,
    /** 水印内容 (空字符串表示使用当前登录用户名) */
    watermarkContent: ''
  }
}

/**
 * 系统配置总接口
 * @description 集成所有配置模块的顶层接口
 */
export interface SystemConfigType {
  /** 应用基础信息 */
  app: BaseConfig

  /** API相关配置 */
  api: ApiConfig

  /** 安全配置 */
  security: SecurityConfig

  /** 外部服务地址 */
  services: ServicesConfig

  /** UI及布局设置 */
  ui: LayoutSetting
}

/**
 * 应用基础配置接口
 * @description 应用程序基本信息配置
 */
interface BaseConfig {
  /** 应用唯一ID，用于本地存储前缀等 */
  id: string
  /** 应用名称/标题 */
  title: string
  /** 应用运行环境 */
  env: 'development' | 'production'
  /** 应用访问路径前缀 */
  contextPath: string
  /** 是否启用前台首页 */
  enableFrontend: boolean
}

/**
 * API配置接口
 * @description API请求相关配置
 */
export interface ApiConfig {
  /** API基础路径 */
  baseUrl: string
  /** 后端服务端口 */
  port: number
  /** 请求超时时间(毫秒) */
  timeout: number
}

/**
 * 安全配置接口
 * @description 应用安全相关配置
 */
export interface SecurityConfig {
  /** 接口加密功能开关 */
  apiEncrypt: boolean
  /** RSA公钥 - 用于加密传输 */
  rsaPublicKey: string
  /** RSA私钥 - 用于解密响应 */
  rsaPrivateKey: string
}

/**
 * 外部服务配置接口
 * @description 第三方服务和外部资源地址配置
 */
export interface ServicesConfig {
  /** 监控系统地址 */
  monitor: string
  /** SnailJob控制台地址 */
  snailJob: string
  /** 仓库地址 */
  gitUrl: string
  /** 文档地址 */
  docUrl: string
}

/**
 * 布局设置接口
 * @description 定义系统UI布局的所有配置项
 */
export interface LayoutSetting {
  /** 网页标题 */
  title: string
  /** 主题色 */
  theme: string
  /** 侧边栏主题 */
  sideTheme: SideTheme
  /** 是否显示设置面板 */
  showSettings: boolean
  /** 是否显示顶部导航 */
  topNav: boolean
  /** 菜单布局模式 */
  menuLayout: MenuLayoutMode
  /** 是否显示多标签导航 */
  tagsView: boolean
  /** 是否固定头部 */
  fixedHeader: boolean
  /** 是否显示侧边栏Logo */
  sidebarLogo: boolean
  /** 是否显示动态标题 */
  dynamicTitle: boolean
  /** 是否启用动画效果 */
  animationEnable: boolean
  /** 是否启用暗黑模式 */
  dark: boolean
  /** 导航栏布局 */
  layout: string
  /** 侧边栏状态 ('1' 打开, '0' 关闭) */
  sidebarStatus: string
  /** 布局大小 */
  size: ElSize
  /** 语言设置 */
  language: LanguageCode
  /** 是否在选择器中显示选项值 */
  showSelectValue?: boolean
  /** 是否显示水印 */
  watermark?: boolean
  /** 水印内容 */
  watermarkContent?: string
}
