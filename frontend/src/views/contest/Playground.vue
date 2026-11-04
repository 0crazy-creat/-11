<template>
  <div class="playground">
    <!-- 顶部条 -->
    <header class="pg-header">
      <div class="left">
        <span class="contest-name">{{ contest?.name || 'CTF 解题赛' }}</span>
        <el-tag v-if="contest" size="small" type="primary" effect="plain">{{ contest.type }}</el-tag>
      </div>
      <div class="center">
        <span class="timer-label">剩余时间</span>
        <span class="timer" :class="{ urgent: remainSec < 600 }">{{ countdown }}</span>
      </div>
      <div class="right">
        <div class="score-box">
          <span class="s-label">我的得分</span>
          <span class="s-value">{{ myScore }}</span>
        </div>
        <div class="score-box">
          <span class="s-label">排名</span>
          <span class="s-value">{{ myRank || '—' }}</span>
        </div>
        <el-button size="small" plain @click="showRank = true">排行榜</el-button>
      </div>
    </header>

    <div class="pg-body">
      <!-- 左：题目列表 -->
      <aside class="challenge-panel">
        <div class="panel-head">
          <span>题目列表</span>
          <el-input v-model="keyword" size="small" placeholder="搜索题目" clearable style="width: 120px" />
        </div>
        <el-scrollbar class="ch-scroll">
          <div v-for="cat in groupedChallenges" :key="cat.name" class="cat-block">
            <div class="cat-name">
              {{ cat.name }}
              <span class="cat-count">{{ cat.solved }}/{{ cat.items.length }}</span>
            </div>
            <div
              v-for="ch in cat.items"
              :key="ch.id"
              class="ch-item"
              :class="{ active: current?.id === ch.id, solved: solvedIds.has(ch.id) }"
              @click="selectChallenge(ch)"
            >
              <span class="ch-title">{{ ch.title }}</span>
              <span class="ch-meta">
                <span class="ch-score">{{ ch.score }}</span>
                <el-icon v-if="solvedIds.has(ch.id)" class="ok"><Select /></el-icon>
              </span>
            </div>
          </div>
          <el-empty v-if="!groupedChallenges.length" description="暂无题目" :image-size="60" />
        </el-scrollbar>
      </aside>

      <!-- 中：题目详情 + 提交 -->
      <main class="detail-panel">
        <template v-if="current">
          <div class="d-head">
            <div>
              <h2 class="d-title">{{ current.title }}</h2>
              <div class="d-tags">
                <el-tag size="small" effect="plain">{{ current.category }}</el-tag>
                <el-tag size="small" type="warning" effect="plain">{{ current.score }} 分</el-tag>
                <el-tag size="small" type="info" effect="plain">解决 {{ current.solveCount || 0 }} 次</el-tag>
                <el-tag v-if="solvedIds.has(current.id)" size="small" type="success" effect="plain">已解决</el-tag>
              </div>
            </div>
          </div>

          <el-scrollbar class="d-scroll">
            <div class="d-desc" v-html="renderedDesc" />
            <!-- 题目类型分流：有容器 → 启动容器；无容器 → 附件下载 -->
            <div v-if="current.docker" class="d-target">
              <div class="target-row">
                <el-icon><Monitor /></el-icon>
                <span class="target-label">动态靶机</span>
                <el-tag v-if="current.judgeMode" size="small" effect="plain" type="info">{{ current.judgeMode }}</el-tag>
                <el-tag v-if="current.dynamicScore" size="small" effect="plain" type="warning">动态积分</el-tag>
              </div>
              <template v-if="target && target.accessUrl">
                <div class="target-url">
                  <a :href="target.accessUrl" target="_blank" rel="noopener">{{ target.accessUrl }}</a>
                  <el-tag size="small" type="success" effect="plain">运行中 · 端口 {{ target.port }}</el-tag>
                </div>
                <div class="target-actions">
                  <el-button size="small" :loading="targetBusy" @click="resetTarget">重置靶机</el-button>
                  <el-button size="small" type="danger" plain :loading="targetBusy" @click="destroyTarget">销毁</el-button>
                </div>
              </template>
              <template v-else>
                <div class="target-hint">点击下方按钮拉起专属容器，端口由平台随机分配（40000-50000）</div>
                <el-button type="primary" size="small" :loading="targetBusy" @click="startTarget">启动容器</el-button>
              </template>
            </div>
            <div v-else-if="current.attachmentUrl" class="d-attach">
              <el-icon><Paperclip /></el-icon>
              <a :href="current.attachmentUrl" target="_blank" rel="noopener">下载附件</a>
            </div>
            <div v-if="current.hint" class="d-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ current.hint }}</span>
            </div>
          </el-scrollbar>

          <div class="submit-bar">
            <el-input
              v-model="flag"
              class="flag-input"
              placeholder="flag{...}"
              :disabled="submitting || solvedIds.has(current.id)"
              @keyup.enter="doSubmit"
            />
            <el-button
              type="primary"
              :loading="submitting"
              :disabled="solvedIds.has(current.id) || !flag"
              @click="doSubmit"
            >{{ solvedIds.has(current.id) ? '已通过' : '提交 Flag' }}</el-button>
          </div>
          <div v-if="feedback" class="feedback" :class="feedback.ok ? 'ok' : 'bad'">
            <el-icon><component :is="feedback.ok ? 'CircleCheck' : 'CircleClose'" /></el-icon>
            <span>{{ feedback.text }}</span>
          </div>
        </template>
        <el-empty v-else description="请从左侧选择一道题目" />
      </main>

      <!-- 右：实时动态 -->
      <aside class="feed-panel">
        <div class="panel-head"><span>实时动态</span></div>
        <el-scrollbar class="feed-scroll">
          <div v-for="f in feed" :key="f.id" class="feed-item" :class="f.type.toLowerCase()">
            <div class="f-top">
              <span class="f-team">{{ f.teamName || f.userName }}</span>
              <span class="f-time">{{ f.time }}</span>
            </div>
            <div class="f-text">{{ f.text }}</div>
          </div>
          <el-empty v-if="!feed.length" description="暂无动态" :image-size="50" />
        </el-scrollbar>
      </aside>
    </div>

    <!-- 排行榜抽屉 -->
    <el-drawer v-model="showRank" title="实时排行榜" size="380px">
      <el-table :data="scoreboard" size="small" stripe max-height="70vh">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="teamName" label="队伍" />
        <el-table-column prop="score" label="得分" width="90" align="right" />
        <el-table-column prop="lastSubmit" label="最近提交" width="90" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContest, listPlayChallenges, submitFlag, getScoreboard, getTargetStatus, startTarget as apiStartTarget, resetTarget as apiResetTarget, destroyTarget as apiDestroyTarget } from '@/api'
