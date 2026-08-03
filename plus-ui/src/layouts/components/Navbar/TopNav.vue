<!-- 顶部菜单导航 -->
<template>
  <!-- 顶部水平导航菜单 -->
  <el-menu
    class="h-[50px]! overflow-hidden"
    :default-active="activeMenu"
    mode="horizontal"
    :ellipsis="false"
    @select="handleSelect"
    :style="{ backgroundColor: 'transparent', borderBottom: 'none' }"
  >
    <!-- 混合模式：只显示一级菜单 -->
    <template v-if="menuLayout === MenuLayoutMode.Mixed">
      <!-- 显示的顶部菜单项 -->
      <template v-for="(item, index) in topMenus">
        <el-menu-item v-if="index < visibleNumber" :key="index" :style="{ '--theme': theme }" :index="item.path">
          <Icon
            class="mr-1"
            v-if="item.meta && item.meta.icon && item.meta.icon !== ('#' as IconCode)"
            :code="item.meta ? (item.meta.icon as IconCode) : null"
          />
          {{ getLocalizedMenuTitle(item) }}
        </el-menu-item>
      </template>

      <!-- 超出数量的菜单项折叠到"更多菜单"中 -->
      <el-sub-menu
        v-if="topMenus.length > visibleNumber"
        :style="{ '--theme': theme }"
        index="more"
        class="more-menu-item"
        popper-class="topnav-more-menu-dropdown"
      >
        <template #title>{{ t('More Menus', '更多菜单') }}</template>
        <template v-for="(item, index) in topMenus">
          <el-menu-item v-if="index >= visibleNumber" :key="index" :index="item.path">
            <Icon class="mr-1 menu-item-icon" :code="item.meta ? (item.meta.icon as IconCode) : null" />
            {{ getLocalizedMenuTitle(item) }}
          </el-menu-item>
        </template>
      </el-sub-menu>
    </template>

    <!-- 水平模式：显示完整的菜单层级 -->
    <template v-else-if="menuLayout === MenuLayoutMode.Horizontal">
      <!-- 显示的水平菜单项 -->
      <template v-for="(item, index) in horizontalMenus" :key="item.fullPath || item.name">
        <template v-if="index < visibleNumber">
          <!-- 有子菜单的情况 -->
          <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.fullPath || item.name">
            <template #title>
              <Icon v-if="item.meta?.icon && item.meta.icon !== '#'" class="mr-1" :code="item.meta.icon" />
              <span>{{ getLocalizedMenuTitle(item) }}</span>
            </template>

            <template v-for="child in item.children" :key="child.fullPath || child.name">
              <!-- 二级子菜单 -->
              <el-sub-menu v-if="child.children && child.children.length > 0" :index="child.fullPath || child.name">
                <template #title>
                  <Icon v-if="child.meta?.icon && child.meta.icon !== '#'" class="mr-1" :code="child.meta.icon" />
                  <span>{{ getLocalizedMenuTitle(child) }}</span>
                </template>

                <el-menu-item
                  v-for="subchild in child.children"
                  :key="subchild.fullPath || subchild.name"
                  :index="subchild.fullPath || subchild.name"
                >
                  <Icon v-if="subchild.meta?.icon && subchild.meta.icon !== '#'" class="mr-1" :code="subchild.meta.icon" />
                  <span>{{ getLocalizedMenuTitle(subchild) }}</span>
                </el-menu-item>
              </el-sub-menu>

              <!-- 二级菜单项 -->
              <el-menu-item v-else :index="child.fullPath || child.name">
                <Icon v-if="child.meta?.icon && child.meta.icon !== '#'" class="mr-1" :code="child.meta.icon" />
                <span>{{ getLocalizedMenuTitle(child) }}</span>
              </el-menu-item>
            </template>
          </el-sub-menu>

          <!-- 无子菜单的情况 -->
          <el-menu-item v-else :index="item.fullPath || item.name">
            <Icon v-if="item.meta?.icon && item.meta.icon !== '#'" class="mr-1" :code="item.meta.icon" />
            <span>{{ getLocalizedMenuTitle(item) }}</span>
          </el-menu-item>
        </template>
      </template>

      <!-- 超出数量的菜单项折叠到"更多菜单"中 -->
      <el-sub-menu
        v-if="horizontalMenus.length > visibleNumber"
        index="more-horizontal"
        class="more-menu-item"
        popper-class="topnav-more-menu-dropdown"
      >
        <template #title>{{ t('More Menus', '更多菜单') }}</template>
        <template v-for="(item, index) in horizontalMenus" :key="`more-${item.fullPath || item.name}`">
          <template v-if="index >= visibleNumber">
            <!-- 有子菜单的情况 -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.fullPath || item.name">
              <template #title>
                <Icon v-if="item.meta?.icon && item.meta.icon !== '#'" class="mr-1" :code="item.meta.icon" />
                <span>{{ getLocalizedMenuTitle(item) }}</span>
              </template>

              <template v-for="child in item.children" :key="child.fullPath || child.name">
                <!-- 二级子菜单 -->
                <el-sub-menu v-if="child.children && child.children.length > 0" :index="child.fullPath || child.name">
                  <template #title>
                    <Icon v-if="child.meta?.icon && child.meta.icon !== '#'" class="mr-1" :code="child.meta.icon" />
                    <span>{{ getLocalizedMenuTitle(child) }}</span>
                  </template>

                  <el-menu-item
                    v-for="subchild in child.children"
                    :key="subchild.fullPath || subchild.name"
                    :index="subchild.fullPath || subchild.name"
                  >
                    <Icon v-if="subchild.meta?.icon && subchild.meta.icon !== '#'" class="mr-1" :code="subchild.meta.icon" />
                    <span>{{ getLocalizedMenuTitle(subchild) }}</span>
                  </el-menu-item>
                </el-sub-menu>

                <!-- 二级菜单项 -->
                <el-menu-item v-else :index="child.fullPath || child.name">
                  <Icon v-if="child.meta?.icon && child.meta.icon !== '#'" class="mr-1" :code="child.meta.icon" />
                  <span>{{ getLocalizedMenuTitle(child) }}</span>
                </el-menu-item>
              </template>
            </el-sub-menu>

            <!-- 无子菜单的情况 -->
            <el-menu-item v-else :index="item.fullPath || item.name">
              <Icon v-if="item.meta?.icon && item.meta.icon !== '#'" class="mr-1 menu-item-icon" :code="item.meta?.icon || ''" />
              <span>{{ getLocalizedMenuTitle(item) }}</span>
            </el-menu-item>
          </template>
        </template>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts" name="TopNav">
