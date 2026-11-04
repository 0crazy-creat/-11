// 选手端真实点击验证：登录 -> 赛事大厅 -> 进入赛事 -> 看到题目
const { chromium } = require('/home/ai/网安平台项目/frontend/node_modules/playwright-core')

const BASE = 'http://127.0.0.1:11001'
;(async () => {
  const browser = await chromium.launch({
    executablePath: '/home/ai/网安平台项目/.toolchain/pw-browsers/chromium-1140/chrome-linux/chrome',
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu']
  })
  const page = await browser.newPage()
  const errs = []
  page.on('pageerror', e => errs.push('pageerror: ' + e.message))
  page.on('console', m => { if (m.type() === 'error') errs.push('console: ' + m.text()) })

  const step = (n, ok, extra = '') => console.log(`  ${ok ? '✅' : '❌'} ${n} ${extra}`)

  // 1. 登录页
  await page.goto(BASE + '/login', { waitUntil: 'networkidle' })
  step('打开登录页', await page.locator('input').first().isVisible())

  // 2. 用选手账号登录
  const inputs = page.locator('.el-input__inner')
  await inputs.nth(0).fill('player01')
  await inputs.nth(1).fill('Player@123')
  await page.locator('button:has-text("登录")').first().click()
  await page.waitForTimeout(2500)
  const url1 = page.url()
  step('登录后跳转到选手端', url1.includes('/portal'), url1)

  // 3. 赛事大厅有卡片
  await page.waitForTimeout(1200)
  const cards = await page.locator('.card').count()
  step('赛事卡片可见', cards >= 3, `cards=${cards}`)

  // 4. 进入赛事
  if (cards > 0) {
    await page.locator('.card').first().click()
    await page.waitForTimeout(2500)
    const url2 = page.url()
    step('进入赛事答题页', url2.includes('/portal/contest/'), url2)

    // 5. 题目列表非空
    const chs = await page.locator('.ch-item').count()
    step('题目列表可见', chs > 0, `题数=${chs}`)

    // 6. 题面已渲染
    const hasContent = await page.locator('.content').count()
    step('题面已渲染', hasContent > 0)

    // 7. 提交 flag 输入框存在
    step('flag 提交框存在', await page.locator('input[placeholder*="flag"]').count() > 0)
  }

  // 8. 选手访问管理后台应被拦回
  await page.goto(BASE + '/console/users', { waitUntil: 'networkidle' })
  await page.waitForTimeout(1500)
  step('选手访问管理后台被拦截', page.url().includes('/portal'), page.url())

  if (errs.length) {
    console.log('  ⚠ 前端错误:')
    errs.slice(0, 5).forEach(e => console.log('    ' + e))
  }
  await browser.close()
})().catch(e => { console.error('FAILED', e.message); process.exit(1) })
