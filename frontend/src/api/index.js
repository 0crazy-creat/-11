import request from '@/utils/request'

/* =====================================================================
   统一 API 出口：同时提供
   1) 扁平函数（login/listContests/...）
   2) 命名空间对象（authApi/contestApi/challengeApi/awdApi/...）
   后端契约：POST /api/ctf/submit  body = { contestId, questionId, content, submitType }
   ===================================================================== */

// ===== 认证 =====
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const getProfile = () => request.get('/auth/me')
export const register = (data) => request.post('/auth/register', data)

export const authApi = {
  login,
  logout,
  register,
  me: getProfile,
  profile: getProfile,
  updateProfile: (data) => request.put('/auth/profile', data),
  changePassword: (data) => request.post('/auth/password', data),
  toggleMfa: (enabled) => request.post('/account/mfa', { enabled }),
  verifyMfa: (code) => request.post('/account/mfa/verify', { code }),
  emailCode: (email) => request.post('/account/email-code', null, { params: { email } }),
  resetPassword: (data) => request.post('/account/reset-password', data),
  forgotPassword: (data) => request.post('/account/forgot-password', data),
  qrConfirmInfo: (state) => request.get('/auth/qr-confirm/info', { params: { state } }),
  qrConfirm: (state) => request.post('/auth/qr-confirm/confirm', { state }),
  refresh: (refreshToken) => request.post('/auth/refresh', { refreshToken })
}

// ===== 赛事 =====
export const listContests = (params) => request.get('/contests', { params })
export const getContest = (id) => request.get(`/contests/${id}`)
export const joinContest = (id) => request.post(`/contests/${id}/join`)
export const createContest = (data) => request.post('/contests', data)
export const updateContest = (id, data) => request.put(`/contests/${id}`, data)
export const removeContest = (id) => request.delete(`/contests/${id}`)

export const contestApi = {
  list: listContests,
  detail: getContest,
  create: createContest,
  update: updateContest,
  remove: removeContest,
  join: joinContest,
  changeStatus: (id, data) => request.post(`/contests/${id}/status`, data),
  getByCode: (code) => request.get(`/contests/code/${code}`),
  bindQuestions: (id, data) => request.post(`/contests/${id}/questions`, data),
  getQuestions: (id) => request.get(`/contests/${id}/questions`),
  removeQuestion: (id, qid) => request.delete(`/contests/${id}/questions/${qid}`)
}

// ===== CTF 答题 =====
export const listChallenges = (contestId) => request.get(`/ctf/contests/${contestId}/challenges`)
export const listPlayChallenges = (contestId) => request.get(`/ctf/contests/${contestId}/challenges/play`)
export const getChallenge = (id) => request.get(`/ctf/challenges/${id}`)

/**
 * 提交 Flag/答案（统一入口）
 * 后端 SubmitRequest = { contestId, questionId, content, submitType, startTs, deviceId }
 *
 * 兼容两种调用方式：
 *   1) submitFlag({ contestId, questionId|challengeId, content|flag, submitType? })
 *   2) submitFlag(contestId, questionId, flag)
 */
export const submitFlag = (payloadOrContestId, questionId, flag) => {
  let body
  if (typeof payloadOrContestId === 'object' && payloadOrContestId !== null) {
    const p = payloadOrContestId
    body = {
      contestId: p.contestId,
      questionId: p.questionId ?? p.challengeId,
      content: p.content ?? p.flag,
      submitType: p.submitType || 'FLAG',
      startTs: p.startTs,
      deviceId: p.deviceId
    }
  } else {
    body = { contestId: payloadOrContestId, questionId, content: flag, submitType: 'FLAG' }
  }
  return request.post('/ctf/submit', body)
}

export const getScoreboard = (contestId) => request.get(`/ctf/contests/${contestId}/scoreboard`)
export const getSolveTrend = (contestId) => request.get(`/ctf/contests/${contestId}/trend`)
export const createChallenge = (data) => request.post('/ctf/challenges', data)
export const updateChallenge = (id, data) => request.put(`/ctf/challenges/${id}`, data)
export const removeChallenge = (id) => request.delete(`/ctf/challenges/${id}`)

