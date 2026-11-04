const B='http://127.0.0.1:11001'
;(async()=>{
  // 1) 校验线上产物包含关键修复
  const idxHtml = await (await fetch(B+'/')).text()
  const idxJs = idxHtml.match(/assets\/index-[A-Za-z0-9_-]+\.js/)[0]
  const idxSrc = await (await fetch(B+'/'+idxJs)).text()
  const loginJs = idxSrc.match(/Login-[A-Za-z0-9_-]+\.js/)[0]
  const src = await (await fetch(B+'/assets/'+loginJs)).text()
  console.log('线上 chunk:', loginJs, src.length, 'bytes')
  const checks = {
    'WAITING 不遮挡': /WAITING/.test(src) && /qr-mask/.test(src),
    'canvas 重试':    /setTimeout/.test(src),
    '二维码库 toCanvas': /toCanvas/.test(src),
    '模拟扫码按钮':    /模拟扫码/.test(src),
  }
  for (const [k,v] of Object.entries(checks)) console.log('  '+k.padEnd(18), v?'✅':'❌')

  // 2) 用真实 qrcode 库在 node 端生成 PNG，验证生成器可用
  const QRCode = require('qrcode')
  const dataUrl = await QRCode.toDataURL('https://example.com/scan-test', {width:180, margin:1})
  console.log('\nqrcode 库生成 PNG:', dataUrl.slice(0,30)+'...', '长度', dataUrl.length)
  console.log('  >>> 二维码生成能力', dataUrl.startsWith('data:image/png')?'✅ 正常':'❌ 异常')

  // 3) 后端扫码接口链路
  const j = async (m,p,b)=>{const r=await fetch(B+p,{method:m,headers:{'Content-Type':'application/json'},body:b?JSON.stringify(b):undefined});return r.json()}
  const qr = await j('POST','/api/auth/wechat/qrcode')
  console.log('\n后端 qrcode 接口: code=%s mode=%s state长度=%s', qr.code, qr.data.mode, qr.data.state.length)
  await j('POST','/api/auth/wechat/callback',{state:qr.data.state,code:'verify'})
  const st = await j('GET','/api/auth/wechat/status?state='+qr.data.state)
  console.log('模拟扫码后状态:', st.data.status)
  const ex = await j('POST','/api/auth/wechat/exchange',{state:qr.data.state})
  console.log('兑换令牌:', ex.code, ex.data?.accessToken?'token=有':'token=无')
})()
