<template>
  <div class="awd-screen">
    <!-- ======= 顶部 ======= -->
    <header class="screen-header">
      <div class="h-side left">
        <div class="conn-chip" :class="wsConnected ? 'on' : 'off'">
          <span class="chip-dot" />{{ wsConnected ? '直播中' : '离线重连' }}
        </div>
      </div>
      <div class="h-center">
        <div class="h-wing" />
        <div class="h-title-box">
          <div class="h-title">{{ contestName }}</div>
          <div class="h-sub">AWD 攻防对抗 · 实时大屏</div>
        </div>
        <div class="h-wing flip" />
      </div>
      <div class="h-side right">
        <div class="timer" :class="{ urgent: remainSeconds <= 60 && remainSeconds > 0 }">
          <div class="t-label">{{ roundText }}</div>
          <div class="t-value">{{ countdown }}</div>
          <div class="t-label">剩余时间</div>
        </div>
      </div>
    </header>

    <!-- ======= 中满分条 ======= -->
    <section class="score-strip">
      <div class="strip-head">
        <span class="strip-icon">◤</span> 实时积分榜
      </div>
      <div class="strip-items">
        <div
          v-for="(t, i) in topTeams"
          :key="t.teamId"
          class="strip-item"
          :class="'medal-' + (i < 3 ? i + 1 : 'x')"
        >
          <span class="rank-badge">{{ i + 1 }}</span>
          <span class="name">{{ teamName(t.teamId) }}</span>
          <span class="score">{{ fmtScore(t.score) }}</span>
        </div>
      </div>
    </section>

    <!-- ======= 四宫格 ======= -->
    <div class="grid">
      <!-- 攻防态势 -->
      <div class="panel">
        <div class="panel-title"><span class="pt-caret" />攻防态势</div>
        <div class="chart-body">
          <div ref="attackChart" class="chart" />
          <div v-if="!hasAttackLinks" class="chart-empty">
            <div class="ce-ring" />
            <p>暂无攻破关系，等待选手交战…</p>
          </div>
        </div>
      </div>

      <!-- 靶机状态 -->
      <div class="panel">
        <div class="panel-title"><span class="pt-caret" />靶机状态<span class="pt-badge">{{ targets.length }}</span></div>
        <div class="target-grid">
          <div v-for="tg in targets" :key="tg.id" class="target" :class="tg.status.toLowerCase()">
            <div class="t-row">
              <span class="t-name">{{ tg.questionTitle || tg.name || ('靶机#' + tg.questionId) }}</span>
              <span class="t-led" />
            </div>
            <div class="t-team">{{ teamName(tg.teamId) }}</div>
            <div class="t-status">{{ statusText(tg.status) }}</div>
            <div class="t-hp"><div class="hp-bar" :style="{ width: hpWidth(tg) }" /></div>
          </div>
          <div v-if="!targets.length" class="board-empty">靶机数据加载中…</div>
        </div>
      </div>

      <!-- 得分趋势 -->
      <div class="panel">
        <div class="panel-title"><span class="pt-caret" />得分趋势</div>
        <div ref="trendChart" class="chart" />
      </div>

      <!-- 实时事件 -->
      <div class="panel">
        <div class="panel-title"><span class="pt-caret" />实时事件<span class="pt-badge live">{{ wsConnected ? 'LIVE' : 'OFF' }}</span></div>
        <div class="event-list">
          <transition-group name="ev" tag="div" class="ev-scroll">
            <div v-for="e in events" :key="e.id" class="event" :class="e.kind">
              <span class="e-time">{{ e.time }}</span>
              <span class="e-dot" />
              <span class="e-text" v-html="e.html" />
            </div>
          </transition-group>
          <div v-if="!events.length" class="board-empty events-empty">
            <span class="pulse-text">等待比赛数据<i>·</i><i>·</i><i>·</i></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { awdApi } from '@/api'
import { connectWs } from '@/utils/ws'

const route = useRoute()
const contestId = route.params.contestId

/* ---------------- 状态 ---------------- */
const contest = ref(null)
const teamNames = ref({})
const leaderboard = ref([])
const targets = ref([])
const attackLog = ref([])          // 真实攻破流水 {roundNo, attackerTeamId, victimTeamId, questionId, firstBlood}
const trendHistory = ref([])       // 后端快照 [{t, scores:{teamId:score}}]
const sessionSamples = ref([])     // 本次浏览期间的本地采样（真实轮询值）
const events = ref([])
const currentRound = ref(0)
const totalRounds = ref(0)
const remainSeconds = ref(0)
const wsConnected = ref(false)

