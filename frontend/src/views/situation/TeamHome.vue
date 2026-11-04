<template>
  <div class="team-home">
    <header class="th-header">
      <div class="avatar">{{ (team.teamName || 'T').slice(0, 1) }}</div>
      <div class="meta">
        <div class="name-row">
          <h1 class="name">{{ team.teamName || '战队详情' }}</h1>
          <el-tag v-if="team.status" size="small" effect="plain">{{ team.status }}</el-tag>
        </div>
        <div class="sub">
          <span v-if="team.school">{{ team.school }}</span>
          <span v-if="team.captain">队长：{{ team.captain }}</span>
          <span v-if="team.createTime">成立于 {{ team.createTime }}</span>
        </div>
      </div>
      <div class="score-card">
        <div class="sc-item"><span class="sc-v">{{ team.score ?? 0 }}</span><span class="sc-l">总积分</span></div>
        <div class="sc-item"><span class="sc-v">{{ team.rank ?? '—' }}</span><span class="sc-l">当前排名</span></div>
        <div class="sc-item"><span class="sc-v">{{ team.solveCount ?? 0 }}</span><span class="sc-l">解题数</span></div>
        <div class="sc-item"><span class="sc-v">{{ team.attackCount ?? 0 }}</span><span class="sc-l">攻击成功</span></div>
      </div>
    </header>

    <div class="th-body">
      <!-- 得分趋势 -->
      <section class="card">
        <div class="card-title">得分趋势</div>
        <div ref="trendRef" class="chart" />
      </section>

      <!-- 成员 -->
      <section class="card">
        <div class="card-title">战队成员</div>
        <el-table :data="members" size="small" stripe max-height="260">
          <el-table-column prop="username" label="成员" min-width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role === 'CAPTAIN'" size="small" type="warning" effect="plain">队长</el-tag>
              <span v-else>成员</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="贡献分" width="90" align="right" />
          <el-table-column prop="solveCount" label="解题" width="80" align="right" />
        </el-table>
        <el-empty v-if="!members.length" description="暂无成员数据" :image-size="50" />
      </section>

      <!-- 解题记录 -->
      <section class="card wide">
        <div class="card-title">解题记录</div>
        <el-table :data="solves" size="small" stripe max-height="320">
          <el-table-column prop="challengeTitle" label="题目" min-width="180" />
          <el-table-column prop="category" label="分类" width="110" />
          <el-table-column prop="score" label="分值" width="80" align="right" />
          <el-table-column prop="firstBlood" label="一血" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.firstBlood" size="small" type="danger" effect="dark">1st</el-tag>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="solvedAt" label="时间" width="170" />
        </el-table>
        <el-empty v-if="!solves.length" description="暂无解题记录" :image-size="50" />
      </section>

      <!-- 攻防记录 -->
      <section class="card">
        <div class="card-title">AWD 攻防记录</div>
        <div class="awd-list">
          <div v-for="a in awdRecords" :key="a.id" class="awd-row" :class="a.type.toLowerCase()">
            <span class="a-type">{{ a.type === 'ATTACK' ? '攻击' : '被攻' }}</span>
            <span class="a-opp">{{ a.targetName }}</span>
            <span class="a-round">R{{ a.round }}</span>
            <span class="a-time">{{ a.time }}</span>
          </div>
          <el-empty v-if="!awdRecords.length" description="暂无攻防记录" :image-size="50" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTeamProfile, getAwdAttackLog } from '@/api'
import { connectWs } from '@/utils/ws'

const props = defineProps({ teamId: { type: [String, Number], required: true } })

const team = ref({})
const members = ref([])
const solves = ref([])
const awdRecords = ref([])
const trendRef = ref(null)
let trendInst = null
let ws = null

async function load() {
  try {
    const p = await getTeamProfile(props.teamId)
    team.value = p?.team || p || {}
    members.value = p?.members || []
    solves.value = p?.solves || p?.solveRecords || []
    awdRecords.value = p?.awdRecords || p?.attacks || []
    renderTrend(p?.trend || p?.scoreTrend || [])
    if (!awdRecords.value.length && p?.contestId) await loadAwd(p.contestId)
  } catch (e) { /* 静默 */ }
}

