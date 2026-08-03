/**
 * OpenAPI 代码生成插件
 * 从 OpenAPI 文档自动生成符合项目风格的 TypeScript 类型和 API 函数
 */

import type { Plugin } from 'vite'
import { existsSync, mkdirSync, writeFileSync, readFileSync } from 'node:fs'
import { resolve, join, dirname } from 'node:path'
import { createHash } from 'node:crypto'
import https from 'node:https'
import http from 'node:http'

import type { OpenApiDocument } from './parser'
import { extractTypes, generateTypesFile } from './typeGenerator'
import { extractApis, generateApiFile, collectUsedTypes } from './apiGenerator'

export interface OpenApiPluginOptions {
  /** OpenAPI 文档 URL（如：http://127.0.0.1:5500/v3/api-docs/business） */
  input: string
  /** 输出目录，相对于项目根目录（如：src/api/generated） */
  output: string
  /** 生成模式：manual-手动触发，once-启动时生成一次 */
  mode?: 'manual' | 'once'
  /** 是否启用插件 */
  enabled?: boolean
  /** 生成前钩子函数 */
  beforeGenerate?: () => void | Promise<void>
  /** 生成后钩子函数 */
  afterGenerate?: () => void | Promise<void>
  /** 忽略配置 */
  ignore?: {
    /** 忽略的 API 路径（支持通配符）如: ['/base/ad/**', '/common/mall/**'] */
    paths?: string[]
    /** 忽略的模块名（精确匹配）如: ['ad', 'order'] */
    modules?: string[]
    /** 忽略的文件名（支持通配符）如: ['system*', '*Api.ts', 'test*'] */
    files?: string[]
    /** 忽略的接口函数名（支持通配符）如: ['template*', '*Test', 'debug*'] */
    functions?: string[]
    /** 自定义过滤函数 */
    filter?: (moduleKey: string, apis: any[]) => boolean
  }
}

/**
 * 路径匹配工具（支持通配符）
 * @param pattern 匹配模式，如 '/base/ad/**'
 * @param path 实际路径，如 '/base/ad/pageAds'
 */