export const challengeApi = {
  list: (arg) => listChallenges(typeof arg === 'object' && arg !== null ? arg.contestId : arg),
  playList: listPlayChallenges,
  detail: getChallenge,
  create: createChallenge,
  update: updateChallenge,
  remove: removeChallenge,
  submit: submitFlag,
  submitFlag: (data) => request.post('/ctf/submissions', data),
  solved: (contestId) => request.get(`/ctf/contests/${contestId}/solved`),
  mySubmissions: (contestId, params) => request.get(`/ctf/contests/${contestId}/submissions`, { params }),
  adminList: (contestId, params) => request.get(`/ctf/contests/${contestId}/challenges`, { params }),
  adminDetail: (contestId, challengeId) => request.get(`/ctf/contests/${contestId}/challenges/${challengeId}`),
  adminCreate: (contestId, data) => request.post(`/ctf/contests/${contestId}/challenges`, data),
  adminUpdate: (contestId, challengeId, data) => request.put(`/ctf/contests/${contestId}/challenges/${challengeId}`, data),
  adminRemove: (contestId, challengeId) => request.delete(`/ctf/contests/${contestId}/challenges/${challengeId}`)
}

export const rankApi = {
  list: (params) => getScoreboard(params?.contestId ?? params),
  myScore: (contestId) => request.get(`/ctf/contests/${contestId}/scoreboard/mine`),
  refresh: (contestId) => request.post(`/ctf/contests/${contestId}/scoreboard/refresh`)
}

// ===== AWD =====
// 说明：/awd/contests/{contestId}/* 由 AwdCompatController 提供只读聚合视图（大屏/看板用）
//       写操作与裁判控制走 /awd/rounds、/awd/targets、/awd/score 权威端点（见 api/awd.js）
export const getAwdOverview = (contestId) => request.get(`/awd/contests/${contestId}/overview`)
export const listAwdTeams = (contestId) => request.get(`/awd/contests/${contestId}/teams`)
export const getAwdScoreboard = (contestId) => request.get(`/awd/contests/${contestId}/scoreboard`)
export const getAwdRounds = (contestId) => request.get(`/awd/contests/${contestId}/rounds`)
export const getAwdAttackLog = (contestId, params) => request.get(`/awd/contests/${contestId}/attacks`, { params })
export const getTargets = (contestId) => request.get(`/awd/contests/${contestId}/targets`)

export const awdApi = {
  getContest: getAwdOverview,
  getLeaderboard: getAwdScoreboard,
  getScoreboard: getAwdScoreboard,
  getTargets,
  getRounds: getAwdRounds,
  getAttacks: getAwdAttackLog,
  listTeams: listAwdTeams,
  scoreLeaderboard: (params) => request.get('/awd/score/leaderboard', { params }),
  scoreTeam: (params) => request.get('/awd/score/team', { params }),
  scoreAttack: (data) => request.post('/awd/score/attack', data),
  scorePatch: (data) => request.post('/awd/score/patch', data),
  scoreRules: () => request.get('/awd/score/rules'),
  scoreRegister: (data) => request.post('/awd/score/register', data),
  scoreSettle: (data) => request.post('/awd/score/settle', data)
}

