<!--
主题色选择器组件 AThemeColorPicker

使用示例：

1. 基本用法
<AThemeColorPicker />

2. 自定义主题色列表
<AThemeColorPicker :colors="['#5D87FF', '#B48DF3', '#1D84FF']" />

3. 自定义当前主题色
<AThemeColorPicker :current-color="currentTheme" @change="handleThemeChange" />
-->
<template>
  <div class="theme-color-picker">
    <div class="color-dots-container">
      <div
        v-for="(color, index) in colors"
        :key="color"
        class="color-dot"
        :class="{ active: color === currentColor }"
        :style="{ background: color, '--index': index }"
        @click="handleColorChange(color)"
      >
        <Icon size="xs" v-if="color === currentColor" code="check" class="check-icon" />
      </div>
    </div>
    <div class="color-trigger-btn">
      <Icon code="palette" size="20px" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { SystemConfig, PREDEFINED_THEME_COLORS } from '@/systemConfig'

/**主题色选择器属性接口*/
interface AThemeColorPickerProps {
  /**主题色列表*/
  colors?: string[]
  /**当前主题色*/
  currentColor?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<AThemeColorPickerProps>(), {
  colors: () => [...PREDEFINED_THEME_COLORS],
  currentColor: SystemConfig.ui.theme
})

/**组件事件定义*/
const emit = defineEmits<{
  /**主题色改变事件*/
  change: [color: string]
}>()

/**处理主题色切换*/
const handleColorChange = (color: string) => {
  if (props.currentColor === color) return

  // 触发change事件
  emit('change', color)

  // 调用主题色设置函数
  setElementThemeColor(color)
}

/** 将hex颜色转换为RGB数组 */
const hexToRgb = (hexColor: string): number[] => {
  const cleanHex = hexColor.replace(/^#/, '')
  let hex = cleanHex

  // 处理缩写形式 (#FFF -> #FFFFFF)
  if (hex.length === 3) {
    hex = hex
      .split('')
      .map((char) => char.repeat(2))
      .join('')
  }

  const hexPairs = hex.match(/../g) || []
  return hexPairs.map((hexPair) => parseInt(hexPair, 16))
}

/** 将RGB颜色转换为hex */
const rgbToHex = (r: number, g: number, b: number): string => {
  const toHex = (value: number) => {
    const hex = value.toString(16)
    return hex.length === 1 ? `0${hex}` : hex
  }
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`
}

/** 获取变浅的颜色 */
const getLightColor = (color: string, level: number): string => {
  const rgb = hexToRgb(color)
  const lightRgb = rgb.map((value) => Math.floor((255 - value) * level + value))
  return rgbToHex(lightRgb[0], lightRgb[1], lightRgb[2])
}

/** 获取变深的颜色 */
const getDarkColor = (color: string, level: number): string => {
  const rgb = hexToRgb(color)
  const darkRgb = rgb.map((value) => Math.floor(value * (1 - level)))
  return rgbToHex(darkRgb[0], darkRgb[1], darkRgb[2])
}

/** 颜色混合 */
const colourBlend = (color1: string, color2: string, ratio: number): string => {
  const validRatio = Math.max(0, Math.min(1, ratio))
  const rgb1 = hexToRgb(color1)
  const rgb2 = hexToRgb(color2)

  const blendedRgb = rgb1.map((value1, index) => {
    const value2 = rgb2[index]
    return Math.round(value1 * (1 - validRatio) + value2 * validRatio)
  })

  return rgbToHex(blendedRgb[0], blendedRgb[1], blendedRgb[2])
}

/** 设置 Element Plus 主题颜色 */
const setElementThemeColor = (color: string) => {
  const mixColor = '#ffffff'
  const elStyle = document.documentElement.style

  // 设置主色
  elStyle.setProperty('--el-color-primary', color)

  // 生成 light 系列颜色 (1-9)
  for (let i = 1; i <= 9; i++) {
    elStyle.setProperty(`--el-color-primary-light-${i}`, getLightColor(color, i / 10))
  }

  // 生成 dark 系列颜色 (1-9)
  for (let i = 1; i <= 9; i++) {
    elStyle.setProperty(`--el-color-primary-dark-${i}`, getDarkColor(color, i / 10))
  }

  // 生成更淡的自定义颜色 (1-15)
  for (let i = 1; i < 16; i++) {
    const itemColor = colourBlend(color, mixColor, i / 16)
    elStyle.setProperty(`--el-color-primary-custom-${i}`, itemColor)
  }
}
</script>

<style lang="scss" scoped>
.theme-color-picker {
  position: relative;
  display: flex;
  align-items: center;

  .color-trigger-btn {
    position: relative;
    z-index: 2;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    cursor: pointer;
    user-select: none;
    transition: all 0.3s;
    font-size: 18px;
    color: var(--el-text-color-regular);

    &:hover {
      color: var(--el-color-primary);
    }

    // 主题色图标特殊大小
    :deep(.icon-font) {
      font-size: 19px !important;
    }
  }

  .color-dots-container {
    position: absolute;
    right: 0;
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 8px 36px 8px 10px;
    pointer-events: none;
    backdrop-filter: blur(10px);
    background-color: rgba(255, 255, 255, 0.9);
    border-radius: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    opacity: 0;
    transition:
      opacity 0.3s ease,
      transform 0.3s ease;
    transform: translateX(10px);

    .color-dot {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      cursor: pointer;
      border-radius: 50%;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
      opacity: 0;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      transform: translateX(20px) scale(0.8);
      outline: none;

      .check-icon {
        color: #fff;
        filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
      }

      &:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        transform: translateX(0) scale(1.1);
      }

      &.active {
        transform: translateX(0) scale(1.2);
      }

      &:focus {
        outline: none;
      }
    }
  }

  &:hover {
    .color-dots-container {
      pointer-events: auto;
      opacity: 1;
      transform: translateX(0);

      .color-dot {
        opacity: 1;
        transition-delay: calc(var(--index) * 0.05s);
        transform: translateX(0) scale(1);
      }
    }

    .color-trigger-btn {
      color: var(--el-color-primary);
    }
  }
}

// 暗黑模式下的样式
.dark .theme-color-picker {
  .color-dots-container {
    background-color: rgba(30, 30, 30, 0.9);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  }
}
</style>
