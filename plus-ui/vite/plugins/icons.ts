import Icons from 'unplugin-icons/vite'

//图标使用方法 https://icones.js.org/ 前往搜索图标,点击unocss,得到样式类
//<div class="i-material-symbols-home-rounded"/>
// 导出图标插件配置函数
// 用于自动处理和导入图标资源
export default () => {
  return Icons({
    // 自动安装图标库
    // 当使用未安装的图标集合时，将自动下载并安装相应的图标依赖
    autoInstall: true
  })
}
