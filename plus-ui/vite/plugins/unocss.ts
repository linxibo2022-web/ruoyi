import UnoCss from 'unocss/vite'

// 导出 UnoCSS 插件配置函数
// UnoCSS 是一个即时原子化 CSS 引擎，提供高性能、灵活的样式解决方案
export default () => {
  return UnoCss({
    // 禁用顶层 await 特性
    // 默认情况下 UnoCSS 开启 hmrTopLevelAwait（热更新顶层等待）
    // 在低版本浏览器中，顶层 await 可能会导致兼容性问题和报错
    // 通过设置 false 可以提高浏览器兼容性
    hmrTopLevelAwait: false
  })
}
