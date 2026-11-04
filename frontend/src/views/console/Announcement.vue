<template>
  <div>
    <el-card shadow="never" header="公告管理">
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">发布公告</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="scope" label="范围" width="110" />
        <el-table-column prop="createdAt" label="发布时间" min-width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="范围">
          <el-select v-model="form.scope">
            <el-option label="全平台" value="GLOBAL" />
            <el-option label="指定赛事" value="CONTEST" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.scope === 'CONTEST'" label="赛事ID"><el-input v-model="form.contestId" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { announceApi } from '@/api'

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const rows = ref([])
const form = reactive({ id: null, title: '', scope: 'GLOBAL', contestId: '', content: '' })

async function load() {
  loading.value = true
  try {
    const data = await announceApi.list({ page: 1, size: 100 })
    rows.value = data.list || data.records || data || []
  } finally { loading.value = false }
}
function openCreate() { Object.assign(form, { id: null, title: '', scope: 'GLOBAL', contestId: '', content: '' }); visible.value = true }
function openEdit(row) { Object.assign(form, row); visible.value = true }
async function save() {
  saving.value = true
  try {
    if (form.id) await announceApi.update(form.id, form)
    else await announceApi.create(form)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally { saving.value = false }
}
async function remove(row) {
  await ElMessageBox.confirm(`删除公告「${row.title}」？`, '提示', { type: 'warning' })
  await announceApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 14px; }
</style>