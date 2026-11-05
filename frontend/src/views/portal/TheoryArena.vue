<template>
  <div class="theory-arena">
    <!-- 顶部信息条 -->
    <div class="csp-page-head">
      <div>
        <h2 class="csp-h2">{{ paper.contestName || '理论赛' }}</h2>
        <p class="csp-text-secondary">
          共 {{ paper.questionCount || 0 }} 题 · 满分 {{ paper.totalScore || 0 }} 分
          <span v-if="answeredCount" class="ans-tip">· 已答 {{ answeredCount }}</span>
        </p>
      </div>
      <div class="head-right">
        <el-tag v-if="submitted" type="success" effect="dark">已交卷</el-tag>
        <el-tag v-else-if="timeLeftText" :type="urgent ? 'danger' : 'info'" effect="plain">
          剩余 {{ timeLeftText }}
        </el-tag>
        <el-button @click="loadPaper" :loading="loading">刷新</el-button>
      </div>
    </div>

    <!-- 加载态 -->
    <el-card v-if="loading" v-loading="true" class="loading-card" />

    <!-- 未报名：提前拦截，避免答完 14 题才发现全部跳过 -->
    <el-card v-else-if="notEnrolled" class="enroll-card">
      <el-result icon="warning" title="尚未报名该理论赛" sub-title="理论赛需要先报名（创建或加入战队）后才能答题计分">
        <template #extra>
          <el-button type="primary" @click="$router.push('/portal')">去赛事大厅报名</el-button>
          <el-button @click="loadPaper">我已报名，刷新</el-button>
        </template>
      </el-result>
    </el-card>

    <!-- 试卷为空 -->
    <el-empty v-else-if="!questions.length" description="该赛事暂未配置理论题，请联系赛事管理员" />

    <!-- 交卷后的成绩单 -->
    <el-card v-else-if="submitted" class="result-card">
      <template #header><span>交卷结果</span></template>
      <div class="result-hero">
        <div class="score-num">{{ summary.gainedScore ?? 0 }}</div>
        <div class="score-meta">
          <p>满分 {{ paper.totalScore || 0 }} 分</p>
          <p>答对 {{ summary.correctCount ?? 0 }} / {{ summary.totalCount ?? 0 }} 题</p>
        </div>
      </div>
      <el-divider />
      <el-table :data="detailRows" size="small" border>
        <el-table-column prop="qid" label="题号" width="180" />
        <el-table-column prop="state" label="结果" width="200" />
      </el-table>
      <div class="result-foot">
        <el-button type="primary" @click="$router.push('/portal')">返回赛事大厅</el-button>
      </div>
    </el-card>

    <!-- 答题主区 -->
    <div v-else class="arena-body">
      <!-- 题号导航 -->
      <el-card class="nav-card">
        <template #header><span>答题卡</span></template>
        <div class="qnav">
          <button
            v-for="(q, i) in questions"
            :key="q.questionId"
            class="qnav-btn"
            :class="{ active: i === current, done: !!answers[q.questionId] }"
            @click="current = i"
          >
            {{ i + 1 }}
          </button>
        </div>
        <el-button type="primary" class="submit-btn" :loading="submitting" @click="confirmSubmit">
          交 卷
        </el-button>
      </el-card>

      <!-- 当前题目 -->
      <el-card class="question-card" v-if="currentQ">
        <template #header>
          <div class="q-head">
            <span class="q-index">第 {{ current + 1 }} 题</span>
            <el-tag size="small" type="info">{{ currentQ.category || '理论' }}</el-tag>
            <el-tag size="small" :type="diffTag(currentQ.difficulty)">{{ diffText(currentQ.difficulty) }}</el-tag>
            <span class="q-score">{{ currentQ.score }} 分</span>
          </div>
        </template>

        <h3 class="q-title">{{ currentQ.title }}</h3>
        <div class="q-stem" v-html="renderStem(currentQ.content)"></div>

        <div class="q-options">
          <el-radio-group v-model="answers[currentQ.questionId]" class="opt-group">
            <label
              v-for="opt in parseOptions(currentQ)"
              :key="opt.key"
              class="opt-item"
              :class="{ picked: answers[currentQ.questionId] === opt.key }"
            >
              <el-radio :value="opt.key">
                <span class="opt-key">{{ opt.key }}</span>
                <span class="opt-text">{{ opt.text }}</span>
              </el-radio>
            </label>
          </el-radio-group>

          <!-- 无法解析出选项时回退为文本作答 -->
          <el-input
            v-if="!parseOptions(currentQ).length"
            v-model="answers[currentQ.questionId]"
            placeholder="请输入你的答案"
            clearable
          />
        </div>

        <div class="q-foot">
          <el-button :disabled="current === 0" @click="current--">上一题</el-button>
          <el-button :disabled="current >= questions.length - 1" type="primary" plain @click="current++">
            下一题
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { theoryApi, teamApi } from '@/api'

