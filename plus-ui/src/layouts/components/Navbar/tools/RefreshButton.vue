<!-- 刷新按钮 -->
<template>
  <!-- 刷新按钮 -->
  <div
    class="navbar-tool-item ml-1 flex-center w-9 h-9 rounded-2 cursor-pointer transition-all duration-300"
    @click="handleRefresh"
  >
    <Icon code="refresh" size="md" animate="rotate180" />
  </div>
</template>

<script setup lang="ts" name="RefreshButton">
import { refreshPage } from '@/utils/tab'

/**
 * 刷新按钮组件
 *
 * 独立的刷新按钮，用于刷新当前页面
 * 从Hamburger组件中分离出来，可独立使用
 */

/**
 * 获取当前路由
 */
const route = useRoute()

/**
 * 处理刷新按钮点击事件
 * 刷新当前页面
 */
const handleRefresh = () => {
  refreshPage(route)
}
</script>

<style lang="scss" scoped></style>
