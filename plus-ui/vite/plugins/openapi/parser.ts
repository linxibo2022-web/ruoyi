/**
 * OpenAPI 解析器
 * 解析 OpenAPI 3.0 规范文档
 */

export interface OpenApiSchema {
  type?: string
  format?: string
  description?: string
  properties?: Record<string, OpenApiSchema>
  items?: OpenApiSchema
  required?: string[]
  allOf?: OpenApiSchema[]
  oneOf?: OpenApiSchema[]
  anyOf?: OpenApiSchema[]
  $ref?: string
  enum?: any[]
  example?: any
  default?: any
}

export interface OpenApiParameter {
  name: string
  in: 'query' | 'path' | 'header' | 'cookie'
  description?: string
  required?: boolean
  schema?: OpenApiSchema
}

export interface OpenApiRequestBody {
  description?: string
  required?: boolean
  content?: Record<
    string,
    {
      schema?: OpenApiSchema
    }
  >
}

export interface OpenApiResponse {
  description?: string
  content?: Record<
    string,
    {
      schema?: OpenApiSchema
    }
  >
}

export interface OpenApiOperation {
  tags?: string[]
  summary?: string
  description?: string
  operationId?: string
  parameters?: OpenApiParameter[]
  requestBody?: OpenApiRequestBody
  responses?: Record<string, OpenApiResponse>
}

export interface OpenApiPathItem {
  get?: OpenApiOperation
  post?: OpenApiOperation
  put?: OpenApiOperation
  delete?: OpenApiOperation
  patch?: OpenApiOperation
}

export interface OpenApiDocument {
  openapi: string
  info: {
    title: string
    version: string
    description?: string
  }
  servers?: Array<{
    url: string
    description?: string
  }>
  paths: Record<string, OpenApiPathItem>
  components?: {
    schemas?: Record<string, OpenApiSchema>
    parameters?: Record<string, OpenApiParameter>
    requestBodies?: Record<string, OpenApiRequestBody>
    responses?: Record<string, OpenApiResponse>
  }
}

/**
 * 解析 $ref 引用
 */
export function parseRef(ref: string): {
  type: 'schema' | 'parameter' | 'requestBody' | 'response'
  name: string
} {
  // 格式: #/components/schemas/AdVo
  const parts = ref.replace('#/components/', '').split('/')
  return {
    type: parts[0] as any,
    name: parts[1]
  }
}

/**
 * 解析引用的 schema
 */
export function resolveRef(doc: OpenApiDocument, ref: string): OpenApiSchema | undefined {
  const { type, name } = parseRef(ref)

  if (type === 'schema' && doc.components?.schemas) {
    return doc.components.schemas[name]
  }

  return undefined
}

/**
 * 获取 schema 的类型名称
 */
export function getSchemaTypeName(schema: OpenApiSchema, doc: OpenApiDocument): string {
  if (schema.$ref) {
    const { name } = parseRef(schema.$ref)
    return name
  }

  return 'unknown'
}

/**
 * 判断是否是泛型类型（如 R«AdVo»）
 */
export function isGenericType(typeName: string): boolean {
  return typeName.includes('«') || typeName.includes('<')
}

/**
 * 解析泛型类型（如 "R«AdVo»" -> { wrapper: "R", inner: "AdVo" }）
 */
export function parseGenericType(typeName: string): { wrapper: string; inner: string } | null {
  // 支持两种格式: R«AdVo» 和 R<AdVo>
  const match = typeName.match(/^([^«<]+)[«<]([^»>]+)[»>]$/)
  if (match) {
    return {
      wrapper: match[1],
      inner: match[2]
    }
  }
  return null
}

/**
 * 从路径中提取模块名称
 * 例如: /base/ad/pageAds -> base/ad
 */
export function extractModuleFromPath(path: string): string {
  const parts = path.split('/').filter((p) => p && !p.startsWith('{'))

  // 去掉第一个部分（通常是 api 或空）
  if (parts[0] === 'api') {
    parts.shift()
  }

  // 取前两部分作为模块路径
  return parts.slice(0, 2).join('/')
}

/**
 * 从 operationId 中提取函数名
 * 例如: pageAdsUsingGET -> pageAds
 *       pageGoods_1UsingGET -> pageGoods
 */
export function extractFunctionName(operationId?: string, path?: string, method?: string): string {
  if (operationId) {
    // 移除 UsingGET/UsingPOST 等后缀
    let functionName = operationId.replace(/Using(GET|POST|PUT|DELETE|PATCH)$/i, '')
    // 移除 Swagger 生成的重复后缀 _数字（因为前端已按路径分组，无需全局去重）
    functionName = functionName.replace(/_\d+$/, '')
    return functionName
  }

  // 如果没有 operationId，从路径生成
  if (path) {
    const parts = path.split('/').filter((p) => p && !p.startsWith('{'))
    return parts[parts.length - 1] || 'unknown'
  }

  return 'unknown'
}

/**
 * 提取响应的数据类型
 */
