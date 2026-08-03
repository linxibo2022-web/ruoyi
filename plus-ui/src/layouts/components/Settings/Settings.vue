<!-- 系统设置面板 -->
<template>
  <!-- 设置抽屉：右侧滑出的配置面板 -->
  <el-drawer
    v-model="isDrawerVisible"
    :with-header="true"
    direction="rtl"
    size="300px"
    header-class="mb-0!"
    modal-class="bg-transparent!"
    close-on-click-modal
  >
    <template #header="{ titleId }">
      <div>
        <el-button link @click="handleResetSettings">
          <el-icon :id="titleId"> <Refresh class="color-[--el-color-primary]" /> </el-icon>{{ t('Reset Settings', '重置配置') }}
        </el-button>
      </div>
    </template>

    <el-divider content-position="center">{{ t('Theme Style', '主题风格') }}</el-divider>
    <div class="grid grid-cols-2 gap-x-1 text-center">
      <div>
        <img
          src="@/assets/images/settings/light.png"
          alt="dark"
          class="w-30 h-18 rounded-[--radius-md] cursor-pointer"
          :class="{ 'border-3px! border-[--el-color-primary]! border-solid!': !layout.dark.value }"
          @click="handleDarkModeToggle($event, false)"
        />
        <div>{{ t('Light', '浅色') }}</div>
      </div>
      <div>
        <img
          src="@/assets/images/settings/dark.png"
          alt="dark"
          class="w-30 h-18 rounded-[--radius-md] cursor-pointer"
          :class="{ 'border-3px! border-[--el-color-primary]! border-solid!': layout.dark.value }"
          @click="handleDarkModeToggle($event, true)"
        />
        <div>{{ t('Dark', '深色') }}</div>
      </div>
    </div>

    <el-divider content-position="center">{{ t('Menu Layout', '菜单布局') }}</el-divider>
    <div class="grid grid-cols-2 gap-x-1 gap-y-2 text-center justify-items-center">
      <div>
        <img
          src="@/assets/images/settings/menu-layout-vertical.png"
          alt="vertical"
          class="w-20 h-12 rounded-[--radius-md] cursor-pointer"
          :class="{ 'border-3px! border-[--el-color-primary]! border-solid!': layout.menuLayout.value === MenuLayoutMode.Vertical }"
          @click="handleMenuLayoutChange(MenuLayoutMode.Vertical)"
        />
        <div class="text-xs mt-1">{{ t('Vertical', '垂直') }}</div>
      </div>
      <div>
        <img
          src="@/assets/images/settings/menu-layout-horizontal.png"
          alt="horizontal"
          class="w-20 h-12 rounded-[--radius-md] cursor-pointer"
          :class="{ 'border-3px! border-[--el-color-primary]! border-solid!': layout.menuLayout.value === MenuLayoutMode.Horizontal }"
          @click="handleMenuLayoutChange(MenuLayoutMode.Horizontal)"
        />
        <div class="text-xs mt-1">{{ t('Horizontal', '水平') }}</div>
      </div>
      <div>
        <img
          src="@/assets/images/settings/menu-layout-mixed.png"
          alt="mixed"
          class="w-20 h-12 rounded-[--radius-md] cursor-pointer"
          :class="{ 'border-3px! border-[--el-color-primary]! border-solid!': layout.menuLayout.value === MenuLayoutMode.Mixed }"
          @click="handleMenuLayoutChange(MenuLayoutMode.Mixed)"
        />
        <div class="text-xs mt-1">{{ t('Mixed', '混合') }}</div>
      </div>
      <div>
        <!-- 双列布局：CSS 线框缩略示意（无 png 资源；风格对齐其它三个：灰底卡片 + 白栏 + 灰菜单条） -->
        <div
          class="dual-thumb w-20 h-12 rounded-[--radius-md] cursor-pointer"
          :class="{ 'border-3px! border-[--el-color-primary]! border-solid!': layout.menuLayout.value === MenuLayoutMode.DualColumn }"
          @click="handleMenuLayoutChange(MenuLayoutMode.DualColumn)"
        >
          <!-- 左图标列面板 -->
          <div class="dt-rail">
            <i></i><i></i><i></i><i></i>
          </div>
          <!-- 子菜单列面板 -->
          <div class="dt-sub">
            <i></i><i></i><i></i><i></i>
          </div>
          <!-- 内容区 -->
          <div class="dt-main">
            <i></i><i></i><i></i>
          </div>
        </div>
        <div class="text-xs mt-1">{{ t('Dual', '双列') }}</div>
      </div>
    </div>

    <el-divider content-position="center">{{ t('Menu Style', '菜单风格') }}</el-divider>
    <div class="grid grid-cols-2 gap-x-1 text-center">
      <div>
        <img
          src="@/assets/images/settings/menu-sidebar-light.png"
          alt="dark"
          class="w-30 h-18 rounded-[--radius-md]"
          :class="{
            'border-3px! border-[--el-color-primary]! border-solid!': layout.sideTheme.value === SideTheme.Light,
            'opacity-50 cursor-not-allowed': isMenuStyleDisabled,
            'cursor-pointer': !isMenuStyleDisabled
          }"
          @click="!isMenuStyleDisabled && handleSideThemeSelect(SideTheme.Light)"
        />
        <div :class="{ 'opacity-50': isMenuStyleDisabled }">{{ t('Light', '浅色') }}</div>
      </div>
      <div>
        <img
          src="@/assets/images/settings/menu-sidebar-dark.png"
          alt="dark"
          class="w-30 h-18 rounded-[--radius-md]"
          :class="{
            'border-3px! border-[--el-color-primary]! border-solid!': layout.sideTheme.value === SideTheme.Dark,
            'opacity-50 cursor-not-allowed': isMenuStyleDisabled,
            'cursor-pointer': !isMenuStyleDisabled
          }"
          @click="!isMenuStyleDisabled && handleSideThemeSelect(SideTheme.Dark)"
        />
        <div :class="{ 'opacity-50': isMenuStyleDisabled }">{{ t('Dark', '深色') }}</div>
      </div>
    </div>

    <el-divider content-position="center">{{ t('Theme Color', '系统主题颜色') }}</el-divider>
    <div class="flex justify-between items-center">
      <div
        v-for="(item, index) in PREDEFINED_THEME_COLORS"
        :key="index"
        class="w-7 h-7 rounded-full cursor-pointer transition-transform hover:scale-110"
        :style="{ backgroundColor: item }"
        :class="{ 'ring-2 ring-[--el-color-primary] ring-offset-1': currentTheme === item }"
        @click="handleThemeColorChange(item)"
      ></div>
    </div>

    <el-divider content-position="center">{{ t('Settings', '基础配置') }}</el-divider>

    <!-- 标签视图开关 -->
    <div class="py-3 text-14px flex justify-between items-center">
      <span>{{ t('Enable Tags-Views', '开启 Tags-Views') }}</span>
      <span class="float-right -mt-0.75 mr-2">
        <el-switch v-model="layout.tagsView.value" />
      </span>
    </div>

    <!-- 固定头部开关 -->
    <div class="py-3 text-14px flex justify-between items-center">
      <span>{{ t('Fixed Header', '固定 Header') }}</span>
      <span class="float-right -mt-0.75 mr-2">
        <el-switch v-model="layout.fixedHeader.value" />
      </span>
    </div>

    <!-- 显示Logo开关 -->
    <div class="py-3 text-14px flex justify-between items-center">
      <span>{{ t('Show Logo', '显示 Logo') }}</span>
      <span class="float-right -mt-0.75 mr-2">
        <el-switch v-model="layout.sidebarLogo.value" />
      </span>
    </div>

    <!-- 动态标题开关 -->
    <div class="py-3 text-14px flex justify-between items-center">
      <span>{{ t('Dynamic Title', '动态标题') }}</span>
      <span class="float-right -mt-0.75 mr-2">
        <el-switch v-model="layout.dynamicTitle.value" />
      </span>
    </div>

    <!-- 选择器显示值开关 -->
    <div class="py-3 text-14px flex justify-between items-center">
      <span>{{ t('Show Select Value', '选择器显示选项值') }}</span>
      <span class="float-right -mt-0.75 mr-2">
        <el-switch v-model="showSelectValueSwitch" />
      </span>
    </div>

    <el-divider content-position="center">{{ t('Watermark', '水印配置') }}</el-divider>

    <!-- 水印开关 -->
    <div class="py-3 text-14px flex justify-between items-center">
      <span>{{ t('Show Watermark', '显示水印') }}</span>
      <span class="float-right -mt-0.75 mr-2">
        <el-switch v-model="layout.watermark.value" :disabled="isWatermarkSwitchDisabled" />
      </span>
    </div>

    <!-- 水印内容 -->
    <div v-if="layout.watermark.value" class="py-3 text-14px">
      <div class="mb-2">{{ t('Watermark Content', '水印内容') }}</div>
      <el-input
        v-model="layout.watermarkContent.value"
        :placeholder="t('Enter watermark content (leave empty to show username)', '请输入水印内容（留空则显示用户名）')"
        clearable
        :disabled="isWatermarkContentDisabled"
      />
    </div>
  </el-drawer>