function matchPath(pattern: string, path: string): boolean {
  // 转换通配符为正则表达式
  // '/base/ad/**' -> /^\/base\/ad\/.*/
  // '/base/ad/*' -> /^\/base\/ad\/[^/]+$/
  const regexPattern = pattern
    .replace(/\//g, '\\/') // 转义斜杠
    .replace(/\*\*/g, '__DOUBLE_STAR__') // 先用占位符保护 **，避免被后续 * 替换影响
    .replace(/\*/g, '[^/]+') // * 匹配除斜杠外的任意字符
    .replace(/__DOUBLE_STAR__/g, '.*') // 最后将占位符还原为 .*

  const regex = new RegExp(`^${regexPattern}$`)
  return regex.test(path)
}

/**
 * 文件名匹配工具（支持通配符）
 * @param pattern 匹配模式，如 'system*', '*Api.ts', 'test*'
 * @param fileName 实际文件名，如 'systemApi.ts'
 */
function matchFileName(pattern: string, fileName: string): boolean {
  // 转换通配符为正则表达式
  // 'system*' -> /^system.*/
  // '*Api.ts' -> /^.*Api\.ts$/
  // 'test*Types.ts' -> /^test.*Types\.ts$/
  const regexPattern = pattern
    .replace(/\./g, '\\.') // 转义点号
    .replace(/\*/g, '.*') // * 匹配任意字符

  const regex = new RegExp(`^${regexPattern}$`)
  return regex.test(fileName)
}

/**
 * 接口函数名匹配工具（支持通配符）
 * @param pattern 匹配模式，如 'template*', '*Test', 'debug*'
 * @param functionName 实际函数名，如 'templateAdd', 'getUserTest'
 */
function matchFunctionName(pattern: string, functionName: string): boolean {
  // 转换通配符为正则表达式
  // 'template*' -> /^template.*/
  // '*Test' -> /^.*Test$/
  // 'debug*Api' -> /^debug.*Api$/
  const regexPattern = pattern.replace(/\*/g, '.*') // * 匹配任意字符

  const regex = new RegExp(`^${regexPattern}$`, 'i') // 忽略大小写
  return regex.test(functionName)
}

/**
 * 检查模块是否应该被忽略
 * @param moduleKey 模块标识（如：base/ad, common/mall/order）
 * @param apis API 列表
 * @param ignoreConfig 忽略配置
 */
function shouldIgnoreModule(moduleKey: string, apis: any[], ignoreConfig?: OpenApiPluginOptions['ignore']): boolean {
  if (!ignoreConfig) return false

  // 1. 检查模块名精确匹配
  if (ignoreConfig.modules) {
    const moduleName = moduleKey.split('/').pop() // base/ad -> ad
    if (moduleName && ignoreConfig.modules.includes(moduleName)) {
      return true
    }
  }

  // 2. 检查路径匹配（通配符）
  if (ignoreConfig.paths && apis.length > 0) {
    // 检查该模块下的任意一个 API 是否匹配忽略路径
    const hasMatchingPath = apis.some((api) => {
      return ignoreConfig.paths!.some((pattern) => matchPath(pattern, api.path))
    })
    if (hasMatchingPath) {
      return true
    }
  }

  // 3. 自定义过滤函数
  if (ignoreConfig.filter) {
    return ignoreConfig.filter(moduleKey, apis)
  }

  return false
}

/**
 * 获取 OpenAPI 文档
 */
async function fetchOpenApiDoc(url: string): Promise<OpenApiDocument> {
  return new Promise((resolve, reject) => {
    const client = url.startsWith('https') ? https : http

    client
      .get(url, (res) => {
        let data = ''

        res.on('data', (chunk) => {
          data += chunk
        })

        res.on('end', () => {
          try {
            const doc = JSON.parse(data) as OpenApiDocument
            resolve(doc)
          } catch (error) {
            reject(new Error(`解析 OpenAPI 文档失败: ${error}`))
          }
        })
      })
      .on('error', (error) => {
        reject(new Error(`获取 OpenAPI 文档失败: ${error.message}`))
      })
  })
}

/**
 * 确保目录存在
 */
function ensureDir(dir: string): void {
  if (!existsSync(dir)) {
    mkdirSync(dir, { recursive: true })
  }
}

/**
 * 写入文件
 */
function writeFile(filePath: string, content: string): void {
  ensureDir(dirname(filePath))
  writeFileSync(filePath, content, 'utf-8')
}

/**
 * 计算文件内容的MD5哈希值
 */
function getFileHash(filePath: string): string {
  if (!existsSync(filePath)) return ''
  try {
    const content = readFileSync(filePath, 'utf-8')
    // 标准化内容:去除前后空白,统一换行符
    const normalized = content.trim().replace(/\r\n/g, '\n')
    return createHash('md5').update(normalized, 'utf-8').digest('hex')
  } catch {
    return ''
  }
}

/**
 * 计算字符串内容的MD5哈希值
 */
function getContentHash(content: string): string {
  // 标准化内容:去除前后空白,统一换行符
  const normalized = content.trim().replace(/\r\n/g, '\n')
  return createHash('md5').update(normalized, 'utf-8').digest('hex')
}

/**
 * 决定文件写入策略
 * @returns { path: 文件路径, shouldWrite: 是否应该写入 }
 */
function decideWriteStrategy(basePath: string, fileName: string, newContent: string): { path: string; shouldWrite: boolean; reason: string } {
  const normalPath = `${basePath}/${fileName}`
  const generatedPath = `${basePath}/${fileName.replace(/\.(ts)$/, '.generated.$1')}`

  // 情况1: 原文件不存在 → 直接生成原文件
  if (!existsSync(normalPath)) {
    return { path: normalPath, shouldWrite: true, reason: '文件不存在，创建新文件' }
  }

  // 情况2: 原文件存在 → 比较内容哈希
  const existingHash = getFileHash(normalPath)
  const newHash = getContentHash(newContent)

  // 内容完全相同 → 跳过生成
  if (existingHash === newHash) {
    return { path: generatedPath, shouldWrite: false, reason: '文件内容相同，跳过生成' }
  }

  // 内容不同 → 已修改，生成 .generated.ts
  return { path: generatedPath, shouldWrite: true, reason: '文件内容已变更，生成 .generated.ts' }
}

/**
 * 生成代码
 * @param doc OpenAPI 文档对象
 * @param outputDir 输出目录（相对路径）
 * @param projectRoot 项目根目录（绝对路径）
 * @param ignoreConfig 忽略配置
 */
async function generateCode(
  doc: OpenApiDocument,
  outputDir: string,
  projectRoot: string,
  ignoreConfig?: OpenApiPluginOptions['ignore']
): Promise<void> {
  const outputPath = resolve(projectRoot, outputDir)

  console.log('📦 开始解析 OpenAPI 文档...')

  // 1. 提取所有类型定义（Bo、Vo、Query等）
  const allTypes = extractTypes(doc)
  console.log(`✅ 提取到 ${allTypes.size} 个类型定义`)

  // 2. 提取所有 API 接口，按模块分组
  const apisByModule = extractApis(doc)
  console.log(`✅ 提取到 ${apisByModule.size} 个模块`)

  // 打印每个模块的接口数量
  apisByModule.forEach((apis, moduleKey) => {
    console.log(`  📂 ${moduleKey}: ${apis.length} 个接口`)
  })

  let totalApis = 0
  apisByModule.forEach((apis) => {
    totalApis += apis.length
  })
  console.log(`✅ 提取到 ${totalApis} 个 API 函数`)

  // 3. 按模块生成文件
  apisByModule.forEach((apis, moduleKey) => {
    if (apis.length === 0) return

    // 检查模块是否应该被忽略
    if (shouldIgnoreModule(moduleKey, apis, ignoreConfig)) {
      console.log(`  ⏭️  忽略模块: ${moduleKey} (匹配忽略规则)`)
      return
    }

    // moduleKey 格式：app/home, common/mall/order, base/ad
    const pathParts = moduleKey.split('/')
    const level1 = pathParts[0]

    // 计算完整路径和模块名
    // app/home -> app/home, home
    // common/mall/order -> common/mall/order, order
    // base/ad -> business/base/ad, ad
    let fullModulePath = ''
    let subModule = ''

    if (level1 === 'app' || level1 === 'common') {
      fullModulePath = moduleKey
      subModule = pathParts[pathParts.length - 1]
    } else {
      fullModulePath = 'business/' + moduleKey
      subModule = pathParts[pathParts.length - 1]
    }

    if (!fullModulePath || !subModule) {
      console.warn(`  ⚠️  跳过模块 ${moduleKey}: 无法解析路径`)
      return
    }

    // 收集此模块使用的所有类型
    const usedTypes = collectUsedTypes(apis)
    const typeNamesToGenerate = new Set<string>()

    usedTypes.forEach((typeName) => {
      typeNamesToGenerate.add(typeName)

      // Query类型需要同时生成对应的Bo类型
      if (typeName.endsWith('Query')) {
        const boName = typeName.replace(/Query$/, 'Bo')
        if (allTypes.has(boName)) {
          typeNamesToGenerate.add(boName)
        }
      }

      // Vo类型如果有对应的Bo类型，也一起生成（用于自动生成Query）
      if (typeName.endsWith('Vo')) {
        const boName = typeName.replace(/Vo$/, 'Bo')
        if (allTypes.has(boName)) {
          typeNamesToGenerate.add(boName)
        }
      }
    })

    // 生成类型文件（*Types.ts）
    const moduleTypes = Array.from(typeNamesToGenerate)
      .map((typeName) => allTypes.get(typeName))
      .filter((t) => t !== undefined)

    if (moduleTypes.length > 0) {
      const typesContent = generateTypesFile(moduleTypes, subModule)
      const moduleDir = join(outputPath, fullModulePath)
      const typesFileName = `${subModule}Types.ts`

      // 检查文件是否被忽略
      const isFileIgnored = ignoreConfig?.files?.some((pattern) => matchFileName(pattern, typesFileName)) || false

      if (isFileIgnored) {
        console.log(`  ⏭️  忽略文件: ${fullModulePath}/${typesFileName} (匹配文件忽略规则)`)
      } else {
        const typeStrategy = decideWriteStrategy(moduleDir, typesFileName, typesContent)
        if (typeStrategy.shouldWrite) {
          writeFile(typeStrategy.path, typesContent)
          const fileName = typeStrategy.path.split(/[/\\]/).pop()
          console.log(`  ✍️  生成类型文件: ${fullModulePath}/${fileName} (${typeStrategy.reason})`)
        } else {
          console.log(`  ⏭️  跳过生成: ${fullModulePath}/${typesFileName} (${typeStrategy.reason})`)
        }
      }
    }

    // 生成 API 函数文件（*Api.ts）
    // 先过滤掉需要忽略的接口函数
    let filteredApis = apis
    if (ignoreConfig?.functions && ignoreConfig.functions.length > 0) {
      const originalCount = apis.length
      filteredApis = apis.filter((api) => {
        // 检查函数名是否匹配忽略规则
        const shouldIgnore = ignoreConfig.functions!.some((pattern) => matchFunctionName(pattern, api.name))
        if (shouldIgnore) {
          console.log(`  ⏭️  忽略接口: ${api.name} (匹配函数忽略规则)`)
        }
        return !shouldIgnore
      })
      const ignoredCount = originalCount - filteredApis.length
      if (ignoredCount > 0) {
        console.log(`  📊 ${fullModulePath}: 过滤 ${ignoredCount} 个接口，保留 ${filteredApis.length} 个`)
      }
    }

    // 如果所有接口都被过滤了，跳过文件生成
    if (filteredApis.length === 0) {
      console.log(`  ⏭️  跳过模块: ${fullModulePath} (所有接口都被过滤)`)
      return
    }

    const apiContent = generateApiFile(filteredApis, usedTypes, subModule)
    const moduleDir = join(outputPath, fullModulePath)
    const apiFileName = `${subModule}Api.ts`

    // 检查文件是否被忽略
    const isFileIgnored = ignoreConfig?.files?.some((pattern) => matchFileName(pattern, apiFileName)) || false

    if (isFileIgnored) {
      console.log(`  ⏭️  忽略文件: ${fullModulePath}/${apiFileName} (匹配文件忽略规则)`)
    } else {
      const apiStrategy = decideWriteStrategy(moduleDir, apiFileName, apiContent)
      if (apiStrategy.shouldWrite) {
        writeFile(apiStrategy.path, apiContent)
        const fileName = apiStrategy.path.split(/[/\\]/).pop()
        console.log(`  ✍️  生成 API 文件: ${fullModulePath}/${fileName} (${filteredApis.length} 个函数, ${apiStrategy.reason})`)
      } else {
        console.log(`  ⏭️  跳过生成: ${fullModulePath}/${apiFileName} (${apiStrategy.reason})`)
      }
    }
  })

  console.log(`\n✨ 代码生成完成！输出目录: ${outputDir}`)
}

/**
 * OpenAPI 代码生成插件
 */
export default function createOpenApiPlugin(options: OpenApiPluginOptions): Plugin {
  const { input, output, mode = 'manual', enabled = true, beforeGenerate, afterGenerate, ignore } = options

  if (!enabled) {
    return {
      name: 'vite-plugin-openapi',
      apply: 'serve'
    }
  }

  let projectRoot = ''

  /** 执行代码生成 */
  const generate = async () => {
    try {
      console.log('\n🚀 OpenAPI 代码生成开始...')
      console.log(`📄 文档地址: ${input}`)
      console.log(`📁 输出目录: ${output}`)

      if (beforeGenerate) {
        await beforeGenerate()
      }

      // 获取 OpenAPI 文档
      const doc = await fetchOpenApiDoc(input)

      // 生成代码
      await generateCode(doc, output, projectRoot, ignore)

      if (afterGenerate) {
        await afterGenerate()
      }
    } catch (error) {
      console.error('❌ OpenAPI 代码生成失败:', error)
    }
  }

  return {
    name: 'vite-plugin-openapi',
    // 移除 apply: 'serve' 限制，让插件在所有模式下都生效（包括构建模式）

    configResolved(config) {
      projectRoot = config.root
    },

    async buildStart() {
      if (mode === 'once') {
        console.log('🔧 OpenAPI 生成模式: once (启动时生成一次)')
        await generate()
      } else {
        console.log('🔧 OpenAPI 生成模式: manual (通过 HTTP 端点手动触发)')
      }
    },

    configureServer(server) {
      // 打印端口信息（在服务器启动后）
      server.httpServer?.once('listening', () => {
        const address = server.httpServer?.address()
        let port = 5173 // 默认端口

        if (address && typeof address === 'object') {
          port = address.port
        }

        if (mode === 'manual') {
          console.log(`💡 访问 http://localhost:${port}/__openapi_generate 触发生成`)
        }
      })

      // 添加 HTTP 端点用于手动触发生成（支持 GET 和 POST）
      server.middlewares.use(async (req, res, next) => {
        if (req.url === '/__openapi_generate' && (req.method === 'POST' || req.method === 'GET')) {
          console.log('📡 收到手动生成请求')

          await generate()

          res.statusCode = 200
          res.setHeader('Content-Type', 'text/html; charset=utf-8')
          res.end(`
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>OpenAPI 代码生成成功</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
      overflow: hidden;
    }

    /* 几何背景容器 */
    .geometric-background {
      position: fixed;
      width: 100vw;
      height: 100vh;
      overflow: hidden;
      background: linear-gradient(135deg, #e8eef9 0%, #f0f4fa 100%);
    }

    /* 几何装饰元素 */
    .geometric-decorations {
      position: absolute;
      inset: 0;
      pointer-events: none;
    }

    .geo-element {
      position: absolute;
      opacity: 0;
      animation-fill-mode: forwards;
      animation-duration: 0.8s;
      animation-timing-function: cubic-bezier(0.25, 0.46, 0.45, 0.94);
    }

    /* 圆形轮廓 */
    .circle-outline {
      top: 10%;
      left: 25%;
      width: 42px;
      height: 42px;
      border: 2px solid #c2d5f0;
      border-radius: 50%;
      animation-name: fadeInUp;
    }

    /* 旋转方块 */
    .square-rotated {
      top: 50%;
      left: 16%;
      width: 60px;
      height: 60px;
      background-color: #d8e4f5;
      transform: rotate(-25deg);
      animation-name: fadeInLeft;
    }

    /* 小圆点 */
    .circle-small {
      bottom: 26%;
      left: 30%;
      width: 18px;
      height: 18px;
      background-color: #c2d5f0;
      border-radius: 50%;
      animation-name: fadeInUp;
    }

    /* 右下方块 */
    .square-bottom-right {
      right: 10%;
      bottom: 10%;
      width: 50px;
      height: 50px;
      background-color: #c2d5f0;
      transform: rotate(45deg);
      animation-name: fadeInRight;
    }

    /* 背景泡泡 */
    .bg-bubble {
      top: -120px;
      right: -120px;
      width: 360px;
      height: 360px;
      background-color: #d8e4f5;
      border-radius: 50%;
      animation-name: scaleIn;
      animation-duration: 1.2s;
    }

    /* 装饰点 */
    .dot {
      width: 14px;
      height: 14px;
      background-color: #a8c5ec;
      border-radius: 50%;
      animation-name: bounceIn;
      animation-duration: 0.6s;
    }

    .dot-top-left {
      top: 140px;
      left: 100px;
    }

    .dot-top-right {
      top: 140px;
      right: 120px;
    }

    .dot-center-right {
      top: 46%;
      right: 22%;
      background-color: #c2d5f0;
    }

    /* 叠加方块组 */
    .squares-group {
      position: absolute;
      bottom: 18px;
      left: 20px;
      width: 140px;
      height: 140px;
    }

    .square {
      position: absolute;
      display: block;
      border-radius: 8px;
      box-shadow: 0 8px 24px rgba(64, 87, 167, 0.12);
    }

    .square-blue {
      top: 12px;
      left: 30px;
      z-index: 2;
      width: 50px;
      height: 50px;
      background-color: rgba(93, 135, 255, 0.3);
      transform: rotate(-10deg);
      animation-name: fadeInLeft;
      animation-delay: 0.2s;
    }

    .square-pink {
      top: 30px;
      left: 48px;
      z-index: 1;
      width: 70px;
      height: 70px;
      background-color: rgba(93, 135, 255, 0.15);
      transform: rotate(10deg);
      animation-name: fadeInLeft;
      animation-delay: 0.2s;
    }

    .square-purple {
      top: 66px;
      left: 86px;
      z-index: 3;
      width: 32px;
      height: 32px;
      background-color: rgba(93, 135, 255, 0.45);
      animation-name: fadeInLeft;
      animation-delay: 0.4s;
    }

    /* 内容容器 */
    .page-container {
      position: relative;
      z-index: 10;
      width: 100vw;
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .success-container {
      background: white;
      border-radius: 24px;
      padding: 60px 40px;
      width: 600px;
      min-width: 480px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
      border: 1px solid rgba(0, 0, 0, 0.05);
      text-align: center;
      opacity: 0;
      animation: slideUp 0.6s ease-out 0.3s forwards;
    }

    /* 图标区域 */
    .success-icon {
      font-size: 80px;
      margin-bottom: 30px;
      opacity: 0;
      animation: bounceIn 0.8s ease-out 0.5s forwards;
    }

    /* 标题 */
    .success-title {
      font-size: 32px;
      font-weight: 700;
      color: #2d3748;
      margin-bottom: 16px;
      opacity: 0;
      animation: slideUp 0.6s ease-out 0.8s forwards;
    }

    /* 副标题 */
    .success-subtitle {
      font-size: 16px;
      color: #718096;
      margin-bottom: 40px;
      opacity: 0;
      animation: slideUp 0.6s ease-out 1s forwards;
    }

    /* 信息区 */
    .info-section {
      background: #f7fafc;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 30px;
      text-align: left;
      opacity: 0;
      animation: slideUp 0.6s ease-out 1.2s forwards;
    }

    .info-item {
      margin: 12px 0;
      font-size: 14px;
      color: #4a5568;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .info-label {
      font-weight: 600;
      color: #2d3748;
      min-width: 80px;
    }

    .info-value {
      background: #2d3748;
      color: #68d391;
      padding: 4px 12px;
      border-radius: 6px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      flex: 1;
      word-break: break-all;
    }

    /* 按钮区 */
    .action-buttons {
      display: flex;
      gap: 16px;
      justify-content: center;
      margin-bottom: 20px;
      opacity: 0;
      animation: slideUp 0.6s ease-out 1.4s forwards;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 12px 24px;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      text-decoration: none;
      transition: all 0.3s ease;
      border: none;
      cursor: pointer;
    }

    .btn-primary {
      background: #409eff;
      color: white;
      box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
    }

    .btn-secondary {
      background: rgba(64, 158, 255, 0.08);
      color: #409eff;
      border: 1px solid rgba(64, 158, 255, 0.2);
    }

    .btn-secondary:hover {
      background: rgba(64, 158, 255, 0.12);
      transform: translateY(-2px);
    }

    /* 提示文字 */
    .hint-text {
      color: #a0aec0;
      font-size: 12px;
      opacity: 0;
      animation: slideUp 0.6s ease-out 1.6s forwards;
    }

    /* 动画定义 */
    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes fadeInLeft {
      from {
        opacity: 0;
        transform: translateX(-30px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @keyframes fadeInRight {
      from {
        opacity: 0;
        transform: translateX(30px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @keyframes scaleIn {
      from {
        opacity: 0;
        transform: scale(0.8);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }

    @keyframes bounceIn {
      0% {
        opacity: 0;
        transform: scale(0.3);
      }
      50% {
        opacity: 1;
        transform: scale(1.05);
      }
      70% {
        transform: scale(0.9);
      }
      100% {
        opacity: 1;
        transform: scale(1);
      }
    }

    @keyframes slideUp {
      0% {
        opacity: 0;
        transform: translateY(30px);
      }
      100% {
        opacity: 1;
        transform: translateY(0);
      }
    }

    /* 响应式 */
    @media (max-width: 768px) {
      .success-container {
        width: 90%;
        min-width: 320px;
        padding: 40px 30px;
      }

      .success-title {
        font-size: 24px;
      }

      .success-subtitle {
        font-size: 14px;
      }

      .action-buttons {
        flex-direction: column;
      }

      .btn {
        width: 100%;
        justify-content: center;
      }
    }
  </style>
</head>
<body>
  <!-- 几何背景 -->
  <div class="geometric-background">
    <div class="geometric-decorations">
      <div class="geo-element circle-outline"></div>
      <div class="geo-element square-rotated"></div>
      <div class="geo-element circle-small"></div>
      <div class="geo-element square-bottom-right"></div>
      <div class="geo-element bg-bubble"></div>
      <div class="geo-element dot dot-top-left"></div>
      <div class="geo-element dot dot-top-right"></div>
      <div class="geo-element dot dot-center-right"></div>
      <div class="squares-group">
        <i class="geo-element square square-blue"></i>
        <i class="geo-element square square-pink"></i>
        <i class="geo-element square square-purple"></i>
      </div>
    </div>
  </div>

  <!-- 内容区域 -->
  <div class="page-container">
    <div class="success-container">
      <div class="success-icon">✨</div>
      <h1 class="success-title">代码生成成功！</h1>
      <p class="success-subtitle">OpenAPI 接口和类型已自动生成</p>

      <div class="info-section">
        <div class="info-item">
          <span class="info-label">📁 输出目录</span>
          <span class="info-value">${output}</span>
        </div>
        <div class="info-item">
          <span class="info-label">📄 文档地址</span>
          <span class="info-value">${input}</span>
        </div>
        <div class="info-item">
          <span class="info-label">⏰ 生成时间</span>
          <span class="info-value">${new Date().toLocaleString('zh-CN')}</span>
        </div>
      </div>

      <div class="action-buttons">
        <a href="javascript:location.reload()" class="btn btn-primary">
          <span>🔄</span>
          重新生成
        </a>
        <a href="/" class="btn btn-secondary">
          <span>🏠</span>
          返回首页
        </a>
      </div>

      <p class="hint-text">💡 提示：查看控制台日志了解详细生成信息</p>
    </div>
  </div>

  <script>
    console.log('%c✨ OpenAPI 代码生成成功！', 'color: #10b981; font-size: 16px; font-weight: bold;');
    console.log('📁 输出目录: ${output}');
    console.log('📄 文档地址: ${input}');
  </script>
</body>
</html>
          `)
          return
        }

        next()
      })
    }
  }
}
