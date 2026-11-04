<template>
  <div class="traffic-capture-panel">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流量取证</span>
          <div class="header-actions">
            <el-button type="primary" @click="showStartDialog" :disabled="!canStartCapture">
              启动捕获
            </el-button>
            <el-button @click="loadCaptures">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="轮次">
          <el-select v-model="filter.roundNo" placeholder="选择轮次" clearable>
            <el-option
              v-for="round in rounds"
              :key="round.roundNo"
              :label="`第 ${round.roundNo} 轮`"
              :value="round.roundNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="战队">
          <el-select v-model="filter.teamId" placeholder="选择战队" clearable>
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="选择状态" clearable>
            <el-option label="捕获中" value="CAPTURING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCaptures">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 捕获列表 -->
      <el-table :data="captures" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roundNo" label="轮次" width="80" />
        <el-table-column prop="teamId" label="战队" width="120">
          <template #default="{ row }">
            {{ getTeamName(row.teamId) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="靶机" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="packetCount" label="数据包" width="100" />
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="{ row }">
            {{ row.duration }}s
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'CAPTURING'"
              size="small"
              type="warning"
              @click="stopCapture(row.id)"
            >
              停止
            </el-button>
            <el-button
              v-if="row.status === 'COMPLETED'"
              size="small"
              type="primary"
              @click="viewAnalysis(row.id)"
            >
              分析结果
            </el-button>
            <el-button
              v-if="row.status === 'COMPLETED'"
              size="small"
              @click="downloadPcap(row.id)"
            >
              下载
            </el-button>
            <el-button
              size="small"
              type="info"
              @click="viewDetail(row.id)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadCaptures"
        @current-change="loadCaptures"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 启动捕获对话框 -->
    <el-dialog v-model="startDialogVisible" title="启动流量捕获" width="600px">
      <el-form :model="startForm" :rules="startRules" ref="startFormRef" label-width="100px">
        <el-form-item label="轮次" prop="roundNo">
          <el-select v-model="startForm.roundNo" placeholder="选择轮次">
            <el-option
              v-for="round in rounds"
              :key="round.roundNo"
              :label="`第 ${round.roundNo} 轮`"
              :value="round.roundNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="战队" prop="teamId">
          <el-select v-model="startForm.teamId" placeholder="选择战队">
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="靶机" prop="targetId">
          <el-select v-model="startForm.targetId" placeholder="选择靶机">
            <el-option
              v-for="target in targets"
              :key="target.id"
              :label="`${target.targetName} (${target.targetIp})`"
              :value="target.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="捕获时长">
          <el-input-number v-model="startForm.duration" :min="60" :max="3600" :step="60" />
          <span style="margin-left: 8px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="过滤规则">
          <el-input
            v-model="startForm.filter"
            placeholder="tcpdump 过滤规则，如：tcp or udp"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startCapture" :loading="starting">启动</el-button>
      </template>
    </el-dialog>

    <!-- 捕获详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="捕获详情" width="700px">
      <div v-if="selectedCapture" class="capture-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="捕获ID">{{ selectedCapture.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedCapture.status)">
              {{ getStatusLabel(selectedCapture.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="轮次">{{ selectedCapture.roundNo }}</el-descriptions-item>
          <el-descriptions-item label="战队">{{ getTeamName(selectedCapture.teamId) }}</el-descriptions-item>
          <el-descriptions-item label="靶机ID">{{ selectedCapture.targetId }}</el-descriptions-item>
          <el-descriptions-item label="存储类型">{{ selectedCapture.storageType }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatSize(selectedCapture.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="数据包数">{{ selectedCapture.packetCount }}</el-descriptions-item>
          <el-descriptions-item label="捕获时长">{{ selectedCapture.duration }}s</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(selectedCapture.startedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatTime(selectedCapture.completedAt) }}</el-descriptions-item>
          <el-descriptions-item label="过期时间">{{ formatTime(selectedCapture.expiresAt) }}</el-descriptions-item>
          <el-descriptions-item label="文件路径" :span="2">
            <code>{{ selectedCapture.captureFile }}</code>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedCapture.errorMessage" label="错误信息" :span="2">
            <el-alert type="error" :closable="false">
              {{ selectedCapture.errorMessage }}
            </el-alert>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="selectedCapture.protocolStats" class="protocol-stats">
          <h4>协议统计</h4>
          <pre>{{ JSON.stringify(selectedCapture.protocolStats, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>

    <!-- 分析结果对话框 -->
    <el-dialog v-model="analysisDialogVisible" title="流量分析结果" width="900px">
      <div v-loading="analysisLoading">
        <el-table :data="analysisResults" border>
          <el-table-column prop="analysisType" label="分析类型" width="150">
            <template #default="{ row }">
              <el-tag>{{ getAnalysisTypeLabel(row.analysisType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="severity" label="严重程度" width="120">
            <template #default="{ row }">
              <el-tag :type="getSeverityType(row.severity)">
                {{ row.severity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="confidence" label="置信度" width="100">
            <template #default="{ row }">
              {{ (row.confidence * 100).toFixed(1) }}%
            </template>
          </el-table-column>
          <el-table-column prop="analyzer" label="分析器" width="150" />
          <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
          <el-table-column label="详情" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="showFindings(row)">查看</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="analyzedAt" label="分析时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.analyzedAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 发现详情对话框 -->
    <el-dialog v-model="findingsDialogVisible" title="发现详情" width="800px">
      <div v-if="selectedFindings" class="findings-detail">
        <pre>{{ JSON.stringify(selectedFindings, null, 2) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  listCaptures,
  getCapture,
  startCapture as startCaptureApi,
  stopCapture as stopCaptureApi,
  getAnalysis,
  triggerAnalysis,
  downloadPcap as downloadPcapApi
} from '@/api/traffic';
import { getRounds } from '@/api/awd';
import { getTeams } from '@/api/team';
import { getTargets } from '@/api/target';

const props = defineProps({
  contestId: {
    type: Number,
    required: true
  }
});

const loading = ref(false);
const starting = ref(false);
const analysisLoading = ref(false);

const captures = ref([]);
const rounds = ref([]);
const teams = ref([]);
const targets = ref([]);
const analysisResults = ref([]);
const selectedCapture = ref(null);
const selectedFindings = ref(null);

const startDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const analysisDialogVisible = ref(false);
const findingsDialogVisible = ref(false);

const startFormRef = ref(null);

const filter = reactive({
  roundNo: null,
  teamId: null,
  status: null
});

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
});

const startForm = reactive({
  roundNo: null,
  teamId: null,
  targetId: null,
  duration: 300,
  filter: 'tcp or udp or icmp'
});

const startRules = {
  roundNo: [{ required: true, message: '请选择轮次', trigger: 'change' }],
  teamId: [{ required: true, message: '请选择战队', trigger: 'change' }],
  targetId: [{ required: true, message: '请选择靶机', trigger: 'change' }]
};

const canStartCapture = computed(() => {
  return rounds.value.length > 0 && teams.value.length > 0 && targets.value.length > 0;
});

onMounted(() => {
  loadInitialData();
  loadCaptures();
});

async function loadInitialData() {
  try {
    const [roundsRes, teamsRes, targetsRes] = await Promise.all([
      getRounds(props.contestId),
      getTeams(props.contestId),
      getTargets(props.contestId)
    ]);
    rounds.value = roundsRes.data;
    teams.value = teamsRes.data;
    targets.value = targetsRes.data;
  } catch (error) {
    ElMessage.error('加载基础数据失败');
  }
}

async function loadCaptures() {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filter
    };
    const res = await listCaptures(props.contestId, params);
    captures.value = res.data.records;
    pagination.total = res.data.total;
  } catch (error) {
    ElMessage.error('加载捕获列表失败');
  } finally {
    loading.value = false;
  }
}

function showStartDialog() {
  Object.assign(startForm, {
    roundNo: rounds.value[0]?.roundNo || null,
    teamId: null,
    targetId: null,
    duration: 300,
    filter: 'tcp or udp or icmp'
  });
  startDialogVisible.value = true;
}

async function startCapture() {
  if (!startFormRef.value) return;
  await startFormRef.value.validate();

  starting.value = true;
  try {
    await startCaptureApi(props.contestId, startForm);
    ElMessage.success('流量捕获已启动');
    startDialogVisible.value = false;
    loadCaptures();
  } catch (error) {
    ElMessage.error(error.message || '启动失败');
  } finally {
    starting.value = false;
  }
}

async function stopCapture(captureId) {
  try {
    await ElMessageBox.confirm('确定要停止该捕获吗？', '提示', { type: 'warning' });
    await stopCaptureApi(props.contestId, captureId);
    ElMessage.success('已停止');
    loadCaptures();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停止失败');
    }
  }
}

async function viewDetail(captureId) {
  try {
    const res = await getCapture(props.contestId, captureId);
    selectedCapture.value = res.data;
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('加载详情失败');
  }
}

async function viewAnalysis(captureId) {
  analysisLoading.value = true;
  analysisDialogVisible.value = true;
  try {
    const res = await getAnalysis(props.contestId, captureId);
    analysisResults.value = res.data;
  } catch (error) {
    ElMessage.error('加载分析结果失败');
  } finally {
    analysisLoading.value = false;
  }
}

async function downloadPcap(captureId) {
  try {
    const url = await downloadPcapApi(props.contestId, captureId);
    window.open(url, '_blank');
  } catch (error) {
    ElMessage.error('下载失败');
  }
}

function showFindings(row) {
  try {
    selectedFindings.value = JSON.parse(row.findings);
    findingsDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('解析失败');
  }
}

function getTeamName(teamId) {
  const team = teams.value.find(t => t.id === teamId);
  return team ? team.name : `战队#${teamId}`;
}

function getStatusType(status) {
  const map = {
    CAPTURING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    EXPIRED: 'info'
  };
  return map[status] || 'info';
}

function getStatusLabel(status) {
  const map = {
    CAPTURING: '捕获中',
    COMPLETED: '已完成',
    FAILED: '失败',
    EXPIRED: '已过期'
  };
  return map[status] || status;
}

function getAnalysisTypeLabel(type) {
  const map = {
    ATTACK_DETECT: '攻击检测',
    VULN_SCAN: '漏洞扫描',
    ANOMALY_DETECT: '异常检测'
  };
  return map[type] || type;
}

function getSeverityType(severity) {
  const map = {
    INFO: 'info',
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  };
  return map[severity] || 'info';
}

function formatSize(bytes) {
  if (!bytes || bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + units[i];
}

function formatTime(time) {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.filter-form {
  margin-bottom: 16px;
}

.capture-detail {
  padding: 20px 0;
}

.protocol-stats {
  margin-top: 24px;
}

.protocol-stats h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
}

.protocol-stats pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 13px;
}

.findings-detail pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 13px;
  max-height: 500px;
}
</style>
