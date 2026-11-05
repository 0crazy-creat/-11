<template>
  <div class="awd-contest-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AWD 攻防赛</span>
          <el-button type="primary" @click="refresh">刷新</el-button>
        </div>
      </template>

      <el-table :data="contests" v-loading="loading" stripe>
        <el-table-column prop="name" label="赛事名称" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/portal/contests/${row.id}`" class="contest-link">{{ row.name }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'AWD' ? 'danger' : 'warning'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="180">
          <template #default="{ row }">
            <div class="time-line">{{ formatTime(row.signupStart) }}</div>
            <div class="time-line sub">{{ formatTime(row.signupEnd) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="比赛时间" width="180">
          <template #default="{ row }">
            <div class="time-line">{{ formatTime(row.startAt) }}</div>
            <div class="time-line sub">{{ formatTime(row.endAt) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="teamCount" label="参赛队伍" width="100" align="center" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="signupOpen(row)" type="primary" size="small" @click="signUp(row)">报名</el-button>
            <el-button v-if="row.status === 'RUNNING' && canEnter(row)" type="success" size="small" @click="enter(row)">进入比赛</el-button>
            <el-button v-if="row.status === 'RUNNING'" type="info" size="small" @click="viewSituation(row)">态势感知</el-button>
            <el-button v-if="row.status === 'FINISHED' || row.status === 'ARCHIVED'" size="small" type="warning" plain @click="viewResult(row)">赛况</el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="contests.length === 0 && !loading" description="暂无 AWD 赛事" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContests } from '@/api/contest'
import { useUserStore } from '@/store/user'

const router = useRouter()
const user = useUserStore()
const contests = ref([])
const loading = ref(false)
/** 赛中仅已报名选手可进入赛场；管理员/裁判等保持原有权限 */
function canEnter(row) {
  const isPlayer = (user.roles || []).includes('PLAYER') && !user.isAdmin
  return !isPlayer || row.joined === true
}

onMounted(() => {
  loadContests()
})

async function loadContests() {
  loading.value = true
  try {
    // 后端 /api/contests 暂不支持 types 参数，必须前端二次过滤，否则会把 CTF 等赛制串台进来
    const res = await getContests({ page: 1, size: 100 })
    const all = res?.records || res || []
    contests.value = all.filter(c => ['AWD', 'AWDP'].includes(String(c.type || '').toUpperCase()))
  } catch (error) {
    console.error('Load AWD contests error:', error)
    ElMessage.error('加载赛事失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function refresh() {
  loadContests()
}

function statusType(status) {
  const map = {
    DRAFT: 'info',
    PENDING: 'warning',
    SIGNUP: 'primary',
    RUNNING: 'success',
    FINISHED: '',
    ARCHIVED: 'info'
  }
  return map[status] || ''
}

function statusLabel(status) {
  const map = {
    DRAFT: '草稿',
    PENDING: '待发布',
    SIGNUP: '报名中',
    RUNNING: '进行中',
    FINISHED: '已结束',
    ARCHIVED: '已归档'
  }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

/** 仅赛前开放报名：状态 SIGNUP 且处于报名时间区间（补报名开关只放宽截止时间）。 */
function signupOpen(row) {
  if (row.status !== 'SIGNUP') return false
  const now = Date.now()
  if (row.signupStart && now < new Date(row.signupStart).getTime()) return false
  if (row.signupEnd && now > new Date(row.signupEnd).getTime() && row.allowLate !== 1) return false
  return true
}

function signUp(contest) {
  router.push(`/portal/contests/${contest.id}`)
}

function enter(contest) {
  router.push(`/portal/awd/${contest.id}`)
}

function viewSituation(contest) {
  router.push(`/situation/${contest.id}`)
}

function viewResult(contest) {
  router.push(`/portal/contests/${contest.id}`)
}

function viewDetail(contest) {
  router.push(`/portal/contests/${contest.id}`)
}
</script>

<style scoped>
.awd-contest-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contest-link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
}

.contest-link:hover {
  text-decoration: underline;
}

.time-line {
  font-size: 13px;
  color: var(--el-text-color-primary);
  line-height: 1.5;
}

.time-line.sub {
  color: var(--el-text-color-secondary);
}
</style>
