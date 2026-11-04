<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索赛事" clearable style="width:240px" @keyup.enter="load" />
        <el-select v-model="query.type" placeholder="类型" clearable style="width:130px">
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openCreate">新建赛事</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="赛事名称" min-width="180" />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="160" />
        <el-table-column prop="endTime" label="结束时间" min-width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="$router.push(`/console/contests/${row.id}`)">详情</el-button>
            <el-button link type="warning" @click="$router.push(`/play/${row.id}`)">答题</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="新建赛事" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type"><el-option v-for="t in types" :key="t" :label="t" :value="t" /></el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="range" type="datetimerange" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑赛事" width="600px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="editForm.code" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editForm.type">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option label="筹备中" value="DRAFT" />
            <el-option label="未开始" value="PENDING" />
            <el-option label="报名中" value="SIGNUP" />
            <el-option label="进行中" value="RUNNING" />
            <el-option label="已结束" value="FINISHED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="editRange" type="datetimerange" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="updateContest">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contestApi } from '@/api'

const types = ['CTF', 'AWD', 'AWDP', 'THEORY', 'SCENARIO', 'CFS']
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editVisible = ref(false)
const editSaving = ref(false)
const rows = ref([])
const range = ref([])
const editRange = ref([])
const query = reactive({ keyword: '', type: '' })
const form = reactive({ name: '', code: '', type: 'CTF', description: '' })
const editForm = reactive({ id: null, name: '', code: '', type: 'CTF', status: 'DRAFT', description: '' })

function statusType(s) { return { RUNNING: 'success', PENDING: 'info', FINISHED: 'warning' }[s] || 'info' }

async function load() {
  loading.value = true
  try {
    const data = await contestApi.list({ page: 1, size: 100, ...query })
    rows.value = data.list || data.records || data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { name: '', code: '', type: 'CTF', description: '' })
  range.value = []
  visible.value = true
}
async function save() {
  saving.value = true
  try {
    const payload = { ...form }
    if (range.value?.length === 2) { payload.startTime = range.value[0]; payload.endTime = range.value[1] }
    await contestApi.create(payload)
    ElMessage.success('已创建')
    visible.value = false
    load()
  } finally { saving.value = false }
}
function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    name: row.name,
    code: row.code,
    type: row.type,
    status: row.status,
    description: row.description
  })
  editRange.value = row.startTime && row.endTime ? [new Date(row.startTime), new Date(row.endTime)] : []
  editVisible.value = true
}
async function updateContest() {
  editSaving.value = true
  try {
    const payload = { ...editForm }
    if (editRange.value?.length === 2) {
      payload.startTime = editRange.value[0]
      payload.endTime = editRange.value[1]
    }
    await contestApi.update(editForm.id, payload)
    ElMessage.success('已更新')
    editVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.message || '更新失败')
  } finally { editSaving.value = false }
}
async function remove(row) {
  await ElMessageBox.confirm(`删除赛事「${row.name}」？`, '提示', { type: 'warning' })
  await contestApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
</style>