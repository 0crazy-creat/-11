<template>
  <div class="ranking-page">
    <header class="rk-header">
      <div class="left">
        <h1 class="rk-title">实时排行榜</h1>
        <span class="rk-sub" v-if="updatedAt">更新于 {{ updatedAt }}</span>
        <el-tag v-if="dataSource" size="small" :type="dataSource === 'REDIS' ? 'success' : 'info'" effect="plain">
          {{ sourceLabel }}
        </el-tag>
      </div>
      <div class="rk-tools">
        <el-select v-model="contestId" size="small" style="width: 220px" placeholder="选择赛事" @change="onContestChange">
          <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="String(c.id)" />
        </el-select>

        <el-radio-group v-if="ctfMode" v-model="mode" size="small" @change="onModeChange">
          <el-radio-button value="total">总分榜</el-radio-button>
          <el-radio-button value="stage">阶段榜</el-radio-button>
          <el-radio-button value="firstblood">首杀榜</el-radio-button>
        </el-radio-group>

        <el-select v-if="ctfMode" v-model="topN" size="small" style="width: 110px" @change="reload">
          <el-option v-for="n in TOP_OPTIONS" :key="n" :label="`Top ${n}`" :value="n" />
        </el-select>
        <el-input-number v-if="ctfMode" v-model="topN" size="small" :min="1" :max="500" :step="10"
          controls-position="right" style="width: 120px" @change="reload" />

        <el-select v-model="refreshMs" size="small" style="width: 130px">
          <el-option label="实时 1 秒" :value="1000" />
          <el-option label="3 秒" :value="3000" />
          <el-option label="10 秒" :value="10000" />
          <el-option label="30 秒" :value="30000" />
          <el-option label="手动刷新" :value="0" />
        </el-select>

        <el-switch v-if="ctfMode && mode !== 'firstblood'" v-model="showMatrix" size="small"
          active-text="解题矩阵" inline-prompt style="margin: 0 2px" />

        <template v-if="canExport">
          <el-button size="small" @click="doExport('csv')">导出 CSV</el-button>
          <el-button size="small" @click="doExport('xlsx')">导出 XLSX</el-button>
        </template>

        <el-button size="small" :icon="Refresh" circle @click="reload" />
      </div>
    </header>

    <section v-if="ctfMode && mode === 'stage'" class="stage-bar">
      <div class="stage-chips">
        <button v-for="s in stages" :key="s.index" class="stage-chip"
          :class="{ active: s.index === stageIndex, live: s.current }" @click="pickStage(s.index)">
          <span class="sc-label">{{ s.label }}</span>
          <span class="sc-time">{{ shortLabel(s) }}</span>
          <span class="sc-count">{{ s.solves }} 解</span>
        </button>
      </div>
      <div class="stage-hint">阶段榜 = 截至该阶段末的累计得分（按提交当时的分值回放，不受后续动态衰减影响）</div>
    </section>

    <section class="podium" v-if="podiumOrder.length">
      <div v-for="t in podiumOrder" :key="t.id" class="podium-item" :class="`p${t.rank}`" @click="goTeam(t)">
        <div class="p-rank">{{ medal(t.rank) }}</div>
        <el-avatar :size="40" :src="t.logo" class="p-logo">{{ (t.teamName || '?').charAt(0) }}</el-avatar>
        <div class="p-name">{{ t.teamName }}</div>
        <div class="p-score">{{ t.score }}</div>
        <div class="p-solves">{{ t.solveCount ?? 0 }} 解<span v-if="t.firstBloods" class="p-fb"> · 🔥{{ t.firstBloods }}</span></div>
      </div>
    </section>

    <section v-if="ctfMode && mode !== 'firstblood'" class="card trend-card">
      <div class="card-title">得分趋势</div>
      <div ref="trendRef" class="trend-chart"></div>
    </section>

    <el-input v-model="keyword" size="small" placeholder="搜索战队 / 题目" clearable style="width: 200px; margin-bottom: 10px" />

    <el-table v-if="ctfMode && mode !== 'firstblood'" :data="pagedList" stripe size="default"
      class="rk-table" v-loading="loading" :row-class-name="rowClass" border>
      <el-table-column label="排名" width="80" align="center" fixed>
        <template #default="{ row }">
          <span class="rank-cell" :class="`r${row.rank}`">{{ row.rank }}</span>
        </template>
      </el-table-column>
      <el-table-column label="战队" min-width="200" fixed>
        <template #default="{ row }">
          <div class="team-cell">
            <el-avatar :size="24" :src="row.logo">{{ (row.teamName || '?').charAt(0) }}</el-avatar>
            <div class="team-main">
              <a class="team-link" @click="goTeam(row)">{{ row.teamName }}</a>
              <div class="team-sub">
                <span v-if="row.orgName" class="org">{{ row.orgName }}</span>
                <el-tag v-if="row.firstBloods" size="small" type="danger" effect="dark">一血 ×{{ row.firstBloods }}</el-tag>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="得分" width="100" align="right" />
      <el-table-column prop="solveCount" label="解题数" width="90" align="right" />
      <el-table-column label="最近解题" width="160">
        <template #default="{ row }">{{ fmt(row.lastSolveAt || row.lastSubmit) }}</template>
      </el-table-column>
      <el-table-column v-if="showMatrix" v-for="c in challenges" :key="c.questionId"
        :label="shorten(c.title)" width="96" align="center">
        <template #header>
          <el-tooltip :content="`${c.title} · ${c.category || '未分类'} · ${c.solvedCount || 0} 解`" placement="top">
            <span>{{ shorten(c.title) }}<em>({{ c.solvedCount || 0 }})</em></span>
          </el-tooltip>
        </template>
        <template #default="{ row }">
          <span v-if="cellOf(row, c.questionId)" class="solve-cell" :class="bloodClass(row, c.questionId)">
            {{ bloodText(row, c.questionId) }}
          </span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else-if="ctfMode" :data="pagedList" stripe size="default" class="rk-table" v-loading="loading" border>
      <el-table-column type="index" label="序号" :index="fbRank" width="70" align="center" />
      <el-table-column label="题目" min-width="200">
        <template #default="{ row }">
          <div class="q-cell">
            <span class="q-title">{{ row.questionTitle }}</span>
            <div class="q-sub">
              <el-tag size="small" effect="plain">{{ row.category || '—' }}</el-tag>
              <el-tag size="small" :type="diffType(row.difficulty)" effect="plain">{{ row.difficulty || '—' }}</el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分值" width="90" align="right" />
      <el-table-column label="首杀战队" min-width="180">
        <template #default="{ row }">
          <div class="team-cell">
            <el-tag size="small" type="danger" effect="dark">1st</el-tag>
            <el-avatar :size="22" :src="row.logo">{{ (row.teamName || '?').charAt(0) }}</el-avatar>
            <a class="team-link" @click="goTeam(row)">{{ row.teamName }}</a>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="solver" label="解题者" width="130" />
      <el-table-column label="首杀时间" width="170">
        <template #default="{ row }">{{ fmt(row.solvedAt) }}</template>
      </el-table-column>
      <el-table-column label="解出队伍" min-width="180">
        <template #default="{ row }">
          <el-progress :percentage="acceptRate(row)" :stroke-width="10" :show-text="false" />
          <div class="rate">{{ row.solvedCount }}/{{ boardTeamCount }} 队 · {{ acceptRate(row) }}%</div>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else :data="pagedList" stripe size="default" class="rk-table" v-loading="loading">
      <el-table-column label="排名" width="80" align="center">
        <template #default="{ row }">
          <span class="rank-cell" :class="`r${row.rank}`">{{ row.rank }}</span>
        </template>
      </el-table-column>
      <el-table-column label="战队" min-width="180">
        <template #default="{ row }">
          <a class="team-link" @click="goTeam(row)">{{ row.teamName }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="得分" width="110" align="right" />
      <el-table-column prop="solveCount" label="解题数" width="100" align="right" />
      <el-table-column prop="attackCount" label="攻击成功" width="110" align="right" />
      <el-table-column label="最近提交" width="170">
        <template #default="{ row }">{{ fmt(row.lastSubmit) }}</template>
      </el-table-column>
      <el-table-column label="趋势" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.delta != null" :class="row.delta >= 0 ? 'up' : 'down'">
            {{ row.delta >= 0 ? '▲' : '▼' }} {{ Math.abs(row.delta) }}
          </span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="filteredList.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getSituationRanking, listContests, rankApi, exportApi } from '@/api'
import { connectWs } from '@/utils/ws'
import { useUserStore } from '@/store/user'

const props = defineProps({ contestId: { type: [String, Number], default: null } })
const router = useRouter()
const user = useUserStore()

const TOP_OPTIONS = [10, 20, 50, 100, 200, 500]
const PALETTE = ['#409eff', '#ff5b5b', '#67c23a', '#e6a23c', '#909399', '#8e44ad',
  '#16a085', '#f39c12', '#2c3e50', '#c0392b']

const contestId = ref(props.contestId ? String(props.contestId) : null)
const contests = ref([])
const mode = ref('total')
const topN = ref(50)
const refreshMs = ref(1000)
const showMatrix = ref(true)
const stageIndex = ref(1)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const updatedAt = ref('')
const dataSource = ref('')

const rawList = ref([])
const challenges = ref([])
const cells = ref([])
const stages = ref([])
const fbList = ref([])
const timeline = ref(null)

const trendRef = ref(null)
let ws = null
let timer = null
let trendInst = null

const currentContest = computed(() =>
  contests.value.find((c) => String(c.id) === String(contestId.value)) || null)
const isAwd = computed(() => (currentContest.value?.type || '').toUpperCase().startsWith('AWD'))
const ctfMode = computed(() => !!contestId.value && !isAwd.value)
const canExport = computed(() => {
  if (!contestId.value) return false
  const st = (currentContest.value?.status || '').toUpperCase()
  return !!user.isAdmin || st === 'FINISHED' || st === 'ARCHIVED'
})
const sourceLabel = computed(() =>
  ({ REDIS: '实时榜', DB: '数据库快照', REPLAY: '阶段回放' }[dataSource.value] || '—'))
const boardTeamCount = computed(() =>
  currentContest.value?.teamCount || filteredList.value.length || 1)

const cellIndex = computed(() => {
  const map = new Map()
  cells.value.forEach((c) => map.set(`${c.teamId}_${c.questionId}`, c))
  return map
})

const normalized = computed(() => {
  let list
  if (ctfMode.value && mode.value === 'firstblood') {
    list = fbList.value.map((r, i) => ({
      id: r.questionId ?? i,
      rank: i + 1,
      questionTitle: r.questionTitle ?? r.title ?? `题目${i + 1}`,
      category: r.category,
      difficulty: r.difficulty,
      score: Number(r.score ?? 0),
      teamId: r.teamId,
      globalTeamId: r.globalTeamId,
      teamName: r.teamName ?? '—',
      logo: r.logo,
      solver: r.userNickname ?? r.solver ?? '—',
      solvedAt: r.solvedAt ?? r.firstBloodAt,
      solvedCount: r.solveTeamCount ?? 0
    }))
  } else {
    list = rawList.value.map((r, i) => ({
      id: r.teamId ?? r.id ?? i,
      teamId: r.teamId ?? r.id,
      globalTeamId: r.globalTeamId,
      rank: r.rank ?? i + 1,
      teamName: r.teamName ?? r.name ?? `战队${i + 1}`,
      logo: r.logo,
      orgName: r.orgName,
      score: Number(r.score ?? 0),
      solveCount: r.solveCount ?? r.solves ?? r.solved ?? 0,
      attackCount: r.attackCount ?? r.attacks ?? 0,
      firstBloods: r.firstBloodCount ?? r.firstBloods ?? (r.firstBlood ? 1 : 0),
      lastSubmit: r.lastSubmitTime ?? r.lastSubmit,
      lastSolveAt: r.lastSolveAt,
      delta: r.delta ?? r.rankDelta ?? null
    }))
    if (!ctfMode.value) {
      list.sort((a, b) => b.score - a.score)
      list = list.map((r, i) => ({ ...r, rank: i + 1 }))
    }
  }
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list
  return list.filter((r) =>
    (r.teamName || '').toLowerCase().includes(kw) ||
    (r.questionTitle || '').toLowerCase().includes(kw))
})

const filteredList = computed(() => normalized.value)
const podiumOrder = computed(() => {
  if (!ctfMode.value || mode.value !== 'total') return []
  const [a, b, c] = normalized.value.slice(0, 3)
  return [b, a, c].filter(Boolean)
})
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function medal(rank) {
  return rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : `#${rank}`
}
function shorten(t) {
  const s = String(t || '题目')
  return s.length > 8 ? `${s.slice(0, 8)}…` : s
}
function shortLabel(s) {
  return `${fmt(s.startAt).slice(5, 16)} → ${fmt(s.endAt).slice(5, 16)}`
}
function fmt(v) {
  if (!v) return '—'
  const text = String(v).replace('T', ' ')
  return text.length > 19 ? text.slice(0, 19) : text
}
function diffType(d) {
  return { EASY: 'success', MEDIUM: 'warning', HARD: 'danger' }[(d || '').toUpperCase()] || 'info'
}
function cellOf(row, questionId) {
  return cellIndex.value.get(`${row.teamId}_${questionId}`) || null
}
function bloodClass(row, questionId) {
  const c = cellOf(row, questionId)
  if (!c) return ''
  return c.bloodRank === 1 ? 'fb1' : c.bloodRank === 2 ? 'fb2' : c.bloodRank === 3 ? 'fb3' : 'normal'
}
function bloodText(row, questionId) {
  const c = cellOf(row, questionId)
  if (!c) return ''
  return c.bloodRank === 1 ? '1st' : c.bloodRank === 2 ? '2nd' : c.bloodRank === 3 ? '3rd' : '✓'
}
function rowClass({ row }) {
  return row.firstBloods ? 'row-first' : ''
}
function acceptRate(row) {
  const total = boardTeamCount.value || 1
  return Math.round(((row.solvedCount || 0) / total) * 100)
}
function fbRank(index) {
  return (page.value - 1) * pageSize.value + index + 1
}

async function loadContests() {
  try {
    const c = await listContests({ size: 200 })
    contests.value = Array.isArray(c) ? c : (c?.records || [])
    if (!contestId.value && contests.value.length) {
      const running = contests.value.find((x) => (x.status || '').toUpperCase() === 'RUNNING')
      contestId.value = String((running || contests.value[0]).id)
    }
  } catch (e) { /* 静默 */ }
}

function reload() {
  page.value = 1
  load()
}

async function load(asSilent = false) {
  if (!contestId.value) {
    rawList.value = []
    fbList.value = []
    return
  }
  if (!asSilent) loading.value = true
  try {
    if (!ctfMode.value) {
      const params = { limit: Math.min(topN.value, 500), contestId: contestId.value }
      const data = await getSituationRanking(params)
      rawList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
      dataSource.value = ''
    } else if (mode.value === 'firstblood') {
      fbList.value = (await rankApi.firstBlood(contestId.value)) || []
      dataSource.value = 'DB'
    } else {
      const params = { topN: Math.min(topN.value, 500) }
      const s = stages.value.find((x) => x.index === stageIndex.value)
      if (mode.value === 'stage' && s?.endAt) params.asOf = String(s.endAt).replace('T', ' ')
      const data = await rankApi.detail(contestId.value, params)
      rawList.value = data?.ranks || []
      challenges.value = data?.challenges || []
      cells.value = data?.cells || []
      dataSource.value = data?.source || ''
      if (Array.isArray(data?.stages) && data.stages.length) stages.value = data.stages
    }
    updatedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch (e) {
    if (!asSilent) {
      rawList.value = []
      fbList.value = []
    }
  } finally {
    loading.value = false
  }
}

async function loadStages() {
  if (!contestId.value || !ctfMode.value) {
    stages.value = []
    return
  }
  try {
    stages.value = (await rankApi.stages(contestId.value)) || []
    if (!stages.value.some((s) => s.index === stageIndex.value)) stageIndex.value = 1
  } catch (e) {
    stages.value = []
  }
}

async function loadTimeline() {
  if (!contestId.value || !ctfMode.value) {
    timeline.value = null
  } else {
    try {
      timeline.value = await rankApi.timeline(contestId.value, { traces: 10 })
    } catch (e) {
      timeline.value = null
    }
  }
  renderTrend()
}

function renderTrend() {
  const el = trendRef.value
  if (!el) return
  // 切换榜单会让 v-if 重建容器，旧实例绑在已卸载的节点上，需要重新初始化
  if (trendInst && trendInst.getDom() !== el) {
    trendInst.dispose()
    trendInst = null
  }
  if (!trendInst) trendInst = echarts.init(el)
  const t = timeline.value
  if (!t || !t.traces || !t.traces.length) {
    trendInst.setOption({
      title: { text: '暂无趋势数据', left: 'center', top: 'middle', textStyle: { color: '#909399', fontSize: 13 } }
    }, true)
    return
  }
  trendInst.setOption({
    tooltip: { trigger: 'axis' },
    legend: { type: 'scroll', top: 0, textStyle: { fontSize: 12 } },
    grid: { left: 48, right: 24, top: 34, bottom: 28 },
    xAxis: {
      type: 'category',
      data: t.labels || [],
      axisLabel: { fontSize: 11 }
    },
    yAxis: { type: 'value', name: '累计得分' },
    color: PALETTE,
    series: t.traces.map((tr, i) => ({
      name: tr.teamName,
      type: 'line',
      smooth: true,
      showSymbol: false,
      connectNulls: true,
      lineStyle: { width: 2 },
      itemStyle: { color: PALETTE[i % PALETTE.length] },
      data: (tr.scores || []).map((v) => Number(v) || 0)
    }))
  }, true)
}

function pickStage(index) {
  stageIndex.value = index
  reload()
}

function onModeChange() {
  page.value = 1
  load()
  nextTick(renderTrend)
}

async function onContestChange() {
  page.value = 1
  stageIndex.value = 1
  rawList.value = []
  fbList.value = []
  challenges.value = []
  cells.value = []
  await loadStages()
  await load()
  await loadTimeline()
  startTimer()
  subscribe()
}

async function doExport(ext) {
  if (!contestId.value) return
  try {
    const params = {
      contestId: contestId.value,
      board: mode.value,
      topN: Math.min(topN.value, 500),
      ...(mode.value === 'stage' ? { stage: stageIndex.value } : {})
    }
    const fn = ext === 'xlsx' ? exportApi.scoreboardXlsx : exportApi.scoreboardCsv
    const blob = await fn(params)
    const object = blob instanceof Blob ? blob : new Blob([blob])
    const url = URL.createObjectURL(object)
    const a = document.createElement('a')
    a.href = url
    a.download = `${currentContest.value?.name || 'contest'}-${mode.value}-${Date.now()}.${ext}`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('已开始下载')
  } catch (e) {
    ElMessage.error(`导出失败：${e?.message || '无权限或榜单为空'}`)
  }
}

function goTeam(t) {
  if (t.globalTeamId) router.push(`/team-home/${t.globalTeamId}`)
  else if (t.teamId || t.id) router.push(`/team/${t.teamId || t.id}`)
}

function subscribe() {
  try { ws?.close() } catch (e) { /* 忽略 */ }
  ws = null
  if (!contestId.value) return
  try {
    ws = connectWs(`/ws/contest/${contestId.value}`, (msg) => {
      const type = (msg.type || '').toUpperCase()
      if (type === 'SCORE_UPDATE' || type === 'FIRST_BLOOD') {
        load(true)
        loadTimeline()
        if (mode.value === 'stage') loadStages()
      }
    })
  } catch (e) { /* 静默 */ }
}

function startTimer() {
  stopTimer()
  if (!refreshMs.value || !contestId.value) return
  timer = setInterval(() => load(true), refreshMs.value)
}
function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

watch(refreshMs, startTimer)
watch(() => props.contestId, (v) => {
  if (v && String(v) !== contestId.value) {
    contestId.value = String(v)
    onContestChange()
  }
})

onMounted(async () => {
  await loadContests()
  await nextTick()
  if (trendRef.value) trendInst = echarts.init(trendRef.value)
  await loadStages()
  await load()
  await loadTimeline()
  subscribe()
  startTimer()
  window.addEventListener('resize', () => trendInst?.resize())
})

onBeforeUnmount(() => {
  stopTimer()
  try { ws?.close() } catch (e) { /* 忽略 */ }
  try { trendInst?.dispose() } catch (e) { /* 忽略 */ }
  trendInst = null
})
</script>

<style scoped>
.ranking-page { min-height: 100vh; background: #f7f8fa; padding: 20px 24px 32px; font-family: var(--csp-font-family); }
.rk-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 12px; }
.rk-header .left { display: flex; align-items: center; gap: 12px; }
.rk-title { margin: 0; font-size: 22px; font-weight: 600; color: var(--csp-title); }
.rk-sub { font-size: 12px; color: var(--csp-text-secondary); }
.rk-tools { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.stage-bar { margin-bottom: 14px; }
.stage-chips { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 6px; }
.stage-chip {
  display: flex; flex-direction: column; align-items: flex-start; gap: 2px;
  min-width: 150px; padding: 8px 12px; border-radius: 8px; cursor: pointer;
  border: 1px solid var(--csp-border); background: #fff; color: var(--csp-text);
  font-family: inherit; transition: all 0.2s;
}
.stage-chip:hover { border-color: var(--csp-primary); }
.stage-chip.active { border-color: var(--csp-primary); background: #ecf5ff; box-shadow: 0 2px 8px rgba(64, 158, 255, 0.18); }
.stage-chip.live { border-style: dashed; }
.sc-label { font-size: 13px; font-weight: 600; }
.sc-time { font-size: 11px; color: var(--csp-text-secondary); font-family: var(--csp-font-mono); }
.sc-count { font-size: 11px; color: var(--csp-primary); }
.stage-hint { font-size: 12px; color: var(--csp-text-secondary); margin-top: 4px; }

.card { background: #fff; border-radius: 10px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); padding: 14px 16px; }
.trend-card { margin-bottom: 16px; }
.card-title { font-size: 14px; font-weight: 600; color: var(--csp-title); margin-bottom: 6px; }
.trend-chart { height: 220px; }

.podium { display: flex; justify-content: center; align-items: flex-end; gap: 18px; margin: 10px 0 20px; }
.podium-item {
  width: 200px; border-radius: 10px; padding: 14px; text-align: center; cursor: pointer;
  background: #fff; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); transition: transform 0.2s;
  border-top: 4px solid var(--csp-border);
}
.podium-item:hover { transform: translateY(-4px); }
.podium-item.p1 { border-top-color: #ffb400; padding-top: 22px; }
.podium-item.p2 { border-top-color: #9aa5b1; }
.podium-item.p3 { border-top-color: #c97f3a; }
.p-rank { font-size: 20px; font-weight: 700; color: var(--csp-text-secondary); }
.p-logo { margin: 4px auto 6px; display: block; }
.p-name { font-size: 15px; font-weight: 600; color: var(--csp-title); margin: 4px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.p-score { font-size: 22px; font-weight: 700; color: var(--csp-primary); font-family: var(--csp-font-mono); }
.p-solves { font-size: 12px; color: var(--csp-text-secondary); margin-top: 4px; }
.p-fb { color: #ff5b5b; }

.rk-table { background: #fff; border-radius: 10px; overflow: hidden; }
.rank-cell { font-family: var(--csp-font-mono); font-weight: 600; color: var(--csp-text); }
.rank-cell.r1 { color: #ffb400; }
.rank-cell.r2 { color: #9aa5b1; }
.rank-cell.r3 { color: #c97f3a; }
.team-cell { display: flex; align-items: center; gap: 8px; }
.team-main { display: flex; flex-direction: column; line-height: 1.3; }
.team-sub { display: flex; align-items: center; gap: 6px; }
.org { font-size: 11px; color: var(--csp-text-secondary); }
.team-link { color: var(--csp-primary); cursor: pointer; }
.team-link:hover { text-decoration: underline; }
.q-cell { display: flex; flex-direction: column; gap: 4px; }
.q-title { font-weight: 600; color: var(--csp-title); }
.q-sub { display: flex; gap: 6px; }
.rate { font-size: 11px; color: var(--csp-text-secondary); margin-top: 2px; }

.solve-cell {
  display: inline-block; min-width: 34px; padding: 2px 6px; border-radius: 4px;
  font-size: 12px; font-weight: 600; text-align: center; font-family: var(--csp-font-mono);
}
.solve-cell.fb1 { background: #ff5b5b; color: #fff; }
.solve-cell.fb2 { background: #ffd8a8; color: #8a4b00; }
.solve-cell.fb3 { background: #ffeaa6; color: #7a6400; }
.solve-cell.normal { background: #e8f3ff; color: var(--csp-primary); }
.up { color: var(--csp-success); font-size: 12px; }
.down { color: var(--csp-danger); font-size: 12px; }
.muted { color: var(--csp-placeholder); }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
:deep(.row-first td) { background: #fff8e6 !important; }
</style>
