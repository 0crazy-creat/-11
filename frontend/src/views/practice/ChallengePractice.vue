<template>
  <div class="challenge-practice">
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
              <div class="challenge-title">{{ challenge.title }}</div>
              <el-tag size="small" :type="categoryTagType(challenge.category)">{{ challenge.category }}</el-tag>
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
              <el-button type="primary" size="small" @click="openChallenge(challenge)" :disabled="!challenge.containerReady">
                {{ challenge.containerReady ? '开始答题' : '容器未就绪' }}
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
    <el-dialog v-model="dialogVisible" :title="currentChallenge?.title" width="800px" destroy-on-close>
      <div v-if="currentChallenge" class="challenge-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分类">{{ currentChallenge.category }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ difficultyLabel(currentChallenge.difficulty) }}</el-descriptions-item>
          <el-descriptions-item label="分值">{{ currentChallenge.score }}</el-descriptions-item>
          <el-descriptions-item label="解出数">{{ currentChallenge.solvedCount || 0 }}</el-descriptions-item>
        </el-descriptions>
        <div class="content" v-html="renderMarkdown(currentChallenge.content)"></div>
        <div v-if="currentChallenge.attachment" class="attachment">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, User, Download } from '@element-plus/icons-vue'
import { getChallenges, submitFlag as apiSubmitFlag } from '@/api/challenge'

const categories = ['Web', 'Misc', 'Crypto', 'Reverse', 'Pwn', 'Theory', 'Scenario']
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

let searchTimer = null

onMounted(() => {
  loadChallenges()
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
    ElMessage.error('加载题目失败')
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
  return map[category] || ''
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
  dialogVisible.value = true
}

async function submitFlag() {
  if (!flagInput.value.trim()) {
    ElMessage.warning('请输入 Flag')
    return
  }
  submitting.value = true
  try {
    const res = await apiSubmitFlag(currentChallenge.value.id, flagInput.value)
    if (res?.correct) {
      ElMessage.success('Flag 正确！')
      dialogVisible.value = false
      loadChallenges()
    } else {
      ElMessage.error(res?.message || 'Flag 错误')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

function showWriteup(challenge) {
  currentWriteup.value = challenge.writeup || ''
  writeupDialogVisible.value = true
}

function renderMarkdown(content) {
  if (!content) return ''
  // 简单的 Markdown 渲染（实际项目应使用 markdown-it 等库）
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

function downloadAttachment(url) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.challenge-practice {
  padding: 0;
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
  transition: transform 0.2s;
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
  font-weight: 600;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.challenge-body {
  padding: 16px;
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
}

.tag {
  margin-right: 6px;
  margin-bottom: 6px;
}

.challenge-stats {
  display: flex;
  align-items: center;
  gap: 6px;
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

.challenge-detail .attachment {
  margin-top: 16px;
}

.writeup-content {
  line-height: 1.8;
  padding: 0 20px 20px;
}
</style>
