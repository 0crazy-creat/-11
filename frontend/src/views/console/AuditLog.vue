<template>
  <div>
    <div class="csp-page-head">
      <h2 class="csp-h2">审计日志</h2>
      <div class="tools">
        <el-input v-model="kw" placeholder="按动作/资源筛选" clearable style="width: 220px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="exportCsv">导出 CSV</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top: 16px" empty-text="暂无日志">
      <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
      <el-table-column prop="action" label="动作" width="180" />
      <el-table-column prop="resource" label="资源" width="140" />
      <el-table-column prop="resourceId" label="资源ID" width="180" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
    </el-table>

    <el-pagination
      class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" :current-page="page" :page-size="size" :page-sizes="[10, 20, 50]"
      @current-change="onPage" @size-change="onSize" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const kw = ref('')

async function load() {
  loading.value = true
  try {
    const data = await request.get('/audit-logs', {
      params: { page: page.value, size: size.value, keyword: kw.value || undefined }
    })
    list.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    list.value = []
  } finally { loading.value = false }
}
function onPage(p) { page.value = p; load() }
function onSize(s) { size.value = s; page.value = 1; load() }
function exportCsv() {
  const token = localStorage.getItem('csp_token') || ''
  window.open(`/api/export/submissions.csv?token=${encodeURIComponent(token)}`, '_blank')
}
onMounted(load)
</script>

<style scoped>
.csp-page-head { display: flex; justify-content: space-between; align-items: center }
.tools { display: flex; gap: 8px }
.pager { margin-top: 16px; justify-content: flex-end }
</style>
