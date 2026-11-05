<template>
  <div class="challenge-practice">
    <div v-if="loggedIn" class="scoreboard">
      <div class="score-card">
        <span class="sc-label">我的积分</span>
        <span class="sc-value">{{ stats.totalScore }}</span>
      </div>
      <div class="score-card">
        <span class="sc-label">已解出</span>
        <span class="sc-value">{{ stats.solvedCount }}<small> / {{ total }}</small></span>
      </div>
      <div class="score-card">
        <span class="sc-label">提交次数</span>
        <span class="sc-value">{{ stats.submitCount }}</span>
      </div>
      <div class="score-card">
        <span class="sc-label">正确率</span>
        <span class="sc-value">{{ stats.accuracy }}<small>%</small></span>
      </div>
      <div class="rank-card">
        <div class="rank-title">练习榜</div>
        <div v-for="(u, i) in leaderboard" :key="u.userId" class="rank-row">
          <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
          <span class="rank-name">{{ u.name || u.username }}</span>
          <span class="rank-score">{{ u.score }}</span>
        </div>
        <div v-if="!leaderboard.length" class="rank-empty">暂无数据</div>
      </div>
    </div>

    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>题目练习</span>
          <div class="filter-actions">
            <el-select v-model="filter.category" placeholder="分类" clearable style="width: 120px" @change="loadChallenges">
              <el-option label="全部" value="" />
              <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
            </el-select>
            <el-select v-model="filter.difficulty" placeholder="难度" clearable style="width: 120px" @change="loadChallenges">
              <el-option label="全部" value="" />
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
              <el-option label="地狱" value="INSANE" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="搜索题目" clearable style="width: 200px" @input="debounceSearch">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="challenge in challenges" :key="challenge.id">
          <el-card class="challenge-item" :body-style="{ padding: '0px' }" shadow="hover">
            <div class="challenge-header" :class="`difficulty-${challenge.difficulty?.toLowerCase()}`">
              <div class="challenge-title">
                <span class="title-text">{{ challenge.title }}</span>
                <el-tag v-if="challenge.solved" size="small" type="success" effect="dark" class="solved-tag">已解出</el-tag>
              </div>
              <el-tag size="small" :type="categoryTagType(challenge.category)">{{ normCategory(challenge.category) }}</el-tag>
            </div>
            <div class="challenge-body">
              <div class="challenge-info">
                <span class="score">{{ challenge.score }} 分</span>
                <span class="difficulty">{{ difficultyLabel(challenge.difficulty) }}</span>
              </div>
              <div class="challenge-tags">
                <el-tag v-for="tag in parseTags(challenge.tags)" :key="tag" size="small" type="info" class="tag">{{ tag }}</el-tag>
              </div>
              <div class="challenge-stats">
                <el-icon><User /></el-icon>
                <span>{{ challenge.solvedCount || 0 }} 人解出</span>
              </div>
            </div>
            <div class="challenge-footer">
              <el-button type="primary" size="small" @click="openChallenge(challenge)">
                开始答题
              </el-button>
              <el-button size="small" @click="showWriteup(challenge)">查看题解</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="challenges.length === 0 && !loading" description="暂无题目" />

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadChallenges"
        />
      </div>
    </el-card>

    <!-- 答题对话框 -->
    <el-dialog v-model="dialogVisible" :title="currentChallenge?.title" width="800px" destroy-on-close @close="onChallengeDialogClose">
      <div v-if="currentChallenge" class="challenge-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分类">{{ normCategory(currentChallenge.category) }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ difficultyLabel(currentChallenge.difficulty) }}</el-descriptions-item>
          <el-descriptions-item label="分值">{{ currentChallenge.score }}</el-descriptions-item>
          <el-descriptions-item label="解出数">{{ currentChallenge.solvedCount || 0 }}</el-descriptions-item>
        </el-descriptions>
        <div class="content" v-html="renderMarkdown(currentChallenge.content)"></div>

        <!-- 容器题：启动靶机面板 -->
        <div v-if="currentIsContainer" class="container-panel">
          <div class="cp-head">
            <el-icon><Monitor /></el-icon>
            <span class="cp-title">动态靶机</span>
            <el-tag size="small" effect="plain" type="info">{{ extInfo.challengeType || 'STATIC_CONTAINER' }}</el-tag>
          </div>
          <template v-if="container && (container.accessUrl || container.status === 'RUNNING')">
            <div class="cp-url">
              <a v-if="container.accessUrl" :href="container.accessUrl" target="_blank" rel="noopener">{{ container.accessUrl }}</a>
              <span v-else>容器启动中，请稍后刷新…</span>
              <el-tag size="small" :type="container.simulated ? 'warning' : 'success'" effect="plain">
                {{ container.simulated ? '模拟容器（镜像未就绪）' : '运行中' }} · 端口 {{ container.port }}
              </el-tag>
            </div>
            <div class="cp-countdown" v-if="containerHasExpiry">
              <el-icon><Timer /></el-icon>
              <span v-if="containerRemainSec > 0">剩余 <b :class="{ urgent: containerRemainSec < 600 }">{{ containerCountdown }}</b></span>
              <span v-else class="expired">容器已到期，请重置</span>
            </div>
            <div class="cp-actions">
              <el-button size="small" :loading="containerBusy" @click="doResetContainer">重置靶机</el-button>
              <el-button size="small" type="primary" plain :loading="containerBusy" @click="doExtendContainer">续期</el-button>
              <el-button size="small" type="danger" plain :loading="containerBusy" @click="doDestroyContainer">销毁</el-button>
            </div>
          </template>
          <template v-else>
            <div class="cp-hint">点击下方按钮拉起专属容器，端口由平台随机分配</div>
            <el-button type="primary" size="small" :loading="containerBusy" @click="doStartContainer">启动容器</el-button>
          </template>
        </div>

        <div v-else-if="currentChallenge.attachment" class="attachment">
          <el-button type="primary" link @click="downloadAttachment(currentChallenge.attachment)">
            <el-icon><Download /></el-icon> 下载附件
          </el-button>
        </div>
        <el-divider />
        <el-form @submit.prevent="submitFlag">
          <el-form-item label="提交 Flag">
            <el-input v-model="flagInput" placeholder="flag{...}" clearable>
              <template #append>
                <el-button type="primary" @click="submitFlag" :loading="submitting">提交</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>

    <!-- 题解对话框 -->
    <el-dialog v-model="writeupDialogVisible" title="题解" width="700px">
      <div v-if="currentWriteup" class="writeup-content" v-html="renderMarkdown(currentWriteup)"></div>
      <el-empty v-else description="暂无题解" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, User, Download, Monitor, Timer } from '@element-plus/icons-vue'
