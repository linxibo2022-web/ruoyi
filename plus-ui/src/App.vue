<!-- 应用根组件 -->
<template>
  <!-- Element Plus 配置提供者组件
       - locale: 设置国际化语言环境
       - size: 设置组件的默认尺寸
       从布局状态管理中获取全局的语言和尺寸配置 -->
  <el-config-provider :locale="layout.locale.value" :size="layout.size.value">
    <!-- 路由视图组件 用于渲染当前路由对应的页面内容-->
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
const layout = useLayout()
// 获取主题管理钩子
const { setTheme } = useTheme()
// 获取功能配置 Store
const featureStore = useFeatureStore()

// 组件挂载后的生命周期钩子
onMounted(async () => {
  // 使用 nextTick 确保 DOM 已经完全渲染
  await nextTick()
  // 初始化主题样式
  // 使用 useTheme 钩子应用当前主题
  setTheme(layout.theme.value)
  // 初始化系统功能配置
  await featureStore.initFeatures()
})
</script>
