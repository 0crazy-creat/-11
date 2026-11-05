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
        <el-button v-if="canEnroll && !myTeam" type="primary" @click="enrollVisible = true">报名参赛</el-button>
        <el-tag v-if="myTeam" type="success" effect="light">已报名：{{ myTeam.name }}</el-tag>
        <el-button v-if="myTeam" text @click="enrollVisible = true">报名详情</el-button>
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
              <template v-if="target && (target.accessUrl || target.status === 'RUNNING')">
                <div class="target-info">
                  <div class="target-url-row">
                    <span class="url-label">访问地址：</span>
                    <a v-if="target.accessUrl" :href="target.accessUrl" target="_blank" rel="noopener" class="url-link">{{ target.accessUrl }}</a>
                    <span v-else class="url-link">容器启动中，请稍后刷新…</span>
                    <el-tag size="small" type="success" effect="plain">
                      {{ target.simulated ? '模拟模式' : '运行中' }} · 端口 {{ target.port }}
                    </el-tag>
                  </div>
                  <div class="target-countdown">
                    <el-icon><Timer /></el-icon>
                    <span v-if="targetRemainSec > 0">
                      剩余时间 <b :class="{ urgent: targetRemainSec < 600 }">{{ targetCountdown }}</b>
                    </span>
                    <span v-else class="expired">容器已到期，请重置</span>
                  </div>
                  <div class="target-actions">
                    <el-button size="small" :loading="targetBusy" @click="resetTarget">重置靶机</el-button>
                    <el-button size="small" type="primary" plain :loading="targetBusy" @click="extendTarget">续期</el-button>
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

    <EnrollDialog v-model="enrollVisible" :contest-id="contestId" @success="load" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, Monitor, VideoPlay, Paperclip, Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { renderMarkdown } from '@/utils/markdown'
import EnrollDialog from '@/components/contest/EnrollDialog.vue'

const route = useRoute()
const contestId = computed(() => route.params.contestId)
const contest = ref({})
const challenges = ref([])
const current = ref(null)
const solved = ref(new Set())
const answer = ref('')
const result = ref(null)
const submitting = ref(false)
const enrollVisible = ref(false)
const myTeam = ref(null)
const errMsg = ref('')
const target = ref(null)
const targetBusy = ref(false)
const targetRemainSec = ref(0)
let targetTimer = null

const targetCountdown = computed(() => {
  const s = Math.max(0, targetRemainSec.value)
  const h = String(Math.floor(s / 3600)).padStart(2, '0')
  const m = String(Math.floor((s % 3600) / 60)).padStart(2, '0')
  const sec = String(s % 60).padStart(2, '0')
  return `${h}:${m}:${sec}`
})

const canEnroll = computed(() => ['SIGNUP', 'RUNNING', 'PENDING'].includes(contest.value.status))

function statusText(s) {
  return { DRAFT: '筹备中', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档' }[s] || s || '—'
}
/** 题面统一以 Markdown 存储，交给通用渲染器解析（内部已做 HTML 转义防注入）。 */
const render = (md) => renderMarkdown(md)

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

function startTargetTimer() {
  stopTargetTimer()
  if (!target.value?.expectStopAt) { targetRemainSec.value = 0; return }
  const end = new Date(target.value.expectStopAt).getTime()
  const tick = () => {
    targetRemainSec.value = Math.max(0, Math.floor((end - Date.now()) / 1000))
    if (targetRemainSec.value <= 0) stopTargetTimer()
  }
  tick()
  targetTimer = setInterval(tick, 1000)
}

function stopTargetTimer() {
  if (targetTimer) { clearInterval(targetTimer); targetTimer = null }
}

async function loadTargetStatus() {
  if (!current.value?.docker) return
  try {
    const data = await request.get(`/ctf/contests/${contestId.value}/targets`, {
      params: { questionId: current.value.id }
    })
    target.value = data || null
  } catch (e) {
    target.value = null
  } finally {
    startTargetTimer()
  }
}

async function extendTarget() {
  targetBusy.value = true
  try {
    target.value = await request.post(`/ctf/contests/${contestId.value}/targets/extend`, null, {
      params: { questionId: current.value.id }
    })
    startTargetTimer()
    ElMessage.success('容器已续期')
  } catch (e) {
    ElMessage.error(e?.message || '续期失败')
  } finally {
    targetBusy.value = false
  }
}

async function startTarget() {
  if (!current.value?.docker) return
  targetBusy.value = true
  try {
    target.value = await request.post(`/ctf/contests/${contestId.value}/targets/start`, null, {
      params: { questionId: current.value.id }
    })
    startTargetTimer()
    ElMessage.success(`容器已启动：${target.value.accessUrl || '正在分配访问地址'}`)
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
    startTargetTimer()
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
    stopTargetTimer()
    targetRemainSec.value = 0
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
  else { target.value = null; stopTargetTimer(); targetRemainSec.value = 0 }
})
onMounted(load)
onBeforeUnmount(stopTargetTimer)
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
.target-url-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; font-size: 13px; flex-wrap: wrap; }
.target-countdown { display: flex; align-items: center; gap: 6px; margin-bottom: 10px; font-size: 13px; color: #4e5969; }
.target-countdown b { font-family: var(--csp-font-mono); color: #295FFF; }
.target-countdown b.urgent { color: #f53f3f; }
.target-countdown .expired { color: #f53f3f; }
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
