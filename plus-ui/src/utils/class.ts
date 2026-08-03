/**
 * DOM 元素 class 操作相关工具函数
 *
 * 包含以下功能：
 * - 类名检查: 检查元素是否包含指定类名 (hasClass)
 * - 类名添加: 向元素添加类名 (addClass)
 * - 类名移除: 从元素移除类名 (removeClass)
 * - 类名切换: 切换元素的类名 (toggleClass)
 * - 类名替换: 替换元素的类名 (replaceClass)
 * - 类名设置: 设置元素的类名（替换所有现有类名）(setClass)
 * - 类名获取: 获取元素的所有类名数组 (getClassList)
 */

/**
 * 检查元素是否包含指定类名
 * @param {HTMLElement} element - 要检查的 DOM 元素
 * @param {string} className - 要检查的类名
 * @returns {boolean} - 是否包含该类名
 */
export const hasClass = (element: HTMLElement, className: string): boolean => {
  if (!element || !className) {
    return false
  }
  return !!element.className.match(new RegExp('(\\s|^)' + className + '(\\s|$)'))
}

/**
 * 向元素添加类名
 * @param {HTMLElement} element - 目标 DOM 元素
 * @param {string} className - 要添加的类名
 */
export const addClass = (element: HTMLElement, className: string): void => {
  if (!element || !className) {
    return
  }

  if (!hasClass(element, className)) {
    element.className = element.className.trim() + ' ' + className
  }
}

/**
 * 从元素移除类名
 * @param {HTMLElement} element - 目标 DOM 元素
 * @param {string} className - 要移除的类名
 */
export const removeClass = (element: HTMLElement, className: string): void => {
  if (!element || !className) {
    return
  }

  if (hasClass(element, className)) {
    const reg = new RegExp('(\\s|^)' + className + '(\\s|$)')
    element.className = element.className.replace(reg, ' ').trim()
  }
}

/**
 * 切换元素的类名（有则移除，无则添加）
 * @param {HTMLElement} element - 目标 DOM 元素
 * @param {string} className - 要切换的类名
 */
export const toggleClass = (element: HTMLElement, className: string): void => {
  if (!element || !className) {
    return
  }

  if (hasClass(element, className)) {
    removeClass(element, className)
  } else {
    addClass(element, className)
  }
}

/**
 * 替换元素的类名
 * @param {HTMLElement} element - 目标 DOM 元素
 * @param {string} oldClassName - 要替换的旧类名
 * @param {string} newClassName - 替换成的新类名
 */
export const replaceClass = (element: HTMLElement, oldClassName: string, newClassName: string): void => {
  if (!element || !oldClassName || !newClassName) {
    return
  }

  removeClass(element, oldClassName)
  addClass(element, newClassName)
}

/**
 * 设置元素的类名（替换所有现有类名）
 * @param {HTMLElement} element - 目标 DOM 元素
 * @param {string} className - 要设置的类名
 */
export const setClass = (element: HTMLElement, className: string): void => {
  if (!element) {
    return
  }

  element.className = className || ''
}

/**
 * 获取元素的所有类名数组
 * @param {HTMLElement} element - 目标 DOM 元素
 * @returns {string[]} - 类名数组
 */
export const getClassList = (element: HTMLElement): string[] => {
  if (!element || !element.className) {
    return []
  }

  return element.className.trim().split(/\s+/)
}
