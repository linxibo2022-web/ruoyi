// 国际化
import { useI18n as useVueI18n } from 'vue-i18n'
import i18n from '@/locales/i18n'
import type { LanguageType } from '@/locales/i18n'
import el_zhCn from 'element-plus/es/locale/lang/zh-cn'
import { LanguageCode } from '@/systemConfig'

/**
 * 增强的国际化钩子 (useI18n)
 *
 * 扩展了Vue I18n的原生useI18n钩子，提供更多实用功能，
 * 并与应用状态管理集成。
 *
 * 包含以下功能：
 * - 翻译功能:
 *   - t: 增强的翻译函数，支持多语言字段国际化
 *   - translateRouteTitle: 翻译路由标题的便捷方法
 *
 * - 语言管理:
 *   - currentLanguage: 当前语言（响应式）
 *   - currentLanguageName: 当前语言的显示名称
 *   - languages: 可用语言列表
 *   - setLanguage: 设置应用语言
 *   - initLanguage: 初始化语言设置
 *
 * - 语言状态:
 *   - isChinese: 检查当前是否为中文环境
 *   - isEnglish: 检查当前是否为英文环境
 *
 * - Vue I18n原生功能:
 *   - locale: 当前语言（响应式）
 *   - availableLocales: 可用语言列表
 *   - fallbackLocale: 备用语言
 *   - messages: 所有语言消息
 *   - d: 日期格式化
 *   - n: 数字格式化
 *   - rt: 运行时翻译
 *   - te: 检查翻译键是否存在
 *   - tm: 翻译消息函数
 */
