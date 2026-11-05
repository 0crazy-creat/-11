<template>
  <div class="wxc-wrap">
    <div class="wxc-card">
      <div class="wxc-logo">CSP</div>
      <h1>微信扫码登录</h1>
      <p class="sub">网络安全竞赛平台</p>

      <template v-if="status === 'processing'">
        <el-icon class="is-loading wxc-spin"><Loading /></el-icon>
        <p class="tip">正在验证微信授权…</p>
      </template>

      <template v-else-if="status === 'success'">
        <el-icon class="wxc-ok"><CircleCheckFilled /></el-icon>
        <p class="tip ok">登录成功</p>
        <p class="sub2">{{ redirectTip }}</p>
        <el-button type="primary" size="large" style="width:100%;margin-top:20px" @click="goHome">
          立即进入
        </el-button>
      </template>

      <template v-else>
        <el-icon class="wxc-fail"><CircleCloseFilled /></el-icon>
        <p class="tip err">{{ message }}</p>
        <el-button type="primary" size="large" style="width:100%;margin-top:20px" @click="goLogin">
          返回登录页
        </el-button>
      </template>

      <p class="foot">微信开放平台 · 网页授权登录</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const status = ref('processing')
const message = ref('')
const homePath = ref('/portal')

const redirectTip = computed(() =>
  homePath.value === '/console' ? '即将进入管理控制台…' : '即将进入选手门户…'
)

function goLogin() {
  router.replace({ name: 'login' })
}

function goHome() {
  router.replace(homePath.value)
}

function decideHome() {
  const roles = user.roles?.value || []
  const perms = user.profile?.permissions || []
  const isAdmin = roles.some(r => ['ADMIN', 'SUPER_ADMIN', 'PLATFORM_ADMIN', 'JUDGE', 'TEACHER'].includes(r)) || perms.includes('*')
  homePath.value = isAdmin ? '/console' : '/portal'
}

onMounted(async () => {
  const code = String(route.query.code || '')
  const state = String(route.query.state || '')
  if (!code || !state) {
    status.value = 'error'
    message.value = '缺少微信授权参数，请返回登录页重新扫码'
    return
  }
  try {
    // 1) code → openid → 建号/绑定，会话置为 CONFIRMED
    await request.post('/auth/wechat/callback', { state, code })
    // 2) 用 state 换登录令牌
    const vo = await request.post('/auth/wechat/exchange', { state })
    user.applyLogin(vo)
    decideHome()
    status.value = 'success'
    ElMessage.success('微信登录成功')
    setTimeout(() => { if (status.value === 'success') goHome() }, 1200)
  } catch (e) {
    status.value = 'error'
    message.value = e?.message || '微信登录失败，请返回登录页重试'
  }
})
</script>

<style scoped>
.wxc-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(160deg, #e8eeff 0%, #f7f9ff 60%, #ffffff 100%);
}
.wxc-card {
  width: 340px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(41, 95, 255, 0.12);
  padding: 32px 28px 24px;
  text-align: center;
}
.wxc-logo {
  width: 52px;
  height: 52px;
  margin: 0 auto 12px;
  border-radius: 12px;
  background: #07c160;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  line-height: 52px;
  letter-spacing: 1px;
}
h1 { margin: 0; font-size: 20px; color: #1d2129; }
.sub { margin: 6px 0 22px; font-size: 12px; color: #86909c; letter-spacing: 1px; }
.sub2 { margin: 4px 0 0; font-size: 12px; color: #86909c; }
.tip { margin: 14px 0 0; font-size: 14px; color: #4e5969; }
.tip.ok { color: #00b42a; font-weight: 600; }
.tip.err { color: #f53f3f; word-break: break-all; }
.wxc-spin { font-size: 38px; color: #07c160; }
.wxc-ok { font-size: 46px; color: #00b42a; }
.wxc-fail { font-size: 46px; color: #f53f3f; }
.foot { margin: 24px 0 0; font-size: 11px; color: #c9cdd4; }
</style>
