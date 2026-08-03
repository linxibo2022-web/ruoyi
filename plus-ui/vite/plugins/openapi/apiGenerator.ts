/**
 * API 函数生成器
 * 生成 *Api.ts 文件
 */

import type { OpenApiDocument, OpenApiOperation, BasicQueryParam } from './parser'
import { extractResponseType, extractRequestBodyType, extractQueryParams, extractFunctionName, parseGenericType } from './parser'
import { getHttpMethod, isPageApi, formatPath, extractPathParams, cleanDescription, generateImports } from './utils'

/**
 * API 函数排序优先级
 * 按照常见 CRUD 操作顺序排列
 */
const API_SORT_ORDER = {
  // 1. 分页查询
  page: 10,
  list: 11,
  query: 12,

  // 2. 单个查询/详情
  get: 20,
  detail: 21,
  info: 22,

  // 3. 新增
  add: 30,
  create: 31,
  insert: 32,
  save: 33,

  // 4. 修改
  update: 40,
  edit: 41,
  modify: 42,

  // 5. 删除
  delete: 50,
  remove: 51,

  // 6. 批量操作
  batch: 60,
  import: 61,
  export: 62,

  // 7. 其他操作
  default: 100
}

/**
 * 获取 API 函数的排序权重
 * @param functionName 函数名
 * @returns 排序权重(数字越小越靠前)
 */
function getApiSortWeight(functionName: string): number {
  const lowerName = functionName.toLowerCase()

  // 检查函数名是否以某个关键词开头
  for (const [keyword, weight] of Object.entries(API_SORT_ORDER)) {
    if (lowerName.startsWith(keyword)) {
      return weight
    }
  }

  return API_SORT_ORDER.default
}

/**
 * 对 API 函数列表进行排序
 * @param apis API 函数列表
 * @returns 排序后的 API 函数列表
 */
export function sortApis(apis: ApiFunction[]): ApiFunction[] {
  return [...apis].sort((a, b) => {
    const weightA = getApiSortWeight(a.name)
    const weightB = getApiSortWeight(b.name)

    // 按权重排序
    if (weightA !== weightB) {
      return weightA - weightB
    }

    // 权重相同时按函数名字母顺序
    return a.name.localeCompare(b.name)
  })
}

/** API 函数定义 */
export interface ApiFunction {
  /** 函数名 */
  name: string
  /** HTTP 方法（GET/POST/PUT/DELETE） */
  method: string
  /** API 路径 */
  path: string
  /** API 摘要 */
  summary?: string
  /** API 描述 */
  description?: string
  /** 查询参数类型（对象类型，如 AdQuery） */
  queryType?: string
  /** 基本类型查询参数列表 */
  basicQueryParams?: BasicQueryParam[]
  /** 请求体类型 */
  bodyType?: string
  /** 响应类型 */
  responseType: string
  /** 路径参数列表 */
  pathParams: string[]
}

/**
 * 解析单个 API 操作为函数定义
 * @param path API 路径
 * @param method HTTP 方法
 * @param operation OpenAPI 操作对象
 * @param doc OpenAPI 文档对象
 * @returns API 函数定义
 */
export function parseOperation(path: string, method: string, operation: OpenApiOperation, doc: OpenApiDocument): ApiFunction {
  const functionName = extractFunctionName(operation.operationId, path, method)
  const pathParams = extractPathParams(path)

  // 提取响应类型（已在 parser.ts 中处理好了）
  const responseType = extractResponseType(operation, doc)

  // 提取查询参数（支持对象类型和基本类型）
  const queryParams = extractQueryParams(operation, doc)

  return {
    name: functionName,
    method: getHttpMethod(method),
    path,
    summary: cleanDescription(operation.summary),
    description: cleanDescription(operation.description),
    queryType: queryParams.queryType,
    basicQueryParams: queryParams.basicParams,
    bodyType: extractRequestBodyType(operation, doc) || undefined,
    responseType,
    pathParams
  }
}

/**
 * 从summary中提取业务名称
 * @param summary OpenAPI 的 summary，如 "获取广告配置详细信息", "查询用户列表"
 * @param functionName 函数名，如 "getAd", "pageAds"
 * @returns 业务名称，如 "广告配置", "用户"
 */
