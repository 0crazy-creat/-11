<template>
  <div class="login-wrap">
    <div class="login-card">
      <h1>网络安全竞赛平台</h1>
      <p class="sub">Cyber Security Platform</p>

      <el-tabs v-model="tab" stretch class="login-tabs">
        <!-- ========== 账号密码 ========== -->
        <el-tab-pane label="账号登录" name="pwd">
          <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="submit">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名 / 邮箱" size="large">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="submit">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item v-if="needMfa" prop="mfaCode">
              <el-input v-model="form.mfaCode" placeholder="双因素验证码" size="large">
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ========== 手机号登录 ========== -->
        <el-tab-pane label="手机号" name="phone">
          <el-form :model="pform" :rules="prules" ref="pformRef" @submit.prevent="submitPhone">
            <el-form-item prop="phone">
              <el-input v-model="pform.phone" placeholder="手机号" size="large" maxlength="11">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-row">
                <el-input v-model="pform.smsCode" placeholder="短信验证码" size="large" maxlength="6" />
                <el-button size="large" :disabled="countdown > 0" :loading="smsSending" @click="sendSms">
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-alert v-if="devSmsCode" :title="`本地验证环境验证码：${devSmsCode}`" type="info" :closable="false" show-icon class="mb12" />
            <el-form-item>
              <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submitPhone">登录 / 自动注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ========== 扫码登录 ========== -->
        <el-tab-pane label="扫码登录" name="qr" :lazy="false">
          <div class="qr-box">
            <div class="qr-frame" :class="{ 'qr-frame-real': qrMode === 'real' }">
              <canvas v-if="qrMode !== 'real'" ref="qrCanvas" width="180" height="180" />
              <div v-else id="wx-login-container" ref="wxBox" class="wx-login-container"></div>
              <div v-if="qrState && qrState !== 'WAITING'" class="qr-mask">
                <el-icon class="ok" v-if="qrState === 'CONFIRMED'"><CircleCheckFilled /></el-icon>
                <el-icon class="spin" v-else><Loading /></el-icon>
                <span>{{ qrText }}</span>
              </div>
            </div>
            <p class="qr-tip">请使用<strong>微信</strong>扫描二维码登录</p>
            <p v-if="qrMode === 'real'" class="qr-tip sub">
              二维码由微信开放平台提供，过期后请点击刷新
            </p>
            <p v-else class="qr-tip sub">未扫码将自动刷新 · 剩余 {{ qrLeft }}s</p>
            <el-button v-if="qrMode === 'real'" text type="primary" size="small" @click="newQr">刷新二维码</el-button>
            <el-button v-if="qrState === 'CONFIRMED'" type="primary" size="large" style="width:100%" :loading="loading" @click="exchangeQr">
              进入平台
            </el-button>
            <el-button v-else-if="qrMode === 'simulated'" type="primary" plain size="large"
                       style="width:100%;margin-top:10px" :loading="simulating" @click="simulateScan">
              模拟扫码（演示环境）
            </el-button>
            <div class="third-row">
              <span class="divider">其他方式</span>
              <div class="third-btns">
                <el-button circle @click="thirdLogin('github')" title="GitHub 登录"><el-icon><Link /></el-icon></el-button>
                <el-button circle @click="thirdLogin('dingtalk')" title="钉钉登录"><el-icon><ChatDotRound /></el-icon></el-button>
                <el-button circle @click="thirdLogin('wechat')" title="微信第三方"><el-icon><ChatLineRound /></el-icon></el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="foot">
        <span>还没有账号？</span>
        <el-link type="primary" @click="registerVisible = true">立即注册</el-link>
        <el-divider direction="vertical" />
        <el-link type="primary" @click="forgotVisible = true">忘记密码</el-link>
      </div>
    </div>

    <!-- 注册 -->
    <el-dialog v-model="registerVisible" title="注册账号" width="440px">
      <el-form :model="regForm" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="regForm.username" placeholder="登录账号" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="regForm.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="regForm.email" placeholder="用于找回密码" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="regForm.phone" maxlength="11" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="regForm.password" type="password" show-password placeholder="8-64 位，含字母与数字" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="regLoading" @click="doRegister">注册</el-button>
      </template>
    </el-dialog>

    <!-- 忘记密码 -->
    <el-dialog v-model="forgotVisible" title="找回密码" width="440px">
      <el-form :model="fgForm" label-width="90px">
        <el-form-item label="邮箱"><el-input v-model="fgForm.email" /></el-form-item>
        <el-form-item label="验证码">
          <div class="sms-row">
            <el-input v-model="fgForm.code" maxlength="6" />
            <el-button :disabled="fgCd > 0" @click="sendEmailCode">{{ fgCd > 0 ? `${fgCd}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码"><el-input v-model="fgForm.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotVisible = false">取消</el-button>
        <el-button type="primary" :loading="fgLoading" @click="doReset">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import QRCode from 'qrcode'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Key, Link, ChatDotRound, ChatLineRound, CircleCheckFilled, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const user = useUserStore()

const tab = ref('pwd')
const loading = ref(false)
const needMfa = ref(false)

/* ---------- 账号密码 ---------- */
const formRef = ref()
const form = reactive({ username: '', password: '', mfaCode: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}


/** 把后端 LoginVO 写入 Pinia store（user.login 只接受账号密码，这里复用其持久化字段）。 */
function applyLoginVO(d) {
  if (!d) return
  user.token = d.accessToken || d.token || ''
  if (d.refreshToken) localStorage.setItem('csp_refresh_token', d.refreshToken)
  const u = d.user || d.profile || {}
  user.profile = {
    ...u,
    roles: d.roles || u.roles || [],
    permissions: d.permissions || u.permissions || []
  }
  if (user.token) localStorage.setItem('csp_token', user.token)
  else localStorage.removeItem('csp_token')
  if (user.profile) localStorage.setItem('csp_user', JSON.stringify(user.profile))
  else localStorage.removeItem('csp_user')
}

function redirectAfterLogin() {
  const roles = user.roles?.value || []
  const perms = user.profile?.permissions || []
  const isAdmin = roles.some(r => ['ADMIN', 'SUPER_ADMIN', 'PLATFORM_ADMIN', 'JUDGE', 'TEACHER'].includes(r)) || perms.includes('*')
  router.push(route.query.redirect || (isAdmin ? '/console' : '/portal'))
}

async function submit() {
  if (needMfa.value && !form.mfaCode) { ElMessage.warning('请输入双因素验证码'); return }
  await formRef.value?.validate(async (ok) => {
    if (!ok) return
    loading.value = true
    try {
      await user.login({ username: form.username, password: form.password, mfaCode: form.mfaCode || undefined })
      ElMessage.success('登录成功')
      redirectAfterLogin()
    } catch (e) {
      const msg = e?.message || '登录失败'
      if (msg.includes('双因素')) { needMfa.value = true; ElMessage.warning(msg) }
      else ElMessage.error(msg)
    } finally { loading.value = false }
  })
}

/* ---------- 手机号 ---------- */
const pformRef = ref()
const pform = reactive({ phone: '', smsCode: '' })
const prules = {
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  smsCode: [{ required: true, len: 6, message: '请输入 6 位短信验证码', trigger: 'blur' }]
}
const smsSending = ref(false)
const countdown = ref(0)
const devSmsCode = ref('')
let cdTimer = null

async function sendSms() {
  if (!/^1[3-9]\d{9}$/.test(pform.phone)) { ElMessage.warning('请输入正确的手机号'); return }
  smsSending.value = true
  try {
    const d = await request.post('/auth/sms-code', null, { params: { phone: pform.phone } })
    devSmsCode.value = d?.code || ''
    ElMessage.success('验证码已发送')
    countdown.value = 60
    cdTimer = setInterval(() => { if (--countdown.value <= 0) clearInterval(cdTimer) }, 1000)
  } catch (e) { ElMessage.error(e?.message || '发送失败') } finally { smsSending.value = false }
}

async function submitPhone() {
  await pformRef.value?.validate(async (ok) => {
    if (!ok) return
    loading.value = true
    try {
      const vo = await request.post('/auth/login-phone', { phone: pform.phone, smsCode: pform.smsCode })
      applyLoginVO(vo)
      ElMessage.success('登录成功')
      redirectAfterLogin()
    } catch (e) { ElMessage.error(e?.message || '登录失败') } finally { loading.value = false }
  })
}

/* ---------- 扫码登录 ---------- */
const qrCanvas = ref()
const wxBox = ref()
const qrState = ref('')
const qrText = ref('')
const qrLeft = ref(0)
const qrSession = ref('')
const qrContent = ref('')
const qrMode = ref('')
const wxInfo = ref(null)
let qrTimer = null, qrCdTimer = null, wxScriptPromise = null

const qrTextMap = { WAITING: '等待扫码', SCANNED: '已扫码，请在手机确认', CONFIRMED: '已确认，点击进入平台', EXPIRED: '二维码已过期' }

async function drawQr(content, retry = 0) {
  // el-tab-pane 懒渲染：切 Tab 后 canvas 可能尚未挂载，轮询等待最多 3s
  const c = qrCanvas.value
  if (!c) {
    if (retry < 30) {
      await new Promise(r => setTimeout(r, 100))
      return drawQr(content, retry + 1)
    }
    console.warn('二维码 canvas 未挂载')
    return
  }
  if (!content) return
  try {
    // 用真实 QR 编码库生成可扫描二维码（纠错级别 M，模块尺寸自适应 180px）
    await QRCode.toCanvas(c, content, {
      width: 180,
      margin: 1,
      errorCorrectionLevel: 'M',
      color: { dark: '#1d2129', light: '#ffffff' }
    })
  } catch (e) {
    console.error('二维码生成失败', e)
  }
}

/** 加载微信官方 WxLogin.js（真实模式）。脚本来自公网 res.wx.qq.com。 */
function loadWxLoginScript() {
  if (window.WxLogin) return Promise.resolve()
  if (wxScriptPromise) return wxScriptPromise
  wxScriptPromise = new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = 'https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js'
    s.async = true
    s.onload = () => resolve()
    s.onerror = () => {
      wxScriptPromise = null
      reject(new Error('微信登录脚本加载失败，请确认网络可访问 res.wx.qq.com'))
    }
    document.head.appendChild(s)
  })
  return wxScriptPromise
}

/**
 * 真实微信扫码：用官方 WxLogin.js 在容器内嵌 iframe。
 * self_redirect=false 时扫码授权后由顶层窗口回跳 redirect_uri（/wechat-callback）。
 */
async function renderWxLogin(d) {
  if (!d || !d.appId || !d.redirectUri || !d.state) {
    ElMessage.error('微信登录配置缺失，请联系管理员')
    return
  }
  try {
    await loadWxLoginScript()
  } catch (e) {
    ElMessage.error(e?.message || '微信登录脚本加载失败')
    return
  }
  // WxLogin 只接受 DOM id，必须等容器挂载完成
  for (let i = 0; i < 30 && !wxBox.value; i++) {
    await new Promise(r => setTimeout(r, 100))
  }
  if (!wxBox.value) { console.warn('微信扫码容器未挂载'); return }
  wxBox.value.innerHTML = ''
  // eslint-disable-next-line no-new
  new window.WxLogin({
    self_redirect: false,
    id: 'wx-login-container',
    appid: d.appId,
    scope: d.scope || 'snsapi_login',
    redirect_uri: encodeURIComponent(d.redirectUri),
    state: d.state,
    style: 'black',
    href: ''
  })
}

async function newQr() {
  try {
    const d = await request.post('/auth/wechat/qrcode')
    qrSession.value = d.state
    qrMode.value = d.mode || ''
    qrState.value = d.status || 'WAITING'
    qrText.value = qrTextMap[qrState.value] || qrState.value
    qrLeft.value = d.expireSeconds || d.expiresIn || 120
    clearInterval(qrTimer)
    clearInterval(qrCdTimer)

    if (qrMode.value === 'real') {
      // 真实微信：不画 canvas、不轮询。扫码后微信把 code/state 回跳到 /wechat-callback
      wxInfo.value = d
      qrContent.value = d.redirectUri || ''
      await nextTick()
      await renderWxLogin(d)
      return
    }

    // 演示模式：二维码内容 = 平台自己的确认页 URL，手机打开后点确认
    wxInfo.value = null
    qrContent.value = d.qrContent || d.state
    await nextTick()
    // 等 Tab 过渡结束 & canvas 真正挂载
    await new Promise(r => setTimeout(r, 120))
    await nextTick()
    drawQr(qrContent.value)
    qrCdTimer = setInterval(() => {
      if (--qrLeft.value <= 0) { clearInterval(qrCdTimer); qrState.value = 'EXPIRED'; qrText.value = '二维码已过期'; newQr() }
    }, 1000)
    qrTimer = setInterval(pollQr, 2000)
  } catch (e) { console.warn('二维码生成失败', e) }
}

async function pollQr() {
  if (!qrSession.value) return
  try {
    const d = await request.get('/auth/wechat/status', { params: { state: qrSession.value } })
    qrState.value = d.status
    qrText.value = qrTextMap[d.status] || d.status
    if (d.status === 'CONFIRMED') clearInterval(qrTimer)
  } catch { /* ignore */ }
}


/** 演示环境：无需真实微信，直接触发后端模拟扫码回调 → 确认 → 兑换令牌。 */
const simulating = ref(false)
async function simulateScan() {
  if (!qrSession.value) return
  simulating.value = true
  try {
    await request.post('/auth/wechat/callback', { state: qrSession.value, code: 'sim_front_' + Date.now() })
    const st = await request.get('/auth/wechat/status', { params: { state: qrSession.value } })
    qrState.value = st.status
    qrText.value = qrTextMap[st.status] || st.status
    if (st.status === 'CONFIRMED') {
      ElMessage.success('模拟扫码成功，请点击「进入平台」')
    }
  } catch (e) { ElMessage.error(e?.message || '模拟扫码失败') } finally { simulating.value = false }
}

async function exchangeQr() {
  loading.value = true
  try {
    const d = await request.post('/auth/wechat/exchange', { state: qrSession.value })
    applyLoginVO(d)
    ElMessage.success('登录成功')
    redirectAfterLogin()
  } catch (e) { ElMessage.error(e?.message || '登录失败') } finally { loading.value = false }
}

async function thirdLogin(provider) {
  const label = { github: 'GitHub', dingtalk: '钉钉', wechat: '微信' }[provider] || provider
  loading.value = true
  try {
    const d = await request.post('/auth/login-third', {
      provider, thirdToken: `dev-${provider}-${Date.now()}`
    })
    applyLoginVO(d)
    ElMessage.success(`${label} 登录成功`)
    redirectAfterLogin()
  } catch (e) {
    ElMessage.error(e?.message || `${label} 登录失败`)
  } finally { loading.value = false }
}

/* ---------- 注册 / 找回 ---------- */
const registerVisible = ref(false)
const regLoading = ref(false)
const regForm = reactive({ username: '', nickname: '', email: '', phone: '', password: '' })

async function doRegister() {
  regLoading.value = true
  try {
    await authApi.register(regForm)
    ElMessage.success('注册成功，请登录')
    registerVisible.value = false
  } catch (e) { ElMessage.error(e?.message || '注册失败') } finally { regLoading.value = false }
}

const forgotVisible = ref(false)
const fgLoading = ref(false)
const fgCd = ref(0)
const fgForm = reactive({ email: '', code: '', password: '' })
let fgTimer = null

async function sendEmailCode() {
  if (!fgForm.email) { ElMessage.warning('请输入邮箱'); return }
  try {
    const d = await request.post('/account/email-code', null, { params: { email: fgForm.email } })
    if (d?.code) ElMessage.info(`验证码：${d.code}`)
    else ElMessage.success('验证码已发送')
    fgCd.value = 60
    fgTimer = setInterval(() => { if (--fgCd.value <= 0) clearInterval(fgTimer) }, 1000)
  } catch (e) { ElMessage.error(e?.message || '发送失败') }
}

async function doReset() {
  fgLoading.value = true
  try {
    await request.post('/account/reset-password', { email: fgForm.email, code: fgForm.code, password: fgForm.password })
    ElMessage.success('密码已重置，请登录')
    forgotVisible.value = false
  } catch (e) { ElMessage.error(e?.message || '重置失败') } finally { fgLoading.value = false }
}

onMounted(async () => {
  // 只在扫码 Tab 激活时初始化，避免 WxLogin 在隐藏容器里渲染
  if (tab.value === 'qr') newQr()
})
onBeforeUnmount(() => { clearInterval(qrTimer); clearInterval(qrCdTimer); clearInterval(cdTimer); clearInterval(fgTimer) })

watch(tab, async (v) => {
  if (v !== 'qr') return
  await nextTick()
  if (!qrSession.value) {
    newQr()
  } else if (qrMode.value === 'real') {
    // 切回扫码页时容器已被重建，需要用同一个 state 重新挂载微信 iframe
    if (wxInfo.value) await renderWxLogin(wxInfo.value)
  } else if (qrContent.value) {
    drawQr(qrContent.value)
  } else {
    newQr()
  }
})
</script>

<style scoped>
.login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px;
  background: radial-gradient(circle at 30% 20%, #ffffff 0%, #f7f8fa 60%); }
.login-card { width: 400px; padding: 32px; border-radius: 14px; background: rgba(255,255,255,0.98);
  box-shadow: 0 20px 60px rgba(41,95,255,0.14); border: 1px solid #e8eeff; }
.login-card h1 { margin: 0; font-size: 22px; color: #1d2129; text-align: center; }
.sub { text-align: center; color: #8b98ad; font-size: 12px; margin: 6px 0 18px; letter-spacing: 2px; text-transform: uppercase; }
.login-tabs :deep(.el-tabs__item) { font-size: 14px; }
.foot { text-align: center; font-size: 13px; color: #6b7891; margin-top: 6px; }
.sms-row { display: flex; gap: 8px; width: 100%; }
.sms-row .el-input { flex: 1; }
.mb12 { margin-bottom: 12px; }

.qr-box { display: flex; flex-direction: column; align-items: center; padding: 6px 0 4px; }
.qr-frame { position: relative; padding: 10px; border: 1px solid #e8eeff; border-radius: 10px; background: #fff; }
.qr-frame canvas { display: block; }
.qr-frame-real { padding: 0; border: none; }
.wx-login-container { width: 300px; height: 400px; overflow: hidden; }
.wx-login-container :deep(iframe) { display: block; }
.qr-mask { position: absolute; inset: 10px; background: rgba(255,255,255,0.94); border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; font-size: 13px; color: #295FFF; }
.qr-mask .ok { font-size: 34px; color: #00b42a; }
.qr-mask .spin { font-size: 26px; animation: spin 1.4s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.qr-tip { margin: 12px 0 2px; font-size: 13px; color: #4e5969; }
.qr-tip.sub { font-size: 12px; color: #86909c; margin: 2px 0 14px; }
.third-row { width: 100%; margin-top: 6px; }
.divider { display: block; text-align: center; font-size: 12px; color: #c9cdd4; position: relative; margin-bottom: 10px; }
.divider::before, .divider::after { content: ''; position: absolute; top: 50%; width: 34%; height: 1px; background: #e5e6eb; }
.divider::before { left: 0; } .divider::after { right: 0; }
.third-btns { display: flex; justify-content: center; gap: 14px; }
</style>
