import process from 'node:process'

/**
 * Vite 配置文件
 * @description 配置Vite构建和开发服务器选项
 */

import { ConfigEnv, defineConfig, loadEnv, UserConfig } from 'vite'
// 导入自定义插件配置
import createPlugins from './vite/plugins'
// 导入CSS自动添加兼容性前缀插件
import autoprefixer from 'autoprefixer'
// Node.js 路径处理模块
import path from 'path'

// 导出Vite配置
export default async ({ command, mode }: ConfigEnv): Promise<UserConfig> => {
  // 加载环境变量
  const env = loadEnv(mode, path.resolve(process.cwd(), 'env'))
  console.log('命令, 模式 -> ', command, mode)
  console.log('环境变量 env -> ', env)
  return defineConfig({
    envDir: './env', // 自定义环境变量目录
    // 部署生产环境和开发环境下的URL
    // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
    // 例如 https://ruoyi.plus/
    // 如果应用被部署在一个子路径上，你就需要用这个选项指定这个子路径
    // 例如，如果你的应用被部署在 https://ruoyi.plus/admin/，则设置 baseUrl 为 /admin/
    base: env.VITE_APP_CONTEXT_PATH,

    // 解析配置
    resolve: {
      // 路径别名配置
      alias: {
        // @ 指向 src 目录
        '@': path.join(process.cwd(), './src')
      },
      // 导入时可以省略的扩展名列表
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },

    // 插件配置，使用自定义插件工厂函数创建插件列表
    // 第二个参数表示是否是构建模式
    plugins: createPlugins(env, command === 'build'),

    // 开发服务器配置
    server: {
      // 监听所有地址，包括局域网和公网地址
      host: '0.0.0.0',
      //允许访问的域名列表 .ruoyikj.top代表一级以及子域名访问
      allowedHosts: ['.ruoyikj.top'],
      // 从环境变量中获取端口号
      port: Number(env.VITE_APP_PORT),
      // 端口被占用时自动递增
      strictPort: false,
      // 让 Vite 自己决定打开哪个 URL（会自动用最终端口）
      open: true,
      // 代理配置，用于解决跨域问题
      proxy: {
        // 使用环境变量中的API前缀作为代理匹配条件
        [env.VITE_APP_BASE_API]: {
          // 代理目标地址
          target: 'http://127.0.0.1:' + env.VITE_APP_BASE_API_PORT,
          // 支持跨域
          changeOrigin: true,
          // 支持WebSocket
          ws: true,
          // 路径重写，去除API前缀
          rewrite: (path) => path.replace(new RegExp('^' + env.VITE_APP_BASE_API), '')
        }
      }
    },

    // CSS相关配置
    css: {
      // 预处理器配置
      preprocessorOptions: {
        scss: {
          // SCSS配置，使用现代编译器API
          // additionalData: '@use "@/assets/styles/abstracts/variables as *";'
          // javascriptEnabled: true
          api: 'modern-compiler'
        }
      },
      // PostCSS配置
      postcss: {
        plugins: [
          // 自动添加浏览器兼容性前缀
          autoprefixer(),
          // 移除多余的charset声明的插件
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                atRule.remove()
              }
            }
          }
        ]
      }
    },

    // 依赖优化选项
    optimizeDeps: {
      // 预构建的依赖项，可以提高首次加载速度
      include: [
        // Vue核心库
        'vue',
        // Vue路由
        'vue-router',
        // 状态管理
        'pinia',
        // HTTP客户端
        'axios',
        // Vue组合式API工具集
        '@vueuse/core',
        // 图表库
        'echarts',
        // 国际化
        'vue-i18n',
        // 富文本编辑器
        '@vueup/vue-quill',
        // 图片转换工具
        'image-conversion',
        // Element Plus组件CSS
        'element-plus/es/components/**/css',
        // JSON格式化工具
        'vue-json-pretty',
        // 文件保存工具
        'file-saver',
        // 富文本编辑器
        '@wangeditor/editor-for-vue',
        // 富文本工具
        '@wangeditor/editor',
        // 二维码
        'qrcode',
        // 拖拽
        'vue-draggable-plus',
        //高亮
        'highlight.js',
        // 省市区
        'element-china-area-data'
      ]
    }
  })
}