// ===== 战队 =====
export const teamApi = {
  // 后端真实路径：/teams（全局战队视图，GlobalTeamController）
  list: (params) => request.get('/teams', { params }),
  detail: (id) => request.get(`/teams/${id}`),
  remove: (id) => request.delete(`/teams/${id}`),
  // 赛事维度（ContestTeamController：/api/contests/{contestId}/teams）
  listByContest: (contestId, params) => request.get(`/contests/${contestId}/teams`, { params }),
  review: (contestId, teamId, status) => request.post(`/contests/${contestId}/teams/${teamId}/review`, null, { params: { status } }),
  members: (contestId, teamId) => request.get(`/contests/${contestId}/teams/${teamId}/members`),
  // CTF 赛事战队（CtfEnrollController：/api/ctf/contests/{contestId}/teams）
  getMyTeam: (contestId) => request.get(`/ctf/contests/${contestId}/teams/mine`),
  create: (contestId, data) => request.post(`/ctf/contests/${contestId}/teams`, data),
  join: (contestId, data) => request.post(`/ctf/contests/${contestId}/teams/join`, data),
  joinByCode: (contestId, data) => request.post(`/ctf/contests/${contestId}/teams/join-by-code`, data),
  quit: (contestId) => request.delete(`/ctf/contests/${contestId}/teams/quit`),
  generateInviteCode: (contestId, teamId) => request.post(`/ctf/contests/${contestId}/teams/${teamId}/invite-code`),
  refreshInviteCode: (contestId, teamId) => request.put(`/ctf/contests/${contestId}/teams/${teamId}/invite-code`),
  reviewCtf: (contestId, teamId, data) => request.post(`/ctf/contests/${contestId}/teams/${teamId}/review`, data)
}

// ===== 公告 =====
export const announceApi = {
  list: (params) => request.get('/announcements', { params }),
  create: (data) => request.post('/announcements', data),
  update: (id, data) => request.put(`/announcements/${id}`, data),
  remove: (id) => request.delete(`/announcements/${id}`)
}

