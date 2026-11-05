<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>赛事题目管理</span>
          <span class="head-tip">仅显示当前赛事挂载的题目；题库总量请到「全局题库」管理</span>
        </div>
      </template>
      <div class="toolbar">
        <el-select v-model="contestId" placeholder="选择赛事" style="width:220px" @change="load">
          <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" :disabled="!contestId" @click="openCreate">新增题目</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="title" label="题目" min-width="180" />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ normCategory(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" />
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagColor(row.challengeType)" size="small">
              {{ getTypeLabel(row.challengeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="镜像" width="180">
          <template #default="{ row }">
            <span v-if="row.containerImage" class="image-text">{{ row.containerImage }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑题目' : '新增题目'" width="700px" destroy-on-close>
      <el-form :model="form" label-width="110px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
            <el-option label="地狱" value="INSANE" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值"><el-input-number v-model="form.score" :min="0" :step="100" /></el-form-item>
        <el-form-item label="题目描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>

        <el-divider content-position="left">题目类型</el-divider>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.challengeType" @change="onTypeChange">
            <el-radio-button value="STATIC_ATTACHMENT">静态附件</el-radio-button>
            <el-radio-button value="STATIC_CONTAINER">静态容器</el-radio-button>
            <el-radio-button value="DYNAMIC_CONTAINER">动态容器</el-radio-button>
          </el-radio-group>
          <div class="form-help">{{ typeHelpText }}</div>
        </el-form-item>

        <template v-if="isContainerType">
          <el-divider content-position="left">容器配置</el-divider>
          <el-form-item label="Docker镜像" required>
            <el-input v-model="form.containerImage" placeholder="如: csp/web-php:latest">
              <template #prepend>镜像名</template>
            </el-input>
          </el-form-item>
          <el-form-item label="暴露端口">
            <el-input-number v-model="form.exposePort" :min="1" :max="65535" />
            <div class="form-help">容器内服务端口，Web题填80，SSH题填22</div>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="CPU(核)">
                <el-input-number v-model="form.cpuLimit" :min="0.1" :max="4" :step="0.1" :precision="1" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="内存(MB)">
                <el-input-number v-model="form.memoryLimit" :min="64" :max="4096" :step="64" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="存储(MB)">
                <el-input-number v-model="form.storageLimit" :min="128" :max="2048" :step="128" size="small" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="网络模式">
            <el-select v-model="form.networkMode" style="width: 100%">
              <el-option label="隔离网络" value="ISOLATED" />
              <el-option label="开放网络" value="OPEN" />
              <el-option label="自定义网络" value="CUSTOM" />
            </el-select>
          </el-form-item>
        </template>

        <el-divider content-position="left">Flag配置</el-divider>
        <template v-if="form.challengeType === 'DYNAMIC_CONTAINER'">
          <el-form-item label="Flag模板">
            <el-input v-model="form.flagTemplate" placeholder="如: flag{[TEAM_HASH]}">
              <template #prepend>模板</template>
            </el-input>
            <div class="form-help">[GUID]随机UUID, [TEAM_HASH]队伍哈希</div>
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="Flag">
            <el-input v-model="form.flag" placeholder="输入静态Flag">
              <template #prepend>Flag</template>
            </el-input>
          </el-form-item>
        </template>

        <template v-if="form.challengeType === 'STATIC_ATTACHMENT'">
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
              <span v-if="form.attachmentUrl" class="upload-file">
                <a :href="form.attachmentUrl" target="_blank" rel="noopener">{{ attachmentName }}</a>
                <el-button link type="danger" @click="form.attachmentUrl = ''">移除</el-button>
              </span>
              <span v-else class="upload-tip">文件直接上传到服务器，选手在题目页点击即可下载</span>
            </div>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contestApi, challengeApi } from '@/api'

const categories = ['Web', 'Pwn', 'Reverse', 'Crypto', 'Misc', 'Blockchain', 'Forensics', 'Theory', 'Scenario']
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const contests = ref([])
const contestId = ref(null)
const rows = ref([])

const defaultForm = {
  id: null, title: '', category: 'WEB', difficulty: 'EASY', score: 100,
  flag: '', attachmentUrl: '', description: '',
  challengeType: 'STATIC_ATTACHMENT', containerImage: '',
  exposePort: 80, cpuLimit: 1.0, memoryLimit: 256, storageLimit: 512,
  networkMode: 'ISOLATED', flagTemplate: 'flag{[TEAM_HASH]}'
}
const form = reactive({ ...defaultForm })

const isContainerType = computed(() =>
  form.challengeType === 'STATIC_CONTAINER' || form.challengeType === 'DYNAMIC_CONTAINER'
)

const typeHelpText = computed(() => {
  const map = {
    'STATIC_ATTACHMENT': '纯附件题：所有队伍下载相同附件，使用相同Flag',
    'STATIC_CONTAINER': '静态容器题：所有队伍使用相同Docker镜像和Flag',
    'DYNAMIC_CONTAINER': '动态容器题：每队独立容器，Flag动态生成（推荐）'
  }
  return map[form.challengeType] || ''
})

const CATEGORY_ALIASES = {
  web: 'Web', misc: 'Misc', crypto: 'Crypto', reverse: 'Reverse', pwn: 'Pwn',
  blockchain: 'Blockchain', forensics: 'Forensics', theory: 'Theory', scenario: 'Scenario'
}

/** 题库历史数据存在 WEB/Web 混用，统一显示 */
function normCategory(category) {
  const raw = (category == null ? '' : String(category)).trim()
  if (!raw) return '-'
  return CATEGORY_ALIASES[raw.toLowerCase()] || raw
}

function getTypeLabel(type) {
  const map = { 'STATIC_ATTACHMENT': '静态附件', 'STATIC_CONTAINER': '静态容器', 'DYNAMIC_CONTAINER': '动态容器' }
  return map[type] || '未知'
}

function getTypeTagColor(type) {
  const map = { 'STATIC_ATTACHMENT': 'info', 'STATIC_CONTAINER': 'warning', 'DYNAMIC_CONTAINER': 'success' }
  return map[type] || 'info'
}

// ===== 附件上传（走后端 /api/files/upload，不走 axios 拦截器）=====
const uploadUrl = '/api/files/upload'
const uploading = ref(false)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('csp_token') || ''}`
}))
const attachmentName = computed(() => {
  const raw = (form.attachmentUrl || '').split('/').pop() || ''
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
    form.attachmentUrl = res.data.url
    ElMessage.success('附件已上传')
  } else {
    ElMessage.error((res && res.message) || '上传失败')
  }
}

function onAttachmentError() {
  uploading.value = false
  ElMessage.error('上传失败，请检查文件大小（单文件不超过 200MB）')
}

function onTypeChange() {
  if (!isContainerType.value) { form.containerImage = ''; form.exposePort = 80 }
  if (form.challengeType !== 'DYNAMIC_CONTAINER') { form.flagTemplate = '' }
}

async function load() {
  if (!contestId.value) return
  loading.value = true
  try {
    const data = await challengeApi.list({ contestId: contestId.value })
    rows.value = data.list || data.records || data || []
  } finally { loading.value = false }
}

function openCreate() { Object.assign(form, { ...defaultForm }); visible.value = true }

function openEdit(row) {
  let rawExt = {}
  if (row.ext) {
    try {
      rawExt = typeof row.ext === 'object' ? row.ext : JSON.parse(row.ext)
    } catch {
      rawExt = {}
    }
  }
  Object.assign(form, {
    ...defaultForm, ...row,
    challengeType: row.challengeType || (row.containerImage || row.imageName ? 'STATIC_CONTAINER' : 'STATIC_ATTACHMENT')
  })
  // 保留 ext 中的历史字段（solvedCount 等），保存时与新配置合并，避免整体覆盖丢失
  form._rawExt = rawExt
  visible.value = true
}

async function save() {
  if (!form.title) { ElMessage.warning('请输入题目标题'); return }
  if (isContainerType.value && !form.containerImage) { ElMessage.warning('容器题必须填写Docker镜像名'); return }
  if (form.challengeType !== 'DYNAMIC_CONTAINER' && !form.flag) { ElMessage.warning('静态题必须填写Flag'); return }

  saving.value = true
  try {
    const { _rawExt, ...rest } = form
    const payload = {
      ...rest,
      contestId: contestId.value,
      imageName: form.containerImage,
      ext: JSON.stringify({
        ...(_rawExt || {}),
        challengeType: form.challengeType,
        isDocker: isContainerType.value,
        judgeMode: form.challengeType === 'DYNAMIC_CONTAINER' ? 'DYNAMIC' : 'STATIC',
        ...(isContainerType.value ? {
          imageName: form.containerImage,
          containerImage: form.containerImage,
          exposePort: form.exposePort,
          cpuLimit: form.cpuLimit,
          memoryLimit: form.memoryLimit,
          storageLimit: form.storageLimit,
          networkMode: form.networkMode
        } : {})
      })
    }
    if (form.id) await challengeApi.update(form.id, payload)
    else await challengeApi.create(payload)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally { saving.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(
    `将「${row.title}」从当前赛事移除？题库中的题目不会被删除。`,
    '提示',
    { type: 'warning' }
  )
  await challengeApi.remove(row.id)
  ElMessage.success('已从赛事移除')
  await load()
}

onMounted(async () => {
  const data = await contestApi.list({ page: 1, size: 100 })
  contests.value = data.list || data.records || data || []
  if (!contests.value.length) return
  // 优先选中进行中的赛事，避免默认落在已结束的测试赛事上造成「题库空了」的错觉
  const running = contests.value.find(c => c.status === 'RUNNING')
  contestId.value = (running || contests.value[0]).id
  await load()
})
</script>

<style scoped>
.card-head { display: flex; align-items: baseline; gap: 10px; }
.head-tip { font-size: 12px; font-weight: 400; color: var(--el-text-color-secondary); }
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.form-help { font-size: 12px; color: #909399; margin-top: 4px; }
.image-text { font-family: monospace; font-size: 12px; color: #606266; }
.upload-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.upload-file { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; }
.upload-file a { color: var(--el-color-primary); text-decoration: none; }
.upload-file a:hover { text-decoration: underline; }
.upload-tip { font-size: 12px; color: var(--el-text-color-secondary); }
.text-gray { color: #c0c4cc; }
</style>