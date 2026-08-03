<!-- 404未找到 -->
<template>
  <AGeometricBackground class="error-page-404">
    <!-- 错误信息卡片 -->
    <div class="error-page-container">
      <div class="error-container">
        <!-- 404图标区域 -->
        <div class="error-icon-section">
          <div class="error-number">
            <span class="number-4 left">4</span>
            <div class="number-0">
              <span class="static-zero">0</span>
            </div>
            <span class="number-4 right">4</span>
          </div>
        </div>

        <!-- 错误信息区域 -->
        <div class="error-content">
          <h1 class="error-title">{{ message }}</h1>
          <p class="error-description">
            对不起，您正在寻找的页面不存在。<br />
            可能是链接错误或页面已被移动。
          </p>

          <!-- 倒计时提示 -->
          <p class="countdown-tip">{{ countdown }} 秒后自动返回首页...</p>

          <!-- 操作按钮 -->
          <div class="error-actions">
            <router-link to="/index" class="btn-primary">
              <span class="btn-icon">🏠</span>
              返回首页
            </router-link>
            <button @click="goBack" class="btn-secondary">
              <span class="btn-icon">↩️</span>
              返回上页
            </button>
          </div>
        </div>
      </div>
    </div>
  </AGeometricBackground>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AGeometricBackground from '@/components/ATheme/AGeometricBackground.vue'

const router = useRouter()

const message = computed(() => {
  return '页面走丢了！'
})

// 倒计时秒数
const countdown = ref(3)
let timer: NodeJS.Timeout | null = null

const goBack = () => {
  // 检查是否有历史记录
  if (window.history.length > 1) {
    router.back()
  } else {
    // 如果没有历史记录，跳转到首页
    router.push('/index')
  }
}

// 启动倒计时
onMounted(() => {
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (timer) {
        clearInterval(timer)
      }
      // 倒计时结束，跳转到首页
      router.push('/')
    }
  }, 1000)
})

// 清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

// 404 错误页面主容器
.error-page-404 {
  position: relative;
  width: 100vw;
  height: 100vh;
}

// 错误页面容器 - 全屏居中布局
.error-page-container {
  position: relative;
  z-index: 10;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-container {
  background: var(--bg-level-1);
  border-radius: 24px;
  padding: 60px 40px;
  width: 600px;
  min-width: 480px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--el-border-color-lighter);
  text-align: center;
  transition: all 0.3s ease;
}

.error-icon-section {
  margin-bottom: 40px;

  .error-number {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;
    margin-bottom: 20px;

    .number-4 {
      font-size: 120px;
      font-weight: 800;
      color: var(--el-color-primary);
      opacity: 0;
      animation: slideInScale 0.8s ease-out forwards;

      &.left {
        animation-delay: 0.2s;
      }

      &.right {
        animation-delay: 0.6s;
      }
    }

    .number-0 {
      position: relative;
      width: 120px;
      height: 120px;
      opacity: 0;
      animation: slideInScale 0.8s ease-out 0.4s forwards;
      display: flex;
      align-items: center;
      justify-content: center;

      .static-zero {
        font-size: 120px;
        font-weight: 800;
        color: #cbd5e0;
        line-height: 1;
      }
    }
  }
}

.error-content {
  .error-title {
    font-size: 32px;
    font-weight: 700;
    color: var(--app-text);
    margin-bottom: 16px;
    opacity: 0;
    animation: slideUp 0.6s ease-out 0.8s forwards;
  }

  .error-description {
    font-size: 16px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
    margin-bottom: 40px;
    opacity: 0;
    animation: slideUp 0.6s ease-out 1s forwards;
  }

  .countdown-tip {
    font-size: 14px;
    color: var(--el-color-primary);
    margin-bottom: 30px;
    font-weight: 500;
    opacity: 0;
    animation: slideUp 0.6s ease-out 1.1s forwards;
  }

  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-bottom: 40px;
    flex-wrap: wrap;
    opacity: 0;
    animation: slideUp 0.6s ease-out 1.2s forwards;

    .btn-primary,
    .btn-secondary {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 12px 24px;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      text-decoration: none;
      transition: all 0.3s ease;
      border: none;
      cursor: pointer;

      .btn-icon {
        font-size: 16px;
      }
    }

    .btn-primary {
      background: var(--el-color-primary);
      color: white;
      box-shadow: 0 4px 15px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.3);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.4);
      }
    }

    .btn-secondary {
      background: rgba(var(--el-color-primary-rgb, 93, 135, 255), 0.08);
      color: var(--el-color-primary);
      border: 1px solid rgba(var(--el-color-primary-rgb, 93, 135, 255), 0.2);

      &:hover {
        background: rgba(var(--el-color-primary-rgb, 93, 135, 255), 0.12);
        transform: translateY(-2px);
      }
    }
  }
}

// 404 页面专用动画
@keyframes slideInScale {
  0% {
    opacity: 0;
    transform: scale(0.5) translateY(30px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes slideUp {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .error-container {
    width: 90%;
    min-width: 320px;
    padding: 40px 30px;
  }

  .error-number {
    .number-4 {
      font-size: 80px;
    }

    .number-0 {
      width: 80px;
      height: 80px;

      .static-zero {
        font-size: 80px;
      }
    }
  }

  .error-title {
    font-size: 24px;
  }

  .error-description {
    font-size: 14px;
  }

  .countdown-tip {
    font-size: 13px;
    margin-bottom: 25px;
  }

  .error-actions {
    flex-direction: column;
    align-items: center;

    .btn-primary,
    .btn-secondary {
      width: 200px;
      justify-content: center;
    }
  }
}

@media (max-width: 480px) {
  .error-container {
    padding: 30px 20px;
  }

  .error-number {
    gap: 10px;

    .number-4 {
      font-size: 60px;
    }

    .number-0 {
      width: 60px;
      height: 60px;

      .static-zero {
        font-size: 60px;
      }
    }
  }

  .countdown-tip {
    font-size: 12px;
    margin-bottom: 20px;
  }
}

// 暗色主题适配
.dark .error-page-404 {
  .error-number {
    .static-zero {
      color: rgba(255, 255, 255, 0.3);
    }
  }
}
</style>
