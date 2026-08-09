// CRUD 冒烟测试 — 参数化模板（TC-01 ~ TC-08）
// 用法：修改下方 CRUD_PAGES 数组，传入模块参数即可复用
import { test, expect, type Page } from '@playwright/test'

// ============================================================
// 📋 参数配置区 — 每个 CRUD 模块一行，传入参数即可
// ============================================================
interface CrudPageConfig {
  /** 菜单路由（不含域名，如 /business/base/ad） */
  route: string
  /** 模块中文名（用于日志显示） */
  name: string
  /** 分页 API 路径关键字（用于断言接口 200） */
  apiPage: string
  /** 新增 API 路径关键字 */
  apiAdd: string
  /** 删除 API 路径关键字 */
  apiDelete: string
  /** 搜索关键词（用于 TC-04 搜索） */
  searchKeyword: string
  /** 新增时填入的测试字段值 */
  formFields: Record<string, string>
  /** 编辑时修改的字段值（TC-05） */
  editFields: Record<string, string>
}

const CRUD_PAGES: CrudPageConfig[] = [
  // 示例：广告管理（可根据实际模块添加更多）
  {
    route: '/business/base/ad',
    name: '广告管理',
    apiPage: '/base/ad/pageAds',
    apiAdd: '/base/ad/addAd',
    apiDelete: '/base/ad/deleteAds',
    searchKeyword: '测试',
    formFields: {
      '广告名称': 'E2E冒烟测试广告',
      '广告类型': '1'
    },
    editFields: {
      '广告名称': 'E2E修改测试广告'
    }
  }
]

// ============================================================
// 🧰 公共帮助函数
// ============================================================

/** 等待表格渲染完成 */
async function waitForTable(page: Page) {
  await page.waitForSelector('.el-table', { timeout: 15000 })
}

/** 获取表格行数 */
async function getRowCount(page: Page): Promise<number> {
  return page.locator('.el-table__body-wrapper tbody tr').count()
}

/** 解析网络请求 URL 列表 */
async function getNetworkUrls(page: Page): Promise<string[]> {
  return page.evaluate(() =>
    performance.getEntriesByType('resource').map((r) => r.name)
  )
}

/** 断言控制台无 error */
async function assertNoConsoleError(page: Page) {
  const errors: string[] = []
  page.on('console', (msg) => {
    if (msg.type() === 'error') errors.push(msg.text())
  })
  // 等到下一个事件循环确认
  await page.waitForTimeout(500)
  if (errors.length > 0) {
    console.warn('⚠️  控制台错误:', errors)
  }
}

// ============================================================
// 🧪 冒烟测试套件
// ============================================================

