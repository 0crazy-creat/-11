<template>
  <div>
    <div class="csp-page-head">
      <div>
        <h2 class="csp-h2">我的靶场</h2>
        <p class="csp-text-secondary">选择赛事后点击「启动容器」，系统会动态拉起独立靶机并分配 40000-50000 随机端口</p>
      </div>
      <el-select v-model="contestId" style="width: 300px" placeholder="选择赛事" @change="load">
        <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>

    <el-alert v-if="errMsg" :title="errMsg" type="warning" show-icon :closable="false" class="mb12" />

    <el-empty v-if="!contests.length" description="暂无可参与的 AWD/AWDP 赛事" />

    <div v-else-if="!loading && !list.length" class="empty-box">
      <el-empty description="尚未启动靶机">
        <el-button type="primary" size="large" :loading="starting" @click="start()">启动容器</el-button>
        <p class="hint">启动后约 3-10 秒完成容器编排，将返回形如 http://127.0.0.1:4xxxx 的访问地址</p>
      </el-empty>
    </div>

    <el-row v-else :gutter="16" class="mt16">
      <el-col v-for="t in list" :key="t.id" :span="8">
        <el-card shadow="hover" class="tcard">
          <div class="row">
            <el-tag :type="t.status === 'RUNNING' ? 'success' : 'info'" size="small">{{ t.status }}</el-tag>
            <span class="health" :class="{ ok: t.health === 1 }">
              <el-icon><CircleCheck v-if="t.health === 1" /><Warning v-else /></el-icon>
              {{ t.health === 1 ? '健康' : '异常' }}
            </span>
          </div>
          <h3 class="name">靶机 #{{ String(t.questionId).slice(-4) }}</h3>

          <div v-if="t.accessUrl" class="addr-box">
            <p class="addr-label">靶机地址</p>
            <a :href="t.accessUrl" target="_blank" class="addr">{{ t.accessUrl }}</a>
            <p class="meta">宿主端口 {{ t.port }} · 容器 IP {{ t.ip }}</p>
          </div>
          <div v-else class="addr-box pending">
            <p class="addr-label">靶机地址</p>
            <p class="addr muted">容器未就绪</p>
          </div>

          <div class="acts">
            <el-button type="primary" :disabled="!t.accessUrl" @click="open(t.accessUrl)">
              <el-icon><Link /></el-icon> 打开靶场
            </el-button>
            <el-button :disabled="!t.accessUrl" @click="copy(t.accessUrl)">复制地址</el-button>
            <el-button :loading="starting" @click="start(t.questionId)">重启</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="list.length" class="refresh-bar">
      <el-button type="primary" plain :loading="starting" @click="start()">再启动一台容器</el-button>
      <el-button text @click="load">刷新状态</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Warning, Link } from '@element-plus/icons-vue'
import { listMyTargets, startMyTarget, listContests } from '@/api'

const contestId = ref('')
const contests = ref([])
const list = ref([])
const loading = ref(false)
const starting = ref(false)
const errMsg = ref('')

async function loadContests() {
  const data = await listContests({ page: 1, size: 50 })
  const all = data?.records || []
  contests.value = all.filter(c => c.type === 'AWD' || c.type === 'AWDP')
  if (contests.value.length) { contestId.value = contests.value[0].id; await load() }
}
async function load() {
  if (!contestId.value) return
  loading.value = true; errMsg.value = ''
  try {
    const data = await listMyTargets(contestId.value)
    list.value = data || []
  } catch (e) { list.value = []; errMsg.value = e?.message || '' } finally { loading.value = false }
}
async function start(questionId) {
  if (!contestId.value) return ElMessage.warning('请先选择赛事')
  starting.value = true; errMsg.value = ''
  try {
    const t = await startMyTarget(contestId.value, questionId)
    ElMessage.success(`容器已启动：${t?.accessUrl || ''}`)
    await load()
  } catch (e) { errMsg.value = e?.message || '启动失败'; ElMessage.error(errMsg.value) } finally { starting.value = false }
}
function open(url) { if (url) window.open(url, '_blank') }
async function copy(url) {
  try { await navigator.clipboard.writeText(url); ElMessage.success('地址已复制') }
  catch { ElMessage.info(url) }
}
onMounted(loadContests)
</script>

<style scoped>
.mt16 { margin-top: 16px } .mb12 { margin-bottom: 12px }
.csp-page-head { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px }
.tcard { border-radius: 12px; margin-bottom: 16px }
.row { display: flex; justify-content: space-between; align-items: center }
.health { font-size: 12px; color: #86909c; display: inline-flex; align-items: center; gap: 4px }
.health.ok { color: #00b42a }
.name { margin: 12px 0 10px; font-size: 15px; color: var(--csp-title) }
.addr-box { background: #e8eeff; border-radius: 8px; padding: 10px 12px; margin-bottom: 12px }
.addr-box.pending { background: #f7f8fa }
.addr-label { margin: 0 0 4px; font-size: 12px; color: #86909c }
.addr { font-family: ui-monospace, Menlo, monospace; font-size: 14px; font-weight: 600;
  color: #295FFF; word-break: break-all; text-decoration: none }
.addr:hover { text-decoration: underline }
.addr.muted { color: #c9cdd4; font-weight: 400 }
.meta { font-size: 12px; color: #86909c; margin: 6px 0 0 }
.acts { display: flex; gap: 8px; flex-wrap: wrap }
.empty-box { text-align: center; padding: 40px 0 }
.hint { margin-top: 12px; font-size: 12px; color: #86909c }
.refresh-bar { margin-top: 8px; display: flex; gap: 8px }
</style>
