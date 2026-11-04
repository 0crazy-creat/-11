<template>
  <div class="awd-arena">
    <div class="csp-page-head">
      <div>
        <h2 class="csp-h2">{{ contest.name || 'AWD 攻防赛' }}</h2>
        <p class="csp-text-secondary">
          <span class="csp-tag">{{ contest.type }}</span>
          <span class="csp-status" :class="{'is-running': contest.status==='RUNNING'}">{{ statusLabel(contest.status) }}</span>
          <span v-if="contest.code"> · {{ contest.code }}</span>
        </p>
      </div>
      <div class="head-actions">
        <el-button v-if="canEnroll && !myTeam" type="primary" :loading="enrolling" @click="enroll">报名参赛</el-button>
        <el-tag v-if="myTeam" type="success" effect="light">已报名：{{ myTeam.name }}</el-tag>
        <el-button @click="$router.push(`/rank/${contestId}`)">排行榜</el-button>
        <el-button type="primary" plain @click="$router.push(`/awd/screen/${contestId}`)">
          <el-icon><Monitor /></el-icon>
          攻防大屏
        </el-button>
      </div>
    </div>

    <el-alert v-if="errMsg" :title="errMsg" type="warning" show-icon :closable="true" class="mb16" />

    <el-card v-if="currentRound" shadow="never" class="panel mt16 round-banner" :class="'round-' + currentRound.status?.toLowerCase()">
      <div class="round-banner-inner">
        <div class="round-info">
          <el-tag :type="roundTagType(currentRound.status)" effect="dark" size="large">第 {{ currentRound.roundNo }} 轮</el-tag>
          <span class="round-status-text">{{ roundStatusLabel(currentRound.status) }}</span>
          <span v-if="currentRound.startAt" class="round-time">
            {{ formatTime(currentRound.startAt) }} ~ {{ formatTime(currentRound.endAt) }}
          </span>
        </div>
        <div v-if="currentRound.status === 'RUNNING' && remainingSeconds > 0" class="countdown">
          <span class="cd-label">剩余时间</span>
          <span class="cd-value">{{ formatCountdown(remainingSeconds) }}</span>
        </div>
        <div v-if="currentRound.status === 'RUNNING'" class="round-actions">
          <el-button type="success" :loading="actionBusy" @click="openAttackDialog">提交 Flag</el-button>
          <el-button v-if="isAwdp" type="warning" plain :loading="actionBusy" @click="showPatchDialog = true">提交修补</el-button>
        </div>
      </div>
    </el-card>

    <el-card v-if="isAdmin" shadow="never" class="panel mt16">
      <template #header>
        <div class="panel-header-row">
          <span class="panel-title">裁判控制台</span>
          <div>
            <el-button size="small" type="primary" :loading="actionBusy" @click="provisionAll">一键部署全部靶机</el-button>
            <el-button size="small" :loading="actionBusy" @click="loadRounds">刷新轮次</el-button>
          </div>
        </div>
      </template>

      <div class="create-round-row">
        <el-input-number v-model="newRoundNo" :min="1" :max="999" style="width: 120px" />
        <el-input-number v-model="newRoundDuration" :min="30" :max="86400" :step="30" style="width: 140px" />
        <span class="duration-hint">秒（{{ Math.floor(newRoundDuration / 60) }}分{{ newRoundDuration % 60 }}秒）</span>
        <el-select v-model="newRoundMode" style="width: 100px">
          <el-option label="AWD" value="AWD" />
          <el-option label="AWDP" value="AWDP" />
        </el-select>
        <el-button type="primary" :loading="actionBusy" @click="createRound">创建轮次</el-button>
      </div>

      <el-table :data="rounds" size="small" class="mt12" v-loading="roundLoading">
        <el-table-column prop="roundNo" label="轮次" width="70" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="roundTagType(row.status)" size="small">{{ roundStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startAt" label="开始" width="170">
          <template #default="{ row }">{{ formatTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column prop="endAt" label="结束" width="170">
          <template #default="{ row }">{{ formatTime(row.endAt) }}</template>
        </el-table-column>
        <el-table-column prop="durationS" label="时长" width="80">
          <template #default="{ row }">{{ row.durationS }}s</template>
        </el-table-column>
        <el-table-column label="操作" min-width="280">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" :loading="actionBusy" @click="beginRound(row)">开始</el-button>
            <el-button v-if="row.status === 'RUNNING'" size="small" type="warning" :loading="actionBusy" @click="checkRound(row)">检查</el-button>
            <el-button v-if="row.status === 'CHECKING'" size="small" type="primary" :loading="actionBusy" @click="finishRound(row)">结算</el-button>
            <el-button v-if="['PENDING','RUNNING','CHECKING'].includes(row.status)" size="small" type="danger" plain :loading="actionBusy" @click="abortRound(row)">中止</el-button>
            <el-button v-if="row.status === 'RUNNING'" size="small" :loading="actionBusy" @click="settleRound(row)">手动结算</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="16" class="mt16">
      <el-col :span="12">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">赛事信息</span></template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="赛事名称">{{ contest.name }}</el-descriptions-item>
            <el-descriptions-item label="赛事类型">{{ contest.type }}</el-descriptions-item>
            <el-descriptions-item label="比赛时间">{{ contest.startAt }} ~ {{ contest.endAt }}</el-descriptions-item>
            <el-descriptions-item label="赛事描述">{{ contest.description || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">实时排名 (Top 10)</span></template>
          <el-table :data="leaderboard" size="small" max-height="280" v-loading="scoreLoading">
            <el-table-column prop="rank" label="#" width="50" />
            <el-table-column prop="teamName" label="战队" min-width="100" />
            <el-table-column prop="score" label="总分" width="70" />
            <el-table-column prop="attackScore" label="攻击" width="65" />
            <el-table-column prop="defenseScore" label="防御" width="65" />
            <el-table-column prop="slaPenalty" label="SLA" width="65">
              <template #default="{ row }">
                <span :class="{'neg': row.slaPenalty < 0}">{{ row.slaPenalty }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel mt16" v-if="myTeam || isAdmin">
      <template #header>
        <div class="panel-header-row">
          <div class="target-header">
            <el-icon><Monitor /></el-icon>
            <span class="panel-title">{{ isAdmin ? '全部靶机' : '我的靶机' }}</span>
            <el-tag v-if="targets.length" type="info" size="small">{{ targets.length }} 台</el-tag>
          </div>
          <div v-if="isAdmin">
            <el-button size="small" :loading="targetBusy" @click="healthCheck">健康检查</el-button>
            <el-button size="small" :loading="targetBusy" @click="loadTargets">刷新</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!targets.length && !targetLoading" description="暂无靶机，管理员请先部署靶机" :image-size="60" />

      <div v-loading="targetLoading" class="target-grid">
        <div v-for="tg in targets" :key="tg.id" class="target-card" :class="(tg.status || 'pending').toLowerCase()">
          <div class="tg-top">
            <span class="tg-name">{{ tg.name || tg.questionTitle || ('靶机#' + tg.questionId) }}</span>
            <el-tag size="small" :type="targetStatusType(tg.status)">{{ targetStatusLabel(tg.status) }}</el-tag>
          </div>
          <div class="tg-info" v-if="tg.accessUrl">
            <span class="tg-label">地址：</span>
            <a :href="tg.accessUrl" target="_blank" class="tg-link">{{ tg.accessUrl }}</a>
          </div>
          <div class="tg-info" v-if="tg.ip">
            <span class="tg-label">IP：</span>
            <span class="tg-value">{{ tg.ip }}:{{ tg.port }}</span>
          </div>
          <div class="tg-meta" v-if="tg.health != null">
            <el-tag size="small" :type="tg.health === 1 ? 'success' : 'danger'">{{ tg.health === 1 ? '健康' : '异常' }}</el-tag>
            <span v-if="tg.cpuPercent != null" class="tg-cpu">CPU {{ tg.cpuPercent.toFixed(1) }}%</span>
            <span v-if="tg.memUsedMb != null" class="tg-mem">MEM {{ tg.memUsedMb.toFixed(0) }}MB</span>
          </div>
          <div class="tg-actions" v-if="isAdmin">
            <el-button size="small" :loading="targetBusy" @click="resetTarget(tg)">重置</el-button>
            <el-button size="small" type="danger" plain :loading="targetBusy" @click="destroyTarget(tg)">销毁</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="!myTeam && !isAdmin" shadow="never" class="panel mt16">
      <el-empty description="请先报名参赛，报名后可查看靶机与提交 Flag" :image-size="80">
        <el-button v-if="canEnroll" type="primary" :loading="enrolling" @click="enroll">立即报名</el-button>
      </el-empty>
    </el-card>

    <el-card v-if="myTeam && currentRound && currentRound.status === 'RUNNING'" shadow="never" class="panel mt16">
      <template #header><span class="panel-title">攻击提交记录</span></template>
      <el-table :data="attackLog" size="small" max-height="200">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="questionId" label="题目" width="80" />
        <el-table-column prop="victimTeamId" label="受害队" width="80" />
        <el-table-column prop="success" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gainedScore" label="得分" width="70" />
        <el-table-column prop="message" label="信息" />
      </el-table>
    </el-card>

    <el-card v-if="isAwdp && myTeam" shadow="never" class="panel mt16">
      <template #header><span class="panel-title">防御修补记录</span></template>
      <el-table :data="defenseLog" size="small" max-height="200">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="roundNo" label="轮次" width="70" />
        <el-table-column prop="questionId" label="题目" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="defenseStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defenseScore" label="防御分" width="80" />
        <el-table-column prop="fileName" label="文件" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="panel mt16">
      <template #header><span class="panel-title">赛事公告</span></template>
      <el-empty v-if="!bulletins.length" description="暂无公告" :image-size="50" />
      <div v-else class="bulletin-list">
        <div v-for="b in bulletins" :key="b.id" class="bulletin-item">
          <div class="b-title">{{ b.title }}</div>
          <div class="b-time">{{ b.createdAt }}</div>
          <div class="b-content">{{ b.content }}</div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="showAttackDialog" title="提交攻击 Flag" width="480px">
      <el-form :model="attackForm" label-width="90px">
        <el-form-item label="受害队伍">
          <el-input-number v-model="attackForm.victimTeamId" :min="1" placeholder="对方队伍ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="题目ID">
          <el-input-number v-model="attackForm.questionId" :min="1" placeholder="攻击的题目ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Flag">
          <el-input v-model="attackForm.flag" placeholder="输入获取到的 flag" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAttackDialog = false">取消</el-button>
        <el-button type="primary" :loading="actionBusy" @click="doSubmitAttack">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPatchDialog" title="提交防御修补" width="520px">
      <el-form :model="patchForm" label-width="90px">
        <el-form-item label="题目ID">
          <el-input-number v-model="patchForm.questionId" :min="1" placeholder="修补的题目ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="轮次号">
          <el-input-number v-model="patchForm.roundNo" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="修补说明">
          <el-input v-model="patchForm.description" type="textarea" :rows="2" placeholder="简述修复内容" />
        </el-form-item>
        <el-form-item label="修补包">
          <input ref="patchFileRef" type="file" accept=".zip,.patch,.diff,.tar.gz,.tgz" @change="onPatchFileChange" />
          <span v-if="patchForm.file" class="file-hint">{{ patchForm.file.name }}（{{ (patchForm.file.size / 1024).toFixed(1) }} KB）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPatchDialog = false">取消</el-button>
        <el-button type="primary" :loading="actionBusy" :disabled="!patchForm.file" @click="doSubmitPatch">上传并校验</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const user = useUserStore()
const contestId = computed(() => Number(route.params.contestId))
const isAwdp = computed(() => String(contest.value.type || '').toUpperCase() === 'AWDP')

const contest = ref({})
const myTeam = ref(null)
const rounds = ref([])
const currentRound = ref(null)
const targets = ref([])
const leaderboard = ref([])
const bulletins = ref([])
const attackLog = ref([])
const defenseLog = ref([])
const errMsg = ref('')

const enrolling = ref(false)
const targetLoading = ref(false)
const targetBusy = ref(false)
const roundLoading = ref(false)
const scoreLoading = ref(false)
const actionBusy = ref(false)

const remainingSeconds = ref(0)
let countdownTimer = null

const isAdmin = computed(() => user.isAdmin)
const canEnroll = computed(() => ['SIGNUP', 'RUNNING', 'PENDING'].includes(contest.value.status))

const showAttackDialog = ref(false)
const showPatchDialog = ref(false)
const attackForm = ref({ victimTeamId: null, questionId: null, flag: '' })
const patchForm = ref({ questionId: null, roundNo: null, description: '', file: null })
const patchFileRef = ref(null)

const newRoundNo = ref(1)
const newRoundDuration = ref(600)
const newRoundMode = ref('AWD')

function statusLabel(s) {
  return { DRAFT: '筹备中', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档' }[s] || s || '—'
}
function roundStatusLabel(s) {
  return { PENDING: '待开始', RUNNING: '攻击中', CHECKING: '检查中', SETTLING: '结算中', FINISHED: '已结束', ABORTED: '已中止' }[s] || s
}
function roundTagType(s) {
  return { PENDING: 'info', RUNNING: 'success', CHECKING: 'warning', SETTLING: '', FINISHED: 'info', ABORTED: 'danger' }[s] || 'info'
}
function targetStatusLabel(s) {
  return { RUNNING: '运行中', UP: '运行中', DOWN: '已宕机', PENDING: '待启动', STOPPED: '已停止', ERROR: '异常' }[s] || s || '待启动'
}
function targetStatusType(s) {
  return { RUNNING: 'success', UP: 'success', DOWN: 'danger', STOPPED: 'info', ERROR: 'danger' }[s] || 'warning'
}
function defenseStatusType(s) {
  return { VERIFIED: 'success', CHECKING: 'warning', APPLYING: 'warning', PENDING: 'info', REJECTED: 'danger', FAILED: 'danger', COMPROMISED: 'danger' }[s] || 'info'
}
function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}
function formatCountdown(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

async function loadContest() {
  try {
    contest.value = await request.get(`/contests/${contestId.value}`) || {}
  } catch (e) {
    errMsg.value = '赛事信息加载失败：' + (e?.message || '')
  }
}

async function loadMyTeam() {
  try {
    myTeam.value = await request.get(`/ctf/contests/${contestId.value}/teams/mine`) || null
  } catch { myTeam.value = null }
}

async function loadRounds() {
  roundLoading.value = true
  try {
    rounds.value = await request.get(`/awd/rounds`, { params: { contestId: contestId.value } }) || []
    currentRound.value = await request.get(`/awd/rounds/${contestId.value}/current`) || null
  } catch {
    rounds.value = []
    currentRound.value = null
  } finally { roundLoading.value = false }
}

async function loadTargets() {
  if (!myTeam.value && !isAdmin.value) return
  targetLoading.value = true
  try {
    targets.value = await request.get(`/awd/targets/mine`, { params: { contestId: contestId.value } }) || []
  } catch { targets.value = [] } finally { targetLoading.value = false }
}

async function loadLeaderboard() {
  scoreLoading.value = true
  try {
    const data = await request.get(`/awd/score/leaderboard`, { params: { contestId: contestId.value, topN: 10 } }) || []
    leaderboard.value = data.map((t, i) => ({ ...t, rank: i + 1 }))
  } catch { leaderboard.value = [] } finally { scoreLoading.value = false }
}

async function loadBulletins() {
  try {
    const data = await request.get(`/announcements`, { params: { contestId: contestId.value, size: 5 } })
    bulletins.value = data?.records || data || []
  } catch { bulletins.value = [] }
}

async function loadAttackLog() {
  try {
    attackLog.value = await request.get(`/awd/contests/${contestId.value}/attacks`) || []
  } catch { attackLog.value = [] }
}

async function loadDefenseLog() {
  if (!isAwdp.value || !myTeam.value) return
  try {
    defenseLog.value = await request.get(`/awd/defense/list`, { params: { contestId: contestId.value, teamId: myTeam.value.id } }) || []
  } catch { defenseLog.value = [] }
}

async function createRound() {
  actionBusy.value = true
  try {
    await request.post(`/awd/rounds`, {
      contestId: contestId.value,
      roundNo: newRoundNo.value,
      durationS: newRoundDuration.value,
      mode: newRoundMode.value,
      provisionTargets: true
    })
    ElMessage.success(`第 ${newRoundNo.value} 轮已创建`)
    newRoundNo.value++
    await loadRounds()
  } catch (e) {
    ElMessage.error(e?.message || '创建轮次失败')
  } finally { actionBusy.value = false }
}

async function beginRound(row) {
  actionBusy.value = true
  try {
    await request.post(`/awd/rounds/${contestId.value}/${row.roundNo}/start`)
    ElMessage.success(`第 ${row.roundNo} 轮已开始`)
    await loadRounds()
    await loadTargets()
  } catch (e) { ElMessage.error(e?.message || '操作失败') } finally { actionBusy.value = false }
}

async function checkRound(row) {
  actionBusy.value = true
  try {
    await request.post(`/awd/rounds/${contestId.value}/${row.roundNo}/check`)
    ElMessage.success(`第 ${row.roundNo} 轮进入检查阶段`)
    await loadRounds()
  } catch (e) { ElMessage.error(e?.message || '操作失败') } finally { actionBusy.value = false }
}

async function finishRound(row) {
  actionBusy.value = true
  try {
    await request.post(`/awd/rounds/${contestId.value}/${row.roundNo}/finish`)
    ElMessage.success(`第 ${row.roundNo} 轮已结算`)
    await loadRounds()
    await loadLeaderboard()
  } catch (e) { ElMessage.error(e?.message || '操作失败') } finally { actionBusy.value = false }
}

async function abortRound(row) {
  try { await ElMessageBox.confirm(`确定中止第 ${row.roundNo} 轮？`, '提示', { type: 'warning' }) } catch { return }
  actionBusy.value = true
  try {
    await request.post(`/awd/rounds/${contestId.value}/${row.roundNo}/abort`)
    ElMessage.success(`第 ${row.roundNo} 轮已中止`)
    await loadRounds()
  } catch (e) { ElMessage.error(e?.message || '操作失败') } finally { actionBusy.value = false }
}

async function settleRound(row) {
  actionBusy.value = true
  try {
    await request.post(`/awd/score/settle`, null, { params: { contestId: contestId.value, roundNo: row.roundNo } })
    ElMessage.success('手动结算完成')
    await loadLeaderboard()
  } catch (e) { ElMessage.error(e?.message || '结算失败') } finally { actionBusy.value = false }
}

async function provisionAll() {
  actionBusy.value = true
  try {
    const data = await request.post(`/awd/targets/provision-all`, null, { params: { contestId: contestId.value } })
    ElMessage.success(`靶机部署完成：${typeof data === 'number' ? data + ' 台' : '成功'}`)
    await loadTargets()
  } catch (e) { ElMessage.error(e?.message || '部署失败') } finally { actionBusy.value = false }
}

async function healthCheck() {
  targetBusy.value = true
  try {
    await request.post(`/awd/targets/health-check`, null, { params: { contestId: contestId.value } })
    ElMessage.success('健康检查完成')
    await loadTargets()
  } catch (e) { ElMessage.error(e?.message || '健康检查失败') } finally { targetBusy.value = false }
}

async function resetTarget(tg) {
  targetBusy.value = true
  try {
    await request.post(`/awd/targets/reset`, null, {
      params: { contestId: contestId.value, teamId: tg.teamId, questionId: tg.questionId }
    })
    ElMessage.success('靶机已重置')
    await loadTargets()
  } catch (e) { ElMessage.error(e?.message || '重置失败') } finally { targetBusy.value = false }
}

async function destroyTarget(tg) {
  try { await ElMessageBox.confirm('确定要销毁该靶机吗？', '提示', { type: 'warning' }) } catch { return }
  targetBusy.value = true
  try {
    await request.post(`/awd/targets/destroy`, null, {
      params: { contestId: contestId.value, teamId: tg.teamId, questionId: tg.questionId }
    })
    ElMessage.success('靶机已销毁')
    await loadTargets()
  } catch (e) { ElMessage.error(e?.message || '销毁失败') } finally { targetBusy.value = false }
}

function openAttackDialog() {
  attackForm.value = { victimTeamId: null, questionId: null, flag: '' }
  showAttackDialog.value = true
}

async function doSubmitAttack() {
  if (!attackForm.value.victimTeamId || !attackForm.value.questionId || !attackForm.value.flag) {
    ElMessage.warning('请填写完整信息')
    return
  }
  actionBusy.value = true
  try {
    const res = await request.post(`/awd/score/attack`, {
      contestId: contestId.value,
      victimTeamId: attackForm.value.victimTeamId,
      questionId: attackForm.value.questionId,
      flag: attackForm.value.flag
    })
    if (res?.success) {
      ElMessage.success(`攻击成功！得分 +${res.gainedScore}${res.firstBlood ? ' (First Blood!)' : ''}`)
    } else {
      ElMessage.error(res?.message || 'Flag 不正确')
    }
    showAttackDialog.value = false
    await loadAttackLog()
    await loadLeaderboard()
  } catch (e) { ElMessage.error(e?.message || '提交失败') } finally { actionBusy.value = false }
}

function onPatchFileChange(e) {
  patchForm.value.file = e.target.files?.[0] || null
}

async function doSubmitPatch() {
  if (!patchForm.value.file) { ElMessage.warning('请选择修补包'); return }
  actionBusy.value = true
  try {
    const fd = new FormData()
    fd.append('contestId', String(contestId.value))
    if (myTeam.value?.id) fd.append('teamId', String(myTeam.value.id))
    if (patchForm.value.questionId) fd.append('questionId', String(patchForm.value.questionId))
    if (patchForm.value.roundNo) fd.append('roundNo', String(patchForm.value.roundNo))
    if (patchForm.value.description) fd.append('description', patchForm.value.description)
    fd.append('file', patchForm.value.file)
    const res = await request.post(`/awd/defense/submit`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res?.verified || res?.status === 'VERIFIED') {
      ElMessage.success(`修补验证通过！防御分 +${res.gainedScore || 0}`)
    } else if (res?.status === 'FAILED') {
      ElMessage.error(`修补失败：${res.failReason || res.message || '未知原因'}`)
    } else {
      ElMessage.info(`修补状态：${res?.status || '已提交'}`)
    }
    showPatchDialog.value = false
    await loadDefenseLog()
    await loadLeaderboard()
  } catch (e) { ElMessage.error(e?.message || '提交失败') } finally { actionBusy.value = false }
}

async function enroll() {
  enrolling.value = true
  try {
    await request.post(`/ctf/contests/${contestId.value}/teams`, {
      contestId: contestId.value,
      name: '战队' + Date.now().toString().slice(-4)
    })
    ElMessage.success('报名成功')
    await loadAll()
  } catch (e) { ElMessage.error(e?.message || '报名失败') } finally { enrolling.value = false }
}

function updateCountdown() {
  if (!currentRound.value?.endAt) { remainingSeconds.value = 0; return }
  const end = new Date(currentRound.value.endAt).getTime()
  const now = Date.now()
  remainingSeconds.value = Math.max(0, Math.floor((end - now) / 1000))
}

async function loadAll() {
  await Promise.allSettled([loadContest(), loadMyTeam(), loadRounds(), loadLeaderboard(), loadBulletins()])
  await Promise.allSettled([loadTargets(), loadAttackLog(), loadDefenseLog()])
}

let pollTimer = null
onMounted(() => {
  loadAll()
  pollTimer = setInterval(loadAll, 10000)
  countdownTimer = setInterval(updateCountdown, 1000)
})
onUnmounted(() => {
  clearInterval(pollTimer)
  clearInterval(countdownTimer)
})

watch(currentRound, (r) => {
  if (r?.status === 'RUNNING') {
    newRoundNo.value = (r.roundNo || 0) + 1
  }
})
</script>

<style scoped>
.awd-arena { padding: 0; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.mt12 { margin-top: 12px; }
.csp-page-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
.head-actions { display: flex; gap: 8px; align-items: center; }
.panel { border-radius: 12px; }
.panel-title { font-weight: 600; color: var(--csp-title); }
.panel-header-row { display: flex; justify-content: space-between; align-items: center; width: 100%; }

.round-banner { border-left: 4px solid var(--csp-primary); }
.round-banner.round-running { border-left-color: #00b42a; background: rgba(0,180,42,0.03); }
.round-banner.round-checking { border-left-color: #ff7d00; background: rgba(255,125,0,0.03); }
.round-banner.round-settling { border-left-color: #7b61ff; background: rgba(123,97,255,0.03); }
.round-banner.round-finished { border-left-color: #c9cdd4; }
.round-banner-inner { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.round-info { display: flex; align-items: center; gap: 12px; flex: 1; }
.round-status-text { font-size: 15px; font-weight: 600; }
.round-time { font-size: 12px; color: #86909c; }
.countdown { text-align: center; }
.cd-label { display: block; font-size: 12px; color: #86909c; }
.cd-value { font-size: 24px; font-weight: 700; font-family: monospace; color: #f53f3f; }
.round-actions { display: flex; gap: 8px; }

.create-round-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 12px; }
.duration-hint { font-size: 12px; color: #86909c; }

.target-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px; }
.target-card {
  padding: 14px; border-radius: 10px; border: 1px solid #e5e6eb;
  background: #fafbfc; display: flex; flex-direction: column; gap: 8px; transition: all 0.2s;
}
.target-card:hover { border-color: var(--csp-primary); box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.target-card.running { border-color: rgba(0,180,42,0.4); background: rgba(0,180,42,0.03); }
.target-card.up { border-color: rgba(0,180,42,0.4); background: rgba(0,180,42,0.03); }
.target-card.down { border-color: rgba(245,63,63,0.4); background: rgba(245,63,63,0.03); }
.target-card.error { border-color: rgba(245,63,63,0.4); background: rgba(245,63,63,0.03); }
.target-card.stopped { opacity: 0.6; }
.tg-top { display: flex; justify-content: space-between; align-items: center; }
.tg-name { font-size: 14px; font-weight: 600; color: #1d2129; }
.tg-info { font-size: 12px; color: #4e5969; display: flex; align-items: center; gap: 4px; }
.tg-label { color: #86909c; flex-shrink: 0; }
.tg-link { color: var(--csp-primary); text-decoration: none; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tg-link:hover { text-decoration: underline; }
.tg-value { font-family: monospace; }
.tg-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #86909c; }
.tg-cpu, .tg-mem { font-family: monospace; }
.tg-actions { display: flex; gap: 6px; margin-top: 4px; }
.target-header { display: flex; align-items: center; gap: 8px; }

.neg { color: #f53f3f; }
.file-hint { margin-left: 8px; font-size: 12px; color: #86909c; }

.bulletin-list { display: flex; flex-direction: column; gap: 12px; }
.bulletin-item { padding: 10px 12px; border-radius: 8px; background: #f7f8fa; border-left: 3px solid var(--csp-primary); }
.b-title { font-size: 14px; font-weight: 600; color: #1d2129; }
.b-time { font-size: 12px; color: #86909c; margin-top: 2px; }
.b-content { font-size: 13px; color: #4e5969; margin-top: 6px; line-height: 1.6; }

.csp-tag { font-size: 12px; padding: 1px 6px; border-radius: 4px; background: var(--csp-primary-light); color: var(--csp-primary); }
.csp-status.is-running { color: #00b42a; font-weight: 600; }
</style>
