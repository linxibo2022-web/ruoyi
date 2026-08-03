/**
 * 主题切换动画工具
 * 使用 View Transition API 实现从点击位置扩散的圆形切换效果
 */

import { useLayout } from '@/composables/useLayout'

/**
 * 主题切换动画
 * @param event 鼠标点击事件
 * @param isDark 当前是否为暗黑模式
 */
export const toggleThemeWithAnimation = (event: MouseEvent, isDark: boolean) => {
  const layout = useLayout()

  // 获取点击位置
  const x = event.clientX
  const y = event.clientY

  // 计算从点击位置到视窗最远角的距离(最大圆半径)
  const endRadius = Math.hypot(Math.max(x, innerWidth - x), Math.max(y, innerHeight - y))

  // 设置 CSS 变量,用于圆形动画的中心点和半径
  const root = document.documentElement
  root.style.setProperty('--theme-x', `${x}px`)
  root.style.setProperty('--theme-y', `${y}px`)
  root.style.setProperty('--theme-r', `${endRadius}px`)

  // 检查浏览器是否支持 View Transition API
  if (document.startViewTransition) {
    // 使用 View Transition API 执行动画
    document.startViewTransition(() => {
      layout.toggleDark(!isDark)
    })
  } else {
    // 不支持则直接切换,无动画
    layout.toggleDark(!isDark)
  }
}
