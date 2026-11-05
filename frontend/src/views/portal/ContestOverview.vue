<template>
  <div class="contest-overview" v-loading="loading">
    <div class="csp-page-head">
      <div>
        <h2 class="csp-h2">{{ contest.name || '赛事详情' }}</h2>
        <p class="csp-text-secondary head-tags">
          <el-tag size="small" :color="typeMeta.bg" :style="{ color: typeMeta.color, borderColor: typeMeta.color }">
            {{ typeMeta.label }}
          </el-tag>
          <el-tag size="small" :type="statusTagType">{{ statusLabel }}</el-tag>
          <el-tag size="small" effect="plain">{{ contest.mode === 'INDIVIDUAL' ? '个人赛' : '团队赛' }}</el-tag>
          <span v-if="contest.code" class="code">· {{ contest.code }}</span>
        </p>
      </div>
      <div class="head-actions">
        <template v-if="running">
          <el-button v-if="myTeam || isAdmin" type="success" @click="enter">
            <el-icon><VideoPlay /></el-icon>
            进入赛场
          </el-button>
          <el-tag v-else type="info" effect="plain" size="large">报名已截止</el-tag>
        </template>
        <template v-else-if="ended">
          <el-tag type="info" effect="plain" size="large">赛事已结束</el-tag>
        </template>
        <template v-else>
          <el-button v-if="showEnroll" type="primary" @click="enrollVisible = true">报名参赛</el-button>
          <el-tag v-else-if="myTeam" type="success" effect="light">已报名：{{ myTeam.name }}</el-tag>
        </template>
        <el-button v-if="isAwd" type="primary" plain @click="$router.push(`/awd/screen/${contestId}`)">攻防大屏</el-button>
        <el-button @click="$router.push(`/rank/${contestId}`)">排行榜</el-button>
      </div>
    </div>

    <el-alert v-if="errMsg" :title="errMsg" type="warning" show-icon :closable="false" class="mb16" />

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">赛事信息</span></template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="赛事名称" :span="2">{{ contest.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="赛事类型">{{ typeMeta.label }}</el-descriptions-item>
            <el-descriptions-item label="参赛形式">{{ contest.mode === 'INDIVIDUAL' ? '个人赛' : '团队赛' }}</el-descriptions-item>
            <el-descriptions-item label="报名时间" :span="2">{{ range(contest.signupStart, contest.signupEnd) }}</el-descriptions-item>
            <el-descriptions-item label="比赛时间" :span="2">{{ range(contest.startAt, contest.endAt) }}</el-descriptions-item>
            <el-descriptions-item label="参赛队伍">{{ contest.teamCount ?? 0 }} 支</el-descriptions-item>
            <el-descriptions-item label="人数上限">{{ contest.maxPlayers || '不限' }}</el-descriptions-item>
            <el-descriptions-item label="主办方" :span="2">{{ contest.organizer || '—' }}</el-descriptions-item>
            <el-descriptions-item label="赛事描述" :span="2">
              <div class="desc">{{ contest.description || '—' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="showChallenges" shadow="never" class="panel mt16">
          <template #header>
            <div class="panel-header-row">
              <span class="panel-title">赛题列表（{{ challenges.length }}）</span>
              <span v-if="ended" class="header-hint">赛事已结束，题面与 flag 不再开放</span>
            </div>
          </template>
          <el-empty v-if="!challenges.length" description="该赛事暂无赛题" :image-size="60" />
          <el-table v-else :data="challenges" size="small" stripe>
            <el-table-column prop="title" label="题目" min-width="200" />
            <el-table-column prop="category" label="分类" width="110" />
            <el-table-column prop="difficulty" label="难度" width="100" />
            <el-table-column prop="score" label="分值" width="80" align="center" />
            <el-table-column prop="solvedCount" label="解出队伍" width="100" align="center" />
          </el-table>
        </el-card>

        <el-card v-if="running || ended" shadow="never" class="panel mt16">
          <template #header>
            <div class="panel-header-row">
              <span class="panel-title">{{ ended ? '最终排名' : '实时排名' }}</span>
              <span class="header-hint">{{ standings.length }} 支参赛队伍</span>
            </div>
          </template>
          <el-empty v-if="!standings.length" description="暂无比赛成绩" :image-size="60" />
          <el-table v-else :data="standings" size="small" stripe :row-class-name="rowClass">
            <el-table-column prop="rank" label="#" width="56" align="center" />
            <el-table-column label="战队" min-width="160">
              <template #default="{ row }">
                {{ row.teamName || ('战队' + row.teamId) }}
                <el-tag v-if="isMine(row)" size="small" type="success" effect="plain">我的队伍</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="总分" width="90" align="center" />
            <template v-if="isAwd">
              <el-table-column prop="attackScore" label="攻击分" width="90" align="center" />
              <el-table-column prop="defenseScore" label="防御分" width="90" align="center" />
              <el-table-column prop="slaPenalty" label="SLA" width="80" align="center" />
            </template>
            <template v-else>
              <el-table-column prop="solved" label="解题数" width="90" align="center" />
              <el-table-column label="一血" width="80" align="center">
                <template #default="{ row }">{{ row.firstBlood ? '是' : '—' }}</template>
              </el-table-column>
              <el-table-column label="最后解出" min-width="150">
                <template #default="{ row }">{{ fmt(row.lastSolveAt) }}</template>
              </el-table-column>
            </template>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">我的参赛</span></template>
          <template v-if="myTeam">
            <div class="my-team-name">{{ myTeam.name }}</div>
            <el-tag size="small" type="success" effect="plain">已报名</el-tag>
            <el-button
              v-if="status === 'SIGNUP'"
              text
              type="primary"
              class="mt8"
              @click="enrollVisible = true"
            >报名详情 / 取消报名</el-button>
          </template>
          <template v-else>
            <div class="enroll-state">{{ enrollHint }}</div>
            <el-button v-if="showEnroll" type="primary" class="mt8" @click="enrollVisible = true">立即报名</el-button>
            <el-button v-else-if="running && (myTeam || isAdmin)" type="success" class="mt8" @click="enter">进入赛场</el-button>
          </template>
        </el-card>

        <el-card v-if="isAwd" shadow="never" class="panel mt16">
          <template #header><span class="panel-title">赛制说明</span></template>
          <p class="rule-text">{{ typeMeta.hint }}</p>
          <p class="rule-text">每个参赛队拥有独立靶机，攻击他队靶机获取 flag 得分，防守失分与 SLA 扣分实时计入总分。</p>
        </el-card>
      </el-col>
    </el-row>

    <EnrollDialog v-model="enrollVisible" :contest-id="contestId" @success="load" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import request from '@/utils/request'
import EnrollDialog from '@/components/contest/EnrollDialog.vue'
import { useUserStore } from '@/store/user'
import { getContestType, contestEntryRoute } from '@/constants/contestType'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const contestId = computed(() => String(route.params.contestId))

const contest = ref({})
const myTeam = ref(null)
const challenges = ref([])
const standings = ref([])
const loading = ref(false)
const enrollVisible = ref(false)
const errMsg = ref('')

const typeMeta = computed(() => getContestType(contest.value.type))
const status = computed(() => String(contest.value.status || '').toUpperCase())
const ended = computed(() => ['FINISHED', 'ARCHIVED'].includes(status.value))
const running = computed(() => status.value === 'RUNNING')
const isAwd = computed(() => ['AWD', 'AWDP'].includes(String(contest.value.type || '').toUpperCase()))
const isAdmin = computed(() => user.isAdmin)

const statusLabel = computed(() => ({
  DRAFT: '筹备中', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档'
}[status.value] || status.value || '—'))
const statusTagType = computed(() => ({
  SIGNUP: 'primary', RUNNING: 'success', PENDING: 'warning', DRAFT: 'info', ARCHIVED: 'info'
}[status.value] || 'info'))

/** 报名窗口：仅赛前 SIGNUP 且处于报名时间区间（与后端 ContestGuard 保持一致）。 */
const signupOpen = computed(() => {
  if (status.value !== 'SIGNUP') return false
  const now = Date.now()
  const begin = contest.value.signupStart ? new Date(contest.value.signupStart).getTime() : null
  const close = contest.value.signupEnd ? new Date(contest.value.signupEnd).getTime() : null
  if (begin && now < begin) return false
  if (close && now > close && contest.value.allowLate !== 1) return false
  return true
})
const showEnroll = computed(() => signupOpen.value && !myTeam.value)
// 赛中仅已报名选手/管理员可见赛题（后端同样校验）；赛后公开
const showChallenges = computed(() => ended.value || !!myTeam.value || (running.value && isAdmin.value))

const enrollHint = computed(() => {
  if (status.value === 'DRAFT' || status.value === 'PENDING') return '比赛尚未开始，报名开启后即可报名'
  if (status.value === 'SIGNUP') {
    if (signupOpen.value) return `报名开放中，截止 ${fmt(contest.value.signupEnd)}`
    if (contest.value.signupStart && Date.now() < new Date(contest.value.signupStart).getTime()) {
      return `报名将于 ${fmt(contest.value.signupStart)} 开放`
    }
    return '报名已截止'
  }
  if (status.value === 'RUNNING') return '比赛已开始，报名已截止'
  return '赛事已结束'
})

function fmt(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').replace(/\.\d+/, '').slice(0, 19)
}
function range(a, b) { return `${fmt(a)} ~ ${fmt(b)}` }
function isMine(row) { return !!myTeam.value && row.teamId === myTeam.value.id }
function rowClass({ row }) { return isMine(row) ? 'mine-row' : '' }
function enter() { router.push(contestEntryRoute(contest.value)) }

async function load() {
  loading.value = true
  errMsg.value = ''
  try {
    contest.value = await request.get(`/contests/${contestId.value}`) || {}
  } catch (e) {
    errMsg.value = '赛事信息加载失败：' + (e?.message || '')
  }
  try {
    myTeam.value = await request.get(`/ctf/contests/${contestId.value}/teams/mine`) || null
  } catch {
    myTeam.value = null
  }
  await Promise.allSettled([loadChallenges(), loadStandings()])
  loading.value = false
}

async function loadChallenges() {
  if (!showChallenges.value) { challenges.value = []; return }
  try {
    challenges.value = await request.get(`/ctf/contests/${contestId.value}/challenges/summary`) || []
  } catch {
    challenges.value = []
  }
}

async function loadStandings() {
  if (!running.value && !ended.value) { standings.value = []; return }
  try {
    const data = isAwd.value
      ? await request.get('/awd/score/leaderboard', { params: { contestId: contestId.value, topN: 100 } })
      : await request.get(`/ctf/contests/${contestId.value}/scoreboard`, { params: { topN: 100 } })
    standings.value = (data || []).map((r, i) => ({ ...r, rank: r.rank ?? i + 1 }))
  } catch (e) {
    standings.value = []
    ElMessage.warning('比赛成绩加载失败：' + (e?.message || ''))
  }
}

watch(() => route.params.contestId, load)
onMounted(load)
</script>

<style scoped>
.contest-overview { padding: 0; }
.mt16 { margin-top: 16px; }
.mt8 { margin-top: 8px; }
.mb16 { margin-bottom: 16px; }
.csp-page-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
.head-tags { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.code { color: #86909c; }
.head-actions { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.panel { border-radius: 12px; }
.panel-title { font-weight: 600; color: var(--csp-title); }
.panel-header-row { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.header-hint { font-size: 12px; color: #86909c; }
.desc { white-space: pre-wrap; line-height: 1.7; }
.my-team-name { font-size: 16px; font-weight: 600; color: var(--csp-title); margin-bottom: 8px; }
.enroll-state { font-size: 13px; color: #4e5969; line-height: 1.6; }
.rule-text { font-size: 13px; color: #4e5969; line-height: 1.7; margin: 0 0 8px; }
:deep(.mine-row) { background: rgba(0, 180, 42, 0.06); }
</style>
