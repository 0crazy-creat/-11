<template>
  <el-dialog :model-value="modelValue" title="报名参赛" width="560px"
             @update:model-value="$emit('update:modelValue', $event)" @open="loadOptions">
    <div v-loading="loading">
      <div class="mode-row">
        <el-tag :type="mode === 'TEAM' ? 'success' : 'primary'" effect="dark">
          {{ mode === 'TEAM' ? '团队赛' : '个人赛' }}
        </el-tag>
        <span class="mode-tip">
          {{ mode === 'TEAM' ? '战队报名仅队长可提交，队员随队自动参赛' : '以个人身份参赛' }}
        </span>
      </div>

      <el-alert v-if="enrolled" type="success" :closable="false" show-icon
                :title="`已报名：${enrollName}`" description="进入赛场即可开始作答，如需更换身份请先取消报名。" />

      <template v-else-if="mode === 'TEAM'">
        <el-empty v-if="!captainTeams.length" :image-size="90"
                  description="你还不是任何战队的队长，请先在「战队管理」创建战队">
          <el-button type="primary" @click="goTeams">前往战队管理</el-button>
        </el-empty>
        <el-radio-group v-else v-model="pickedTeam" class="team-picker">
          <el-radio v-for="t in captainTeams" :key="t.id" :value="t.id" border class="team-radio">
            <span class="radio-name">{{ t.name }}</span>
            <span class="radio-sub">{{ t.members || 0 }}/{{ t.maxMembers || 8 }} 人</span>
          </el-radio>
        </el-radio-group>
      </template>

      <el-alert v-else type="info" :closable="false" show-icon
                title="将以个人身份报名，提交后由系统生成参赛记录。" />
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button v-if="!enrolled && mode === 'TEAM'" type="primary" :loading="submitting"
                 :disabled="!pickedTeam" @click="submit">确认报名</el-button>
      <el-button v-else-if="!enrolled" type="primary" :loading="submitting" @click="submit">确认报名</el-button>
      <el-button v-else type="danger" plain :loading="submitting" @click="cancelEnroll">取消报名</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { teamApi } from '@/api'

const props = defineProps({ modelValue: Boolean, contestId: [String, Number] })
const emit = defineEmits(['update:modelValue', 'success'])

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const mode = ref('TEAM')
const enrolled = ref(false)
const enrollName = ref('')
const captainTeams = ref([])
const pickedTeam = ref(null)

async function loadOptions() {
  loading.value = true
  pickedTeam.value = null
  try {
    const data = await teamApi.enrollOptions(props.contestId)
    mode.value = data?.mode || 'TEAM'
    enrolled.value = !!data?.enrolled
    enrollName.value = data?.enrollName || ''
    captainTeams.value = data?.teams || []
    pickedTeam.value = captainTeams.value[0]?.id || null
  } catch (e) {
    ElMessage.error(e?.message || '报名信息加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  submitting.value = true
  try {
    await teamApi.enroll(props.contestId, mode.value === 'TEAM'
      ? { mode: 'TEAM', teamId: pickedTeam.value }
      : { mode: 'INDIVIDUAL' })
    ElMessage.success('报名成功')
    emit('update:modelValue', false)
    emit('success')
  } catch (e) {
    ElMessage.error(e?.message || '报名失败')
  } finally {
    submitting.value = false
  }
}

async function cancelEnroll() {
  submitting.value = true
  try {
    await teamApi.cancelEnroll(props.contestId)
    ElMessage.success('已取消报名')
    await loadOptions()
    emit('success')
  } catch (e) {
    ElMessage.error(e?.message || '取消失败')
  } finally {
    submitting.value = false
  }
}

function goTeams() {
  emit('update:modelValue', false)
  router.push('/portal/my-team')
}
</script>

<style scoped>
.mode-row { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.mode-tip { font-size: 12px; color: var(--el-text-color-secondary); }
.team-picker { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.team-radio { width: 100%; margin-right: 0; }
.radio-name { font-weight: 600; }
.radio-sub { margin-left: 8px; font-size: 12px; color: var(--el-text-color-secondary); }
</style>