import { constantRoutes } from '@/router/router'
import { isHttp } from '@/utils/validators'
import { MenuLayoutMode } from '@/systemConfig'
import { type RouteRecordRaw } from 'vue-router'

/**
 * ===== 国际化 =====
 */
const { t } = useI18n()

/**
 * ===== 响应式数据定义 =====
 */

// 顶部菜单可视数量（响应式计算得出）
const visibleNumber = ref<number>(-1)

// 当前选中的菜单索引
const currentIndex = ref<string>()

// 需要隐藏侧边栏的路由列表
const hideList = ['/index', '/user/profile']

/**
 * ===== Store 和路由实例 =====
 */

const layout = useLayout()
const permissionStore = usePermissionStore()
const route = useRoute()
const router = useRouter()

/**
 * ===== 计算属性 =====
 */

// 主题颜色配置
const theme = layout.theme

// 侧边栏状态（是否折叠）
const sidebar = layout.sidebar

// 菜单布局模式
const menuLayout = layout.menuLayout

// 获取所有路由信息
const routers = computed(() => permissionStore.getTopbarRoutes())

// 顶部菜单列表（过滤隐藏菜单，处理根路径）
const topMenus = computed(() => {
  const topMenus: RouteRecordRaw[] = []
  routers.value.map((menu) => {
    if (menu.hidden !== true) {
      // 兼容顶部栏一级菜单内部跳转
      if (menu.path === '/' && menu.children) {
        topMenus.push(menu.children ? menu.children[0] : menu)
      } else {
        topMenus.push(menu)
      }
    }
  })
  return topMenus
})

// 子菜单路由列表（处理路径拼接和父路径关系）
const childrenMenus = computed(() => {
  const childrenMenus: RouteRecordRaw[] = []
  routers.value.map((router) => {
    router.children?.forEach((item) => {
      if (item.parentPath === undefined) {
        // 处理子路由路径拼接
        if (router.path === '/') {
          item.path = '/' + item.path
        } else {
          if (!isHttp(item.path)) {
            item.path = router.path + '/' + item.path
          }
        }
        item.parentPath = router.path
      }
      childrenMenus.push(item)
    })
  })
  return constantRoutes.concat(childrenMenus)
})