function extractBusinessName(summary: string | undefined, functionName: string): string {
  // 优先从中文 summary 中提取业务名称
  if (summary) {
    // 移除操作动词和修饰词，提取业务主体
    // "获取广告配置详细信息" -> "广告配置"
    // "查询广告配置详细" -> "广告配置"
    // "查询用户列表" -> "用户"
    // "新增订单" -> "订单"
    let cleaned = summary
      .replace(/^(查询|获取|新增|添加|修改|更新|删除|移除|导入|导出|批量|保存)/, '') // 移除开头的操作动词
      .trim()

    // 循环移除末尾的修饰词，直到没有为止
    let prevCleaned = ''
    while (prevCleaned !== cleaned) {
      prevCleaned = cleaned
      cleaned = cleaned.replace(/(列表|详细|详情|信息)$/, '').trim()
    }

    if (cleaned) return cleaned
  }

  // 如果 summary 为空或提取失败，从函数名推断
  // getAd -> Ad, pageAds -> Ad, getUserInfo -> UserInfo
  const entityName = functionName
    .replace(/^(page|list|query|get|detail|info|add|create|insert|save|update|edit|modify|delete|remove|batch|import|export)/i, '')
    .replace(/s$/, '') // 移除复数形式

  return entityName || '数据'
}

/**
 * 生成操作描述
 * @param functionName 函数名
 * @param summary OpenAPI 的 summary
 * @param businessName 业务名称
 * @returns 操作描述
 */
function generateOperationDescription(functionName: string, summary: string | undefined, businessName: string): string {
  const lowerName = functionName.toLowerCase()

  // 如果 summary 已经是中文且格式正确，优先使用
  if (summary && /^(查询|获取|新增|添加|修改|更新|删除|移除|导入|导出|批量)/.test(summary)) {
    // 修正描述: "获取XXX信息" -> "查询XXX详细"
    if (summary.includes('详细信息') || summary.includes('详情')) {
      return summary.replace(/获取/, '查询').replace(/信息$/, '').replace(/详情/, '详细')
    }
    return summary
  }

  // 根据函数名前缀生成标准描述
  if (lowerName.startsWith('page') || lowerName.startsWith('list')) {
    return `查询${businessName}列表`
  }
  if (lowerName.startsWith('get') || lowerName.startsWith('detail') || lowerName.startsWith('info')) {
    return `查询${businessName}详细`
  }
  if (lowerName.startsWith('add') || lowerName.startsWith('create') || lowerName.startsWith('insert')) {
    return `新增${businessName}`
  }
  if (lowerName.startsWith('save')) {
    return `保存${businessName}`
  }
  if (lowerName.startsWith('update') || lowerName.startsWith('edit') || lowerName.startsWith('modify')) {
    return `修改${businessName}`
  }
  if (lowerName.startsWith('delete') || lowerName.startsWith('remove')) {
    return `删除${businessName}`
  }
  if (lowerName.startsWith('batch')) {
    return `批量操作${businessName}`
  }
  if (lowerName.startsWith('import')) {
    return `导入${businessName}`
  }
  if (lowerName.startsWith('export')) {
    return `导出${businessName}`
  }

  // 默认使用 summary 或函数名
  return summary || functionName
}

/**
 * 生成参数描述
 * @param paramName 参数名
 * @param businessName 业务名称
 * @returns 参数描述
 */
function generateParamDescription(paramName: string, businessName: string): string {
  if (paramName === 'query') return '查询参数'

  // 对于 id/ids/data 参数，需要清理业务名称中的修饰词
  // "广告配置详细" -> "广告配置"
  const cleanedBusinessName = businessName.replace(/(列表|详细|详情|信息)$/g, '').trim() || businessName

  if (paramName === 'data') return `${cleanedBusinessName}数据`
  if (paramName === 'id') return `${cleanedBusinessName}ID`
  if (paramName === 'ids') return `${cleanedBusinessName}ID`

  // 默认使用参数名
  return paramName
}

/**
 * 生成单个 API 函数代码
 * @param api API 函数定义
 * @returns 生成的函数代码字符串
 */
