// vite/plugins/svg-icon.ts
// 本地静态 SVG 图标：构建时合并为 SVG sprite，运行时通过 <use xlink:href="#icon-xxx"> 使用
// 适用场景：iconfont 找不到的品牌 icon（钉钉/MaxKey/TopIAM 等），
// 或需要与移动端 /static/*.svg 保持"放文件即可用"体验的场景
// 图标目录：plus-ui/src/assets/icons/svg/*.svg
// symbol id 命名：icon-[name]（与移动端 SOCIAL_CONFIGS 里的 icon 字段对齐）

import { createSvgIconsPlugin } from 'vite-plugin-svg-icons-ng'
import path from 'node:path'

export default () => {
  return createSvgIconsPlugin({
    // 扫描目录（仅扫描 svg/ 根，避免误收 custom/iconify 等图标的残留 svg）
    iconDirs: [path.resolve(process.cwd(), 'src/assets/icons/svg')],
    // symbolId 规则：去掉 [dir]，只用文件名，使用方写 icon: 'dingtalk' 即可
    symbolId: 'icon-[name]',
    // 注入到 body 之前，避免被 App 挂载前的空 DOM 覆盖
    inject: 'body-first'
  })
}
