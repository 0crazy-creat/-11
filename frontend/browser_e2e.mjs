import { chromium } from 'playwright-core'
const B = 'http://127.0.0.1:11001'
const exe = process.env.CHROME || '/usr/bin/chromium'
let browser
try {
  browser = await chromium.launch({ executablePath: exe, args: ['--no-sandbox','--disable-dev-shm-usage'] })
} catch (e) {
  console.log('LAUNCH_FAIL:', e.message.slice(0,120)); process.exit(2)
}
const ctx = await browser.newContext()
const page = await ctx.newPage()
const logs = []
page.on('console', m => logs.push(`[${m.type()}] ${m.text().slice(0,150)}`))
page.on('pageerror', e => logs.push(`[pageerror] ${String(e).slice(0,200)}`))

await page.goto(B + '/login', { waitUntil: 'networkidle', timeout: 30000 })
console.log('TITLE:', await page.title())
const tabs = await page.locator('.el-tabs__item').allTextContents()
console.log('TABS:', JSON.stringify(tabs))

// 切到扫码登录
const qrTab = page.locator('.el-tabs__item', { hasText: '扫码' })
await qrTab.click()
await page.waitForTimeout(2500)

const canvasInfo = await page.evaluate(() => {
  const c = document.querySelector('canvas')
  if (!c) return { exists: false }
  const ctx = c.getContext('2d')
  const d = ctx.getImageData(0, 0, c.width, c.height).data
  let dark = 0
  for (let i = 0; i < d.length; i += 4) if (d[i] < 128) dark++
  return { exists: true, w: c.width, h: c.height, darkPixels: dark, hasContent: dark > 100 }
})
console.log('QR_CANVAS:', JSON.stringify(canvasInfo))

const simBtn = page.locator('button', { hasText: '模拟扫码' })
const simVisible = await simBtn.isVisible().catch(() => false)
console.log('SIM_BTN_VISIBLE:', simVisible)

if (simVisible) {
  await simBtn.click()
  await page.waitForTimeout(2500)
  const stateText = await page.locator('.qr-mask span').textContent().catch(() => '')
  console.log('AFTER_SIM_STATE:', stateText)
  const enterBtn = page.locator('button', { hasText: '进入平台' })
  const enterVisible = await enterBtn.isVisible().catch(() => false)
  console.log('ENTER_BTN_VISIBLE:', enterVisible)
  if (enterVisible) {
    await enterBtn.click()
    await page.waitForTimeout(3500)
    console.log('AFTER_LOGIN_URL:', page.url())
    const tk = await page.evaluate(() => localStorage.getItem('csp_token'))
    console.log('TOKEN_STORED:', tk ? 'YES(len=' + tk.length + ')' : 'NO')
  }
}

// 第三方按钮
const thirdBtns = await page.locator('.third-btns button').count()
console.log('THIRD_BTN_COUNT:', thirdBtns)

console.log('--- console/errors ---')
logs.slice(-12).forEach(l => console.log(' ', l))
await browser.close()
