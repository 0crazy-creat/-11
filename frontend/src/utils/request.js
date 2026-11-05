import axios from 'axios'
import { ElMessage } from 'element-plus'

// 延迟获取 router，避免 request -> router -> request 循环依赖
const gotoLogin = async () => {
  localStorage.removeItem('csp_token')
  localStorage.removeItem('csp_user')
  const { default: router } = await import('@/router')
  const current = router.currentRoute.value
  if (current.name !== 'login') {
    router.replace({ name: 'login', query: { redirect: current.fullPath } })
  }
}

const http = axios.create({
  baseURL: '/api',
  timeout: 120000
})

// 请求拦截：注入 token
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('csp_token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：解包统一响应体 Result<T>
http.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准响应（文件流等）直接返回
    if (res == null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code === 200 || res.code === 0) {
      return res.data
    }
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      gotoLogin()
      return Promise.reject(new Error(res.message || '未认证'))
    }
    ElMessage.error(res.message || '请求失败')
    // 关键：把后端业务错误包成「带 response 的 Error」，让调用方统一的
    // e.response.data.message 取值方式依然可用（否则各处只会拿到兜底文案）
    const bizErr = new Error(res.message || '请求失败')
    bizErr.code = res.code
    bizErr.response = { data: res }
    return Promise.reject(bizErr)
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      gotoLogin()
    }
    ElMessage.error(error?.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http