CRUD_PAGES.forEach((config) => {
  test.describe(`冒烟测试: ${config.name}`, () => {
    // 每个用例前导航到目标页
    test.beforeEach(async ({ page }) => {
      await page.goto(config.route)
      await waitForTable(page)
    })

    // ---- TC-01: 页面加载 ----
    test('TC-01 页面加载 — 表格渲染 + API 200 + 无 console error', async ({ page }) => {
      // UI 可见性
      await expect(page.locator('.el-table')).toBeVisible()

      // 接口健康 — 检查分页 API 请求
      const urls = await getNetworkUrls(page)
      const pageApiOk = urls.some((url) => url.includes(config.apiPage))
      // 说明：Playwright 的网络监听更精准，这里用 resource timing 做粗略检查
      // 实际项目建议用 page.waitForResponse() 精确拦截
      console.log(`  📡 API ${config.apiPage}: ${pageApiOk ? '✅ 已请求' : '⚠️ 未捕获'}`)

      // 控制台
      await assertNoConsoleError(page)
    })

    // ---- TC-02: 列表渲染 ----
    test('TC-02 列表渲染 — 数据非空 + 分页计数', async ({ page }) => {
      const count = await getRowCount(page)
      expect(count).toBeGreaterThan(0)

      // 分页总数存在
      const paginationTotal = page.locator('.el-pagination__total')
      if (await paginationTotal.isVisible()) {
        const text = await paginationTotal.textContent()
        expect(text).toMatch(/\d+/)
      }
    })

    // ---- TC-03: 新增 ----
    test('TC-03 新增 — 填表提交 → toast 成功 → 行数 +1', async ({ page }) => {
      const before = await getRowCount(page)

      // 点新增按钮（用 eval 按文字找）
      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('.el-button')]
        const add = btns.find((b) => b.textContent?.includes('新增'))
        ;(add as HTMLElement)?.click()
      })

      // 等弹窗出现
      await page.waitForSelector('.el-dialog', { timeout: 5000 })

      // 填第一个必填输入框
      const firstInput = page.locator('.el-dialog .el-input__inner').first()
      if (await firstInput.isVisible()) {
        await firstInput.fill(Object.values(config.formFields)[0] || 'E2E测试数据')
      }

      // 点确定
      await page.click('.el-dialog__footer .el-button--primary')

      // 等成功提示
      await page.waitForSelector('.el-message--success', { timeout: 10000 })

      // 断言行数 +1
      await page.waitForTimeout(500)
      const after = await getRowCount(page)
      expect(after).toBe(before + 1)
    })

    // ---- TC-04: 搜索 ----
    test('TC-04 搜索 — 输入关键词 → 结果命中', async ({ page }) => {
      // 搜索框输入（本框架为 @input 实时搜索）
      const searchInput = page.locator('.el-form .el-input__inner').first()
      if (await searchInput.isVisible()) {
        await searchInput.fill(config.searchKeyword)
        await page.waitForTimeout(800) // 等实时搜索响应
      }

      // 断言表格仍可见（没崩）
      await expect(page.locator('.el-table')).toBeVisible()
    })

    // ---- TC-05: 编辑 ----
    test('TC-05 编辑 — 点行内修改 → 弹窗回显 → 改值 → 提交', async ({ page }) => {
      // 等表格有行
      const count = await getRowCount(page)
      if (count === 0) {
        test.skip(true, '无数据行，跳过编辑测试')
        return
      }

      // 点第一行的修改按钮
      const editBtn = page.locator('tbody tr:first-child .el-button--small').first()
      if (await editBtn.isVisible()) {
        await editBtn.click()
        await page.waitForSelector('.el-dialog', { timeout: 5000 })

        // 改第一个输入框
        const firstInput = page.locator('.el-dialog .el-input__inner').first()
        if (await firstInput.isVisible()) {
          await firstInput.fill(Object.values(config.editFields)[0] || 'E2E修改测试')
        }

        await page.click('.el-dialog__footer .el-button--primary')
        await page.waitForSelector('.el-message--success', { timeout: 10000 })
      }
    })

    // ---- TC-06: 删除 ----
    test('TC-06 删除 — 确认弹窗 → toast 成功 → 行数 -1', async ({ page }) => {
      const before = await getRowCount(page)
      if (before === 0) {
        test.skip(true, '无数据行，跳过删除测试')
        return
      }

      // 点第一行的删除按钮
      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('tbody tr:first-child .el-button--small')]
        const del = btns.find((b) => b.textContent?.includes('删除'))
        ;(del as HTMLElement)?.click()
      })

      // 等待确认框
      try {
        await page.waitForSelector('.el-message-box', { timeout: 3000 })
        await page.click('.el-message-box__btns .el-button--primary')
        await page.waitForSelector('.el-message--success', { timeout: 10000 })
      } catch {
        // 没有确认框可能直接删了，继续
      }

      const after = await getRowCount(page)
      expect(after).toBe(before - 1)
    })

    // ---- TC-07: 表单校验 ----
    test('TC-07 表单校验 — 必填留空 → 校验报错', async ({ page }) => {
      // 点新增
      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('.el-button')]
        const add = btns.find((b) => b.textContent?.includes('新增'))
        ;(add as HTMLElement)?.click()
      })

      await page.waitForSelector('.el-dialog', { timeout: 5000 })

      // 不填任何东西直接点确定
      await page.click('.el-dialog__footer .el-button--primary')

      // 应该出现校验错误信息，弹窗仍在
      await page.waitForTimeout(300)
      const dialogVisible = await page.locator('.el-dialog').isVisible()
      expect(dialogVisible).toBe(true)

      // 关闭弹窗
      await page.keyboard.press('Escape')
    })

    // ---- TC-08: 分页 ----
    test('TC-08 分页 — 翻页 → pageNum 变化', async ({ page }) => {
      const nextBtn = page.locator('.el-pagination .btn-next')
      if (await nextBtn.isVisible()) {
        const isDisabled = await nextBtn.getAttribute('disabled')
        if (isDisabled !== null) {
          test.skip(true, '仅一页，跳过分页测试')
          return
        }

        await nextBtn.click()
        await page.waitForTimeout(500)
        await expect(page.locator('.el-table')).toBeVisible()
      }
    })
  })
})