const attackChart = ref(null)
const trendChart = ref(null)
let aChart = null, tChart = null
let ws = null, timer = null, pollTimer = null
let prevScores = {}                // 上次轮询的队伍分数，用于差分事件

const TEAM_COLORS = ['#22d3ee', '#f43f5e', '#a78bfa', '#34d399', '#fbbf24', '#fb923c']

const contestName = computed(() => contest.value?.name || 'AWD 攻防对抗')
const roundText = computed(() =>
  currentRound.value > 0 ? `第 ${currentRound.value}${totalRounds.value ? ' / ' + totalRounds.value : ''} 轮` : '轮次待定')
const topTeams = computed(() => leaderboard.value.slice(0, 6))
const hasAttackLinks = computed(() => aggregatedLinks().length > 0)

const countdown = computed(() => {
  const s = Math.max(0, remainSeconds.value)
  const h = String(Math.floor(s / 3600)).padStart(2, '0')
  const m = String(Math.floor((s % 3600) / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${h}:${m}:${ss}`
})

/* ---------------- 工具 ---------------- */
function teamName(id) {
  return String(teamNames.value?.[id] ?? teamNames.value?.[String(id)] ?? ('战队' + id))
}
function qName(qid) {
  const t = targets.value.find(x => String(x.questionId) === String(qid))
  return t?.questionTitle || t?.name || ('题目#' + qid)
}
function fmtScore(n) {
  return String(n).replace(/[^0-9.\-]/g, '')
}
function statusText(s) {
  return { UP: '运行中', DOWN: '已宕机', PENDING: '待启动', STOPPED: '已停止' }[s] || s || '未知'
}
function hpWidth(tg) {
  if (tg.health === 1) return '100%'
  if (String(tg.status).toLowerCase() === 'down') return '0%'
  return '40%'
}
function nowTime() {
  return new Date().toLocaleTimeString('zh-CN', { hour12: false })
}

function pushEvent(kind, html) {
  events.value.unshift({ id: Date.now() + '-' + Math.random(), kind, html, time: nowTime() })
  if (events.value.length > 60) events.value.pop()
}
const esc = s => String(s).replace(/[<>&"]/g, c => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;' }[c]))

/* ---------------- 真·事件流 ---------------- */
function loadHistoricalEvents() {
  // 由旧到新排序后的流水，前台从新到旧展示
  const log = attackLog.value.slice().reverse()
  for (const a of log) {
    renderAttackToEvents(a)
  }
}
function renderAttackToEvents(a) {
  const atk = esc(teamName(a.attackerTeamId))
  const vic = esc(teamName(a.victimTeamId))
  const q = esc(qName(a.questionId))
  if (a.firstBlood) {
    pushEvent('first', `<b class="hl-gold">${atk}</b> 一血！攻破 <b class="hl-red">${vic}</b> · ${q}`)
  } else {
    pushEvent('attack', `${atk} 攻破 <b class="hl-red">${vic}</b> · ${q}（第 ${a.roundNo} 轮）`)
  }
}

/* ---------------- 数据加载 ---------------- */
async function loadAll(initial = false) {
  try {
    const [ov, lb, tg, atk, hs] = await Promise.all([
      awdApi.getContest(contestId).catch(() => null),
      awdApi.getLeaderboard(contestId).catch(() => []),
      awdApi.getTargets(contestId).catch(() => []),
      awdApi.getAttacks(contestId).catch(() => []),
      awdApi.history ? awdApi.history(contestId).catch(() => []) : Promise.resolve([])
    ])
    if (ov) contest.value = ov
    const names = ov?.teamNames || {}
    if (Object.keys(names).length) teamNames.value = names
    if (ov?.currentRoundDetail != null || ov?.currentRound != null) {
      currentRound.value = ov?.currentRound || currentRound.value
      totalRounds.value = ov?.roundCount || totalRounds.value
      const det = ov?.currentRoundDetail
      if (det?.remainingSeconds != null) {
        remainSeconds.value = Number(det.remainingSeconds)
      } else if (ov?.endAt) {
        remainSeconds.value = Math.max(0, Math.floor((new Date(ov.endAt) - Date.now()) / 1000))
      }
    }

    const board = Array.isArray(lb) ? lb : (lb?.list || [])
    leaderboard.value = board

    // 分数差分 → 事件（轮询兜底，无 WS 时也能看到交战）
    if (!initial) diffScores(prevScores, board)
    prevScores = {}
    for (const t of board) prevScores[String(t.teamId)] = Number(fmtScore(t.score))

    targets.value = Array.isArray(tg) ? tg : (tg?.list || [])

    // 攻击流水合并（增量去重）
    const fresh = Array.isArray(atk) ? atk : []
    const seen = new Set(attackLog.value.map(a => `${a.roundNo}|${a.attackerTeamId}|${a.victimTeamId}|${a.questionId}`))
    for (const a of fresh) {
      const k = `${a.roundNo}|${a.attackerTeamId}|${a.victimTeamId}|${a.questionId}`
      if (!seen.has(k)) { attackLog.value.push(a); seen.add(k) }
    }

    // 得分历史（后端快照 + 本次浏览实时采样）
    trendHistory.value = Array.isArray(hs) ? hs : []
    sessionSamples.value.push({ t: Date.now(), scores: Object.fromEntries(board.map(t => [String(t.teamId), Number(fmtScore(t.score))])) })
    if (sessionSamples.value.length > 120) sessionSamples.value.shift()

    await nextTick()
    renderAttackChart()
    renderTrendChart()
    if (initial) loadHistoricalEvents()
  } catch (e) {
    console.warn('load awd data failed', e)
  }
}

/** 用分数差补事件：只在分数上涨时提示（攻击事件已由 WS 精确推送，这里兜底） */
function diffScores(prev, board) {
  if (!prev || !Object.keys(prev).length) return
  for (const t of board) {
    const id = String(t.teamId)
    const nowV = Number(fmtScore(t.score))
    if (prev[id] != null && nowV > prev[id]) {
      const delta = nowV - prev[id]
      if (!events.value.slice(0, 8).some(e => e.deltaFor === id && e.delta === delta)) {
        const withDelta = { kind: 'score', html: `<b class="hl-blue">${esc(teamName(id))}</b> 得分 +${delta}`, deltaFor: id, delta }
        events.value.unshift({ id: Date.now() + '-' + Math.random(), time: nowTime(), ...withDelta })
        if (events.value.length > 60) events.value.pop()
      }
    }
  }
}

/* ---------------- WS：与后端 WsMessage 对齐（type/payload） ---------------- */
function setupWs() {
  try {
    ws = connectWs(`/ws/awd/${contestId}`, (msg) => {
      wsConnected.value = true
      const d = msg?.payload ?? msg?.data ?? null   // ← 修正：后端字段是 payload
      if (!d) return
      switch (msg.type) {
        case 'ATTACK_EVENT':
          renderAttackToEvents(d)
          attackLog.value.push(d)
          break
        case 'FIRST_BLOOD': // ATTACK_EVENT 已渲染一血，此处仅确保事件去冗余，不入列表
          break
        case 'SCORE_UPDATE': {
          const id = String(d.teamId ?? d.teamIdOf ?? '')
          const items = leaderboard.value
          const hit = items.find(t => String(t.teamId) === id)
          if (hit) hit.score = d.score
          break
        }
        case 'ROUND_CHANGE': {
          const round = d.roundNo ?? d.round
          if (round != null) {
            currentRound.value = round
            pushEvent('info', `轮次切换：进入第 <b class="hl-blue">${round}</b> 轮`)
          }
          loadAll()
          break
        }
        case 'TARGET_ALERT':
          pushEvent('down', `<b class="hl-red">${esc(d.teamName || teamName(d.teamId))}</b> 靶机 ${esc(d.targetName || qName(d.questionId))} 宕机`)
          loadAll()
          break
        case 'ANNOUNCE':
          pushEvent('info', esc(d.content || d.message || ''))
          break
        default:
          break
      }
      nextTick(() => { renderAttackChart(); renderTrendChart() })
    })
    ws.onopen = () => { wsConnected.value = true }
    ws.onclose = () => { wsConnected.value = false }
  } catch (e) {
    console.warn('ws failed', e)
  }
}

/* ---------------- 图表 ---------------- */
function aggregatedLinks() {
  const m = new Map()
  for (const a of attackLog.value) {
    const k = `${a.attackerTeamId}->${a.victimTeamId}`
    m.set(k, (m.get(k) || 0) + 1)
  }
  const links = []
  for (const [k, w] of m) {
    const [source, target] = k.split('->')
    links.push({
      source: teamName(source), target: teamName(target), count: w,
      lineStyle: { width: Math.min(1 + w * 0.8, 4), color: 'rgba(244,63,94,0.45)', curveness: 0.22 }
    })
  }
  links.sort((x, y) => y.count - x.count)
  return links.slice(0, 24) // 防止过多连线糊成一团
}

function renderAttackChart() {
  if (!aChart) return
  const teams = topTeams.value
  const nodes = teams.map((t, i) => ({
    name: teamName(t.teamId),
    symbolSize: Math.max(36, Math.min(64, 30 + (Number(fmtScore(t.score)) || 0) / 8)),
    itemStyle: { color: TEAM_COLORS[i % TEAM_COLORS.length], shadowBlur: 12, shadowColor: 'rgba(0,0,0,0.5)' }
  }))
  const names = new Set(nodes.map(n => n.name))
  const links = aggregatedLinks().filter(l => names.has(l.source) && names.has(l.target))
  aChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      formatter: p => p.dataType === 'edge'
        ? `${p.data.source} → ${p.data.target}<br/>攻破 ${p.data.count} 次`
        : p.name
    },
    series: [{
      type: 'graph',
      layout: 'circular',
      circular: { rotateLabel: true },
      data: nodes,
      links,
      roam: false,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: 8,
      label: { show: true, position: 'bottom', color: '#c9d8f2', fontSize: 12, fontWeight: 600 },
      lineStyle: { opacity: 0.6 }
    }]
  }, { notMerge: true })
}

function buildTrendSeries() {
  // 后端快照 + 本次浏览采样，合并为完整时间线
  const all = [...trendHistory.value, ...sessionSamples.value]
    .map(p => ({ t: Number(p.t), scores: p.scores || {} }))
    .sort((a, b) => a.t - b.t)
  const teams = topTeams.value
  const series = []
  const categories = all.map(p => {
    const d = new Date(p.t)
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  })
  teams.forEach((t, i) => {
    const id = String(t.teamId)
    const data = all.map(p => (p.scores[id] ?? null))
    series.push({
      name: teamName(id),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 3,
      showSymbol: data.length <= 2,
      data,
      lineStyle: { width: i === 0 ? 2.6 : 1.8, color: TEAM_COLORS[i % TEAM_COLORS.length] },
      itemStyle: { color: TEAM_COLORS[i % TEAM_COLORS.length] },
      areaStyle: i === 0 ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(34,211,238,0.25)' },
          { offset: 1, color: 'rgba(34,211,238,0)' }
        ])
      } : undefined
    })
  })
  return { categories, series }
}

function renderTrendChart() {
  if (!tChart) return
  const { categories, series } = buildTrendSeries()
  tChart.setOption({
    backgroundColor: 'transparent',
    grid: { top: 30, left: 44, right: 14, bottom: 26 },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,20,44,0.9)', borderColor: 'rgba(99,144,219,0.3)', textStyle: { color: '#c9d8f2', fontSize: 12 } },
    legend: {
      top: 2, right: 6, icon: 'roundRect', itemWidth: 10, itemHeight: 4,
      textStyle: { color: '#8aa0c0', fontSize: 11 }, itemGap: 12
    },
    xAxis: {
      type: 'category', data: categories, boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(120,150,205,0.35)' } },
      axisTick: { show: false },
      axisLabel: { color: '#7e93b8', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(120,150,205,0.12)' } },
      axisLabel: { color: '#7e93b8', fontSize: 10 }
    },
    series
  }, { notMerge: true })
}

/* ---------------- 生命周期 ---------------- */
onMounted(() => {
  loadAll(true)
  setupWs()
  aChart = echarts.init(attackChart.value)
  tChart = echarts.init(trendChart.value)
  timer = setInterval(() => { if (remainSeconds.value > 0) remainSeconds.value-- }, 1000)
  pollTimer = setInterval(() => loadAll(false), 15000)
  window.addEventListener('resize', handleResize)
})

function handleResize() {
  aChart?.resize()
  tChart?.resize()
}

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(pollTimer)
  ws?.close()
  aChart?.dispose()
  tChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* ================= 主题变量 ================= */
.awd-screen {
  --bg-0: #050a18;
  --bg-1: #0a1430;
  --panel: rgba(13, 26, 55, 0.72);
  --panel-border: rgba(96, 145, 230, 0.18);
  --accent: #22d3ee;
  --blue: #3b82f6;
  --red: #f43f5e;
  --gold: #fbbf24;
  --green: #34d399;
  --text-1: #dbe7ff;
  --text-2: #8aa3c7;
  --text-3: #5e7397;
  --mono: 'JetBrains Mono', 'Courier New', monospace;

  min-height: 100vh;
  background:
    radial-gradient(1100px 420px at 50% -8%, rgba(59, 130, 246, 0.16), transparent 62%),
    radial-gradient(700px 300px at calc(100% + 100px) 110%, rgba(34, 211, 238, 0.1), transparent 60%),
    linear-gradient(172deg, var(--bg-1) 0%, var(--bg-0) 58%, #060c1e 100%);
  color: var(--text-1);
  padding: 14px 22px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-family: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ================= 顶部 ================= */
.screen-header {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 14px 22px;
  background: linear-gradient(180deg, rgba(17, 32, 66, 0.85), rgba(11, 21, 48, 0.8));
  border: 1px solid var(--panel-border);
  border-radius: 12px;
  overflow: hidden;
}
.screen-header::after {
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(34, 211, 238, 0.75), rgba(59, 130, 246, 0.6), transparent);
}
.h-side { display: flex; align-items: center; }
.h-side.right { justify-content: flex-end; }
.conn-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  letter-spacing: 2px;
  padding: 5px 13px;
  border-radius: 999px;
  border: 1px solid rgba(52, 211, 153, 0.35);
  color: var(--green);
  background: rgba(52, 211, 153, 0.06);
}
.conn-chip.off {
  border-color: rgba(251, 191, 36, 0.35);
  color: var(--gold);
  background: rgba(251, 191, 36, 0.06);
}
.chip-dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; box-shadow: 0 0 8px currentColor; animation: breathe 2s ease-in-out infinite; }
@keyframes breathe { 50% { opacity: 0.35; } }

.h-center { display: flex; align-items: center; gap: 16px; min-width: 0; }
.h-wing {
  flex: 0 0 90px;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(34, 211, 238, 0.7));
  position: relative;
}
.h-wing::before {
  content: '';
  position: absolute;
  left: 8px; top: -3px;
  width: 8px; height: 8px;
  border: 1px solid rgba(34, 211, 238, 0.8);
  transform: rotate(45deg);
  background: rgba(34, 211, 238, 0.25);
}
.h-wing.flip { transform: scaleX(-1); }
.h-title-box { text-align: center; min-width: 0; }
.h-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: linear-gradient(180deg, #f3f8ff 18%, #9fc8f7 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 30px rgba(76, 148, 245, 0.35);
}
.h-sub { font-size: 12px; color: var(--text-2); letter-spacing: 6px; margin-top: 3px; }

.timer { text-align: center; }
.t-label { font-size: 11px; color: var(--text-2); letter-spacing: 3px; }
.t-value {
  font-size: 38px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--accent);
  font-family: var(--mono);
  letter-spacing: 5px;
  text-shadow: 0 0 22px rgba(34, 211, 238, 0.4);
}
.timer.urgent .t-value { color: var(--red); animation: blink 1s steps(2) infinite; }
@keyframes blink { 50% { opacity: 0.45; } }

/* ================= 积分条 ================= */
.score-strip {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 12px 20px;
  background: var(--panel);
  border: 1px solid var(--panel-border);
  border-radius: 12px;
  backdrop-filter: blur(4px);
}
.strip-head {
  flex: 0 0 auto;
  font-size: 13px;
  font-weight: 600;
  color: var(--accent);
  letter-spacing: 3px;
  padding-right: 16px;
  border-right: 1px solid var(--panel-border);
  display: flex;
  align-items: center;
  gap: 8px;
}
.strip-icon { font-size: 10px; color: var(--accent); }
.strip-items { display: flex; gap: 10px; flex-wrap: wrap; flex: 1; }
.strip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 16px 6px 10px;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.07);
  border: 1px solid rgba(96, 145, 230, 0.16);
  transition: transform .2s, border-color .2s;
}
.strip-item:hover { transform: translateY(-1px); }
.strip-item.medal-1 { background: rgba(251, 191, 36, 0.12); border-color: rgba(251, 191, 36, 0.5); box-shadow: 0 0 14px rgba(251, 191, 36, 0.12); }
.strip-item.medal-2 { background: rgba(203, 213, 225, 0.1); border-color: rgba(203, 213, 225, 0.38); }
.strip-item.medal-3 { background: rgba(251, 146, 60, 0.1); border-color: rgba(251, 146, 60, 0.36); }
.rank-badge {
  width: 22px; height: 22px;
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 6px;
  font-family: var(--mono);
  font-size: 12px; font-weight: 700;
  color: var(--text-2);
  background: rgba(148, 163, 184, 0.1);
}
.medal-1 .rank-badge { color: #1a1206; background: linear-gradient(160deg, #fde047, #f59e0b); box-shadow: 0 0 10px rgba(251, 191, 36, 0.5); }
.medal-2 .rank-badge { color: #1e2430; background: linear-gradient(160deg, #e5eaf3, #94a3b8); }
.medal-3 .rank-badge { color: #231407; background: linear-gradient(160deg, #fdba74, #ea580c); }
.name { font-size: 13px; color: var(--text-1); max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.score { font-size: 15px; font-weight: 700; font-family: var(--mono); color: var(--blue); }
.medal-1 .score { color: var(--gold); }
.strip-item:not(.medal-1) .score { color: #60a5fa; }

/* ================= 四宫格 ================= */
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 12px;
  flex: 1;
  min-height: 0;
}
.panel {
  position: relative;
  background: var(--panel);
  border: 1px solid var(--panel-border);
  border-radius: 12px;
  padding: 13px 15px;
  display: flex;
  flex-direction: column;
  min-height: 270px;
  overflow: hidden;
  backdrop-filter: blur(4px);
}
/* 面板角标 */
.panel::before, .panel::after {
  content: '';
  position: absolute;
  width: 14px; height: 14px;
  pointer-events: none;
}
.panel::before {
  top: -1px; left: -1px;
  border-top: 2px solid rgba(34, 211, 238, 0.55);
  border-left: 2px solid rgba(34, 211, 238, 0.55);
  border-top-left-radius: 12px;
}
.panel::after {
  bottom: -1px; right: -1px;
  border-bottom: 2px solid rgba(59, 130, 246, 0.5);
  border-right: 2px solid rgba(59, 130, 246, 0.5);
  border-bottom-right-radius: 12px;
}
.panel-title {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 14px;
  font-weight: 700;
  color: #a5cdff;
  letter-spacing: 3px;
  margin-bottom: 10px;
}
.pt-caret {
  width: 4px; height: 14px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--accent), var(--blue));
  box-shadow: 0 0 8px rgba(34, 211, 238, 0.6);
}
.pt-badge {
  margin-left: auto;
  font-size: 11px;
  font-family: var(--mono);
  color: var(--text-2);
  background: rgba(96, 145, 230, 0.12);
  border: 1px solid rgba(96, 145, 230, 0.2);
  padding: 2px 9px;
  border-radius: 999px;
  letter-spacing: 1px;
}
.pt-badge.live { color: var(--green); border-color: rgba(52, 211, 153, 0.35); background: rgba(52, 211, 153, 0.08); }
.chart-body { position: relative; flex: 1; min-height: 0; }
.chart { position: absolute; inset: 0; }
.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-3);
  font-size: 12px;
  letter-spacing: 2px;
  pointer-events: none;
}
.ce-ring {
  width: 64px; height: 64px;
  border-radius: 50%;
  border: 1px dashed rgba(122, 158, 216, 0.35);
  animation: spin 9s linear infinite;
  position: relative;
}
.ce-ring::before {
  content: '';
  position: absolute;
  inset: 16px;
  border-radius: 50%;
  border: 1px solid rgba(122, 158, 216, 0.2);
}
@keyframes spin { to { transform: rotate(360deg); } }
.board-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  font-size: 12px;
  letter-spacing: 2px;
  padding: 24px 0;
  grid-column: 1 / -1;
}
.pulse-text i { animation: pulse3 1.6s infinite; }
.pulse-text i:nth-child(2) { animation-delay: .3s; }
.pulse-text i:nth-child(3) { animation-delay: .6s; }
@keyframes pulse3 { 0%, 100% { opacity: .2; } 50% { opacity: 1; } }

/* ================= 靶机 ================= */
.target-grid {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}
.target {
  padding: 10px 12px;
  border-radius: 9px;
  border: 1px solid rgba(96, 145, 230, 0.12);
  border-left: 3px solid var(--green);
  background: rgba(52, 211, 153, 0.05);
  transition: border-color .2s;
}
.target.down { border-left-color: var(--red); background: rgba(244, 63, 94, 0.09); }
.target.pending, .target.stopped { border-left-color: var(--text-3); background: rgba(148, 163, 184, 0.04); }
.t-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.t-name {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.t-led { flex: 0 0 auto; width: 7px; height: 7px; border-radius: 50%; background: var(--green); box-shadow: 0 0 7px var(--green); }
.target.down .t-led { background: var(--red); box-shadow: 0 0 7px var(--red); animation: breathe 1.2s infinite; }
.target.pending .t-led, .target.stopped .t-led { background: var(--text-3); box-shadow: none; }
.t-team { font-size: 11px; color: var(--text-2); margin-top: 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.t-status { font-size: 11px; margin-top: 4px; letter-spacing: 1px; }
.target.up .t-status { color: var(--green); }
.target.down .t-status { color: var(--red); }
.target.pending .t-status, .target.stopped .t-status { color: var(--text-3); }
.t-hp { height: 3px; background: rgba(255, 255, 255, 0.07); border-radius: 2px; margin-top: 7px; overflow: hidden; }
.hp-bar { height: 100%; background: linear-gradient(90deg, var(--green), var(--accent)); transition: width .4s; }
.target.down .hp-bar { background: var(--red); }
.target.pending .hp-bar, .target.stopped .hp-bar { background: var(--text-3); }

/* ================= 事件 ================= */
.event-list { flex: 1; min-height: 0; display: flex; }
.ev-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-right: 4px;
}
.event {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12.5px;
  line-height: 1.5;
  padding: 7px 12px 7px 10px;
  border-radius: 8px;
  background: rgba(96, 145, 230, 0.05);
  border: 1px solid rgba(96, 145, 230, 0.09);
  position: relative;
}
.event::before {
  content: '';
  position: absolute;
  left: 0; top: 20%; bottom: 20%;
  width: 3px;
  border-radius: 2px;
  background: rgba(96, 145, 230, 0.4);
}
.event.first { background: rgba(251, 191, 36, 0.09); border-color: rgba(251, 191, 36, 0.28); }
.event.first::before { background: var(--gold); box-shadow: 0 0 8px rgba(251, 191, 36, 0.7); }
.event.attack::before { background: var(--red); box-shadow: 0 0 8px rgba(244, 63, 94, 0.5); }
.event.score::before { background: var(--blue); }
.event.info::before { background: var(--text-3); }
.event.down { background: rgba(244, 63, 94, 0.08); border-color: rgba(244, 63, 94, 0.25); }
.event.down::before { background: var(--red); animation: breathe 1.2s infinite; }
.e-time { flex: 0 0 auto; color: var(--text-3); font-family: var(--mono); font-size: 11px; }
.e-dot { display: none; }
.e-text { color: var(--text-2); }
.event.first .e-text, .event.attack .e-text { color: var(--text-1); }
.hl-gold { color: var(--gold); }
.hl-red { color: #fb7185; }
.hl-blue { color: #60a5fa; }
.events-empty { height: 100%; }
.ev-enter-active { transition: all .35s ease; }
.ev-enter-from { opacity: 0; transform: translateX(-10px); }
.ev-leave-active { display: none; }

/* ================= 滚动条 ================= */
.target-grid::-webkit-scrollbar, .ev-scroll::-webkit-scrollbar { width: 5px; }
.target-grid::-webkit-scrollbar-thumb, .ev-scroll::-webkit-scrollbar-thumb {
  background: rgba(96, 145, 230, 0.3);
  border-radius: 3px;
}
.target-grid::-webkit-scrollbar-track, .ev-scroll::-webkit-scrollbar-track { background: transparent; }
</style>
