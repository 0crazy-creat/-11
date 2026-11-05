<template>
  <div class="awd-screen">
    <header class="screen-header">
      <div class="h-left">
        <span class="dot" :class="wsConnected ? 'on' : 'off'" />
        <span class="title">{{ contest?.name || 'AWD 攻防对抗' }}</span>
      </div>
      <div class="h-center">
        <div class="timer">
          <span class="label">剩余时间</span>
          <span class="value">{{ countdown }}</span>
        </div>
      </div>
      <div class="h-right">
        <span class="round">第 {{ currentRound }} / {{ totalRounds }} 轮</span>
      </div>
    </header>

    <section class="score-strip">
      <div class="strip-title">实时积分榜</div>
      <div class="strip-items">
        <div v-for="(t, i) in topTeams" :key="t.id" class="strip-item" :class="{ first: i === 0 }">
          <span class="rank">{{ i + 1 }}</span>
          <span class="name">{{ t.teamName || t.name || ('战队' + t.teamId) }}</span>
          <span class="score">{{ t.score }}</span>
        </div>
      </div>
    </section>

    <div class="grid">
      <div class="panel">
        <div class="panel-title">攻防态势</div>
        <div ref="attackChart" class="chart" />
      </div>
      <div class="panel">
        <div class="panel-title">靶机状态</div>
        <div class="target-grid">
          <div v-for="tg in targets" :key="tg.id" class="target" :class="tg.status.toLowerCase()">
            <div class="t-name">{{ tg.questionTitle || tg.name || ('靶机#' + tg.questionId) }}</div>
            <div class="t-team">{{ tg.teamName || '—' }}</div>
            <div class="t-status">{{ statusText(tg.status) }}</div>
            <div class="t-hp">
              <div class="hp-bar" :style="{ width: (tg.health === 1 ? 100 : 0) + '%' }" />
            </div>
          </div>
        </div>
      </div>
      <div class="panel">
        <div class="panel-title">得分趋势</div>
        <div ref="trendChart" class="chart" />
      </div>
      <div class="panel">
        <div class="panel-title">实时事件</div>
        <div class="event-list">
          <div v-for="e in events" :key="e.id" class="event" :class="e.type.toLowerCase()">
            <span class="e-time">{{ e.time }}</span>
            <span class="e-text">{{ e.text }}</span>
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

const contest = ref(null)
const leaderboard = ref([])
const targets = ref([])
const events = ref([])
const currentRound = ref(0)
const totalRounds = ref(0)
const remainSeconds = ref(0)
const wsConnected = ref(false)

const attackChart = ref(null)
const trendChart = ref(null)
let aChart = null, tChart = null
let ws = null, timer = null, pollTimer = null