</template>

<script setup lang="ts" name="Settings">
// ====================== 常量定义 ======================
import { SideTheme, MenuLayoutMode, PREDEFINED_THEME_COLORS } from '@/systemConfig'
import { showLoading, hideLoading } from '@/utils/modal'
import { Refresh } from '@element-plus/icons-vue'
import { toggleThemeWithAnimation } from '@/utils/themeAnimation'

// ====================== 状态管理 ======================
const { t } = useI18n()
const layout = useLayout()
const permissionStore = usePermissionStore()
const { currentTheme, setTheme, addAlphaToHex } = useTheme()

// ====================== 组件状态 ======================
/** 抽屉显示状态 */
const isDrawerVisible = ref(false)

/** 用户选择的侧边栏主题（用于深色模式恢复） */
const userSelectedSideTheme = ref(layout.sideTheme.value)

/** 当前侧边栏主题 */
const currentSideTheme = layout.sideTheme

// ====================== 计算属性 ======================
/** 是否禁用菜单风格选择（水平布局模式下禁用） */
const isMenuStyleDisabled = computed(() => {
  return layout.menuLayout.value === MenuLayoutMode.Horizontal
})

/**
 * 选择器显示值开关的计算属性
 * @description el-switch 不支持 undefined，需要转换为 boolean
 * 如果用户是超管且值为 undefined，默认显示为 true
 */
