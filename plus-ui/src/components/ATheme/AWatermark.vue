<!-- 全局水印组件 -->
<template>
  <!-- 插槽内容 -->
  <slot></slot>

  <!-- 水印层 - 使用 Teleport 传送到 body -->
  <Teleport to="body">
    <el-watermark
      v-if="isVisible"
      :content="content"
      :font="fontConfig"
      :gap="gap"
      :offset="offset"
      :rotate="rotate"
      :z-index="zIndex"
      class="global-watermark-layer"
    />
  </Teleport>
</template>

<script setup lang="ts">
import { ElWatermark } from 'element-plus'
import { SystemConfig } from '@/systemConfig'

/**
 * 水印组件 Props
 */
interface WatermarkProps {
  /** 是否显示水印 */
  visible?: boolean
  /** 水印内容 */
  content?: string | string[]
  /** 字体大小 */
  fontSize?: number
  /** 字体颜色 */
  fontColor?: string
  /** 旋转角度 */
  rotate?: number
  /** 水平间距 */
  gapX?: number
  /** 垂直间距 */
  gapY?: number
  /** 水平偏移 */
  offsetX?: number
  /** 垂直偏移 */
  offsetY?: number
  /** 层级 */
  zIndex?: number
}

// 定义 props 带默认值
const props = withDefaults(defineProps<WatermarkProps>(), {
  visible: false,
  content: () => SystemConfig.app.title || 'ruoyi-plus-uniapp',
  fontSize: 16,
  fontColor: 'rgba(128, 128, 128, 0.2)',
  rotate: -22,
  gapX: 100,
  gapY: 100,
  offsetX: 50,
  offsetY: 50,
  zIndex: 99999
})

// 是否显示水印
const isVisible = computed(() => props.visible)

// 字体配置
const fontConfig = computed(() => ({
  fontSize: props.fontSize,
  color: props.fontColor
}))

// 间距配置 [x, y]
const gap = computed<[number, number]>(() => [props.gapX, props.gapY])

// 偏移配置 [x, y]
const offset = computed<[number, number]>(() => [props.offsetX, props.offsetY])
</script>

<style lang="scss">
/* 全局水印层样式 - 不使用 scoped */
.global-watermark-layer {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 99999 !important; /* 确保在最上层 */
  pointer-events: none !important; /* 不阻挡交互事件 */
  overflow: hidden !important;

  /* 确保水印容器内的所有元素都不阻挡交互 */
  * {
    pointer-events: none !important;
  }
}
</style>
