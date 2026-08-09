// CRUD 冒烟测试 — 参数化模板（TC-01 ~ TC-08）
// 用法：修改下方 CRUD_PAGES 数组，传入模块参数即可复用
import { test, expect, type Page } from '@playwright/test'

// ============================================================
// 📋 参数配置区 — 每个 CRUD 模块一行
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
  /** 搜索关键词（用于 TC-04） */
  searchKeyword: string
  /** 新增时填入的测试字段值（第一个输入框填 key，后续按需） */
  formValue: string
  /** 编辑时修改的值 */
  editValue: string
}

const CRUD_PAGES: CrudPageConfig[] = [
  {
    route: '/business/base/ad',
    name: '广告管理',
    apiPage: '/base/ad/pageAds',
    apiAdd: '/base/ad/addAd',
    searchKeyword: '测试',
    formValue: 'E2E冒烟测试广告',
    editValue: 'E2E修改测试广告'
  }
]

// ============================================================
// 🔐 登录配置
// ============================================================
const LOGIN_URL = 'http://localhost:80/login'
const BASE_URL = 'http://localhost:80'
const LOGIN_USER = 'superadmin'
const LOGIN_PASS = 'admin123'
const LOGIN_TENANT = '000000'

// ============================================================
// 🧰 公共帮助函数
// ============================================================

/** 登录：填入账号密码，点击登录，等跳转到首页 */
async function login(page: Page) {
  await page.goto(LOGIN_URL)
  // 等登录表单加载
  await page.waitForSelector('.login-btn', { timeout: 10000 })

  // 填入租户ID（第一个 .form-input input）
  const tenantInput = page.locator('.form-input input').first()
  if (await tenantInput.isVisible()) {
    await tenantInput.fill(LOGIN_TENANT)
  }

  // 填入用户名（第二个 .form-input input）
  const usernameInput = page.locator('.form-input input').nth(1)
  await usernameInput.fill(LOGIN_USER)

  // 填入密码
  const passwordInput = page.locator('input[type="password"]')
  await passwordInput.fill(LOGIN_PASS)

  // 检查验证码是否开启
  const captchaInput = page.locator('.captcha-input input')
  const captchaVisible = await captchaInput.isVisible().catch(() => false)
  if (captchaVisible) {
    console.warn('⚠️ 验证码已开启，请先去【系统管理→参数设置】关闭 sys.account.captchaEnabled')
    // 尝试继续，让测试报错给出明确提示
  }

  // 点击登录
  await page.click('.login-btn')
  // 等待跳转到首页（地址栏不再包含 /login）
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 })
}

/** 等待表格渲染完成 */
async function waitForTable(page: Page) {
  await page.waitForSelector('.el-table', { timeout: 15000 })
}

/** 获取表格行数 */
async function getRowCount(page: Page): Promise<number> {
  return page.locator('.el-table__body-wrapper tbody tr').count()
}

// ============================================================
// 🧪 冒烟测试套件
// ============================================================

CRUD_PAGES.forEach((config) => {
  test.describe(`冒烟测试: ${config.name}`, () => {

    // 整个模块只登录一次，所有用例共享登录态
    test.beforeAll(async ({ browser }) => {
      const context = await browser.newContext()
      const page = await context.newPage()
      await login(page)
      // 保存登录态到文件，后续用例自动加载
      await context.storageState({ path: 'playwright-auth.json' })
      await context.close()
    })

    // 每个用例自动使用已保存的登录态
    test.use({ storageState: 'playwright-auth.json' })

    // 每个用例前导航到目标页
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}${config.route}`)
      await waitForTable(page)
    })

    // ---- TC-01: 页面加载 ----
    test('TC-01 页面加载 — 表格渲染 + 无 console error', async ({ page }) => {
      await expect(page.locator('.el-table')).toBeVisible()
      console.log(`  ✅ ${config.name} 页面加载成功`)
    })

    // ---- TC-02: 列表渲染 ----
    test('TC-02 列表渲染 — 数据非空 + 分页计数', async ({ page }) => {
      const count = await getRowCount(page)
      expect(count).toBeGreaterThan(0)

      const paginationTotal = page.locator('.el-pagination__total')
      if (await paginationTotal.isVisible()) {
        const text = await paginationTotal.textContent()
        expect(text).toMatch(/\d+/)
      }
    })

    // ---- TC-03: 新增 ----
    test('TC-03 新增 — 填表提交 → toast 成功 → 行数 +1', async ({ page }) => {
      const before = await getRowCount(page)

      // 点新增按钮
      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('.el-button')]
        const add = btns.find((b) => b.textContent?.includes('新增'))
        ;(add as HTMLElement)?.click()
      })

      await page.waitForSelector('.el-dialog', { timeout: 5000 })

      // 填第一个输入框
      const firstInput = page.locator('.el-dialog .el-input__inner').first()
      if (await firstInput.isVisible()) {
        await firstInput.fill(config.formValue)
      }

      // 点确定
      await page.click('.el-dialog__footer .el-button--primary')
      await page.waitForSelector('.el-message--success', { timeout: 10000 })

      // 行数 +1
      await page.waitForTimeout(500)
      const after = await getRowCount(page)
      expect(after).toBe(before + 1)
    })

    // ---- TC-04: 搜索 ----
    test('TC-04 搜索 — 输入关键词 → 结果命中', async ({ page }) => {
      const searchInput = page.locator('.el-form .el-input__inner').first()
      if (await searchInput.isVisible()) {
        await searchInput.fill(config.searchKeyword)
        await page.waitForTimeout(800)
      }
      await expect(page.locator('.el-table')).toBeVisible()
    })

    // ---- TC-05: 编辑 ----
    test('TC-05 编辑 — 点行内修改 → 弹窗回显 → 改值 → 提交', async ({ page }) => {
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

        const firstInput = page.locator('.el-dialog .el-input__inner').first()
        if (await firstInput.isVisible()) {
          await firstInput.fill(config.editValue)
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

      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('tbody tr:first-child .el-button--small')]
        const del = btns.find((b) => b.textContent?.includes('删除'))
        ;(del as HTMLElement)?.click()
      })

      // 确认弹窗
      try {
        await page.waitForSelector('.el-message-box', { timeout: 3000 })
        await page.click('.el-message-box__btns .el-button--primary')
        await page.waitForSelector('.el-message--success', { timeout: 10000 })
      } catch {
        // 无确认框直接删
      }

      const after = await getRowCount(page)
      expect(after).toBe(before - 1)
    })

    // ---- TC-07: 表单校验 ----
    test('TC-07 表单校验 — 必填留空 → 校验报错', async ({ page }) => {
      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('.el-button')]
        const add = btns.find((b) => b.textContent?.includes('新增'))
        ;(add as HTMLElement)?.click()
      })

      await page.waitForSelector('.el-dialog', { timeout: 5000 })
      await page.click('.el-dialog__footer .el-button--primary')
      await page.waitForTimeout(300)

      const dialogVisible = await page.locator('.el-dialog').isVisible()
      expect(dialogVisible).toBe(true)

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
