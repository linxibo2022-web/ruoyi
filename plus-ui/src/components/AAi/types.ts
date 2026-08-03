/** AI功能类型 */
export type AiFeature = 'optimize' | 'generate' | 'review' | 'translate' | 'suggest'

/** AI助手Props */
export interface AAiAssistantProps {
  /** 双向绑定的值 */
  modelValue?: string | any

  /** 工作模式 */
  mode?: AiFeature

  /** 字段名称 */
  field?: string

  /** 字段类型 */
  fieldType?: 'title' | 'description' | 'remark' | 'custom'

  /** 上下文信息 */
  context?: Record<string, any>

  /** 模型提供商 */
  provider?: string

  /** 模型名称 */
  modelName?: string

  /** 创造性参数 */
  temperature?: number

  /** AI角色 */
  role?: string

  /** 系统提示词 */
  systemPrompt?: string

  /** 功能配置 */
  features?: {
    optimize?: boolean
    generate?: boolean
    review?: boolean
    translate?: boolean
  }

  /** 触发方式 */
  trigger?: 'button' | 'icon' | 'auto'

  /** 展示位置 */
  position?: 'inline' | 'popup' | 'sidebar'

  /** 尺寸 */
  size?: ElSize

  /** 会话ID */
  sessionId?: string

  /** 最大历史记录 */
  maxHistory?: number

  /** 是否显示历史 */
  showHistory?: boolean

  /** 是否自动保存 */
  autoSave?: boolean

  /** 数据生成相关 */
  schema?: FieldSchema[]
  generateCount?: number

  /** 内容审核相关 */
  content?: Record<string, any>
  reviewRules?: ReviewRule[]

  /** 翻译相关 */
  targetLanguage?: string
}

/** 字段结构定义 */
export interface FieldSchema {
  name: string
  type: 'string' | 'number' | 'date' | 'image' | 'enum'
  label: string
  required?: boolean
  options?: any[]
  relation?: string
  format?: string
  example?: string
}

/** 审核规则 */
export interface ReviewRule {
  field: string
  type: 'required' | 'format' | 'length' | 'keywords'
  message: string
  fix?: (value: any) => any
}

/** 历史记录项 */
export interface HistoryItem {
  input: string | any
  output: any
  feature: AiFeature
  timestamp: string
}

/** AI设置 */
export interface AiSettings {
  provider: string
  modelName?: string
  temperature: number
  role?: string
}

/** 优化选项 */
export interface OptimizeOptions {
  type: 'polish' | 'expand' | 'shorten' | 'formal' | 'casual' | 'marketing'
  maxLength?: number
  minLength?: number
  keywords?: string[]
  style?: string
}

/** AI响应结果 */
export interface AiResult {
  content: string
  tokenUsage?: {
    promptTokens: number
    completionTokens: number
    totalTokens: number
  }
  model?: string
  finishReason?: string
}