import {
  getChallenges,
  submitPracticeFlag,
  getPracticeStats,
  getPracticeLeaderboard,
  getPracticeContainer,
  startPracticeContainer,
  resetPracticeContainer,
  destroyPracticeContainer,
  extendPracticeContainer
} from '@/api/challenge'

const categories = ['Web', 'Misc', 'Crypto', 'Reverse', 'Pwn', 'Blockchain', 'Theory', 'Scenario']
const challenges = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filter = reactive({
  category: '',
  difficulty: '',
  keyword: ''
})

const dialogVisible = ref(false)
const currentChallenge = ref(null)
const flagInput = ref('')
const submitting = ref(false)

const writeupDialogVisible = ref(false)
const currentWriteup = ref('')

// ===== 练习计分 =====
const loggedIn = ref(!!localStorage.getItem('csp_token'))
const stats = ref({ totalScore: 0, solvedCount: 0, submitCount: 0, accuracy: 0 })
const leaderboard = ref([])

async function loadStats() {
  if (!loggedIn.value) return
  try {
    const s = await getPracticeStats()
    if (s) stats.value = { ...stats.value, ...s }
    const lb = await getPracticeLeaderboard(5)
    leaderboard.value = Array.isArray(lb) ? lb : []
  } catch {
    // 未登录或接口异常时静默，不影响刷题
  }
}

