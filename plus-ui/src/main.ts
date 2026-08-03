/**
 * 应用入口文件
 * @description 初始化Vue应用并配置全局插件、样式、路由等
 */
import { createApp } from 'vue'

/**
 * 全局样式导入
 */
// UnoCSS (原子化CSS框架)
import 'virtual:uno.css'
// Element Plus暗黑模式变量
import 'element-plus/theme-chalk/dark/css-vars.css'
// 自定义全局样式
import '@/assets/styles/main.scss'
// 系统图标
import '@/assets/icons/system/iconfont.css'
// 本地静态 SVG sprite 注册（通过 vite-plugin-svg-icons-ng 生成）
// 支持 <Icon code="dingtalk" /> 形式引用 src/assets/icons/svg/*.svg
import 'virtual:svg-icons-register'
/**
 * 核心应用配置
 */
// 根组件
import App from './App.vue'
// Pinia状态管理
import store from '@/stores/store'
// Vue Router路由管理
import router from './router/router'

/**
 * 自定义指令
 * @description 注册v-permission等自定义指令
 */
import directive from '@/directives/directives'

/**
 * 代码高亮配置
 * @description 用于文档、代码展示等场景
 */
// 高亮主题样式 - 深色主题
import 'highlight.js/styles/atom-one-dark.css'
// 高亮核心库
import 'highlight.js/lib/common'
// Vue高亮组件
import HighLight from '@highlightjs/vue-plugin'

// Element Plus图标组件
import ElementIcons from '@/plugins/elementIcons'

/**
 * 国际化配置
 * @description 多语言支持
 */
import i18n from '@/locales/i18n'

/**
 * Element Plus组件配置
 * @description 修改Element Plus组件的默认行为
 */
// 修改 el-dialog 默认点击遮照为不关闭
import { ElDialog } from 'element-plus'

ElDialog.props.closeOnClickModal.default = false

/**
 * 创建Vue应用实例
 */
const app = createApp(App)

/**
 * 注册全局插件和模块
 */
// 代码高亮插件
app.use(HighLight)
// Element图标 插件
app.use(ElementIcons)
// 路由
app.use(router)
// 状态管理
app.use(store)
// 国际化
app.use(i18n)
// 注册自定义指令
directive(app)

/**
 * 挂载应用到DOM
 */
app.mount('#app')
