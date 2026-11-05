<template>
  <div>
    <el-card shadow="never" header="题解审核">
      <div class="toolbar">
        <el-radio-group v-model="status" @change="onFilter">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">待审核</el-radio-button>
          <el-radio-button label="APPROVED">已通过</el-radio-button>
          <el-radio-button label="REJECTED">已拒绝</el-radio-button>
        </el-radio-group>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="所属题目" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.questionTitle">{{ row.questionTitle }}</span>
            <span v-else class="mono">{{ row.questionId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属赛事" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.contestName">{{ row.contestName }}</span>
            <span v-else-if="row.contestId" class="mono">{{ row.contestId }}</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="170" />
        <el-table-column label="审核意见" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reviewRemark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button
              link type="success"
              :disabled="row.status === 'APPROVED'"
              @click="doReview(row, 'APPROVED')"
            >通过</el-button>
            <el-button
              link type="danger"
              :disabled="row.status === 'REJECTED'"
              @click="doReview(row, 'REJECTED')"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" size="680px" :title="current?.title || '题解详情'">
      <div v-if="current" class="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="所属题目">{{ current.questionTitle || current.questionId }}</el-descriptions-item>
          <el-descriptions-item label="所属赛事">{{ current.contestName || current.contestId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(current.status)" size="small">{{ statusText(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ current.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ current.reviewRemark || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="content" v-html="renderMarkdown(current.content)" />
        <div v-if="current.status === 'PENDING'" class="detail-actions">
          <el-button type="success" @click="doReview(current, 'APPROVED')">通过</el-button>
          <el-button type="danger" @click="doReview(current, 'REJECTED')">拒绝</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reviewWriteups, reviewWriteup } from '@/api'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref('PENDING')

const detailVisible = ref(false)
const current = ref(null)

const STATUS_TEXT = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
const STATUS_TYPE = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
function statusText(s) { return STATUS_TEXT[s] || s || '—' }
function statusType(s) { return STATUS_TYPE[s] || 'info' }

async function load() {
  loading.value = true
  try {
    const data = await reviewWriteups({
      page: page.value,
      size: size.value,
      ...(status.value ? { status: status.value } : {})
    })
    rows.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}
function onFilter() { page.value = 1; load() }
function onSizeChange() { page.value = 1; load() }

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function doReview(row, target) {
  const actionText = target === 'APPROVED' ? '通过' : '拒绝'
  let remark = ''
  if (target === 'REJECTED') {
    try {
      const r = await ElMessageBox.prompt('请输入拒绝理由（可选）', `拒绝题解「${row.title}」`, {
        inputPlaceholder: '例如：内容与题目无关',
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消'
      })
      remark = r.value || ''
    } catch (e) { return }
  } else {
    try {
      await ElMessageBox.confirm(`确认通过题解「${row.title}」？通过后将对外公开。`, '提示', {
        type: 'warning', confirmButtonText: '确认通过', cancelButtonText: '取消'
      })
    } catch (e) { return }
  }
  try {
    await reviewWriteup(row.id, target, remark)
    ElMessage.success(`已${actionText}`)
    detailVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

function escapeHtml(s) {
  return String(s || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}
function renderMarkdown(md) {
  const text = escapeHtml(md)
  return text
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br/>')
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
.mono { font-family: var(--csp-font-mono, monospace); font-size: 12px; }
.detail { display: flex; flex-direction: column; gap: 16px; }
.content {
  background: var(--csp-bg-page);
  border: 1px solid var(--csp-border);
  border-radius: var(--csp-radius-sm);
  padding: 16px;
  line-height: 1.7;
  word-break: break-word;
}
.detail-actions { display: flex; gap: 10px; }
</style>