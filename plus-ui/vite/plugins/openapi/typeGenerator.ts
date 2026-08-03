/**
 * TypeScript 类型生成器
 * 生成 *Types.ts 文件
 */

import type { OpenApiDocument, OpenApiSchema } from './parser'
import { resolveRef } from './parser'
import { schemaToTsType, safePropName, cleanDescription } from './utils'

/** 类型定义 */
export interface TypeDefinition {
  /** 类型名称 */
  name: string
  /** 属性列表 */
  properties: Array<{
    /** 属性名 */
    name: string
    /** 属性类型 */
    type: string
    /** 是否必需 */
    required: boolean
    /** 属性描述 */
    description?: string
  }>
  /** 类型描述 */
  description?: string
  /** 继承的类型 */
  extends?: string[]
}

/**
 * 将 OpenAPI Schema 转换为 TypeScript 类型字符串
 * @param schema OpenAPI Schema 对象
 * @param doc OpenAPI 文档对象
 * @param requiredProps 必需属性列表
 * @returns TypeScript 类型字符串
 */
function generatePropertyType(schema: OpenApiSchema, doc: OpenApiDocument, requiredProps: string[] = []): string {
  // 处理 $ref
  if (schema.$ref) {
    const refName = schema.$ref.split('/').pop()
    return refName || 'any'
  }

  // 处理数组
  if (schema.type === 'array') {
    if (schema.items?.$ref) {
      const refName = schema.items.$ref.split('/').pop()
      return `${refName}[]`
    }
    if (schema.items) {
      const itemType = generatePropertyType(schema.items, doc, requiredProps)
      return `${itemType}[]`
    }
    return 'any[]'
  }

  // 处理对象
  if (schema.type === 'object' && schema.properties) {
    return 'Record<string, any>'
  }

  // 处理枚举
  if (schema.enum) {
    return schema.enum.map((v) => (typeof v === 'string' ? `'${v}'` : v)).join(' | ')
  }

  // 处理基本类型
  return schemaToTsType(schema.type, schema.format, schema.items)
}

/**
 * 将 OpenAPI Schema 解析为类型定义
 * @param name 类型名称
 * @param schema OpenAPI Schema 对象
 * @param doc OpenAPI 文档对象
 * @returns 类型定义对象，如果应跳过则返回 null
 */
export function parseSchemaToType(name: string, schema: OpenApiSchema, doc: OpenApiDocument): TypeDefinition | null {
  // 跳过泛型包装类型（如 R«AdVo»、RVoid、RLong等）
  if (name.includes('«') || name.includes('R<') || (name.startsWith('R') && name.length > 1 && /[A-Z]/.test(name[1]))) {
    return null
  }

  // 跳过 PageResult 包装类型（已经是全局类型）
  if (name.includes('PageResult')) {
    return null
  }

  const properties: TypeDefinition['properties'] = []
  const requiredProps = schema.required || []
  const extendsTypes: string[] = []

  // 处理 allOf（通常用于继承）
  if (schema.allOf) {
    for (const subSchema of schema.allOf) {
      if (subSchema.$ref) {
        const refName = subSchema.$ref.split('/').pop()
        if (refName) {
          extendsTypes.push(refName)
        }
      } else if (subSchema.properties) {
        // 合并属性
        Object.entries(subSchema.properties).forEach(([propName, propSchema]) => {
          properties.push({
            name: propName,
            type: generatePropertyType(propSchema, doc, requiredProps),
            required: requiredProps.includes(propName),
            description: cleanDescription(propSchema.description)
          })
        })
      }
    }
  }

  // 处理普通属性
  if (schema.properties) {
    Object.entries(schema.properties).forEach(([propName, propSchema]) => {
      properties.push({
        name: propName,
        type: generatePropertyType(propSchema, doc, requiredProps),
        required: requiredProps.includes(propName),
        description: cleanDescription(propSchema.description)
      })
    })
  }

  // 如果没有属性且没有继承，跳过
  if (properties.length === 0 && extendsTypes.length === 0) {
    return null
  }

  return {
    name,
    properties,
    description: cleanDescription(schema.description),
    extends: extendsTypes.length > 0 ? extendsTypes : undefined
  }
}

/**
 * 生成 TypeScript 接口定义代码
 * @param typeDef 类型定义对象
 * @returns TypeScript 接口代码字符串
 */
