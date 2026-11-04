import request from '@/utils/request'

/**
 * 获取题目列表（练习模式）
 */
export function getChallenges(params) {
  return request({
    url: '/api/ctf/challenges',
    method: 'get',
    params
  })
}

/**
 * 获取题目详情
 */
export function getChallengeDetail(id) {
  return request({
    url: `/api/ctf/challenges/${id}`,
    method: 'get'
  })
}

/**
 * 提交 Flag（练习模式）
 */
export function submitFlag(challengeId, flag) {
  return request({
    url: `/api/ctf/challenges/${challengeId}/submit`,
    method: 'post',
    data: { flag }
  })
}

/**
 * 启动题目容器
 */
export function startContainer(challengeId) {
  return request({
    url: `/api/ctf/challenges/${challengeId}/container/start`,
    method: 'post'
  })
}

/**
 * 停止题目容器
 */
export function stopContainer(challengeId) {
  return request({
    url: `/api/ctf/challenges/${challengeId}/container/stop`,
    method: 'post'
  })
}

/**
 * 获取容器状态
 */
export function getContainerStatus(challengeId) {
  return request({
    url: `/api/ctf/challenges/${challengeId}/container/status`,
    method: 'get'
  })
}
