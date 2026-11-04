import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('csp_token') || '')
  const profile = ref(JSON.parse(localStorage.getItem('csp_user') || 'null'))
  const roles = computed(() => profile.value?.roles || [])
  const isAdmin = computed(() => roles.value.some(r => ['ADMIN', 'SUPER_ADMIN', 'PLATFORM_ADMIN'].includes(r)))

  function persist() {
    if (token.value) localStorage.setItem('csp_token', token.value)
    else localStorage.removeItem('csp_token')
    if (profile.value) localStorage.setItem('csp_user', JSON.stringify(profile.value))
    else localStorage.removeItem('csp_user')
  }

  async function login(payload) {
    const data = await authApi.login(payload)
    // 后端 LoginVO：accessToken / refreshToken / user / roles / permissions
    token.value = data.accessToken || data.token || ''
    if (data.refreshToken) localStorage.setItem('csp_refresh_token', data.refreshToken)
    // LoginVO 的 roles / permissions 在顶层，需要合并进 profile 供路由守卫与菜单使用
    const u = data.user || data.profile || {}
    profile.value = {
      ...u,
      roles: data.roles || u.roles || [],
      permissions: data.permissions || u.permissions || []
    }
    persist()
    return data
  }

  async function refresh() {
    if (!token.value) return null
    try {
      const data = await authApi.me()
      // /auth/me 同样返回 LoginVO 结构，取其中的 user
      const u2 = data?.user || data?.profile || data || {}
      profile.value = {
        ...u2,
        roles: data?.roles || u2.roles || [],
        permissions: data?.permissions || u2.permissions || []
      }
      if (data?.accessToken) token.value = data.accessToken
      persist()
    } catch (e) {
      // 只有明确的认证失败才登出，网络抖动不应清空登录态
      const msg = String(e?.message || '')
      const status = e?.response?.status
      if (status === 401 || msg.includes('登录已过期') || msg.includes('未认证')) {
        logout()
      }
    }
    return profile.value
  }

  function logout() {
    token.value = ''
    profile.value = null
    persist()
  }

  return { token, profile, roles, isAdmin, login, refresh, logout }
})