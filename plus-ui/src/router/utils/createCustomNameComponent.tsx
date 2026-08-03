import { Component, defineComponent, h } from 'vue'
import { to } from '@/utils/to'

interface Options {
  /**
   * 自定义组件名称
   * 该名称将用于组件定义，并影响keep-alive的缓存识别
   */
  name?: string
}

/**
 * 自定义组件名称工厂函数
 *
 * 该函数用于动态生成具有自定义名称的组件，主要解决路由缓存问题。
 * 当后台返回的路由需要动态生成组件名时，可使用此函数来创建带有指定名称的组件，
 * 以确保组件在keep-alive缓存中能被正确识别。
 *
 * @author 感谢 @fourteendp 的贡献
 * @see https://github.com/vbenjs/vue-vben-admin/issues/3927
 *
 * @param {Function} loader - 异步加载组件的函数，通常是一个动态导入语句
 * @param {Object} options - 配置选项
 * @param {string} [options.name] - 自定义的组件名称
 * @returns {Function} 返回一个异步函数，该函数解析为一个Vue组件
 *
 * @example
 * // 基本用法
 * const UserComponent = createCustomNameComponent(
 *   () => import('./UserComponent.vue'),
 *   { name: 'UserComponent' }
 * );
 *
 * // 在路由中使用
 * const routes = [
 *   {
 *     path: '/user',
 *     name: 'user',
 *     component: createCustomNameComponent(
 *       () => import('./views/user/index.vue'),
 *       { name: 'User' }
 *     )
 *   }
 * ];
 */
export const createCustomNameComponent = (loader: () => Promise<any>, options: Options = {}): (() => Promise<Component>) => {
  // 从选项中提取名称
  const { name } = options

  // 用于存储加载后的组件
  let component: Component | null = null

  /**
   * 加载组件的内部函数
   * 使用异步加载确保组件只在需要时才被加载
   */
  const load = async () => {
    const [error, loadedModule] = await to(loader())
    if (error) {
      console.error(`无法解析组件 ${name}，错误:`, error.message)
      return
    }

    // 提取默认导出
    component = loadedModule.default
  }

  /**
   * 返回一个异步函数，该函数会生成并返回包装后的组件
   * 这个函数会在路由导航时被调用
   */
  return async () => {
    // 如果组件尚未加载，则进行加载
    if (!component) {
      await load()
    }

    // 返回一个新的组件定义，该定义使用指定的名称并渲染加载的组件
    return Promise.resolve(
      defineComponent({
        // 使用自定义名称
        name,
        render() {
          // 使用h函数渲染加载的组件
          return h(component as Component)
        }
      })
    )
  }
}