async function loadAwd(contestId) {
  try {
    const rows = await getAwdAttackLog(contestId, { limit: 50, teamId: props.teamId })
    const list = Array.isArray(rows) ? rows : (rows?.records || [])
    awdRecords.value = list.map((r, i) => ({
      id: r.id ?? i,
      type: String(r.attackerTeamId) === String(props.teamId) ? 'ATTACK' : 'DEFEND',
      targetName: String(r.attackerTeamId) === String(props.teamId)
        ? (r.victimTeamName || r.victim || '—')
        : (r.attackerTeamName || r.attacker || '—'),
      round: r.round ?? '-',
      time: r.createdAt || r.time || ''
    }))
  } catch (e) { /* 静默 */ }
}

function renderTrend(data) {
  if (!trendInst) return
  const points = Array.isArray(data) ? data : []
  trendInst.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 20, top: 24, bottom: 30 },
    xAxis: {
      type: 'category',
      data: points.map((p) => p.time || p.label || ''),
      axisLabel: { color: '#86909c', fontSize: 11 },
      axisLine: { lineStyle: { color: '#e5e6eb' } }
    },
    yAxis: { type: 'value', axisLabel: { color: '#86909c', fontSize: 11 }, splitLine: { lineStyle: { color: '#f2f3f5' } } },
    series: [{
      type: 'line', smooth: true, showSymbol: false,
      areaStyle: { opacity: 0.15, color: '#295FFF' },
      lineStyle: { color: '#295FFF', width: 2 },
      itemStyle: { color: '#295FFF' },
      data: points.map((p) => p.score ?? p.value ?? 0)
    }]
  }, true)
}

function subscribe() {
  try {
    ws = connectWs(`/ws/team/${props.teamId}`, (msg) => {
      if ((msg.type || '').toUpperCase() === 'SCORE_UPDATE') load()
    })
  } catch (e) { /* 静默 */ }
}

onMounted(async () => {
  await nextTick()
  trendInst = echarts.init(trendRef.value)
  await load()
  subscribe()
  window.addEventListener('resize', resize)
})

function resize() { trendInst?.resize() }

onBeforeUnmount(() => {
  ws?.close()
  trendInst?.dispose()
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.team-home { min-height: 100vh; background: #f7f8fa; padding: 20px 24px 32px; font-family: var(--csp-font-family); }

.th-header {
  display: flex; align-items: center; gap: 20px; padding: 20px 24px; margin-bottom: 16px;
  background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.avatar {
  width: 64px; height: 64px; flex: 0 0 64px; border-radius: 14px;
  background: linear-gradient(135deg, var(--csp-primary), #6a8cff);
  color: #fff; font-size: 28px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.meta { flex: 1; min-width: 0; }
.name-row { display: flex; align-items: center; gap: 10px; }
.name { margin: 0; font-size: 22px; font-weight: 600; color: var(--csp-title); }
.sub { display: flex; gap: 16px; margin-top: 6px; font-size: 13px; color: var(--csp-text-secondary); flex-wrap: wrap; }
.score-card { display: flex; gap: 24px; }
.sc-item { display: flex; flex-direction: column; align-items: flex-end; }
.sc-v { font-size: 22px; font-weight: 700; color: var(--csp-primary); font-family: var(--csp-font-mono); }
.sc-l { font-size: 12px; color: var(--csp-text-secondary); }

.th-body { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.card { background: #fff; border-radius: 12px; padding: 16px 18px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); min-height: 0; }
.card.wide { grid-column: 1 / -1; }
.card-title { font-size: 15px; font-weight: 600; color: var(--csp-title); margin-bottom: 12px; }
.chart { height: 260px; }
.awd-list { max-height: 280px; overflow-y: auto; display: flex; flex-direction: column; gap: 4px; }
.awd-row {
  display: grid; grid-template-columns: 54px 1fr 44px 140px; gap: 8px; align-items: center;
  font-size: 12px; padding: 6px 8px; border-radius: 6px; background: #f7f8fa;
}
.awd-row .a-type { font-weight: 600; color: var(--csp-text); }
.awd-row.attack .a-type { color: var(--csp-success); }
.awd-row.defend .a-type { color: var(--csp-danger); }
.a-opp { color: var(--csp-text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.a-round { font-family: var(--csp-font-mono); color: var(--csp-text-secondary); }
.a-time { font-family: var(--csp-font-mono); color: var(--csp-placeholder); font-size: 11px; }
.muted { color: var(--csp-placeholder); }
</style>