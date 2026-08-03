<!-- 通用几何装饰背景组件 -->
<template>
  <div class="geometric-background">
    <!-- 几何装饰元素 -->
    <div class="geometric-decorations">
      <!-- 基础几何形状 -->
      <div class="geo-element circle-outline animate-fade-in-up"></div>
      <div class="geo-element square-rotated animate-fade-in-left"></div>
      <div class="geo-element circle-small animate-fade-in-up"></div>
      <div class="geo-element square-bottom-right animate-fade-in-right"></div>

      <!-- 背景泡泡 -->
      <div class="geo-element bg-bubble animate-scale-in"></div>

      <!-- 装饰点 -->
      <div class="geo-element dot dot-top-left animate-bounce-in"></div>
      <div class="geo-element dot dot-top-right animate-bounce-in"></div>
      <div class="geo-element dot dot-center-right animate-bounce-in"></div>

      <!-- 叠加方块组 -->
      <div class="squares-group">
        <i class="geo-element square square-blue animate-fade-in-left-rotated"></i>
        <i class="geo-element square square-pink animate-fade-in-left-rotated"></i>
        <i class="geo-element square square-purple animate-fade-in-left-no-rotation"></i>
      </div>
    </div>

    <!-- 默认插槽，允许在背景上放置内容 -->
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
// 无需额外逻辑
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

// 使用项目中的主题色变量
$primary-base: var(--el-color-primary);
$primary-light-7: var(--el-color-primary-light-7);
$primary-light-8: var(--el-color-primary-light-8);
$primary-light-9: var(--el-color-primary-light-9);

.geometric-background {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(
    135deg,
    color-mix(in srgb, $primary-light-9 100%, var(--bg-base)) 0%,
    color-mix(in srgb, $primary-light-8 80%, var(--bg-base)) 100%
  );

  // 几何装饰元素
  .geometric-decorations {
    position: absolute;
    inset: 0;
    pointer-events: none; // 不阻止鼠标事件

    .geo-element {
      position: absolute;
      opacity: 0;
      animation-fill-mode: forwards;
      animation-duration: 0.8s;
      animation-timing-function: cubic-bezier(0.25, 0.46, 0.45, 0.94);
    }

    // 圆形轮廓
    .circle-outline {
      top: 10%;
      left: 25%;
      width: 42px;
      height: 42px;
      border: 2px solid $primary-light-8;
      border-radius: 50%;
    }

    // 旋转方块
    .square-rotated {
      top: 50%;
      left: 16%;
      width: 60px;
      height: 60px;
      background-color: color-mix(in srgb, $primary-light-8 80%, var(--bg-base));
      transform: rotate(-25deg);
    }

    // 小圆点
    .circle-small {
      bottom: 26%;
      left: 30%;
      width: 18px;
      height: 18px;
      background-color: $primary-light-8;
      border-radius: 50%;
    }

    // 右下方块
    .square-bottom-right {
      right: 10%;
      bottom: 10%;
      width: 50px;
      height: 50px;
      background-color: $primary-light-8;
      transform: rotate(45deg);
    }

    // 背景泡泡
    .bg-bubble {
      top: -120px;
      right: -120px;
      width: 360px;
      height: 360px;
      background-color: color-mix(in srgb, $primary-light-8 80%, var(--bg-base));
      border-radius: 50%;
    }

    // 装饰点
    .dot {
      width: 14px;
      height: 14px;
      background-color: $primary-light-7;
      border-radius: 50%;

      &.dot-top-left {
        top: 140px;
        left: 100px;
      }

      &.dot-top-right {
        top: 140px;
        right: 120px;
      }

      &.dot-center-right {
        top: 46%;
        right: 22%;
        background-color: $primary-light-8;
      }
    }

    // 叠加方块组
    .squares-group {
      position: absolute;
      bottom: 18px;
      left: 20px;
      width: 140px;
      height: 140px;

      .square {
        position: absolute;
        display: block;
        border-radius: var(--radius-md);
        box-shadow: 0 8px 24px rgba(64, 87, 167, 0.12);

        &.square-blue {
          top: 12px;
          left: 30px;
          z-index: 2;
          width: 50px;
          height: 50px;
          background-color: color-mix(in srgb, $primary-base 30%, transparent);
          transform: rotate(-10deg);
        }

        &.square-pink {
          top: 30px;
          left: 48px;
          z-index: 1;
          width: 70px;
          height: 70px;
          background-color: color-mix(in srgb, $primary-base 15%, transparent);
          transform: rotate(10deg);
        }

        &.square-purple {
          top: 66px;
          left: 86px;
          z-index: 3;
          width: 32px;
          height: 32px;
          background-color: color-mix(in srgb, $primary-base 45%, transparent);
        }
      }

      // 装饰线条
      &::after {
        content: '';
        position: absolute;
        top: 86px;
        left: 72px;
        width: 80px;
        height: 1px;
        background: linear-gradient(90deg, var(--el-color-primary-light-6, $primary-light-7), transparent);
        opacity: 0;
        transform: rotate(50deg);
        animation: lineGrow 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
        animation-delay: 1.2s;
      }
    }
  }
}

// 动画定义
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes lineGrow {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// 动画类
.animate-fade-in-up {
  animation-name: fadeInUp;
}

.animate-fade-in-left {
  animation-name: fadeInLeft;
}

.animate-fade-in-right {
  animation-name: fadeInRight;
}

.animate-scale-in {
  animation-name: scaleIn;
  animation-duration: 1.2s;
}

.animate-bounce-in {
  animation-name: bounceIn;
  animation-duration: 0.6s;
}

.animate-fade-in-left-rotated {
  animation-name: fadeInLeft;
  animation-delay: 0.2s;
}

.animate-fade-in-left-no-rotation {
  animation-name: fadeInLeft;
  animation-delay: 0.4s;
}

// 暗色主题适配
.dark .geometric-background {
  // 暗黑模式下背景色保持一致,使用深色调
  background: linear-gradient(135deg, #0a0a0a 0%, #141414 100%);

  .geometric-decorations {
    // 暗黑模式下几何元素使用主题色的半透明版本
    .circle-outline {
      border-color: color-mix(in srgb, $primary-base 30%, transparent);
    }

    .square-rotated {
      background-color: color-mix(in srgb, $primary-base 8%, transparent);
    }

    .bg-bubble {
      display: none !important; // 暗黑模式下隐藏右上角大气泡
    }

    .circle-small,
    .square-bottom-right {
      background-color: color-mix(in srgb, $primary-base 20%, transparent);
    }

    .dot {
      background-color: color-mix(in srgb, $primary-base 25%, transparent);

      &.dot-top-right {
        display: none !important; // 暗黑模式下隐藏右上角的装饰点
      }

      &.dot-center-right {
        background-color: color-mix(in srgb, $primary-base 15%, transparent);
      }
    }

    .squares-group {
      .square {
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);

        &.square-blue {
          background-color: color-mix(in srgb, $primary-base 20%, transparent);
        }

        &.square-pink {
          background-color: color-mix(in srgb, $primary-base 10%, transparent);
        }

        &.square-purple {
          background-color: color-mix(in srgb, $primary-base 30%, transparent);
        }
      }

      &::after {
        background: linear-gradient(90deg, color-mix(in srgb, $primary-base 30%, transparent), transparent);
      }
    }
  }
}
</style>
