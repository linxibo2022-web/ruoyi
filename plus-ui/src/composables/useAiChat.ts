/**
 * AI对话组合式函数
 * 封装AI调用的通用逻辑
 * @author 抓蛙师
 * @date 2025-01-26
 */

import { ref } from 'vue'
import { aiChat, aiOptimize, aiGenerate, aiReview, aiTranslate } from '@/api/business/base/ai/aiApi'
import type {
  AiChatBo,
  AiChatVo,
  AiFeatureType,
  AiOptimizeOptions,
  AiGenerateOptions,
  AiReviewOptions,
  AiTranslateOptions,
  FieldSchema
} from '@/api/business/base/ai/aiTypes'

/** AI调用选项 */
export interface UseAiChatOptions {
  /** 模型提供商 */
  provider?: string
  /** 模型名称 */
  modelName?: string
  /** 温度参数 */
  temperature?: number
  /** 系统提示词 */
  systemPrompt?: string
  /** 成功回调 */
  onSuccess?: (response: string) => void
  /** 错误回调 */
  onError?: (error: Error) => void
}

/**
 * AI对话基础Hook
 */
export function useAiChat(options: UseAiChatOptions = {}) {
  const { provider = 'deepseek', modelName, temperature = 0.7, systemPrompt, onSuccess, onError } = options

  /** 加载状态 */
  const loading = ref(false)

  /** 响应内容 */
  const response = ref('')

  /** Token使用情况 */
  const tokenUsage = ref<AiChatVo['tokenUsage'] | null>(null)

  /** 响应时间 */
  const responseTime = ref<number | null>(null)

  /** 错误信息 */
  const error = ref<Error | null>(null)

  /**
   * 发送消息
   */
  const sendMessage = async (message: string, aiFeature?: AiFeatureType, customOptions?: Partial<UseAiChatOptions>): Promise<string> => {
    loading.value = true
    response.value = ''
    error.value = null
    tokenUsage.value = null
    responseTime.value = null

    const finalOptions = { ...options, ...customOptions }

    try {
      const aiChatBo: AiChatBo = {
        message,
        aiFeature,
        provider: finalOptions.provider || provider,
        modelName: finalOptions.modelName || modelName,
        temperature: finalOptions.temperature || temperature,
        systemPrompt: finalOptions.systemPrompt || systemPrompt
      }

      const [err, result] = await aiChat(aiChatBo)

      if (err) {
        error.value = err
        if (onError) {
          onError(err)
        }
        throw err
      }

      if (!result) {
        const noDataError = new Error('响应数据为空')
        error.value = noDataError
        if (onError) {
          onError(noDataError)
        }
        throw noDataError
      }

      response.value = result.content
      tokenUsage.value = result.tokenUsage
      responseTime.value = result.responseTime

      if (onSuccess) {
        onSuccess(response.value)
      }

      return response.value
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置状态
   */
  const reset = () => {
    loading.value = false
    response.value = ''
    error.value = null
    tokenUsage.value = null
    responseTime.value = null
  }

  return {
    loading,
    response,
    tokenUsage,
    responseTime,
    error,
    sendMessage,
    reset
  }
}

/**
 * AI文本优化Hook
 */
export function useAiTextOptimize(options: UseAiChatOptions = {}) {
  const { loading, response, error, tokenUsage, responseTime } = useAiChat(options)

  const optimize = async (text: string, type: AiOptimizeOptions['type'] = 'polish', optimizeOptions?: AiOptimizeOptions): Promise<string> => {
    loading.value = true
    response.value = ''
    error.value = null

    const typePrompts = {
      polish: '请润色以下内容，使其更加流畅专业',
      expand: '请扩写以下内容，增加细节和描述',
      shorten: '请精简以下内容，保留核心信息',
      formal: '请将以下内容改写为正式专业的表达',
      casual: '请将以下内容改写为轻松口语化的表达',
      marketing: '请将以下内容改写为营销推广文案'
    }

    let prompt = `${typePrompts[type]}：\n\n${text}\n\n`

    if (optimizeOptions?.maxLength) {
      prompt += `最多${optimizeOptions.maxLength}字。`
    }
    if (optimizeOptions?.minLength) {
      prompt += `至少${optimizeOptions.minLength}字。`
    }
    if (optimizeOptions?.keywords && optimizeOptions.keywords.length > 0) {
      prompt += `必须包含：${optimizeOptions.keywords.join('、')}。`
    }
    if (optimizeOptions?.style) {
      prompt += `风格：${optimizeOptions.style}。`
    }

    prompt += '\n请直接输出优化后的内容，不要添加解释。'

    try {
      const aiChatBo: AiChatBo = {
        message: prompt,
        aiFeature: 'optimize',
        provider: options.provider,
        modelName: options.modelName,
        temperature: options.temperature,
        systemPrompt: options.systemPrompt
      }

      const [err, result] = await aiOptimize(aiChatBo)

      if (err) {
        error.value = err
        throw err
      }

      if (!result) {
        const noDataError = new Error('响应数据为空')
        error.value = noDataError
        throw noDataError
      }

      response.value = result.content
      tokenUsage.value = result.tokenUsage
      responseTime.value = result.responseTime

      return response.value
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    optimize,
    loading,
    response,
    error,
    tokenUsage,
    responseTime
  }
}

/**
 * AI数据生成Hook
 */
export function useAiDataGenerate(options: UseAiChatOptions = {}) {
  const { loading, response, error, tokenUsage, responseTime } = useAiChat(options)

  const generate = async (schema: FieldSchema[], count: number = 10, generateOptions?: AiGenerateOptions): Promise<any[]> => {
    loading.value = true
    response.value = ''
    error.value = null

    let prompt = `请生成${count}条数据。\n\n`
    prompt += '字段定义：\n'
    schema.forEach((field) => {
      prompt += `- ${field.label}（${field.name}）: ${field.type}`
      if (field.required) {
        prompt += ' [必填]'
      }
      if (field.example) {
        prompt += ` 示例: ${field.example}`
      }
      prompt += '\n'
    })

    if (generateOptions?.dataType === 'realistic') {
      prompt += '\n要求：数据要真实、合理，字段之间要有逻辑关联。\n'
    }

    prompt += '\n请返回JSON数组格式，不要添加markdown标记。'

    try {
      const aiChatBo: AiChatBo = {
        message: prompt,
        aiFeature: 'generate',
        provider: options.provider,
        modelName: options.modelName,
        temperature: options.temperature || 0.8,
        systemPrompt: options.systemPrompt
      }

      const [err, result] = await aiGenerate(aiChatBo)

      if (err) {
        error.value = err
        throw err
      }

      if (!result) {
        const noDataError = new Error('响应数据为空')
        error.value = noDataError
        throw noDataError
      }

      response.value = result.content
      tokenUsage.value = result.tokenUsage
      responseTime.value = result.responseTime

      // 解析JSON
      const jsonMatch = result.content.match(/\[[\s\S]*\]/)
      if (jsonMatch) {
        const data = JSON.parse(jsonMatch[0])
        return data
      }

      throw new Error('无法解析生成的数据')
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    generate,
    loading,
    response,
    error,
    tokenUsage,
    responseTime
  }
}

/**
 * AI内容审核Hook
 */
export function useAiContentReview(options: UseAiChatOptions = {}) {
  const { loading, response, error, tokenUsage, responseTime } = useAiChat(options)

  const review = async (content: Record<string, any>, reviewOptions?: AiReviewOptions): Promise<any> => {
    loading.value = true
    response.value = ''
    error.value = null

    let prompt = '请审核以下内容：\n\n'

    Object.entries(content).forEach(([key, value]) => {
      prompt += `【${key}】: ${value}\n`
    })

    prompt += '\n审核要求：\n'
    if (reviewOptions?.checkItems?.includes('compliance')) {
      prompt += '- 合规性检查\n'
    }
    if (reviewOptions?.checkItems?.includes('sensitive')) {
      prompt += '- 敏感词检测\n'
    }
    if (reviewOptions?.checkItems?.includes('quality')) {
      prompt += '- 质量评估\n'
    }

    if (reviewOptions?.autoFix) {
      prompt += '\n请为问题提供修复建议。\n'
    }

    prompt += '\n返回JSON格式的审核结果，包含status、score、issues等字段。'

    try {
      const aiChatBo: AiChatBo = {
        message: prompt,
        aiFeature: 'review',
        provider: options.provider,
        modelName: options.modelName,
        temperature: options.temperature || 0.3,
        systemPrompt: options.systemPrompt
      }

      const [err, result] = await aiReview(aiChatBo)

      if (err) {
        error.value = err
        throw err
      }

      if (!result) {
        const noDataError = new Error('响应数据为空')
        error.value = noDataError
        throw noDataError
      }

      response.value = result.content
      tokenUsage.value = result.tokenUsage
      responseTime.value = result.responseTime

      // 解析JSON
      const jsonMatch = result.content.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const reviewResult = JSON.parse(jsonMatch[0])
        return reviewResult
      }

      throw new Error('无法解析审核结果')
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    review,
    loading,
    response,
    error,
    tokenUsage,
    responseTime
  }
}

/**
 * AI翻译Hook
 */
export function useAiTranslate(options: UseAiChatOptions = {}) {
  const { loading, response, error, tokenUsage, responseTime } = useAiChat(options)

  const translate = async (text: string, targetLanguage: string = '英文', translateOptions?: AiTranslateOptions): Promise<string> => {
    loading.value = true
    response.value = ''
    error.value = null

    let prompt = `请将以下内容翻译成${targetLanguage}：\n\n${text}\n\n`

    if (translateOptions?.keepFormat) {
      prompt += '要求：保持原文的格式和风格。\n'
    }

    prompt += '请直接输出翻译结果，不要添加解释。'

    try {
      const aiChatBo: AiChatBo = {
        message: prompt,
        aiFeature: 'translate',
        provider: options.provider,
        modelName: options.modelName,
        temperature: options.temperature || 0.3,
        systemPrompt: options.systemPrompt
      }

      const [err, result] = await aiTranslate(aiChatBo)

      if (err) {
        error.value = err
        throw err
      }

      if (!result) {
        const noDataError = new Error('响应数据为空')
        error.value = noDataError
        throw noDataError
      }

      response.value = result.content
      tokenUsage.value = result.tokenUsage
      responseTime.value = result.responseTime

      return response.value
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    translate,
    loading,
    response,
    error,
    tokenUsage,
    responseTime
  }
}
