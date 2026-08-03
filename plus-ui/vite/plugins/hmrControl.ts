import type { Plugin, ViteDevServer } from 'vite'

/**
 * HMR 控制插件
 * 提供 HTTP 端点来暂停/恢复文件监听，用于代码生成场景
 *
 * 使用场景：
 * 当后端代码生成器覆盖前端文件时，Vite 会检测到文件变化并触发 HMR，
 * 导致页面多次刷新或闪烁。通过此插件可以：
 * 1. 在代码生成前暂停文件监听
 * 2. 代码生成完成后恢复监听并触发单次全量刷新
 *
 * 端点：
 * - GET /__hmr_pause  暂停文件监听
 * - GET /__hmr_resume 恢复文件监听并刷新页面
 */
export function createHmrControlPlugin(): Plugin {
  let server: ViteDevServer
  let isPaused = false
  let unwatchedPaths: string[] = []

  return {
    name: 'vite-plugin-hmr-control',

    configureServer(_server) {
      server = _server

      // 暂停 HMR 端点
      server.middlewares.use((req, res, next) => {
        if (req.url === '/__hmr_pause') {
          handlePause(res)
          return
        }

        if (req.url === '/__hmr_resume') {
          handleResume(res)
          return
        }

        next()
      })
    }
  }

  /**
   * 处理暂停请求
   */
  function handlePause(res: any) {
    if (!isPaused && server.watcher) {
      isPaused = true

      // 获取需要暂停监听的路径（src 目录）
      const srcPath = server.config.root + '/src'
      unwatchedPaths = [srcPath]

      // 使用 unwatch 暂停对 src 目录的监听
      server.watcher.unwatch(srcPath)

      console.log('\x1b[36m%s\x1b[0m', '[HMR Control] 文件监听已暂停，等待代码生成...')
    }

    sendJsonResponse(res, { success: true, paused: true })
  }

  /**
   * 处理恢复请求
   * 延迟2秒后恢复监听，确保后端文件写入完成
   */
  function handleResume(res: any) {
    if (isPaused) {
      // 先返回响应，不阻塞前端
      sendJsonResponse(res, { success: true, paused: false })

      console.log('\x1b[33m%s\x1b[0m', '[HMR Control] 等待2秒后恢复文件监听...')

      // 延迟2秒后恢复监听
      setTimeout(() => {
        isPaused = false

        try {
          // 重新添加监听路径
          if (unwatchedPaths.length > 0) {
            server.watcher.add(unwatchedPaths)
            unwatchedPaths = []
          }

          // 触发全量刷新
          server.ws.send({ type: 'full-reload' })
          console.log('\x1b[32m%s\x1b[0m', '[HMR Control] 文件监听已恢复，页面已刷新')
        } catch (error) {
          console.error('\x1b[31m%s\x1b[0m', '[HMR Control] 恢复文件监听失败:', error)
        }
      }, 2000)

      return
    }

    sendJsonResponse(res, { success: true, paused: false })
  }

  /**
   * 发送 JSON 响应
   */
  function sendJsonResponse(res: any, data: object) {
    res.setHeader('Content-Type', 'application/json')
    res.setHeader('Access-Control-Allow-Origin', '*')
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
    res.statusCode = 200
    res.end(JSON.stringify(data))
  }
}