const showSelectValueSwitch = computed({
  get() {
    const value = layout.showSelectValue.value
    // 如果有明确设置，返回该值
    if (value !== undefined) {
      return value
    }
    // 否则根据角色判断（超管默认为 true）
    const userStore = useUserStore()
    const userRoles = userStore.roles
    if (userRoles && userRoles.length > 0) {
      return userRoles.includes('superadmin') || userRoles.includes('admin')
    }
    return false
  },
  set(value: boolean) {
    layout.showSelectValue.value = value
  }
})

/**
 * 水印开关是否禁用
 * @description 可以根据业务需求扩展禁用条件，例如根据权限、环境等
 */
const isWatermarkSwitchDisabled = computed(() => {
  // 这里可以添加禁用逻辑，例如：
  // - 如果是演示环境，禁用水印开关
  // - 如果用户没有修改水印权限，禁用开关
  // 目前默认不禁用
  return false
})

/**
 * 水印输入框是否禁用
 * @description 当水印开关被禁用时，输入框也应该被禁用
 */
const isWatermarkContentDisabled = computed(() => {
  return isWatermarkSwitchDisabled.value
})

// ====================== 主题管理相关方法 ======================
/**
 * 处理侧边栏主题选择
 * @param themeType - 选择的主题类型
 */
