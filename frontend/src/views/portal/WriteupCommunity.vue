<template>
  <div>
    <div class="csp-page-head">
      <div>
        <h2 class="csp-h2">题解社区</h2>
        <p class="csp-text-secondary">浏览公开题解、点赞互动，也可以发布自己的解题思路</p>
      </div>
      <div class="head-acts">
        <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 220px"
                  @keyup.enter="load" @clear="load">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openEditor()">
          <el-icon><EditPen /></el-icon> 发布题解
        </el-button>
      </div>
    </div>

    <el-alert v-if="errMsg" :title="errMsg" type="warning" show-icon :closable="false" class="mb12" />

    <div v-loading="loading">
      <el-empty v-if="!loading && !list.length" description="暂无公开题解，欢迎发布第一篇" />
      <el-card v-for="w in list" :key="w.id" shadow="hover" class="item" @click="openDetail(w.id)">
        <div class="item-head">
          <h3 class="name">{{ w.title }}</h3>
          <el-tag size="small" type="success">已公开</el-tag>
        </div>
        <p class="meta">
          题目 #{{ String(w.questionId).slice(-6) }} ·
          浏览 {{ w.viewCount || 0 }} · 点赞 {{ w.likeCount || 0 }} ·
          {{ (w.createdAt || '').replace('T', ' ').slice(0, 16) }}
        </p>
        <div class="content">{{ excerpt(w.content) }}</div>
        <div class="item-foot" @click.stop>
          <el-button size="small" :type="liked[w.id] ? 'primary' : 'default'" @click="like(w)">
            <el-icon><Star /></el-icon> 点赞 {{ w.likeCount || 0 }}
          </el-button>
          <el-button size="small" text @click="openDetail(w.id)">查看全文</el-button>
        </div>
      </el-card>
    </div>

    <div v-if="total > list.length" class="more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>

    <!-- 详情抽屉：可浏览全文 + 点赞 -->
    <el-drawer v-model="detailVisible" size="640px" :title="current?.title || '题解详情'">
      <div v-if="current" class="detail">
        <p class="meta">
          题目 #{{ String(current.questionId).slice(-6) }} ·
          浏览 {{ current.viewCount || 0 }} · 点赞 {{ current.likeCount || 0 }}
        </p>
        <div class="detail-content">{{ current.content }}</div>
        <div class="detail-foot">
          <el-button type="primary" @click="like(current)">
            <el-icon><Star /></el-icon> 点赞 {{ current.likeCount || 0 }}
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 发布题解 -->
    <el-dialog v-model="editorVisible" title="发布题解" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属赛事">
          <el-select v-model="form.contestId" placeholder="选择赛事" style="width:100%" @change="loadChallenges">
            <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目">
          <el-select v-model="form.questionId" placeholder="选择题目" style="width:100%">
            <el-option v-for="q in challenges" :key="q.id" :label="q.title" :value="q.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="支持 Markdown" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交（待审核）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, EditPen, Star } from '@element-plus/icons-vue'
import { listWriteups, getWriteup, likeWriteup, submitWriteup, listContests } from '@/api'
import request from '@/utils/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const errMsg = ref('')
const liked = reactive({})

const detailVisible = ref(false)
const current = ref(null)

const editorVisible = ref(false)
const saving = ref(false)
const contests = ref([])
const challenges = ref([])
const form = reactive({ contestId: '', questionId: '', title: '', content: '' })

function excerpt(t) { return String(t || '').replace(/[#*`>]/g, '').slice(0, 220) }

async function load(reset = true) {
  loading.value = true; errMsg.value = ''
  if (reset) { page.value = 1; list.value = [] }
  try {
    const data = await listWriteups({ page: page.value, size: 10, ...(keyword.value ? { keyword: keyword.value } : {}) })
    const records = data?.records || []
    list.value = reset ? records : [...list.value, ...records]
    total.value = Number(data?.total || 0)
  } catch (e) { errMsg.value = e?.message || '加载失败' } finally { loading.value = false }
}
function loadMore() { page.value += 1; load(false) }

async function openDetail(id) {
  try {
    current.value = await getWriteup(id)
    detailVisible.value = true
  } catch (e) { ElMessage.error(e?.message || '题解不可见') }
}

async function like(w) {
  try {
    const r = await likeWriteup(w.id)
    w.likeCount = r?.likeCount ?? (w.likeCount || 0) + 1
    if (current.value && current.value.id === w.id) current.value.likeCount = w.likeCount
    liked[w.id] = true
    ElMessage.success('点赞成功')
  } catch (e) { ElMessage.error(e?.message || '点赞失败') }
}

async function openEditor() {
  if (!contests.value.length) {
    const data = await listContests({ page: 1, size: 50 })
    contests.value = data?.records || []
  }
  editorVisible.value = true
}
async function loadChallenges(cid) {
  challenges.value = []
  form.questionId = ''
  if (!cid) return
  try {
    const data = await request.get(`/ctf/contests/${cid}/challenges/play`)
    challenges.value = data || []
  } catch (e) { ElMessage.warning('需先报名该赛事才能选择题目') }
}
async function save() {
  if (!form.questionId || !form.title || !form.content) return ElMessage.warning('题目、标题、内容均为必填')
  saving.value = true
  try {
    await submitWriteup({ ...form })
    ElMessage.success('已提交，等待管理员审核后公开')
    editorVisible.value = false
    Object.assign(form, { contestId: '', questionId: '', title: '', content: '' })
  } catch (e) { ElMessage.error(e?.message || '提交失败') } finally { saving.value = false }
}
onMounted(() => load())
</script>

<style scoped>
.mb12 { margin-bottom: 12px }
.csp-page-head { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 16px }
.head-acts { display: flex; gap: 10px; align-items: center }
.item { margin-bottom: 14px; border-radius: 12px; cursor: pointer; transition: all .2s }
.item:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(41,95,255,.12) }
.item-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px }
.name { margin: 0; font-size: 16px; color: var(--csp-title) }
.meta { margin: 6px 0 10px; font-size: 12px; color: #86909c }
.content { font-size: 13px; color: #4e5969; line-height: 1.7; white-space: pre-wrap;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden }
.item-foot { margin-top: 12px; display: flex; gap: 8px; align-items: center }
.more { text-align: center; margin: 16px 0 }
.detail-content { font-size: 14px; color: #1d2129; line-height: 1.9; white-space: pre-wrap; margin-top: 12px }
.detail-foot { margin-top: 24px; padding-top: 16px; border-top: 1px solid #e5e6eb }
</style>
