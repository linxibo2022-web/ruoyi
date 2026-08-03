// 自定义国际化配置
import { createI18n } from 'vue-i18n'

import zh_CN from '@/locales/zh_CN'
import en_US from '@/locales/en_US'
// 引入Element Plus的语言包
import el_en from 'element-plus/es/locale/lang/en'
import el_zhCn from 'element-plus/es/locale/lang/zh-cn'
import { LanguageCode } from '@/systemConfig'

/**
 * 获取当前语言
 * @description 从本地存储中获取用户设置的语言，如果没有则使用默认语言（中文）
 * @returns 当前语言代码，如 zh_CN 或 en_US
 */
export const getLanguage = (): LanguageCode => {
  const layout = useLayout()

  // 如果存在有效的语言配置则返回，否则使用默认语言
  if (layout.language.value) {
    return layout.language.value
  }
  return LanguageCode.zh_CN
}

/**
 * 创建 i18n 实例
 * @description 配置国际化实例，设置默认语言和翻译消息
 */
const i18n = createI18n({
  globalInjection: true, // 全局注入 $t, $d 等方法到模板中
  allowComposition: true, // 允许组合式 API
  legacy: false, // 使用 Vue 3 Composition API 模式
  locale: getLanguage(), // 设置当前语言
  messages: {
    zh_CN: {
      ...zh_CN,
      // 添加Element Plus的中文语言包（只合并 el 命名空间，避免 name 键冲突）
      el: el_zhCn.el
    },
    en_US: {
      ...en_US,
      // 添加Element Plus的英文语言包（只合并 el 命名空间，避免 name 键冲突）
      el: el_en.el
    }
  }
})

export default i18n

// 导出语言包类型，用于类型检查和自动补全
export type LanguageType = typeof zh_CN
