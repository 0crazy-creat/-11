<template>
  <div class="arena">
    <div class="csp-page-head">
      <div>
        <h2 class="csp-h2">{{ contest.name || '赛事' }}</h2>
        <p class="csp-text-secondary">
          <span class="csp-tag">{{ contest.type }}</span>
          <span class="csp-status" :class="{'is-running': contest.status==='RUNNING'}">{{ statusText(contest.status) }}</span>
          <span v-if="contest.code"> · {{ contest.code }}</span>
        </p>
      </div>
      <div class="head-actions">
        <el-button v-if="canEnroll && !myTeam" type="primary" :loading="enrolling" @click="enroll">报名参赛</el-button>
        <el-tag v-if="myTeam" type="success" effect="light">已报名：{{ myTeam.name }}</el-tag>
        <el-button @click="$router.push(`/rank/${contestId}`)">排行榜</el-button>
      </div>
    </div>

    <el-alert v-if="errMsg" :title="errMsg" type="warning" show-icon :closable="false" class="mb16" />

    <el-row :gutter="16" class="mt16">
      <el-col :span="24">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">题目列表（{{ challenges.length }}）</span></template>
          <el-empty v-if="!challenges.length" description="该赛事暂无题目" :image-size="70" />
          <div class="ch-scroll-container">
            <div v-for="c in challenges" :key="c.id" class="ch-item" :class="{ active: current?.id === c.id, solved: solved.has(c.id) }" @click="current = c">
              <div class="ch-row">
                <span class="ch-title">{{ c.title }}</span>
                <el-icon v-if="solved.has(c.id)" color="#00b42a"><CircleCheckFilled /></el-icon>
              </div>
              <div class="ch-meta">
                <span class="csp-tag">{{ c.category }}</span>
                <span>{{ c.score }} 分</span>
                <span>{{ c.difficulty }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" style="margin-top: 16px">
        <el-card shadow="never" class="panel">
          <el-empty v-if="!current" description="请选择左侧题目" />
          <template v-else>
            <h3 class="csp-h3">{{ current.title }}</h3>
            <div class="meta">
              <span class="csp-tag">{{ current.category }}</span>
              <span>{{ current.score }} 分</span>
              <span>{{ current.difficulty }}</span>
            </div>
            <el-divider />
            <div class="content csp-body" v-html="render(current.description || current.content || '暂无题面')" />

            <!-- 容器题：显示靶机控制 -->
            <div v-if="current.docker" class="target-section">
              <el-divider />
              <div class="target-header">
                <el-icon><Monitor /></el-icon>
                <span class="target-title">动态靶机</span>
                <el-tag v-if="current.judgeMode" size="small" type="info" effect="plain">{{ current.judgeMode }}</el-tag>
              </div>
              <template v-if="target && target.accessUrl">
                <div class="target-info">
                  <div class="target-url-row">
                    <span class="url-label">访问地址：</span>
                    <a :href="target.accessUrl" target="_blank" rel="noopener" class="url-link">{{ target.accessUrl }}</a>
                    <el-tag size="small" type="success" effect="plain">运行中 · 端口 {{ target.port }}</el-tag>
                  </div>
                  <div class="target-actions">
                    <el-button size="small" :loading="targetBusy" @click="resetTarget">重置靶机</el-button>
                    <el-button size="small" type="danger" plain :loading="targetBusy" @click="destroyTarget">销毁容器</el-button>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="target-hint">点击下方按钮启动专属容器，端口由平台随机分配（40000-50000）</div>
                <el-button type="primary" :loading="targetBusy" @click="startTarget">
                  <el-icon><VideoPlay /></el-icon>
                  启动容器
                </el-button>
              </template>
            </div>

            <!-- 附件题：显示下载链接 -->
            <div v-else-if="current.attachmentUrl" class="attachment-section">
              <el-divider />
              <div class="attachment-row">
                <el-icon><Paperclip /></el-icon>
                <a :href="current.attachmentUrl" target="_blank" rel="noopener">下载附件</a>
              </div>
            </div>

            <el-divider />
            <div class="submit-row">
              <el-input v-model="answer" placeholder="输入 flag，如 flag{...}" clearable style="max-width: 420px" @keyup.enter="submit" />
              <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
              <el-tag v-if="result" :type="result.correct ? 'success' : 'danger'" effect="light">{{ result.message }}</el-tag>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, Monitor, VideoPlay, Paperclip } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const contestId = computed(() => route.params.contestId)
