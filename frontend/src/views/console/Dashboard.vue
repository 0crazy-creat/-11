<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :span="6">
        <div class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16">
        <el-card shadow="never" header="进行中赛事">
          <el-table :data="contests" size="small" v-loading="loading">
            <el-table-column prop="name" label="赛事名称" min-width="180" />
            <el-table-column prop="type" label="类型" width="90" />
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" min-width="160" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="$router.push(`/console/contests/${row.id}`)">详情</el-button>
                <el-button link type="success" @click="$router.push(`/play/${row.id}`)">进入</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" header="平台公告">
          <el-empty v-if="!announcements.length" description="暂无公告" :image-size="80" />
          <ul v-else class="ann-list">
            <li v-for="a in announcements" :key="a.id">
              <div class="ann-title">{{ a.title }}</div>
              <div class="ann-time">{{ a.createdAt }}</div>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { contestApi, announceApi, statsApi } from '@/api'

const loading = ref(false)
const contests = ref([])
const announcements = ref([])
const stats = ref({ userCount: 0, teamCount: 0, contestCount: 0, challengeCount: 0 })

const cards = computed(() => [
  { label: '注册用户', value: stats.value.userCount },
  { label: '战队数量', value: stats.value.teamCount },
  { label: '赛事总数', value: stats.value.contestCount },
  { label: '题目总数', value: stats.value.challengeCount }
])

function statusType(s) {
  return { RUNNING: 'success', PENDING: 'info', FINISHED: 'warning' }[s] || 'info'
}

onMounted(async () => {
  loading.value = true
  try {
    const [c, a, s] = await Promise.allSettled([contestApi.list({ page: 1, size: 10 }), announceApi.list({ page: 1, size: 8 }), statsApi.overview()])
    if (c.status === 'fulfilled') contests.value = c.value.list || c.value.records || c.value || []
    if (a.status === 'fulfilled') announcements.value = a.value.list || a.value.records || a.value || []
    if (s.status === 'fulfilled') stats.value = { ...stats.value, ...s.value }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.stat-label { color: #7d8ba1; font-size: 13px; }
.stat-value { font-size: 30px; font-weight: 700; color: #1d2129; margin-top: 6px; }
.ann-list { list-style: none; padding: 0; margin: 0; }
.ann-list li { padding: 10px 0; border-bottom: 1px dashed #eceff3; }
.ann-title { color: #1d2129; font-size: 14px; }
.ann-time { color: #9aa6b8; font-size: 12px; margin-top: 4px; }
</style>