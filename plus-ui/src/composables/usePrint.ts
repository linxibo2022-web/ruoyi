// 打印工具
import { nextTick } from 'vue'

/**
 * 单张图片打印选项
 */
interface PrintImageOptions {
  /** 打印文档标题 */
  title?: string
  /** 图片显示宽度 (CSS值) */
  width?: string
  /** 图片显示高度 (CSS值) */
  height?: string
  /** 图片适应方式 */
  fit?: 'contain' | 'cover' | 'fill'
}

/**
 * 多张图片打印选项
 */
interface PrintImagesOptions {
  /** 打印文档标题 */
  title?: string
  /** 布局方式 */
  layout?: 'vertical' | 'horizontal' | 'grid'
  /** 图片间距 (CSS值) */
  spacing?: string
}

/**
 * 打印功能返回类型
 */
interface UsePrintReturn {
  /** 打印整页 */
  printPage: () => void
  /** 打印指定元素 */
  printElement: (elementId: string) => Promise<void>
  /** 打印单张图片 */
  printImage: (imageUrl: string, options?: PrintImageOptions) => Promise<void>
  /** 打印多张图片 */
  printImages: (imageUrls: string[], options?: PrintImagesOptions) => Promise<void>
  /** 打印HTML内容 */
  printHtml: (htmlContent: string, styles?: string) => Promise<void>
}

/**
 * Vue 3 打印功能组合函数
 * 提供多种打印方式：整页、指定元素、单张图片、多张图片、HTML内容
 */
