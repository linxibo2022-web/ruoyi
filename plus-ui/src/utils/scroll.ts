/**
 * 滚动相关工具函数
 *
 * 包含以下功能：
 * - 滚动动画控制: 提供动画缓动和平滑切换 (easeInOutQuad, requestAnimFrame)
 * - 滚动位置操作: 获取和设置页面滚动位置 (getScrollPosition, setScrollPosition)
 * - 平滑滚动导航: 滚动到指定位置或元素 (scrollTo, scrollToTop, scrollToElement)
 * - 可见性检测: 检查元素是否在视口内可见 (isElementInViewport)
 */

/**
 * 二次缓动函数 - 平滑的开始和结束动画
 * @param t 当前时间
 * @param b 起始值
 * @param c 变化量
 * @param d 持续时间
 * @returns {number} 当前时间对应的值
 */
const easeInOutQuad = (t: number, b: number, c: number, d: number): number => {
  t /= d / 2
  if (t < 1) {
    return (c / 2) * t * t + b
  }
  t--
  return (-c / 2) * (t * (t - 2) - 1) + b
}

/**
 * requestAnimationFrame 的跨浏览器兼容版本
 * 用于智能动画控制，提供更平滑的动画体验
 */
const requestAnimFrame = (() => {
  return (
    window.requestAnimationFrame ||
    (window as any).webkitRequestAnimationFrame ||
    (window as any).mozRequestAnimationFrame ||
    function (callback: FrameRequestCallback) {
      window.setTimeout(callback, 1000 / 60)
    }
  )
})()

/**
 * 获取当前页面的滚动位置
 * @returns {number} 当前滚动位置（像素）
 */
export const getScrollPosition = (): number => {
  return document.documentElement.scrollTop || (document.body.parentNode as HTMLElement).scrollTop || document.body.scrollTop
}

/**
 * 设置页面滚动位置
 * 由于难以确定具体的滚动元素，这里同时设置所有可能的元素
 * @param position 滚动位置（像素）
 */
export const setScrollPosition = (position: number): void => {
  document.documentElement.scrollTop = position
  ;(document.body.parentNode as HTMLElement).scrollTop = position
  document.body.scrollTop = position
}

/**
 * 平滑滚动到指定位置
 * @param to 目标位置（像素）
 * @param duration 动画持续时间（毫秒），默认为500ms
 * @param callback 滚动完成后的回调函数
 */
export const scrollTo = (to: number, duration: number = 500, callback?: () => void): void => {
  const start = getScrollPosition()
  const change = to - start
  const increment = 20
  let currentTime = 0

  const animateScroll = () => {
    // 增加时间
    currentTime += increment
    // 使用二次缓动函数计算当前值
    const val = easeInOutQuad(currentTime, start, change, duration)
    // 设置滚动位置
    setScrollPosition(val)

    // 如果动画未完成，继续执行动画
    if (currentTime < duration) {
      requestAnimFrame(animateScroll)
    } else {
      // 动画完成，执行回调
      if (callback && typeof callback === 'function') {
        callback()
      }
    }
  }

  animateScroll()
}

/**
 * 滚动到页面顶部
 * @param duration 动画持续时间（毫秒），默认为500ms
 * @param callback 滚动完成后的回调函数
 */
export const scrollToTop = (duration: number = 500, callback?: () => void): void => {
  scrollTo(0, duration, callback)
}

/**
 * 滚动到指定元素
 * @param element 目标元素或元素选择器
 * @param offset 偏移量（像素），默认为0
 * @param duration 动画持续时间（毫秒），默认为500ms
 * @param callback 滚动完成后的回调函数
 */
export const scrollToElement = (element: HTMLElement | string, offset: number = 0, duration: number = 500, callback?: () => void): void => {
  let targetElement: HTMLElement | null

  if (typeof element === 'string') {
    targetElement = document.querySelector(element)
  } else {
    targetElement = element
  }

  if (targetElement) {
    const elementPosition = targetElement.getBoundingClientRect().top + getScrollPosition()
    scrollTo(elementPosition + offset, duration, callback)
  } else {
    console.warn('Target element not found')
    if (callback) callback()
  }
}

/**
 * 检查元素是否在视口内
 * @param element 要检查的元素
 * @param partiallyVisible 是否计算部分可见（默认为false，表示完全可见）
 * @returns {boolean} 元素是否在视口内
 */
export const isElementInViewport = (element: HTMLElement, partiallyVisible: boolean = false): boolean => {
  const rect = element.getBoundingClientRect()
  const windowHeight = window.innerHeight || document.documentElement.clientHeight

  return partiallyVisible ? rect.top < windowHeight && rect.bottom >= 0 : rect.top >= 0 && rect.bottom <= windowHeight
}
