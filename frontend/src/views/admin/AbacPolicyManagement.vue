<template>
  <div class="abac-policy-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>ABAC 策略管理</span>
          <el-button type="primary" @click="showCreateDialog">新增策略</el-button>
        </div>
      </template>

      <!-- 策略列表 -->
      <el-table :data="policies" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="policyName" label="策略名称" min-width="150" />
        <el-table-column prop="resourceType" label="资源类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.resourceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="effect" label="效果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.effect === 'ALLOW' ? 'success' : 'danger'">
              {{ row.effect }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="togglePolicy(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeletePolicy(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 策略详情对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑策略' : '新增策略'"
        width="600px"
      >
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="策略名称" prop="policyName">
            <el-input v-model="form.policyName" placeholder="请输入策略名称" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入策略描述"
            />
          </el-form-item>
          <el-form-item label="资源类型" prop="resourceType">
            <el-select v-model="form.resourceType" placeholder="请选择资源类型">
              <el-option label="赛事" value="contest" />
              <el-option label="题目" value="question" />
              <el-option label="战队" value="team" />
              <el-option label="靶机" value="target" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作" prop="action">
            <el-select v-model="form.action" placeholder="请选择操作">
              <el-option label="查看" value="view" />
              <el-option label="编辑" value="edit" />
              <el-option label="删除" value="delete" />
              <el-option label="提交" value="submit" />
            </el-select>
          </el-form-item>
          <el-form-item label="条件表达式" prop="conditionExpr">
            <el-input
              v-model="form.conditionExpr"
              type="textarea"
              :rows="4"
              placeholder="SpEL 表达式，如：user.orgCode == resource.orgCode"
            />
            <div class="form-tip">
              支持变量：user.id, user.username, user.roles, user.orgCode, resource.*, environment.*
            </div>
          </el-form-item>
          <el-form-item label="效果" prop="effect">
            <el-radio-group v-model="form.effect">
              <el-radio label="ALLOW">允许</el-radio>
              <el-radio label="DENY">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="优先级" prop="priority">
            <el-input-number v-model="form.priority" :min="0" :max="1000" />
            <div class="form-tip">数字越大优先级越高</div>
          </el-form-item>
          <el-form-item label="启用状态" prop="enabled">
            <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>

      <!-- 策略测试对话框 -->
      <el-dialog v-model="testDialogVisible" title="策略测试" width="700px">
        <el-form :model="testForm" label-width="120px">
          <el-form-item label="用户ID">
            <el-input-number v-model="testForm.userId" :min="1" />
          </el-form-item>
          <el-form-item label="资源类型">
            <el-select v-model="testForm.resourceType">
              <el-option label="赛事" value="contest" />
              <el-option label="题目" value="question" />
              <el-option label="战队" value="team" />
              <el-option label="靶机" value="target" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源ID">
            <el-input v-model="testForm.resourceId" />
          </el-form-item>
          <el-form-item label="操作">
            <el-select v-model="testForm.action">
              <el-option label="查看" value="view" />
              <el-option label="编辑" value="edit" />
              <el-option label="删除" value="delete" />
              <el-option label="提交" value="submit" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源属性">
            <el-input
              v-model="testForm.resourceJson"
              type="textarea"
              :rows="5"
              placeholder='{"orgCode": "ORG001", "status": "RUNNING"}'
            />
          </el-form-item>
        </el-form>
        <div v-if="testResult" class="test-result">
          <h4>测试结果：</h4>
          <el-alert
            :title="testResult.allowed ? '允许访问' : '拒绝访问'"
            :type="testResult.allowed ? 'success' : 'error'"
            :description="testResult.reason"
            show-icon
            :closable="false"
          />
          <div v-if="testResult.matchedPolicy" class="matched-policy">
            <strong>匹配策略：</strong> {{ testResult.matchedPolicy.policyName }}
          </div>
        </div>
        <template #footer>
          <el-button @click="testDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="runTest" :loading="testing">测试</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getPolicies,
  createPolicy,
  updatePolicy,
  deletePolicy,
  testPolicy
} from '@/api/abac';

const loading = ref(false);
const submitting = ref(false);
const testing = ref(false);
const policies = ref([]);
const dialogVisible = ref(false);
const testDialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);

const form = reactive({
  id: null,
  policyName: '',
  description: '',
  resourceType: '',
  action: '',
  conditionExpr: '',
  effect: 'ALLOW',
  priority: 0,
  enabled: 1
});

const testForm = reactive({
  userId: 1,
  resourceType: 'contest',
  resourceId: '',
  action: 'view',
  resourceJson: '{}'
});

const testResult = ref(null);

const rules = {
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
  action: [{ required: true, message: '请选择操作', trigger: 'change' }],
  conditionExpr: [{ required: true, message: '请输入条件表达式', trigger: 'blur' }],
  effect: [{ required: true, message: '请选择效果', trigger: 'change' }]
};

onMounted(() => {
  loadPolicies();
});

async function loadPolicies() {
  loading.value = true;
  try {
    const res = await getPolicies();
    policies.value = res.data;
  } catch (error) {
    ElMessage.error('加载策略列表失败');
  } finally {
    loading.value = false;
  }
}

function showCreateDialog() {
  isEdit.value = false;
  Object.assign(form, {
    id: null,
    policyName: '',
    description: '',
    resourceType: '',
    action: '',
    conditionExpr: '',
    effect: 'ALLOW',
    priority: 0,
    enabled: 1
  });
  dialogVisible.value = true;
}

function showEditDialog(row) {
  isEdit.value = true;
  Object.assign(form, row);
  dialogVisible.value = true;
}

async function submitForm() {
  if (!formRef.value) return;
  await formRef.value.validate();

  submitting.value = true;
  try {
    if (isEdit.value) {
      await updatePolicy(form.id, form);
      ElMessage.success('更新成功');
    } else {
      await createPolicy(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadPolicies();
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

async function handleDeletePolicy(id) {
  try {
    await ElMessageBox.confirm('确定要删除该策略吗？', '提示', {
      type: 'warning'
    });
    await deletePolicy(id);
    ElMessage.success('删除成功');
    loadPolicies();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
}

async function togglePolicy(row) {
  try {
    await updatePolicy(row.id, { enabled: row.enabled });
    ElMessage.success('状态更新成功');
  } catch (error) {
    row.enabled = row.enabled === 1 ? 0 : 1;
    ElMessage.error('状态更新失败');
  }
}

function showTestDialog() {
  testResult.value = null;
  testDialogVisible.value = true;
}

async function runTest() {
  testing.value = true;
  try {
    let resource = {};
    try {
      resource = JSON.parse(testForm.resourceJson);
    } catch {
      ElMessage.error('资源属性 JSON 格式错误');
      return;
    }

    const res = await testPolicy({
      userId: testForm.userId,
      resourceType: testForm.resourceType,
      resourceId: testForm.resourceId,
      action: testForm.action,
      resource
    });
    testResult.value = res.data;
  } catch (error) {
    ElMessage.error('测试失败');
  } finally {
    testing.value = false;
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.test-result {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.matched-policy {
  margin-top: 12px;
  padding: 8px;
  background: #fff;
  border-radius: 4px;
}
</style>
