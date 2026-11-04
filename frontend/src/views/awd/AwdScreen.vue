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
          <span class="name">{{ t.name }}</span>
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
            <div class="t-name">{{ tg.name }}</div>
            <div class="t-team">{{ tg.teamName }}</div>
            <div class="t-status">{{ statusText(tg.status) }}</div>
            <div class="t-hp">
              <div class="hp-bar" :style="{ width: tg.health + '%' }" />
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
  const teams = leaderboard.value.slice(0, 6).map(t => t.name)
  const nodes = teams.map((n, i) => ({ name: n, symbolSize: 44, itemStyle: { color: ['#295FFF', '#f53f3f', '#7c4dff', '#00b42a', '#ffab00', '#ff9100'][i] } }))
  const links = []
  for (let i = 0; i < nodes.length; i++) {
    for (let j = 0; j < nodes.length; j++) {
      if (i !== j && Math.random() > 0.6) links.push({ source: nodes[i].name, target: nodes[j].name, lineStyle: { color: '#f53f3f', width: 1.5, curveness: 0.2 } })
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
      label: { show: true, color: '#4e5969', fontSize: 12 },
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
    xAxis: { type: 'category', data: x, axisLine: { lineStyle: { color: '#e5e6eb' } }, axisLabel: { color: '#86909c' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#e5e6eb' } }, axisLabel: { color: '#86909c' } },
    series: [
      { name: leaderboard.value[0]?.name || 'Team A', type: 'line', smooth: true, data: s1, areaStyle: { opacity: 0.15 }, itemStyle: { color: '#295FFF' } },
      { name: leaderboard.value[1]?.name || 'Team B', type: 'line', smooth: true, data: s2, areaStyle: { opacity: 0.15 }, itemStyle: { color: '#f53f3f' } }
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
.awd-screen {
  min-height: 100vh;
  background: #f7f8fa;
  color: #4e5969;
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 16px;
  border-bottom: 1px solid #e5e6eb;
}
.h-left { display: flex; align-items: center; gap: 10px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.dot.on { background: #00b42a; box-shadow: 0 0 12px #00b42a; }
.dot.off { background: #f53f3f; }
.title { font-size: 22px; font-weight: 700; letter-spacing: 2px; color: #1d2129; }
.timer { display: flex; flex-direction: column; align-items: center; }
.timer .label { font-size: 12px; color: #86909c; }
.timer .value { font-size: 28px; font-weight: 700; color: #295FFF; font-family: 'Courier New', monospace; letter-spacing: 3px; }
.round { font-size: 14px; color: #86909c; }
.score-strip {
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(90deg, rgba(0,229,255,0.06), rgba(0,229,255,0.02));
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  padding: 10px 16px;
}
.strip-title { font-size: 13px; color: #86909c; white-space: nowrap; }
.strip-items { display: flex; gap: 12px; flex-wrap: wrap; }
.strip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 6px;
  background: rgba(255,255,255,0.04);
}
.strip-item.first { background: rgba(0,229,255,0.12); border: 1px solid rgba(0,229,255,0.3); }
.strip-item .rank { font-weight: 700; color: #ffab00; }
.strip-item .name { font-size: 13px; }
.strip-item .score { font-weight: 700; color: #295FFF; }
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 16px;
  flex: 1;
  min-height: 0;
}
.panel {
  background: rgba(10, 22, 40, 0.6);
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 260px;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #295FFF;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 3px solid #295FFF;
}
.chart { flex: 1; min-height: 220px; }
.target-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
  overflow-y: auto;
}
.target {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #e5e6eb;
  background: rgba(255,255,255,0.02);
}
.target.up { border-color: rgba(0,230,118,0.4); }
.target.down { border-color: rgba(255,77,109,0.6); background: rgba(255,77,109,0.06); }
.t-name { font-size: 13px; font-weight: 600; color: #1d2129; }
.t-team { font-size: 11px; color: #86909c; margin-top: 2px; }
.t-status { font-size: 11px; margin-top: 4px; }
.target.up .t-status { color: #00b42a; }
.target.down .t-status { color: #f53f3f; }
.t-hp { height: 4px; background: #e5e6eb; border-radius: 2px; margin-top: 6px; overflow: hidden; }
.hp-bar { height: 100%; background: linear-gradient(90deg, #00b42a, #295FFF); transition: width .3s; }
.event-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.event { display: flex; gap: 10px; font-size: 12px; padding: 4px 8px; border-radius: 4px; background: rgba(255,255,255,0.02); }
.e-time { color: #86909c; font-family: monospace; }
.event.down .e-text { color: #f53f3f; }
.event.first .e-text { color: #ffab00; }

.dot.on { background: #00b42a; box-shadow: 0 0 8px #00b42a; }
.dot.off { background: #f53f3f; }
.title { font-size: 18px; font-weight: 600; letter-spacing: 1px; }
.h-center { display: flex; align-items: center; gap: 24px; }
.timer { display: flex; flex-direction: column; align-items: center; }
.timer .label { font-size: 11px; color: #86909c; }
.timer .value { font-family: var(--csp-font-mono); font-size: 22px; font-weight: 700; color: #295FFF; }
.timer .value.urgent { color: #f53f3f; animation: blink 1s steps(2) infinite; }
@keyframes blink { 50% { opacity: 0.4; } }
.round { font-size: 13px; color: #a8b7cc; }
.clock { font-family: var(--csp-font-mono); font-size: 14px; color: #86909c; }

.score-strip {
  flex: 0 0 auto; display: flex; align-items: center; gap: 16px;
  padding: 10px 24px; border-bottom: 1px solid rgba(77,171,247,0.12);
  background: rgba(15,23,48,0.6);
}
.strip-title { flex: 0 0 auto; font-size: 13px; color: #86909c; letter-spacing: 1px; }
.strip-items { flex: 1; display: flex; gap: 12px; overflow: hidden; }
.strip-item {
  display: flex; align-items: center; gap: 8px; padding: 6px 12px;
  background: rgba(77,171,247,0.08); border: 1px solid rgba(77,171,247,0.15); border-radius: 6px;
  font-size: 13px; white-space: nowrap;
}
.strip-item.first { background: rgba(255,212,59,0.12); border-color: rgba(255,212,59,0.35); }
.strip-item .rank { font-family: var(--csp-font-mono); color: #86909c; }
.strip-item.first .rank { color: #ff7d00; }
.strip-item .name { color: #4e5969; }
.strip-item .score { font-family: var(--csp-font-mono); color: #ff7d00; font-weight: 700; }
.strip-item .delta { font-size: 11px; font-family: var(--csp-font-mono); }
.delta.up { color: #00b42a; }
.delta.down { color: #f53f3f; }

.grid {
  flex: 1; min-height: 0; display: grid;
  grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr;
  gap: 14px; padding: 14px 24px 20px;
}
.panel {
  display: flex; flex-direction: column; min-height: 0;
  background: rgba(15,23,48,0.75); border: 1px solid rgba(77,171,247,0.14); border-radius: 8px;
  padding: 12px 14px;
}
.panel-title { font-size: 13px; font-weight: 600; color: #4e5969; letter-spacing: 1px; margin-bottom: 8px; }
.chart { flex: 1; min-height: 0; }

.target-grid { flex: 1; min-height: 0; overflow-y: auto; display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; }
.target { padding: 8px 10px; border-radius: 6px; background: rgba(255,255,255,0.03); border-left: 3px solid #00b42a; }
.target.down { border-left-color: #f53f3f; }
.target.attacked { border-left-color: #ff7d00; }
.target.unknown { border-left-color: #86909c; }
.t-top { display: flex; justify-content: space-between; font-size: 12px; }
.t-name { color: #1d2129; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.t-team { color: #86909c; font-size: 11px; flex: 0 0 auto; }
.t-status { font-size: 11px; color: #86909c; margin: 3px 0 5px; }
.t-hp { height: 4px; background: rgba(255,255,255,0.08); border-radius: 2px; overflow: hidden; }
.hp-bar { height: 100%; background: #00b42a; transition: width 0.4s; }
.target.down .hp-bar { background: #f53f3f; }
.target.attacked .hp-bar { background: #ff7d00; }
.t-foot { display: flex; justify-content: space-between; font-size: 10px; color: #86909c; margin-top: 4px; font-family: var(--csp-font-mono); }

.event-list { flex: 1; min-height: 0; overflow-y: auto; display: flex; flex-direction: column; gap: 4px; }
.event { display: flex; gap: 10px; font-size: 12px; line-height: 1.5; padding: 3px 0; border-bottom: 1px dashed rgba(255,255,255,0.04); }
.e-time { flex: 0 0 auto; color: #86909c; font-family: var(--csp-font-mono); }
.event.down .e-text { color: #f53f3f; }
.event.first .e-text { color: #ff7d00; }
.event.score .e-text { color: #295FFF; }
.event.info .e-text { color: #86909c; }
</style>
