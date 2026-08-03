<!-- 双列侧边栏（左图标列 + 右子菜单列） -->
<template>
  <div class="dual-sidebar h-full" :class="[currentSideTheme, { 'rail-only': railOnly }]" :style="dualStyles">
    <!-- 顶部 Logo 区（保留，与其它布局一致；只剩图标列时收为图标） -->
    <Logo v-if="isLogoVisible" :collapse="railOnly" class="dual-logo" />

    <!-- 主体：左图标列 + 右子菜单列 -->
    <div class="dual-body">
      <!-- 左：一级模块图标列 -->
      <div class="dual-rail">
        <el-scrollbar class="h-full">
          <!-- 首页入口（非菜单模块，固定置顶；选中它时右侧子菜单栏收起） -->
          <div class="rail-item" :class="{ 'is-active': currentTop === HOME_PATH }" :title="homeTitle" @click="goHome">
            <Icon size="22px" class="rail-icon" :code="HOME_ICON" />
            <span class="rail-text">{{ homeTitle }}</span>
          </div>
          <div
            v-for="(mod, index) in topModules"
            :key="mod.path + index"
            class="rail-item"
            :class="{ 'is-active': currentTop === mod.path }"
            :title="getLocalizedMenuTitle(mod)"
            @click="handleSelectModule(mod)"
          >
            <Icon size="22px" class="rail-icon" :code="mod.meta ? (mod.meta.icon as IconCode) : null" />
            <span class="rail-text">{{ getLocalizedMenuTitle(mod) }}</span>
          </div>
        </el-scrollbar>
      </div>

      <!-- 右：选中模块的子菜单列 -->
      <div class="dual-sub">
        <el-scrollbar class="scrollbar-wrapper">
          <el-menu
            :key="currentTop"
            :default-active="currentActiveMenu"
            :background-color="menuBackgroundColor"
            :text-color="menuTextColor"
            :active-text-color="currentThemeColor"
            :unique-opened="true"
            :collapse="false"
            :collapse-transition="false"
            mode="vertical"
            class="h-full w-full border-none"
          >
            <template v-if="subRoutes.length > 0">
              <!-- base-path 传「模块绝对路径 + 子段」，保证叶子 el-menu-item 的 index 为绝对路径，与 route.path 匹配才能高亮 -->
              <SidebarItem v-for="(route, index) in subRoutes" :key="route.path + index" :item="route" :base-path="joinPath(currentTop, route.path)" />
            </template>
            <!-- 选中模块无子菜单时的占位提示 -->
            <div v-else class="dual-empty">{{ t('No submenu', '暂无子菜单') }}</div>
          </el-menu>
      </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="DualSidebar">
import Logo from './Logo.vue'
import SidebarItem from './SidebarItem.vue'
import variables from '@/assets/styles/abstracts/exports.module.scss'
import { type RouteRecordRaw } from 'vue-router'
import { SideTheme } from '@/systemConfig'

// ============== 依赖 ==============
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const layout = useLayout()

// 需要从图标列排除的系统/兜底路由
const EXCLUDE_PATHS = ['/redirect', '/login', '/register', '/401', '/user', '/index', '/']

// 首页（非菜单模块，固定置顶入口）
const HOME_PATH = '/index'
const HOME_ICON = 'home3' as IconCode // 与 constant.ts 首页路由 meta.icon 一致
const homeTitle = computed(() => t('menu.index') !== 'menu.index' ? t('menu.index') : t('Home', '首页'))

// ============== 一级模块（左列） ==============
/**
 * 左侧图标列的数据源：完整一级模块树（与混合模式同源，稳定不受 setSidebarRouters 影响）
 */
const topModules = computed<RouteRecordRaw[]>(() =>
  permissionStore.getTopbarRoutes().filter((m: any) => m.hidden !== true && !EXCLUDE_PATHS.includes(m.path))
)

/** 是否首页 */
const isHome = computed<boolean>(() => route.path === HOME_PATH)

/**
 * 当前生效的一级模块 path —— 完全由路由反推（取路径第一段拼成 /xxx）
 * 采用「路由驱动」模型：选中态永远等于当前所在模块，不引入手动预览状态，
 * 从根本上避免"选中后移开又丢失选中"的分叉问题。
 * 首页高亮首页入口；其它非模块页不做兜底高亮（避免误点亮第一个模块）。
 */
const currentTop = computed<string>(() => {
  if (isHome.value) return HOME_PATH
  const seg = route.path.split('/').filter(Boolean)[0]
  const guess = seg ? '/' + seg : ''
  if (topModules.value.some((m) => m.path === guess)) return guess
  return ''
})

/** 当前模块对象（首页/无匹配时为 undefined） */
const currentModule = computed<RouteRecordRaw | undefined>(() => topModules.value.find((m) => m.path === currentTop.value))

/** 右列子菜单：当前模块的可见 children */
const subRoutes = computed<RouteRecordRaw[]>(() => (currentModule.value?.children ?? []).filter((c: any) => c.hidden !== true))

// ============== 菜单激活状态 ==============
const currentActiveMenu = computed<string>(() => {
  const { meta, path } = route
  if (meta.activeMenu) return meta.activeMenu as string
  return path
})

// ============== 交互 ==============
/** 是否外部链接 */
const isHttpLink = (p?: string): boolean => /^https?:\/\//.test(p || '')

/** 拼接完整路径（处理绝对路径 / 外链） */
const joinPath = (base: string, seg: string): string => {
  if (!seg) return base
  if (isHttpLink(seg) || seg.startsWith('/')) return seg
  return `${base.replace(/\/$/, '')}/${seg}`
}