import { connectWs } from '@/utils/ws'

const props = defineProps({ contestId: { type: [String, Number], required: true } })
const route = useRoute()

const contest = ref(null)
const challenges = ref([])
const current = ref(null)
const keyword = ref('')
const flag = ref('')
const submitting = ref(false)
const feedback = ref(null)
const solvedIds = ref(new Set())
const myScore = ref(0)
const myRank = ref(null)
const scoreboard = ref([])
const showRank = ref(false)
const feed = ref([])
const target = ref(null)
const targetBusy = ref(false)
const remainSec = ref(0)
const totalSec = ref(0)

let timer = null
let ws = null
let feedSeq = 0

const countdown = computed(() => {
  const s = Math.max(0, remainSec.value)
  const h = String(Math.floor(s / 3600)).padStart(2, '0')
  const m = String(Math.floor((s % 3600) / 60)).padStart(2, '0')
  const sec = String(s % 60).padStart(2, '0')
  return `${h}:${m}:${sec}`
})

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return challenges.value
  return challenges.value.filter((c) => (c.title || '').toLowerCase().includes(kw))
})

const groupedChallenges = computed(() => {
  const map = new Map()
  for (const ch of filtered.value) {
    const cat = ch.category || '其他'
    if (!map.has(cat)) map.set(cat, { name: cat, items: [], solved: 0 })
    const g = map.get(cat)
    g.items.push(ch)
    if (solvedIds.value.has(ch.id)) g.solved++
  }
  return [...map.values()]
})

const renderedDesc = computed(() => {
  const text = current.value?.description || '暂无描述'
  // 简易换行渲染，避免引入完整 markdown 依赖
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/\n/g, '<br/>')
})

function pushFeed(item) {
  feed.value.unshift({ id: ++feedSeq, time: new Date().toLocaleTimeString('zh-CN', { hour12: false }), ...item })
  if (feed.value.length > 80) feed.value.pop()
}

function selectChallenge(ch) {
  current.value = ch
  flag.value = ''
  feedback.value = null
  target.value = null
  // 仅容器题查询靶机状态；附件题不发请求
  if (ch && ch.docker) loadTargetStatus(ch)
}

async function loadTargetStatus(ch) {
  try {
    target.value = await getTargetStatus(props.contestId, ch.id)
  } catch (e) {
    target.value = null
  }
}

async function startTarget() {
  if (!current.value?.docker) return
  targetBusy.value = true
  try {
    target.value = await apiStartTarget(props.contestId, current.value.id)
    ElMessage.success(`容器已启动：${target.value.accessUrl}`)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '容器启动失败')
  } finally {
    targetBusy.value = false
  }
}

