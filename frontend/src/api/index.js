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
  profile: getProfile
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
  join: joinContest
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
  submit: submitFlag
}

export const rankApi = {
  list: (params) => getScoreboard(params?.contestId ?? params)
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
  listTeams: listAwdTeams
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
  members: (contestId, teamId) => request.get(`/contests/${contestId}/teams/${teamId}/members`)
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
  updateStatus: (id, data) => request.put(`/users/${id}/status`, data),
  remove: (id) => request.delete(`/users/${id}`)
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
export const getCfsTopologyDef = (id) => request.get(`/cfs/admin/topologies/${id}`)
export const publishCfsTopology = (id) => request.post(`/cfs/admin/topologies/${id}/publish`)
export const startCfsContest = (contestId) => request.post(`/cfs/admin/contests/${contestId}/start`)

export const cfsApi = {
  topology: getCfsTopology,
  submit: submitCfsFlag,
  instance: getCfsInstance,
  createTopology: createCfsTopology,
  topologyDef: getCfsTopologyDef,
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