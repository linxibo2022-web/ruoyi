// Vitest 测试配置文件
import { defineConfig } from 'vitest/config'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      // @ 指向 src 目录，与 vite.config.ts 保持一致
      '@': path.join(process.cwd(), './src')
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  test: {
    // jsdom 模拟浏览器环境，支持 DOM API
    environment: 'jsdom',
    // 全局注入 describe/it/expect，无需 import
    globals: true,
    // 测试文件匹配模式
    include: ['src/**/*.{test,spec}.{js,ts}'],
    // 排除 e2e 目录（Playwright 的地盘）
    exclude: ['src/__tests__/e2e/**'],
    // 覆盖率配置
    coverage: {
      provider: 'v8',
      include: ['src/utils/**', 'src/composables/**', 'src/store/**'],
      reporter: ['text', 'lcov']
    }
  }
})
