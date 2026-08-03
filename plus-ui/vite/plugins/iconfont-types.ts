// vite/plugins/iconfont-types.ts

import type { Plugin } from 'vite'
import { existsSync, readdirSync, readFileSync, writeFileSync, mkdirSync, statSync } from 'node:fs'
import { join, resolve, dirname, relative } from 'node:path'

interface IconItem {
  code: string
  name: string
}

interface IconifyIconItem extends IconItem {
  value: string // iconify 图标还有 value 属性
}

interface IconfontGlyph {
  font_class: string
  name: string
  unicode: string
  unicode_decimal: number
}

interface IconifyIcon {
  code: string
  name: string
  value: string
}

interface IconfontJson {
  glyphs: IconfontGlyph[]
}

interface IconifyJson {
  type: 'iconify'
  icons: IconifyIcon[]
}

export default (): Plugin => {
  const iconsDir = 'src/assets/icons'
  const outputPath = 'src/types/icons.d.ts'
  let projectRoot = ''

  // 解析 iconfont JSON 文件
  const parseIconfontJson = (jsonPath: string): IconItem[] => {
    try {
      const content = readFileSync(jsonPath, 'utf-8')
      const data: IconfontJson = JSON.parse(content)

      if (data.glyphs && Array.isArray(data.glyphs)) {
        return data.glyphs.map((glyph) => ({
          code: glyph.font_class || glyph.name,
          name: glyph.name
        }))
      }

      return []
    } catch (error) {
      console.warn(`解析 iconfont 文件失败: ${jsonPath}`, error)
      return []
    }
  }

  // 解析 iconify JSON 文件
  const parseIconifyJson = (jsonPath: string): IconifyIconItem[] => {
    try {
      const content = readFileSync(jsonPath, 'utf-8')
      const data: IconifyJson = JSON.parse(content)

      if (data.type === 'iconify' && data.icons && Array.isArray(data.icons)) {
        return data.icons.map((icon) => ({
          code: icon.code,
          name: icon.name,
          value: icon.value
        }))
      }

      return []
    } catch (error) {
      console.warn(`解析 iconify 文件失败: ${jsonPath}`, error)
      return []
    }
  }

  // 扫描本地静态 SVG 目录（src/assets/icons/svg/*.svg）
  // 文件名（不含扩展名）即为图标 code，与 vite-plugin-svg-icons-ng 的 symbolId 规则一致
  const parseSvgDir = (svgDirPath: string): IconItem[] => {
    if (!existsSync(svgDirPath)) return []
    try {
      return readdirSync(svgDirPath)
        .filter((f) => f.toLowerCase().endsWith('.svg'))
        .map((f) => {
          const code = f.replace(/\.svg$/i, '')
          return { code, name: code }
        })
    } catch (error) {
      console.warn(`扫描 SVG 目录失败: ${svgDirPath}`, error)
      return []
    }
  }

  // 获取所有图标
  const getAllIcons = (iconsPath: string) => {
    const iconfontIcons: IconItem[] = []
    const iconifyIcons: IconifyIconItem[] = []
    const svgIcons: IconItem[] = []
    const seenCodes = new Set<string>()

    if (!existsSync(iconsPath)) {
      console.warn(`图标目录不存在: ${iconsPath}`)
      return { iconfontIcons, iconifyIcons, svgIcons, allIcons: [] }
    }

    // 扫描目录下的文件
    const scanDirectory = (dirPath: string) => {
      try {
        const items = readdirSync(dirPath)

        items.forEach((item) => {
          const itemPath = join(dirPath, item)

          try {
            if (statSync(itemPath).isDirectory()) {
              scanDirectory(itemPath) // 递归扫描子目录
            } else if (item.endsWith('.json')) {
              // 先尝试解析为 iconify 格式
              const iconifyIconsFromFile = parseIconifyJson(itemPath)
              if (iconifyIconsFromFile.length > 0) {
                console.log(`发现 iconify 图标文件: ${itemPath}, 图标数量: ${iconifyIconsFromFile.length}`)
                iconifyIcons.push(...iconifyIconsFromFile)
              } else {
                // 否则按 iconfont 格式解析
                const iconfontIconsFromFile = parseIconfontJson(itemPath)
                if (iconfontIconsFromFile.length > 0) {
                  console.log(`发现 iconfont 图标文件: ${itemPath}, 图标数量: ${iconfontIconsFromFile.length}`)
                  iconfontIcons.push(...iconfontIconsFromFile)
                }
              }
            }
          } catch (error) {
            console.warn(`跳过无法访问的文件: ${itemPath}`)
          }
        })
      } catch (error) {
        console.warn(`无法读取目录: ${dirPath}`)
      }
    }

    scanDirectory(iconsPath)

    // 扫描 svg/ 子目录（仅第一层，与 vite-plugin-svg-icons-ng 的扫描范围一致）
    const svgFromDir = parseSvgDir(join(iconsPath, 'svg'))
    if (svgFromDir.length > 0) {
      console.log(`发现本地 SVG 图标: ${svgFromDir.length} 个`)
      svgIcons.push(...svgFromDir)
    }

    // 合并所有图标，去重
    const allIcons: IconItem[] = []

    // 先添加 iconfont 图标
    iconfontIcons.forEach((icon) => {
      if (!seenCodes.has(icon.code)) {
        seenCodes.add(icon.code)
        allIcons.push(icon)
      }
    })

    // 再添加 iconify 图标（只保留 code 和 name）
    iconifyIcons.forEach((icon) => {
      if (!seenCodes.has(icon.code)) {
        seenCodes.add(icon.code)
        allIcons.push({ code: icon.code, name: icon.name })
      }
    })

    // 最后添加本地 SVG sprite 图标
    svgIcons.forEach((icon) => {
      if (!seenCodes.has(icon.code)) {
        seenCodes.add(icon.code)
        allIcons.push(icon)
      }
    })

    console.log(`图标扫描完成 - iconfont: ${iconfontIcons.length}, iconify: ${iconifyIcons.length}, svg: ${svgIcons.length}, 总计: ${allIcons.length}`)

    return {
      iconfontIcons: iconfontIcons.sort((a, b) => a.code.localeCompare(b.code)),
      iconifyIcons: iconifyIcons.sort((a, b) => a.code.localeCompare(b.code)),
      svgIcons: svgIcons.sort((a, b) => a.code.localeCompare(b.code)),
      allIcons: allIcons.sort((a, b) => a.code.localeCompare(b.code))
    }
  }

  // 生成类型文件内容
  const generateTypeContent = (iconData: ReturnType<typeof getAllIcons>): string => {
    const { iconfontIcons, iconifyIcons, svgIcons, allIcons } = iconData

    const iconCodes = allIcons.map((icon) => `    | '${icon.code}'`).join('\n')

    // iconfont 图标数组
    const iconfontArray = iconfontIcons.map((icon) => `  { code: '${icon.code}', name: '${icon.name}' }`).join(',\n')

    // iconify 图标数组
    const iconifyArray = iconifyIcons.map((icon) => `  { code: '${icon.code}', name: '${icon.name}', value: '${icon.value}' }`).join(',\n')

    // svg sprite 图标数组（来自 src/assets/icons/svg/*.svg）
    const svgArray = svgIcons.map((icon) => `  { code: '${icon.code}', name: '${icon.name}' }`).join(',\n')

    // 所有图标数组（用于图标选择器）
    const allArray = allIcons.map((icon) => `  { code: '${icon.code}', name: '${icon.name}' }`).join(',\n')

    return `/**
 * 图标类型声明文件
 *
 * 此文件由 iconfont-types 插件自动生成
 * 扫描目录: ${iconsDir}
 * 图标数量: ${allIcons.length} 个图标 (iconfont: ${iconfontIcons.length}, iconify: ${iconifyIcons.length}, svg: ${svgIcons.length})
 *
 * 请勿手动修改此文件，所有修改将在下次构建时被覆盖
 */

declare global {
  /** 图标代码类型 */
  type IconCode =\n${iconCodes || 'never'}
}

/** 图标项接口 */
export interface IconItem {
  code: string
  name: string
}

/** iconify 图标项接口 */
export interface IconifyIconItem extends IconItem {
  value: string
}

/** iconfont 图标列表 (仅来自 iconfont JSON 文件) */
export const ICONFONT_ICONS: IconItem[] = [
${iconfontArray}
]

/** iconify 预设图标列表 (包含 value 属性) */
export const ICONIFY_ICONS: IconifyIconItem[] = [
${iconifyArray}
]

/** 本地 SVG sprite 图标列表 (来自 src/assets/icons/svg/*.svg) */
export const SVG_ICONS: IconItem[] = [
${svgArray}
]

/** 所有可用图标列表 (用于图标选择器) */
export const ALL_ICONS: IconItem[] = [
${allArray}
]

/** 检查代码是否为有效的图标 */
export const isValidIconCode = (code: string): code is IconCode => {
  return ALL_ICONS.some((icon) => icon.code === code)
}

/** 检查代码是否为 iconfont 图标 */
export const isIconfontIcon = (code: string): boolean => {
  return ICONFONT_ICONS.some((icon) => icon.code === code)
}

/** 检查代码是否为 iconify 图标 */
export const isIconifyIcon = (code: string): boolean => {
  return ICONIFY_ICONS.some((icon) => icon.code === code)
}

/** 检查代码是否为本地 SVG sprite 图标 */
export const isSvgIcon = (code: string): boolean => {
  return SVG_ICONS.some((icon) => icon.code === code)
}

/** 获取 iconify 图标的 value */
export const getIconifyValue = (code: string): string | undefined => {
  return ICONIFY_ICONS.find((icon) => icon.code === code)?.value
}

/** 根据代码获取图标名称 */
export const getIconName = (code: IconCode): string => {
  return ALL_ICONS.find((icon) => icon.code === code)?.name || code
}

/** 搜索图标 */
export const searchIcons = (query: string): IconItem[] => {
  const searchTerm = query.toLowerCase()
  return ALL_ICONS.filter((icon) => icon.code.toLowerCase().includes(searchTerm) || icon.name.toLowerCase().includes(searchTerm))
}

/** 获取所有图标代码 */
export const getAllIconCodes = (): IconCode[] => {
  return ALL_ICONS.map((icon) => icon.code as IconCode)
}

export {}
`
  }

  // 写入类型文件
  const writeTypeFile = (showLog = true) => {
    const iconsPath = resolve(projectRoot, iconsDir)
    const outputFilePath = resolve(projectRoot, outputPath)

    try {
      const iconData = getAllIcons(iconsPath)
      const typeContent = generateTypeContent(iconData)

      const outputDir = dirname(outputFilePath)
      if (!existsSync(outputDir)) {
        mkdirSync(outputDir, { recursive: true })
      }

      writeFileSync(outputFilePath, typeContent, 'utf-8')

      if (showLog) {
        console.log(`图标类型已生成: ${outputPath} (${iconData.allIcons.length} 个图标)`)
      }
    } catch (error) {
      console.error('生成图标类型时出错:', error)
    }
  }

  return {
    name: 'iconfont-types',

    configResolved(config) {
      projectRoot = config.root
    },

    buildStart() {
      writeTypeFile()
    },

    handleHotUpdate({ file }) {
      const iconsPath = resolve(projectRoot, iconsDir)

      // JSON 变化（iconfont / iconify）或 svg/ 下 .svg 变化时重新生成类型
      if (file.startsWith(iconsPath) && (file.endsWith('.json') || file.endsWith('.svg'))) {
        console.log(`检测到图标文件变化: ${relative(projectRoot, file)}`)
        writeTypeFile()
      }
    }
  }
}
