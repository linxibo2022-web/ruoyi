<!-- 侧栏菜单项 -->
<template>
  <div v-if="!menuItem.hidden">
    <!-- 单级菜单渲染 -->
    <template v-if="shouldRenderAsSingleItem && (!singleChildRoute.children || singleChildRoute.noShowingChildren) && !menuItem.alwaysShow">
      <AppLink v-if="singleChildRoute.meta" :to="buildRoutePath(singleChildRoute.path, singleChildRoute.query)">
        <el-menu-item :index="buildRoutePath(singleChildRoute.path)" :class="{ 'submenu-title-noDropdown': !isNestedItem }">
          <Icon
            size="20px"
            class="menu-item-icon"
            :class="layout.sidebar.value.opened ? '' : 'icon-collapsed'"
            :code="singleChildRoute.meta.icon || (menuItem.meta && menuItem.meta.icon)"
          />
          <template #title>
            <span class="menu-title" :title="getTooltipTitle(getLocalizedMenuTitle(singleChildRoute))">
              {{ getLocalizedMenuTitle(singleChildRoute) }}
            </span>
          </template>
        </el-menu-item>
      </AppLink>
    </template>

    <!-- 多级菜单渲染 -->
    <el-sub-menu v-else ref="subMenuRef" :index="buildRoutePath(menuItem.path)" teleported>
      <template v-if="menuItem.meta" #title>
        <Icon
          size="20px"
          class="menu-item-icon"
          :class="layout.sidebar.value.opened ? '' : 'ml-[20px]'"
          :code="menuItem.meta ? menuItem.meta.icon : null"
        />
        <span class="menu-title" :title="getTooltipTitle(getLocalizedMenuTitle(menuItem))">
          {{ getLocalizedMenuTitle(menuItem) }}
        </span>
      </template>

      <!-- 递归渲染子菜单项 -->
      <SidebarItem
        v-for="(childRoute, index) in menuItem.children"
        :key="childRoute.path + index"
        :is-nest="true"
        :item="childRoute"
        :base-path="buildRoutePath(childRoute.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts" name="SidebarItem">
import AppLink from './AppLink.vue'
import { isExternal } from '@/utils/validators'
import { normalizePath } from '@/utils/string'
import { RouteRecordRaw } from 'vue-router'

// ============== 状态管理 ==============
const layout = useLayout()

// ============== 国际化 ==============
const { t } = useI18n()

// ============== 组件属性定义 ==============
/**
 * 侧边栏菜单项组件的属性接口
 */
interface SidebarItemProps {
  /** 当前菜单项的路由配置对象 */
  item: RouteRecordRaw
  /** 是否为嵌套子菜单项 */
  isNest?: boolean
  /** 父级路径前缀 */
  basePath?: string
}

const props = withDefaults(defineProps<SidebarItemProps>(), {
  isNest: false,
  basePath: ''
})

// ============== 响应式数据 ==============
/**
 * 存储单个子路由信息
 * 用于判断是否应该直接显示子路由而不是父级菜单
 */
const singleChildRoute = ref<any>({})

/**
 * 子菜单组件引用
 */
const subMenuRef = ref()

// ============== 计算属性 ==============
/**
 * 获取当前菜单项配置的别名
 * 提高代码可读性
 */
const menuItem = computed(() => props.item)

/**
 * 判断是否为嵌套菜单项的别名
 */
const isNestedItem = computed(() => props.isNest)

/**
 * 判断是否应该渲染为单个菜单项
 * 当只有一个可见子路由时，直接显示子路由
 */
const shouldRenderAsSingleItem = computed(() => hasOnlyOneVisibleChild(menuItem.value, menuItem.value.children))

// ============== 核心方法 ==============
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
 * 判断父路由是否只有一个可显示的子路由
 * 用于决定菜单的渲染方式：单级菜单 vs 多级菜单
 *
 * @param parentRoute - 父级路由配置
 * @param childRoutes - 子路由配置数组
 * @returns 是否只有一个可显示的子路由
 */
const hasOnlyOneVisibleChild = (parentRoute: RouteRecordRaw, childRoutes?: RouteRecordRaw[]): boolean => {
  // 确保子路由数组存在
  const children = childRoutes || []

  // 过滤出所有可显示的子路由
  const visibleChildren = children.filter((child) => {
    if (child.hidden) {
      return false
    }

    // 设置当前找到的可见子路由
    singleChildRoute.value = child
    return true
  })

  // 只有一个可见子路由时，直接显示该子路由
  if (visibleChildren.length === 1) {
    return true
  }

  // 没有可见子路由时，显示父路由本身
  if (visibleChildren.length === 0) {
    singleChildRoute.value = {
      ...parentRoute,
      path: '',
      noShowingChildren: true
    }
    return true
  }

  return false
}

/**
 * 构建完整的路由路径
 * 处理相对路径、绝对路径、外部链接和查询参数
 *
 * @param routePath - 路由路径
 * @param routeQuery - 路由查询参数（JSON字符串）
 * @returns 完整的路由路径对象或字符串
 */
const buildRoutePath = (routePath: string, routeQuery?: string): any => {
  // 处理外部链接
  if (isExternal(routePath)) {
    return routePath
  }

  // 如果基础路径是外部链接，直接返回
  if (isExternal(props.basePath as string)) {
    return props.basePath
  }

  // 处理带查询参数的路由
  if (routeQuery) {
    try {
      const queryParams = JSON.parse(routeQuery)
      return {
        path: normalizePath(`${props.basePath}/${routePath}`),
        query: queryParams
      }
    } catch (error) {
      console.warn('Failed to parse route query:', routeQuery)
    }
  }

  // 返回标准化的路径
  return normalizePath(`${props.basePath}/${routePath}`)
}

/**
 * 获取菜单项的tooltip标题
 * 只有当标题长度超过阈值时才显示tooltip
 *
 * @param title - 菜单标题
 * @returns tooltip标题，空字符串表示不显示tooltip
 */
const getTooltipTitle = (title: string | undefined): string => {
  // 标题长度阈值，超过此长度才显示tooltip
  const TOOLTIP_LENGTH_THRESHOLD = 5

  if (!title || title.length <= TOOLTIP_LENGTH_THRESHOLD) {
    return ''
  }

  return title
}
</script>

<style lang="scss" scoped></style>
