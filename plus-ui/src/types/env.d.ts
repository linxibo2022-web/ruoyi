/**
 * Vite 项目类型定义 (vite-env.d.ts)
 *
 * 为 Vite 项目提供全局类型支持，包括环境变量类型检查和文件导入类型定义。
 *
 * 包含以下定义:
 * - Vue 组件导入: 支持在 TypeScript 中导入 .vue 单文件组件
 * - 环境变量类型: 为项目中使用的所有环境变量提供类型检查
 * - 安全配置: 加密相关环境变量的类型定义 (RSA公钥/私钥)
 * - 应用配置: 应用基础配置环境变量 (标题、应用ID、基础路径)
 * - 服务配置: 后端服务和相关服务地址配置
 * - 功能开关: 各项功能的开关配置 (WebSocket、SSE、加密)
 */
declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const Component: DefineComponent<{}, {}, any>
  export default Component
}

/**
 * 环境变量类型定义
 * 用于 Vite 项目的环境变量类型检查
 */
interface ImportMetaEnv {
  /** 页面标题 */
  VITE_APP_TITLE: string
  /** 应用ID (每个项目唯一，避免不同项目间键值冲突) */
  VITE_APP_ID: string
  /** 运行环境配置 (development/production/test) */
  VITE_APP_ENV: 'development' | 'production'
  /** API基础路径 */
  VITE_APP_BASE_API: string
  /** 后端服务端口 */
  VITE_APP_BASE_API_PORT: number
  /** 应用访问路径前缀，例如: /admin/ */
  VITE_APP_CONTEXT_PATH: string
  /** 是否开启前台首页 */
  VITE_ENABLE_FRONTEND: string
  /** 监控系统地址 */
  VITE_APP_MONITOR_ADMIN: string
  /** SnailJob 任务调度控制台地址 */
  VITE_APP_SNAILJOB_ADMIN: string
  /** 仓库地址 */
  VITE_APP_GIT_URL: string
  /** 文档地址 */
  VITE_APP_DOC_URL: string
  /** 是否在打包时开启压缩，支持 gzip 和 brotli (1: 开启, 0: 关闭) */
  VITE_BUILD_COMPRESS: number
  /** Vite开发服务器端口 */
  VITE_APP_PORT: number
  /** 接口加密功能开关 (如需关闭，后端也必须对应关闭) (true: 开启, false: 关闭) */
  VITE_APP_API_ENCRYPT: string
  /**
   * 接口加密传输 RSA 公钥
   * 与后端解密私钥对应，如需更换，前后端必须同时更换
   */
  VITE_APP_RSA_PUBLIC_KEY: string
  /**
   * 接口响应解密 RSA 私钥
   * 与后端加密公钥对应，如需更换，前后端必须同时更换
   */
  VITE_APP_RSA_PRIVATE_KEY: string
  /** WebSocket开关 (true: 启用WebSocket, false: 使用默认SSE推送) */
  VITE_APP_WEBSOCKET: string
  /** SSE(Server-Sent Events)开关 (true: 启用, false: 禁用) */
  VITE_APP_SSE: string
}

/**
 * Vite中的ImportMeta扩展
 * 用于在代码中访问环境变量
 */
interface ImportMeta {
  /** 环境变量对象 */
  readonly env: ImportMetaEnv
  // readonly glob: any; // 已注释，如需glob导入功能可取消注释
}
