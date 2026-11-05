<template>
  <div class="archive-manage">
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>赛事备案归档</span>
          <el-button type="primary" @click="openCreate">新建归档</el-button>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="备案编号">
          <el-input v-model="query.filingNo" placeholder="按备案编号查询" clearable style="width: 200px" @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="赛事">
          <el-select v-model="query.contestId" placeholder="全部赛事" clearable filterable style="width: 240px">
            <el-option v-for="c in contests" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" size="small" empty-text="暂无归档记录">
        <el-table-column prop="filingNo" label="备案编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contestName" label="赛事名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="赛制" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.contestType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="filingOrg" label="备案单位" min-width="140" show-overflow-tooltip />
        <el-table-column label="快照规模" width="200">
          <template #default="{ row }">
            <span class="stat">题 {{ row.questionCount ?? 0 }}</span>
            <span class="stat">队 {{ row.teamCount ?? 0 }}</span>
            <span class="stat">提交 {{ row.submissionCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="archivedAt" label="归档时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" :loading="verifyingId === row.id" @click="doVerify(row)">完整性校验</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadList"
          @size-change="reload"
        />
      </div>
    </el-card>

    <!-- 新建归档 -->
    <el-dialog v-model="createVisible" title="新建赛事归档" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="赛事" prop="contestId">
          <el-select v-model="form.contestId" placeholder="选择要归档的赛事" filterable style="width: 100%">
            <el-option v-for="c in contests" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备案编号" prop="filingNo">
          <el-input v-model="form.filingNo" maxlength="128" show-word-limit placeholder="主办方/监管方备案号" />
        </el-form-item>
        <el-form-item label="备案单位">
          <el-input v-model="form.filingOrg" maxlength="255" placeholder="可选" />
        </el-form-item>
        <el-form-item label="归档说明">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="2000" show-word-limit placeholder="可选" />
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="归档将生成不可变快照并把赛事状态流转为 ARCHIVED，操作后赛事主数据变更不影响快照。"
      />
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">确认归档</el-button>
      </template>
    </el-dialog>

    <!-- 归档详情 -->
    <el-dialog v-model="detailVisible" title="归档详情" width="720px">
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="备案编号">{{ detail.filingNo }}</el-descriptions-item>
          <el-descriptions-item label="备案单位">{{ detail.filingOrg || '-' }}</el-descriptions-item>
          <el-descriptions-item label="赛事名称">{{ detail.contestName }}</el-descriptions-item>
          <el-descriptions-item label="赛事编号">{{ detail.contestCode }}</el-descriptions-item>
          <el-descriptions-item label="赛制">{{ detail.contestType }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="题目数">{{ detail.questionCount }}</el-descriptions-item>
          <el-descriptions-item label="战队数">{{ detail.teamCount }}</el-descriptions-item>
          <el-descriptions-item label="提交数">{{ detail.submissionCount }}</el-descriptions-item>
          <el-descriptions-item label="参赛人数">{{ detail.playerCount }}</el-descriptions-item>
          <el-descriptions-item label="归档时间">{{ detail.archivedAt }}</el-descriptions-item>
          <el-descriptions-item label="归档人">{{ detail.archivedBy }}</el-descriptions-item>
          <el-descriptions-item label="快照摘要" :span="2">
            <span class="checksum">{{ detail.checksum || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="归档说明" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { archiveApi, listContests } from '@/api'

const rows = ref([])
const contests = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const verifyingId = ref(null)

const query = reactive({ filingNo: '', contestId: null })

const createVisible = ref(false)
const creating = ref(false)
const formRef = ref(null)
const form = reactive({ contestId: null, filingNo: '', filingOrg: '', remark: '' })

const detailVisible = ref(false)
const detail = ref(null)

const rules = {
  contestId: [{ required: true, message: '请选择赛事', trigger: 'change' }],
  filingNo: [{ required: true, message: '备案编号不能为空', trigger: 'blur' }]
}

onMounted(async () => {
  await loadContests()
  await loadList()
})

async function loadContests() {
  try {
    const res = await listContests({ page: 1, size: 200 })
    contests.value = res?.records || res || []
  } catch (e) {
    contests.value = []
  }
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (query.filingNo) params.filingNo = query.filingNo
    if (query.contestId) params.contestId = query.contestId
    const res = await archiveApi.page(params)
    rows.value = res?.records || res || []
    total.value = Number(res?.total || rows.value.length)
  } catch (e) {
    rows.value = []
    ElMessage.error(e.message || '加载归档列表失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  loadList()
}

function resetQuery() {
  query.filingNo = ''
  query.contestId = null
  reload()
}

function openCreate() {
  form.contestId = null
  form.filingNo = ''
  form.filingOrg = ''
  form.remark = ''
  createVisible.value = true
}

async function submitCreate() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  creating.value = true
  try {
    await archiveApi.create(form.contestId, {
      filingNo: form.filingNo,
      filingOrg: form.filingOrg || null,
      remark: form.remark || null
    })
    ElMessage.success('归档成功')
    createVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error(e.message || '归档失败')
  } finally {
    creating.value = false
  }
}

async function showDetail(row) {
  detailVisible.value = true
  detail.value = null
  try {
    detail.value = await archiveApi.detail(row.id)
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败')
  }
}

async function doVerify(row) {
  verifyingId.value = row.id
  try {
    const ok = await archiveApi.verify(row.id)
    if (ok === true) {
      ElMessage.success('快照校验通过，内容完整未被篡改')
    } else {
      ElMessage.error('快照校验失败：内容与摘要不一致')
    }
  } catch (e) {
    ElMessage.error(e.message || '校验失败')
  } finally {
    verifyingId.value = null
  }
}
</script>

<style scoped>
.archive-manage { padding: 0; }
.filter-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filter-form { margin-bottom: -18px; }
.stat { margin-right: 10px; color: var(--csp-text); font-size: 13px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.checksum { font-family: Consolas, monospace; font-size: 12px; word-break: break-all; color: var(--csp-text); }
.detail-body { padding: 0 4px; }
</style>