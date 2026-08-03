<!-- 认证页面左侧品牌展示区域(登录/注册通用) -->
<template>
  <AGeometricBackground class="auth-left-view">
    <!-- Logo -->
    <a class="logo" href="https://ruoyi.plus" target="_blank" rel="noopener noreferrer">
      <img src="@/assets/logo/logo.png" class="icon" alt="Logo" />
      <h1 class="title">ruoyi-plus-uniapp</h1>
    </a>

    <!-- 中间插画图片 -->
    <div class="left-illustration">
      <AThemeSvg :src="loginIcon" size="100%" />
    </div>

    <!-- 底部标语文字 -->
    <div class="text-wrap">
      <h1>{{ t('login.leftView.title') }}</h1>
      <p>{{ t('login.leftView.subtitle') }}</p>
    </div>

    <!-- 太阳/月亮切换按钮 - 覆盖在几何装饰之上 -->
    <div class="theme-toggle-button" @click="toggleDarkModeWithAnimation"></div>
  </AGeometricBackground>
</template>

<script setup lang="ts">
import { SystemConfig } from '@/systemConfig'
import AThemeSvg from '@/components/ATheme/AThemeSvg.vue'
import AGeometricBackground from '@/components/ATheme/AGeometricBackground.vue'
import loginIconUrl from '@/assets/images/login_icon.svg?url'
import { toggleThemeWithAnimation } from '@/utils/themeAnimation'

const { t } = useI18n()
const appTitle = SystemConfig.app.title
const loginIcon = loginIconUrl

// 布局管理器
const layout = useLayout()

// 暗黑模式状态
const isDark = computed(() => layout.dark.value)

// 切换暗黑模式 (带圆形扩散动画)
const toggleDarkModeWithAnimation = (event: MouseEvent) => {
  toggleThemeWithAnimation(event, isDark.value)
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

.auth-left-view {
  position: relative;
  width: 65vw;
  height: 100vh;
  padding: 20px;

  // Logo 区域 - 始终固定在左上角
  .logo {
    position: fixed;
    top: 20px;
    left: 20px;
    z-index: 1000;
    display: flex;
    align-items: center; // 垂直居中对齐
    text-decoration: none; // 移除链接下划线
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      opacity: 0.8;
      transform: translateX(2px);
    }

    .icon {
      width: 46px;
      height: 46px;
      transition: transform 0.3s ease;
    }

    &:hover .icon {
      transform: scale(1.05);
    }

    .title {
      margin: 0 0 0 10px; // 移除上下margin，只保留左边距
      font-size: 20px;
      font-weight: 400;
      color: var(--app-text);
    }
  }

  // 中间插画
  .left-illustration {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 40%;
    z-index: 10;
    // 注意: 动画的 transform 会覆盖 translate(-50%, -50%)
    // 所以需要在动画中也应用居中偏移
    animation: slideInLeftCentered 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  }

  // 底部文字
  .text-wrap {
    position: absolute;
    bottom: 80px;
    width: 100%;
    text-align: center;
    animation: slideInLeft 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;

    h1 {
      font-size: 24px;
      font-weight: 400;
      color: var(--app-text);
      margin: 0 0 10px 0;
    }

    p {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin: 0;
    }
  }

  // 太阳/月亮切换按钮
  .theme-toggle-button {
    position: absolute;
    top: 3%;
    right: 3%;
    z-index: 100;
    width: 50px;
    height: 50px;
    cursor: pointer;
    background: color-mix(in srgb, var(--el-color-primary-light-7) 80%, var(--bg-base));
    border-radius: 50%;
    transition: all 0.3s;
    animation: fadeInDown 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
    opacity: 0;

    // 亮色模式下隐藏 before 伪元素
    &::before {
      display: none;
    }

    &::after {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 100%;
      height: 100%;
      content: '';
      background: linear-gradient(to right, #fcbb04, #fffc00);
      border-radius: 50%;
      opacity: 0;
      transition: all 0.5s;
      transform: translate(-50%, -50%);
    }

    &:hover {
      box-shadow: 0 0 36px #fffc00;

      &::after {
        opacity: 1;
      }
    }
  }

  // AuthLeftView 专用动画
  @keyframes slideInLeft {
    from {
      opacity: 0;
      transform: translateX(-30px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  // 居中的 slideInLeft 动画（用于中间插画）
  @keyframes slideInLeftCentered {
    from {
      opacity: 0;
      transform: translate(-50%, -50%) translateX(-30px);
    }
    to {
      opacity: 1;
      transform: translate(-50%, -50%) translateX(0);
    }
  }

  @keyframes fadeInDown {
    from {
      opacity: 0;
      transform: translateY(-30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // 响应式: 平板及以下调整布局
  @media (max-width: $device-ipad-pro) {
    // 小屏下扩展到全屏,作为整个页面的背景
    position: fixed; // 固定定位
    top: 0;
    left: 0;
    width: 100vw; // 占满整个视口宽度
    height: 100vh; // 占满整个视口高度
    padding: 0;
    z-index: 0; // 在最底层,作为背景
    // 保留渐变背景,不设置为 transparent

    // 小屏下隐藏中间的插图和底部欢迎文字
    .left-illustration,
    .text-wrap {
      display: none;
    }

    // 保留几何装饰元素
    // Logo 保持位置不变 (20px, 20px)
  }
}

// 暗色主题适配
.dark .auth-left-view {
  .logo .title {
    color: rgba(255, 255, 255, 0.85);
  }

  .text-wrap {
    h1 {
      color: rgba(255, 255, 255, 0.85);
    }

    p {
      color: rgba(255, 255, 255, 0.45);
    }
  }

  // 暗黑模式下主题切换按钮变成月亮
  .theme-toggle-button {
    &::before {
      display: block !important; // 暗黑模式下显示 before 伪元素
      position: absolute;
      top: 0;
      left: 15px;
      width: 50px;
      height: 50px;
      content: '';
      background-color: #141414; // 使用与暗黑模式背景一致的颜色,使遮挡圆融入背景
      border-radius: 50%;
      transition: all 0.3s ease-in-out;
    }

    // 暗黑模式下隐藏 after 伪元素(太阳效果)
    &::after {
      display: none !important;
    }

    &:hover {
      box-shadow: 0 0 36px rgba(255, 255, 255, 0.3); // 月亮的微光效果

      &::before {
        left: 18px; // 月牙移动
      }
    }
  }
}
</style>
