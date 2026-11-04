<template>
  <div>
    <el-card shadow="never" header="题目管理">
      <div class="toolbar">
        <el-select v-model="contestId" placeholder="选择赛事" style="width:220px" @change="load">
          <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" :disabled="!contestId" @click="openCreate">新增题目</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" size="small">
        <el-table-column prop="title" label="题目" min-width="180" />
        <el-table-column prop="category" label="分类" width="100" />
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
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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
          <el-form-item label="附件地址">
            <el-input v-model="form.attachmentUrl" placeholder="附件下载URL" />
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

const categories = ['WEB', 'PWN', 'REVERSE', 'CRYPTO', 'MISC', 'BLOCKCHAIN', 'AWD']
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

function getTypeLabel(type) {
  const map = { 'STATIC_ATTACHMENT': '静态附件', 'STATIC_CONTAINER': '静态容器', 'DYNAMIC_CONTAINER': '动态容器' }
  return map[type] || '未知'
}

function getTypeTagColor(type) {
  const map = { 'STATIC_ATTACHMENT': 'info', 'STATIC_CONTAINER': 'warning', 'DYNAMIC_CONTAINER': 'success' }
  return map[type] || 'info'
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
  Object.assign(form, {
    ...defaultForm, ...row,
    challengeType: row.challengeType || (row.containerImage || row.imageName ? 'STATIC_CONTAINER' : 'STATIC_ATTACHMENT')
  })
  visible.value = true
}

async function save() {
  if (!form.title) { ElMessage.warning('请输入题目标题'); return }
  if (isContainerType.value && !form.containerImage) { ElMessage.warning('容器题必须填写Docker镜像名'); return }
  if (form.challengeType !== 'DYNAMIC_CONTAINER' && !form.flag) { ElMessage.warning('静态题必须填写Flag'); return }

  saving.value = true
  try {
    const payload = { ...form, contestId: contestId.value, imageName: form.containerImage }
    if (form.id) await challengeApi.update(form.id, payload)
    else await challengeApi.create(payload)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally { saving.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm('删除题目 ' + row.title + ' ？', '提示', { type: 'warning' })
  await challengeApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const data = await contestApi.list({ page: 1, size: 100 })
  contests.value = data.list || data.records || data || []
  if (contests.value.length) { contestId.value = contests.value[0].id; load() }
})
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.form-help { font-size: 12px; color: #909399; margin-top: 4px; }
.image-text { font-family: monospace; font-size: 12px; color: #606266; }
.text-gray { color: #c0c4cc; }
</style>