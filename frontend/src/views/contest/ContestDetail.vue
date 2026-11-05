<template>
  <div v-loading="loading">
    <el-page-header :title="contest.name || '赛事详情'" @back="$router.back()">
      <template #content>
        <el-tag :type="statusType(contest.status)">{{ contest.status }}</el-tag>
        <span class="meta">{{ contest.startTime }} ~ {{ contest.endTime }}</span>
      </template>
      <template #extra>
                <el-button type="primary" @click="$router.push(`/play/${contestId}`)">进入答题</el-button>
        <el-button @click="$router.push(`/rank/${contestId}`)">排行榜</el-button>
        <el-button @click="$router.push(`/situation/${contestId}`)">态势大屏</el-button>
      </template>
    </el-page-header>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="赛事信息">
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="编码">{{ contest.code }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ contest.type }}</el-descriptions-item>
            <el-descriptions-item label="模式">
              <el-tag size="small" :type="contest.mode === 'INDIVIDUAL' ? 'primary' : 'success'">
                {{ contest.mode === 'INDIVIDUAL' ? '个人赛' : '团队赛' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始">{{ contest.startTime }}</el-descriptions-item>
            <el-descriptions-item label="结束">{{ contest.endTime }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ contest.description }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="实时排行榜 (Top 10)">
          <el-table :data="rank" size="small">
            <el-table-column prop="rank" label="#" width="60" />
            <el-table-column prop="teamName" label="战队" min-width="140" />
            <el-table-column prop="score" label="得分" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" header="题目列表" style="margin-top:16px">
      <el-table :data="challenges" size="small">
        <el-table-column prop="title" label="题目" min-width="200" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="difficulty" label="难度" width="100" />
        <el-table-column prop="score" label="分值" width="90" />
        <el-table-column prop="solvedCount" label="解出" width="90" />
      </el-table>
    </el-card>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { contestApi, challengeApi, rankApi } from '@/api'

const props = defineProps({ id: { type: [String, Number], required: true } })
const contestId = props.id
const loading = ref(false)
const contest = ref({})
const challenges = ref([])
const rank = ref([])

function statusType(s) { return { RUNNING: 'success', PENDING: 'info', FINISHED: 'warning' }[s] || 'info' }

onMounted(async () => {
  loading.value = true
  try {
    const [c, ch, rk] = await Promise.allSettled([
      contestApi.detail(contestId),
      challengeApi.list({ contestId }),
      rankApi.list({ contestId, limit: 10 })
    ])
    if (c.status === 'fulfilled') contest.value = c.value || {}
    if (ch.status === 'fulfilled') challenges.value = ch.value.list || ch.value.records || ch.value || []
    if (rk.status === 'fulfilled') rank.value = rk.value.list || rk.value.records || rk.value || []
  } finally { loading.value = false }
})
</script>

<style scoped>
.meta { margin-left: 10px; color: #8b98ad; font-size: 13px; }
</style>