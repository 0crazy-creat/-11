<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索战队" clearable style="width:240px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="战队名" min-width="160" />
        <el-table-column prop="captainName" label="队长" width="120" />
        <el-table-column prop="members" label="人数" width="80" />
        <el-table-column prop="score" label="总分" width="100" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/team/${row.id}`)">主页</el-button>
            <el-button link type="danger" @click="remove(row)">解散</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teamApi } from '@/api'

const loading = ref(false)
const rows = ref([])
const query = reactive({ keyword: '' })

async function load() {
  loading.value = true
  try {
    const data = await teamApi.list({ page: 1, size: 100, ...query })
    rows.value = data.list || data.records || data || []
  } finally { loading.value = false }
}
async function remove(row) {
  await ElMessageBox.confirm(`解散战队「${row.name}」？`, '提示', { type: 'warning' })
  await teamApi.remove(row.id)
  ElMessage.success('已解散')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
</style>