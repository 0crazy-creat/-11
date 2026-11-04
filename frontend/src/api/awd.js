import request from '@/utils/request';

/**
 * AWD 轮次管理（对齐后端真实端点）
 *
 * 后端存在两套入口：
 *   1) AwdRoundController          /api/awd/rounds
 *        GET    /api/awd/rounds?contestId=         轮次列表
 *        GET    /api/awd/rounds/{contestId}/current 当前进行中轮次
 *        GET    /api/awd/rounds/{contestId}/{roundNo} 轮次详情
 *        POST   /api/awd/rounds                      创建/编排轮次
 *        POST   /api/awd/rounds/{contestId}/{roundNo}/start|check|finish|abort
 *   2) AwdCompatController         /api/awd/contests/{contestId}/rounds
 *        只读列表（兼容旧页面）
 *
 * 本文件统一走 AwdRoundController 的权威路径，避免 /rounds/current 等 404。
 */

/**
 * 获取比赛轮次列表
 * 后端：GET /api/awd/rounds?contestId={contestId}
 */
export function getRounds(contestId) {
  return request({
    url: '/awd/rounds',
    method: 'get',
    params: { contestId }
  });
}

/**
 * 获取当前轮次
 * 后端：GET /api/awd/rounds/{contestId}/current
 * 无进行中轮次时返回 null（拦截器解包后为 null）
 */
export function getCurrentRound(contestId) {
  return request({
    url: `/awd/rounds/${contestId}/current`,
    method: 'get'
  });
}

/**
 * 开始新一轮（创建轮次）
 * 后端：POST /api/awd/rounds，body = CreateRoundRequest
 */
export function startNewRound(data) {
  return request({
    url: '/awd/rounds',
    method: 'post',
    data
  });
}

/**
 * 结束当前轮次（结算）
 * 后端：POST /api/awd/rounds/{contestId}/{roundNo}/finish
 */
export function endRound(contestId, roundNo) {
  return request({
    url: `/awd/rounds/${contestId}/${roundNo}/finish`,
    method: 'post'
  });
}

/**
 * 获取轮次详情
 * 后端：GET /api/awd/rounds/{contestId}/{roundNo}
 */
export function getRoundDetail(contestId, roundNo) {
  return request({
    url: `/awd/rounds/${contestId}/${roundNo}`,
    method: 'get'
  });
}

/**
 * 开局（PENDING → RUNNING）
 * 后端：POST /api/awd/rounds/{contestId}/{roundNo}/start
 */
export function startRound(contestId, roundNo) {
  return request({
    url: `/awd/rounds/${contestId}/${roundNo}/start`,
    method: 'post'
  });
}

/**
 * 进入检查阶段（RUNNING → CHECKING）
 * 后端：POST /api/awd/rounds/{contestId}/{roundNo}/check
 */
export function checkRound(contestId, roundNo) {
  return request({
    url: `/awd/rounds/${contestId}/${roundNo}/check`,
    method: 'post'
  });
}

/**
 * 中止轮次
 * 后端：POST /api/awd/rounds/{contestId}/{roundNo}/abort
 */
export function abortRound(contestId, roundNo) {
  return request({
    url: `/awd/rounds/${contestId}/${roundNo}/abort`,
    method: 'post'
  });
}