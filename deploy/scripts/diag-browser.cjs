/**
 * 诊断：真实浏览器打开首页，收集控制台错误 / 页面异常 / 失败请求
 */
const { chromium } = require('/home/ai/网安平台项目/frontend/node_modules/playwright-core')

const BASE = process.env.BASE || 'http://127.0.0.1:11001'

;(async () => {
  const browser = await chromium.launch({
    headless: true,
    executablePath: '/home/ai/网安平台项目/.toolchain/pw-browsers/chromium-1140/chrome-linux/chrome',
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu']
  })
  const page = await browser.newPage()

  const logs = []
  page.on('console', m => logs.push(`[console.${m.type()}] ${m.text()}`))
  page.on('pageerror', e => logs.push(`[pageerror] ${e.message}\n${(e.stack||'').split('\n').slice(0,6).join('\n')}`))
  page.on('requestfailed', r => logs.push(`[reqfail] ${r.method()} ${r.url()} -> ${r.failure()?.errorText}`))
  page.on('response', r => { if (r.status() >= 400) logs.push(`[http ${r.status()}] ${r.url()}`) })

  console.log('=== 打开 /login ===')
  const resp = await page.goto(BASE + '/login', { waitUntil: 'networkidle', timeout: 30000 }).catch(e => {
    logs.push('[goto error] ' + e.message); return null
  })
  console.log('HTTP:', resp ? resp.status() : 'N/A')
  await page.waitForTimeout(2500)

  const appHtml = await page.evaluate(() => document.getElementById('app')?.innerHTML || '(no #app)')
  console.log('=== #app innerHTML 长度:', appHtml.length, '===')
  console.log(appHtml.slice(0, 600))

  console.log('\n=== 输入框数量:', await page.locator('input').count())
  console.log('=== 按钮数量:', await page.locator('button').count())

  console.log('\n=== 浏览器日志 ===')
  logs.forEach(l => console.log(l))

  await browser.close()
})().catch(e => { console.error('FATAL', e); process.exit(1) })