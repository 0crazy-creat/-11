<template>
  <div class="qrc-wrap">
    <div class="qrc-card">
      <div class="qrc-logo">CSP</div>
      <h1>扫码登录确认</h1>
      <p class="sub">网络安全竞赛平台</p>

      <template v-if="loading">
        <el-icon class="is-loading qrc-loading"><Loading /></el-icon>
        <p class="tip">正在校验二维码…</p>
      </template>

      <template v-else-if="!valid">
        <el-icon class="qrc-fail"><CircleCloseFilled /></el-icon>
        <p class="tip err">{{ message }}</p>
        <el-button type="primary" size="large" style="width:100%;margin-top:16px" @click="goLogin">
          返回登录页
        </el-button>
      </template>

      <template v-else-if="confirmed">
        <el-icon class="qrc-ok"><CircleCheckFilled /></el-icon>
        <p class="tip ok">已确认登录</p>
        <p class="sub2">请回到电脑端，页面将自动完成登录</p>
      </template>

      <template v-else>
        <el-icon class="qrc-ok"><Iphone /></el-icon>
        <p class="tip">请在手机上确认登录</p>
        <p class="sub2">确认后电脑端将自动登录</p>
        <el-button
          type="primary"
          size="large"
          style="width:100%;margin-top:20px"
          :loading="submitting"
          @click="doConfirm">
          确认登录
        </el-button>
      </template>

      <p class="foot">演示环境 · 无需真实微信</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheckFilled, CircleCloseFilled, Iphone } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const valid = ref(false)
const confirmed = ref(false)
const message = ref('')
const submitting = ref(false)
const state = ref('')

onMounted(async () => {
  state.value = String(route.query.state || '')
  if (!state.value) {
    loading.value = false
    valid.value = false
    message.value = '缺少扫码参数，请重新扫码'
    return
  }
  try {
    const d = await request.get('/auth/qr-confirm/info', { params: { state: state.value } })
    valid.value = !!d.valid
    confirmed.value = !!d.confirmed
    message.value = d.message || '二维码已过期'
  } catch (e) {
    valid.value = false
    message.value = e?.message || '二维码校验失败'
  } finally {
    loading.value = false
  }
})

async function doConfirm() {
  submitting.value = true
  try {
    await request.post('/auth/qr-confirm/confirm', { state: state.value })
    confirmed.value = true
    ElMessage.success('已确认，电脑端即将登录')
  } catch (e) {
    ElMessage.error(e?.message || '确认失败')
  } finally {
    submitting.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.qrc-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #e8eeff 0%, #f7f9ff 60%, #ffffff 100%);
  padding: 24px;
}
.qrc-card {
  width: 340px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(41, 95, 255, 0.12);
  padding: 32px 28px 24px;
  text-align: center;
}
.qrc-logo {
  width: 52px;
  height: 52px;
  margin: 0 auto 12px;
  border-radius: 12px;
  background: #295fff;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  line-height: 52px;
  letter-spacing: 1px;
}
h1 {
  margin: 0;
  font-size: 20px;
  color: #1d2129;
}
.sub {
  margin: 6px 0 22px;
  font-size: 12px;
  color: #86909c;
  letter-spacing: 1px;
}
.sub2 {
  margin: 4px 0 0;
  font-size: 12px;
  color: #86909c;
}
.tip {
  margin: 14px 0 0;
  font-size: 14px;
  color: #4e5969;
}
.tip.ok {
  color: #00b42a;
  font-weight: 600;
}
.tip.err {
  color: #f53f3f;
}
.qrc-loading {
  font-size: 38px;
  color: #295fff;
}
.qrc-ok {
  font-size: 46px;
  color: #00b42a;
}
.qrc-fail {
  font-size: 46px;
  color: #f53f3f;
}
.foot {
  margin: 24px 0 0;
  font-size: 11px;
  color: #c9cdd4;
}
</style>