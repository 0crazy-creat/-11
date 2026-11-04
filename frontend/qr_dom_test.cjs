const { JSDOM } = require('jsdom')
const B = 'http://127.0.0.1:11001'

;(async () => {
  const html = await (await fetch(B + '/')).text()
  const dom = new JSDOM(html, {
    url: B + '/login',
    runScripts: 'dangerously',
    pretendToBeVisual: true,
    resources: 'usable',
  })
  const w = dom.window

  // canvas 打桩：记录绘制调用
  let drawCalls = 0, lastCtx = null
  w.HTMLCanvasElement.prototype.getContext = function () {
    if (!lastCtx) {
      drawCalls = 0
      lastCtx = new Proxy({ canvas: this }, {
        get(t, k) {
          if (k === 'fillRect' || k === 'putImageData' || k === 'drawImage') {
            return (...a) => { drawCalls++; return undefined }
          }
          if (k === 'getImageData') return () => ({ data: new Uint8ClampedArray(4) })
          if (k === 'createImageData') return () => ({ data: new Uint8ClampedArray(4) })
          if (k in t) return t[k]
          return () => {}
        },
        set(t, k, v) { t[k] = v; return true }
      })
    }
    return lastCtx
  }
  w.HTMLCanvasElement.prototype.toDataURL = () => 'data:image/png;base64,iVBORw0KGgo='

  await new Promise(r => setTimeout(r, 6000))

  const doc = w.document
  console.log('=== 登录页 DOM ===')
  console.log('  #app 子节点:', doc.querySelector('#app')?.children.length || 0)
  const tabs = [...doc.querySelectorAll('.el-tabs__item')].map(e => e.textContent.trim())
  console.log('  Tabs:', JSON.stringify(tabs))

  // 切到扫码 Tab
  const qrTab = [...doc.querySelectorAll('.el-tabs__item')].find(e => e.textContent.includes('扫码'))
  if (!qrTab) { console.log('❌ 未找到扫码 Tab'); return }
  qrTab.dispatchEvent(new w.MouseEvent('click', { bubbles: true }))
  console.log('  → 已点击「扫码登录」')
  await new Promise(r => setTimeout(r, 4000))

  const canvas = doc.querySelector('canvas')
  console.log('\n=== 扫码页渲染结果 ===')
  console.log('  canvas 存在:', !!canvas, canvas ? `(${canvas.width}x${canvas.height})` : '')
  console.log('  canvas 绘制调用次数:', drawCalls, drawCalls > 0 ? '✅ 已绘制二维码' : '❌ 未绘制')

  const mask = doc.querySelector('.qr-mask')
  console.log('  遮罩层:', mask ? `显示（${mask.textContent.trim()}）` : '未渲染 ✅ 不遮挡')

  const simBtn = [...doc.querySelectorAll('button')].find(b => b.textContent.includes('模拟扫码'))
  console.log('  「模拟扫码」按钮:', simBtn ? '✅ 可见' : '❌ 不可见')

  const tip = doc.querySelector('.qr-tip')
  console.log('  提示文案:', tip?.textContent.trim() || '-')
})().catch(e => console.log('FAIL:', e.message.slice(0, 300)))
