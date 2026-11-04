/** WebSocket 连接工具：自动拼接后端地址、JSON 解析、断线重连。 */

function baseUrl() {
  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
  // 开发环境通过 vite 代理 /ws，生产同源
  return `${proto}//${location.host}`
}

export function connectWs(path, onMessage, options = {}) {
  const { reconnect = true, maxRetry = 8 } = options
  let retry = 0
  let ws = null
  let closedByUser = false

  function open() {
    ws = new WebSocket(`${baseUrl()}${path}`)
    ws.onmessage = (evt) => {
      try {
        const data = JSON.parse(evt.data)
        onMessage && onMessage(data)
      } catch (e) {
        onMessage && onMessage({ type: 'RAW', data: evt.data })
      }
    }
    ws.onopen = () => { retry = 0 }
    ws.onclose = () => {
      if (!closedByUser && reconnect && retry < maxRetry) {
        const delay = Math.min(1000 * 2 ** retry, 15000)
        retry++
        setTimeout(open, delay)
      }
    }
    ws.onerror = () => { /* 交给 onclose 处理 */ }
  }

  open()

  // 返回可被上层代理的伪 socket
  return {
    get readyState() { return ws?.readyState },
    set onopen(fn) { if (ws) ws.onopen = () => { fn && fn(); } },
    set onclose(fn) { if (ws) ws.onclose = () => { fn && fn(); } },
    close() {
      closedByUser = true
      ws?.close()
    },
    send(data) {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(typeof data === 'string' ? data : JSON.stringify(data))
      }
    }
  }
}