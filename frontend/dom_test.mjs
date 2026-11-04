import { JSDOM } from 'jsdom'
import fs from 'fs'

const B = 'http://127.0.0.1:11001'
// 1) 拉取真实部署产物
const idxHtml = await (await fetch(B + '/')).text()
const idxJs = idxHtml.match(/assets\/index-[A-Za-z0-9_-]+\.js/)[0]
const idxSrc = await (await fetch(B + '/' + idxJs)).text()
const loginJs = idxSrc.match(/Login-[A-Za-z0-9_-]+\.js/)[0]
const loginSrc = await (await fetch(B + '/assets/' + loginJs)).text()

console.log('部署产物:', idxJs, '/', loginJs, `(${loginSrc.length} bytes)`)

// 2) 在 login chunk 中定位关键调用（压缩后仍保留的 API 路径片段）
const probes = {
  '扫码状态轮询': /wechat\/status|wechatStatus/,
  '扫码回调':     /wechat\/callback|wechatCallback/,
  '扫码换token':  /wechat\/exchange|wechatExchange/,
  '二维码生成':   /toCanvas/,
  '第三方登录':   /login-third|loginByThird/,
  '手机号验证码': /sms-code|sendSmsCode/,
  '模拟扫码按钮': /模拟扫码/,
  '进入平台按钮': /进入平台/,
}
console.log('\n--- 登录页 chunk 静态特征 ---')
for (const [k, re] of Object.entries(probes)) {
  console.log(`  ${k.padEnd(12)} ${re.test(loginSrc) ? '✅' : '❌'}`)
}

// 3) jsdom 渲染 index.html，确认 SPA 能挂载
const dom = new JSDOM(idxHtml, {
  url: B + '/login',
  runScripts: 'outside-only',
  pretendToBeVisual: true,
  resources: 'usable',
})
dom.window.HTMLCanvasElement.prototype.getContext = function () {
  return { getImageData: () => ({ data: new Uint8ClampedArray(4) }),
           fillRect(){}, clearRect(){}, drawImage(){}, putImageData(){},
           createImageData: () => ({ data: new Uint8ClampedArray(4) }) }
}
console.log('\n--- jsdom 挂载 ---')
console.log('  document.title:', dom.window.document.title || '(空)')
console.log('  #app 存在:', !!dom.window.document.querySelector('#app'))
console.log('  script 标签数:', dom.window.document.querySelectorAll('script').length)

// 4) 后端接口在本轮的真实响应（逐项）
const j = async (m, p, b) => {
  const r = await fetch(B + p, { method: m, headers: {'Content-Type':'application/json'}, body: b?JSON.stringify(b):undefined })
  return { status: r.status, body: await r.json().catch(()=>({})) }
}
console.log('\n--- 后端接口实测（登录页三个入口）---')
// 扫码
let r = await j('POST', '/api/auth/wechat/qrcode')
const state = r.body?.data?.state
console.log('  扫码-生成二维码   code=%s mode=%s', r.body.code, r.body?.data?.mode)
await j('POST', '/api/auth/wechat/callback', { state, code: 'dom_test' })
r = await j('GET', '/api/auth/wechat/status?state=' + state)
console.log('  扫码-模拟后状态   status=%s', r.body?.data?.status)
r = await j('POST', '/api/auth/wechat/exchange', { state })
console.log('  扫码-换取令牌     code=%s token=%s', r.body.code, r.body?.data?.accessToken ? '有' : '无')
// 第三方 x3
for (const p of ['github','dingtalk','wechat']) {
  r = await j('POST', '/api/auth/login-third', { provider: p, thirdToken: 'dom-' + p })
  console.log('  三方-%-9s code=%s token=%s user=%s', p, r.body.code, r.body?.data?.accessToken ? '有':'无', r.body?.data?.user?.username)
}
// 手机号
r = await j('POST', '/api/auth/sms-code?phone=13900139000')
const code = r.body?.data?.code
console.log('  手机号-发码      code=%s code值=%s', r.body.code, code)
r = await j('POST', '/api/auth/login-phone', { phone: '13900139000', smsCode: code })
console.log('  手机号-登录      code=%s token=%s', r.body.code, r.body?.data?.accessToken ? '有':'无')
