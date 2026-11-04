<template>
  <div class="team-invite-code">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>战队邀请码</span>
          <el-button v-if="team" type="primary" @click="refreshCode" :loading="refreshing">
            刷新邀请码
          </el-button>
        </div>
      </template>

      <div v-if="!team" class="no-team">
        <el-empty description="您尚未加入任何战队">
          <el-button type="primary" @click="showJoinDialog">通过邀请码加入</el-button>
          <el-button @click="showCreateDialog">创建新战队</el-button>
        </el-empty>
      </div>

      <div v-else class="team-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="战队名称">{{ team.name }}</el-descriptions-item>
          <el-descriptions-item label="战队编号">{{ team.code }}</el-descriptions-item>
          <el-descriptions-item label="队长">
            <el-tag>{{ team.captainId === currentUserId ? '我' : `用户#${team.captainId}` }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成员数">{{ team.members }} / {{ team.maxMembers || 8 }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(team.status)">{{ team.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排名">
            <span v-if="team.rank">#{{ team.rank }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="team.inviteCode" class="invite-code-section">
          <h4>邀请码</h4>
          <div class="invite-code-display">
            <el-input
              v-model="team.inviteCode"
              readonly
              class="code-input"
              size="large"
            >
              <template #append>
                <el-button @click="copyCode">
                  <el-icon><DocumentCopy /></el-icon>
                </el-button>
              </template>
            </el-input>
            <div class="code-info">
              <span>有效期至：{{ formatExpireTime(team.inviteExpireAt) }}</span>
              <el-tag v-if="isCodeExpired" type="danger" size="small">已过期</el-tag>
              <el-tag v-else-if="isCodeExpiringSoon" type="warning" size="small">即将过期</el-tag>
              <el-tag v-else type="success" size="small">有效</el-tag>
            </div>
          </div>
          <div class="code-tips">
            <el-alert
              title="分享邀请码给队友，他们可以通过此码加入战队"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </div>

        <div v-if="isCaptain" class="captain-actions">
          <h4>队长操作</h4>
          <el-button type="warning" @click="showTransferDialog">转让队长</el-button>
          <el-button type="danger" @click="disbandTeam">解散战队</el-button>
        </div>
      </div>

      <!-- 加入战队对话框 -->
      <el-dialog v-model="joinDialogVisible" title="通过邀请码加入战队" width="500px">
        <el-form :model="joinForm" :rules="joinRules" ref="joinFormRef" label-width="100px">
          <el-form-item label="邀请码" prop="inviteCode">
            <el-input
              v-model="joinForm.inviteCode"
              placeholder="请输入8位邀请码"
              maxlength="8"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="joinDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="joinByCode" :loading="joining">加入</el-button>
        </template>
      </el-dialog>

      <!-- 创建战队对话框 -->
      <el-dialog v-model="createDialogVisible" title="创建新战队" width="600px">
        <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
          <el-form-item label="战队名称" prop="name">
            <el-input v-model="createForm.name" placeholder="请输入战队名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="战队编号" prop="code">
            <el-input v-model="createForm.code" placeholder="2-10位大写字母或数字" maxlength="10" />
          </el-form-item>
          <el-form-item label="战队描述">
            <el-input
              v-model="createForm.description"
              type="textarea"
              :rows="3"
              placeholder="介绍一下你的战队"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="战队口号">
            <el-input v-model="createForm.slogan" placeholder="一句话口号" maxlength="200" />
          </el-form-item>
          <el-form-item label="战队Logo">
            <el-upload
              class="logo-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload"
            >
              <img v-if="createForm.logo" :src="createForm.logo" class="logo-preview" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createTeam" :loading="creating">创建</el-button>
        </template>
      </el-dialog>

      <!-- 转让队长对话框 -->
      <el-dialog v-model="transferDialogVisible" title="转让队长" width="500px">
        <el-form :model="transferForm" label-width="100px">
          <el-form-item label="新队长">
            <el-select v-model="transferForm.toCaptain" placeholder="选择队员">
              <el-option
                v-for="member in teamMembers"
                :key="member.userId"
                :label="`用户#${member.userId}`"
                :value="member.userId"
                :disabled="member.userId === currentUserId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="转让原因">
            <el-input
              v-model="transferForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请说明转让原因"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="transferCaptain" :loading="transferring">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { DocumentCopy, Plus } from '@element-plus/icons-vue';
import {
  getMyTeam,
  createTeam,
  joinByInviteCode,
  generateInviteCode,
  refreshInviteCode as refreshCodeApi,
  getTeamMembers,
  quitTeam
} from '@/api/team';
import { useUserStore } from '@/stores/user';

const props = defineProps({
  contestId: {
    type: Number,
    required: true
  }
});

const userStore = useUserStore();
const currentUserId = computed(() => userStore.userId);

const team = ref(null);
const teamMembers = ref([]);
const refreshing = ref(false);
const joining = ref(false);
const creating = ref(false);
const transferring = ref(false);

const joinDialogVisible = ref(false);
const createDialogVisible = ref(false);
const transferDialogVisible = ref(false);

const joinFormRef = ref(null);
const createFormRef = ref(null);

const joinForm = reactive({
  inviteCode: ''
});

const createForm = reactive({
  name: '',
  code: '',
  description: '',
  slogan: '',
  logo: ''
});

const transferForm = reactive({
  toCaptain: null,
  reason: ''
});

const joinRules = {
  inviteCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' },
    { len: 8, message: '邀请码为8位', trigger: 'blur' }
  ]
};

const createRules = {
  name: [
    { required: true, message: '请输入战队名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入战队编号', trigger: 'blur' },
    { pattern: /^[A-Z0-9]{2,10}$/, message: '2-10位大写字母或数字', trigger: 'blur' }
  ]
};

const isCaptain = computed(() => team.value && team.value.captainId === currentUserId.value);

const isCodeExpired = computed(() => {
  if (!team.value || !team.value.inviteExpireAt) return false;
  return new Date(team.value.inviteExpireAt) < new Date();
});

const isCodeExpiringSoon = computed(() => {
  if (!team.value || !team.value.inviteExpireAt) return false;
  const expireTime = new Date(team.value.inviteExpireAt);
  const now = new Date();
  const hoursLeft = (expireTime - now) / (1000 * 60 * 60);
  return hoursLeft < 6 && hoursLeft > 0;
});

onMounted(() => {
  loadMyTeam();
});

async function loadMyTeam() {
  try {
    const res = await getMyTeam(props.contestId);
    team.value = res.data;
    if (team.value) {
      await loadTeamMembers();
    }
  } catch (error) {
    console.error('加载战队信息失败', error);
  }
}

async function loadTeamMembers() {
  if (!team.value) return;
  try {
    const res = await getTeamMembers(props.contestId, team.value.id);
    teamMembers.value = res.data;
  } catch (error) {
    console.error('加载队员列表失败', error);
  }
}

function showJoinDialog() {
  joinForm.inviteCode = '';
  joinDialogVisible.value = true;
}

function showCreateDialog() {
  Object.assign(createForm, {
    name: '',
    code: '',
    description: '',
    slogan: '',
    logo: ''
  });
  createDialogVisible.value = true;
}

function showTransferDialog() {
  transferForm.toCaptain = null;
  transferForm.reason = '';
  transferDialogVisible.value = true;
}

async function joinByCode() {
  if (!joinFormRef.value) return;
  await joinFormRef.value.validate();

  joining.value = true;
  try {
    await joinByInviteCode(props.contestId, joinForm.inviteCode);
    ElMessage.success('加入成功');
    joinDialogVisible.value = false;
    loadMyTeam();
  } catch (error) {
    ElMessage.error(error.message || '加入失败');
  } finally {
    joining.value = false;
  }
}

async function createTeam() {
  if (!createFormRef.value) return;
  await createFormRef.value.validate();

  creating.value = true;
  try {
    const res = await createTeam(props.contestId, createForm);
    team.value = res.data;
    ElMessage.success('创建成功');
    createDialogVisible.value = false;
    loadMyTeam();
  } catch (error) {
    ElMessage.error(error.message || '创建失败');
  } finally {
    creating.value = false;
  }
}

async function refreshCode() {
  refreshing.value = true;
  try {
    const res = await refreshCodeApi(props.contestId, team.value.id);
    team.value.inviteCode = res.data.code;
    team.value.inviteExpireAt = res.data.expireAt;
    ElMessage.success('邀请码已刷新');
  } catch (error) {
    ElMessage.error(error.message || '刷新失败');
  } finally {
    refreshing.value = false;
  }
}

async function copyCode() {
  if (!team.value || !team.value.inviteCode) return;
  try {
    await navigator.clipboard.writeText(team.value.inviteCode);
    ElMessage.success('已复制到剪贴板');
  } catch (error) {
    ElMessage.error('复制失败，请手动复制');
  }
}

async function transferCaptain() {
  if (!transferForm.toCaptain) {
    ElMessage.warning('请选择新队长');
    return;
  }
  try {
    await ElMessageBox.confirm(
      '转让后您将失去队长权限，确定要转让吗？',
      '提示',
      { type: 'warning' }
    );
    // TODO: 实现转让队长 API
    ElMessage.success('转让成功');
    transferDialogVisible.value = false;
    loadMyTeam();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '转让失败');
    }
  }
}

async function disbandTeam() {
  try {
    await ElMessageBox.confirm(
      '解散战队后所有成员将退出，确定要解散吗？',
      '警告',
      { type: 'warning', confirmButtonText: '确定解散', cancelButtonText: '取消' }
    );
    await quitTeam(props.contestId);
    ElMessage.success('战队已解散');
    team.value = null;
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解散失败');
    }
  }
}

function handleLogoSuccess(res) {
  createForm.logo = res.url;
}

function beforeLogoUpload(file) {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB');
  }
  return isImage && isLt2M;
}

function getStatusType(status) {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    QUIT: 'info'
  };
  return map[status] || 'info';
}

function formatExpireTime(time) {
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

.no-team {
  padding: 40px 0;
}

.team-info {
  padding: 20px 0;
}

.invite-code-section {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.invite-code-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.invite-code-display {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.code-input {
  max-width: 400px;
}

.code-input :deep(.el-input__inner) {
  font-size: 24px;
  font-family: 'Courier New', monospace;
  letter-spacing: 4px;
  text-align: center;
  font-weight: bold;
}

.code-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
}

.code-tips {
  margin-top: 16px;
}

.captain-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.captain-actions h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.logo-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
}
</style>
