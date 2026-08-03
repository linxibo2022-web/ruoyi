// 标签页工具
import router from '@/router/router'
import { RouteLocationNormalized, RouteLocationRaw } from 'vue-router'

/**
 * 标签页导航操作相关工具函数
 *
 * 包含以下功能：
 * - 页面刷新: 刷新当前打开的标签页 (refreshPage)
 * - 关闭并打开: 关闭当前标签页并打开新标签页 (closeOpenPage)
 * - 关闭页面: 关闭指定标签页 (closePage)
 * - 关闭全部: 关闭所有标签页 (closeAllPage)
 * - 关闭左侧: 关闭当前标签页左侧的所有标签页 (closeLeftPage)
 * - 关闭右侧: 关闭当前标签页右侧的所有标签页 (closeRightPage)
 * - 关闭其他: 关闭除当前标签页外的所有标签页 (closeOtherPage)
 * - 打开页面: 打开新的标签页 (openPage)
 * - 更新页面: 更新标签页信息 (updatePage)
 *
 * 使用示例:
 * ```ts
 * // 使用命名导入方式
 * import { refreshPage, closePage, openPage } from '@/utils/tab';
 *
 * // 刷新当前页面
 * refreshPage();
 *
 * // 打开新页面
 * openPage('/dashboard');
 * ```
 */

/**
 * 返回类型定义，包含已访问视图和缓存视图
 */
type TagsViewResult = {
  visitedViews: RouteLocationNormalized[]
  cachedViews: string[]
}

/**
 * 刷新当前标签页
 *
 * @param obj 可选，标签对象。如果未提供，则使用当前路由
 * @returns Promise<void>
 *
 * @example
 * // 刷新当前页面
 * refreshPage();
 *
 * @example
 * // 刷新指定路由对象
 * const route = router.currentRoute.value;
 * refreshPage(route);
 *
 */
export const refreshPage = async (obj?: RouteLocationNormalized): Promise<void> => {
  const { path, query, matched } = router.currentRoute.value
  if (obj === undefined) {
    // 查找当前路由中的组件
    for (const m of matched) {
      if (m.components?.default?.name && !['Layout', 'ParentView'].includes(m.components.default.name)) {
        obj = {
          name: m.components.default.name,
          path: path,
          query: query,
          matched: undefined,
          fullPath: undefined,
          hash: undefined,
          params: undefined,
          redirectedFrom: undefined,
          meta: undefined
        }
        break // 找到第一个匹配的组件后退出循环
      }
    }
  }

  // 提取查询参数和路径
  const targetQuery = obj?.query || {}
  const targetPath = obj?.path || ''

  // 从缓存中移除视图
  await useLayout().delCachedView(obj)

  // 使用redirect路由刷新页面
  await router.replace({
    path: '/redirect' + targetPath,
    query: targetQuery
  })
}

/**
 * 关闭当前标签页并打开新标签页
 *
 * @param obj 要打开的新路由对象
 *
 * @example
 * // 关闭当前页面并打开用户列表页
 * closeOpenPage({ path: '/system/user' });//closeOpenPage('/system/user');
 *
 * @example
 * // 关闭当前页面并打开带查询参数的页面
 * closeOpenPage({
 *   path: '/system/user/detail',
 *   query: { id: '123' }
 * });
 *
 * @example
 * // 在操作完成后跳转到新页面
 * const submitAndRedirect = async () => {
 *   await saveData();
 *   closeOpenPage({ path: '/dashboard' });
 * };
 */
export const closeOpenPage = (obj: RouteLocationRaw): void => {
  useLayout().delView(router.currentRoute.value)
  if (obj !== undefined) {
    router.push(obj)
  }
}

