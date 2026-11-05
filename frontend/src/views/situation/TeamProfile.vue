<template>
  <div class="tp-page" v-loading="loading">
    <section class="tp-head card">
      <el-avatar :size="76" :src="data.logo" class="tp-logo">{{ (data.name || '?').charAt(0) }}</el-avatar>
      <div class="tp-head-main">
        <div class="tp-title-row">
          <h1 class="tp-name">{{ data.name || '战队主页' }}</h1>
          <el-tag v-if="data.myRole === 'CAPTAIN'" size="small" type="success" effect="dark">队长</el-tag>
          <el-tag v-else-if="data.myRole === 'MEMBER'" size="small" effect="dark">队员</el-tag>
        </div>
        <p class="tp-slogan" v-if="data.slogan">{{ data.slogan }}</p>
        <p class="tp-desc" v-if="data.description">{{ data.description }}</p>
        <div class="tp-meta">
          <span>队长：{{ data.captainName || '—' }}</span>
          <span>队员：{{ data.members || 0 }} / {{ data.maxMembers || 0 }}</span>
          <span>建立于 {{ fmt(data.createdAt) }}</span>
        </div>
      </div>
      <div class="tp-head-acts">
        <el-button size="small" :icon="Refresh" circle @click="load" />
        <el-button size="small" @click="goConsole">管理战队</el-button>
      </div>
    </section>

    <section class="tp-stats">
      <div class="stat card">
        <div class="stat-v">{{ s.contests || 0 }}</div>
        <div class="stat-l">参赛场次</div>
        <div class="stat-x">进行中 {{ s.runningContests || 0 }} 场</div>
      </div>
      <div class="stat card">
        <div class="stat-v">{{ s.totalScore || 0 }}</div>
        <div class="stat-l">累计得分</div>
        <div class="stat-x">跨全部赛事</div>
      </div>
      <div class="stat card">
        <div class="stat-v">{{ s.totalSolves || 0 }}</div>
        <div class="stat-l">解题总数</div>
        <div class="stat-x">按题目去重</div>
      </div>
      <div class="stat card">
        <div class="stat-v fb">{{ s.totalFirstBloods || 0 }}</div>
        <div class="stat-l">一血次数</div>
        <div class="stat-x">🔥 首杀</div>
      </div>
      <div class="stat card">
        <div class="stat-v">{{ s.bestRank ? `#${s.bestRank}` : '—' }}</div>
        <div class="stat-l">最高排名</div>
        <div class="stat-x ellipsis">{{ s.bestContest || '—' }}</div>
      </div>
      <div class="stat card">
        <div class="stat-v">{{ s.writeups || 0 }}</div>
        <div class="stat-l">题解数</div>
        <div class="stat-x">已通过审核</div>
      </div>
    </section>

    <el-tabs v-model="tab" class="tp-tabs card">
      <el-tab-pane label="参赛历史" name="history">
        <el-table :data="data.history || []" size="default" stripe border>
          <el-table-column label="赛事" min-width="220">
            <template #default="{ row }">
              <div class="c-cell">
                <a class="link" @click="goContest(row)">{{ row.contestName }}</a>
                <span class="sub">{{ row.contestCode }} · {{ row.organizer || '—' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.type || '—' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="赛事状态" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="statusType(row.contestStatus)">{{ statusText(row.contestStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="报名时间" width="170">
            <template #default="{ row }">{{ fmt(row.startAt) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="170">
            <template #default="{ row }">{{ fmt(row.endAt) }}</template>
          </el-table-column>
          <el-table-column label="排名" width="90" align="center">
            <template #default="{ row }">
              <span class="rank-v" :class="`r${row.rank}`">{{ row.rank ? medal(row.rank) : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="100" align="right" />
          <el-table-column prop="solved" label="解题" width="80" align="right" />
          <el-table-column label="一血" width="80" align="right">
            <template #default="{ row }">
              <span v-if="row.firstBloods" class="fb-text">🔥 {{ row.firstBloods }}</span>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="goBoard(row)">赛事榜</el-button>
              <el-button link type="primary" size="small" @click="goMatch(row)">本场详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <p class="empty" v-if="!(data.history || []).length">该战队暂无参赛记录</p>
      </el-tab-pane>

      <el-tab-pane label="获奖记录" name="awards">
        <div class="award-list">
          <div v-for="(a, i) in data.awards || []" :key="i" class="award card">
            <div class="award-medal">{{ medal(a.rank) }}</div>
            <div class="award-main">
              <div class="award-name">{{ a.contestName }}</div>
              <div class="award-sub">
                {{ a.level || '优胜' }} · 第 {{ a.rank }} 名 · {{ a.type || '—' }} · 得分 {{ a.score || 0 }}
              </div>
            </div>
            <div class="award-time">{{ fmt(a.endAt).slice(0, 10) }}</div>
          </div>
        </div>
        <p class="empty" v-if="!(data.awards || []).length">暂无获奖记录（结算后按最终名次自动生成前三）</p>
      </el-tab-pane>

      <el-tab-pane label="能力画像" name="ability">
        <div class="ab-grid">
          <div class="ab-left">
            <div class="ab-title">战队整体能力画像</div>
            <div ref="radarRef" class="radar"></div>
          </div>
          <div class="ab-right">
            <div class="ab-title">难度分布</div>
            <div v-for="(v, k) in difficulties" :key="k" class="diff-row">
              <span class="diff-k">{{ diffText(k) }}</span>
              <el-progress :percentage="pct(v, maxDiff)" :stroke-width="12" :show-text="false" />
              <span class="diff-v">{{ v }}</span>
            </div>
            <p class="empty sm" v-if="!Object.keys(difficulties).length">暂无解题数据</p>
            <div class="ab-title mt">分类解题数</div>
            <div class="cat-tags">
              <el-tag v-for="(v, k) in abilities" :key="k" size="small" effect="plain" class="cat-tag">
                {{ k }} · {{ v }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="ab-title mt">队员画像</div>
        <el-table :data="data.roster || []" size="default" stripe border @row-click="pickMember">
          <el-table-column label="队员" min-width="180">
            <template #default="{ row }">
              <div class="member-cell">
                <el-avatar :size="28" :src="row.avatar">{{ (row.nickname || row.username || '?').charAt(0) }}</el-avatar>
                <div class="team-main">
                  <span class="m-name">{{ row.nickname || row.username }}</span>
                  <span class="m-sub">{{ row.username }}<template v-if="row.orgName"> · {{ row.orgName }}</template></span>
                </div>
                <el-tag v-if="row.role === 'CAPTAIN'" size="small" type="warning" effect="dark">队长</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="contests" label="参与赛事" width="100" align="right" />
          <el-table-column prop="solves" label="解题数" width="100" align="right" />
          <el-table-column prop="score" label="得分" width="110" align="right" />
          <el-table-column label="一血" width="90" align="right">
            <template #default="{ row }">
              <span v-if="row.firstBloods" class="fb-text">🔥 {{ row.firstBloods }}</span>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="最近解题" width="170">
            <template #default="{ row }">{{ fmt(row.lastSolveAt) }}</template>
          </el-table-column>
          <el-table-column label="擅长方向" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="(v, k) in topCats(row.categories)" :key="k" size="small" effect="plain" class="cat-tag">
                {{ k }} {{ v }}
              </el-tag>
              <span v-if="!Object.keys(row.categories || {}).length" class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="画像" width="90" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click.stop="pickMember(row)">雷达图</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="题解集合" name="writeups">
        <div v-for="w in data.writeups || []" :key="w.id" class="wp card" @click="openWriteup(w.id)">
          <div class="wp-head">
            <span class="wp-title">{{ w.title }}</span>
            <el-tag size="small" effect="plain">{{ w.category || '—' }}</el-tag>
          </div>
          <p class="wp-meta">
            {{ w.author || '匿名' }} · {{ w.contestName || '—' }} · 题目 {{ w.questionTitle || '—' }} ·
            浏览 {{ w.viewCount || 0 }} · 点赞 {{ w.likeCount || 0 }} · {{ fmt(w.createdAt).slice(0, 16) }}
          </p>
        </div>
        <p class="empty" v-if="!(data.writeups || []).length">该战队暂无已审核通过的题解</p>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="wpVisible" :title="wpCurrent.title || '题解'" width="720px">
      <p class="wp-meta">{{ wpCurrent.author }} · {{ fmt(wpCurrent.createdAt) }}</p>
      <div class="wp-content">{{ wpCurrent.content }}</div>
      <template #footer>
        <el-button @click="wpVisible = false">关闭</el-button>
        <el-button type="primary" @click="likeWriteupNow">点赞 {{ wpCurrent.likeCount || 0 }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { teamApi, getWriteup, likeWriteup } from '@/api'

const props = defineProps({ teamId: { type: [String, Number], default: null } })
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tab = ref('history')
const data = ref({})
const memberFocus = ref(null)
const radarRef = ref(null)
let radarInst = null

const wpVisible = ref(false)
const wpCurrent = ref({})

const s = computed(() => data.value.stats || {})
const abilities = computed(() => data.value.abilities || {})
const difficulties = computed(() => data.value.difficulties || {})
const maxDiff = computed(() => Math.max(1, ...Object.values(difficulties.value)))

function fmt(v) {
  if (!v) return '—'
  const text = String(v).replace('T', ' ')
  return text.length > 19 ? text.slice(0, 19) : text
}
function medal(rank) {
  return rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : `#${rank}`
}
function pct(v, total) {
  return Math.round((v / (total || 1)) * 100)
}
function diffText(d) {
  return { EASY: '简单', MEDIUM: '中等', HARD: '困难' }[(d || '').toUpperCase()] || d
}
function statusText(st) {
  return { DRAFT: '草稿', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档' }[(st || '').toUpperCase()] || (st || '—')
}
function statusType(st) {
  const k = (st || '').toUpperCase()
  if (k === 'RUNNING') return 'success'
  if (k === 'FINISHED' || k === 'ARCHIVED') return 'info'
  if (k === 'SIGNUP') return 'warning'
  return ''
}
function topCats(cats) {
  const entries = Object.entries(cats || {}).sort((a, b) => b[1] - a[1]).slice(0, 4)
  return Object.fromEntries(entries)
}

async function load() {
  const id = props.teamId || route.params.teamId
  if (!id) return
  loading.value = true
  try {
    data.value = (await teamApi.profile(id)) || {}
    memberFocus.value = null
    await nextTick()
    renderRadar()
  } catch (e) {
    ElMessage.error(e?.message || '战队主页加载失败')
    data.value = {}
  } finally {
    loading.value = false
  }
}

function pickMember(row) {
  memberFocus.value = row
  renderRadar()
}

function renderRadar() {
  if (!radarRef.value) return
  if (!radarInst) radarInst = echarts.init(radarRef.value)
  const src = memberFocus.value?.categories && Object.keys(memberFocus.value.categories).length
    ? memberFocus.value.categories
    : abilities.value
  const entries = Object.entries(src || {}).sort((a, b) => b[1] - a[1]).slice(0, 8)
  const name = memberFocus.value
    ? `${memberFocus.value.nickname || memberFocus.value.username} 能力画像`
    : `${data.value.name || '战队'} 能力画像`
  if (!entries.length) {
    radarInst.setOption({
      title: { text: '暂无解题数据', left: 'center', top: 'middle', textStyle: { color: '#909399', fontSize: 13 } }
    }, true)
    return
  }
  const max = Math.max(...entries.map((e) => e[1]), 1)
  radarInst.setOption({
    title: { text: name, left: 'center', top: 4, textStyle: { fontSize: 13 } },
    tooltip: {},
    radar: {
      indicator: entries.map(([k]) => ({ name: k, max })),
      radius: '64%',
      center: ['50%', '56%'],
      axisName: { color: '#4e5969', fontSize: 11 },
      splitArea: { areaStyle: { color: ['#f7fbff', '#eef4fb'] } }
    },
    series: [{
      type: 'radar',
      symbolSize: 5,
      areaStyle: { color: 'rgba(64, 158, 255, 0.28)' },
      lineStyle: { color: '#409eff' },
      itemStyle: { color: '#409eff' },
      data: [{ value: entries.map(([, v]) => v), name }]
    }]
  }, true)
}

function goContest(row) {
  if (row.contestId) router.push(`/portal/contests/${row.contestId}`)
}
function goBoard(row) {
  if (row.contestId) router.push(`/rank/${row.contestId}`)
}
function goMatch(row) {
  if (row.contestTeamId) router.push(`/team/${row.contestTeamId}`)
}
function goConsole() {
  router.push('/portal/my-team')
}

async function openWriteup(id) {
  try {
    wpCurrent.value = await getWriteup(id)
    wpVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '题解不可见')
  }
}
async function likeWriteupNow() {
  try {
    await likeWriteup(wpCurrent.value.id)
    ElMessage.success('已点赞')
    wpCurrent.value = { ...wpCurrent.value, likeCount: (wpCurrent.value.likeCount || 0) + 1 }
  } catch (e) { /* 静默 */ }
}

watch(() => props.teamId, load)
watch(tab, async (t) => {
  if (t === 'ability') {
    await nextTick()
    renderRadar()
    radarInst?.resize()
  }
})

onMounted(async () => {
  await load()
  window.addEventListener('resize', () => radarInst?.resize())
})
onBeforeUnmount(() => {
  try { radarInst?.dispose() } catch (e) { /* 忽略 */ }
  radarInst = null
})
</script>

<style scoped>
.tp-page { min-height: 100vh; background: #f7f8fa; padding: 20px 24px 32px; font-family: var(--csp-font-family); }
.card { background: #fff; border-radius: 10px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); padding: 16px; }

.tp-head { display: flex; gap: 18px; align-items: flex-start; margin-bottom: 16px; }
.tp-logo { flex: none; }
.tp-head-main { flex: 1; min-width: 0; }
.tp-title-row { display: flex; align-items: center; gap: 10px; }
.tp-name { margin: 0; font-size: 24px; font-weight: 700; color: var(--csp-title); }
.tp-slogan { margin: 6px 0 0; color: var(--csp-primary); font-size: 14px; }
.tp-desc { margin: 6px 0 0; color: var(--csp-text-secondary); font-size: 13px; line-height: 1.7; }
.tp-meta { display: flex; gap: 18px; margin-top: 10px; font-size: 12px; color: var(--csp-text-secondary); flex-wrap: wrap; }
.tp-head-acts { display: flex; flex-direction: column; gap: 8px; }

.tp-stats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-bottom: 16px; }
.stat { text-align: center; padding: 14px 8px; }
.stat-v { font-size: 24px; font-weight: 700; color: var(--csp-primary); font-family: var(--csp-font-mono); }
.stat-v.fb { color: #ff5b5b; }
.stat-l { font-size: 13px; color: var(--csp-title); margin-top: 4px; }
.stat-x { font-size: 11px; color: var(--csp-text-secondary); margin-top: 2px; }
.ellipsis, .c-cell .sub { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.tp-tabs { padding: 6px 16px 16px; }
.c-cell { display: flex; flex-direction: column; line-height: 1.4; min-width: 0; }
.c-cell .sub { font-size: 11px; color: var(--csp-text-secondary); }
.link { color: var(--csp-primary); cursor: pointer; }
.link:hover { text-decoration: underline; }
.rank-v { font-family: var(--csp-font-mono); font-weight: 700; }
.rank-v.r1 { color: #ffb400; }
.rank-v.r2 { color: #9aa5b1; }
.rank-v.r3 { color: #c97f3a; }
.fb-text { color: #ff5b5b; font-weight: 600; }
.muted { color: var(--csp-placeholder); }

.award-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.award { display: flex; align-items: center; gap: 12px; }
.award-medal { font-size: 26px; }
.award-main { flex: 1; min-width: 0; }
.award-name { font-size: 15px; font-weight: 600; color: var(--csp-title); }
.award-sub { font-size: 12px; color: var(--csp-text-secondary); margin-top: 4px; }
.award-time { font-size: 12px; color: var(--csp-text-secondary); font-family: var(--csp-font-mono); }

.ab-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.ab-title { font-size: 14px; font-weight: 600; color: var(--csp-title); margin-bottom: 8px; }
.mt { margin-top: 20px; }
.radar { height: 300px; }
.diff-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.diff-k { width: 44px; font-size: 12px; color: var(--csp-text-secondary); }
.diff-v { width: 34px; text-align: right; font-size: 12px; font-family: var(--csp-font-mono); }
.diff-row :deep(.el-progress) { flex: 1; }
.cat-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.cat-tag { margin: 0 !important; }

.member-cell { display: flex; align-items: center; gap: 8px; }
.team-main { display: flex; flex-direction: column; line-height: 1.3; }
.m-name { font-weight: 600; color: var(--csp-title); }
.m-sub { font-size: 11px; color: var(--csp-text-secondary); }

.wp { cursor: pointer; margin-bottom: 12px; transition: all .2s; }
.wp:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(41, 95, 255, .12); }
.wp-head { display: flex; align-items: center; gap: 10px; }
.wp-title { font-size: 15px; font-weight: 600; color: var(--csp-title); }
.wp-meta { margin: 8px 0 0; font-size: 12px; color: #86909c; }
.wp-content { font-size: 14px; color: #1d2129; line-height: 1.9; white-space: pre-wrap; margin-top: 12px; max-height: 420px; overflow: auto; }

.empty { text-align: center; color: var(--csp-text-secondary); padding: 28px 0; font-size: 13px; }
.empty.sm { padding: 10px 0; }
</style>
