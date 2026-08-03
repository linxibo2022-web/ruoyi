// 动画库的公共前缀，用于 Animate.css 类名
const ANIMATE_PREFIX = 'animate__animated '

/**
 * Animation 动画工具 (useAnimation)
 *
 * 基于 Animate.css 的动画工具集，提供了丰富的动画效果和管理功能。
 *
 * 包含以下功能：
 * - 动画效果库: 提供多种预定义的动画效果 (animationEffects)
 * - 动画配置: 创建和管理进入/离开动画配置 (createAnimationConfig)
 * - 预设配置: 常用场景的动画预设 (searchAnimate, menuSearchAnimate, logoAnimate)
 * - 动画控制: 设置、切换和应用动画 (setAnimation, toggleRandomAnimation)
 * - 随机动画: 支持随机动画效果 (getRandomAnimation, isRandomAnimation)
 * - DOM操作: 为DOM元素应用动画效果 (applyAnimation)
 * - 状态管理: 跟踪和控制当前动画状态 (currentAnimation, currentConfig)
 */
export const animationEffects = {
  // 无动画
  EMPTY: '',
  // 脉冲效果：轻微缩放和颤动
  PULSE: `${ANIMATE_PREFIX}animate__pulse`,
  // 橡皮筋效果：元素弹性拉伸
  RUBBER_BAND: `${ANIMATE_PREFIX}animate__rubberBand`,
  // 弹跳进入：从小变大
  BOUNCE_IN: `${ANIMATE_PREFIX}animate__bounceIn`,
  // 从左侧弹跳进入
  BOUNCE_IN_LEFT: `${ANIMATE_PREFIX}animate__bounceInLeft`,
  // 渐入效果：从透明到不透明
  FADE_IN: `${ANIMATE_PREFIX}animate__fadeIn`,
  // 从左侧渐入
  FADE_IN_LEFT: `${ANIMATE_PREFIX}animate__fadeInLeft`,
  // 从上方渐入
  FADE_IN_DOWN: `${ANIMATE_PREFIX}animate__fadeInDown`,
  // 从下方渐入
  FADE_IN_UP: `${ANIMATE_PREFIX}animate__fadeInUp`,
  // X轴翻转进入
  FLIP_IN_X: `${ANIMATE_PREFIX}animate__flipInX`,
  // 从左侧光速进入
  LIGHT_SPEED_IN_LEFT: `${ANIMATE_PREFIX}animate__lightSpeedInLeft`,
  // 从左下方旋转进入
  ROTATE_IN_DOWN_LEFT: `${ANIMATE_PREFIX}animate__rotateInDownLeft`,
  // 滚动进入
  ROLL_IN: `${ANIMATE_PREFIX}animate__rollIn`,
  // 缩放进入
  ZOOM_IN: `${ANIMATE_PREFIX}animate__zoomIn`,
  // 从上方缩放进入
  ZOOM_IN_DOWN: `${ANIMATE_PREFIX}animate__zoomInDown`,
  // 从左侧滑入
  SLIDE_IN_LEFT: `${ANIMATE_PREFIX}animate__slideInLeft`,
  // 光速进入
  LIGHT_SPEED_IN: `${ANIMATE_PREFIX}animate__lightSpeedIn`,
  // 渐出效果
  FADE_OUT: `${ANIMATE_PREFIX}animate__fadeOut`
}

/**
 * 动画配置接口
 * 定义进入和离开动画的结构
 */
export interface AnimationConfig {
  // 进入时的动画
  enter: string
  // 离开时的动画
  leave: string
}

/**
 * 随机动画列表
 */
export const animateList: string[] = Object.values(animationEffects).filter((effect) => effect !== animationEffects.EMPTY)

/**
 * 默认动画效果
 */
export const defaultAnimate = animationEffects.FADE_IN

/**
 * 创建动画配置
 * @param enterAnimation 进入动画
 * @param leaveAnimation 离开动画
 * @returns 动画配置对象
 */
export const createAnimationConfig = (
  enterAnimation: string = animationEffects.FADE_IN,
  leaveAnimation: string = animationEffects.FADE_OUT
): AnimationConfig => {
  return {
    enter: enterAnimation,
    leave: leaveAnimation
  }
}

// 预定义的常用动画配置
// 搜索动画配置
export const searchAnimate = createAnimationConfig(animationEffects.EMPTY, animationEffects.EMPTY)
// 菜单搜索动画配置
export const menuSearchAnimate = createAnimationConfig(animationEffects.FADE_IN, animationEffects.FADE_OUT)
// Logo 动画配置
export const logoAnimate = createAnimationConfig(animationEffects.FADE_IN, animationEffects.FADE_OUT)

/**
 * 动画钩子函数
 * 提供动画相关功能和状态管理
 */
export const useAnimation = () => {
  // 当前启用的动画
  const currentAnimation = ref<string>(defaultAnimate)

  // 是否启用随机动画
  const isRandomAnimation = ref<boolean>(false)

  // 当前动画配置
  const currentConfig = ref<AnimationConfig>({
    enter: animationEffects.FADE_IN,
    leave: animationEffects.FADE_OUT
  })

  /**
   * 获取随机动画效果
   * @returns 随机动画类名
   */
  const getRandomAnimation = (): string => {
    const index = Math.floor(Math.random() * animateList.length)
    return animateList[index]
  }

  /**
   * 设置当前动画
   * @param animation 动画类名
   */
  const setAnimation = (animation: string): void => {
    currentAnimation.value = animation
  }

  /**
   * 切换随机动画模式
   * @param value 是否启用随机动画
   */
  const toggleRandomAnimation = (value?: boolean): void => {
    isRandomAnimation.value = value !== undefined ? value : !isRandomAnimation.value
  }

  /**
   * 设置当前动画配置
   * @param config 动画配置
   */
  const setAnimationConfig = (config: AnimationConfig): void => {
    currentConfig.value = config
  }

  /**
   * 获取下一个动画
   * 如果启用随机模式则返回随机动画，否则返回当前动画
   */
  const nextAnimation = computed(() => {
    if (isRandomAnimation.value) {
      return getRandomAnimation()
    }
    return currentAnimation.value
  })

  /**
   * 为元素应用动画
   * @param element DOM元素
   * @param animation 动画类名
   * @param callback 动画结束后的回调
   */
  const applyAnimation = (element: HTMLElement, animation: string = nextAnimation.value, callback?: () => void): void => {
    // 移除可能存在的动画类
    element.classList.forEach((cls) => {
      if (cls.startsWith('animate__')) {
        element.classList.remove(cls)
      }
    })

    // 添加新动画类
    animation.split(' ').forEach((cls) => {
      if (cls) element.classList.add(cls)
    })

    // 动画结束后执行回调
    if (callback) {
      const handleAnimationEnd = () => {
        callback()
        element.removeEventListener('animationend', handleAnimationEnd)
      }
      element.addEventListener('animationend', handleAnimationEnd)
    }
  }

  return {
    // 状态
    currentAnimation,
    isRandomAnimation,
    currentConfig,
    nextAnimation,

    // 方法
    getRandomAnimation,
    setAnimation,
    toggleRandomAnimation,
    createAnimationConfig,
    setAnimationConfig,
    applyAnimation,

    // 预定义配置
    searchAnimate,
    menuSearchAnimate,
    logoAnimate,

    // 常量
    animationEffects,
    animateList,
    defaultAnimate
  }
}
