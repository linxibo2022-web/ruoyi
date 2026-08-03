<!-- 语言切换 -->
<template>
  <el-tooltip :content="showTooltip ? t('navbar.language') : ''" :disabled="!showTooltip" effect="dark" placement="bottom" :offset="6">
    <div :class="containerClass">
      <el-dropdown trigger="hover" @command="handleLanguageChange">
        <div :class="triggerClass">
          <Icon code="globe" :size="iconSize" :animate="iconAnimate" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :disabled="isChinese" command="zh_CN">中文</el-dropdown-item>
            <el-dropdown-item :disabled="isEnglish" command="en_US">English</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts" name="LangSelect">
import { AnimateType } from '@/components/Icon/Icon.vue'
const { t, setLanguage, isChinese, isEnglish } = useI18n()
import { LanguageCode } from '@/systemConfig'
import { showMsgSuccess, showLoading, hideLoading } from '@/utils/modal'

/**
 * 组件属性
 */
interface LangSelectProps {
  /** 是否显示提示框 */
  showTooltip?: boolean
  /** 是否显示动画效果 */
  showAnimate?: boolean
  /** 是否显示背景悬停效果 */
  showBackground?: boolean
}

const props = withDefaults(defineProps<LangSelectProps>(), {
  showTooltip: true,
  showAnimate: true,
  showBackground: true
})

// 计算容器类名
const containerClass = computed(() => {
  return props.showBackground ? 'flex-center h-full px-1' : ''
})

// 计算触发器类名
const triggerClass = computed(() => {
  return props.showBackground ? 'navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer' : 'simple-trigger'
})

// 计算图标尺寸
const iconSize = computed(() => {
  return props.showBackground ? 'md' : '20px'
})

// 计算图标动画
const iconAnimate = computed(() => {
  return (props.showAnimate ? 'moveUp' : undefined) as AnimateType
})

// 定义语言切换成功的消息
const messages = {
  [LanguageCode.zh_CN]: '切换语言成功！',
  [LanguageCode.en_US]: 'Switch Language Successful!'
}

/**
 * 处理语言切换
 * 使用 loading 遮罩 + requestAnimationFrame 延迟，避免 UI 卡顿
 * @param lang 目标语言
 */
const handleLanguageChange = (lang: LanguageCode) => {
  // 1. 立即显示全局 loading 遮罩，遮盖渲染过程
  showLoading({
    text: lang === LanguageCode.zh_CN ? '切换中...' : 'Switching...',
    background: 'rgba(255, 255, 255, 0.7)'
  })

  // 2. 使用 requestAnimationFrame 延迟执行，让 loading 先渲染出来
  requestAnimationFrame(() => {
    // 使用增强版钩子的 setLanguage 方法
    // 这会同时更新 Vue I18n 的 locale 和 layout 中的语言设置
    setLanguage(lang)

    // 3. 等待 DOM 更新完成后关闭 loading
    nextTick(() => {
      // 额外延迟 100ms 确保所有组件完成渲染
      setTimeout(() => {
        hideLoading()
        // 显示成功消息，优先使用对应语言的消息，否则使用默认中文
        showMsgSuccess(messages[lang] || '切换语言成功！')
      }, 100)
    })
  })
}
</script>

<style lang="scss" scoped>
// 简单触发器样式 (用于登录页、注册页等)
.simple-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s;
  font-size: 18px;
  color: var(--el-text-color-regular);

  &:hover {
    color: var(--el-color-primary);
  }
}
</style>