const route = useRoute()
// 雪花 ID 必须全程保持字符串，切勿 Number()
const contestId = computed(() => String(route.params.contestId || ''))

const loading = ref(false)
const submitting = ref(false)
const submitted = ref(false)
/** 未报名标记：后端判分要求 contest_team.status=APPROVED，提前拦截 */
const notEnrolled = ref(false)
const paper = ref({})
const questions = ref([])
const current = ref(0)
const answers = reactive({})
const summary = ref({})
const now = ref(Date.now())
let timer = null

const currentQ = computed(() => questions.value[current.value] || null)
const answeredCount = computed(() => Object.values(answers).filter(v => v !== '' && v != null).length)

/** 剩余时间（基于后端返回的 endAt） */
const timeLeftText = computed(() => {
  if (!paper.value.endAt) return ''
  const end = new Date(paper.value.endAt).getTime()
  if (!Number.isFinite(end)) return ''
  const left = end - now.value
  if (left <= 0) return '已超时'
  const h = Math.floor(left / 3600000)
  const m = Math.floor((left % 3600000) / 60000)
  const s = Math.floor((left % 60000) / 1000)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})
const urgent = computed(() => {
  if (!paper.value.endAt) return false
  return new Date(paper.value.endAt).getTime() - now.value < 5 * 60 * 1000
})

/** 交卷明细行 */
const detailRows = computed(() =>
  (summary.value.details || []).map(d => {
    const idx = String(d).indexOf(':')
    return { qid: idx > 0 ? d.slice(0, idx) : d, state: idx > 0 ? d.slice(idx + 1) : '' }
  })
)

function diffText(d) {
  return { EASY: '简单', MEDIUM: '中等', HARD: '困难', INSANE: '地狱' }[d] || d || '—'
}
function diffTag(d) {
  return { EASY: 'success', MEDIUM: 'warning', HARD: 'danger', INSANE: 'danger' }[d] || 'info'
}

/**
 * 题干渲染：剥掉形如 "- a) xxx" 的选项行（选项单独渲染为单选框），
 * 仅保留真正的题干正文。
 */
function renderStem(content) {
  if (!content) return ''
  const lines = String(content).split(/\r?\n/)
  const kept = lines.filter(l => !/^\s*[-*]?\s*[a-zA-Z]\s*[)）.、]\s*\S/.test(l))
  const text = kept.join('\n').trim() || String(content)
  return text.replace(/\n/g, '<br/>')
}

/**
 * 从题干中解析选项。理论题种子数据的 content 内嵌 "- a) 选项文本"。
 * 解析失败时返回空数组，由模板回退到文本输入。
 */
function parseOptions(q) {
  if (!q || !q.content) return []
  const out = []
  const re = /^\s*[-*]?\s*([a-zA-Z])\s*[)）.、]\s*(.+?)\s*$/
  for (const line of String(q.content).split(/\r?\n/)) {
    const m = line.match(re)
    if (m) out.push({ key: m[1].toLowerCase(), text: m[2] })
  }
  return out
}

/**
 * 报名前置校验：与后端 SubmissionService.submit() 一致，
 * 要求 myTeam 存在且 status=APPROVED，否则判分会被逐题跳过。
 */
async function checkEnrolled() {
  try {
    const team = await teamApi.getMyTeam(contestId.value)
    const ok = team && String(team.status || '').toUpperCase() === 'APPROVED'
    notEnrolled.value = !ok
    return ok
  } catch {
    // 接口异常时放行，让后端给出权威判定
    notEnrolled.value = false
    return true
  }
}

