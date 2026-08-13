// CRUD 冒烟测试 — 参数化模板（TC-01 ~ TC-08）
// 用法：修改下方 CRUD_PAGES 数组，传入模块参数即可复用
// 断言策略：页面加载/列表接口/新增/编辑/删除/表单校验为硬断言（失败即测试失败，杜绝假绿）；
//           环境性场景（空列表、仅一页）允许软跳过并输出日志
import { test, expect, type Page } from '@playwright/test'

// ============================================================
// 📋 参数配置区 — 每个 CRUD 模块一行
// ============================================================
interface CrudPageConfig {
  /** 菜单路由（不含域名，如 /system/user） */
  route: string
  /** 模块中文名（用于日志显示） */
  name: string
  /** 分页 API 路径关键字（用于断言接口 200） */
  apiPage: string
  /** 新增 API 路径关键字 */
  apiAdd: string
  /** 搜索关键词（用于 TC-04，应保证该环境必有匹配数据，如 'admin'） */
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
// 使用相对路径，由 playwright.config.ts 的 baseURL 拼接完整 URL
// 本地默认 http://localhost:80，CI 通过 PLAYWRIGHT_BASE_URL 覆盖
const LOGIN_URL = '/login'
const LOGIN_USER = 'superadmin'
const LOGIN_PASS = 'admin123'
const LOGIN_TENANT = '000000'

// 本次运行唯一用户名 — TC-03 新增该用户，TC-05/06 搜索定位后编辑/删除，
// 闭环自洽，不依赖列表首行（首行可能是受保护的内置用户）
const uniqueUserName = `e2e_${Date.now()}`

// ============================================================
// 🧰 公共帮助函数
// ============================================================

/** 登录：填入账号密码，点登录，等跳转到首页 */
async function login(page: Page) {
  await page.goto(LOGIN_URL)
  // 等登录表单加载
  await expect(page.locator('.login-btn')).toBeVisible({ timeout: 10000 })

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

  // 验证码必须已关闭（CI 启动后已通过 SQL 关闭），开启时无法自动化，直接失败并给出明确指引
  const captchaInput = page.locator('.captcha-input input')
  const captchaVisible = await captchaInput.isVisible().catch(() => false)
  if (captchaVisible) {
    throw new Error('检测到验证码已开启，E2E 无法自动登录。请关闭验证码（sys_config system.security.captcha-enabled=false）后重跑')
  }

  await page.click('.login-btn')
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 30000 })
}

/** 等待表格渲染完成 */
async function waitForTable(page: Page) {
  await expect(page.locator('.el-table')).toBeVisible({ timeout: 15000 })
}

/** 获取表格行数 */
async function getRowCount(page: Page): Promise<number> {
  return page.locator('.el-table__body-wrapper tbody tr').count()
}

/** 等待列表接口返回 200（必须在触发请求的操作之前调用） */
function waitListApi(page: Page, apiKeyword: string) {
  return page.waitForResponse((resp) => resp.url().includes(apiKeyword) && resp.status() === 200, { timeout: 10000 })
}

/** 点击新增按钮并等待弹窗打开 */
async function openAddDialog(page: Page) {
  await page.evaluate(() => {
    const btns = [...document.querySelectorAll('.el-button')]
    const add = btns.find((b) => b.textContent?.includes('新增'))
    ;(add as HTMLElement)?.click()
  })
  await expect(page.locator('.el-dialog')).toBeVisible({ timeout: 5000 })
}

// ============================================================
// 🧪 冒烟测试套件
// ============================================================

