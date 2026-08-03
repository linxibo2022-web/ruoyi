<!-- 侧栏Logo -->
<template>
  <!-- 侧边栏Logo容器 -->
  <div class="sidebar-logo-container" :class="{ 'is-collapsed': isCollapsed }">
    <!-- Logo切换动画 -->
    <transition :enter-active-class="logoTransition.enter" mode="out-in">
      <!-- 折叠状态：仅显示Logo图标 -->
      <router-link v-if="isCollapsed" key="collapsed" class="sidebar-logo-link" to="/" :title="appTitle">
        <img v-if="hasLogo" :src="logoImageSrc" :alt="appTitle" class="sidebar-logo" />
        <h1 v-else class="sidebar-title collapsed-title" :style="titleStyle">
          {{ appTitleFirstChar }}
        </h1>
      </router-link>

      <!-- 展开状态：显示Logo图标+标题 -->
      <router-link v-else key="expanded" class="sidebar-logo-link" to="/" :title="appTitle">
        <img v-if="hasLogo" :src="logoImageSrc" :alt="appTitle" class="sidebar-logo" />
        <h1 class="sidebar-title expanded-title" :style="titleStyle">
          {{ appTitle }}
        </h1>
      </router-link>
    </transition>
  </div>
</template>

<script setup lang="ts" name="Logo">
import variables from '@/assets/styles/abstracts/exports.module.scss'
import logoImage from '@/assets/logo/logo.png'
import { logoAnimate } from '@/composables/useAnimation'

/**
 * ===== 组件Props定义 =====
 */

interface LogoProps {
  /** 侧边栏是否折叠 */
  collapse: boolean
}

const props = withDefaults(defineProps<LogoProps>(), {
  collapse: false
})

/**
 * ===== 常量定义 =====
 */

// 应用标题
const APP_TITLE = 'erp-sys'

/**
 * ===== 依赖注入 =====
 */

import { MenuLayoutMode } from '@/systemConfig'

const layout = useLayout()

/**
 * ===== 计算属性 =====
 */

// 侧边栏是否折叠（语义化命名）
const isCollapsed = computed(() => props.collapse)

// 当前侧边栏主题
const currentSideTheme = computed(() => layout.sideTheme.value)

// 是否为水平布局模式
const isHorizontalLayout = computed(() => layout.menuLayout.value === MenuLayoutMode.Horizontal)

// 是否为深色主题
const isDarkTheme = computed(() => currentSideTheme.value === 'theme-dark')

// 应用标题
const appTitle = computed(() => APP_TITLE)

// 应用标题首字符（折叠时显示）
const appTitleFirstChar = computed(() => appTitle.value.charAt(0).toUpperCase())

// 是否有Logo图片
const hasLogo = computed(() => Boolean(logoImage))

// Logo图片源
const logoImageSrc = computed(() => logoImage)

// Logo动画配置
const logoTransition = computed(() => logoAnimate)

/**
 * ===== 样式计算属性 =====
 */

// 标题文字样式
const titleStyle = computed(() => {
  // 水平布局模式下，使用 CSS 变量以适应主题切换
  if (isHorizontalLayout.value) {
    return { color: 'var(--el-text-color-primary)' }
  }
  // 侧边栏模式下，根据侧边栏主题决定颜色
  return { color: isDarkTheme.value ? variables.logoTitleColor : variables.logoLightTitleColor }
})
</script>

<style lang="scss" scoped>
/* Logo容器基础样式 - 使用CSS变量 */
.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 50px;
  line-height: 50px;
  text-align: left; /* 改为左对齐 */
  overflow: hidden;
  //background-color: var(--menu-bg);
  transition: all var(--duration-normal) ease;

  /* Logo链接样式 */
  .sidebar-logo-link {
    display: flex;
    align-items: center;
    justify-content: flex-start; /* 左对齐 */
    height: 100%;
    width: 100%;
    padding-left: 24px; /* 与菜单项左边距对齐 */
    text-decoration: none;
    transition: all var(--duration-normal) ease;

    &:hover {
      opacity: 0.8;
    }
  }

  /* Logo图片样式 */
  .sidebar-logo {
    width: 36px;
    height: 36px;
    object-fit: contain;
    transition: all var(--duration-normal) ease;
    flex-shrink: 0;
  }

  /* 标题文字基础样式 */
  .sidebar-title {
    margin: 0;
    line-height: 1;
    font-size: 18px;
    font-family: 'PingFang SC', 'Helvetica Neue', Helvetica, 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
    transition: all var(--duration-normal) ease;
    white-space: nowrap;
    overflow: hidden;
    //color: var(--menu-text-active);
  }

  /* 展开状态标题样式 */
  .expanded-title {
    margin-left: 12px;
    text-overflow: ellipsis;
  }

  /* 折叠状态标题样式 */
  .collapsed-title {
    font-size: 18px;
    font-weight: 700;
  }

  /* 折叠状态样式调整 */
  &.is-collapsed {
    .sidebar-logo {
      margin-right: 0;
    }

    .sidebar-logo-link {
      justify-content: center;
      padding-left: 0; /* 折叠状态下移除左边距 */
    }
  }
}
</style>