const handleSideThemeSelect = (themeType: SideTheme) => {
  // 记录用户的选择偏好
  userSelectedSideTheme.value = themeType

  // 深色模式下不允许切换到浅色侧边栏
  if (layout.dark.value && themeType === SideTheme.Light) {
    return
  }

  // 应用选择的主题
  layout.sideTheme.value = themeType
}

/**
 * 处理主题颜色变更
 * @param color - 新的主题颜色
 */
const handleThemeColorChange = (color: string) => {
  setTheme(color)
}

/**
 * 处理深色模式切换 (带圆形扩散动画)
 * @param event - 鼠标点击事件
 * @param isDark - 是否开启深色模式
 */
const handleDarkModeToggle = (event: MouseEvent, isDark: boolean) => {
  // 如果当前模式已经是目标模式,则不执行切换
  if (layout.dark.value === isDark) {
    return
  }

  // 使用动画切换
  toggleThemeWithAnimation(event, layout.dark.value)
}

// ====================== 布局配置相关方法 ======================
/**
 * 处理菜单布局模式切换
 * @param mode - 选择的布局模式
 */
const handleMenuLayoutChange = (mode: MenuLayoutMode) => {
  // 更新菜单布局模式
  layout.menuLayout.value = mode

  // 根据布局模式设置相应的状态
  switch (mode) {
    case MenuLayoutMode.Vertical:
      // 垂直布局：关闭顶部导航，显示侧边栏
      layout.topNav.value = false
      layout.toggleSideBarHide(false)
      // 恢复完整的侧边栏路由
      permissionStore.setSidebarRouters(permissionStore.defaultRoutes as any)
      break

    case MenuLayoutMode.Mixed:
      // 混合布局：开启顶部导航，显示侧边栏
      layout.topNav.value = true
      layout.toggleSideBarHide(false)
      // 侧边栏将根据选中的顶级菜单动态显示子菜单
      break

    case MenuLayoutMode.DualColumn:
      // 双列布局：关闭顶部导航，显示侧边栏（左图标列 + 右子菜单列）
      layout.topNav.value = false
      layout.toggleSideBarHide(false)
      // 复位为完整侧边栏路由：从混合/水平切回时保证数据干净（双列左列读 topbarRoutes 不受影响）
      permissionStore.setSidebarRouters(permissionStore.defaultRoutes as any)
      break

    case MenuLayoutMode.Horizontal:
      // 水平布局：开启顶部导航，隐藏侧边栏
      layout.topNav.value = true
      layout.toggleSideBarHide(true)
      // 确保侧边栏路由数据包含完整的菜单结构，供 TopNav 使用
      const fullRoutes = permissionStore.getDefaultRoutes()
      if (fullRoutes && fullRoutes.length > 0) {
        permissionStore.setSidebarRouters(fullRoutes)
      }
      break
  }

  // 在深色模式下切换菜单布局时，强制触发侧边栏主题更新
  // 这样可以确保侧边栏背景色正确应用
  if (layout.dark.value) {
    nextTick(() => {
      const currentTheme = layout.sideTheme.value
      layout.sideTheme.value = currentTheme === SideTheme.Dark ? SideTheme.Light : SideTheme.Dark
      nextTick(() => {
        layout.sideTheme.value = SideTheme.Dark
      })
    })
  }
}

/**
 * 处理顶部导航切换（保留兼容性）
 * @param isEnabled - 是否启用顶部导航
 */
const handleTopNavToggle = (isEnabled: boolean) => {
  // 根据topNav状态设置相应的布局模式
  const mode = isEnabled ? MenuLayoutMode.Mixed : MenuLayoutMode.Vertical
  handleMenuLayoutChange(mode)
}

// ====================== 配置管理相关方法 ======================
/**
 * 重置所有配置到默认状态
 */
const handleResetSettings = async () => {
  showLoading(t('Clearing settings cache and refreshing, please wait...', '正在清除设置缓存并刷新，请稍候...'))

  try {
    layout.resetConfig()
    await delay(1000)

    // 刷新页面以应用默认配置
    window.location.reload()
  } catch (error) {
    console.error('重置配置失败:', error)
    hideLoading()
  }
}

