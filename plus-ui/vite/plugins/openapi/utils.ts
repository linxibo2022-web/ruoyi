/**
 * OpenAPI 代码生成工具函数
 */

/**
 * 将 schema 类型转换为 TypeScript 类型
 */
export function schemaToTsType(type?: string, format?: string, items?: any): string {
  if (!type) return 'any'

  switch (type) {
    case 'string':
      return format === 'date-time' ? 'string' : 'string'
    case 'integer':
    case 'number':
      return 'number'
    case 'boolean':
      return 'boolean'
    case 'array':
      if (items?.$ref) {
        const refName = items.$ref.split('/').pop()
        return `${refName}[]`
      }
      return items ? `${schemaToTsType(items.type, items.format)}[]` : 'any[]'
    case 'object':
      return 'Record<string, any>'
    default:
      return 'any'
  }
}

/**
 * 转换为驼峰命名
 */
export function toCamelCase(str: string): string {
  return str.replace(/[-_](.)/g, (_, char) => char.toUpperCase())
}

/**
 * 转换为帕斯卡命名
 */
export function toPascalCase(str: string): string {
  const camel = toCamelCase(str)
  return camel.charAt(0).toUpperCase() + camel.slice(1)
}

/**
 * 清理描述文本（移除 HTML 标签）
 */
export function cleanDescription(desc?: string): string {
  if (!desc) return ''
  return desc
    .replace(/<[^>]*>/g, '') // 移除 HTML 标签
    .replace(/\s+/g, ' ') // 合并空白
    .trim()
}

/**
 * 获取 HTTP 方法对应的 http 工具方法名
 */
export function getHttpMethod(method: string): string {
  const map: Record<string, string> = {
    get: 'get',
    post: 'post',
    put: 'put',
    delete: 'del',
    patch: 'patch'
  }
  return map[method.toLowerCase()] || 'get'
}

/**
 * 检查是否是分页接口（根据函数名判断）
 */
export function isPageApi(functionName: string): boolean {
  return functionName.startsWith('page') || functionName.includes('Page')
}

/**
 * 格式化路径参数
 * /base/ad/{id} -> /base/ad/${id}
 */
export function formatPath(path: string): string {
  // eslint-disable-next-line no-template-curly-in-string
  return path.replace(/\{([^}]+)\}/g, '${$1}')
}

/**
 * 提取路径参数名称
 * /base/ad/{id} -> ['id']
 */
export function extractPathParams(path: string): string[] {
  const matches = path.match(/\{([^}]+)\}/g)
  if (!matches) return []
  return matches.map((m) => m.slice(1, -1))
}

/**
 * 类型排序权重(用于 import 语句排序)
 * Query > Bo > Vo > 其他
 */
const TYPE_SORT_WEIGHT: Record<string, number> = {
  Query: 1,
  Bo: 2,
  Vo: 3
}

/**
 * 获取类型的排序权重
 */
function getTypeSortWeight(typeName: string): number {
  if (typeName.endsWith('Query')) return TYPE_SORT_WEIGHT.Query
  if (typeName.endsWith('Bo')) return TYPE_SORT_WEIGHT.Bo
  if (typeName.endsWith('Vo')) return TYPE_SORT_WEIGHT.Vo
  return 100 // 其他类型
}

/**
 * 生成导入语句
 * 按照 Query > Bo > Vo > 其他 的顺序排序
 */
export function generateImports(types: string[], from: string): string {
  if (types.length === 0) return ''

  // 对类型进行排序
  const sortedTypes = [...types].sort((a, b) => {
    const weightA = getTypeSortWeight(a)
    const weightB = getTypeSortWeight(b)

    if (weightA !== weightB) {
      return weightA - weightB
    }

    // 权重相同时按字母顺序
    return a.localeCompare(b)
  })

  return `import type { ${sortedTypes.join(', ')} } from '${from}'`
}

/**
 * 生成 JSDoc 注释
 */
export function generateJsDoc(summary?: string, description?: string, params?: Array<{ name: string; desc: string; type: string }>): string {
  const lines: string[] = ['/**']

  if (summary) {
    lines.push(` * ${cleanDescription(summary)}`)
  }

  if (description && description !== summary) {
    lines.push(` * ${cleanDescription(description)}`)
  }

  if (params && params.length > 0) {
    params.forEach((p) => {
      lines.push(` * @param ${p.name} ${p.desc}`)
    })
  }

  lines.push(` * @returns {Result<${params?.[0]?.type || 'void'}>} 结果`)
  lines.push(' */')

  return lines.join('\n')
}

/**
 * 缩进文本
 */
export function indent(text: string, spaces: number = 2): string {
  const prefix = ' '.repeat(spaces)
  return text
    .split('\n')
    .map((line) => prefix + line)
    .join('\n')
}

/**
 * 首字母小写
 */
export function lowerFirst(str: string): string {
  return str.charAt(0).toLowerCase() + str.slice(1)
}

/**
 * 首字母大写
 */
export function upperFirst(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * 安全的属性名（如果包含特殊字符则用引号包裹）
 */
export function safePropName(name: string): string {
  if (/^[a-z_$][\w$]*$/i.test(name)) {
    return name
  }
  return `'${name}'`
}
