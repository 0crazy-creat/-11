<template>
  <div class="mqtt-manage">
    <!-- 连接状态 -->
    <el-card shadow="never" class="status-card">
      <template #header>
        <div class="card-header">
          <span>MQTT 消息总线</span>
          <el-button size="small" :loading="statusLoading" @click="loadStatus">刷新状态</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="运行模式">
          <el-tag :type="status.mode === 'broker' ? 'success' : 'warning'" size="small">
            {{ status.mode === 'broker' ? '外部 Broker' : '本地内存总线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="启用">
          <el-tag :type="status.enabled ? 'success' : 'info'" size="small">{{ status.enabled ? '是' : '否' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Broker 地址">{{ status.broker || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Client ID">{{ status.clientId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="已用 Topic" :span="2">
          <el-tag v-for="t in (status.topics || [])" :key="t" size="small" class="topic-tag" @click="pickTopic(t)">{{ t }}</el-tag>
          <span v-if="!status.topics || !status.topics.length" class="csp-text-secondary">暂无</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        v-if="status.mode !== 'broker'"
        type="info"
        :closable="false"
        show-icon
        class="mode-tip"
        title="当前未配置外部 Broker，使用本地内存总线（保留最近 100 条消息，重启后清空）"
      />
    </el-card>

    <el-row :gutter="16">
      <!-- 发布 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header><span>发布消息</span></template>
          <el-form label-width="72px" size="small">
            <el-form-item label="Topic" required>
              <el-input v-model="form.topic" placeholder="如 contest/awd/round" clearable />
            </el-form-item>
            <el-form-item label="QoS">
              <el-radio-group v-model="form.qos">
                <el-radio-button :value="0">0</el-radio-button>
                <el-radio-button :value="1">1</el-radio-button>
                <el-radio-button :value="2">2</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="Payload">
              <el-input v-model="form.payload" type="textarea" :rows="6" placeholder='{"event":"round_start","round":3}' />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="publishing" @click="doPublish">发布</el-button>
              <el-button @click="fillSample">填入示例</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 订阅 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>拉取 Topic 消息</span>
              <div class="head-actions">
                <el-input v-model="query.topic" placeholder="输入 topic" size="small" style="width: 180px" clearable />
                <el-select v-model="query.limit" size="small" style="width: 96px">
                  <el-option :value="10" label="最近 10" />
                  <el-option :value="20" label="最近 20" />
                  <el-option :value="50" label="最近 50" />
                </el-select>
                <el-button size="small" type="primary" :loading="loadingMsgs" @click="loadMessages">拉取</el-button>
              </div>
            </div>
          </template>
          <el-table :data="messages" v-loading="loadingMsgs" size="small" height="360" empty-text="暂无消息">
            <el-table-column prop="ts" label="时间" width="170">
              <template #default="{ row }">{{ fmtTs(row.ts) }}</template>
            </el-table-column>
            <el-table-column prop="topic" label="Topic" width="180" show-overflow-tooltip />
            <el-table-column prop="payload" label="Payload" show-overflow-tooltip />
            <el-table-column prop="qos" label="QoS" width="70" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mqttApi } from '@/api'

const status = ref({})
const statusLoading = ref(false)
const publishing = ref(false)
const loadingMsgs = ref(false)
const messages = ref([])

const form = reactive({ topic: '', payload: '', qos: 0 })
const query = reactive({ topic: '', limit: 20 })

async function loadStatus() {
  statusLoading.value = true
  try {
    // 响应拦截器已解包 Result<T>，这里拿到的是 data 本身
    status.value = (await mqttApi.status()) || {}
  } catch (e) {
    ElMessage.error('获取 MQTT 状态失败')
  } finally {
    statusLoading.value = false
  }
}

async function doPublish() {
  if (!form.topic.trim()) {
    ElMessage.warning('请填写 Topic')
    return
  }
  publishing.value = true
  try {
    await mqttApi.publish(form.topic.trim(), form.payload, form.qos)
    ElMessage.success('已发布')
    query.topic = form.topic.trim()
    await Promise.all([loadStatus(), loadMessages()])
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

async function loadMessages() {
  if (!query.topic.trim()) {
    ElMessage.warning('请填写要拉取的 Topic')
    return
  }
  loadingMsgs.value = true
  try {
    messages.value = (await mqttApi.messages(query.topic.trim(), query.limit)) || []
  } catch (e) {
    ElMessage.error('拉取消息失败')
  } finally {
    loadingMsgs.value = false
  }
}

function pickTopic(t) {
  query.topic = t
  form.topic = t
  loadMessages()
}

function fillSample() {
  if (!form.topic) form.topic = 'contest/awd/round'
  form.payload = JSON.stringify({ event: 'round_start', round: 3, ts: Date.now() }, null, 2)
}

function fmtTs(ts) {
  if (!ts) return '-'
  const d = new Date(Number(ts))
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

onMounted(loadStatus)
</script>

<style scoped>
.mqtt-manage { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.head-actions { display: flex; gap: 8px; align-items: center; }
.topic-tag { margin: 0 6px 6px 0; cursor: pointer; }
.mode-tip { margin-top: 12px; }
</style>