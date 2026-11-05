<template>
  <div class="notifications">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="head-left">
            <span>我的通知</span>
            <el-tag v-if="unread > 0" type="danger" size="small" effect="light">{{ unread }} 条未读</el-tag>
          </div>
          <div class="head-actions">
            <el-radio-group v-model="filterRead" size="small" @change="reload">
              <el-radio-button :value="null">全部</el-radio-button>
              <el-radio-button :value="0">未读</el-radio-button>
              <el-radio-button :value="1">已读</el-radio-button>
            </el-radio-group>
            <el-button v-if="unread > 0" type="primary" plain size="small" @click="handleReadAll">全部已读</el-button>
            <el-button size="small" @click="reload">刷新</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-timeline v-if="items.length">
          <el-timeline-item
            v-for="it in items"
            :key="it.id"
            :timestamp="formatTime(it.sentAt)"
            placement="top"
            :type="it.readFlag ? 'info' : 'primary'"
          >
            <el-card class="notify-card" :class="{ unread: !it.readFlag }" shadow="hover">
              <div class="notify-header">
                <h3>{{ it.title }}</h3>
                <div class="tags">
                  <el-tag size="small" effect="plain" :type="channelTagType(it.channel)">{{ channelLabel(it.channel) }}</el-tag>
                  <el-tag v-if="!it.readFlag" size="small" type="danger" effect="light">未读</el-tag>
                </div>
              </div>
              <div class="notify-content">{{ it.content || '（无正文）' }}</div>
              <div class="notify-footer">
                <span class="meta">{{ it.contestId ? `赛事相关` : '平台通知' }}</span>
                <el-button v-if="!it.readFlag" type="primary" link size="small" @click="handleRead(it)">标记已读</el-button>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-else description="暂无通知" />
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { notifyApi } from '@/api'

const items = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unread = ref(0)
const filterRead = ref(null)

onMounted(reload)

async function loadUnread() {
  try {
    const n = await notifyApi.unreadCount()
    unread.value = typeof n === 'object' && n !== null ? Number(n.count ?? n.unread ?? 0) : Number(n || 0)
  } catch (e) {
    unread.value = 0
  }
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (filterRead.value !== null) params.readFlag = filterRead.value
    const res = await notifyApi.my(params)
    items.value = res?.records || res || []
    total.value = Number(res?.total || items.value.length)
  } catch (e) {
    items.value = []
    ElMessage.error(e.message || '加载通知失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  loadList()
  loadUnread()
}

/** 通知状态变化后广播，让顶栏铃铛角标同步刷新 */
function emitChanged() {
  window.dispatchEvent(new CustomEvent('csp:notify-changed'))
}

async function handleRead(it) {
  try {
    await notifyApi.read([it.id])
    it.readFlag = 1
    unread.value = Math.max(0, unread.value - 1)
    emitChanged()
    ElMessage.success('已标记为已读')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleReadAll() {
  try {
    await notifyApi.readAll()
    items.value.forEach(i => { i.readFlag = 1 })
    unread.value = 0
    emitChanged()
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

function channelLabel(c) {
  return { INBOX: '站内信', WS: '实时', EMAIL: '邮件', SMS: '短信' }[c] || c || '站内信'
}

function channelTagType(c) {
  return { INBOX: '', WS: 'success', EMAIL: 'warning', SMS: 'danger' }[c] || ''
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.notifications { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.head-left { display: flex; align-items: center; gap: 8px; }
.head-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.notify-card { margin-bottom: 4px; }
.notify-card.unread { border-left: 3px solid var(--csp-primary, #295fff); }

.notify-header { display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-bottom: 10px; }
.notify-header h3 { margin: 0; font-size: 16px; color: var(--csp-title, var(--el-text-color-primary)); }
.tags { display: flex; gap: 6px; flex-shrink: 0; }

.notify-content {
  line-height: 1.8;
  color: var(--el-text-color-regular);
  padding: 10px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  margin-bottom: 12px;
  white-space: pre-wrap;
}

.notify-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.meta { color: var(--el-text-color-secondary); font-size: 13px; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>