async function resetTarget() {
  targetBusy.value = true
  try {
    target.value = await apiResetTarget(props.contestId, current.value.id)
    ElMessage.success('靶机已重置')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '重置失败')
  } finally {
    targetBusy.value = false
  }
}

async function destroyTarget() {
  targetBusy.value = true
  try {
    await apiDestroyTarget(props.contestId, current.value.id)
    target.value = null
    ElMessage.success('靶机已销毁')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '销毁失败')
  } finally {
    targetBusy.value = false
  }
}

async function loadContest() {
  try {
    contest.value = await getContest(props.contestId)
    if (contest.value?.startTime && contest.value?.endTime) {
      const now = Date.now()
      const end = new Date(contest.value.endTime).getTime()
      remainSec.value = Math.max(0, Math.floor((end - now) / 1000))
      totalSec.value = remainSec.value
    }
  } catch (e) {
    ElMessage.warning('赛事信息加载失败，使用默认展示')
  }
}

async function loadChallenges() {
  try {
    const list = await listPlayChallenges(props.contestId)
    challenges.value = Array.isArray(list) ? list : (list?.records || [])
    const solved = new Set()
    challenges.value.forEach((c) => { if (c.solved) solved.add(c.id) })
    solvedIds.value = solved
    if (!current.value && challenges.value.length) selectChallenge(challenges.value[0])
  } catch (e) {
    ElMessage.error('题目加载失败')
  }
}

async function loadScoreboard() {
  try {
    const data = await getScoreboard(props.contestId)
    const rows = Array.isArray(data) ? data : (data?.records || [])
    scoreboard.value = rows
    const me = rows.find((r) => r.teamId === route.query.teamId || r.userId === route.query.userId)
    if (me) { myScore.value = me.score; myRank.value = rows.indexOf(me) + 1 }
  } catch (e) { /* 忽略 */ }
}

async function doSubmit() {
  if (!flag.value.trim()) return
  submitting.value = true
  feedback.value = null
  try {
    const res = await submitFlag({ contestId: Number(props.contestId), challengeId: current.value.id, flag: flag.value.trim() })
    const ok = res === true || res?.correct === true || res?.success === true || res?.data === true
    if (ok) {
      solvedIds.value = new Set([...solvedIds.value, current.value.id])
      myScore.value += current.value.score || 0
      feedback.value = { ok: true, text: `提交成功，获得 ${current.value.score} 分！` }
      flag.value = ''
    } else {
      feedback.value = { ok: false, text: res?.message || 'Flag 不正确，请重试' }
    }
  } catch (e) {
    feedback.value = { ok: false, text: e?.response?.data?.message || '提交失败，请稍后再试' }
  } finally {
    submitting.value = false
  }
}

function startTimer() {
  timer = setInterval(() => {
    if (remainSec.value > 0) remainSec.value--
  }, 1000)
}

function subscribe() {
  try {
    ws = connectWs(`/ws/contest/${props.contestId}`, (msg) => {
      const type = (msg.type || '').toUpperCase()
      if (type === 'FIRST_BLOOD') {
        pushFeed({ type: 'FIRST', teamName: msg.data?.teamName, text: `一血！${msg.data?.challengeTitle || ''}` })
      } else if (type === 'SCORE_UPDATE') {
        if (msg.data?.challengeId && solvedIds.value.has(msg.data.challengeId) === false) {
          pushFeed({ type: 'SOLVE', teamName: msg.data?.teamName, text: `解出 ${msg.data?.challengeTitle || '题目'}` })
        }
        loadScoreboard()
      } else if (type === 'ANNOUNCE') {
        pushFeed({ type: 'INFO', teamName: '公告', text: msg.data?.content || msg.data })
      }
    })
  } catch (e) { /* WS 不可用时静默降级 */ }
}

onMounted(async () => {
  await loadContest()
  await loadChallenges()
  await loadScoreboard()
  startTimer()
  subscribe()
  pushFeed({ type: 'INFO', teamName: '系统', text: '进入赛场，祝你好运！' })
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  if (ws) ws.close()
})
</script>

<style scoped>
.playground {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
  color: #1d2129;
  font-family: var(--csp-font-family);
}

