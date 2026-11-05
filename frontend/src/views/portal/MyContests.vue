<template>
  <div>
    <div class="csp-page-head"><h2 class="csp-h2">我的比赛</h2></div>
    <el-empty v-if="!loading && !list.length" description="还没有参加过比赛，去赛事大厅看看吧" />
    <el-card v-for="c in list" :key="c.id" shadow="never" class="item">
      <div class="row">
        <div>
          <h3 class="name">{{ c.name }}</h3>
          <p class="meta">{{ c.type }} · {{ c.code }} · {{ statusText(c.status) }}</p>
          <p class="meta">比赛：{{ fmt(c.startAt) }} ~ {{ fmt(c.endAt) }}</p>
        </div>
        <el-button type="primary" @click="open(c)">{{ c.status === 'RUNNING' ? '进入' : '详情' }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { contestEntryRoute } from '@/constants/contestType'
const router = useRouter()
const loading = ref(false)
const list = ref([])
function statusText(s) { return { DRAFT:'筹备中',PENDING:'未开始',SIGNUP:'报名中',RUNNING:'进行中',FINISHED:'已结束',ARCHIVED:'已归档' }[s] || s }
function fmt(t) { if (!t) return '—'; return new Date(t).toLocaleString('zh-CN') }
function open(c) { router.push(c.status === 'RUNNING' ? contestEntryRoute(c) : `/portal/contests/${c.id}`) }
async function load() {
  loading.value = true
  try {
    const data = await request.get('/contests', { params: { page: 1, size: 100 } })
    const all = data?.records || []
    // 后端按当前用户返回 joined（队长/队员/个人报名均计入）
    list.value = all.filter(c => c.joined === true)
  } finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.item { margin-bottom: 12px; border-radius: 10px }
.row { display: flex; justify-content: space-between; align-items: center }
.name { margin: 0; font-size: 15px; color: var(--csp-title) }
.meta { margin: 4px 0 0; font-size: 12px; color: #86909c }
</style>