// ===== 容器（练习靶机）状态 =====
const container = ref(null)
const containerBusy = ref(false)
const containerRemainSec = ref(0)
let containerTimer = null
let containerExpireAtLocal = 0

const extInfo = computed(() => {
  const raw = currentChallenge.value?.ext
  if (!raw) return {}
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
})

const currentIsContainer = computed(() => {
  const ext = extInfo.value
  if (!ext) return false
  const type = ext.challengeType
  if (type === 'STATIC_CONTAINER' || type === 'DYNAMIC_CONTAINER') return true
  if (ext.isDocker === true) return true
  return !!(ext.imageName || ext.containerImage)
})

const containerCountdown = computed(() => {
  const s = Math.max(0, containerRemainSec.value)
  const h = String(Math.floor(s / 3600)).padStart(2, '0')
  const m = String(Math.floor((s % 3600) / 60)).padStart(2, '0')
  const sec = String(s % 60).padStart(2, '0')
  return `${h}:${m}:${sec}`
})

const containerHasExpiry = computed(() => !!parseServerTime(container.value?.expectStopAt))

// 后端 Jackson 输出 "yyyy-MM-dd HH:mm:ss"，需转成可被各浏览器解析的本地时间
function parseServerTime(value) {
  if (!value) return null
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value
  const d = new Date(String(value).trim().replace(' ', 'T'))
  return Number.isNaN(d.getTime()) ? null : d
}

function isLoggedIn() {
  return !!localStorage.getItem('csp_token')
}

// 分类大小写 / 中英文别名归并：WEB、web、Web 统一显示为 Web
const CATEGORY_ALIASES = {
  web: 'Web', 'web安全': 'Web',
  misc: 'Misc', '杂项': 'Misc',
  crypto: 'Crypto', '密码学': 'Crypto',
  reverse: 'Reverse', '逆向': 'Reverse',
  pwn: 'Pwn',
  blockchain: 'Blockchain', '区块链': 'Blockchain',
  theory: 'Theory', '理论': 'Theory',
  scenario: 'Scenario', '场景': 'Scenario',
  forensics: 'Forensics', '取证': 'Forensics'
}

function normCategory(category) {
  const raw = (category == null ? '' : String(category)).trim()
  if (!raw) return '其他'
  const alias = CATEGORY_ALIASES[raw.toLowerCase()]
  if (alias) return alias
  return raw.charAt(0).toUpperCase() + raw.slice(1).toLowerCase()
}

let searchTimer = null

onMounted(() => {
  loadChallenges()
  loadStats()
})

onBeforeUnmount(() => {
  stopContainerTimer()
  clearTimeout(searchTimer)
})

async function loadChallenges() {
  loading.value = true
  try {
    const res = await getChallenges({
      page: page.value,
      pageSize: pageSize.value,
      category: filter.category,
      difficulty: filter.difficulty,
      keyword: filter.keyword
    })
    challenges.value = res?.records || res || []
    total.value = res?.total || 0
  } catch (error) {
    console.error('Load challenges error:', error)
    ElMessage.error('加载题目失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function debounceSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadChallenges()
  }, 300)
}

function categoryTagType(category) {
  const map = {
    Web: 'danger',
    Misc: 'warning',
    Crypto: 'success',
    Reverse: 'info',
    Pwn: 'danger',
    Theory: '',
    Scenario: 'warning'
  }
  return map[normCategory(category)] || ''
}

function difficultyLabel(difficulty) {
  const map = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难',
    INSANE: '地狱'
  }
  return map[difficulty] || difficulty
}

function parseTags(tags) {
  if (!tags) return []
  try {
    return Array.isArray(tags) ? tags : JSON.parse(tags)
  } catch {
    return tags.split(',').map(t => t.trim()).filter(t => t)
  }
}

