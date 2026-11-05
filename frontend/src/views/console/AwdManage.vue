<template>
  <div class="awd-manage">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span class="title">AWD 赛事管理</span>
          <el-select v-model="contestId" placeholder="选择 AWD 赛事" style="width: 240px" @change="reload">
            <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
      </template>

      <el-empty v-if="!contestId" description="请选择一场 AWD 赛事" />

      <template v-else>
        <div class="toolbar">
          <el-button type="primary" :loading="busy" @click="provisionAll">一键部署全部靶机</el-button>
          <el-button :loading="busy" @click="healthCheck">健康检查</el-button>
          <el-button :loading="busy" @click="runChecker">执行 Checker 检测</el-button>
          <el-button type="danger" plain :loading="busy" @click="destroyAll">销毁全部靶机</el-button>
          <span class="stat">靶机 {{ targets.length }} 台 · 服务正常 {{ upCount }} 台</span>
        </div>

        <el-table :data="targets" size="small" v-loading="loading">
          <el-table-column label="队伍" width="140">
            <template #default="{ row }">{{ row.teamName || row.teamId }}</template>
          </el-table-column>
          <el-table-column label="题目" min-width="150">
            <template #default="{ row }">{{ row.questionTitle || row.questionId }}</template>
          </el-table-column>
          <el-table-column label="服务地址" width="190">
            <template #default="{ row }">
              <a v-if="row.accessUrl" :href="row.accessUrl" target="_blank">{{ row.accessUrl }}</a>
              <span v-else class="muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="SSH" width="210">
            <template #default="{ row }">
              <span v-if="row.sshPort" class="mono">{{ row.sshUser || 'root' }}@{{ host }}:{{ row.sshPort }}</span>
              <span v-else class="muted">未启用</span>
            </template>
          </el-table-column>
          <el-table-column label="SSH 密码" width="130">
            <template #default="{ row }">
              <span v-if="row.sshPassword" class="mono">{{ row.sshPassword }}</span>
              <span v-else class="muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="服务检测" width="230">
            <template #default="{ row }">
              <el-tag v-if="row.serviceOk != null" size="small" :type="row.serviceOk === 1 ? 'success' : 'danger'">
                {{ row.serviceOk === 1 ? '正常' : '异常' }}
              </el-tag>
              <span class="msg">{{ row.lastCheckMsg }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="resetTarget(row)">重置</el-button>
              <el-button link type="danger" @click="destroyTarget(row)">销毁</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">Checker 配置</el-divider>
        <div class="toolbar">
          <el-button size="small" type="primary" plain @click="addChecker">新增 Checker</el-button>
          <span class="stat">每 60 秒自动巡检一次，结果写入靶机「服务检测」列</span>
        </div>
        <el-table :data="checkers" size="small" v-loading="loading">
          <el-table-column label="题目" min-width="180">
            <template #default="{ row }">{{ questionTitle(row.questionId) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.checkType === 'TCP' ? 'warning' : 'info'">{{ row.checkType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetPath" label="路径" width="120" />
          <el-table-column prop="expectStatus" label="期望状态" width="100" />
          <el-table-column prop="timeoutMs" label="超时(ms)" width="100" />
          <el-table-column label="启用" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="editChecker(row)">编辑</el-button>
              <el-button link type="danger" @click="removeChecker(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <el-dialog v-model="checkerVisible" title="Checker 配置" width="560px" destroy-on-close>
      <el-form :model="checkerForm" label-width="100px">
        <el-form-item label="题目" required>
          <el-select v-model="checkerForm.questionId" style="width: 100%" placeholder="选择 AWD 题目">
            <el-option v-for="q in awdQuestions" :key="q.id" :label="q.title" :value="q.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测类型">
          <el-radio-group v-model="checkerForm.checkType">
            <el-radio-button value="HTTP">HTTP</el-radio-button>
            <el-radio-button value="TCP">TCP</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="检测路径">
          <el-input v-model="checkerForm.targetPath" placeholder="/" :disabled="checkerForm.checkType === 'TCP'" />
        </el-form-item>
        <el-form-item label="期望状态码">
          <el-input-number v-model="checkerForm.expectStatus" :min="100" :max="599" :disabled="checkerForm.checkType === 'TCP'" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="checkerForm.timeoutMs" :min="500" :max="30000" :step="500" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="checkerForm.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveChecker">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const contests = ref([])
const contestId = ref(null)
const targets = ref([])
const checkers = ref([])
const awdQuestions = ref([])
const loading = ref(false)
const busy = ref(false)
const saving = ref(false)
const checkerVisible = ref(false)

const host = window.location.hostname || '127.0.0.1'

const checkerForm = reactive({
  id: null, contestId: null, questionId: null,
  checkType: 'HTTP', targetPath: '/', expectStatus: 200, timeoutMs: 3000, enabled: 1
})

const upCount = computed(() => targets.value.filter(t => t.serviceOk === 1).length)

function questionTitle(id) {
  const q = awdQuestions.value.find(x => String(x.id) === String(id))
  return q ? q.title : ('#' + id)
}

async function loadContests() {
  const data = await request({ url: '/contests', method: 'get', params: { page: 1, size: 100 } })
  const list = data?.records || data?.list || []
  contests.value = list.filter(c => ['AWD', 'AWDP'].includes(c.type))
  if (contests.value.length) {
    contestId.value = contests.value[0].id
    await reload()
  }
}

async function loadTargets() {
  targets.value = await request({ url: '/awd/targets', method: 'get', params: { contestId: contestId.value } }) || []
}

async function loadCheckers() {
  checkers.value = await request({ url: '/awd/checkers', method: 'get', params: { contestId: contestId.value } }) || []
}

async function loadQuestions() {
  const data = await request({ url: '/questions', method: 'get', params: { page: 1, size: 100, bankType: 'AWD' } })
  awdQuestions.value = data?.records || []
}

async function reload() {
  if (!contestId.value) return
  loading.value = true
  try {
    await Promise.all([loadTargets(), loadCheckers(), loadQuestions()])
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function provisionAll() {
  busy.value = true
  try {
    const r = await request({ url: '/awd/targets/provision-all', method: 'post', params: { contestId: contestId.value } })
    ElMessage.success(`已部署 ${r?.provisioned ?? r ?? 0} 台靶机`)
    await loadTargets()
  } catch (e) {
    ElMessage.error(e.message || '部署失败')
  } finally {
    busy.value = false
  }
}

async function healthCheck() {
  busy.value = true
  try {
    await request({ url: '/awd/targets/health-check', method: 'post', params: { contestId: contestId.value } })
    ElMessage.success('健康检查完成')
    await loadTargets()
  } catch (e) {
    ElMessage.error(e.message || '检查失败')
  } finally {
    busy.value = false
  }
}

async function runChecker() {
  busy.value = true
  try {
    const r = await request({ url: '/awd/checkers/run', method: 'post', params: { contestId: contestId.value } })
    ElMessage.success(`检测完成：正常 ${r?.up ?? 0} / 异常 ${r?.down ?? 0}`)
    await loadTargets()
  } catch (e) {
    ElMessage.error(e.message || '检测失败')
  } finally {
    busy.value = false
  }
}

async function destroyAll() {
  try {
    await ElMessageBox.confirm('销毁该赛事下全部靶机？', '提示', { type: 'warning' })
  } catch { return }
  busy.value = true
  try {
    await request({ url: '/awd/targets/destroy-all', method: 'post', params: { contestId: contestId.value } })
    ElMessage.success('已销毁')
    await loadTargets()
  } catch (e) {
    ElMessage.error(e.message || '销毁失败')
  } finally {
    busy.value = false
  }
}

async function resetTarget(row) {
  try {
    await request({ url: '/awd/targets/reset', method: 'post', params: {
      contestId: contestId.value, teamId: row.teamId, questionId: row.questionId
    } })
    ElMessage.success('已重置')
    await loadTargets()
  } catch (e) {
    ElMessage.error(e.message || '重置失败')
  }
}

async function destroyTarget(row) {
  try {
    await ElMessageBox.confirm(`销毁「${row.teamName || row.teamId}」的靶机？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await request({ url: '/awd/targets/destroy', method: 'post', params: {
      contestId: contestId.value, teamId: row.teamId, questionId: row.questionId
    } })
    ElMessage.success('已销毁')
    await loadTargets()
  } catch (e) {
    ElMessage.error(e.message || '销毁失败')
  }
}

function addChecker() {
  Object.assign(checkerForm, {
    id: null, contestId: contestId.value, questionId: awdQuestions.value[0]?.id || null,
    checkType: 'HTTP', targetPath: '/', expectStatus: 200, timeoutMs: 3000, enabled: 1
  })
  checkerVisible.value = true
}

function editChecker(row) {
  Object.assign(checkerForm, { ...row, contestId: contestId.value })
  checkerVisible.value = true
}

async function saveChecker() {
  if (!checkerForm.questionId) {
    ElMessage.warning('请选择题目')
    return
  }
  saving.value = true
  try {
    await request({ url: '/awd/checkers', method: 'post', data: checkerForm })
    ElMessage.success('已保存')
    checkerVisible.value = false
    await loadCheckers()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function removeChecker(row) {
  try {
    await ElMessageBox.confirm('删除该 checker 配置？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await request({ url: `/awd/checkers/${row.id}`, method: 'delete' })
    ElMessage.success('已删除')
    await loadCheckers()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(loadContests)
</script>

<style scoped>
.head { display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 16px; font-weight: 600; }
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.stat { margin-left: auto; font-size: 12px; color: var(--el-text-color-secondary); }
.mono { font-family: Consolas, Monaco, monospace; font-size: 12px; }
.muted { color: var(--el-text-color-placeholder); }
.msg { margin-left: 6px; font-size: 11px; color: var(--el-text-color-secondary); }
</style>
