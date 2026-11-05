<template>
  <div class="notify-manage">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card class="send-card">
          <template #header>
            <div class="card-header">
              <span>发送通知</span>
              <el-tag size="small" effect="plain" type="info">不填 userId 即为全员广播</el-tag>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" maxlength="255" show-word-limit placeholder="请输入通知标题" />
            </el-form-item>

            <el-form-item label="正文" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="5"
                maxlength="10000"
                show-word-limit
                placeholder="请输入通知正文"
              />
            </el-form-item>

            <el-form-item label="渠道" prop="channel">
              <el-select v-model="form.channel" style="width: 100%">
                <el-option label="站内信" value="INBOX" />
                <el-option label="实时推送" value="WS" />
                <el-option label="邮件" value="EMAIL" />
                <el-option label="短信" value="SMS" />
              </el-select>
            </el-form-item>

            <el-form-item label="接收用户">
              <el-input v-model="form.userId" placeholder="留空表示广播给所有人；也可填用户 ID" clearable />
            </el-form-item>

            <el-form-item label="关联赛事">
              <el-input v-model="form.contestId" placeholder="留空表示平台级通知" clearable />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我发出的通知记录</span>
              <el-button size="small" @click="loadList">刷新</el-button>
            </div>
          </template>

          <el-table v-loading="loading" :data="items" size="small" empty-text="暂无通知记录">
            <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
            <el-table-column label="渠道" width="90">
              <template #default="{ row }">
                <el-tag size="small" effect="plain" :type="channelTagType(row.channel)">
                  {{ channelLabel(row.channel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="范围" width="100">
              <template #default="{ row }">
                <span>{{ row.userId ? '定向' : '广播' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.readFlag ? 'success' : 'info'" effect="light">
                  {{ row.readFlag ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发送时间" width="170">
              <template #default="{ row }">{{ formatTime(row.sentAt) }}</template>
            </el-table-column>
            <el-table-column label="投递" width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.deliverStatus || '-' }}</template>
            </el-table-column>
          </el-table>

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
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { notifyApi } from '@/api'

const formRef = ref(null)
const sending = ref(false)

const form = reactive({
  title: '',
  content: '',
  channel: 'INBOX',
  userId: '',
  contestId: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  channel: [{ required: true, message: '请选择渠道', trigger: 'change' }]
}

const items = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await notifyApi.my({ page: page.value, size: pageSize.value })
    items.value = res?.records || res || []
    total.value = Number(res?.total || items.value.length || 0)
  } catch (e) {
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (!valid) return
    sending.value = true
    try {
      const payload = {
        title: form.title.trim(),
        content: form.content,
        channel: form.channel
      }
      // 雪花 ID 保持字符串，避免精度丢失
      if (form.userId) payload.userId = String(form.userId).trim()
      if (form.contestId) payload.contestId = String(form.contestId).trim()

      await notifyApi.send(payload)
      ElMessage.success('通知已发送')
      handleReset()
      page.value = 1
      loadList()
    } catch (e) {
      ElMessage.error(e.message || '发送失败')
    } finally {
      sending.value = false
    }
  })
}

function handleReset() {
  form.title = ''
  form.content = ''
  form.channel = 'INBOX'
  form.userId = ''
  form.contestId = ''
  formRef.value?.clearValidate()
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
  return Number.isNaN(d.getTime()) ? String(t) : d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.notify-manage { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.send-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }
</style>