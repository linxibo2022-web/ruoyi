/**
 * 字典数据管理 (useDictStore)
 *
 * 基于 Pinia 的字典数据管理模块，提供统一的字典数据存储、访问和转换功能。
 *
 * 包含以下功能：
 * - 字典存储: 集中管理应用中的所有字典数据 (dict)
 * - 字典访问: 快速获取指定类型的字典数据 (getDict)
 * - 字典设置: 动态添加或更新字典项 (setDict)
 * - 标签转换: 根据字典值获取对应的显示标签 (getDictLabel, getDictLabels)
 * - 值转换: 根据显示标签获取对应的字典值 (getDictValue)
 * - 对象获取: 获取字典项的完整对象数据 (getDictItem)
 * - 数据清理: 支持删除单个或所有字典数据 (removeDict, cleanDict)
 * - 国际化支持: 自动优先使用 i18n 翻译，键格式为 dict.{dictType}.{value}
 */

import i18n from '@/locales/i18n'

/**应用模块名称*/
const DICT_MODULE = 'dict'

/**
 * 获取字典标签的国际化翻译
 * @param dictType 字典类型
 * @param value 字典值
 * @returns 国际化翻译或 null
 */
const getDictI18nLabel = (dictType: string, value: string | number): string | null => {
  const key = `dict.${dictType}.${String(value)}`
  // 检查翻译键是否存在
  if (i18n.global.te(key)) {
    return i18n.global.t(key)
  }
  return null
}

