import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由分层：
 *  /login                 登录（公开）
 *  /portal/**             选手端（赛事大厅 / 我的比赛 / 题解社区 / 答题）—— 所有登录用户
 *  /console/**            管理端（赛事/题目/战队/用户/公告/归档/通知）—— 需要管理类权限
 *  /situation /rank ...   公开大屏（免登录）
 *  /awd/**                AWD 大屏
 */

const portalRoutes = [
  { path: '', name: 'portal-home', component: () => import('@/views/portal/ContestHall.vue'), meta: { title: '赛事大厅' } },
  { path: 'announcements', name: 'portal-announcements', component: () => import('@/views/portal/Announcements.vue'), meta: { title: '公告栏' } },
  { path: 'awd', name: 'portal-awd-list', component: () => import('@/views/portal/AwdContestList.vue'), meta: { title: 'AWD 比赛平台' } },
  { path: 'practice', name: 'portal-practice', component: () => import('@/views/practice/ChallengePractice.vue'), meta: { title: '题目练习' } },
  { path: 'ctf', name: 'portal-ctf-list', component: () => import('@/views/portal/CtfContestList.vue'), meta: { title: 'CTF 比赛' } },
  { path: 'my-team', name: 'portal-my-team', component: () => import('@/views/portal/MyTeam.vue'), meta: { title: '战队管理' } },
  { path: 'profile', name: 'portal-profile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人信息' } },
  { path: 'my', name: 'portal-my', component: () => import('@/views/portal/MyContests.vue'), meta: { title: '我的比赛' } },
  { path: 'writeups', name: 'portal-writeups', component: () => import('@/views/portal/WriteupCommunity.vue'), meta: { title: '题解社区' } },
  { path: 'notifications', name: 'portal-notifications', component: () => import('@/views/portal/Notifications.vue'), meta: { title: '消息通知' } },
  { path: 'contest/:contestId', name: 'portal-play', component: () => import('@/views/portal/PlayArena.vue'), props: true, meta: { title: '赛事答题' } },
  // 六赛制统一入口：注册表 route 全量落地
  { path: 'theory/:contestId', name: 'portal-theory', component: () => import('@/views/portal/PlayArena.vue'), props: true, meta: { title: '理论赛' } },
  { path: 'scenario/:contestId', name: 'portal-scenario', component: () => import('@/views/portal/ScenarioArena.vue'), props: true, meta: { title: '场景实操赛' } },
  { path: 'cfs/:contestId', name: 'portal-cfs', component: () => import('@/views/portal/CfsArena.vue'), props: true, meta: { title: 'CFS 场景化渗透' } },
  { path: 'awd/:contestId', name: 'portal-awd', component: () => import('@/views/awd/AwdArena.vue'), props: true, meta: { title: 'AWD 攻防赛' } },
  { path: 'awdp/:contestId', name: 'portal-awdp', component: () => import('@/views/awd/AwdArena.vue'), props: true, meta: { title: 'AWDP 攻防赛' } },
  // Phase 1 新增：AWD 流量取证
  { path: 'awd/:contestId/traffic', name: 'portal-awd-traffic', component: () => import('@/views/awd/TrafficCapturePanel.vue'), props: true, meta: { title: 'AWD 流量取证' } }
]

