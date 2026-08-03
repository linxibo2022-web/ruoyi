<!-- 重定向 -->
<template>
  <div></div>
</template>

<script setup lang="ts">
// 导入Vue Router的路由相关钩子函数
import { useRoute, useRouter } from 'vue-router'

// 获取当前路由对象
const route = useRoute()
// 获取路由器实例，用于编程式导航
const router = useRouter()
// 从当前路由中解构获取params和query参数
const { params, query } = route
// 从params中获取path参数
const { path } = params

// 执行路由重定向，保留原查询参数，但更新路径
// 这会将当前路由替换为新路径，不会产生新的历史记录
router.replace({ path: '/' + path, query })
</script>
