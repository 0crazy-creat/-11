<template>
  <div class="awdp-defense">
    <header class="page-header">
      <h2>AWDP 防御修补</h2>
      <p class="sub">上传修补包（zip / patch）→ 平台自动 check 校验 → 应用补丁并重启靶机 → 防御分结算</p>
    </header>

    <!-- 提交表单 -->
    <section class="card">
      <h3>提交修补包</h3>
      <el-form :model="form" label-width="110px" class="form">
        <el-form-item label="赛事ID">
          <el-input v-model="form.contestId" placeholder="AWDP 赛事ID" style="width: 220px" />
        </el-form-item>
        <el-form-item label="题目ID">
          <el-input v-model="form.questionId" placeholder="题目/靶机ID" style="width: 220px" />
        </el-form-item>
        <el-form-item label="轮次号">
          <el-input v-model="form.roundNo" placeholder="留空取当前可修补轮次" style="width: 220px" />
        </el-form-item>
        <el-form-item label="修补说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="简述修复内容，便于裁判复核" />
        </el-form-item>
        <el-form-item label="修补包">
          <input ref="fileRef" type="file" accept=".zip,.patch,.diff,.tar.gz,.tgz" @change="onFileChange" />
          <span v-if="form.file" class="file-tip">{{ form.file.name }}（{{ (form.file.size / 1024).toFixed(1) }} KB）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="onSubmit">
            上传并自动校验
          </el-button>
          <el-button @click="loadMine">刷新我的提交</el-button>
        </el-form-item>
      </el-form>
      <el-progress v-if="submitting" :percentage="uploadPercent" />
    </section>

    <!-- 最近一次提交结果（逐步证据） -->
    <section v-if="lastResult" class="card result" :class="resultClass">
      <h3>本次验证结果：{{ lastResult.status }}</h3>
      <p class="msg">{{ lastResult.message }}</p>
      <div class="score-line">
        防御分增量：<b>{{ lastResult.gainedScore }}</b>　队伍总分：<b>{{ lastResult.totalScore }}</b>
      </div>
      <el-steps :active="stepActive" finish-status="success" simple class="steps">
        <el-step title="拷入容器" :status="boolStatus(lastResult.copyOk)" />
        <el-step title="应用补丁" :status="boolStatus(lastResult.applyOk)" />
        <el-step title="重启靶机" :status="boolStatus(lastResult.restartOk)" />
        <el-step title="健康检查" :status="boolStatus(lastResult.healthOk)" />
        <el-step title="check 校验" :status="boolStatus(lastResult.checkOk)" />
      </el-steps>
      <el-collapse v-if="lastResult.applyOutput || lastResult.checkOutput || (lastResult.failReasons && lastResult.failReasons.length)">
        <el-collapse-item title="验证过程输出（点击展开）" name="1">
          <div v-if="lastResult.failReasons && lastResult.failReasons.length" class="fail">
            <p v-for="(r, i) in lastResult.failReasons" :key="i">✗ {{ r }}</p>
          </div>
          <pre v-if="lastResult.applyOutput" class="out">[apply]\n{{ lastResult.applyOutput }}</pre>
          <pre v-if="lastResult.checkOutput" class="out">[check]\n{{ lastResult.checkOutput }}</pre>
          <p class="sha">SHA-256：{{ lastResult.sha256 }}</p>
        </el-collapse-item>
      </el-collapse>
    </section>

    <!-- 历史提交 -->
    <section class="card">
      <h3>我的修补提交记录</h3>
      <el-table :data="mine" size="small" empty-text="暂无提交记录">
        <el-table-column prop="roundNo" label="轮次" width="70" />
        <el-table-column prop="questionId" label="题目" width="80" />
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
        <el-table-column prop="packageType" label="类型" width="80" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defenseScore" label="防御分" width="80" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { defenseApi } from '@/api'

const form = reactive({ contestId: '', questionId: '', roundNo: '', remark: '', file: null })
const fileRef = ref(null)
const submitting = ref(false)
const uploadPercent = ref(0)
const lastResult = ref(null)
const mine = ref([])

const canSubmit = computed(() => form.contestId && form.questionId && form.file)

function onFileChange(e) {
  form.file = e.target.files?.[0] || null
}

async function onSubmit() {
  if (!canSubmit.value) return
  const fd = new FormData()
  fd.append('contestId', form.contestId)
  fd.append('questionId', form.questionId)
  if (form.roundNo) fd.append('roundNo', form.roundNo)
  if (form.remark) fd.append('remark', form.remark)
  fd.append('file', form.file)

  submitting.value = true
  uploadPercent.value = 0
  try {
    const { data } = await defenseApi.submit(fd, (evt) => {
      if (evt.total) uploadPercent.value = Math.round((evt.loaded / evt.total) * 100)
    })
    lastResult.value = data
    const ok = data.status === 'VERIFIED'
    ElMessage[ok ? 'success' : 'warning'](data.message || '处理完成')
    await loadMine()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    submitting.value = false
  }
}

async function loadMine() {
  if (!form.contestId) return
  try {
    const { data } = await defenseApi.listMine(form.contestId)
    mine.value = data || []
  } catch (e) { /* ignore */ }
}

async function showDetail(id) {
  try {
    const { data } = await defenseApi.detail(id)
    ElMessage.info(`状态 ${data.status}，防御分 ${data.defenseScore}`)
  } catch (e) { /* ignore */ }
}

const stepActive = computed(() => {
  const r = lastResult.value
  if (!r) return 0
  let n = 0
  if (r.copyOk) n++
  if (r.applyOk) n++
  if (r.restartOk) n++
  if (r.healthOk) n++
  if (r.checkOk) n++
  return n
})
const resultClass = computed(() => {
  const s = lastResult.value?.status
  if (s === 'VERIFIED') return 'ok'
  if (s === 'COMPROMISED') return 'warn'
  return 'bad'
})
function boolStatus(v) {
  return v === true ? 'success' : v === false ? 'error' : 'wait'
}
function tagType(s) {
  return { VERIFIED: 'success', COMPROMISED: 'warning', REJECTED: 'danger', FAILED: 'danger' }[s] || 'info'
}
function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}
</script>

<style scoped>
.awdp-defense { max-width: 1000px; margin: 0 auto; padding: 20px; }
.page-header h2 { margin: 0 0 4px; }
.page-header .sub { color: #888; font-size: 13px; margin: 0 0 16px; }
.card { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 16px 20px; margin-bottom: 18px; }
.card h3 { margin: 0 0 12px; font-size: 15px; }
.file-tip { margin-left: 10px; color: #666; font-size: 12px; }
.result.ok { border-left: 4px solid #67c23a; }
.result.warn { border-left: 4px solid #e6a23c; }
.result.bad { border-left: 4px solid #f56c6c; }
.result .msg { font-weight: 600; margin: 6px 0; }
.score-line { margin: 8px 0; }
.steps { margin: 12px 0; }
.out { background: #f5f7fa; padding: 10px; border-radius: 6px; font-size: 12px; white-space: pre-wrap; max-height: 260px; overflow: auto; }
.fail p { color: #f56c6c; margin: 2px 0; }
.sha { color: #999; font-size: 12px; word-break: break-all; }
</style>