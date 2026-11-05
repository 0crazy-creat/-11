<template>
  <div class="cfs-topology">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>CFS 分层拓扑管理</span>
          <div class="actions">
            <el-select v-model="filterContestId" placeholder="全部赛事" clearable style="width: 220px" @change="loadList">
              <el-option v-for="c in contests" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
            </el-select>
            <el-button @click="loadList" :loading="loading">刷新</el-button>
            <el-button type="primary" @click="openCreate">新建拓扑</el-button>
          </div>
        </div>
      </template>

      <el-table :data="rows" v-loading="loading" size="small" border>
        <el-table-column prop="code" label="拓扑编码" width="160" />
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="layerCount" label="层数" width="80" align="center" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属赛事" min-width="160">
          <template #default="{ row }">{{ contestName(row.contestId) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status !== 'PUBLISHED'"
              link type="success"
              size="small"
              @click="doPublish(row)"
            >发布</el-button>
            <el-button
              v-if="row.contestId"
              link
              type="warning"
              size="small"
              @click="doStart(row)"
            >开赛</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无拓扑，点击「新建拓扑」开始编排" />
        </template>
      </el-table>
    </el-card>

    <!-- 新建拓扑 -->
    <el-dialog v-model="createVisible" title="新建 CFS 分层拓扑" width="980px" top="4vh">
      <el-form :model="form" label-width="96px" size="small">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="拓扑编码" required>
              <el-input v-model="form.code" placeholder="如 CFS-WEB-3L" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="拓扑名称" required>
              <el-input v-model="form.name" placeholder="如 三层 Web 渗透" maxlength="128" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属赛事">
              <el-select v-model="form.contestId" placeholder="可留空作为通用模板" clearable style="width: 100%">
                <el-option v-for="c in cfsContests" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="512" />
        </el-form-item>

        <el-divider content-position="left">层定义（1~3 层，越深层分值越高，仅 LAYER1 暴露端口）</el-divider>
        <div class="layer-list">
          <div v-for="(l, i) in form.layers" :key="i" class="layer-row">
            <el-tag :type="i === 0 ? 'success' : 'warning'" effect="plain" class="layer-tag">
              LAYER{{ l.layerNo }}
            </el-tag>
            <el-input v-model="l.name" placeholder="层名称" style="width: 150px" />
            <el-input-number v-model="l.baseScore" :min="1" :step="50" placeholder="基础分" controls-position="right" style="width: 130px" />
            <el-input-number v-model="l.scoreStep" :min="0" :step="10" placeholder="递进步长" controls-position="right" style="width: 130px" />
            <el-input v-model="l.networkCidr" placeholder="网段，留空自动" style="width: 170px" />
            <span class="expose-hint">{{ l.layerNo === 1 ? '对外暴露' : '内网隐藏' }}</span>
            <el-button
              v-if="form.layers.length > 1 && i === form.layers.length - 1"
              link
              type="danger"
              size="small"
              @click="removeLayer(i)"
            >删除</el-button>
          </div>
          <el-button v-if="form.layers.length < 3" link type="primary" size="small" @click="addLayer">
            + 增加一层（最多 3 层）
          </el-button>
        </div>

        <el-divider content-position="left">节点定义（可选，可后续补充）</el-divider>
        <div class="node-list">
          <div v-for="(n, i) in form.nodes" :key="i" class="node-row">
            <el-select v-model="n.layerNo" style="width: 110px">
              <el-option v-for="l in form.layers" :key="l.layerNo" :label="`LAYER${l.layerNo}`" :value="l.layerNo" />
            </el-select>
            <el-input v-model="n.nodeCode" placeholder="节点编码" style="width: 130px" />
            <el-input v-model="n.name" placeholder="节点名称" style="width: 150px" />
            <el-input v-model="n.imageName" placeholder="镜像，如 ctf/web" style="width: 170px" />
            <el-input-number v-model="n.containerPort" :min="1" :max="65535" placeholder="端口" controls-position="right" style="width: 110px" />
            <el-input v-model="n.flag" placeholder="flag（可空）" style="width: 180px" />
            <el-button link type="danger" size="small" @click="form.nodes.splice(i, 1)">删除</el-button>
          </div>
          <el-button link type="primary" size="small" @click="addNode">+ 添加节点</el-button>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 拓扑详情 -->
    <el-dialog v-model="detailVisible" :title="`拓扑详情 - ${detail?.name || ''}`" width="860px">
      <el-descriptions :column="2" size="small" border v-if="detail">
        <el-descriptions-item label="编码">{{ detail.code }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="层数">{{ detail.layerCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">层</el-divider>
      <el-table :data="detailLayers" size="small" border>
        <el-table-column prop="layerNo" label="层号" width="70" align="center" />
        <el-table-column prop="name" label="名称" width="120" />
        <el-table-column prop="baseScore" label="基础分" width="90" />
        <el-table-column prop="networkCidr" label="网段" min-width="150" />
        <el-table-column label="暴露端口" width="100" align="center">
          <template #default="{ row }">{{ row.exposePort ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">节点</el-divider>
      <el-table :data="detailNodes" size="small" border>
        <el-table-column prop="layerNo" label="层" width="70" align="center" />
        <el-table-column prop="nodeCode" label="编码" width="130" />
        <el-table-column prop="name" label="名称" min-width="130" />
        <el-table-column prop="imageName" label="镜像" min-width="140" />
        <el-table-column prop="score" label="分值" width="80" align="center" />
      </el-table>
      <el-empty v-if="!detailNodes.length" description="该拓扑暂未配置节点" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cfsApi, contestApi } from '@/api'

const loading = ref(false)
const creating = ref(false)
const createVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const detailLayers = ref([])
const detailNodes = ref([])
const rows = ref([])
const contests = ref([])
const filterContestId = ref('')

// 雪花 ID 保持字符串，切勿 Number()
const cfsContests = computed(() => contests.value.filter(c => c.type === 'CFS'))

const form = reactive({
  code: '',
  name: '',
  description: '',
  contestId: '',
  layers: [],
  nodes: []
})

function contestName(id) {
  if (!id) return '通用模板'
  const c = contests.value.find(x => String(x.id) === String(id))
  return c ? c.name : id
}

function resetForm() {
  form.code = ''
  form.name = ''
  form.description = ''
  form.contestId = ''
  form.layers = [makeLayer(1, 100)]
  form.nodes = []
}

function makeLayer(no, baseScore) {
  return {
    layerNo: no,
    name: no === 1 ? '外网' : no === 2 ? '内网' : '深网',
    prevLayerNo: no - 1,
    baseScore: baseScore ?? [100, 300, 600][no - 1] ?? 100 * no,
    scoreStep: 50,
    networkCidr: '',
    exposePort: no === 1 ? 1 : 0
  }
}

function addLayer() {
  const no = form.layers.length + 1
  const prev = form.layers[form.layers.length - 1]
  const base = (prev?.baseScore ?? 100) + 200
  form.layers.push(makeLayer(no, base))
}

function removeLayer(i) {
  form.layers.splice(i, 1)
  // 重排层号，保证连续
  form.layers.forEach((l, idx) => {
    l.layerNo = idx + 1
    l.prevLayerNo = idx
    l.exposePort = idx === 0 ? 1 : 0
    l.name = idx === 0 ? '外网' : idx === 1 ? '内网' : '深网'
  })
}

function addNode() {
  form.nodes.push({
    layerNo: form.layers[0]?.layerNo ?? 1,
    nodeCode: '',
    name: '',
    imageName: '',
    containerPort: 80,
    flag: '',
    isEntry: 0
  })
}

async function loadContests() {
  try {
    const res = await contestApi.list({ page: 1, size: 200 })
    contests.value = res?.records || []
  } catch { /* ignore */ }
}

async function loadList() {
  loading.value = true
  try {
    const res = await cfsApi.listTopologies(filterContestId.value ? { contestId: filterContestId.value } : {})
    rows.value = Array.isArray(res) ? res : (res?.records || [])
  } catch (e) {
    ElMessage.error(e.message || '加载拓扑列表失败')
    rows.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  createVisible.value = true
}

/** 前端预校验：与后端 CfsUnlockGuard 规则保持一致，提前拦截而不是等 400 */
function validate() {
  if (!form.code.trim()) return '拓扑编码不能为空'
  if (!form.name.trim()) return '拓扑名称不能为空'
  if (!form.layers.length) return '至少需要一层定义'
  if (form.layers.length > 3) return '最多 3 层'

  let prevBase = null
  for (const l of form.layers) {
    if (prevBase !== null && l.baseScore <= prevBase) {
      return `递进得分要求越深层分值越高：LAYER${l.layerNo} 分值须大于 LAYER${l.layerNo - 1}`
    }
    prevBase = l.baseScore
  }

  for (const n of form.nodes) {
    if (!n.nodeCode?.trim()) return '节点编码不能为空'
    if (!n.name?.trim()) return '节点名称不能为空'
    if (n.layerNo < 1 || n.layerNo > form.layers.length) {
      return `节点 ${n.nodeCode} 层号越界：${n.layerNo}`
    }
  }
  return null
}

async function submitCreate() {
  const err = validate()
  if (err) {
    ElMessage.warning(err)
    return
  }
  creating.value = true
  try {
    const payload = {
      code: form.code.trim(),
      name: form.name.trim(),
      description: form.description || undefined,
      contestId: form.contestId || undefined,
      layers: form.layers.map(l => ({
        layerNo: l.layerNo,
        name: l.name || undefined,
        prevLayerNo: l.prevLayerNo,
        baseScore: l.baseScore,
        scoreStep: l.scoreStep,
        networkCidr: l.networkCidr || undefined,
        exposePort: l.exposePort
      })),
      nodes: form.nodes.map(n => ({
        layerNo: n.layerNo,
        nodeCode: n.nodeCode.trim(),
        name: n.name.trim(),
        imageName: n.imageName || undefined,
        containerPort: n.containerPort || 80,
        flag: n.flag || undefined,
        isEntry: n.layerNo === 1 ? 1 : 0
      }))
    }
    await cfsApi.createTopology(payload)
    ElMessage.success('拓扑创建成功')
    createVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    creating.value = false
  }
}

async function openDetail(row) {
  detail.value = row
  detailVisible.value = true
  detailLayers.value = []
  detailNodes.value = []
  try {
    const [ls, ns] = await Promise.all([
      cfsApi.topologyLayers(row.id),
      cfsApi.topologyNodes(row.id)
    ])
    detailLayers.value = ls || []
    detailNodes.value = ns || []
  } catch (e) {
    ElMessage.error(e.message || '加载拓扑详情失败')
  }
}

async function doPublish(row) {
  try {
    await ElMessageBox.confirm(
      `发布后该拓扑可用于开赛，确认发布「${row.name}」吗？`,
      '确认发布',
      { type: 'warning' }
    )
  } catch { return }
  try {
    await cfsApi.publish(row.id)
    ElMessage.success('拓扑已发布')
    await loadList()
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  }
}

async function doStart(row) {
  try {
    await ElMessageBox.confirm(
      `将为所有审核通过的战队拉起分层拓扑，确认开赛「${contestName(row.contestId)}」吗？`,
      '确认开赛',
      { type: 'warning' }
    )
  } catch { return }
  try {
    const n = await cfsApi.start(row.contestId)
    ElMessage.success(`开赛成功，已为 ${n ?? 0} 支战队拉起拓扑`)
  } catch (e) {
    ElMessage.error(e.message || '开赛失败')
  }
}

onMounted(async () => {
  await loadContests()
  await loadList()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 10px; align-items: center; }

.layer-list, .node-list { display: flex; flex-direction: column; gap: 10px; }
.layer-row, .node-row {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 10px; background: #f7f9ff; border-radius: 8px;
}
.layer-tag { width: 74px; text-align: center; }
.expose-hint { font-size: 12px; color: #86909c; width: 64px; }
</style>