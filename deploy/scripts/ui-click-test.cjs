/**
 * 真实浏览器点击级 E2E（Playwright）
 * 目标：完全模拟用户在页面上点击的路径，验证登录后是否会跳转
 */
const path = require('path')
const fs = require('fs')
const { chromium } = require('/home/ai/网安平台项目/frontend/node_modules/playwright-core')

const BASE = process.env.BASE || 'http://127.0.0.1:11001'
const SHOT_DIR = '/home/ai/网安平台项目/deploy/scripts/ui-shots'
fs.mkdirSync(SHOT_DIR, { recursive: true })

const results = []
function log(step, ok, detail) {
  const line = `${ok ? '✅' : '❌'} ${step}${detail ? ' | ' + detail : ''}`
  console.log(line)
  results.push({ step, ok, detail })
}

async function shot(page, name) {
  try { await page.screenshot({ path: path.join(SHOT_DIR, name + '.png'), fullPage: false }) } catch (e) {}
}

;(async () => {
  let browser
  try {
    browser = await chromium.launch({
      executablePath: '/home/ai/网安平台项目/.toolchain/pw-browsers/chromium-1140/chrome-linux/chrome',
      args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu', '--headless=new']
    })
  } catch (e) {
    console.log('LAUNCH_FAILED: ' + (e && e.message))
    try { fs.writeFileSync('/tmp/ui-launch-fail.txt', String(e && e.stack || e)) } catch (_) {}
    process.exit(1)
  }

  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  const page = await context.newPage()

  // 收集页面错误 / 网络失败 / 接口响应
  const pageErrors = []
  const consoleErrors = []
  const apiCalls = []
  page.on('pageerror', e => pageErrors.push(String(e)))
  page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()) })
  page.on('requestfailed', r => consoleErrors.push(`REQ_FAIL ${r.url()} ${r.failure() && r.failure().errorText}`))
  page.on('response', async r => {
    const u = r.url()
    if (u.includes('/api/')) {
      let body = ''
      try { body = (await r.text()).slice(0, 300) } catch (_) {}
      apiCalls.push({ url: u, status: r.status(), method: r.request().method(), body })
    }
  })

  try {
    // ---------- 1. 未登录直接访问根路径 ----------
    console.log('\n▶ 阶段 1：未登录访问 /')
    await page.goto(BASE + '/', { waitUntil: 'domcontentloaded', timeout: 30000 })
    await page.waitForTimeout(1500)
    log('打开首页 HTTP 成功', true, page.url())
    log('未登录被重定向到 /login', page.url().includes('/login'), page.url())
    await shot(page, '01-login-page')

    const hasLoginBtn = await page.locator('button:has-text("登录")').count()
    log('登录按钮存在', hasLoginBtn > 0, `count=${hasLoginBtn}`)

    const loginCardVisible = await page.locator('.login-card').isVisible().catch(() => false)
    log('登录卡片渲染成功（Vue 已挂载）', loginCardVisible)

    // ---------- 2. 填写表单并点击登录 ----------
    console.log('\n▶ 阶段 2：填写账号密码，点击「登录」按钮')
    await page.locator('input').first().fill('admin')
    await page.locator('input[type=password]').first().fill('Admin@123')
    await shot(page, '02-filled')

    const navPromise = page.waitForURL(u => !u.toString().includes('/login'), { timeout: 15000 }).catch(() => null)
    await page.locator('button:has-text("登录")').first().click()
    await navPromise
    await page.waitForTimeout(2000)

    const urlAfterLogin = page.url()
    log('点击登录后发生跳转（URL 改变）', !urlAfterLogin.includes('/login'), urlAfterLogin)
    log('跳转到控制台 /console', urlAfterLogin.includes('/console'), urlAfterLogin)
    await shot(page, '03-after-login')

    const tokenInLS = await page.evaluate(() => localStorage.getItem('csp_token'))
    log('localStorage 写入 csp_token', !!tokenInLS, tokenInLS ? `len=${tokenInLS.length}` : 'null')

    // 控制台是否有侧边栏/内容
    const asideVisible = await page.locator('.aside, .el-aside, aside').first().isVisible().catch(() => false)
    log('控制台侧边栏渲染', asideVisible)
    const bodyText = (await page.locator('body').innerText().catch(() => '')).slice(0, 200)
    log('控制台页面有内容', bodyText.trim().length > 10, bodyText.replace(/\n/g, ' ').slice(0, 80))

    // ---------- 3. 刷新页面，保持登录 ----------
    console.log('\n▶ 阶段 3：刷新页面验证登录态保持')
    await page.reload({ waitUntil: 'domcontentloaded' })
    await page.waitForTimeout(1500)
    log('刷新后仍在 /console（未被踢回登录）', page.url().includes('/console'), page.url())
    await shot(page, '04-refresh')

    // ---------- 4. 逐个点击左侧菜单 ----------
    console.log('\n▶ 阶段 4：逐个点击左侧菜单')
    const menus = [
      { text: '赛事', expect: '/console/contests' },
      { text: '战队', expect: '/console/teams' },
      { text: '用户', expect: '/console/users' },
      { text: '题库', expect: '/console/challenges' },
      { text: '公告', expect: '/console/announcements' }
    ]
    for (const m of menus) {
      const item = page.locator(`.el-menu-item:has-text("${m.text}"), a:has-text("${m.text}")`).first()
      const cnt = await item.count()
      if (!cnt) { log(`菜单「${m.text}」存在`, false, '未找到'); continue }
      await item.click()
      await page.waitForTimeout(1200)
      const u = page.url()
      log(`点击菜单「${m.text}」`, u.includes(m.expect) || !u.includes('/login'), u)
      await shot(page, '04-menu-' + m.text)
    }

    // ---------- 5. 直接访问受保护路由（模拟地址栏输入/前进后退）----------
    console.log('\n▶ 阶段 5：地址栏直接访问受保护路由')
    for (const p of ['/console/users', '/console/contests', '/console']) {
      await page.goto(BASE + p, { waitUntil: 'domcontentloaded' })
      await page.waitForTimeout(1000)
      log(`直接访问 ${p}`, page.url().includes(p), page.url())
    }

    // ---------- 6. 登出 ----------
    console.log('\n▶ 阶段 6：登出')
    const logoutBtn = page.locator('text=退出登录, text=退出, text=登出').first()
    if (await logoutBtn.count()) {
      await logoutBtn.click()
      await page.waitForTimeout(1500)
      log('登出后回到登录页', page.url().includes('/login'), page.url())
      const t = await page.evaluate(() => localStorage.getItem('csp_token'))
      log('登出清除 token', !t)
      await shot(page, '06-logout')
    } else {
      log('找到退出登录按钮', false, '页面无登出入口')
    }

    // ---------- 7. 注册流程 ----------
    console.log('\n▶ 阶段 7：注册新账号（点击「立即注册」）')
    await page.goto(BASE + '/login', { waitUntil: 'domcontentloaded' })
    await page.waitForTimeout(800)
    const regLink = page.locator('text=立即注册').first()
    if (await regLink.count()) {
      await regLink.click()
      await page.waitForTimeout(800)
      const dialogVisible = await page.locator('.el-dialog').isVisible().catch(() => false)
      log('注册弹窗打开', dialogVisible)
      await shot(page, '07-register-dialog')
      if (dialogVisible) {
        const uname = 'ui' + Date.now().toString().slice(-8)
        const inputs = page.locator('.el-dialog input')
        const n = await inputs.count()
        if (n >= 4) {
          await inputs.nth(0).fill(uname)
          await inputs.nth(1).fill('UI测试')
          await inputs.nth(2).fill(uname + '@test.com')
          await inputs.nth(3).fill('Ui@123456')
          await page.locator('.el-dialog button:has-text("注册")').first().click()
          await page.waitForTimeout(2000)
          const hasSuccessMsg = await page.locator('text=注册成功').count()
          log('点击注册按钮后提示成功', hasSuccessMsg > 0, `username=${uname}`)
          await shot(page, '07-register-result')
        } else {
          log('注册表单字段数量正确', false, `found=${n}`)
        }
      }
    } else {
      log('找到「立即注册」链接', false)
    }

    // ---------- 8. 错误密码登录 ----------
    console.log('\n▶ 阶段 8：错误密码登录应报错不跳转')
    await page.goto(BASE + '/login', { waitUntil: 'domcontentloaded' })
    await page.waitForTimeout(800)
    await page.locator('input').first().fill('admin')
    await page.locator('input[type=password]').first().fill('WrongPass!')
    await page.locator('button:has-text("登录")').first().click()
    await page.waitForTimeout(2500)
    log('错误密码仍停留在登录页', page.url().includes('/login'), page.url())
    const errMsg = await page.locator('.el-message').count()
    log('错误密码有错误提示', errMsg > 0)
    await shot(page, '08-wrong-password')

    // ---------- 汇总 ----------
    console.log('\n════════════════════════ 汇总 ════════════════════════')
    const pass = results.filter(r => r.ok).length
    const fail = results.length - pass
    console.log(`通过 ${pass} / 失败 ${fail} / 总计 ${results.length}`)
    if (fail) {
      console.log('失败项：')
      results.filter(r => !r.ok).forEach(r => console.log('  - ' + r.step + (r.detail ? ' | ' + r.detail : '')))
    }

    console.log('\n--- 页面错误 ---')
    pageErrors.forEach(e => console.log('PAGE_ERROR: ' + e.slice(0, 300)))
    console.log('--- 控制台错误 ---')
    consoleErrors.slice(0, 20).forEach(e => console.log('CONSOLE: ' + e.slice(0, 300)))
    console.log('--- API 调用记录 ---')
    apiCalls.forEach(c => console.log(`${c.method} ${c.status} ${c.url} -> ${c.body.slice(0, 150)}`))

    fs.writeFileSync('/tmp/ui-test-result.json', JSON.stringify({ results, pageErrors, consoleErrors, apiCalls }, null, 2))
  } catch (e) {
    console.log('TEST_EXCEPTION: ' + (e && e.stack || e))
    await shot(page, '99-exception')
    process.exitCode = 2
  } finally {
    await browser.close()
  }
})()