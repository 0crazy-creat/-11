<template>
  <div class="my-team-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的战队</span>
          <el-button type="primary" @click="showCreateDialog = true" v-if="!myTeam && contestId">创建战队</el-button>
        </div>
      </template>

      <div v-if="loading" v-loading="true" style="min-height: 200px" />

      <div v-else-if="!myTeam && !contestId" class="empty-state">
        <el-empty description="请先选择一个赛事">
          <el-select v-model="selectedContestId" placeholder="选择赛事" filterable @change="onContestChange" style="width: 300px">
            <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id">
              <span>{{ c.name }}</span>
              <el-tag size="small" :color="contestMeta(c.type)?.color" style="margin-left: 8px; color: #fff; border: none">
                {{ contestMeta(c.type)?.short || c.type }}
              </el-tag>
            </el-option>
          </el-select>
        </el-empty>
      </div>

      <div v-else-if="!myTeam && contestId" class="empty-state">
        <el-empty description="你尚未加入该赛事的战队">
          <el-button type="primary" @click="showCreateDialog = true">创建新战队</el-button>
          <el-button @click="showJoinDialog = true">通过邀请码加入</el-button>
        </el-empty>
      </div>

      <div v-else>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="战队名">{{ myTeam?.name }}</el-descriptions-item>
          <el-descriptions-item label="战队ID">{{ myTeam?.id }}</el-descriptions-item>
          <el-descriptions-item label="队长">{{ myTeam?.captainName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(myTeam?.status)">{{ statusLabel(myTeam?.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属赛事">{{ currentContestName }}</el-descriptions-item>
          <el-descriptions-item label="人数">{{ members?.length || 0 }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">战队成员</div>
        <el-table :data="members || []" size="small" border>
          <el-table-column prop="userId" label="用户ID" width="200" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="nickname" label="昵称" width="150" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="String(row.userId) === String(myTeam?.captainId) ? 'danger' : ''">
                {{ String(row.userId) === String(myTeam?.captainId) ? '队长' : '队员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="joinedAt" label="加入时间" />
        </el-table>

        <div class="section-title" v-if="isCaptain">邀请码</div>
        <div v-if="isCaptain" class="invite-section">
          <el-input v-model="inviteCode" readonly style="width: 320px">
            <template #append>
              <el-button @click="generateCode" :loading="codeLoading">生成邀请码</el-button>
              <el-button @click="copyCode">复制</el-button>
            </template>
          </el-input>
        </div>

        <div class="actions" v-if="isCaptain">
          <el-button type="danger" plain @click="disbandTeam">解散战队</el-button>
        </div>
        <div class="actions" v-else>
          <el-button type="danger" plain @click="quitTeam">退出战队</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-16" v-if="!contestId">
      <template #header><span class="card-title">可参加的赛事</span></template>
      <el-table :data="contests" size="small" v-loading="contestsLoading">
        <el-table-column prop="name" label="赛事名称" min-width="200" />
        <el-table-column prop="type" label="赛制" width="120">
          <template #default="{ row }">
            <el-tag size="small" :color="contestMeta(row.type)?.color" style="color: #fff; border: none">
              {{ contestMeta(row.type)?.short || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'RUNNING' || row.status === 'SIGNUP' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="onContestChange(row.id)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="创建战队" width="420px">
      <el-form @submit.prevent="createTeam">
        <el-form-item label="战队名称">
          <el-input v-model="newTeamName" placeholder="输入战队名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createTeam" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showJoinDialog" title="加入战队" width="420px">
      <el-form @submit.prevent="joinByCode">
        <el-form-item label="邀请码">
          <el-input v-model="joinInviteCode" placeholder="输入邀请码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" @click="joinByCode" :loading="joinLoading">加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getContestType } from '@/constants/contestType'
import request from '@/utils/request'

const user = useUserStore()
const loading = ref(false)
const contestsLoading = ref(false)
const createLoading = ref(false)
const joinLoading = ref(false)
const codeLoading = ref(false)

const contestId = ref(null)
const selectedContestId = ref(null)
const myTeam = ref(null)
const members = ref([])
const contests = ref([])
const inviteCode = ref('')
const showCreateDialog = ref(false)
const showJoinDialog = ref(false)
const newTeamName = ref('')
const joinInviteCode = ref('')

const isCaptain = computed(() => {
  if (!myTeam.value) return false
  return String(myTeam.value.captainId) === String(user.profile?.id)
})

const currentContestName = computed(() => {
  const c = contests.value.find(x => String(x.id) === String(contestId.value))
  return c?.name || ''
})

const contestMeta = (type) => getContestType(type)

function statusType(s) {
  return { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger' }[s] || 'info'
}
function statusLabel(s) {
  return { APPROVED: '已审核', PENDING: '待审核', REJECTED: '已拒绝' }[s] || s || '-'
}

async function loadContests() {
  contestsLoading.value = true
  try {
    const data = await request.get('/contests', { params: { page: 1, size: 100 } })
    contests.value = data?.records || data || []
  } finally {
    contestsLoading.value = false
  }
}

async function onContestChange(cid) {
  const id = cid || selectedContestId.value
  contestId.value = id
  selectedContestId.value = id
  await loadMyTeam()
}

async function loadMyTeam() {
  if (!contestId.value) return
  loading.value = true
  try {
    const data = await request.get(`/ctf/contests/${contestId.value}/teams/mine`)
    myTeam.value = data || null
    if (myTeam.value) {
      await loadMembers()
      await loadInviteCode()
    }
  } catch {
    myTeam.value = null
  } finally {
    loading.value = false
  }
}

async function loadMembers() {
  if (!myTeam.value) return
  try {
    const data = await request.get(`/ctf/contests/${contestId.value}/teams/${myTeam.value.id}/members`)
    members.value = data || []
  } catch {
    members.value = []
  }
}

async function loadInviteCode() {
  if (!isCaptain.value || !myTeam.value) return
  try {
    const data = await request.post(`/ctf/contests/${contestId.value}/teams/${myTeam.value.id}/invite-code`)
    inviteCode.value = data || ''
  } catch { /* ignore */ }
}

async function createTeam() {
  if (!newTeamName.value.trim()) {
    ElMessage.warning('请输入战队名称')
    return
  }
  createLoading.value = true
  try {
    await request.post(`/ctf/contests/${contestId.value}/teams`, {
      contestId: String(contestId.value),
      name: newTeamName.value.trim()
    })
    ElMessage.success('战队创建成功')
    showCreateDialog.value = false
    newTeamName.value = ''
    await loadMyTeam()
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    createLoading.value = false
  }
}

async function joinByCode() {
  if (!joinInviteCode.value.trim()) {
    ElMessage.warning('请输入邀请码')
    return
  }
  joinLoading.value = true
  try {
    await request.post(`/ctf/contests/${contestId.value}/teams/join-by-code`, null, {
      params: { inviteCode: joinInviteCode.value.trim() }
    })
    ElMessage.success('已加入战队')
    showJoinDialog.value = false
    joinInviteCode.value = ''
    await loadMyTeam()
  } catch (e) {
    ElMessage.error(e.message || '加入失败')
  } finally {
    joinLoading.value = false
  }
}

async function generateCode() {
  codeLoading.value = true
  try {
    const data = await request.put(`/ctf/contests/${contestId.value}/teams/${myTeam.value.id}/invite-code`)
    inviteCode.value = data || ''
    ElMessage.success('邀请码已刷新')
  } catch (e) {
    ElMessage.error(e.message || '生成失败')
  } finally {
    codeLoading.value = false
  }
}

function copyCode() {
  navigator.clipboard.writeText(inviteCode.value).then(() => ElMessage.success('已复制'))
}

async function disbandTeam() {
  await ElMessageBox.confirm('确定解散战队？此操作不可恢复', '警告', { type: 'warning' })
  try {
    await request.delete(`/ctf/contests/${contestId.value}/teams/${myTeam.value.id}`)
    ElMessage.success('战队已解散')
    myTeam.value = null
    members.value = []
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function quitTeam() {
  await ElMessageBox.confirm('确定退出战队？', '提示', { type: 'warning' })
  try {
    await request.delete(`/ctf/contests/${contestId.value}/teams/quit`)
    ElMessage.success('已退出战队')
    myTeam.value = null
    members.value = []
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => {
  loadContests()
})
</script>

<style scoped>
.my-team-page { max-width: 900px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; font-size: 16px; }
.section-title { font-weight: 600; font-size: 14px; margin: 16px 0 8px; color: #303133; }
.invite-section { margin-bottom: 16px; }
.actions { margin-top: 16px; }
.mt-16 { margin-top: 16px; }
.empty-state { padding: 40px 0; }
</style>
