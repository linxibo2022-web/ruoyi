<!--
一个让 SVG 图片跟随主题色变化的组件

使用方式:
<AThemeSvg :src="svgUrl" size="100%" />

注意:
- 只对特定的 SVG 图片有效,会将 SVG 中的固定颜色替换为主题色变量
- 颜色映射规则见 COLOR_MAPPINGS 配置
- 图片来源: https://iconpark.oceanengine.com/illustrations/13
-->
<template>
  <div class="atheme-svg" :style="sizeStyle">
    <div v-if="svgContent" class="svg-container" v-html="svgContent"></div>
    <div v-else class="loading-placeholder">Loading SVG...</div>
  </div>
</template>

<script setup lang="ts" name="AThemeSvg">
import { useLayout } from '@/composables/useLayout'

/**
 * AThemeSvg 组件属性接口定义
 */
interface AThemeSvgProps {
  /** SVG 文件路径(本地或远程) */
  src?: string

  /** SVG 尺寸,可以是具体数值或百分比 */
  size?: string | number

  /** 主题色(不传则使用全局主题色) */
  themeColor?: string
}

/**
 * 设置组件默认属性值
 */
const props = withDefaults(defineProps<AThemeSvgProps>(), {
  src: '',
  size: 500,
  themeColor: 'var(--el-color-primary)'
})

// ==================== 响应式数据 ====================

/** SVG 内容 */
const svgContent = ref('')

/** 布局管理器(获取全局主题色) */
const layout = useLayout()

// ==================== 计算属性 ====================

/**
 * 计算尺寸样式
 */
const sizeStyle = computed(() => {
  const sizeValue = typeof props.size === 'number' ? `${props.size}px` : props.size
  return {
    width: sizeValue,
    height: sizeValue
  }
})

// ==================== 颜色映射配置 ====================

/**
 * SVG 固定颜色到主题色变量的映射表
 * @description 会将 SVG 中的这些固定颜色替换为对应的主题色变量
 */
const COLOR_MAPPINGS = {
  // 浅蓝色 -> 主题色 light-6
  '#C7DEFF': 'var(--el-color-primary-light-6)',

  // 深蓝色 -> 主题色 dark-2
  '#071F4D': 'var(--el-color-primary-dark-2)',

  // 青色 -> 主题色 light-1
  '#00E4E5': 'var(--el-color-primary-light-1)',

  // 蓝色 -> 主题色(基础色)
  '#006EFF': 'var(--el-color-primary)',

  // 白色 -> 背景色
  '#fff': 'var(--bg-level-1)',
  '#ffffff': 'var(--bg-level-1)',

  // 淡蓝色 -> 主题色 light-7
  '#DEEBFC': 'var(--el-color-primary-light-7)'
} as const

// ==================== 核心逻辑 ====================

/**
 * 将主题色应用到 SVG 内容
 * @param content 原始 SVG 内容
 * @returns 处理后的 SVG 内容
 * @description 遍历颜色映射表,将 SVG 中的固定颜色替换为主题色变量
 */
const applyThemeToSvg = (content: string): string => {
  return Object.entries(COLOR_MAPPINGS).reduce((processedContent, [originalColor, themeColor]) => {
    // 创建正则表达式,匹配 fill 和 stroke 属性中的颜色
    const fillRegex = new RegExp(`fill="${originalColor}"`, 'gi')
    const strokeRegex = new RegExp(`stroke="${originalColor}"`, 'gi')

    // 替换颜色
    return processedContent.replace(fillRegex, `fill="${themeColor}"`).replace(strokeRegex, `stroke="${themeColor}"`)
  }, content)
}

/**
 * 加载 SVG 文件内容
 * @description 通过 fetch 获取 SVG 文件,应用主题色后更新到 svgContent
 */
const loadSvgContent = async () => {
  if (!props.src) {
    svgContent.value = ''
    return
  }

  try {
    const response = await fetch(props.src)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const content = await response.text()
    svgContent.value = applyThemeToSvg(content)
  } catch (error) {
    console.error('[AThemeSvg] Failed to load SVG:', error)
    svgContent.value = ''
  }
}

// ==================== 生命周期 ====================

/**
 * 监听 src 和主题色变化,自动重新加载 SVG
 * @description 当 SVG 路径或主题色变化时,重新加载并应用新主题色
 */
watchEffect(() => {
  // 访问 layout.theme 以建立响应式依赖
  // 当主题色变化时,会自动触发重新加载
  const _theme = layout.theme.value
  loadSvgContent()
})
</script>

<style scoped lang="scss">
/**
 * 主容器样式
 */
.atheme-svg {
  display: block;
  width: 100%;
  height: auto;

  /**
   * 加载占位符
   */
  .loading-placeholder {
    width: 100%;
    padding: 40px;
    text-align: center;
    color: #999;
    font-size: 14px;
  }

  /**
   * SVG 容器样式
   */
  .svg-container {
    width: 100%;
    height: auto;

    /**
     * 深度选择器,确保内部 SVG 元素填充整个容器
     */
    :deep(svg) {
      display: block;
      width: 100%;
      height: auto;
    }
  }
}
</style>
