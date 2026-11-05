/**
 * 轻量 Markdown → HTML 渲染器。
 *
 * 背景：题库题面统一以 Markdown 存储（`## 描述`、`**提示**`、`- 列表`、```代码块```），
 * 但选手端此前只做「转义 + 换行」，导致页面上直接裸露 `##`、`**` 等原始标记。
 * 这里补一个零依赖实现，覆盖题面实际用到的语法子集。
 *
 * 安全：**先整体 HTML 转义再解析**，因此题干里的 `<script>` 会被转成文本，
 * 输出到 `v-html` 不会执行；链接仅放行 http/https。
 */

const ESCAPE_MAP = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }

/** HTML 转义，阻断标签注入 */
function escapeHtml(s) {
  return String(s ?? '').replace(/[&<>"']/g, (m) => ESCAPE_MAP[m])
}

/**
 * 行内标记：代码 → 加粗 → 斜体 → 链接。
 * 顺序有讲究：先吃掉 `` `...` ``，避免代码里的 `*` `_` 被当成强调符。
 * 入参必须是**已转义**的文本。
 */
function inline(text) {
  return text
    .replace(/`([^`\n]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*\n]+)\*\*/g, '<strong>$1</strong>')
    .replace(/(^|[^*\w])\*([^*\n]+)\*/g, '$1<em>$2</em>')
    .replace(/\[([^\]\n]+)\]\((https?:\/\/[^\s)]+)\)/g,
      '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
}

/**
 * 把 Markdown 文本渲染为可直接交给 `v-html` 的 HTML 片段。
 *
 * @param {string} md 原始 Markdown
 * @returns {string} 安全的 HTML
 */
export function renderMarkdown(md) {
  const src = escapeHtml(md)
  if (!src.trim()) return ''

  const lines = src.split(/\r?\n/)
  const out = []
  let inCode = false
  /** 当前打开的列表标签：'ul' | 'ol' | null */
  let listType = null

  const closeList = () => {
    if (listType) {
      out.push(`</${listType}>`)
      listType = null
    }
  }

  for (const raw of lines) {
    const line = raw.replace(/\s+$/, '')

    // 围栏代码块：``` 或 ```lang
    if (/^\s*```/.test(line)) {
      if (inCode) {
        out.push('</code></pre>')
        inCode = false
      } else {
        closeList()
        out.push('<pre class="md-pre"><code>')
        inCode = true
      }
      continue
    }
    if (inCode) {
      out.push(`${line}\n`)
      continue
    }

    // 空行：结束当前列表
    if (!line.trim()) {
      closeList()
      continue
    }

    // 标题：题面内的 ## 降到 h4，避免抢过页面主标题
    const heading = line.match(/^(#{1,6})\s+(.*)$/)
    if (heading) {
      closeList()
      const level = Math.min(heading[1].length + 2, 6)
      out.push(`<h${level} class="md-h">${inline(heading[2])}</h${level}>`)
      continue
    }

    // 分隔线
    if (/^\s*(-{3,}|\*{3,}|_{3,})\s*$/.test(line)) {
      closeList()
      out.push('<hr class="md-hr"/>')
      continue
    }

    // 引用（转义后 > 变成 &gt;）
    const quote = line.match(/^\s*&gt;\s?(.*)$/)
    if (quote) {
      closeList()
      out.push(`<blockquote class="md-quote">${inline(quote[1])}</blockquote>`)
      continue
    }

    // 无序列表
    const ul = line.match(/^\s*[-*+]\s+(.*)$/)
    if (ul) {
      if (listType !== 'ul') {
        closeList()
        out.push('<ul class="md-ul">')
        listType = 'ul'
      }
      out.push(`<li>${inline(ul[1])}</li>`)
      continue
    }

    // 有序列表
    const ol = line.match(/^\s*\d+[.)]\s+(.*)$/)
    if (ol) {
      if (listType !== 'ol') {
        closeList()
        out.push('<ol class="md-ol">')
        listType = 'ol'
      }
      out.push(`<li>${inline(ol[1])}</li>`)
      continue
    }

    // 普通段落
    closeList()
    out.push(`<p class="md-p">${inline(line)}</p>`)
  }

  if (inCode) out.push('</code></pre>')
  closeList()
  return out.join('\n')
}

export default renderMarkdown