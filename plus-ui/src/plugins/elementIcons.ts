import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { App } from 'vue'

/**
 * Element Plus 图标全局注册插件
 *
 * 该插件用于全局注册所有 Element Plus 图标组件，
 * 注册后可以在模板中直接使用图标组件，无需单独导入。
 * 例如：<Edit />、<Search />、<Delete /> 等。
 */
export default {
  install: (app: App) => {
    // 遍历所有 Element Plus 图标并逐个注册为全局组件
    // 注册后可以在任意组件中直接使用，无需导入
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
      app.component(key, component)
    }
  }
}