export function generateApiFunction(api: ApiFunction): string {
  const lines: string[] = []

  // 提取业务名称
  const businessName = extractBusinessName(api.summary, api.name)

  // 生成操作描述
  const operationDesc = generateOperationDescription(api.name, api.summary, businessName)

  // 生成注释
  lines.push('/**')
  lines.push(` * ${operationDesc}`)

  // 参数注释
  if (api.pathParams.length > 0) {
    api.pathParams.forEach((param) => {
      const paramDesc = generateParamDescription(param, businessName)
      lines.push(` * @param ${param} ${paramDesc}`)
    })
  }

  // 基本类型查询参数注释
  if (api.basicQueryParams && api.basicQueryParams.length > 0) {
    api.basicQueryParams.forEach((param) => {
      const desc = param.description || param.name
      lines.push(` * @param ${param.name} ${desc}`)
    })
  }

  // 对象查询参数注释
  if (api.queryType) {
    lines.push(` * @param query 查询参数`)
  }

  // 请求体参数注释
  if (api.bodyType) {
    const paramDesc = generateParamDescription('data', businessName)
    lines.push(` * @param data ${paramDesc}`)
  }

  // 处理返回类型：如果是新增接口且返回 number，改为 string | number
  let returnType = api.responseType
  if (api.name.toLowerCase().startsWith('add') && returnType === 'number') {
    returnType = 'string | number'
  }

  lines.push(` * @returns {Result<${returnType}>} 结果`)
  lines.push(' */')

  // 生成函数签名
  const params: string[] = []

  // 路径参数
  if (api.pathParams.length > 0) {
    api.pathParams.forEach((param) => {
      // 如果是删除接口且参数名是 ids（复数），支持数组类型（批量删除）
      if (api.name.toLowerCase().startsWith('delete') && param === 'ids') {
        params.push(`${param}: string | number | Array<string | number>`)
      } else {
        params.push(`${param}: string | number`)
      }
    })
  }

  // 基本类型查询参数
  if (api.basicQueryParams && api.basicQueryParams.length > 0) {
    api.basicQueryParams.forEach((param) => {
      const optional = param.required ? '' : '?'
      params.push(`${param.name}${optional}: ${param.type}`)
    })
  }

  // 对象查询参数
  if (api.queryType) {
    params.push(`query?: ${api.queryType}`)
  }

  // 请求体参数
  if (api.bodyType) {
    params.push(`data: ${api.bodyType}`)
  }

  const paramStr = params.length > 0 ? params.join(', ') : ''
  lines.push(`export const ${api.name} = (${paramStr}): Result<${returnType}> => {`)

  // 生成函数体
  const formattedPath = formatPath(api.path)

  // 如果有路径参数，使用模板字符串
  const pathExpression = api.pathParams.length > 0 ? `\`${formattedPath}\`` : `'${api.path}'`

  // 判断是否有基本类型查询参数
  const hasBasicParams = api.basicQueryParams && api.basicQueryParams.length > 0

  if (api.method === 'get' || api.method === 'del') {
    // GET/DELETE 请求
    if (api.queryType) {
      // 对象查询参数
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression}, query)`)
    } else if (hasBasicParams) {
      // 基本类型查询参数 - 构建参数对象
      const paramNames = api.basicQueryParams!.map((p) => p.name).join(', ')
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression}, { ${paramNames} })`)
    } else {
      // 无查询参数
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression})`)
    }
  } else {
    // POST/PUT/PATCH 请求
    if (api.bodyType) {
      // 请求体参数
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression}, data)`)
    } else if (api.queryType) {
      // 对象查询参数
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression}, query)`)
    } else if (hasBasicParams) {
      // 基本类型查询参数 - 构建参数对象
      const paramNames = api.basicQueryParams!.map((p) => p.name).join(', ')
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression}, { ${paramNames} })`)
    } else {
      // 无参数
      lines.push(`  return http.${api.method}<${returnType}>(${pathExpression})`)
    }
  }

  lines.push('}')

  return lines.join('\n')
}

/**
 * 生成完整的 API 文件内容
 * @param apis API 函数列表
 * @param types 使用的类型集合
 * @param moduleName 模块名
 * @returns 完整的 API 文件代码
 */
