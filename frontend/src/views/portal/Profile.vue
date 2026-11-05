<template>
  <div class="profile-page">
    <el-card shadow="never">
      <template #header><span class="card-title">个人信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ profile?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ profile?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ profile?.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ profile?.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ profile?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in profile?.roles || []" :key="r" size="small" class="mr-4">{{ roleLabel(r) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="profile?.status === 1 ? 'success' : 'danger'" size="small">
            {{ profile?.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="双因素认证">
          <el-switch v-model="mfaEnabled" @change="toggleMfa" :loading="mfaLoading" />
          <span class="mfa-hint">{{ mfaEnabled ? '已开启' : '未开启' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ profile?.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="mt-16">
      <template #header><span class="card-title">修改密码</span></template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 480px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="pwdLoading">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt-16">
      <template #header><span class="card-title">编辑资料</span></template>
      <el-form ref="editFormRef" :model="editForm" label-width="100px" style="max-width: 480px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="updateProfile" :loading="editLoading">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const user = useUserStore()
const profile = computed(() => user.profile)

const mfaEnabled = ref(false)
const mfaLoading = ref(false)
const pwdLoading = ref(false)
const editLoading = ref(false)
const pwdFormRef = ref()
const editFormRef = ref()

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const editForm = reactive({ nickname: '', realName: '', phone: '', email: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
  else callback()
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 64, message: '密码长度需 8-64 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const roleLabel = (r) => ({
  ADMIN: '管理员', SUPER_ADMIN: '超级管理员', PLATFORM_ADMIN: '平台管理员',
  JUDGE: '裁判', TEACHER: '教师', PLAYER: '选手', OBSERVER: '观察者', VIEWER: '观摩'
}[r] || r)

async function loadProfile() {
  try {
    const data = await request.get('/auth/me')
    const u = data?.user || data || {}
    Object.assign(editForm, {
      nickname: u.nickname || '',
      realName: u.realName || '',
      phone: u.phone || '',
      email: u.email || ''
    })
    mfaEnabled.value = u.mfaEnabled === 1 || u.mfaEnabled === true
  } catch { /* ignore */ }
}

async function changePassword() {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await request.post('/auth/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    pwdLoading.value = false
  }
}

async function updateProfile() {
  editLoading.value = true
  try {
    await request.put('/auth/profile', editForm)
    await user.refresh()
    ElMessage.success('资料已更新')
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    editLoading.value = false
  }
}

async function toggleMfa(val) {
  mfaLoading.value = true
  try {
    if (val) {
      const data = await request.post('/account/mfa')
      ElMessage.success('MFA 已开启，密钥: ' + (data?.secret || data?.key || '已绑定'))
    } else {
      await request.post('/account/mfa', { enabled: false })
      ElMessage.info('MFA 已关闭')
    }
  } catch (e) {
    mfaEnabled.value = !val
    ElMessage.error(e.message || '操作失败')
  } finally {
    mfaLoading.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { max-width: 800px; }
.card-title { font-weight: 600; font-size: 16px; }
.mt-16 { margin-top: 16px; }
.mr-4 { margin-right: 4px; }
.mfa-hint { margin-left: 8px; color: #909399; font-size: 13px; }
</style>
