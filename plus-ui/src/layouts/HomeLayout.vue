<!-- 首页布局 -->
<template>
  <div class="" :style="{ '--current-color': theme }">
    <!-- 主内容区 -->
    <div class="">
      <!-- 主内容 -->
      <app-main />
    </div>
  </div>
</template>

<script setup lang="ts" name="HomeLayout">
import AppMain from './components/AppMain/AppMain.vue'
import { SystemConfig } from '@/systemConfig'

// 获取应用设置
const theme = computed(() => useLayout().theme)

// 组件挂载时初始化
onMounted(() => {
  // 建立实时通信连接
  webSocket.initialize()
  webSocket.connect()

  useSSE(SystemConfig.api.baseUrl + '/resource/sse')
})
</script>