export const useDictStore = defineStore(DICT_MODULE, () => {
  /**
   * 字典数据集合
   * @description 使用Map存储多个字典数据，key为字典类型，value为字典选项数组
   * @example
   * {
   *   'sys_user_gender': [
   *     { label: '男', value: '0' ... },
   *     { label: '女', value: '1' ... }
   *   ],
   *   'sys_enable_status': [
   *     { label: '正常', value: '0' ... },
   *     { label: '停用', value: '1' ... }
   *   ]
   * }
   */
  const dict = ref<Map<string, DictItem[]>>(new Map())

  /**
   * 获取字典
   * @param key 字典key
   * @returns 字典数据数组或null
   * @example getDict('sys_user_gender')
   */
  const getDict = (key: string): DictItem[] | null => {
    if (!key) {
      return null
    }
    return dict.value.get(key) || null
  }

  /**
   * 设置字典
   * @param key 字典key
   * @param value 字典value
   * @returns 是否设置成功
   * @example
   * setDict('sys_user_gender', [
   *   { label: '男', value: '0' },
   *   { label: '女', value: '1' }
   * ])
   */
  const setDict = (key: string, value: DictItem[]): boolean => {
    if (!key) {
      return false
    }
    try {
      dict.value.set(key, value)
      return true
    } catch (e) {
      console.error('设置字典时发生错误:', e)
      return false
    }
  }

  /**
   * 根据字典类型或字典数据和值获取标签（支持国际化）
   * @param keyOrData 字典类型或字典数据
   * @param value 字典值
   * @param dictType 字典类型（当 keyOrData 为数组时，需要传入此参数以支持 i18n）
   * @returns 对应的标签名
   * @description
   * - 优先使用 i18n 翻译（键格式：dict.{dictType}.{value}）
   * - 如果没有 i18n 翻译，则使用数据库中的标签
   * @example
   * getDictLabel('sys_user_gender', '0') // 优先返回 i18n 翻译，否则返回 '男'
   * getDictLabel(dictData, '0', 'sys_user_gender') // 使用字典数据并传入类型以支持 i18n
   */
  const getDictLabel = (
    keyOrData: string | Ref<DictItem[]> | DictItem[],
    value: string | number,
    dictType?: string
  ): string => {
    let dictData: Ref<DictItem[]> | undefined
    let actualDictType: string | undefined = dictType

    if (typeof keyOrData === 'string') {
      actualDictType = keyOrData
      dictData = ref(getDict(keyOrData))
    } else if (isRef(keyOrData)) {
      dictData = keyOrData
    } else {
      // 处理 DictItem[] 类型
      dictData = ref(keyOrData)
    }

    // 优先尝试获取 i18n 翻译
    if (actualDictType) {
      const i18nLabel = getDictI18nLabel(actualDictType, value)
      if (i18nLabel) {
        return i18nLabel
      }
    }

    // 降级到数据库中的标签
    if (!dictData) return ''
    const item = dictData.value.find((item) => item.value === String(value))
    return item ? item.label : ''
  }

  /**
   * 批量获取字典标签（支持国际化）
   * @param keyOrData 字典类型或字典数据
   * @param values 字典值数组
   * @param dictType 字典类型（当 keyOrData 为数组时，需要传入此参数以支持 i18n）
   * @returns 对应的标签数组
   * @description
   * - 优先使用 i18n 翻译（键格式：dict.{dictType}.{value}）
   * - 如果没有 i18n 翻译，则使用数据库中的标签
   * @example
   * getDictLabels('sys_user_gender', ['0', '1']) // 返回 ['男', '女'] 或 i18n 翻译
   * getDictLabels(dictData, ['0', '1'], 'sys_user_gender') // 使用字典数据并传入类型以支持 i18n
   */
  const getDictLabels = (
    keyOrData: string | Ref<DictItem[]>,
    values: (string | number)[],
    dictType?: string
  ): string[] => {
    if (!values || values.length === 0) return []

    let dictData: Ref<DictItem[]> | undefined
    let actualDictType: string | undefined = dictType

    if (typeof keyOrData === 'string') {
      actualDictType = keyOrData
      dictData = ref(getDict(keyOrData))
    } else {
      dictData = keyOrData
    }

    if (!dictData) return values.map(() => '')

    return values.map((value) => {
      // 优先尝试获取 i18n 翻译
      if (actualDictType) {
        const i18nLabel = getDictI18nLabel(actualDictType, value)
        if (i18nLabel) {
          return i18nLabel
        }
      }
      // 降级到数据库中的标签
      const item = dictData.value.find((item) => item.value === value)
      return item ? item.label : ''
    })
  }

  /**
   * 获取字典项的完整对象
   * @param keyOrData 字典类型或字典数据
   * @param value 字典值
   * @returns 完整的字典项对象或null
   * @example
   * getDictItem('sys_user_gender', '0') // 返回 { label: '男', value: '0' }
   * getDictItem(dictData, '0') // 直接使用字典数据获取字典项
   */
  const getDictItem = (keyOrData: string | DictItem[], value: string | number): DictItem | null => {
    let dictData: DictItem[] | undefined

    if (typeof keyOrData === 'string') {
      dictData = getDict(keyOrData)
    } else {
      dictData = keyOrData
    }

    if (!dictData) return null

    return dictData.find((item) => item.value === value) || null
  }
  /**
   * 根据标签获取字典值
   * @param key 字典类型
   * @param label 字典标签
   * @returns 对应的字典值
   * @example getDictValue('sys_user_gender', '男') // 返回 '0'
   */
  const getDictValue = (key: string, label: string): string | number | null => {
    const dictData = getDict(key)
    if (!dictData) return null

    const item = dictData.find((item) => item.label === label)
    return item ? item.value : null
  }

  /**
   * 删除字典
   * @param key 字典key
   * @returns 是否删除成功
   * @example removeDict('sys_user_gender')
   */
  const removeDict = (key: string): boolean => {
    if (!key) {
      return false
    }
    try {
      return dict.value.delete(key)
    } catch (e) {
      console.error('删除字典时发生错误:', e)
      return false
    }
  }

  /**
   * 清空字典
   * @description 清空所有字典数据
   * @example cleanDict()
   */
  const cleanDict = (): void => {
    dict.value.clear()
  }

  return {
    dict,
    getDict,
    setDict,
    getDictLabel,
    getDictLabels,
    getDictItem,
    getDictValue,
    removeDict,
    cleanDict
  }
})
