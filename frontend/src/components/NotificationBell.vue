<template>
  <el-popover
    v-model:visible="panelVisible"
    placement="bottom-end"
    :width="360"
    trigger="click"
    popper-class="notify-popper"
  >
    <template #reference>
      <span class="notify-trigger" :title="`通知${unread > 0 ? `（${unread} 条未读）` : ''}`">
        <el-badge :value="unread" :hidden="unread <= 0" :max="99">
          <el-icon :size="18"><Bell /></el-icon>
        </el-badge>
      </span>
    </template>

    <div class="notify-panel">
      <div class="panel-head">
        <span class="panel-title">通知</span>
        <div class="panel-actions">
          <el-button v-if="unread > 0" link type="primary" size="small" @click="handleReadAll">全部已读</el-button>
          <el-button link type="primary" size="small" @click="goList">查看全部</el-button>
        </div>
      </div>

      <div v-loading="loading" class="panel-body">
        <template v-if="items.length">
          <div
            v-for="it in items"
            :key="it.id"
            class="notify-item"
            :class="{ unread: !it.readFlag }"
            @click="handleOpen(it)"
          >
            <div class="item-head">
              <span class="item-title">{{ it.title }}</span>
              <span class="item-time">{{ formatTime(it.sentAt) }}</span>
            </div>
            <div class="item-content">{{ it.content || '（无正文）' }}</div>
            <div class="item-meta">
              <el-tag size="small" effect="plain" :type="channelTagType(it.channel)">{{ channelLabel(it.channel) }}</el-tag>
              <span v-if="!it.readFlag" class="dot" />
            </div>
          </div>
        </template>
        <el-empty v-else :image-size="60" description="暂无通知" />
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { notifyApi } from '@/api'

const router = useRouter()
const unread = ref(0)
const items = ref([])
const loading = ref(false)
const panelVisible = ref(false)
let timer = null

const POLL_MS = 30000

async function loadUnread() {
  try {
    const n = await notifyApi.unreadCount()
    // 后端可能返回数字或 {count:n}
    unread.value = typeof n === 'object' && n !== null ? Number(n.count ?? n.unread ?? 0) : Number(n || 0)
  } catch (e) {
    // 未登录/接口不可用时不打扰用户
    unread.value = 0
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await notifyApi.my({ page: 1, size: 8 })
    items.value = res?.records || res || []
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function handleOpen(it) {
  if (!it.readFlag) {
    try {
      await notifyApi.read([it.id])
      it.readFlag = 1
      unread.value = Math.max(0, unread.value - 1)
      emitChanged()
    } catch (e) { /* 静默 */ }
  }
}

async function handleReadAll() {
  try {
    await notifyApi.readAll()
    unread.value = 0
    items.value.forEach(i => { i.readFlag = 1 })
    emitChanged()
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

/** 通知列表页标记已读后，通过全局事件让铃铛角标同步刷新 */
const NOTIFY_CHANGED_EVENT = 'csp:notify-changed'
function emitChanged() {
  window.dispatchEvent(new CustomEvent(NOTIFY_CHANGED_EVENT))
}
function onNotifyChanged() {
  loadUnread()
}

function goList() {
  panelVisible.value = false
  const isConsole = router.currentRoute.value.path.startsWith('/console')
  router.push(isConsole ? '/console/notifications' : '/portal/notifications')
}

function channelLabel(c) {
  return { INBOX: '站内信', WS: '实时', EMAIL: '邮件', SMS: '短信' }[c] || c || '站内信'
}

function channelTagType(c) {
  return { INBOX: '', WS: 'success', EMAIL: 'warning', SMS: 'danger' }[c] || ''
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  const diff = Date.now() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

watch(panelVisible, v => { if (v) loadList() })

onMounted(() => {
  loadUnread()
  timer = setInterval(loadUnread, POLL_MS)
  window.addEventListener('csp:notify-changed', onNotifyChanged)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('csp:notify-changed', onNotifyChanged)
})

defineExpose({ refresh: loadUnread })
</script>

<style scoped>
.notify-trigger {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  color: var(--csp-text);
  transition: color .2s;
}
.notify-trigger:hover { color: var(--csp-primary); }

.notify-panel { margin: -6px; }
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px 10px;
  border-bottom: 1px solid var(--csp-border);
}
.panel-title { font-weight: 600; color: var(--csp-title); font-size: 14px; }
.panel-actions { display: flex; gap: 4px; }

.panel-body { max-height: 380px; overflow-y: auto; }

.notify-item {
  padding: 10px 12px;
  border-bottom: 1px solid var(--csp-border);
  cursor: pointer;
  transition: background .15s;
}
.notify-item:hover { background: var(--csp-bg-card); }
.notify-item.unread { background: var(--csp-primary-light, #e8eeff); }
.notify-item.unread:hover { background: var(--csp-primary-light, #e8eeff); }

.item-head { display: flex; justify-content: space-between; gap: 8px; align-items: baseline; }
.item-title { font-size: 13px; font-weight: 600; color: var(--csp-title); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-time { font-size: 11px; color: var(--csp-text-secondary, #909399); flex-shrink: 0; }
.item-content {
  font-size: 12px;
  color: var(--csp-text);
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-meta { margin-top: 6px; display: flex; align-items: center; gap: 8px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: var(--csp-primary); display: inline-block; }
</style>