async function loadPaper() {
  if (!contestId.value) return
  loading.value = true
  try {
    if (!(await checkEnrolled())) {
      questions.value = []
      return
    }
    const res = await theoryApi.paper(contestId.value)
    paper.value = res || {}
    questions.value = res?.questions || []
    current.value = 0
    // 初始化答题槽
    Object.keys(answers).forEach(k => delete answers[k])
    questions.value.forEach(q => { answers[q.questionId] = '' })
  } catch (e) {
    ElMessage.error(e.message || '加载理论试卷失败')
    questions.value = []
  } finally {
    loading.value = false
  }
}

async function confirmSubmit() {
  if (!questions.value.length) return
  const unanswered = questions.value.filter(q => !answers[q.questionId]).length
  const tip = unanswered > 0
    ? `还有 ${unanswered} 题未作答，交卷后不可修改，确定交卷吗？`
    : '交卷后不可修改，确定提交吗？'
  try {
    await ElMessageBox.confirm(tip, '确认交卷', { type: 'warning' })
  } catch {
    return
  }
  await doSubmit()
}

async function doSubmit() {
  submitting.value = true
  try {
    const payload = questions.value
      .filter(q => answers[q.questionId])
      .map(q => ({ questionId: q.questionId, answer: answers[q.questionId] }))
    if (!payload.length) {
      ElMessage.warning('请至少作答一题')
      return
    }
    const res = await theoryApi.submit(contestId.value, payload)
    summary.value = res || {}
    submitted.value = true
    ElMessage.success(`交卷成功，得分 ${res?.gainedScore ?? 0}`)
  } catch (e) {
    ElMessage.error(e.message || '交卷失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPaper()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.theory-arena { padding: 0; }
.head-right { display: flex; align-items: center; gap: 10px; }
.ans-tip { color: var(--csp-primary); }
.loading-card { min-height: 220px; }

.arena-body { display: grid; grid-template-columns: 220px 1fr; gap: 16px; margin-top: 16px; }
.nav-card :deep(.el-card__body) { padding: 14px; }
.qnav { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-bottom: 14px; }
.qnav-btn {
  height: 34px; border: 1px solid var(--el-border-color);
  background: #fff; border-radius: 6px; cursor: pointer;
  font-size: 13px; color: var(--csp-title); transition: all .15s;
}
.qnav-btn:hover { border-color: var(--csp-primary); color: var(--csp-primary); }
.qnav-btn.done { background: #e8eeff; border-color: #b9caff; color: var(--csp-primary); font-weight: 600; }
.qnav-btn.active { background: var(--csp-primary); border-color: var(--csp-primary); color: #fff; font-weight: 600; }
.submit-btn { width: 100%; }

.q-head { display: flex; align-items: center; gap: 10px; }
.q-index { font-weight: 600; color: var(--csp-title); }
.q-score { margin-left: auto; color: #ff7d00; font-weight: 600; font-size: 13px; }
.q-title { font-size: 16px; font-weight: 600; color: var(--csp-title); margin: 4px 0 12px; }
.q-stem { font-size: 14px; line-height: 1.8; color: #4e5969; margin-bottom: 16px; }

.opt-group { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.opt-item {
  display: block; padding: 10px 14px; border: 1px solid var(--el-border-color);
  border-radius: 8px; cursor: pointer; transition: all .15s; width: 100%;
}
.opt-item:hover { border-color: var(--csp-primary); background: #f7f9ff; }
.opt-item.picked { border-color: var(--csp-primary); background: #e8eeff; }
.opt-item :deep(.el-radio) { width: 100%; height: auto; margin-right: 0; }
.opt-item :deep(.el-radio__label) { display: inline-flex; gap: 8px; align-items: flex-start; white-space: normal; }
.opt-key { font-weight: 600; color: var(--csp-primary); text-transform: uppercase; }
.opt-text { color: #1d2129; line-height: 1.6; }

.q-foot { display: flex; justify-content: space-between; margin-top: 20px; }

.result-card { margin-top: 16px; }
.result-hero { display: flex; align-items: center; gap: 24px; padding: 8px 4px; }
.score-num { font-size: 52px; font-weight: 700; color: var(--csp-primary); line-height: 1; }
.score-meta p { margin: 4px 0; color: #4e5969; font-size: 14px; }
.result-foot { margin-top: 16px; text-align: right; }
</style>