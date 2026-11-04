/* =====================================================================
   赛制注册表（六赛制唯一权威来源）
   —— 页面禁止再散落 `{CTF:'is-ctf', ...}[type]` 之类的硬编码判断。
   颜色遵循《竞赛平台视觉规范》：主色 #295FFF / 深色 #1b48e8 / 浅色 #e8eeff，
   状态色 #00b42a / #ff7d00 / #f53f3f / #c9cdd4，仅浅色主题。
   同时集中承载「角色 × 赛制」协作矩阵，供前端菜单/按钮按角色渲染。
   ===================================================================== */

/** @type {Record<string, {label:string, short:string, color:string, bg:string, route:string, dynamicTargets:boolean, hint:string}>} */
export const CONTEST_TYPES = {
  CTF:      { label: 'CTF 解题赛',     short: 'CTF',  color: '#295FFF', bg: '#e8eeff', route: '/portal/contest/:id',  dynamicTargets: false, hint: '解题夺旗，按分值动态衰减排名' },
  AWD:      { label: 'AWD 攻防赛',     short: 'AWD',  color: '#f53f3f', bg: '#ffece8', route: '/portal/awd/:id',      dynamicTargets: true,  hint: '回合制攻防，攻陷他队靶机得分、守住本队失分' },
  AWDP:     { label: 'AWDP 增强攻防',  short: 'AWDP', color: '#ff7d00', bg: '#fff3e8', route: '/portal/awd/:id',      dynamicTargets: true,  hint: '攻防 + 漏洞修补，修补加分、失守扣分' },
  THEORY:   { label: '理论赛',         short: '理论', color: '#00b42a', bg: '#e8ffea', route: '/portal/theory/:id',   dynamicTargets: false, hint: '闭卷理论答题，交卷即判分' },
  SCENARIO: { label: '场景实操赛',     short: '场景', color: '#7b61ff', bg: '#f0ecff', route: '/portal/scenario/:id', dynamicTargets: false, hint: '真实业务场景逐级闯关，按关卡累计得分' },
  CFS:      { label: 'CFS 场景化渗透', short: 'CFS',  color: '#1b48e8', bg: '#e8eeff', route: '/portal/cfs/:id',      dynamicTargets: true,  hint: '多题库混合场景渗透，按攻击链阶段计分' }
}

/** 赛制编码顺序（大厅 tab 使用） */
export const CONTEST_TYPE_CODES = ['CTF', 'AWD', 'AWDP', 'THEORY', 'SCENARIO', 'CFS']

/** 安全取赛制元信息，未知类型回落为中性灰 */
export function getContestType(type) {
  const t = String(type || '').toUpperCase()
  return CONTEST_TYPES[t] || {
    label: type || '未知赛制', short: type || '—', color: '#c9cdd4', bg: '#f2f3f5',
    route: '/portal/contest/:id', dynamicTargets: false, hint: '暂未支持的赛制'
  }
}
export const contestTypeMeta = getContestType

/** 是否动态靶机（决定是否出现「启动容器」按钮） */
export function isDynamicTargets(type) { return getContestType(type).dynamicTargets }

/** 攻防大屏赛制（只有 AWD/AWDP 走 /awd/screen） */
export const AWD_SCREEN_TYPES = ['AWD', 'AWDP']

/**
 * 统一入口路由（以赛制注册表 route 为唯一权威）：
 *  - AWD/AWDP → /awd/screen/:id
 *  - CTF → /portal/contest/:id
 *  - THEORY → /portal/theory/:id
 *  - SCENARIO → /portal/scenario/:id
 *  - CFS → /portal/cfs/:id（CfsArena 分层拓扑，不走 AWD 大屏）
 */
export function contestEntryRoute(c) {
  if (!c) return '/portal'
  const id = c.id ?? c.contestId
  return getContestType(c.type).route.replace(':id', String(id))
}

/* =====================================================================
   角色 × 赛制 协作矩阵
   ===================================================================== */
export const ROLE_LABELS = {
  ADMIN: '管理员', SUPER_ADMIN: '超级管理员', PLATFORM_ADMIN: '平台管理员',
  JUDGE: '裁判', TEACHER: '教师', PLAYER: '选手', OBSERVER: '观察者'
}

/** 能力集合：solve 做题 / targets 查看靶机 / review 审核题解 / manage 管理赛事 / screen 大屏 */
export const ROLE_CAPS = {
  ADMIN:          { solve: true,  targets: 'all',    review: true,  manage: 'all',   screen: true },
  SUPER_ADMIN:    { solve: true,  targets: 'all',    review: true,  manage: 'all',   screen: true },
  PLATFORM_ADMIN: { solve: true,  targets: 'all',    review: true,  manage: 'all',   screen: true },
  JUDGE:          { solve: true,  targets: 'all-rd', review: true,  manage: 'part',  screen: true },
  TEACHER:        { solve: true,  targets: 'own',    review: true,  manage: 'own',   screen: true },
  PLAYER:         { solve: 'join', targets: 'team',  review: false, manage: false,   screen: false },
  OBSERVER:       { solve: false, targets: 'screen', review: false, manage: false,   screen: true }
}

/** 由后端 roles 数组推导主角色 */
export function roleOf(roles) {
  const list = Array.isArray(roles) ? roles : (roles ? [roles] : [])
  const order = ['SUPER_ADMIN', 'ADMIN', 'PLATFORM_ADMIN', 'JUDGE', 'TEACHER', 'OBSERVER', 'PLAYER']
  return order.find(r => list.includes(r)) || 'PLAYER'
}
export function roleLabelOf(role) { return ROLE_LABELS[role] || role || '选手' }

/**
 * 角色能否执行某能力。type 目前对所有赛制一致（矩阵按角色定义），
 * 预留 type 参数以便后续按赛制细化。
 */
export function canDo(role, cap, _type) {
  const caps = ROLE_CAPS[role] || ROLE_CAPS.PLAYER
  const v = caps[cap]
  return v === true || (typeof v === 'string' && v !== 'false')
}

export const CONTEST_STATUS = {
  DRAFT: '筹备中', PENDING: '未开始', SIGNUP: '报名中', RUNNING: '进行中', FINISHED: '已结束', ARCHIVED: '已归档'
}
export function contestStatusMeta(s) {
  const text = CONTEST_STATUS[String(s || '').toUpperCase()] || (s || '—')
  const color = { RUNNING: '#00b42a', SIGNUP: '#ff7d00' }[String(s || '').toUpperCase()] || '#c9cdd4'
  return { text, color }
}

export default CONTEST_TYPES