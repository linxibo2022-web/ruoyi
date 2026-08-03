<!-- 侧边栏 -->
<template>
  <div :class="{ 'has-logo': isLogoVisible }" :style="sidebarStyles" class="sidebar-container h-full">
    <!-- 应用logo -->
    <logo v-if="isLogoVisible" :collapse="isSidebarCollapsed" />

    <!-- 菜单滚动容器 -->
    <el-scrollbar :class="currentSideTheme" class="scrollbar-wrapper">
      <transition :enter-active-class="menuSearchAnimate.enter" mode="out-in">
        <el-menu
          :default-active="currentActiveMenu"
          :collapse="isSidebarCollapsed"
          :background-color="menuBackgroundColor"
          :text-color="menuTextColor"
          :unique-opened="true"
          :active-text-color="currentThemeColor"
          :collapse-transition="false"
          mode="vertical"
          class="h-full w-full border-none"
        >
          <!-- 递归渲染侧边栏菜单项 -->
          <SidebarItem v-for="(route, index) in authorizedSidebarRoutes" :key="route.path + index" :item="route" :base-path="route.path" />
        </el-menu>
      </transition>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts" name="Sidebar">
import Logo from './Logo.vue'
import SidebarItem from './SidebarItem.vue'
import variables from '@/assets/styles/abstracts/exports.module.scss'
import { type RouteRecordRaw } from 'vue-router'
import { menuSearchAnimate } from '@/composables/useAnimation'
import { SideTheme } from '@/systemConfig'

// ============== 布局状态管理 ==============
const currentRoute = useRoute()
const permissionStore = usePermissionStore()

// 获取布局状态管理
const layout = useLayout()

// ============== 菜单路由配置 ==============
/**
 * 获取用户有权限访问的侧边栏路由列表
 * @returns 经过权限过滤的路由配置数组
 */
const authorizedSidebarRoutes = computed<RouteRecordRaw[]>(() => permissionStore.getSidebarRoutes())

// ============== 侧边栏显示配置 ==============
/**
 * 是否显示应用logo
 */
const isLogoVisible = layout.sidebarLogo

/**
 * 当前侧边栏主题样式类名
 */
const currentSideTheme = layout.sideTheme

/**
 * 当前主题色配置
 */
const currentThemeColor = layout.theme

/**
 * 侧边栏是否处于折叠状态
 */
const isSidebarCollapsed = computed(() => !layout.sidebar.value.opened)

// ============== 菜单激活状态 ==============
/**
 * 计算当前应该激活的菜单项路径
 * 优先使用路由meta中的activeMenu配置，否则使用当前路径
 */
const currentActiveMenu = computed(() => {
  const { meta, path } = currentRoute

  // 如果路由配置了自定义激活菜单，优先使用
  if (meta.activeMenu) {
    return meta.activeMenu
  }

  return path
})

// ============== 主题颜色配置 ==============
/**
 * 根据当前主题获取菜单背景色
 */
const menuBackgroundColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuBackground : variables.menuLightBackground))

/**
 * 根据当前主题获取菜单文字颜色
 */
const menuTextColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuColor : variables.menuLightColor))

/**
 * 根据当前主题获取菜单悬停背景色
 */
const menuHoverColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuHover : variables.menuLightHover))

/**
 * 根据当前主题获取菜单悬停文字颜色
 */
const menuHoverTextColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuHoverText : variables.menuLightHoverText))

/**
 * 侧边栏样式对象（包含CSS变量）
 */
const sidebarStyles = computed(() => ({
  backgroundColor: menuBackgroundColor.value
}))

/**需要把这俩变量值提高层级到最顶层*/
watch(
  [menuHoverColor, menuHoverTextColor],
  ([hoverColor, hoverTextColor]) => {
    document.documentElement.style.setProperty('--menu-hover-color', hoverColor)
    document.documentElement.style.setProperty('--menu-hover-text-color', hoverTextColor)
  },
  { immediate: true }
)
</script>
