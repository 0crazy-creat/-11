<template>
  <div class="portal-page">
    <div class="csp-page-head">
      <h2 class="csp-h2">赛事大厅</h2>
      <p class="csp-text-secondary">选择赛事报名参赛，比赛开始后可进入对应赛制入口</p>
    </div>

    <!-- 角色-赛制能力提示条（多角色协作矩阵在前端可见） -->
    <el-alert
      v-if="matrixHint"
      class="role-hint"
      type="info"
      :closable="false"
      show-icon
      :title="matrixHint"
    />

    <el-tabs v-model="tab" class="mt16">
      <el-tab-pane label="全部赛事" name="all" />
      <el-tab-pane label="进行中" name="RUNNING" />
      <el-tab-pane label="可报名" name="SIGNUP" />
      <el-tab-pane label="已结束" name="FINISHED" />
    </el-tabs>

    <el-empty v-if="!loading && !filtered.length" description="暂无可查看的赛事" />
    <div v-loading="loading" class="grid">
      <el-card
        v-for="c in filtered"
        :key="c.id"
        shadow="hover"
        class="card"
        :class="`type-${(c.type || '').toLowerCase()}`"
        :style="{ '--type-color': typeMeta(c.type).color, '--type-bg': typeMeta(c.type).bg }"
        @click="open(c)"
      >
        <div class="row1">
          <span class="type-badge">{{ typeMeta(c.type).label }}</span>
          <span class="csp-status" :class="statusClass(c.status)">{{ statusText(c.status) }}</span>
        </div>
        <h3 class="name">{{ c.name }}</h3>
        <p class="code">{{ c.code }}</p>
        <p class="desc">{{ c.description || '—' }}</p>
        <div class="foot">
          <span>上限 {{ c.maxPlayers }} 人</span>
          <el-button type="primary" link @click.stop="open(c)">{{ entryText(c) }}</el-button>
        </div>
        <p class="fmt-hint">{{ typeMeta(c.type).hint }}</p>
        <div class="caps">
          <el-tag v-for="cap in capabilities(c)" :key="cap" size="small" effect="plain">{{ cap }}</el-tag>
          <el-tag v-if="typeMeta(c.type).dynamicTargets" size="small" type="warning" effect="plain">动态靶机</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { contestApi } from '@/api'
import { useUserStore } from '@/store/user'
import {
  CONTEST_TYPES, getContestType, contestEntryRoute, roleOf, canDo, ROLE_CAPS
} from '@/constants/contestType'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const list = ref([])
const tab = ref('all')
const role = computed(() => roleOf(user.roles))

const matrixHint = computed(() => {
  const r = role.value
  if (r === 'ADMIN' || r === 'SUPER_ADMIN' || r === 'PLATFORM_ADMIN') return '当前角色：平台管理员 · 可见全部赛制，免报名进入，可管理赛事与审核题解'
  if (r === 'JUDGE') return '当前角色：裁判 · 可见全部赛制，免报名，可只读查看全部靶机并审核题解'
  if (r === 'TEACHER') return '当前角色：教师 · 可见全部赛制，免报名，可查看本赛事靶机并审核题解'
  if (r === 'OBSERVER') return '当前角色：观察者 · 仅可查看公开赛事与实时分数（只读）'
  return '当前角色：选手 · 仅可见已报名赛事，需报名后进入答题/启动容器'
})

const filtered = computed(() => {
  const r = role.value
  let arr = list.value
  // OBSERVER 只看公开赛事；PLAYER 只看已报名（后端已按角色过滤，这里兜底）
  if (r === 'OBSERVER') arr = arr.filter(c => c.public !== false && c.visibility !== 'PRIVATE')
  if (r === 'PLAYER') arr = arr.filter(c => c.joined === true || c.joined === undefined)
  return tab.value === 'all' ? arr : arr.filter(c => c.status === tab.value)
})