// 处理菜单路径拼接
const processMenuPaths = (menus: any[], basePath = ''): any[] => {
  return menus.map((menu) => {
    const fullPath = basePath ? `${basePath}/${menu.path}` : menu.path
    const processedMenu = {
      ...menu,
      fullPath: fullPath
    }

    if (menu.children && menu.children.length > 0) {
      processedMenu.children = processMenuPaths(
        menu.children.filter((child: any) => !child.hidden),
        fullPath
      )
    }

    return processedMenu
  })
}

// 水平模式的完整菜单列表（包含所有层级）
const horizontalMenus = computed(() => {
  if (menuLayout.value !== MenuLayoutMode.Horizontal) {
    return []
  }

  // 在水平模式下，使用侧边栏路由数据（应该在切换模式时已设置为完整数据）
  const allRoutes = permissionStore.getSidebarRoutes()
  console.log('原始水平菜单数据:', allRoutes)

  // 过滤掉隐藏的菜单项，只保留业务菜单
  const filteredMenus = allRoutes.filter((menu: any) => {
    // 过滤掉隐藏的菜单
    if (menu.hidden === true) return false

    // 过滤掉重定向、登录、注册等系统路由
    const systemPaths = ['/redirect', '/login', '/register', '/401', '/user', '/index']
    if (systemPaths.includes(menu.path)) return false

    // 过滤掉根路径
    if (menu.path === '/') return false

    return true
  })

  // 处理路径拼接
  const processedMenus = processMenuPaths(filteredMenus)

  console.log('处理后的水平菜单数据:', processedMenus)
  return processedMenus
})

// 当前激活的菜单路径
const activeMenu = computed(() => {
  let path = route.path

  // 水平模式：直接使用原始路径，不做任何特殊映射
  if (menuLayout.value === MenuLayoutMode.Horizontal) {
    return path
  }

  // 混合模式：首页是一级路由（/index），没有子菜单，直接返回并隐藏侧边栏
  if (path === '/index') {
    layout.toggleSideBarHide(true)
    activeRoutes(path)
    return path
  }

  let activePath = path

  // 混合模式：处理多级路径的激活状态（如 /system/user → 激活顶部 /system）
  if (path !== undefined && path.lastIndexOf('/') > 0 && hideList.indexOf(path) === -1) {
    const tmpPath = path.substring(1, path.length)
    if (!route.meta.link) {
      activePath = '/' + tmpPath.substring(0, tmpPath.indexOf('/'))
      layout.toggleSideBarHide(false)
    }
  } else if (!route.children) {
    activePath = path
    layout.toggleSideBarHide(true)
  }

  activeRoutes(activePath)
  return activePath
})

/**
 * ===== 业务逻辑方法 =====
 */

/**
 * 根据屏幕宽度计算可显示的菜单数量
 * 优化后的响应式逻辑，考虑左侧Logo和右侧工具栏的空间占用
 */
const setVisibleNumber = (): void => {
  const clientWidth = document.body.getBoundingClientRect().width

  // 在水平布局模式下，需要为Logo预留空间
  const isHorizontal = menuLayout.value === MenuLayoutMode.Horizontal

  if (clientWidth > 1600) {
    visibleNumber.value = isHorizontal ? 6 : 5
  } else if (clientWidth > 1400) {
    visibleNumber.value = isHorizontal ? 5 : 4
  } else if (clientWidth > 1200) {
    visibleNumber.value = isHorizontal ? 4 : sidebar.value.opened ? 1 : 2
  } else if (clientWidth > 1000) {
    // 因为此时需要显示更多工具条
    visibleNumber.value = isHorizontal ? 3 : sidebar.value.opened ? 1 : 2
  } else if (clientWidth > 800) {
    visibleNumber.value = isHorizontal ? 2 : 3
  } else if (clientWidth > 600) {
    visibleNumber.value = isHorizontal ? 1 : 2
  } else {
    visibleNumber.value = 1
  }
}

/**
 * 处理菜单选择事件
 * @param {string} key - 选中的菜单路径
 */