const topTeams = computed(() => leaderboard.value.slice(0, 6))
const countdown = computed(() => {
  const s = Math.max(0, remainSeconds.value)
  const h = String(Math.floor(s / 3600)).padStart(2, '0')
  const m = String(Math.floor((s % 3600) / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${h}:${m}:${ss}`
})

function statusText(s) {
  return { UP: '运行中', DOWN: '已宕机', PENDING: '待启动', STOPPED: '已停止' }[s] || s
}

function pushEvent(type, text) {
  events.value.unshift({
    id: Date.now() + Math.random(),
    type,
    text,
    time: new Date().toLocaleTimeString('zh-CN', { hour12: false })
  })
  if (events.value.length > 50) events.value.pop()
}

function renderAttackChart() {
  if (!aChart) return
  const teams = leaderboard.value.slice(0, 6).map(t => t.teamName || t.name || ('战队' + t.teamId))
  const nodes = teams.map((n, i) => ({ name: n, symbolSize: 44, itemStyle: { color: ['#4dabff', '#ff5d6e', '#b57bff', '#00d4a0', '#ffd257', '#ff9a4d'][i] } }))
  const links = []
  for (let i = 0; i < nodes.length; i++) {
    for (let j = 0; j < nodes.length; j++) {
      if (i !== j && Math.random() > 0.6) links.push({ source: nodes[i].name, target: nodes[j].name, lineStyle: { color: 'rgba(255,93,110,0.55)', width: 1.5, curveness: 0.2 } })
    }
  }
  aChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {},
    series: [{
      type: 'graph',
      layout: 'circular',
      data: nodes,
      links,
      roam: false,
      label: { show: true, position: 'inside', color: '#ffffff', fontSize: 11 },
      lineStyle: { opacity: 0.4 }
    }]
  })
}

function renderTrendChart() {
  if (!tChart) return
  const x = [], s1 = [], s2 = []
  for (let i = 0; i < 12; i++) {
    x.push(`R${i + 1}`)
    s1.push(Math.round(500 + i * 40 + Math.random() * 60))
    s2.push(Math.round(480 + i * 35 + Math.random() * 60))
  }
  tChart.setOption({
    backgroundColor: 'transparent',
    grid: { top: 20, left: 40, right: 10, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: x, axisLine: { lineStyle: { color: 'rgba(140,170,220,0.35)' } }, axisLabel: { color: '#8aa0c0' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(140,170,220,0.12)' } }, axisLabel: { color: '#8aa0c0' } },
    series: [
      { name: leaderboard.value[0]?.name || 'Team A', type: 'line', smooth: true, data: s1, areaStyle: { opacity: 0.18 }, itemStyle: { color: '#4dabff' } },
      { name: leaderboard.value[1]?.name || 'Team B', type: 'line', smooth: true, data: s2, areaStyle: { opacity: 0.18 }, itemStyle: { color: '#ff5d6e' } }
    ]
  })
}

async function loadAll() {
  try {
    const [c, lb, tg] = await Promise.all([
      awdApi.getContest(contestId).catch(() => null),
      awdApi.getLeaderboard(contestId).catch(() => []),
      awdApi.getTargets(contestId).catch(() => [])
    ])
    if (c) {
      contest.value = c
      currentRound.value = c.currentRound || 0
      totalRounds.value = c.roundCount || 0
      if (c.endTime) {
        remainSeconds.value = Math.max(0, Math.floor((new Date(c.endTime) - Date.now()) / 1000))
      }
    }
    leaderboard.value = Array.isArray(lb) ? lb : (lb?.list || [])
    targets.value = Array.isArray(tg) ? tg : (tg?.list || [])
    await nextTick()
    renderAttackChart()
    renderTrendChart()
  } catch (e) {
    console.warn('load awd data failed', e)
  }
}

function setupWs() {
  try {
    ws = connectWs(`/ws/awd/${contestId}`, (msg) => {
      wsConnected.value = true
      if (msg.type === 'SCORE_UPDATE') {
        leaderboard.value = msg.data?.leaderboard || leaderboard.value
        renderAttackChart()
        renderTrendChart()
      } else if (msg.type === 'ROUND_START') {
        currentRound.value = msg.data?.round || currentRound.value + 1
        pushEvent('INFO', `第 ${currentRound.value} 轮开始`)
      } else if (msg.type === 'TARGET_DOWN') {
        pushEvent('DOWN', `${msg.data?.teamName} 靶机 ${msg.data?.targetName} 宕机`)
        loadAll()
      } else if (msg.type === 'FIRST_BLOOD') {
        pushEvent('FIRST', `一血！${msg.data?.teamName} 攻破 ${msg.data?.targetName}`)
      } else if (msg.type === 'ANNOUNCE') {
        pushEvent('INFO', msg.data?.content || '')
      }
    })
    ws.onopen = () => { wsConnected.value = true }
    ws.onclose = () => { wsConnected.value = false }
  } catch (e) {
    console.warn('ws failed', e)
  }
}

onMounted(() => {
  loadAll()
  setupWs()
  aChart = echarts.init(attackChart.value)
  tChart = echarts.init(trendChart.value)
  renderAttackChart()
  renderTrendChart()
  timer = setInterval(() => { if (remainSeconds.value > 0) remainSeconds.value-- }, 1000)
  pollTimer = setInterval(loadAll, 15000)
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
/* ===== 统一深色大屏主题：深海军蓝底 + 青蓝主色 + 金色榜首 + 红色攻击 ===== */
.awd-screen {
  min-height: 100vh;
  background:
    radial-gradient(1200px 500px at 50% -10%, rgba(60, 130, 246, 0.14), transparent 60%),
    linear-gradient(180deg, #0a1226 0%, #0a1930 55%, #081226 100%);
  color: #cfe0ff;
  padding: 14px 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ---------- 顶部 ---------- */
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 22px;
  background: rgba(15, 27, 55, 0.65);
  border: 1px solid rgba(77, 171, 247, 0.18);
  border-radius: 10px;
}
.h-left { display: flex; align-items: center; gap: 10px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.dot.on { background: #00d4a0; box-shadow: 0 0 10px rgba(0, 212, 160, 0.9); }
.dot.off { background: #ff5d6e; box-shadow: 0 0 8px rgba(255, 93, 110, 0.8); }
.title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #eaf3ff;
  text-shadow: 0 0 18px rgba(77, 171, 247, 0.45);
}
.timer { display: flex; flex-direction: column; align-items: center; }
.timer .label {
  font-size: 12px;
  color: #8aa0c0;
  letter-spacing: 2px;
}
.timer .value {
  font-size: 30px;
  font-weight: 700;
  color: #3ce6e0;
  font-family: var(--csp-font-mono, 'Courier New', monospace);
  letter-spacing: 4px;
  text-shadow: 0 0 16px rgba(60, 230, 224, 0.35);
}
.round {
  font-size: 14px;
  color: #8aa0c0;
  letter-spacing: 1px;
}

/* ---------- 实时积分榜 ---------- */
.score-strip {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 22px;
  background: rgba(15, 27, 55, 0.5);
  border: 1px solid rgba(77, 171, 247, 0.14);
  border-radius: 10px;
}
.strip-title {
  font-size: 13px;
  color: #8aa0c0;
  white-space: nowrap;
  letter-spacing: 2px;
  padding-left: 10px;
  border-left: 3px solid #3ce6e0;
}
.strip-items { display: flex; gap: 10px; flex-wrap: wrap; }
.strip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border-radius: 6px;
  background: rgba(77, 171, 247, 0.08);
  border: 1px solid rgba(77, 171, 247, 0.18);
}
.strip-item.first {
  background: rgba(255, 210, 87, 0.1);
  border-color: rgba(255, 210, 87, 0.45);
}
.strip-item .rank {
  font-family: var(--csp-font-mono, 'Courier New', monospace);
  font-weight: 700;
  color: #8aa0c0;
}
.strip-item.first .rank { color: #ffd257; }
.strip-item .name { font-size: 13px; color: #dbe8ff; }
.strip-item .score {
  font-family: var(--csp-font-mono, 'Courier New', monospace);
  font-weight: 700;
  color: #4dabff;
}
.strip-item.first .score { color: #ffd257; }

/* ---------- 四宫格面板 ---------- */
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 12px;
  flex: 1;
  min-height: 0;
}
.panel {
  background: linear-gradient(180deg, rgba(18, 32, 63, 0.85), rgba(13, 23, 47, 0.85));
  border: 1px solid rgba(77, 171, 247, 0.16);
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  min-height: 260px;
  box-shadow: inset 0 0 40px rgba(24, 58, 121, 0.12);
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #9fd4ff;
  letter-spacing: 2px;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 3px solid #3ce6e0;
}
.chart { flex: 1; min-height: 220px; }

/* ---------- 靶机卡片 ---------- */
.target-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
  overflow-y: auto;
}
.target {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid rgba(77, 171, 247, 0.12);
  border-left: 3px solid #00d4a0;
  background: rgba(77, 171, 247, 0.05);
}
.target.down { border-left-color: #ff5d6e; background: rgba(255, 93, 110, 0.08); }
.target.pending, .target.stopped { border-left-color: #8aa0c0; }
.t-name {
  font-size: 13px;
  font-weight: 600;
  color: #e3eeff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.t-team { font-size: 11px; color: #8aa0c0; margin-top: 2px; }
.t-status { font-size: 11px; margin-top: 4px; }
.target.up .t-status { color: #00d4a0; }
.target.down .t-status { color: #ff5d6e; }
.target.pending .t-status, .target.stopped .t-status { color: #8aa0c0; }
.t-hp {
  height: 4px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  margin-top: 6px;
  overflow: hidden;
}
.hp-bar {
  height: 100%;
  background: linear-gradient(90deg, #00d4a0, #3ce6e0);
  transition: width .3s;
}
.target.down .hp-bar { background: #ff5d6e; }

/* ---------- 实时事件 ---------- */
.event-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.event {
  display: flex;
  gap: 10px;
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(77, 171, 247, 0.08);
}
.e-time { color: #6f87ab; font-family: var(--csp-font-mono, 'Courier New', monospace); flex: 0 0 auto; }
.e-text { color: #b9cdea; }
.event.down .e-text { color: #ff8095; }
.event.first .e-text { color: #ffd257; }
.event.score .e-text { color: #6cb8ff; }
.event.info .e-text { color: #9db4d4; }

/* ---------- 滚动条 ---------- */
.target-grid::-webkit-scrollbar,
.event-list::-webkit-scrollbar { width: 6px; }
.target-grid::-webkit-scrollbar-thumb,
.event-list::-webkit-scrollbar-thumb {
  background: rgba(77, 171, 247, 0.25);
  border-radius: 3px;
}
.target-grid::-webkit-scrollbar-track,
.event-list::-webkit-scrollbar-track { background: transparent; }
</style>
