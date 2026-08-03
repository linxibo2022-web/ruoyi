/**
 * AI助手类型定义
 * @author 抓蛙师
 * @date 2025-01-26
 */

/** AI功能类型 */
export type AiFeatureType = 'optimize' | 'generate' | 'review' | 'translate'

/** Token使用情况视图对象 (对应后端 AiChatVo.TokenUsageVo) */
export interface TokenUsageVo {
  /** 输入Token数 */
  promptTokens: number
  /** 输出Token数 */
  completionTokens: number
  /** 总Token数 */
  totalTokens: number
}

/** AI对话业务对象 (对应后端 AiChatBo) */
export interface AiChatBo {
  /** 用户消息 */
  message: string
  /** AI功能类型 */
  aiFeature?: AiFeatureType
  /** 模型提供商 (deepseek, qianwen, openai, claude等) */
  provider?: string
  /** 模型名称 */
  modelName?: string
  /** 系统提示词 */
  systemPrompt?: string
  /** 温度参数 (0-2, 默认0.7) */
  temperature?: number
  /** 最大Token数 */
  maxTokens?: number
  /** 上下文信息 */
  context?: Record<string, any>
}

/** AI对话视图对象 (对应后端 AiChatVo) */
export interface AiChatVo {
  /** AI回复内容 */
  content: string
  /** Token使用情况 */
  tokenUsage?: TokenUsageVo
  /** 响应时间(毫秒) */
  responseTime?: number
}

/** AI文本优化选项 */
export interface AiOptimizeOptions {
  /** 优化类型 */
  type?: 'polish' | 'expand' | 'shorten' | 'formal' | 'casual' | 'marketing'
  /** 最大长度 */
  maxLength?: number
  /** 最小长度 */
  minLength?: number
  /** 关键词 */
  keywords?: string[]
  /** 风格 */
  style?: string
}

/** AI数据生成选项 */
export interface AiGenerateOptions {
  /** 字段定义 */
  schema?: FieldSchema[]
  /** 生成数量 */
  count?: number
  /** 数据类型 */
  dataType?: 'test' | 'realistic' | 'demo'
  /** 地区 */
  locale?: string
  /** 是否关联 */
  related?: boolean
}

/** 字段结构定义 */
export interface FieldSchema {
  /** 字段名 */
  name: string
  /** 字段类型 */
  type: 'string' | 'number' | 'date' | 'image' | 'enum'
  /** 字段标签 */
  label: string
  /** 是否必填 */
  required?: boolean
  /** 枚举选项 */
  options?: any[]
  /** 关联字段 */
  relation?: string
  /** 格式 */
  format?: string
  /** 示例 */
  example?: string
}

/** AI内容审核选项 */
export interface AiReviewOptions {
  /** 审核级别 */
  level?: 'loose' | 'normal' | 'strict'
  /** 检查项 */
  checkItems?: string[]
  /** 是否自动修复 */
  autoFix?: boolean
}

/** AI审核结果 */
export interface AiReviewResult {
  /** 状态 */
  status: 'pass' | 'warning' | 'reject'
  /** 评分 */
  score: number
  /** 问题列表 */
  issues: Array<{
    field: string
    type: string
    message: string
    suggestion?: string
  }>
}

/** AI翻译选项 */
export interface AiTranslateOptions {
  /** 目标语言 */
  targetLanguage?: string
  /** 保持格式 */
  keepFormat?: boolean
}
