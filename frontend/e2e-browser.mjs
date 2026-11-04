// 真实浏览器端到端点击测试：登录 → 跳转 → 逐个菜单页面访问
import { chromium } from 'playwright-core'

const BASE = process.env.BASE || 'http://localhost:11001'
const results = []
let pass = 0, fail = 0

const ok = (n, extra = '') => { pass++; results.push(`  ✅ ${n} ${extra}`) }
const bad = (n, extra = '') => { fail++; results.push(`  ❌ ${n} ${extra}`) }

const browser = await chromium.launch({
  executablePath: process.env.CHROME_PATH,
  args: ['--no-sandbox', '--disable-dev-shm-usage']
})
const page = await browser.newPage()
const consoleErrors = []
page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()) })
page.on('pageerror', e => consoleErrors.push('pageerror: ' + e.message))

try {
  // 1. 打开首页
  await page.goto(BASE + '/', { waitUntil: 'networkidle', timeout: 30000 })
  const title = await page.title()
  title.includes('CSP') ? ok('首页加载', `title="${title}"`) : bad('首页加载', `title="${title}"`)

  // 2. 未登录访问 /console 应被守卫重定向到 /login
  await page.goto(BASE + '/console', { waitUntil: 'networkidle' })
  const urlAfterGuard = page.url()
  urlAfterGuard.includes('/login') ? ok('未登录守卫重定向', urlAfterGuard) : bad('未登录守卫', urlAfterGuard)

  // 3. 填写登录表单并点击登录
  await page.waitForSelector('input', { timeout: 10000 })
  const inputs = page.locator('input')
  const cnt = await inputs.count()
  await inputs.nth(0).fill('admin')
  await inputs.nth(1).fill('Admin@123')
  ok('表单填充', `inputs=${cnt}`)

  // 点击登录按钮
  const btn = page.locator('button:has-text("登录"), button:has-text("登 录"), button[type="submit"]').first()
  await btn.click()
  await page.waitForTimeout(3000)

  const urlAfterLogin = page.url()
  const token = await page.evaluate(() => localStorage.getItem('csp_token'))
  urlAfterLogin.includes('/console') ? ok('登录后跳转控制台', urlAfterLogin) : bad('登录后跳转', urlAfterLogin)
  token ? ok('token 已写入 localStorage', `len=${token.length}`) : bad('token 未写入')

  // 4. 登录态保持：刷新页面后仍在控制台
  await page.reload({ waitUntil: 'networkidle' })
  await page.waitForTimeout(1500)
  page.url().includes('/console') ? ok('刷新后登录态保持', page.url()) : bad('刷新后掉登录', page.url())

  // 5. 逐个访问业务路由
  const routes = [
    ['/console', '控制台'],
    ['/contests', '赛事列表'],
    ['/questions', '题库管理'],
    ['/users', '用户管理'],
    ['/roles', '角色权限'],
    ['/audit', '审计日志'],
    ['/announcements', '通知公告'],
    ['/rank', '排行榜'],
    ['/situation', '3D态势大屏'],
    ['/awd', 'AWD大屏'],
  ]
  for (const [path, name] of routes) {
    await page.goto(BASE + path, { waitUntil: 'networkidle' }).catch(() => {})
    await page.waitForTimeout(1200)
    const cur = page.url()
    const bodyText = await page.locator('body').innerText().catch(() => '')
    // 掉登录判定：被踢回 /login
    if (cur.includes('/login')) {
      bad(`${name} (${path}) 掉登录`, cur)
    } else if (bodyText.trim().length < 20) {
      bad(`${name} (${path}) 页面空白`)
    } else {
      ok(`${name} (${path})`, `内容${bodyText.length}字`)
    }
  }

  // 6. 登出
  await page.evaluate(() => localStorage.clear())
  await page.goto(BASE + '/console', { waitUntil: 'networkidle' })
  page.url().includes('/login') ? ok('清除 token 后被守卫拦截') : bad('清除 token 后未拦截')

} catch (e) {
  bad('测试异常', e.message.slice(0, 200))
} finally {
  await browser.close()
}

console.log('\n════════ 浏览器点击测试结果 ════════')
results.forEach(r => console.log(r))
console.log(`\n通过 ${pass} / 失败 ${fail}`)
if (consoleErrors.length) {
  console.log('\n控制台错误（前5条）:')
  consoleErrors.slice(0, 5).forEach(e => console.log('  ⚠️ ' + e.slice(0, 160)))
}
process.exit(fail > 0 ? 1 : 0)
