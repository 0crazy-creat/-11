/**
 * 真实浏览器点击级 E2E：模拟用户输入账号密码并点击登录按钮
 * 目标：验证登录后是否真的跳转到 /console，并抓取所有控制台错误 / 网络请求
 */
const path = require('path')
const { chromium } = require('/home/ai/网安平台项目/frontend/node_modules/playwright-core')

// 默认走 Docker 部署入口（csp-frontend nginx :80）；单端口模式设 BASE=http://127.0.0.1:11001
const BASE = process.env.BASE || 'http://127.0.0.1'
const CHROME = '/home/ai/网安平台项目/.toolchain/pw-browsers/chromium-1140/chrome-linux/chrome'

const sleep = (ms) => new Promise(r => setTimeout(r, ms))

;(async () => {
  const browser = await chromium.launch({
    executablePath: CHROME,
    headless: true,
    args: [
      '--no-sandbox',
      '--disable-gpu',
      '--disable-dev-shm-usage',
      '--disable-setuid-sandbox',
      '--no-first-run'
    ]
  })

  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  const page = await ctx.newPage()

  const consoleMsgs = []
  const pageErrors = []
  const requests = []
  const responses = []

  page.on('console', m => consoleMsgs.push(`[${m.type()}] ${m.text()}`))
  page.on('pageerror', e => pageErrors.push(String(e && e.stack || e)))
  page.on('request', r => {
    const u = r.url()
    if (u.includes('/api/')) requests.push(`${r.method()} ${u.replace(BASE, '')}`)
  })
  page.on('response', async r => {
    const u = r.url()
    if (u.includes('/api/')) {
      let body = ''
      try { body = (await r.text()).slice(0, 300) } catch (e) { body = '<unreadable>' }
      responses.push(`${r.status()} ${r.request().method()} ${u.replace(BASE, '')} :: ${body}`)
    }
  })

  console.log('=== 步骤1：打开登录页 ===')
  const resp = await page.goto(BASE + '/login', { waitUntil: 'networkidle', timeout: 30000 })
  console.log('HTTP:', resp.status())
  console.log('URL:', page.url())

  await page.waitForSelector('input', { timeout: 15000 })
  const inputs = await page.$$('input')
  console.log('输入框数量:', inputs.length)

  console.log('\n=== 步骤2：填写账号密码 ===')
  await page.fill('input[placeholder*="用户名"], input:not([type=password])', 'admin')
  await page.fill('input[type=password]', 'Admin@123')
  const uVal = await page.inputValue('input:not([type=password])')
  const pVal = await page.inputValue('input[type=password]')
  console.log(`已填用户名=${uVal} 密码长度=${pVal.length}`)

  console.log('\n=== 步骤3：点击登录按钮（真实点击）===')
  const btn = await page.$('button:has-text("登录"), button.el-button--primary')
  if (!btn) { console.log('❌ 未找到登录按钮'); await browser.close(); process.exit(1) }
  console.log('按钮文本:', (await btn.innerText()).trim())

  await btn.click()
  console.log('已点击，等待跳转...')

  // 轮询等待 URL 变化
  let jumped = false
  for (let i = 0; i < 40; i++) {
    await sleep(250)
    const cur = page.url()
    if (!cur.includes('/login')) { jumped = true; break }
  }

  console.log('\n=== 步骤4：跳转结果 ===')
  console.log('当前 URL:', page.url())
  console.log('是否离开登录页:', jumped ? '✅ 是' : '❌ 否（仍停留在登录页）')

  if (jumped) {
    await page.waitForLoadState('networkidle', { timeout: 20000 }).catch(() => {})
    await sleep(800)
    const title = await page.title()
    const appLen = await page.evaluate(() => document.querySelector('#app')?.innerHTML?.length || 0)
    const bodyText = await page.evaluate(() => document.body.innerText.slice(0, 600))
    console.log('页面标题:', title)
    console.log('#app 渲染长度:', appLen)
    console.log('--- 页面可见文本（前600字）---')
    console.log(bodyText)

    await page.screenshot({ path: '/tmp/login-after-jump.png', fullPage: false })
    console.log('截图已保存: /tmp/login-after-jump.png')
  }

  // 刷新页面，验证登录态是否保持
  console.log('\n=== 步骤5：刷新页面验证登录态保持 ===')
  await page.reload({ waitUntil: 'networkidle', timeout: 30000 }).catch(() => {})
  await sleep(1200)
  console.log('刷新后 URL:', page.url())
  const stayLogged = !page.url().includes('/login')
  console.log('登录态是否保持:', stayLogged ? '✅ 是' : '❌ 否（被踢回登录页）')

  // 点击侧边菜单若干项，模拟导航
  if (stayLogged) {
    console.log('\n=== 步骤6：点击侧边栏菜单逐项导航 ===')
    const menuItems = await page.$$('.el-menu-item, .el-sub-menu__title')
    console.log('菜单项数量:', menuItems.length)
    const targets = []
    for (const it of menuItems) {
      const t = (await it.innerText().catch(() => '')).trim()
      if (t) targets.push({ el: it, text: t })
    }
    for (let i = 0; i < Math.min(targets.length, 10); i++) {
      const before = page.url()
      try {
        await targets[i].el.click({ timeout: 5000 })
        await sleep(900)
        const after = page.url()
        const len = await page.evaluate(() => document.querySelector('#app')?.innerHTML?.length || 0)
        console.log(`  [${i + 1}] 点击「${targets[i].text}」 -> ${after.replace(BASE, '')} | 渲染长度=${len}`)
        if (after.includes('/login')) console.log('      ⚠️ 被踢回登录页！')
      } catch (e) {
        console.log(`  [${i + 1}] 点击「${targets[i].text}」失败: ${e.message.slice(0, 120)}`)
      }
    }
  }

  console.log('\n=== 网络请求（/api/）===')
  requests.forEach(r => console.log('  ', r))

  console.log('\n=== 网络响应（/api/）===')
  responses.forEach(r => console.log('  ', r))

  console.log('\n=== 页面异常 ===')
  if (pageErrors.length === 0) console.log('  无')
  pageErrors.forEach(e => console.log('  ❌', e.split('\n')[0]))

  console.log('\n=== 控制台消息 ===')
  if (consoleMsgs.length === 0) console.log('  无')
  consoleMsgs.slice(0, 40).forEach(m => console.log('  ', m.slice(0, 300)))

  await browser.close()
  console.log('\n=== 测试结束 ===')
})().catch(e => {
  console.error('FATAL:', e)
  process.exit(1)
})