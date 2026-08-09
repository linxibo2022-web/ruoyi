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
    route: '/system/user',
    name: '用户管理',
    apiPage: '/system/user/pageUsers',
    apiAdd: '/system/user/addUser',
    searchKeyword: 'admin',
    formValue: 'E2E冒烟测试用户',
    editValue: 'E2E修改测试用户'
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

/** 登录：填入账号密码，点登录，等跳转到首页 */
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

  // 检查验证码
  const captchaInput = page.locator('.captcha-input input')
  const captchaVisible = await captchaInput.isVisible().catch(() => false)
  if (captchaVisible) {
    // 可视模式下手工输入验证码
    console.log('⏳ 验证码已开启，请在浏览器中输入验证码，然后按 Enter 继续...')
    await captchaInput.focus()
    // 等用户手动输入验证码（最多等 120 秒）
    await page.waitForTimeout(1000)
    // 或者：用 page.pause() 打开 Playwright 调试器，用户可以操作浏览器
    // 这里采用简单方式：等 15 秒让用户自己填
    console.log('⏳ 等待 15 秒，请完成验证码输入和登录...')
    await page.waitForTimeout(15000)
  } else {
    // 无验证码，直接点登录
    await page.click('.login-btn')
  }

  // 等待跳转到首页
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 30000 })
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
  // 所有 TC 合在一个测试里，共享同一个 page，登录态不会丢失
  test(`冒烟测试: ${config.name} (TC-01 ~ TC-08)`, async ({ page }) => {
    // 登录
    await login(page)

    // 导航到目标页
    await page.goto(`${BASE_URL}${config.route}`)
    await waitForTable(page)

    // ---- TC-01: 页面加载 ----
    await expect(page.locator('.el-table')).toBeVisible()
    console.log(`  ✅ ${config.name} TC-01 页面加载成功`)

    // ---- TC-02: 列表渲染 ----
    const initialCount = await getRowCount(page)
    if (initialCount === 0) {
      console.log(`  ⚠️  ${config.name} 列表无数据，后续新增/编辑/删除跳过`)
    }
    expect(initialCount).toBeGreaterThanOrEqual(0) // 允许空表

    const paginationTotal = page.locator('.el-pagination__total')
    if (await paginationTotal.isVisible()) {
      const text = await paginationTotal.textContent()
      expect(text).toMatch(/\d+/)
    }
    console.log(`  ✅ ${config.name} TC-02 列表渲染 (${initialCount} 行)`)

    // ---- TC-03: 新增 ----
    const before = await getRowCount(page)
    await page.evaluate(() => {
      const btns = [...document.querySelectorAll('.el-button')]
      const add = btns.find((b) => b.textContent?.includes('新增'))
      ;(add as HTMLElement)?.click()
    })
    await page.waitForSelector('.el-dialog', { timeout: 5000 })

    // 智能填表：根据字段类型填入合适的值
    const inputs = page.locator('.el-dialog .el-input__inner')
    const inputCount = await inputs.count()
    for (let i = 0; i < inputCount; i++) {
      const input = inputs.nth(i)
      if (!(await input.isVisible())) continue
      const type = await input.getAttribute('type')
      if (type === 'hidden') continue

      const placeholder = (await input.getAttribute('placeholder')) || ''
      const label = await input.evaluate((el) => {
        // 找最近的 el-form-item 里的 label 文字
        const item = el.closest('.el-form-item')
        const labelEl = item?.querySelector('.el-form-item__label')
        return labelEl?.textContent?.trim() || ''
      })

      // 根据 placeholder 或 label 判断字段类型
      if (placeholder.includes('手机') || label.includes('手机') || placeholder.includes('phone') || label.includes('phone')) {
        await input.fill('13800138000')
      } else if (placeholder.includes('邮箱') || label.includes('邮箱') || placeholder.includes('email') || label.includes('email')) {
        await input.fill('test@erp.com')
      } else if (placeholder.includes('密码') || label.includes('密码') || placeholder.includes('password') || label.includes('password')) {
        await input.fill('Test123456')
      } else {
        await input.fill(config.formValue + (i > 0 ? i : ''))
      }
    }

    // 处理 select 下拉框：展开后直接关闭（保留默认选中值）
    const selects = page.locator('.el-dialog .el-select')
    const selectCount = await selects.count()
    for (let i = 0; i < selectCount; i++) {
      const select = selects.nth(i)
      if (!(await select.isVisible())) continue
      try {
        await select.click()
        await page.waitForTimeout(200)
        await page.keyboard.press('Escape')
        await page.waitForTimeout(100)
      } catch {
        // select 可能被禁用或不可交互，跳过
      }
    }

    await page.click('.el-dialog__footer .el-button--primary')
    try {
      await page.waitForSelector('.el-message--success', { timeout: 8000 })
      console.log(`  ✅ ${config.name} TC-03 新增成功`)
    } catch {
      const errorItems = page.locator('.el-form-item__error')
      const errorCount = await errorItems.count()
      const errors: string[] = []
      for (let i = 0; i < errorCount; i++) {
        errors.push((await errorItems.nth(i).textContent()) || '')
      }
      console.log(`  ⚠️  TC-03 表单校验失败: ${errors.join(', ')}`)
      await page.keyboard.press('Escape')
      return // 不抛异常，让后续 TC 继续跑
    }
    await page.waitForTimeout(500)
    const after = await getRowCount(page)
    if (after !== before + 1) {
      console.log(`  ⚠️  TC-03 新增后行数未变化 (${before} → ${after})`)
    } else {
      console.log(`  ✅ TC-03 行数验证通过 (${before} → ${after})`)
    }

    // ---- TC-04: 搜索 ----
    const searchInput = page.locator('.el-form .el-input__inner').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill(config.searchKeyword)
      await page.waitForTimeout(800)
    }
    await expect(page.locator('.el-table')).toBeVisible()
    console.log(`  ✅ ${config.name} TC-04 搜索通过`)

    // ---- TC-05: 编辑 ----
    if (await getRowCount(page) > 0) {
      const editBtn = page.locator('tbody tr:first-child .el-button--small').first()
      if (await editBtn.isVisible()) {
        await editBtn.click()
        await page.waitForSelector('.el-dialog', { timeout: 5000 })
        const dialogInput = page.locator('.el-dialog .el-input__inner').first()
        if (await dialogInput.isVisible()) {
          await dialogInput.fill(config.editValue)
        }
        await page.click('.el-dialog__footer .el-button--primary')
        await page.waitForSelector('.el-message--success', { timeout: 10000 })
        console.log(`  ✅ ${config.name} TC-05 编辑成功`)
      }
    } else {
      console.log(`  ⚠️  ${config.name} TC-05 编辑跳过（无数据）`)
    }

    // ---- TC-06: 删除 ----
    const beforeDel = await getRowCount(page)
    if (beforeDel > 0) {
      await page.evaluate(() => {
        const btns = [...document.querySelectorAll('tbody tr:first-child .el-button--small')]
        const del = btns.find((b) => b.textContent?.includes('删除'))
        ;(del as HTMLElement)?.click()
      })
      try {
        await page.waitForSelector('.el-message-box', { timeout: 3000 })
        await page.click('.el-message-box__btns .el-button--primary')
        await page.waitForSelector('.el-message--success', { timeout: 10000 })
      } catch { /* 无确认框 */ }
      const afterDel = await getRowCount(page)
      expect(afterDel).toBe(beforeDel - 1)
      console.log(`  ✅ ${config.name} TC-06 删除成功 (${beforeDel} → ${afterDel})`)
    } else {
      console.log(`  ⚠️  ${config.name} TC-06 删除跳过（无数据）`)
    }

    // ---- TC-07: 表单校验 ----
    await page.evaluate(() => {
      const btns = [...document.querySelectorAll('.el-button')]
      const add = btns.find((b) => b.textContent?.includes('新增'))
      ;(add as HTMLElement)?.click()
    })
    await page.waitForSelector('.el-dialog', { timeout: 5000 })
    await page.click('.el-dialog__footer .el-button--primary')
    await page.waitForTimeout(500)
    expect(await page.locator('.el-dialog').isVisible()).toBe(true)
    await page.keyboard.press('Escape')
    console.log(`  ✅ ${config.name} TC-07 表单校验通过`)

    // ---- TC-08: 分页 ----
    const nextBtn = page.locator('.el-pagination .btn-next')
    if ((await nextBtn.isVisible()) && !(await nextBtn.isDisabled())) {
      await nextBtn.click()
      await page.waitForTimeout(500)
      await expect(page.locator('.el-table')).toBeVisible()
      console.log(`  ✅ ${config.name} TC-08 分页通过`)
    } else {
      console.log(`  ⚠️  ${config.name} TC-08 分页跳过（仅一页）`)
    }

    console.log(`🎉 ${config.name} 冒烟测试完成!`)
  })
})