/** 递归查找模块下第一个可导航叶子的完整路径 */
const findFirstLeafPath = (node: RouteRecordRaw, basePath: string): string => {
  const full = joinPath(basePath, node.path)
  const kids = (node.children ?? []).filter((c: any) => c.hidden !== true)
  if (kids.length === 0) return full
  return findFirstLeafPath(kids[0], full)
}

/**
 * 点击左侧一级模块：一律导航进该模块的「第一个子菜单」（首个可导航叶子）
 * 让选中态跟随路由，一次点击到位；天然实现"点主菜单默认选中第一个子菜单"
 */
const handleSelectModule = (mod: RouteRecordRaw): void => {
  const target = findFirstLeafPath(mod, '')
  if (isHttpLink(target)) {
    window.open(target, '_blank')
    return
  }
  // 已在目标页则不重复跳转（避免 NavigationDuplicated），其余情况直接跳
  if (target !== route.path) {
    router.push(target)
  }
}

/** 点击首页入口 */
const goHome = (): void => {
  if (route.path !== HOME_PATH) {
    router.push(HOME_PATH)
  }
}

// ============== 国际化标题（与 SidebarItem 一致的逻辑） ==============
const nameToTitle = (name?: string) => {
  if (!name) return ''
  const cleanName = name.replace(/\d+$/, '')
  return cleanName.charAt(0).toUpperCase() + cleanName.slice(1)
}

const getLocalizedMenuTitle = (item: any): string => {
  const meta = item?.meta
  const name = item?.name
  if (!meta && !name) return ''
  if (meta?.i18nKey) {
    const translated = t(meta.i18nKey)
    if (translated !== meta.i18nKey) return translated
  }
  return t(nameToTitle(name), meta?.title)
}

// ============== 主题配色（复用 Sidebar 逻辑） ==============
const currentSideTheme = layout.sideTheme
const currentThemeColor = layout.theme
const isLogoVisible = layout.sidebarLogo

/** 侧边栏是否展开（折叠时仅显示左侧图标列） */
const sidebarOpened = computed(() => layout.sidebar.value.opened)

/**
 * 是否只显示图标列（收起右侧子菜单栏）：
 * 折叠态 / 首页 / 当前无可展示子菜单模块 时成立
 */
const railOnly = computed<boolean>(() => !sidebarOpened.value || isHome.value || !currentModule.value)

const menuBackgroundColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuBackground : variables.menuLightBackground))
const menuTextColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuColor : variables.menuLightColor))
const menuHoverColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuHover : variables.menuLightHover))
const menuHoverTextColor = computed(() => (currentSideTheme.value === SideTheme.Dark ? variables.menuHoverText : variables.menuLightHoverText))

/**
 * 根容器样式：背景 + 通过 CSS 变量下发菜单文字色（供左图标列/表头复用，保证与右列 el-menu 一致）
 */
const dualStyles = computed(() => ({
  backgroundColor: menuBackgroundColor.value,
  '--dual-text-color': menuTextColor.value
}))

/**
 * 复刻默认 Sidebar 的行为：把 hover 颜色提升为全局 CSS 变量
 * DualSidebar 会替代 Sidebar 渲染，必须自行维护这两个变量，否则右列 el-menu 悬停色失效
 */
watch(
  [menuHoverColor, menuHoverTextColor],
  ([hoverColor, hoverTextColor]) => {
    document.documentElement.style.setProperty('--menu-hover-color', hoverColor)
    document.documentElement.style.setProperty('--menu-hover-text-color', hoverTextColor)
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
/* 双列容器：顶部 Logo 区 + 主体（左图标列 + 右子菜单列） */
.dual-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 顶部 Logo 区（与其它布局的 logo 高度一致） */
.dual-logo {
  flex-shrink: 0;
  height: 50px;
}

/* 主体：左右两列，占据 Logo 之下的剩余高度 */
.dual-body {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

/* 左：一级模块图标列 */
.dual-rail {
  width: 74px;
  flex-shrink: 0;
  height: 100%;
  border-right: 1px solid rgba(0, 0, 0, 0.06);

  .rail-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 5px;
    margin: 6px;
    padding: 4px 2px;
    /* 正方形图标块（宽高相等，更规整） */
    aspect-ratio: 1 / 1;
    border-radius: 10px;
    cursor: pointer;
    /* 默认文字色对齐菜单主题文字色（深色侧栏=浅字，浅色侧栏=深字） */
    color: var(--dual-text-color);
    transition: all var(--duration-normal, 0.2s) ease;

    .rail-text {
      font-size: 12px;
      line-height: 1.1;
      text-align: center;
      max-width: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    /* 悬停：对齐默认菜单 hover 变量（由 watcher 维护） */
    &:hover:not(.is-active) {
      color: var(--menu-hover-text-color);
      background-color: var(--menu-hover-color);
    }

    /* 激活：对齐默认菜单 active 变量（Settings.vue 按深/浅色维护：深色=实心主题底+白字，浅色=浅蓝底+主题字） */
    &.is-active {
      color: var(--el-menu-active-text-color);
      background-color: var(--el-menu-active-bg-color);
      font-weight: 600;
    }
  }
}

/* 右：子菜单列 */
.dual-sub {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;

  .scrollbar-wrapper {
    flex: 1;
    min-height: 0;
  }

  .dual-empty {
    padding: 24px 16px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    text-align: center;
  }
}

/* 折叠态：仅显示左侧图标列，隐藏右侧子菜单列 */
.dual-sidebar.rail-only {
  .dual-sub {
    display: none;
  }
}

/* 深色主题下左列分隔线适配 */
.dual-sidebar.theme-dark .dual-rail {
  border-right-color: rgba(255, 255, 255, 0.08);
}
</style>