// ===== 用户 =====
export const userApi = {
  page: (params) => request.get('/users', { params }),
  detail: (id) => request.get(`/users/${id}`),
  create: (data) => request.post('/users', data),
  update: (id, data) => request.put(`/users/${id}`, data),
  updateStatus: (id, data) => request.put(`/users/${id}/status`, data),
  remove: (id) => request.delete(`/users/${id}`),
  getRoles: (id) => request.get(`/users/${id}/roles`),
  resetPassword: (id, data) => request.post(`/users/${id}/password/reset`, data),
  batchImport: (formData) => request.post('/users/batch/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  batchExport: (params) => request.get('/users/batch/export', { params, responseType: 'blob' })
}

// ===== 角色 =====
export const roleApi = {
  list: () => request.get('/roles'),
  detail: (id) => request.get(`/roles/${id}`),
  create: (data) => request.post('/roles', data),
  update: (id, data) => request.put(`/roles/${id}`, data),
  remove: (id) => request.delete(`/roles/${id}`),
  permissions: () => request.get('/roles/permissions'),
  assignPermissions: (id, data) => request.put(`/roles/${id}/permissions`, data)
}

// ===== 通知 =====
export const notifyApi = {
  my: (params) => request.get('/notify/my', { params }),
  unreadCount: () => request.get('/notify/unread-count'),
  inbox: (params) => request.get('/notify/inbox', { params }),
  detail: (id) => request.get(`/notify/${id}`),
  read: (ids) => request.post('/notify/read', { ids }),
  readAll: () => request.post('/notify/read-all'),
  send: (data) => request.post('/notify/send', data)
}

// ===== 题目库 =====
export const questionApi = {
  page: (params) => request.get('/questions', { params }),
  detail: (id) => request.get(`/questions/${id}`),
  create: (data) => request.post('/questions', data),
  update: (id, data) => request.put(`/questions/${id}`, data),
  remove: (id) => request.delete(`/questions/${id}`),
  import: (data) => request.post('/questions/import', data)
}

// ===== 审计日志 =====
export const auditApi = {
  page: (params) => request.get('/audit-logs', { params }),
  detail: (id) => request.get(`/audit-logs/${id}`)
}

// ===== 归档 =====
export const archiveApi = {
  // 后端 ArchiveCmd 要求 filingNo 必填，必须带 body，否则 400
  create: (contestId, data) => request.post(`/archives/contest/${contestId}`, data),
  page: (params) => request.get('/archives', { params }),
  byContest: (contestId) => request.get(`/archives/contest/${contestId}`),
  detail: (id) => request.get(`/archives/${id}`),
  verify: (id) => request.get(`/archives/${id}/verify`)
}

// ===== 导出 =====
export const exportApi = {
  submissionsCsv: (params) => request.get('/export/submissions.csv', { params, responseType: 'blob' }),
  submissionsXlsx: (params) => request.get('/export/submissions.xlsx', { params, responseType: 'blob' }),
  writeupsCsv: (params) => request.get('/export/writeups.csv', { params, responseType: 'blob' })
}

// ===== 统计 =====
export const statsApi = {
  overview: (params) => request.get('/stats/overview', { params })
}

// ===== 态势感知（后端两种形态：path 变量 + query，这里统一用 query 形态）=====
export const getSituationOverview = (params) => request.get('/situation/overview', { params })
export const getSituationRanking = (params) => request.get('/situation/ranking', { params })
export const getTeamProfile = (teamId) => request.get(`/situation/teams/${teamId}`)
export const get3DData = (params) => request.get('/situation/3d', { params })
export const getTargetMonitor = (params) => request.get('/situation/monitor', { params })
export const getFlylines = (contestId, params) => request.get(`/situation/${contestId}/flylines`, { params })
export const getReplay = (contestId, params) => request.get(`/situation/${contestId}/replay`, { params })

export const situationApi = {
  overview: getSituationOverview,
  ranking: getSituationRanking,
  teamProfile: getTeamProfile,
  data3d: get3DData,
  monitor: getTargetMonitor,
  flylines: getFlylines,
  replay: getReplay
}

// ===== 手机号 / 扫码 / 第三方登录（需求 41 段）=====
export const sendSmsCode = (phone) => request.post('/auth/sms-code', null, { params: { phone } })
export const loginByPhone = (data) => request.post('/auth/login-phone', data)
export const loginByThird = (data) => request.post('/auth/login-third', data)
export const wechatQrcode = () => request.post('/auth/wechat/qrcode')
export const wechatStatus = (state) => request.get('/auth/wechat/status', { params: { state } })
export const wechatCallback = (data) => request.post('/auth/wechat/callback', data)
export const wechatExchange = (data) => request.post('/auth/wechat/exchange', data)

Object.assign(authApi, {
  sendSmsCode, loginByPhone, loginByThird,
  wechatQrcode, wechatStatus, wechatCallback, wechatExchange
})

// ===== 选手端 AWD 靶机（AwdTargetController：/awd/targets/mine*）=====
export const listMyTargets = (contestId) => request.get('/awd/targets/mine', { params: { contestId } })
export const startMyTarget = (contestId, questionId, roundNo) =>
  request.post('/awd/targets/mine/start', null, {
    params: { contestId, ...(questionId ? { questionId } : {}), ...(roundNo ? { roundNo } : {}) }
  })

// ===== 练习模式 =====
export const practiceApi = {
  challenges: (params) => request.get('/practice/challenges', { params }),
  challengeDetail: (id) => request.get(`/practice/challenges/${id}`),
  categories: () => request.get('/practice/categories')
}

// ===== 理论赛 =====
export const theoryApi = {
  paper: (contestId) => request.get(`/ctf/contests/${contestId}/theory/paper`),
  submit: (contestId, data) => request.post(`/ctf/contests/${contestId}/theory/submit`, data)
}


// ===== CTF 动态靶机（CtfTargetController：/ctf/contests/{cid}/targets/*）=====
export const getTargetStatus = (contestId, questionId) =>
  request.get(`/ctf/contests/${contestId}/targets`, { params: { questionId } })
export const startTarget = (contestId, questionId) =>
  request.post(`/ctf/contests/${contestId}/targets/start`, null, { params: { questionId } })
export const resetTarget = (contestId, questionId) =>
  request.post(`/ctf/contests/${contestId}/targets/reset`, null, { params: { questionId } })
export const destroyTarget = (contestId, questionId) =>
  request.post(`/ctf/contests/${contestId}/targets/destroy`, null, { params: { questionId } })
export const destroyAllTargets = (contestId) =>
  request.post(`/ctf/contests/${contestId}/targets/destroy-all`)

// ===== 题解社区 =====
export const listWriteups = (params) => request.get('/writeups', { params })
export const getWriteup = (id) => request.get(`/writeups/${id}`)
export const likeWriteup = (id) => request.post(`/writeups/${id}/like`)
export const submitWriteup = (data) => request.post('/writeups', data)
export const myWriteups = () => request.get('/writeups/mine')
export const deleteWriteup = (id) => request.delete(`/writeups/${id}`)
export const reviewWriteups = (params) => request.get('/writeups/review', { params })
export const reviewWriteup = (id, status, remark) =>
  request.post(`/writeups/${id}/review`, null, { params: { status, ...(remark ? { remark } : {}) } })

// ===== CFS 场景化渗透赛 =====
export const getCfsTopology = (contestId) => request.get(`/cfs/contests/${contestId}/topology`)
export const submitCfsFlag = (data) => request.post('/cfs/submissions', data)
export const getCfsInstance = (contestId) => request.get(`/cfs/contests/${contestId}/instance`)
export const createCfsTopology = (data) => request.post('/cfs/admin/topologies', data)
export const listCfsTopologies = (params) => request.get('/cfs/admin/topologies', { params })
export const getCfsTopologyDef = (id) => request.get(`/cfs/admin/topologies/${id}`)
export const getCfsTopologyLayers = (id) => request.get(`/cfs/admin/topologies/${id}/layers`)
export const getCfsTopologyNodes = (id) => request.get(`/cfs/admin/topologies/${id}/nodes`)
export const publishCfsTopology = (id) => request.post(`/cfs/admin/topologies/${id}/publish`)
export const startCfsContest = (contestId) => request.post(`/cfs/admin/contests/${contestId}/start`)

export const cfsApi = {
  topology: getCfsTopology,
  submit: submitCfsFlag,
  instance: getCfsInstance,
  createTopology: createCfsTopology,
  listTopologies: listCfsTopologies,
  topologyDef: getCfsTopologyDef,
  topologyLayers: getCfsTopologyLayers,
  topologyNodes: getCfsTopologyNodes,
  publish: publishCfsTopology,
  start: startCfsContest
}

// ===== AWDP 防御修补 =====
export const submitDefensePatch = (formData, onUploadProgress) =>
  request.post('/awd/defense/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
export const listMyDefenseSubmissions = (contestId, teamId) =>
  request.get('/awd/defense/list', { params: { contestId, ...(teamId ? { teamId } : {}) } })
export const getDefenseSubmission = (id) => request.get(`/awd/defense/${id}`)
export const listContestDefenseSubmissions = (contestId, status) =>
  request.get(`/awd/defense/contest/${contestId}`, { params: { ...(status ? { status } : {}) } })
export const markDefenseCompromised = (id) => request.post(`/awd/defense/${id}/compromise`)

export const defenseApi = {
  submit: submitDefensePatch,
  listMine: listMyDefenseSubmissions,
  detail: getDefenseSubmission,
  listByContest: listContestDefenseSubmissions,
  markCompromised: markDefenseCompromised
}

// ===== 场景实操赛（SCENARIO）=====
// 后端 ScenarioController 基路径是 /api/scenario（不是 /api/ctf/scenarios）
export const getScenarioArena = (contestId) =>
  request.get(`/scenario/contests/${contestId}/scenarios`)
export const submitScenarioFlag = (data) => request.post('/scenario/submissions', data)

export const provisionScenarioEnv = (data) => request.post('/scenario/env/provision', data)
export const destroyScenarioEnv = (params) => request.post('/scenario/env/destroy', null, { params })
export const getScenarioEnv = (params) => request.get('/scenario/env', { params })
export const listScenarioEnvs = (params) => request.get('/scenario/env/list', { params })

export const precheckScenarioContest = (contestId) =>
  request.get('/scenario/env/precheck', { params: { contestId } })
export const precheckScenarioOne = (scenarioId, contestId) =>
  request.get(`/scenario/env/precheck/${scenarioId}`, { params: { contestId } })

export const getScenarioDependencyTree = (params) =>
  request.get('/scenario/env/dependency/tree', { params })
export const checkScenarioDependency = (params) =>
  request.get('/scenario/env/dependency/check', { params })

export const scenarioApi = {
  arena: getScenarioArena,
  submit: submitScenarioFlag,
  provision: provisionScenarioEnv,
  destroy: destroyScenarioEnv,
  getEnv: getScenarioEnv,
  listEnvs: listScenarioEnvs,
  precheck: precheckScenarioContest,
  precheckOne: precheckScenarioOne,
  dependencyTree: getScenarioDependencyTree,
  checkDependency: checkScenarioDependency
}

// ===== LVP 轻量虚拟化（LvpController：/api/lvp/vms）=====
// 注意：后端为内存实现，服务重启后实例清空；生产可对接 docker-java / libvirt
export const lvpApi = {
  list: () => request.get('/lvp/vms'),
  detail: (id) => request.get(`/lvp/vms/${id}`),
  create: (data) => request.post('/lvp/vms', data),
  start: (id) => request.post(`/lvp/vms/${id}/start`),
  stop: (id) => request.post(`/lvp/vms/${id}/stop`),
  snapshot: (id, tag) => request.post(`/lvp/vms/${id}/snapshot`, null, { params: tag ? { tag } : {} }),
  destroy: (id) => request.delete(`/lvp/vms/${id}`),
  summary: () => request.get('/lvp/vms/summary')
}

// ===== AWD 轮次控制（AwdRoundControlController）=====
export const awdRoundApi = {
  begin: (contestId, roundNo) => request.post(`/awd/rounds/${contestId}/${roundNo}/begin`),
  running: (contestId, roundNo) => request.get(`/awd/rounds/${contestId}/${roundNo}/running`),
  settle: (contestId, roundNo) => request.post(`/awd/rounds/${contestId}/${roundNo}/settle`),
  control: (contestId, roundNo) => request.get(`/awd/rounds/${contestId}/${roundNo}/control`),
  interrupt: (contestId, roundNo, data) => request.post(`/awd/rounds/${contestId}/${roundNo}/interrupt`, data),
  resume: (contestId, roundNo) => request.post(`/awd/rounds/${contestId}/${roundNo}/resume`),
  interruptStatus: (contestId, roundNo) => request.get(`/awd/rounds/${contestId}/${roundNo}/interrupt-status`),
  dispute: (contestId, roundNo, data) => request.post(`/awd/rounds/${contestId}/${roundNo}/dispute`, data),
  listDisputes: (contestId, roundNo) => request.get(`/awd/rounds/${contestId}/${roundNo}/dispute`),
  arbitrate: (contestId, roundNo, data) => request.post(`/awd/rounds/${contestId}/${roundNo}/arbitrate`, data),
  nextStates: (contestId, roundNo) => request.get(`/awd/rounds/${contestId}/${roundNo}/next-states`),
  tick: () => request.post('/awd/rounds/tick')
}

// ===== 运维兼容层（OpsCompatController）=====
export const opsApi = {
  antiCheatEvents: (params) => request.get('/anti-cheat/events', { params }),
  trafficAnalysis: (params) => request.get('/traffic/analysis', { params }),
  theoryReport: (params) => request.get('/statistics/theory/report', { params }),
  classReport: (params) => request.get('/statistics/class/report', { params }),
  sla: (params) => request.get('/sla', { params }),
  archiveContest: (contestId) => request.post(`/contests/${contestId}/archive`),
  teacherStatus: () => request.get('/roles/teacher/status')
}

// ===== CTF 靶机管理（裁判端）=====
export const ctfTargetAdminApi = {
  list: (contestId, params) => request.get(`/ctf/contests/${contestId}/targets/admin/list`, { params }),
  forceDestroy: (contestId, containerId) => request.post(`/ctf/contests/${contestId}/targets/admin/force-destroy/${containerId}`)
}

// ===== AWD 靶机续期 =====
export const ctfTargetExtendApi = {
  extend: (contestId, data) => request.post(`/ctf/contests/${contestId}/targets/extend`, data)
}