<!-- 顶部导航栏 -->
<template>
  <div class="h-50px w-full flex items-center justify-between navbar-border">
    <!-- 左侧区域：Logo(水平模式) / 汉堡菜单 + 面包屑/TopNav -->
    <div class="h-full flex items-center navbar-left">
      <!-- 水平模式下显示Logo -->
      <div v-if="isHorizontalLayout" class="flex items-center h-full navbar-logo">
        <Logo v-if="layout.sidebarLogo.value" :collapse="width < 768" />
        <RefreshButton />
      </div>

      <!-- 非水平模式下显示汉堡菜单和刷新按钮 -->
      <div v-else class="flex items-center h-full ml-2">
        <Hamburger :is-active="layout.sidebar.value.opened" @toggle-click="toggleSideBar" />
        <RefreshButton />
      </div>

      <!-- 根据模式显示面包屑或顶部导航 -->
      <div class="navbar-content flex-1 min-w-0">
        <TopNav v-if="layout.topNav.value" />
        <div v-else class="h-full flex items-center">
          <Breadcrumb />
        </div>
      </div>
    </div>

    <!-- 右侧工具栏 - 始终在右侧 -->
    <div class="h-full flex items-center navbar-tools">
      <template v-if="layout.device.value !== 'mobile'">
        <!-- 租户选择组件 - 只在大屏幕显示 -->
        <TenantSelect v-if="width > 1200" @tenant-change="onTenantChange" />

        <!-- 搜索菜单 - 中等屏幕以上显示 -->
        <NavbarSearch v-if="width > 768" />

        <!-- 全屏切换 - 大屏幕显示 -->
        <FullscreenToggle v-if="width > 1024" />

        <!-- 消息通知 - 中等屏幕以上显示 -->
        <Notice v-if="width > 768" />

        <!-- AI 聊天助手 - 中等屏幕以上显示 -->
        <AiChat v-if="width > 768" />

        <!-- 语言选择 - 大屏幕显示 -->
        <LangSelect v-if="width > 1024" />

        <!-- 布局大小设置 - 超大屏幕显示 -->
        <!--        <SizeSelect v-if="width > 1200" />-->

        <!-- 布局设置 - 始终显示 -->
        <LayoutSetting @set-layout="setLayout" />
      </template>

      <!-- 移动端简化工具栏 -->
      <template v-else>
        <LayoutSetting @set-layout="setLayout" />
      </template>

      <!-- 用户头像和下拉菜单 - 始终显示 -->
      <UserDropdown :is-dynamic-tenant="isDynamicTenant" />
    </div>
  </div>
</template>

<script setup lang="ts">
// 左侧组件
import Hamburger from './Hamburger.vue'
import Logo from '../Sidebar/Logo.vue'
import RefreshButton from './tools/RefreshButton.vue'
import TopNav from './TopNav.vue'
import Breadcrumb from './Breadcrumb.vue'
import NavbarSearch from './tools/NavbarSearch.vue'
import { MenuLayoutMode, SideTheme } from '@/systemConfig'

// 右侧工具栏组件 - 按功能逻辑顺序排列
// 核心功能
import TenantSelect from './tools/TenantSelect.vue'
// 通知功能
import Notice from './tools/Notice.vue'
// AI 聊天功能
import AiChat from './tools/AiChat.vue'
// 显示偏好设置
import FullscreenToggle from './tools/FullscreenToggle.vue'
import LangSelect from './tools/LangSelect.vue'
import SizeSelect from './tools/SizeSelect.vue'
// 系统设置
import LayoutSetting from './tools/LayoutSetting.vue'
// 用户相关
import UserDropdown from './tools/UserDropdown.vue'

// 获取布局状态管理
const layout = useLayout()
const { width } = useWindowSize()

// 是否处于动态租户模式
const isDynamicTenant = ref(false)

// 计算属性：是否为水平布局模式
const isHorizontalLayout = computed(() => {
  return layout.menuLayout.value === MenuLayoutMode.Horizontal
})

// 切换侧边栏
const toggleSideBar = () => {
  layout.toggleSideBar(false)
}

// 租户变化事件处理
const onTenantChange = (dynamic: boolean) => {
  isDynamicTenant.value = dynamic
}

// 设置布局事件处理
const emits = defineEmits(['setLayout'])
const setLayout = () => {
  emits('setLayout')
}

// 监听布局模式变化，水平模式下自动切换为浅色菜单
watch(
  () => layout.menuLayout.value,
  (newLayout) => {
    if (newLayout === MenuLayoutMode.Horizontal) {
      // 水平模式下强制切换为浅色菜单主题
      layout.sideTheme.value = SideTheme.Light
    }
  },
  { immediate: false }
)
</script>

<style lang="scss" scoped>
/* Navbar整体布局 */
.navbar-border {
  /* 确保在小屏幕下不会溢出 */
  overflow: hidden;
}

/* 左侧区域样式 */
.navbar-left {
  /* 占用剩余空间，但不会超出容器 */
  flex: 1;
  min-width: 0; /* 允许缩小 */
  max-width: calc(100% - 200px); /* 为右侧工具栏留出最小空间 */
}

/* 右侧工具栏样式 */
.navbar-tools {
  /* 固定宽度，避免挤压左侧 */
  flex-shrink: 0;

  /* 用户头像组件需要更大的左边距 */
  > *:last-child {
    margin-left: 12px;
  }
}

/* 导航内容区域 */
.navbar-content {
  /* 确保内容不会溢出 */
  overflow: hidden;

  /* TopNav 和 Breadcrumb 的容器样式 */
  :deep(.el-menu) {
    /* 确保水平菜单不会溢出 */
    overflow: hidden;
  }
}

/* 水平布局模式下的Logo样式调整 */
.navbar-logo {
  /* 固定左侧区域宽度，避免挤压菜单 */
  flex-shrink: 0;

  :deep(.sidebar-logo-container) {
    /* 移除背景色，让Logo适应navbar背景 */
    background: transparent;
    /* 调整内边距，适应navbar高度 */
    height: 50px;
    line-height: 50px;

    .sidebar-logo-link {
      /* 适当调整左边距 */
      padding-left: 16px;
    }

    .sidebar-title {
      /* 确保文字颜色在navbar中清晰可见 */
      color: var(--el-text-color-primary);
    }
  }
}

/* 响应式样式 */
@media (max-width: 768px) {
  .navbar-left {
    max-width: calc(100% - 120px); /* 小屏幕下为右侧留出更少空间 */
  }

  .navbar-logo {
    :deep(.sidebar-logo-container) {
      .sidebar-logo-link {
        padding-left: 8px; /* 小屏幕下减少左边距 */
      }

      .expanded-title {
        margin-left: 6px; /* 减少图标和文字间距 */
      }
    }
  }
}

@media (max-width: 480px) {
  .navbar-left {
    max-width: calc(100% - 80px); /* 超小屏幕下进一步压缩 */
  }

  .navbar-tools {
    /* 超小屏幕下工具项间距更紧凑 */
    > * {
      margin-left: 2px;
    }
  }
}
</style>