CRUD_PAGES.forEach((config) => {
  // 所有 TC 合在一个测试里，共享同一个 page，登录态不会丢失
  test(`冒烟测试: ${config.name} (TC-01 ~ TC-08)`, async ({ page }) => {
    test.setTimeout(60_000) // CI 全局 30s 不够 8 个 TC 串行

    // 登录
    await login(page)

    // ---- TC-01/02: 页面加载 + 列表接口 200 ----
    const listResp = waitListApi(page, config.apiPage)
    await page.goto(config.route)
    await waitForTable(page)
    await listResp // 硬断言：列表接口必须 200，失败说明后端链路异常
    console.log(`  ✅ ${config.name} TC-01/02 页面加载与列表接口正常`)

    // ---- TC-03: 新增（硬断言） ----
    await openAddDialog(page)

    // 智能填表：根据 label 判断字段类型填入合适的值
    const inputs = page.locator('.el-dialog .el-input__inner')
    const inputCount = await inputs.count()
    for (let i = 0; i < inputCount; i++) {
      const input = inputs.nth(i)
      if (!(await input.isVisible())) continue
      const type = await input.getAttribute('type')
      if (type === 'hidden') continue

      const label = await input.evaluate((el) => {
        // 找最近的 el-form-item 里的 label 文字
        const item = el.closest('.el-form-item')
        const labelEl = item?.querySelector('.el-form-item__label')
        return labelEl?.textContent?.trim() || ''
      })

      // 根据 label 判断字段类型
      if (label.includes('手机') || label.includes('phone')) {
        await input.fill('13800138000')
      } else if (label.includes('邮箱') || label.includes('email')) {
        await input.fill('test@erp.com')
      } else if (label.includes('密码') || label.includes('password')) {
        await input.fill('Test123456')
      } else if (label.includes('用户名称') || label.includes('用户名')) {
        // 用户名填本次运行唯一值，供 TC-05/06 搜索定位
        await input.fill(uniqueUserName)
      } else {
        await input.fill(config.formValue + (i > 0 ? i : ''))
      }
    }

    // 处理 select 下拉框：展开后选中第一个选项（满足"角色"等必填字段），Esc 收起
    const selects = page.locator('.el-dialog .el-select')
    const selectCount = await selects.count()
    for (let i = 0; i < selectCount; i++) {
      const select = selects.nth(i)
      if (!(await select.isVisible())) continue
      try {
        await select.click()
        const option = page.locator('.el-select-dropdown:visible .el-select-dropdown__item').first()
        if (await option.isVisible().catch(() => false)) {
          await option.click()
        }
        await page.keyboard.press('Escape')
        await page.waitForTimeout(100)
      } catch {
        // select 可能被禁用或不可交互，跳过（提交失败时由下方硬断言暴露）
      }
    }

    await page.click('.el-dialog__footer .el-button--primary')
    // 硬断言：新增必须出现成功提示。失败时 Playwright 自动截图，报告可见表单校验/后端错误
    await expect(page.locator('.el-message--success').first(), 'TC-03 新增失败').toBeVisible({ timeout: 8000 })
    console.log(`  ✅ ${config.name} TC-03 新增成功 (${uniqueUserName})`)

    // ---- TC-04: 搜索（硬断言） ----
    const searchInput = page.locator('.el-form .el-input__inner').first()
    await expect(searchInput, '未找到搜索输入框').toBeVisible()
    const searchResp = waitListApi(page, config.apiPage)
    await searchInput.fill(config.searchKeyword)
    await searchResp // 硬断言：搜索请求必须 200
    const searchRowCount = await getRowCount(page)
    expect(searchRowCount, `TC-04 搜索"${config.searchKeyword}"无结果`).toBeGreaterThan(0)
    console.log(`  ✅ ${config.name} TC-04 搜索通过 (${searchRowCount} 行)`)

    // ---- TC-05: 编辑（硬断言） ----
    const editSearchResp = waitListApi(page, config.apiPage)
    await searchInput.fill(uniqueUserName)
    await editSearchResp
    const targetRow = page.locator('.el-table__body-wrapper tbody tr', { hasText: uniqueUserName })
    await expect(targetRow.first(), 'TC-05 未找到新增用户').toBeVisible({ timeout: 10000 })
    // 操作列第一个按钮为"修改"（用户管理 row.userId !== 1 才渲染，新增用户必渲染）
    await targetRow.first().locator('.el-button').first().click()
    await expect(page.locator('.el-dialog')).toBeVisible({ timeout: 5000 })
    // 编辑弹窗第一个输入框为昵称（用户名/密码字段编辑时隐藏），改昵称安全
    const dialogInput = page.locator('.el-dialog .el-input__inner').first()
    await dialogInput.fill(config.editValue)
    await page.click('.el-dialog__footer .el-button--primary')
    // 硬断言：编辑必须出现成功提示
    await expect(page.locator('.el-message--success').first(), 'TC-05 编辑失败').toBeVisible({ timeout: 8000 })
    console.log(`  ✅ ${config.name} TC-05 编辑成功`)

    // ---- TC-06: 删除（硬断言） ----
    // 操作列第二个按钮为"删除"
    await targetRow.first().locator('.el-button').nth(1).click()
    await expect(page.locator('.el-message-box')).toBeVisible({ timeout: 3000 })
    await page.click('.el-message-box__btns .el-button--primary')
    // 硬断言：删除后列表刷新，目标行必须消失（搜索词仍为 uniqueUserName）
    await expect(targetRow, 'TC-06 删除后目标行仍存在').toHaveCount(0)
    console.log(`  ✅ ${config.name} TC-06 删除成功`)

    // ---- TC-07: 表单校验（硬断言） ----
    await openAddDialog(page)
    await page.click('.el-dialog__footer .el-button--primary')
    await page.waitForTimeout(300)
    // 硬断言：空表单提交必须被校验拦截，弹窗保持打开
    await expect(page.locator('.el-dialog'), 'TC-07 空表单提交未被拦截').toBeVisible()
    await page.keyboard.press('Escape')
    console.log(`  ✅ ${config.name} TC-07 表单校验通过`)

    // ---- TC-08: 分页 ----
    await searchInput.fill('') // 清空搜索恢复全量列表
    await page.waitForTimeout(500)
    const nextBtn = page.locator('.el-pagination .btn-next')
    if ((await nextBtn.isVisible()) && !(await nextBtn.isDisabled())) {
      await nextBtn.click()
      await waitForTable(page)
      console.log(`  ✅ ${config.name} TC-08 分页通过`)
    } else {
      console.log(`  ⚠️  ${config.name} TC-08 分页跳过（仅一页）`)
    }

    console.log(`🎉 ${config.name} 冒烟测试完成!`)
  })
})