/* 顶部 */
.pg-header {
  height: 56px;
  flex: 0 0 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: linear-gradient(90deg, #f7f8fa, #e5e6eb);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.pg-header .left { display: flex; align-items: center; gap: 10px; }
.contest-name { font-size: 16px; font-weight: 600; letter-spacing: 0.5px; }
.pg-header .center { display: flex; align-items: baseline; gap: 8px; }
.timer-label { font-size: 12px; color: #86909c; }
.timer { font-family: var(--csp-font-mono); font-size: 20px; font-weight: 700; color: #295FFF; letter-spacing: 1px; }
.timer.urgent { color: #f53f3f; animation: blink 1s steps(2) infinite; }
@keyframes blink { 50% { opacity: 0.45; } }
.pg-header .right { display: flex; align-items: center; gap: 16px; }
.score-box { display: flex; flex-direction: column; align-items: flex-end; }
.s-label { font-size: 11px; color: #86909c; }
.s-value { font-size: 16px; font-weight: 700; color: #ff7d00; font-family: var(--csp-font-mono); }

/* 主体三栏 */
.pg-body { flex: 1; display: grid; grid-template-columns: 260px 1fr 300px; min-height: 0; }

.panel-head {
  height: 42px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 12px; font-size: 13px; font-weight: 600; color: #4e5969;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

/* 左栏 */
.challenge-panel { display: flex; flex-direction: column; background: #ffffff; border-right: 1px solid rgba(255, 255, 255, 0.07); min-height: 0; }
.ch-scroll { flex: 1; }
.cat-block { padding: 6px 0; }
.cat-name { padding: 6px 14px; font-size: 12px; color: #6b7f9e; display: flex; justify-content: space-between; }
.cat-count { color: #295FFF; }
.ch-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 14px; cursor: pointer; font-size: 13px; color: #4e5969;
  border-left: 3px solid transparent; transition: background 0.15s;
}
.ch-item:hover { background: rgba(77, 171, 247, 0.08); }
.ch-item.active { background: rgba(77, 171, 247, 0.16); border-left-color: #295FFF; color: #1d2129; }
.ch-item.solved .ch-title { color: #51cf66; }
.ch-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ch-meta { display: flex; align-items: center; gap: 6px; flex: 0 0 auto; }
.ch-score { font-size: 12px; color: #ff7d00; font-family: var(--csp-font-mono); }
.ch-meta .ok { color: #51cf66; }

/* 中栏 */
.detail-panel { display: flex; flex-direction: column; background: #f7f8fa; min-height: 0; padding: 0 22px; }
.d-head { padding: 18px 0 12px; border-bottom: 1px solid rgba(255, 255, 255, 0.07); }
.d-title { margin: 0 0 10px; font-size: 20px; font-weight: 600; color: #1d2129; }
.d-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.d-scroll { flex: 1; padding: 16px 0; min-height: 0; }
.d-desc { font-size: 14px; line-height: 1.8; color: #4e5969; word-break: break-word; }
.d-attach, .d-hint { margin-top: 14px; display: flex; align-items: center; gap: 8px; font-size: 13px; }
.d-attach a { color: #295FFF; text-decoration: none; }
.d-attach a:hover { text-decoration: underline; }
.d-target { margin-top: 14px; padding: 12px; border: 1px solid rgba(41,95,255,0.18); border-radius: 8px; background: rgba(41,95,255,0.04); }
.target-row { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #295FFF; font-weight: 600; }
.target-url { margin: 10px 0; display: flex; align-items: center; gap: 10px; font-size: 13px; }
.target-url a { color: #295FFF; font-family: var(--csp-font-mono); }
.target-hint { margin: 8px 0 10px; font-size: 12px; color: #86909c; }
.target-actions { display: flex; gap: 8px; }
.d-hint { color: #ff7d00; background: rgba(255, 212, 59, 0.08); padding: 10px 12px; border-radius: 6px; }
.submit-bar { display: flex; gap: 10px; padding: 14px 0; border-top: 1px solid rgba(255, 255, 255, 0.07); }
.flag-input { flex: 1; }
.feedback { display: flex; align-items: center; gap: 8px; font-size: 13px; padding-bottom: 14px; }
.feedback.ok { color: #51cf66; }
.feedback.bad { color: #f53f3f; }

/* 右栏 */
.feed-panel { display: flex; flex-direction: column; background: #ffffff; border-left: 1px solid rgba(255, 255, 255, 0.07); min-height: 0; }
.feed-scroll { flex: 1; }
.feed-item { padding: 9px 12px; border-bottom: 1px solid rgba(255, 255, 255, 0.04); }
.f-top { display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 3px; }
.f-team { color: #295FFF; font-weight: 600; }
.f-time { color: #86909c; font-family: var(--csp-font-mono); }
.f-text { font-size: 12px; color: #a8b7cc; line-height: 1.5; }
.feed-item.first .f-team { color: #ff7d00; }
.feed-item.solve .f-team { color: #51cf66; }

:deep(.el-input__wrapper) { background: #f7f8fa; box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset; }
:deep(.el-input__inner) { color: #1d2129; }
</style>