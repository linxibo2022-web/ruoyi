<!-- 内嵌链接 -->
<template>
  <div :style="'height:' + height">
    <iframe :id="iframeId" style="width: 100%; height: 100%; border: 0" :src="src"></iframe>
  </div>
</template>

<script setup lang="ts" name="InnerLink">
/**
 * IFrame 组件的 Props 接口定义
 */
interface IFrameProps {
  /**
   * iframe 的源地址 URL
   * @default '/'
   */
  src?: string

  /**
   * iframe 的唯一标识 ID
   * 必填，用于精确定位和操作特定的 iframe
   */
  iframeId: string
}

// 使用 withDefaults 定义 props，提供默认值和必填项
const props = withDefaults(defineProps<IFrameProps>(), {
  src: '/'
})

// 响应式计算高度
const height = ref('')

/**
 * 计算并设置 iframe 高度
 * 减去顶部导航栏等固定区域的高度
 */
const calculateHeight = () => {
  height.value = `${document.documentElement.clientHeight - 94.5}px`
}

// 在组件挂载时计算初始高度
onMounted(() => {
  calculateHeight()

  // 监听窗口大小变化，动态调整高度
  window.addEventListener('resize', calculateHeight)
})

// 组件卸载时移除事件监听，防止内存泄漏
onUnmounted(() => {
  window.removeEventListener('resize', calculateHeight)
})
</script>
