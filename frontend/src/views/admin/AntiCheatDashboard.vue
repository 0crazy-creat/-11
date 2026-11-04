<template>
  <div class="anti-cheat-dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">参赛用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowRiskCount }}</div>
              <div class="stat-label">低风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="32"><InfoFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.mediumRiskCount }}</div>
              <div class="stat-label">中风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon :size="32"><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highRiskCount }}</div>
              <div class="stat-label">高风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 高风险用户列表 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>高风险用户</span>
              <el-radio-group v-model="riskLevel" size="small" @change="loadHighRiskUsers">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="high">高风险</el-radio-button>
                <el-radio-button label="critical">极高风险</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="highRiskUsers" v-loading="loading" border>
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="auditType" label="检测类型" width="150">
              <template #default="{ row }">
                <el-tag :type="getAuditTypeTag(row.auditType)">
                  {{ getAuditTypeLabel(row.auditType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="严重程度" width="120">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severity)">
                  {{ row.severity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="riskScore" label="风险分" width="100">
              <template #default="{ row }">
                <span :style="{ color: getRiskScoreColor(row.riskScore), fontWeight: 'bold' }">
                  {{ row.riskScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="详情" min-width="200">
              <template #default="{ row }">
                <el-button size="small" @click="showDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="actionTaken" label="已采取措施" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.actionTaken" type="info" size="small">
                  {{ row.actionTaken }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 风险分布图 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>风险类型分布</span>
          </template>
          <div ref="chartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户风险详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户风险详情" width="800px">
      <div v-if="selectedUser" class="user-risk-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ selectedUser.userId }}</el-descriptions-item>
          <el-descriptions-item label="最高风险分">
            <span :style="{ color: getRiskScoreColor(selectedUser.maxRiskScore), fontWeight: 'bold' }">
              {{ selectedUser.maxRiskScore }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="使用设备数">{{ selectedUser.deviceCount }}</el-descriptions-item>
          <el-descriptions-item label="审计记录数">{{ selectedUser.totalAuditCount }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>设备指纹</h4>
          <el-table :data="selectedUser.fingerprints" border size="small">
            <el-table-column prop="deviceId" label="设备ID" min-width="200" show-overflow-tooltip />
            <el-table-column prop="trustScore" label="信任分" width="100">
              <template #default="{ row }">
                <el-tag :type="getTrustScoreType(row.trustScore)">
                  {{ row.trustScore }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="riskFlags" label="风险标记" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.riskFlags" type="danger" size="small">
                  {{ row.riskFlags }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="lastSeenAt" label="最后出现" width="180">
              <template #default="{ row }">
                {{ formatTime(row.lastSeenAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section">
          <h4>审计日志</h4>
          <el-table :data="selectedUser.auditLogs" border size="small">
            <el-table-column prop="auditType" label="类型" width="150" />
            <el-table-column prop="count" label="次数" width="100" />
          </el-table>
        </div>

        <div class="detail-actions">
          <el-button type="warning" @click="suspendUser(selectedUser.userId)">
            暂停用户
          </el-button>
          <el-button type="info" @click="clearUser(selectedUser.userId)">
            标记为误判
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { User, Warning, InfoFilled, CircleCloseFilled } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getHighRiskUsers, getUserRiskSummary } from '@/api/antiCheat';

const props = defineProps({
  contestId: {
    type: Number,
    required: true
  }
});

const loading = ref(false);
const riskLevel = ref('all');
const highRiskUsers = ref([]);
const selectedUser = ref(null);
const detailDialogVisible = ref(false);
const chartRef = ref(null);
let chartInstance = null;

const stats = reactive({
  totalUsers: 0,
  lowRiskCount: 0,
  mediumRiskCount: 0,
  highRiskCount: 0
});

onMounted(() => {
  loadHighRiskUsers();
  initChart();
  startAutoRefresh();
});

onUnmounted(() => {
  stopAutoRefresh();
  if (chartInstance) {
    chartInstance.dispose();
  }
});

async function loadHighRiskUsers() {
  loading.value = true;
  try {
    const minRiskScore = riskLevel.value === 'critical' ? 80 : 
                          riskLevel.value === 'high' ? 60 : 40;
    const res = await getHighRiskUsers(props.contestId, minRiskScore);
    highRiskUsers.value = res.data;
    
    // 更新统计
    stats.highRiskCount = highRiskUsers.value.filter(u => u.riskScore >= 80).length;
    stats.mediumRiskCount = highRiskUsers.value.filter(u => u.riskScore >= 60 && u.riskScore < 80).length;
    stats.lowRiskCount = highRiskUsers.value.filter(u => u.riskScore >= 40 && u.riskScore < 60).length;
    stats.totalUsers = highRiskUsers.value.length;
    
    updateChart();
  } catch (error) {
    ElMessage.error('加载高风险用户失败');
  } finally {
    loading.value = false;
  }
}

async function showDetail(row) {
  try {
    const res = await getUserRiskSummary(props.contestId, row.userId);
    selectedUser.value = {
      ...res.data,
      userId: row.userId,
      maxRiskScore: row.riskScore
    };
    
    // 转换审计日志为表格格式
    if (selectedUser.value.auditLogs) {
      const logs = [];
      for (const [type, count] of Object.entries(selectedUser.value.auditLogs)) {
        logs.push({ auditType: type, count });
      }
      selectedUser.value.auditLogs = logs;
    }
    
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('加载用户详情失败');
  }
}

async function suspendUser(userId) {
  try {
    await ElMessageBox.confirm(
      '确定要暂停该用户吗？暂停后用户将无法继续参赛。',
      '警告',
      { type: 'warning' }
    );
    // TODO: 实现暂停用户 API
    ElMessage.success('用户已暂停');
    detailDialogVisible.value = false;
    loadHighRiskUsers();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
}

async function clearUser(userId) {
  try {
    await ElMessageBox.confirm(
      '确定要将该用户标记为误判吗？这将清除该用户的作弊记录。',
      '提示',
      { type: 'info' }
    );
    // TODO: 实现标记误判 API
    ElMessage.success('已标记为误判');
    detailDialogVisible.value = false;
    loadHighRiskUsers();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
}

function initChart() {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  
  const option = {
    title: {
      text: '风险类型分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '风险类型',
        type: 'pie',
        radius: '50%',
        data: [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  chartInstance.setOption(option);
}

function updateChart() {
  if (!chartInstance) return;
  
  // 统计各类型风险数量
  const typeCount = {};
  highRiskUsers.value.forEach(user => {
    const type = user.auditType;
    typeCount[type] = (typeCount[type] || 0) + 1;
  });
  
  const data = Object.entries(typeCount).map(([name, value]) => ({ name, value }));
  
  chartInstance.setOption({
    series: [{
      data
    }]
  });
}

let refreshTimer = null;

function startAutoRefresh() {
  refreshTimer = setInterval(() => {
    loadHighRiskUsers();
  }, 30000); // 30秒刷新一次
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

function getAuditTypeTag(type) {
  const map = {
    MULTI_DEVICE: 'danger',
    RATE_LIMIT: 'warning',
    BRUTE_FORCE: 'danger',
    MACHINE_SPEED: 'warning',
    MULTI_IP: 'warning'
  };
  return map[type] || 'info';
}

function getAuditTypeLabel(type) {
  const map = {
    MULTI_DEVICE: '多账号',
    RATE_LIMIT: '限流',
    BRUTE_FORCE: '暴力猜解',
    MACHINE_SPEED: '机器速度',
    MULTI_IP: '同IP多账号'
  };
  return map[type] || type;
}

function getSeverityType(severity) {
  const map = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  };
  return map[severity] || 'info';
}

function getRiskScoreColor(score) {
  if (score >= 80) return '#f56c6c';
  if (score >= 60) return '#e6a23c';
  if (score >= 40) return '#409eff';
  return '#67c23a';
}

function getTrustScoreType(score) {
  if (score < 60) return 'danger';
  if (score < 80) return 'warning';
  return 'success';
}

function formatTime(time) {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
}
</script>

<style scoped>
.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-risk-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.detail-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}
</style>
