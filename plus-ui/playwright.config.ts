// Playwright E2E 测试配置
import { defineConfig } from '@playwright/test'

export default defineConfig({
  // 测试目录
  testDir: './src/__tests__/e2e',
  // 全局超时 30s
  timeout: 30_000,
  // 失败重试：CI 里 1 次，本地 0 次
  retries: process.env.CI ? 1 : 0,
  // 报告格式
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list']
  ],
  use: {
    // 前端默认地址
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:80',
    // 截图：仅在失败时
    screenshot: 'only-on-failure',
    // 视频：失败时保留
    video: 'retain-on-failure'
  },
  // 浏览器配置
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' }
    }
  ]
})