// ====================== 工具函数 ======================
/**
 * 延迟执行函数
 * @param ms - 延迟毫秒数
 */
const delay = (ms: number): Promise<void> => {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

// ====================== 抽屉控制方法 ======================
/**
 * 打开设置抽屉
 */
const openSettingsDrawer = () => {
  isDrawerVisible.value = true
}

/**
 * 关闭设置抽屉
 */
const closeSettingsDrawer = () => {
  isDrawerVisible.value = false
}

// ====================== 响应式监听 ======================
/**
 * 监听深色模式变化，自动调整侧边栏主题
 */
watch(
  () => layout.dark.value,
  (isDarkMode) => {
    if (isDarkMode) {
      // 开启深色模式：强制使用深色侧边栏
      layout.sideTheme.value = SideTheme.Dark
    } else {
      // 关闭深色模式：恢复用户选择的主题
      layout.sideTheme.value = userSelectedSideTheme.value
    }
  },
  { immediate: true }
)

/*
 * 监听侧边栏主题和主主题颜色变化，动态更新菜单激活颜色
 */
watch(
  [() => layout.sideTheme.value, () => layout.theme.value],
  ([newSideTheme, newTheme]) => {
    /* 设置自定义活跃背景颜色、文字 */
    document.documentElement.style.setProperty('--custom-active-bg-color', addAlphaToHex(newTheme, 0.1))
    document.documentElement.style.setProperty('--custom-active-text-color', newTheme)

    /* 菜单激活颜色设置 */
    if (newSideTheme === SideTheme.Light) {
      document.documentElement.style.setProperty('--el-menu-active-bg-color', addAlphaToHex(newTheme, 0.1))
      document.documentElement.style.setProperty('--el-menu-active-text-color', newTheme)
    } else if (newSideTheme === SideTheme.Dark) {
      document.documentElement.style.setProperty('--el-menu-active-bg-color', newTheme)
      document.documentElement.style.setProperty('--el-menu-active-text-color', '#fff') // 深色模式下文字使用白色
    }
  },
  {
    immediate: true
  }
)

// ====================== 组件对外接口 ======================
defineExpose({
  openSetting: openSettingsDrawer,
  closeSetting: closeSettingsDrawer,
  isVisible: readonly(isDrawerVisible)
})
</script>

<style lang="scss" scoped>
/* 双列布局预览缩略图（CSS 线框，严格对齐其它三张 png 的设计语言：
   白底卡片 + 圆角灰色面板（侧栏/子菜单）+ 面板内白色小条 + 右侧浅灰内容行） */
.dual-thumb {
  display: flex;
  align-items: stretch;
  gap: 4px;
  padding: 7px;
  overflow: hidden;
  /* 近白卡片背景，与兄弟缩略图一致 */
  background-color: #f4f5f7;

  /* 左：图标列面板（窄，灰色圆角面板 + 内含白色小条） */
  .dt-rail {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    gap: 3px;
    width: 9px;
    padding: 4px 0;
    border-radius: 3px;
    background-color: #dfe1e6;

    i {
      width: 5px;
      height: 2.5px;
      border-radius: 1px;
      background-color: #fff;
    }
  }

  /* 中：子菜单列面板（稍宽，同款灰色圆角面板 + 内含白色条） */
  .dt-sub {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 3px;
    width: 17px;
    padding: 4px 3px;
    border-radius: 3px;
    background-color: #dfe1e6;

    i {
      width: 100%;
      height: 2.5px;
      border-radius: 1px;
      background-color: #fff;
    }
  }

  /* 右：内容区（浅灰内容行） */
  .dt-main {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 4px;
    flex: 1;
    padding: 3px 0;

    i {
      width: 100%;
      height: 4px;
      border-radius: 2px;
      background-color: #ebedf0;
    }
  }
}
</style>
