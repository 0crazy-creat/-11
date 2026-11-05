<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索战队" clearable style="width:240px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column label="战队" min-width="180">
          <template #default="{ row }">
            <div class="team-cell">
              <el-avatar :size="28" :src="row.logo">{{ (row.name || '?').slice(0, 1) }}</el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="slogan" label="口号" min-width="150" show-overflow-tooltip />
        <el-table-column label="成员" width="90">
          <template #default="{ row }">{{ row.members || 0 }}/{{ row.maxMembers || 8 }}</template>
        </el-table-column>
        <el-table-column label="参赛赛事" width="100">
          <template #default="{ row }">{{ row.enrolledContests || 0 }}</template>
        </el-table-column>
        <el-table-column label="邀请码" width="120">
          <template #default="{ row }">{{ row.inviteCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewMembers(row)">成员</el-button>
            <el-button link type="danger" @click="remove(row)">强制解散</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="memberVisible" :title="`「${active?.name || ''}」成员`" width="520px">
      <el-table :data="memberRows" size="small">
        <el-table-column label="成员" min-width="140">
          <template #default="{ row }">{{ row.nickname || row.username }}</template>
        </el-table-column>
        <el-table-column prop="orgName" label="单位" min-width="120" show-overflow-tooltip />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'CAPTAIN' ? 'warning' : 'info'">
              {{ row.role === 'CAPTAIN' ? '队长' : '队员' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teamApi } from '@/api'

const loading = ref(false)
const rows = ref([])
const query = reactive({ keyword: '' })
const memberVisible = ref(false)
const memberRows = ref([])
const active = ref(null)

async function load() {
  loading.value = true
  try {
    const data = await teamApi.manageList({ page: 1, size: 100, ...query })
    rows.value = data?.records || data?.list || []
  } finally { loading.value = false }
}

async function viewMembers(row) {
  active.value = row
  memberRows.value = await teamApi.members(row.id) || []
  memberVisible.value = true
}

async function remove(row) {
  await ElMessageBox.confirm(`强制解散战队「${row.name}」？其参赛记录同步撤下。`, '危险操作', { type: 'error' })
  await teamApi.forceDisband(row.id)
  ElMessage.success('已解散')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.team-cell { display: flex; align-items: center; gap: 8px; }
</style>