export function usePrint(): UsePrintReturn {
  /**
   * 打印当前整个页面
   * 使用浏览器原生打印功能
   */
  const printPage = () => {
    window.print()
  }

  /**
   * 打印页面中指定的DOM元素
   * 会复制当前页面的所有样式，确保打印效果与页面显示一致
   * @param elementId - 要打印的元素ID
   * @example
   * await printElement('my-table') // 打印ID为my-table的元素
   */
  const printElement = async (elementId: string): Promise<void> => {
    const element = document.getElementById(elementId)
    if (!element) {
      console.warn(`Element with id "${elementId}" not found`)
      return
    }

    const printWindow = window.open('', '_blank')
    if (!printWindow) {
      console.error('Failed to open print window')
      return
    }
    if (!printWindow) {
      console.error('Failed to open print window')
      return
    }
    if (!printWindow) {
      console.error('Failed to open print window')
      return
    }
    if (!printWindow) {
      console.error('Failed to open print window')
      return
    }

    // 获取当前页面所有样式
    const styles = Array.from(document.styleSheets)
      .map((styleSheet: CSSStyleSheet) => {
        try {
          return Array.from(styleSheet.cssRules)
            .map((rule: CSSRule) => rule.cssText)
            .join('')
        } catch (e) {
          // 处理跨域样式表访问限制
          return ''
        }
      })
      .join('')

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <meta charset="utf-8">
          <title>打印</title>
          <style>
            ${styles}
            body {
              margin: 0;
              padding: 20px;
              font-family: Arial, sans-serif;
            }
            img {
              max-width: 100%;
              height: auto;
              page-break-inside: avoid;
            }
            @media print {
              body {
                margin: 0;
                padding: 30px 0 0 0;
              }
              img {
                max-width: 100%;
                height: auto;
                page-break-inside: avoid;
              }
            }
          </style>
        </head>
        <body>
          ${element.outerHTML}
        </body>
      </html>
    `)

    printWindow.document.close()

    // 等待图片加载完成后再打印
    await waitForImages(printWindow.document)

    printWindow.focus()
    printWindow.print()
    printWindow.close()
  }

  /**
   * 打印单张图片
   * 适用于需要单独打印一张图片的场景，支持多种显示选项
   * @param imageUrl - 图片URL地址
   * @param options - 打印选项
   * @example
   * // 基础用法
   * await printImage('/path/to/image.jpg')
   *
   * // 自定义选项
   * await printImage('/path/to/image.jpg', {
   *   title: '产品图片',
   *   width: '300px',
   *   height: '200px',
   *   fit: 'cover'
   * })
   */
  const printImage = async (imageUrl: string, options: PrintImageOptions = {}): Promise<void> => {
    const {
      title = '图片打印',
      width = 'auto',
      height = 'auto',
      fit = 'contain' // contain, cover, fill
    } = options

    const printWindow = window.open('', '_blank')

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <meta charset="utf-8">
          <title>${title}</title>
          <style>
            body {
              margin: 0;
              padding: 0;
              display: flex;
              justify-content: center;
              align-items: center;
              min-height: 100vh;
            }
            img {
              max-width: 100%;
              max-height: 100vh;
              width: ${width};
              height: ${height};
              object-fit: ${fit};
              page-break-inside: avoid;
            }
            @media print {
              body {
                margin: 0;
                padding: 30px 0 0 0;
                display: flex;
                justify-content: center;
                align-items: flex-start;
                min-height: calc(100vh - 30px);
              }
              img {
                max-width: 100%;
                max-height: calc(100vh - 30px);
                page-break-inside: avoid;
              }
            }
          </style>
        </head>
        <body>
          <img src="${imageUrl}" alt="${title}" />
        </body>
      </html>
    `)

    printWindow.document.close()

    // 等待图片加载完成
    const img = printWindow.document.querySelector('img')
    if (img) {
      await new Promise<void>((resolve) => {
        if (img.complete) {
          resolve()
        } else {
          img.onload = () => resolve()
          img.onerror = () => resolve() // 即使加载失败也继续执行
        }
      })
    }

    printWindow.focus()
    printWindow.print()
    printWindow.close()
  }

  /**
   * 批量打印多张图片
   * 适用于需要同时打印多张图片的场景，支持不同布局方式
   * @param imageUrls - 图片URL数组
   * @param options - 打印选项
   * @example
   * // 基础用法 - 垂直排列
   * await printImages(['/img1.jpg', '/img2.jpg', '/img3.jpg'])
   *
   * // 网格布局
   * await printImages(['/img1.jpg', '/img2.jpg', '/img3.jpg'], {
   *   title: '产品图集',
   *   layout: 'grid',
   *   spacing: '10px'
   * })
   */
  const printImages = async (imageUrls: string[], options: PrintImagesOptions = {}): Promise<void> => {
    const {
      title = '图片打印',
      layout = 'vertical', // vertical, horizontal, grid
      spacing = '20px'
    } = options

    if (!Array.isArray(imageUrls) || imageUrls.length === 0) {
      console.warn('printImages: imageUrls must be a non-empty array')
      return
    }

    const printWindow = window.open('', '_blank')

    const imagesHtml = imageUrls
      .map((url, index) => `<img src="${url}" alt="图片 ${index + 1}" />`)
      .join('')

    // 根据布局类型生成对应的CSS样式
    const layoutStyles: Record<string, string> = {
      vertical: `
        .images-container {
          display: flex;
          flex-direction: column;
          gap: ${spacing};
          align-items: center;
        }
        img {
          max-width: 100%;
          height: auto;
        }
      `,
      horizontal: `
        .images-container {
          display: flex;
          flex-direction: row;
          gap: ${spacing};
          flex-wrap: wrap;
          justify-content: center;
        }
        img {
          max-width: calc(50% - ${spacing}/2);
          height: auto;
        }
      `,
      grid: `
        .images-container {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: ${spacing};
        }
        img {
          width: 100%;
          height: auto;
        }
      `
    }

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <meta charset="utf-8">
          <title>${title}</title>
          <style>
            body {
              margin: 0;
              padding: 20px;
              font-family: Arial, sans-serif;
            }
            ${layoutStyles[layout] || layoutStyles.vertical}
            img {
              page-break-inside: avoid;
              object-fit: contain;
            }
            @media print {
              body {
                margin: 0;
                padding: 30px 10px 10px 10px;
              }
            }
          </style>
        </head>
        <body>
          <div class="images-container">
            ${imagesHtml}
          </div>
        </body>
      </html>
    `)

    printWindow.document.close()

    // 等待所有图片加载完成
    await waitForImages(printWindow.document)

    printWindow.focus()
    printWindow.print()
    printWindow.close()
  }

  /**
   * 等待文档中所有图片加载完成的辅助函数
   * @param doc - 文档对象
   * @returns 返回所有图片加载完成的Promise
   */
  const waitForImages = (doc: Document): Promise<void[]> => {
    const images = doc.querySelectorAll('img')
    const imagePromises = Array.from(images).map((img: HTMLImageElement) => {
      return new Promise<void>((resolve) => {
        if (img.complete) {
          resolve()
        } else {
          img.onload = () => resolve()
          img.onerror = () => resolve() // 加载失败也要resolve，避免阻塞
        }
      })
    })
    return Promise.all(imagePromises)
  }

  /**
   * 打印自定义HTML内容
   * 适用于需要打印动态生成的HTML内容的场景
   * @param htmlContent - 要打印的HTML字符串
   * @param styles - 额外的CSS样式
   * @example
   * const htmlContent = `
   *   <h1>报告标题</h1>
   *   <p>这是报告内容...</p>
   *   <img src="/chart.png" alt="图表" />
   * `
   * const customStyles = `
   *   h1 { color: blue; text-align: center; }
   *   p { font-size: 14px; line-height: 1.6; }
   * `
   * await printHtml(htmlContent, customStyles)
   */
  const printHtml = async (htmlContent: string, styles: string = ''): Promise<void> => {
    if (!htmlContent || typeof htmlContent !== 'string') {
      console.warn('printHtml: htmlContent must be a non-empty string')
      return
    }

    const printWindow = window.open('', '_blank')

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <meta charset="utf-8">
          <title>打印</title>
          <style>
            body {
              margin: 0;
              padding: 20px;
              font-family: Arial, sans-serif;
            }
            img {
              max-width: 100%;
              height: auto;
              page-break-inside: avoid;
            }
            ${styles}
            @media print {
              body {
                margin: 0;
                padding: 30px 10px 10px 10px;
              }
            }
          </style>
        </head>
        <body>
          ${htmlContent}
        </body>
      </html>
    `)

    printWindow.document.close()

    // 如果HTML中包含图片，等待加载完成
    await waitForImages(printWindow.document)

    printWindow.focus()
    printWindow.print()
    printWindow.close()
  }

  // 返回所有可用的打印方法
  return {
    printPage, // 打印整页
    printElement, // 打印指定元素
    printImage, // 打印单张图片
    printImages, // 打印多张图片
    printHtml // 打印HTML内容
  }
}
