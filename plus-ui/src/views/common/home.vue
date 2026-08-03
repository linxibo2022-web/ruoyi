<!-- 首页 -->
<template>
  <!-- 中间白色内容区域 -->
  <div class="w-full min-h-screen flex justify-center relative">
    <!-- 背景图片层 - 只调整这层的透明度 -->
    <div :style="{ backgroundImage: `url(${homeBg})` }" class="absolute inset-0 bg-cover bg-center bg-no-repeat opacity-70 h-screen"></div>

    <!-- 内容层 - 透明度不受影响 -->
    <div class="w-full min-h-screen shadow-2xl relative z-10">
      <!-- 头部横幅 -->
      <div class="py-20 text-center text-white">
        <div class="">
          <h1 class="text-5xl font-bold drop-shadow-lg">欢迎来到我们的平台</h1>
          <p class="text-xl mt-4 opacity-90 leading-relaxed">为您提供最优质的服务体验</p>
          <el-button class="px-8 py-3" type="primary" size="large" icon="StarFilled" @click="handleGetStarted"> 立即开始 </el-button>
        </div>
      </div>

      <!-- 特色功能区 -->
      <div class="py-4">
        <div class="max-w-5xl mx-auto px-5">
          <h2 class="text-3xl font-semibold text-center mb-12 text-gray-800">核心功能</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div
              v-for="feature in features"
              :key="feature.id"
              class="bg-white rounded-xl p-6 text-center shadow-lg hover:shadow-xl transition-all duration-300 hover:-translate-y-2"
            >
              <div class="mb-4 flex justify-center">
                <el-icon :size="48" :color="feature.color">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <h3 class="text-lg font-semibold mb-3 text-gray-800">{{ feature.title }}</h3>
              <p class="text-gray-600 text-sm leading-relaxed">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计数据区 -->
      <div class="py-16 text-#333">
        <div class="max-w-4xl mx-auto px-5">
          <div class="grid grid-cols-4 gap-8">
            <div v-for="stat in stats" :key="stat.label" class="text-center">
              <div class="mb-2">
                <el-icon :size="32" :color="stat.color">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
              <div class="text-3xl font-bold mb-1">{{ stat.value }}{{ stat.label === '满意度' ? '%' : '+' }}</div>
              <div>{{ stat.label }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import homeBg from '@/assets/images/homeBg.jpg'
import { ref, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { StarFilled, TrophyBase, Lock, Lightning, User, Avatar, Medal } from '@element-plus/icons-vue'

const router = useRouter()

// 特色功能数据 - 使用 markRaw 避免响应式警告
const features = ref([
  {
    id: 1,
    icon: markRaw(StarFilled),
    color: '#fbbf24',
    title: '高质量服务',
    description: '我们提供专业、可靠的服务，确保您的满意度'
  },
  {
    id: 2,
    icon: markRaw(Lock),
    color: '#10b981',
    title: '安全可靠',
    description: '采用最先进的安全技术，保护您的数据安全'
  },
  {
    id: 3,
    icon: markRaw(Lightning),
    color: '#f59e0b',
    title: '快速响应',
    description: '7×24小时快速响应，第一时间解决您的问题'
  },
  {
    id: 4,
    icon: markRaw(TrophyBase),
    color: '#ef4444',
    title: '行业领先',
    description: '多年行业经验，获得众多客户的信赖和好评'
  }
])

// 统计数据 - 使用 markRaw 避免响应式警告
const stats = ref([
  {
    label: '注册用户',
    value: 10000,
    icon: markRaw(User),
    color: '#3b82f6'
  },
  {
    label: '服务客户',
    value: 5000,
    icon: markRaw(Avatar),
    color: '#10b981'
  },
  {
    label: '项目完成',
    value: 8000,
    icon: markRaw(Medal),
    color: '#f59e0b'
  },
  {
    label: '满意度',
    value: 99.9,
    icon: markRaw(StarFilled),
    color: '#ef4444'
  }
])

// 方法
const handleGetStarted = () => {
  // 检查用户是否已登录
  const userStore = useUserStore()
  if (userStore.token) {
    router.push('/index')
    ElMessage.success('欢迎回来！')
  } else {
    router.push('/login?redirect=/index')
  }
}

onMounted(() => {
  // 页面加载时的初始化逻辑
  console.log('Home page loaded')

  // 显示欢迎消息
  ElNotification({
    title: '欢迎访问',
    message: '欢迎来到我们的平台，希望您有愉快的体验！',
    type: 'success',
    duration: 3000
  })
})
</script>

<style scoped lang="scss"></style>