function openChallenge(challenge) {
  currentChallenge.value = challenge
  flagInput.value = ''
  container.value = null
  containerRemainSec.value = 0
  stopContainerTimer()
  dialogVisible.value = true
  loadContainer()
}

function onChallengeDialogClose() {
  stopContainerTimer()
  container.value = null
  containerRemainSec.value = 0
}

// ===== 练习容器交互 =====

async function loadContainer() {
  stopContainerTimer()
  const id = currentChallenge.value?.id
  // 列表接口公开，容器接口需登录：未登录时静默跳过，避免被 401 强制跳转登录页
  if (!id || !currentIsContainer.value || !isLoggedIn()) {
    container.value = null
    containerRemainSec.value = 0
    return
  }
  try {
    const data = await getPracticeContainer(id)
    container.value = data || null
  } catch (e) {
    container.value = null
  }
  if (container.value?.accessUrl && container.value?.expectStopAt) {
    startContainerTimer()
  } else {
    containerRemainSec.value = 0
  }
}

function syncRemainSec() {
  containerRemainSec.value = containerExpireAtLocal > 0
    ? Math.max(0, Math.floor((containerExpireAtLocal - Date.now()) / 1000)) : 0
}

function startContainerTimer() {
  stopContainerTimer()
  const c = container.value
  const end = parseServerTime(c?.expectStopAt)
  const serverNow = parseServerTime(c?.checkedAt)
  if (!end) {
    containerRemainSec.value = 0
    return
  }
  // 后端 LocalDateTime 不带时区（容器 JVM 为 UTC），直接用浏览器本地时间解析会差 8 小时。
  // 用服务端自身的 expectStopAt - checkedAt 求剩余时长，再挂到本地单调时钟上递减。
  const remainSec = serverNow
    ? Math.max(0, Math.floor((end.getTime() - serverNow.getTime()) / 1000))
    : Math.max(0, Math.floor((end.getTime() - Date.now()) / 1000))
  containerExpireAtLocal = Date.now() + remainSec * 1000
  syncRemainSec()
  containerTimer = setInterval(syncRemainSec, 1000)
}

function stopContainerTimer() {
  if (containerTimer !== null) {
    clearInterval(containerTimer)
    containerTimer = null
  }
  containerExpireAtLocal = 0
}

async function runContainerAction(api, successMsg) {
  const id = currentChallenge.value?.id
  if (!id) return
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再操作靶机')
    return
  }
  containerBusy.value = true
  try {
    await api(id)
    ElMessage.success(successMsg)
    await loadContainer()
  } catch (e) {
    // 请求拦截器已统一提示（含镜像缺失 409、未登录 401 等）
  } finally {
    containerBusy.value = false
  }
}

async function doStartContainer() {
  await runContainerAction(startPracticeContainer, '靶机已启动')
}

async function doResetContainer() {
  await runContainerAction(resetPracticeContainer, '靶机已重置')
}

async function doExtendContainer() {
  await runContainerAction(extendPracticeContainer, '靶机已续期')
}

async function doDestroyContainer() {
  const id = currentChallenge.value?.id
  if (!id) return
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录后再操作靶机')
    return
  }
  containerBusy.value = true
  try {
    await destroyPracticeContainer(id)
    container.value = null
    containerRemainSec.value = 0
    stopContainerTimer()
    ElMessage.success('靶机已销毁')
  } catch (e) {
    // 请求拦截器已统一提示
  } finally {
    containerBusy.value = false
  }
}

