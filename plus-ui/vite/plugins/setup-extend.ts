import setupExtend from 'unplugin-vue-setup-extend-plus/vite'

// 导出 Vue setup 语法扩展插件配置函数
// 允许在 setup 语法中为组件添加额外的属性和选项
export default () => {
  return setupExtend({
    // 空配置对象，使用默认配置
    // 可以根据需要添加自定义配置选项
  })
}