function statusText(s) {
  return { DRAFT: '筹备中', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档' }[s] || s
}
function statusClass(s) {
  return { RUNNING: 'is-running', SIGNUP: 'is-signup', FINISHED: 'is-done', ARCHIVED: 'is-done' }[s] || 'is-idle'
}

/* ---- 六赛制唯一来源：constants/contestType.js，页面不再硬编码 ---- */
function typeMeta(t) { return getContestType(t) }
const FORMAT_ENTRY = {
  CTF: '开始解题 →', AWD: '进入攻防大屏 →', AWDP: '进入攻防大屏 →',
  THEORY: '开始答题 →', SCENARIO: '进入场景闯关 →', CFS: '进入分层渗透 →'
}
function entryText(c) {
  const r = role.value
  if (r === 'OBSERVER') return '查看实时分数 →'
  if (r === 'PLAYER') return c.joined ? (FORMAT_ENTRY[String(c.type||'').toUpperCase()] || '进入赛事 →') : '报名参赛 →'
  return FORMAT_ENTRY[String(c.type||'').toUpperCase()] || '进入赛事 →'
}
function capabilities(c) {
  const r = role.value
  const caps = []
  if (canDo(r, 'solve', c.type)) caps.push(r === 'PLAYER' ? '需报名做题' : '免报名做题')
  if (canDo(r, 'targets', c.type)) caps.push(r === 'JUDGE' || r === 'OBSERVER' ? '靶机只读' : '靶机可见')
  if (canDo(r, 'review', c.type)) caps.push('审核题解')
  if (canDo(r, 'manage', c.type)) caps.push('管理赛事')
  if (!caps.length) caps.push('只读')
  return caps
}

async function load() {
  loading.value = true
  try {
    const data = await contestApi.list({ page: 1, size: 100 })
    list.value = data?.records || []
  } finally {
    loading.value = false
  }
}
function open(c) {
  // 统一走赛制注册表路由，AWD/AWDP → /awd/screen/:id，其余 → /portal/**/:id
  router.push(contestEntryRoute(c))
}
onMounted(load)
</script>

<style scoped>
.portal-page { padding: 0 }
.mt16 { margin-top: 16px }
.role-hint { margin-top: 12px; border-radius: 8px }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px }
.card { cursor: pointer; border-radius: 12px; transition: transform .15s; border-top: 3px solid var(--type-color, var(--csp-primary)) }
.card:hover { transform: translateY(-2px) }
.row1 { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px }
.type-badge { font-size: 12px; padding: 2px 8px; border-radius: 4px; background: var(--type-bg, var(--csp-primary-light)); color: var(--type-color, var(--csp-primary)); font-weight: 600 }
.csp-status { font-size: 12px; color: #c9cdd4 }
.csp-status.is-running { color: #00b42a; font-weight: 600 }
.csp-status.is-signup { color: #ff7d00; font-weight: 600 }
.name { font-size: 16px; font-weight: 600; color: var(--csp-title); margin: 0 0 4px }
.code { font-size: 12px; color: #86909c; margin: 0 0 8px }
.desc { font-size: 13px; color: #4e5969; min-height: 36px; margin: 0 0 12px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical }
.foot { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #86909c }
.caps { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px }
.fmt-hint { font-size: 12px; color: #86909c; margin: 4px 0 0; line-height: 1.5 }
/* 六赛制卡片分流：每赛制专属渐变头 + 图标色，视觉上一眼区分 */
.card.type-ctf      { background: linear-gradient(180deg,#f5f8ff 0%,#fff 42%) }
.card.type-awd      { background: linear-gradient(180deg,#fff2f0 0%,#fff 42%) }
.card.type-awdp     { background: linear-gradient(180deg,#fff7ec 0%,#fff 42%) }
.card.type-theory   { background: linear-gradient(180deg,#f0fff2 0%,#fff 42%) }
.card.type-scenario { background: linear-gradient(180deg,#f7f4ff 0%,#fff 42%) }
.card.type-cfs      { background: linear-gradient(180deg,#eef2ff 0%,#fff 42%) }
.card.type-awd .type-badge, .card.type-awdp .type-badge { box-shadow: 0 0 0 1px rgba(245,63,63,.15) }
.card.type-cfs { border-top-width: 4px }
</style>