/**
 * 关闭指定标签页
 *
 * @param obj 可选，要关闭的标签页对象。如果未提供，则关闭当前标签页
 * @returns Promise，包含已访问视图和缓存视图的数组
 *
 * @example
 * // 关闭当前页面
 * closePage();
 *
 * @example
 * // 关闭指定路由页面
 * const route = { path: '/system/user', ... };
 * closePage(route);
 *
 * @example
 * // 关闭当前页面并处理导航结果
 * closePage().then(result => {
 *   console.log('已关闭页面，剩余页面数:', result.visitedViews.length);
 * });
 */
export const closePage = async (obj?: RouteLocationNormalized): Promise<TagsViewResult | any> => {
  if (obj === undefined) {
    const { visitedViews } = await useLayout().delView(router.currentRoute.value)
    // 获取最后一个标签页（当前标签页已被删除）
    const latestView = visitedViews.slice(-1)[0]
    // 如果有其他标签页，则跳转到最后一个标签页，否则跳转到首页
    return router.push(latestView ? latestView.fullPath : '/')
  }
  return useLayout().delView(obj)
}

/**
 * 关闭所有标签页
 *
 * @returns Promise，包含已访问视图和缓存视图的数组
 * @example
 * // 关闭所有标签页
 * closeAllPage();
 */
export const closeAllPage = (): Promise<TagsViewResult> => {
  return useLayout().delAllViews()
}

/**
 * 关闭左侧标签页
 *
 * @param obj 可选，参考标签页对象。如果未提供，则使用当前路由
 * @returns Promise，包含已访问视图和缓存视图的数组
 * @example
 * // 关闭当前页面左侧的所有标签页
 * closeLeftPage();
 */
export const closeLeftPage = (obj?: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
  return useLayout().delLeftTags(obj || router.currentRoute.value)
}

/**
 * 关闭右侧标签页
 *
 * @param obj 可选，参考标签页对象。如果未提供，则使用当前路由
 * @returns Promise，包含已访问视图和缓存视图的数组
 *
 * @example
 * // 关闭当前页面右侧的所有标签页
 * closeRightPage();
 */
export const closeRightPage = (obj?: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
  return useLayout().delRightTags(obj || router.currentRoute.value)
}

/**
 * 关闭其他标签页
 *
 * @param obj 可选，要保留的标签页对象。如果未提供，则保留当前路由
 * @returns Promise，包含已访问视图和缓存视图的数组
 *
 * @example
 * // 关闭除当前页面外的所有标签页
 * closeOtherPage();
 *
 * @example
 * // 关闭除指定页面外的所有标签页
 * const route = router.currentRoute.value;
 * closeOtherPage(route);
 */
export const closeOtherPage = (obj?: RouteLocationNormalized): Promise<TagsViewResult> => {
  return useLayout().delOthersViews(obj || router.currentRoute.value)
}

/**
 * 打开新标签页
 *
 * @param url 路由地址
 * @param title 可选，标签页标题
 * @param query 可选，查询参数
 * @returns 路由跳转的Promise结果
 *
 * @example
 * // 打开基本页面
 * openPage('/dashboard');
 *
 * @example
 * // 打开带标题和参数的页面
 * openPage('/system/user/detail', '用户详情', { id: '123' });
 */
export const openPage = (url: string, title?: string, query: Record<string, any> = {}) => {
  const obj = {
    path: url,
    query: title ? { ...query, title } : query
  }
  return router.push(obj)
}

/**
 * 更新标签页信息
 *
 * @param obj 标签页对象
 * @returns Promise
 *
 * @example
 * // 更新当前页面标签信息
 * const route = { ...router.currentRoute.value, meta: { title: '新标题' } };
 * updatePage(route);
 *
 * @example
 * // 在动态修改页面标题时使用
 * const updateTitle = (newTitle) => {
 *   const route = {
 *     ...router.currentRoute.value,
 *     meta: { ...router.currentRoute.value.meta, title: newTitle }
 *   };
 *   updatePage(route);
 * };
 */
export const updatePage = (obj: RouteLocationNormalized) => {
  return useLayout().updateVisitedView(obj)
}