export function generateTypeCode(typeDef: TypeDefinition): string {
  const lines: string[] = []

  // 添加注释
  if (typeDef.description) {
    lines.push(`/** ${typeDef.description} */`)
  }

  // 生成接口声明
  let declaration = `export interface ${typeDef.name}`

  // 添加继承
  if (typeDef.extends && typeDef.extends.length > 0) {
    declaration += ` extends ${typeDef.extends.join(', ')}`
  }

  declaration += ' {'
  lines.push(declaration)

  // 生成属性
  typeDef.properties.forEach((prop, index) => {
    if (prop.description) {
      lines.push(`  /** ${prop.description} */`)
    }
    const optional = prop.required ? '' : '?'
    lines.push(`  ${safePropName(prop.name)}${optional}: ${prop.type}`)
    // 最后一个属性不加空行
    if (index < typeDef.properties.length - 1) {
      lines.push('')
    }
  })

  lines.push('}')

  return lines.join('\n')
}

/**
 * 根据 Bo 类型生成对应的 Query 类型
 * 自动过滤审计字段，并继承 PageQuery
 * @param boDef Bo 类型定义
 * @returns Query 类型定义
 */
function generateQueryTypeFromBo(boDef: TypeDefinition): TypeDefinition {
  const queryName = boDef.name.replace(/Bo$/, 'Query')

  return {
    name: queryName,
    properties: boDef.properties.filter((prop) => {
      // 过滤掉不需要的字段
      return !['createDept', 'createBy', 'createTime', 'updateBy', 'updateTime', 'remark'].includes(prop.name)
    }),
    description: boDef.description?.replace('业务对象', '查询类型'),
    extends: ['PageQuery']
  }
}

/**
 * 获取类型的排序权重
 * Query > Bo > Vo > 其他
 */
function getTypeSortWeight(typeName: string): number {
  if (typeName.endsWith('Query')) return 1
  if (typeName.endsWith('Bo')) return 2
  if (typeName.endsWith('Vo')) return 3
  return 100
}

/**
 * 对类型定义列表进行排序
 * @param types 类型定义列表
 * @returns 排序后的类型定义列表
 */
function sortTypes(types: TypeDefinition[]): TypeDefinition[] {
  return [...types].sort((a, b) => {
    const weightA = getTypeSortWeight(a.name)
    const weightB = getTypeSortWeight(b.name)

    // 按权重排序
    if (weightA !== weightB) {
      return weightA - weightB
    }

    // 权重相同时按字母顺序
    return a.name.localeCompare(b.name)
  })
}

/**
 * 生成完整的 Types 文件内容
 * @param types 类型定义列表
 * @param moduleName 模块名
 * @returns 完整的 Types 文件代码
 */
export function generateTypesFile(types: TypeDefinition[], moduleName: string): string {
  const lines: string[] = []

  // 先生成所有 Query 类型（从 Bo 自动生成）
  const allTypeDefs: TypeDefinition[] = []
  types.forEach((typeDef) => {
    if (typeDef.name.endsWith('Bo')) {
      // 先添加自动生成的 Query 类型
      const queryDef = generateQueryTypeFromBo(typeDef)
      allTypeDefs.push(queryDef)
    }
    // 然后添加原类型
    allTypeDefs.push(typeDef)
  })

  // 对所有类型进行排序：Query > Bo > Vo > 其他
  const sortedTypes = sortTypes(allTypeDefs)

  // 生成每个类型定义
  sortedTypes.forEach((typeDef) => {
    lines.push(generateTypeCode(typeDef))
    lines.push('')
  })

  // 移除最后一个空行
  if (lines.length > 0 && lines[lines.length - 1] === '') {
    lines.pop()
  }

  return lines.join('\n') + '\n' // 文件末尾加一个换行符
}

/**
 * 从 OpenAPI 文档中提取所有类型定义
 * @param doc OpenAPI 文档对象
 * @returns 类型名称到类型定义的映射
 */
export function extractTypes(doc: OpenApiDocument): Map<string, TypeDefinition> {
  const types = new Map<string, TypeDefinition>()

  if (!doc.components?.schemas) {
    return types
  }

  Object.entries(doc.components.schemas).forEach(([name, schema]) => {
    const typeDef = parseSchemaToType(name, schema, doc)
    if (typeDef) {
      types.set(name, typeDef)
    }
  })

  return types
}
