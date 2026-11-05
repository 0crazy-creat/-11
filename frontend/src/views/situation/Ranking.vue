<template>
  <div class="ranking-page">
    <header class="rk-header">
      <div class="left">
        <h1 class="rk-title">实时排行榜</h1>
        <span class="rk-sub" v-if="updatedAt">更新于 {{ updatedAt }}</span>
      </div>
      <div class="rk-tools">
        <el-select v-model="contestId" size="small" style="width: 200px" placeholder="全部赛事" clearable @change="load">
          <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-radio-group v-model="sortBy" size="small">
          <el-radio-button value="score">按得分</el-radio-button>
          <el-radio-button value="solves">按解题</el-radio-button>
        </el-radio-group>
        <el-input v-model="keyword" size="small" placeholder="搜索战队" clearable style="width: 160px" />
        <el-button size="small" :icon="Refresh" circle @click="load" />
      </div>
    </header>

    <section class="podium" v-if="podiumOrder.length">
      <div v-for="t in podiumOrder" :key="t.id" class="podium-item" :class="`p${t.rank}`" @click="goTeam(t)">
        <div class="p-rank">{{ t.rank }}</div>
        <div class="p-name">{{ t.teamName }}</div>
        <div class="p-score">{{ t.score }}</div>
        <div class="p-solves">{{ t.solveCount ?? 0 }} 解</div>
      </div>
    </section>

    <el-table :data="pagedList" stripe size="default" class="rk-table" v-loading="loading">
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
      <el-table-column prop="score" label="得分" width="110" align="right" sortable />
      <el-table-column prop="solveCount" label="解题数" width="100" align="right" />
      <el-table-column prop="attackCount" label="攻击成功" width="110" align="right" />
      <el-table-column prop="lastSubmit" label="最近提交" width="170" />
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getSituationRanking, listContests } from '@/api'
import { connectWs } from '@/utils/ws'

const props = defineProps({ contestId: { type: [String, Number], default: null } })
const router = useRouter()

const contestId = ref(props.contestId ? String(props.contestId) : null)
const contests = ref([])
const rawList = ref([])
const keyword = ref('')
const sortBy = ref('score')
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const updatedAt = ref('')
let ws = null

const normalized = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  let list = rawList.value.map((r, i) => ({
    id: r.teamId ?? r.id ?? i,
    teamName: r.teamName ?? r.name ?? `战队${i + 1}`,
    score: Number(r.score ?? 0),
    solveCount: r.solveCount ?? r.solves ?? 0,
    attackCount: r.attackCount ?? r.attacks ?? 0,
    lastSubmit: r.lastSubmitTime ?? r.lastSubmit ?? '—',
    delta: r.delta ?? r.rankDelta ?? null
  }))
  if (kw) list = list.filter((r) => r.teamName.toLowerCase().includes(kw))
  list.sort((a, b) => (sortBy.value === 'solves' ? b.solveCount - a.solveCount : b.score - a.score))
  return list.map((r, i) => ({ ...r, rank: i + 1 }))
})

const filteredList = computed(() => normalized.value)
const podiumOrder = computed(() => {
  const [a, b, c] = normalized.value.slice(0, 3)
  return [b, a, c].filter(Boolean)
})
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

async function loadContests() {
  try {
    const c = await listContests({ size: 100 })
    contests.value = Array.isArray(c) ? c : (c?.records || [])
  } catch (e) { /* 静默 */ }
}

async function load() {
  loading.value = true
  try {
    const params = { limit: 500 }
    if (contestId.value) params.contestId = contestId.value
    const data = await getSituationRanking(params)
    rawList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
    updatedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch (e) {
    rawList.value = []
  } finally {
    loading.value = false
  }
}

function goTeam(t) {
  if (t.id) router.push(`/team/${t.id}`)
}

function subscribe() {
  try {
    const path = contestId.value ? `/ws/contest/${contestId.value}` : '/ws/contest/0'
    ws = connectWs(path, (msg) => {
      if ((msg.type || '').toUpperCase() === 'SCORE_UPDATE') load()
    })
  } catch (e) { /* 静默 */ }
}

onMounted(async () => {
  await loadContests()
  await load()
  subscribe()
})

onBeforeUnmount(() => ws?.close())
</script>

<style scoped>
.ranking-page { min-height: 100vh; background: #f7f8fa; padding: 20px 24px 32px; font-family: var(--csp-font-family); }
.rk-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.rk-header .left { display: flex; align-items: baseline; gap: 12px; }
.rk-title { margin: 0; font-size: 22px; font-weight: 600; color: var(--csp-title); }
.rk-sub { font-size: 12px; color: var(--csp-text-secondary); }
.rk-tools { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.podium { display: flex; justify-content: center; align-items: flex-end; gap: 18px; margin: 10px 0 24px; }
.podium-item {
  width: 200px; border-radius: 10px; padding: 16px; text-align: center; cursor: pointer;
  background: #fff; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); transition: transform 0.2s;
  border-top: 4px solid var(--csp-border);
}
.podium-item:hover { transform: translateY(-4px); }
.podium-item.p1 { border-top-color: #ffb400; padding-top: 26px; }
.podium-item.p2 { border-top-color: #9aa5b1; }
.podium-item.p3 { border-top-color: #c97f3a; }
.p-rank { font-size: 26px; font-weight: 700; color: var(--csp-text-secondary); font-family: var(--csp-font-mono); }
.podium-item.p1 .p-rank { color: #ffb400; }
.p-name { font-size: 15px; font-weight: 600; color: var(--csp-title); margin: 6px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.p-score { font-size: 22px; font-weight: 700; color: var(--csp-primary); font-family: var(--csp-font-mono); }
.p-solves { font-size: 12px; color: var(--csp-text-secondary); margin-top: 4px; }

.rk-table { background: #fff; border-radius: 10px; overflow: hidden; }
.rank-cell { font-family: var(--csp-font-mono); font-weight: 600; color: var(--csp-text); }
.rank-cell.r1 { color: #ffb400; }
.rank-cell.r2 { color: #9aa5b1; }
.rank-cell.r3 { color: #c97f3a; }
.team-link { color: var(--csp-primary); cursor: pointer; }
.team-link:hover { text-decoration: underline; }
.up { color: var(--csp-success); font-size: 12px; }
.down { color: var(--csp-danger); font-size: 12px; }
.muted { color: var(--csp-placeholder); }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
