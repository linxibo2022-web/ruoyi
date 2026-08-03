<!-- 401未授权 -->
<template>
  <AGeometricBackground class="error-page-401">
    <!-- 错误信息卡片 -->
    <div class="error-page-container">
      <div class="error-container">
        <!-- 401图标区域 -->
        <div class="error-icon-section">
          <div class="error-number">
            <span class="number-4 left">4</span>
            <span class="number-0">0</span>
            <span class="number-1 right">1</span>
          </div>

          <!-- 权限图标 -->
          <div class="permission-icon">
            <div class="lock-container">
              <div class="lock-body">
                <div class="lock-shackle"></div>
                <div class="keyhole"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 错误信息区域 -->
        <div class="error-content">
          <h1 class="error-title">访问被拒绝！</h1>
          <p class="error-description">
            对不起，您没有访问权限。<br />
            请联系管理员获取相关权限。
          </p>

          <!-- 操作按钮 -->
          <div class="error-actions">
            <router-link to="/" class="btn-primary">
              <span class="btn-icon">🏠</span>
              返回首页
            </router-link>
            <button @click="back" class="btn-secondary">
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
import { useRoute, useRouter } from 'vue-router'
import AGeometricBackground from '@/components/ATheme/AGeometricBackground.vue'

// 获取路由实例
const route = useRoute()
const router = useRouter()

/**
 * 返回上一页或首页
 * @description 根据URL参数决定返回行为
 * 当noGoBack参数存在时跳转到首页，否则返回上一页
 */
const back = () => {
  if (route.query.noGoBack) {
    router.push({ path: '/' })
  } else {
    // 检查是否有历史记录
    if (window.history.length > 1) {
      router.go(-1)
    } else {
      // 如果没有历史记录，跳转到首页
      router.push({ path: '/' })
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

// 401 错误页面主容器
.error-page-401 {
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
    margin-bottom: 30px;

    .number-4,
    .number-0,
    .number-1 {
      font-size: 120px;
      font-weight: 800;
      color: var(--el-color-primary);
      opacity: 0;
      animation: slideInScale 0.8s ease-out forwards;
    }

    .number-4.left {
      animation-delay: 0.2s;
    }

    .number-0 {
      color: #e53e3e;
      animation-delay: 0.4s;
    }

    .number-1.right {
      animation-delay: 0.6s;
    }
  }

  .permission-icon {
    opacity: 0;
    animation: slideInScale 0.8s ease-out 0.8s forwards;

    .lock-container {
      display: inline-block;
      position: relative;

      .lock-body {
        width: 60px;
        height: 45px;
        background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
        border-radius: 8px;
        position: relative;
        box-shadow: 0 4px 15px rgba(229, 62, 62, 0.3);

        .lock-shackle {
          position: absolute;
          top: -25px;
          left: 50%;
          transform: translateX(-50%);
          width: 35px;
          height: 25px;
          border: 5px solid #e53e3e;
          border-bottom: none;
          border-radius: 20px 20px 0 0;
          background: transparent;
        }

        .keyhole {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 8px;
          height: 12px;
          background: rgba(255, 255, 255, 0.9);
          border-radius: 50% 50% 0 0;
        }

        .keyhole::after {
          content: '';
          position: absolute;
          bottom: -6px;
          left: 50%;
          transform: translateX(-50%);
          width: 3px;
          height: 8px;
          background: rgba(255, 255, 255, 0.9);
        }
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
    animation: slideUp 0.6s ease-out 1s forwards;
  }

  .error-description {
    font-size: 16px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
    margin-bottom: 40px;
    opacity: 0;
    animation: slideUp 0.6s ease-out 1.2s forwards;
  }

  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    flex-wrap: wrap;
    opacity: 0;
    animation: slideUp 0.6s ease-out 1.4s forwards;

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

// 401 页面专用动画
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
    padding: 40px 20px;
  }

  .error-number {
    gap: 15px;

    .number-4,
    .number-0,
    .number-1 {
      font-size: 80px;
    }
  }

  .lock-container .lock-body {
    width: 50px;
    height: 38px;

    .lock-shackle {
      width: 30px;
      height: 20px;
      top: -20px;
      border-width: 4px;
    }

    .keyhole {
      width: 6px;
      height: 10px;
    }

    .keyhole::after {
      width: 2px;
      height: 6px;
    }
  }

  .error-title {
    font-size: 24px;
  }

  .error-description {
    font-size: 14px;
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

    .number-4,
    .number-0,
    .number-1 {
      font-size: 60px;
    }
  }

  .lock-container .lock-body {
    width: 45px;
    height: 35px;

    .lock-shackle {
      width: 25px;
      height: 18px;
      top: -18px;
      border-width: 3px;
    }
  }
}

// 暗色主题适配
.dark .error-page-401 {
  .error-number {
    .number-0 {
      color: #fc8181; // 暗黑模式下的红色保持特殊显示
    }
  }
}
</style>