const consoleRoutes = [
  { path: '', name: 'console-home', component: () => import('@/views/console/Dashboard.vue'), meta: { title: '控制台', perm: null } },
  { path: 'contests', name: 'admin-contests', component: () => import('@/views/contest/ContestList.vue'), meta: { title: '赛事管理', perm: 'contest:create' } },
  { path: 'contests/:id', name: 'admin-contest-detail', component: () => import('@/views/contest/ContestDetail.vue'), props: true, meta: { title: '赛事详情', perm: 'contest:update' } },
  { path: 'challenges', name: 'admin-challenges', component: () => import('@/views/contest/ChallengeManage.vue'), meta: { title: '题库管理', perm: 'question:create' } },
  { path: 'teams', name: 'admin-teams', component: () => import('@/views/contest/TeamList.vue'), meta: { title: '战队管理', perm: 'team:manage' } },
  { path: 'users', name: 'admin-users', component: () => import('@/views/console/UserList.vue'), meta: { title: '用户管理', perm: 'user:manage' } },
  { path: 'announcements', name: 'admin-announcements', component: () => import('@/views/console/Announcement.vue'), meta: { title: '公告管理', perm: 'announcement:manage' } },
  { path: 'audit', name: 'admin-audit', component: () => import('@/views/console/AuditLog.vue'), meta: { title: '审计日志', perm: 'audit:view' } },
  { path: 'notifications', name: 'admin-notifications', component: () => import('@/views/console/NotificationManage.vue'), meta: { title: '通知管理', perm: 'announcement:manage' } },
  // Phase 1 新增路由
  { path: 'abac', name: 'admin-abac', component: () => import('@/views/admin/AbacPolicyManagement.vue'), meta: { title: 'ABAC 策略管理', perm: 'abac:view' } },
  { path: 'anti-cheat/:contestId', name: 'admin-anti-cheat', component: () => import('@/views/admin/AntiCheatDashboard.vue'), props: true, meta: { title: '反作弊监控', perm: 'contest:control' } }
]

const routes = [
  { path: '/login', name: 'login', component: () => import('@/views/auth/Login.vue'), meta: { public: true } },
  { path: '/qr-confirm', name: 'qr-confirm', component: () => import('@/views/auth/QrConfirm.vue'), meta: { public: true, title: '扫码确认' } },
  { path: '/', redirect: '/portal' },

  // ===== 选手端 =====
  {
    path: '/portal',
    component: () => import('@/layouts/PortalLayout.vue'),
    children: portalRoutes
  },
  // 兼容旧链接 /play/:contestId
  { path: '/play/:contestId', redirect: to => `/portal/contest/${to.params.contestId}` },

  // ===== 管理端 =====
  {
    path: '/console',
    component: () => import('@/layouts/ConsoleLayout.vue'),
    children: consoleRoutes
  },

  // ===== 公开大屏 =====
  { path: '/awd-screen/:contestId', name: 'awd-screen', component: () => import('@/views/awd/AwdScreen.vue'), props: true, meta: { public: true } },
  // 注册表 AWD/AWDP 统一入口（/awd/screen/:id）
  { path: '/awd/screen/:contestId', name: 'awd-screen-alias', component: () => import('@/views/awd/AwdScreen.vue'), props: true, meta: { public: true } },
  { path: '/awdp/defense', name: 'awdp-defense-noid', component: () => import('@/views/awd/AwdpDefense.vue') },
  { path: '/situation/:contestId?', name: 'situation', component: () => import('@/views/situation/Situation3D.vue'), props: true, meta: { public: true } },
  { path: '/rank/:contestId?', name: 'ranking', component: () => import('@/views/situation/Ranking.vue'), props: true, meta: { public: true } },
  { path: '/team/:teamId', name: 'team-home', component: () => import('@/views/situation/TeamHome.vue'), props: true, meta: { public: true } },

  { path: '/:pathMatch(.*)*', redirect: '/portal' }
]

const router = createRouter({ history: createWebHistory(), routes })

/** 解析本地登录态 */
function currentUser() {
  try {
    const raw = localStorage.getItem('csp_user') || localStorage.getItem('csp_profile')
    return raw ? JSON.parse(raw) : null
  } catch (e) { return null }
}

router.beforeEach((to) => {
  if (to.meta.public) return true

  const token = localStorage.getItem('csp_token')
  if (!token) return { name: 'login', query: { redirect: to.fullPath } }

  // 管理端需要对应权限
  const perm = to.meta.perm
  if (perm) {
    const u = currentUser()
    const perms = u?.permissions || []
    const roles = u?.roles || []
    const isAdmin = roles.includes('ADMIN') || perms.includes('*')
    if (!isAdmin && !perms.includes(perm)) {
      return { path: '/portal' }
    }
  }
  return true
})

export default router
