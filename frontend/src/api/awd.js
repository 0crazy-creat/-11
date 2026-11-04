import request from '@/utils/request';

/**
 * 获取比赛轮次列表
 */
export function getRounds(contestId) {
  return request({
    url: `/api/awd/contests/${contestId}/rounds`,
    method: 'get'
  });
}

/**
 * 获取当前轮次
 */
export function getCurrentRound(contestId) {
  return request({
    url: `/api/awd/contests/${contestId}/rounds/current`,
    method: 'get'
  });
}

/**
 * 开始新一轮
 */
export function startNewRound(contestId) {
  return request({
    url: `/api/awd/contests/${contestId}/rounds/start`,
    method: 'post'
  });
}

/**
 * 结束当前轮次
 */
export function endRound(contestId, roundId) {
  return request({
    url: `/api/awd/contests/${contestId}/rounds/${roundId}/end`,
    method: 'post'
  });
}

/**
 * 获取轮次详情
 */
export function getRoundDetail(contestId, roundId) {
  return request({
    url: `/api/awd/contests/${contestId}/rounds/${roundId}`,
    method: 'get'
  });
}
