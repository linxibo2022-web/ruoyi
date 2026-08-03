// 权限路由状态
import router, { constantRoutes, dynamicRoutes } from '@/router/router'
import { getRouters } from '@/api/system/core/menu/menuApi'
import { type RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/Layout.vue'
import ParentView from '@/layouts/components/AppMain/ParentView.vue'
import InnerLink from '@/layouts/components/AppMain/iframe/InnerLink.vue'
import { createCustomNameComponent } from '@/router/utils/createCustomNameComponent'
import { SystemConfig } from '@/systemConfig'
import { testMenuData } from '@/router/modules/test'

/**
 * 权限路由管理 (usePermissionStore)
 *
 * 基于 Pinia 的权限路由管理模块，提供动态路由生成、路由权限控制和多布局路由管理。
 *
 * 包含以下功能：
 * - 路由加载: 从后端获取并动态构建应用路由结构 (generateRoutes)
 * - 路由转换: 将路由配置转换为前端组件对象 (filterAsyncRouter)
 * - 权限过滤: 基于用户权限和角色过滤动态路由 (filterDynamicRoutes)
 * - 多布局支持: 管理顶部导航、侧边栏等不同布局的路由 (setTopbarRoutes, setSidebarRouters)
 * - 组件加载: 自动加载路由对应的视图组件 (loadView)
 * - 路由获取: 提供多种路由数据访问方法 (getRoutes, getSidebarRoutes, getTopbarRoutes)
 * - 路由检查: 检测路由名称冲突，避免导航问题 (duplicateRouteChecker)
 */

/**
 * 应用模块名称
 */
const PERMISSION_MODULE = 'permission'

/**
 * 路由接口
 * @property name 路由名称，可选
 * @property path 路由路径，必须
 * @property children 子路由数组，可选
 */
interface Route {
  name?: string | symbol
  path: string
  children?: Route[]
}

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

/**
 * 动态路由遍历，验证是否具备权限
 * @param routes 路由数组
 * @returns 过滤后的路由数组
 */
const filterDynamicRoutes = (routes: RouteRecordRaw[]): RouteRecordRaw[] => {
  const res: RouteRecordRaw[] = []
  const { hasPermission, hasRole } = useAuth()

  routes.forEach((route) => {
    if (route.permissions) {
      // 检查是否有任一所需权限
      if (hasPermission(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      // 检查是否有任一所需角色
      if (hasRole(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

/**
 * 加载视图组件
 * @param view 视图路径字符串
 * @param name 组件名称
 * @returns 加载的视图组件
 */
const loadView = (view: any, name: string) => {
  let res
  for (const path in modules) {
    // 从完整路径中提取视图路径
    const viewsIndex = path.indexOf('/views/')
    let dir = path.substring(viewsIndex + 7)
    dir = dir.substring(0, dir.lastIndexOf('.vue'))
    if (dir === view) {
      // 创建具有自定义名称的组件
      res = createCustomNameComponent(modules[path], { name })
      return res
    }
  }
  return res
}

/**
 * 检查路由name是否重复
 * @param localRoutes 本地路由（静态路由）
 * @param routes 动态路由
 */
const duplicateRouteChecker = (localRoutes: Route[], routes: Route[]): void => {
  // 递归展平嵌套路由
  const flatRoutes = (routes: Route[]): Route[] => {
    const res: Route[] = []
    routes.forEach((route) => {
      if (route.children) {
        res.push(...flatRoutes(route.children))
      } else {
        res.push(route)
      }
    })
    return res
  }

  // 合并并展平所有路由
  const allRoutes = flatRoutes([...localRoutes, ...routes])

  // 检查重复的路由名称
  const nameList: string[] = []
  allRoutes.forEach((route) => {
    if (!route.name) return

    const name = route.name.toString()
    if (nameList.includes(name)) {
      const message = `路由名称: [${name}] 重复, 会造成 404`
      console.error(message)
      ElNotification({
        title: '路由名称重复',
        message,
        type: 'error'
      })
      return
    }
    nameList.push(name)
  })
}

/**
 * 权限状态管理
 * @description 管理应用的路由权限、菜单等权限相关状态。
 * 此Store负责从后端获取路由配置，并将其转换为前端可用的路由对象，
 * 同时处理不同布局（顶部导航、侧边栏等）需要的路由格式。
 *
 * @example
 * // 在组件中使用
 * import { usePermissionStore } from '@/stores/permission';

 */
export const usePermissionStore = defineStore(PERMISSION_MODULE, () => {
  /**
   * 路由记录
   * @description 所有路由配置的集合，包含静态路由和动态添加的路由
   */
  const routes = ref<RouteRecordRaw[]>([])

  /**
   * 动态添加的路由
   * @description 从后端获取并动态添加的路由配置
   */
  const addRoutes = ref<RouteRecordRaw[]>([])

  /**
   * 默认路由
   * @description 用于基础布局的路由配置
   */
  const defaultRoutes = ref<RouteRecordRaw[]>([])

  /**
   * 顶部栏路由
   * @description 用于顶部导航栏显示的路由配置
   */
  const topbarRouters = ref<RouteRecordRaw[]>([])

  /**
   * 侧边栏路由
   * @description 用于侧边栏菜单显示的路由配置
   */
  const sidebarRouters = ref<RouteRecordRaw[]>([])

  /**
   * 获取所有路由
   * @returns 完整的路由记录数组
   * @example const allRoutes = permissionStore.getRoutes();
   */
  const getRoutes = (): RouteRecordRaw[] => {
    return routes.value as RouteRecordRaw[]
  }

  /**
   * 获取默认路由
   * @returns 默认路由数组
   * @example const routes = permissionStore.getDefaultRoutes();
   */
  const getDefaultRoutes = (): RouteRecordRaw[] => {
    return defaultRoutes.value as RouteRecordRaw[]
  }

  /**
   * 获取侧边栏路由
   * @returns 侧边栏路由数组，用于渲染侧边栏菜单
   * @example const routes = permissionStore.getSidebarRoutes();
   */
  const getSidebarRoutes = (): RouteRecordRaw[] => {
    return sidebarRouters.value as RouteRecordRaw[]
  }

  /**
   * 获取顶部栏路由
   * @returns 顶部栏路由数组，用于渲染顶部导航
   * @example const routes = permissionStore.getTopbarRoutes();
   */
  const getTopbarRoutes = (): RouteRecordRaw[] => {
    return topbarRouters.value as RouteRecordRaw[]
  }

  /**
   * 设置路由
   * @param newRoutes 新路由数组
   * @description 设置动态路由，并更新完整路由集合
   * @example permissionStore.setRoutes(dynamicRoutes);
   */
  const setRoutes = (newRoutes: RouteRecordRaw[]): void => {
    addRoutes.value = newRoutes
    routes.value = constantRoutes.concat(newRoutes)
  }

  /**
   * 设置默认路由
   * @param routes 路由数组
   * @description 设置默认路由配置
   * @example permissionStore.setDefaultRoutes(routes);
   */
  const setDefaultRoutes = (routes: RouteRecordRaw[]): void => {
    defaultRoutes.value = constantRoutes.concat(routes)
  }

  /**
   * 设置顶部栏路由
   * @param routes 路由数组
   * @description 设置顶部导航栏的路由配置
   * @example permissionStore.setTopbarRoutes(routes);
   */
  const setTopbarRoutes = (routes: RouteRecordRaw[]): void => {
    topbarRouters.value = routes
  }

  /**
   * 设置侧边栏路由
   * @param routes 路由数组
   * @description 设置侧边栏菜单的路由配置
   * @example permissionStore.setSidebarRouters(routes);
   */
  const setSidebarRouters = (routes: RouteRecordRaw[]): void => {
    sidebarRouters.value = routes
  }

  /**
   * 遍历后台传来的路由字符串，转换为组件对象
   * @param asyncRouterMap 后台传来的路由字符串
   * @param lastRouter 上一级路由，用于构建嵌套路由
   * @param type 是否是重写路由，为true时会对children进行特殊处理
   * @returns 处理后的路由数组
   */
  const filterAsyncRouter = (asyncRouterMap: RouteRecordRaw[], lastRouter?: RouteRecordRaw, type = false): RouteRecordRaw[] => {
    return asyncRouterMap.filter((route) => {
      if (type && route.children) {
        route.children = filterChildren(route.children, undefined)
      }
      // Layout ParentView 组件特殊处理
      if (route.component?.toString() === 'Layout') {
        route.component = Layout
      } else if (route.component?.toString() === 'ParentView') {
        route.component = ParentView
      } else if (route.component?.toString() === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component, route.name as string)
      }
      if (route.children != null && route.children && route.children.length) {
        route.children = filterAsyncRouter(route.children, route, type)
      } else {
        delete route.children
        delete route.redirect
      }
      return true
    })
  }

  /**
   * 过滤子路由
   * @param childrenMap 子路由数组
   * @param lastRouter 上一级路由，用于构建完整路径
   * @returns 处理后的子路由数组
   */
  const filterChildren = (childrenMap: RouteRecordRaw[], lastRouter?: RouteRecordRaw): RouteRecordRaw[] => {
    let children: RouteRecordRaw[] = []
    childrenMap.forEach((el) => {
      // 构建完整子路由路径
      el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
      if (el.children && el.children.length && el.component?.toString() === 'ParentView') {
        // ParentView组件的子路由需要特殊处理
        children = children.concat(filterChildren(el.children, el))
      } else {
        children.push(el)
      }
    })
    return children
  }

  /**
   * 生成路由
   * @description 从后端获取路由数据并处理成可用的路由配置。
   * 此方法会执行以下操作：
   * 1. 从API获取路由配置
   * 2. 转换路由配置为前端组件
   * 3. 处理不同场景需要的路由格式
   * 4. 添加有权限的动态路由
   * 5. 检查路由名称冲突
   *
   * @returns 处理后的路由数组Promise
   * @example
   * // 在应用初始化时调用
   * const [err, data] = await permissionStore.generateRoutes();
   * // 路由生成后可以进行其他初始化
   * initApp();
   */
  const generateRoutes = async (): Result<RouteRecordRaw[]> => {
    // 从后端API获取路由数据
    const [err, data] = await getRouters()
    if (err) {
      return [err, null]
    }
    // 开发环境添加测试菜单数据
    if (SystemConfig.app.env === 'development') {
      const toolMenuIndex = data.findIndex((item: any) => item.name === 'Tool3')
      if (toolMenuIndex !== -1) {
        data[toolMenuIndex].children.push(...testMenuData)
      }
    }
    // 深拷贝路由数据用于不同处理场景
    const sdata = JSON.parse(JSON.stringify(data))
    const rdata = JSON.parse(JSON.stringify(data))
    const defaultData = JSON.parse(JSON.stringify(data))

    // 处理不同场景的路由格式
    const sidebarRoutes = filterAsyncRouter(sdata)
    const rewriteRoutes = filterAsyncRouter(rdata, undefined, true)
    const defaultRoutes = filterAsyncRouter(defaultData)

    // 处理动态权限路由
    const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
    asyncRoutes.forEach((route) => {
      router.addRoute(route)
    })

    // 设置各类路由到store
    setRoutes(rewriteRoutes)
    setSidebarRouters(constantRoutes.concat(sidebarRoutes))
    setDefaultRoutes(sidebarRoutes)
    setTopbarRoutes(defaultRoutes)

    // 路由name重复检查，避免404问题
    duplicateRouteChecker(asyncRoutes, sidebarRoutes)

    return [null, rewriteRoutes]
  }

  return {
    routes,
    topbarRouters,
    sidebarRouters,
    defaultRoutes,
    getRoutes,
    getDefaultRoutes,
    getSidebarRoutes,
    getTopbarRoutes,
    setRoutes,
    generateRoutes,
    setSidebarRouters
  }
})
