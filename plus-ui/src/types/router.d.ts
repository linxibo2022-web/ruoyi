// 路由类型声明
import { type LocationQuery, type RouteMeta as VRouteMeta } from 'vue-router'

/**
 * 路由类型扩展 (vue-router.d.ts)
 *
 * 通过声明合并扩展 Vue Router 的类型定义，为路由系统添加自定义属性和功能支持。
 *
 * 包含以下扩展:
 * - 路由元数据: 扩展原生RouteMeta，支持自定义标题、图标和缓存控制 (RouteMeta)
 * - 权限控制: 为路由添加基于角色和权限的访问控制能力 (permissions, roles)
 * - 导航配置: 控制路由在侧边栏、面包屑和标签栏中的显示行为 (hidden, alwaysShow)
 * - 外部链接: 支持在路由系统中集成外部链接 (link)
 * - 国际化支持: 为路由标题提供多语言支持 (i18nKey)
 * - 标签导航: 为多标签页导航提供专用的路由视图类型 (TagView)
 */
declare module 'vue-router' {
  /**
   * 路由元数据接口
   * @extends VRouteMeta Vue Router 原生的 RouteMeta 类型
   */
  interface RouteMeta extends VRouteMeta {
    /** 外部链接 */
    link?: string
    /** 路由标题，显示在侧边栏、面包屑和标签栏 */
    title?: string
    /** 是否固定在标签栏，不可关闭 */
    affix?: boolean
    /** 是否不缓存该路由（默认false） */
    noCache?: boolean
    /** 当路由设置了该属性，则会高亮相对应的侧边栏 */
    activeMenu?: string
    /** 路由图标，对应路径src/types/icons.d.ts */
    icon?: IconCode
    /** 如果设置为false，则不会在breadcrumb面包屑中显示 */
    breadcrumb?: boolean
    /** 国际化键 */
    i18nKey?: string
  }

  /**
   * 路由记录基础接口
   * @description 扩展路由记录基础属性，添加权限控制等自定义属性
   */
  interface _RouteRecordBase {
    /** 当设置为true时，该路由不会在侧边栏出现 */
    hidden?: boolean | string | number
    /** 访问路由所需的权限标识 */
    permissions?: string[]
    /** 访问路由所需的角色 */
    roles?: string[]
    /**
     * 总是显示根路由
     * 当你一个路由下面的children声明的路由大于1个时，自动会变成嵌套的模式
     * 只有一个时，会将那个子路由当做根路由显示在侧边栏
     * 若想不管路由下面的children声明的个数都显示根路由，可设置alwaysShow: true
     */
    alwaysShow?: boolean
    /** 访问路由的默认传递参数 */
    query?: string
    /** 父路由路径 */
    parentPath?: string
  }

  /**
   * 路由位置基础接口
   * @description 扩展路由位置基础属性
   */
  interface _RouteLocationBase {
    /** 子路由配置 */
    children?: _RouteRecordBase[]
    /** 路由路径 */
    path?: string
    /** 路由标题 */
    title?: string
  }

  /**
   * 标签视图接口
   * @description 用于标签页导航的路由视图信息
   */
  interface TagView {
    /** 完整路径，包含参数和查询部分 */
    fullPath?: string
    /** 路由名称 */
    name?: string
    /** 路由路径 */
    path?: string
    /** 路由标题 */
    title?: string
    /** 路由元数据 */
    meta?: RouteMeta
    /** 路由查询参数 */
    query?: LocationQuery
  }
}

export {}
