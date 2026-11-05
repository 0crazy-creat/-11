<template>
  <div class="statistics-report">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>统计分析报表</span>
          <div class="actions">
            <el-select
              v-model="contestId"
              placeholder="选择赛事"
              clearable
              filterable
              style="width: 260px"
              @change="onContestChange"
            >
              <el-option
                v-for="c in contests"
                :key="c.id"
                :label="`${c.name}（${c.code}）`"
                :value="String(c.id)"
              />
            </el-select>
            <el-button :loading="loading" @click="reload">刷新</el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="!contestId"
        type="info"
        :closable="false"
        show-icon
        title="请先选择赛事，理论赛报告需按赛事维度统计"
        class="mb12"
      />

      <el-tabs v-model="tab">
        <!-- 理论赛分析 -->
        <el-tab-pane label="理论赛成绩分析" name="theory">
          <el-empty v-if="!contestId" description="请选择赛事" :image-size="70" />
          <el-empty
            v-else-if="!theoryData"
            description="该赛事暂无提交数据"
            :image-size="70"
          />
          <template v-else>
            <!-- 概览 -->
            <div class="stat-row">
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">参与战队</div>
                <div class="stat-value">{{ theoryData.teamAnalysis?.length || 0 }}</div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">涉及题目</div>
                <div class="stat-value">{{ theoryData.questionAnalysis?.length || 0 }}</div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">总提交次数</div>
                <div class="stat-value">{{ totalSubmits }}</div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">整体正确率</div>
                <div class="stat-value">{{ overallAcc }}%</div>
              </el-card>
            </div>

            <el-divider content-position="left">题目分析（正确率升序，暴露薄弱题）</el-divider>
            <el-table :data="theoryData.questionAnalysis" size="small" border>
              <el-table-column prop="question_id" label="题目 ID" min-width="200" />
              <el-table-column prop="submits" label="提交数" width="100" align="center" />
              <el-table-column prop="corrects" label="正确数" width="100" align="center" />
              <el-table-column label="正确率" min-width="180">
                <template #default="{ row }">
                  <el-progress
                    :percentage="Number(row.acc) || 0"
                    :color="accColor(row.acc)"
                    :stroke-width="12"
                  />
                </template>
              </el-table-column>
            </el-table>

            <el-divider content-position="left">战队得分（降序）</el-divider>
            <el-table :data="theoryData.teamAnalysis" size="small" border>
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="team_id" label="战队 ID" min-width="200" />
              <el-table-column prop="score" label="得分" width="110" align="center" />
              <el-table-column prop="solved" label="解出题数" width="110" align="center" />
              <el-table-column prop="attempts" label="提交次数" width="110" align="center" />
            </el-table>

            <el-divider content-position="left">分类分析</el-divider>
            <el-table :data="theoryData.categoryAnalysis" size="small" border>
              <el-table-column prop="category" label="题目分类" width="160" />
              <el-table-column prop="submits" label="提交数" width="100" align="center" />
              <el-table-column prop="corrects" label="正确数" width="100" align="center" />
              <el-table-column label="正确率" min-width="180">
                <template #default="{ row }">
                  <el-progress
                    :percentage="Number(row.acc) || 0"
                    :color="accColor(row.acc)"
                    :stroke-width="12"
                  />
                </template>
              </el-table-column>
            </el-table>

            <el-alert
              v-if="theoryData.partialScoring"
              type="success"
              :closable="false"
              show-icon
              class="mt12"
              :title="`多选题判分策略：${theoryData.partialScoring.multiChoice}`"
            />
          </template>
        </el-tab-pane>

        <!-- 班级分析 -->
        <el-tab-pane label="班级能力分析" name="class">
          <div class="toolbar">
            <el-input
              v-model="className"
              placeholder="按班级名称过滤（可空）"
              clearable
              style="width: 240px"
              @keyup.enter="loadClass"
            />
            <el-button type="primary" plain :loading="loading" @click="loadClass">查询</el-button>
          </div>

          <el-empty
            v-if="!classData || !classData.byClass?.length"
            description="暂无班级统计数据（用户未填写班级或尚无提交）"
            :image-size="70"
          />
          <template v-else>
            <el-table :data="classData.byClass" size="small" border>
              <el-table-column label="班级" min-width="180">
                <template #default="{ row }">{{ row.className || '未填写班级' }}</template>
              </el-table-column>
              <el-table-column prop="users" label="人数" width="100" align="center" />
              <el-table-column prop="totalScore" label="总分" width="120" align="center" />
              <el-table-column prop="avgScore" label="人均分" width="120" align="center" />
            </el-table>
            <p class="gen-at">生成时间：{{ classData.generatedAt }}</p>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { opsApi, contestApi } from '@/api'

const loading = ref(false)
const tab = ref('theory')
const contests = ref([])
// 雪花 ID 全程字符串，切勿 Number()
const contestId = ref('')
const className = ref('')

const theoryData = ref(null)
const classData = ref(null)

const totalSubmits = computed(() =>
  (theoryData.value?.questionAnalysis || []).reduce((s, r) => s + Number(r.submits || 0), 0)
)

const overallAcc = computed(() => {
  const list = theoryData.value?.questionAnalysis || []
  let sub = 0
  let cor = 0
  for (const r of list) {
    sub += Number(r.submits || 0)
    cor += Number(r.corrects || 0)
  }
  if (!sub) return '0.0'
  return ((cor / sub) * 100).toFixed(1)
})

function accColor(acc) {
  const v = Number(acc) || 0
  if (v >= 80) return '#00b42a'
  if (v >= 50) return '#ff7d00'
  return '#f53f3f'
}

async function loadContests() {
  try {
    const res = await contestApi.list({ page: 1, size: 200 })
    contests.value = res?.records || []
  } catch { /* 忽略，列表为空时用户仍可手动重试 */ }
}

async function loadTheory() {
  if (!contestId.value) {
    theoryData.value = null
    return
  }
  loading.value = true
  try {
    const res = await opsApi.theoryReport({ contestId: contestId.value })
    // 后端在无数据时仍返回空数组，这里判定为"无数据"
    const hasData = (res?.questionAnalysis?.length || 0) > 0
    theoryData.value = hasData ? res : null
  } catch (e) {
    ElMessage.error(e.message || '加载理论赛报告失败')
    theoryData.value = null
  } finally {
    loading.value = false
  }
}

async function loadClass() {
  loading.value = true
  try {
    const params = {}
    if (contestId.value) params.contestId = contestId.value
    if (className.value.trim()) params.className = className.value.trim()
    const res = await opsApi.classReport(params)
    classData.value = res || null
  } catch (e) {
    ElMessage.error(e.message || '加载班级报告失败')
    classData.value = null
  } finally {
    loading.value = false
  }
}

function onContestChange() {
  if (tab.value === 'theory') loadTheory()
  else loadClass()
}

function reload() {
  if (tab.value === 'theory') loadTheory()
  else loadClass()
}

onMounted(async () => {
  await loadContests()
  await loadClass()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 10px; align-items: center; }
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; }
.mb12 { margin-bottom: 12px; }
.mt12 { margin-top: 12px; }

.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 8px;
}
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-label { font-size: 13px; color: #86909c; margin-bottom: 6px; }
.stat-value { font-size: 24px; font-weight: 600; color: var(--csp-primary); line-height: 1; }

.gen-at { margin-top: 12px; font-size: 12px; color: #86909c; text-align: right; }
</style>