export function extractResponseType(operation: OpenApiOperation, doc: OpenApiDocument): string {
  const response200 = operation.responses?.['200']
  if (!response200?.content) {
    return 'void'
  }

  const jsonContent = response200.content['application/json'] || response200.content['*/*']
  if (!jsonContent?.schema) {
    return 'void'
  }

  const schema = jsonContent.schema

  // 处理 $ref
  if (schema.$ref) {
    let typeName = getSchemaTypeName(schema, doc)

    // 如果是泛型类型 R«AdVo» 或 R<AdVo>
    if (isGenericType(typeName)) {
      const parsed = parseGenericType(typeName)
      if (parsed) {
        // R«AdVo» -> AdVo（我们只需要内部类型，因为 Result<T> 已经是项目的标准返回）
        typeName = parsed.inner

        // 如果内部类型是 PageResult«AdVo»，继续解析
        if (isGenericType(typeName)) {
          const innerParsed = parseGenericType(typeName)
          if (innerParsed) {
            // PageResult«AdVo» -> PageResult<AdVo>
            return `${innerParsed.wrapper}<${innerParsed.inner}>`
          }
        }
      }
    }

    // 如果是 RVoid、RLong、RPageResultXxx、RListXxx 等，去掉 R 前缀
    if (typeName.startsWith('R') && typeName.length > 1 && /[A-Z]/.test(typeName[1])) {
      const innerType = typeName.substring(1)
      // RVoid -> void, RLong -> number
      if (innerType === 'Void') return 'void'
      if (innerType === 'Long' || innerType === 'Integer') return 'number'
      if (innerType === 'String') return 'string'
      if (innerType === 'Boolean') return 'boolean'

      // RPageResultGoodsVo -> PageResult<GoodsVo>
      if (innerType.startsWith('PageResult')) {
        const voName = innerType.replace('PageResult', '')
        return `PageResult<${voName}>`
      }

      // RListAdVo -> AdVo[]
      if (innerType.startsWith('List')) {
        const voName = innerType.replace('List', '')
        return `${voName}[]`
      }

      return innerType
    }

    return typeName
  }

  // 处理数组
  if (schema.type === 'array' && schema.items?.$ref) {
    const itemType = getSchemaTypeName(schema.items, doc)
    return `${itemType}[]`
  }

  return 'any'
}

/**
 * 提取请求体类型
 */
export function extractRequestBodyType(operation: OpenApiOperation, doc: OpenApiDocument): string | null {
  const requestBody = operation.requestBody
  if (!requestBody?.content) {
    return null
  }

  const jsonContent = requestBody.content['application/json'] || requestBody.content['*/*']
  if (!jsonContent?.schema) {
    return null
  }

  const schema = jsonContent.schema
  if (schema.$ref) {
    return getSchemaTypeName(schema, doc)
  }

  return null
}

/**
 * 基本类型查询参数定义
 */
export interface BasicQueryParam {
  name: string
  type: string
  required: boolean
  description?: string
}

/**
 * 查询参数提取结果
 */
export interface QueryParamsResult {
  /** 对象类型查询参数（如 AdQuery） */
  queryType?: string
  /** 基本类型查询参数列表 */
  basicParams?: BasicQueryParam[]
}

/**
 * 将 OpenAPI 类型映射为 TypeScript 类型
 */
export function mapOpenApiTypeToTS(schema?: OpenApiSchema): string {
  if (!schema) return 'any'

  const type = schema.type
  const format = schema.format

  switch (type) {
    case 'integer':
      return 'number'
    case 'number':
      return 'number'
    case 'boolean':
      return 'boolean'
    case 'array':
      if (schema.items) {
        const itemType = mapOpenApiTypeToTS(schema.items)
        return `${itemType}[]`
      }
      return 'any[]'
    case 'object':
      return 'Record<string, any>'
    case 'string':
      // 特殊格式处理
      if (format === 'date' || format === 'date-time') {
        return 'string'  // 可以改为 Date，根据项目约定
      }
      return 'string'
    default:
      return 'any'
  }
}

/**
 * 提取查询参数（支持对象类型和基本类型）
 */
export function extractQueryParams(operation: OpenApiOperation, doc: OpenApiDocument): QueryParamsResult {
  const queryParams = operation.parameters?.filter((p) => p.in === 'query')
  if (!queryParams || queryParams.length === 0) {
    return {}
  }

  // 检查第一个参数是否是对象类型（有 $ref）
  const firstParam = queryParams[0]
  if (firstParam.schema?.$ref) {
    let typeName = getSchemaTypeName(firstParam.schema, doc)

    // 将 Bo 结尾的类型转换为 Query（GoodsBo -> GoodsQuery）
    if (typeName.endsWith('Bo')) {
      typeName = typeName.replace(/Bo$/, 'Query')
    }

    return { queryType: typeName }
  }

  // 提取基本类型参数列表
  const basicParams: BasicQueryParam[] = queryParams.map((p) => ({
    name: p.name,
    type: mapOpenApiTypeToTS(p.schema),
    required: p.required || false,
    description: p.description
  }))

  return { basicParams }
}

/**
 * 提取查询参数类型（兼容旧版本 API）
 * @deprecated 请使用 extractQueryParams 代替
 */
export function extractQueryParamType(operation: OpenApiOperation, doc: OpenApiDocument): string | null {
  const result = extractQueryParams(operation, doc)
  return result.queryType || null
}
