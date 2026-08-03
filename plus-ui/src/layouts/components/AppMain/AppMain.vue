<!-- 主内容区域 -->
<template>
  <!--
  主内容区域组件 - 负责渲染当前激活路由的内容
  这是布局中最核心的内容展示区域
-->
  <section class="app-main">
    <!-- 使用 el-scrollbar 替代原生滚动 -->
    <el-scrollbar class="p-4">
      <!-- 路由视图部分 -->
      <router-view v-slot="{ Component, route }">
        <transition :enter-active-class="animate" mode="out-in">
          <keep-alive :include="layout.cachedViews.value">
            <component :is="Component" v-if="!route.meta.link" :key="route.path" />
          </keep-alive>
        </transition>
      </router-view>
      <!-- iframe处理 -->
      <IframeToggle />
    </el-scrollbar>
  </section>
</template>

<script setup lang="ts" name="AppMain">
/**
 * 主内容区域组件
 *
 * 该组件是布局的核心部分，负责渲染当前激活路由对应的视图组件
 * 实现了页面切换动画、组件缓存和iframe处理等功能
 */
import IframeToggle from './iframe/IframeToggle.vue'

const route = useRoute()
const layout = useLayout()

// 初始化动画实例，避免重复创建
const animation = useAnimation()

/**
 * 过渡动画相关变量
 * animate: 当前使用的动画类名
 * animationEnable: 是否启用动画效果
 */
const animate = ref<string>('')
const animationEnable = ref(layout.animationEnable.value)

/**
 * 监听动画启用状态变化
 * 当animationEnable设置变化时:
 * - 如果启用，使用随机动画效果
 * - 如果禁用，使用默认动画或无动画
 */
watch(
  () => layout.animationEnable.value,
  (val: boolean) => {
    animationEnable.value = val
    animate.value = val ? animation.getRandomAnimation() : animation.defaultAnimate
  },
  { immediate: true } // 组件挂载时立即执行一次
)

/**
 * 处理外部链接类型的路由
 * 当路由包含meta.link属性时，将该路由添加到iframe视图列表中
 * 由IframeToggle组件负责渲染这些外部链接
 */
watchEffect(() => {
  if (route.meta.link) {
    layout.addIframeView(route)
  }
})
</script>

<style lang="scss" scoped>
.app-main {
  /* 计算内容区域高度: 视口高度减去顶部导航栏高度(50px) */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  z-index: 1;
  overflow: hidden; /* 防止app-main自身滚动 */
  background-color: var(--app-bg);

  /* 确保el-scrollbar占满可用空间 */
  .el-scrollbar {
    height: 100%;
  }
}

/* 当头部固定时的调整 */
.fixed-header + .app-main {
  padding-top: 50px;
}

/* 当启用标签页视图时的调整 */
.hasTagsView {
  .app-main {
    /* 调整高度: 视口高度减去导航栏(50px)和标签页(34px)的总高度 */
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
/* 改进的滚动条处理方案：美化水平和垂直滚动条，保持适当粗细 */

/* 统一设置滚动条基本样式 - 适用于 Webkit 浏览器 (Chrome, Safari, Edge) */
::-webkit-scrollbar {
  width: 6px; /* 垂直滚动条宽度更细 */
  height: 6px; /* 水平滚动条高度一致 */
}

::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-lighter);
  border-radius: var(--radius-round);

  &:hover {
    background-color: var(--el-border-color);
  }
}

::-webkit-scrollbar-track {
  background-color: transparent;
  border-radius: var(--radius-round);
}

/* 滚动条转角处理 */
::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 针对 Firefox */
html,
body {
  scrollbar-width: thin;
  scrollbar-color: var(--el-border-color-lighter) transparent;
  overflow: auto;
}

/* 优化el-scrollbar样式 */
.el-scrollbar {
  .el-scrollbar__bar.is-vertical {
    width: 6px;
    opacity: 1;

    &:hover {
      opacity: 1;
    }
  }

  .el-scrollbar__bar.is-horizontal {
    height: 6px;
    opacity: 1;

    &:hover {
      opacity: 1;
    }
  }

  .el-scrollbar__thumb {
    background-color: var(--el-border-color-dark);
    border-radius: var(--radius-round);
  }

  /* 修复滚动区域高度问题 */
  .el-scrollbar__wrap {
    max-height: 100%; /* 确保不超出容器高度 */
  }
}

/* 修复el-dialog打开时的CSS样式问题 */
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 0 !important; /* 防止对话框导致的padding变化 */
  }
}

/* 添加平滑滚动效果 */
html {
  scroll-behavior: smooth;
}
</style>
