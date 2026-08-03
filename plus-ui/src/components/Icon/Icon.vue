<!--
Icon 图标组件 - 支持三种图标源，统一使用 <Icon /> 组件渲染

================ 图标源优先级 ================
同一个 code 按 iconfont > iconify > 本地 SVG sprite 的顺序匹配。
规避方式：iconify 可通过 value 直写 i- 前缀强制走 iconify。

================ 三种图标源对比 ================
| 类型          | 来源                               | 用法                              | 变色    | 新增成本                       |
| ------------- | ---------------------------------- | --------------------------------- | ------- | ------------------------------ |
| iconfont      | src/assets/icons/system/ 字体文件  | <Icon code="user" />              | 支持    | 去 iconfont.cn 项目更新字体    |
| iconify       | @iconify/json 或 preset.json       | <Icon value="i-mdi:github" />     | 支持    | 直接写 i-xxx 即可              |
| svg sprite    | src/assets/icons/svg/*.svg         | <Icon code="dingtalk" />          | 支持 ⚠️ | 放 .svg 文件即可               |

⚠️ svg sprite 变色要求：SVG 源文件内需使用 fill="currentColor" 或 stroke="currentColor"，
    组件会自动把外部 color 属性映射到 currentColor。

================ 常规用法 ================
<Icon code="battery" />
<Icon code="user" size="lg" color="#409EFF" />
<Icon value="i-carbon:user-avatar" size="24px" />
<Icon code="search" size="20px" color="red" />

================ 本地 SVG sprite ================
适合品牌 logo / 单色自定义图标（iconfont 找不到的）。
操作：把 xxx.svg 放到 src/assets/icons/svg/ 根目录，文件名即 code。

<Icon code="dingtalk" color="#0089FF" size="lg" />  // src/assets/icons/svg/dingtalk.svg
<Icon code="maxkey" size="32px" />                   // src/assets/icons/svg/maxkey.svg
<Icon code="topiam" color="currentColor" />          // 继承父元素颜色

================ 尺寸（size） ================
预设：xs(12px) / sm(16px) / md(20px) / lg(24px) / xl(32px) / 2xl(40px)
自定义：数字（如 48，按 px）或字符串（如 "1.3em" / "20px" / "2rem"）

<Icon code="search" size="xs" />
<Icon code="search" size="lg" />
<Icon code="search" :size="48" />
<Icon code="search" size="1.5em" />

================ 动画（animate） ================
shake / rotate180 / moveUp / expand / shrink / breathing

<Icon code="search" animate="shake" />
<Icon code="refresh" animate="rotate180" />
<Icon code="language" animate="moveUp" />
<Icon code="fullscreen" animate="expand" />
<Icon code="fullscreen-exit" animate="shrink" />
<Icon code="notification" animate="breathing" />
-->
<template>
  <!-- iconfont 图标使用 i 标签 -->
  <div
    v-if="computedIconClass"
    :class="['inline-flex items-center justify-center', computedIconClass, sizeClass, animateClass]"
    :style="inlineStyles"
  ></div>

  <!-- iconify 图标使用 div 标签 -->
  <div
    v-else-if="computedValue"
    :class="['inline-flex items-center justify-center fill-current', computedValue, sizeClass, animateClass]"
    :style="inlineStyles"
  ></div>

  <!-- 本地 SVG sprite（来自 src/assets/icons/svg/*.svg） -->
  <!-- SVG 内需使用 fill="currentColor" 才能响应 color 属性 -->
  <svg
    v-else-if="computedSvgHref"
    :class="['inline-block icon-svg-sprite', sizeClass, animateClass]"
    :style="inlineStyles"
    aria-hidden="true"
  >
    <use :xlink:href="computedSvgHref" fill="currentColor" />
  </svg>
</template>

<script setup lang="ts" name="Icon">
import { computed } from 'vue'
import { isIconfontIcon, isIconifyIcon, isSvgIcon, getIconifyValue } from '@/types/icons.d'

/**
 * 尺寸预设类型定义
 */
type SizePreset = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl'

/**
 * 图标动画类型定义
 */
export type AnimateType = 'shake' | 'rotate180' | 'moveUp' | 'expand' | 'shrink' | 'breathing'

/**
 * Icon 组件属性接口
 */
interface IconProps {
  /** 图标值，可以是完整的图标类名（用于 iconify 或自定义类） */
  value?: string
  /** 图标代码，用于标识图标，优先从 iconfont 查找，然后从 iconify 查找 */
  code?: IconCode
  /** 图标大小，支持预设尺寸、数字(px)或字符串 */
  size?: SizePreset | string | number
  /** 图标颜色，支持任何CSS颜色值 */
  color?: string
  /** 图标动画效果类型 */
  animate?: AnimateType
}

/**
 * 组件属性定义，设置默认值
 */
const props = withDefaults(defineProps<IconProps>(), {
  size: '1.3em'
})

/**
 * 计算 iconfont 图标的 CSS 类名
 * 优先级：直接 value（非 iconify） > code（在 iconfont 中存在）
 */
const computedIconClass = computed(() => {
  // 如果有 value 且不是 iconify 格式，直接返回作为 CSS 类
  if (props.value && !props.value.startsWith('i-')) {
    return props.value
  }

  // 如果有 code，检查是否在 iconfont 图标库中
  if (props.code) {
    const isIconfont = isIconfontIcon(props.code)
    if (isIconfont) {
      return `iconfont icon-${props.code}`
    }
  }

  return undefined
})

