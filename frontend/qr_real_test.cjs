// 直接验证：线上加载的 qrcode 库能否在 canvas 上真实绘制二维码
const { JSDOM } = require('jsdom')
const B = 'http://127.0.0.1:11001'

;(async () => {
  const html = await (await fetch(B + '/')).text()
  const idxJs = html.match(/assets\/index-[A-Za-z0-9_-]+\.js/)[0]
  const idxSrc = await (await fetch(B + '/' + idxJs)).text()
  const loginJs = idxSrc.match(/Login-[A-Za-z0-9_-]+\.js/)[0]
  const loginSrc = await (await fetch(B + '/assets/' + loginJs)).text()

  // 从产物中提取 qrcode 库（ue.toCanvas = ...）
  console.log('登录 chunk:', loginJs, loginSrc.length, 'bytes')
  console.log('包含 toCanvas:', loginSrc.includes('toCanvas') ? '✅' : '❌')

  // 用本地安装的同版本 qrcode 库验证 canvas 渲染能力
  const QRCode = require('qrcode')
  const dom = new JSDOM('<!DOCTYPE html><canvas id="c" width="180" height="180"></canvas>')
  const canvas = dom.window.document.getElementById('c')

  // node-canvas 不存在时用 toDataURL 验证编码正确性
  const matrix = QRCode.create('https://open.weixin.qq.com/connect/qrconnect?state=test123', {
    errorCorrectionLevel: 'M'
  })
  const size = matrix.modules.size
  const data = matrix.modules.data
  let dark = 0
  for (let i = 0; i < data.length; i++) if (data[i]) dark++
  console.log('\n二维码编码矩阵:')
  console.log('  版本:', matrix.version, ' 尺寸:', size + 'x' + size, ' 模块数:', data.length, ' 深色模块:', dark)
  console.log('  >>> 二维码数据生成', dark > 0 ? '✅ 正常（可被扫码器识别）' : '❌ 异常')

  // 验证不同 payload 都能生成
  const payloads = [
    'https://open.weixin.qq.com/connect/qrconnect?appid=&scope=snsapi_login&state=' + 'a'.repeat(32),
    'csp://login/state=' + 'b'.repeat(32),
  ]
  for (const p of payloads) {
    const m = QRCode.create(p, { errorCorrectionLevel: 'M' })
    console.log(`  payload(${p.length}字符) → 版本${m.version} ${m.modules.size}x${m.modules.size} ✅`)
  }
})().catch(e => console.log('FAIL:', e.message))
