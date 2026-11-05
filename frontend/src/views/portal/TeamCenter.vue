<template>
  <div class="team-center">
    <el-row :gutter="16">
      <!-- 左侧：我的战队 -->
      <el-col :lg="8" :md="24" :sm="24" :xs="24">
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">我的战队</span>
              <el-button type="primary" size="small" :icon="Plus" @click="openCreate">创建战队</el-button>
            </div>
          </template>

          <el-empty v-if="!myTeams.length" description="还没有战队，创建一个或凭邀请码加入" :image-size="90" />
          <div v-else class="team-list">
            <div v-for="t in myTeams" :key="t.id" class="team-item" :class="{ active: t.id === selectedId }"
                 @click="select(t.id)">
              <el-avatar :size="42" :src="t.logo">{{ (t.name || '?').slice(0, 1) }}</el-avatar>
              <div class="team-item-main">
                <div class="team-item-name">
                  {{ t.name }}
                  <el-tag v-if="t.myRole === 'CAPTAIN'" size="small" type="warning" effect="dark">队长</el-tag>
                  <el-tag v-else size="small" type="info">队员</el-tag>
                </div>
                <div class="team-item-sub">{{ t.members || 0 }}/{{ t.maxMembers || 8 }} 名成员</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="side-card" style="margin-top: 16px">
          <template #header><span class="card-title">加入战队</span></template>
          <el-input v-model="joinCode" placeholder="输入战队邀请码" maxlength="8" clearable>
            <template #append>
              <el-button :loading="joining" @click="joinTeam">加入</el-button>
            </template>
          </el-input>
          <div class="hint">邀请码由队长在战队详情页生成，8 位大写字母与数字。</div>
        </el-card>
      </el-col>

      <!-- 右侧：详情 / 发现战队 -->
      <el-col :lg="16" :md="24" :sm="24" :xs="24">
        <el-card shadow="never">
          <el-tabs v-model="tab">
            <el-tab-pane label="战队详情" name="detail">
              <el-empty v-if="!current" description="选择左侧战队查看详情" :image-size="100" />
              <template v-else>
                <div class="team-hero">
                  <el-avatar :size="72" :src="current.logo">{{ (current.name || '?').slice(0, 1) }}</el-avatar>
                  <div class="hero-main">
                    <div class="hero-name">
                      {{ current.name }}
                      <el-tag size="small" :type="current.myRole === 'CAPTAIN' ? 'warning' : 'info'" effect="dark">
                        {{ current.myRole === 'CAPTAIN' ? '我是队长' : '我是队员' }}
                      </el-tag>
                    </div>
                    <div class="hero-slogan">{{ current.slogan || '还没有战队口号' }}</div>
                    <div class="hero-desc">{{ current.description || '还没有战队简介' }}</div>
                  </div>
                  <div class="hero-metrics">
                    <div class="metric"><b>{{ current.members || 0 }}</b><span>成员</span></div>
                    <div class="metric"><b>{{ current.maxMembers || 8 }}</b><span>上限</span></div>
                    <div class="metric"><b>{{ enrolledCount }}</b><span>参赛中</span></div>
                  </div>
                </div>

                <div class="hero-actions">
                  <template v-if="isCaptain">
                    <el-button :icon="Edit" @click="openEdit">编辑资料</el-button>
                    <el-button :icon="Refresh" @click="regenCode">刷新邀请码</el-button>
                    <el-button type="danger" plain :icon="Delete" @click="disband">解散战队</el-button>
                  </template>
                  <el-button v-else type="warning" plain @click="quit">退出战队</el-button>
                </div>

                <el-divider content-position="left">邀请码</el-divider>
                <div class="invite-row">
                  <el-input :model-value="current.inviteCode || '未生成'" readonly>
                    <template #append>
                      <el-button :icon="DocumentCopy" @click="copyCode">复制</el-button>
                    </template>
                  </el-input>
                </div>

                <el-divider content-position="left">成员（{{ members.length }}）</el-divider>
                <div class="member-grid">
                  <div v-for="m in members" :key="m.userId" class="member-card">
                    <el-avatar :size="40" :src="m.avatar">{{ (m.nickname || m.username || '?').slice(0, 1) }}</el-avatar>
                    <div class="member-info">
                      <div class="member-name">
                        {{ m.nickname || m.username }}
                        <el-tag v-if="m.role === 'CAPTAIN'" size="small" type="warning">队长</el-tag>
                      </div>
                      <div class="member-sub">{{ m.orgName || m.orgCode || '—' }}</div>
                    </div>
                    <div class="member-ops" v-if="isCaptain && m.role !== 'CAPTAIN'">
                      <el-button link type="primary" size="small" @click="transferTo(m)">设为队长</el-button>
                      <el-button link type="danger" size="small" @click="kick(m)">移出</el-button>
                    </div>
                  </div>
                </div>
              </template>
            </el-tab-pane>

            <el-tab-pane label="发现战队" name="browse">
              <div class="browse-bar">
                <el-input v-model="keyword" placeholder="搜索战队名" clearable style="width: 260px"
                          @keyup.enter="loadBrowse" @clear="loadBrowse" />
                <el-button :icon="Search" @click="loadBrowse">搜索</el-button>
              </div>
              <el-table :data="browseList" v-loading="browseLoading" empty-text="暂无战队">
                <el-table-column label="战队" min-width="200">
                  <template #default="{ row }">
                    <div class="row-team">
                      <el-avatar :size="30" :src="row.logo">{{ (row.name || '?').slice(0, 1) }}</el-avatar>
                      <span>{{ row.name }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="slogan" label="口号" min-width="160" show-overflow-tooltip />
                <el-table-column label="成员" width="100">
                  <template #default="{ row }">{{ row.members || 0 }}/{{ row.maxMembers || 8 }}</template>
                </el-table-column>
                <el-table-column label="操作" width="140" align="right">
                  <template #default="{ row }">
                    <el-button v-if="row.myRole" link type="info" size="small" @click="select(row.id)">已在队中</el-button>
                    <el-button v-else link type="primary" size="small" @click="askJoin(row)">要邀请码</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建 / 编辑 -->
    <el-dialog v-model="formVisible" :title="formMode === 'create' ? '创建战队' : '编辑战队'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="战队名" prop="name">
          <el-input v-model="form.name" maxlength="32" placeholder="全局唯一" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-input v-model="form.logo" placeholder="图片地址，可留空" />
        </el-form-item>
        <el-form-item label="口号">
          <el-input v-model="form.slogan" maxlength="60" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.maxMembers" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, DocumentCopy, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { teamApi } from '@/api'
const tab = ref('detail')
const myTeams = ref([])
const selectedId = ref(null)
const members = ref([])
const enrolledCount = ref(0)
const browseList = ref([])
const browseLoading = ref(false)
const keyword = ref('')
const joinCode = ref('')
const joining = ref(false)

const formVisible = ref(false)
const formMode = ref('create')
const saving = ref(false)
const formRef = ref()
const form = reactive({ name: '', logo: '', slogan: '', description: '', maxMembers: 8 })
const rules = { name: [{ required: true, message: '请输入战队名', trigger: 'blur' }] }

const current = computed(() => myTeams.value.find(t => String(t.id) === String(selectedId.value)) || null)
const isCaptain = computed(() => current.value?.myRole === 'CAPTAIN')

async function loadMine(keepSelection = true) {
  myTeams.value = await teamApi.mine() || []
  if (!keepSelection || !current.value) {
    selectedId.value = myTeams.value[0]?.id || null
  }
  await loadDetail()
}

async function loadDetail() {
  members.value = []
  enrolledCount.value = 0
  if (!selectedId.value) return
  const t = await teamApi.detail(selectedId.value)
  if (t) {
    const idx = myTeams.value.findIndex(x => String(x.id) === String(t.id))
    if (idx >= 0) myTeams.value[idx] = { ...myTeams.value[idx], ...t }
    enrolledCount.value = t.enrolledContests || 0
  }
  members.value = await teamApi.members(selectedId.value) || []
}

async function select(id) {
  selectedId.value = id
  tab.value = 'detail'
  await loadDetail()
}

async function loadBrowse() {
  browseLoading.value = true
  try {
    browseList.value = await teamApi.browse({ keyword: keyword.value, limit: 50 }) || []
  } finally {
    browseLoading.value = false
  }
}

function openCreate() {
  formMode.value = 'create'
  Object.assign(form, { name: '', logo: '', slogan: '', description: '', maxMembers: 8 })
  formVisible.value = true
}

function openEdit() {
  formMode.value = 'edit'
  const t = current.value
  Object.assign(form, {
    name: t.name, logo: t.logo || '', slogan: t.slogan || '',
    description: t.description || '', maxMembers: t.maxMembers || 8
  })
  formVisible.value = true
}

async function submitForm() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (formMode.value === 'create') {
      const t = await teamApi.create({ ...form })
      ElMessage.success('战队已创建')
      await loadMine(false)
      selectedId.value = t.id
      await loadDetail()
    } else {
      await teamApi.update(selectedId.value, { ...form })
      ElMessage.success('已保存')
      await loadMine()
    }
    formVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function joinTeam() {
  const code = (joinCode.value || '').trim().toUpperCase()
  if (code.length < 6) return ElMessage.warning('请输入正确的邀请码')
  joining.value = true
  try {
    await teamApi.join(code)
    ElMessage.success('已加入战队')
    joinCode.value = ''
    await loadMine(false)
  } catch (e) {
    ElMessage.error(e.message || '加入失败')
  } finally {
    joining.value = false
  }
}

function askJoin(row) {
  ElMessageBox.prompt(`向「${row.name}」队长索取邀请码`, '加入战队', {
    inputPlaceholder: '8 位邀请码',
    inputPattern: /^[A-Z0-9]{6,8}$/,
    inputErrorMessage: '邀请码格式不正确'
  }).then(async ({ value }) => {
    await teamApi.join(value.trim().toUpperCase())
    ElMessage.success('已加入战队')
    await loadMine(false)
    selectedId.value = row.id
    await loadDetail()
  }).catch(() => {})
}

async function quit() {
  await ElMessageBox.confirm('确定退出该战队？', '提示', { type: 'warning' })
  try {
    await teamApi.quit(current.value.id)
    ElMessage.success('已退出')
    await loadMine(false)
  } catch (e) {
    ElMessage.error(e.message || '退出失败')
  }
}

async function disband() {
  await ElMessageBox.confirm(`解散战队「${current.value.name}」后不可恢复，确定？`, '危险操作', { type: 'error' })
  try {
    await teamApi.disband(current.value.id)
    ElMessage.success('战队已解散')
    await loadMine(false)
  } catch (e) {
    ElMessage.error(e.message || '解散失败')
  }
}

async function regenCode() {
  try {
    const code = await teamApi.refreshInviteCode(current.value.id)
    current.value.inviteCode = code
    ElMessage.success('邀请码已刷新')
  } catch (e) {
    ElMessage.error(e.message || '刷新失败')
  }
}

async function copyCode() {
  const code = current.value?.inviteCode
  if (!code) return ElMessage.warning('还没有邀请码')
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('已复制邀请码')
  } catch {
    ElMessage.info(`邀请码：${code}`)
  }
}

