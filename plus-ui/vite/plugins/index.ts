import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

import createUnoCss from './unocss'
import createAutoImport from './auto-imports'
import createComponents from './components'
import createIcons from './icons'
import createSvgIcon from './svg-icon' // 本地静态 SVG 合成 sprite
import createIconfontTypes from './iconfont-types' // iconfont 类型插件
import createCompression from './compression'
import createSetupExtend from './setup-extend'
import createOpenApiPlugin from './openapi' // OpenAPI 代码生成插件
import { createHmrControlPlugin } from './hmrControl' // HMR 控制插件（代码生成时暂停热更新）
import path from 'path'

// 导出 Vite 插件配置函数
// 根据环境和构建状态动态加载和配置插件
export default (viteEnv: any, isBuild = false): [] => {
  // 存储所有 Vite 插件的数组
  const vitePlugins: any = []

  // Vue 官方插件，提供 Vue 3 单文件组件支持
  vitePlugins.push(vue())

  // Vue 开发工具插件，增强开发调试体验
  vitePlugins.push(vueDevTools())

  // UnoCSS 原子化 CSS 引擎，提供快速、灵活的样式解决方案
  vitePlugins.push(createUnoCss())

  // 自动导入插件，减少手动 import 的工作
  // 自动导入 Vue、Vue Router、Pinia 等常用库的函数
  vitePlugins.push(createAutoImport(path))

  // 组件自动导入插件
  // 自动导入 Element Plus 组件和图标
  vitePlugins.push(createComponents(path))

  // 文件压缩插件，根据环境变量配置 Gzip 或 Brotli 压缩
  vitePlugins.push(createCompression(viteEnv))

  // 图标自动导入插件（iconify）
  // 支持从 Iconify 等图标集自动安装和使用图标
  vitePlugins.push(createIcons())

  // 本地静态 SVG 合成 sprite 插件
  // 扫描 src/assets/icons/svg/*.svg，构建时合并为雪碧图
  // 使用：<Icon code="dingtalk" />（文件名即 id）
  vitePlugins.push(createSvgIcon())

  // iconfont 图标类型生成插件
  // 自动扫描 iconfont 资源并生成 TypeScript 类型
  vitePlugins.push(createIconfontTypes())

  // setup 语法糖扩展插件
  // 提供额外的 setup 语法增强功能
  vitePlugins.push(createSetupExtend())

  // OpenAPI 代码生成插件
  const apiPort = viteEnv.VITE_APP_BASE_API_PORT || '5500'
  vitePlugins.push(
    createOpenApiPlugin({
      input: `http://127.0.0.1:${apiPort}/v3/api-docs/business`,
      output: 'src/api',
      mode: 'manual',
      enabled: true,
      ignore: {
        // 忽略 chat 模块 (最后一层目录)
        modules: ['chat', 'order', 'notify', 'ai'],
        // 忽略 system 模块的所有文件
        files: ['system*', 'tableDictTypes*'],
        // 忽略所有以 template 开头的接口
        functions: ['template*', 'import*', 'export*']
      }
    })
  )

  // HMR 控制插件（仅开发模式）
  // 用于代码生成时暂停文件监听，避免页面多次刷新
  if (!isBuild) {
    vitePlugins.push(createHmrControlPlugin())
  }

  // 返回配置好的插件数组
  return vitePlugins
}