export const useI18n = () => {
  // 获取Vue I18n的原始钩子
  const vueI18n = useVueI18n()

  // 获取布局状态管理
  const layout = useLayout()

  /**
   * 增强的翻译函数，支持多语言字段国际化
   * @param key 翻译键名
   * @param fieldInfoOrValue 字段信息对象或多语言配置对象或中文翻译值
   * @returns 翻译后的文本
   * @example
   * <template>
   *   <!-- 1. 只使用 field (字段名) -->
   *   <el-table-column :label="t('', { field: 'UserName' })" prop="userName" />
   *   <!-- 英文环境: "UserName", 中文环境: "UserName" -->
   *
   *   <!-- 2. 只使用 comment (字段备注) -->
   *   <el-table-column :label="t('', { comment: '用户名' })" prop="userName" />
   *   <!-- 英文环境: "用户名", 中文环境: "用户名" -->
   *
   *   <!-- 3. 同时使用 field 和 comment -->
   *   <el-table-column :label="t('', { field: 'UserName', comment: '用户名' })" prop="userName" />
   *   <!-- 英文环境: "UserName", 中文环境: "用户名" -->
   *
   *   <!-- 4. 使用语言特定的翻译 -->
   *   <el-table-column
   *     :label="t('', { [LanguageCode.zh_CN]: '用户名', [LanguageCode.en_US]: 'UserName' })"
   *     prop="userName"
   *   />
   *   <!-- 英文环境: "UserName", 中文环境: "用户名" -->
   *
   *   <!-- 5. 混合使用字段名、备注和语言映射 -->
   *   <el-table-column
   *     :label="t('', {
   *       field: 'UserName',
   *       comment: '用户名(默认)',
   *       [LanguageCode.zh_CN]: '用户名(中文)'
   *     })"
   *     prop="userName"
   *   />
   *   <!-- 英文环境: "UserName", 中文环境: "用户名(中文)" -->
   *
   *   <!-- 6. 使用传统的i18n键 -->
   *   <el-table-column :label="t('user.userName')" prop="userName" />
   *   <!-- 根据i18n配置翻译 'user.userName' 键 -->
   * </template>
   *
   * <script setup>
   * import { useI18n } from '@/composables/useI18n';
   * import { LanguageCode } from '@/types/enums/languageCode';
   *
   * const { t } = useI18n();
   * </script>
   */
  const t = (
    key: ObjKeysToUnion<LanguageType & typeof el_zhCn> | string,
    fieldInfoOrValue?:
      | ({
          field?: string
          comment?: string
        } & Partial<Record<LanguageCode, string>> &
          Record<string, any>)
      | string
  ): string => {
    // 处理简单用法：t('userId', '用户id')
    if (typeof fieldInfoOrValue === 'string') {
      // 先检查键是否存在于语言文件中（如 validation.xxx）
      if (vueI18n.te(key)) {
        return vueI18n.t(key) as string
      }

      const currentLang = currentLanguage.value

      // 如果是中文环境，直接返回第二个参数（中文翻译）
      if (currentLang === LanguageCode.zh_CN) {
        return fieldInfoOrValue
      }
      // 如果是英文或其他环境，返回第一个参数（英文）
      else {
        return key
      }
    }

    // 优先使用常规国际化处理（如果 key 存在于语言文件中）
    // 这样可以正确支持命名参数插值，如 t('key', { name: value })
    if (vueI18n.te(key)) {
      return vueI18n.t(key, fieldInfoOrValue as Record<string, unknown>)
    }

    // 只有 key 不存在时，才处理字段信息（用于动态字段翻译）
    if (fieldInfoOrValue) {
      const fieldInfo = fieldInfoOrValue
      const currentLang = currentLanguage.value

      // 优先级1: 使用当前语言的映射
      if (fieldInfo[currentLang]) {
        return fieldInfo[currentLang]
      }

      // 优先级2: 根据当前语言使用 field 或 comment
      if (currentLang === LanguageCode.zh_CN) {
        // 中文环境优先使用 comment
        if ('comment' in fieldInfo && fieldInfo.comment) {
          return fieldInfo.comment
        }
      } else {
        // 非中文环境优先使用 field
        if ('field' in fieldInfo && fieldInfo.field) {
          return fieldInfo.field
        }
      }

      // 优先级3: 降级策略
      if (currentLang === LanguageCode.zh_CN) {
        // 中文环境降级到 field
        if ('field' in fieldInfo && fieldInfo.field) {
          return fieldInfo.field
        }
        // 然后尝试英文
        if (fieldInfo[LanguageCode.en_US]) {
          return fieldInfo[LanguageCode.en_US]
        }
      } else {
        // 非中文环境降级到 comment
        if ('comment' in fieldInfo && fieldInfo.comment) {
          return fieldInfo.comment
        }
        // 然后尝试中文
        if (fieldInfo[LanguageCode.zh_CN]) {
          return fieldInfo[LanguageCode.zh_CN]
        }
      }

      // 优先级4: 使用任何可用的翻译
      const values = Object.entries(fieldInfo)
        .filter(([key, value]) => key !== 'field' && key !== 'comment' && value)
        .map(([_, value]) => value)

      if (values.length > 0) {
        return values[0]
      }
    }

    // 按钮键降级处理
    if (typeof key === 'string' && key.startsWith('button.') && key.split('.').length > 2) {
      // 提取操作类型（最后一部分）
      const parts = key.split('.')
      const action = parts[parts.length - 1]
      const genericButtonKey = `button.${action}`

      // 尝试使用通用按钮键
      if (vueI18n.te(genericButtonKey)) {
        return vueI18n.t(genericButtonKey)
      }
    }

    // 无翻译，返回键名本身
    return key
  }

  /**
   * 当前语言
   * @description 从应用状态中获取当前语言
   */
  const currentLanguage = layout.language

  /**
   * 可用的语言列表
   */
  const languages = computed(() => vueI18n.availableLocales)

  /**
   * 设置语言
   * @param lang 语言代码
   * @description 同时更新Vue I18n和应用状态的语言设置
   */
  const setLanguage = (lang: LanguageCode): boolean => {
    if (vueI18n.availableLocales.includes(lang)) {
      // 设置Vue I18n的locale
      vueI18n.locale.value = lang

      // 同步到布局状态管理
      layout.changeLanguage(lang)

      // 更新HTML的lang属性
      document.querySelector('html')?.setAttribute('lang', lang)

      return true
    }
    console.warn(`Language ${lang} is not available`)
    return false
  }

  /**
   * 获取当前语言的显示名称
   */
  const currentLanguageName = computed(() => {
    switch (currentLanguage.value) {
      case LanguageCode.zh_CN:
        return '简体中文'
      case LanguageCode.en_US:
        return 'English'
      default:
        return currentLanguage.value
    }
  })

  /**
   * 检查当前是否为中文环境
   */
  const isChinese = computed(() => currentLanguage.value === LanguageCode.zh_CN)

  /**
   * 检查当前是否为英文环境
   */
  const isEnglish = computed(() => currentLanguage.value === LanguageCode.en_US)

  /**
   * 初始化语言设置
   * @description 确保Vue I18n的locale与应用状态同步
   */
  const initLanguage = () => {
    // 使用布局状态管理中保存的语言
    vueI18n.locale.value = layout.language.value
    document.querySelector('html')?.setAttribute('lang', layout.language.value)
  }

  return {
    // 强类型的翻译函数
    t,

    // Vue I18n原始属性和方法
    locale: vueI18n.locale, // 当前语言（响应式）
    availableLocales: vueI18n.availableLocales, // 可用语言列表
    fallbackLocale: vueI18n.fallbackLocale, // 备用语言
    messages: vueI18n.messages, // 所有语言消息

    // 格式化方法
    d: vueI18n.d, // 日期格式化
    n: vueI18n.n, // 数字格式化
    rt: vueI18n.rt, // 运行时翻译
    te: vueI18n.te, // 检查翻译键是否存在
    tm: vueI18n.tm, // 翻译消息函数

    // 扩展状态
    currentLanguage, // 当前语言
    currentLanguageName, // 当前语言名称
    languages, // 可用语言列表（别名）
    isChinese, // 是否为中文环境
    isEnglish, // 是否为英文环境

    // 扩展方法
    setLanguage, // 设置语言
    translateRouteTitle, // 翻译路由标题
    initLanguage // 初始化语言设置
  }
}

/**
 * 翻译路由标题的便捷方法
 * @example translateRouteTitle('dashboard')
 */
export const translateRouteTitle = (title: string): string => {
  if (!title) return ''

  const routeKey = `route.${title}` as ObjKeysToUnion<LanguageType>
  const hasKey = i18n.global.te(routeKey)
  if (hasKey) {
    return i18n.global.t(routeKey)
  }

  return title
}

/**
 * 对象键名转联合类型
 * { a: 1, b: { ba: 1, bb: 2} } ---> a | b.ba | b.bb
 */
export type ObjKeysToUnion<T, Depth extends number[] = [0, 1, 2, 3]> = Depth['length'] extends 4
  ? never
  : T extends object
    ? { [K in keyof T]: `${K & string}${T[K] extends object ? `.${ObjKeysToUnion<T[K], [...Depth, 0]>}` : ''}` }[keyof T]
    : never
