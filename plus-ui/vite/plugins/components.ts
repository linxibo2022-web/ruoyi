import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import IconsResolver from 'unplugin-icons/resolver'

// 导出一个函数，用于配置 Vue 组件自动导入插件
export default (path: any) => {
  return Components({
    resolvers: [
      // 自动导入 Element Plus 组件
      // 这将自动引入 Element Plus 的组件，无需手动 import
      ElementPlusResolver({
        importStyle: false
      }),

      // 自动注册图标组件
      // 使用 Element Plus 图标集合（'ep'）
      // 这允许直接使用图标组件，如 <i-ep-xxx />
      IconsResolver({
        enabledCollections: ['ep']
      })
    ],

    // 生成组件类型声明文件的路径
    // 这个文件将包含自动导入组件的类型定义
    // 路径解析：从当前文件向上两级，进入 src/types 目录
    dts: path.resolve(path.join(process.cwd(), './src'), 'types', 'components.d.ts')
  })
}