/**
 * 计算最终使用的 iconify 图标值
 * 只有在非 iconfont / 非本地 SVG 情况下才计算
 */
const computedValue = computed(() => {
  // 直接返回以 i- 开头的 value（iconify 格式）
  if (props.value?.startsWith('i-')) {
    return props.value
  }

  // 如果有 code，进行图标类型判断
  if (props.code) {
    // 检查是否为预设的 iconify 图标
    const isIconifyPreset = isIconifyIcon(props.code)
    if (isIconifyPreset) {
      return getIconifyValue(props.code)
    }

    // 本地 SVG sprite 由 computedSvgHref 处理，这里不接管
    if (isSvgIcon(props.code)) {
      return undefined
    }

    // 如果不在 iconfont 中，尝试作为 iconify 图标处理
    if (!isIconfontIcon(props.code)) {
      return `i-${props.code}`
    }
  }

  return undefined
})

/**
 * 计算本地 SVG sprite 的 use xlink:href
 * 对应 vite-plugin-svg-icons-ng 的 symbolId='icon-[name]'
 * 仅当 code 在 SVG_ICONS 清单中（即 src/assets/icons/svg/ 下存在同名 .svg）才生效
 */
const computedSvgHref = computed(() => {
  if (!props.code) return undefined
  // 优先级：iconfont > iconify，都不是才走 svg sprite
  if (isIconfontIcon(props.code)) return undefined
  if (isIconifyIcon(props.code)) return undefined
  if (isSvgIcon(props.code)) return `#icon-${props.code}`
  return undefined
})

/**
 * 处理图标尺寸的 CSS 类
 * 预设尺寸会返回对应的 Tailwind CSS 类，但对于iconfont图标需要特殊处理
 */
const sizeClass = computed(() => {
  const sizeValue = props.size

  // 数字类型的尺寸通过内联样式处理
  if (typeof sizeValue === 'number') {
    return ''
  }

  // 预定义尺寸映射
  const sizeMap: Record<SizePreset, string> = {
    'xs': computedIconClass.value ? '' : 'w-3 h-3', // 12px
    'sm': computedIconClass.value ? '' : 'w-4 h-4', // 16px
    'md': computedIconClass.value ? '' : 'w-5 h-5', // 20px
    'lg': computedIconClass.value ? '' : 'w-6 h-6', // 24px
    'xl': computedIconClass.value ? '' : 'w-8 h-8', // 32px
    '2xl': computedIconClass.value ? '' : 'w-10 h-10' // 40px
  }

  return sizeMap[sizeValue as SizePreset] || ''
})

/**
 * 处理内联样式（颜色和自定义尺寸）
 */
const inlineStyles = computed(() => {
  const styles: Record<string, string> = {}

  // 设置图标颜色
  if (props.color) {
    styles.color = props.color
  }

  // 预设尺寸到像素值的映射
  const presetSizeMap: Record<SizePreset, string> = {
    'xs': '12px',
    'sm': '16px',
    'md': '20px',
    'lg': '24px',
    'xl': '32px',
    '2xl': '40px'
  }

  // 处理数字类型的尺寸
  if (typeof props.size === 'number') {
    const sizeValue = `${props.size}px`
    // iconfont 图标需要设置 font-size，iconify 图标需要设置 width/height
    if (computedIconClass.value) {
      // iconfont 图标
      styles.fontSize = sizeValue
    } else {
      // iconify 图标
      styles.width = sizeValue
      styles.height = sizeValue
    }
  }
  // 处理预设尺寸
  else if (typeof props.size === 'string' && presetSizeMap[props.size as SizePreset]) {
    const sizeValue = presetSizeMap[props.size as SizePreset]
    if (computedIconClass.value) {
      // iconfont 图标使用 font-size
      styles.fontSize = sizeValue
    }
    // iconify 图标使用 CSS 类，不需要内联样式
  }
  // 处理字符串类型的自定义尺寸（非预设尺寸）
  else if (typeof props.size === 'string' && !sizeClass.value) {
    // iconfont 图标需要设置 font-size，iconify 图标需要设置 width/height
    if (computedIconClass.value) {
      // iconfont 图标
      styles.fontSize = props.size
    } else {
      // iconify 图标
      styles.width = props.size
      styles.height = props.size
    }
  }

  return styles
})

/**
 * 计算动画类名
 */
const animateClass = computed(() => {
  return props.animate ? `icon-hover-${props.animate}` : ''
})
</script>

<style scoped>
/**
 * 确保 iconfont 图标正确显示
 * 设置字体族和抗锯齿优化
 */
.iconfont {
  font-family: 'iconfont' !important;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/**
 * 本地 SVG sprite 图标
 * 通过 currentColor 响应外部 color 属性
 * 默认 vertical-align 避免行内布局时偏移
 */
.icon-svg-sprite {
  fill: currentColor;
  overflow: hidden;
  vertical-align: -0.15em;
}

/**
 * 确保所有图标以中心点为旋转轴
 */
div[class*='icon-hover-rotate'] {
  position: relative;
  transform-origin: center center !important;
}

/* 特殊处理：确保旋转图标的内容居中 */
div[class*='icon-hover-rotate'].iconfont {
  text-align: center;
  line-height: 1;
}
</style>
