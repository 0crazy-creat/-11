<template>
  <div class="cfs-arena">
    <el-card shadow="never" class="head">
      <div class="head-row">
        <div>
          <h3 class="title">{{ topology?.topologyName || 'CFS 分层拓扑' }}</h3>
          <div class="sub">
            已解锁至 <b>LAYER{{ topology?.unlockedLayer ?? 1 }}</b>
            · 累计得分 <b class="score">{{ topology?.totalScore ?? 0 }}</b>
          </div>
        </div>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-empty v-if="!loading && !topology" :description="loadError || '尚未报名该赛事或赛事未配置分层拓扑'" />

    <div v-loading="loading" class="layers">
      <div
        v-for="layer in topology?.layers || []"
        :key="layer.layerNo"
        class="layer"
        :class="{ locked: !layer.unlocked, solved: layer.solved }"
      >
        <div class="layer-head">
          <div class="layer-title">
            <span class="badge" :class="layer.unlocked ? 'on' : 'off'">
              {{ layer.unlocked ? '已解锁' : '未解锁' }}
            </span>
            <b>LAYER{{ layer.layerNo }}</b>
            <span class="layer-name">{{ layer.name }}</span>
            <el-tag v-if="layer.solved" type="success" size="small" effect="dark">已攻破</el-tag>
          </div>
          <div class="layer-meta">
            <span>基础分 {{ layer.baseScore }}</span>
            <span v-if="layer.networkCidr">网段 {{ layer.networkCidr }}</span>
            <el-tag v-if="layer.exposePort === 1" size="small" type="warning">对外暴露</el-tag>
          </div>
        </div>

        <el-alert
          v-if="!layer.unlocked"
          :title="layer.lockedReason || '尚未解锁'"
          type="warning"
          :closable="false"
          show-icon
          class="locked-tip"
        />

        <el-table :data="layer.nodes || []" size="small" class="nodes">
          <el-table-column prop="nodeCode" label="节点" width="120" />
          <el-table-column prop="name" label="任务" min-width="160" />
          <el-table-column label="分值" width="80">
            <template #default="{ row }">{{ row.score }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.solved" type="success" size="small">已攻破</el-tag>
              <el-tag v-else-if="row.reachable" type="info" size="small">可访问</el-tag>
              <el-tag v-else type="danger" size="small" effect="plain">锁定</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="访问地址" min-width="180">
            <template #default="{ row }">
              <span v-if="row.hostPort">{{ row.host }}:{{ row.hostPort }}</span>
              <span v-else-if="row.innerIp" class="muted">层内 {{ row.innerIp }}（仅本层可达）</span>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="提交 flag" width="260">
            <template #default="{ row }">
              <div class="submit-cell">
                <el-input
                  v-model="flags[row.nodeId]"
                  size="small"
                  placeholder="flag{...}"
                  :disabled="!row.reachable || row.solved"
                  @keyup.enter="submit(row)"
                />
                <el-button
                  size="small"
                  type="primary"
                  :disabled="!row.reachable || row.solved"
                  :loading="submitting[row.nodeId]"
                  @click="submit(row)"
                >提交</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cfsApi } from '@/api'

const route = useRoute()
const contestId = route.params.contestId || route.query.contestId

const loading = ref(false)
const topology = ref(null)
/** 拓扑加载失败的真实原因（未开赛 / 未报名 / 未配置拓扑），用于空态直接展示，避免笼统文案误导选手 */
const loadError = ref('')
const flags = reactive({})
const submitting = reactive({})

async function load() {
  if (!contestId) return
  loading.value = true
  try {
    topology.value = await cfsApi.topology(contestId)
    loadError.value = ''
  } catch (e) {
    topology.value = null
    // 空态已展示具体原因，这里不再弹 ElMessage，避免同一条错误出现两次
    loadError.value = e?.response?.data?.message || '加载 CFS 拓扑失败'
  } finally {
    loading.value = false
  }
}

async function submit(node) {
  const flag = (flags[node.nodeId] || '').trim()
  if (!flag) return ElMessage.warning('请输入 flag')
  submitting[node.nodeId] = true
  try {
    const res = await cfsApi.submit({ contestId, nodeId: node.nodeId, flag })
    if (res?.correct) {
      ElMessage.success(res.message || 'flag 正确')
      flags[node.nodeId] = ''
      await load()
    } else {
      ElMessage.error(res?.message || 'flag 错误')
    }
  } catch (e) {
    // 前置未完成/层未解锁会走这里，错误信息由后端给出
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    submitting[node.nodeId] = false
  }
}

onMounted(load)
</script>

<style scoped>
.cfs-arena { padding: 16px; }
.head-row { display: flex; justify-content: space-between; align-items: center; }
.title { margin: 0 0 6px; }
.sub { color: #666; font-size: 13px; }
.score { color: #e6a23c; font-size: 16px; }
.layers { margin-top: 16px; display: flex; flex-direction: column; gap: 16px; }
.layer { border: 1px solid #ebeef5; border-radius: 8px; padding: 12px 14px; background: #fff; }
.layer.locked { background: #fafafa; border-style: dashed; }
.layer.solved { border-color: #67c23a; }
.layer-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.layer-title { display: flex; align-items: center; gap: 8px; }
.layer-name { color: #606266; }
.layer-meta { display: flex; gap: 12px; color: #909399; font-size: 12px; align-items: center; }
.badge { padding: 2px 8px; border-radius: 10px; font-size: 12px; color: #fff; }
.badge.on { background: #67c23a; }
.badge.off { background: #c0c4cc; }
.locked-tip { margin-bottom: 8px; }
.muted { color: #b0b3b8; }
.submit-cell { display: flex; gap: 6px; }
</style>