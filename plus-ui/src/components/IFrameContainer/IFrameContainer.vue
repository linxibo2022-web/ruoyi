<!--
 IFrame 容器组件
-->
<template>
  <div class="relative w-full overflow-hidden bg-gray-100" :style="{ height: containerHeight }">
    <!-- iframe -->
    <iframe class="block w-full h-full border-none bg-white" :src="props.src" border="0" scrolling="auto" />
  </div>
</template>

<script setup lang="ts" name="IFrameContainer">
/**
 * IFrame 容器组件的属性接口
 */
interface IFrameContainerProps {
  /**
   * iframe 要加载的源地址
   * 必填，必须是一个有效的 URL 字符串
   */
  src: string
}

// 使用 withDefaults 定义 props
const props = withDefaults(defineProps<IFrameContainerProps>(), {})

// 响应式状态
const containerHeight = ref('')

/**
 * 计算并设置 iframe 容器高度
 */
const calculateHeight = () => {
  // 减去90px，为了适应页面布局
  containerHeight.value = `${document.documentElement.clientHeight - 90}px`
}

onMounted(() => {
  // 初始计算高度
  calculateHeight()
  // 监听窗口大小变化
  window.addEventListener('resize', calculateHeight)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', calculateHeight)
})
</script>
