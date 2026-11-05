<template>
  <div class="lvp-vm">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>LVP 轻量虚拟机管理</span>
          <div class="actions">
            <el-button @click="reload" :loading="loading">刷新</el-button>
            <el-button type="primary" @click="openCreate">创建虚拟机</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="warning"
        :closable="false"
        show-icon
        class="mb12"
        title="当前为内存实现：服务重启后所有虚拟机实例会清空，仅供演示与联调"
      />

      <!-- 资源汇总 -->
      <div class="stat-row">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">实例总数</div>
          <div class="stat-value">{{ summary.total ?? 0 }}</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value">{{ summary.running ?? 0 }}</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">CPU 总核数</div>
          <div class="stat-value">{{ summary.totalVcpu ?? 0 }}</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">内存总量 (MB)</div>
          <div class="stat-value">{{ summary.totalMemoryMb ?? 0 }}</div>
        </el-card>
      </div>

      <el-table :data="rows" v-loading="loading" size="small" border class="mt12">
        <el-table-column prop="id" label="实例 ID" width="180" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="image" label="镜像" min-width="170" />
        <el-table-column label="规格" width="140" align="center">
          <template #default="{ row }">{{ row.vcpu }}C / {{ row.memoryMb }}MB</template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="stateTag(row.state)">{{ stateText(row.state) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="快照" width="80" align="center">
          <template #default="{ row }">{{ Object.keys(row.snapshots || {}).length }}</template>
        </el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button
              link type="success" size="small"
              :disabled="row.state === 'RUNNING'"
              @click="doStart(row)"
            >启动</el-button>
            <el-button
              link type="warning" size="small"
              :disabled="row.state !== 'RUNNING'"
              @click="doStop(row)"
            >停止</el-button>
            <el-button
              link type="primary" size="small"
              :disabled="row.state !== 'RUNNING'"
              @click="doSnapshot(row)"
            >快照</el-button>
            <el-button link type="danger" size="small" @click="doDestroy(row)">销毁</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无虚拟机，点击「创建虚拟机」开始" />
        </template>
      </el-table>
    </el-card>

    <!-- 创建 -->
    <el-dialog v-model="createVisible" title="创建轻量虚拟机" width="520px">
      <el-form :model="form" label-width="96px" size="small">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如 lvp-web-01" maxlength="64" />
        </el-form-item>
        <el-form-item label="镜像">
          <el-input v-model="form.image" placeholder="留空使用 csp/lvp-base:1.0" />
        </el-form-item>
        <el-form-item label="vCPU">
          <el-input-number v-model="form.vcpu" :min="1" :max="8" controls-position="right" />
          <span class="hint">1 ~ 8 核</span>
        </el-form-item>
        <el-form-item label="内存">
          <el-input-number v-model="form.memoryMb" :min="128" :max="4096" :step="128" controls-position="right" />
          <span class="hint">128 ~ 4096 MB</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { lvpApi } from '@/api'

const loading = ref(false)
const creating = ref(false)
const createVisible = ref(false)
const rows = ref([])
const summary = ref({})

const form = reactive({
  name: '',
  image: '',
  vcpu: 1,
  memoryMb: 512
})

function stateText(s) {
  return { CREATED: '已创建', RUNNING: '运行中', STOPPED: '已停止', DESTROYED: '已销毁' }[s] || s || '—'
}
function stateTag(s) {
  return { CREATED: 'info', RUNNING: 'success', STOPPED: 'warning', DESTROYED: 'danger' }[s] || 'info'
}

async function loadSummary() {
  try {
    summary.value = (await lvpApi.summary()) || {}
  } catch { summary.value = {} }
}

async function loadList() {
  loading.value = true
  try {
    const res = await lvpApi.list()
    rows.value = Array.isArray(res) ? res : (res?.records || [])
  } catch (e) {
    ElMessage.error(e.message || '加载虚拟机列表失败')
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function reload() {
  await Promise.all([loadList(), loadSummary()])
}

function openCreate() {
  form.name = ''
  form.image = ''
  form.vcpu = 1
  form.memoryMb = 512
  createVisible.value = true
}

async function submitCreate() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入虚拟机名称')
    return
  }
  creating.value = true
  try {
    await lvpApi.create({
      name: form.name.trim(),
      image: form.image.trim() || undefined,
      vcpu: form.vcpu,
      memoryMb: form.memoryMb
    })
    ElMessage.success('虚拟机创建成功')
    createVisible.value = false
    await reload()
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    creating.value = false
  }
}

async function doStart(row) {
  try {
    await lvpApi.start(row.id)
    ElMessage.success(`已启动 ${row.name}`)
    await reload()
  } catch (e) { ElMessage.error(e.message || '启动失败') }
}

async function doStop(row) {
  try {
    await lvpApi.stop(row.id)
    ElMessage.success(`已停止 ${row.name}`)
    await reload()
  } catch (e) { ElMessage.error(e.message || '停止失败') }
}

async function doSnapshot(row) {
  try {
    const tag = `snap-${Date.now().toString().slice(-6)}`
    await lvpApi.snapshot(row.id, tag)
    ElMessage.success(`快照 ${tag} 已创建`)
    await reload()
  } catch (e) { ElMessage.error(e.message || '快照失败') }
}

async function doDestroy(row) {
  try {
    await ElMessageBox.confirm(
      `销毁后该实例不可恢复，确认销毁「${row.name}」吗？`,
      '确认销毁',
      { type: 'warning' }
    )
  } catch { return }
  try {
    await lvpApi.destroy(row.id)
    ElMessage.success('已销毁')
    await reload()
  } catch (e) { ElMessage.error(e.message || '销毁失败') }
}

onMounted(reload)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 10px; align-items: center; }
.mb12 { margin-bottom: 12px; }
.mt12 { margin-top: 12px; }
.hint { margin-left: 10px; font-size: 12px; color: #86909c; }

.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-label { font-size: 13px; color: #86909c; margin-bottom: 6px; }
.stat-value { font-size: 24px; font-weight: 600; color: var(--csp-primary); line-height: 1; }
</style>