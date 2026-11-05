<template>
  <div class="question-bank">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <div>
            <span class="title">全局题库</span>
            <span class="subtitle">共 {{ total }} 道题，与赛事挂载相互独立</span>
          </div>
          <el-button type="primary" @click="openCreate">新增题目</el-button>
        </div>
      </template>

      <div class="filters">
        <el-radio-group v-model="filter.bankType" @change="reload">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="CTF">CTF</el-radio-button>
          <el-radio-button value="THEORY">理论</el-radio-button>
          <el-radio-button value="SCENARIO">场景</el-radio-button>
          <el-radio-button value="AWD">AWD</el-radio-button>
        </el-radio-group>

        <el-select v-model="filter.category" placeholder="分类" clearable style="width: 130px" @change="reload">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>

        <el-select v-model="filter.difficulty" placeholder="难度" clearable style="width: 120px" @change="reload">
          <el-option label="简单" value="EASY" />
          <el-option label="中等" value="MEDIUM" />
          <el-option label="困难" value="HARD" />
          <el-option label="地狱" value="INSANE" />
        </el-select>

        <el-input
          v-model="filter.keyword"
          placeholder="搜索标题 / 题面"
          clearable
          style="width: 220px"
          @input="debouncedReload"
        />
      </div>

      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="title" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column label="题库" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="bankTagType(row.bankType)">{{ bankLabel(row.bankType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ normCategory(row.category) }}</template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">{{ difficultyLabel(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <span>{{ typeLabel(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="镜像" width="210">
          <template #default="{ row }">
            <span class="mono">{{ imageOf(row) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑题目' : '新增题目'" width="780px" destroy-on-close>
      <el-form :model="form" label-width="110px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="题库类型" required>
          <el-radio-group v-model="form.bankType" :disabled="!!form.id">
            <el-radio-button value="CTF">CTF</el-radio-button>
            <el-radio-button value="THEORY">理论</el-radio-button>
            <el-radio-button value="SCENARIO">场景</el-radio-button>
            <el-radio-button value="AWD">AWD</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="255" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
                <el-option label="地狱" value="INSANE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值">
              <el-input-number v-model="form.score" :min="0" :max="100000" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="题面">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="支持 Markdown：## 标题、- 列表、`代码`" />
        </el-form-item>

        <el-divider content-position="left">题目类型</el-divider>
        <el-form-item label="类型">
          <el-radio-group v-model="form.challengeType">
            <el-radio-button value="STATIC_ATTACHMENT">静态附件</el-radio-button>
            <el-radio-button value="STATIC_CONTAINER">静态容器</el-radio-button>
            <el-radio-button value="DYNAMIC_CONTAINER">动态容器</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <template v-if="isContainer">
          <el-form-item label="Docker 镜像" required>
            <el-input v-model="form.imageName" placeholder="如 csp/web-sql-easy:latest" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="容器端口">
                <el-input-number v-model="form.exposePort" :min="1" :max="65535" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="CPU(核)">
                <el-input-number v-model="form.cpuLimit" :min="0.1" :max="4" :step="0.1" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="内存(MB)">
                <el-input-number v-model="form.memoryLimit" :min="64" :max="4096" :step="64" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-else>
          <el-form-item label="题目附件">
            <div class="upload-row">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                name="file"
                :show-file-list="false"
                :before-upload="onBeforeUpload"
                :on-success="onAttachmentUploaded"
                :on-error="onAttachmentError"
              >
                <el-button :loading="uploading" type="primary" plain>上传附件</el-button>
              </el-upload>
              <span v-if="form.attachment" class="upload-file">
                <a :href="form.attachment" target="_blank" rel="noopener">{{ attachmentName }}</a>
                <el-button link type="danger" @click="form.attachment = ''">移除</el-button>
              </span>
              <span v-else class="upload-tip">文件直接上传到服务器，选手在题目页点击即可下载</span>
            </div>
          </el-form-item>
        </template>

        <el-divider content-position="left">答案配置</el-divider>
        <el-form-item v-if="form.bankType === 'CTF'" label="Flag">
          <el-input v-model="form.flag" placeholder="flag{...}" />
        </el-form-item>
        <el-form-item v-else label="参考答案">
          <el-input v-model="form.answer" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionApi } from '@/api'

const categories = ['Web', 'Pwn', 'Reverse', 'Crypto', 'Misc', 'Blockchain', 'Forensics', 'Theory', 'Scenario']

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)

const filter = reactive({ bankType: '', category: '', difficulty: '', keyword: '' })

const defaultForm = () => ({
  id: null,
  bankType: 'CTF',
  title: '',
  category: 'Web',
  difficulty: 'EASY',
  score: 100,
  status: 1,
  content: '',
  answer: '',
  flag: '',
  attachment: '',
  challengeType: 'STATIC_ATTACHMENT',
  imageName: '',
  exposePort: 80,
  cpuLimit: 1.0,
  memoryLimit: 256
})

const form = reactive(defaultForm())

const isContainer = computed(
  () => form.challengeType === 'STATIC_CONTAINER' || form.challengeType === 'DYNAMIC_CONTAINER'
)

// ===== 附件上传（走后端 /api/files/upload，不走 axios 拦截器）=====
const uploadUrl = '/api/files/upload'
const uploading = ref(false)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('csp_token') || ''}`
}))
const attachmentName = computed(() => {
  const raw = (form.attachment || '').split('/').pop() || ''
  const idx = raw.indexOf('_')
  const name = idx >= 0 ? raw.slice(idx + 1) : raw
  try {
    return decodeURIComponent(name)
  } catch {
    return name
  }
})

function onBeforeUpload() {
  uploading.value = true
  return true
}

function onAttachmentUploaded(res) {
  uploading.value = false
  if (res && res.code === 0 && res.data && res.data.url) {
    form.attachment = res.data.url
    ElMessage.success('附件已上传')
  } else {
    ElMessage.error((res && res.message) || '上传失败')
  }
}

function onAttachmentError() {
  uploading.value = false
  ElMessage.error('上传失败，请检查文件大小（单文件不超过 200MB）')
}

let searchTimer = null

onMounted(load)
onBeforeUnmount(() => clearTimeout(searchTimer))

function bankLabel(type) {
  return { CTF: 'CTF', THEORY: '理论', SCENARIO: '场景', AWD: 'AWD' }[type] || type || '未知'
}

function bankTagType(type) {
  return { CTF: 'danger', THEORY: 'success', SCENARIO: 'warning', AWD: 'info' }[type] || 'info'
}

function difficultyLabel(d) {
  return { EASY: '简单', MEDIUM: '中等', HARD: '困难', INSANE: '地狱' }[d] || d || '-'
}

const CATEGORY_ALIASES = {
  web: 'Web', misc: 'Misc', crypto: 'Crypto', reverse: 'Reverse', pwn: 'Pwn',
  blockchain: 'Blockchain', forensics: 'Forensics', theory: 'Theory', scenario: 'Scenario'
}

function normCategory(category) {
  const raw = (category == null ? '' : String(category)).trim()
  if (!raw) return '-'
  return CATEGORY_ALIASES[raw.toLowerCase()] || raw
}

function parseExt(row) {
  if (!row?.ext) return {}
  if (typeof row.ext === 'object') return row.ext
  try {
    return JSON.parse(row.ext)
  } catch {
    return {}
  }
}

function typeLabel(row) {
  const ext = parseExt(row)
  const type = ext.challengeType
  if (type === 'DYNAMIC_CONTAINER') return '动态容器'
  if (type === 'STATIC_CONTAINER') return '静态容器'
  if (type === 'STATIC_ATTACHMENT') return '静态附件'
  return row.imageName ? '静态容器' : '静态附件'
}

function imageOf(row) {
  const ext = parseExt(row)
  return ext.containerImage || ext.imageName || row.imageName || ''
}

async function load() {
  loading.value = true
  try {
    const data = await questionApi.page({
      page: page.value,
      size: size.value,
      bankType: filter.bankType || undefined,
      category: filter.category || undefined,
      difficulty: filter.difficulty || undefined,
      keyword: filter.keyword || undefined
    })
    rows.value = data?.records || data?.list || []
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载题库失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function debouncedReload() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(reload, 300)
}

function onSizeChange() {
  page.value = 1
  load()
}

function openCreate() {
  Object.assign(form, defaultForm())
  visible.value = true
}

function openEdit(row) {
  const ext = parseExt(row)
  Object.assign(form, defaultForm(), {
    id: row.id,
    bankType: row.bankType || 'CTF',
    title: row.title || '',
    category: normCategory(row.category) === '-' ? 'Web' : normCategory(row.category),
    difficulty: row.difficulty || 'EASY',
    score: row.score ?? 100,
    status: row.status ?? 1,
    content: row.content || '',
    answer: row.answer || '',
    flag: row.flag || '',
    attachment: row.attachment || '',
    challengeType: ext.challengeType || (row.imageName ? 'STATIC_CONTAINER' : 'STATIC_ATTACHMENT'),
    imageName: ext.containerImage || ext.imageName || row.imageName || '',
    exposePort: ext.exposePort || 80,
    cpuLimit: ext.cpuLimit ?? 1.0,
    memoryLimit: ext.memoryLimit ?? 256
  })
  visible.value = true
}

function buildPayload() {
  const ext = { challengeType: form.challengeType }
  if (isContainer.value) {
    ext.imageName = form.imageName
    ext.containerImage = form.imageName
    ext.exposePort = form.exposePort
    ext.cpuLimit = form.cpuLimit
    ext.memoryLimit = form.memoryLimit
    ext.storageLimit = 512
    ext.networkMode = 'ISOLATED'
    ext.isDocker = true
    if (form.challengeType === 'DYNAMIC_CONTAINER') ext.judgeMode = 'DYNAMIC'
  } else {
    ext.isDocker = false
  }

  return {
    bankType: form.bankType,
    title: form.title.trim(),
    category: form.category,
    difficulty: form.difficulty,
    score: form.score,
    status: form.status,
    content: form.content,
    answer: form.answer || null,
    flag: form.flag || null,
    attachment: isContainer.value ? null : (form.attachment || null),
    imageName: isContainer.value ? form.imageName : null,
    ext: JSON.stringify(ext)
  }
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入题目标题')
    return
  }
  if (isContainer.value && !form.imageName.trim()) {
    ElMessage.warning('容器题必须填写 Docker 镜像')
    return
  }
  if (form.bankType === 'CTF' && !form.flag.trim()) {
    ElMessage.warning('CTF 题必须填写 Flag')
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (form.id) await questionApi.update(form.id, payload)
    else await questionApi.create(payload)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } catch (e) {
    // 请求拦截器已统一提示
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除题目「${row.title}」？该操作会同时从所有赛事移除。`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await questionApi.remove(row.id)
    ElMessage.success('已删除')
    if (rows.value.length === 1 && page.value > 1) page.value -= 1
    load()
  } catch (e) {
    // 请求拦截器已统一提示
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.subtitle {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 14px;
}

.mono {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-file {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.upload-file a {
  color: var(--el-color-primary);
  text-decoration: none;
}

.upload-file a:hover {
  text-decoration: underline;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
