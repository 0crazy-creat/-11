import { chromium } from 'playwright-core'
const B='http://127.0.0.1:11001'
const browser = await chromium.launch({ executablePath: process.env.CHROME, args:['--no-sandbox','--disable-dev-shm-usage'] })
const ctx = await browser.newContext()
const page = await ctx.newPage()
const errs=[]; page.on('pageerror', e=>errs.push(String(e).slice(0,160)))

await page.goto(B+'/login', { waitUntil:'networkidle', timeout:30000 })
await page.locator('.el-tabs__item', { hasText:'扫码' }).click()
await page.waitForTimeout(2000)

const providers = [
  { icon:'GitHub', idx:0 },
  { icon:'钉钉', idx:1 },
  { icon:'微信', idx:2 },
]
for (const p of providers) {
  await ctx.clearCookies()
  await page.evaluate(()=>localStorage.clear())
  await page.goto(B+'/login', { waitUntil:'networkidle' })
  await page.locator('.el-tabs__item', { hasText:'扫码' }).click()
  await page.waitForTimeout(1500)
  const btns = page.locator('.third-btns button')
  const n = await btns.count()
  if (p.idx >= n) { console.log(`${p.icon}: 按钮不存在 (共${n}个)`); continue }
  const [resp] = await Promise.all([
    page.waitForResponse(r=>r.url().includes('/api/auth/login-third'), { timeout:15000 }).catch(()=>null),
    btns.nth(p.idx).click()
  ])
  await page.waitForTimeout(2000)
  const tk = await page.evaluate(()=>localStorage.getItem('csp_token'))
  const status = resp ? resp.status() : 'no-response'
  const body = resp ? await resp.json().catch(()=>({})) : {}
  console.log(`${p.icon}: http=${status} code=${body.code} user=${(body.data?.user||{}).username||'-'} token=${tk?'YES':'NO'} url=${page.url().replace(B,'')}`)
  const msg = await page.locator('.el-message').first().textContent().catch(()=>'')
  if (msg) console.log(`    提示: ${msg.trim()}`)
}
console.log('--- errors ---'); errs.slice(-5).forEach(e=>console.log(' ',e))
await browser.close()