async function transferTo(m) {
  await ElMessageBox.confirm(`将队长转让给 ${m.nickname || m.username}？`, '转让队长', { type: 'warning' })
  try {
    await teamApi.transfer(current.value.id, m.userId)
    ElMessage.success('已转让队长')
    await loadMine()
    await loadDetail()
  } catch (e) {
    ElMessage.error(e.message || '转让失败')
  }
}

async function kick(m) {
  await ElMessageBox.confirm(`将 ${m.nickname || m.username} 移出战队？其赛事参赛资格同步撤销。`, '移除成员', { type: 'warning' })
  try {
    await teamApi.kick(current.value.id, m.userId)
    ElMessage.success('已移除')
    await loadMine()
    await loadDetail()
  } catch (e) {
    ElMessage.error(e.message || '移除失败')
  }
}

onMounted(async () => {
  await loadMine(false)
  await loadBrowse()
})
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-weight: 600; }
.hint { margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary); }
.team-list { display: flex; flex-direction: column; gap: 8px; }
.team-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter); border-radius: 8px; cursor: pointer;
}
.team-item:hover { background: var(--el-fill-color-light); }
.team-item.active { border-color: var(--el-color-primary); background: var(--el-color-primary-light-9); }
.team-item-main { flex: 1; min-width: 0; }
.team-item-name { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.team-item-sub { font-size: 12px; color: var(--el-text-color-secondary); }
.team-hero { display: flex; align-items: center; gap: 16px; }
.hero-main { flex: 1; min-width: 0; }
.hero-name { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: 600; }
.hero-slogan { margin-top: 4px; color: var(--el-color-primary); font-size: 13px; }
.hero-desc { margin-top: 4px; font-size: 12px; color: var(--el-text-color-secondary); }
.hero-metrics { display: flex; gap: 24px; }
.metric { text-align: center; }
.metric b { display: block; font-size: 20px; }
.metric span { font-size: 12px; color: var(--el-text-color-secondary); }
.hero-actions { margin-top: 16px; display: flex; gap: 8px; }
.member-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 10px; }
.member-card {
  display: flex; align-items: center; gap: 10px; padding: 10px;
  border: 1px solid var(--el-border-color-lighter); border-radius: 8px;
}
.member-info { flex: 1; min-width: 0; }
.member-name { display: flex; align-items: center; gap: 6px; font-weight: 500; }
.member-sub { font-size: 12px; color: var(--el-text-color-secondary); }
.browse-bar { display: flex; gap: 8px; margin-bottom: 12px; }
.row-team { display: flex; align-items: center; gap: 8px; }
</style>
