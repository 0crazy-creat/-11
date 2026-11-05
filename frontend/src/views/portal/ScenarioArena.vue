<template>
  <div class="scenario-arena">
    <el-card shadow="never" class="head">
      <div class="head-row">
        <div>
          <h3 class="title">{{ arena?.contestName || '场景实操赛' }}</h3>
          <div class="sub">
            已完成 <b>{{ arena?.solvedCount ?? 0 }}</b> / {{ arena?.totalCount ?? 0 }} 个任务
            · 我的得分 <b class="score">{{ arena?.myScore ?? 0 }}</b> / {{ arena?.totalScore ?? 0 }}
          </div>
        </div>
        <div class="ops">
          <el-button :loading="loading" @click="load">刷新</el-button>
          <el-button v-if="privileged" :loading="prechecking" type="warning" plain @click="runPrecheck">
            一键预检
          </el-button>
        </div>
      </div>
    </el-card>

    <el-alert
      v-if="precheckReport"
      :type="precheckReport.passed ? 'success' : 'error'"
      :closable="true"
      show-icon
      class="precheck"
      @close="precheckReport = null"
    >
      <template #title>
        预检{{ precheckReport.passed ? '通过' : '未通过' }}
        （Docker {{ precheckReport.dockerAvailable ? '可用' : '不可用' }}）
      </template>
      <div v-for="(it, i) in precheckReport.items || []" :key="i" class="check-item">
        <el-tag :type="levelType(it.level)" size="small">{{ it.level }}</el-tag>
        <span class="code">[{{ it.code }}]</span>
        <span class="msg">{{ it.scene }}：{{ it.message }}</span>
      </div>
    </el-alert>

    <el-alert
      v-if="arena && !arena.answering"
      type="warning"
      show-icon
      :closable="false"
      class="closed-tip"
      title="当前不可提交"
      :description="arena.closedReason || '赛事未在进行中'"
    />

    <el-empty v-if="!loading && !(arena?.scenarios || []).length" description="尚未报名该赛事或赛事未配置场景" />

    <div v-loading="loading" class="scenes">
      <el-card v-for="scene in arena?.scenarios || []" :key="scene.title" shadow="never" class="scene">
        <div class="scene-head">
          <div class="scene-title">
            <b>{{ scene.title }}</b>
            <el-tag v-if="scene.solved" type="success" size="small" effect="dark">全部完成</el-tag>
            <span class="progress">{{ scene.solvedTasks }} / {{ scene.totalTasks }}</span>
          </div>
          <div class="scene-score">场景总分 {{ scene.score }}</div>
        </div>

        <el-tree
          :data="scene.children || []"
          node-key="questionId"
          :props="{ label: 'title', children: 'children' }"
          default-expand-all
          class="task-tree"
        >
          <template #default="{ data }">
            <div class="task-node" :class="{ solved: data.solved }">
              <div class="task-main">
                <el-tag :type="data.solved ? 'success' : 'info'" size="small" effect="plain">
                  {{ data.solved ? '已完成' : '待完成' }}
                </el-tag>
                <span class="task-title">{{ data.title }}</span>
                <el-tag v-if="data.category" size="small" type="info">{{ data.category }}</el-tag>
                <el-tag v-if="data.preQuestionId" size="small" type="warning">依赖前序任务</el-tag>
                <span class="task-score">+{{ data.score }}</span>
              </div>
              <div class="task-ops">
                <el-input
                  v-model="flags[data.questionId]"
                  placeholder="提交 Evidence Flag"
                  size="small"
                  class="flag-input"
                  :disabled="data.solved"
                  @keyup.enter="submit(data)"
                />
                <el-button
                  size="small"
                  type="primary"
                  :disabled="data.solved"
                  :loading="submitting === data.questionId"
                  @click="submit(data)"
                >提交</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getScenarioArena,
  submitScenarioFlag,
  precheckScenarioContest
} from '@/api'

const route = useRoute()
// 雪花 ID 为 19 位，超出 Number.MAX_SAFE_INTEGER，必须保持字符串避免精度丢失
const contestId = String(route.params.contestId)

const loading = ref(false)
const prechecking = ref(false)
const submitting = ref(null)
const arena = ref(null)
const precheckReport = ref(null)
const flags = reactive({})
const privileged = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getScenarioArena(contestId)
    arena.value = res
  } catch (e) {
    ElMessage.error(e?.message || '加载场景树失败')
  } finally {
    loading.value = false
  }
}

async function submit(node) {
  const flag = (flags[node.questionId] || '').trim()
  if (!flag) {
    ElMessage.warning('请先填写 Evidence Flag')
    return
  }
  submitting.value = node.questionId
  try {
    const r = await submitScenarioFlag({
      contestId,
      questionId: node.questionId,
      flag
    })
    if (r?.correct) {
      ElMessage.success(`提交正确，+${r.score ?? 0} 分${r.firstBlood ? '（一血）' : ''}`)
      flags[node.questionId] = ''
      await load()
    } else {
      ElMessage.error(r?.message || 'Flag 错误')
    }
  } catch (e) {
    ElMessage.error(e?.message || '提交失败（可能前置任务未完成）')
  } finally {
    submitting.value = null
  }
}

async function runPrecheck() {
  prechecking.value = true
  try {
    precheckReport.value = await precheckScenarioContest(contestId)
    if (precheckReport.value?.passed) {
      ElMessage.success('环境预检通过')
    } else {
      ElMessage.warning('环境预检未通过，请查看详情')
    }
  } catch (e) {
    ElMessage.error(e?.message || '预检失败')
  } finally {
    prechecking.value = false
  }
}

function levelType(level) {
  if (level === 'ERROR') return 'danger'
  if (level === 'WARN') return 'warning'
  return 'success'
}

onMounted(load)
</script>

<style scoped>
.scenario-arena { padding: 16px; }
.head { margin-bottom: 12px; }
.head-row { display: flex; align-items: center; justify-content: space-between; }
.title { margin: 0 0 6px; }
.sub { color: #606266; font-size: 13px; }
.score { color: #e6a23c; font-size: 16px; }
.precheck { margin-bottom: 12px; }
.check-item { margin: 4px 0; font-size: 12px; }
.code { margin: 0 6px; color: #909399; }
.scenes { display: flex; flex-direction: column; gap: 12px; }
.scene-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.scene-title { display: flex; align-items: center; gap: 8px; }
.progress { color: #909399; font-size: 12px; }
.scene-score { color: #409eff; font-size: 13px; }
.task-tree { background: transparent; }
.task-node { display: flex; align-items: center; justify-content: space-between; gap: 12px; width: 100%; padding: 4px 0; }
.task-node.solved .task-title { color: #67c23a; }
.task-main { display: flex; align-items: center; gap: 8px; }
.task-title { font-weight: 500; }
.task-score { color: #e6a23c; font-size: 13px; }
.task-ops { display: flex; align-items: center; gap: 8px; }
.flag-input { width: 260px; }
</style>