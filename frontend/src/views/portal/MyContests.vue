<template>
  <div>
    <div class="csp-page-head"><h2 class="csp-h2">我的比赛</h2></div>
    <el-empty v-if="!loading && !list.length" description="还没有参加过比赛，去赛事大厅看看吧" />
    <el-card v-for="c in list" :key="c.id" shadow="never" class="item">
      <div class="row">
        <div>
          <h3 class="name">{{ c.name }}</h3>
          <p class="meta">{{ c.type }} · {{ c.code }} · {{ statusText(c.status) }}</p>
        </div>
        <el-button type="primary" @click="$router.push(`/play/${c.id}`)">进入</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
const loading = ref(false)
const list = ref([])
function statusText(s) { return { DRAFT:'筹备中',PENDING:'未开始',SIGNUP:'报名中',RUNNING:'进行中',FINISHED:'已结束',ARCHIVED:'已归档' }[s] || s }
async function load() {
  loading.value = true
  try {
    const data = await request.get('/contests', { params: { page: 1, size: 100 } })
    const all = data?.records || []
    const mine = []
    for (const c of all) {
      try {
        const r = await request.get(`/ctf/contests/${c.id}/teams/mine`)
        if (r) mine.push(c)
      } catch (e) { /* ignore */ }
    }
    list.value = mine
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
