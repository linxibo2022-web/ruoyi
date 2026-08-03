/**
 * UnoCSS 配置文件
 * @description 定义项目的原子化CSS配置
 */

// 导入 UnoCSS 的核心功能和预设
import {
  defineConfig,
  presetAttributify, // 属性化模式预设
  presetIcons, // 图标支持预设
  presetTypography, // 排版预设
  presetUno, // 默认工具类预设
  presetWebFonts, // Web字体预设
  transformerDirectives, // @apply等指令转换器
  transformerVariantGroup // 变体组转换器
} from 'unocss'

// 导入预设图标数组以添加到安全列表
import { ICONIFY_ICONS } from './src/types/icons.d'

// 导出UnoCSS配置
export default defineConfig({
  /**
   * 快捷方式定义
   * 将常用的样式组合定义为简单的类名，提高开发效率
   *
   * 使用示例:
   * <div class="panel-title">标题文本</div>
   * <div class="flex-center">居中内容</div>
   * <button class="btn-primary">提交</button>
   */
  shortcuts: {
    // 面板标题样式
    'panel-title':
      'pb-[5px] font-sans leading-[1.1] font-medium text-base text-[#6379bb] border-b border-b-solid border-[var(--el-border-color-light)] mb-5 mt-0',

    // 布局快捷方式
    'flex-center': 'flex items-center justify-center', // 居中对齐的弹性布局
    'flex-between': 'flex items-center justify-between', // 两端对齐的弹性布局
    'absolute-center': 'absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2', // 绝对定位居中

    // 容器快捷方式
    'card': 'bg-white dark:bg-dark-800 rounded shadow p-4', // 卡片容器

    // 标签快捷方式
    'tag': 'inline-block px-2 py-1 text-xs rounded' // 标签基础样式
  },

  /**
   * 主题配置
   * 定义颜色、间距、字体等主题变量，支持亮色/暗色模式
   *
   * 使用示例:
   * <div class="bg-primary text-white">主题色背景</div>
   * <div class="text-text-secondary">次要文本</div>
   * <div class="border-border p-sidebar">带主题边框和内边距</div>
   */
  theme: {
    // 颜色配置：定义语义化的颜色变量，与CSS变量关联
    colors: {
      // 状态颜色
      'primary': 'var(--el-color-primary)', // 主色调，用于品牌和关键元素
      'primary_dark': 'var(--el-color-primary-light-5)', // 主色调深色变体，用于悬停状态
      'success': 'var(--color-success)', // 成功状态颜色
      'warning': 'var(--color-warning)', // 警告状态颜色
      'danger': 'var(--color-danger)', // 危险/错误状态颜色
      'info': 'var(--color-info)', // 信息/提示状态颜色

      // 文本颜色
      'text-base': 'var(--text-color)', // 基础文本颜色
      'text-secondary': 'var(--text-color-secondary)', // 次要文本颜色
      'text-muted': 'var(--text-muted)', // 弱化文本颜色
      'heading': 'var(--heading-color)', // 标题文本颜色

      // 边框颜色
      'border': 'var(--border-color)', // 标准边框颜色
      'border-light': 'var(--border-color-light)', // 浅色边框
      'border-lighter': 'var(--border-color-lighter)', // 更浅的边框颜色

      // 背景颜色
      'bg-base': 'var(--bg-color)', // 基础背景颜色
      'bg-page': 'var(--bg-color-page)', // 页面背景颜色
      'bg-overlay': 'var(--bg-color-overlay)', // 覆盖层背景颜色

      // 菜单颜色
      'menu-bg': 'var(--menu-bg)', // 菜单背景色
      'menu-text': 'var(--menu-color)', // 菜单文本颜色
      'menu-active': 'var(--menu-active-text)', // 菜单激活项文本颜色
      'menu-hover': 'var(--menu-hover)', // 菜单悬停背景色

      // 子菜单颜色
      'submenu-bg': 'var(--submenu-bg)', // 子菜单背景色
      'submenu-active': 'var(--submenu-active-text)', // 子菜单激活项文本颜色
      'submenu-hover': 'var(--submenu-hover)' // 子菜单悬停背景色
    },

    // 间距变量：定义布局尺寸，与CSS变量关联
    spacing: {
      'sidebar': 'var(--sidebar-width)', // 侧边栏宽度
      'header': 'var(--header-height)', // 头部高度
      'tags-view': 'var(--tags-view-height)' // 标签视图高度
    },

    // 字体配置：定义文本样式
    fontFamily: {
      'base': 'var(--font-family-base)' // 基础字体族
    },

    // 阴影配置：定义元素阴影效果
    boxShadow: {
      'base': 'var(--shadow-base)', // 基础阴影效果
      'light': 'var(--shadow-light)' // 轻微阴影效果
    },

    // 边框圆角：定义元素圆角大小
    borderRadius: {
      'base': 'var(--border-radius-base)', // 基础圆角大小
      'small': 'var(--border-radius-small)' // 小尺寸圆角
    }
  },

  /**
   * 安全列表 - 确保所有图标类名都被包含，即使未在代码中显式使用
   */
  safelist: [
    // 添加所有预设图标的类名到安全列表
    ...ICONIFY_ICONS.map((icon) => icon.value)
  ],

  /**
   * 自定义规则
   * 定义无法用预设满足的特殊样式规则
   *
   * 使用示例:
   * <div class="sidebar-width">侧边栏宽度</div>
   * <div class="scrollbar-y h-80">可滚动容器</div>
   * <div class="text-ellipsis w-40">这是一段很长的文本将会被省略...</div>
   */
  rules: [
    // 布局相关规则
    ['sidebar-width', { 'width': 'var(--sidebar-width)' }], // 侧边栏宽度
    ['header-height', { 'height': 'var(--header-height)' }], // 头部高度

    // 滚动条相关规则
    ['scrollbar', { 'overflow': 'auto' }], // 启用双向滚动条
    ['scrollbar-y', { 'overflow-y': 'auto', 'overflow-x': 'hidden' }], // 仅垂直滚动条
    ['scrollbar-x', { 'overflow-x': 'auto', 'overflow-y': 'hidden' }], // 仅水平滚动条

    // 文本处理规则
    ['text-ellipsis', { 'white-space': 'nowrap', 'overflow': 'hidden', 'text-overflow': 'ellipsis' }], // 单行文本省略
    [
      'line-clamp-2',
      {
        // 多行文本省略（2行）
        'overflow': 'hidden',
        'display': '-webkit-box',
        '-webkit-line-clamp': '2',
        '-webkit-box-orient': 'vertical'
      }
    ],

    // 定位规则
    ['relative-full', { 'position': 'relative', 'width': '100%', 'height': '100%' }] // 相对定位且填满容器
  ],

  /**
   * 预设配置
   * 激活UnoCSS的各种预设功能
   *
   * 使用示例:
   * <div class="text-xl text-blue-500 hover:text-blue-700">预设样式</div> (presetUno)
   * <div text="xl blue-500 hover:blue-700">属性化模式</div> (presetAttributify)
   * <div class="i-carbon-home text-2xl"></div> (presetIcons - 显示home图标)
   * <article class="prose prose-sm">排版内容...</article> (presetTypography)
   */
  presets: [
    // 默认预设：提供大多数常用的原子化CSS类
    presetUno(),

    // 属性化模式预设：允许将类转换为属性，如<div m="2" text="sm blue">
    presetAttributify(),

    // 图标预设：支持各种图标集成，如<div class="i-carbon-home">
    presetIcons({}),

    // 排版预设：提供丰富的文本排版相关样式
    presetTypography(),

    // Web字体预设：支持在线字体的便捷使用
    presetWebFonts({
      // 字体配置
      fonts: {
        // 可在此处添加自定义网络字体
      }
    })
  ],

  /**
   * 转换器配置
   * 扩展UnoCSS的语法功能
   *
   * 使用示例:
   * <div class="@apply text-center bg-gray-100 dark:bg-gray-800">使用@apply指令</div> (transformerDirectives)
   * <div class="hover:(bg-blue-500 text-white font-bold)">使用变体组</div> (transformerVariantGroup)
   */
  transformers: [
    // 指令转换器：支持@apply、@screen等指令
    transformerDirectives(),

    // 变体组转换器：简化多变体编写，如hover:(bg-blue text-white)
    transformerVariantGroup()
  ]
})
