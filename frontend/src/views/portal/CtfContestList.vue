<template>
  <div class="ctf-contest-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>CTF 解题赛</span>
          <el-button type="primary" @click="refresh">刷新</el-button>
        </div>
      </template>

      <el-table :data="contests" v-loading="loading" stripe>
        <el-table-column prop="name" label="赛事名称" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/portal/contest/${row.id}`" class="contest-link">{{ row.name }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startAt" label="开始时间" width="180">
          <template #default="{ row }">{{ formatTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column prop="endAt" label="结束时间" width="180">
          <template #default="{ row }">{{ formatTime(row.endAt) }}</template>
        </el-table-column>
        <el-table-column prop="teamCount" label="参赛队伍" width="100" align="center" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'SIGNUP'" type="primary" size="small" @click="signUp(row)">报名</el-button>
            <el-button v-if="row.status === 'RUNNING'" type="success" size="small" @click="enter(row)">进入比赛</el-button>
            <el-button v-if="row.status === 'RUNNING'" type="info" size="small" @click="viewSituation(row)">态势感知</el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="contests.length === 0 && !loading" description="暂无 CTF 赛事" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContests } from '@/api/contest'

const router = useRouter()
const contests = ref([])
const loading = ref(false)

onMounted(() => {
  loadContests()
})

async function loadContests() {
  loading.value = true
  try {
    const res = await getContests({ types: 'CTF' })
    console.log('CTF contests API response:', res)
    contests.value = res?.records || res || []
    console.log('CTF contests loaded:', contests.value.length)
  } catch (error) {
    console.error('Load CTF contests error:', error)
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

function signUp(contest) {
  router.push(`/portal/contest/${contest.id}`)
}

function enter(contest) {
  router.push(`/portal/contest/${contest.id}`)
}

function viewSituation(contest) {
  router.push(`/situation/${contest.id}`)
}

function viewDetail(contest) {
  ElMessage.info(`赛事详情: ${contest.name}`)
}
</script>

<style scoped>
.ctf-contest-list {
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
</style>