const handleSelect = (key: string): void => {
  currentIndex.value = key

  if (isHttp(key)) {
    // 外部链接在新窗口打开
    window.open(key, '_blank')
    return
  }

  // 水平模式：直接跳转，不显示侧边栏
  if (menuLayout.value === MenuLayoutMode.Horizontal) {
    // 在水平模式下，所有菜单项都直接跳转
    const routeMenu = findRouteInMenus(key, horizontalMenus.value)
    if (routeMenu && routeMenu.query) {
      const query = JSON.parse(routeMenu.query)
      router.push({ path: key, query: query })
    } else {
      router.push({ path: key })
    }
    return
  }

  // 混合模式：保持原有逻辑
  const route = routers.value.find((item) => item.path === key)
  if (!route || !route.children) {
    // 无子路由的菜单直接跳转
    const routeMenu = childrenMenus.value.find((item) => item.path === key)
    if (routeMenu && routeMenu.query) {
      const query = JSON.parse(routeMenu.query)
      router.push({ path: key, query: query })
    } else {
      router.push({ path: key })
    }
    layout.toggleSideBarHide(true)
  } else {
    // 有子路由的菜单显示侧边栏
    activeRoutes(key)
    layout.toggleSideBarHide(false)
  }
}

/**
 * 在菜单树中查找指定路径的路由
 */
const findRouteInMenus = (path: string, menus: RouteRecordRaw[]): RouteRecordRaw | null => {
  for (const menu of menus) {
    if (menu.path === path) {
      return menu
    }
    if (menu.children) {
      const found = findRouteInMenus(path, menu.children)
      if (found) return found
    }
  }
  return null
}

/**
 * 激活指定路径的子路由
 * @param {string} key - 父级路由路径
 * @returns {RouteRecordRaw[]} 匹配的子路由列表
 */
const activeRoutes = (key: string): RouteRecordRaw[] => {
  const routes: RouteRecordRaw[] = []

  if (childrenMenus.value && childrenMenus.value.length > 0) {
    childrenMenus.value.map((item) => {
      if (key === item.parentPath || (key === 'index' && '' === item.path)) {
        routes.push(item)
      }
    })
  }

  if (routes.length > 0) {
    permissionStore.setSidebarRouters(routes)
  } else {
    layout.toggleSideBarHide(true)
  }

  return routes
}

/**
 * 将name转换为英文标题
 * @param name 路由name属性
 * @returns 转换后的英文标题
 */
const nameToTitle = (name: string) => {
  if (!name) return ''
  // 去掉末尾的数字，保留英文部分
  const cleanName = name.replace(/\d+$/, '')
  // 首字母大写
  return cleanName.charAt(0).toUpperCase() + cleanName.slice(1)
}

/**
 * 获取本地化的菜单标题
 * 支持国际化翻译，优先使用i18nKey进行翻译，否则使用默认title
 * 如果都没有，则使用name转换的标题作为兜底
 *
 * @param item - 路由项对象
 * @returns 本地化后的菜单标题
 */
const getLocalizedMenuTitle = (item: any): string => {
  const meta = item?.meta
  const name = item?.name

  if (!meta && !name) return ''

  // 优先使用国际化键
  if (meta?.i18nKey) {
    const translatedTitle = t(meta.i18nKey)
    // 检查翻译是否成功（翻译成功时返回值不等于原key）
    if (translatedTitle !== meta.i18nKey) {
      return translatedTitle
    }
  }

  // 使用使用名称转换的标题或者原始标题
  return t(nameToTitle(name), meta.title)
}

/**
 * ===== 生命周期钩子 =====
 */

onMounted(() => {
  // 监听窗口大小变化，动态调整可显示菜单数量
  window.addEventListener('resize', setVisibleNumber)
  // 初始化可显示菜单数量
  setVisibleNumber()
})

onBeforeUnmount(() => {
  // 清理事件监听器
  window.removeEventListener('resize', setVisibleNumber)
})
</script>

<style lang="scss" scoped>
/* 修复更多菜单下拉箭头显示问题 */
.more-menu-item {
  width: 118px;
  height: 49px !important;

  :deep(.el-sub-menu__title) {
    display: flex;
    align-items: center;
    height: 50px;
    line-height: 50px;
  }

  :deep(.el-sub-menu__icon-arrow) {
    position: static;
    margin-left: 5px;
    transform: none;
    transition: transform 0.3s;
  }
}

/* 确保水平菜单的整体高度一致 */
:deep(.el-menu--horizontal) {
  .el-menu-item,
  .el-sub-menu {
    height: 50px;
    line-height: 50px;
  }

  .el-sub-menu .el-sub-menu__title {
    height: 50px;
    line-height: 50px;
  }
}
</style>
