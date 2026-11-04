const { JSDOM } = require('jsdom')
const B = 'http://127.0.0.1:11001'
;(async () => {
  const idxHtml = await (await fetch(B + '/')).text()
  const dom = new JSDOM(idxHtml, {
    url: B + '/login', runScripts: 'dangerously', pretendToBeVisual: true, resources: 'usable'
  })
  const w = dom.window
  // canvas 打桩（二维码库需要）
  w.HTMLCanvasElement.prototype.getContext = function () {
    return new Proxy({}, { get: (t, k) => {
      if (k === 'getImageData') return () => ({ data: new Uint8ClampedArray(4) })
      if (k === 'createImageData') return () => ({ data: new Uint8ClampedArray(4) })
      if (k === 'canvas') return this
      return () => {}
    }})
  }
  w.HTMLCanvasElement.prototype.toDataURL = () => 'data:image/png;base64,iVBORw0KGgo='
  await new Promise(r => setTimeout(r, 6000))
  const doc = w.document
  console.log('=== 真实 DOM 渲染结果（jsdom 加载线上产物）===')
  console.log('  title        :', doc.title || '(空)')
  console.log('  #app 子节点  :', doc.querySelector('#app')?.children.length ?? 0)
  const tabs = [...doc.querySelectorAll('.el-tabs__item')].map(e => e.textContent.trim())
  console.log('  登录 Tab     :', JSON.stringify(tabs))
  console.log('  输入框数量   :', doc.querySelectorAll('input').length)
  console.log('  按钮文本     :', JSON.stringify([...doc.querySelectorAll('button')].map(b=>b.textContent.trim()).filter(Boolean).slice(0,8)))
  console.log('  canvas 数量  :', doc.querySelectorAll('canvas').length)
  const errs = []
  w.addEventListener('error', e => errs.push(String(e.message)))
  console.log('  页面错误     :', errs.length ? errs.slice(0,3) : '无')
})().catch(e => { console.log('RENDER_FAIL:', e.message.slice(0,200)) })