async function submitFlag() {
  if (!flagInput.value.trim()) {
    ElMessage.warning('请输入 Flag')
    return
  }
  submitting.value = true
  try {
    const res = await submitPracticeFlag(currentChallenge.value.id, flagInput.value.trim())
    if (res?.correct) {
      ElMessage.success(
        res.alreadySolved ? '该题已解出过，本次不计分' : `答案正确！本题 ${res.score || 0} 分`
      )
      if (currentChallenge.value) currentChallenge.value.solved = true
      flagInput.value = ''
      dialogVisible.value = false
      loadStats()
    } else {
      ElMessage.error(res?.message || 'Flag 不正确，请重试')
    }
  } catch (e) {
    ElMessage.error(e.message || '判题失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function showWriteup(challenge) {
  currentWriteup.value = challenge.writeup || ''
  writeupDialogVisible.value = true
}

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

// 轻量 Markdown 渲染：先转义 HTML 再解析，避免题面注入脚本
function renderMarkdown(content) {
  if (!content) return ''
  const codeBlocks = []
  let src = String(content).replace(/\r\n/g, '\n')

  // 围栏代码块先抽离，避免后续规则破坏其内容
  src = src.replace(/```[^\n]*\n([\s\S]*?)```/g, (_, code) => {
    const index = codeBlocks.push(`<pre class="md-code"><code>${escapeHtml(code)}</code></pre>`) - 1
    return `\u0000CODE${index}\u0000`
  })

  src = escapeHtml(src)

  src = src
    .replace(/^######\s+(.+)$/gm, '<h6>$1</h6>')
    .replace(/^#####\s+(.+)$/gm, '<h5>$1</h5>')
    .replace(/^####\s+(.+)$/gm, '<h4>$1</h4>')
    .replace(/^###\s+(.+)$/gm, '<h3>$1</h3>')
    .replace(/^##\s+(.+)$/gm, '<h2>$1</h2>')
    .replace(/^#\s+(.+)$/gm, '<h1>$1</h1>')
    .replace(/^[ \t]*(?:---|\*\*\*)[ \t]*$/gm, '<hr>')
    .replace(/^&gt;\s?(.+)$/gm, '<blockquote>$1</blockquote>')

  src = src.replace(/(?:^|\n)((?:[-*+]\s+[^\n]*(?:\n|$))+)/g, (_, block) => {
    const items = block.trim().split('\n')
      .map(line => `<li>${line.replace(/^[-*+]\s+/, '')}</li>`)
      .join('')
    return `\n<ul>${items}</ul>`
  })

  src = src.replace(/(?:^|\n)((?:\d+\.\s+[^\n]*(?:\n|$))+)/g, (_, block) => {
    const items = block.trim().split('\n')
      .map(line => `<li>${line.replace(/^\d+\.\s+/, '')}</li>`)
      .join('')
    return `\n<ol>${items}</ol>`
  })

  src = src
    .replace(/`([^`\n]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*\n]+)\*\*/g, '<strong>$1</strong>')
    .replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>')
    .replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')

  src = src.split(/\n{2,}/).map(block => {
    const trimmed = block.trim()
    if (!trimmed) return ''
    // 代码块占位符不能包进 <p>，否则 <pre> 嵌在段落里会导致排版异常
    if (/^\u0000CODE\d+\u0000$/.test(trimmed)) return trimmed
    if (/^<(h\d|ul|ol|pre|blockquote|hr)/.test(trimmed)) return trimmed
    return `<p>${trimmed.replace(/\n/g, '<br>')}</p>`
  }).filter(Boolean).join('\n')

  return src.replace(/\u0000CODE(\d+)\u0000/g, (_, i) => codeBlocks[Number(i)])
}

function downloadAttachment(url) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.challenge-practice {
  padding: 0;
}

/* 练习计分板 */
.scoreboard {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) minmax(180px, 1.5fr);
  gap: 12px;
  margin-bottom: 16px;
}

.score-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-bg-color);
}

.sc-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.sc-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  color: var(--el-text-color-primary);
}

.sc-value small {
  font-size: 13px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
}

.rank-card {
  padding: 12px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-bg-color);
}

.rank-title {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  line-height: 1.9;
}

.rank-no {
  width: 16px;
  text-align: center;
  color: var(--el-text-color-secondary);
}

