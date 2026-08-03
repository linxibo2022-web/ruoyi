<!-- 主布局 -->
<template>
  <div class="app-wrapper" :class="classObj" :style="{ '--current-color': theme }">
    <!-- 移动端侧边栏遮罩层 -->
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside" />

    <!-- 侧边栏：双列模式渲染 DualSidebar，其余模式渲染标准 Sidebar -->
    <DualSidebar v-if="showSidebar && menuLayout === MenuLayoutMode.DualColumn" class="sidebar-container" />
    <Sidebar v-else-if="showSidebar" class="sidebar-container" />

    <!-- 主内容区 -->
    <div
      class="main-container"
      :class="{
        hasTagsView: needTagsView,
        sidebarHide: sidebar.hide || menuLayout === MenuLayoutMode.Horizontal,
        horizontalLayout: menuLayout === MenuLayoutMode.Horizontal
      }"
    >
      <!-- 固定顶部区域 -->
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar ref="navbarRef" @set-layout="setLayout" />
        <tags-view v-if="needTagsView" />
      </div>

      <!-- 主内容 -->
      <app-main />

      <!-- 设置面板 -->
      <settings ref="settingRef" />
    </div>

    <!-- 全局水印 - 独立渲染，不包裹内容 -->
    <AWatermark :visible="watermarkVisible" :content="watermarkContentText" />
  </div>
</template>

<script setup lang="ts" name="Layout">
import Sidebar from './components/Sidebar/Sidebar.vue'
import DualSidebar from './components/Sidebar/DualSidebar.vue'
import AppMain from './components/AppMain/AppMain.vue'
import Navbar from './components/Navbar/Navbar.vue'
import Settings from './components/Settings/Settings.vue'
import TagsView from './components/TagsView/TagsView.vue'
import AWatermark from '@/components/ATheme/AWatermark.vue'
import { SystemConfig, MenuLayoutMode } from '@/systemConfig'
import { useLayout } from '@/composables/useLayout'

// 获取应用布局状态
const layout = useLayout()

// 响应式状态
const theme = layout.theme
const sidebar = layout.sidebar
const device = layout.device
const needTagsView = layout.tagsView
const fixedHeader = layout.fixedHeader
const menuLayout = layout.menuLayout

// 水印配置
const watermarkVisible = layout.watermark
const userStore = useUserStore()
const watermarkContentText = computed(() => {
  // 优先使用用户配置的水印内容
  const configContent = layout.watermarkContent.value

  // 如果配置了具体内容（非空字符串），使用配置的内容
  if (configContent && configContent.trim() !== '') {
    return configContent
  }

  // 如果配置为空，使用当前登录用户的用户名
  const userName = userStore.userInfo?.userName
  if (userName) {
    return userName
  }

  // 兜底：使用应用标题
  return SystemConfig.app.title || 'ruoyi-plus-uniapp'
})

// 计算是否显示侧边栏
const showSidebar = computed(() => {
  return menuLayout.value !== MenuLayoutMode.Horizontal && !sidebar.value.hide
})

// 计算侧边栏状态相关的class
// 双列模式下首页只显示图标列（子菜单栏收起），外层容器需收窄到图标列宽度
const route = useRoute()
const isDualHome = computed(() => menuLayout.value === MenuLayoutMode.DualColumn && route.path === '/index')

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile',
  horizontalLayout: menuLayout.value === MenuLayoutMode.Horizontal,
  dualLayout: menuLayout.value === MenuLayoutMode.DualColumn,
  dualHome: isDualHome.value
}))

// 响应式处理窗口大小变化
const { width } = useWindowSize()
const WIDTH = 992 // 响应式断点

watchEffect(() => {
  // 根据窗口宽度切换设备类型和侧边栏状态
  if (device.value === 'mobile') {
    layout.closeSideBar()
  }

  if (width.value - 1 < WIDTH) {
    layout.toggleDevice('mobile')
    layout.closeSideBar()
  } else {
    layout.toggleDevice('pc')
    layout.openSideBar()
  }
})

// 组件引用
const navbarRef = ref<InstanceType<typeof Navbar>>()
const settingRef = ref<InstanceType<typeof Settings>>()

// 组件挂载时初始化
onMounted(() => {
  // 建立实时通信连接
  webSocket.initialize()
  webSocket.connect()

  useSSE(SystemConfig.api.baseUrl + '/resource/sse')
})

// 处理移动端点击侧边栏外区域关闭侧边栏
const handleClickOutside = () => {
  layout.closeSideBar()
}

// 打开设置面板
const setLayout = () => {
  settingRef.value?.openSetting()
}
</script>
