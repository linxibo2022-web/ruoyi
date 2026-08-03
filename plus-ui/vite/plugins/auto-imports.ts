import AutoImport from 'unplugin-auto-import/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * 创建 AutoImport 插件配置
 */
export default (path: any) => {
  return AutoImport({
    // 自动导入的库和模块
    imports: ['vue', 'vue-router', '@vueuse/core', 'pinia'],
    //自动导入组合函数 和状态管理模块
    dirs: ['src/composables', 'src/stores/modules'],
    // ESLint 自动导入配置
    eslintrc: {
      // 启用 ESLint 自动生成配置
      enabled: true,
      // ESLint 配置文件路径
      filepath: './.eslintrc-auto-import.json',
      // 全局属性值
      globalsPropValue: true
    },
    // 解析器配置
    resolvers: [
      // 自动导入 Element Plus 组件和函数（包含样式）
      ElementPlusResolver({
        importStyle: false
      })
    ],
    // 是否在 vue 模板中自动导入
    vueTemplate: true,
    // 类型声明文件路径
    dts: 'src/types/auto-imports.d.ts',
    // 设置导入优先级
    defaultExportByFilename: false
  })
}
