<template>
  <div class="announcements">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告栏</span>
          <el-button type="primary" @click="refresh">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-timeline v-if="announcements.length > 0">
          <el-timeline-item
            v-for="item in announcements"
            :key="item.id"
            :timestamp="formatTime(item.createdAt)"
            placement="top"
            :type="item.status === 'PUBLISHED' ? 'primary' : 'info'"
          >
            <el-card class="announcement-card" shadow="hover">
              <div class="announcement-header">
                <h3>{{ item.title }}</h3>
                <el-tag v-if="item.scope === 'CONTEST'" type="warning" size="small">赛事公告</el-tag>
                <el-tag v-else type="success" size="small">全局公告</el-tag>
              </div>
              <div class="announcement-content" v-html="renderMarkdown(item.content)"></div>
              <div class="announcement-footer">
                <span class="author">发布人: {{ item.authorName || '管理员' }}</span>
                <el-button v-if="item.status === 'PUBLISHED'" type="primary" link @click="markAsRead(item)">
                  {{ item.readFlag ? '已读' : '标记已读' }}
                </el-button>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-else description="暂无公告" />
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadAnnouncements"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnnouncements, markAnnouncementRead } from '@/api/announcement'

const announcements = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(() => {
  loadAnnouncements()
})

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await getAnnouncements({
      page: page.value,
      pageSize: pageSize.value
    })
    announcements.value = res?.records || res || []
    total.value = res?.total || 0
  } catch (error) {
    ElMessage.error('加载公告失败')
  } finally {
    loading.value = false
  }
}

function refresh() {
  page.value = 1
  loadAnnouncements()
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

function renderMarkdown(content) {
  if (!content) return ''
  // 简单的 Markdown 渲染
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/#{1,6}\s(.*?)(\n|$)/g, '<h4>$1</h4>')
}

async function markAsRead(item) {
  if (item.readFlag) return
  try {
    await markAnnouncementRead(item.id)
    item.readFlag = true
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('标记失败')
  }
}
</script>

<style scoped>
.announcements {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-card {
  margin-bottom: 16px;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.announcement-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.announcement-content {
  line-height: 1.8;
  color: var(--el-text-color-regular);
  margin-bottom: 16px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.author {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