export function generateApiFile(apis: ApiFunction[], types: Set<string>, moduleName: string): string {
  const lines: string[] = []

  // 生成导入语句
  if (types.size > 0) {
    lines.push(generateImports(Array.from(types), `./${moduleName}Types`))
    lines.push('') // 导入后加一个空行
  }

  // 对 API 函数进行排序
  const sortedApis = sortApis(apis)

  // 生成每个 API 函数
  sortedApis.forEach((api, index) => {
    lines.push(generateApiFunction(api))
    if (index < sortedApis.length - 1) {
      lines.push('') // 函数之间加空行
    }
  })

  return lines.join('\n') + '\n' // 文件末尾加一个换行符
}

/**
 * 从 API 路径中提取模块标识
 * @param path API 路径
 * @returns 模块标识（如：app/home, common/mall/order, base/ad）
 * @example
 * /app/home/xxx -> app/home
 * /common/mall/order/xxx -> common/mall/order
 * /common/system/xxx -> common/system
 * /base/ad/xxx -> base/ad
 */
function extractModuleKey(path: string): string {
  const parts = path.split('/').filter((p) => p && !p.startsWith('{'))

  if (parts.length === 0) return 'default'

  const level1 = parts[0]

  // /app/xxx 或 /common/xxx (2层) -> 取前2层
  // /app/home/getTenantIdByAppid (3层+) -> 取前2层（app/home）
  // /common/mall/order/createOrder (4层) -> 需要判断是否是 mall/order 这种双层
  if (level1 === 'app' || level1 === 'common') {
    // 对于 common，需要判断第二层是否是 mall 等双层结构
    if (level1 === 'common' && parts.length >= 3) {
      const level2 = parts[1]
      // common/mall/order/xxx -> common/mall/order
      // common/system/xxx -> common/system
      // common/base/xxx -> common/base
      if (level2 === 'mall' || level2 === 'base') {
        return parts.slice(0, 3).join('/')
      }
    }
    // app/home/xxx -> app/home
    // common/system/xxx -> common/system
    // common/ai/chat/xxx -> common/ai/chat
    if (parts.length >= 3 && (parts[1] === 'ai' || parts[1] === 'base')) {
      return parts.slice(0, 3).join('/')
    }

    return parts.slice(0, 2).join('/')
  }

  // 其他情况(/base/ad/xxx) -> 取前2层
  if (parts.length >= 2) {
    return parts.slice(0, 2).join('/')
  }

  return parts[0]
}

/**
 * 从 OpenAPI 文档中提取所有 API 函数
 * @param doc OpenAPI 文档对象
 * @returns 按模块分组的 API 函数集合（模块标识 -> API 函数列表）
 */
export function extractApis(doc: OpenApiDocument): Map<string, ApiFunction[]> {
  const apisByModule = new Map<string, ApiFunction[]>()

  Object.entries(doc.paths).forEach(([path, pathItem]) => {
    // 排除移动端接口（/app 开头的路径）
    if (path.startsWith('/app')) {
      return
    }

    ;['get', 'post', 'put', 'delete', 'patch'].forEach((method) => {
      const operation = pathItem[method as keyof typeof pathItem] as OpenApiOperation | undefined

      if (operation) {
        const api = parseOperation(path, method, operation, doc)

        // 根据路径分组（而不是 tags）
        const moduleKey = extractModuleKey(path)
        if (!apisByModule.has(moduleKey)) {
          apisByModule.set(moduleKey, [])
        }
        apisByModule.get(moduleKey)!.push(api)
      }
    })
  })

  return apisByModule
}

/**
 * 收集 API 函数中使用的所有类型
 * @param apis API 函数列表
 * @returns 使用的类型名称集合
 */
export function collectUsedTypes(apis: ApiFunction[]): Set<string> {
  const types = new Set<string>()

  apis.forEach((api) => {
    if (api.queryType) {
      types.add(api.queryType)
    }
    if (api.bodyType) {
      types.add(api.bodyType)
    }

    // 从响应类型中提取
    let responseType = api.responseType

    // PageResult<AdVo> -> AdVo
    const pageMatch = responseType.match(/^PageResult<(.+)>$/)
    if (pageMatch) {
      responseType = pageMatch[1]
    }

    // AdVo[] -> AdVo
    const arrayMatch = responseType.match(/^(.+)\[\]$/)
    if (arrayMatch) {
      responseType = arrayMatch[1]
    }

    // 只添加非基础类型
    if (responseType !== 'void' && responseType !== 'any' && responseType !== 'string' && responseType !== 'number' && responseType !== 'boolean') {
      types.add(responseType)
    }
  })

  return types
}
