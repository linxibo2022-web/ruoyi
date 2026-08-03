<!-- 侧栏链接 -->
<template>
  <!-- 智能链接组件：自动判断内部/外部链接并渲染对应标签 -->
  <component :is="linkComponentType" v-bind="linkAttributes" :class="linkClasses" @click="handleLinkClick">
    <slot />
  </component>
</template>

<script setup lang="ts" name="AppLink">
import { isExternal } from '@/utils/validators'
import type { RouteLocationRaw } from 'vue-router'

/**
 * ===== 组件Props定义 =====
 */

interface AppLinkProps {
  /** 链接地址 - 支持内部路由对象或外部URL字符串 */
  to: string | RouteLocationRaw

  /** 外部链接打开方式 */
  target?: '_blank' | '_self' | '_parent' | '_top'

  /** 外部链接rel属性 */
  rel?: string

  /** 是否禁用链接 */
  disabled?: boolean

  /** 自定义CSS类名 */
  customClass?: string

  /** 是否阻止默认行为（用于自定义点击处理） */
  preventDefault?: boolean
}

const props = withDefaults(defineProps<AppLinkProps>(), {
  target: '_blank',
  rel: 'noopener noreferrer',
  disabled: false,
  customClass: '',
  preventDefault: false
})

/**
 * ===== 组件事件定义 =====
 */

const emit = defineEmits<{
  /** 链接点击事件 */
  'click': [event: Event, linkType: 'internal' | 'external']
}>()

/**
 * ===== 计算属性 =====
 */

// 判断是否为外部链接
const isExternalLink = computed(() => {
  return typeof props.to === 'string' && isExternal(props.to)
})

// 链接类型（内部/外部）
const linkType = computed(() => {
  return isExternalLink.value ? 'external' : 'internal'
})

// 渲染的组件类型
const linkComponentType = computed(() => {
  if (props.disabled) {
    return 'span' // 禁用状态渲染为span
  }
  return isExternalLink.value ? 'a' : 'router-link'
})

// 链接属性配置
const linkAttributes = computed(() => {
  // 禁用状态不设置任何链接属性
  if (props.disabled) {
    return {}
  }

  // 外部链接属性
  if (isExternalLink.value) {
    return {
      href: props.to as string,
      target: props.target,
      rel: props.rel
    }
  }

  // 内部链接属性
  return {
    to: props.to
  }
})

// 链接样式类
const linkClasses = computed(() => [
  props.customClass,
  {
    // 禁用状态样式
    'cursor-not-allowed opacity-50': props.disabled,

    // 链接基础样式
    'transition-colors duration-200': !props.disabled,

    // 外部链接标识（可选）
    'external-link': isExternalLink.value && !props.disabled,

    // 内部链接标识（可选）
    'internal-link': !isExternalLink.value && !props.disabled
  }
])

/**
 * ===== 事件处理方法 =====
 */

/**
 * 处理链接点击事件
 * @param {Event} event - 点击事件对象
 */
const handleLinkClick = (event: Event): void => {
  // 如果是外链且target为_blank，让浏览器自然处理
  if (isExternalLink.value && props.target === '_blank') {
    return // 不阻止默认行为，让链接在新窗口打开
  }

  // 如果禁用或需要阻止默认行为
  if (props.disabled || props.preventDefault) {
    event.preventDefault()
    event.stopPropagation()
  }

  // 禁用状态不触发事件
  if (props.disabled) {
    return
  }

  // 触发自定义点击事件
  emit('click', event, linkType.value)

  // 外部链接额外处理
  if (isExternalLink.value && !props.preventDefault) {
    // 可以在这里添加统计、日志等逻辑
    console.debug('外部链接点击:', props.to)
  }
}

/**
 * ===== 工具方法 =====
 */

/**
 * 检查链接是否有效
 * @returns {boolean} 链接是否有效
 */
const isValidLink = (): boolean => {
  if (!props.to) return false

  if (typeof props.to === 'string') {
    return props.to.trim().length > 0
  }

  return true
}

/**
 * 获取链接的显示文本（用于调试或日志）
 * @returns {string} 链接文本描述
 */
const getLinkDescription = (): string => {
  if (typeof props.to === 'string') {
    return props.to
  }

  if (typeof props.to === 'object' && props.to.path) {
    return props.to.path
  }

  return JSON.stringify(props.to)
}

// 在开发环境下验证链接有效性
if (process.env.NODE_ENV === 'development') {
  watchEffect(() => {
    if (!isValidLink()) {
      console.warn(`AppLink: 无效的链接地址`, props.to)
    }
  })
}
</script>

<style lang="scss" scoped>
/* 外部链接样式 */
.external-link {
  /* 可以添加外部链接图标 */
  &::after {
    content: '↗';
    display: inline-block;
    margin-left: 4px;
    font-size: 0.75em;
    opacity: 0.7;
    transition: opacity 0.2s ease;
  }

  &:hover::after {
    opacity: 1;
  }
}

/* 内部链接样式 */
.internal-link {
  /* 内部链接的特定样式 */
  &:hover {
    text-decoration: none;
  }
}

/* 禁用状态样式 */
.cursor-not-allowed {
  &:hover {
    text-decoration: none !important;
  }

  &::after {
    display: none !important;
  }
}

/* 链接基础样式 */
a,
:deep(.router-link-active),
:deep(.router-link-exact-active) {
  text-decoration: none;

  &:hover {
    text-decoration: none;
  }
}
</style>
