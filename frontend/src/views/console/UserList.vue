<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" clearable style="width:240px" @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="toggle(row)">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
          layout="total, prev, pager, next, sizes" @current-change="load" @size-change="load" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', status: '' })

async function load() {
  loading.value = true
  try {
    const data = await userApi.page(query)
    rows.value = data.list || data.records || []
    total.value = data.total || rows.value.length
  } finally {
    loading.value = false
  }
}
function reset() { query.keyword = ''; query.status = ''; query.page = 1; load() }

async function toggle(row) {
  await userApi.updateStatus(row.id, row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE')
  ElMessage.success('已更新')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除用户 ${row.username}？`, '提示', { type: 'warning' })
  await userApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; display: flex; justify-content: flex-end; }
</style>