const contest = ref({})
const challenges = ref([])
const current = ref(null)
const solved = ref(new Set())
const answer = ref('')
const result = ref(null)
const submitting = ref(false)
const enrolling = ref(false)
const myTeam = ref(null)
const errMsg = ref('')
const target = ref(null)
const targetBusy = ref(false)

const canEnroll = computed(() => ['SIGNUP', 'RUNNING', 'PENDING'].includes(contest.value.status))

function statusText(s) {
  return { DRAFT: '筹备中', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档' }[s] || s || '—'
}
function render(md) {
  return String(md).replace(/[<>&]/g, m => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;' }[m])).replace(/\n/g, '<br/>')
}

async function load() {
  errMsg.value = ''
  try {
    const data = await request.get(`/contests/${contestId.value}`)
    contest.value = data || {}
  } catch (e) { errMsg.value = '赛事信息加载失败：' + (e?.message || '') }

  try {
    const data = await request.get(`/ctf/contests/${contestId.value}/challenges/play`)
    challenges.value = data || []
    current.value = challenges.value[0] || null
    if (!challenges.value.length) errMsg.value = '该赛事暂未配置题目，请联系赛事管理员'
  } catch (e) { errMsg.value = '题目加载失败：' + (e?.message || '') }

  try {
    const data = await request.get(`/ctf/contests/${contestId.value}/solved`)
    solved.value = new Set(data || [])
  } catch (e) { /* 未报名时忽略 */ }

  try {
    const data = await request.get(`/ctf/contests/${contestId.value}/teams/mine`)
    myTeam.value = data || null
  } catch (e) { myTeam.value = null }
}

async function enroll() {
  enrolling.value = true
  try {
    await request.post(`/ctf/contests/${contestId.value}/teams`, { contestId: contestId.value, name: '战队' + Date.now().toString().slice(-4) })
    ElMessage.success('报名成功')
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '报名失败')
  } finally { enrolling.value = false }
}

async function submit() {
  if (!answer.value.trim()) return ElMessage.warning('请输入 flag')
  submitting.value = true
  result.value = null
  try {
    const data = await request.post('/ctf/submissions', {
      contestId: contestId.value, questionId: current.value.id, content: answer.value.trim(), submitType: 'FLAG'
    })
    result.value = data
    if (data?.correct) {
      solved.value = new Set([...solved.value, current.value.id])
      ElMessage.success('答案正确！')
      answer.value = ''
    }
  } catch (e) {
    result.value = { correct: false, message: e?.message || '提交失败' }
  } finally { submitting.value = false }
}

async function loadTargetStatus() {
  if (!current.value?.docker) return
  try {
    target.value = await request.get(`/ctf/contests/${contestId.value}/targets`, {
      params: { questionId: current.value.id }
    })
  } catch (e) {
    target.value = null
  }
}

async function startTarget() {
  if (!current.value?.docker) return
  targetBusy.value = true
  try {
    target.value = await request.post(`/ctf/contests/${contestId.value}/targets/start`, null, {
      params: { questionId: current.value.id }
    })
    ElMessage.success(`容器已启动：${target.value.accessUrl}`)
  } catch (e) {
    const errorMsg = e?.response?.data?.message || e?.message || '容器启动失败'
    if (errorMsg.includes('未报名')) {
      ElMessage.warning('请先点击"报名参赛"按钮报名赛事')
    } else if (errorMsg.includes('镜像')) {
      ElMessage.error('题目未配置 Docker 镜像，请联系管理员')
    } else {
      ElMessage.error(errorMsg)
    }
  } finally {
    targetBusy.value = false
  }
}

async function resetTarget() {
  targetBusy.value = true
  try {
    target.value = await request.post(`/ctf/contests/${contestId.value}/targets/reset`, null, {
      params: { questionId: current.value.id }
    })
    ElMessage.success('靶机已重置')
  } catch (e) {
    ElMessage.error(e?.message || '重置失败')
  } finally {
    targetBusy.value = false
  }
}

async function destroyTarget() {
  try {
    await ElMessageBox.confirm('确定要销毁当前容器吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  targetBusy.value = true
  try {
    await request.post(`/ctf/contests/${contestId.value}/targets/destroy`, null, {
      params: { questionId: current.value.id }
    })
    target.value = null
    ElMessage.success('容器已销毁')
  } catch (e) {
    ElMessage.error(e?.message || '销毁失败')
  } finally {
    targetBusy.value = false
  }
}

watch(() => route.params.contestId, load)
watch(current, (ch) => {
  if (ch?.docker) loadTargetStatus(ch)
  else target.value = null
})
onMounted(load)
</script>

<style scoped>
.mb16 { margin-bottom: 16px }
.mt16 { margin-top: 16px }
.csp-page-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px }
.head-actions { display: flex; gap: 8px; align-items: center }
.panel { border-radius: 12px }
.panel-title { font-weight: 600; color: var(--csp-title) }
.ch-item { padding: 10px 12px; border-radius: 8px; cursor: pointer; margin-bottom: 6px; border: 1px solid transparent }
.ch-item:hover { background: #f7f8fa }
.ch-item.active { background: var(--csp-primary-light); border-color: var(--csp-primary) }
.ch-item.solved .ch-title { color: #00b42a }
.ch-row { display: flex; justify-content: space-between; align-items: center }
.ch-title { font-size: 14px; font-weight: 500; color: var(--csp-title) }
.ch-meta { display: flex; gap: 8px; font-size: 12px; color: #86909c; margin-top: 4px }
.ch-scroll-container { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 8px; scroll-snap-type: x mandatory }
.ch-scroll-container::-webkit-scrollbar { height: 6px }
.ch-scroll-container::-webkit-scrollbar-thumb { background: #c9cdd4; border-radius: 3px }
.ch-scroll-container .ch-item { min-width: 200px; max-width: 240px; scroll-snap-align: start; flex-shrink: 0 }
.csp-tag { font-size: 12px; padding: 1px 6px; border-radius: 4px; background: var(--csp-primary-light); color: var(--csp-primary) }
.meta { display: flex; gap: 10px; font-size: 13px; color: #86909c; margin-top: 6px }
.content { line-height: 1.8; color: #1d2129; max-height: 46vh; overflow: auto; padding-right: 6px }
.submit-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap }
.csp-status.is-running { color: #00b42a; font-weight: 600 }

/* 容器靶机 */
.target-section { margin: 16px 0; }
.target-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; color: #295FFF; font-weight: 600; }
.target-title { font-size: 14px; }
.target-info { padding: 12px; background: rgba(41,95,255,0.04); border: 1px solid rgba(41,95,255,0.18); border-radius: 8px; }
.target-url-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; font-size: 13px; }
.url-label { color: #86909c; }
.url-link { color: #295FFF; font-family: monospace; text-decoration: none; }
.url-link:hover { text-decoration: underline; }
.target-actions { display: flex; gap: 8px; }
.target-hint { margin: 8px 0 12px; font-size: 12px; color: #86909c; }

/* 附件下载 */
.attachment-section { margin: 16px 0; }
.attachment-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.attachment-row a { color: #295FFF; text-decoration: none; }
.attachment-row a:hover { text-decoration: underline; }
</style>