.rank-no.top {
  color: var(--el-color-danger);
  font-weight: 600;
}

.rank-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-score {
  font-weight: 600;
  color: var(--el-color-primary);
}

.rank-empty {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 1200px) {
  .scoreboard {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .rank-card {
    grid-column: span 2;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.challenge-item {
  margin-bottom: 16px;
  height: calc(100% - 16px);
  display: flex;
  flex-direction: column;
  transition: transform 0.2s;
}

.challenge-item :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.challenge-item:hover {
  transform: translateY(-4px);
}

.challenge-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.challenge-header.difficulty-easy {
  background: linear-gradient(135deg, #67c23a20 0%, #67c23a05 100%);
}

.challenge-header.difficulty-medium {
  background: linear-gradient(135deg, #e6a23c20 0%, #e6a23c05 100%);
}

.challenge-header.difficulty-hard {
  background: linear-gradient(135deg, #f56c6c20 0%, #f56c6c05 100%);
}

.challenge-header.difficulty-insane {
  background: linear-gradient(135deg, #909399 0%, #90939905 100%);
}

.challenge-title {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  font-weight: 600;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.solved-tag {
  flex: none;
}

.challenge-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.challenge-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.score {
  font-weight: 600;
  color: var(--el-color-primary);
}

.difficulty {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.challenge-tags {
  margin-bottom: 12px;
  min-height: 26px;
}

.tag {
  margin-right: 6px;
  margin-bottom: 6px;
}

.challenge-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: auto;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.challenge-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.challenge-detail {
  padding: 0 20px;
}

.challenge-detail .content {
  margin-top: 20px;
  line-height: 1.8;
}

/* v-html 渲染出的 Markdown 内容需要 :deep 才能命中 */
.challenge-detail .content :deep(p) {
  margin: 10px 0;
}

.challenge-detail .content :deep(h1),
.challenge-detail .content :deep(h2),
.challenge-detail .content :deep(h3),
.challenge-detail .content :deep(h4),
.challenge-detail .content :deep(h5),
.challenge-detail .content :deep(h6) {
  margin: 18px 0 10px;
  font-weight: 600;
  line-height: 1.4;
}

.challenge-detail .content :deep(h1) { font-size: 22px; }
.challenge-detail .content :deep(h2) { font-size: 19px; }
.challenge-detail .content :deep(h3) { font-size: 17px; }
.challenge-detail .content :deep(h4) { font-size: 15px; }

.challenge-detail .content :deep(ul),
.challenge-detail .content :deep(ol) {
  margin: 10px 0;
  padding-left: 24px;
}

.challenge-detail .content :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 14px;
  border-left: 3px solid var(--el-border-color);
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.challenge-detail .content :deep(code) {
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--el-fill-color-light);
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.challenge-detail .content :deep(pre.md-code) {
  margin: 12px 0;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: #f7f8fa;
  overflow-x: auto;
}

.challenge-detail .content :deep(pre.md-code code) {
  padding: 0;
  background: none;
}

.challenge-detail .content :deep(hr) {
  margin: 18px 0;
  border: none;
  border-top: 1px solid var(--el-border-color-lighter);
}

.challenge-detail .content :deep(a) {
  color: var(--el-color-primary);
}

.challenge-detail .attachment {
  margin-top: 16px;
}

/* 动态靶机面板 */
.container-panel {
  margin-top: 16px;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.cp-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.cp-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.cp-url {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  font-size: 13px;
}

.cp-url a {
  color: var(--el-color-primary);
  text-decoration: none;
  word-break: break-all;
}

.cp-url a:hover {
  text-decoration: underline;
}

.cp-countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.cp-countdown b {
  font-variant-numeric: tabular-nums;
  color: var(--el-text-color-primary);
}

.cp-countdown b.urgent,
.cp-countdown .expired {
  color: var(--el-color-danger);
}

.cp-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cp-hint {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.writeup-content {
  line-height: 1.8;
  padding: 0 20px 20px;
}
</style>
